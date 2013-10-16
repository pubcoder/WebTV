package webtv;

import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public abstract class SiteNode extends CommonNode
{
    protected String title;
    protected String date;
    protected String status;
    protected boolean busy = false, refreshing = false;
    protected HTTPHelper web = new HTTPHelper();

    public SiteNode(DefaultTreeModel model, String title){
        super(model);
        this.title = title.trim();
    }

    @Override
    public synchronized void refresh(){
        if (busy) return;
        busy = true;
        status = "loading";
        if (isLeaf()) repaintChange();
        else repaintChangeAndStructure();        
        new Thread(new Runnable(){
            public void run(){ reload(); }
        }, "SiteNode").start();
    }

    protected abstract void doReload();
    /*
    protected void doReload()
    {
        InputStream is = web.getStream(getURL(), getReferer());
        if (is == null) status = web.getStatus();
        try {
            parseDoc(is, web.getContentLength());
            status = null;            
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(SiteNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(SiteNode.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    protected abstract void parseDoc(InputStream is, int length) throws IOException, Exception;
    */

    protected void reload() {
        doReload();
        synchronized (this) {        
            refreshing = true;
            if (isLeaf()) repaintChange();
            else repaintChangeAndStructure();
            busy = false; 
        }
    }

    protected void setDate(String date) {
        if (date != null) this.date = date.trim();
        else this.date = date;
        repaintChange();
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(title);
        if (date != null) s.append(" ").append(date);
        if (status!=null) s.append(" [").append(status).append("]");
        return s.toString();
    }
}
