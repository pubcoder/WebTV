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
    static final String defaultUrl = "http://zebra.15min.lt/lt/video/kanalai//naujausi";
    String url = defaultUrl;
    String link = null;
    private TreeSet<String> ids = new TreeSet<String>();

    public Channel(DefaultTreeModel model, String title, String link, String cookie) {
        super(model, title);
        this.link = link;
        web.setCookie(cookie);
    }

    public Channel(DefaultTreeModel model, String title, String link, String cookie, String url) {
        super(model, title);
        this.url = url;
        this.link = link;
        web.setCookie(cookie);
    }    
    
    @Override
    public boolean isLeaf(){ return false; }    

    @Override
    protected void doReload() {
        try {
            web.setRequestedWith(null);
            InputStream is = web.getStream(link, ZebraList.site);
            int len = web.getContentLength();
            if (len<0) len = Integer.MAX_VALUE;
            is.skip(len);
            is.close();
            web.setRequestedWith("XMLHttpRequest");
            String doc = web.getDoc(url, link);
            findShows(doc);
            status = null;
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(Channel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean extra = false;
    private void findShows(String doc) {
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
                add(new Show(model, name+" "+duration, page, web.getCookie()));
                ids.add(page);
            }
            i = doc.indexOf(durBegin, nameEnd+nameFinish.length());
        }
        if (defaultUrl.equals(url) && !extra) {
            extra = true;
            add(new Channel(model, "Populiariausi", link, web.getCookie(),
                    "http://zebra.15min.lt/lt/video/kanalai//populiariausi"));
            add(new Channel(model, "Geriausi", link, web.getCookie(),
                    "http://zebra.15min.lt/lt/video/kanalai//geriausi"));
        }
    }
    
}
