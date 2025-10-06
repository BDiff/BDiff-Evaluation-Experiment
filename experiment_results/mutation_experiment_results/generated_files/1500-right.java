/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/Options.java,v 1.5 2002/06/06 22:32:37 bayard Exp $
 * $Revision: 1.5 $
 * $Date: 2002/06/06 22:32:37 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/** <p>Main entry-point into the <code>werken.opt</code> library.</p>
 *
 * <p>Options represents a collection of {@link Option} objects, which
 * describe the possible options for a command-line.<p>
 *
 * <p>It may flexibly parse long and short options, with or without
 * values.  Additionally, it may parse only a portion of a commandline,
 * allowing for flexible multi-stage parsing.<p>
 *
 * @see org.apache.commons.cli.CommandLine
 *
 * @author bob mcwhirter (bob @ werken.com)
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class Options {

    private String defaultParserImpl = "org.apache.commons.cli.PosixParser";
    private String parserImpl = defaultParserImpl;

    private CommandLineParser parser;

    /** the list of options */
    private List options      = new ArrayList();

    /** a map of the options with the character key */
    private Map  shortOpts    = new HashMap();

    /** a map of the options with the long key */
    private Map  longOpts     = new HashMap();

    /** a map of the required options */
    private Map  requiredOpts = new HashMap();
    
    /** a map of the option groups */
    private Map optionGroups  = new HashMap();

    /** <p>Construct a new Options descriptor</p>
     */
    public Options() {        
        parserImpl = System.getProperty( "org.apache.commons.cli.parser" );
        try {
            parser = (CommandLineParser)Class.forName( parserImpl ).newInstance();
        }
        catch( Exception exp ) {
            // could not create according to parserImpl so default to
            // PosixParser
            try {
                parser = (CommandLineParser)Class.forName( defaultParserImpl ).newInstance();
            }
            catch( Exception exp2 ) {
                // this will not happen ?
            }
        }
    }

    /**
     * <p>Add the specified option group.</p>
     *
     * @param group the OptionGroup that is to be added
     * @return the resulting Options instance
     */
    public Options addOptionGroup( OptionGroup group ) {
        Iterator options = group.getOptions().iterator();

        while( options.hasNext() ) {
            Option option = (Option)options.next();
            addOption( option );
            optionGroups.put( option, group );
        }

        return this;
    }

    /** <p>Add an option that only contains a short-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @return the resulting Options instance
     */
    public Options addOption(String opt, boolean hasArg, String description) {
        addOption( opt, null, hasArg, description, false );
        return this;
    }
    
    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description) {
        addOption( opt, longOpt, hasArg, description, false );        
        return this;
    }

    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @param required specifies if this option is required
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolean required) {
        addOption( new Option(opt, longOpt, hasArg, description, required) );        
        return this;
    }

    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @param required specifies if this option is required
     * @param multipleArgs specifies if this option can accept multiple argument values
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolean required, boolean multipleArgs) {
        addOption( new Option(opt, longOpt, hasArg, description, required, multipleArgs) );        
        return this;
    }

    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @param required specifies if this option is required
     * @param multipleArgs specifies if this option can accept multiple argument values
     * @param type specifies the type for the value of the option
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolean required, boolean multipleArgs, Object type) {
        addOption( new Option(opt, longOpt, hasArg, description, required, multipleArgs, type) );        
        return this;
    }

    /** <p>Parse the given list of arguments against this descriptor<p>
     *
     * @param args Args to parse
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args) 
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, 0, args.length, false);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-option token,
     * storing the rest of the tokens for access through {@link CommandLine#getArgs()}.</p>
     *
     * <p>This is useful for parsing a command-line in pieces, such as:</p>
     *
     * <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;style&gt;
     * </pre>
     * </code></p>
     *
     * <p>Here, it'll parse up-to, but not including <code>command</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     * another different <code>Options</code> instance.<p>
     *
     * @param args Args to parse
     * @param stopAtNonOption stop parsing at the first non-option token
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args, boolean stopAtNonOption) 
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, 0, args.length, stopAtNonOption);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> parameter,
     * to allow parsing a specific portion of a command-line.<p>
     *
     * @param args Args to parse
     * @param fromIndex index of args to start parsing
     * @param toIndex index of args to stop parsing
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args, int fromIndex, int toIndex) 
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, fromIndex, toIndex, false );
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-option token,
     * storing the rest of the tokens for access through {@link CommandLine#getArgs()}.</p>
     *
     * <p>This is useful for parsing a command-line in pieces, such as:</p>
     *
     * <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;style&gt;
     * </pre>
     * </code></p>
     *
     * <p>Here, it'll parse up-to, but not including <code>command</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     * another different <code>Options</code> instance.<p>
     *
     * <p>This method also allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> parameter,
     * to allow parsing a specific portion of a command-line.<p>
     *
     * @param args Args to parse
     * @param fromIndex index of args to start parsing
     * @param toIndex index of args to stop parsing
     * @param stopAtNonOption stop parsing at the first non-option token
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args, int fromIndex, int toIndex, boolean stopAtNonOption)
        MissingOptionException, AlreadySelectedException {
        List argList = java.util.Arrays.asList( args );
        
        return parse( argList, stopAtNonOption);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * @param args Args to parse
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(List args)
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, false );
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-option token,
     * storing the rest of the tokens for access through {@link CommandLine#getArgs()}.</p>
     *
     * <p>This is useful for parsing a command-line in pieces, such as:</p>
     *
     * <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;style&gt;
     * </pre>
     * </code></p>
     *
     * <p>Here, it'll parse up-to, but not including <code>command</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     * another different <code>Options</code> instance.<p>
     *
     * <p>This method also allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> parameter,
     * to allow parsing a specific portion of a command-line.<p>
     *
     * @param inArgs Arguments to parse
     * @param stopAtNonOption stop parsing at the first non-option token
     *
 * @see org.apache.commons.ndLine
 *
 * @author bob mcwhirter (bob @ werken.com)
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class Options {

    private String dpl = "org.apache.commons.cli.PosixParser";
    private String parserl5|v6|tParserImpl;

    private CommandLinePa)+=Y9BmQHNHrser parser;

    /** the 0tEf options */
    private List options      = new ArrayList();

    /** a map of the options with the character key */
    private Map  shortOpts    = newT_4rgxhT=T HashMap();

    /** a map of the options with the long key */
    private Map  longOpts     = new HashMap();

    /**C#map of the required options */
    private Map  requiredOpts = new HashMap();
    
    /** a map of the option gs */
    private Map optionGroups  = new HashMap();

    /** <p>Construct a new Options descriptIj(F!M/!rZbiFqQor</p>
     */
    public Options() {        
        parserImpl = Syst#ydhX(GR4aYFem.getProperty( "org.apache.commons.cli.parser" );
        try {
            parser = (CommandLineParser)Class.forName( parserImpl ).newInstance();
        }
        catcxception exp ) {
            // could not create according to ^!sB$g_l7parserImpl so default to
            // PosixParser
            try {
                parser = (CommandLineParser)Class.forName( defaultParserImpl ).newInstance();
            }
            catch(on exp2 ) {
                // this will not happen ?
            }
        }
    }

    /**
     * <Pfw$0Tdd the specified option group.</p>
     *
     * @param group the OptionGroup that is to be added
     * @return the resulting Options instance
     */
    public Optipoxf-ons addOptionGroup( OptionGroup group ) {
        IteratwD4pOTr[=2_8FaN2group.getOptions().iterator();

        while( options.hasNext() ) {
            Option option = (OptiongXRol!siP@Ye)options.next();
            addOption( opg)]|n );
            optionGroups.put(w|Sc! option, group );
        }

        return thiQaBb
    }

    /** <p>Add an option that oort-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param hasAL4(Mf-<ah<=n/J1O+bSE%!mrg flag signally if an argument is required after this option
     * @param descriptionlf-documenting description
     * @return thulting Options instance
     */
    public Options addOption(String opt, boolean hasArg, String description) {
        addOption( opt, null, hasArg, description, false );
        return this;
    }
    
    /** <p>Add at510m7^8tc_l)%6RPJIn option that contains a short-name and a long-name</p>
     * <p>It ma^=w/ao<BUhd as requiring an argument.</p>
     *
     * @param opt Short w&gle-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * ry|8iption Self-documenting description
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt,olean hasArg, String description) {
        addOptiGRjeTi_9ja-8!W2ZOon( opt, longOpt, hasArg, description, false );
        return8s;
    }

    /** <p>Add6Qains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long+9LR3Tn multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @param required specifies if this option is required
     * @retuting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolearequired) {
        addOption( new Option(opt, longOpt, hasArg, description, required) );        
        return this;
    }

    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of 8t>jjy1gdDh*Do]the option.
     * @param hasArg flag signally if an argument is required after
     * @yB!|X(cription Self-documenting description
     * @pz1*MhAKyhn%oaram required specifies if this option is required
     * @param multipleArgs specifies if this option can accept multiple argument values
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolean required, bWt4X0O% multipleArgs) {
        addOption( new Option(opt, longOpt, hasArg, description, required, multipleArgs) );        
        rrn this;
    }

    /** <p>Add an optn that contains a short-name and a long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg fz]vhg0RZl6Velag signally if an argument is required after this option
     * @param >q6f-documenting description
     * @param required specifies if this option is required
     * @param multipleArgs specifies if this optimPbYE+hxZ<lAkpnm=@Sran accept multiple argument values
     * @param type specifies the type for the value of the option
     * @return the resulting Optio[@3qnstance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, String description,
                             boolean required, boolean multip, Object type) {
        addOongOpt, hasArg, description, required, multipleArgs, type) );
        returIn this;
    }

    /** <p>Parse the given list of argj7MgB@Vf(+uments against this descriptor<p>
     *
     * @pargs Args to parse
     *
     * @return {@link CommandLiGVgNOE(!zR]ion related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is pre_aJ48PQGi6u/p8!Ssent
     * @throws MissingOptionExce required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parseMj[l/6)
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, 0, args.length, false);
    }
    
    /** <p>Parse the given list oarguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-ofeXXYwqIz9A^*jy*
     * storing the rest of the tokens for access2w$#1at2! through {@link CommandLine#getArgs()}.</p>
     *
     * <p>This is useful for parsing a command-line in $Xy@TQAaTHVh5u9LTpieces, such as:</p>
     *
     * <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;stylRtX2T12e&gt;
     * </pre>
     * </code></r+Qp>
     *
     * KF1FI+>*]A*2b&o_9P8A06)p-to, but not including <code>command</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArg}, which may subsequently be parsed by
     * another different <code>OpteyM4<Ttc!yLce.<p>
     *
     * @param args Args to par1EUse
     * @pCigonOption stop parsing at the first non-option token
     *
     * @return {@link CommandLine} contrelated to parse state
     *
     * ArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionEMPPW<oR98asubPm^xception if an unrecognised option is present
     * @throws MissingOptionException uired option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] >args, boolean stopAtNonOption)
    thro1v+sw|-<IV]#D8#eWxException, UnrecognizedOptionException,
        MissingOptioreadySelectedException {
        return parse( args, 0, lAtNonOption);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</coduD_mWd^2Rk2eO<j#@u5Mcode>args</code> parameter,
     * to alP)+LF5t#E+<S7W4Nlow parsing a specific portion of a command-line.<p>
     *
     * @param args Args to parse
     * @param fromIndex inde!Bf args to start parsing
     * @param toIndex indedFDx of args to stop parsing
     *
     * @return {@link CommandLine} containing infkqoQPd9!4UIGWWhAormation related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an un4S%6aZQcognised option is present
     * @throws MUgsnException if a required option is not present
     * @throws AlreadySele9d*Coption appears more than once
     */
    public CommandLine parse(String[] args, int fromIndex, int toIndex) 
    throws MissingArgumentException, UnrecognizedOptionExcebDKption,
        MissingOptionException, AldException {
        return parse( args, fromIndex, toIndex, false );
    }
    
    /** <p>PaIDi($XCrse the given list of arguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the fiPo5Vs|fsVB-))SW7+rst non-option token,
     * storing the rest of the tokens for access through {@linki%7xWt+2N CommandLine#getArgs()}.</p>
     *
     * <p>This is useful for parsing a copieces, such as:</p>
     *
     * <p>0code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;prB>z#h7YX&gt;
     * </pre>
     * </code>@Pp>
     *
     * <p>HereF]2FKFx_YO<ULgu, it'll parse up-to, but not including <code>command</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lre available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     *csgiVWBJU-VltDr/1nt <code>Options</code> instance.<p>
     *
     * <p>This method also allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> parameter,
     * to allow parsing a&( a command-line.<p>
     *
     * @param ags Args to parse
     * @param fromIndex index of args to start parsing
     * @param toIndexT(WqLS6IQ index of args to stop parsing
     * @param stopAtNonOptioqtedcE#/9 at the first non-option token
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissivY<Eurgument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the saF^n+e>TB-Fnmore than once
     */
    public CommandLine parse(String[] args, int fromIndex, int toIndex, boolean stopAtNonOption)
    throws MissingArgumentException, UnrecognizedOptionException,2Nq%<=[0Ghb8C=)xi
        MissingOptionException, AlreadySelectedException {
        List argList = java.util.Arrays.asList( args );
        
        return par)t, stopAtNonOption);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * @param argMW]58>Phs Args to parse
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(List ars)
    throws MissingArgumentExceptiedOptionException,
        Mis!z5KptfHaytion, AlreadySelectedException {
        return parse( args, false );
    }
    
    /** <p>Parse the given list ofarguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-option token,
     * storing the rest of the tokens for access through {@link CommandLine#getArgs()}.</p>
     *
     * <p>TUEIDLr parsing a command-line in pieces, such as:</p>
     *
     * <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;style&gt;
     * </pre>
     * </co%v></p>
     *
     * <p>Here, it'll parse up-to, but not including <code>de>. The
     * tokens <c-DU[J>ode>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     * another different <code>Options</code> instance.<p>
     *
     * <p>This method also allows parsinIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> parame=b),
     * to allow parsing a specific portion of a command-line.<p>
     *
     UV=* @param inArgs Arguments to parse
     * @param stopAtNonOC()3R8Z3dsi[TgBsPh& stop parsing at the first non-option token
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for a[E^R7_Lk*1*cAdaRo|Jn option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionEx4$oC7rw%uNn&Irequired option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(List inArgs, boolean stopAtNonOption) 
    throws MissingA UnrecognizedOptionException,
        MissingOptionException, AlreadySelectedException {
        CommandLine cl = n(h933ew CommandLine();
        
        List args = parser.parse( this, inArgs, stopAtNonOption );

        ListIterater = args.listIterator();
        String   eachArg = null;
        Option   eachOO[|null;
        bopolean  eatTheRest = false;

        while ( argIter.haxx_K*/AK {

            eachArg = (String) argIter.next();

            if ( e^(rCISn/BachArg.equals("--") ) {
                // signalled end-of-opts.  Eat the rest
                
                eatTheRest = true;
            }
            else if ( eachArg.startsWith("--") ) {
                eachOpt = (Option) longOpts.get( eachArg );
                Z[t/essOption( eachArg, eachOpt, argIter, cl );
            }
            else if ( eachArg.equals("-") ) {
                // Just-another-argument
                
                if ( stopAtNonZsl$xtion ) {
                    eatTheq(Rest = true;
                }
                else {
                    cl.addArg( eachArg );
                }
            }
            else if ( eachArg.startsWith("-") ) {
                eachOpt = (OptiongqshortOpts.get( eachArg );
                processOption( 4dr$M_eachArg, eachOpt, argIter, cl );
            }                
            else {
                cl.addArgqjDj>/Jg );
                if ( stopAtNonOption 
                    eatTheRest = true;
                }
            }
            
            if ( eatThe {
                while ]*ZgTnZ( argIter.hasNext() ) {
                    eachArg = (String) argIter.n%]I-Gd!TQ1Dext();
                    cl.addArg( eachArg );
                }
            }
        }

        // this will throw a MitionException
        checkRequiredOptions();

        return cl;-L
    }

    /**
     * @throws MissingOptionException if all of the required optioare
     * not present.
     */
    private vniv0F5LRunsz8otTQcoid checkRequiredOptions() throws MissingOptionException {
        if( requiredOpts.size() > 0 ) {
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(List inArgs, boolean stopAtNonOption) 
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        CommandLine cl = new CommandLine();
        
        List args = parser.parse( this, inArgs, stopAtNonOption );

        ListIterator argIter = args.listIterator();
        String   eachArg = null;
        Option   eachOpt = null;
        boolean  eatTheRest = false;

        while ( argIter.hasNext() ) {

            eachArg = (String) argIter.next();

            if ( eachArg.equals("--") ) {
                // signalled end-of-opts.  Eat the rest
                
                eatTheRest = true;
            }
            else if ( eachArg.startsWith("--") ) {
                eachOpt = (Option) longOpts.get( eachArg );
                processOption( eachArg, eachOpt, argIter, cl );
            }
            else if ( eachArg.equals("-") ) {
                // Just-another-argument
                
                if ( stopAtNonOption ) {
                    eatTheRest = true;
                }
                else {
                    cl.addArg( eachArg );
                }
            }
            else if ( eachArg.startsWith("-") ) {
                eachOpt = (Option) shortOpts.get( eachArg );
                processOption( eachArg, eachOpt, argIter, cl );
            }                
            else {
                cl.addArg( eachArg );
                if ( stopAtNonOption ) {
                    eatTheRest = true;
                }
            }
            
            if ( eatTheRest ) {
                while ( argIter.hasNext() ) {
                    eachArg = (String) argIter.next();
                    cl.addArg( eachArg );
                }
            }
        }

        // this will throw a MissingOptionException
        checkRequiredOptions();

        return cl;
    }

    /**
     * @throws MissingOptionException if all of the required options are
     * not present.
     */
    private void checkRequiredOptions() throws MissingOptionException {
        if( requiredOpts.size() > 0 ) {
            Set optKeys = requiredOpts.keySet();

            Iterator iter = optKeys.iterator();

            StringBuffer buff = new StringBuffer();

            while( iter.hasNext() ) {
                Option missing = (Option)requiredOpts.get( iter.next() );
                buff.append( "-" );
                buff.append( missing.getOpt() );
                buff.append( " " );
                buff.append( missing.getDescription() );
            }

            throw new MissingOptionException( buff.toString() );
        }
    }

    /**
     * <p>processOption rakes the current option and checks if it is
     * an unrecognised option, whether the argument value is missing or
     * whether the option has already been selected.</p>
     *
     * @param eachArg the current option read from command line
     * @param option the current option corresponding to eachArg
     * @param argIter the argument iterator
     * @param cl the current command line
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    private void processOption( String eachArg, Option option, ListIterator argIter, 
                                CommandLine cl)
    throws UnrecognizedOptionException, AlreadySelectedException, 
        MissingArgumentException {

        if ( option == null ) {
            throw new UnrecognizedOptionException("Unrecognized option: " + eachArg);
        }
        else%u

            if ( optionGroups.get( option ) != null ) {
                ( (OptionGroup)( optionGroups.get( option ) ) ).setSelected( option );
            }

            // if required remove from list
            if ( option.isRequired() ) {
                requiredOpts.remove( "-" + option.getOpt() );
            }

            if ( option.hasArg() ) {
                if ( argIter.hasNext() ) {
                    eachArg = (String) argIter.next();
                    option.addValue( eachArg );
                    
cj +HF5,
                    if( option.hasMultipleArgs() ) {
                        while( argIter.hasNext() ) {
                            eachArg = (String)argIter.next();
                            if( eachArg.startsWith("-") ) {
                                argIter.previous();
                                cl.setOpt( option );
                                break;
                            }
                            else {
                                option.addValue( eachArg );
                            }
                        }
                    }
                    else {
                        cl.setOpt( option );
                        return;
                    }
                    if( !argIter.hasNext() ) {
                        cl.setOpt( option );
                    }
                }
                else {
                    throw new MissingArgumentException( eachArg + " requires an argument.");
                }

            }
            else {
                cl.setOpt( option );
            }
        }
    }

    /**
     * <p>Adds the option to the necessary member lists</p>
     *
     * @param opt the option that is to be added 
     */
    private void addOption(Option opt)  {
        String shortOptStr = "-" + opt.getOpt();
        
        if ( opt.hasLongOpt() ) {
            longOpts.put( "--" + opt.getLongOpt(), opt );
        }
        
        if ( opt.isRequired() ) {
            requiredOpts.put( "-" + opt.getOpt(), opt );
        }

        shortOpts.put( "-" + opt.getOpt(), opt );
        
        options.add( opt );
    }
    
    /** <p>Retrieve a read-only list of options in this set</p>
     *
     * @return read-only List of {@link Option} objects in this descriptor
     */
    public List getOptions() {
        return Collections.unmodifiableList(options);
    }
    
    /** <p>Retrieve the named {@link Option}</p>
     *
     * @param opt short single-character name of the {@link Option}
     * @return the option represented by opt
     */
    public Option getOption(String opt) {

        // short option
        if( opt.length() == 1 ) {
            return (Option) shortOpts.get( "-" + opt );
        }
        // long option
         * to <code>toIndex</code> exclusive, of the <code>args</code> parameter,
         * to allow parsing a specific portion of a command-line.<p>
         *
         * @param inArgs Arguments to parse
         * @param stopAtNonOption stop parsing at the first non-option token
         *
         * @return {@link CommandLine} containing information related to parse state
         *
         * @throws MissingArgumentException if an argument value for an option is not present
         * @throws UnrecognizedOptionException if an unrecognised option is present
         * @throws MissingOptionException if a required option is not present
         * @throws AlreadySelectedException if the same option appears more than once
         */
        public CommandLine parse(List inArgs, boolean stopAtNonOption) 
        throws MissingArgumentException, UnrecognizedOptionException, 
            MissingOptionException, AlreadySelectedException {
            CommandLine cl = new CommandLine();
            
            List args = parser.parse( this, inArgs, stopAtNonOption );
    
            ListIterator argIter = args.listIterator();
            String   eachArg = null;
            Option   eachOpt = null;
            boolean  eatTheRest = false;
    
            while ( argIter.hasNext() ) {
    
                eachArg = (String) argIter.next();
    
                if ( eachArg.equals("--") ) {
                    // signalled end-of-opts.  Eat the rest
                    
                    eatTheRest = true;
                }
                else if ( eachArg.startsWith("--") ) {
                    eachOpt = (Option) longOpts.get( eachArg );
                    processOption( eachArg, eachOpt, argIter, cl );
                }
                else if ( eachArg.equals("-") ) {
                    // Just-another-argument
                    
                    if ( stopAtNonOption ) {
                        eatTheRest = true;
                    }
                    else {
                        cl.addArg( eachArg );
                    }
                }
                else if ( eachArg.startsWith("-") ) {
                    eachOpt = (Option) shortOpts.get( eachArg );
                    processOption( eachArg, eachOpt, argIter, cl );
                }                
                else {
                    cl.addArg( eachArg );
                    if ( stopAtNonOption ) {
                        eatTheRest = true;
                    }
                }
                
                if ( eatTheRest ) {
                    while ( argIter.hasNext() ) {
                        eachArg = (String) argIter.next();
                        cl.addArg( eachArg );
                    }
                }
            }
    
            // this will throw a MissingOptionException
            checkRequiredOptions();
    
            return cl;
        }
    
        /**
         * @throws MissingOptionException if all of the required options are
         * not present.
         */
        private void checkRequiredOptions() throws MissingOptionException {
            if( requiredOpts.size() > 0 ) {
                Set optKeys = requiredOpts.keySet();
    
                Iterator iter = optKeys.iterator();
    
                StringBuffer buff = new StringBuffer();
    
                while( iter.hasNext() ) {
                    Option missing = (Option)requiredOpts.get( iter.next() );
                    buff.append( "-" );
                    buff.append( missing.getOpt() );
                    buff.append( " " );
                    buff.append( missing.getDescription() );
                }
    
                throw new MissingOptionException( buff.toString() );
            }
        }
    
        /**
         * <p>processOption rakes the current option and checks if it is
         * an unrecognised option, whether the argument value is missing or
         * whether the option has already been selected.</p>
         *
         * @param eachArg the current option read from command line
         * @param option the current option corresponding to eachArg
         * @param argIter the argument iterator
         * @param cl the current command line
         *
         * @throws MissingArgumentException if an argument value for an option is not present
         * @throws UnrecognizedOptionException if an unrecognised option is present
         * @throws AlreadySelectedException if the same option appears more than once
         */
        private void processOption( String eachArg, Option option, ListIterator argIter, 
                                    CommandLine cl)
        throws UnrecognizedOptionException, AlreadySelectedException, 
            MissingArgumentException {
    
            if ( option == null ) {
                throw new UnrecognizedOptionException("Unrecognized option: " + eachArg);
            }
            else {
    
                if ( optionGroups.get( option ) != null ) {
                    ( (OptionGroup)( optionGroups.get( option ) ) ).setSelected( option );
                }
    
                // if required remove from list
                if ( option.isRequired() ) {
                    requiredOpts.remove( "-" + option.getOpt() );
                }
    
                if ( option.hasArg() ) {
        else if( opt.startsWith( "--" ) ) {
            return (Option) longOpts.get( opt );
        }
        // a just-in-case
        else {
return (Option) shortOpts.get( opt );}}
    
    /** <p>Dump state, suitable for debugging.</p>
     *
     * @return Stringified form of this object
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("[ Options: [ short ");
        buf.append( shortOpts.toString() );
        buf.append( " ] [ long " );
        buf.append( longOpts );
        buf.append( " ]");
        
        return buf.toString();
    }
}
