/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/JAASRealm.java,v 1.4 2002/06/18 09:14:49 remm Exp $
 * $Revision: 1.4 $
 * $Date: 2002/06/18 09:14:49 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implmentation of <b>Realm</b> that authenticates users via the <em>Java
 * Authentication and Authorization Service</em> (JAAS).  JAAS support requires
 * either JDK 1.4 (which includes it as part of the standard platform) or
 * JDK 1.3 (with the plug-in <code>jaas.jar</code> file).</p>
 *
 * <p>The value configured for the <code>appName</code> property is passed to
 * the <code>javax.security.auth.login.LoginContext</code> constructor, to
 * specify the <em>application name</em> used to select the set of relevant
 * <code>LoginModules</code> required.</p>
 *
 * <p>The JAAS Specification describes the result of a successful login as a
 * <code>javax.security.auth.Subject</code> instance, which can contain zero
 * or more <code>java.security.Principal</code> objects in the return value
 * of the <code>Subject.getPrincipals()</code> method.  However, it provides
 * no guidance on how to distinguish Principals that describe the individual
 * user (and are thus appropriate to return as the value of
 * request.getUserPrincipal() in a web application) from the Principal(s)
 * that describe the authorized roles for this user.  To maintain as much
 * independence as possible from the underlying <code>LoginMethod</code>
 * implementation executed by JAAS, the following policy is implemented by
 * this Realm:</p>
 * <ul>
 * <li>The JAAS <code>LoginModule</code> is assumed to return a
 *     <code>Subject with at least one <code>Principal</code> instance
 *     representing the user himself or herself, and zero or more separate
 *     <code>Principals</code> representing the security roles authorized
 *     for this user.</li>
 * <li>On the <code>Principal</code> representing the user, the Principal
 *     name is an appropriate value to return via the Servlet API method
 *     <code>HttpServletRequest.getRemoteUser()</code>.</li>
 * <li>On the <code>Principals</code> representing the security roles, the
 *     name is the name of the authorized security role.</li>
 * <li>This Realm will be configured with two lists of fully qualified Java
 *     class names of classes that implement
 *     <code>java.security.Principal</code> - one that identifies class(es)
 *     representing a user, and one that identifies class(es) representing
 *     a security role.</li>
 * <li>As this Realm iterates over the <code>Principals</code> returned by
 *     <code>Subject.getPrincipals()</code>, it will identify the first
 *     <code>Principal</code> that matches the "user classes" list as the
 *     <code>Principal</code> for this user.</li>
 * <li>As this Realm iterates over the <code>Princpals</code> returned by
 *     <code>Subject.getPrincipals()</code>, it will accumulate the set of
 *     all <code>Principals</code> matching the "role classes" list as
 *     identifying the security roles for this user.</li>
 * <li>It is a configuration error for the JAAS login method to return a
 *     validated <code>Subject</code> without a <code>Principal</code> that
 *     matches the "user classes" list.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/06/18 09:14:49 $
 */

public class JAASRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The application name passed to the JAAS <code>LoginContext</code>,
     * which uses it to select the set of relevant <code>LoginModules</code>.
     */
    protected String appName = "Tomcat";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
        "org.apache.catalina.realm.JAASRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "JAASRealm";


    /**
     * The list of role class names, split out for easy processing.
     */
    protected ArrayList roleClasses = new ArrayList();


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The set of user class names, split out for easy processing.
     */
    protected ArrayList userClasses = new ArrayList();


    // ------------------------------------------------------------- Properties

    
    /**
     * setter for the appName member variable
     */
    public void setAppName(String name) {
        appName = name;
    }
    
    /**
     * getter for the appName member variable
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Comma-delimited list of <code>javax.security.Principal</code> classes
     * that represent security roles.
     */
    protected String roleClassNames = null;

    public String getRoleClassNames() {
        return (this.roleClassNames);
    }

    public void setRoleClassNames(String roleClassNames) {
        this.roleClassNames = roleClassNames;
        roleClasses.clear();
        String temp = this.roleClassNames;
        if (temp == null) {
            return;
        }
        while (true) {
            int comma = temp.indexOf(',');
            if (comma < 0) {
                break;
            }
            roleClasses.add(temp.substring(0, comma).trim());
            temp = temp.substring(comma + 1);
        }
        temp = temp.trim();
        if (temp.length() > 0) {
            roleClasses.add(temp);
        }
    }


    /**
     * Comma-delimited list of <code>javax.security.Principal</code> classes
     * that represent individual users.
     */
    protected String userClassNames = null;

    public String getUserClassNames() {
        return (this.userClassNames);
    }

    public void setUserClassNames(String userClassNames) {
        this.userClassNames = userClassNames;
        userClasses.clear();
        String temp = this.userClassNames;
        if (temp == null) {
            return;
        }
        while (true) {
            int comma = temp.indexOf(',');
            if (comma < 0) {
                break;
            }
            userClasses.add(temp.substring(0, comma).trim());
            temp = temp.substring(comma + 1);
        }
        temp = temp.trim();
        if (temp.length() > 0) {
            userClasses.add(temp);
        }
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return null (don't authenticate). This
     * event is also logged, and the connection will be closed so that
     * a subsequent request will automatically re-open it.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        // Establish a LoginContext to use for authentication
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext
                (appName, new JAASCallbackHandler(this, username,
                                                  credentials));
        } catch (LoginException e) {
            log(sm.getString("jaasRealm.loginException", username), e);
            return (null);
        }

        // Negotiate a login via this LoginContext
        Subject subject = null;
        try {
            loginContext.login();
            subject = loginContext.getSubject();
            if (subject == null) {
                if (debug >= 2)
                    log(sm.getString("jaasRealm.failedLogin", username));
                return (null);
            }
        } catch (AccountExpiredException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.accountExpired", username));
            return (null);
        } catch (CredentialExpiredException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.credentialExpired", username));
            return (null);
        } catch (FailedLoginException e) {
            if (debug >= 2)
                log(sm.getString("jaasRealm.failedLogin", username));
            return (null);
        } catch (LoginException e) {
            log(sm.getString("jaasRealm.loginException", username), e);
            return (null);
        }

        // Return the appropriate Principal for this authenticated Subject
        Principal principal = createPrincipal(subject);
        if (principal == null) {
            log(sm.getString("jaasRealm.authenticateError", username));
            return (null);
        }
        if (debug >= 2) {
            log(sm.getString("jaasRealm.authenticateSuccess", username));
        }
        return (principal);

    }


    // -------------------------------------------------------- Package Methods


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


    /**
     * Construct and return a <code>java.security.Principal</code> instance
     * representing the authenticated user for the specified Subject.  If no
     * such Principal can be constructed, return <code>null</code>.
     *
     * @param subject The Subject representing the logged in user
     */
    protected Principal createPrincipal(Subject subject) {
        // Prepare to scan the Principals for this Subject
        String username = null;
        String password = null; // Will not be carried forward
        ArrayList roles = new ArrayList();

        // Scan the Principals for this Subject
        Iterator principals = subject.getPrincipals().iterator();
        while (principals.hasNext()) {
            Principal principal = (Principal) principals.next();
            String principalClass = principal.getClass().getName();
            if ((username == null) && userClasses.contains(principalClass)) {
                username = principal.getName();
            }
            if (roleClasses.contains(principalClass)) {
                roles.add(principal.getName());
            }
        }

        // Create the resulting Principal for our authenticated user
        if (username != null) {
            return (new GenericPrincipal(this, username, password, roles));
        } else {
            return (null);
        }

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     *
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public void start() throws LifecycleException {

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

    }


}
