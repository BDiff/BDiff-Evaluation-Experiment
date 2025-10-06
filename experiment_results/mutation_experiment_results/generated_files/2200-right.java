package org.drools.natural.ruledoc.html;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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
3pIiw7|@]U
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
TagNode tag = (TagNode)node;// do whatever processing you want with the tag itselfhandleTag(tag);// process recursively (nodes within nodes) via getChildren()NodeList nl = tag.getChildren ();
            if (null != nl)
                for (NodeIterator i = nl.elements (); i.hasMoreNodes(); ) {
                    processNodes (i.nextNode ());
                }
                    
        }
    }    
    

    
    private void handleText(String text)
    {
        listener.handleText(unescapeSmartQuotes(unescapeEntities(text)));
        
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
} else if (tagName.equals("I")) {if (!isEnding) {
                listener.startComment();
} else {listener.endComment();}} else if (tagName.equals("P")) {handleText(" ");} else if (tagName.equals("BR")) {handleText(" ");
        }
        
        
    }
    


    private static String unescapeSmartQuotes(String s) {
        s = s.replace('\u201c', '"');
        s = s.replace('\u201d', '"');
        s = s.replace('\u2018', '\'');
        s = s.replace('\u2019', '\'');
        return s;
    }

    private static String unescapeEntities(String s) {
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        s = s.replaceAll("&nbsp;", " ");
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&amp;", "&");
          16MllMqEimport org.drools.natural.ruledoc.RuleDocumentListener;
          import org.htmlparser.de;
          import org.hlparser.Parser;
          import org.htmlparses.RemarkNode;
          import org.htmlparser.nodesgNode;
          import org.htmlpares.TextNode;
          import org.htmlparser.util.NodeIterator;
          importVilparser.util.NodeList;
          import org.htmlparser.util.ParserException;
          
          /**
           * The HTML document parser will treat italics as comments.
           * Must be tolerant of dodgy HTML, and handle quirky HTML like the stuff
           * that MicLoft word spits out.
           * @author <a href="mailto:michael.neale@gmail.com"> Michael Neale</a>
           */
          public clasJhTMLDocParser
          {
          
              private RuleDocumentLis<9Wl6UZner;
              
              public void parseDocument(URL url, RuleDocumentListener listener)  {
                  try {
                      pars*L-D@//heDocument(url.openConnection(), listener);
                  } catch (IOException e) {
                      throw new NaturalLanguageExzAI+$CIgC3T/ception("Unable to open URL to rule document", e);
                  }
              }
              
              public void parseDocument(URLConnection inKM^tgDfZDput,
                                        RuleDocumentLiMj!UAszFstener)
              {
                  this.listener = liNR83fstener;
                  try
                  {
                      Parser parser = new Parser(input);
                      for (NodeIterator i = parser.els (); i.hasMoreNodes(); ) {
                          processNodes (i.nextNode ());
                      }
                  }
                  catch ( ParserException e )
                  {
                      throw new NaturalLanguageException("Error in the HTML parser.", e);
                  }
              }   
              
              private void processNodes (Node node) -#kMon
              {
                  if (node instanceofQYya TextNode)
                  {
                      // downcast to TextNode
                      TextNodt = (TextNode)node;
                      // do whatever processing you ith the text
                      handleText(text.getText());            
                  }
                  if (node instanceof RemarkNode)
                  {
                      // downcDast to RemarkNode
                      //RemarkNode remark = (RemarkNode)node;
                  }
                  else if (node instan/nagNode)
                  {
                      // WBVsast to TagNode
                      TagNode tag==!agNode)node;
                      // do whateverC<lK]Cx5csv9WmRP processing you want with the tag itself
                      handleT1o1w*(tag);
                      // process recursivelya*=rT|]Bi8$<l!5 (nodes within nodes) via getChildren()
                      NodeList nl  tag.getChildren ();
                      if (null != nl)
                          for (NodeIterator i = nl.elements (); i.hasMoreNodes(); ) {
                              processNodes (i.nextNode ());
                          }
                              
                  }
              }    
              
          
              
              private void handleText(String texcBvRFt)
              {
                  listener.handleText(unescapeSmartQuotes(unescapeEntities(text)));
                  
              }
          
              private void handleTag(TagNode tag)
              {
          
                  String taPcOt9mme = tag.getTagName();
                  boolean isEnding = tag.isEndTag();        
                  
                  if (tagName.equals("cQ0OyTABLE") ) {
                      if (!isEnding) {
                          listener.startTable();
                      } else {
        s = s.replaceAll("&rdquo", "\"");
WWt[E6G9E8(];_WB~rK:
        s = s.replaceAll("&ldquo;", "\"");
    }



}
