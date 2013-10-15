/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.balsas;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class BalsasList extends SiteNode 
{
    public static final String url = "http://www.balsas.lt/video";
    public static final String ref = "http://www.balsas.lt/";
    private TreeSet<String> ids = new TreeSet<String>();
    
    public BalsasList(DefaultTreeModel model)
    {
        super(model, "Balsas.lt");
    }

    @Override
    public boolean isLeaf(){ return false; }    

    @Override
    protected void doReload() {
        String d = web.getDoc(url, ref);
        if (d == null) {status = web.getStatus(); return ; }
        String linkBegin = "http://www.balsas.lt/video/";
        String linkEnd = "\">";
        String link = web.findFirst(linkBegin, linkEnd);
        while (link != null) {
            web.skipLastPostfix();
            link = linkBegin+link;            
            String name = web.findNext("</a>");
            if (name == null) break;
            web.skipLastPostfix();            
            if (!ids.contains(link)) {
                add(new Category(model, name, link));
                ids.add(link);
            }
            link = web.findNext(linkBegin, linkEnd);
        }
        status = null;
    }
    
}
