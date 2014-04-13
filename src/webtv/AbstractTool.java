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
        @Override
        public void started() {}
        @Override
        public void updateSize(int size) {}
        @Override
        public void update(String status) {}
        @Override
        public void incomplete(String status) {}
        @Override
        public void finished() {}
    };
    ProgressListener listener = dummy;

    @Override
    public void setProgressListener(ProgressListener l) { listener = l; }

    
    String url, path;
    
    @Override
    public synchronized void download(String url, String path) {
        if (downloading) return;
        downloading = true;
        this.url = url;
        this.path = path;
        new Thread(this, "DownloadTool").start();
    }

    @Override
    public synchronized void cancelDownload(){
        downloading = false;
        if (downloader != null){
            downloader.destroy();
            downloader = null;
        }
    }

    @Override
    public void run() {
        synchronized(this) {
            downloading = false;
        }
        listener.finished();
    }
    
}
