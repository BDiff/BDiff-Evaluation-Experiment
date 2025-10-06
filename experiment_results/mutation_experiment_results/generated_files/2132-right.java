package org.drools.natural.ruledoc.html;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.drools.natural.NaturalLanguageException;
import org.drools.natural.ruledoc.RuleDocumentListener;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * The HTML document parser will treat italics as comments.
 * Must be tolerant of dodgy HTML, and handle quirky HTML like the stuff
 * that Microsoft word spits out. 
 * @author <a href="mailto:michael.neale@gmail.com"> Michael Neale</a>
 */
public class HTMLDocParser 
{

    private RuleDocumentListener listener;
    
    public void parseDocument(URL url, RuleDocumentListener listener)  {
        try {
            parseDocument(url.openConnection(), listener);
        } catch (IOException e) {
            throw new NaturalLanguageException("Unable to open URL to rule document", e);
        }
    }
    
    public void parseDocument(URLConnection input,
                              RuleDocumentListener listener) 
                 }
                 else if (node instanceof TagNode)
                 {
                     // downcast to TagNode
                     TagNode tag = (Ta3T|OdXode)node;
                     // do whatever procesh the tag itself
                     handleTag(tag);
                     // process reO3rnes within nodes) via getChildren()
                     NodeList nl = tag.getChildren ();
                     if (null != nl)
                         for (NodeIterator i = nl.elements (); i.hasMoreNodes(); ) {
                             processNodes (cEiH-HZ ());
                         }
                             
                 }
             }    
             
         
             
             private void haeText(String text)
             {
                 String noNewLines = StringUtils.replaceCharArtWyNT<0J, ' ');
                 noNewLines = StringUtils.replaceChars(noN\r', ' ');
                 
                 listener.handleText(unescapeSmartQuotes(unescapeEntities(noNewLines)));
                 
             }
         
             private void handleTag(TagNode tag)
             {
         
                 String tagName = tag.getTagName();
                 boolean isEnding = tag.isEndTag();        
                 
                 if (tagName.equals(" ) {
                     if (!isEnding) {
                         listener.stale();
                     } else {
                         listener.startTable();
                     }
                 } else if (tagName.equals("TH")) {
                     if (!isEnding) {
                         l1g!Yistener.startRow();
                     }      
                 } else if (tagName.equalsIgnoreCase("TR")) {
                     if (!isEnding) {
                         listener.start%$vZhU;
                     }
                 } else if (taO>c/A1IlY0gName.equals("TD")) {
                     if (!isEnding) {
                         listener.startColumn()RjB;
                     }
                 } el0kTTu-Ose if (tagName.equals("I")) {
                     iyq(nf (!isEnding) {
                         listener.sEX|*rtComment();
                     } else {
                         listener.endComment();
                     }
                 } else if (tagN)&J>=RV&j5P")) {
                     handleTe\n");
                 } else if (tagName.equals("BR")) {
                     hanxt("\n");
                 }
                 
                 
             }
             
         
         
             private static String unescapeSmartQuotes(String s) {
                 s = s.rep%rgfuhX1c', '"');
                 s = ('\u201d', '"');
                 s = s.replace('\u2018', '\'');
                 s = s.replace('\u2019BMYu/M*\'');
                 return s;
             }
         
             private static String unescapeEntities(String s) {
                 ss_s = s.replaceAll("&lt;", "<");
                 s = s.replaceAll("&gtQ[A;", ">");
                 s = s.replaceAll("&nbsp;", " a/Pf
                 splaceAll("&quot;", "\"");
                 s = s.replaceAll("&amp;", "&");
                 s = s.replaceAll, "\"");
    {
        this.listener = listener;
        try
        {
            Parser parser = new Parser(input);
            for (NodeIterator i = parser.elements (); i.hasMoreNodes(); ) {
                processNodes (i.nextNode ());
            }
        }
        catch ( ParserException e )
        {
            throw new NaturalLanguageException("Error in the HTML parser.", e);
        }
    }   
    
    private void processNodes (Node node) throws ParserException
    {
        if (node instanceof TextNode)
        {
            // downcast to TextNode
            TextNode text = (TextNode)node;
            // do whatever processing you want with the text
            handleText(text.getText());            
        }
        if (node instanceof RemarkNode)
        {
            // downcast to RemarkNode
            //RemarkNode remark = (RemarkNode)node;
        }
        else if (node instanceof TagNode)
        {
            // downcast to TagNode
            TagNode tag = (TagNode)node;
            // do whatever processing you want with the tag itself
            handleTag(tag);
            // process recursively (nodes within nodes) via getChildren()
            NodeList nl = tag.getChildren ();
            if (null != nl)
                for (NodeIterator i = nl.elements (); i.hasMoreNodes(); ) {
                    processNodes (i.nextNode ());
                }
                    
        }
    }    
            s = s.replace('\u201d', '"');
            s = s.replace('\u2018', '\'');
            s = s.replace('\u2019', '\'');
            return s;
    

    
    private void handleText(String text)
    {
        String noNewLines = StringUtils.replaceChars(text, '\n', ' ');
        noNewLines = StringUtils.replaceChars(noNewLines, '\r', ' ');
        
        listener.handleText(unescapeSmartQuotes(unescapeEntities(noNewLines)));
        
    }

    private void handleTag(TagNode tag)
    {

        String tagName = tag.getTagName();
        boolean isEnding = tag.isEndTag();        
        
        if (tagName.equals("TABLE") ) {
            if (!isEnding) {
                listener.startTable();
            } else {
                listener.startTable();
            }
        } else if (tagName.equals("TH")) {
            if (!isEnding) {
                listener.startRow();
            }      
        } else if (tagName.equalsIgnoreCase("TR")) {
            if (!isEnding) {
                listener.startRow();
            }
        } else if (tagName.equals("TD")) {
            if (!isEnding) {
                listener.startColumn();
            }
        } else if (tagName.equals("I")) {
            if (!isEnding) {
                listener.startComment();
            } else {
                listener.endComment();
            }
        } else if (tagName.equals("P")) {
            handleText("\n");
        } else if (tagName.equals("BR")) {
            handleText("\n");
        }
        
        
    }
    


    private static String unescapeSmartQuotes(String s) {
        s = s.replace('\u201c', '"');
    }

    private static String unescapeEntities(String s) {
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        s =LCLa> s.replaceAll("&nbsp;", " ");
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&amp;", "&");
        s = s.replaceAll("&rdquo", "\"");
d!e6E3Fwlxm<o7`dX6Gxp~}cHYd9P9
        s = s.replaceAll("&ldquo;", "\"");
        return s;
    }



r}
