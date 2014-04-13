package webtv.tv3play;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class ProgramList extends SiteNode 
{
    private final String url, ref;
    private TreeSet<String> ids = new TreeSet<String>();

    public ProgramList(DefaultTreeModel model, String url, String ref, String title) {
        super(model, title);
        this.url = url;
        this.ref = ref;
        setAllowsChildren(true);
    }

    @Override
    public boolean isLeaf(){ return false; }    

    @Override
    protected void doReload() 
    {
        String doc = web.getDoc(url, ref);
        if (doc == null) { status = web.getStatus(); return; }
        final String dateBegin = "data-published=\"<strong>Patalpinta</strong>";
        final String dateEnd = "\"";
        final String linkBegin = "href=\""+url+"/";
        final String linkEnd = "?";
        final String nameBegin = "<h3 class=\"clip-title\">";
        final String nameEnd = "</h3>";
        String date = web.findFirst(dateBegin, dateEnd);
        while (date != null){
            date = date.trim();
            web.skipLastPostfix();
            String id = web.findNext(linkBegin, linkEnd);
            if (id == null) break;
            web.skipLastPostfix();
            String name = web.findNext(nameBegin, nameEnd);
            if (name == null) break;
            name = name.trim();
            web.skipLastPostfix();
            if (!ids.contains(name)){
                add(new Program(model, id, title+" ("+name+") "+date));
                ids.add(name);
            }
            date = web.findNext(dateBegin, dateEnd);
        }
        status = null;
    }    
}
