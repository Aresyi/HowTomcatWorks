/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/StandardManager.java,v 1.19 2002/06/09 02:19:43 remm Exp $
 * $Revision: 1.19 $
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


package org.apache.catalina.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.LifecycleSupport;


/**
 * Standard implementation of the <b>Manager</b> interface that provides
 * simple session persistence across restarts of this component (such as
 * when the entire server is shut down and restarted, or when a particular
 * web application is reloaded.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.19 $ $Date: 2002/06/09 02:19:43 $
 */

public class StandardManager
    extends ManagerBase
    implements Lifecycle, PropertyChangeListener, Runnable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    private int checkInterval = 60;


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "StandardManager/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    private int maxActiveSessions = -1;


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static String name = "StandardManager";


    /**
     * Path name of the disk file in which active sessions are saved
     * when we stop, and from which these sessions are loaded when we start.
     * A <code>null</code> value indicates that no persistence is desired.
     * If this pathname is relative, it will be resolved against the
     * temporary working directory provided by our context, available via
     * the <code>javax.servlet.context.tempdir</code> context attribute.
     */
    private String pathname = "SESSIONS.ser";


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    private boolean threadDone = false;


    /**
     * Name to register for the background thread.
     */
    private String threadName = "StandardManager";


    // ------------------------------------------------------------- Properties


    /**
     * Return the check interval (in seconds) for this Manager.
     */
    public int getCheckInterval() {

        return (this.checkInterval);

    }


    /**
     * Set the check interval (in seconds) for this Manager.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval(int checkInterval) {

        int oldCheckInterval = this.checkInterval;
        this.checkInterval = checkInterval;
        support.firePropertyChange("checkInterval",
                                   new Integer(oldCheckInterval),
                                   new Integer(this.checkInterval));

    }


    /**
     * Set the Container with which this Manager has been associated.  If
     * it is a Context (the usual case), listen for changes to the session
     * timeout property.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        // Default processing provided by our superclass
        super.setContainer(container);

        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
            setMaxInactiveInterval
                ( ((Context) this.container).getSessionTimeout()*60 );
            ((Context) this.container).addPropertyChangeListener(this);
        }

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
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }


    /**
     * Set the maximum number of actives Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        support.firePropertyChange("maxActiveSessions",
                                   new Integer(oldMaxActiveSessions),
                                   new Integer(this.maxActiveSessions));

    }


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }


    /**
     * Return the session persistence pathname, if any.
     */
    public String getPathname() {

        return (this.pathname);

    }


    /**
     * Set the session persistence pathname to the specified value.  If no
     * persistence support is desired, set the pathname to <code>null</code>.
     *
     * @param pathname New session persistence pathname
     */
    public void setPathname(String pathname) {

        String oldPathname = this.pathname;
        this.pathname = pathname;
        support.firePropertyChange("pathname", oldPathname, this.pathname);

    }


    // --------------------------------------------------------- Public Methods


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

        if ((maxActiveSessions >= 0) &&
          (sessions.size() >= maxActiveSessions))
            throw new IllegalStateException
                (sm.getString("standardManager.createSession.ise"));

        return (super.createSession());

    }


    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    public void load() throws ClassNotFoundException, IOException {

        if (debug >= 1)
            log("Start: Loading persisted sessions");

        // Initialize our internal data structures
        recycled.clear();
        sessions.clear();

        // Open an input stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        if (debug >= 1)
            log(sm.getString("standardManager.loading", pathname));
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null) {
                if (debug >= 1)
                    log("Creating custom object input stream for class loader "
                        + classLoader);
                ois = new CustomObjectInputStream(bis, classLoader);
            } else {
                if (debug >= 1)
                    log("Creating standard object input stream");
                ois = new ObjectInputStream(bis);
            }
        } catch (FileNotFoundException e) {
            if (debug >= 1)
                log("No persisted data file found");
            return;
        } catch (IOException e) {
            log(sm.getString("standardManager.loading.ioe", e), e);
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }

        // Load the previously unloaded active sessions
        synchronized (sessions) {
            try {
                Integer count = (Integer) ois.readObject();
                int n = count.intValue();
                if (debug >= 1)
                    log("Loading " + n + " persisted sessions");
                for (int i = 0; i < n; i++) {
                    StandardSession session = new StandardSession(this);
                    session.readObjectData(ois);
                    session.setManager(this);
                    sessions.put(session.getId(), session);
                    ((StandardSession) session).activate();
                }
            } catch (ClassNotFoundException e) {
              log(sm.getString("standardManager.loading.cnfe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } catch (IOException e) {
              log(sm.getString("standardManager.loading.ioe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } finally {
                // Close the input stream
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException f) {
                    // ignored
                }

                // Delete the persistent storage file
                if (file != null && file.exists() )
                    file.delete();
            }
        }

        if (debug >= 1)
            log("Finish: Loading persisted sessions");
    }


    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @exception IOException if an input/output error occurs
     */
    public void unload() throws IOException {

        if (debug >= 1)
            log("Unloading persisted sessions");

        // Open an output stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        if (debug >= 1)
            log(sm.getString("standardManager.unloading", pathname));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        } catch (IOException e) {
            log(sm.getString("standardManager.unloading.ioe", e), e);
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
            throw e;
        }

        // Write the number of active sessions, followed by the details
        ArrayList list = new ArrayList();
        synchronized (sessions) {
            if (debug >= 1)
                log("Unloading " + sessions.size() + " sessions");
            try {
                oos.writeObject(new Integer(sessions.size()));
                Iterator elements = sessions.values().iterator();
                while (elements.hasNext()) {
                    StandardSession session =
                        (StandardSession) elements.next();
                    list.add(session);
                    ((StandardSession) session).passivate();
                    session.writeObjectData(oos);
                }
            } catch (IOException e) {
                log(sm.getString("standardManager.unloading.ioe", e), e);
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException f) {
                        ;
                    }
                    oos = null;
                }
                throw e;
            }
        }

        // Flush and close the output stream
        try {
            oos.flush();
            oos.close();
            oos = null;
        } catch (IOException e) {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
            throw e;
        }

        // Expire all the sessions we just wrote
        if (debug >= 1)
            log("Expiring " + list.size() + " persisted sessions");
        Iterator expires = list.iterator();
        while (expires.hasNext()) {
            StandardSession session = (StandardSession) expires.next();
            try {
                session.expire(false);
            } catch (Throwable t) {
                ;
            }
        }

        if (debug >= 1)
            log("Unloading complete");

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
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        if (debug >= 1)
            log("Starting");

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("standardManager.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Force initialization of the random number generator
        if (debug >= 1)
            log("Force random number initialization starting");
        String dummy = generateSessionId();
        if (debug >= 1)
            log("Force random number initialization completed");

        // Load unloaded sessions, if any
        try {
            load();
        } catch (Throwable t) {
            log(sm.getString("standardManager.managerLoad"), t);
        }

        // Start the background reaper thread
        threadStart();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        if (debug >= 1)
            log("Stopping");

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop the background reaper thread
        threadStop();

        // Write out sessions
        try {
            unload();
        } catch (IOException e) {
            log(sm.getString("standardManager.managerUnload"), e);
        }

        // Expire all active sessions
        Session sessions[] = findSessions();
        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];
            if (!session.isValid())
                continue;
            try {
                session.expire();
            } catch (Throwable t) {
                ;
            }
        }

        // Require a new random number generator if we are restarted
        this.random = null;

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process property change events from our associated Context.
     *
     * @param event The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        // Validate the source of this event
        if (!(event.getSource() instanceof Context))
            return;
        Context context = (Context) event.getSource();

        // Process a relevant property change
        if (event.getPropertyName().equals("sessionTimeout")) {
            try {
                setMaxInactiveInterval
                    ( ((Integer) event.getNewValue()).intValue()*60 );
            } catch (NumberFormatException e) {
                log(sm.getString("standardManager.sessionTimeout",
                                 event.getNewValue().toString()));
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return a File object representing the pathname to our
     * persistence file, if any.
     */
    private File file() {

        if (pathname == null)
            return (null);
        File file = new File(pathname);
        if (!file.isAbsolute()) {
            if (container instanceof Context) {
                ServletContext servletContext =
                    ((Context) container).getServletContext();
                File tempdir = (File)
                    servletContext.getAttribute(Globals.WORK_DIR_ATTR);
                if (tempdir != null)
                    file = new File(tempdir, pathname);
            }
        }
//        if (!file.isAbsolute())
//            return (null);
        return (file);

    }


    /**
     * Invalidate all sessions that have expired.
     */
    private void processExpires() {

        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();

        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];
            if (!session.isValid())
                continue;
            int maxInactiveInterval = session.getMaxInactiveInterval();
            if (maxInactiveInterval < 0)
                continue;
            int timeIdle = // Truncate, do not round up
                (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
            if (timeIdle >= maxInactiveInterval) {
                try {
                    session.expire();
                } catch (Throwable t) {
                    log(sm.getString("standardManager.expireException"), t);
                }
            }
        }

    }


    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    private void threadSleep() {

        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
            ;
        }

    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    private void threadStart() {

        if (thread != null)
            return;

        threadDone = false;
        threadName = "StandardManager[" + container.getName() + "]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.setContextClassLoader(container.getLoader().getClassLoader());
        thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    private void threadStop() {

        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;

    }


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            processExpires();
        }

    }
}
