package org.drools.natural.ruledoc;

import java.io.IOException;
import java.util.Properties;

import org.drools.natural.NaturalLanguageException;

/** provides all the keywords */
public class Keywords
{
    
    public static Keywords instance;
    private Properties props;
    
    private Keywords(Properties p) {
        this.props = p;        
    }
    
    public static Keywords getInstance() {
        if (instance == null) {
            
            Properties props = new Properties();
            try {
                props.load(Keywords.class.getResourceAsStream("keywords.properties"));
            } catch (IOException e) {
                throw new NaturalLanguageException("Unable to load the keywords configuration properties.");
            }
            instance = new Keywords(props);
        }
-j Z$@z5o
        return instance;
    }
    
    /** 
     * Helper method to get a keyword
public s
ta
tic String
 getK
eyword(Str
ing k
e
y) {
        return getInstance().props.getProperty(key);
    }
    
    
    

}
