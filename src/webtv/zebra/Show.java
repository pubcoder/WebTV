package webtv.zebra;

import javax.swing.tree.DefaultTreeModel;
import webtv.Product;
import webtv.WGetTool;

/**
 *
 * @author marius
 */
public class Show extends Product
{
    String url, link;

    Show(DefaultTreeModel model, String name, String url, String cookie) {
        super(model, name, new WGetTool());
        this.url = url;
        web.setCookie(cookie);
        refresh();
    }
      
    @Override
    public void download() { 
        if (link!=null)
            tool.download(link, path); 
    }

    static final String linkPref="http://vf.zebra.lt/files/";
    static final String linkBegin="http:\\/\\/vf.zebra.lt\\/files\\/";
    static final String linkFinish = "\"";
    
    @Override
    protected void doReload() 
    {       
        String d = web.getDoc(url, url);
        if (d == null) { status = web.getStatus(); return; }
        link = web.findFirst(linkBegin, linkFinish);
        if (link != null) {
            link = linkPref+link;
            status = null;           
            setDate(web.findFirst("<span class=\"uploaded\">Įkeltas:</span>", "</"));
        } else parseAdult();
    }

    private void parseAdult() 
    {
        web.setOrigin("http://zebra.15min.lt");
        String d = web.putDoc(url, url, 
                    "enter_adult=1&remember_adult=1", 
                    "application/x-www-form-urlencoded");
        if (d == null) { status = web.getStatus(); return; }
        link = web.findFirst(linkBegin, linkFinish);
        if (link != null) {
            link = linkPref+link;
            status = null;            
            setDate(web.findFirst("<span class=\"uploaded\">Įkeltas:</span>", "</"));
        }
    }
}
