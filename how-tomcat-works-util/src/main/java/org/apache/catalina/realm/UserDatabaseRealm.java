/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/UserDatabaseRealm.java,v 1.8 2002/06/09 02:19:43 remm Exp $
 * $Revision: 1.8 $
 * $Date: 2002/06/09 02:19:43 $
 *
 * ====================================================================
 *
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


package org.apache.catalina.realm;


import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import javax.naming.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of {@link Realm} that is based on an implementation of
 * {@link UserDatabase} made available through the global JNDI resources
 * configured for this instance of Catalina.  Set the <code>resourceName</code>
 * parameter to the global JNDI resources name for the configured instance
 * of <code>UserDatabase</code> that we should consult.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2002/06/09 02:19:43 $
 * @since 4.1
 */

public class UserDatabaseRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>UserDatabase</code> we will use to authenticate users
     * and identify associated roles.
     */
    protected UserDatabase database = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info =
        "org.apache.catalina.realm.UserDatabaseRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "UserDatabaseRealm";


    /**
     * The global JNDI name of the <code>UserDatabase</code> resource
     * we will be utilizing.
     */
    protected String resourceName = "UserDatabase";


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


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
     * Return the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     */
    public String getResourceName() {

        return resourceName;

    }


    /**
     * Set the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     *
     * @param resourceName The new global JNDI name
     */
    public void setResourceName(String resourceName) {

        this.resourceName = resourceName;

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

        // Does a user with this username exist?
        User user = database.findUser(username);
        if (user == null) {
            return (null);
        }

        // Do the credentials specified by the user match?
        // FIXME - Update all realms to support encoded passwords
        boolean validated = false;
        if (hasMessageDigest()) {
            // Hex hashes should be compared case-insensitive
            validated = (digest(credentials)
                         .equalsIgnoreCase(user.getPassword()));
        } else {
            validated =
                (digest(credentials).equals(user.getPassword()));
        }
        if (!validated) {
            if (debug >= 2) {
                log(sm.getString("userDatabaseRealm.authenticateFailure",
                                 username));
            }
            return (null);
        }

        // Construct a GenericPrincipal that represents this user
        if (debug >= 2) {
            log(sm.getString("userDatabaseRealm.authenticateSuccess",
                             username));
        }
        ArrayList combined = new ArrayList();
        Iterator roles = user.getRoles();
        while (roles.hasNext()) {
            Role role = (Role) roles.next();
            String rolename = role.getRolename();
            if (!combined.contains(rolename)) {
                combined.add(rolename);
            }
        }
        Iterator groups = user.getGroups();
        while (groups.hasNext()) {
            Group group = (Group) groups.next();
            roles = group.getRoles();
            while (roles.hasNext()) {
                Role role = (Role) roles.next();
                String rolename = role.getRolename();
                if (!combined.contains(rolename)) {
                    combined.add(rolename);
                }
            }
        }
        return (new GenericPrincipal(this, user.getUsername(),
                                     user.getPassword(), combined));

    }


    // ------------------------------------------------------ Protected Methods


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

        return (null);

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (null);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        try {
            StandardServer server = (StandardServer) ServerFactory.getServer();
            Context context = server.getGlobalNamingContext();
            database = (UserDatabase) context.lookup(resourceName);
        } catch (Throwable e) {
            e.printStackTrace();
            log(sm.getString("userDatabaseRealm.lookup", resourceName), e);
            database = null;
        }
        if (database == null) {
            throw new LifecycleException
                (sm.getString("userDatabaseRealm.noDatabase", resourceName));
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

        // Release reference to our user database
        database = null;

    }


}
