package org.ompekar.chat;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Map;

public class HibernateFactory {
    private static SessionFactory sessionFactory;

    private HibernateFactory() {
    }

    public static synchronized SessionFactory getInstance() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                //configuration.configure("hibernate.cfg.xml");
                //configuration.configure();

                //hibernate connection parameters
                for (Map.Entry<Object, Object> e : Config.getInstance().entrySet()) {
                    String key = (String) e.getKey();
                    if (key.startsWith("connection.") || key.startsWith("hibernate.")){
                        String value = (String) e.getValue();
                        configuration.setProperty(key, value);
                    }
                }
                configuration.setProperty("hbm2ddl.auto", Config.getInstance().getProperty("hbm2ddl.auto",""));
                configuration.setProperty("dialect", Config.getInstance().getProperty("dialect",""));




                configuration.addAnnotatedClass(org.ompekar.chat.User.class);
                configuration.addAnnotatedClass(org.ompekar.chat.Message.class);

                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (Throwable ex) {
                System.err.println("Failed to create sessionFactory object." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }
}


