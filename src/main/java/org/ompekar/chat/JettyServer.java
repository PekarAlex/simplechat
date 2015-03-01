package org.ompekar.chat;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class JettyServer {
    public static Properties appProperties;

    public static void main(String[] args) throws Exception {

        appProperties = new Properties();

        InputStream is = new FileInputStream("chatproperties.xml");
        //load the xml file into properties format
        appProperties.loadFromXML(is);


        initLoggers();
        Logger log = Logger.getLogger(JettyServer.class);


        WebAppContext webAppContext = getWebAppContext();

        Server server = new Server(80);
        server.setHandler(webAppContext);


        TimerTask timerTask = new DispatchMessagesListTimerTask();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        long delay=Long.valueOf(appProperties.getProperty("dispatcher.delay","6000000"));
        timer.scheduleAtFixedRate(timerTask, delay, delay);
        log.info("Messages dispatcher started");


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
        webAppContext.addServlet(new ServletHolder(new ChatVelocityServlet()), "/");
        webAppContext.addEventListener(new SessionListener());
        return webAppContext;
    }
}

