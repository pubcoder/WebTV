/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marius
 */
public class WGetTool extends AbstractTool 
{    
    @Override
    public void run() {
        String cmd[] = new String[]{
            "/usr/bin/wget", "-c", url, "-O", path
        };
        try {
            downloader = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(downloader.getErrorStream()));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    listener.update(line);
                }
                line = reader.readLine();
            }
            /*
             * p.getInputStream().close(); p.getOutputStream().close();
                    p.getErrorStream().close();
             */
            int i = downloader.waitFor();
            downloader = null;
            if (i != 0) {
                listener.incomplete(line + " {" + i + "}");
            } else {
                listener.finished();
            }
        } catch (InterruptedException ex) {
            listener.incomplete(ex.getMessage());            
            Logger.getLogger(WGetTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            listener.incomplete(ex.getMessage());            
            Logger.getLogger(WGetTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (this) {
            downloading = false;
        }
    }
}
