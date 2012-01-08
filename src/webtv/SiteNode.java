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
import webtv.tv3webtv.SiteMapNode;

/**
 *
 * @author marius
 */
public abstract class SiteNode extends CommonNode
{
    protected static String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/532.8 (KHTML, like Gecko) Chrome/4.0.295.0 Safari/532.8";
    protected String title;
    protected String status;
    protected boolean busy = false, refreshing = false;

    public SiteNode(DefaultTreeModel model, String title){
        super(model);
        this.title = title;
    }

    @Override
    public void refresh(){
        new Thread(new Runnable(){
            public void run(){ reload(); }
        }).start();
    }

    protected abstract String getURL();
    protected abstract String getReferer();
    protected abstract void parseDoc(InputStream is) throws IOException, Exception;

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
                parseDoc(is);
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
            status = ex.getMessage();
            Logger.getLogger(SiteMapNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(SiteMapNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        busy = false;
    }

    @Override
    public String toString() {
        if (status==null) return title;
        else return title+" ["+status+"]";
    }
}
