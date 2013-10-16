package webtv.lnk;

import javax.swing.tree.DefaultTreeModel;
import webtv.Product;
import webtv.RTMPTool;
import webtv.WGetTool;

/**
 *
 * @author marius
 */
public class Show extends Product
{
    final String url, referer;
    String mp4link;
    
    public Show(DefaultTreeModel model, String name, String link, String referer) {
        super(model, name, new RTMPTool());
        this.url = link;
        this.referer = referer;
        this.setUserObject(name);
        refresh();
    }

    /*
     rtmpdump --rtmp rtmp://stream.lnk.lt/vod 
     --playpath mp4:20130116/f002797l-130115-67mp4_EKh3fTU6PuaGZDIgN8iBCcXRM4bQVo1x.mp4 
     --swfFvy http://lnk.lnkgo.lt/js/flowplayer/flowplayer.commercial.swf 
     --pageUrl http://lnk.lnkgo.lt/video-perziura/4167/karamelines-naujienos-2013-01-16 
     --flv file.flv
     */
    @Override
    public void download() {
        if (mp4link==null) return;
        ((RTMPTool)tool).download("rtmp://stream.lnk.lt/vod", mp4link, 
            "http://lnk.lnkgo.lt/js/flowplayer/flowplayer.commercial.swf", url, path);
    }

    /*
1) fetch link
2) find a preview url like:  
var url         = '/videos/videos/previewVideo/4157/aHR0cDovL2xuay5sbmtnby5sdC91cGxvYWRzL2ltYWdlcy92aWRlb3MvNzE5MDQ4ZmRhNjkwMjIwY2YzMmFmMmZjYjljNWU2ZjBqcGdfMVFwU1B3RUpoV3lnbWNaTUdYMm5Ua3FEZnRqb1J1ZUMuanBn/c1c9NjQwJnNIPTM2MCZxdWFsaXR5PTk1/0';
3) fetch the preview:
/videos/videos/previewVideo/4157/aHR0cDovL2xuay5sbmtnby5sdC91cGxvYWRzL2ltYWdlcy92aWRlb3MvNzE5MDQ4ZmRhNjkwMjIwY2YzMmFmMmZjYjljNWU2ZjBqcGdfMVFwU1B3RUpoV3lnbWNaTUdYMm5Ua3FEZnRqb1J1ZUMuanBn/c1c9NjQwJnNIPTM2MCZxdWFsaXR5PTk1/0
4) locate the mp4 stream uri in the preview:
url: 'mp4:20130116/f002797l-130115-67mp4_EKh3fTU6PuaGZDIgN8iBCcXRM4bQVo1x.mp4',
     */
    static final String urlPrefix = "http://lnk.lnkgo.lt/videos/videos/previewVideo/";
    static final String linkBegin = "/videos/videos/previewVideo/";
    static final String linkEnd = "'";
    static final String mp4Prefix = "mp4:";
    static final String mp4Begin = "'mp4:";
    static final String mp4End = "'";
    
    @Override
    protected void doReload() {
        String doc = web.getDoc(url, referer);
        if (doc == null) { status = web.getStatus(); return ; }
        String link = web.findFirst(linkBegin, linkEnd);
        if (link==null) { status = "couldn't find download link1"; return; }
        link = urlPrefix+link;
        doc = web.getDoc(link, url);
        if (doc == null) { status = web.getStatus(); return; }
        mp4link = web.findFirst(mp4Begin, mp4End);
        if (mp4link==null) {
            int i = doc.indexOf("id=\"disabledVideo\"");
            if (i>=0) {
                int j = doc.indexOf(">", i)+1;
                int k = doc.indexOf("<", j);
                if (j>0 && k>0) {
                    status = doc.substring(j, k).trim();
                    return;
                }
            }
            status = "couldn't find download link2"; 
            return; 
        }
        mp4link = mp4Prefix + mp4link;
        status = null;        
    }
    
}
