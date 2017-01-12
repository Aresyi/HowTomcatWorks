/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/RealmBase.java,v 1.12 2002/06/09 02:19:43 remm Exp $
 * $Revision: 1.12 $
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


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.util.HexUtils;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.MD5Encoder;


/**
 * Simple implementation of <b>Realm</b> that reads an XML file to configure
 * the valid users, passwords, and roles.  The file format (and default file
 * location) are identical to those currently supported by Tomcat 3.X.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2002/06/09 02:19:43 $
 */

public abstract class RealmBase
    implements Lifecycle, Realm {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Realm is associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Digest algorithm used in storing passwords in a non-plaintext format.
     * Valid values are those accepted for the algorithm name by the
     * MessageDigest class, or <code>null</code> if no digesting should
     * be performed.
     */
    protected String digest = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
        "org.apache.catalina.realm.RealmBase/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The MessageDigest object for digesting user credentials (passwords).
     */
    protected MessageDigest md = null;


    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder md5Encoder = new MD5Encoder();


    /**
     * MD5 message digest provider.
     */
    protected static MessageDigest md5Helper;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    protected boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * Should we validate client certificate chains when they are presented?
     */
    protected boolean validate = true;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);

    }

    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the digest algorithm  used for storing credentials.
     */
    public String getDigest() {

        return digest;

    }


    /**
     * Set the digest algorithm used for storing credentials.
     *
     * @param digest The new digest algorithm
     */
    public void setDigest(String digest) {

        this.digest = digest;

    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return info;

    }


    /**
     * Return the "validate certificate chains" flag.
     */
    public boolean getValidate() {

        return (this.validate);

    }


    /**
     * Set the "validate certificate chains" flag.
     *
     * @param validate The new validate certificate chains flag
     */
    public void setValidate(boolean validate) {

        this.validate = validate;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

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

        String serverCredentials = getPassword(username);

        if ( (serverCredentials == null)
             || (!serverCredentials.equals(credentials)) )
            return null;

        return getPrincipal(username);

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

        return (authenticate(username, credentials.toString()));

    }


    /**
     * Return the Principal associated with the specified username, which
     * matches the digest calculated using the given parameters using the
     * method described in RFC 2069; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param clientDigest Digest which has been submitted by the client
     * @param nOnce Unique (or supposedly unique) token which has been used
     * for this request
     * @param realm Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest :
     * MD5(Method + ":" + uri)
     */
    public Principal authenticate(String username, String clientDigest,
                                  String nOnce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {

        /*
          System.out.println("Digest : " + clientDigest);

          System.out.println("************ Digest info");
          System.out.println("Username:" + username);
          System.out.println("ClientSigest:" + clientDigest);
          System.out.println("nOnce:" + nOnce);
          System.out.println("nc:" + nc);
          System.out.println("cnonce:" + cnonce);
          System.out.println("qop:" + qop);
          System.out.println("realm:" + realm);
          System.out.println("md5a2:" + md5a2);
        */


        String md5a1 = getDigest(username, realm);
        if (md5a1 == null)
            return null;
        String serverDigestValue = md5a1 + ":" + nOnce + ":" + nc + ":"
            + cnonce + ":" + qop + ":" + md5a2;
        String serverDigest =
            md5Encoder.encode(md5Helper.digest(serverDigestValue.getBytes()));
        //System.out.println("Server digest : " + serverDigest);

        if (serverDigest.equals(clientDigest))
            return getPrincipal(username);
        else
            return null;
    }



    /**
     * Return the Principal associated with the specified chain of X509
     * client certificates.  If there is none, return <code>null</code>.
     *
     * @param certs Array of client certificates, with the first one in
     *  the array being the certificate of the client itself.
     */
    public Principal authenticate(X509Certificate certs[]) {

        if ((certs == null) || (certs.length < 1))
            return (null);

        // Check the validity of each certificate in the chain
        if (debug >= 1)
            log("Authenticating client certificate chain");
        if (validate) {
            for (int i = 0; i < certs.length; i++) {
                if (debug >= 2)
                    log(" Checking validity for '" +
                        certs[i].getSubjectDN().getName() + "'");
                try {
                    certs[i].checkValidity();
                } catch (Exception e) {
                    if (debug >= 2)
                        log("  Validity exception", e);
                    return (null);
                }
            }
        }

        // Check the existence of the client Principal in our database
        return (getPrincipal(certs[0].getSubjectDN().getName()));

    }


    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.  This method can be overridden by Realm
     * implementations, but the default is adequate when an instance of
     * <code>GenericPrincipal</code> is used to represent authenticated
     * Principals from this Realm.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {

        if ((principal == null) || (role == null) ||
            !(principal instanceof GenericPrincipal))
            return (false);
        GenericPrincipal gp = (GenericPrincipal) principal;
        if (!(gp.getRealm() == this))
            return (false);
        boolean result = gp.hasRole(role);
        if (debug >= 2) {
            String name = principal.getName();
            if (result)
                log(sm.getString("realmBase.hasRoleSuccess", name, role));
            else
                log(sm.getString("realmBase.hasRoleFailure", name, role));
        }
        return (result);

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("realmBase.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Create a MessageDigest instance for credentials, if desired
        if (digest != null) {
            try {
                md = MessageDigest.getInstance(digest);
            } catch (NoSuchAlgorithmException e) {
                throw new LifecycleException
                    (sm.getString("realmBase.algorithm", digest), e);
            }
        }

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop()
        throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("realmBase.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Clean up allocated resources
        md = null;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Digest the password using the specified algorithm and
     * convert the result to a corresponding hexadecimal string.
     * If exception, the plain credentials string is returned.
     *
     * <strong>IMPLEMENTATION NOTE</strong> - This implementation is
     * synchronized because it reuses the MessageDigest instance.
     * This should be faster than cloning the instance on every request.
     *
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    protected String digest(String credentials)  {

        // If no MessageDigest instance is specified, return unchanged
        if (hasMessageDigest() == false)
            return (credentials);

        // Digest the user credentials and return as hexadecimal
        synchronized (this) {
            try {
                md.reset();
                md.update(credentials.getBytes());
                return (HexUtils.convert(md.digest()));
            } catch (Exception e) {
                log(sm.getString("realmBase.digest"), e);
                return (credentials);
            }
        }

    }

    protected boolean hasMessageDigest() {
        return !(md == null);
    }

    /**
     * Return the digest associated with given principal's user name.
     */
    protected String getDigest(String username, String realmName) {
        if (md5Helper == null) {
            try {
                md5Helper = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }
        String digestValue = username + ":" + realmName + ":"
            + getPassword(username);
        byte[] digest =
            md5Helper.digest(digestValue.getBytes());
        return md5Encoder.encode(digest);
    }


    /**
     * Return a short name for this Realm implementation, for use in
     * log messages.
     */
    protected abstract String getName();


    /**
     * Return the password associated with the given principal's user name.
     */
    protected abstract String getPassword(String username);


    /**
     * Return the Principal associated with the given user name.
     */
    protected abstract Principal getPrincipal(String username);


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = null;
        String name = null;
        if (container != null) {
            logger = container.getLogger();
            name = container.getName();
        }

        if (logger != null) {
            logger.log(getName()+"[" + name + "]: " + message);
        } else {
            System.out.println(getName()+"[" + name + "]: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = null;
        String name = null;
        if (container != null) {
            logger = container.getLogger();
            name = container.getName();
        }

        if (logger != null) {
            logger.log(getName()+"[" + name + "]: " + message, throwable);
        } else {
            System.out.println(getName()+"[" + name + "]: " + message);
            throwable.printStackTrace(System.out);
        }
    }


    // --------------------------------------------------------- Static Methods


    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     *
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     * @param algorithm Algorithm used to do th digest
     */
    public final static String Digest(String credentials, String algorithm) {

        try {
            // Obtain a new message digest with "digest" encryption
            MessageDigest md =
                (MessageDigest) MessageDigest.getInstance(algorithm).clone();
            // encode the credentials
            md.update(credentials.getBytes());
            // Digest the credentials and return as hexadecimal
            return (HexUtils.convert(md.digest()));
        } catch(Exception ex) {
            ex.printStackTrace();
            return credentials;
        }

    }


    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     */
    public static void main(String args[]) {

        if(args.length > 2 && args[0].equalsIgnoreCase("-a")) {
            for(int i=2; i < args.length ; i++){
                System.out.print(args[i]+":");
                System.out.println(Digest(args[i], args[1]));
            }
        } else {
            System.out.println
                ("Usage: RealmBase -a <algorithm> <credentials>");
        }

    }


}
