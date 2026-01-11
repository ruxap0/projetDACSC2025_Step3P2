package hepl.DACSC.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigServer {
    private final Properties properties;

    public ConfigServer() throws IOException {
        properties = new Properties();

        try(InputStream in = getClass().getClassLoader().getResourceAsStream("configserver.properties")){
            if(in == null){
                throw new IOException("File 'configserver.properties' not found");
            }

            properties.load(in);
        }
    }

    public int getPort(){
        return Integer.parseInt(properties.getProperty("PORT_SERVER"));
    }

    public String getDBUser(){
        return properties.getProperty("DB_USER");
    }

    public String getDBPasswd(){
        return properties.getProperty("DB_PASSWD");
    }

    public String getDBLink(){
        return properties.getProperty("LINK_DB");
    }

    public String getDBDriver(){
        return properties.getProperty("DB_DRIVER");
    }
}
