package webtv.zebra;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class ZebraList extends SiteNode 
{
    public static final String site = "http://www.zebra.lt";
    public static final String url = "http://www.zebra.lt/lt/video/kanalai";
    public static final String ref = "http://www.zebra.lt/lt/video";

    private TreeSet<String> ids = new TreeSet<String>();
    
    public ZebraList(DefaultTreeModel model){
        super(model, "Zebra");
    }   
    @Override
    protected String getURL() { return url; }
    @Override
    protected String getReferer() { return ref; }
    @Override
    public boolean isLeaf(){ return false; }        
    
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
        findChannels(doc);
    }

    static final String channels = "<div class=\"arrow arrow_selected\"></div>Kanalai</a>";
    static final String chanPref = "/lt/video/kanalas/";
    static final String chanBegin = " href=\""+chanPref;
    static final String chanFinish = "\">";
    static final String nameFinish = "</a>";
    
    private void findChannels(StringBuilder doc) {
        
        int i = doc.indexOf(channels);
        if (i<0) return;
        i = doc.indexOf(chanBegin, i+channels.length());
        while (i>0) {
            int chanStart = i+chanBegin.length();
            int chanEnd = doc.indexOf(chanFinish, chanStart);
            if (chanEnd<0) break;
            String link = site+chanPref+doc.substring(chanStart, chanEnd);
            int nameStart = chanEnd+chanFinish.length();
            int nameEnd = doc.indexOf(nameFinish, nameStart);
            if (nameEnd<0) break;
            String name = doc.substring(nameStart, nameEnd);
            if (!ids.contains(link)) {
                add(new Channel(model, name, link, cookie));
                ids.add(link);
            }
            i = doc.indexOf(chanBegin, nameEnd+nameFinish.length());
        }
    }    
}
