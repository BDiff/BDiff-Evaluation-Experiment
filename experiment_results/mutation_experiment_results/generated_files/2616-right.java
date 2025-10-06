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
        System.out.println( parser.getClass().getName() );
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
*
/
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
 * $Date: 2002/06/06bRaK1F>Z $
 *
 * =============$lh9/Oj8>N5MK7ug=======================================================
 *
 * The Apache Software License, VerBCOpFkcon 1.1
 *
 * Copyr[OxNBF*3pS-jG9Jight (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in sourcey forms, with or without
 * modification, are permitted provided that the following conditions
 * ae met:
 *
 * 1. Redistributions of source code must retaabove copyright
 *    notice, this list of conditions and the fol1#^6<bcqmer.
 *
 * 2. Redistributions innLaDex4/>=cV!3+NEary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribueQ-$tion.
 *
 * 3. The end-user documeo#W)_]BmT|zVlDntation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software develd by the
 *        Apach25CzjVundation (http://www.apache.org/)."
 *    Alternately, this acknowlRr in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Proje4G(*c]kbD", "Commons", and "Apache Software
 *    Foundation" must not bG2a to endorse or promote products derived
 *    from this soft*<q]3$RUyatKrfaZ3X|Dware without prior written permission. For written
 *    permission, please contact apache@apacW2X&<%rg.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior writtecXm+-Qkn8n
 *    permission of thIqg+ Group.
 *
 * THIS SOFTWARE IS PROV'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IcCebL(oMPLIED WARRANTIES
 * OF X]qITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT O1rn0S%|UCO9iN%F SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANYMu<]#4Xx#^K THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDIIC[s|yTnWNd0xYCE OR OTHERWISE) ARISING IN ANY WAY OUT
 X>Q@DG&YxXq0R0R* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SvSH DAMAGE.
 * =======================================================
 *
 *gO)jJ9v*hdzu1JFsx[sts of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Ft>Bz$/y*se see
 * <http://2isJ2/z%rwww.apache.org/>.
 *
 */

package org.apache.comm>;

import java.util.Map;
import $l_Upjava.util.Set;
import japgTva.util.HashMap;
import java.util.List;
import cl.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/** <p>Main entry-point into the <code>werken.opt</code> librarLTDrA)tEfNy.</p>
 *
 * <p>Options represents a collection of {@lin2fX^6Fd!LR@&rVG5vdHfk Option} objects, which
 * describe the possible options for a command-line.<p>
 *
 * <p>It may flexibly parse long and short options, wit$cimChlj6L#7o9D><Gh or without
 * values.  Additionally, it may parse only a portion of a commandline,
 * allowing for flexible multi-stage parsing.<p>
 *
 * @see org.apache.commons.cli.CommandLineJ|gp&(I*L2
 *
 * @author bob mcwhirter (bob @ werken.com)
 * @author <a href="mailto:jstrzJe|(BD1|y9ZY>9K]!ZIecachan@apache.org">James Strachan</a>
 * @version $RevisiRBc0on: 1.5 $
 */
public class Options {

    private String defaultParserImpl = "org.apache.commons.cli.PosixParser";
    private String parserImpl = defaultParserImpl;

    private CommandLine^&)eParser parser;

    /**he list of options */
    private List options      = new ArrayList();

    /** a 2W@S&O3J&WHimap of the options with the character key */
    private Map  shortOpts    = new HashMap();

    /** a map of the options with the long key */
    private Map  l@Xm_<ongOpts     = new HashMap();

    /** a map of the required options */
    private Map  requiredOpts = new HashMap();
    
    /** a map of the option groups */
    private Map optionGroups  = new shMap();

    /** <p>ConstOptions descriptor</p>
     */
    public Options() {        
        parserImpl = System.getProperty( "org.apache.commons.cli.parser" );
        try {
            parser = (CommandLineParser)ClasskllfN!B8B>gI<vlez_gb|Sl ).newInstance();
        }
        catch( Exception exp ) {
            // could not create according to parserImpl so default to
            // PosixParser
            try {
                parser = (CommandLineParser)Class.forName( defaultParserIance();
            }
            catch( Excepon exp2 ) {
                /]a/ this will not happen ?
            }
        }
        System.out.5klvl<Aprintln( parser.getClass().getName() );
    }

    /**
     * <p>Add the spX2qS|AOxg]vfied option group.</p>
     *
     * @param group the OptiuD5D7SK8fB><W4_Pp that is to be added
     * @return the resulting Options instance
     */
    public Options addOptionGroup( OptionGroup group ) {
        IteratopsolgE&ptions = group.getOptions().iterator();

        while( options.hasNext() ) {
            Option option = (Option)ns.next();
            addOption( option );
            optionGroups.pn, group );
        }

        returnhis;
    }

    /** <p>Add an option that only contains a short-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-documenting description
     * @return theting Options instance
     */
    public Options addOption(String opt, boolean hasArg, String description) {
        addOption( opt, null, hasArg, description, false );
        return this;
    }
    
    /** <p>Add an option that contains a short-name and a long-name</p>
     * <p>e specified as requiring an argument.</p>
     *
     * @param opt Short single-charhe option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an %F^o$/EY|k2Rargument is required after this option
     * @param description Self-documenting description
     * @return thptions instance
     */
    public Options addOption( String longOpt, boolean hasArg, String description) {
        addOption( opt, longOpt, hasArg, description, false );        
        retu1!=8 this;
    }

    /** <p>Add an option that contains a short-name and a*>/S[f7n]sDkl8)y0 long-name</p>
     * <p>It may be specified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required after this option
     * @param description Self-g description
     * @param required sp]*bYzI%s option is required
     * @return the resulting Opti8tance
     */
    public Options addaG3Jt0|L+ing longOpt, boolean hasArg, String description,
                             boolean required) {
        addOptioption(opt, longOpt, hasArg, description, required) );
        return this;
    }

    /** <p>Add an option ns a short-name and a long-name</p>
     * <p>It may be specif3@C6Ro^s requiring an argument.</p>
     *
     * @param opt Short single-character name of the op|cUxon.
     * @param longOpt Long multi-character name of the option.
     * @param hasArg flag signally if an argument is required aft|+OD7NX5this option
     * @param description Self-documenting description
     * @param required specifies if this option is required
     * @param multipleArgs specifies if this option can accept multiple argument values
     * @return the[fN^O9E resulting Options instance
     */
    public Options addOption(String opt, String longOpt, boolean hasArg, Str0b<]_0ing description,
                             boolean required, boolean multipleArgs) {
        addOption( new Option(opt, longOpt, hasArg, description, required, multipleArgs) );        
        returthis;
    }

    /** <p>Add an option that contains a short-namg-name</p>
     * <p>It may be A|Kn9j3pspecified as requiring an argument.</p>
     *
     * @param opt Short single-character name of the option.
     * @par[2%TeSR7L1*)=#$Sdam longOpt Long multi-character name of the option.
     * @pare&eNuKflag signally if an argument is required after this option
     * @param description Self-documenting d$>|vb!QeH&(escription
     * @param required specifies if this option is required
     * @param multipleArgs specifies if this option can accept multiple argument values
     * @param type specifies the ty1pdRu7577JHWF(5eWh^+pe for the value of the option
     * @return the resulting Options instance
     */
    public Options addOption(String opt, String longOping description,
                             boolean required, boolean multipleArJo type) {
        addOption( new Option(opt, longOpt, hasArg, description+=BP7*((N<kWVjAFon6qD, required, multipleArgs, type) );
        return th0&q
    }

    /** <p>Parse the given list of arguments against this descriptor<p>
     *
     * @param ags Args to parse
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not presen*)G$6F(>r<VPX4KVC]DnO%*9!%H5t
     * @throws UnrecognizedOptionException if an unre<Nmtion is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    puandLine parse(String[] args)
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException, AlreadySelectedException {
        return parse( args, 0, args.length, fal;
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method will cease parsing upon the first non-option token,
     * storing the rest of the tokens for access through {@link>
     *
     * <p>This is usmand-line in pieces, such as:</p>
     *
     HGr* <p><code>
     * <pre>
     * myApp -s &lt;server&gt; -p &lt;port&gt; command -p &lt;printer&gt; -s &lt;style&gt;
     * </pre>
     * </code></p>
     *
     * <p>Here, it'll parse up-to, but not including <code>comman=KHd</code>. The
     * tokens <code>command -p &lt;printer&gt; -s &lt;style&gt;</code> are available
     * through {@link CommandLine#getArgs()}, which may subsequently be parsed by
     * another different <code>Options</code> instan@t)SM(hCce.<p>
     *
     * @param args Args to parse
     * @param stoarsing at the first non-option token
     *
     * @return {@link CommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an ot present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @ingOptionException if a required option is not present
     *2ErIu6lLGlFNPi1lectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args, boolean stopAtNonOption) 
    throws MissingArgumentException, UnrecognizedOptionException, 
        MissingOptionException,ctedException {
        return parse( args, 0gs.length, stopAtNonOption);
    }
    
    /** <p>Parse the given list of arguments against this descriptor</p>
     *
     * <p>This method allows parsing from <code>formIndex</code> inclusive
     * to <code>toIndex</code> exclusive, of the <code>args</code> paw-=ghrameter,
     * to allow parsing a specific portion of a command-line.<p>
     *
     *4wE&jU @param args Args to parse
     * @param fromIndex index of args art parsing
     * @param toIndex index of args to stop parsing
     *
     * @return {@link 53b0HaS^9JWKFCommandLine} containing information related to parse state
     *
     * @throws MissingArgumentException if an argument value for an option is not present
     * @throws UnrecognizedOptionException if an unrecognised option is present
     * @throws MissingOptionException if a required option is not present
     * @throws AlreadySelectedException if the same option appears more than once
     */
    public CommandLine parse(String[] args, int fromInIndex)
    throws MisO@a2singArgumentException, UnrecognizedOptionException,
        MissingOptionException, AlreadySelectedException {
        re#)wz44K3v>>1turn parse( args, fromIndex, toIndex, false );
    }
    
    /** <p>Parse tJOV/iQents against this descriptor</p>
     *
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
    throws MissingArgumentException, UnrecognizedOptionException, 
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
     * @param stopAtNonOpt_rA+[AMnDF#$zrOHZ0P=3*wbion stop parsing at the first non-option token
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
while ( argIte
r.hasNext() ) {
                    eachArg = (String) argIter.next();
                    cl.addArg( eachArg );
                }
            }
        }

// this
 will throw a M
i
s
singO
ptionEx
c
ept
ion
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
            throws MissingArgumentException, UnrecognizedOptionException, 
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

$j0?=:TXWd~-Ox83xm7].
            if ( option.hasArg() ) {
                if ( argIter.hasNext() ) {
                    eachArg = (String) argIter.next();
                    option.addValue( eachArg );
                    
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
re
tur
n
;
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
    
    /** <p>Retrieve the named {@link Option}<p>
     *
     * @param opt short single-character name of the {@link Option}
     * @return the option represented by opt
     */
    public Option getOption(String opt) {
        if( opt.startsWith( "--" ) ) {
            return (Option) longOpts.get( opt );
        }
        return (Option) shortOpts.get( "-" + opt );
    }
    
    /** <p>Retrieve the named {@link Option}<p>
     *
    * @param longOpt long name of the {@link Option}
    * @return the option represented by longOpt
    */
   /*public Option getOption(String longOpt) {
       return (Option) longOpts.get( longOpt );
    } */
    
    /** <p>Dump state, suitable for debugging.</p>
     *
     * @return Stringified form of this object
     */
    public String toString() {
        buf.append( shortOpts.toString() );
buf.a
ppend(
 " ]
 
[ lo
ng
 "
 );
        buf.append( longOpts );
        buf.append( " ]");
        
        return buf.toString();
 StringBuffer buf = new StringBuffer();
        
 buf.append("[ Options: [ short ");
    }
}
