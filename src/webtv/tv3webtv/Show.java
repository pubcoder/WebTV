/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.tv3webtv;

import java.io.IOException;
import java.io.InputStream;
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
public class Show extends Product 
{
    String url, referer, link;    
        
    public Show(DefaultTreeModel model, String title, String url, String referer){
        super(model, title, new RTMPTool());
        this.url = url;
        this.referer = referer;
        refresh();
    }
    
    @Override
    public void download() { 
        ((RTMPTool)tool).download(link, path, 
                "b8880becde3d77d6c11f9ef453053617667eaf4890f1f8748035f4003d70eeda", 
                "28811032"); 
    }

    @Override
    public void cancelDownload() { tool.cancelDownload(); }

    XMLParser parser = new XMLParser();
    @Override
    protected void parseDoc(InputStream is, int length) 
            throws IOException, Exception 
    {
        parser.parse(is, myhandler);
    }

    @Override
    protected String getURL() { return url; }

    @Override
    protected String getReferer() { return referer; }

    protected String rtmp, titleField=null;
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
