package org.ompekar.chat;

import org.apache.log4j.Logger;
import javax.mail.Message;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

public class SendEmail {
    public static void sendEmail(String address, String text) {
        Properties props = new Properties();

        //auth parameters for smtp
        final String username = Config.getInstance().getProperty("mailauth.user");
        final String password = Config.getInstance().getProperty("mailauth.password");

        //smtp server parameters
        for (Map.Entry<Object, Object> e : Config.getInstance().entrySet()) {
                String key = (String) e.getKey();
                if (key.startsWith("mail.")){
                    String value = (String) e.getValue();
                    props.put(key,value);
                }
        }
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@ompekar.org"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(address));
            message.setSubject("Chat::New User Confirmation");
            message.setContent(text, "text/html; charset=utf-8");
            Transport.send(message);
            Logger log = Logger.getLogger("ChatLogger");
            log.info(String.format("Confirmation email sended to %s",address));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
