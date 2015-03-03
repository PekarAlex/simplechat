package org.ompekar.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties properties;

    private Config(){
    }

    public static synchronized Properties getInstance() {
        if (properties==null) {
            properties = new Properties();

            try {
                InputStream is = new FileInputStream("chat.properties");
                //load the config file into properties format
                properties.load(is);
            }  catch (IOException e) {
            throw new RuntimeException(e);
            }
        }
        return properties;
    }
}
