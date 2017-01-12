/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/ManagerBase.java,v 1.12 2002/09/19 22:55:48 amyroh Exp $
 * $Revision: 1.12 $
 * $Date: 2002/09/19 22:55:48 $
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


package org.apache.catalina.session;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.catalina.Container;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.util.StringManager;


/**
 * Minimal implementation of the <b>Manager</b> interface that supports
 * no session persistence or distributable capabilities.  This class may
 * be subclassed to create more sophisticated Manager implementations.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2002/09/19 22:55:48 $
 */

public abstract class ManagerBase implements Manager {


    // ----------------------------------------------------- Instance Variables


    /**
     * The default message digest algorithm to use if we cannot use
     * the requested one.
     */
    protected static final String DEFAULT_ALGORITHM = "MD5";


    /**
     * The number of random bytes to include when generating a
     * session identifier.
     */
    protected static final int SESSION_ID_BYTES = 16;


    /**
     * The message digest algorithm to be used when generating session
     * identifiers.  This must be an algorithm supported by the
     * <code>java.security.MessageDigest</code> class on your platform.
     */
    protected String algorithm = DEFAULT_ALGORITHM;


    /**
     * The Container with which this Manager is associated.
     */
    protected Container container;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The DefaultContext with which this Manager is associated.
     */
    protected DefaultContext defaultContext = null;
    
    
    /**
     * Return the MessageDigest implementation to be used when
     * creating session identifiers.
     */
    protected MessageDigest digest = null;


    /**
     * The distributable flag for Sessions created by this Manager.  If this
     * flag is set to <code>true</code>, any user attributes added to a
     * session controlled by this Manager must be Serializable.
     */
    protected boolean distributable;


    /**
     * A String initialization parameter used to increase the entropy of
     * the initialization of our random number generator.
     */
    protected String entropy = null;


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info = "ManagerBase/1.0";


    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = 60;


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static String name = "ManagerBase";


    /**
     * A random number generator to use when generating session identifiers.
     */
    protected Random random = null;


    /**
     * The Java class name of the random number generator class to be used
     * when generating session identifiers.
     */
    protected String randomClass = "java.security.SecureRandom";


    /**
     * The set of previously recycled Sessions for this Manager.
     */
    protected ArrayList recycled = new ArrayList();


    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected HashMap sessions = new HashMap();


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    // ------------------------------------------------------------- Properties


    /**
     * Return the message digest algorithm for this Manager.
     */
    public String getAlgorithm() {

        return (this.algorithm);

    }


    /**
     * Set the message digest algorithm for this Manager.
     *
     * @param algorithm The new message digest algorithm
     */
    public void setAlgorithm(String algorithm) {

        String oldAlgorithm = this.algorithm;
        this.algorithm = algorithm;
        support.firePropertyChange("algorithm", oldAlgorithm, this.algorithm);

    }


    /**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);

    }


    /**
     * Return the DefaultContext with which this Manager is associated.
     */
    public DefaultContext getDefaultContext() {

        return (this.defaultContext);

    }


    /**
     * Set the DefaultContext with which this Manager is associated.
     *
     * @param defaultContext The newly associated DefaultContext
     */
    public void setDefaultContext(DefaultContext defaultContext) {

        DefaultContext oldDefaultContext = this.defaultContext;
        this.defaultContext = defaultContext;
        support.firePropertyChange("defaultContext", oldDefaultContext, this.defaultContext);

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
     * Return the MessageDigest object to be used for calculating
     * session identifiers.  If none has been created yet, initialize
     * one the first time this method is called.
     */
    public synchronized MessageDigest getDigest() {

        if (this.digest == null) {
            if (debug >= 1)
                log(sm.getString("managerBase.getting", algorithm));
            try {
                this.digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                log(sm.getString("managerBase.digest", algorithm), e);
                try {
                    this.digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException f) {
                    log(sm.getString("managerBase.digest",
                                     DEFAULT_ALGORITHM), e);
                    this.digest = null;
                }
            }
            if (debug >= 1)
                log(sm.getString("managerBase.gotten"));
        }

        return (this.digest);

    }


    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    public boolean getDistributable() {

        return (this.distributable);

    }


    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {

        boolean oldDistributable = this.distributable;
        this.distributable = distributable;
        support.firePropertyChange("distributable",
                                   new Boolean(oldDistributable),
                                   new Boolean(this.distributable));

    }


    /**
     * Return the entropy increaser value, or compute a semi-useful value
     * if this String has not yet been set.
     */
    public String getEntropy() {

        // Calculate a semi-useful value if this has not been set
        if (this.entropy == null)
            setEntropy(this.toString());

        return (this.entropy);

    }


    /**
     * Set the entropy increaser value.
     *
     * @param entropy The new entropy increaser value
     */
    public void setEntropy(String entropy) {

        String oldEntropy = entropy;
        this.entropy = entropy;
        support.firePropertyChange("entropy", oldEntropy, this.entropy);

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    public int getMaxInactiveInterval() {

        return (this.maxInactiveInterval);

    }


    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveInterval(int interval) {

        int oldMaxInactiveInterval = this.maxInactiveInterval;
        this.maxInactiveInterval = interval;
        support.firePropertyChange("maxInactiveInterval",
                                   new Integer(oldMaxInactiveInterval),
                                   new Integer(this.maxInactiveInterval));

    }


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }


    /**
     * Return the random number generator instance we should use for
     * generating session identifiers.  If there is no such generator
     * currently defined, construct and seed a new one.
     */
    public synchronized Random getRandom() {

        if (this.random == null) {
            synchronized (this) {
                if (this.random == null) {
                    // Calculate the new random number generator seed
                    log(sm.getString("managerBase.seeding", randomClass));
                    long seed = System.currentTimeMillis();
                    char entropy[] = getEntropy().toCharArray();
                    for (int i = 0; i < entropy.length; i++) {
                        long update = ((byte) entropy[i]) << ((i % 8) * 8);
                        seed ^= update;
                    }
                    try {
                        // Construct and seed a new random number generator
                        Class clazz = Class.forName(randomClass);
                        this.random = (Random) clazz.newInstance();
                        this.random.setSeed(seed);
                    } catch (Exception e) {
                        // Fall back to the simple case
                        log(sm.getString("managerBase.random", randomClass),
                            e);
                        this.random = new java.util.Random();
                        this.random.setSeed(seed);
                    }
                    log(sm.getString("managerBase.complete", randomClass));
                }
            }
        }

        return (this.random);

    }


    /**
     * Return the random number generator class name.
     */
    public String getRandomClass() {

        return (this.randomClass);

    }


    /**
     * Set the random number generator class name.
     *
     * @param randomClass The new random number generator class name
     */
    public void setRandomClass(String randomClass) {

        String oldRandomClass = this.randomClass;
        this.randomClass = randomClass;
        support.firePropertyChange("randomClass", oldRandomClass,
                                   this.randomClass);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    public void add(Session session) {

        synchronized (sessions) {
            sessions.put(session.getId(), session);
        }

    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {

        // Recycle or create a Session instance
        Session session = null;
        synchronized (recycled) {
            int size = recycled.size();
            if (size > 0) {
                session = (Session) recycled.get(size - 1);
                recycled.remove(size - 1);
            }
        }
        if (session != null)
            session.setManager(this);
        else
            session = new StandardSession(this);

        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(this.maxInactiveInterval);
        String sessionId = generateSessionId();
        String jvmRoute = getJvmRoute();
        // @todo Move appending of jvmRoute generateSessionId()???
        if (jvmRoute != null) {
            sessionId += '.' + jvmRoute;
            session.setId(sessionId);
        }
        /*
        synchronized (sessions) {
            while (sessions.get(sessionId) != null)        // Guarantee uniqueness
                sessionId = generateSessionId();
        }
        */
        session.setId(sessionId);

        return (session);

    }


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException {

        if (id == null)
            return (null);
        synchronized (sessions) {
            Session session = (Session) sessions.get(id);
            return (session);
        }

    }


    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     */
    public Session[] findSessions() {

        Session results[] = null;
        synchronized (sessions) {
            results = new Session[sessions.size()];
            results = (Session[]) sessions.values().toArray(results);
        }
        return (results);

    }


    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    public void remove(Session session) {

        synchronized (sessions) {
            sessions.remove(session.getId());
        }

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Generate and return a new session identifier.
     */
    protected synchronized String generateSessionId() {

        // Generate a byte array containing a session identifier
        Random random = getRandom();
        byte bytes[] = new byte[SESSION_ID_BYTES];
        getRandom().nextBytes(bytes);
        bytes = getDigest().digest(bytes);

        // Render the result as a String of hexadecimal digits
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0x0f);
            if (b1 < 10)
                result.append((char) ('0' + b1));
            else
                result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                result.append((char) ('0' + b2));
            else
                result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Retrieve the enclosing Engine for this Manager.
     *
     * @return an Engine object (or null).
     */
    public Engine getEngine() {
        Engine e = null;
        for (Container c = getContainer(); e == null && c != null ; c = c.getParent()) {
            if (c != null && c instanceof Engine) {
                e = (Engine)c;
            }
        }
        return e;
    }


    /**
     * Retrieve the JvmRoute for the enclosing Engine.
     * @return the JvmRoute or null.
     */
    public String getJvmRoute() {
        Engine e = getEngine();
        return e == null ? null : e.getJvmRoute();
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    void log(String message) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log(getName() + "[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println(getName() + "[" + containerName
                               + "]: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    void log(String message, Throwable throwable) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log(getName() + "[" + container.getName() + "] "
                       + message, throwable);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println(getName() + "[" + containerName
                               + "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Add this Session to the recycle collection for this Manager.
     *
     * @param session Session to be recycled
     */
    void recycle(Session session) {

        synchronized (recycled) {
            recycled.add(session);
        }

    }


}
