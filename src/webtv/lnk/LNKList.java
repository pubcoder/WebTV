package webtv.lnk;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class LNKList extends SiteNode 
{
    public static final String url = "http://lnk.lnkgo.lt/video-kategorija/";
    public static final String ref = "http://www.lnk.lt/";
    private TreeSet<String> ids = new TreeSet<String>();
    
    public LNKList(DefaultTreeModel model)
    {
        super(model, "LNK Go.lt");
    }

    @Override
    public boolean isLeaf(){ return false; }    
    
    static final String urlPrefix = "http://lnk.lnkgo.lt/video-kategorija/";
    static final String linkBegin = "<a href=\"/video-kategorija/";
    static final String linkEnd = "\"";
    
    @Override
    protected void doReload() {
        String d = web.getDoc(url, ref);
        if (d == null) {status = web.getStatus(); return ; }
        String link = web.findFirst(linkBegin, linkEnd);
        while (link != null) {
            web.skipLastPostfix();
            link = urlPrefix+link;
            String name = web.findNext(">","<");
            if (name == null) break;
            web.skipLastPostfix();            
            if (name.length()>0) {
                name = name.replaceAll("&nbsp;", " ").trim();
                if (!ids.contains(link)) {
                    add(new Category(model, name, link, url));
                    ids.add(link);
                }
            }
            link = web.findNext(linkBegin, linkEnd);
        }
        status = null;
    }
    
}
