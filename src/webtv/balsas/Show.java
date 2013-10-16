/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv.balsas;

import javax.swing.tree.DefaultTreeModel;
import webtv.Product;
import webtv.WGetTool;

/**
 *
 * @author marius
 */
class Show extends Product
{
    final String url, referer;
    
    public Show(DefaultTreeModel model, String name, String link, String referer) {
        super(model, name, new WGetTool());
        this.url = link;
        this.referer = referer;
    }

    @Override
    public void download() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doReload() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
