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
    private final String url;
    private TreeSet<String> ids = new TreeSet<String>();

    public ProgramList(DefaultTreeModel model, String title, String url) {
        super(model, title);
        this.url = url;
        setAllowsChildren(true);
    }

    @Override
    public boolean isLeaf(){ return false; }    

    static final String listbegin = "<h2>Serijų";
    static final String hrefplay = "<a href=\"/play/";
    static final String hrefend = "\">";
    static final String spanname = "class=\"formatname\">";
    static final String spanepisode = "class=\"episodeinfo\">";
    static final String spanend = "</span>";
    static final String divdate = "class=\"broadcast-date";
    static final String divstart = ">";
    static final String divend = "</div>";

    @Override
    protected void doReload() 
    {
        String doc = web.getDoc(url, TV3Play.url);
        if (doc == null) { status = web.getStatus(); return; }        

        int i = web.find(listbegin);
        if (i<0) return;
        web.skipLastPostfix();
        String id = web.findNext(hrefplay, hrefend);
        while (id != null){
            int q = id.indexOf('?');
            if (q>=0) id = id.substring(0, q);
            web.skipLastPostfix();
            String name = web.findNext(spanname, spanend);
            if (name==null) break;
            web.skipLastPostfix();
            String episode = web.findNext(spanepisode, spanend);
            if (episode==null) break;
            web.skipLastPostfix();
            web.find(divdate); web.skipLastPostfix();
            String date = web.findNext(divstart, divend);
            if (date==null) break;
            web.skipLastPostfix();
            if (!ids.contains(id)){
                add(new Program(model, id, name+" "+date+" ("+episode+")"));
                ids.add(id);
            }
            id = web.findNext(hrefplay, hrefend);
        }
        status = null;
    }    
}
