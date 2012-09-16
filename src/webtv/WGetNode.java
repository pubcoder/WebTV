package webtv;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public class WGetNode extends CommonNode 
{
    URI addr;
    String title, filename, scheme;
    String status = null;
    boolean ready = false, seen = false;
    boolean downloading = false;
    Process downloader = null;

    public WGetNode(DefaultTreeModel model, String title, String url)
    {
        super(model);
        try {
            addr = new URI(url);
        } catch (URISyntaxException ex) {
            Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        filename = "wget/"+title+".flv";
        this.title = title;
        scheme = addr.getScheme();
        File f;
        if ("file".equals(scheme)) f = new File(addr);
        else f = new File(filename);
        if (f.exists()) { status = "[exists]"; ready = true; }
        setUserObject(title);
    }
    
    public WGetNode(DefaultTreeModel model, URI uri)
    {
        super(model);
        addr = uri;
        scheme = addr.getScheme();
        try {
            filename = URLDecoder.decode(addr.toURL().getFile(), "UTF-8");
            int slash = filename.lastIndexOf('/');
            if (slash >= 0) {
                title = filename.substring(slash + 1);
            } else {
                title = filename;
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);

        } catch (MalformedURLException ex) {
            Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        File f;
        if ("file".equals(scheme)) f = new File(addr);
        else f = new File(filename);
        if (f.exists()) { status = "[exists]"; ready = true; }
        setUserObject(title);
    }

    public void cancelDownload(){
        if (downloader == null){
            downloader.destroy();
            downloader = null;
        }
    }

    public void play() {     
        String cmd[] = {"/usr/bin/vlc", filename};
        try {
            Runtime.getRuntime().exec(cmd);
            seen = true;
            repaintChange();
        } catch (IOException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(){
        new File(addr).delete();
        ready = false;
        status = "[deleted]";
        repaintChange();
    }

    @Override
    public String toString(){
        String s = title;
        if (seen) s += " [seen]";
        if (status != null) {
            return s+": "+status;
        } else return s;
    }

    public boolean isReady(){
        return ready;
    }

    public boolean isBusy(){
        return downloading;
    }

    public void download() {
        if (downloading) return;
        if ("file".equals(addr.getScheme())) return;
        downloading = true;
        new Thread(new Runnable(){
            public void run() {
                //System.out.println("Loading product");
                status = "downloading";
                repaintChange();
                String cmd[] = new String[] {
                    "/usr/bin/wget", "-c", addr.toString(), 
                    "-O", "wget/"+filename
                };
                try {
                    //System.out.println("Executing download");
                    System.out.println("Downloading: "+addr);
                    downloader = Runtime.getRuntime().exec(cmd);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(downloader.getErrorStream()));
                    int i = -1;
                    String line = reader.readLine();
                    while (line != null) {
                        line = line.trim();
                        if (line.length() > 0) {                            
                            status = line;
                            repaintChange();
                        }
                        line = reader.readLine();
                    }
/*                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();*/
                    i = downloader.waitFor();
                    downloader = null;
                    if (i!=0) {
                        status = status + " {"+i + "}";
                    } else {
                        status = "[ready]";
                        ready = true;
                    }
                    //System.out.println("Finished download");
                } catch (InterruptedException ex) {
                    Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                downloading = false;
                repaintChange();
            }
        }).start();
    }
}
