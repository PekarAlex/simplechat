package org.ompekar.chat;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer {

    public static void main(String[] args) throws Exception {

        initLoggers();
        Logger log = Logger.getLogger(JettyServer.class);

        WebAppContext webAppContext = getWebAppContext();
        Server server = new Server(Integer.valueOf(Config.getInstance().getProperty("jetty.port", "8080")));
        server.setHandler(webAppContext);
        server.start();
        log.info("Start Jetty Server");
        server.join();
    }

    private static void initLoggers() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.ompekar.chat.JettyLog4jLogger");
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d [%t] %p %c %x - %m%n")));
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("Jetty").setLevel(Level.WARN);
        Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.WARN);
        Logger.getLogger("Velocity").setLevel(Level.WARN);
    }

    private static WebAppContext getWebAppContext() {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setSessionHandler(new SessionHandler(new HashSessionManager()));
        webAppContext.setResourceBase("src/main/webapp");
        webAppContext.setWelcomeFiles(new String[]{"loginregister.html"});
        webAppContext.setAttribute("welcomeServlets", true);

        DefaultServlet dS = new DefaultServlet();
        for (String ext : "js css png jpg ico".split(" "))
            webAppContext.addServlet(new ServletHolder(dS), "*." + ext);

        ChatServlet chatServlet=new ChatServlet();

        //Create inner class object
        ChatServlet.SessionListener sessionListener=chatServlet.new SessionListener();
        webAppContext.addServlet(new ServletHolder(chatServlet), "/");
        webAppContext.addEventListener(sessionListener);
        return webAppContext;
    }
}

