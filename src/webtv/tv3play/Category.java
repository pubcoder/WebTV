/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.tv3play;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class Category extends SiteNode 
{
    private static String ref = TV3Play.url;    
    private final String url;
    private TreeSet<String> ids = new TreeSet<>();    
    
    public Category(DefaultTreeModel model, String url, String title)
    {
        super(model, title);
        this.url = url;
        setAllowsChildren(true);        
    }

    @Override
    public boolean isLeaf(){ return false; }    
    
    @Override
    protected void doReload() {
        String doc = web.getDoc(url, ref);
        if (doc == null) {status = web.getStatus(); return ; }
        final String linkPrefix = "http://www.tv3play.lt/programos/";
        final String linkBegin = "<a href=\""+linkPrefix;
        final String linkEnd = "\"";
        final String titleBegin = "<h3 class=\"clip-title\">";
        final String titleEnd = "</h3>";
        String link = web.findFirst(linkBegin, linkEnd);
        while (link != null && link.length()>0) {
            web.skipLastPostfix();
            String title = web.findNext(titleBegin, titleEnd);
            if (title == null || title.length() == 0) break;
            if (!ids.contains(title)) {
                add(new ProgramList(model, linkPrefix+link, url, title));
                ids.add(title);
            }
            web.skipLastPostfix();
            link = web.findNext(linkBegin, linkEnd);
        }
        status = null;
    }
}
