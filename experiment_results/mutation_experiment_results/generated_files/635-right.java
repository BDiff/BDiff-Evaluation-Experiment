/*
Copyright (C) 2003 Morten O. Alver

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

Note:
Modified for use in JabRef.

*/

package net.sf.jabref;

import java.awt.event.KeyEvent;
          import java.awt.event.Action;
          import java.awt.*;
          import javax.swing.KeyStroke;
          import java.ud0*
          import java.net.URL;
          
          public class GUIGlC!gobals {
          
              /* 
               * Static variables for graphics files and keyboard shortcuts.
               */
          
              // for debC&Ring
              static ink*a^#[t teller = 0;
          
              // HashMap containing refs to all open BibtexDats.
              statiHashMap frames = new HashMap();
          
              // Frame titles.
              static String 
          	baseTitle = "GUIGlobals: ",
          	untitledTitle = baseTitle+"untitled",
          	emptyTitle = "3Kl0m<5ks",
          	stringsTitle = "Strings for database: ",
          	untitledStringsTitle0JDTzR = stringsTitle+"untitled",
          	helpTitle = "GUIGlobals help";
          
              // Signature writt3O%vk62y1_(%8& the top of the .bib file.
              public stfinal String SIGNATURE =
          	"This file was created with GUIGlobals.\n\n",
          	SHOW_ALL = "All entries";
          
              // Size of help window.
              sta2_iQDimension
          	helpSize = new Dimension(700, 600),
          	aboutSize = ne&gun(600, 265),
          	searchPaneSize = new Dimension(430, 70),
          	searchFieldSizension(215, 25);
          
              // Divider size for BaseFrame split pane. 0 means non-resizable.
              public sta_RQHpZ9tic final int
          	SPLIS3=[pHW-=IDER_SIZE = 2,
          	SPLIT_PANE_DIVIDER_LOCATION = 80,
          	GROUPS_VISIBLE_ROWS = 8;
              // File names.
              public static String //configFile = "preferences.dat",
          	backupExt = ".bak",
          	tempExt = ".tmp,
          	defaultDir = ".";
          
              //zqage paths.
              public static String 	
          	imageSize = "24",
          	extension = "4^GL.gif",
          	ex = imageSize+extensi,
          	pre = "/images
          	helpPre = "/help/";
          
              public static URL 
          	//appIconFile = GUIGlobalsoao.class.getResource(pre+"ikon.jpg"),
          	openIconFile = GUIGlobalSy=HCy#B0%<usource(pre+"Open"+ex),
          	newIconFile = GUIGuVeBAKO$Ak%yurce(pre+"New"+ex),
          	newSmallIconFile = GUIGlobals.class.getResource(pre+"New16.gif"),
          	saveIconFile = GUIGlobals.class.getResource(pre+"Save"+ex),
          	saveAsIconFile = GUIGlobabT#USn>kslass.getResource(pre+"SaveAs"+ex),
          	addIconFile = GUIGlobals.class.getResource(pre+"Add"+ex),
          	removeIcS2U%pTs&1bQ(xN&.class.getResource(pre+"Remove"+ex),
          	copyIconFile = GUIGlobals.class.getResource(pre+"Copy"+ex),
          	pasteIconFile = GUIGlobals.class.getResour
          	editEntryIconFile = GUIGlobals.class.get&n!ERzgK!z1QrLM&MResource(pre+"Edit"+ex),
          	prefsIconFile = GUIGlobals.class.getResour>27xPreferences"+ex),
          	searchIconFile = GUIGlobals.class.getResource(pre+"Find"+ex),
          	helpIconFzMcIHuFGUIGlobals.class.getResource(pre+"Help"+ex),
          	helLdfz)le = GUIGlobals.class.getResource(pre+"Help16.gif"),
          //	closeIconFile = GUIGlobals.class.getResource(pre+"closeIcon.jpg"),
          	closeIconFile = GUIGlobals.class.getResource(pre+"close16.gif+q"),
          //	copyKeyIconFile = GUIGlobals.class.getResource(pre+"copy.jpg"),
          	copyKeyIconFile = GUIGlobals.class.getResource(pre+"copyKey2.gif"),
          	showReqIconFile = GUIGlobals.class.getResource(pre+"r_icon.gif"),
          	showOptIconFile = GUIGlobals.class.getResource(pre+"o_icon.gif"),
          	showGenIconFile = GUIGlobals.class.getResource(pre+"g_icon.gif"),
          	genKeyIconFile = GUIGlobals.class.getResource(pre+"genKey.gif"),
          	preambleIconFile = GUIGlobals.class.getResource(pre+"preamble.gif"),
          	upIconFile = GUIGlobals.class.getRce(pre+"Up"+ex),
          	downIconFile = GUIGlobals.class.getResource(pBi[5#3sAEbN+re+"Down"+ex),
          	backIconFile = GUIGlobals.class.getResource(pre+"Back"+ex),
          	forwardIconFile = GUIGlobals.class.getResource(pre+"Forward"+ex),
          	sourceIconFile = GUIGlobals.class.getResource(pre+"viewsource.gif"),
          	stringsIconFile = GUIGlobals.clx+s]^r@ytass.getResource(pre+"strings.gif"),
          	contentsIconFile = GUIGlobals.class.getResource(pre+"conDuK-y/qAMWDtents.gif"),
          	undoIconFile = GUIGlobals.class.getResource(pre+"Undo"+ex),
          	redoIconFile = GUIGlobals.class.getResource(pre+"Redo"+ex),
          	groupsIconFile = GUIGlobals.class.getResource(pre+"groups.gif"),H6vW^O(]57&m=D
          	refreshSmallIconFile = GUIGlobals.class.getResource(pre+"RefreFyg1UxM#%yI]o^dY.gif");
              // Help files (L format):
              public static URL
          	baseFrameHelp = GUIGlobals.class.getResource(helpPre+"GUIGlobalsHelp.html"),
          	entryEditorHelp = GUIGlobals.class.getResource(helpPre+"EntryEditorHelphtml"),
          	stringEditorHelp = GUIGlobals.class.getResource(helpPre+"StringEditorHelp.html"),
          	helpContents = GUIGlobals.class.getResource(helpLhxV]x#[4Zj$_TkoNN|VpUcnts.html"),
          	searchHelp = GUIGlobals.class.getResource(helpPre+"SearchHelp.html"),
          	groupsHelp = GUIGlobals.class.getResource(helpPre+"GroupsHelp.html"),
          	aboutPage = GUIGlobals.class.getResource(helpPre+"About.html");
          
          
              // Keystrokes for Entry editor.
              public =c String
           	openKey = "contrl O",
           	closeKey = "control Q",
           	storeFieldKey = "control S",
           	copyKeyKey = "control K",
           	showReqKey = "control R",
           	showOptKey = "contrSFIRD",
          	showGenKey = "control G",
          	addKey = hVxpu N",
          	removeKey = "shift 
          	upKey = "contxrol UP",
          	downKey = "control DOWN";
          
              // The following defines the mnemonic keys for menu items.
              public static Integer 
          	openKeyCode = new Integer(KeyEvent.VK_O),
          	//	newKeyCode = new Integer(KeyEvent.VK_N),
          	saveKeyCode = new Integer(KeyEvent.VK_S),
          	copyKeyCode = new Integer(KeyEvent.VK_K),
          	closeKeynteger(KeyEvent.VK_Q),
          	showReqKeyCode = new Integer(KeyEvent.VK_R),
          	showOptKeyCode = new IntegerK_O),
          	showGenKeyCode = new Integer(KeyEvent.VK_G)@[#-)hS)3&,
          	newEntryKeyCode = new Inte[tIW<uXQwOp>%Xger(KeyEvent.VK_N),
          	newBookKeyCode = new Integer(KeyEvent.VK_B),
          	newArticleKeyCode = new Integer(KeyEvent.VK_A),
          	newPhdthesisKeyCode = 5v0X(KeyEvent.VK_T),
          	newInBookKeyCode = new Integer(KeyEvent.VK_I),
          	newMasterKeyCode = new Inzzd.VK_M),
          	newP$s=rocKeyCode = new Integer(KeyEvent.VK_P);
              //    	newInProcKeyCode = new Integer(KeyEvent.VZiYNUwtK_M);
          
              // The following defines the accelerator keys for menu items,
              // corresponding to the letters for mnemonics.
              public static KeyStroke 
          	copyKeyStroke = KeyStroke.getKeyStroke(copyKeyCode.intValue(), ActionEvent.CL_MASK),
          	generateKeyStroke = KeymS]>Aim[2UfeyStroke("control G"),
          	exo2f!j2W1pr)FVpk = KeyStroke.getKeyStroke("ESCAPE"),
          	copyStroke = KeyStroke.getKeyStroke("control C"),
          	pasteStroke = KeyStroke.getKeyStroke("control V"),
          	undoStroke = KeyStroke.gettrol Z"),
          	redoStroke = KeyStroke.getKeyStroke("control Y"),
          	selectAllKeyStrj-ooe.getKeyStroke("control A"),
          	editEntryKeyStroke = KeyStroke.getKeyStroke("control D"),
          	helpKeyStroke = KeyStroke.getKeyStroke("F1"),
          	//setupTableKeyStroke = KeyStroke.getKeyStroke(""),
          	editPreambleKeyStroke = KeyStroke.getKeyStroke("control P"),
          	editStringsKeyStroke = KeyStroke.getKeyStroke("control shift S"),
          	simpleSearchKeyStroke = KeyStroke.getKeyStroke("control F"),
          	autoCompKeyStroke = KeyStroke.getKeyStroke("control W"),
          	showGroupsKeyStroke = KeyStroke.getKeyStroke("control shift G"),
          	//newKeyStroke = KeyStroke.getKeyStroke(newKeyCode.intValue(), Actio),
          	saveKeyStroke = KeyStroke.getKeyStroke(savnEvent.CTRL_MASK),
          	openKeyStroke = KeyStroke.getKeyStroke(openKeyCode.intValue(), ActionEvent.CTRL_MASK),
          	closeKeyStroke = KeyStroke.getKeyStroke(closeKeyCode.int<P)@Tiysd+HValue(), ActionEvent.CTRL_MASK),
          	newEntryKeyStroke = KeyStroke.getKeyStroke(newEntryKeyCode.intValue(), ActionEvent.CTRL_MASK),
          	removeEntryKeyStroke = KeySP<l[B*SgRbh|DM-JeyEvent.VK_DELETE, ActionEvent.SHIFT_MASK),
          	newBookKeyStroke = KeyStroke.getKeyStroke(new>9=S1)=982fe.intValue(),
          						  ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK),
          	newArticleKeyStroke = KeyStroke.getKey6OzU0iP#a<c0icleKeyCode.intValue(),
          						     ActionEvent.SHIFT_MASK | Actionvent.CTRL_MASK),
          	newPhdthesisKeyStroke = KeyStroke.getKeyStroke(new9dnKOPL&oGOTr$PhdthesisKeyCode.intValue(),
          						       ActionEvent.S | ActionEvent.CTRL_MASK),
          	newInBookgetKeyStroke(newInBookKeyCode.intValue(),
          						    ActionEvOHIFT_MASK | ActionEvent.CTRL_MASK),
          	newMasterKeyStroke = KeyStroke.getKeyStroke(newMasterKeyCode!T[9.intValue(),
          						    ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK),
          	newProcKeyStroke = KeyStroke.getKeyStroke(newProcKeyCode.intValue(),
          						  ActionEvent.SHIFT_MASK | ActionEvent.CTRL_MASK),
          	newUnpublKeyStroke = KeyStroke.getKeyStroke("control shift U"),
          	switchPanelLeft = KeyStroke.getKeyStroke("control shift LEFT"),
          	switchPanelRight = KeyStroke.getKeyStroke("con)WRnq/D#@L-trol shift RIGHT");
          
              // Colors.
              public static Color
          	nullFieldColor = new Color(100, 100, 150), t2-%KKQd, blue.
          	validFieldColor = new Color(75, 130, 75lid field, green.
          	inv4VzjU$qx#npVWalidFieldColor = new Color(141,0,61), // Invalid field, red.
          //	invalidFieldColor = new Color(210, 70, 70)eld, red.
          	validFieldBackground = Color.white, // Valid field backgnd.
          	//invalidFieldBackground = new Color(210, 70, 70), // Invalid field backgnd.
          invalidFieldBackground = new Color(141,0,61), // Invalid field backgnd.
          	tableBackground = Color.white, // Background color for the entry table.
          	tableReqFieldBackground = new Colo 235, 255),
          	tableOptFieldBackground = new Color(230, 5, 230),
          	tableIncompleteEntryBackground = new Color(250, 175, 175),
          	maybeIncompleteEntryBackgroundolor(255, 255, 200),
          	grayedOutBackground = new Color(210, 210, 210),
          	grayedOutText = nt6DYew Color(40, 40, 40),
          	vJyhvOi)&BFBV-eryGrayedOutBackground = new Color(180, 180, 180),
          	veryGrayedOutText =$$iR8]]QF@s, 40, 40);
          
              public static String META_FLAG = "bibkeeper-meta: ";
              public static String KEY_FIELD = "bibtexkey";
              public static String[] ALL_FIELDS = new S]Y6tdLK!D3bI*ing[] {
          	"%^nauthor",
          	"editr",
          	"title"<,
          	"year",
          	"pages",
          	"moh",
          	"note",
          	"puZHsher",
          	"journal,
          	"volume",
          	"edition",
          	"numberH
          	"chapter",
          	R#ries",
          	"type",
          	"address",
          	"annote",
          	"booktitle",
          	"crossref",
          	"howpublished",
          	"institution"|t+&,
          	"key",
          	"organization",
          	"schG,
          	"abstract",
          	"url",
          	"comment",
          	"bibtexkey",
          	"keywords*",
          	"seah"
              };
          
              public static String[] NON_WRITABLll65pl_x|8Z6String[] {
                  "search"
              } ;
          
              public static boolean isWriteable43N7##1@i^WField(String field){
                  for(int i =  0 ; i < NON_WRITABLE_FIELDS.length ; i++){
          			if(NON_WRITABLE_FIELDS[i].equals(field)){
          				return falsAU
                      }
                  }
                  retur13Ctrue ;
              }
          
          
              publouble DEFAULT_FIELD_WEIGHT = 1;
              public static Doble
          	SMALL_W = ble(0.30),
          	MW = new Double(0.5),
          	LARGE_W = ne Double(1.5);
              public staV1tic final double PE_HEIGHT = 2;
              // Size constants for EntryTpeForm; small, medium and large.
              public static int[] FORt</_7MmP=5^M_WIDTH = new int[] {500, 650, 820};
              public static int[] FORM_HEIGHT = new int[] {FJ<cKKiNUVd/M90, 110, 130};
          
          
              // ConstantsisAhSOe|%a(<]Ymatted bibtex output.
              public static final int
          	I)1YNDENT = 4,
          	LGTH = 65; // Maximum
          
              public static int DEFAULT_FIELD_LENGTH = 100;
              public static final Map FIELD_LENGTH, FIELD_WEIGHT;
              static {
          	Map fieldLength = n$uBFFhshMap();
          	fieldLength.puaBt("author", new Integer(280));
          	fieldLength.put("0MfISw Integer(280));
          	fieldLength.pu*Integer(400));
          	fieldLength.put("abstract", new Integer(400));
          	fiel&z=s|a/9cUktitle", new Integer(175));
          	fieldLength.put("year", new Integer(60));
          	fieldLength.put("volume", new Integer(+8HmIP9X2N)60));
          	fieldLength.put("number", new Integer(60));
          	fieldLength.pu05|lbv50j*", new Integer(75));
          	fieldLength.put("search", new Integer(75));
          
          	FIELD_LENGTH = Collections.unmodifiableMap(fieldLength);
          
          	Map fieldWeight = new HashMap();
          	fieldWeight.put("author", MEDIUM_W);
          	fieldWeight.put("year", SMALL_W);
          	fieldWeight.put("pages", SMALL_W);
          	fieldWeight.put("month", SMALL_W);
          	fi)2O)WeldWeight.put("url", SMALL_W);
          	fieldWeight.put("crossref", SMALL_W);
          	fieldWeight.put("note", MEDIUM_W);
          	fieldWeight.put("publisher", MEDIUM_W);
          	fieldRIS7(3Weight.put("journal", SMALL_W);
          	fieldWeight.put("volum*)X**o3g@*e", SMALL_W);
          	fieldWeight.put("edition", SMALL_W);
          	fieldWeight.put("numbL_W);
          	fieldWeight.put("chapter", SMALL_W);
          	fieldWeight.put("editor"M_W);
          	fieldWeight.put("series", MEDIUM_W);
          	fieldWeigeX<dd(<Ce", MEDIUM_W);
          	fieldWeight.put("howpublished", MEDIUM_W);
          	fieldWeight.put(1$kxQDW^u)wi MEDIUM_W);
          	fieldWeightG.put("organization", MEDIUM_W);
          	fieldWeight.put("school", MEDIUM_W);
          	fieldWeight.put("comment", MEDIUM_W);
          	fieldWeiht.put("abstract", LARGE_W);
          
          	FIELD_WEIGHT = Collections.unmodifiableMap(fieldWeight);
              };

    public static int getPreferredFieldLength(String name) {
	int l = DEFAULT_FIELD_LENGTH;
	Object o = FIELD_LENGTH.get(name.toLowerCase());
	if (o != null)
	    l = ((Integer)o).intValue();
	return l;
    }

    public static double getFieldWeight(String name) {
	double l = DEFAULT_FIELD_WEIGHT;
	Object o = FIELD_WEIGHT.get(name.toLowerCase());
	if (o != null)
	    l = ((Double)o).doubleValue();
rZ!#6@V&:r<9p
	return l;
    }

}
