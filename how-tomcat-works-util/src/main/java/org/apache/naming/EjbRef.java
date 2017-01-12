/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/naming/EjbRef.java,v 1.2 2001/03/22 17:20:04 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2001/03/22 17:20:04 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.naming;

import javax.naming.Reference;
import javax.naming.Context;
import javax.naming.StringRefAddr;

/**
 * Represents a reference address to an EJB.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2001/03/22 17:20:04 $
 */

public class EjbRef
    extends Reference {


    // -------------------------------------------------------------- Constants


    /**
     * Default factory for this reference.
     */
    public static final String DEFAULT_FACTORY = 
        org.apache.naming.factory.Constants.DEFAULT_EJB_FACTORY;


    /**
     * EJB type address type.
     */
    public static final String TYPE = "type";


    /**
     * Remote interface classname address type.
     */
    public static final String REMOTE = "remote";


    /**
     * Link address type.
     */
    public static final String LINK = "link";


    // ----------------------------------------------------------- Constructors


    /**
     * EJB Reference.
     * 
     * @param ejbType EJB type
     * @param home Home interface classname
     * @param remote Remote interface classname
     * @param link EJB link
     */
    public EjbRef(String ejbType, String home, String remote, String link) {
        this(ejbType, home, remote, link, null, null);
    }


    /**
     * EJB Reference.
     * 
     * @param ejbType EJB type
     * @param home Home interface classname
     * @param remote Remote interface classname
     * @param link EJB link
     */
    public EjbRef(String ejbType, String home, String remote, String link,
                  String factory, String factoryLocation) {
        super(home, factory, factoryLocation);
        StringRefAddr refAddr = null;
        if (ejbType != null) {
            refAddr = new StringRefAddr(TYPE, ejbType);
            add(refAddr);
        }
        if (remote != null) {
            refAddr = new StringRefAddr(REMOTE, remote);
            add(refAddr);
        }
        if (link != null) {
            refAddr = new StringRefAddr(LINK, link);
            add(refAddr);
        }
    }


    // ----------------------------------------------------- Instance Variables


    // -------------------------------------------------------- RefAddr Methods


    // ------------------------------------------------------ Reference Methods


    /**
     * Retrieves the class name of the factory of the object to which this 
     * reference refers.
     */
    public String getFactoryClassName() {
        String factory = super.getFactoryClassName();
        if (factory != null) {
            return factory;
        } else {
            factory = System.getProperty(Context.OBJECT_FACTORIES);
            if (factory != null) {
                return null;
            } else {
                return DEFAULT_FACTORY;
            }
        }
    }


    // ------------------------------------------------------------- Properties


}
