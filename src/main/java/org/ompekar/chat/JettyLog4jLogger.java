package org.ompekar.chat;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class JettyLog4jLogger implements org.eclipse.jetty.util.log.Logger {
    private Logger logger;

    public JettyLog4jLogger() {
        this.logger = Logger.getLogger("Jetty");
    }

    public JettyLog4jLogger(Logger logger) {
        this.logger = logger;
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void setDebugEnabled(boolean enabled) {
        logger.setLevel(enabled ? Level.DEBUG : Level.INFO);
    }

    public void info(String s, Object... args) {
        logger.info(format(s, args));
    }

    public void info(String s, Throwable t) {
        logger.info(s, t);
    }

    public void info(Throwable t) {
        logger.info("", t);
    }


    public void debug(String s, long l) {
        logger.debug(s);
    }

    public void debug(String s, Object... args) {
        logger.debug(format(s, args));
    }

    public void debug(String msg, Throwable th) {
        logger.debug(msg, th);
    }

    public void debug(Throwable t) {
        logger.debug("", t);
    }

    public void warn(String s, Object... args) {
        logger.warn(format(s, args));
    }

    public void warn(String msg, Throwable th) {
        logger.warn(msg, th);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(Throwable t) {
        logger.warn("", t);
    }

    public void ignore(Throwable throwable) {
        logger.info("Ignored", throwable);
    }

    public org.eclipse.jetty.util.log.Logger getLogger(String name) {
        return new JettyLog4jLogger(Logger.getLogger(name));
    }

    public String getName() {
        return logger.getName();
    }

    private static String format(String s, Object... args) {
        // {} text {} text ...
        String result = "";

        int i = 0;
        for (String text : s.split("\\{\\}")) {
            result += text;
            if (args.length > i) result += args[i];
            i++;
        }

        return result;
    }
}