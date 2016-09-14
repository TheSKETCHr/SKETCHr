package com.theironyard.controllers;

import com.theironyard.entities.Comment;
import com.theironyard.entities.Drawing;
import com.theironyard.entities.User;
import com.theironyard.services.CommentRepository;
import com.theironyard.services.DrawingRepository;
import com.theironyard.services.UserRepository;
import com.theironyard.utils.PasswordStorage;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ericweidman on 4/3/16.
 */

@RestController
public class SKETCHrController {
    @Autowired
    UserRepository users;
    @Autowired
    DrawingRepository drawings;
    @Autowired
    CommentRepository comments;

    Server dbui = null;


    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();
    }

    @PreDestroy
    public void destroy() {
        dbui.stop();
    }


    @RequestMapping(path = "/create-user", method = RequestMethod.POST)
    public String createUser(@RequestBody User newUser) throws Exception {
        User user = users.findByUserName(newUser.getUserName());
        if (user == null) {
            user = new User(newUser.getUserName(), PasswordStorage.createHash(newUser.getPasswordHash()));
            users.save(user);
            System.out.println("User added!");
            return null;
        } else {
            throw new Exception("Username already taken.");
        }

    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(@RequestBody User assumedUser, HttpSession session) throws Exception {
        User user = users.findByUserName(assumedUser.getUserName());
        if (user == null) {
            throw new Exception("Username does not exist.");
        }
        else if (!PasswordStorage.verifyPassword(assumedUser.getPasswordHash(), user.getPasswordHash())) {
            //Compares the assumedUsers password hash to the one in the DB.
            throw new Exception("Invalid password!");
        }
        else {
            session.setAttribute("userName", user.getUserName());
            return user.getUserName();

        }
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public Drawing drawingUp(String imgName, String canvasIMG, HttpSession session) throws Exception {
        String userName = (String) session.getAttribute("userName");
        if(userName == null){
            throw new Exception("You must be logged in to upload photos.");
        }
        User user = users.findByUserName(userName);
        Drawing newDrawing = new Drawing(imgName, canvasIMG, user);

        drawings.save(newDrawing);
        return null;
    }

    @RequestMapping(path = "/photo/{id}", method = RequestMethod.GET)
    public Drawing getDrawing(@PathVariable("id") int id) {
        Drawing drawing = drawings.findOne(id);
        return drawing;

    }

    @RequestMapping(path = "/photo/{id}", method = RequestMethod.DELETE)
    public void  deleteDrawing(@PathVariable("id") int id) {
        List<Comment> commentsById = comments.findAllByDrawingId(id);
        comments.delete(commentsById);
        drawings.delete(id);
    }
    @RequestMapping(path = "/comment/{id}", method = RequestMethod.DELETE)
    public void deleteComment(@PathVariable("id") int id){
        comments.delete(id);
    }

    @RequestMapping(path ="/user-photos", method = RequestMethod.GET)
    public List<Drawing> userDrawings(HttpSession session){
        String userName = (String) session.getAttribute("userName");
        User user = users.findByUserName(userName);
        List<Drawing> userDrawings = drawings.findByUser(user);

        return userDrawings;
    }

    @RequestMapping(path = "/gallery", method = RequestMethod.GET)
    public List<Drawing> allDrawings() {
        List<Drawing> allDrawings = (List<Drawing>) drawings.findAll();
          return allDrawings;
    }


    @RequestMapping(path = "/photo/{id}", method = RequestMethod.PUT)
    public Drawing editDrawing(@PathVariable("id") int id, @RequestBody Drawing drawing) {
        Drawing oldDrawing = drawings.findOne(id);
        oldDrawing = drawing;
        drawings.save(drawing);
        return drawing;
    }

    @RequestMapping(path = "/comment/{id}", method = RequestMethod.PUT)
    public Comment editComment(@PathVariable("id") int id, String comment){
        Comment saveComment = comments.findOne(id);
        saveComment.setComment(comment);
        comments.save(saveComment);
        return saveComment;
    }

    @RequestMapping(path = "/add-comment/{id}", method = RequestMethod.POST)
    public Comment addComment(String theComment, @PathVariable int id, HttpSession session){
         String userName = (String) session.getAttribute("userName");
         Drawing drawing = drawings.findOne(id);
         Comment comment = new Comment(drawing, theComment, userName);
         comments.save(comment);
        return null;
    }

     @RequestMapping(path = "/get-comments/{id}", method = RequestMethod.GET)
     public List<Comment> getComments(@PathVariable("id") int id){
         List<Comment> comment1 = comments.findAllByDrawingId(id);
         List<Comment> passThese = new ArrayList();
         for (Comment comment : comment1){
             Comment temp = new Comment(comment.getId(), comment.getComment(), comment.getUserName());
             passThese.add(temp);
         }
         return passThese;

     }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session) {
        session.invalidate();
    }


//    @RequestMapping(path = "/user-photos", method = RequestMethod.GET)
//    public List<Drawing> userDrawing(HttpSession session){
//        String userName = (String) session.getAttribute("userName");
//        User user = users.findByUserName(userName);
//        List<Drawing> allInfo = drawings.findByUser(user);
//        List<Drawing> passThis =  new ArrayList<>();
//        for (Drawing drawing : allInfo){
//            User user1 = new User(user.getId(), user.getUserName());
//            Drawing temp = new Drawing(drawing.getId(), drawing.getPicName(), drawing.getFileName(), user1);
//            passThis.add(temp);
//        }
//             return passThis;
//
//    }
//    @RequestMapping(path = "/upload", method = RequestMethod.POST)
//    public Drawing stringUp(@RequestBody String drawing, HttpSession session) throws Exception {
//        String userName = (String) session.getAttribute("userName");
//        if (userName == null) {
//            throw new Exception("You must be logged in to upload photos.");
//        }
//        User user = users.findByUserName(userName);
//        Drawing newDrawing = new Drawing(drawing, user);
//        drawings.save(newDrawing);
//        return null;
//    }

}
