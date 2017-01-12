/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/MemoryRealm.java,v 1.13 2002/06/09 02:19:43 remm Exp $
 * $Revision: 1.13 $
 * $Date: 2002/06/09 02:19:43 $
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


package org.apache.catalina.realm;


import java.security.Principal;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.StringManager;
import org.apache.commons.digester.Digester;


/**
 * Simple implementation of <b>Realm</b> that reads an XML file to configure
 * the valid users, passwords, and roles.  The file format (and default file
 * location) are identical to those currently supported by Tomcat 3.X.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>: It is assumed that the in-memory
 * collection representing our defined users (and their roles) is initialized
 * at application startup and never modified again.  Therefore, no thread
 * synchronization is performed around accesses to the principals collection.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.13 $ $Date: 2002/06/09 02:19:43 $
 */

public final class MemoryRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Realm is associated.
     */
    private Container container = null;


    /**
     * The Digester we will use to process in-memory database files.
     */
    private static Digester digester = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info =
        "org.apache.catalina.realm.MemoryRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */

    protected static final String name = "MemoryRealm";


    /**
     * The pathname (absolute or relative to Catalina's current working
     * directory) of the XML file containing our database information.
     */
    private String pathname = "conf/tomcat-users.xml";


    /**
     * The set of valid Principals for this Realm, keyed by user name.
     */
    private HashMap principals = new HashMap();


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return info;

    }


    /**
     * Return the pathname of our XML file containing user definitions.
     */
    public String getPathname() {

        return pathname;

    }


    /**
     * Set the pathname of our XML file containing user definitions.  If a
     * relative pathname is specified, it is resolved against "catalina.base".
     *
     * @param pathname The new pathname
     */
    public void setPathname(String pathname) {

        this.pathname = pathname;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        GenericPrincipal principal =
            (GenericPrincipal) principals.get(username);

        boolean validated = false;
        if (principal != null) {
            if (hasMessageDigest()) {
                // Hex hashes should be compared case-insensitive
                validated = (digest(credentials)
                             .equalsIgnoreCase(principal.getPassword()));
            } else {
                validated = 
                    (digest(credentials).equals(principal.getPassword()));
            }
        }

        if (validated) {
            if (debug >= 2)
                log(sm.getString("memoryRealm.authenticateSuccess", username));
            return (principal);
        } else {
            if (debug >= 2)
                log(sm.getString("memoryRealm.authenticateFailure", username));
            return (null);
        }

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Add a new user to the in-memory database.
     *
     * @param username User's username
     * @param password User's password (clear text)
     * @param roles Comma-delimited set of roles associated with this user
     */
    void addUser(String username, String password, String roles) {

        // Accumulate the list of roles for this user
        ArrayList list = new ArrayList();
        roles += ",";
        while (true) {
            int comma = roles.indexOf(',');
            if (comma < 0)
                break;
            String role = roles.substring(0, comma).trim();
            list.add(role);
            roles = roles.substring(comma + 1);
        }

        // Construct and cache the Principal for this user
        GenericPrincipal principal =
            new GenericPrincipal(this, username, password, list);
        principals.put(username, principal);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a configured <code>Digester</code> to use for processing
     * the XML input file, creating a new one if necessary.
     */
    protected synchronized Digester getDigester() {

        if (digester == null) {
            digester = new Digester();
            digester.setDebug(this.debug);
            digester.setValidating(false);
            digester.addRuleSet(new MemoryRuleSet());
        }
        return (digester);

    }


    /**
     * Return a short name for this Realm implementation.
     */
    protected String getName() {

        return (this.name);

    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {

        GenericPrincipal principal =
            (GenericPrincipal) principals.get(username);
        if (principal != null) {
            return (principal.getPassword());
        } else {
            return (null);
        }

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (Principal) principals.get(username);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate the existence of our database file
        File file = new File(pathname);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), pathname);
        if (!file.exists() || !file.canRead())
            throw new LifecycleException
                (sm.getString("memoryRealm.loadExist",
                              file.getAbsolutePath()));

        // Load the contents of the database file
        if (debug >= 1)
            log(sm.getString("memoryRealm.loadPath",
                             file.getAbsolutePath()));
        Digester digester = getDigester();
        try {
            synchronized (digester) {
                digester.push(this);
                digester.parse(file);
            }
        } catch (Exception e) {
            throw new LifecycleException("memoryRealm.readXml", e);
        }

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // No shutdown activities required

    }


}
