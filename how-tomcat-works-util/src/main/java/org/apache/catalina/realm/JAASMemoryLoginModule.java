/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/JAASMemoryLoginModule.java,v 1.1 2001/11/13 22:42:31 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2001/11/13 22:42:31 $
 *
 * ====================================================================
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


import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.catalina.Container;
import org.apache.catalina.Realm;
import org.apache.commons.digester.Digester;


/**
 * <p>Implementation of the JAAS <strong>LoginModule</strong> interface,
 * primarily for use in testing <code>JAASRealm</code>.  It utilizes an
 * XML-format data file of username/password/role information identical to
 * that supported by <code>org.apache.catalina.realm.MemoryRealm</code>
 * (except that digested passwords are not supported).</p>
 *
 * <p>This class recognizes the following string-valued options, which are
 * specified in the configuration file (and passed to our constructor in
 * the <code>options</code> argument:</p>
 * <ul>
 * <li><strong>debug</strong> - Set to "true" to get debugging messages
 *     generated to System.out.  The default value is <code>false</code>.</li>
 * <li><strong>pathname</strong> - Relative (to the pathname specified by the
 *     "catalina.base" system property) or absolute pahtname to the
 *     XML file containing our user information, in the format supported by
 *     {@link MemoryRealm}.  The default value matches the MemoryRealm
 *     default.</li>
 * </ul>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This class implements
 * <code>Realm</code> only to satisfy the calling requirements of the
 * <code>GenericPrincipal</code> constructor.  It does not actually perform
 * the functionality required of a <code>Realm</code> implementation.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2001/11/13 22:42:31 $
 */

public class JAASMemoryLoginModule implements LoginModule, Realm {


    // ----------------------------------------------------- Instance Variables


    /**
     * The callback handler responsible for answering our requests.
     */
    protected CallbackHandler callbackHandler = null;


    /**
     * Has our own <code>commit()</code> returned successfully?
     */
    protected boolean committed = false;


    /**
     * Should we log debugging messages?
     */
    protected boolean debug = false;


    /**
     * The configuration information for this <code>LoginModule</code>.
     */
    protected Map options = null;


    /**
     * The absolute or relative pathname to the XML configuration file.
     */
    protected String pathname = "conf/tomcat-users.xml";


    /**
     * The <code>Principal</code> identified by our validation, or
     * <code>null</code> if validation falied.
     */
    protected Principal principal = null;


    /**
     * The set of <code>Principals</code> loaded from our configuration file.
     */
    protected HashMap principals = new HashMap();


    /**
     * The state information that is shared with other configured
     * <code>LoginModule</code> instances.
     */
    protected Map sharedState = null;


    /**
     * The subject for which we are performing authentication.
     */
    protected Subject subject = null;


    // --------------------------------------------------------- Public Methods


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


    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * fails.  This method is called if the <code>LoginContext</code>
     * failed somewhere in the overall authentication chain.
     *
     * @return <code>true</code> if this method succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the abort fails
     */
    public boolean abort() throws LoginException {

        // If our authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Clean up if overall authentication failed
        if (committed)
            logout();
        else {
            committed = false;
            principal = null;
        }
        return (true);

    }


    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * was successful.  This method is called if the <code>LoginContext</code>
     * succeeded in the overall authentication chain.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the commit fails
     */
    public boolean commit() throws LoginException {

        // If authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Add our Principal to the Subject if needed
        if (!subject.getPrincipals().contains(principal))
            subject.getPrincipals().add(principal);
        committed = true;
        return (true);

    }


    /**
     * Initialize this <code>LoginModule</code> with the specified
     * configuration information.
     *
     * @param subject The <code>Subject</code> to be authenticated
     * @param callbackHandler A <code>CallbackHandler</code> for communicating
     *  with the end user as necessary
     * @param sharedState State information shared with other
     *  <code>LoginModule</code> instances
     * @param options Configuration information for this specific
     *  <code>LoginModule</code> instance
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {

        // Save configuration values
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        // Perform instance-specific initialization
        this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (options.get("pathname") != null)
            this.pathname = (String) options.get("pathname");

        // Load our defined Principals
        load();

    }


    /**
     * Phase 1 of authenticating a <code>Subject</code>.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the authentication fails
     */
    public boolean login() throws LoginException {

        // Set up our CallbackHandler requests
        if (callbackHandler == null)
            throw new LoginException("No CallbackHandler specified");
        Callback callbacks[] = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        // Interact with the user to retrieve the username and password
        String username = null;
        String password = null;
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password =
                new String(((PasswordCallback) callbacks[1]).getPassword());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        // Validate the username and password we have received
        principal = null; // FIXME - look up and check password

        // Report results based on success or failure
        if (principal != null) {
            return (true);
        } else {
            throw new
                FailedLoginException("Username or password is incorrect");
        }

    }


    /**
     * Log out this user.
     *
     * @return <code>true</code> in all cases because thie
     *  <code>LoginModule</code> should not be ignored
     *
     * @exception LoginException if logging out failed
     */
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(principal);
        committed = false;
        principal = null;
        return (true);

    }


    // ---------------------------------------------------------- Realm Methods


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {

        return (null);

    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        ;

    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (null);

    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        ;

    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified username, which
     * matches the digest calculated using the given parameters using the
     * method described in RFC 2069; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param digest Digest which has been submitted by the client
     * @param nonce Unique (or supposedly unique) token which has been used
     * for this request
     * @param realm Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest :
     * MD5(Method + ":" + uri)
     */
    public Principal authenticate(String username, String digest,
                                  String nonce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {

        return (null);

    }


    /**
     * Return the Principal associated with the specified chain of X509
     * client certificates.  If there is none, return <code>null</code>.
     *
     * @param certs Array of client certificates, with the first one in
     *  the array being the certificate of the client itself.
     */
    public Principal authenticate(X509Certificate certs[]) {

        return (null);

    }


    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {

        return (false);

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        ;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Load the contents of our configuration file.
     */
    protected void load() {

        // Validate the existence of our configuration file
        File file = new File(pathname);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), pathname);
        if (!file.exists() || !file.canRead()) {
            log("Cannot load configuration file " + file.getAbsolutePath());
            return;
        }

        // Load the contents of our configuration file
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addRuleSet(new MemoryRuleSet());
        try {
            digester.push(this);
            digester.parse(file);
        } catch (Exception e) {
            log("Error processing configuration file " +
                file.getAbsolutePath(), e);
            return;
        }

    }


    /**
     * Log a message.
     *
     * @param message The message to be logged
     */
    protected void log(String message) {

        System.out.print("JAASMemoryLoginModule: ");
        System.out.println(message);

    }


    /**
     * Log a message and associated exception.
     *
     * @param message The message to be logged
     * @param exception The associated exception
     */
    protected void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }


}
