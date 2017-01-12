/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/MemoryUserDatabaseFactory.java,v 1.4 2002/02/03 00:56:58 craigmcc Exp $
 * $Revision: 1.4 $
 * $Date: 2002/02/03 00:56:58 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


package org.apache.catalina.users;


import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;


/**
 * <p>JNDI object creation factory for <code>MemoryUserDatabase</code>
 * instances.  This makes it convenient to configure a user database
 * in the global JNDI resources associated with this Catalina instance,
 * and then link to that resource for web applications that administer
 * the contents of the user database.</p>
 *
 * <p>The <code>MemoryUserDatabase</code> instance is configured based
 * on the following parameter values:</p>
 * <ul>
 * <li><strong>pathname</strong> - Absolute or relative (to the directory
 *     path specified by the <code>catalina.base</code> system property)
 *     pathname to the XML file from which our user information is loaded,
 *     and to which it is stored.  [conf/tomcat-users.xml]</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/02/03 00:56:58 $
 * @since 4.1
 */

public class MemoryUserDatabaseFactory implements ObjectFactory {


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Create and return a new <code>MemoryUserDatabase</code> instance
     * that has been configured according to the properties of the
     * specified <code>Reference</code>.  If you instance can be created,
     * return <code>null</code> instead.</p>
     *
     * @param obj The possibly null object containing location or
     *  reference information that can be used in creating an object
     * @param name The name of this object relative to <code>nameCtx</code>
     * @param nameCtx The context relative to which the <code>name</code>
     *  parameter is specified, or <code>null</code> if <code>name</code>
     *  is relative to the default initial context
     * @param environment The possibly null environment that is used in
     *  creating this object
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws Exception {

        // We only know how to deal with <code>javax.naming.Reference</code>s
        // that specify a class name of "org.apache.catalina.UserDatabase"
        if ((obj == null) || !(obj instanceof Reference)) {
            return (null);
        }
        Reference ref = (Reference) obj;
        if (!"org.apache.catalina.UserDatabase".equals(ref.getClassName())) {
            return (null);
        }

        // Create and configure a MemoryUserDatabase instance based on the
        // RefAddr values associated with this Reference
        MemoryUserDatabase database = new MemoryUserDatabase(name.toString());
        RefAddr ra = null;

        ra = ref.get("pathname");
        if (ra != null) {
            database.setPathname(ra.getContent().toString());
        }

        // Return the configured database instance
        database.open();
        database.save();
        return (database);

    }


}
