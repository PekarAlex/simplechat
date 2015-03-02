package org.ompekar.chat;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.*;
import org.hibernate.Query;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "Users")
public class User {


    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "username", unique = true)
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "confirmed")
    private Boolean confirmed;

    public User() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static User getUser(String userName) {
        SessionFactory factory = HibernateFactory.getInstance();
        Session session = factory.openSession();
        User user = null;
        try {

            String hql = "FROM User WHERE username = :username";
            Query query = session.createQuery(hql);
            query.setParameter("username", userName);
            user = (User) query.uniqueResult();

        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return user;
    }

    public static User confirmUser(String uuid) {
        SessionFactory factory = HibernateFactory.getInstance();
        Session session = factory.openSession();
        Transaction tx = null;
        User user = null;
        try {
            tx = session.beginTransaction();

            String hql = "FROM User WHERE uuid = :uuid";
            Query query = session.createQuery(hql);
            query.setParameter("uuid", uuid);
            user = (User) query.uniqueResult();
            user.setConfirmed(true);
            session.save(user);
            tx.commit();


        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return user;
    }


    public static boolean addUser(User user) {
        SessionFactory factory = HibernateFactory.getInstance();
        Session session = factory.openSession();
        Transaction tx = null;
        boolean userCreated = false;
        try {
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
            userCreated = true;
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return userCreated;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!email.equals(user.email)) return false;
        if (!password.equals(user.password)) return false;
        if (!userName.equals(user.userName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(userName);
        hcb.append(password);
        hcb.append(email);
        return hcb.toHashCode();
    }
}
