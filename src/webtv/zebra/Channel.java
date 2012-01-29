package webtv.zebra;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class Channel extends SiteNode 
{
    static final String defaultUrl = "http://www.zebra.lt/lt/video/kanalai//naujausi";
    String url = defaultUrl;
    String link = null;
    private TreeSet<String> ids = new TreeSet<String>();

    public Channel(DefaultTreeModel model, String title, String link, String cookie) {
        super(model, title);
        this.link = link;
        this.cookie = cookie;
    }

    public Channel(DefaultTreeModel model, String title, String link, String cookie, String url) {
        super(model, title);
        this.url = url;
        this.link = link;
        this.cookie = cookie;
    }    
    
    @Override
    protected String getURL() { return url; }
    @Override
    protected String getReferer() { return link; }
    @Override
    protected void prepare(HttpURLConnection con) { 
        con.addRequestProperty("X-Requested-With", "XMLHttpRequest");
        if (cookie != null)
            con.addRequestProperty("Cookie", cookie);
    }
    @Override
    public boolean isLeaf(){ return false; }    

    @Override
    protected void reload() {
        if (busy) return;
        busy = true;
        status = "loading";
        repaintChangeAndStructure();
        try {
            //System.out.println("Sending request");
            HttpURLConnection con = (HttpURLConnection) new URL(link).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", agent);
            con.setRequestProperty("Referer", ZebraList.site);
            con.setDoInput(true);
            con.setDoOutput(true);            
            con.connect();
            InputStream is = con.getInputStream();
            cookie = con.getHeaderField("Set-Cookie");
            int len = con.getContentLength();
            if (len<0) len = Integer.MAX_VALUE;
            is.skip(len);
            is.close();
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
        }
        busy = false;
        super.reload();
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
        findShows(doc);
    }

    boolean extra = false;
    private void findShows(StringBuilder doc) {
        String durBegin = "<span class=\"time\">&nbsp;";
        String durFinish = "&nbsp;";
        String linkBegin = "<a class=\"title\" href=\"";
        String linkFinish = "\">";
        String nameFinish = "</a>";
        int i = doc.indexOf(durBegin);
        while (i>0) {
            int durStart = i+durBegin.length();
            int durEnd = doc.indexOf(durFinish, durStart);
            if (durEnd<0) break;
            String duration = doc.substring(durStart, durEnd);
            i = doc.indexOf(linkBegin, durEnd+durFinish.length());
            if (i<0) break;
            int linkStart = i+linkBegin.length();
            int linkEnd = doc.indexOf(linkFinish, linkStart);
            if (linkEnd<0) break;
            String page = ZebraList.site+doc.substring(linkStart, linkEnd);
            int nameStart = linkEnd+linkFinish.length();
            int nameEnd = doc.indexOf(nameFinish, nameStart);
            if (nameEnd<0) break;
            String name = doc.substring(nameStart, nameEnd);
            if (!ids.contains(page)) {
                add(new Show(model, name+" "+duration, page, cookie));
                ids.add(page);
            }
            i = doc.indexOf(durBegin, nameEnd+nameFinish.length());
        }
        if (defaultUrl.equals(url) && !extra) {
            extra = true;
            add(new Channel(model, "Populiariausi", link, cookie,
                    "http://www.zebra.lt/lt/video/kanalai//populiariausi"));
            add(new Channel(model, "Geriausi", link, cookie,
                    "http://www.zebra.lt/lt/video/kanalai//geriausi"));
        }
    }
    
}
