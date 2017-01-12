/*
 * StoreBase.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/StoreBase.java,v 1.6 2002/08/28 17:08:58 bobh Exp $
 * $Revision: 1.6 $
 * $Date: 2002/08/28 17:08:58 $
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
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;

/**
 * Abstract implementation of the Store interface to
 * support most of the functionality required by a Store.
 *
 * @author Bip Thelin
 * @version $Revision: 1.6 $, $Date: 2002/08/28 17:08:58 $
 */

public abstract class StoreBase
    implements Lifecycle, Runnable, Store {

    // ----------------------------------------------------- Instance Variables

    /**
     * The descriptive information about this implementation.
     */
    protected static String info = "StoreBase/1.0";

    /**
     * The interval (in seconds) between checks for expired sessions.
     */
    protected int checkInterval = 60;

    /**
     * Name to register for the background thread.
     */
    protected String threadName = "StoreBase";

    /**
     * Name to register for this Store, used for logging.
     */
    protected static String storeName = "StoreBase";

    /**
     * The background thread.
     */
    protected Thread thread = null;

    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;

    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * The Manager with which this JDBCStore is associated.
     */
    protected Manager manager;

    // ------------------------------------------------------------- Properties

    /**
     * Return the info for this Store.
     */
    public String getInfo() {
        return(info);
    }

    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(threadName);
    }

    /**
     * Return the name for this Store, used for logging.
     */
    public String getStoreName() {
        return(storeName);
    }

    /**
     * Set the debugging detail level for this Store.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    /**
     * Return the debugging detail level for this Store.
     */
    public int getDebug() {
        return(this.debug);
    }


    /**
     * Set the check interval (in seconds) for this Store.
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
     * Return the check interval (in seconds) for this Store.
     */
    public int getCheckInterval() {
        return(this.checkInterval);
    }

    /**
     * Set the Manager with which this Store is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {
        Manager oldManager = this.manager;
        this.manager = manager;
        support.firePropertyChange("manager", oldManager, this.manager);
    }

    /**
     * Return the Manager with which the Store is associated.
     */
    public Manager getManager() {
        return(this.manager);
    }

    // --------------------------------------------------------- Public Methods

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
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /**
     * Add a property change listener to this component.
     *
     * @param listener a value of type 'PropertyChangeListener'
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // --------------------------------------------------------- Protected Methods

    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     *
     */
    protected void processExpires() {
        long timeNow = System.currentTimeMillis();
        String[] keys = null;

        if(!started)
            return;

        try {
            keys = keys();
        } catch (IOException e) {
            log (e.toString());
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            try {
                StandardSession session = (StandardSession) load(keys[i]);
                if (!session.isValid())
                    continue;
                int maxInactiveInterval = session.getMaxInactiveInterval();
                if (maxInactiveInterval < 0)
                    continue;
                int timeIdle = // Truncate, do not round up
                    (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle >= maxInactiveInterval) {
                    if ( ( (PersistentManagerBase) manager).isLoaded( keys[i] )) {
                        // recycle old backup session
                        session.recycle();
                    } else {
                        // expire swapped out session
                        session.expire();
                    }
                    remove(session.getId());
                }
            } catch (IOException e) {
                log (e.toString());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                log (e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        Logger logger = null;
        Container container = manager.getContainer();

        if (container != null)
            logger = container.getLogger();

        if (logger != null) {
            logger.log(getStoreName()+"[" + container.getName() + "]: "
                       + message);
        } else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println(getStoreName()+"[" + containerName
                               + "]: " + message);
        }
    }

    // --------------------------------------------------------- Thread Methods

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

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString(getStoreName()+".alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

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
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString(getStoreName()+".notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop the background reaper thread
        threadStop();
    }

    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        thread = new Thread(this, getThreadName());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    protected void threadSleep() {
        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
            ;
        }
    }

    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    protected void threadStop() {
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
}
