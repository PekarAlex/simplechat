package org.ompekar.chat;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashSet;

public class SessionListener implements HttpSessionListener {


    @Override
    public void sessionCreated(HttpSessionEvent event) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {

        HttpSession session = event.getSession();
        User currentUser = (User) session.getAttribute("user");
        HashSet<User> userSet = (HashSet<User>) session.getServletContext().getAttribute("users");
        synchronized (userSet) {
            userSet.remove(currentUser);
        }
    }


}