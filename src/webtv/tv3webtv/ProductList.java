/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv.tv3webtv;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import webtv.SiteNode;
import webtv.XMLParser;

/**
 *
 * @author marius
 */
public class ProductList extends SiteNode {
    public final String url;
    public static final String referer = SiteMapNode.referer;
    private final String id;
    protected TreeSet<String> ids = new TreeSet<String>();    

    public ProductList(DefaultTreeModel model, String title, String id){
        super(model, title);
        this.id = id;
        this.url = "http://viastream.viasat.tv/Products/Category/"+id;
        setAllowsChildren(true);
    }

    @Override
    protected String getURL() { return url; }
    @Override
    protected String getReferer() { return referer; }

    DefaultHandler myhandler = new DefaultHandler() {

        String prodId;
        String prodTitle;
        int field = 0;

        @Override
        public void characters(char[] ch, int start, int length) {
            switch (field) {
                case 2:
                    prodId = new String(ch, start, length);
                    break;
                case 3:
                    prodTitle = new String(ch, start, length);
                    break;
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) {
            if ("Product".equals(qName)) {
                prodId = null;
                prodTitle = null;
                field = 1;
            } else if (field == 1 && "ProductId".equals(qName)) {
                field = 2;
            } else if (field == 1 && "Title".equals(qName)) {
                field = 3;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (field == 3 && "Title".equals(qName)) {
                field = 1;
            } else if (field == 2 && "ProductId".equals(qName)) {
                field = 1;
            } else if ("Product".equals(qName) && !ids.contains(prodId)) {
                SiteNode node = new Show(model, prodTitle, "http://viastream.viasat.tv/products/"+prodId, url);
                if (refreshing) ProductList.this.insert(node, 0);
                else add(node);
                ids.add(prodId);
                field = 0;
            }
        }
    };
    
    @Override
    public boolean isLeaf(){ return false; }

    
    XMLParser parser = new XMLParser();
    @Override
    protected void parseDoc(InputStream is, int length) 
            throws IOException, Exception 
    {
        parser.parse(is, myhandler);
    }


}
