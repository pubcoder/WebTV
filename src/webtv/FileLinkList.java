/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webtv;

import java.io.File;
import java.util.TreeSet;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author marius
 */
public class FileLinkList extends CommonNode {
    TreeSet<File> files = new TreeSet<File>();
    public FileLinkList(final DefaultTreeModel model){
        super(model);
        setAllowsChildren(true);
        setUserObject("Files");
    }
    @Override
    public boolean isLeaf(){ return false; }
    @Override
    public void refresh(){
        new Thread("FileLinkList"){
            @Override
            public void run(){
                File dir = new File("wget");
                if (dir.exists() && dir.isDirectory()) {
                    for (File f : dir.listFiles()) {
                        if (f.isFile() && !files.contains(f)) {
                            add(new WGetNode(model, f.toURI()));
                            files.add(f);
                        }
                    }
                    repaintStructure();
                }
            }
        }.start();
    }
}
