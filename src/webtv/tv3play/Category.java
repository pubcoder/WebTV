/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.tv3play;

import javax.swing.tree.DefaultTreeModel;
import webtv.CommonNode;

/**
 *
 * @author marius
 */
public class Category extends CommonNode 
{
    private final String title;
    
    public Category(DefaultTreeModel model, String doc, String title, int start, int end)
    {
        super(model);
        this.title = title;
        findPrograms(doc, start, end);
    }
    
    protected final void findPrograms(String doc, int start, int end)
    {
        final String hrefprog = "<a href=\"/program/";
        final String tagend = "\">";
        final String aend = "</a>";
        int i = doc.indexOf(hrefprog, start);
        while (i>0 && i<end) {            
            int urlend = doc.indexOf(tagend, i+hrefprog.length());
            if (urlend < 0) break;
            String url = "http://www.tv3play.lt"+doc.substring(i+"<a href=\"".length(), urlend);
            int progStart = urlend+tagend.length();
            int progEnd = doc.indexOf(aend, progStart);
            if (progEnd<0) break;
            String prog = doc.substring(progStart, progEnd);
            i = doc.indexOf(hrefprog, progEnd+aend.length());
            add(new ProgramRSSList(model, prog, url));
            //add(new ProgramList(model, prog, url));
        }
    }
    
    @Override
    public String toString() { return title; }    
}
