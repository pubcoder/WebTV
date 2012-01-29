package webtv;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Common features for all nodes: refreshing and restructuring upon changes.
 * @author marius
 */
public class CommonNode extends DefaultMutableTreeNode
{
    protected DefaultTreeModel model;

    public CommonNode(DefaultTreeModel model){ this.model = model; }

    private Runnable changeAndStruct = new Runnable(){
                public void run() {
                    model.nodeChanged(CommonNode.this);
                    model.nodeStructureChanged(CommonNode.this);
                }
            };
    private Runnable change = new Runnable(){
                public void run() {
                    model.nodeChanged(CommonNode.this);
                }
            };

    private Runnable struct = new Runnable(){
                public void run() {
                    model.nodeStructureChanged(CommonNode.this);
                }
            };

    protected void repaintChangeAndStructure(){
        setUserObject(this.toString());
        SwingUtilities.invokeLater(changeAndStruct);
    }

    protected void repaintChange(){
        setUserObject(this.toString());
        SwingUtilities.invokeLater(change);
    }
    protected void repaintStructure(){
        setUserObject(this.toString());
        SwingUtilities.invokeLater(struct);
    }

    void refresh() {}

}
