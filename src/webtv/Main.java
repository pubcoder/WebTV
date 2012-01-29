package webtv;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import webtv.tv3play.Program;
import webtv.tv3play.TV3Play;
import webtv.tv3webtv.SiteMapNode;
import webtv.zebra.ZebraList;

/**
 *
 * @author marius
 */
public class Main extends JFrame implements TreeWillExpandListener, 
                                            ActionListener, DownloadListener
{
    static final int MAX_DOWNLOADS = 3;
    JTree tree;
    DefaultMutableTreeNode root;
    FileLinkList files;
    DefaultTreeModel model;
    JPopupMenu nodeMenu;
    JPopupMenu prodMenu;
    Queue<Product> queue = new ConcurrentLinkedQueue<Product>();
    HashSet<Product> active = new HashSet<Product>();
    static final String pngs[] = {"tv3-16.png","tv3-24.png","tv3-32.png","tv3-48.png","tv3-64.png" };
    public Main(){
        model = new DefaultTreeModel(null);
        root = new DefaultMutableTreeNode("WebTV ;-)");
        model.setRoot(root);
        root.add(new TV3Play(model));
        root.add(new ZebraList(model));
        root.add(new SiteMapNode(model, "TV3 Lithuania", "0"));
        root.add(files = new FileLinkList(model));        
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(false);
        tree.addTreeWillExpandListener(Main.this);
        tree.addMouseListener(new PopupListener());
        //tree.getModel().valueForPathChanged(arg0, tree)
        //tree.setShowsRootHandles(true);
        tree.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e) {
                TreePath path = tree.getSelectionPath();
                Object o = path.getLastPathComponent();
                if (path == null) return;
                switch (e.getKeyChar()) {
                    case 10:
                    case 13:
                        execute(path);
                        break;
                    case KeyEvent.VK_PROPS:
                        Point point = tree.getMousePosition();
                        if (o instanceof Product) {
                            prodMenu.show(tree, point.x, point.y);
                        } else {
                            nodeMenu.show(tree, point.x, point.y);
                        }
                        break;
                    case 127:
                        if (o instanceof Product) {
                            Product p = (Product) o;
                            p.delete();
                        } else if (o instanceof WGetNode){
                            WGetNode w = (WGetNode) o;
                            w.delete();
                        }
                        break;
                    default:
                        System.out.println("Typed: "+((int)e.getKeyChar())+" "+e);
                }
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });
        nodeMenu = createNodeMenu();
        prodMenu = createProductMenu();
        add(new JScrollPane(tree));
        ArrayList<Image> imageList = new ArrayList<Image>(pngs.length);
        for (String name: pngs) {
            ImageIcon i = new ImageIcon(getClass().getResource("/webtv/"+name));
            if (i != null) imageList.add(i.getImage());
        }
        if (!imageList.isEmpty()) setIconImages(imageList);

        setTitle("WebTV :-)");
        setSize(800, 750);
        setVisible(true);
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    for (Product p : queue) {
                        p.setScheduled(false);
                    }
                    queue.clear();
                    Iterator<Product> i = active.iterator();
                    while (i.hasNext()) {
                        Product p = i.next();
                        p.cancelDownload();
                    }
                    active.clear();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
                System.exit(0);
            }
        });
    }

    public synchronized void finished(Product p) {
        boolean c = active.remove(p);
        if (!c) {
            System.err.println("Completed download ("+p+") but it was not among active!");
        }
        p.removeDownloadListener(this);
        if (active.size()<MAX_DOWNLOADS) {            
            if (!queue.isEmpty()) {
                //System.out.println("Starting new download");
                p = queue.poll();
                p.addDownloadListener(this);
                p.download();
                active.add(p);
            } else {
                //System.out.println("Empty queue");
            }
        }
    }

    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!showPopup(e)) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    //System.out.println("Mouse released at row="+selRow);
                    if (selRow != -1) {
                        if (e.getClickCount() == 2) {
                            //System.out.println("DoubleClick");
                            execute(path);
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    try {
                        String sel = (String) Toolkit.getDefaultToolkit().getSystemSelection().getData(DataFlavor.stringFlavor);
                        if (sel == null) {
                            sel = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                        }
                        URI url = new URI(sel);
                        String scheme = url.getScheme();
                        if ("http".equals(scheme) || "https".equals(scheme)) {
                            if (url.getHost().contains("tv3play.lt")) {
                                try {
                                    String filename = url.toURL().getFile();
                                    if (filename.indexOf('?') >= 0) {
                                        filename = filename.substring(0, filename.indexOf('?'));
                                    }
                                    if (filename.endsWith("/")) {
                                        filename = filename.substring(0, filename.length() - 1);
                                    }
                                    String[] path = filename.split("/");
                                    String id = path[path.length - 1];
                                    files.add(new Program(model, id));
                                } catch (MalformedURLException ex) {
                                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                files.add(new WGetNode(model, url));
                            }
                            tree.expandRow(1);
                            files.repaintStructure();
                        }
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    } catch (java.net.MalformedURLException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedFlavorException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        private boolean showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                tree.setSelectionPath(path);
                Object o = path.getLastPathComponent();
                if (o instanceof Product || o instanceof WGetNode) {
                    prodMenu.show(e.getComponent(), e.getX(), e.getY());
                } else if (o instanceof SiteNode || o instanceof FileLinkList) {
                    nodeMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                return true;
            } else return false;
        }
    }

    public void execute(TreePath path) {
        Object o = path.getLastPathComponent();
        if (o instanceof Product) {
            Product p = (Product) o;
            Product.State s = p.getState();
            switch (s) {
                default:
                case Unknown:
                case Incomplete:
                case Deleted:
                    enqueue(p);
                    break;
                case Downloading:
                case Ready:
                case Exists:
                    p.play();
                    break;
                case Scheduled:
                    if (active.size() < MAX_DOWNLOADS) {
                        download(p);
                    }
                    break;
                case Loading:
                    break;
            }
        } else if (o instanceof SiteNode){
            tree.expandPath(path);
        } else if (o instanceof FileLinkList){
            ((FileLinkList)o).refresh();
        } else if (o instanceof WGetNode) {
            WGetNode w = (WGetNode) o;
            if (w.isReady()) w.play();
            else if (!w.isBusy()) w.download();
        }
    }

    MouseListener ml = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {            
        }
    };

    final JPopupMenu createNodeMenu(){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem("Refresh");
        mi.addActionListener(this); menu.add(mi);
        return menu;
    }

    final JPopupMenu createProductMenu(){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem("Launch");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Enqueue");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Download");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Play");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Delete");
        mi.addActionListener(this); menu.add(mi);
        return menu;
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main();
    }

    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        //System.out.println("willexpand");
        CommonNode node = (CommonNode)e.getPath().getLastPathComponent();
        node.refresh();
    }

    public void treeWillCollapse(TreeExpansionEvent arg0) throws ExpandVetoException {        
    }

    protected void enqueue(Product p) {
        if (active.size()<MAX_DOWNLOADS) {
            p.addDownloadListener(this);
            p.download();
            active.add(p);
        } else {
            p.setScheduled(true);
            queue.add(p);
        }
    }

    protected void download(Product p) {
        queue.remove(p);
        p.addDownloadListener(this);
        p.download();
        active.add(p);
    }

    public void actionPerformed(ActionEvent e) {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            String cmd = e.getActionCommand();
            Object o = path.getLastPathComponent();
            if (o instanceof Product) {
                Product p = (Product)o;
                if ("Refresh".equals(cmd)) {
                    p.refresh();
                } else if ("Enqueue".equals(cmd)) {
                    enqueue(p);
                } else if ("Download".equals(cmd)) {
                    if (active.size() < MAX_DOWNLOADS) {
                        download(p);
                    } else {
                        enqueue(p);
                    }
                } else if ("Play".equals(cmd)) {
                    p.play();
                } else if ("Delete".equals(cmd)) {
                    p.delete();
                }
            } else if (o instanceof SiteNode) {
                SiteNode node = (SiteNode)o;
                node.refresh();
            } else if (o instanceof WGetNode) {
                WGetNode w = (WGetNode)o;
                if ("Download".equals(cmd)) {
                    w.download();
                } else if ("Play".equals(cmd)) {
                    w.play();
                } else if ("Delete".equals(cmd)) {
                    w.delete();
                }
            }
        }
    }

}
