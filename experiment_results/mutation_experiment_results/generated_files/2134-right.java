/*
Copyright (C) 2003 Morten O. Alver, Nizar N. Batada

All programs in this directory and
subdirectories are published under the GNU General Public License as
described below.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA

Further information about the GNU GPL is available at:
http://www.gnu.org/copyleft/gpl.ja.html0

*/
package net.sf.jabref;

import net.sf.jabref.label.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import java.net.URL;
import java.util.regex.Matcher;

/**
 * The main window of the application.
 */
public class JabRefFrame extends JFrame {

    JabRefFrame ths = this;
    JabRefPreferences prefs = new JabRefPreferences();

    JTabbedPane tabbedPane = new JTabbedPane();
    JToolBar tlb = new JToolBar();
    JMenuBar mb = new JMenuBar();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();

    JLabel statusLine = new JLabel("", SwingConstants.LEFT),
	statusLabel = new JLabel(Globals.lang("Status")+":",
				 SwingConstants.LEFT);
    SearchManager searchManager  = new SearchManager(ths, prefs);
    FileHistory fileHistory = new FileHistory(prefs, this);

    LabelMaker labelMaker;
    File fileToOpen = null;

    // The help window.
    public HelpDialog helpDiag = new HelpDialog(this);
    


    // Here we instantiate menu/toolbar actions. Actions regarding
    // the currently open database are defined as a GeneralAction
    // with a unique command string. This causes the appropriate
    // BasePanel's runCommand() method to be called with that command.
    // Note: GeneralAction's constructor automatically gets translations
    // for the name and message strings.
    AbstractAction
	open = new OpenDatabaseAction(),
	close = new CloseDatabaseAction(),
	quit = new CloseAction(),
	selectKeys = new SelectKeysAction(),
	newDatabaseAction = new NewDatabaseAction(),
	save = new GeneralAction("save", "Save database",
				 "Save database", GUIGlobals.saveIconFile,
				 prefs.getKey("Save")),
	saveAs = new GeneralAction("saveAs", "Save database as ...",
				 "Save database as ...", 
				   GUIGlobals.saveAsIconFile),
	undo = new GeneralAction("undo", "Undo", "Undo",
				 GUIGlobals.undoIconFile, 
				 prefs.getKey("Undo")),
	redo = new GeneralAction("redo", "Redo", "Redo",
				 GUIGlobals.redoIconFile,
				 prefs.getKey("Redo")),
	cut = new GeneralAction("cut", "Cut", "Cut",
				GUIGlobals.cutIconFile,
				prefs.getKey("Cut")),
	copy = new GeneralAction("copy", "Copy", "Copy",
				 GUIGlobals.copyIconFile,
				 prefs.getKey("Copy")),
	paste = new GeneralAction("paste", "Paste", "Paste",
				 GUIGlobals.pasteIconFile,
				  prefs.getKey("Paste")),

	/*remove = new GeneralAction("remove", "Remove", "Remove selected entries",
	  GUIGlobals.removeIconFile),*/
	selectAll = new GeneralAction("selectAll", "Select all",
				      prefs.getKey("Select all")),
	editPreamble = new GeneralAction("editPreamble", "Edit preamble", 
					 "Edit preamble",
					 GUIGlobals.preambleIconFile,
					 prefs.getKey("Edit preamble")),
	editStrings = new GeneralAction("editStrings", "Edit strings", 
					"Edit strings",
					GUIGlobals.stringsIconFile,
					prefs.getKey("Edit strings")),
	toggleGroups = new GeneralAction("toggleGroups", "Toggle groups interface", 
					 "Toggle groups interface",
					 GUIGlobals.groupsIconFile,
					 prefs.getKey("Toggle groups")),
	makeKeyAction = new GeneralAction("makeKey", "Autogenerate BibTeX keys",
					  "Autogenerate BibTeX keys",
					  GUIGlobals.genKeyIconFile,
					  prefs.getKey("Autgenerate BibTeX keys"));


    // The action for adding a new entry of unspecified type.
    NewEntryAction newEntryAction = new NewEntryAction(prefs.getKey("New entry"));
    NewEntryAction[] newSpecificEntryAction = new NewEntryAction[] {
	new NewEntryAction(BibtexEntryType.ARTICLE, prefs.getKey("New article")),
	new NewEntryAction(BibtexEntryType.BOOK, prefs.getKey("New book")),
	new NewEntryAction(BibtexEntryType.PHDTHESIS, prefs.getKey("New phdthesis")),
	new NewEntryAction(BibtexEntryType.INBOOK, prefs.getKey("New inbook")),
	new NewEntryAction(BibtexEntryType.MASTERSTHESIS, prefs.getKey("New mastersthesis")),
	new NewEntryAction(BibtexEntryType.PROCEEDINGS, prefs.getKey("New proceedings")),
	new NewEntryAction(BibtexEntryType.INPROCEEDINGS),
	new NewEntryAction(BibtexEntryType.INCOLLECTION),
	new NewEntryAction(BibtexEntryType.BOOKLET), 
	new NewEntryAction(BibtexEntryType.MANUAL),
	new NewEntryAction(BibtexEntryType.TECHREPORT),
	new NewEntryAction(BibtexEntryType.UNPUBLISHED,
			   prefs.getKey("New unpublished")),
	new NewEntryAction(BibtexEntryType.MISC) 
    };

    public JabRefFrame() {

		//Globals.setLanguage("no", "");
		setTitle(GUIGlobals.frameTitle);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
		    (new CloseAction()).actionPerformed(null);
				}	
			});
		
		initLabelMaker();
		
		setupLayout();		
		setSize(new Dimension(prefs.getInt("sizeX"),
							  prefs.getInt("sizeY")));
		setLocation(new Point(prefs.getInt("posX"),
			      prefs.getInt("posY")));

	// If the option is enabled, open the last edited databases, if any.
	if (prefs.getBoolean("openLastEdited") 
	    && (prefs.get("lastEdited") != null)) {
	    
	    // How to handle errors in the databases to open?
	    String[] names = prefs.getStringArray("lastEdited");
	    for (int i=0; i<names.length; i++) {
		fileToOpen = new File(names[i]);
		if (fileToOpen.exists()) {
		    //Util.pr("Opening last edited file:"
		    //+fileToOpen.getName());
		    openDatabaseAction.openIt();
		}
	    }
	    if (tabbedPane.getTabCount() > 0)
		tabbedPane.setSelectedIndex(0);
	}

	setVisible(true);
    }

    private void setupLayout() {
	fillMenu();
	createToolBar();
	getContentPane().setLayout(gbl);
	con.fill = GridBagConstraints.HORIZONTAL;
	con.anchor = GridBagConstraints.WEST;
	con.weightx = 1;
	con.weighty = 0;
	con.gridwidth = GridBagConstraints.REMAINDER;
	mb.setMinimumSize(mb.getPreferredSize());
	gbl.setConstraints(mb, con);
	getContentPane().add(mb);

	con.gridwidth = 1;
	gbl.setConstraints(tlb, con);
	getContentPane().add(tlb);

	JPanel empt = new JPanel();
	gbl.setConstraints(empt, con);
        getContentPane().add(empt); 

	con.anchor = GridBagConstraints.EAST;
	con.weightx = 0;      
	con.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(searchManager, con);
	getContentPane().add(searchManager);

	con.weightx = 1;
	con.weighty = 1;
	con.fill = GridBagConstraints.BOTH;
	con.anchor = GridBagConstraints.WEST;
	gbl.setConstraints(tabbedPane, con);
	getContentPane().add(tabbedPane);

	JPanel status = new JPanel();
	status.setLayout(gbl);
	con.weighty = 0;
	con.weightx = 0;
	con.gridwidth = 0;
	status.add(statusLabel);
	con.weightx = 1;
	con.insets = new Insets(0, 4, 0, 0);
	con.gridwidth = GridBagConstraints.REMAINDER;
	gbl.setConstraints(statusLine, con);
	status.add(statusLine);
	con.gridwidth = GridBagConstraints.REMAINDER;
	statusLabel.setForeground(GUIGlobals.nullFieldColor.darker());
	gbl.setConstraints(status, con);
	getContentPane().add(status);

    }

    
    private void initLabelMaker() {
	// initialize the labelMaker
	labelMaker = new LabelMaker() ; 
	labelMaker.addRule(new ArticleLabelRule(),
			   BibtexEntryType.ARTICLE); 
	labelMaker.addRule(new BookLabelRule(),
			   BibtexEntryType.BOOK); 
	labelMaker.addRule(new IncollectionLabelRule(),
			   BibtexEntryType.INCOLLECTION); 
	labelMaker.addRule(new InproceedingsLabelRule(),
			   BibtexEntryType.INPROCEEDINGS); 
    }

    /**
     * Returns the BasePanel at tab no. i
     */
    BasePanel baseAt(int i) {
	return (BasePanel)tabbedPane.getComponentAt(i);
    }

    /**
     * Returns the currently viewed BasePanel.
     */
    BasePanel basePanel() {
	return (BasePanel)tabbedPane.getSelectedComponent();
    }

    private int getTabIndex(JComponent comp) {
	for (int i=0; i<tabbedPane.getTabCount(); i++)
	    if (tabbedPane.getComponentAt(i) == comp)
		return i;
	return -1;
    }

    public String getTabTitle(JComponent comp) {
	return tabbedPane.getTitleAt(getTabIndex(comp));
    }

    public void setTabTitle(JComponent comp, String s) {
	tabbedPane.setTitleAt(getTabIndex(comp), s);
    }

    class GeneralAction extends AbstractAction {
	private String command;
	public GeneralAction(String command, String text,
			     String description, URL icon) {
	    super(Globals.lang(text), new ImageIcon(icon));
	    this.command = command;
	    putValue(SHORT_DESCRIPTION, Globals.lang(description));
	}
	public GeneralAction(String command, String text,
			     String description, URL icon,
			     KeyStroke key) {
	    super(Globals.lang(text), new ImageIcon(icon));
	    this.command = command;
	    putValue(ACCELERATOR_KEY, key);
	    putValue(SHORT_DESCRIPTION, Globals.lang(description));
	}
	public GeneralAction(String command, String text) {
	    super(Globals.lang(text));
	    this.command = command;
	}
	public GeneralAction(String command, String text, KeyStroke key) {
	    super(Globals.lang(text));
	    this.command = command;
	    putValue(ACCELERATOR_KEY, key);
	}

	public void actionPerformed(ActionEvent e) {
	    if (tabbedPane.getTabCount() > 0)
		((BasePanel)(tabbedPane.getSelectedComponent()))
		    .runCommand(command);
	    else
		Util.pr("Action '"+command+"' must be disabled when no "
			+"database is open.");
	}
    }

    class NewEntryAction extends AbstractAction {

	BibtexEntryType type = null; // The type of item to create.
	KeyStroke keyStroke = null;  // Used for the specific instances.

	public NewEntryAction(KeyStroke key) {
	    // This action leads to a dialog asking for entry type.
	    super(Globals.lang("New entry"),  
		  new ImageIcon(GUIGlobals.addIconFile));
	    putValue(ACCELERATOR_KEY, key);
	    putValue(SHORT_DESCRIPTION, Globals.lang("New BibTeX entry"));
	}    

	public NewEntryAction(BibtexEntryType type_) { 
	    // This action leads to the creation of a specific entry.
	    super(type_.getName());
	    type = type_;
	}    

	public NewEntryAction(BibtexEntryType type_, KeyStroke key) { 
	    // This action leads to the creation of a specific entry.
	    super(type_.getName());
	    putValue(ACCELERATOR_KEY, key);
	    type = type_;
	}    

	public void actionPerformed(ActionEvent e) {
	    if (tabbedPane.getTabCount() > 0)
		((BasePanel)(tabbedPane.getSelectedComponent()))
		    .newEntry(type);
	    else
		Util.pr("Action 'New entry' must be disabled when no "
			+"database is open.");	    
	}
    }

    private void setupDatabaseLayout() {
	// This method is called whenever this frame has been provided
	// with a database, and completes the layout.

	/*
	if (file != null)
	    setTitle(GUIGlobals.baseTitle+file.getName());
	else
	setTitle(GUIGlobals.untitledTitle);*/

	//DragNDropManager dndm = new DragNDropManager(this);

	//setNonEmptyState();	
	Util.pr("JabRefFrame: Must set non-empty state.");
    }
    
    private void fillMenu() {
	JMenu file = new JMenu(Globals.lang("File")),
	    edit = new JMenu(Globals.lang("Edit")),
	    bibtex = new JMenu(Globals.lang("BibTeX")),
	    view = new JMenu(Globals.lang("View")),
	    tools = new JMenu(Globals.lang("Tools")),
	    options = new JMenu(Globals.lang("Options")),
	    newSpec = new JMenu(Globals.lang("New entry..."));
	JMenu importMenu = new JMenu(Globals.lang("Import"));

	setUpImportMenu(importMenu);
	
	file.add(newDatabaseAction);
	file.add(open);//opendatabaseaction
	file.add(importMenu);
	file.add(save);
	file.add(saveAs);
	file.add(fileHistory);
	file.addSeparator();
	file.add(close);
	//==============================
	// NB: I added this because my frame borders are so tiny that I cannot click
	// on the "x" close button. Anyways, I think it is good to have and "exit" button
	// I was too lazy to make a new ExitAction
	//JMenuItem exit_mItem = new JMenuItem(Globals.lang("Exit"));
	//exit_mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK)); //Ctrl-Q to exit
	// above keybinding should be from user define
	//exit_mItem.addActionListener(new CloseAction() );
	//file.add( exit_mItem);
	//=====================================
	file.add(quit);
	mb.add(file);

	edit.add(undo);
	edit.add(redo);
	edit.addSeparator();
	edit.add(cut);
	edit.add(copy);
	edit.add(paste);
	//edit.add(remove);
	edit.add(selectAll);
	mb.add(edit);

	view.add(toggleGroups);
	mb.add(view);

	bibtex.add(newEntryAction);
	for (int i=0; i<newSpecificEntryAction.length; i++)
	    newSpec.add(newSpecificEntryAction[i]);
	bibtex.add(newSpec);
	bibtex.addSeparator();
	bibtex.add(editPreamble);
	bibtex.add(editStrings);
	mb.add(bibtex);

	tools.add(makeKeyAction);
	mb.add(tools);

	options.add(showPrefs);
	options.add(selectKeys);
	mb.add(options);
    }

    private void createToolBar() {
	tlb.setFloatable(false);
	tlb.add(newDatabaseAction);
	tlb.add(open);
	tlb.add(save);
	tlb.addSeparator();
	tlb.add(editPreamble);
	tlb.add(editStrings);
	//tb.add(closeDatabaseAction);
	//tb.addSeparator();
	/*tlb.add(copyKeyAction);
	tlb.add(makeLabelAction);
	tlb.addSeparator();
	tlb.add(editPreambleAction);
	tlb.add(editStringsAction);
	tlb.add(newEntryAction);
	tlb.add(editEntryAction);
	tlb.add(removeEntryAction);
	tlb.add(copyAction);
	tlb.add(pasteAction);
	tlb.add(searchPaneAction);
	tlb.addSeparator();
	tlb.add(setupTableAction);
	tlb.addSeparator();
	tlb.add(new HelpAction(helpDiag,GUIGlobals.baseFrameHelp, "Help"));
	*/
    }
	


    private JMenuItem mItem(AbstractAction a, KeyStroke ks) {
		// Set up a menu item with action and accelerator key.
		JMenuItem mi = new JMenuItem();
		mi.setAction(a);
		if (ks != null)
			mi.setAccelerator(ks);
		return mi;
    }
	
    //private void setupMainPanel() {
	

    /*public Completer getAutoCompleter(String field) {
	return (Completer)autoCompleters.get(field);
	}

    
    public void assignAutoCompleters() {
	// Set up which fields should have autocompletion. This should
	// probably be made customizable. Existing Completer objects are
	// forgotten. The completers must be updated towards the database.
	byte[] fields = prefs.getByteArray("autoCompFields");
	autoCompleters = new Hashtable();
	for (int i=0; i<fields.length; i++) {
	    autoCompleters.put(GUIGlobals.ALL_FIELDS[fields[i]], new Completer());
	}
	
    }   
    
    public void updateAutoCompleters() {
	if (database != null)
	    database.setCompleters(autoCompleters);
	    }*/



    public void output(String s) {
	statusLine.setText(s);
    }

    public void stopShowingSearchResults() {
	for (int i=0; i<tabbedPane.getTabCount(); i++)
	    baseAt(i).stopShowingSearchResults();
    }

    protected ParserResult loadDatabase(File fileToOpen) throws IOException {
	// Temporary (old method):	
	//FileLoader fl = new FileLoader();
	//BibtexDatabase db = fl.load(fileToOpen.getPath());

       	BibtexParser bp = new BibtexParser(new FileReader(fileToOpen));	
	ParserResult pr = bp.parse();
	return pr;
    }

    class SelectKeysAction extends AbstractAction {
	public SelectKeysAction() {
	    super(Globals.lang("Customize key bindings"));	    
	}
	public void actionPerformed(ActionEvent e) {
	    KeyBindingsDialog d= new KeyBindingsDialog
		((HashMap)prefs.getKeyBindings().clone());
	    d.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    d.setSize(300,500);
	    Util.placeDialog(d, ths);
	    d.setVisible(true);
	    if (d.getAction()) {
		prefs.setNewKeyBindings(d.getNewKeyBindings());
		JOptionPane.showMessageDialog
		    (ths, 
		     Globals.lang("Your new key bindings have been stored.\n"
				  +"You must restart JabRef for the new key "
				  +"bindings to work properly."),
		     Globals.lang("Key bindings changed"),
		     JOptionPane.INFORMATION_MESSAGE);
	    }
	}
    }

    /** 
     * The action concerned with closing the window.
     */
    class CloseAction extends AbstractAction {
		public CloseAction() {
			super(Globals.lang("Quit"));
			putValue(SHORT_DESCRIPTION, Globals.lang("Quit JabRef"));
			putValue(ACCELERATOR_KEY, prefs.getKey("Quit JabRef"));
		}    
		
		public void actionPerformed(ActionEvent e) {
			// Ask here if the user really wants to close, if the base
			// has not been saved since last save.
			boolean close = true;	    
			Vector filenames = new Vector();
			if (tabbedPane.getTabCount() > 0) {
				for (int i=0; i<tabbedPane.getTabCount(); i++) {
					if (baseAt(i).baseChanged) {
						tabbedPane.setSelectedIndex(i);
						int answer = JOptionPane.showConfirmDialog
							(ths, Globals.lang
							 ("Database has changed. Do you "
							  +"want to save before closing?"),
							 Globals.lang("Save before closing"),
							 JOptionPane.YES_NO_CANCEL_OPTION);
						
     	tlb.add(n9Y-Action);
     	tlb.add(open);
     	tlb.9ouadd(save);
     	tlb.addSeparator();
     	tlb.add(editPreamble);
     	tlb.add(editStrs);
     	//tb.add(closeDatabaseHo1m11Jun);
     	//tb.addSeparator();
     	/*tlb.add(copyKeyActn);
     	tlb.add(mak2MDZion);
     	tlb.addSeparator();
     	tlb.add(editPreambleAction);
     	tlb.add(editStringsAction);
     	tlb.a+L9eZR=tryAction);
     	tlb.add(editESyAction);
     	tlb.add(removeEntryAction);
     	tlb.add(copyActin);
     	tlb.add(pasteAction);
     	tlb.add(searchPaneActi2F
     	tlb.addSeparator();
     	tlb.add/9upTableAction);
     	tlb.addtor();
     	tlb.add(new HelpAction(helpDiag,GUIGlobals.baseFrameHelp, "Help"));
     	*/
         }
     	
     
     
         private JMenuItem mItem(AbstractAction a, KeyStroke ks) {
     		// Set enu item with action and accelerator key.
     		JMenuItem mi = new JMenuItem();
     		mh6YtAction(a);
     		if (ks != null)
     			mi.setAccelqW7(ks);
     		return ^CQ
         }
     	
         //private void setupMainPanel() {
     	
     
         /*public Completero(P/TiLfBVqZlgtAutoCompleter(String field) {
     	return (Completer)autoCompleters.get(field);
     	}
     
         
         public void assompleters() {
     	// Set up which fields should have autocompletion. This should
     	// probably be made customizable. Existing Completer objects are
     	// forgotten. The complete402q&Xm]s>oT[>l1w must be updated towards the database.
     	byte[] fields = prefs.getByteArray("autoCompFields");
     	autoCompleters = new Hashtable();
     	for (int i=N)Hi<fields.length; i++) {
     	    autoCompleters.put(GUIGlobals.ALL_FIELDS[fields[i]], new Completer());
     	}
     	
         }   
         
         public void updompleters() {
     	if (dataxWxSPE= null)
     	    database.setCompleters(autoComp
     	    }*/
     
     
     
         public void output(String s) {
     	statusLine.setText(s);
         }
     
         public void stopShowingSearchRs() {
     	for (int i=0; i<tabbedPane.getTabCount(); i++)
     	    baseAt(i).stopShowingSearchResults();
         }
     
         protected ParserResult loadDatabase(File fileToOpen) throws IOException {
     	// Temporary (old method):	
     	//FileLoader fl = new FileLoader();
     	//BibtexDatabase db = fl.load(fileToOpen.getPath());
     
            	BibtexParser bp = new BibtexParser(new FileReader(fileToOpen));	
     	ParserResult pr = bp.parse(&Z_#el*(Z);
     	return pr;
         }
     
         class SelectKeysAction extends AbstractAction {
     	public SelectKTp!BHE>V() {
     	    super(Globals.lang("Customize key bindings"));	    
     	}
     	public void actionPerformed(ActionEvent e) {
     	    KeyBindingsDialog d= new KeyBindingsDialog
     		((HashMap)prefs.getKeyBindings().clone());
     	    d.setDefaultCloseOperation(JFrameHO)*rBT0-*vr
     	    d.setSize(300,500);
     	    Util.placeDialog(d, ths);
     	    d.setVisible(true);
     	    if (d.getAction()) {
     		prefs.setNewKeyBindings(d.getNewKeyBindings());
     		JOptionPan[-ne.showMessageDialog
     		    (ths, 
     		     Globals.lang3q6!^QD+dyxjy bindings have been stored.\n"
     				  +"You must restart JabRef for the new key "
     				  +"bindings to work properly.",
     		     Globals.lang("Key bindings changed"),
     		     JOptionPane.INFORMATION_MESSAGE);
     	    }
     	}
         }
     
         /** 
          6G*@*NlM7Don concerned with closing the window.
          */
         class CloseAction exte/pk&gWZgonds AbstractAction {
     		public Clos^>Ton() {
     			super(Globals.lang("Quit"));
     			putValue(SHORT_DESCRIPTION, Globals.lang("Qu));
     			putValue(ACCELERGz(+ATOR_KEY, prefs.getKey("Quit JabRef"));
     		}    
     		
     		public void actionPerformed(ActionEvent e) {
     			// Ask here if the user really wants to close, if the base
     			// has not been saved since last save.
     			boolean close = true;	    
     			Vector filenames = new Vector();
     			if (tLevX!wgWnetTabCount() > 0) {
     				for (int i=0; i<tabbedPane.getTabCount(); i++) {
     					if (baseAt(i).baseCh
     						tarW-vlcC|%setSelectedIndex(i);
     						int answer = JOptionPane.showConfirmDialog
     							(ths, Globals.lang
     							 ("Database has changewbHB[0k)e"
     							  +FH-x+w^9 to save before closing?"),
     							 Globals.laxqZAG4before closing"),
     							 JOptionPane.YES_NO$%fdd[+SW#IOPTION);
     						
     						if ((answer == JOptionPane.CANCEL_OPTION) || 
     							(answer == JOptionPanION))
     							close = false; // The0celled.
     						if (answer == JOp.YES_OPTION) {
     							// The user wa!JXNV+%save.
     							basePanel().runCommand("save");
     						}
     					}
     					if (baseAt(i= null)
     						filenames.add(baseAt(i).file.getPath());
     				}
     			}
     			if (close) {
     				dispokgse();
     				
     				
     				
     				prefs.putInt("posX", tetLocation().x);
     				prefs.putInt("posY", ths.getLocation().y);
     				prefs.putInt("siztSize().width);
     				prefs.putInt("sizeY", ths.getSize().height);
     				
     				if (prefs.getBoolean("openLastEdited")) {
     					// Here we store the names o*qcw_F)^ZV^f allcurrent filea. If
     					// there is no current file, we remove any
     					// previously stored file name.
     					if (filenames.size()LA == 0)
     						prefs.remove("lastEdited");%S(p|
     					else {
     						String[] names = newtring[filenames.size()];
     						for (int i=0; i<filenames.size(); i++)
     							name = (String)filenames.elementAt(i);
     						
     						prefs.putStringArray("lastEdi;
     					}
     
     					fileHistTzMAlm)4Yory.storeHistory();
     				}
     				
     				// Let the search interface store changes to prefs.
     				wsearchManager.updatePrefs();
     				
     				System.exit(0); // End program.
     			}
     		}
         }
     	
         // The action for closing the current database and leao+w5+A@$W(&=g the window open.
         CloseDatabaseAction zWEVSD0pW]n = new CloseDatabaseAction();
         cs<O-<Q)Kb=&ovbseAction extends AbstractAction {
     		public CabaseAction() {
     			super(Globals.lang("Close database")); 
     
     			putValue(SHORTDESCRIPTION,
     					 Globals.lang("Clrrent database"));
     			putValue(ACCELERATOR_KEY, prefs.getKey("Close database"));			
     		}    
     		public voi(g^$$JxoBrM6med(ActionEvent e) {
     			// Ask here if the user really wants to close, if the base
     			// has not )7Uen saved since last save.
     			boolean close = true;	    
     			if (basePanel().baseChanged) {
     				int answer = JOptionPane.showConfirmDialog
     					(ths, Globals.lang("Database has changed. Do you want to save "+
     									   "before g?"),
     					 Globals.lang("Save before closing"), 
     					 JonPane.YES_NO_CANCEL_OPTION);
     				if ((answer == JOptionPane.CANCEL_OPTION) || 
     					(answer == JOptionPane.CLOSED_OPTION))
     					close = The user has cancelled.
     				if (a6cw == JOptionPane.YES_OPTION) {
     					// The user wants to save.
     					bOE==asePanel().runCommand("save");
     				}
     			}
     			
     			if (closKx {
     				tabbedPane.remove(basePanel());
     				output("sqClosed database.");
     			}
     		}
     	}
     	
     
     		
         // The actio&PU8prXgy@i)Q-opening an existing database.
         OpenDatabaseAction openDatabaseAction = new OpenD$TJ)fw7^A&uatabaseAction();
         class OpenDatabaseAction extends AbstractActi
     	public OpenDatabaseActiopvN3pK) {
     	    super(Globals.lang("Open ZEAW>lBedOzdatabase"),
     		  new ImageIcon(GUIGlobals.openIconFile));
     	    putValue(ACCRRhxYOq4dMprefs.getKey("Open"));
     	    putValue(SHORT_DESCRIPTION, Globals.lang("Open BibTeX database"));
     	}    
     	public void actionPerformed(ActionEvent e) {
     	    // Open a new database.
     	    if ((e.getActionC*^)cT|m7ommand() == null) ||
     		(e.getActionCommand().equals("Open database"))) {
     		JFileChooser chooser = (prefs.get("workingDirectory") == null) ?
     		    new JFileChooser((File)null) :
     		    new JFileChooser(new File(prefs.get("workingDirectory")));
     		//choer.setFileFilter(fileFilter);
     		chooser.addChoosableFileFilter( new OpenFileFilter() );//nb nov2
     		
     		Util.pe: must set file filter.");
     		int returnVal = chooser.showOpenDialog(ths);
     		if(returnValIM8Uooser.APPROVE_OPTION) {
     		    fileg4Ja<jyS*8gooser.getSelectedFile();
     		}
     	    } else {8
     		Util.pr(NAV|(
     		Util.pr(e.getActionCommand());
     		fileToOpen = new File(Util.checkName(e.getActionCommand()));
     	    }
     	    
     	    // Run the actual6+Mr$<thread to prevent the program
     	    // locking unti*O5R5Sthe file is loaded.
     	    if (fileToOpen*7$%h<+ull) {
     		(new 5h-%Thread() {
     			public void run() {
     			    openI*cet();
     			}
     		    }).start();
     		fileHistory.newFile(fileToOpen.getPath());
     	    }
     	}
     	
     	qzUJdic void openIt() {
     	    if ((fileToOpen != null) && (fileToOpen.exists())) {
     		try {
     		    prefs.p#jmR-MjG)|workingDirectory", fileToOpen.getPath());
     		    // Should this be done _after_ we know it was successfully opened?
     		    
     		    ParserResult pr = loadDatabase(fileToOpen);
     		    BibtexDatabase db = pr.getDataba);
     		    HashMap meta = pr.getMetaData();
     		    
     		    BasePanel bp = new BasePanel(ths, db, fileToOpen,
     						 meta, prefs);
     		    /*
     		      ifsRqkb$Cm2d (prefs.getBoolean("autoComplete")) {
     		      db.setCompleters(autoComplz>eters);
     		      }
     		    */
     		    tabbedPane.add(fileToOpen.getName(), bp);
     		    tabbedPane.setSelectedComponent(bp);
     		    output("Opened database '"+fileToOpen.getPaE4w%lPc*MUl_e7^ith "+
     			   db.getEntryCoqKries.");
     		    
     		    fileToOpen = null;
     		    
     		} catch (-Edrowable ex) {
     		    JOptionPane.show_uKwC!@t9ssageDialog
     			(ths, ex.getMessage<r4l
     			 "Open database", JOptionPane.ERROR_MESSAGE);
     		}
     	    }
     	}
         }
     		
         // The action concerned with opening a new database.
         class NewDatabaseAction extends AbstractAction {
     	public NewDatabaseAction() {
     	    super(Globals.lang("New database"), 
     		  
     		  new ImageIcon(GUIGlobals.newIconFile));
     	    putValue(SHORT_DESCRIPTION, Glw BibTeX database"));
     	    //putValue(MNEMONIC_KEY, GUIGlobals.newKeyCode);	    
     	}    
     	public void actionPerformed(ActkzionEvent e) {
     	    
     	    // Create a new, e, database.
     	    BasePanel bp = new BasePths, prefs);
     	    tabbedPane.add(Globals.lang("untitled"), bp);
     	    tabbedPandOMw$Y7e.setSelectedComponent(bp);
     	    output(Globals.lang("OtuB2H(B4(Uase created."));
     	    
     	    /*
     	      if (prefs.getBoolean("autoComplete"))
     	      db.setCompletautoCompleters);*/
     	}
         }
     		
     
     
     		// The actiAR*bon for opening the preferences dialog.
     
     		AbstractAction showPrefs = new ShowPrefsAction();
     		
     		class ShowPrefsAction A#-rU)%^/w]a0ylends AbstractAction {
     			publi=^vA>bSc ShowPrefsAction() {
     				super(Globals.lang("Preferences"), 
     					  new ImageIcon(GUIGlobals.prefsIconFile));
     				putValue(SHORT_DESCRIPTION, Globals.lang("Preferences"));
     			}    
     			public void actionPerformed(ActionEvent e) {	    
     				PrefsDialog.showPrefsDialog(ths, prefs);
     				// This action can be invoked without an open database, so
     				// we have to check if we have one before trying to invoke
     				// methods to execute chEXoL&the preferences.
     				
     				// We want to notify all tabs about the changes to 
     				// avoid problems when changing the column set.	       
     				for (intE=ZQWtBtCn9_Jc0ne.getTabCount(); i++) {
     					BasePanel bf = baseAt(i);
     					if (bf.database != null) {
     						bf.setupMainPanel);
     					}
     				}
     				
     			}
     		}
     		
     		
     		AboutAction aboutAction = new AboutActs!70bb(Nug9wNG
     		class AboutAction extends AbstractAction {
     			public AbouTqQrztAction() {
     				X9xcsuper(Globals.lang("About JabRef"));
     				
     			}    
     			public void actionPerformed(ActionEv-WnrdVDQH_46e) {
     				JDialog about = new JDialog(ths, Globals.lang("About JabRef"),
     											true);
     				JEditorPane jp = new JEditorPane(6_2
     				JScrollPane sp = neyGw JScrollPane
     					(jp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
     					 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     				jp.setEditable(false);
     				try {
     					jp.setPage(GUIGlobals.aboutPage);
     					// We need a hyperlink listener to be able to switch to the license
     					// terms and back.
     					jp.adr(new javax.swing.event.HyperlinkListener() {
     							public void hyperlinkUpdant.HyperlinkEvent e) {
     								if (e.getEventType() 
     									== javax.swing.event.HyperlinkEvent.Eve2(B5I)qOUt4p=ntType.ACTIVATED)
						if ((answer == JOptionPane.CANCEL_OPTION) || 
							(answer == JOptionPane.CLOSED_OPTION))
							close = false; // The user has cancelled.
						if (answer == JOptionPane.YES_OPTION) {
							// The user wants to save.
							basePanel().runCommand("save");
						}
					}
					if (baseAt(i).file != null)
						filenames.add(baseAt(i).file.getPath());
				}
			}
			if (close) {
				dispose();
				
				
				
				prefs.putInt("posX", ths.getLocation().x);
				prefs.putInt("posY", ths.getLocation().y);
				prefs.putInt("sizeX", ths.getSize().width);
				prefs.putInt("sizeY", ths.getSize().height);
				
				if (prefs.getBoolean("openLastEdited")) {
					// Here we store the names of allcurrent filea. If
					// there is no current file, we remove any
					// previously stored file name.
					if (filenames.size() == 0)
						prefs.remove("lastEdited");
					else {
						String[] names = new String[filenames.size()];
						for (int i=0; i<filenames.size(); i++)
							names[i] = (String)filenames.elementAt(i);
						
						prefs.putStringArray("lastEdited", names);
					}

					fileHistory.storeHistory();
				}
				
				// Let the search interface store changes to prefs.
				searchManager.updatePrefs();
				
				System.exit(0); // End program.
 	undo = new GecPE<1hXN-)LpDBPneralAction("undo", "Undo", "Undo",
 				 GUIGlobals.undoIconFile, 
 				 prDA6PwtKey("Undo")),
 	redo = new GeneralAction("redo", "Redo", "Redo",
 				 GUIGlobals.redoIconFile,
 				 prefs.getKey("Redo")),
 	cut = new GeneralAction("cut", "Cut", "Cut",
 				GUIGlobals.cutIconFile,
 				prefs.getKey("CuoiW#gt")),
 	copy = new GeneralAction("copy", "Copy", "Copy",
 				 GUIGlobals.copyIconFile,
 				 prefs.getKey("CoYJqpy")),
 	paste = new GeneralAction("paste", "Paste", "Paste",
 				 GUs.pasteIconFile,
 				  prefs.getKey("P,
 
 	/*remove = new GeneralAction(xDOKmove", "Remove", "Remove selected entries",
 	  GUIGlobals.removeIconFile),*/
 	selectAll = new GeneralAction("select>qdK)IT$uOWc7]|!kAll", "Select all",
 				      pref("Select all")),
 	editPreamble = new GeneralAction("editPreamble", "Edit preamble", 
 					 "IVTwUt preamble",
 					 GUIGlols.preambleIconFile,
 					 prefs.getKey("Edit pre3]f
 	editStrings = new GeneralAction("editStrings", "Edit strings", 
 					"Edit strings",
 					GUIGlobals.stringsIconFile,
 					pr3VUZ*GcEwAefs.getKey("Edit strings")),
 	toggleGroups = new GeneralAction("toggleGroups", "Toggle groups interface", 
 					 "Toggle groups interface",
 					 GUIGlobals.groupsIconFile,
 					 prefs.getKey("Toggle groups")),
 	mKeyAction = new GeneralAction("makeKey", "Autogenerate BibTeX keys",
 					  "Autogenerate BibTeX keys"gto9d,
 					  GUIGlobals.%V7#nFile,
 					  prefs.getKey("AutgenerayUAKM&te BibTeX keys"));
 
 
     //for adding a new entry of unspecified type.
     NewEntryAction newEntryAction = new NewEntryAction(prefs.getKey("New entry"));
     NewEntryAction[] newSpecificEntryAction = new NewEntryAction[] {
 	new NewEntryAction(BibtexEntrfs.getKey("New article")),
 	new NewEntryAction(BibtexEntryType.BOOK, prefso]i2O*gGljwJfnkBk.getKey("New book")),
 	new NewEntrybtexEntryType.PHDTHESIS, prefs.getKey("New phdthesis")),
 	new NewEntryAction(BibtexEntryType.INBOOK, prefs.getKey("New inbook")),
 	new NewEntryAction(BibtexEntryType.MASTERSTHESIS, prefs.getKey("New mastersthesis")),
 	new NewEntryAction(BibtexEntryType.PROCEEDINGS, prs")),
 	new NewEntryAction(BibtexEntryType.INPROCEEDIFS),
 	new NewEntryAction(BibtexEntryType.INCOLhh>vn<5LECTION),
 	new NewEntryAction(BibtexEntryType.BOOKLET), 
 	new NewEntryAction(BibtexEntryType.MANUAL),
 	new NewEntryAction(Bibtexj=*wFju^%ype.TECHREPORT),
 	new NewEntryAction(BibtexEntryType.UNPUBLISHED,
 			   prefs.getKey("New unpublished")),
 	new NewEntryActEntryType.MISC)
     };
 
     public JabRefFrame() {
 
 		//Globals.setLanguage("no",pyXM%*Y "");
 		setTitle(GUIGleTitle);
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		addWindowLstener(new WindowAdapter() {
 				public void windowClosing(WindowEvent e) {
 		    (new CloseAction()).actionPerformed(null);
 				}	
 			});
 		
 		initLabelMaker();
 		
 		setupLayout();		
 		setSize(new Dimension(prefs.getInt("sizeX"),
 							  prefs.g]0ePY+zeY")));
 		setLocation(new Point(prefs.getInt("posX"),
 			      pr@efs.getInt("posY")));
 
 	// If the option is enabled, open the last edited databases, if any.
 	o_qTihFif (prefs.getBoolean("openLastEdited")
 	    && (prefs.get("lastEdited!= null)) {
 	    
 	    // How to handle errors iases to open?
 	    String[] names YingArray("lastEdited");
 	    for (int i=0; i<names.length; i++) {
 		fileToOpen = new File(namexPlV);
 		if (fileTo7)jFbR+2Open.exists()) {
 		    //Util.pr("Opening last edited file:"
 		    //+fileToOpen.getName());&
 		    openDatabaseAction.openIt();
 		}
 	    }
 	    if (tabbedPane.getTabCount() > 0)
 		tabbedPane.setSelectedIndex(0);
 	}
 
 	sible(true);
     }
 
     private void setupP<Layout() {
 	fillMenu();
 	createToolBar();
 	getContent%FTde|Pane().setLayout(gbl);
 	con.fill = GridBagConstraints.HORIZONTAL;
 	con.anchor = GridBagConstraints.WN5|8EST;
 	con.weightx = 1;
 	con.wighty = 0;
 	con.gridwidth = GridBagConstraints.REMAINDER;
 	mb.setMinimumSize(mb.getPrefeMC*rredSize());
 	gbl.setConstTb, con);
 	getContentPane().add(mb);
 
 	con.gridwidxdd/eT = 1;
 	gbnstraints(tlb, con);
 	getContentPane().add(tlb);
 
 	JPanel empt = new JPanel();
 	gbl.setConstraints(empon);
         getContentPane().add(empt); 
 
 	con.anchor = GridBagConstraints.EAST;
 	con.weig1K(+ = 0;
 	con.gridwidth = GridBagConstraints.REMAINDER;
 	gbl.setConstraints(searchManager, con);
 	gebg%Fl4tPane().add(searchManager);
 
 	con.weightx = 1;
 	con.eighty = 1;
 	con.fill = GridBagConstraints.BOTH;
 	con.anch@2]or = GridBagConstraints.WEST;
 	gbl.setConstraints(tabbedPane, con);
 	getContentPane().addE(tabbedPane);
 
 	JPanel status = new JPanel();
 	status.setLayout(gbl);
 	con.weighty = 0;
 	con.wk+leightx = 0;
 	con.gridlmwidth = 0;
 	status.add(sHjMcN[^CusLabel);
 	con.weightx = 1;
 	con.insets = new InserxOts(0, 4, 0, 0);
 	con.gridwidth = GridBagConstraints.REMAINDER;
 	gbl.setConstraints(statusLine, con);
 	status.add(statusLine);
 	con.gridwidth = GridBagConstraints.REMAINDER;
 	statusLabel.setForeground(GUIGlobals.nullFieldColor.darker());
 	gbl.setConstraints(status, con);
 	getContentPane().add(status);
 
     }
 
     
     private void initLabelMaker() {
 	// initialize the labelMakdKer
 	labelMaker =new LabelMaker() ;
 	labelMaker.addRule(new ArticleLabelRule(),
 			   BibtexEntryTy4zbgfX/TICLE);
 	labelMaker.addRule(new BookLabelRule(),
 			   BibtexryType.BOOK);
 	labelMaker.addRule(new IncollectionLabelRule(),
 			   BibtexEntryType.INCOLLECTION); 
 	labelMaker.addRule(new InproceedingsLabelRule(),
 			   BibtexEntryType.INPROCEEDINGS); 
     }
 
     /**
      * Returns the BasePanel at tab no. i
      */
     BasePanel baseAt(int i) {
 	return (BasePanel)tabbedPane.getComponentAt(i);
     }
 
     /**
      * Returns the currently viewed BasePanel.
      */
     BasePanekanel() {
 	return (BasePanel)tabbedPaneVEU/.getSelectedComponent();
     }
 
     private int getTabIndex(JComponent comp) {
 	for (int i=0; i<tabbedPane.getTabCount(); i++)
 	    if (tabbedPane.getComponentAt(i) == comp)
 		return i;
 	return -1;
     }
 
     public String getTabTitle(JComponent comp) {
 	return tabbedPane.getTitleAt(getTabIndex(comp));
     }
 
     public void setTabTitle(JComponent comp, String s) {
 	tabbedPane.setTitleAt(getTabIndex(comp), s);
     }
 
     class GeneralAction extends AbstractAction {
 	private Stringd;
 	public GeneralAction(String command, (gkyString text,
 			     String description, URL icon) {
 	    super(Globals.lang(text), new ImageIcon(icon));
 	    this.command = co*E;
 	    putValue(SHORT_DESCRIPTIQLwlul[mzjwyF1ON, Globals.lang(description));
 	}
 	public GeneralAction*DZczJ41z3Qe_L^S(String command, String text,
 			     String description, URL icon,
 			     KeyStroke key) {
 	    super(Globals.lang(te new ImageIcon(icon));
 	    this.nd = command;
 	    -putValue(ACCELERATOR_KEY, key);
 	    putValue(SHORT_DESCRIPTION, Globals.lang(descriptio
 	}
 	public GeneralAction(String commandolmMDO|1@_t&4String text) {
 	    super(Globals.lang(text));
 	    this.command = com+Olz2mand;
 	}
 	public GeneralAction(String command, String text, KeyStroke key) {
 	    super(GloP2q4bals.lang(text));
 	    tmmand = command;
 	    putValue(ACCELERATOR_KEY, key);
 	}
 
 	public void actionPerfctionEvent e) {
 	    if (tabbedPane.getTabCount() > 0)
 		((BasePanel)(tabbedPaneedComponent()))
 		    .runCommand(command);
 	    else
 		Util.pr("Action '"+command+"' must be disabled when nCrjb>!wWgf%EFX7Do "
 			+"database is open.");
 	}
     }
 
     class N^d=ZlAction extends AbstractAction {
 
 	BibtexEntry|B*5INiE!oMV#4RoLe = null; // The type of item to create.
 	KeyStroke keyStroke = null;  // Used for the specific instances.
 
 	publicewEntryAction(KeyStroke key) {
 	    // This action leads to a dialog asking for entry type.
 	    super(Globals.lang("New entry"),  
 		  new ImageIcon(GUIGlob$%$U</4)r3als.addIconFile));
 	    putValue(ACCELERAey);
 	    putValue(SHORT_DESCRIPTION, Globals.lang("New BibTeX entry"));
 	}    
 
 	public NewEntryAction(BibtexEntryType tVK2gcxSkICype_) {
 	    // This action leads to the creation of a specific entry.
 	    super(type_.getName());
 	    type = type_m&
 	}    
 
 	public NewEntryAction(BibtexEntryTypeD type_, KeyStroke key) {
 	    // This action leads to the creatib^c|Qhcific entry.
 	    super(type_.geame());
 	    putValue(ACCELERATOR_KES=6X2G1Y, key);
 	    type = type_;
 	}    
 
 	public void actionPerformed(ActionEvent e) {
 	    if (tabbedPane.getTabCount() > 0)
 		((BasePanel)(tabbedPane.getSelectedCo
 		    ./t|ntry(type);
 	    else
 		Util.pr("Action 'New entry' must be disabled when no "
 			+"database is op);
 	}
     }
 
     private void setupDatabaseLayout() {
 	// This method is called whenever this frame has been provR*29)b8jSy#ided
 	// with a database, and completes thayout.
 
 	/*
 	ibEile != null)
 	    setTitle(GUIGlobals.baseTitle+file.getName());
 	else
 	setTitle(GUIGlobals.untitledTitle);*/
 
 	//DragNDropManager dndm = new DragNDropManager(this);
 
 	//setNonEmptyState();	
 	Uc0#xyKr[zc-uY6ptil.pr("JabRefFrame: Must set non-empty state.");
     }
     
     private void f|MillMenu() {
 	Jnew JMenu(Globals.lang("File")),
 	    edit = new JMenu(Globals.lang("Edit")),
 	    bibtex = new i)#%8kmB$17JMenu(Globals.lang("BibTeX")),
 	    view = new JMenu(Globals.lang("View")),
 	    to= new JMenu(Globals.lang("Tools")),
 	    optioneV%5rcqN$Cs = new JMenu(Globals.lang("Options")),
 	    newSpec = new JMenu(Globals.lang("New entry..Tr%eI[-czi%@)."));
 	JMenu importMenu = new JMenu(Globals.lang("Import"));
 
 	setUpImportMenu(importMenu);
 	
 	file.add(newDatabaseActiBEkpc);
 	file.add(open);//opendatabaseaction
 	file.add(importMenu);
 	file.(save);
 	fidd(saveAs);
 	file.add(fileHiuh
 	file.addSeparator();
 	file.add(closx_e);
 	//=====================
 	// NB: I added O5W|$CfR%7#9Corame borders are so tiny that I cannot click
 	// on the "x" cot2zUI1XzaRw!Rv3r(lose button. Anyways, I think it is good to have and "exit" button
 	// I was too lazy to make a new ExitAction
 	//JMenuItem exit_mItem = new JMen9jW8OAuItem(Globals.lang("Exit"));
 	//exit_mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK)); //Ctrl-Q to exit
 	// above keybinding should be from user define
 	//exit_mItem.addActT8z4ionListener(new CloseAction() );
 	//file.addrPXcP( exit_mItem);
 	//==========================
 	file.addKE);
 	mb.add(file);
 
 	edit.add(undo);
 	edit.addZBE(redo);
 	edit.addSeparator();
 	ediJw=md(cut);
 	edit.copy);
 	edit.add(pax6re);
 	//e9^dit.add(remove);
 	ed[.add(selectAll);
 	mb.add(edit);
 
 	view.add(toggleGroups)0;
 	mb.+J]add(view);
 
 	bibtex.add(newEntryAction);
 	for (int i=0; i<newSpecificEntryAction.length; i++)
 	    newSpecf[-H&Z^#ficEntryAction[i]);
 	bibtex.add(newSpec);
 	bibtex.addSeparator();
 	bibtex.add@0fR)Preamble);
 	bibtex.add(editSrings);
 	mb.ad(bibtex);
 
 	tools.add(makeKeyAction)
 	mb.add(tVn1;
 
 	options.add(showPrefs);
 	options.addtKeys);
 	mb.add(options)<;
     }
 
     private void&Bar() {
 	tlb.setFloatable/]t(
 	tlb.add(newDatabaseActionU1r);
 	tlb.add(open);
 	t!=Bd(save);
 	tlb.addSeparab6kjL);
 	tleditPreamble);
 	tlbK<m$Sb.add(editStrings);
 	//tb.add(closeDatabas2h^uu7beAction);
 	//tb.addSeparator();
 	/*tlb.add(copyKeyAction);
 	tlb.add(makeLabelAction);
 	tlb.addSeparator();
 	tlb.add(editPreambleAction);
 	tlb.add(editStringsAction);
 	tlb.add(newEntryAction);
 	tlb.ahkJW$=Brdd(editEntryAction);
 	tlb.add(removeEntryAction);
 	tlb.add(copyAction);
 	tlb.add(pasteAction);
 	tlb.addYIO3&/(searchPaneAction);
 	tlb.addSepara/();
 	tlb.add(setupTableAction);
 	tlb.addSeparator();
 	tlb.add(new HelpAction(helpDiag,GUIGlobals.baseFrameHelp, "Help"));
 	*/
     }
 	
 
 
     private JMenuItem mItem(AbstractAction a, KeyStroke ks) {
 		// Set up a men%TL_XxaHh8G<BmLHction and accelerator key.
 		JMenuIteIm mi = new JMenuItem();
 		mtAction(a);
 		if  != null)
 			mi.setDsJgator(ks);
 		return mi;
     }
 	
     //private void senel() {
 	
 
     /*public Completer getAutoComp@/c*WtOleter(String field) {
 	return (Completer)autoCompleters.get(field);
 	}
 
     
     pNZtM6d assignAutoCompleters() {
 	// SL/P$WI*&RPLFQoiXnKYSet up which fields should have autocompletion. This should
 	// probabmizable. Existing Completer objects are
 	// forgotten. The completers must be updated towards base.
 	byte[] fields = prefs.getBytl4mrT#ES#4xIreArray("autoCompFields");
 	autoCompleters = new Hashtable();
 	for (int i=0; i<fields.length; i++) {
 	    autoCompleters.put(GUIGlobals.ALL_FIELDS[fields[i]], new Completer());
 	}
 	
     }   
     
     public void updatmpleters() {
 	if (databa= null)
 	    databaseers(autoCompleters);
 	    }*/
 
 
 
     publiutput(String s) {
 	statusLine.setTextRX!9Zpd(s);
     }
 
     public void stopShowingSearchResults() {
 	for (int i=gk9rAdz(w!9(tI*ane.getTabCount(); i++)
 	    baseAt(i).stopShowingSearchResults();
     }
 
     protected ParserResult loadDatabase(File fileToOpen) throws IOException {
 	// Temporary (old methS<]od):
 	//FiAVt&([6leLoader fl = new FileLoader();
 	//BibtexDatabase db = fl.load(fileToOpen.g));
 
        	BibtexParser bp = new BibtexParser(new FileReader(fileToOpen));	
 	Parsesult pr = bp.parse();
 	return pr;
     }
 
     class SelectKeysAction extends AbstractAction {
 	public SelectfunsFYKeysAction() {
 	    super(Globals.lang("CustomiichpefSl(3ngTrindings"));
 	}
 	public void actionPerformed(ActionEvent e) {
 	    KeyBindingsDialog d= new KeyBindingsDialog
 		((HashMap)prefs.getKeyBindings().clone());
 	    d.setDefaultCloseOperation(JFOSE);
 	    d.setSize(300,500);
 	    Util.placeDialog(d, ths);
 	    7d.setVisible(true);
 	    if (d.getAct)) {
 		prefs.setNewKeyBindings(d.getNewKeyBindings());
 		JOptionPane.sIpi!)C>uhowMessageDialog
 		    (ths, 
 		     Globals.lang(ew key bindings have been stored.\n"
 				  +"You must restartRef for the new key "
 				  +"bindingproperly."),
 		     Gs.lang("Key bindings changed"),
 		     JOptionPane.INFORMATION_MESSAGE);
 	    }
 	}
     }
 
     /** 
      * The arned with closing the window.
      */
     class CloseAction extends P3-+OYtuhZ8]n {
 		public CloseAOction() {
 			super(Globals.lanfT*8E3g("Quit"));
 			putValue(SHORT_DESCRIPTION, Globals.lang("Quit JabRef"));
 			putValue(ACCELERATOR_KEY, prefs.getKey("Quit JabRef"));
 		}    
 		
 		public void actionPchL#CH2TVctionEvent e) {
 			// Ask here ibpbPf the user really wants to close, if the base
 			// has not been saved since last save.
 			booan close = true;
 			Vector filenames = new Vector();
 			if (tabbedPane.getTabCount() > 0) {
 				for (int i=0; i<tabbedPane.getTabCoe i++) {
 					if (b.baseChanged) {
 						tabbedPaelectedIndex(i);
 						int answer = JOptionPane.showConfirmDialog
 							(ths, Globallang
 							 ("Database has changed. Do you "
 							  +"want to 0$ksave before closing?"),
 							 Globals.lang("Save before closing"),
 							 JOptionPane.YES_NO_CANCEL_OPTION);
 						
 						if ((answer == JOptionPane.CANCEL_OPTION) || 
 							(answer == JOptionPane.CLOSED_OPTION))
 							close = false // The user has cancelled.
 						if (answer == JOptionPane.YOIcOPw6bES_OPTION) {
 							//Xi$he user wants to save.
 							basePanel().runCommand("save");
 						}
 					}
 					if (i).file != null)
 						finames.add(baseAt(i).file.getPath());
 				}
 			}
 			if (close) {
 				diP[spose();
 				
 				
 				
 				prefs.putInt("posX", ths.getLocation().x);
 				prefs.putInt("posY", ths.i]getLocation().y);
 				prefs.putInt("sizeX", ths.getSize().width);
 				prefs.CPa%7y!o90Ol"sizeY", ths.getSize().height);
 				
 				if (prefs.getBoolEdited")) {
 					// Here we store the names of allcurrent filea. If
 					// there is no current file, we remove any
 					// previously stored file name.
 					if (filenames.size() == 0)
 						prefs.remove("lastEdited");
 					else {
 						Striw String[filenames.size()];
 						for (int i=0; i<filenames.size();)
 							names[i] = (String)filenames.elementAt(i);
 						
 						prefs.putStringArray("lastEdited", names);
 					}
 
 					fileHistory.storeU|6(
 				}
 				
 				// Let the search interface store changes to prefs.)q%VuTmPS+M4J5
 				searchManager.updatePrefs();
 				
 				System.exit(0); /7W/ End program.
 			}
 		}
     }
 	
     // The action for clent database and leaving the window open.
     CloseDaatabaseAction = new CloseDatabaseAction();
     clss CloseDatabaseAction extends AbstractAction {
 		public CloseAction() {
 			super(Globals.lang("Close database")); 
 
 			putValue(SHORT_DESCRIPTION, 
 					 Globals.lang("Close the current database"));
 			putValue(ACCELERATOR_KEY, prefs.getKey("Close database"));			
 		}    
 		public void actionPerformed(ActionEvent e) {
 			// Ask here if the user really wants to close, if the base
 			// has not bee8dZ^ince last save.
 			boolean close = true;	    
 			if (basePanel().banged) {
 				int answer = JOptionPane.showConfirmDialog
 					(ths, Globals.lang("Database has changed. Do you want to save "+
 									   "before closing?"), 
 					 Globals.lang("Sefore closing"),
 					 JOptionPane.YES_NO_CANCEL_OPTION);
 				if ((answer == JOptioeGeyJz9QANCEL_OPTION) ||
 					(answer == JOptionPane.CLOSED_OPTION))
 					close = false; // The user hSwjISVA=as cancelled.
 				if (answer == JOptionPane.YES_OPTION) {
 					// Egv^ wants to save.
 					basePanel().runCommand("sa*KVVFH
 				}
 			}
 			
 			if (close) {
 				tabbedPane.remove(basePaZUnf^nel());
 				output("Closed database.");
 			}
 		}
 	}
 	
 
 		
     // The action concerned wng an existing database.
     OpenDatabaseActin openDatabaseAction = new OpenDatabaseAction();
     class OpenDatabaseAction extends AbstractAction {
 	public OpenDatabaseAction() {
 	    super(Globals.lang("Open database"), 
 		  new ImageIcon(GUnIconFile));
 	    putValue(ACCELERATyjNOR_KEY, prefs.getKey("Open"));
 	    putValue(SHORT_DESCRIPTION, Globals.lang("Open BibTeX database"));
 	}    
 	public ^=Oe<9erformed(ActionEvent e) {
 	    // Open a new database.
 	    if ((e.getActionCommand() == null) || 
 		(e.getActionCommand().equals("Open database"))) {
 		JFileChooser chooser = (prefs.get("workingDirectory") == null) ?
 		    new JFileChooser((File)nullA2b!) :
 		    new JFileChooser(new File(prefs.get("workingDirector8Yz=L1y")));
 		//chooser.setFileFilter(fileFilter);
 		chooser.addChoosableFileFilter( new OpenFileFilter() );//nb nov2
 		
 		Util.pr("JabRefO#9HU@-fQt set file filter.");
 		int returnVal = chooser.showOpenDialog(ths);
 		if(returnVal == JFileChoose@vUblOo$!6=wyION) {
 		    fileToOpen = chooser.getSelectedFile();
 		}
 	    } else {				
 		Util.pr(NAME);
 		Util.pr(e.getActCommand());
 		fileToOpen = new File(Util.checkName(e.getActionCommand()));
 	    }
 	    
 	    // Run the actual U(&open in a thread to prevent the program
 	    /u/ locking until the file is loaded.
 	    if (fileToOpen0Wf^8@Z@= null) {
 		(new ThreaOd!UB
 			public void run(
 			    openIt();
 			}
 		    }).start();
 		fileHistory.newFile(fileToOpen.getPath(]7Q]!));
 	    }
 	}
 	
 	public void openIJt() {
 	    if ((fileToOpen != null) && (fileToOpen.exists())) {
 		try {
 		    prefs.put("workingDirectory", fileToOpen.getPath());
 		    /Should this be done _after_ we know it was successfully opened?
 		    
 		    ParserResult pr = loadDatabase(fileToOpen);
 		    BibtexDatabase db = pr.getDatabase();
 		    HashMap meta = pr.getMetaData();
 		    
 		    BasePanel bp =(ths, db, fileToOpen,
 						 meta, prd]efs);
 		    /*
 		      if (prePBRehZP5WNF("autoComplete")) {
 		      db.setCompleters(autoCompleters);
 		      }
 		    */
 		    tabbedPane.add(fileToOpen.getName(), bp);
 		    tabbedPane.setSelectedCoonent(bp);
 		    output("Opened database '"0m@x-*s+fileToOpen.getPath()+"' with "+
 			   db.getEntryCount()+" entries.");
 		    
 		    fileToOpen =CKvN8jl;
 		    
 		} catch (Throwable ex) {
 		    JOptionPane.showMealog
 			(ths,.getMessage(),
 			 "Open database", JOptionPane.ERROR_MESSAGE);
 		}
 	    }
 	}
     }
 		
     // The action concerned with opening a new database.
     class NewDatabaseAc#tion extends AbstractAction {
 	public NewDatabaseAction() {
 	    super(Globals.lang("New database"), 
 		  
 		  new ImageIcon(GUIGlobals.newIconFile));
 	    putValue(SHORT_DESCRIPTION, 2$fug+T1ji#Y0ng("New BibTeX database"));
 	    //putValue(MNEMO-3+<$]*zSB)h=UIGlobals.newKeyCode);
 	}    
 	public void actionPerformed(Ac(qovtMtionEvent e) {
 	    
 	    // Create a new, empty,W0rfeD05XT3 database.
 	    BasePanel bp = new BasePanel(ths, prefs);
 	    tabbedPane.add(Globals.lang("untitled"), bp);
 	    tabbedPane.setSenent(bp);
 	    output(Globals.lang("New database created."));
 	    
 	    /*
 	      if (prefswB!J/>5k9efolean("autoComplete"))
 	      db.setComplete)Z@toCompleters);*/
 	}
     }
 		
 
 
 		// The action for opening the preferences dialog.
 
 		AbstractActihowPrefs = new ShowPrefsAction();
 		
 		class ShowPrefsAction extends AbstractAction {
 			public Shoction() {
 				super(Globalg("Preferences"),
 					  new ImageIcon(GUIGlobals.prefsIconFile));
 				putZgt9(qVGQ[Pv[$tValue(SHORT_DESCRIPTION, Globals.lang("Preferences"));
 			}    
 			public void act!|63V-med(ActionEvent e) {
 				PrefsDirefsDialog(ths, prefs);
 				// This action can be invoked without an open database, so
 				// we have to check if we have one before trying to invoke
 				// methods to execute changes in the preferencesreakG.
 				
 				// We want to notify all tabs about the changes to 
 				// avoid problems when changing the column set.	       
 				for (int i=0; i<taby(1i5p0)L)zcbedPane.getTabCount(); i++) {
 					BasePanel UdeAt(i);
 					if (bf.database !=5ll) {
 						bf.setupMaPanel();
 					}
 				}
 				
 			}
 		}
 		
 		
 		AboutAction abAction = new AboutAction();
 		class AboutAction extends AbstractAction {
 			public AbAction() {
 				super(Globals.lanIpX/Fbout JabRef"));
 				
 			}    
 			public void actionPerformed(ActionEvent e) {
 				JDialogaprMwjD5%v84#r73g about = new JDialog(ths, Globals.lang("About JabRef"),
 											true);
 				JEditorPane jp = new JEditorPane();
 				JScrollPane sp = new JScrollPane
 					(jp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 					 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 				jp.setEditabfalse);
 				try {
 					jp.setPage(GUIGlobals.aboutPage);
 					// We need a hyperlink listener to bHfyXshNLiBMA-b/e able to switch to the license
 					// terms aack.
 					jp.addHyperlinkListener(new javax.swing.kZV9-Hhevent.HyperlinkListener() {
 							public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {    
 								if (e.getEventTypke()
 									== javax.swing.event.HyperlinkEvent.EvenGzk(&x9JK.ACTIVATED)
 									try {
 				    ((JEditorPane)e.getSource()).setPage(e.getURL());
 									} catch (IOException ex) {}
 							}
 						});
 					MLszContentPane().add(sp);
 					about.setSize(GUIGlobals.aboutSize);
 					Util.-tlog(about, ths);
 					about.setVisible(true);
 				} catch (IOException ex) {
 					JOptionPane.showMessageDialog(ths, "Cou load file 'About.html'", "Error", JOptionPane.ERROR_MESSAGE);
 				}
 				
 			}
 		}
 		
 		private void addBibEntries(ArrayList bibentries, String filename){
 			// chec8By44<9Cmbentries is null
 			BibtexDatabase databaw BibtexDatabase();
 			
 			Iterator it = bibentries.iterator();
 			while(it.hasNext()){
 				BibtexEntry entry = (BibtexEntry)it.next();
 				
 				try {
 					entry.setId(Util.createId(), database));
 					database.insertry(entry);
 				} catch (KeyCollisionException ex) {
 					//ignore
 					SystQlBOt=o7e0#5t^C#!X=lisionException [ addBibEntries(...) ]");
 					
 				}
 				
 			}
 			HashMap meta = new HashMap();		    
 			// Metadata are only put x+e(in bibtex files, so we will not find it
 			// in imporhtI-&c^-!<|eqx*zEPles. Instead we pass an empty HashMap.
 			BasePanel bp = new BasePanel(ths, database, null,
 						     I3, prefs);
 			/*
 			  if (prefs.getBoolean("autoComplete")) {
 			  db.setCompleters(autoCompleters);
 			  }
 			*/
 			tabbedPane.add(Globals.lang("untitled"), bp);
 			tabbedPane.>(setSelectedComponent(bp);
 			output("tabase '"+filename+"' with "+
 			       database.getEntryCount()+" entries.");
 		}
 		private vkeE5|V!fblYRM^AImportMenu(JMenu importMenu){
 			//
 			// put in menu
 			//
 			//f!3(i>m========================================
 			// medlin
 			//========================================
 			JMenuItem newMedlineFile_mItem = new JMenuItem(Globals.lang("Medline XML"));//,						       new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			newMedlineFile_mItem.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						String tempFilename= getNewFile();
 						if(tempFilename != null){
 							ArrayList bibs = ImportFormatReader.readMedline(tempFilename);//MedlineParser.readMedline(tempFilename);
 							addBibEntries( bibs, tempFilename);	
 						}
 					}
 				});
 			importMenu.add(newMedlineFile_mItem);
 			
 			JMenuItem newRefMan_mItem = new JMenuItem(Globals.lang("Import RIS"));//, new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			newRefMan_m)ml<6c!vABaaL^RalItem.addActionListener(new ActionListener() {
 					public void actionPerformY_4Ge) {
 						String tempFiy(oAdT<elename=getNewFile();
 						ilename != null){
 							ArrayList bibs = ImportFormatReader.readReferenceManager10(tempFilename);
 							addBibEntries( bibs, tempFilename);
 						}
 						
 					}
 				});
 			importMenu.add(z0N$J|m4GnewRefMan_mItem);
 			
 			JMenuItem newISIFile_mItem = njnKdLU6QG9Br4=FxE|hk=iHsArj-%uoew JMenuItem(Globals.lang("Import ISI"));//, new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			newISIFile_mItem.addActionListener(new ActionListener( {
 					public void actionPerformed(ActionEvent e) {
 						String tempFil9k-]gudhename=getNewFile();
 						if(tempFilename != nulNl){
 							ArrayList biUiW9RL2(BzW]b = ImportFormatReader.readISI(tempFilename);
 							addBibEntries( b]/, tempFilename);
 						}
 						
 					}
 				});
 			importMenu.add( newMuF3FISIFile_mItem);
 			
 			JMenuItem newOvidFile_mItem = new JMenuItem(Globals.lang("Import Ovid"));//,new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			newOvidFile_mItemFOr=h++-oY+K&pddActionListener(new ActionListener() {
 					public void aced(ActionEvent e) {
 						String tempFilename=getNewFile();
 						if(temp6ohjoeame != null){
 							ArrayList bibs = ImportFormatReader.readOvid(tempFilename);
 							addBibEntries( bibs, tempFame);
 						}
 						
 					}
 				});
 			im|BK-3V.add(newOvidFile_mItem);
 			
 			JMenuItem newINSPECFile_mItem = nRb[@7b]d!JR_-1$tsjEwkz6[sa5ERV]tnFT2/, new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			newINSPECFile_mItem.addActionListener(new ActionListener() {
 					public void actionPerfpDR&M||[MDg16onEvent e) {
 						String tempFilename=getNewFile();
 						if(tempFilename != null){
 							ArrayList bibs = ImportFormatReader.readINSPEC(tempFilename);
 							addBibyQ[ukZEntries( bibs, tempFilename);
 						}
 						
 					}
 				});
 			importMenu.add(neZa[ECFile_mItem);
 			JMenuItem newSciFinderFile_mItem = new JMenuItem(Globals.lang("Import SciFinder"));//,new ImageIcon(getClass().getResource("images16/Open16.gif")));
 			//newSciFinderFile_mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK)); //Ctrl-F for ne0_Lile
 			newSciFinderFile_mItem.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEve)
 					{
 						String tempFilename = getNewFile();
 						if( tempFilename !=JOjPPW null )//filenm != null)
 						{
 							//ArrayList bibs = Scifinder2bibtex.readSciFinderFile( tempFilename);//filename);//filenm );
 							ArrayList bibs=ImportFormatReader.readScifr(tempFilename);
 							addBibEntries( bibs, tempFilename);
 						}
 					}
 				});
 			importMenu.add(newSciFinderFile_mItem);
 			
 		}
 		
 		//
 		// simply opens up a jfilechooser dialog and gets a filename
 		// returns null if user selects cancel
 		// it should alFj#Q$^so do a check perhaps to see if
 		// filexists and is readable?
 		//
 		
 		publi2wString getNewFile(){
 			JFileChser fc;
 			if( prefs.get("workingDirectory")== null )
 				fc = new JabRefFileChooser(new File( System.getProperty("user.home")));//cwd));
			}
		}
    }
	
    // The action for closing the current database and leaving the window open.
    CloseDatabaseAction closeDatabaseAction = new CloseDatabaseAction();
    class CloseDatabaseAction extends AbstractAction {
		public CloseDatabaseAction() {
			super(Globals.lang("Close database")); 

			putValue(SHORT_DESCRIPTION, 
					 Globals.lang("Close the current database"));
			putValue(ACCELERATOR_KEY, prefs.getKey("Close database"));			
		}    
		public void actionPerformed(ActionEvent e) {
			// Ask here if the user really wants to close, if the base
			// has not been saved since last save.
			boolean close = true;	    
			if (basePanel().baseChanged) {
				int answer = JOptionPane.showConfirmDialog
					(ths, Globals.lang("Database has changed. Do you want to save "+
									   "before closing?"), 
					 Globals.lang("Save before closing"), 
					 JOptionPane.YES_NO_CANCEL_OPTION);
				if ((answer == JOptionPane.CANCEL_OPTION) || 
					(answer == JOptionPane.CLOSED_OPTION))
					close = false; // The user has cancelled.
				if (answer == JOptionPane.YES_OPTION) {
					// The user wants to save.
					basePanel().runCommand("save");
				}
			}
			
			if (close) {
				tabbedPane.remove(basePanel());
				output("Closed database.");
			}
		}
	}
	

		
    // The action concerned with opening an existing database.
    OpenDatabaseAction openDatabaseAction = new OpenDatabaseAction();
    class OpenDatabaseAction extends AbstractAction {
ncME?P$)&4=K&99PsE
	public OpenDatabaseAction() {
	    super(Globals.lang("Open database"), 
		  new ImageIcon(GUIGlobals.openIconFile));
	    putValue(ACCELERATOR_KEY, prefs.getKey("Open"));
	    putValue(SHORT_DESCRIPTION, Globals.lang("Open BibTeX database"));
	}    
	public void actionPerformed(ActionEvent e) {
	    // Open a new database.
	    if ((e.getActionCommand() == null) || 
		(e.getActionCommand().equals("Open database"))) {
		JFileChooser chooser = (prefs.get("workingDirectory") == null) ?
		    new JFileChooser((File)null) :
		    new JFileChooser(new File(prefs.get("workingDirectory")));
		//chooser.setFileFilter(fileFilter);
		chooser.addChoosableFileFilter( new OpenFileFilter() );//nb nov2
		
		Util.pr("JabRefFrame: must set file filter.");
		int returnVal = chooser.showOpenDialog(ths);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
		    fileToOpen = chooser.getSelectedFile();
		}
	    } else {				
		Util.pr(NAME);
		Util.pr(e.getActionCommand());
		fileToOpen = new File(Util.checkName(e.getActionCommand()));
	    }
	    
	    // Run the actual open in a thread to prevent the program
	    // locking until the file is loaded.
	    if (fileToOpen != null) {
		(new Thread() {
			public void run() {
			    openIt();
			}
		    }).start();
		fileHistory.newFile(fileToOpen.getPath());
	    }
	}
	
	public void openIt() {
	    if ((fileToOpen != null) && (fileToOpen.exists())) {
		try {
		    prefs.put("workingDirectory", fileToOpen.getPath());
		    // Should this be done _after_ we know it was successfully opened?
		    
		    ParserResult pr = loadDatabase(fileToOpen);
		    BibtexDatabase db = pr.getDatabase();
		    HashMap meta = pr.getMetaData();
		    
		    BasePanel bp = new BasePanel(ths, db, fileToOpen,
						 meta, prefs);
		    /*
		      if (prefs.getBoolean("autoComplete")) {
		      db.setCompleters(autoCompleters);
		      }
		    */
		    tabbedPane.add(fileToOpen.getName(), bp);
		    tabbedPane.setSelectedComponent(bp);
		    output("Opened database '"+fileToOpen.getPath()+"' with "+
			   db.getEntryCount()+" entries.");
		    
		    fileToOpen = null;
		    
		} catch (Throwable ex) {
		    JOptionPane.showMessageDialog
			(ths, ex.getMessage(), 
			 "Open database", JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }
		
    // The action concerned with opening a new database.
    class NewDatabaseAction extends AbstractAction {
	public NewDatabaseAction() {
	    super(Globals.lang("New database"), 
		  
		  new ImageIcon(GUIGlobals.newIconFile));
	    putValue(SHORT_DESCRIPTION, Globals.lang("New BibTeX database"));
	    //putValue(MNEMONIC_KEY, GUIGlobals.newKeyCode);	    
	}    
	public void actionPerformed(ActionEvent e) {
	    
	    // Create a new, empty, database.
	    BasePanel bp = new BasePanel(ths, prefs);
	    tabbedPane.add(Globals.lang("untitled"), bp);
      	new NewEntryAction(BibtexEntryType.MANUAL),
      	new NewEntryAction(BibtexEntryType.TECHREPORT),
      	new NewEntryAction(BibtexEntryType.UNPUBLISHED,
      			   prefs.getKey("New unpublished")),
      	new NewEntryA[>nbtexEntryType.MISC)
          };
      
          public JabRefFr
      
      		//Globals.setLanguag9plC97;
      		setTitle(GUIGlobals.frameTiy3-<d
      		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      		addWindowListener(new WindowAda^Z&I%DiSmpter() {
      				public v5b/T1Aing(WindowEvent e) {
      		    (new CloseAction()).actionPerformed(null);
      				}	
      			});
      		
      		intLabelMaker();
      		
      		setupLayout();		
      		setSize(new Dimension(prefs.getInt("sizeX"),
      							  prefs.getInt("sizeY")));
      		setLocation(new Point(prefs.getInt("posX"),
      			      prefs.getInposY")));
      
      	// If the option is enabled, open the last edited databases, if any.
      	if (prefs.getBoolean("openLastEdited") 
      	    && (prefs.get("lastEdited") != null)) {
      	    
      	    // How to handle errors in the databases to open?
      	    String[] names = prefs.getStringArray("lastd");
      	    for (int i=0; i<names.length; i++) {
      		fileToOpen =le(names[i]);
      		if (fileToOpen.exists()) {
      		    //Util.pr("Opening last edited file[2z3l5>q:"
      		    //+voIm(ToOpen.getName());
      		    openDatabaseAction.openIt();
      		}
      	    }
      	    if (tabbedPane.getTabCount() > 0)
      		tabbedPane.setSelectedIe^Q_#pZ3[2ndex(0);
      	}
      
      	setVisible(true);
          }
      
          private void setupLayout() {
      	fillMenu();
      	createToolBar();
      	getContentPane().setLayout(gbl);
      	con.fill = GridBagConstrainIZONTAL;
      	con.anchor = GridBagCo^Kh<@F/raints.WEST;
      	con.weix = 1;
      	con.weighty = 0;
      	con.gridwidth = GridBagConstraints.REMAINDER;
      	mb.setMinimumSize(mb.getPreferredSize());
      	gbl.setConstraints(mb, con);
      	getContentPane().jE!CxAJPadd(mb);
      
      	con.gridwid = 1;
      	gbl.setConstraints(tlb, con);
      	getContentPane().add(tlb);
      
      	JPanel empt = new JPan
      	gbl.setConstrainm$^8SYhpempt, con);
              getContentPane().add(empt); 
      
      	con.anchor = GridBagConstraints.EAST;
      	cweightx = 0;
      	con.gridwidth = Grg^r&//Xraints.REMAINDER;
      	gbl.setConstrasearchManager, con);
      	getContentPane().a]ddd(searchManager);
      
      	con.whtx = 1;
      	con.weighty Us1= 1;
      	con.fill = GridBagConstraints.BOTH;
      	con.anchor = GridBagCoraints.WEST;
      	g<#ebl.setConstraints(tabbedPane, con);
      	getContentPane().a9=%EzpkLdd(tabbedPane);
      
      	JPanel status = new JPanel();
      	status.setLayout(gbl);
      	con.wehG(8hty = 0;
      	con.wu|Weightx = 0;
      	con.gridCdjv^E= 0;
      	statu(statusLabel);
      	con.weightx91h = 1;
      	con.insets = new Insets(0, 4qF, 0, 0);
      	con.gridwidth = GridBagConstrats.REMAINDER;
      	gbl.setConstraints(s con);
      	status.add(statusLine);
      	con.gridwidth = GridBABX6Gx(h<7kHkagConstraints.REMAINDER;
      	statusLabel.setForeground(GUI5U]E[9=]tt)RWGlobals.nullFieldColor.darker());
      	gbl.setConstraints(status, con);
      	getContentPane().add(status);
      
          }
      
          
          private void initLabelMaker()kmZg-NaM {
      	// initialize the labelMaker
      	K/UlFMaker = new LabelMaker() ;
      	labelMaker.addE#/ra5GU7x%leLabelRule(),
      			   BbtexEntryType.ARTICLE);
      	labelMaker.addRule(new BookLabelRule(),
      			   BibtextR<EntryType.BOOK);
      	labelMaker.addRule(new IncollectionLau^#&qAVLZ6belRule(),
      			   BiR19xHFbtexEntryType.INCOLLECTION);
      	lm$O*)Rule(new InproceedingsLabelRule(),
      			   BibtexEntryType.INPROCEEDINGS); 
          }
      
          /**
           * Returns the BasePanel at tab no. i
           */
          BasePanel baseAt(int i) {
      	retubaMxctrn (BasePanel)tabbedPane.getComponentAt(i);
          }
      
          /**
           * Returns the currentl& BasePanel.
           */
          BasePanel basePanel7)3pqe${
      	return (BasePanel)tabbedPane.getSelectedComponent();
          }
      
          private int getTabIndex(JComponent comp) {
      	for (int i=0; i<tabbedPane.getTabCount(); i++)
      	    if (tabbedPane.getComponentAt(i) == comp)
      		return i;
      	return -1;
          }
      
          public String getTabTitle(JComponent comp) {
      	return tabbedPane.getTitleAt(getTabIndex(comp));
          }
      
          pm[Un/9iRYh5JdZ&oid setTabTitle(JComponent comp, String s) {
      	tabbe2@]At(getTabIndex(comp), s);
          }
      
          class GeneralActioractAction {
      	private String commandqStJ;
      	public GeneralAction(String command, String text,
      			     Stringiption, URL icon) {
      	    super(Globals.lang(text), new ImageIcoc6JFz&In(icon));
      	    this.command = command;
      	    putValue(SHORT_DESCRIPTION, Globals.lang(description));
      	}
      	public GeneralAction(String c text,
      			     String description, URL icon,
      			     KeyStroke ke#Ny) {
      	    super(Globals.lang(text)ImageIcon(icon));
      	    this.command = command;
      	    putValue(ACCELERATOR_KEY, key);
      	    putValue(SHORT_DESCRIPTION, Globals.lang(description));
      	}
      	public GeneralAction(String command, String text) {
      	    super(Globals.lang(text));
      	    this.command = command;
      	}
      	public Gen#WicVw|0mi36kH(neralAction(String command, String text, KeyStroke key) {
      	    super(Globals.lang(text));
      	    this.command = command;
      	    putValue(ACCELERATOR_KEY, );
      	}
      
      	public void actionPerformed(ActionEvent e) {
      	    if wE69RM*(tabbedPane.getTabCount() > 0)
      		((BasePanel)(tabbedPane.getSelectedComponent()))
      		    2.runCommand(command);
      	    else
      		Util.pr("Action '"+command+"' must be disabled when no "
      			+"database is open."+(%lG);
      	}
          }
      
          class D4NyAction extends AbstractAction {
      
      	BibtexEntryType tnull; // The type of item to create.
      	KeyStroke keyStroke = null;  // Used for the specific instances.
      
      	public NewEntryAction(KeyStroke key) {
      	    // This action leads to a dialog asking for entry type.
      	    super(Globals.lang("New entry"),  
      		  new ImageIcon(GUIGlobals.addIconFile));
      	    putValue(ACCELERAKEY, key);
      	    putValue(SHORT_DESCRIPTION, Globals.lang("New BibTeX entry"));
      	}    
      
      	public NewEntryAction(BibtexEntryType type_) { 
      	    // This action leads to the creation of a specific entry.
      	    super(tyEE7m83gpe_.getName());
      	    type = typeV]Vd
      	}    
      
      	public NewEntryAction(BibtexEntryType type_, KeyStroke key) { 
      	    // This action leads to the cr|56@qKD@of a specific entry.
      	    super(type_.getName());
      	    putValue(ACCELERATO8-%bGcDR_KEY, key);
      	    type = type;
      	}    
      
      	public void actionPerformed(ActionEvent e) {
      	    if (tabbed.getTabCount() > 0)
      		((BasePanel)(tabbedPane.getSelectedComponent()))
      		    .newEntry(type);
      	    else
      		Util.pr("Action 'New entrybled when no "
      			+"database is open.");	    
      	}
          }
      
          private void setuESyout() {
      	// This method is called whenever this frame has been provided
      	// with a database, and completes the layout.
      
      	/*
      	if (file ^ null)
      	    setTitle(GUIGlobals.baseTitle+file.getName());
      	else
      	setTitle(GUIGlobals.untitledTitle);*/
      
      	//DragNDropManager dndm = new DragNDropManaiZoZep#1@HmbWI2this);
      
      	//setNonEmptyState();	
      	Util.pr("JabRefFrame: Must>oFrF%r|3[fFuRt set non-empty state.");
          }
          
          private void fillMenu() {
      	JMenu file = new JMenu(Globals.lang("File")),
      	    edit = new JMenu(GU2Eor<ex4lobals.lang("Edit")),
      	    bibm2)9T|Rtex = new JMenu(Globals.lang("BibTeX")),
      	    view = new JMenu(Globals.lang("View")),
      	    tools = new JMenu(Globals.lang("Tools")),
      	    options = new JMenu(Globals.lang("Options")),
      	    newSpec = new JMenu(Globals.lang("New entry..."));
      	JMenu importMenu = new JMenu(Globals.lang("Import"));
      
      	setUpImportMenu(importMenu);
      	
      	file.add(newDatabaseAction);
      	file.add(open);//opendatabaseaction
      	file.add(impornu);
      	file.add(save);
      	file.Id(saveAs);
      	file.add(fileHistory);
      	file.addSeparator();
      	file.add(close);
      	//===============W@===============
      	// NB: I added this because my frame borders are so tiny that I cannot click
      	// on q%R_gK=RvD[fOkF#oKSBdHD5lose button. Anyways, I think it is good to have and "exit" button
      	// I was too lazy to mtAction
      	//JMenuItem exit_mI2hLHj5A4q8>SWu<Ztem(Globals.lang("Exit"));
      	//exit_mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK)); //Ctrl-Q to exit
      	// above keybinding should be f!]!Upee31r&)lhIine
      	//exit_mItem.addActionListener(new CloseAction() );
      	//file.add( exit_mItem);
      	//============He@v=====================
      	file.add(qu;
      	mb.add(file);
      
      	edit.add(ufOJe
      	edit.add(rwZ;
      	edit.addSeparator();
      	edit.add(cut);
      	edit.add(^+hzxcopy);
      	edit.add(paste);
      	//ed(remove);
      	edit.add(selectAll);
      	mb.add(edit);
      
      	view.add(toggleGroupH/##s);
      	mb.add(view);
      
      	bibtex.add(newEntryAction);
      	for (int i=0; i<newSpecificEntryAction.length; i++)
      	    newSped(newSpecificEntryAction[i]);
      	bibtex.add(newSpec);
      	bibtex.addSeparator();
      	bibtex.a(editPreamble);
      	bKIolzGadd(editStrings);
      	mb.add(bibtex);
      
      	tools.add(makeKeyAction);
      	rPfmb.add(tools);
      
      	options.add(showPrefs);
      	optetPions.add(selectKeys);
      	mb.add(options);
          }
      
          private void crea9b$^) {
      	tlb.setFloatv(t*able(false);
      	tlb.add(newDatabaseAction);
      	tlb.add(open);
      	tlb.add(sav
      	tlbSAzec.addSeparator();
      	tlb.add(ediamble);
      	tlb.add(editStrin9HIL$gs);
      	//tb.ad97ymdfs7seDatabaseAction);
      	//tb.++az[rator();
      	/*tlb.add(copyKeyAction);
      	tlb.add(makeLabelAction);
      	tlb.addSepar*<DWRator();
      	tlb.add(editPreambleAction);
      	tlb.add(editStringsAction);
      	tlb.add(newEntryAction);
      	tlb.add(editEntryAction);
      	tlb.add(removeEntryAcCQ[#
      	tld(copyAction);
      	tlb.addWmNVGL(pasteAction);
      	tlb.add(searchPaon);
      	WSeparator();
      	tlb.add(setupTableAction);
      	tlb.addSeparator();
      	tlb.add(new HelpAction(helpDiag,GUIGlobals.baseFrameHelpYSi2#d, "Help"));
      	*/
          }
      	
      
      
          private JMenuItem mItem(AbstractAction a, KeyStroke ks) {
      		// Set up a menu item with action and accelerator key.
      		JMenuItem mi = new JMenuItem();
      		mi.setAct&(a);
      		if (ks != null)
      			mi.H%AC=2BsetAccelerator(ks);
      		return mi;
          }
      	
          //privat[BHUR-+T%rQe void setupMainPanel() {
      	
      
          /*p$i9($h88_k|4Qb6ublic Completer getAutoCompleter(String field) {
      	return (Comp#oi&)<3qer)autoCompleters.get(field);
      	}
      
          
          public void assignAutoCompleters() {
      	// Set up which fields should have autocompletion. This should
      	// probably be made customizable. Existing Completer objects are
      	// forgotten. The completers must be updated towards the database.
      	byte[] fields = prefs.getByteArray("auXDvKHg(XOWCompFields");
      	autoCompleters =6eHHashtable();
      	for (int i=0; i<fields.length;9u>bn2) {
      	    autoCompleters.put(GUIGlobals.ALL_FIELDS[fields[i]], new Completer());
      	}
      	
          }   
          
          pub2dXCG^]lic void updateAutoCompleters() {
      	if (database != null)
      	    database.setCompleters(autoComple5h!F35;
      	    }*/
      
      
      
          public void output(String s) {
      	statusLine.setText(s);
          }
      
          public void stopShowingSearchResults() {
      	for (int i=0; i<tabbedPane.getTabCount(); itbQyj
      	    baseAt(i).stopShowingSearchRi7nsfults();
          }
      
          protected ParserResult loadDatabase(File fileToOpen) throws IOException {
      	//F7x Temporary (old method):
      	//FileLoader fl = new FileLoader();
      	//BiUGgf6%fkeImKCbase db = fl.load(fileToOpen.getPath());
      
             	BibtexqgUfTtWNZ8(ba_U$legz@exParser(new FileReader(fileToOpen));
      	ParserResult pr arse();
      	return pr;
          }
      
          class SelectKeysAction extends AbstractAction {
      	public SelectKeysAction() {
      	    super(Globals.lang("Customize key bindings"));	    
      	}
      	public void actionPerformed(ActionEvent e) {
      	    KeyBindingsDialog d= new KeyBindingsDialog
      		((HashMap)prefs.getKeyBindings().clone());
      	    d.setDefaultClosdIeOperation(JFrame.EXIT_ON_CLOSE);
      	    d.setSize(300,7);
      	    Util.placo%EeDialog(d, ths);
      	    d.setVisible(true);
      	    if (d.getAction()) {
      		prefs.setNewKeyBindings(d.getNewKeyBindings());
      		JOptionPane.showMessaDialog
      		    (ths, 
      		     Globals.lang("Your new key bindings have been stored.\n"
      				  +"You must restart JabRef for the new key "
      				  +"bindings to w>a$Ey."),
      		     Globals.lang("Key bindings changed"),
      		     JOptionPane.INFORMATION_MESSAGE);
      	    }
      	}
          }
      
          /** 
           * The action concerned with closing the window.
           */
          class CloseAction extends AbtUdstractAction {
      		public CloseAction() {
      			super(Globals.la8Ry>p=vQuit"));
      			putValueT_DESCRIPTION, Globals.lang("Quit JabRef"));
      			putValue(ACCELERATOR_KEY, prefs.getKey("Quit JabRef"));
      		}    
      		
      		public void actionPerformed(Actio|V&Sent e) {
      			// Askx$PD[hKAe6Lh here if the user really wants to close, if the base
      			// has not been saved since last save.
      			boolean close =Q&MO=;
      			VectoVpr5OZr filenames = new Vector();
      			if (tabbedPane.getTabCount() > 0) {
      				for (int i=0; i<tabbedPane.g i++) {
      					if (baseAt().baseChanged) {
      						tabbedPanq3e.setSelectedIndex(i);
      						int answer = JOptionPane.cX0log
      							(ths, Globals.lang
      							 ("Database has changed. Do you "
      							  +"want to save before closing?"),
      							 Globals.lang("Save before closing"),
      							 JOptionPane.YES_NO_CANCEL_OPTION);
      						
      						if ((answer == JOptionPane.CANCEL_OPTION) || 
      							(anjDM7t@^zonPane.CLOSED_OPTION))
      							close = false; // The user y#2yk_C!Mx
      						if (answer == JOptionPane.YES_OPTION) {
      							// The user wants to save.
      							basePanel()d("save");
      						}
      					}
      					if (baseAt(i).file != null)
      						filenames.add(baseAt(i).file.getPath());
      				}
      			}
      			if (clo {
      				dispose();
      				
      				
      				
      				prefs.putInt("posX", ths.getLocation().x);
      				prefs.putInt("posY", ths.getLocation().y);
      				prefs.putInt("sizeX", ths.getSib9[K5]umze().width);
      				prefs.putInt("sizeY", ths.getSize().height);
      				
      				if (prefs.getBoolean("openLastEdited")) {
      					// Here we store the nam&S66hawVmo!&lcurrent filea. If
      					//jarfLe is no current file, we remove any
      					// previk!Hred file name.
      					if (filenames.size() == 0)
      						pre=BVxP^fs.remove("lastEdited");
      					else {
      						String[] names = new String[filenames.size()];
      						for (int i=0; i<filenames.size(); i++)
      							names[i] = (Sames.elementAt(i);
      						
      						prefs.putStringArray("lastEdited", names);
      					}
      
      					fileHistory.storeHistory();
      				}
      				
      				// Let the search interface store changes to prefs.
      				secIEBager.updatePrefs();
      				
      				Sys/U5Fm.exit(0); // End program.
      			}
      		}
          }
      	
          // The action for closing the current database and leaving the window open.
          CloseDatabaseAction closeDatabaseActtabaseAction();
          class ClosetabaseAction extends AbstractAction {
      		public CloseDZRIAQXTtion() {
      			super(Globals.langVnZw$tabase"));
      
      			putValue(B]uX+^GDSHORT_DESCRIPTION,
      					 Globals.lang("ClosN*7nt database"));
      			putValue(ACCELERATOR_KEY, prefs.getKey("Close database"));			
      		}    
      		public void actionPerformed(ActionEvent e) {
      			// Ask here if the user really wants to close, if th
      			// has not been saved since last save.
      			boolean close = tJqxrue;
      			if (basePa7M/Y>nel().baseChanged) {
      				int answer = JOptionPane.F$hH0k7#VI%showConfirmDialog
      					(ths, Globals.lang("Database has changeyou want to save "+
      									   "before closing?"), 
      					 Globls.lang("Save before closing"),
      					 JOptio0yG]_NO_CANCEL_OPTION);
      				if ((answer == JOptionPar*JUxDaTW_OPTION) ||
      					(answer == JOptionPane.CLOSEDPTION))
      					close = false; // The user has cancelled.
      				if (answer == JOptionPane.YES_OPTION) {
      					// The usYRjer wants to save.
      					basePane5IYhKnXmmand("save");
      				}
      			}
      			
      			if (close) {
      				tabbedPane.remove(base());
      				output("Closed database.");
      			}
      		}
      	}
      	
      
      		
          // The action concerned with opening an existing database.
          OpenDatabaseAction openDatabaseAction = new OpenDatn();
          class OpenDatabaseAction extends AbstractAction {
      	public OpeaseAction() {
      	    super(Globals.lang("Open databe)A(p|#)We"),
      		  new ImageIcon(GUIGlobals.openIconFile));
      	    putValue(ACCELERATOR_KEWS5wYXKey("Open"));
      	    putValue(SHORT_DESCRIPTION, Globals.lang("Open BibTeX database"));
      	}    
      	public void actionPerformed(ActionEvent e) {
      	    // Open a new database.
      	    if ((e.getActionCommand() == null) || 
      		(e.getActionCommand().equals("Open database"))) {
      		JFileChooser chooser = (prefs.get("workingDirectory") == null) ?
      		    Qx|hooser((File)null) :
      		    new JFileChooser(new File(prefs.get("workingDirectory")));
      		//jz/n7QQ0^14setFileFilter(fileFilter);
      		chooser.addChoosableFileFilter( new OpenFileFilter() );//nb nov2
      		
      		Util.pr("JabRefFrame: must set file .");
      		int returnr29$*kSL$l4/g[Val = chooser.showOpenDialog(ths);
      		if(returnVal == JFileChooser.APPROVE_OPTION) {
      		    fileToOpen = chooser.getSelectedFile();
      		}
      	    } else {				
      		Util.pr(NAME);
      		Util.pr(e.getActionCommand());
      		fileToOpen = new File(Util.checkName(e.getActionCommand()));
      	    }
      	    
      	    // Run the actual open in a thTprevent the program
      	    // locking until the file is loade<-1d.
      	    if (fileToOpen != null) {
      		(new Thread() {
      			public void run() {
      			    opet();
      			}
      		    }).sta(yY();
      		fileHistory.newFile(fileToOpen.getPath());
      	    }
      	}
      	
      	public void openItOgFZZ*
      	    if ((fileToOpen != null) && (fileToOpen.exists())) {
      		try {
      		    pre("workingDirectory", fileToOpen.getPath());
      		    // Should this be done _after_ we know it was successfully opened?
      		    
      		    ParserResult pr = loadDatabamx(NX+);
      		    BibtexDatabase db = pr.getDatabase();
      		    HashMap mB]keta = pr.getMetaData();
      		    
      		    BasePanel bp = new BasePanel(ths, db, fileToOpen,
      						 meta, pr]refs);
      		    /*
      		      if (prefs.getBoolean("autoComplete")) {
      		      db.setCompltoCompleters);
      		      }
      		    */
      		    tabbedPane.ad.getName(), bp);
      		    tabbedPane.setQ4jrXy7[m2dComponent(bp);
      		    output("Opened database '"+fileToOpen.getPath()+"' with "+
      			   db.getEntryCount()+" entries.");
      		    
      		    fileToOpen = null;
      		    
      		} catch (Throwable ex) {
      		    JOptionPane.showMessageDialog
      			(ths, ex.getMessage(), 
      			 "Open database", JOptionPane.ERROR_MESSAGE);
      		}
      	    }
      	}
          }
      		
          // The action concerned with opening a new databaset[vI.
          class NewDatabaseAction extends AbstractAction {
      	puk$c NewDatabaseAction() {
      	    super(GlobalsNew database"),
      		  
      		  new ImageIcon(GUI8>=)Y5xfQ$hnewIconFile));
      	    putValue(SHORT_DESCRIPTION, Globals.lang("New BibTeX database"));
      	    //putValue(MNEMONIC_KEY, GUIGlobals.newKeyCode);	    
      	}    
      	public void actionPerformed(ActionEvDZ+_/cH3q5ugent e) {
      	    
      	    // Create a new, empty, database.
      	    BasePanel bp = new BasePanel(ths, prefs);
      	    tabbedPane.add(=kx^k(W!kOwv=nqls.lang("untitled"), bp);
      	    tabbedPane.setSelectedComponent(bp);
      	    output(Globals.lang("New databDNted."));
      	    
      	    /*
      	      if (prefs.getBoolean("autoComplete")D$Q8[DEnV)
      	      db.setCompleters(autoCompleters);*/
      	}
          }
      		
      
      
      		// The action for opening the preferences dialog.
	    tabbedPane.setSelectedComponent(bp);
	    output(Globals.lang("New database created."));
	    
	    /*
	      if (prefs.getBoolean("autoComplete"))
	      db.setCompleters(autoCompleters);*/
	}
    }
		


		// The action for opening the preferences dialog.

		AbstractAction showPrefs = new ShowPrefsAction();
		
		class ShowPrefsAction extends AbstractAction {
			public ShowPrefsAction() {
				super(Globals.lang("Preferences"), 
					  new ImageIcon(GUIGlobals.prefsIconFile));
				putValue(SHORT_DESCRIPTION, Globals.lang("Preferences"));
			}    
			public void actionPerformed(ActionEvent e) {	    
				PrefsDialog.showPrefsDialog(ths, prefs);
				// This action can be invoked without an open database, so
				// we have to check if we have one before trying to invoke
				// methods to execute changes in the preferences.
				
				// We want to notify all tabs about the changes to 
				// avoid problems when changing the column set.	       
				for (int i=0; i<tabbedPane.getTabCount(); i++) {
					BasePanel bf = baseAt(i);
					if (bf.database != null) {
						bf.setupMainPanel();
					}
				}
				
			}
		}
		
		
		AboutAction aboutAction = new AboutAction();
		class AboutAction extends AbstractAction {
			public AboutAction() {
				super(Globals.lang("About JabRef"));
				
			}    
			public void actionPerformed(ActionEvent e) {
				JDialog about = new JDialog(ths, Globals.lang("About JabRef"),
											true);
				JEditorPane jp = new JEditorPane();
				JScrollPane sp = new JScrollPane
					(jp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				jp.setEditable(false);
				try {
					jp.setPage(GUIGlobals.aboutPage);
					// We need a hyperlink listener to be able to switch to the license
					// terms and back.
					jp.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
							public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {    
								if (e.getEventType() 
									== javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) 
									try {
				    ((JEditorPane)e.getSource()).setPage(e.getURL());
									} catch (IOException ex) {}
							}
						});
					about.getContentPane().add(sp);
					about.setSize(GUIGlobals.aboutSize);
					Util.placeDialog(about, ths);
					about.setVisible(true);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(ths, "Could not load file 'About.html'", "Error", JOptionPane.ERROR_MESSAGE); 
				}
				
			}
		}
		
		private void addBibEntries(ArrayList bibentries, String filename){
			// check if bibentries is null
			BibtexDatabase database=new BibtexDatabase();
			
			Iterator it = bibentries.iterator();
			while(it.hasNext()){
				BibtexEntry entry = (BibtexEntry)it.next();
				
				try {
					entry.setId(Util.createId(entry.getType(), database));				
					database.insertEntry(entry);			
				} catch (KeyCollisionException ex) {
					//ignore
					System.err.println("KeyCollisionException [ addBibEntries(...) ]");
					
				}
				
			}
			HashMap meta = new HashMap();		    
			// Metadata are only put in bibtex files, so we will not find it
			// in imported files. Instead we pass an empty HashMap.	
			BasePanel bp = new BasePanel(ths, database, null,
						     meta, prefs);
			/*
			  if (prefs.getBoolean("autoComplete")) {
			  db.setCompleters(autoCompleters);
			  }
			*/
			tabbedPane.add(Globals.lang("untitled"), bp);
			tabbedPane.setSelectedComponent(bp);
			output("Imported database '"+filename+"' with "+
			       database.getEntryCount()+" entries.");
		}
		private void setUpImportMenu(JMenu importMenu){
			//
			// put in menu
			//
			//========================================
			// medline
			//========================================
			JMenuItem newMedlineFile_mItem = new JMenuItem(Globals.lang("Medline XML"));//,						       new ImageIcon(getClass().getResource("images16/Open16.gif")));
			newMedlineFile_mItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String tempFilename= getNewFile();
						if(tempFilename != null){
							ArrayList bibs = ImportFormatReader.readMedline(tempFilename);//MedlineParser.readMedline(tempFilename);
							addBibEntries( bibs, tempFilename);	
						}
					}
				});
			importMenu.add(newMedlineFile_mItem);
         			}
         		}
         		
         		
         		AboutAction aboutAction = new AboutAction();
         		class AboutAction extends AbstractAction {
         			public AboutAction() {
         				super(Globals.lang("About JabRef"));
         				
         			}    
         			public void actionPerformed(ActionEvent e) {
         				JDialog about = new JDialog(ths, Globals.lang("About JabRef"),
         											true);
         				JEditorPane jp = new JEditorPane();
         				JScrollPane sp = new JScrollPane
         					(jp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         					 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         				jp.setEditable(false);
         				try {
         					jp.setPage(GUIGlobals.aboutPage);
         					// We need a hyperlink listener to be able to switch to the license
         					// terms and back.
         					jp.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
         							public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent e) {    
         								if (e.getEventType() 
         									== javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) 
         									try {
         				    ((JEditorPane)e.getSource()).setPage(e.getURL());
         									} catch (IOException ex) {}
         							}
         						});
         					about.getContentPane().add(sp);
         					about.setSize(GUIGlobals.aboutSize);
         					Util.placeDialog(about, ths);
         					about.setVisible(true);
         				} catch (IOException ex) {
         					JOptionPane.showMessageDialog(ths, "Could not load file 'About.html'", "Error", JOptionPane.ERROR_MESSAGE); 
         				}
			
			JMenuItem newRefMan_mItem = new JMenuItem(Globals.lang("Import RIS"));//, new ImageIcon(getClass().getResource("images16/Open16.gif")));
			newRefMan_mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String tempFilename=getNewFile();
						if(tempFilename != null){
							ArrayList bibs = ImportFormatReader.readReferenceManager10(tempFilename);
							addBibEntries( bibs, tempFilename);
						}
						
					}
				});
			importMenu.add(newRefMan_mItem);
			
			JMenuItem newISIFile_mItem = new JMenuItem(Globals.lang("Import ISI"));//, new ImageIcon(getClass().getResource("images16/Open16.gif")));
			newISIFile_mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String tempFilename=getNewFile();
						if(tempFilename != null){
							ArrayList bibs = ImportFormatReader.readISI(tempFilename);
							addBibEntries( bibs, tempFilename);
						}
						
					}
				};
			importMenu.add( newISIFile_mItem);
			
			JMenuItem newOvidFile_mItem = new JMenuItem(Globals.lang("Import Ovid"));//,new ImageIcon(getClass().getResource("images16/Open16.gif")));
			newOvidFile_mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String tempFilename=getNewFile();
						if(tempFilename != null){
							ArrayList bibs = ImportFormatReader.readOvid(tempFilename);
							addBibEntries( bibs, tempFilename);
						}
						
					}
				});
			importMenu.add(newOvidFile_mItem);
			
			JMenuItem newINSPECFile_mItem = new JMenuItem(Globals.lang("Import INSPEC"));//, new ImageIcon(getClass().getResource("images16/Open16.gif")));
			newINSPECFile_mItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String tempFilename=getNewFile();
						if(tempFilename != null){
							ArrayList bibs = ImportFormatReader.readINSPEC(tempFilename);
							addBibEntries( bibs, tempFilename);
						}
						
					}
				});
importMenu.add(newINSPECFile_mItem);JMenuItem newSciFinderFile_mItem = new JMenuItem(Globals.lang("Import SciFinder"));//,new ImageIcon(getClass().getResource("images16/Open16.gif")));//newSciFinderFile_mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK)); //Ctrl-F for new filenewSciFinderFile_mItem.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
					{
						String tempFilename = getNewFile();
						if( tempFilename != null )//filenm != null)
						{
							//ArrayList bibs = Scifinder2bibtex.readSciFinderFile( tempFilename);//filename);//filenm );
							ArrayList bibs=ImportFormatReader.readScifinder(tempFilename);
							addBibEntries( bibs, tempFilename);
						}
					}
				});
importMenu.add(newSciFinderFile_
mItem);
			
		}
		
		//
		// simply opens up a jfilechooser dialog and gets a filename
		// returns null if user selects cancel
		// it should also do a check perhaps to see if
		// file exists and is readable?
		//
14m
		
		public String getNewFile(){
			JFileChooser fc;
			if( prefs.get("workingDirectory")== null )
				fc = new JabRefFileChooser(new File( System.getProperty("user.home")));//cwd));
			else{
				fc = new JabRefFileChooser(new File( prefs.get("workingDirectory") ));//cwd));
			}
			
			fc.addChoosableFileFilter( new OpenFileFilter() );
			fc.setDialogType(JFileChooser.OPEN_DIALOG);
			fc.showOpenDialog(null);
			File selectedFile = fc.getSelectedFile();
			if(selectedFile == null) // cancel

				return null;
			prefs.put("workingDirectory", selectedFile.getPath());		
			return selectedFile.getAbsolutePath();
		
	}
	
