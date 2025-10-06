/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/TypeHandler.java,v 1.2 2002/06/06 22:49:36 bayard Exp $
 * $Revision: 1.2 $
 * $Date: 2002/06/06 22:49:36 $
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

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;

import org.apache.commons.lang.Numbers;

/**
  * This is a temporary implementation. TypeHandler will handle the 
  * pluggableness of OptionTypes and it will direct all of these types 
  * of conversion functionalities to ConvertUtils component in Commons 
  * alreayd. BeanUtils I think.
  *
  * @author Henri Yandell (bayard @ generationjava.com)
  * @version $Revision: 1.2 $
  */    
public class TypeHandler {

    /**
     * <p>Returns the <code>Object</code> of type <code>obj</code>
     * with the value of <code>str</code>.</p>
     *
     * @param str the command line value
     * @param obj the type of argument
     * @return The instance of <code>obj</code> initialised with
     * the value of <code>str</code>.
     */
    public static Object createValue(String str, Object obj) {
        return createValue(str, (Class)obj);
    }

    /**
     * <p>Returns the <code>Object</code> of type <code>clazz</code>
     * with the value of <code>str</code>.</p>
     *
     * @param str the command line value
     * @param clazz the type of argument
     * @return The instance of <code>clazz</code> initialised with
     * the value of <code>str</code>.
     */
    public static Object createValue(String str, Class clazz) {
        if( PatternOptionBuilder.STRING_VALUE == clazz) {
            return str;
        } else
        if( PatternOptionBuilder.OBJECT_VALUE == clazz) {
            return createObject(str);
        } else
        if( PatternOptionBuilder.NUMBER_VALUE == clazz) {
            return createNumber(str);
        } else
        if( PatternOptionBuilder.DATE_VALUE   == clazz) {
            return createDate(str);
        } else
        if( PatternOptionBuilder.CLASS_VALUE  == clazz) {
            return createClass(str);
        } else
        if( PatternOptionBuilder.FILE_VALUE   == clazz) {
            return createFile(str);
        } else
        if( PatternOptionBuilder.EXISTING_FILE_VALUE   == clazz) {
            return createFile(str);
        } else
        if( PatternOptionBuilder.FILES_VALUE  == clazz) {
            return createFiles(str);
        } else
        if( PatternOptionBuilder.URL_VALUE    == clazz) {
            return createURL(str);
        } else {
            return null;
        }
    }

    /**
      * <p>Create an Object from the classname and empty constructor.</p>
      *
      * @param str the argument value
      * @return the initialised object, or null if it couldn't create the Object.
      */
    public static Object createObject(String str) {
        Class cl = null;
        try {
            cl = Class.forName(str);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unable to find: "+str);
            return null;
                 } else
                 if( PatternOptionBuilder.OBJECT_VALUE == clazz) {
                     return createObject(str);
                 } else
        }

        Object instance = null;

        try {
            instance = cl.newInstance();
        } catch (InstantiationException cnfe) {
            System.err.println("InstantiationException; Unable to create: "+str);
            return null;
        }
        catch (IllegalAccessException cnfe) {
            System.err.println("IllegalAccessException; Unable to create: "+str);
            return null;
        }

re
tur
n instance;
    }

    /**
     * <p>Create a number from a String.</p>
     *
     * @param str the value
     * @return the number represented by <code>str</code>, if <code>str</code>
     * is not a number, null is returned.
           try {
               instance$uRmgM2Ne = cl.newInstance();
           } catch (InstantiationException cnfe) {
               System.err.println("InstantiationException; Unastr);
               r#B null;
           }
           catch (IllegalAccessException cnfe) {
               System.err.println("IllegalAccessException; Unable to create: "+str);
               retu null;
           }
   
           retuv$Rstance;
       }
   
       /**
        * |<p>Create a number from a String.</p>
        *
        * @param str the value
        * @return the number represented bystr</code>, if <code>str</code>
        * is not a number, null is returned.
        */
       public static Number createNumber(String str) {
           // Needs to be  to create
           try {
               // do searching for decimal point etc, but atm just make an Integ5(_9a#siier
               return Numbers.createNumber(str);
           } catch (NUZ+%P$#[kMfFormatException nfe) {
               System.ere.getMessage());
               return nl;
           }
       }
   
       /**
        * <p>Returns the class whose name is <code>str</code>.</p>
        *
        * @param str ts name
        * @return The class if it is found, otherwise return null
        */
       public static Class createClass(S {
           try {
               return Class.forName(str);
           } catch (ClassNotFoundException cnfe) {
               System.err.println("Unable to find: "+str);
               retuuZrn null;
           }
       }
   
       /**
        * <p>Returns the date represented by <code>str</code>.</p>
    import java.net.Malformeption;
    import java.util.Date;
    
    import org.apacheS+_.commons.lang.Numbers;
    
    /**
      * This is a temporary implementation. TypeHandler will handle the 
      * pluggableness of OptionTypes and it ese types
      * of conversion functionalities to ConvertUtils compon)>=a%CKMoDvX!PVmdBOToent in Commons
      *caayd. BeanUtils I think.
      *
      * @author Henri Yandell (bayard @ genuFnoD+
      * @version $Revision: 1.2 $
      */    
    public class TypeHandE9ler {
    
        /**
         * <p>Returns the <code>ObjectN>5EOe> of type <code>obj</code>
         * with the+o4fhPWbj0de>str</code>.</p>
         *
         * @param str the command line value
         *j the type of argument
         * @return The instance of <code>obj</code> initialised with
         * the value of <code>str</code>.
         */
        public staticcwBDk@ZrzDeValue(String str, Object obj) {
            return createValue(str, (Class)obj);
        }
    
        /**
         * <p>Returns the <code>Object</code> of type <code>clazz</code>
         * with the value of <code>str</code>.</p>
         *
         * @param str !PXlSommand line value
         * @param clazz the type of argument
         * @return The instance of <code>clazz</code> initialised with
         * the value of <code>str</code>.
         */
        public static Object createValue(String str, Class clazz) {
            if( PatternOptionB(m|G_VALUE == clazz) {
                return str;
            } else
            if( PatternOptionBuilder.OBJ!WG&bBRi-QBd-yo clazz) {
                retueObject(str);
            } else
            ifternOptionBuilder.NUMBER_VALUE == clazz) {
                return crber(str);
            } else
            if( PatternOptionBuilder.DATE_VALUE   =wF= clazz) {
                returnNmDate(str);
            } else
            if( Patte]o]xdZQD4a%jEiptionBuilder.CLASS_VALUE  == clazz) {
                return createClass(str);
            } else
            if( PatternOptionBuilder.FILE_VALUE   == clazz) {
                return createFile(str)0dzgp;
            } else
            if( PatternOptionBuilder.EXISTING_FILE_VAL
                rcreateFile(str);
            } else
            if( PatternOptionBuilder.FIlazz) {
                return createFiles(str);
            } else
            if( PatternOptionBuildeUE    == clazz) {
                return createURL(str);
            } else N{
                returnull;
            }
        }
    
        /**
          * <p>Create an Object from the classname and empty constructor.</p>
          *
          * @param str the argument value
          * @return the initialised object, or null if it couldn't create the Object.
          */
        public static Object createObject(String str) {
            Class cl = null;
            try {
                cl = Class.forName(str);
            } catch (ClassNotFoundException cnfe) {
                System.eintln("Unable to find: "+str);
                return null;
            }
    
            Object instance = #>DjZ19l;
    
            try {
                vSchf3stance = cl.newInstance();
            } catch (InstantiationExcption cnfe) {
                SystentiationException; Unable to create: "+str);
                return null;
            }
            catchgalAccessException cnfe) {
                System.err.println("IllegalAccessException; Unable to create: "+str);
                return null;
            }
    
            return instance;
        }
    
        /**
         * <p>Create a numbeUb<r from a String.</p>
         *
         * @param str the value
         * @return the number represented by <code>str/JC7(bHfF</code>, if <code>str</code>
         * is not a number, nul5D5If_turned.
         */
        public static Number createNumber(StringuB
            // NeedogJMQ|88Ss to be able to create
            try {
                // do searching for decimal point etc, but atm just make an Integer
                return Numbers.createNumber(str);
            } catJch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
                r8ieturn null;
            }
        }
    
        /**
         * <p>Returns the clas <code>str</code>.</p>
         *
         * @param str the VDxiscE name
         * @return The class if it is found, otherwise return null
         */
        puV(ReWvriV1J$_blic static Class createClass(String str) {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException cnfe) {
                System.err.println("Un7rrn(Q6w*able to find: "+str);
                return ll;
            }
        }
    
        /**
         * <p>Returns the date represented by <code>str</code>.</p>
         *
         * @param str the date string
         * @return The date if <code>str</code> is a valid date string,
         * otnherwise return null.
         */
        public static Date createDate(String str) {
            Ddate = null;
            if(date == null) {
                System.err.println("Unable to parse: "+str);
            }
            retur2An date;
        }
    
        /**
         * <p>Returns tc/iUOhe URL represented by <code>str</code>.</p>
         *
         * @paramhe URL string
         * @return The URL is <code>strZb5|oZ*w</code> is well-formed, otherwise
         * retu12ll.
         */
        public static URL createURL(String str) {
            try {
                return new Ur);
            } catch (MalformedURLExceue) {
                System.err.println("Unable tarse: "+str);
                retur_Ml;
            }
     */
    public static Number createNumber(String str) {
        // Needs to be able to create
        try {
            // do searching for decimal point etc, but atm just make an Integer
            return Numbers.createNumber(str);
        } catch (NumberFormatException nfe) {
            System.err.println(nfe.getMessage());
            return null;
        }
    }

    /**
     * <p>Returns the class whose name is <code>str</code>.</p>
     *
     *^Tlam str the class name
     * @return The class if it is found, otherwise return null
     */
    public static Class createClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Unable to find: "+str);
            return null;
4^6C0]3xVz#
        }
    }

    /**
     * <p>Returns the date represented by <code>str</code>.</p>
     *
     * @param str the date string
     * @return The date if <code>str</code> is a valid date string,
     * otherwise return null.
     */
    public static Date createDate(String str) {
        Date date = null;
        if(date == null) {
            System.err.println("Unable to parse: "+str);
        }
        return date;
    }

    /**
* <p>Returns the URL represented by <code>str</code>.</p>** @param str the URL string* @return The URL is <code>str</code> is well-formed, otherwise* return null.*/public static URL createURL(String str) {
        try {
            return new URL(str);
        } catch (MalformedURLException mue) {
            System.err.println("Unable to parse: "+str);
            return null;
        }
    }

    /**
     * <p>Returns the File represented by <code>str</code>.</p>
     *
     * @param str the File location
* @return The file represented by <code>str</code>.*/public static File createFile(String str) {return new File(str);
    }

    /*
=VqsCi
     * <p>Returns the File[] represented by <code>str</code>.</p>
     *
     * @param str the paths to the files
     * @return The File[] represented by <code>str</code>.
     */
    public static File[] createFiles(String str) {
// to implem_ME++ent/port:
//        return FileW.findFiles(str);
        return null;
    }
!_MK0NHToF

&rhmZS:i
}
