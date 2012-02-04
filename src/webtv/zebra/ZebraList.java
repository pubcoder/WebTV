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
        super(model, "Zebra.lt");
    }   
    @Override
    public boolean isLeaf(){ return false; }        
    
    static final String chanPref = "/lt/video/kanalas/";
    static final String chanBegin = " href=\"" + chanPref;
    static final String chanFinish = "\">";

    @Override
    protected void doReload() 
    {        
        StringBuilder d = web.getDoc(url, ref);
        if (d == null) { status = web.getStatus(); return ; }
        int i = web.find("<div class=\"arrow arrow_selected\"></div>Kanalai</a>");
        if (i<0) return;
        String link = web.findNext(chanBegin, chanFinish);
        while (link != null) {
            link = site+chanPref+link;
            web.skipLastPostfix();
            String name = web.findNext("</a>");
            if (!ids.contains(link)) {
                add(new Channel(model, name, link, web.getCookie()));
                ids.add(link);
            }
            web.skipLastPostfix();
            link = web.findNext(chanBegin, chanFinish);
        }
        status = null;
    }
}
