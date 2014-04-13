package webtv.tv3play;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class TV3Play extends SiteNode 
{
    public static final String ref = "http://www.tv3play.lt/";
    public static final String url = "http://www.tv3play.lt/programos";
    private TreeSet<String> ids = new TreeSet<>();
  
    @Override
    public boolean isLeaf(){ return false; }    
    
    @Override
    protected void doReload()
    {
        String doc = web.getDoc(url, ref);
        if (doc == null) {status = web.getStatus(); return ; }
        final String linkPrefix = "http://www.tv3play.lt/kategorijos/";
        final String linkBegin = "href=\""+linkPrefix;
        final String linkEnd = "\"";
        final String titleBegin = "<h3 class=\"clip-title\">";
        final String titleEnd = "</h3>";
        String link = web.findFirst(linkBegin, linkEnd);
        while (link != null && link.length()>0) {
            web.skipLastPostfix();
            String title = web.findNext(titleBegin, titleEnd);
            if (title == null || title.length() == 0) break;
            if (!ids.contains(title)) {
                add(new Category(model, linkPrefix+link, title));
                ids.add(title);
            }
            web.skipLastPostfix();
            link = web.findNext(linkBegin, linkEnd);
        }
        status = null;
    }
    
    public TV3Play(DefaultTreeModel model)
    {
        super(model, "TV3Play");
    }
}
