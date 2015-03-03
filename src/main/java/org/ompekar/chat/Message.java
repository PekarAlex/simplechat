package org.ompekar.chat;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

@Entity
@Table(name = "Messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true)
    private Long id = -1L;

    @Column(name = "created")
    @Type(type = "timestamp")
    private Date created;

    @Lob
    @Column(name = "message")
    private String message;

    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "recipient_uuid")
    private User recipient;

    public Message() {
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static boolean addMessage(Message message) {
        SessionFactory factory = HibernateFactory.getInstance();
        Session session = factory.openSession();
        Transaction tx = null;
        boolean messageCreated = false;
        try {
            tx = session.beginTransaction();
            session.save(message);
            tx.commit();
            messageCreated = true;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return messageCreated;
    }

    public void setRecipientToMessage(){
        if (message.charAt(0)=='@'){

            //find user
            int firstSpace=message.indexOf(' ');
            String recipientText=message.substring(1,firstSpace);
            recipient=User.getUser(recipientText);
            message=message.substring(firstSpace+1);
        }
    }

    public static LinkedList<Message> getMessages(int Count) {
        LinkedList<Message> messageLinkedList = null;
        SessionFactory factory = HibernateFactory.getInstance();
        Session session = factory.openSession();

        try {
            String hql = "FROM Message ORDER BY created DESC";
            org.hibernate.Query query = session.createQuery(hql);
            query.setMaxResults(Count);
            messageLinkedList = new LinkedList(query.list());
            Collections.reverse(messageLinkedList);
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        if (messageLinkedList == null) messageLinkedList = new LinkedList<Message>();
        return messageLinkedList;
    }

    public String createMessageTitle(){
        String username=getUser().getUserName();
        User recipient=getRecipient();
        if (recipient!=null) username=username+" -> "+recipient.getUserName();
        return username;
    }

}
