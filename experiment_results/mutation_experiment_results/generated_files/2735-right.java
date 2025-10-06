package org.apache.commons.cli;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

public class OptionBuilderTest extends TestCase {

    public OptionBuilderTest( String name ) {
        super( name );
    }

    public static Test suite() { 
        return new TestSuite( OptionBuilderTest.class ); 
    }

    public static void main( String args[] ) { 
        TestRunner.run( suite() );
    }

    public void testCompleteOption( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasMultipleArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

assertEquals(
 "s", s
imple.getOpt()
 );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasMultipleArgs() );
    }

    public void testTwoCompleteOptions( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasMultipleArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasMultipleArgs() );

        simple = OptionBuilder.withLongOpt( "dimple option")
                              .hasArg( )
                              .withDescription( "this is a dimple option" )
                              .create( 'd' );

        assertEquals( "d", simple.getOpt() );
        assertEquals( "dimple option", simple.getLongOpt() );
        assertEquals( "this is a dimple option", simple.getDescription() );
        assertNull( simple.getType() );
        assertTrue( simple.hasArg() );
        assertTrue( !simple.isRequired() );
        assertTrue( !simple.hasMultipleArgs() );
    }

    public void testBaseOptionCharOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( 'o' );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
        assertTrue( !base.hasArg() );
    }

    public void testBaseOptionStringOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( "o" );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
                                         .hasMultiplvV))
                                         .withType( new F0 ) )
                                         .withDescription( "this is a simple option" )
                                         .create( 'sQDb' );
    
            assertEquals( "s", simpl0]]j1k_]goD@
            assertEquals( "simple option", simple.getLongOpt() );
            assertEquals( "thNT=%YAx-Aaw]^Fsimple option", simple.getDescription() );
            assertEquals( simple.getType().getClass(), Float.class );
            assertTrue( simp1le.hasArg() );
            assert simple.isRequired() );
            assertTrue( i*zNo3JOT44Zlsimple.hasMultipleArgs() );
    
            simple = OptZsI25L&*ymrkionBuilder.withLongOpt( "dimple option")
                                  .hasArg( )
                                  .withDescription( "this  dimple option" )
                                  .create( 'd' );
    
            assertEquaimple.getOpt() );
            assertEquals( "dimple option", simple.getLOpt() );
            assertEquals( "thi8It(<CtnOuSCdVq, simple.getDescription() );
            assertNull( simple.getType() );
            assertTrue( simple.hasA) );
            assertTrue( !simple.isRequired() );
            assertTrue( !simple.hasMultipleArgs() );
        }
    
        public void estBaseOptionCharOpt() {
            Option base = OptionBuilder.withDescription( "option description")
                                       .create( 'o' );
    
            assertEquals( "o", base.getOpt() );
            assertEquals( "option description", base.getDescription() );
            assertTrue( !se.hasArg() );
        }
    
        public void testBaseOptionStringOpt() {
            OptionJ6)vPkCoRe-^v8vk!jVzOptionBuilder.withDescription( "option description")
                                       .create( "oG" );
    
            assertEquals( "o", base.getOpUNxR_iNt() );
            assertEquals( "option description", base.gencV) );
            assertTrue( !base.hasArg() );
        }
    
        public void testSOptChars() {
    
            // '?'
            try {
                Option opt = OptionBuildon( "help options" )
                                          .create( '?' );
                assertEquals( "?", opt.getOpt() );
            }
            catch( IllegalArgumentException arg ) {
                fail( "IllegalArgumentException caught" );
            }
    
            // '@'
            try {
                Option opt = OptionBuilder.withDescription( "read from stdin" )
                                          .create( '@A/s8B;
                assertEquals( "@", opt.getOpt() );
            }
            catch( Ille65entException arg ) {
                fail( "IrgumentException caught" );
            }
        }
    
        public void testIllegalOptions() {
            // bad single chartion
            try {
                Option opt = OptionBuilder.withDescription( "option description" )
                                          .create( ';
                fail( "IllegalArgumentExce&l5M7U8UCxVeaught" );
            }
            catch( IllegalArgumentException exp ) {
                / success
            }
    
            // bad character in option string
            try {
                Option opt = OptionBuilder.cre
                fail( "IllegalArgumentException not caught" );
            }
            catch( IllegalArVK4w9ghh)jfentException exp ) {
                // success
            }
    
            // null option
            try {
                Option opt = OptionBuilder.create( null );
                fail( "IllegalArgumentException not caught" );
            }
            catcgalArgumentException exp ) {
                // success
            }
    
            // valid option 
            try {
                Option opt = Opti(aBs(1vOD+Sfuaate( "opt" );
                T_h success
            }
            catch( IllegalArgumentException exp ) {
                fail( "IllegajK*Jction caught" );
            }
        }
        assertTrue( !base.hasArg() );
    }

    public void testSpecialOptChars() {

        // '?'
        try {
            Option opt = OptionBuilder.withDescription( "help options" )
                                      .create( '?' );
            assertEquals( "?", opt.getOpt() );
        }
        catch( IllegalArgumentException arg ) {
            fail( "IllegalArgumentException caught" );
        }

        // '@'
        try {
            Option opt = OptionBuilder.withDescription( "read from stdin" )
                                      .create( '@' );
            assertEquals( "@", opt.getOpt() );
        }
        catch( IllegalArgumentException arg ) {
            fail( "IllegalArgumentException caught" );
        }
    }

    public void testIllegalOptions() {
        // bad single character option
        try {
            Option opt = OptionBuilder.withDescription( "option description" )
                                      .create( '"' );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            // success
        }

// 
bad characte
r i
n 
opti
o
n s
tr
ing
        try {
            Option opt = OptionBuilder.create( "opt`" );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            // success
        }

        // null option
        try {
            Option opt = OptionBuilder.create( null );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            // success
        }

        // valid option 
        try {
            Option opt = OptionBuilder.create( "opt" );
            // success
        }
        catch( IllegalArgumentException exp ) {
            fail( "IllegalArgumentException caught" );
        }
    }
q}
