/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author marius
 */
public class Main extends JFrame implements TreeWillExpandListener, ActionListener, DownloadListener {
    JTree tree;
    SiteNode root;
    DefaultTreeModel model;
    JPopupMenu nodeMenu;
    JPopupMenu prodMenu;
    Queue<Product> queue = new ConcurrentLinkedQueue<Product>();
    HashSet<Product> active = new HashSet<Product>();
    public Main(){
        model = new DefaultTreeModel(null);
        root = new SiteMapNode(model, "TV3 Lithuania", "0");
        model.setRoot(root);
        tree = new JTree(model);
        root.refresh();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(false);
        tree.addTreeWillExpandListener(this);
        tree.addMouseListener(new PopupListener());
        //tree.addMouseListener(ml);
        //tree.getModel().valueForPathChanged(arg0, tree)
        tree.setShowsRootHandles(true);
        nodeMenu = createNodeMenu();
        prodMenu = createProductMenu();
        add(new JScrollPane(tree));
        setTitle("WebTV :-)");
        setSize(570, 750);
        setVisible(true);
//        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e){
                for (Product p: queue)
                    p.setScheduled(false);
                queue.clear();
                Iterator<Product> i = active.iterator();
                while (i.hasNext()) {
                    Product p = i.next();
                    active.remove(p);
                    p.cancelDownload();
                }
                System.exit(0);
            }
        });
    }

    public void finished(Product p) {
        active.remove(p);
        p.removeDownloadListener(this);
        if (active.isEmpty()) {
            p = queue.poll();            
            p.addDownloadListener(this);
            p.download();
            active.add(p);
        }
    }

    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                tree.setSelectionPath(path);
                Object o = path.getLastPathComponent();
                if (o instanceof Product) prodMenu.show(e.getComponent(),e.getX(), e.getY());
                else nodeMenu.show(e.getComponent(),e.getX(), e.getY());
            }
        }
    }


    /*
    MouseListener ml = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {            
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            //System.out.println("Mouse released at row="+selRow);
            if (selRow != -1) {
                if (e.getClickCount() == 2) {
                    //System.out.println("DoubleClick");
                    Object o = selPath.getLastPathComponent();
                    if (o instanceof Product) {
                        Product p = (Product)o;
                        p.download(model);
                    }
                }
            }
        }
    };
     */

    JPopupMenu createNodeMenu(){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem("Refresh");
        mi.addActionListener(this); menu.add(mi);
        return menu;
    }

    JPopupMenu createProductMenu(){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi = new JMenuItem("Enqueue");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Download");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Launch");
        mi.addActionListener(this); menu.add(mi);
        mi = new JMenuItem("Delete");
        mi.addActionListener(this); menu.add(mi);
        return menu;
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Main();
    }

    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        //System.out.println("willexpand");
        SiteNode node = (SiteNode)e.getPath().getLastPathComponent();
        node.refresh();
    }

    public void treeWillCollapse(TreeExpansionEvent arg0) throws ExpandVetoException {        
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
                    if (active.isEmpty()) {
                        p.addDownloadListener(this);
                        p.download();
                        active.add(p);
                    } else {
                        p.setScheduled(true);
                        queue.add(p);
                    }
                } else if ("Download".equals(cmd)) {
                    p.addDownloadListener(this);
                    p.download();
                    active.add(p);
                } else if ("Launch".equals(cmd)) {
                    p.launch();
                } else if ("Delete".equals(cmd)) {
                    p.delete();
                }
            } else {
                SiteNode node = (SiteNode)o;
                node.refresh();
            }
        }
    }

}
