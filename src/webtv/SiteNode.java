/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeSet;
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
public abstract class SiteNode extends CommonNode {
    private static String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/532.8 (KHTML, like Gecko) Chrome/4.0.295.0 Safari/532.8";
    protected static SAXParserFactory factory;
    protected SAXParser parser;
    protected String title, id;
    protected String status;
    protected boolean busy = false, refreshing = false;
    protected TreeSet<String> ids = new TreeSet<String>();

    public SiteNode(DefaultTreeModel model, String title, String id){
        super(model);
        this.title = title;
        this.id = id;

        if (factory==null) factory = SAXParserFactory.newInstance();
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void refresh(){
        new Thread(new Runnable(){
            public void run(){ reload(); }
        }).start();
    }

    protected abstract String getURL();
    protected abstract String getReferer();
    protected abstract DefaultHandler getHandler();

    protected void reload() {
        if (busy) return;
        busy = true;
        status = "loading";
        repaintChangeAndStructure();
        try {
            //System.out.println("Sending request");
            HttpURLConnection con = (HttpURLConnection) new URL(getURL()).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", agent);
            con.setRequestProperty("Referer", getReferer());
/*
            con.setRequestProperty("Accept", accept);
            con.setRequestProperty("Accept-Language", acceptLang);
            con.setRequestProperty("Accept-Encoding", acceptEnc);
            con.setRequestProperty("Accept-Charset", acceptCharset);
            con.setRequestProperty("Keep-Alive", keepAlive);
            con.setRequestProperty("Connection", connection);
            con.setRequestProperty("cookie", cookie);*/
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            int res = con.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                parser.parse(is, getHandler());
                is.close();
                status = null;
            } else {
                status = con.getResponseMessage();
                System.err.println(status);
            }
            //System.out.println("Done request, got "+this.getChildCount()+" children");
            refreshing = true;
            repaintChangeAndStructure();
        } catch (IOException ex) {
            Logger.getLogger(SiteMapNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(SiteMapNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        busy = false;
    }

    @Override
    public String toString() {
        if (status==null) return title;
        else return title+" ["+status+"]";
/*
        if (status==null) return getClass().getSimpleName()+" "+title;
        else return getClass().getSimpleName()+" "+title+" ["+status+"]";
 */
    }
}
