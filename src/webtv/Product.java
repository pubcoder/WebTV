/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author marius
 */
public class Product extends SiteNode
{
    public String filename;
    protected String rtmp, titleField=null;
    public long size;

    public enum State { Unknown, Loading, Downloading, Incomplete, Ready, Exists, Deleted, Scheduled };
    protected State state = State.Unknown;
    boolean seen = false;

    protected void checkFileState() {
        File f = new File(filename);
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

    public Product(DefaultTreeModel model, String title, String id){
        super(model, title, id);
        filename = title+".flv";
        checkFileState();
    }

    @Override
    protected String getURL() {
        return ("http://viastream.viasat.tv/products/"+id);
    }

    @Override
    protected String getReferer() {
        return SiteMapNode.referer;
    }


    DefaultHandler handler = new DefaultHandler(){
    int field = 0;

    @Override
    public void characters(char[] ch, int start, int length)
    {
        switch (field) {
            case 5: rtmp = new String(ch, start, length); break;
            case 10: titleField = new String(ch, start, length); repaintChange(); break;
        }
        //System.out.println(new String(ch, start, length));
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs)
    {
        if ("Products".equals(qName)) {
            field = 1;
        } else if (field==1 && "Product".equals(qName)) {
            field = 2;
        } else if (field==2 && "Title".equals(qName)) {
            field = 10;
        } else if (field==2 && "Videos".equals(qName)) {
            field = 3;
        } else if (field==3 && "Video".equals(qName)) {
            field = 4;
        } else if (field==4 && "Url".equals(qName)) {
            field = 5;
        }
        //System.out.println("<"+qName+">");
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (field==10 && "Title".equals(qName)) {
            field = 2;
        } else if (field==5 && "Url".equals(qName)) {
            field = 4;
        } else if (field==4 && "Video".equals(qName)) {
            field = 3;
        } else if (field==3 && "Videos".equals(qName)) {
            field = 2;
        } else if (field==2 && "Product".equals(qName)) {
            field = 1;
        } else if (field==1 && "Products".equals(qName)) {
            field = 0;
        }
        //System.out.println("</"+qName+">");
    }
    };

    @Override
    protected DefaultHandler getHandler() {
        return handler;
    }

    boolean downloading = false;

    protected String[] getDownloadCommand(){
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
        String downCmd[] = { "/usr/bin/rtmpdump", "--swfhash",
            "b8880becde3d77d6c11f9ef453053617667eaf4890f1f8748035f4003d70eeda",
            "--swfsize", "28811032", "-r", rtmp, "-o", filename };
        String resumeCmd[] = { "/usr/bin/rtmpdump", "--swfhash",
            "b8880becde3d77d6c11f9ef453053617667eaf4890f1f8748035f4003d70eeda",
            "--swfsize", "28811032", "--resume", "-r", rtmp, "-o", filename };
        return downCmd;/*
        if (State.Unknown.equals(state) || State.Loading.equals(state)) return downCmd;
        else return resumeCmd;*/
    }

    Process downloader = null;

    public void download() {
        if (downloading) return;
        downloading = true;
        state = State.Downloading;
        new Thread(new Runnable(){
            public void run() {
                //System.out.println("Loading product");
                reload();
                try {
                    if (rtmp == null || rtmp.length() == 0) {
                        throw new Exception("No video URL");
                    }
                    status = "downloading";
                    repaintChange();
                    String cmd[] = getDownloadCommand();
                    //System.out.println("Executing download");
                    System.out.println("Downloading: " + rtmp);
                    downloader = Runtime.getRuntime().exec(cmd);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(downloader.getErrorStream()));
                    int i = -1;
                    String line = reader.readLine();
                    while (line != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            status = line;
                            // System.out.println(status);
                            repaintChange();
                        }
                        i = line.indexOf("filesize");
                        line = reader.readLine();
                    }
                    if (i >= 0) {
                        size = Integer.parseInt(line.substring(i + 8).trim());
                    }
                    while (line != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            status = line;
                            //System.out.println(status);
                            repaintChange();
                        }
                        line = reader.readLine();
                    }
                    /*                    p.getInputStream().close();
                    p.getOutputStream().close();
                    p.getErrorStream().close();*/
                    i = downloader.waitFor();
                    downloader = null;
                    if (i != 0) {
                        status = status + " {" + i + "}";
                        state = State.Incomplete;
                    } else {
                        status = null;
                        state = State.Ready;
                    }
                    //System.out.println("Finished download");
                } catch (InterruptedException ex) {
                    checkFileState();
                    status = ex.getMessage();
                    //ex.printStackTrace(System.err);
                } catch (IOException ex) {
                    if (state != State.Deleted) {
                        checkFileState();
                        status = ex.getMessage();
                        ex.printStackTrace(System.err);
                    } else status = "{killed}";
                } catch (Exception ex) {
                    checkFileState();
                    status = ex.getMessage();
                    ex.printStackTrace(System.err);
                }
                downloading = false;
                repaintChange();
                for (DownloadListener l: listeners)
                    l.finished(Product.this);
            }
        }, "Download").start();
    }

    public void cancelDownload(){
        if (downloader == null){
            downloader.destroy();
            downloader = null;
        }
    }

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
        String cmd[] = {"/usr/bin/totem", "--enqueue", filename};
        try {
            Runtime.getRuntime().exec(cmd);
            seen = true;
            this.repaintChange();
        } catch (IOException ex) {
            Logger.getLogger(Product.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(){
        new File(filename).delete();
        state = State.Deleted;
        if (downloader != null) {
            downloader.destroy();
            downloader = null;
        }
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
        String r = title;
        if (seen) r = r+" [seen]";
        switch (state) {
            case Unknown:
            case Downloading:
                if (status != null) r = r + " ["+status+"]";
                break;
            default: 
                if (status==null) r = r +" ["+state+"]";
                else r = r +" ["+state+": "+status+"]";
                break;
        }
        return r;
    }
    HashSet<DownloadListener> listeners =  new HashSet<DownloadListener>();
    public void addDownloadListener(DownloadListener d) {
        listeners.add(d);
    }
    public void removeDownloadListener(DownloadListener d) {
        listeners.remove(d);
    }
}
