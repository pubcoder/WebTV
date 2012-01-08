/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    protected String getURL() { return url; }
    @Override
    protected String getReferer() { return MainPage.url; }
    @Override
    public boolean isLeaf(){ return false; }    
    

    @Override
    protected void parseDoc(InputStream is) throws IOException, Exception {
        StringBuilder doc = new StringBuilder();
        int size = 4096;
        byte data[] = new byte[size];
        while (true){
            int got = is.read(data, 0, size);
            if (got<0) break;
            doc.append(new String(data, 0, got));
        }
        findPrograms(doc);        
    }

    private void findPrograms(StringBuilder doc) {
        final String listbegin = "<h2>Serijų sąrašas</h2>";
        final String hrefplay = "<a href=\"/play/";
        final String tagend = "/\" >";
        final String aend = "</a>";
        final String td2begin = "<td class=\"col2\">";
        final String td3begin = "<td class=\"col3\">";
        final String td4begin = "<td class=\"col4\">";
        final String tdend = "</td>";
        int i = doc.indexOf(listbegin);
        if (i<0) return;
        i = doc.indexOf(hrefplay, i);
        while (i>0){
            int idStart = i+hrefplay.length();
            int idEnd = doc.indexOf(tagend, idStart);
            if (idEnd<0) break;
            String id = doc.substring(idStart, idEnd);
            int nameStart = idEnd+tagend.length();
            int nameEnd = doc.indexOf(aend, nameStart);
            if (nameEnd<0) break;
            String name = doc.substring(nameStart, nameEnd);
            int noStart = doc.indexOf(td2begin, nameEnd+aend.length());
            if (noStart<0) break;
            noStart += td2begin.length();
            int noEnd = doc.indexOf(tdend, noStart);
            if (noEnd<0) break;
            String no = doc.substring(noStart, noEnd);
            int durationStart = doc.indexOf(td3begin, noEnd+tdend.length());
            if (durationStart<0) break;
            durationStart += td3begin.length();
            int durationEnd = doc.indexOf(tdend, durationStart);
            if (durationEnd<0) break;
            String duration = doc.substring(durationStart, durationEnd);
            int dateStart = doc.indexOf(td4begin, durationEnd+tdend.length());
            if (dateStart<0) break;
            dateStart += td4begin.length();
            int dateEnd = doc.indexOf(tdend, dateStart);
            if (dateEnd<0) break;
            String date = doc.substring(dateStart, dateEnd);
            if (!ids.contains(id)){
                add(new Program(model, id, name+" "+date+" (#"+no+") "+duration));
                ids.add(id);
            }
            i = doc.indexOf(hrefplay, dateEnd+tdend.length());
        }
    }
    
}
