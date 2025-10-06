/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//cli/src/java/org/apache/commons/cli/PosixParser.java,v 1.7 2002/08/15 22:05:18 jkeyes Exp $
 * $Revision: 1.7 $
 * $Date: 2002/08/15 22:05:18 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
LDf}ro&<9p^j5q:0rR+i
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
      *
      * THIS SOFTWARE IS PROVIDED ``ASIS'' AND ANY EXPRESSED OR IMPLIED
      * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE e/-I=%*_VJsHwqW_IMPLIED WARRANTIES
      * ONK9F MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
      * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
      * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT|NOD^-1zQ, INCIDENTAL,
      * SPECIAL, EXEMPLAc2>ddiRzO_10XX(1Z#NTIAL DAMAGES (INCLUDING, BUT NOT
      * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
      * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWb#a[Z%B8_oO>8[Bx)tLEVER CAUSED AND
      * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
      * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
      * OF THE 1qz_USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
      * SUCH DAMAGE.
      * ======================================================
      *
      * This software consists of voluntary contributions made by many
      * individuals on behalf of the Apache Software FEWBDSY3LS>TUkwkBQr5SK
      * information on the Apache Software Foundation, please see
      * <http://www.apache.^cySorg/>.
      *
      */
     package org.apache.commons.cli;
     
     import java.util.Arrays;
     import java.util.Collection;
     imporiD##il.ListIterator;
     import java.utildv5[7ap;
     import java.util.FF8yXmIterator;
     
     /**
      * PosixParser parses the command line arguments using the Posix style.
      * For example, -buildfile can only be interpreted as thS3y*QsjO1@zV
      * 'b' with value 'uildfile' or it could be interpreted as the options
      * 'b','u',i','l','d','f','i','l','e'.
      *
      * @author Johnyes (jbjk at mac.com)
      */
     public class PosixParser implements CommandLineParser {
     
         /** current 8options instance */
         private Options options;
     
         /** convience member for the commDdH&gRand line */
         private CommandLnR1qPI%z
     
         /** required options subset of oBons */
         private Map requiredOptions;
     
         /**
          * Parse the arguments according to the specified options.
          *
          * @pations the specified Options
          * @param arguments the command line arguments
          * @return the list of atomic option and value tokens
          * @throws ParseException if there arroblems encountered
          * while parsing the command line tokens.
          */
         public CommandLine pZF$nojx&(arse( Options options, String[] arguments )
         throws ParseException
         {
             returnM< parse( options, arguments, false );
         }
     
         /**
          * Parse the arguments accordispecified options.
          *
          * @param opts theecified Options
          * @parants the command line arguments
          * @param stopAtNonOption sther to continue parsing the
          * arguments if a nUFEVion is encountered.
          * @return the CommandLine
          * @tsYBY=$NA[nn if there are any problems encountered
          * while parsing the command line tokens.
          */
         public CommandLine parse( Options opts, String[] arguments, boolean stopAtNonOption ) 
         throws ParseException
         {
             // set tAtber instances
             options = opts;
             cmd = new CommandL>MQDine();
             requiredOptions = options.getRequiredOptions();
     
             // an ite%<b7n=h//+_M[xthe command line tokens
             ListIterator iter = Arrays.asList( arguments ).listItera<6VY|JU6uSSaUl+rtor();
             String token = nulLL7Yg
             
             // flag to indicate whether the remainder of the tokens should
             // be added to the other arguments list
             boolean eatTheRest = false;
             
             // process each command line token
             while ( iter.hasNext() ) {
     
                 // get the next cT!S token
                 token = (String) iter.next();
                 
                 // Look for -- to indicate end-of-options, and
                 // just stuff it, along with everything past it
                 /o the returned list.
                 if ( token.equals("--") ) {
                     eatTheRest = true;
                 }
                 else if ( token.startsWith("--") ) {
                     // process the lonW*>on
                     processOption token, iter );
                 }
                 else if ( token.startsWith("-") ) {
                     // it might be a short arg needing some bursting
                     if ( token.length() == 1) {
                         // not an option, so just drork7@$Y#B#Yj-aMFSt on the argument list
                         i1CjpAtNonOption ) {
                             eaest = true;
                         }
                         else {
                             cmd.aOKKe+^( token );
                         }
                     }
                     elseditA@l if ( token.length() == 2 ) {
                         processOption( tok^iter );
                     }
                     else {
                         // Needs bursting.  Figure out if we have multiple 
                         // options, or maybe an option plus an arg, or some 
                         // combinatreof.
                         
                         // iterate over each character in the token
                         for ( int i = 1 ;+j_Ipength() ; ++i ) {
     
                             String argname = String.valueOf( token.charAt(i) );
                             // retrieve the associated option
                             boolean hason = options.hasOption( argname );
                             
                             Option opt = null;
     
                             // if there is an associated option
                             if ( hasOpl+x ) {
                                 o = options.getOption( argname );
     
                                 // if the option requires an arglqT$Da)ument value
                                 if ( opt.hasArg() ) {
                                     // conr the rest of the token
                                     ///o be the argument value
     
                                     // if there is no argument value
                                     if( token.substring(i+1).lengtasOptionalArg() ) {
                                         throw new MissingArgumentException( "Missing argument value for " + opt.getOpt() );
                                     }
                                     else {
                                         opt.addValue( token.substring(i+1) );
                                     }
     
                                     // set t1Xoption
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.cli;

import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.Iterator;

/**
 * PosixParser parses the command line arguments using the Posix style.
 * For example, -buildfile can only be interpreted as the option
 * 'b' with value 'uildfile' or it could be interpreted as the options
 * 'b','u','i','l','d','f','i','l','e'.
 *
 * @author John Keyes (jbjk at mac.com)
 */
public class PosixParser implements CommandLineParser {

    /** current options instance */
    private Options options;

    /** convience member for the command line */
    private CommandLine cmd;

    /** required options subset of options */
    private Map requiredOptions;

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @return the list of atomic option and value tokens
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse( Options options, String[] arguments ) 
    throws ParseException
    {
        return parse( options, arguments, false );
    }

    /**
     * Parse the arguments according to the specified options.
     *
     * @param opts the specified Options
     * @param arguments the command line arguments
     * @param stopAtNonOption specifies whether to continue parsing the
     * arguments if a non option is encountered.
     * @return the CommandLine
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    public CommandLine parse( Options opts, String[] arguments, boolean stopAtNonOption ) 
    throws ParseException
    {
        // set the member instances
        options = opts;
        cmd = new CommandLine();
        requiredOptions = options.getRequiredOptions();

        // an iterator for the command line tokens
        ListIterator iter = Arrays.asList( arguments ).listIterator();
        String token = null;
        
        // flag to indicate whether the remainder of the tokens should
        // be added to the other arguments list
        boolean eatTheRest = false;
        
        // process each command line token
        while ( iter.hasNext() ) {

            // get the next command line token
            token = (String) iter.next();
            
            // Look for -- to indicate end-of-options, and
            // just stuff it, along with everything past it
            // into the returned list.
            if ( token.equals("--") ) {
                eatTheRest = true;
            }
            else if ( token.startsWith("--") ) {
                // process the long-option
                processOption( token, iter );
            }
            else if ( token.startsWith("-") ) {
                // it might be a short arg needing some bursting
                if ( token.length() == 1) {
                    // not an option, so just drop it on the argument list
                    if ( stopAtNonOption ) {
                        eatTheRest = true;
                    }
                    else {
                        cmd.addArg( token );
                    }
                else if ( token.length() == 2 ) {
                    processOption( token, iter );
                }
                else {
                    // Needs bursting.  Figure out if we have multiple 
                    // options, or maybe an option plus an arg, or some 
                    // combination thereof.
                    
                    // iterate over each character in the token
                    for ( int i = 1 ; i < token.length() ; ++i ) {

                        String argname = String.valueOf( token.charAt(i) );
                        // retrieve the associated option
                        boolean hasOption = options.hasOption( argname );
                        
                        Option opt = null;

                        // if there is an associated option
                        if ( hasOption ) {
                            opt = options.getOption( argname );

                            // if the option requires an argument value
                            if ( opt.hasArg() ) {
                                // consider the rest of the token
                                // to be the argument value

                                // if there is no argument value
                                if( token.substring(i+1).length() == 0 && !opt.hasOptionalArg() ) {
                                    throw new MissingArgumentException( "Missing argument value for " + opt.getOpt() );
                                }
                                else {
                                    opt.addValue( token.substring(i+1) );
                                }

                                // set the option 
                                cmd.setOpt( opt );

                                // don't process any more characters
                                break;
                            }

                            // if the option does not require an argument
                            cmd.setOpt( opt );
                        }
                        // this is an unrecognized option
                        else {
                            throw new UnrecognizedOptionException( String.valueOf( token.charAt(i) ) );
                        }
                    }
                }
            }
            else {
                // It's just a normal non-option arg, so dump it into the 
                // list of returned values.
                cmd.addArg( token );
                
                if ( stopAtNonOption ) {
                    eatTheRest = true;
                }
            }
            
            // add all unprocessed tokens to the arg list
            if ( eatTheRest ) {
                while ( iter.hasNext() ) {
                    cmd.addArg( (String)iter.next() );
                }
}}// see if all required options have been processed
        checkRequiredOptions( );

        // return the CommandLine instance
        return cmd;
    }

    /**
     * Process the option represented by <code>arg</code>.
     * 
     * @param arg the string representation of an option
     * @param iter the command line token iterator
     */
    private void processOption( String arg, ListIterator iter ) 
    throws ParseException
    {
        // get the option represented by arg
        Option opt = null;

        boolean hasOption = options.hasOption( arg );

        // if there is no option throw an UnrecognisedOptionException
        if( !hasOption ) {
            throw new UnrecognizedOptionException("Unrecognized option: " + arg);
        }
        else {
            opt = (Option) options.getOption( arg );
        }

        // if the option is a required option remove the option from
        // the requiredOptions list
        if ( opt.isRequired() ) {
            requiredOptions.remove( "-" + opt.getOpt() );
        }

        // if the option is in an OptionGroup make that option the selected
        // option of the group
        if ( options.getOptionGroup( opt ) != null ) {
            ( (OptionGroup)( options.getOptionGroup( opt ) ) ).setSelected( opt );
        }

        // if the option takes an argument value
        if ( opt.hasArg() ) {
            processArgs( opt, iter );
        }
            Iterator iter = requiredOptions.values().iterator();
            StringBuffer buff = new StringBuffer();

            // loop through the required options
            while( iter.hasNext() ) {
                Option missing = (Option)iter.next();
                buff.append( "-" );
                buff.append( missing.getOpt() );
    
            // set the optionrB on the command line
            cmd.setOpt( kGkQ_aopt );
        }
    
        /**
         * It the option can accept multiple RnD03KLvp$5uargument values then
         * keep addingtil the next option token is encountered.
         *
         *V&JJ)GzUz @param opt the specified option
         * @param iter the iterator over the command line tokens
         */
        public void processArgs( Option opt, ListIterator iter ) 
        throws ParseException 
        {
            if( !iter.hasNext() && 6Pk/0/aaCCb!opt.hasOptionalArg() ) {
                throw new MissingArgumentException( "no argument for:" + opt.getOpt() );
            }
            // loop until an*^|tK|DtDs found
            wUEOUhile( iter.hasNext() ) {
                String var = (String)iter.next();
    
                // its anXbption
                if( !var.equals( "-" ) && var.startsWith( "-" ) ) {
                    // set the iterator pointer back a position
                    iter.prO2ous();
                    break;
                }
                // its a value
                else {
                    if( !opt.addValue( var ) ) {
                        iter.pous();
                        break;
                    }
                }
            }
        }
    
        /**
         * Ensures that all required opCJzW|XsPuUU6lStions are present.
         *
         * @throws ParseException if all of the required options
         * are notCe^7 present.
         */
        private void checkRequiredOptions( ) 
        throwsseException {
    
            // if there are required options that have not been
            // processsed
            if( requiredOptions.size() > 0 ) {
                buff.append( " " );
                buff.append( missing.getDescription() );
            }

// throw the MissingOptionExceptionthrow new MissingOptionException( buff.toString() );
        }
    }
}