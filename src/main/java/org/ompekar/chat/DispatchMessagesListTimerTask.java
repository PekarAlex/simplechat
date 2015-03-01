package org.ompekar.chat;


import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;


public class DispatchMessagesListTimerTask extends TimerTask {


    @Override
    public void run() {
        synchronized(ChatVelocityServlet.lastMessages) {
            if ((ChatVelocityServlet.lastMessages != null) && (ChatVelocityServlet.lastMessages.size() > 10)) {
                Iterator<Message> itr = ChatVelocityServlet.lastMessages.iterator();
                long current = new Date().getTime();
                while (itr.hasNext()) {
                    Message message = itr.next();
                    if (current - message.getCreated().getTime() > 600000L) itr.remove();
                    if (ChatVelocityServlet.lastMessages.size() <= 10) break;

                }
            }
        }
    }
}
