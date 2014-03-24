package webtv;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.filechooser.FileFilter;

/**
 * Allows editing settings like paths.
 * @author marius
 */
public class Settings
{
    private static final String osName = System.getProperty("os.name");
    private static final Preferences preferences = Preferences.userRoot().node("/net/webtv/settings");
    private static Component parent = null;
    
    private static final String wgetMenu = "wget executable";
    private static File wgetFile = findExecutable("wget");
    public static String wgetPath = wgetFile.getAbsolutePath();
    
    private static final String rtmpMenu = "rtmpdump executable";
    private static File rtmpFile = findExecutable("rtmpdump");
    public static String rtmpPath = rtmpFile.getAbsolutePath();

    private static final String vlcMenu = "vlc executable";
    private static File vlcFile = findExecutable("vlc");
    public static String vlcPath = vlcFile.getAbsolutePath();
    
    
    private static final ActionListener pathHandler = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || (f.isFile() && f.canExecute());
                }
                @Override
                public String getDescription() {
                    return "Executable files";
                }
            });
            String cmd = e.getActionCommand();
            switch(cmd){
                case wgetMenu:
                    chooser.setSelectedFile(wgetFile);
                    if (chooser.showDialog(parent, "Select") == JFileChooser.APPROVE_OPTION) {
                        wgetFile = chooser.getSelectedFile();
                        wgetPath = wgetFile.getAbsolutePath();
                        preferences.put("wget", wgetPath);
                    }
                    break;
                case rtmpMenu:
                    chooser.setSelectedFile(rtmpFile);
                    if (chooser.showDialog(parent, "Select") == JFileChooser.APPROVE_OPTION) {
                        rtmpFile = chooser.getSelectedFile();
                        rtmpPath = rtmpFile.getAbsolutePath();
                        preferences.put("rtmpdump", rtmpPath);
                    }
                    break;
                case vlcMenu:
                    chooser.setSelectedFile(vlcFile);
                    if (chooser.showDialog(parent, "Select") == JFileChooser.APPROVE_OPTION) {
                        vlcFile = chooser.getSelectedFile();
                        vlcPath = vlcFile.getAbsolutePath();
                        preferences.put("vlc", vlcPath);
                    }
                    break;                    
            }
        }
    };

    public static JMenu getMenu(Component parent) {
        Settings.parent = parent;
        JMenu menu = new JMenu("Settings");
        menu.add(wgetMenu).addActionListener(pathHandler);
        menu.add(rtmpMenu).addActionListener(pathHandler);
        menu.add(vlcMenu).addActionListener(pathHandler);
        return menu;
    }
    
    private static File findExecutable(String name)  
    {
        File result = null;
        String pref = preferences.get(name, null);
        if (pref != null) {
            result = new File(pref);
            if (!result.isFile() || !result.canExecute()) 
                result = null;
        }
        if (result == null) {
            String paths = System.getenv("PATH");  
            String[] dirs = paths.split(File.pathSeparator);
            String exe = name;
            if (osName.toLowerCase().contains("win")) exe += ".exe";
            for (String pathDir : dirs)
            {  
                result = new File(pathDir, name);  
                if (result.isFile() && result.canExecute())
                {  
                    break;
                }  
            }
            if (result == null) 
                result = new File(dirs[0], name);            
        }
        preferences.put(name, result.getAbsolutePath());
        return result;
    }
    
}
