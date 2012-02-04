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
public class RTMPTool extends AbstractTool
{      
    static final String toolPath = "/usr/bin/rtmpdump";    
    String cmd[];    
    String cmdSimple[] = new String[]{
        toolPath, "--resume", "-r", null, "-o", null
    };
    
    @Override
    public synchronized void download(String url, String path) {
        if (downloading) return;
        downloading = true;
        cmd = cmdSimple;
        cmd[3] = url;
        cmd[5] = path;
        new Thread(this, "RTMPTool").start();
    }

        /*
         * How to get swfsize and swfhash:
         * download the player:
         * wget "http://flvplayer.viastream.viasat.tv/flvplayer/syndicatedPlayer/syndicated.swf"
         * "unzip" it:
         * flasm -x syndicated.swf
         * check the unzipped size:
         * ls -l syndicated.swf
         * compute the SHA256:
         * openssl sha -sha256 -hmac "Genuine Adobe Flash Player 001" syndicated.swf
         */    
    
    String cmdHash[] = new String[]{
        toolPath, "--resume", "-r", null, "-o", null,
        "--swfhash", null, "--swfsize", null
    };    
    public synchronized void download(String url, String path,
        String swfHash, String swfSize) 
    {
        if (downloading) return;
        downloading = true;
        cmd = cmdHash;
        cmd[3] = url;     cmd[5] = path;
        cmd[7] = swfHash; cmd[9] = swfSize;
        new Thread(this, "RTMPTool").start();
    }
    
    String cmdLong[] = new String[]{
        toolPath, "--resume", "-r", null, "-o", null,
        "--swfVfy", null, "--pageUrl", null,
        "--tcUrl", null, "--app", null,
        "--swfAge", "0", "--live",
        "--flashVer", "LNX 10,3,183,7"
    };
    public synchronized void download(String url, String path,
            String swfUrl, String pageUrl, String targetUrl, String app) 
    {
        if (downloading) return;
        downloading = true;
        cmd = cmdLong;
        cmd[3]  = url;       cmd[5]  = path;
        cmd[7]  = swfUrl;    cmd[9]  = pageUrl;
        cmd[11] = targetUrl; cmd[13] = app;
        new Thread(this, "RTMPTool").start();
    }
            
    @Override
    public void run() {
        listener.started();
        try {
            downloader = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(downloader.getErrorStream()));
            int i = -1;
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) listener.update(line);
                i = line.indexOf("filesize");
                line = reader.readLine();
            }
            if (i >= 0) {
                int size = Integer.parseInt(line.substring(i + 8).trim());
                listener.updateSize(size);
            }
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) listener.update(line);
                line = reader.readLine();
            }
            /*
               p.getInputStream().close(); p.getOutputStream().close();
               p.getErrorStream().close();
             */
            i = downloader.waitFor();
            downloader = null;
            if (i != 0) {
                listener.incomplete(line + " {" + i + "}");
            } else {
                listener.finished();
            }
        } catch (InterruptedException ex) {
            if (downloading) listener.incomplete(ex.getMessage());
            else listener.incomplete("Cancelled");
            Logger.getLogger(RTMPTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            if (downloading) listener.incomplete(ex.getMessage());
            else listener.incomplete("Cancelled");
            Logger.getLogger(RTMPTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if (downloading) listener.incomplete(ex.getMessage());
            else listener.incomplete("Cancelled");
            Logger.getLogger(RTMPTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (this) {
            downloading = false;
        }
    }
}
