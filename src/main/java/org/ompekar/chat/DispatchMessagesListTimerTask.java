package org.ompekar.chat;


import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;


public class DispatchMessagesListTimerTask extends TimerTask {


    @Override
    public void run() {
        synchronized(ChatServlet.lastMessages) {
            if ((ChatServlet.lastMessages != null) && (ChatServlet.lastMessages.size() > 10)) {
                Iterator<Message> itr = ChatServlet.lastMessages.iterator();
                long current = new Date().getTime();
                while (itr.hasNext()) {
                    Message message = itr.next();
                    if (current - message.getCreated().getTime() > 600000L) itr.remove();
                    if (ChatServlet.lastMessages.size() <= 10) break;

                }
            }
        }
    }
}
