/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.balsas;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
class Category extends SiteNode 
{
    final String url;
    private TreeSet<String> ids = new TreeSet<String>();    

    public Category(DefaultTreeModel model, String name, String link) {
        super(model, name);
        this.url = link;
    }

    @Override
    public boolean isLeaf(){ return false; }    

    protected void doReload() {
        String doc = web.getDoc(url, BalsasList.url);
        if (doc == null) { status = web.getStatus(); return ; }
        String titleDiv = "<div class=\"title\">";
        int i = web.find(titleDiv);
        while (i>0) {            
            web.skipLastPostfix();
            String link = web.findNext("href=\"/video/", "\"");
            if (link == null) break;
            web.skipLastPostfix();
            link = "http://www.balsas.lt/video/"+link;
            String name = web.findNext(">", "</a>");
            if (name == null) break;
            web.skipLastPostfix();
            if (!ids.contains(link)) {
                add(new Show(model, name, link, url));
                ids.add(link);
            }
            i = web.find(titleDiv);
        }
        status = null;
    }        
}
