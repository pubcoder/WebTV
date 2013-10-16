package webtv.lnk;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class Category extends SiteNode 
{
    final String url;
    final String referer;
    private TreeSet<String> ids = new TreeSet<String>();    

    public Category(DefaultTreeModel model, String name, String link, String referer) {
        super(model, name);
        if (link.indexOf('?')<0) this.url = link + "?page=1&orderBy=created";
        else this.url = link;
        this.referer = referer;
    }

    @Override
    public boolean isLeaf(){ return false; }    

    public static final String urlPrefix = "http://lnk.lnkgo.lt/video-perziura/";
    static final String linkBegin = "<a href=\"/video-perziura/";
    static final String linkEnd = "\"";
    
    @Override
    protected void doReload() {
        String doc = web.getDoc(url, referer);
        if (doc == null) { status = web.getStatus(); return ; }
        int count = 0;
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
                    add(new Show(model, name, link, url));
                    ids.add(link);
                    count++;
                }
            }
            link = web.findNext(linkBegin, linkEnd);
        }
        if (count==9) {
            int i = url.indexOf("page=")+5;
            int j = url.indexOf('&', i);
            int page = Integer.parseInt(url.substring(i, j))+1;
            link = url.substring(0, url.indexOf('?'))+"?page="+page+"&orderBy=created";
            if (!ids.contains(link)) {
                add(new Category(model, "Page "+page, link, url));
                ids.add(link);
            }
        }
        status = null;
    }        
}
