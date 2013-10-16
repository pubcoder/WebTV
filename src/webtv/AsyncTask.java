package webtv;

import javax.swing.SwingUtilities;

/**
 *
 * @author marius
 */
public abstract class AsyncTask<T>
{
    public abstract void prepare();
    public abstract T compute();
    public abstract void success(T result);
    public abstract void failure(Exception e);
    
    public void dispatch() {
        prepare();
        new Thread(worker, this.getClass().getSimpleName()).start();
    }
    
    Runnable worker = new Runnable() {
        @Override
        public void run() {
            try {
                final T result = compute();
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        success(result);
                    }
                });
            } catch (final Exception e) {
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        failure(e);
                    }
                });
            }
        }
    };
}
