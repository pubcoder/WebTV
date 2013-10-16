package webtv.tv3play;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import webtv.Product;
import webtv.RTMPTool;
import webtv.XMLParser;

/**
 *
 * @author marius
 */
public class Program extends Product
{
    String id, url;
    String refererRnd, swfRnd, page;
 
    boolean ready = false;

    public Program(DefaultTreeModel model, String id)
    {
        super(model, "TV3Play-"+id, new RTMPTool());
        this.id = id;
        this.url = "http://viastream.viasat.tv/products/"+id;
        path = title+".flv";
        checkFileState();

        page = "http://www.tv3play.lt/play/"+id+"/";
        long rnd = System.currentTimeMillis()/1000;
        refererRnd = "http://flvplayer.viastream.viasat.tv/flvplayer/play/swf/player.swf?rnd="+rnd;
        swfRnd = "http://flvplayer.viastream.viasat.tv/play/swf/player110516.swf?rnd="+rnd;
        setUserObject(id);
        refresh();
    }

    public Program(DefaultTreeModel model, String id, String title)
    {
        super(model, title, new RTMPTool());
        this.id = id;
        this.url = "http://viastream.viasat.tv/PlayProduct/"+id;
        titleField = title;
        path = this.title+".flv";
        checkFileState();

        page = "http://www.tv3play.lt/play/"+id+"/";
        long rnd = System.currentTimeMillis()/1000;
        refererRnd = "http://flvplayer.viastream.viasat.tv/flvplayer/play/swf/player.swf?rnd="+rnd;
        swfRnd = refererRnd;
        //swfRnd = "http://flvplayer.viastream.viasat.tv/play/swf/player110516.swf?rnd="+rnd;
        setUserObject(title);
        refresh();        
    }
        
    @Override
    public void download() {
        ((RTMPTool)tool).download(link, path, swfRnd, page, 
                "rtmp://video.tv3.lt/tv3/mtg", "tv3/mtg");
    }
    @Override
    public void cancelDownload() { tool.cancelDownload(); }

    XMLParser parser = new XMLParser();
    
    @Override
    protected void doReload() {
        InputStream is = web.getStream(url, refererRnd);
        if (is == null) { status = web.getStatus(); return; }
        try {
            parser.parse(is, myhandler);
            if (link.startsWith("http")) {
                web.getDoc(link, url);
                url = web.findFirst("<Url>", "</Url>");
                status = web.findFirst("<Msg>", "</Msg>");
            } else status = null;
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (titleField==null) {            
            title = titleField+" ("+id+")";
            String newFilename = title+".flv";
            new File(path).renameTo(new File(newFilename));
            path = newFilename;
        }
    }
    
    protected String link, titleField=null;
    DefaultHandler myhandler = new DefaultHandler() {
        int field = 0;
        @Override
        public void characters(char[] ch, int start, int length) {
            switch (field) {
                case 5:
                    link = new String(ch, start, length);
                    break;
                case 10:
                    titleField = new String(ch, start, length);
                    repaintChange();
                    break;
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if ("Products".equals(qName)) {
                field = 1;
            } else if (field == 1 && "Product".equals(qName)) {
                field = 2;
            } else if (field == 2 && "Title".equals(qName)) {
                field = 10;
            } else if (field == 2 && "Videos".equals(qName)) {
                field = 3;
            } else if (field == 3 && "Video".equals(qName)) {
                field = 4;
            } else if (field == 4 && "Url".equals(qName)) {
                field = 5;
            }
            //System.out.println("<"+qName+">");
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (field == 10 && "Title".equals(qName)) {
                field = 2;
            } else if (field == 5 && "Url".equals(qName)) {
                field = 4;
            } else if (field == 4 && "Video".equals(qName)) {
                field = 3;
            } else if (field == 3 && "Videos".equals(qName)) {
                field = 2;
            } else if (field == 2 && "Product".equals(qName)) {
                field = 1;
            } else if (field == 1 && "Products".equals(qName)) {
                field = 0;
            }
            //System.out.println("</"+qName+">");
        }
    };
}
