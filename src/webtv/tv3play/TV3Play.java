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
public class TV3Play extends SiteNode 
{
    public static final String url = "http://www.tv3play.lt/categories";
    public static final String referer = "http://www.tv3play.lt/";
    private TreeSet<String> ids = new TreeSet<String>();

    @Override
    protected String getURL() { return url; }

    @Override
    protected String getReferer() { return referer; }
    
    @Override
    public boolean isLeaf(){ return false; }    
    

    @Override
    protected void parseDoc(InputStream is, int length) throws IOException
    {
        StringBuilder doc = new StringBuilder();
        if (length<0) length = 4096;
        byte data[] = new byte[length];
        while (true){
            int got = is.read(data, 0, length);
            if (got < 0) break;
            doc.append(new String(data, 0, got));
        }
        findCategories(doc);
    }
    
    protected void findCategories(StringBuilder doc)
    {
        final String h2id = "<h2 id=\"";
        final String tagend = "\">";
        final String h2end = "</h2>";
        int i = doc.indexOf(h2id);
        while (i>=0) {
            i += h2id.length();
            int nameStart = doc.indexOf(tagend, i);
            if (nameStart<0) break;
            nameStart += tagend.length();
            int nameEnd = doc.indexOf(h2end, nameStart);
            if (nameEnd<0) break;
            String cat = doc.substring(nameStart, nameEnd);
            int catStart = nameEnd + h2end.length();
            i = doc.indexOf(h2id, catStart);
            int next = i;
            if (next<0) next = doc.length();
            if (!ids.contains(cat)) {
                add(new Category(model, doc, cat, catStart, next));
                ids.add(cat);
            }
        }
    }
    
    public TV3Play(DefaultTreeModel model)
    {
        super(model, "TV3Play");
    }
}
