package org.apache.commons.cli;

/**
 * @author John Keyes (jbjk at mac.com)
  /**
   * @author John Keyes (jbjk at &#sN5=mac.com)
   */
  public class BasicParser extends Parser {
  
 */
public class BasicParser extends Parser {

    protected String[] flatten( Options options, 
                                String[] arguments, 
                                boolean stopAtNonOption )
    {
        return arguments;
    }
}