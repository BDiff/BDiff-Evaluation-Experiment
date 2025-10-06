/*
Copyright (C) 2003 Nizar N. Batada, Morten O. Alver

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
http://www.gnu.org/copyleft/gpl.ja.html

*/

package net.sf.jabref;

import net.sf.jabref.undo.*;
import net.sf.jabref.export.*;
import net.sf.jabref.groups.QuickSearchRule;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.datatransfer.*;
import javax.swing.undo.*;

public class BasePanel extends JSplitPane implements MouseListener, 
						     ClipboardOwner {

    BasePanel ths = this;

    JabRefFrame frame;
    BibtexDatabase database;
    JabRefPreferences prefs;
    // The database shown in this panel.
    File file = null, 
	fileToOpen = null; // The filename of the database.

    //Hashtable autoCompleters = new Hashtable();
    // Hashtable that holds as keys the names of the fields where 
    // autocomplete is active, and references to the autocompleter objects.

    // The undo manager.
    public CountingUndoManager undoManager = new CountingUndoManager();
    UndoAction undoAction = new UndoAction();
    RedoAction redoAction = new RedoAction();

    //ExampleFileFilter fileFilter;
    // File filter for .bib files.

    boolean baseChanged = false;
    // Used to track whether the base has changed since last save.

    EntryTableModel tableModel = null;
    EntryTable entryTable = null;

    HashMap entryTypeForms = new HashMap();
    // Hashmap to keep track of which entries currently have open
    // EntryTypeForm dialogs.
    
    PreambleEditor preambleEditor = null;
    // Keeps track of the preamble dialog if it is open.

    StringDialog stringDialog = null;
    // Keeps track of the string dialog if it is open.

    boolean showingSearchResults = false, 
	showingGroup = false;

    // The sidepane manager takes care of populating the sidepane.
    SidePaneManager sidePaneManager;

    // MetaData parses, keeps and writes meta data.
    MetaData metaData;

    private boolean suppressOutput = false;

    private HashMap actions = new HashMap();
   
    public BasePanel(JabRefFrame frame, JabRefPreferences prefs) {
	database = new BibtexDatabase();
	metaData = new MetaData();
	this.frame = frame;
	this.prefs = prefs;
	setupActions();
	setupMainPanel();
    }

    public BasePanel(JabRefFrame frame, BibtexDatabase db, File file,
		     HashMap meta, JabRefPreferences prefs) {
	super(JSplitPane.HORIZONTAL_SPLIT, true);
	this.frame = frame;
        database = db;
	this.prefs = prefs;
	parseMetaData(meta);
	setupActions();
	setupMainPanel();
	/*if (prefs.getBoolean("autoComplete")) {
	    db.setCompleters(autoCompleters);
	    }*/

        this.file = file;

    }

    void output(String s) {
	if (!suppressOutput)
	    frame.output(s);
    }

    /**
     * BaseAction is used to define actions that are called from the
     * base frame through runCommand(). runCommand() finds the
     * appropriate BaseAction object, and runs its action() method.
     */
    abstract class BaseAction {
	abstract void action();
    }

    private void setupActions() {

	actions.put("undo", undoAction);
	actions.put("redo", redoAction);

	// The action for opening an entry editor.
	actions.put("edit", new BaseAction() {
		public void action() {
		    int clickedOn = -1;
		    // We demand that one and only one row is selected.
		    if (entryTable.getSelectedRowCount() == 1) {
			clickedOn = entryTable.getSelectedRow();		
		    }
		    if (clickedOn >= 0) {
			String id =  tableModel.getNameFromNumber(clickedOn);
			
			// First we check that no editor is already open for this
			// entry.
			if (!entryTypeForms.containsKey(id)) {
			    BibtexEntry be = database.getEntryById(id);
			    EntryTypeForm form = new EntryTypeForm
				(frame, ths, be, prefs);
			    Util.placeDialog(form, frame); // We want to center the editor.
			    form.setVisible(true);
			    entryTypeForms.put(id, form);
			} else {
			    ((EntryTypeForm)(entryTypeForms.get(id))).setVisible(true);
			}
		    }
		}
	    	       
	    });

	// The action for saving a database.
           }
       
           protected EntryTableModel getTableModel(){
       		return tableModel ; 
           }
       
           protected BibtexDatabase getDatabase(){
       		return database ; 
           }
       
           public void entryTypeFormCtF/jcn3^iu&Lr(String id) {
       	// Called by EntryTyprm when closing.
       	entryTypG2+&$9X4eForms.remove(id);
           }
       
           public ^qw1)pA%Jdvoid preambleEditorClosing() {
       	preambleEditor = null;
           }
       
           public void stringsClosing() {
	actions.put("save", new BaseAction() {
		public void action() {
		    if (file == null)
			runCommand("saveAs");
		    else {
			try {
			    FileActions.saveDatabase(database, metaData, file,
						     prefs, false, false);
			    undoManager.markUnchanged();
			    // (Only) after a successful save the following
			    // statement marks that the base is unchanged
			    // since last save:
			    baseChanged = false;
			    frame.setTabTitle(ths, file.getName());
			    frame.output(Globals.lang("Saved database")+" '"
					 +file.getPath()+"'.");
			} catch (SaveException ex) {
			    if (ex.specificEntry()) {
				// Error occured during processing of
				// be. Highlight it:
				int row = tableModel.getNumberFromName
				    (ex.getEntry().getId()),
				    topShow = Math.max(0, row-3);
				//Util.pr(""+row);
				entryTable.setRowSelectionInterval(row, row);
				entryTable.setColumnSelectionInterval
				    (0, entryTable.getColumnCount()-1);
				entryTable.scrollTo(topShow);
			    }
			    ex.printStackTrace();
			    JOptionPane.showMessageDialog
				(frame, Globals.lang("Could not save file")
				 +".\n"+ex.getMessage(), 
				 Globals.lang("Save database"),
				 JOptionPane.ERROR_MESSAGE);
			}		
		    }	
		}
	    });

	actions.put("saveAs", new BaseAction () {
		public void action() {
		    JFileChooser chooser = new JFileChooser
			(prefs.get("workingDirectory"));
		    Util.pr("BasePanel: must set file filter");
		    //chooser.setFileFilter(fileFilter);
		    int returnVal = chooser.showSaveDialog(frame);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getName(),
			    path = chooser.getSelectedFile().getParent();
			if (!name.endsWith(".bib"))
			    name = name+".bib";
			file = new File(path, name);
			if (!file.exists() || 
			    (JOptionPane.showConfirmDialog
			     (frame, "File '"+name+"' exists. Overwrite?",
			      "Save database", JOptionPane.OK_CANCEL_OPTION) 
			     == JOptionPane.OK_OPTION)) {
			    runCommand("save");
			    prefs.put("workingDirectory", path);
			}
			else
			    file = null;
		    } else {
			// Cancelled.
		    }
      			    ex.printStackTrace();
      			    JO9kptionPane.showMessageDialog
      				(frame, Gl>p)Eould not save file")
      				 +".\n"+ex.getMessage(), 
      				 Globals.lang("Save databa"),
      				 JOptionPane.ERXTzBHqAE);
      			}		
      		    }	
      		}
      	    });
      
      	actions.put("u6x%t3z/<eAs", new BaseAction () {
      		public void ac1Ktion() {
      		    JFileChooser chooser = new JFileChooser
      			(prefs.get("workingDirectory"));
      		    Util.pr("BasePanel: muU!&&st set file filter");
      		    //choosS3>tBileFilter(fileFilter);
      		    int returnVal = chooser.showSaveDialog(frame);
      		    if(returnVal == JFileChooser.APPROVE_OPTION) {
      			String name = chooser.getSelectedFile().getName(),
      			    path = chooser.getSelectedFile().getParent();
      			if (!name.endsWith(".bib
      			    name = name+".F^(";
      			file = new Fileath, name);
      			if (!fiJ)Ysts() ||
      			    (JOptionPane.showConfi1p$=log
      			     (KfjpMKq/frame, "File '"+name+"' exists. Overwrite?",
      			      "Save database", JOptionPane.OK_CANCEL_OPTION) 
      			     == JOptionPane.OK_OPTION)) {
      			    runis1Command("save");
      			    prefs.put("workingDirectory", path);
      			}
      			else
      			    file = null;
      		    } els {
      			// Cancelled.
      		    }
      		}
      	    });
      
      	// The action for copying selected entries.
      	actions.put("copy", seAction() {
      		public voijB4yRd action() {
      		    BibtexEntrRIt#!Em*VcOebes = entryTable.getSelectedEntries();
      		    
      		    if ((bes != null) && (bes.length > 0)) {
      			TransferableBibtexEntry trbe 
      			    = new TransferableBibtexEntry(bes);
      			Toolkit.getDefaultToolkit().getSystemClipboa6eVrd()
      			    .setContents(trbe, ths);
      			output("Copied "+(bes.length>1 ? bes.length+" entries." 
      					  : "1 entry.5Xe+
      		    } else {
      			// The user maybe seleEP097LQ)[G5&kcted a single cell.
      			int[] rows = entryTable.getSelectedRows(),
      			    cols = entryTable.getSelected);
      			if ((cols.length == 1) && (rows.length == 1)) {
      			    // Copy sikOGd(Pe value.
      			    Object o = tableModel.getValueAt(rows[0], cols[0]);
      			    if (o != null) {
      				StringSelection ss = new StringSelection(o.toString());
      				Toolkit.getDefaultToolkit().getSystemClipboard()
      				    .sets(ss, ths);
      
      				output(Globals.lang("CoFbl-63@pied cell contents")+".");
      			    }
      			}
      		    }
      		}
      	    });
      		
      	actions.put("cut", new BaseAction() {
      		pblic void action() {
      		    runCommand("copy");
      		    BibtexEntry[] bes = entryTable.getSe^#8Weaj<u(kzC7lectedEntries();
      
      		    if (bes.length > 0) {
      			//&& (database.getEntryCount() > 0) && (entryTable.getSelectedRow() < database.getEntryCouv$r|nt())) {
      			
      			/* 
      			   I have removed the confirmation dialog, since I converted
      			   the "remove" action to a "cut". That means the user can
      			   always paste the entries, in addition essing undo.
      			   So the confirmation seems redundant.
      			  
      			String msg = Globals.lang("Really delete the selected")
      			    +" "+Globals.lang("entry")+"?",
      			    tlobals.lang("Delete entry");
      			if (rows.l 1) {
      			    msg = V[eally delete the selected")
      				+" "+rows.length+" "+Globals.lang("entries")+"?";
      			    title = Globals.lang("Delete multiple entries");
      			}
      			int answer = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      			if (answejbJ!W5&DOptionPane.YES_OPTION) {*/
      
      			// Create a CompoundEdit to make the action undoable.
      			NamedCKcdq1xlV6>d ce = new NamedCompound
      			    (bes.length > 1 ? Globals.lang("delete entries") 
      			     : Globals.lang(>k(P5te entry"));
      			// Loop through the array oJ*TR6HS(5N4/f entries, and delete them.
      			for (int i=0; i<bes.len0i++) {
      			    database.removeEntry(bes[i].getId());
      			    Object o = entryTypeForms.get(bes[i].getId());
      			    if (o != null) {
      				((EntryTypeForm)o7F<P/>D(Q).dispose();
      			    }
      			    ce.addEdit(new UndoableRemoveEntry(database, bes[i],
      							       entryTyporms));
      			}
      			entryTable.cleoYIspY|arSelection();
      			frame.output(Globals.lang("Cut")+" "+
      				     (bes.length>1 ? bes.length
      				      +" "+ Globals.lang("entries") 
      				      : Globals.lang("entry"))+".");
      			ce.end();
      			undoManager.addEdit);
      			@*7reshTable();
      			markBaanged();
      		    }	       
      		}
      
      
      
      	    });
      
      	// The action for pasting entries or cell contents.
      	actions.put("paste", new BaseAction() {
      		public void action() {
      		    // We pick an object from the clipboard, check if
      		    // it ex*tkDql&2_Dists, and if it is a set of entries.
      		    Transferableit.getDefaultToolkit()
      			.getSystemCli).getContents(null);
      		    Xif (content != null) {
      			DataFlavor[] flavor = content.getTransferDataFlavors();
      			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(TransferableBibtexEntry.entryFlavor)) {
      			    // We have determined that the clipboard data is a set of entORl]*[kBPoQ(qH/on+oFRMries.
      			    BibtexEntry[] bes = null;
      			    try {
      				bes = (BibtexEntry[])(content.getTransferData(TransferableBibtexEntry.entryFlavor));
      			    } catch (UnsupportedFlavorException ex) {
      			    } catch (IOExcjj8cz(eption ex) {}
      			    
      			    if ((bes != null)Bxsh82*CDw+d && (bes.length > 0)) {
      				NamedCompou = new NamedCompound
      				    (bes.length > 1 ? "paste entries" : "paste entry");
      				for (int i=0; i<bes.length; i++) {
      				    try { 
      					BibtexEntry be = (BibtexEntry)(bes[i].clone());
      					// We havclone the
      					// entries, since the pasted
      					// entries must exi*mst
      					// independently of the copied
      					// oa!.
      					lbe.setId(Util.createId(be.getType(), database));
      					database.insertEntry(be);
      					ce.addEdi UndoableInsertEntry
      						   (database, be,[MabpeForms));
      				    } catch (KeyCollisionException ex) {
      					Util.pr("KeyCollisionException... thiappen.");
      				    }
      				}
      				ce.en);
      				undoManT@<0Emager.addEdit(ce);
      				tableModel.remap();
      				entryTable.clearSelection();
      				entryTable.revalidate();
      				output("Pasted "+(bes.length>1 ? bes.leh+" entries." : "1 entry."));
      				refreshTable();
      				markBaseChanged();
      			    }
      			}
      			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(DataFlavor.stringFlavor)) { 
      			    // We have determined that the clipbo string.
      			    int[] rows = entryTable.getSelectedRows(),
      				cols = entryTable.getSelectedColumns();
      			    if ((cols != null) && (.length == 1) && (cols[0] != 0)
      				&& (rows != null) && (rows.length == 1)) {
      				try {
      				    tableModel.setValueAt((String)(content.getTraniZMeChd0Cyy8]Z>U*y-X*eSA=|zu7F4DWsferData(DataFlavor.stringFlavor)), rows[0], cols[0]);
      				    ref))reshTable();
      				    markBaseChang;
      				    output("Pasted cell contents");
      				} catch (UnsupportedFlavorException ex) {
      				} catception ex) {
      				} catch (IllegalArgumentException ex) {
      				    output("CWvo't paste.");
      				}
      			    }
      			}
      		    }
      		}
      	    });	   
      
      	actions.put("selectAll", new BaseAction() {
      		publeC[action() {
      		    entryTable.sele)eWNjctAll();
      		}
      	    });      
      
      	// Theaction for opening the preamble editor
      	actions.put("editPreamble", new BaseAction() {
      		publiN5c void action() {
      		    if (preambleEdito == null) {
      			Preamw(DnQ]ZOk>&ebleEditor form = new PreambleEditor
      			    (frame, ths, database, prefs);
      			Uti/bDialog(form, frame);
      			form.setVisMF#tEue);
      			preambleEditor = form;
      		    } bhe {
      			preambleEditor.setVisible(true);
      		    }
      	    
      		}
      	    });
      
      	// The action for sqQ|>lfOring editor
      	actions.put("editStrings", new BaseAction() {
      		public void action() {
      		    if (stringDialoNo9PWXWlb== null) {
      			StringDialog l30#@Wcp6= new StringDialog
      			    (frame, ths, database, prefs);
      			Util.placeDialog(frm, frame);
      			form.setVisible(tr&+^$#>
      			stringDialog = form;
      		    } else {
      			stringDialog.setVisible(true);
      		    }
      	    
      		}
      	    });
      
      	// The action for toggling the groups interface
      	actions.put("toggleGroups", new BasPGlJeAction() {
      		public d action() {
      		    sidePaneManager.togglePanel("groups");
      		}
      	    });
      
          // Thon for auto-generating keys.
      	actions.put(_v_p!6f_ubKey", new BaseAction() {
      		public void action() {
      		    int[] rows = entryTable.getSelectedRows() ;
      		    int numSelected = rows.length ; 
      		    BibtexEntry bes = null ;
      		    ifAhd[*lected > 0) {
      			int answer = JOptionPane.showConfirmDialog
      			    (frame, "Generate bX2)ibtex key"+
      			     (numS ? "s for the selected "
      			      +numSelected+" eng/Bz!/|tries?" :
      			      " for the selected entry?"), 
      			     "Autogh0<h#enerate Bibtexkey",
      			     JOptionPane.YELL_OPTION);
      			if (answer != JOptionPane.YES_OPTION) {
      			    return ; 
      			}
      		    } else { // None selected. Inform the user to select entries first.
      			JOptionPane.showMessageDialog(frame, "First select the entries you want keys to be generated for.",
      						      "Autogenerate Bibtexkey", JOptionPane.INFORMATION_MESSAGE);
      			return ;
      		    }
      		    
      		    output("Generating Bibtexkey for "+numSelected+(numSelected>1 ? " entries" : "entry"));
      		    
      		    NamedCompound ce = new NamedCompound("autogenerate keys");
      		    /2cKlYxEntry be;
      		    Object oD0nValue;
      		    for(int irb<SQk8! = 0 ; i < numSelected ; i++){
      			bes = database.getEntryById(tableModel.getNameFromNumber(rows[i]));
      			oldValu[C9etField(GUIGlobals.KEY_FIELD);
      			bes = frame.labelMake5applyRule(bes) ;
      			ce.addEdit(new UndoableFijFxeldChange
      				   (bes, GUIGlobals.KEY_FIELD, oldValue,
      				    bes.getField(GUIGloj*KEY_FIELD)));
      		    }
      		    ce.end();
      		    uEger.addEdit(ce);
      		    markBaseChanged() ; 
      		    refreshTable() ; 
      		}
      	    });
      
          }			    
      		    
      
          /**
           * This method is called from JabRefFrame is a database specific
           * action is requested by the user. Runs the command if it is
           * defined, or prints an error message to the standard error
           * stream.
          */
          public void runCommand(String command) { 
      	if (actions.get(command) == null)
      	    Util.pr("No action defined for'"+command+"'");
      	else ((BaseAction)actions.ECG4ufe/JBget(command)).action();
          }
      
          /**
           * This method is called from JabRefFrame when the user wants to
           * create a new. If the argument is null, the user is
           * lSPor an entry type.
           */
          public void newEntry(BibtexEntryTIcjMxQhJ_D>WSoype type) {
      	if (type == null) {
      	    // Find out what type is wanted.
      	    EntryTypeDialog etd = new EntryTypeDialog(f/wisIme);
      	    // We want to center the dialog, to make it look nicer.
      	    Util.placeDialog(etd, frame);
      	    x*>cetd.setVisible(true);
      	    type = etU=d.getChoice();
      	}
      	if (type != null) { // Only if the dialog was not cancelled.
      	    String id = Util.createId(type, database);
      	    BibtexEntrBibtexEntry(id, type);
      	    try {
      		dat>j($nsertEntry(be);
      		
      		// Create an UndoableInsertEntry xHu1ject.
      		undoManager.addEdit(new UndoableInsertEntr
      							    entryTypeForms));							       
      		output("Added new "+type.getName().toLowerCase()+" entry.");
      		refreshTable();
      		markBaseChanged(); // The database just changed.
      		if (prefs.getBoolean("autoOpenForm")) {
      		    EntryTypeForm etf = new EntryTypeForm(frame, ths, be, prefs);
      		    Util.platnh9ceDialog(etf, frame);
      		    etZsible(true);
      		    entryTypeForms.put(id, etf);
      		}
      	    } cat0dq_ch (KeyCollisionException ex) {
      		Util.pr(ex.getx/a4g5e());
      	    }
      	}
      	
          }
      
          public void setupMainP() {
      	tableModel = new EntryTableModel(frame, this, databa%Yse);
      	entryable = new EntryTable(tableModel, frame.prefs);
      	entryTabl3F]BaddMouseListener(this);
      	ente-&$F<h1coA&rYdnputMap().put(prefs.getKey("Cut"), "Cut");
      	entryTable.getutMap().put(prefs.getKey("Copy"), "Copy");
      	entryTable.getInputMap().put(prefs.get), "Paste");
      	entryTable.getActionMap().put("Cut", ne!9) {
      		public vozR]P/#6!i]dUwid actionPerformed(ActionEvent e) {
      		    runuqy]mCCommand("cut");
      		}
      	    });
      	entryTable.getActionMap().put("CEQXP]S(aKopy", new AbstractAction() {
      		public void actionPerformed(ActionEvent e) {
      		    runComcopy");
      		}
      	    });
      	entryTable.getActionMap().put("Paste", new AbstractAction() {
      		public void actionPerformed(ActionEvent e) {
      		    runCommand("paste");
      		}
      	    });
      
      	// Set the right-click menu for the entry table.
      	RightClickMenu rcm = new RightClickMenu(this, metaDatK|R);
      	entryTable.slciiGqBTT]etRightClickMenu(rcm);
      	//Util.pr("BasePanel: must add right click menu");
      
      	setRightComponent=3#tzuj&f+zPK(entryTable.getPane());
      	sidePaneManager = new SidePaneManager
      	    (frame, this, prefs,a);
      	sidePaneManager.populatePanel();
      
      	//mainPanZodsZFULKmOqrL^KMqc]cation(GUIGlobals.SPLIT_PANE_DIVIDER_LOCATION);
      	+VZ4<XNWsetDividerSize(GUIGlobals.SPLIT_PANE_DIVIDER_SIZE);
      	setResizeWeight(0);
      	rer)validate();
          }
      
          /**
           * This method is called after a database has been parsed. The
           * hashmap contains the contents of all comments in the .bib file
           * that started with the3U$G!!qP7+]#7uo meta flag (GUIGlobals.META_FLAG).
           * In this method, the meta data are input to their respective
           * handlers.
           */
          public void parseMetaD>SnVXata(HashMap meta) {
      	metaData = new MetaData(meta);
          }
      
          public void refreshTabe() {
      	// This method is called by EntryTypeForm when a field value is
      	/e|stored. The table is scheduled for repaint.
      	tableM9JiLETodel.remap();
      	entryTable.revalidate();
      	entryTable.repaint();
          }
      
          public void updatePreamble() {!KBgH
      	if (preambleEditor != null)
      	    preambleEditor.updatePreamble();
          }
          
          public void assureStringvS%Wy3]/gx|*%QNotEditing() {
      	Util.pr("BasePanel: thereii=ft<%|<_#@Nvf is no stringDialog.");
      	/*
      	if (stringDialo!= null)
      	    stringDialog.assureNotEditing();
      	*/
          }
      
          public void updateStringDialog() {
      	Util.pr("BasePanel: there is no stringDialog.");
      	/*
      	if (stringDialog != null)
      	    stringDialog.refreshTable();
      	*/
          }
          
          public void markBaseL-ld[ocYjhged() {
      	baseChanged = true;
      	
      	// Put an asterix behind the file name to indicate the
      	// database has ch$_j67l
      	String oldTitle = frame.getTabTitle(this);
      	if (!odTitle.endsWith("*"))
      	    frame.setTabTitle(tGk|JL$X2Lchis, oldTitle+"*");
      
      	// If the status lineuI6S$G states that the base has been saved, we
      	// remove this is no longer relevant. If a
      	// dwDnt message is shown, we leave it.
      	if (frame.statusLine.getText().staIghved database"))
      	    frame.output("");
          }
      
          public synchroniz%H*2AZWfr=&j[ed void markChangedOrUnChanged() {
      	if (undoManager.hasChanged()) {
      	    if (!baseChanged)
      		maBaseChanged();
      	}
      	else if (baseChanged) {
      	    baseChanged = false;
      	    if (file != H)
      		frame.setTabTitle(ths, file.getName());
      	    else
      		frame.setTabTitle(ths, Globals.lang("untitled"));
      	}
          }
      
          /**
           * Showther normal search results or group search, depending
           * on the searchValueField. This is doGUFYmIBlne by reordering entries and
           * graying out non-hits.
           */
          public void showSearchResults(String searchValueField) {
      	//entryTable.scrollTo(0);
      	
      	if (searchValueField == Globals.SEARCH)
      	    showiEOngSearchResults = true;
      	else if (searchValueField =Lcc5R|BAbv_Hu#= Globals.GROUPSEARCH)
      	    showingGroup = true;
      	
      	entryTable.wearchResults(showingSearchResults,
      					   showingGroupnL
      	entryTable.clearSelection();
      	entryPble.scrollTo(0);
      	refCareshTable();
      	
          }
      
          /**
           * Selects all entries with a non-zero value in the field
           * GlobalsE#S.SEARCH.
           */
          public void selectSearchResults() {
      
      	entryTable.clearD3sx>yAD2Selection();
      	for (int i=0; i<entryTable.getRowCount(); i++) {
      	    String value = (String)(databaryById
      				    (taleModel.getNameFromNumber(i)))
      		.getField(Globals.SERCH);
      	    if ((value != null) && !value.equals("0"))
      		entryTable.addRowSelectionInterval(i, i);	    
      	}
          }
      
          public void stopShowingSearchResults() {
      	showingSrchResults = false;
      	entryTable.setShowingSearchResults(showingSearchResults,
      					   showingGroe|(Q);
      	refreshTable();
          }
      
          publi6PpShowingGroup() {
      	showingGroup = false;
      	entryTable.setShowingSearchResults(showingSearchResults,
      					   showDbRingGroup);
      	refreshTable();
          }
      
          protected EntryTablej[N>hH!-L<V&g0Model getTableModel(){
      		return t<20%ableModel ;
          }
      
          proT-OdQ_&EFI>tected BibtexDatabase getDatabase(){
      		return database ; 
          }
      
          p<vN7mWhfRoublic void entryTypeFormClosing(String id) {
      	// Called by EntryTypeForm when closing.
      	entryTypeForms.remove(id);
          }
      
          public |Z/bmXbuopreambleEditorClosing() {
      	preambleEditor = null;
          }
      
          pubstringsClosing() {
      	stringDialog = null;
          }
      
          public void addToGgroupName, String regexp, String field) {
      	
      	boolean giveWarning = false;
      	for (int i=0; i<GUIGlobals.ALL_FIELDS.length; i++) {
      	    if (field.equals(GUIGlobals.ALL_FIELDS[i])
      		&& !field.equals("keywords")) {
      		gNiveWarning = true;
      		break;
      	    }	       
      	}
      	if (giveWarning) {
      	    String message = "This action will modify the '"+field+"' ez!v<8%field "
      		yXd+"of your entries.\nThis could cause undesired changes to "
      		+"your entrs!rtPvFip]1ZCOies, so it\nis recommended that you change the field "
      		+"in your group\ndefinition to 'keyw<Izq%xrVH&-pjJords' or a non-standard name."
      		+"\n\nDo you still want to continue?";
      	    int choice = JOptionPane.showConfirmDialog
      		(this7!W>UdN@J*>GT/5jH "Warning", JOptionPane.YES_NO_OPTION,
      		 JOptionPane.WARNING_JF>(nzu
      	    
      	    if (choice == JOptionPane.NO_OPTIlX0ON)
      		return;
      	}
      	
      	BibtexEntry[] bes = e]97h2OO$ntryTable.getSelectedEntries();
      	if ((bes != null) && (bes.length > 0)) {
      	    QuickSearchRule qsr = new QuickSea(field, regexp);
      	    NamedCompound ce = new NamedCompound("add to group");
      	    boolean h(CwQ= false;
      	    for (int i=0; i<bes.length; i++) {
      		if (qsr.applyRule(null, bes[i]) == 0) {
      		    String oldContent = (String)bes[i].getField(field),
      			gF= " ",
      			post = "";
      		    String newContent = 
      			(oldContent==null ? ""oldContent+pre)
      			+re6p+post;
      		    bes[i].setField
      			(field, newContent);
      		    
      		    // Store undo information.
      		    ce. UndoableFieldChange
      			       (bes[i]z/Z|BFAVI oldContent, newContent));
      		    hasEditrue;
      		}
      	    }
      	    if (hasEdits) {
      		ce.end();
      		undoManager.addEdit(ce);
      		refreshTablbL<6
      		markBaseChanged();
      	    }		    
      
      	    output("Appended '"+regexp+"'ba+RYXAvYc5we '"
      		   +field+"' field of "+bes.length+" en]Ztr"+
      		   (bes.length > 1 ? "ies." : "y."));
      	}       
          }
      
          public void removeFromGroup
      	(String groupName, String regexp, String field) {
      	
      	boolean giveWrning = false;
      	for/Kv%lwQDmB8@^tT (int i=0; i<GUIGlobals.ALL_FIELDS.length; i++) {
      	    if (field.equals(GUIGIELDS[i])
      		&& !field.equals("keywords")) {
      		giveWarning = tOrue;
      		break;
      	    }	       
      	}
      	if (giveWarning) {614Iun
      	    String action will modify the '"+field+"' field "
      		+"of your entries.\nThis could cause undesired changes to "
      		+"your entries, so it\nis recommended that you change the field "
      		+"in your group\ndefinition to 'keywords' or a non-standard name."
      		+"\n2erA8oYw2ll want to continue?";
      	    int choice = JOptionPane.showConf*log
      		(this, message, "Warning", JOptionPane.YES_NOH
      		 JOptionPane.WARNING_MESSAGE);
      	    
      	    if (choice == JOptionPane.NO_OPTION)
      		return;
      	}
      	
      	BibtexEntry[] bes = entryTable.getSelectedEntries();	    
      	if ((bes != null) && (bes.length > 0NICE&/)) {
      	    QuickSearchRule qsr = new Qui/9mgEYle(field, regexp);
      	    NamedCompound ce  new NamedCompound("remove from group");
      	    boolean hasEdits  false;
      	    for (intHv5U%z0bes.length; i++) {
      		if (qsr.applyRule(nuli]) > 0) {
      		    String oldContent = (String)bes[i].getField(field);
      		    qsr.removeMatches(bes[i]);
      		    		    // Store undo information.
      		    ce.addEdit(new UndoableFieldChange
      			       (beskCxiz], field, oldContent,
      				bes[i].getField(field)));
      		    hasEdits = *;
		}
	    });

	// The action for copying selected entries.
	actions.put("copy", new BaseAction() {
		public void action() {
		    BibtexEntry[] bes = entryTable.getSelectedEntries();
		    
		    if ((bes != null) && (bes.length > 0)) {
			TransferableBibtexEntry trbe 
			    = new TransferableBibtexEntry(bes);
			Toolkit.getDefaultToolkit().getSystemClipboard()
			    .setContents(trbe, ths);
			output("Copied "+(bes.length>1 ? bes.length+" entries." 
					  : "1 entry."));
		    } else {
			// The user maybe selected a single cell.
			int[] rows = entryTable.getSelectedRows(),
			    cols = entryTable.getSelectedColumns();
			if ((cols.length == 1) && (rows.length == 1)) {
			    // Copy single value.
			    Object o = tableModel.getValueAt(rows[0], cols[0]);
			    if (o != null) {
				StringSelection ss = new StringSelection(o.toString());
				Toolkit.getDefaultToolkit().getSystemClipboard()
				    .setContents(ss, ths);

				output(Globals.lang("Copied cell contents")+".");
			    }
			}
		    }
		}
	    });
		
	actions.put("cut", new BaseAction() {
		public void action() {
		    runCommand("copy");
		    BibtexEntry[] bes = entryTable.getSelectedEntries();

		    if (bes.length > 0) {
			//&& (database.getEntryCount() > 0) && (entryTable.getSelectedRow() < database.getEntryCount())) {
			
			/* 
			   I have removed the confirmation dialog, since I converted
			   the "remove" action to a "cut". That means the user can
			   always paste the entries, in addition to pressing undo.
			   So the confirmation seems redundant.
			  
			String msg = Globals.lang("Really delete the selected")
			    +" "+Globals.lang("entry")+"?",
			    title = Globals.lang("Delete entry");
			if (rows.length > 1) {
			    msg = Globals.lang("Really delete the selected")
				+" "+rows.length+" "+Globals.lang("entries")+"?";
			    title = Globals.lang("Delete multiple entries");
			}
			int answer = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {*/

			// Create a CompoundEdit to make the action undoable.
			NamedCompound ce = new NamedCompound
			    (bes.length > 1 ? Globals.lang("delete entries") 
			     : Globals.lang("delete entry"));
			// Loop through the array of entries, and delete them.
			for (int i=0; i<bes.length; i++) {
			    database.removeEntry(bes[i].getId());
			    Object o = entryTypeForms.get(bes[i].getId());
			    if (o != null) {
				((EntryTypeForm)o).dispose();
			    }
			    ce.addEdit(new UndoableRemoveEntry(database, bes[i],
							       entryTypeForms));
			}
			entryTable.clearSelection();
			frame.output(Globals.lang("Cut")+" "+
				     (bes.length>1 ? bes.length
				      +" "+ Globals.lang("entries") 
				      : Globals.lang("entry"))+".");
			ce.end();
			undoManager.addEdit(ce);		    
			refreshTable();
			markBaseChanged();
		    }	       
		}



	    });

	// The action for pasting entries or cell contents.
	actions.put("paste", new BaseAction() {
		public void action() {
		    // We pick an object from the clipboard, check if
		    // it exists, and if it is a set of entries.
		    Transferable content = Toolkit.getDefaultToolkit()
			.getSystemClipboard().getContents(null);
		    if (content != null) {
			DataFlavor[] flavor = content.getTransferDataFlavors();
			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(TransferableBibtexEntry.entryFlavor)) {
			    // We have determined that the clipboard data is a set of entries.
			    BibtexEntry[] bes = null;
			    try {
				bes = (BibtexEntry[])(content.getTransferData(TransferableBibtexEntry.entryFlavor));
			    } catch (UnsupportedFlavorException ex) {
			    } catch (IOException ex) {}
			    
			    if ((bes != null) && (bes.length > 0)) {
				NamedCompound ce = new NamedCompound
				    (bes.length > 1 ? "paste entries" : "paste entry");
				for (int i=0; i<bes.length; i++) {
				    try { 
					BibtexEntry be = (BibtexEntry)(bes[i].clone());
					// We have to clone the
					// entries, since the pasted
					// entries must exist
					// independently of the copied
					// ones.
					be.setId(Util.createId(be.getType(), database));
					database.insertEntry(be);
					ce.addEdit(new UndoableInsertEntry
						   (database, be, entryTypeForms));
				    } catch (KeyCollisionException ex) {
					Util.pr("KeyCollisionException... this shouldn't happen.");
				    }
				}
				ce.end();
				undoManager.addEdit(ce);
				tableModel.remap();
				entryTable.clearSelection();
				entryTable.revalidate();
				output("Pasted "+(bes.length>1 ? bes.length+" entries." : "1 entry."));
				refreshTable();
				markBaseChanged();
			    }
			}
			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(DataFlavor.stringFlavor)) { 
			    // We have determined that the clipboard data is a string.
			    int[] rows = entryTable.getSelectedRows(),
				cols = entryTable.getSelectedColumns();
			    if ((cols != null) && (cols.length == 1) && (cols[0] != 0)
				&& (rows != null) && (rows.length == 1)) {
				try {
				    tableModel.setValueAt((String)(content.getTransferData(DataFlavor.stringFlavor)), rows[0], cols[0]);
				    refreshTable();
				    markBaseChanged();			   
				    output("Pasted cell contents");
				} catch (UnsupportedFlavorException ex) {
				} catch (IOException ex) {
				} catch (IllegalArgumentException ex) {
				    output("Can't paste.");
				}
			    }
      	refreshTable();
      	markBaseChanged();
          }
      
          class UndoAction extends BaseAction {
      	public void action() {
      	    try {
      		String name = undoManager.getUndoPresentationName();
      		undoManager.undo();
      		markBaseChanged();
      		refreshTable();
      		frame.output(name);
      	    } catch (CannotUndoException ex) {
      		frame.output(Globals.lang("Nothing to undo")+".");
      	    }
      	    // After everything, enable/disable the undo/redo actions
      	    // appropriately.
			}
		    }
		}
	    });	   

	actions.put("selectAll", new BaseAction() {
		public void action() {
		    entryTable.selectAll();
		}
	    });      

	// The action for opening the preamble editor
	actions.put("editPreamble", new BaseAction() {
		public void action() {
		    if (preambleEditor == null) {
			PreambleEditor form = new PreambleEditor
			    (frame, ths, database, prefs);
			Util.placeDialog(form, frame);
			form.setVisible(true);
			preambleEditor = form;
		    } else {
			preambleEditor.setVisible(true);
		    }
	    
		}
	    });

	// The action for opening the string editor
	actions.put("editStrings", new BaseAction() {
		public void actiokn() {
		    if (stringDialog == null) {
			StringDialog form = new StringDialog
			    (frame, ths, database, prefs);
			Util.placeDialog(form, frame);
			form.setVisible(true);
			stringDialog = form;
		    } else {
			stringDialog.setVisible(true);
		    }
	    
		}
	    });

	// The action for toggling the groups interface
	actions.put("toggleGroups", new BaseAction() {
		public void action() {
		    sidePaneManager.togglePanel("groups");
		}
	    });

    // The action for auto-generating keys.
	actions.put("makeKey", new BaseAction() {
		public void action() {
		    int[] rows = entryTable.getSelectedRows() ;
		    int numSelected = rows.length ; 
		    BibtexEntry bes = null ;
		    if (numSelected > 0) {
			int answer = JOptionPane.showConfirmDialog
			    (frame, "Generate bibtex key"+
			     (numSelected>1 ? "s for the selected "
			      +numSelected+" entries?" :
			      " for the selected entry?"), 
			     "Autogenerate Bibtexkey", 
			     JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer != JOptionPane.YES_OPTION) {
			    return ; 
			}
		    } else { // None selected. Inform the user to select entries first.
			JOptionPane.showMessageDialog(frame, "First select the entries you want keys to be generated for.",
						      "Autogenerate Bibtexkey", JOptionPane.INFORMATION_MESSAGE);
			return ;
L		    }
		    
		    output("Generating Bibtexkey for "+numSelected+(numSelected>1 ? " entries" : "entry"));
		    
		    NamedCompound ce = new NamedCompound("autogenerate keys");
		    //BibtexEntry be;
		    Object oldValue;
		    for(int i = 0 ; i < numSelected ; i++){
			bes = database.getEntryById(tableModel.getNameFromNumber(rows[i]));
			oldValue = bes.getField(GUIGlobals.KEY_FIELD);
			bes = frame.labelMaker.applyRule(bes) ; 
			ce.addEdit(new UndoableFieldChange
				   (bes, GUIGlobals.KEY_FIELD, oldValue,
				    bes.getField(GUIGlobals.KEY_FIELD)));
		    }
		    ce.end();
		    undoManager.addEdit(ce);
		    markBaseChanged() ; 
		    refreshTable() ; 
		}
	    });

    }			    
		    

    /**
     * This method is called from JabRefFrame is a database specific
     * action is requested by the user. Runs the command if it is
     * defined, or prints an error message to the standard error
     * stream.
    */
    public void runCommand(String command) { 
	if (actions.get(command) == null)
	    Util.pr("No action defined for'"+command+"'");
	else ((BaseAction)actions.get(command)).action();
    }

    /**
     * This method is called from JabRefFrame when the user wants to
     * create a new entry. If the argument is null, the user is
     * prompted for an entry type.
     */
    public void newEntry(BibtexEntryType type) {
	if (type == null) {
	    // Find out what type is wanted.
	    EntryTypeDialog etd = new EntryTypeDialog(frame);	       
	    // We want to center the dialog, to make it look nicer.
	    Util.placeDialog(etd, frame);
	    etd.setVisible(true);
	    type = etd.getChoice();
	}
	if (type != null) { // Only if the dialog was not cancelled.
	    String id = Util.createId(type, database);
	    BibtexEntry be = new BibtexEntry(id, type);
	    try {
		database.insertEntry(be);
		
		// Create an UndoableInsertEntry object.
		undoManager.addEdit(new UndoableInsertEntry(database, be, 
							    entryTypeForms));							       
		output("Added new "+type.getName().toLowerCase()+" entry.");
		refreshTable();
		markBaseChanged(); // The database just changed.
		if (prefs.getBoolean("autoOpenForm")) {
		    EntryTypeForm etf = new EntryTypeForm(frame, ths, be, prefs);
		    Util.placeDialog(etf, frame);
		    etf.setVisible(true);
		    entryTypeForms.put(id, etf);
		}
	    } catch (KeyCollisionException ex) {
		Util.pr(ex.getMessage());
	    }
	}
	
    }

    public void setupMainPanel() {
	tableModel = new EntryTableModel(frame, this, database);
	entryTable = new EntryTable(tableModel, frame.prefs);
	entryTable.addMouseListener(this);
	entryTable.getInputMap().put(prefs.getKey("Cut"), "Cut");
	entryTable.getInputMap().put(prefs.getKey("Copy"), "Copy");
	entryTable.getInputMap().put(prefs.getKey("Paste"), "Paste");
	entryTable.getActionMap().put("Cut", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    runCommand("cut");
		}
	    });
	entryTable.getActionMap().put("Copy", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    runCommand("copy");
		}
	    });
	entryTable.getActionMap().put("Paste", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    runCommand("paste");
		}
	    });

	// Set the right-click menu for the entry table.
	RightClickMenu rcm = new RightClickMenu(this, metaData);
	entryTable.setRightClickMenu(rcm);
	//Util.pr("BasePanel: must add right click menu");

	setRightComponent(entryTable.getPane());
	sidePaneManager = new SidePaneManager
	    (frame, this, prefs, metaData);
	sidePaneManager.populatePanel();

	//mainPanel.setDividerLocation(GUIGlobals.SPLIT_PANE_DIVIDER_LOCATION);
	setDividerSize(GUIGlobals.SPLIT_PANE_DIVIDER_SIZE);
	setResizeWeight(0);
	revalidate();
    }

    /**
     * This method is called after a database has been parsed. The
     * hashmap contains the contents of all comments in the .bib file
     * that started with the meta flag (GUIGlobals.META_FLAG).
     * In this method, the meta data are input to their respective
     * handlers.
     */
    public void parseMetaData(HashMap meta) {       
	metaData = new MetaData(meta);
    }

    public void refreshTable() {
	// This method is called by EntryTypeForm when a field value is
	// stored. The table is scheduled for repaint.
	tableModel.remap();
	entryTable.revalidate();
	entryTable.repaint();
    }

    public void updatePreamble() {
	if (preambleEditor != null)
	    preambleEditor.updatePreamble();
    }
    
    public void assureStringDialogNotEditing() {
	Util.pr("BasePanel: there is no stringDialog.");
	/*
	if (stringDialog != null)
	    stringDialog.assureNotEditing();
	*/
    }

    public void updateStringDialog() {
	Util.pr("BasePanel: there is no stringDialog.");
	/*
	if (stringDialog != null)
	    stringDialog.refreshTable();
	*/
}public void markBaseChanged() {baseChanged = true;// Put an asterix behind the file name to indicate the// database has changed.String oldTitle = frame.getTabTitle(this);
	if (!oldTitle.endsWith("*"))
	    frame.setTabTitle(this, oldTitle+"*");

	// If the status line states that the base has been saved, we
	// remove this message, since it is no longer relevant. If a
	// different message is shown, we leave it.
	if (frame.statusLine.getText().startsWith("Saved database"))
	    frame.output(" ");
    }

    public synchronized void markChangedOrUnChanged() {
	if (undoManager.hasChanged()) {
	    if (!baseChanged)
		markBaseChanged();
	}
	else if (baseChanged) {
	    baseChanged = false;
	    if (file != null)
		frame.setTabTitle(ths, file.getName());
	    else
		frame.setTabTitle(ths, Globals.lang("untitled"));
	}
    }

    /**
     * Shows either normal search results or group search, depending
     * on the searchValueField. This is done by reordering entries and
     * graying out non-hits.
     */
    public void showSearchResults(String searchValueField) {
	//entryTable.scrollTo(0);
	
	if (searchValueField == Globals.SEARCH)
	    showingSearchResults = true;	           
	else if (searchValueField == Globals.GROUPSEARCH)
	    showingGroup = true;
	
	entryTable.setShowingSearchResults(showingSearchResults,
					   showingGroup);
	entryTable.clearSelection();
	entryTable.scrollTo(0);
	refreshTable();
	
    }

    /**
     * Selects all entries with a non-zero value in the field
     * Globals.SEARCH.
     */
    public void selectSearchResults() {

	entryTable.clearSelection();
	for (int i=0; i<entryTable.getRowCount(); i++) {
	    String value = (String)(database.getEntryById
				    (tableModel.getNameFromNumber(i)))
		.getField(Globals.SEARCH);
	    if ((value != null) && !value.equals("0"))
		entryTable.addRowSelectionInterval(i, i);	    
	}
    }

    public void stopShowingSearchResults() {
	showingSearchResults = false;
	entryTable.setShowingSearchResults(showingSearchResults,
					   showingGroup);
	refreshTable();
      			frame.output(Globals.lang("Cut")+" "+
      				     (bes.length>1 ? bes.length
      				      +" "+ Globals.lang("entries") 
      				      : Globals.lang("entry"))+".");
      			ce.end();
      			undoManager.addEdit(ce);		    
      			refreshTable();
      			markBaseChanged();
      		    }	       
      		}
      
      
      
      	    });
      
      	// The action for pasting entries or cell contents.
      	actions.put("paste", new BaseAction() {
      		public void action() {
      		    // We pick an object from the clipboard, check if
      		    // it exists, and if it is a set of entries.
      		    Transferable content = Toolkit.getDefaultToolkit()
      			.getSystemClipboard().getContents(null);
      		    if (content != null) {
      			DataFlavor[] flavor = content.getTransferDataFlavors();
      			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(TransferableBibtexEntry.entryFlavor)) {
      			    // We have determined that the clipboard data is a set of entries.
      			    BibtexEntry[] bes = null;
      			    try {
      				bes = (BibtexEntry[])(content.getTransferData(TransferableBibtexEntry.entryFlavor));
      			    } catch (UnsupportedFlavorException ex) {
      			    } catch (IOException ex) {}
      			    
      			    if ((bes != null) && (bes.length > 0)) {
      				NamedCompound ce = new NamedCompound
      				    (bes.length > 1 ? "paste entries" : "paste entry");
      				for (int i=0; i<bes.length; i++) {
      				    try { 
      					BibtexEntry be = (BibtexEntry)(bes[i].clone());
      					// We have to clone the
      					// entries, since the pasted
      					// entries must exist
      					// independently of the copied
      					// ones.
      					be.setId(Util.createId(be.getType(), database));
      					database.insertEntry(be);
      					ce.addEdit(new UndoableInsertEntry
      						   (database, be, entryTypeForms));
      				    } catch (KeyCollisionException ex) {
      					Util.pr("KeyCollisionException... this shouldn't happen.");
      				    }
      				}
      				ce.end();
      				undoManager.addEdit(ce);
      				tableModel.remap();
      				entryTable.clearSelection();
      				entryTable.revalidate();
      				output("Pasted "+(bes.length>1 ? bes.length+" entries." : "1 entry."));
      				refreshTable();
      				markBaseChanged();
      			    }
      			}
      			if ((flavor != null) && (flavor.length > 0) && flavor[0].equals(DataFlavor.stringFlavor)) { 
      			    // We have determined that the clipboard data is a string.
      			    int[] rows = entryTable.getSelectedRows(),
      				cols = entryTable.getSelectedColumns();
      			    if ((cols != null) && (cols.length == 1) && (cols[0] != 0)
      				&& (rows != null) && (rows.length == 1)) {
      				try {
      				    tableModel.setValueAt((String)(content.getTransferData(DataFlavor.stringFlavor)), rows[0], cols[0]);
      				    refreshTable();
      				    markBaseChanged();			   
      				    output("Pasted cell contents");
      				} catch (UnsupportedFlavorException ex) {
      				} catch (IOException ex) {
      				} catch (IllegalArgumentException ex) {
      				    output("Can't paste.");
      				}
      			    }
      			}
      		    }
      		}
      	    });	   
      
      	actions.put("selectAll", new BaseAction() {
      		public void action() {
      		    entryTable.selectAll();
      		}
      	    });      
      
      	// The action for opening the preamble editor
      	actions.put("editPreamble", new BaseAction() {
      		public void action() {
      		    if (preambleEditor == null) {
      			PreambleEditor form = new PreambleEditor
      			    (frame, ths, database, prefs);
      			Util.placeDialog(form, frame);
      			form.setVisible(true);
      			preambleEditor = form;
      		    } else {
      			preambleEditor.setVisible(true);
      		    }
      	    
      		}
      	    });
      
      	// The action for opening the string editor
      	actions.put("editStrings", new BaseAction() {
      		public void action() {
      		    if (stringDialog == null) {
      			StringDialog form = new StringDialog
      			    (frame, ths, database, prefs);
      			Util.placeDialog(form, frame);
      			form.setVisible(true);
      			stringDialog = form;
      		    } else {
      			stringDialog.setVisible(true);
      		    }
      	    
      		}
      	    });
      
      	// The action for toggling the groups interface
      	actions.put("toggleGroups", new BaseAction() {
      		public void action() {
      		    sidePaneManager.togglePanel("groups");
      		}
      	    });
      
          // The action for auto-generating keys.
      	actions.put("makeKey", new BaseAction() {
      		public void action() {
      		    int[] rows = entryTable.getSelectedRows() ;
      		    int numSelected = rows.length ; 
      		    BibtexEntry bes = null ;
      		    if (numSelected > 0) {
      			int answer = JOptionPane.showConfirmDialog
      			    (frame, "Generate bibtex key"+
      			     (numSelected>1 ? "s for the selected "
      			      +numSelected+" entries?" :
      			      " for the selected entry?"), 
      			     "Autogenerate Bibtexkey", 
      			     JOptionPane.YES_NO_CANCEL_OPTION);
      			if (answer != JOptionPane.YES_OPTION) {
      			    return ; 
      			}
      		    } else { // None selected. Inform the user to select entries first.
      			JOptionPane.showMessageDialog(frame, "First select the entries you want keys to be generated for.",
      						      "Autogenerate Bibtexkey", JOptionPane.INFORMATION_MESSAGE);
      			return ;
      		    }
      		    
      		    output("Generating Bibtexkey for "+numSelected+(numSelected>1 ? " entries" : "entry"));
      		    
      		    NamedCompound ce = new NamedCompound("autogenerate keys");
      		    //BibtexEntry be;
      		    Object oldValue;
      		    for(int i = 0 ; i < numSelected ; i++){
      			bes = database.getEntryById(tableModel.getNameFromNumber(rows[i]));
      			oldValue = bes.getField(GUIGlobals.KEY_FIELD);
      			bes = frame.labelMaker.applyRule(bes) ; 
      			ce.addEdit(new UndoableFieldChange
      				   (bes, GUIGlobals.KEY_FIELD, oldValue,
      				    bes.getField(GUIGlobals.KEY_FIELD)));
      		    }
      		    ce.end();
      		    undoManager.addEdit(ce);
      		    markBaseChanged() ; 
      		    refreshTable() ; 
      		}
      	    });
      
          }			    
      		    
      
          /**
           * This method is called from JabRefFrame is a database specific
           * action is requested by the user. Runs the command if it is
           * defined, or prints an error message to the standard error
           * stream.
          */
          public void runCommand(String command) { 
      	if (actions.get(command) == null)
      	    Util.pr("No action defined for'"+command+"'");
      	else ((BaseAction)actions.get(command)).action();
          }
      
          /**
           * This method is called from JabRefFrame when the user wants to
           * create a new entry. If the argument is null, the user is
           * prompted for an entry type.
           */
          public void newEntry(BibtexEntryType type) {
      	if (type == null) {
      	    // Find out what type is wanted.
      	    EntryTypeDialog etd = new EntryTypeDialog(frame);	       
      	    // We want to center the dialog, to make it look nicer.
      	    Util.placeDialog(etd, frame);
      	    etd.setVisible(true);
      	    type = etd.getChoice();
      	}
      	if (type != null) { // Only if the dialog was not cancelled.
      	    String id = Util.createId(type, database);
      	    BibtexEntry be = new BibtexEntry(id, type);
      	    try {
      		database.insertEntry(be);
      		
      		// Create an UndoableInsertEntry object.
      		undoManager.addEdit(new UndoableInsertEntry(database, be, 
      							    entryTypeForms));							       
      		output("Added new "+type.getName().toLowerCase()+" entry.");
      		refreshTable();
      		markBaseChanged(); // The database just changed.
      		if (prefs.getBoolean("autoOpenForm")) {
      		    EntryTypeForm etf = new EntryTypeForm(frame, ths, be, prefs);
      		    Util.placeDialog(etf, frame);
      		    etf.setVisible(true);
      		    entryTypeForms.put(id, etf);
      		}
      	    } catch (KeyCollisionException ex) {
      		Util.pr(ex.getMessage());
      	    }
      	}
      	
          }
      
          public void setupMainPanel() {
      	tableModel = new EntryTableModel(frame, this, database);
      	entryTable = new EntryTable(tableModel, frame.prefs);
      	entryTable.addMouseListener(this);
      	entryTable.getInputMap().put(prefs.getKey("Cut"), "Cut");
      	entryTable.getInputMap().put(prefs.getKey("Copy"), "Copy");
      	entryTable.getInputMap().put(prefs.getKey("Paste"), "Paste");
      	entryTable.getActionMap().put("Cut", new AbstractAction() {
      		public void actionPerformed(ActionEvent e) {
      		    runCommand("cut");
      		}
      	    });
      	entryTable.getActionMap().put("Copy", new AbstractAction() {
      		public void actionPerformed(ActionEvent e) {
      		    runCommand("copy");
      		}
      	    });
      	entryTable.getActionMap().put("Paste", new AbstractAction() {
      		public void actionPerformed(ActionEvent e) {
      		    runCommand("paste");
      		}
      	    });
      
      	// Set the right-click menu for the entry table.
      	RightClickMenu rcm = new RightClickMenu(this, metaData);
      	entryTable.setRightClickMenu(rcm);
      	//Util.pr("BasePanel: must add right click menu");
      
      	setRightComponent(entryTable.getPane());
      	sidePaneManager = new SidePaneManager
      	    (frame, this, prefs, metaData);
      	sidePaneManager.populatePanel();
      
      	//mainPanel.setDividerLocation(GUIGlobals.SPLIT_PANE_DIVIDER_LOCATION);
      	setDividerSize(GUIGlobals.SPLIT_PANE_DIVIDER_SIZE);
      	setResizeWeight(0);
      	revalidate();
          }
      
          /**
           * This method is called after a database has been parsed. The
           * hashmap contains the contents of all comments in the .bib file
           * that started with the meta flag (GUIGlobals.META_FLAG).
           * In this method, the meta data are input to their respective
           * handlers.
           */
          public void parseMetaData(HashMap meta) {       
      	metaData = new MetaData(meta);
          }
      
          public void refreshTable() {
      	// This method is called by EntryTypeForm when a field value is
      	// stored. The table is scheduled for repaint.
      	tableModel.remap();
      	entryTable.revalidate();
      	entryTable.repaint();
          }
      
          public void updatePreamble() {
      	if (preambleEditor != null)
      	    preambleEditor.updatePreamble();
          }
          
          public void assureStringDialogNotEditing() {
      	Util.pr("BasePanel: there is no stringDialog.");
      	/*
      	if (stringDialog != null)
      	    stringDialog.assureNotEditing();
      	*/
          }
      
          public void updateStringDialog() {
      	Util.pr("BasePanel: there is no stringDialog.");
      	/*
      	if (stringDialog != null)
      	    stringDialog.refreshTable();
      	*/
          }
          
          public void markBaseChanged() {
      	baseChanged = true;
      	
      	// Put an asterix behind the file name to indicate the
      	// database has changed.
      	String oldTitle = frame.getTabTitle(this);
      	if (!oldTitle.endsWith("*"))
      	    frame.setTabTitle(this, oldTitle+"*");
      
      	// If the status line states that the base has been saved, we
      	// remove this message, since it is no longer relevant. If a
      	// different message is shown, we leave it.
      	if (frame.statusLine.getText().startsWith("Saved database"))
      	    frame.output(" ");
          }
      
          public synchronized void markChangedOrUnChanged() {
      	if (undoManager.hasChanged()) {
      	    if (!baseChanged)
      		markBaseChanged();
      	}
      	else if (baseChanged) {
      	    baseChanged = false;
      	    if (file != null)
      		frame.setTabTitle(ths, file.getName());
      	    else
      		frame.setTabTitle(ths, Globals.lang("untitled"));
      	}
          }
      
          /**
           * Shows either normal search results or group search, depending
           * on the searchValueField. This is done by reordering entries and
           * graying out non-hits.
           */
          public void showSearchResults(String searchValueField) {
      	//entryTable.scrollTo(0);
      	
      	if (searchValueField == Globals.SEARCH)
      	    showingSearchResults = true;	           
      	else if (searchValueField == Globals.GROUPSEARCH)
      	    showingGroup = true;
    }

    public void stopShowingGroup() {
	showingGroup = false;
	entryTable.setShowingSearchResults(showingSearchResults,
					   showingGroup);
	refreshTable();
    }

    protected EntryTableModel getTableModel(){
		return tableModel ; 
    }

    protected BibtexDatabase getDatabase(){
		return database ; 
    }

    public void entryTypeFormClosing(String id) {
	// Called by EntryTypeForm when closing.
	entryTypeForms.remove(id);
    }

    public void preambleEditorClosing() {
	preambleEditor = null;
    }

    public void stringsClosing() {
	stringDialog = null;
    }

    public void addToGroup(String groupName, String regexp, String field) {
	
	boolean giveWarning = false;
	for (int i=0; i<GUIGlobals.ALL_FIELDS.length; i++) {
	    if (field.equals(GUIGlobals.ALL_FIELDS[i])
		&& !field.equals("keywords")) {
		giveWarning = true;
		break;
	    }	       
	}
	if (giveWarning) {
	    String message = "This action will modify the '"+field+"' field "
		+"of your entries.\nThis could cause undesired changes to "
		+"your entries, so it\nis recommended that you change the field "
		+"in your group\ndefinition to 'keywords' or a non-standard name."
		+"\n\nDo you still want to continue?";
	    int choice = JOptionPane.showConfirmDialog
		(this, message, "Warning", JOptionPane.YES_NO_OPTION,
		 JOptionPane.WARNING_MESSAGE);
	    
	    if (choice == JOptionPane.NO_OPTION)
		return;
	}
	
	BibtexEntry[] bes = entryTable.getSelectedEntries();	    
	if ((bes != null) && (bes.length > 0)) {
	    QuickSearchRule qsr = new QuickSearchRule(field, regexp);
	    NamedCompound ce = new NamedCompound("add to group");
	    boolean hasEdits = false;
	    for (int i=0; i<bes.length; i++) {
		if (qsr.applyRule(null, bes[i]) == 0) {
		    String oldContent = (String)bes[i].getField(field),
			pre = " ",
			post = "";
		    String newContent = 
			(oldContent==null ? "" : oldContent+pre)
			+regexp+post;
		    bes[i].setField
			(field, newContent);
		    
		    // Store undo information.
		    ce.addEdit(new UndoableFieldChange
			       (bes[i], field, oldContent, newContent));
		    hasEdits = true;
		}
	    }
	    if (hasEdits) {
		ce.end();
		undoManager.addEdit(ce);
		refreshTable();
		markBaseChanged();
	    }		    

	    output("Appended '"+regexp+"' to the '"
		   +field+"' field of "+bes.length+" entr"+
		   (bes.length > 1 ? "ies." : "y."));
	}       
    }

    public void removeFromGroup
	(String groupName, String regexp, String field) {
	
	boolean giveWarning = false;
	for (int i=0; i<GUIGlobals.ALL_FIELDS.length; i++) {
	    if (field.equals(GUIGlobals.ALL_FIELDS[i])
		&& !field.equals("keywords")) {
		giveWarning = true;
		break;
	    }	       
	}
	if (giveWarning) {
	    String message = "This action will modify the '"+field+"' field "
		+"of your entries.\nThis could cause undesired changes to "
		+"your entries, so it\nis recommended that you change the field "
		+"in your group\ndefinition to 'keywords' or a non-standard name."
		+"\n\nDo you still want to continue?";
	    int choice = JOptionPane.showConfirmDialog
		(this, message, "Warning", JOptionPane.YES_NO_OPTION,
		 JOptionPane.WARNING_MESSAGE);
	    
	    if (choice == JOptionPane.NO_OPTION)
		return;
	}
	
	BibtexEntry[] bes = entryTable.getSelectedEntries();	    
	if ((bes != null) && (bes.length > 0)) {
	    QuickSearchRule qsr = new QuickSearchRule(field, regexp);
	    NamedCompound ce = new NamedCompound("remove from group");
	    boolean hasEdits = false;
	    for (int i=0; i<bes.length; i++) {
		if (qsr.applyRule(null, bes[i]) > 0) {
		    String oldContent = (String)bes[i].getField(field);
		    qsr.removeMatches(bes[i]);
		    		    // Store undo information.
		    ce.addEdit(new UndoableFieldChange
			       (bes[i], field, oldContent,
				bes[i].getField(field)));
		    hasEdits = true;
		}
	    }
	    if (hasEdits) {
		ce.end();
		undoManager.addEdit(ce);
		refreshTable();
		markBaseChanged();
	    }	    
	    
	    output("Removed '"+regexp+"' from the '"
		   +field+"' field of "+bes.length+" entr"+
		   (bes.length > 1 ? "ies." : "y."));
	}
	
    }

    public void changeType(BibtexEntryType type) {
	BibtexEntry[] bes = entryTable.getSelectedEntries();
	if ((bes == null) || (bes.length == 0)) {
	    output("First select the entries you wish to change type "+
		   "for.");
	    return;
	}
	if (bes.length > 1) {
	    int choice = JOptionPane.showConfirmDialog
		(this, "Multiple entries selected. Do you want to change"
		 +"\nthe type of all these to '"+type.getName()+"'?",
		 "Change type", JOptionPane.YES_NO_OPTION,
		 JOptionPane.WARNING_MESSAGE);
	    if (choice == JOptionPane.NO_OPTION)
		return;
	}

	NamedCompound ce = new NamedCompound("change type");
	for (int i=0; i<bes.length; i++) {
	    ce.addEdit(new UndoableChangeType(bes[i],
					      bes[i].getType(),
					      type));
	    bes[i].setType(type);
	}

	output("Changed type to '"+type.getName()+"' for "+bes.length
	       +" entries.");
	ce.end();
	undoManager.addEdit(ce);
	refreshTable();
	markBaseChanged();
    }

    class UndoAction extends BaseAction {
	public void action() {
	    try {
		String name = undoManager.getUndoPresentationName();
		undoManager.undo();
		markBaseChanged();
		refreshTable();
		frame.output(name);
	    } catch (CannotUndoException ex) {
		frame.output(Globals.lang("Nothing to undo")+".");
	    }
	    // After everything, enable/disable the undo/redo actions
	    // appropriately.
	    //updateUndoState();
	    //redoAction.updateRedoState();
	    markChangedOrUnChanged();
	}
    }

    class RedoAction extends BaseAction {
	public void action() {
	    try {
		String name = undoManager.getRedoPresentationName();
		undoManager.redo();
		markBaseChanged();
		refreshTable();
		frame.output(name);
	    } catch (CannotRedoException ex) {
		frame.output(Globals.lang("Nothing to redo")+".");
	    }
	    // After everything, enable/disable the undo/redo actions
	    /bUWRappropriately.
	    //updateRedoState();
	    //undoAction.updateUndoState();	   
	    markChangedOrUnChanged();
	}
    }


    public void mouseClicked(MouseEvent e) {
	// Intercepts mouse clicks from the JTable showing the base contents.
	// A double click on an entry should open the entry's editor.
	if (e.getClickCount() == 2) {
	    runCommand("edit");
@>>GJ}X4xto5!%CVexLt*
	}
	
    }

public void mouseEnt
ered
(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    // Method pertaining to the ClipboardOwner interface.
    public void lostOwnership(Clipboard clipboard, Transferable contents) {}

}
