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
    static final String flashVer = "LNX 11,3,31,230";
    String cmd[];    
    static final String cmdSimple[] = new String[]{
        toolPath, "--resume", "-r", null, "-o", null
    };
    
    @Override
    public synchronized void download(String url, String path) {
        if (downloading) return;
        downloading = true;
        cmd = cmdSimple.clone();
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
    
    static final String cmdHash[] = new String[]{
        toolPath, "--resume", "-r", null, "-o", null,
        "--swfhash", null, "--swfsize", null
    };    
    public synchronized void download(String url, String path,
        String swfHash, String swfSize) 
    {
        if (downloading) return;
        downloading = true;
        cmd = cmdHash.clone();
        cmd[3] = url;     cmd[5] = path;
        cmd[7] = swfHash; cmd[9] = swfSize;
        new Thread(this, "RTMPTool").start();
    }

    static final String cmdWithPath[] = new String[]{
        toolPath, "--rtmp", null, "--playpath", null,
        "--swfVfy", null, "--pageUrl", null,
        "--flv", null, "--flashVer", flashVer
    };
    public synchronized void download(String rtmp, String playpath,
        String swfVfy, String pageUrl, String outputPath) 
    {
        if (downloading) return;
        downloading = true;
        cmd = cmdWithPath.clone();
        cmd[2] = rtmp;     cmd[4] = playpath;    cmd[6] = swfVfy;
        cmd[8] = pageUrl;  cmd[10] = outputPath;
        new Thread(this, "RTMPTool").start();
    }
    
    /**
     * It seems that the --live option is required, 
     * otherwise it downloads only 0.9%
     */
    static final String cmdLong[] = new String[]{
        toolPath, "--live", "--rtmp", null, "--flv", null,
        "--swfVfy", null, "--pageUrl", null,
        "--tcUrl", null, "--app", null,
        "--swfAge", "0", "--flashVer", flashVer
    };
    public synchronized void download(String rtmp, String outputPath,
            String swfVfy, String pageUrl, String targetUrl, String app) 
    {
        if (downloading) return;
        downloading = true;
        cmd = cmdLong.clone();
        cmd[3]  = rtmp;       cmd[5]  = outputPath;
        cmd[7]  = swfVfy;    cmd[9]  = pageUrl;
        cmd[11] = targetUrl; cmd[13] = app;
        new Thread(this, "RTMPTool").start();
    }
            
    @Override
    public void run() {
        listener.started();
        try {
            for (String s: cmd)
                System.out.print(s+" ");
            System.out.println();
            downloader = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(downloader.getErrorStream()));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                if (line.length() > 0) {
                    listener.update(line);
                    if (line.startsWith("INFO:")) {
                        System.out.println(line);                        
                        int i = line.indexOf("filesize");
                        if (i >= 0) {
                            int size = Integer.parseInt(line.substring(i + 8).trim());
                            listener.updateSize(size);
                            break;
                        }
                    } else break;
                }
                line = reader.readLine();
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
            int c = downloader.waitFor();
            downloader = null;
            if (c != 0) {
                listener.incomplete(line + " {" + c + "}");
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
