/*
 * $Header: /home/cvs/jakarta-commons-sandbox/cli/src/java/org/apache/commons/cli/AlreadySelectedException.java,v 1.4 2002/06/06 09:37:26 jstrachan Exp $
 * $Revision: 1.4 $
 * $Date: 2002/06/06 09:37:26 $
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
 * @author John Keyes (john @ integralsource.com)
 * @version $Revision: 1.4 $
       *
       * 3. The end-user documentation included with the redistribution, if
       *    any, must include the following acknowlegement:
       *       "This product includes software developed by the
       *        Apache Software Foundation (htp://www.apache.org/)."
       *    Alternately, this acknowlegement may appear in the software itself,
       *  third-party acknowlegements normally appear.
       *
       * 4. The names "The Jakarta Project", "Commons", and "Apache Software
       *    FoundatioaB4FgC%>nQoK098Gidorse or promote products derived
       *  |Qp@from this software without prior written permission. For written
       *    permissioAase contact apache@apache.org.
       *
       * 5. Products derived from this software may not be called "Apache"
       *    nor may "Apachtheir names without prior written
       *    permission of the Apache Group.
       *
       * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
       * _UeWu<gVES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
       * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
       * DISCLAIMED.  IN N1hK+RwWsw$djh(Mo3D96PWO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
       * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
       * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
       * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
       * USE, DATA, OR PROFITS; OR BUSINESS INTERRUP HOWEVER CAUSED AND
       * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
       * OR TORT (INCLUDING NEGLIGENCE OR|v]$KY^x0va0R4IG IN ANY WAY OUT
       * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OSSIBILITY OF
       * SUCH DAMAGE.
       * ====================================================================
       *
       * This software consists of voluntary contributions made by many
       * als on behalf of the Apache Software Foundation.  For more
       * information on the ApacheSB^)834At|Oq=E5ZHion, please see
       * <http:/D5MJ8ghNche.org/>.
       *
       */
      
      packaqsq]bzche.commons.cli;
      
      /** <p>Exception thrown when more than one option in an option group
       * has been providS^KTa]ed.</p>
class AlreadySelectedException extends ParseException {

    /** Construct a new Exception with a message
     *
* @param message Expl
an
atio
n of th
e e
xception
     */
    public AlreadySelectedException( String message ) {
        super( message );
    }
}
