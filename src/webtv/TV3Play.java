/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.io.File;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public class TV3Play extends Product
{
    String refererRnd, swfRnd, page;
 
    boolean ready = false;

    public TV3Play(DefaultTreeModel model, String id)
    {
        super(model, "TV3Play-"+id, id);
        filename = "wget/"+title+".flv";
        checkFileState();

        page = "http://www.tv3play.lt/play/"+id+"/";
        long rnd = System.currentTimeMillis()/1000;
        refererRnd = "http://flvplayer.viastream.viasat.tv/flvplayer/play/swf/player.swf?rnd="+rnd;
        swfRnd = "http://flvplayer.viastream.viasat.tv/play/swf/player110516.swf?rnd="+rnd;
        setUserObject(id);
        refresh();
    }

    @Override
    protected String getURL() { return "http://viastream.viasat.tv/PlayProduct/"+id; }
    @Override
    protected String getReferer() { return (refererRnd); }

    @Override
    public String toString(){
        String s = title;
        if (seen) s += " [seen]";
        if (status != null) {
            return s+": "+status;
        } else return s;
    }

    @Override
    protected void reload(){
        if (titleField==null) {
            super.reload();
            title = titleField+" ("+id+")";
            String newFilename = "wget/"+title+".flv";
            new File(filename).renameTo(new File(newFilename));
            filename = newFilename;
            repaintChange();
        } else {
            super.reload();
        }
    }

    @Override
    protected String[] getDownloadCommand(){
        String downCmd[] = { "/usr/bin/rtmpdump",
            "--swfVfy", swfRnd,
            "--swfAge", "0",
            "--rtmp", rtmp,
            "--flv", filename,
            "--live",
            "--app", "tv3/mtg",
            "--flashVer", "LNX 10,3,183,7",
            "--swfUrl", swfRnd,
            "--tcUrl", "rtmp://video.tv3.lt/tv3/mtg",
            "--pageUrl", page
        };
        return downCmd;
    }

}
