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
    public static final String url = "http://www.tv3play.lt/categories";
    public static final String ref = "http://www.tv3play.lt/";
    private TreeSet<String> ids = new TreeSet<String>();
  
    @Override
    public boolean isLeaf(){ return false; }    
    
    @Override
    protected void doReload()
    {
        String doc = web.getDoc(url, ref);
        if (doc == null) {status = web.getStatus(); return ; }
        final String h2id = "<h2 id=\"";
        final String tagend = "\">";
        final String h2end = "</h2>";
        int i = web.find(h2id);
        while (i>=0) {
            web.skipLastPostfix();
            String cat = web.findNext(tagend, h2end);
            int catStart = web.skipLastPostfix();
            i = web.find(h2id);
            int next = i;
            if (next<0) next = doc.length();
            if (!ids.contains(cat)) {
                add(new Category(model, doc, cat, catStart, next));
                ids.add(cat);
            }
        }
        status = null;
    }
    
    public TV3Play(DefaultTreeModel model)
    {
        super(model, "TV3Play");
    }
}
