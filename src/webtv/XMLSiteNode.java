/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marius
 */
public abstract class XMLSiteNode extends SiteNode 
{
    protected static SAXParserFactory factory;
    protected SAXParser parser;
    protected DefaultHandler handler;

    public XMLSiteNode(DefaultTreeModel model, String title){
        super(model, title);
        if (factory==null) factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    protected void parseDoc(InputStream is) throws IOException, Exception {
        parser.parse(is, handler);
    }
        
}
