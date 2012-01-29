/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

/**
 *
 * @author marius
 */
public interface DownloadTool {
    void setProgressListener(ProgressListener l);
    void download(String url, String path);
    void cancelDownload();
}
