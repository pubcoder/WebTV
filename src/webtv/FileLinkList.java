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
public class FileLinkList extends CommonNode {
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
                        if (f.isFile()) {
                            add(new WGetNode(model, f.toURI()));
                        }
                    }
                    repaintStructure();
                }
            }
        }.start();
    }
}
