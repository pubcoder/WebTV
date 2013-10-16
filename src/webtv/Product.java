package webtv;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public abstract class Product extends SiteNode
{
    protected String path;
    protected DownloadTool tool;
    protected long size;
    
    public enum State { Unknown, Loading, Downloading, Incomplete, Ready, Exists, Deleted, Scheduled };
    protected State state = State.Unknown;
    boolean seen = false;

    protected final void checkFileState() {
        File f = new File(path);
        if (f.exists()) {
            if (size > 0) {
                if (size > f.length()) {
                    state = State.Incomplete;
                } else {
                    state = State.Ready;
                }
            } else state = State.Exists;
        } else {
            state = State.Unknown;
        }
    }

    public Product(DefaultTreeModel model, String title, DownloadTool tool){
        super(model, title);
        path = "wget/"+title+".flv";
        this.tool = tool;
        tool.setProgressListener(plistener);
        checkFileState();
        setAllowsChildren(false);
    }

    @Override
    public boolean isLeaf() { return true; }
    
    ProgressListener plistener = new ProgressListener() {
        public void started(){ 
            status = "downloading";
            state = State.Downloading;
            repaintChange();
        }
        public void updateSize(int size){
            Product.this.size = size;
        }
        public void update(String status){
            Product.this.status = status;
            repaintChange();
        }
        public void incomplete(String status){
            Product.this.status = status;
            state = State.Incomplete;
            for (DownloadListener l: listeners)
                l.finished(Product.this);
            repaintChange();
        }
        public void finished(){
            status = null;
            state = State.Ready;
            for (DownloadListener l: listeners)
                l.finished(Product.this);            
            repaintChange();            
        }
    };
    
    public abstract void download();
    public void cancelDownload(){ tool.cancelDownload(); }
    
    @Override
    protected void reload(){
        State last = state;
        state = State.Loading;
        super.reload();
        state = last;
        repaintChange();
    }

    public State getState(){ return state; }

    public void play() {
        if (State.Unknown.equals(state) || State.Loading.equals(state)) return;
        File f = new File(path);
        String cmd[] = {"/usr/bin/vlc", "--started-from-file", 
            "--playlist-enqueue", f.getAbsolutePath()};
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.getErrorStream().close();
            p.getOutputStream().close();
            seen = true;
            this.repaintChange();
        } catch (IOException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(){
        new File(path).delete();
        state = State.Deleted;
        cancelDownload();
        repaintChange();
    }
    public void setScheduled(boolean b) {
        if (b) state = State.Scheduled;
        else checkFileState();
        repaintChange();
    }
/*
    State updateState(){
        if (busy) state = State.Loading;
        else if (downloading) state = State.Downloading;
        else {
            File f = new File(filename);
            if (f.exists()) {
                if (size>0) {
                    if (size > f.length()) state = State.Incomplete;
                    else state = State.Ready;
                }
            } else state = State.Unknown;
        }
        return state;
    }
*/
    @Override
    public String toString(){
        StringBuilder r = new StringBuilder(title);
        if (date != null) r.append(' ').append(date);
        if (seen) r.append(" [seen]");
        switch (state) {
            case Unknown:
            case Downloading:
                if (status != null) r.append(" [").append(status).append("]");
                break;
            default: 
                if (status==null) r.append(" [").append(state).append("]");
                else r.append(" [").append(state).append(": ").append(status).append("]");
                break;
        }
        return r.toString();
    }
    HashSet<DownloadListener> listeners =  new HashSet<DownloadListener>();
    public void addDownloadListener(DownloadListener d) {
        listeners.add(d);
    }
    public void removeDownloadListener(DownloadListener d) {
        listeners.remove(d);
    }
}
