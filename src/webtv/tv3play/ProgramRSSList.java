package webtv.tv3play;

import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;
import webtv.SiteNode;
import webtv.rss.Feed;
import webtv.rss.FeedMessage;
import webtv.rss.RSSFeedParser;

/**
 *
 * @author marius
 */
public class ProgramRSSList extends SiteNode 
{
    private final String url;
    private TreeSet<String> ids = new TreeSet<>();

    public ProgramRSSList(DefaultTreeModel model, String title, String url) {
        super(model, title);
        this.url = url;
        setAllowsChildren(true);
    }

    @Override
    public boolean isLeaf(){ return false; }    

    @Override
    protected void doReload() 
    {
        String doc = web.getDoc(url, TV3Play.url);
        if (doc == null) { status = web.getStatus(); return; }        

        String fid = web.findNext("<a href=\"/rss/recent?fid=", "\">");
        if (fid != null) {
            RSSFeedParser parser = new RSSFeedParser("http://www.tv3play.lt/rss/recent?fid="+fid);
            Feed feed = parser.readFeed();
            //setDate(feed.getPubDate());
            for (FeedMessage message : feed.getMessages()) {
                if (!ids.contains(message.getId())) {
                    String length = message.getLength();
                    if (length != null && length.length()>0) try {
                        int seconds = Integer.parseInt(length);
                        int hours = seconds / 60 / 60;
                        seconds -= hours * 60 * 60;
                        int minutes = seconds / 60;
                        seconds -= minutes * 60;
                        length = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    } catch (Exception e){}
                    add(new Program(model, message.getId(), message.getTitle() + " " + message.getPubDate() + " " + length));
                    ids.add(message.getId());
                }
            }
        }
        status = null;
    }    
}
