package org.ompekar.chat;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import static java.lang.Integer.max;

class ChatServlet extends HttpServlet {
    private  HashSet<User> activeUsers;
    private  LinkedList<Message> lastMessages;
    private static VelocityEngine velocityEngine;

    public class DispatchMessagesListTimerTask extends TimerTask {

        @Override
        public void run() {
            synchronized(lastMessages) {
                if (lastMessages != null && lastMessages.size() > 10) {
                    Iterator<Message> itr = lastMessages.iterator();
                    long current = new Date().getTime();
                    while (itr.hasNext()) {
                        Message message = itr.next();
                        if (current - message.getCreated().getTime() > 600000L) itr.remove();
                        if (lastMessages.size() <= 10) break;

                    }
                }
            }
        }
    }

    public class SessionListener implements HttpSessionListener {

        @Override
        public void sessionCreated(HttpSessionEvent event) {
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent event) {

            HttpSession session = event.getSession();
            User currentUser = (User) session.getAttribute("user");
            synchronized (activeUsers) {
                activeUsers.remove(currentUser);
            }
        }
    }

    private static VelocityEngine getInstanceOfVelocityEngine() {
        if (velocityEngine == null) {
            Properties properties = new Properties();
            properties.setProperty("runtime.log.logsystem.log4j.logger", "Velocity");
            properties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
            properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,classpath");
            properties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            properties.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
            properties.setProperty("file.resource.loader.path", "src/main/webapp");
            properties.setProperty("userdirective","org.ompekar.chat.VelocityEscapeDirective");

            velocityEngine = new VelocityEngine(properties);
            velocityEngine.init();
        }
        return velocityEngine;
    }

    protected void handleTemplate(String templateName, HttpServletResponse response, VelocityContext context) throws IOException {
        VelocityEngine ve = getInstanceOfVelocityEngine();
        Template template = null;
        try {
            template = ve.getTemplate(templateName);
        } catch (Exception e) {
            System.out.println("Error " + e);
        }

        /* now render the template into a StringWriter */
        if (template != null) {
            PrintWriter writer = response.getWriter();
            template.merge(context, writer);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";

        User currentUser = (User) request.getSession().getAttribute("user");
        if (uri.equals("/") && currentUser != null) {
            showChatPage(request, response);
        } else if (uri.equals("/") && currentUser == null) {
            forwardToLoginRegisterPage(request, response);
        } else if (uri.endsWith("/loginregister")) {
            showLoginRegisterPage(request, response);
        } else if (uri.endsWith("/login")) {
            loginUser(request, response);
        } else if (uri.endsWith("/register")) {
            registerUser(request, response, base);
        } else if (uri.endsWith("/confirmationinfo")) {
            showConfirmationPage(request, response);
        } else if (uri.endsWith("/confirm")) {
            confirmUser(request, response);
        } else if (uri.endsWith("/postmessage")) {
            postMessage(request, currentUser);
            showMessages(request, response, currentUser);
        } else if (uri.endsWith("/updateusers")) {
            showUsers(response, currentUser);
        } else if (uri.endsWith("/logout")) {
            logoutUser(request, response);
        }
    }

    private void forwardToLoginRegisterPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher rd = request.getRequestDispatcher("/loginregister");
        rd.forward(request, response);
    }

    private void showChatPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().setAttribute("lastMessage", null);
        //Chat window
        VelocityContext context = new VelocityContext();
        context.put("title","Chat");
        handleTemplate("chat.html", response, context);
        //if user has no any http activity for 60 sec
        request.getSession().setMaxInactiveInterval(60);
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect("/");
    }

    private void showUsers(HttpServletResponse response, User currentUser) throws IOException {
        if (currentUser != null) {
            synchronized (activeUsers) {
                VelocityContext context = new VelocityContext();
                context.put("activeUsers", activeUsers);
                handleTemplate("chatusers.html", response, context);
            }
        }
    }

    private void showMessages(HttpServletRequest request, HttpServletResponse response,  User currentUser) throws IOException {
        if (currentUser != null) {
            Message lastMessage = (Message) request.getSession().getAttribute("lastMessage");
            synchronized (lastMessages) {
                request.getSession().setAttribute("lastMessage", lastMessages.getLast());
                int firstIndex;
                int lastIndex = max(lastMessages.size() - 1, 0);
                if (lastMessage == null) {
                    //set firstIndex for 10 messages
                    firstIndex = max(0, lastIndex - 9);
                } else {
                    firstIndex = lastMessages.indexOf(lastMessage) + 1;
                }
                if (firstIndex <= lastIndex) {//some messages should be posted
                    VelocityContext context = new VelocityContext();
                    context.put("messages", lastMessages);
                    context.put("firstIndex", firstIndex);
                    context.put("lastIndex", lastIndex);
                    context.put("username", currentUser.getUserName());
                    handleTemplate("chatnewmessage.html", response, context);
                }
            }
        }
    }

    private void postMessage(HttpServletRequest request, User currentUser) {
        if (currentUser != null) {
            String messagetext = request.getParameter("message");
            if ((messagetext != null) && (!messagetext.isEmpty())) {
                Message message = new Message();
                message.setMessage(messagetext);
                message.setUser(currentUser);
                message.setCreated(new java.util.Date());
                message.setRecipientToMessage();
                if (Message.addMessage(message)) {

                    //block this for other Thread
                    synchronized (lastMessages) {
                        lastMessages.add(message);
                    }
                }
            }
        }
    }

    private void confirmUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uuid = request.getParameter("id");
        if (uuid != null) {
            User user = User.confirmUser(uuid);
            if (user != null) {
                //all is ok, user validated
                synchronized (activeUsers) {
                    activeUsers.add(user);
                }
                request.getSession().setAttribute("user", user);
                response.sendRedirect("/");
            }
        }
    }

    private void showConfirmationPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("username", request.getParameter("login"));
        context.put("email", request.getParameter("email"));
        context.put("title","Confirmation info");
        handleTemplate("confirmationinfo.html", response, context);
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response, String base) throws ServletException, IOException {
        String username = request.getParameter("login");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        if (username != null && password != null && email != null) {
            User user = User.getUser(username);
            if (user == null) {
                //don't have any user with selected name
                user = new User();
                user.setUserName(username);
                user.setPassword(password);
                user.setEmail(email);
                user.setConfirmed(false);
                if (User.addUser(user)) {
                    VelocityEngine ve = getInstanceOfVelocityEngine();
                    Template emailTemplate = ve.getTemplate("confirmationletter.html");
                    VelocityContext context = new VelocityContext();
                    context.put("username", user.getUserName());
                    context.put("confirmationlink", base.concat("confirm"));
                    context.put("confirmationUUID", user.getUuid());
                    context.put("title","Confirmation letter");

                    /* now render the template into a StringWriter */
                    StringWriter writer = new StringWriter();
                    emailTemplate.merge(context, writer);
                    SendEmail.sendEmail(user.getEmail(), writer.toString());
                    RequestDispatcher rd = request.getRequestDispatcher("confirmationinfo");
                    rd.forward(request, response);
                }
            } else {

                //user with this name already registred
                request.setAttribute("registerinfo", "User with same name already exist");
                RequestDispatcher rd = request.getRequestDispatcher("/");
                rd.forward(request, response);
            }
        }
    }

    private void loginUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("login");
        String password = request.getParameter("password");
        if ((username != null) && (password != null)) {
            User user = User.getUser(username);
            if ((user != null) && (user.getPassword().equals(password))) {

                //user is ok,
                if (!user.getConfirmed()) {
                    request.setAttribute("logininfo", "user not confirmed");
                    RequestDispatcher rd = request.getRequestDispatcher("/");
                    rd.forward(request, response);
                } else {
                    synchronized (activeUsers) {
                        activeUsers.add(user);
                    }
                    request.getSession().setAttribute("user", user);
                    response.sendRedirect("/");
                }
            } else {

                //user login or pass error
                request.setAttribute("logininfo", "login or password error");
                RequestDispatcher rd = request.getRequestDispatcher("/");
                rd.forward(request, response);
            }
        }
    }

    private void showLoginRegisterPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("logininfo", request.getAttribute("logininfo"));
        context.put("registerinfo", request.getAttribute("registerinfo"));
        context.put("title","Chat login &amp; registration page");
        handleTemplate("loginregister.html", response, context);
    }





    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Logger log = Logger.getLogger("ChatLogger");

        //hibernate init
        HibernateFactory.getInstance();
        getInstanceOfVelocityEngine();
        log.info("Velocity Engine started");

        activeUsers = new HashSet<User>();
        lastMessages = Message.getMessages(10);

        //Create inner class object
        ChatServlet.DispatchMessagesListTimerTask messagesDispatcher=this.new DispatchMessagesListTimerTask();

        //running timer task as daemon thread
        Timer timer = new Timer(true);
        long delay=Long.valueOf(Config.getInstance().getProperty("dispatcher.delay","6000000"));
        timer.scheduleAtFixedRate(messagesDispatcher, delay, delay);
        log.info("Messages dispatcher started");
    }
}
