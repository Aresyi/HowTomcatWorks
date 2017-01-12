/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/SecurityClassLoad.java,v 1.1 2001/12/30 01:58:20 glenn Exp $
 * $Revision: 1.1 $
 * $Date: 2001/12/30 01:58:20 $
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


package org.apache.catalina.startup;

/**
 * Static class used to preload java classes when using the
 * Java SecurityManager so that the defineClassInPackage
 * RuntimePermission does not trigger an AccessControlException.
 *
 * @author Glenn L. Nielsen
 * @version $Revision: 1.1 $ $Date: 2001/12/30 01:58:20 $
 */

public final class SecurityClassLoad {

    static void securityClassLoad(ClassLoader loader)
        throws Exception {

        if( System.getSecurityManager() == null )
            return;

        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetRequestDispatcher");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetResource");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedGetResourcePaths");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogMessage");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogException");
        loader.loadClass
            (basePackage +
             "core.ApplicationContext$PrivilegedLogThrowable");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedForward");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedInclude");
        loader.loadClass
            (basePackage +
             "core.ContainerBase$PrivilegedAddChild");
        loader.loadClass
            (basePackage +
             "connector.HttpRequestBase$PrivilegedGetSession");
        loader.loadClass
            (basePackage +
             "connector.HttpResponseBase$PrivilegedFlushBuffer");
        loader.loadClass
            (basePackage +
             "loader.WebappClassLoader$PrivilegedFindResource");
        loader.loadClass
            (basePackage + "session.StandardSession");
        loader.loadClass
            (basePackage + "util.CookieTools");
        loader.loadClass
            (basePackage + "util.URL");
        loader.loadClass(basePackage + "util.Enumerator");
        loader.loadClass("javax.servlet.http.Cookie");

    }
}

