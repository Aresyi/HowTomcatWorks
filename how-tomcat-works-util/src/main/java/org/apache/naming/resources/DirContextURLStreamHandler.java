/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/naming/resources/DirContextURLStreamHandler.java,v 1.6 2002/02/11 22:24:28 remm Exp $
 * $Revision: 1.6 $
 * $Date: 2002/02/11 22:24:28 $
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

package org.apache.naming.resources;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.directory.DirContext;

/**
 * Stream handler to a JNDI directory context.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.6 $
 */
public class DirContextURLStreamHandler 
    extends URLStreamHandler {
    
    
    // ----------------------------------------------------------- Constructors
    
    
    public DirContextURLStreamHandler() {
    }
    
    
    public DirContextURLStreamHandler(DirContext context) {
        this.context = context;
    }
    
    
    // -------------------------------------------------------------- Variables
    
    
    /**
     * Bindings class loader - directory context. Keyed by CL id.
     */
    private static Hashtable clBindings = new Hashtable();
    
    
    /**
     * Bindings thread - directory context. Keyed by thread id.
     */
    private static Hashtable threadBindings = new Hashtable();
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Directory context.
     */
    protected DirContext context = null;
    
    
    // ------------------------------------------------------------- Properties
    
    
    // ----------------------------------------------- URLStreamHandler Methods
    
    
    /**
     * Opens a connection to the object referenced by the <code>URL</code> 
     * argument.
     */
    protected URLConnection openConnection(URL u) 
        throws IOException {
        DirContext currentContext = this.context;
        if (currentContext == null)
            currentContext = get();
        return new DirContextURLConnection(currentContext, u);
    }
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Set the java.protocol.handler.pkgs system property.
     */
    public static void setProtocolHandler() {
        String value = System.getProperty(Constants.PROTOCOL_HANDLER_VARIABLE);
        if (value == null) {
            value = Constants.Package;
            System.setProperty(Constants.PROTOCOL_HANDLER_VARIABLE, value);
        } else if (value.indexOf(Constants.Package) == -1) {
            value += "|" + Constants.Package;
            System.setProperty(Constants.PROTOCOL_HANDLER_VARIABLE, value);
        }
    }
    
    
    /**
     * Returns true if the thread or the context class loader of the current 
     * thread is bound.
     */
    public static boolean isBound() {
        return (clBindings.containsKey
                (Thread.currentThread().getContextClassLoader()))
            || (threadBindings.containsKey(Thread.currentThread()));
    }
    
    
    /**
     * Binds a directory context to a class loader.
     */
    public static void bind(DirContext dirContext) {
        ClassLoader currentCL = 
            Thread.currentThread().getContextClassLoader();
        if (currentCL != null)
            clBindings.put(currentCL, dirContext);
    }
    
    
    /**
     * Unbinds a directory context to a class loader.
     */
    public static void unbind() {
        ClassLoader currentCL = 
            Thread.currentThread().getContextClassLoader();
        if (currentCL != null)
            clBindings.remove(currentCL);
    }
    
    
    /**
     * Binds a directory context to a thread.
     */
    public static void bindThread(DirContext dirContext) {
        threadBindings.put(Thread.currentThread(), dirContext);
    }
    
    
    /**
     * Unbinds a directory context to a thread.
     */
    public static void unbindThread() {
        threadBindings.remove(Thread.currentThread());
    }
    
    
    /**
     * Get the bound context.
     */
    public static DirContext get() {

        DirContext result = null;

        Thread currentThread = Thread.currentThread();
        ClassLoader currentCL = currentThread.getContextClassLoader();

        // Checking CL binding
        result = (DirContext) clBindings.get(currentCL);
        if (result != null)
            return result;

        // Checking thread biding
        result = (DirContext) threadBindings.get(currentThread);

        // Checking parent CL binding
        currentCL = currentCL.getParent();
        while (currentCL != null) {
            result = (DirContext) clBindings.get(currentCL);
            if (result != null)
                return result;
            currentCL = currentCL.getParent();
        }

        if (result == null)
            throw new IllegalStateException("Illegal class loader binding");

        return result;

    }
    
    
    /**
     * Binds a directory context to a class loader.
     */
    public static void bind(ClassLoader cl, DirContext dirContext) {
        clBindings.put(cl, dirContext);
    }
    
    
    /**
     * Unbinds a directory context to a class loader.
     */
    public static void unbind(ClassLoader cl) {
        clBindings.remove(cl);
    }
    
    
    /**
     * Get the bound context.
     */
    public static DirContext get(ClassLoader cl) {
        return (DirContext) clBindings.get(cl);
    }
    
    
    /**
     * Get the bound context.
     */
    public static DirContext get(Thread thread) {
        return (DirContext) threadBindings.get(thread);
    }
    
    
}
