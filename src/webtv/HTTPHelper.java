/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webtv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marius
 */
public class HTTPHelper 
{
    static final String agent = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.18 (KHTML, like Gecko) Chrome/18.0.1010.0 Safari/535.18";    
    String cookie = null;
    String origin = null;
    String requestedWith = null;

    StringBuilder doc;
    String status = null;
    int length = -1;
    int index = -1;

    public String getStatus() { return status; }
    public int getContentLength() { return length; }

    public void setOrigin(String origin) { this.origin = origin; }   
    public void setRequestedWith(String requestedWith) { this.requestedWith = requestedWith; }
    
    public String getCookie(){ return cookie; }
    public void setCookie(String cookie) { this.cookie = cookie; }
    public void resetDoc() { doc = null; index = -1; postfix = null; }
    
    public InputStream putStream(String url, String referer, String content, String type)
    {
        resetDoc();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", agent);
            con.setRequestProperty("Referer", url);
            if (requestedWith != null) con.setRequestProperty("X-Requested-With", requestedWith);
            con.setRequestProperty("Content-Length", ""+content.length());
            if (origin != null) con.setRequestProperty("Origin", origin);
            con.setRequestProperty("Content-Type", type);
            if (cookie != null) con.setRequestProperty("Cookie", cookie);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            int res = con.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {            
                OutputStream os = con.getOutputStream();
                os.write(content.getBytes());
                os.flush();
                os.close();
                InputStream is = con.getInputStream();
                cookie = con.getHeaderField("Set-Cookie");
                length = con.getContentLength();
                return is;
            } else {
                status = con.getResponseMessage();
                System.err.println(status);                
            }
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public InputStream getStream(String url, String referer)
    {
        resetDoc();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", agent);
            con.setRequestProperty("Referer", referer);
            if (requestedWith != null) con.setRequestProperty("X-Requested-With", requestedWith);
/*
            con.setRequestProperty("Accept", accept);
            con.setRequestProperty("Accept-Language", acceptLang);
            con.setRequestProperty("Accept-Encoding", acceptEnc);
            con.setRequestProperty("Accept-Charset", acceptCharset);
            con.setRequestProperty("Keep-Alive", keepAlive);
            con.setRequestProperty("Connection", connection);
*/
            if (cookie != null) con.addRequestProperty("Cookie", cookie);            
            con.setDoInput(true);
            con.setDoOutput(true);            
            con.connect();
            int res = con.getResponseCode();
            if (res == HttpURLConnection.HTTP_OK) {
                status = null;
                InputStream is = con.getInputStream();
                String tmp = con.getHeaderField("Set-Cookie");
                if (tmp != null) cookie = tmp;
                length = con.getContentLength();                
                return is;
            } else {
                status = con.getResponseMessage();
                System.err.println(status);
            }            
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            status = ex.getMessage();
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String readDoc(InputStream is)
    {
        doc = new StringBuilder();
        if (length<0) length = 4096;
        byte data[] = new byte[length];
        try {
            while (true) {
                int got = is.read(data, 0, length);
                if (got < 0) break;
                doc.append(new String(data, 0, got));
            }
            return doc.toString();
        } catch (IOException ex) {
            status = ex.getMessage();
            Logger.getLogger(HTTPHelper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }        
    }
    
    public String getDoc(String url, String referer)
    {
        InputStream is = getStream(url, referer);
        if (is == null) return null;
        return readDoc(is);
    }
    
    public String putDoc(String url, String referer, String content, String type)
    {
        InputStream is = putStream(url, referer, content, type);
        if (is == null) return null;
        return readDoc(is);
    }
        
    public int find(String postfix)
    {        
        index = doc.indexOf(postfix, index);
        if (index>=0) this.postfix = postfix;
        return index;
    }
    
    String postfix = null;
    public String findFirst(String prefix, String postfix)
    {
        index = 0;
        return findNext(prefix, postfix);
    }
    public String findNext(String prefix, String postfix)
    {
        index = doc.indexOf(prefix, index);
        if (index<0) return null;
        index += prefix.length();
        return findNext(postfix);
    }
    public String findNext(String postfix)
    {
        int start = index;        
        index = doc.indexOf(postfix, index);
        if (index<0) return null;
        this.postfix = postfix;
        return doc.substring(start, index);        
    }
    public int skipLastPostfix() { index += postfix.length(); return index; }

    public void addRequestProperty(String xRequestedWith, String xmlHttpRequest) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
