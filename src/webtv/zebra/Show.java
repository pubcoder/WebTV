package webtv.zebra;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import webtv.Product;
import webtv.WGetTool;

/**
 *
 * @author marius
 */
public class Show extends Product
{
    String url, link;

    Show(DefaultTreeModel model, String name, String url, String cookie) {
        super(model, name, new WGetTool());
        this.url = url;
        this.cookie = cookie;
        refresh();
    }
      
    @Override
    protected String getURL() { return url; }
    @Override
    protected String getReferer() { return url; }
    @Override
    protected void prepare(HttpURLConnection con) { 
        if (cookie != null)
            con.addRequestProperty("Cookie", cookie);
    }    
    @Override
    public void download() { 
        if (link!=null)
            tool.download(link, path); 
    }
    
    @Override
    protected void parseDoc(InputStream is, int length) 
            throws IOException, Exception 
    {       
        StringBuilder doc = new StringBuilder();
        if (length<0) length = 4096;
        byte data[] = new byte[length];
        while (true){
            int got = is.read(data, 0, length);
            if (got<0) break;
            doc.append(new String(data, 0, got));
        }
        findLink(doc);
    }

    static final String linkPref="http://mano.zebra.lt/video/zebra_video/";
    static final String linkBegin="http:\\/\\/mano.zebra.lt\\/video\\/zebra_video\\/";
    static final String linkFinish = "\"";
    boolean adult = false;
    
    private void findLink(StringBuilder doc) {
        int i = doc.indexOf(linkBegin);
        while (i>0) {        
            int linkStart = i+linkBegin.length();
            int linkEnd = doc.indexOf(linkFinish, linkStart);
            if (linkEnd<0) break;
            link = linkPref+doc.substring(linkStart, linkEnd);
            i = doc.indexOf(linkBegin, linkEnd+linkFinish.length());
        }
        if (link == null && !adult) {
            setAdultCookie();
        }
    }

    private void setAdultCookie() {
        try {
            String content = "enter_adult=1&remember_adult=1";
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", agent);
            con.setRequestProperty("Referer", url);
            con.setRequestProperty("Content-Length", ""+content.length());
            con.setRequestProperty("Origin", ZebraList.site);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Cookie", cookie);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            OutputStream os = con.getOutputStream();
            os.write(content.getBytes());
            os.flush();
            os.close();
            InputStream is = con.getInputStream();
            cookie = con.getHeaderField("Set-Cookie");
            int len = con.getContentLength();
            parseDoc(is, len);
            is.close();
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
