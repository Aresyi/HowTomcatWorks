/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/naming/java/javaURLContextFactory.java,v 1.2 2001/01/25 18:35:35 remm Exp $
 * $Revision: 1.2 $
 * $Date: 2001/01/25 18:35:35 $
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


package org.apache.naming.java;

import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.InitialContextFactory;
import org.apache.naming.SelectorContext;
import org.apache.naming.NamingContext;
import org.apache.naming.ContextBindings;

/**
 * Context factory for the "java:" namespace.
 * <p>
 * <b>Important note</b> : This factory MUST be associated with the "java" URL
 * prefix, which can be done by either :
 * <ul>
 * <li>Adding a 
 * java.naming.factory.url.pkgs=org.apache.catalina.util.naming property
 * to the JNDI properties file</li>
 * <li>Setting an environment variable named Context.URL_PKG_PREFIXES with 
 * its value including the org.apache.catalina.util.naming package name. 
 * More detail about this can be found in the JNDI documentation : 
 * {@link javax.naming.spi.NamingManager#getURLContext(java.lang.String, java.util.Hashtable)}.</li>
 * </ul>
 * 
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2001/01/25 18:35:35 $
 */

public class javaURLContextFactory
    implements ObjectFactory, InitialContextFactory {


    // ----------------------------------------------------------- Constructors


    // -------------------------------------------------------------- Constants


    public static final String MAIN = "initialContext";


    // ----------------------------------------------------- Instance Variables


    /**
     * Initial context.
     */
    protected static Context initialContext = null;


    // --------------------------------------------------------- Public Methods


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Crete a new Context's instance.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        if ((ContextBindings.isThreadBound()) || 
            (ContextBindings.isClassLoaderBound())) {
            return new SelectorContext(environment);
        } else {
            return null;
        }
    }


    /**
     * Get a new (writable) initial context.
     */
    public Context getInitialContext(Hashtable environment)
        throws NamingException {
        if (ContextBindings.isThreadBound() || 
            (ContextBindings.isClassLoaderBound())) {
            // Redirect the request to the bound initial context
            return new SelectorContext(environment, true);
        } else {
            // If the thread is not bound, return a shared writable context
            if (initialContext == null)
                initialContext = new NamingContext(environment, MAIN);
            return initialContext;
        }
    }


}

