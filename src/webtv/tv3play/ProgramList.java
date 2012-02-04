package webtv.tv3play;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;

/**
 *
 * @author marius
 */
public class ProgramList extends SiteNode 
{
    private final String url;
    private TreeSet<String> ids = new TreeSet<String>();

    public ProgramList(DefaultTreeModel model, String title, String url) {
        super(model, title);
        this.url = url;
        setAllowsChildren(true);
    }

    @Override
    public boolean isLeaf(){ return false; }    
    
    @Override
    protected void doReload() 
    {
        StringBuilder doc = web.getDoc(url, TV3Play.url);
        if (doc == null) { status = web.getStatus(); return; }        
        final String listbegin = "<h2>Serijų sąrašas</h2>";
        final String hrefplay = "<a href=\"/play/";
        final String tagend = "/\" >";
        final String aend = "</a>";
        final String td2begin = "<td class=\"col2\">";
        final String td3begin = "<td class=\"col3\">";
        final String td4begin = "<td class=\"col4\">";
        final String tdend = "</td>";

        int i = web.find(listbegin);
        if (i<0) return;
        web.skipLastPostfix();
        String id = web.findNext(hrefplay, tagend);
        while (id != null){
            web.skipLastPostfix();
            String name = web.findNext(aend);
            if (name==null) break;
            web.skipLastPostfix();
            String no = web.findNext(td2begin, tdend);
            if (no==null) break;
            web.skipLastPostfix();
            String duration = web.findNext(td3begin, tdend);
            if (duration==null) break;
            web.skipLastPostfix();
            String date = web.findNext(td4begin, tdend);
            if (date==null) break;
            web.skipLastPostfix();            
            if (!ids.contains(id)){
                add(new Program(model, id, name+" "+date+" (#"+no+") "+duration));
                ids.add(id);
            }
            id = web.findNext(hrefplay, tagend);
        }
        status = null;
    }    
}
