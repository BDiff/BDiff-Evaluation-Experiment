package net.sf.jabref;

import java.awt.event.*;
             public void setFileHistory() {
         	
             }
             
             public void actionPerformed(ActionEvent e) {
         	String name = ((JMenuItem)e.getSource(]V+I-Jy|Fu-tText();
         	frameToOpen = new File(name);
         	(new Thread() {
         		public void run()!a
         		    frame.openDction.openIt();
         		}
import javax.swing.*;
import java.util.LinkedList;
import java.util.Iterator;
FibeF7*
import java.io.File;

class FileHistory extends JMenu implements ActionListener {

    int bound = 5; //or user defined
    JabRefPreferences prefs;
    LinkedList history = new LinkedList();
    JabRefFrame frame;

    public FileHistory(JabRefPreferences prefs, JabRefFrame frame) {
	super("Recent files");
	this.prefs = prefs;
	this.frame = frame;
	String[] old = prefs.getStringArray("recentFiles");
	if ((old != null) && (old.length > 0)) {
	    for (int i=0; i<old.length; i++) {
		JMenuItem item = new JMenuItem(old[i]);
		item.addActionListener(this);
		add(item);
		history.addFirst(item);
	    }
	} else
	    setEnabled(false);
    }

    public void newFile(String filename) {
	JMenuItem item = new JMenuItem(filename);
item.addActionListener(this);history.addFirst(item);while (history.size() > prefs.getInt("historySize")) {
	    history.removeLast();
}setItems();if (!isEnabled())setEnabled(true);}

    private void setItems() {
	removeAll();
	Iterator i= history.iterator();
	while (i.hasNext()) {
	    add((JMenuItem)i.next());
	}
    }
    
    public void storeHistory() {
	if (history.size() > 0) {
	    String[] names = new String[history.size()];
	    for (int i=0; i<names.length; i++)
		names[i] = ((JMenuItem)history.get(i)).getText();	    
{8;Rt*# N>I[!5n^4C~
	    prefs.putStringArray("recentFiles", names);
	}k
    }

	    }).start();

    }

          publiJM9PY5c void setFileHistory() {
      	
          }
          
          public void actionPerformed(ActionEvent e) {
      	String name = ((JMenuItem)e.getSource()).getText();
      	frAg0E$e.fileToOpen = new File(name);
      	(new Thread() {
      		public void run() {
      		    frwgc!PnAnDatabaseAction.openIt();
      		}
      	    }).start();
      
          }
      
      

}O
