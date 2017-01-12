/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/naming/ContextBindings.java,v 1.7 2002/05/31 02:55:21 remm Exp $
 * $Revision: 1.7 $
 * $Date: 2002/05/31 02:55:21 $
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

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.Context;

/**
 * Handles the associations :
 * <ul>
 * <li>Catalina context name with the NamingContext</li>
 * <li>Calling thread with the NamingContext</li>
 * </ul>
 *
 * @author Remy Maucherat
 * @version $Revision: 1.7 $ $Date: 2002/05/31 02:55:21 $
 */

public class ContextBindings {


    // -------------------------------------------------------------- Variables


    /**
     * Bindings name - naming context. Keyed by name.
     */
    private static Hashtable contextNameBindings = new Hashtable();


    /**
     * Bindings thread - naming context. Keyed by thread id.
     */
    private static Hashtable threadBindings = new Hashtable();


    /**
     * Bindings thread - name. Keyed by thread id.
     */
    private static Hashtable threadNameBindings = new Hashtable();


    /**
     * Bindings class loader - naming context. Keyed by CL id.
     */
    private static Hashtable clBindings = new Hashtable();


    /**
     * Bindings class loader - name. Keyed by CL id.
     */
    private static Hashtable clNameBindings = new Hashtable();


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Binds a context name.
     *
     * @param name Name of the context
     * @param context Associated naming context instance
     */
    public static void bindContext(Object name, Context context) {
        bindContext(name, context, null);
    }


    /**
     * Binds a context name.
     *
     * @param name Name of the context
     * @param context Associated naming context instance
     * @param token Security token
     */
    public static void bindContext(Object name, Context context,
                                   Object token) {
        if (ContextAccessController.checkSecurityToken(name, token))
            contextNameBindings.put(name, context);
    }


    /**
     * Unbind context name.
     *
     * @param name Name of the context
     */
    public static void unbindContext(Object name) {
        unbindContext(name, null);
    }


    /**
     * Unbind context name.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void unbindContext(Object name, Object token) {
        if (ContextAccessController.checkSecurityToken(name, token))
            contextNameBindings.remove(name);
    }


    /**
     * Retrieve a naming context.
     *
     * @param name Name of the context
     */
    static Context getContext(Object name) {
        return (Context) contextNameBindings.get(name);
    }


    /**
     * Binds a naming context to a thread.
     *
     * @param name Name of the context
     */
    public static void bindThread(Object name)
        throws NamingException {
        bindThread(name, null);
    }


    /**
     * Binds a naming context to a thread.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void bindThread(Object name, Object token)
        throws NamingException {
        if (ContextAccessController.checkSecurityToken(name, token)) {
            Context context = (Context) contextNameBindings.get(name);
            if (context == null)
                throw new NamingException
                    (sm.getString("contextBindings.unknownContext", name));
            threadBindings.put(Thread.currentThread(), context);
            threadNameBindings.put(Thread.currentThread(), name);
        }
    }


    /**
     * Unbinds a naming context to a thread.
     *
     * @param name Name of the context
     */
    public static void unbindThread(Object name) {
        unbindThread(name, null);
    }


    /**
     * Unbinds a naming context to a thread.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void unbindThread(Object name, Object token) {
        if (ContextAccessController.checkSecurityToken(name, token)) {
            threadBindings.remove(Thread.currentThread());
            threadNameBindings.remove(Thread.currentThread());
        }
    }


    /**
     * Retrieves the naming context bound to a thread.
     */
    public static Context getThread()
        throws NamingException {
        Context context =
            (Context) threadBindings.get(Thread.currentThread());
        if (context == null)
            throw new NamingException
                (sm.getString("contextBindings.noContextBoundToThread"));
        return context;
    }


    /**
     * Retrieves the naming context name bound to a thread.
     */
    static Object getThreadName()
        throws NamingException {
        Object name = threadNameBindings.get(Thread.currentThread());
        if (name == null)
            throw new NamingException
                (sm.getString("contextBindings.noContextBoundToThread"));
        return name;
    }


    /**
     * Tests if current thread is bound to a context.
     */
    public static boolean isThreadBound() {
        return (threadBindings.containsKey(Thread.currentThread()));
    }


    /**
     * Binds a naming context to a class loader.
     *
     * @param name Name of the context
     */
    public static void bindClassLoader(Object name)
        throws NamingException {
        bindClassLoader(name, null);
    }


    /**
     * Binds a naming context to a thread.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void bindClassLoader(Object name, Object token)
        throws NamingException {
        bindClassLoader
            (name, token, Thread.currentThread().getContextClassLoader());
    }


    /**
     * Binds a naming context to a thread.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void bindClassLoader(Object name, Object token,
                                       ClassLoader classLoader)
        throws NamingException {
        if (ContextAccessController.checkSecurityToken(name, token)) {
            Context context = (Context) contextNameBindings.get(name);
            if (context == null)
                throw new NamingException
                    (sm.getString("contextBindings.unknownContext", name));
            clBindings.put(classLoader, context);
            clNameBindings.put(classLoader, name);
        }
    }


    /**
     * Unbinds a naming context to a class loader.
     *
     * @param name Name of the context
     */
    public static void unbindClassLoader(Object name) {
        unbindClassLoader(name, null);
    }


    /**
     * Unbinds a naming context to a class loader.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void unbindClassLoader(Object name, Object token) {
        unbindClassLoader(name, token,
                          Thread.currentThread().getContextClassLoader());
    }


    /**
     * Unbinds a naming context to a class loader.
     *
     * @param name Name of the context
     * @param token Security token
     */
    public static void unbindClassLoader(Object name, Object token,
                                         ClassLoader classLoader) {
        if (ContextAccessController.checkSecurityToken(name, token)) {
            Object n = clNameBindings.get(classLoader);
            if (!(n.equals(name))) {
                return;
            }
            clBindings.remove(classLoader);
            clNameBindings.remove(classLoader);
        }
    }


    /**
     * Retrieves the naming context bound to a class loader.
     */
    public static Context getClassLoader()
        throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Context context = null;
        do {
            context = (Context) clBindings.get(cl);
            if (context != null) {
                return context;
            }
        } while ((cl = cl.getParent()) != null);
        throw new NamingException
            (sm.getString("contextBindings.noContextBoundToCL"));
    }


    /**
     * Retrieves the naming context name bound to a class loader.
     */
    static Object getClassLoaderName()
        throws NamingException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Object name = null;
        do {
            name = clNameBindings.get(cl);
            if (name != null) {
                return name;
            }
        } while ((cl = cl.getParent()) != null);
        throw new NamingException
            (sm.getString("contextBindings.noContextBoundToCL"));
    }


    /**
     * Tests if current class loader is bound to a context.
     */
    public static boolean isClassLoaderBound() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        do {
            if (clBindings.containsKey(cl)) {
                return true;
            }
        } while ((cl = cl.getParent()) != null);
        return false;
    }


}

