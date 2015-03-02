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
                configuration.configure();

                //hibernate connection parameters
                for (Map.Entry<Object, Object> e : JettyServer.appProperties.entrySet()) {
                    String key = (String) e.getKey();
                    if (key.startsWith("connection.")){
                        String value = (String) e.getValue();
                        configuration.setProperty(key,value);
                    }
                }

                StandardServiceRegistryBuilder builder= new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory= configuration.buildSessionFactory(builder.build());
                //depricated
                //ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
                //sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Throwable ex) {
                System.err.println("Failed to create sessionFactory object." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }


}


