/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

/**
 *
 * @author marius
 */
public interface ProgressListener {
    void started();
    void updateSize(int size);
    void update(String status);
    void incomplete(String status);    
    void finished();
}
