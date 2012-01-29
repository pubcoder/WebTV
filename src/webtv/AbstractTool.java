/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

/**
 *
 * @author marius
 */
public class AbstractTool implements DownloadTool, Runnable
{
    protected boolean downloading = false;    
    protected Process downloader = null;
    
    protected static final ProgressListener dummy = new ProgressListener(){
        public void started() {}
        public void updateSize(int size) {}
        public void update(String status) {}
        public void incomplete(String status) {}
        public void finished() {}
    };
    ProgressListener listener = dummy;

    public void setProgressListener(ProgressListener l) { listener = l; }

    
    String url, path;
    
    public synchronized void download(String url, String path) {
        if (downloading) return;
        downloading = true;
        this.url = url;
        this.path = path;
        new Thread(this, "DownloadTool").start();
    }

    public synchronized void cancelDownload(){
        if (downloader == null){
            downloader.destroy();
            downloader = null;
        }
    }

    public void run() {
        synchronized(this) {
            downloading = false;
        }
        listener.finished();
    }
    
}
