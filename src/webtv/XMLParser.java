package webtv;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marius
 */
public class XMLParser
{
    protected static SAXParserFactory factory;
    protected SAXParser parser;

    public XMLParser()
    {
        if (factory==null) factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public void parse(InputStream is, DefaultHandler handler) 
            throws IOException, Exception 
    {
        parser.parse(is, handler);
    }
        
}
