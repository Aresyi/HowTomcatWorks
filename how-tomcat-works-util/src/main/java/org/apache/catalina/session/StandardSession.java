/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/StandardSession.java,v 1.31 2002/07/23 12:49:11 remm Exp $
 * $Revision: 1.31 $
 * $Date: 2002/07/23 12:49:11 $
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


import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of the <b>Session</b> interface.  This object is
 * serializable, so that it can be stored in persistent storage or transferred
 * to a different JVM for distributable session support.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  An instance of this class represents both the
 * internal (Session) and application level (HttpSession) view of the session.
 * However, because the class itself is not declared public, Java logic outside
 * of the <code>org.apache.catalina.session</code> package cannot cast an
 * HttpSession view of this instance back to a Session view.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  If you add fields to this class, you must
 * make sure that you carry them over in the read/writeObject methods so
 * that this class is properly serialized.
 *
 * @author Craig R. McClanahan
 * @author Sean Legassick
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Revision: 1.31 $ $Date: 2002/07/23 12:49:11 $
 */

class StandardSession
    implements HttpSession, Session, Serializable {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new Session associated with the specified Manager.
     *
     * @param manager The manager with which this Session is associated
     */
    public StandardSession(Manager manager) {

        super();
        this.manager = manager;
        if (manager instanceof ManagerBase)
            this.debug = ((ManagerBase) manager).getDebug();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The dummy attribute value serialized when a NotSerializableException is
     * encountered in <code>writeObject()</code>.
     */
    private static final String NOT_SERIALIZED =
        "___NOT_SERIALIZABLE_EXCEPTION___";


    /**
     * The collection of user data attributes associated with this Session.
     */
    private HashMap attributes = new HashMap();


    /**
     * The authentication type used to authenticate our cached Principal,
     * if any.  NOTE:  This value is not included in the serialized
     * version of this object.
     */
    private transient String authType = null;


    /**
     * The <code>java.lang.Method</code> for the
     * <code>fireContainerEvent()</code> method of the
     * <code>org.apache.catalina.core.StandardContext</code> method,
     * if our Context implementation is of this class.  This value is
     * computed dynamically the first time it is needed, or after
     * a session reload (since it is declared transient).
     */
    private transient Method containerEventMethod = null;


    /**
     * The method signature for the <code>fireContainerEvent</code> method.
     */
    private static final Class containerEventTypes[] =
    { String.class, Object.class };


    /**
     * The time this session was created, in milliseconds since midnight,
     * January 1, 1970 GMT.
     */
    private long creationTime = 0L;


    /**
     * The debugging detail level for this component.  NOTE:  This value
     * is not included in the serialized version of this object.
     */
    private transient int debug = 0;


    /**
     * We are currently processing a session expiration, so bypass
     * certain IllegalStateException tests.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    private transient boolean expiring = false;


    /**
     * The facade associated with this session.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    private transient StandardSessionFacade facade = null;


    /**
     * The session identifier of this Session.
     */
    private String id = null;


    /**
     * Descriptive information describing this Session implementation.
     */
    private static final String info = "StandardSession/1.0";


    /**
     * The last accessed time for this Session.
     */
    private long lastAccessedTime = creationTime;


    /**
     * The session event listeners for this Session.
     */
    private transient ArrayList listeners = new ArrayList();


    /**
     * The Manager with which this Session is associated.
     */
    private Manager manager = null;


    /**
     * The maximum time interval, in seconds, between client requests before
     * the servlet container may invalidate this session.  A negative time
     * indicates that the session should never time out.
     */
    private int maxInactiveInterval = -1;


    /**
     * Flag indicating whether this session is new or not.
     */
    private boolean isNew = false;


    /**
     * Flag indicating whether this session is valid or not.
     */
    private boolean isValid = false;


    /**
     * Internal notes associated with this session by Catalina components
     * and event listeners.  <b>IMPLEMENTATION NOTE:</b> This object is
     * <em>not</em> saved and restored across session serializations!
     */
    private transient HashMap notes = new HashMap();


    /**
     * The authenticated Principal associated with this session, if any.
     * <b>IMPLEMENTATION NOTE:</b>  This object is <i>not</i> saved and
     * restored across session serializations!
     */
    private transient Principal principal = null;


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The HTTP session context associated with this session.
     */
    private static HttpSessionContext sessionContext = null;


    /**
     * The property change support for this component.  NOTE:  This value
     * is not included in the serialized version of this object.
     */
    private transient PropertyChangeSupport support =
        new PropertyChangeSupport(this);


    /**
     * The current accessed time for this session.
     */
    private long thisAccessedTime = creationTime;


    // ----------------------------------------------------- Session Properties


    /**
     * Return the authentication type used to authenticate our cached
     * Principal, if any.
     */
    public String getAuthType() {

        return (this.authType);

    }


    /**
     * Set the authentication type used to authenticate our cached
     * Principal, if any.
     *
     * @param authType The new cached authentication type
     */
    public void setAuthType(String authType) {

        String oldAuthType = this.authType;
        this.authType = authType;
        support.firePropertyChange("authType", oldAuthType, this.authType);

    }


    /**
     * Set the creation time for this session.  This method is called by the
     * Manager when an existing Session instance is reused.
     *
     * @param time The new creation time
     */
    public void setCreationTime(long time) {

        this.creationTime = time;
        this.lastAccessedTime = time;
        this.thisAccessedTime = time;

    }


    /**
     * Return the session identifier for this session.
     */
    public String getId() {

        return (this.id);

    }


    /**
     * Set the session identifier for this session.
     *
     * @param id The new session identifier
     */
    public void setId(String id) {

        if ((this.id != null) && (manager != null))
            manager.remove(this);

        this.id = id;

        if (manager != null)
            manager.add(this);

        // Notify interested session event listeners
        fireSessionEvent(Session.SESSION_CREATED_EVENT, null);

        // Notify interested application event listeners
        Context context = (Context) manager.getContainer();
        Object listeners[] = context.getApplicationListeners();
        if (listeners != null) {
            HttpSessionEvent event =
                new HttpSessionEvent(getSession());
            for (int i = 0; i < listeners.length; i++) {
                if (!(listeners[i] instanceof HttpSessionListener))
                    continue;
                HttpSessionListener listener =
                    (HttpSessionListener) listeners[i];
                try {
                    fireContainerEvent(context,
                                       "beforeSessionCreated",
                                       listener);
                    listener.sessionCreated(event);
                    fireContainerEvent(context,
                                       "afterSessionCreated",
                                       listener);
                } catch (Throwable t) {
                    try {
                        fireContainerEvent(context,
                                           "afterSessionCreated",
                                           listener);
                    } catch (Exception e) {
                        ;
                    }
                    // FIXME - should we do anything besides log these?
                    log(sm.getString("standardSession.sessionEvent"), t);
                }
            }
        }

    }


    /**
     * Return descriptive information about this Session implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the last time the client sent a request associated with this
     * session, as the number of milliseconds since midnight, January 1, 1970
     * GMT.  Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access time.
     */
    public long getLastAccessedTime() {

        return (this.lastAccessedTime);

    }


    /**
     * Return the Manager within which this Session is valid.
     */
    public Manager getManager() {

        return (this.manager);

    }


    /**
     * Set the Manager within which this Session is valid.
     *
     * @param manager The new Manager
     */
    public void setManager(Manager manager) {

        this.manager = manager;

    }


    /**
     * Return the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     */
    public int getMaxInactiveInterval() {

        return (this.maxInactiveInterval);

    }


    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the servlet container will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval
     */
    public void setMaxInactiveInterval(int interval) {

        this.maxInactiveInterval = interval;

    }


    /**
     * Set the <code>isNew</code> flag for this session.
     *
     * @param isNew The new value for the <code>isNew</code> flag
     */
    public void setNew(boolean isNew) {

        this.isNew = isNew;

    }


    /**
     * Return the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.  If there
     * is no current associated Principal, return <code>null</code>.
     */
    public Principal getPrincipal() {

        return (this.principal);

    }


    /**
     * Set the authenticated Principal that is associated with this Session.
     * This provides an <code>Authenticator</code> with a means to cache a
     * previously authenticated Principal, and avoid potentially expensive
     * <code>Realm.authenticate()</code> calls on every request.
     *
     * @param principal The new Principal, or <code>null</code> if none
     */
    public void setPrincipal(Principal principal) {

        Principal oldPrincipal = this.principal;
        this.principal = principal;
        support.firePropertyChange("principal", oldPrincipal, this.principal);

    }


    /**
     * Return the <code>HttpSession</code> for which this object
     * is the facade.
     */
    public HttpSession getSession() {

        if (facade == null)
            facade = new StandardSessionFacade(this);
        return (facade);

    }


    /**
     * Return the <code>isValid</code> flag for this session.
     */
    public boolean isValid() {

        return (this.isValid);

    }


    /**
     * Set the <code>isValid</code> flag for this session.
     *
     * @param isValid The new value for the <code>isValid</code> flag
     */
    public void setValid(boolean isValid) {

        this.isValid = isValid;
    }


    // ------------------------------------------------- Session Public Methods


    /**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
    public void access() {

        this.isNew = false;
        this.lastAccessedTime = this.thisAccessedTime;
        this.thisAccessedTime = System.currentTimeMillis();

    }


    /**
     * Add a session event listener to this component.
     */
    public void addSessionListener(SessionListener listener) {

        synchronized (listeners) {
            listeners.add(listener);
        }

    }


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     */
    public void expire() {

        expire(true);

    }


    /**
     * Perform the internal processing required to invalidate this session,
     * without triggering an exception if the session has already expired.
     *
     * @param notify Should we notify listeners about the demise of
     *  this session?
     */
    public void expire(boolean notify) {

        // Mark this session as "being expired" if needed
        if (expiring)
            return;
        expiring = true;
        setValid(false);

        // Remove this session from our manager's active sessions
        if (manager != null)
            manager.remove(this);

        // Unbind any objects associated with this session
        String keys[] = keys();
        for (int i = 0; i < keys.length; i++)
            removeAttribute(keys[i], notify);

        // Notify interested session event listeners
        if (notify) {
            fireSessionEvent(Session.SESSION_DESTROYED_EVENT, null);
        }

        // Notify interested application event listeners
        // FIXME - Assumes we call listeners in reverse order
        Context context = (Context) manager.getContainer();
        Object listeners[] = context.getApplicationListeners();
        if (notify && (listeners != null)) {
            HttpSessionEvent event =
              new HttpSessionEvent(getSession());
            for (int i = 0; i < listeners.length; i++) {
                int j = (listeners.length - 1) - i;
                if (!(listeners[j] instanceof HttpSessionListener))
                    continue;
                HttpSessionListener listener =
                    (HttpSessionListener) listeners[j];
                try {
                    fireContainerEvent(context,
                                       "beforeSessionDestroyed",
                                       listener);
                    listener.sessionDestroyed(event);
                    fireContainerEvent(context,
                                       "afterSessionDestroyed",
                                       listener);
                } catch (Throwable t) {
                    try {
                        fireContainerEvent(context,
                                           "afterSessionDestroyed",
                                           listener);
                    } catch (Exception e) {
                        ;
                    }
                    // FIXME - should we do anything besides log these?
                    log(sm.getString("standardSession.sessionEvent"), t);
                }
            }
        }

        // We have completed expire of this session
        expiring = false;
        if ((manager != null) && (manager instanceof ManagerBase)) {
            recycle();
        }

    }


    /**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {

        // Notify ActivationListeners
        HttpSessionEvent event = null;
        String keys[] = keys();
        for (int i = 0; i < keys.length; i++) {
            Object attribute = getAttribute(keys[i]);
            if (attribute instanceof HttpSessionActivationListener) {
                if (event == null)
                    event = new HttpSessionEvent(this);
                // FIXME: Should we catch throwables?
                ((HttpSessionActivationListener)attribute).sessionWillPassivate(event);
            }
        }

    }


    /**
     * Perform internal processing required to activate this
     * session.
     */
    public void activate() {

        // Notify ActivationListeners
        HttpSessionEvent event = null;
        String keys[] = keys();
        for (int i = 0; i < keys.length; i++) {
            Object attribute = getAttribute(keys[i]);
            if (attribute instanceof HttpSessionActivationListener) {
                if (event == null)
                    event = new HttpSessionEvent(this);
                // FIXME: Should we catch throwables?
                ((HttpSessionActivationListener)attribute).sessionDidActivate(event);
            }
        }

    }


    /**
     * Return the object bound with the specified name to the internal notes
     * for this session, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    public Object getNote(String name) {

        synchronized (notes) {
            return (notes.get(name));
        }

    }


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this session.
     */
    public Iterator getNoteNames() {

        synchronized (notes) {
            return (notes.keySet().iterator());
        }

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        // Reset the instance variables associated with this Session
        attributes.clear();
        setAuthType(null);
        creationTime = 0L;
        expiring = false;
        id = null;
        lastAccessedTime = 0L;
        maxInactiveInterval = -1;
        notes.clear();
        setPrincipal(null);
        isNew = false;
        isValid = false;
        Manager savedManager = manager;
        manager = null;

        // Tell our Manager that this Session has been recycled
        if ((savedManager != null) && (savedManager instanceof ManagerBase))
            ((ManagerBase) savedManager).recycle(this);

    }


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this session.
     *
     * @param name Name of the note to be removed
     */
    public void removeNote(String name) {

        synchronized (notes) {
            notes.remove(name);
        }

    }


    /**
     * Remove a session event listener from this component.
     */
    public void removeSessionListener(SessionListener listener) {

        synchronized (listeners) {
            listeners.remove(listener);
        }

    }


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this session, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    public void setNote(String name, Object value) {

        synchronized (notes) {
            notes.put(name, value);
        }

    }


    /**
     * Return a string representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("StandardSession[");
        sb.append(id);
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------ Session Package Methods


    /**
     * Read a serialized version of the contents of this session object from
     * the specified object input stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream The object input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    void readObjectData(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        readObject(stream);

    }


    /**
     * Write a serialized version of the contents of this session object to
     * the specified object output stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream The object output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    void writeObjectData(ObjectOutputStream stream)
        throws IOException {

        writeObject(stream);

    }


    // ------------------------------------------------- HttpSession Properties


    /**
     * Return the time when this session was created, in milliseconds since
     * midnight, January 1, 1970 GMT.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public long getCreationTime() {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.getCreationTime.ise"));

        return (this.creationTime);

    }


    /**
     * Return the ServletContext to which this session belongs.
     */
    public ServletContext getServletContext() {

        if (manager == null)
            return (null);
        Context context = (Context) manager.getContainer();
        if (context == null)
            return (null);
        else
            return (context.getServletContext());

    }


    /**
     * Return the session context with which this session is associated.
     *
     * @deprecated As of Version 2.1, this method is deprecated and has no
     *  replacement.  It will be removed in a future version of the
     *  Java Servlet API.
     */
    public HttpSessionContext getSessionContext() {

        if (sessionContext == null)
            sessionContext = new StandardSessionContext();
        return (sessionContext);

    }


    // ----------------------------------------------HttpSession Public Methods


    /**
     * Return the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound with that name.
     *
     * @param name Name of the attribute to be returned
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public Object getAttribute(String name) {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.getAttribute.ise"));

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Return an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of the objects bound to this session.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public Enumeration getAttributeNames() {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.getAttributeNames.ise"));

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Return the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound with that name.
     *
     * @param name Name of the value to be returned
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>getAttribute()</code>
     */
    public Object getValue(String name) {

        return (getAttribute(name));

    }


    /**
     * Return the set of names of objects bound to this session.  If there
     * are no such objects, a zero-length array is returned.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>getAttributeNames()</code>
     */
    public String[] getValueNames() {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.getValueNames.ise"));

        return (keys());

    }


    /**
     * Invalidates this session and unbinds any objects bound to it.
     *
     * @exception IllegalStateException if this method is called on
     *  an invalidated session
     */
    public void invalidate() {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.invalidate.ise"));

        // Cause this session to expire
        expire();

    }


    /**
     * Return <code>true</code> if the client does not yet know about the
     * session, or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and the client
     * has disabled the use of cookies, then a session would be new on each
     * request.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public boolean isNew() {

        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.isNew.ise"));

        return (this.isNew);

    }


    /**
     * Bind an object to this session, using the specified name.  If an object
     * of the same name is already bound to this session, the object is
     * replaced.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueBound()</code> on the object.
     *
     * @param name Name to which the object is bound, cannot be null
     * @param value Object to be bound, cannot be null
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>setAttribute()</code>
     */
    public void putValue(String name, Object value) {

        setAttribute(name, value);

    }


    /**
     * Remove the object bound with the specified name from this session.  If
     * the session does not have an object bound with this name, this method
     * does nothing.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueUnbound()</code> on the object.
     *
     * @param name Name of the object to remove from this session.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void removeAttribute(String name) {

        removeAttribute(name, true);

    }


    /**
     * Remove the object bound with the specified name from this session.  If
     * the session does not have an object bound with this name, this method
     * does nothing.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueUnbound()</code> on the object.
     *
     * @param name Name of the object to remove from this session.
     * @param notify Should we notify interested listeners that this
     *  attribute is being removed?
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void removeAttribute(String name, boolean notify) {

        // Validate our current state
        if (!expiring && !isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.removeAttribute.ise"));

        // Remove this attribute from our collection
        Object value = null;
        boolean found = false;
        synchronized (attributes) {
            found = attributes.containsKey(name);
            if (found) {
                value = attributes.get(name);
                attributes.remove(name);
            } else {
                return;
            }
        }

        // Do we need to do valueUnbound() and attributeRemoved() notification?
        if (!notify) {
            return;
        }

        // Call the valueUnbound() method if necessary
        HttpSessionBindingEvent event =
          new HttpSessionBindingEvent((HttpSession) this, name, value);
        if ((value != null) &&
            (value instanceof HttpSessionBindingListener))
            ((HttpSessionBindingListener) value).valueUnbound(event);

        // Notify interested application event listeners
        Context context = (Context) manager.getContainer();
        Object listeners[] = context.getApplicationListeners();
        if (listeners == null)
            return;
        for (int i = 0; i < listeners.length; i++) {
            if (!(listeners[i] instanceof HttpSessionAttributeListener))
                continue;
            HttpSessionAttributeListener listener =
                (HttpSessionAttributeListener) listeners[i];
            try {
                fireContainerEvent(context,
                                   "beforeSessionAttributeRemoved",
                                   listener);
                listener.attributeRemoved(event);
                fireContainerEvent(context,
                                   "afterSessionAttributeRemoved",
                                   listener);
            } catch (Throwable t) {
                try {
                    fireContainerEvent(context,
                                       "afterSessionAttributeRemoved",
                                       listener);
                } catch (Exception e) {
                    ;
                }
                // FIXME - should we do anything besides log these?
                log(sm.getString("standardSession.attributeEvent"), t);
            }
        }

    }


    /**
     * Remove the object bound with the specified name from this session.  If
     * the session does not have an object bound with this name, this method
     * does nothing.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueUnbound()</code> on the object.
     *
     * @param name Name of the object to remove from this session.
     *
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     *
     * @deprecated As of Version 2.2, this method is replaced by
     *  <code>removeAttribute()</code>
     */
    public void removeValue(String name) {

        removeAttribute(name);

    }


    /**
     * Bind an object to this session, using the specified name.  If an object
     * of the same name is already bound to this session, the object is
     * replaced.
     * <p>
     * After this method executes, and if the object implements
     * <code>HttpSessionBindingListener</code>, the container calls
     * <code>valueBound()</code> on the object.
     *
     * @param name Name to which the object is bound, cannot be null
     * @param value Object to be bound, cannot be null
     *
     * @exception IllegalArgumentException if an attempt is made to add a
     *  non-serializable object in an environment marked distributable.
     * @exception IllegalStateException if this method is called on an
     *  invalidated session
     */
    public void setAttribute(String name, Object value) {

        // Name cannot be null
        if (name == null)
            throw new IllegalArgumentException
                (sm.getString("standardSession.setAttribute.namenull"));

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        // Validate our current state
        if (!isValid)
            throw new IllegalStateException
                (sm.getString("standardSession.setAttribute.ise"));
        if ((manager != null) && manager.getDistributable() &&
          !(value instanceof Serializable))
            throw new IllegalArgumentException
                (sm.getString("standardSession.setAttribute.iae"));

        // Replace or add this attribute
        Object unbound = null;
        synchronized (attributes) {
            unbound = attributes.get(name);
            attributes.put(name, value);
        }

        // Call the valueUnbound() method if necessary
        if ((unbound != null) &&
            (unbound instanceof HttpSessionBindingListener)) {
            ((HttpSessionBindingListener) unbound).valueUnbound
              (new HttpSessionBindingEvent((HttpSession) this, name));
        }

        // Call the valueBound() method if necessary
        HttpSessionBindingEvent event = null;
        if (unbound != null)
            event = new HttpSessionBindingEvent
                ((HttpSession) this, name, unbound);
        else
            event = new HttpSessionBindingEvent
                ((HttpSession) this, name, value);
        if (value instanceof HttpSessionBindingListener)
            ((HttpSessionBindingListener) value).valueBound(event);

        // Notify interested application event listeners
        Context context = (Context) manager.getContainer();
        Object listeners[] = context.getApplicationListeners();
        if (listeners == null)
            return;
        for (int i = 0; i < listeners.length; i++) {
            if (!(listeners[i] instanceof HttpSessionAttributeListener))
                continue;
            HttpSessionAttributeListener listener =
                (HttpSessionAttributeListener) listeners[i];
            try {
                if (unbound != null) {
                    fireContainerEvent(context,
                                       "beforeSessionAttributeReplaced",
                                       listener);
                    listener.attributeReplaced(event);
                    fireContainerEvent(context,
                                       "afterSessionAttributeReplaced",
                                       listener);
                } else {
                    fireContainerEvent(context,
                                       "beforeSessionAttributeAdded",
                                       listener);
                    listener.attributeAdded(event);
                    fireContainerEvent(context,
                                       "afterSessionAttributeAdded",
                                       listener);
                }
            } catch (Throwable t) {
                try {
                    if (unbound != null) {
                        fireContainerEvent(context,
                                           "afterSessionAttributeReplaced",
                                           listener);
                    } else {
                        fireContainerEvent(context,
                                           "afterSessionAttributeAdded",
                                           listener);
                    }
                } catch (Exception e) {
                    ;
                }
                // FIXME - should we do anything besides log these?
                log(sm.getString("standardSession.attributeEvent"), t);
            }
        }

    }


    // -------------------------------------------- HttpSession Private Methods


    /**
     * Read a serialized version of this session object from the specified
     * object input stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The reference to the owning Manager
     * is not restored by this method, and must be set explicitly.
     *
     * @param stream The input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        // Deserialize the scalar instance variables (except Manager)
        authType = null;        // Transient only
        creationTime = ((Long) stream.readObject()).longValue();
        lastAccessedTime = ((Long) stream.readObject()).longValue();
        maxInactiveInterval = ((Integer) stream.readObject()).intValue();
        isNew = ((Boolean) stream.readObject()).booleanValue();
        isValid = ((Boolean) stream.readObject()).booleanValue();
        thisAccessedTime = ((Long) stream.readObject()).longValue();
        principal = null;        // Transient only
        //        setId((String) stream.readObject());
        id = (String) stream.readObject();
        if (debug >= 2)
            log("readObject() loading session " + id);

        // Deserialize the attribute count and attribute values
        if (attributes == null)
            attributes = new HashMap();
        int n = ((Integer) stream.readObject()).intValue();
        boolean isValidSave = isValid;
        isValid = true;
        for (int i = 0; i < n; i++) {
            String name = (String) stream.readObject();
            Object value = (Object) stream.readObject();
            if ((value instanceof String) && (value.equals(NOT_SERIALIZED)))
                continue;
            if (debug >= 2)
                log("  loading attribute '" + name +
                    "' with value '" + value + "'");
            synchronized (attributes) {
                attributes.put(name, value);
            }
        }
        isValid = isValidSave;

    }


    /**
     * Write a serialized version of this session object to the specified
     * object output stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The owning Manager will not be stored
     * in the serialized representation of this Session.  After calling
     * <code>readObject()</code>, you must set the associated Manager
     * explicitly.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Any attribute that is not Serializable
     * will be unbound from the session, with appropriate actions if it
     * implements HttpSessionBindingListener.  If you do not want any such
     * attributes, be sure the <code>distributable</code> property of the
     * associated Manager is set to <code>true</code>.
     *
     * @param stream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {

        // Write the scalar instance variables (except Manager)
        stream.writeObject(new Long(creationTime));
        stream.writeObject(new Long(lastAccessedTime));
        stream.writeObject(new Integer(maxInactiveInterval));
        stream.writeObject(new Boolean(isNew));
        stream.writeObject(new Boolean(isValid));
        stream.writeObject(new Long(thisAccessedTime));
        stream.writeObject(id);
        if (debug >= 2)
            log("writeObject() storing session " + id);

        // Accumulate the names of serializable and non-serializable attributes
        String keys[] = keys();
        ArrayList saveNames = new ArrayList();
        ArrayList saveValues = new ArrayList();
        for (int i = 0; i < keys.length; i++) {
            Object value = null;
            synchronized (attributes) {
                value = attributes.get(keys[i]);
            }
            if (value == null)
                continue;
            else if (value instanceof Serializable) {
                saveNames.add(keys[i]);
                saveValues.add(value);
            }
        }

        // Serialize the attribute count and the Serializable attributes
        int n = saveNames.size();
        stream.writeObject(new Integer(n));
        for (int i = 0; i < n; i++) {
            stream.writeObject((String) saveNames.get(i));
            try {
                stream.writeObject(saveValues.get(i));
                if (debug >= 2)
                    log("  storing attribute '" + saveNames.get(i) +
                        "' with value '" + saveValues.get(i) + "'");
            } catch (NotSerializableException e) {
                log(sm.getString("standardSession.notSerializable",
                                 saveNames.get(i), id), e);
                stream.writeObject(NOT_SERIALIZED);
                if (debug >= 2)
                    log("  storing attribute '" + saveNames.get(i) +
                        "' with value NOT_SERIALIZED");
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Fire container events if the Context implementation is the
     * <code>org.apache.catalina.core.StandardContext</code>.
     *
     * @param context Context for which to fire events
     * @param type Event type
     * @param data Event data
     *
     * @exception Exception occurred during event firing
     */
    private void fireContainerEvent(Context context,
                                    String type, Object data)
        throws Exception {

        if (!"org.apache.catalina.core.StandardContext".equals
            (context.getClass().getName())) {
            return; // Container events are not supported
        }
        // NOTE:  Race condition is harmless, so do not synchronize
        if (containerEventMethod == null) {
            containerEventMethod =
                context.getClass().getMethod("fireContainerEvent",
                                             containerEventTypes);
        }
        Object containerEventParams[] = new Object[2];
        containerEventParams[0] = type;
        containerEventParams[1] = data;
        containerEventMethod.invoke(context, containerEventParams);

    }
                                      


    /**
     * Notify all session event listeners that a particular event has
     * occurred for this Session.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireSessionEvent(String type, Object data) {

        if (listeners.size() < 1)
            return;
        SessionEvent event = new SessionEvent(this, type, data);
        SessionListener list[] = new SessionListener[0];
        synchronized (listeners) {
            list = (SessionListener[]) listeners.toArray(list);
        }
        for (int i = 0; i < list.length; i++)
            ((SessionListener) list[i]).sessionEvent(event);

    }


    /**
     * Return the names of all currently defined session attributes
     * as an array of Strings.  If there are no defined attributes, a
     * zero-length array is returned.
     */
    private String[] keys() {

        String results[] = new String[0];
        synchronized (attributes) {
            return ((String[]) attributes.keySet().toArray(results));
        }

    }


    /**
     * Log a message on the Logger associated with our Manager (if any).
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        if ((manager != null) && (manager instanceof ManagerBase)) {
            ((ManagerBase) manager).log(message);
        } else {
            System.out.println("StandardSession: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Manager (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        if ((manager != null) && (manager instanceof ManagerBase)) {
            ((ManagerBase) manager).log(message, throwable);
        } else {
            System.out.println("StandardSession: " + message);
            throwable.printStackTrace(System.out);
        }

    }


}


// -------------------------------------------------------------- Private Class


/**
 * This class is a dummy implementation of the <code>HttpSessionContext</code>
 * interface, to conform to the requirement that such an object be returned
 * when <code>HttpSession.getSessionContext()</code> is called.
 *
 * @author Craig R. McClanahan
 *
 * @deprecated As of Java Servlet API 2.1 with no replacement.  The
 *  interface will be removed in a future version of this API.
 */

final class StandardSessionContext implements HttpSessionContext {


    private HashMap dummy = new HashMap();

    /**
     * Return the session identifiers of all sessions defined
     * within this context.
     *
     * @deprecated As of Java Servlet API 2.1 with no replacement.
     *  This method must return an empty <code>Enumeration</code>
     *  and will be removed in a future version of the API.
     */
    public Enumeration getIds() {

        return (new Enumerator(dummy));

    }


    /**
     * Return the <code>HttpSession</code> associated with the
     * specified session identifier.
     *
     * @param id Session identifier for which to look up a session
     *
     * @deprecated As of Java Servlet API 2.1 with no replacement.
     *  This method must return null and will be removed in a
     *  future version of the API.
     */
    public HttpSession getSession(String id) {

        return (null);

    }



}
