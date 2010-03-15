/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public class WGetNode extends CommonNode {
    URI addr;
    String filename, scheme;
    String status = null;
    boolean ready = false, seen = false;
    boolean downloading = false;
    Process downloader = null;


    public WGetNode(DefaultTreeModel model, URI uri){
        super(model);
        addr = uri;
        scheme = addr.getScheme();
        try {
            filename = addr.toURL().getFile();
        } catch (MalformedURLException ex) {
            Logger.getLogger(WGetNode.class.getName()).log(Level.SEVERE, null, ex);
        }
        int slash = filename.lastIndexOf('/');
        if (slash >= 0) filename = filename.substring(slash+1);
        File f;
        if ("file".equals(scheme)) f = new File(addr);
        else f = new File("wget/"+filename);
        if (f.exists()) { status = "[exists]"; }
        setUserObject(filename);
    }

    public void cancelDownload(){
        if (downloader == null){
            downloader.destroy();
            downloader = null;
        }
    }

    public void play() {     
        String cmd[] = {"/usr/bin/totem", filename};
        try {
            Runtime.getRuntime().exec(cmd);
            seen = true;
            repaintChange();
        } catch (IOException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(){
        new File(filename).delete();
        ready = false;
        status = "[deleted]";
        repaintChange();
    }

    @Override
    public String toString(){
        String s = filename;
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
                /*
                for (DownloadListener l: listeners)
                    l.finished(Product.this);
                 */
            }
        }).start();
    }
}
