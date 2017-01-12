/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/PersistentManagerBase.java,v 1.9 2002/08/28 17:08:58 bobh Exp $
 * $Revision: 1.9 $
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleSupport;


/**
 * Extends the <b>ManagerBase</b> class to implement most of the
 * functionality required by a Manager which supports any kind of
 * persistence, even if onlyfor  restarts.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.9 $ $Date: 2002/08/28 17:08:58 $
 */

public abstract class PersistentManagerBase
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
    private static final String info = "PersistentManagerBase/1.0";


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
    protected static String name = "PersistentManagerBase";


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
    protected boolean threadDone = false;


    /**
     * Name to register for the background thread.
     */
    private String threadName = "PersistentManagerBase";


    /**
     * Store object which will manage the Session store.
     */
    private Store store = null;


    /**
     * Whether to save and reload sessions when the Manager <code>unload</code>
     * and <code>load</code> methods are called.
     */
    private boolean saveOnRestart = true;


    /**
     * How long a session must be idle before it should be backed up.
     * -1 means sessions won't be backed up.
     */
    private int maxIdleBackup = -1;


    /**
     * Minimum time a session must be idle before it is swapped to disk.
     * This overrides maxActiveSessions, to prevent thrashing if there are lots
     * of active sessions. Setting to -1 means it's ignored.
     */
    private int minIdleSwap = -1;

    /**
     * The maximum time a session may be idle before it should be swapped
     * to file just on general principle. Setting this to -1 means sessions
     * should not be forced out.
     */
    private int maxIdleSwap = -1;


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
     * Indicates how many seconds old a session can get, after its last
     * use in a request, before it should be backed up to the store. -1
     * means sessions are not backed up.
     */
    public int getMaxIdleBackup() {

        return maxIdleBackup;

    }


    /**
     * Sets the option to back sessions up to the Store after they
     * are used in a request. Sessions remain available in memory
     * after being backed up, so they are not passivated as they are
     * when swapped out. The value set indicates how old a session
     * may get (since its last use) before it must be backed up: -1
     * means sessions are not backed up.
     * <p>
     * Note that this is not a hard limit: sessions are checked
     * against this age limit periodically according to <b>checkInterval</b>.
     * This value should be considered to indicate when a session is
     * ripe for backing up.
     * <p>
     * So it is possible that a session may be idle for maxIdleBackup +
     * checkInterval seconds, plus the time it takes to handle other
     * session expiration, swapping, etc. tasks.
     *
     * @param backup The number of seconds after their last accessed
     * time when they should be written to the Store.
     */
    public void setMaxIdleBackup (int backup) {

        if (backup == this.maxIdleBackup)
            return;
        int oldBackup = this.maxIdleBackup;
        this.maxIdleBackup = backup;
        support.firePropertyChange("maxIdleBackup",
                                   new Integer(oldBackup),
                                   new Integer(this.maxIdleBackup));

    }


    /**
     * The time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public int getMaxIdleSwap() {

        return maxIdleSwap;

    }


    /**
     * Sets the time in seconds after which a session should be swapped out of
     * memory to disk.
     */
    public void setMaxIdleSwap(int max) {

        if (max == this.maxIdleSwap)
            return;
        int oldMaxIdleSwap = this.maxIdleSwap;
        this.maxIdleSwap = max;
        support.firePropertyChange("maxIdleSwap",
                                   new Integer(oldMaxIdleSwap),
                                   new Integer(this.maxIdleSwap));

    }


    /**
     * The minimum time in seconds that a session must be idle before
     * it can be swapped out of memory, or -1 if it can be swapped out
     * at any time.
     */
    public int getMinIdleSwap() {

        return minIdleSwap;

    }


    /**
     * Sets the minimum time in seconds that a session must be idle before
     * it can be swapped out of memory due to maxActiveSession. Set it to -1
     * if it can be swapped out at any time.
     */
    public void setMinIdleSwap(int min) {

        if (this.minIdleSwap == min)
            return;
        int oldMinIdleSwap = this.minIdleSwap;
        this.minIdleSwap = min;
        support.firePropertyChange("minIdleSwap",
                                   new Integer(oldMinIdleSwap),
                                   new Integer(this.minIdleSwap));

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
     * Get the started status.
     */
    protected boolean isStarted() {

        return started;

    }


    /**
     * Set the started flag
     */
    protected void setStarted(boolean started) {

        this.started = started;

    }


    /**
     * Set the Store object which will manage persistent Session
     * storage for this Manager.
     *
     * @param store the associated Store
     */
    public void setStore(Store store) {

        this.store = store;
        store.setManager(this);

    }


    /**
     * Return the Store object which manages persistent Session
     * storage for this Manager.
     */
    public Store getStore() {

        return (this.store);

    }



    /**
     * Indicates whether sessions are saved when the Manager is shut down
     * properly. This requires the unload() method to be called.
     */
    public boolean getSaveOnRestart() {

        return saveOnRestart;

    }


    /**
     * Set the option to save sessions to the Store when the Manager is
     * shut down, then loaded when the Manager starts again. If set to
     * false, any sessions found in the Store may still be picked up when
     * the Manager is started again.
     *
     * @param save true if sessions should be saved on restart, false if
     *     they should be ignored.
     */
    public void setSaveOnRestart(boolean saveOnRestart) {

        if (saveOnRestart == this.saveOnRestart)
            return;

        boolean oldSaveOnRestart = this.saveOnRestart;
        this.saveOnRestart = saveOnRestart;
        support.firePropertyChange("saveOnRestart",
                                   new Boolean(oldSaveOnRestart),
                                   new Boolean(this.saveOnRestart));

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Clear all sessions from the Store.
     */
    public void clearStore() {

        if (store == null)
            return;

        try {
            store.clear();
        } catch (IOException e) {
            log("Exception clearing the Store: " + e);
            e.printStackTrace();
        }

    }


    /**
     * Called by the background thread after active sessions have
     * been checked for expiration, to allow sessions to be
     * swapped out, backed up, etc.
     */
    public void processPersistenceChecks() {

            processMaxIdleSwaps();
            processMaxActiveSwaps();
            processMaxIdleBackups();

    }


    /**
     * Return a new session object as long as the number of active
     * sessions does not exceed <b>maxActiveSessions</b>. If there
     * aren't too many active sessions, or if there is no limit,
     * a session is created or retrieved from the recycled pool.
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
     * Return true, if the session id is loaded in memory
     * otherwise false is returned
     *
     * @param id The session id for the session to be searched for
     *
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public boolean isLoaded( String id ){
        try {
            if ( super.findSession(id) != null )
                return true;
        } catch (IOException e) {
            log("checking isLoaded for id, " + id + ", "+e.getMessage(), e);
        }
        return false;
    }


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     * This method checks the persistence store if persistence is enabled,
     * otherwise just uses the functionality from ManagerBase.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException {

        Session session = super.findSession(id);
        if (session != null)
            return (session);

        // See if the Session is in the Store
        session = swapIn(id);
        return (session);

    }


    /**
     * Load all sessions found in the persistence mechanism, assuming
     * they are marked as valid and have not passed their expiration
     * limit. If persistence is not supported, this method returns
     * without doing anything.
     * <p>
     * Note that by default, this method is not called by the MiddleManager
     * class. In order to use it, a subclass must specifically call it,
     * for example in the start() and/or processPersistenceChecks() methods.
     */
    public void load() {

        // Initialize our internal data structures
        recycled.clear();
        sessions.clear();

        if (store == null)
            return;

        String[] ids = null;
        try {
            ids = store.keys();
        } catch (IOException e) {
            log("Can't load sessions from store, " + e.getMessage(), e);
            return;
        }

        int n = ids.length;
        if (n == 0)
            return;

        if (debug >= 1)
            log(sm.getString("persistentManager.loading", String.valueOf(n)));

        for (int i = 0; i < n; i++)
            try {
                swapIn(ids[i]);
            } catch (IOException e) {
                log("Failed load session from store, " + e.getMessage(), e);
            }

    }


    /**
     * Remove this Session from the active Sessions for this Manager,
     * and from the Store.
     *
     * @param session Session to be removed
     */
    public void remove(Session session) {

        super.remove (session);

        if (store != null)
            try {
                store.remove(session.getId());
            } catch (IOException e) {
                log("Exception removing session  " + e.getMessage());
                e.printStackTrace();
            }

    }


    /**
     * Save all currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     * <p>
     * Note that by default, this method is not called by the MiddleManager
     * class. In order to use it, a subclass must specifically call it,
     * for example in the stop() and/or processPersistenceChecks() methods.
     */
    public void unload() {

        if (store == null)
            return;

        Session sessions[] = findSessions();
        int n = sessions.length;
        if (n == 0)
            return;

        if (debug >= 1)
            log(sm.getString("persistentManager.unloading",
                             String.valueOf(n)));

        for (int i = 0; i < n; i++)
            try {
                swapOut(sessions[i]);
            } catch (IOException e) {
                ;   // This is logged in writeSession()
            }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Look for a session in the Store and, if found, restore
     * it in the Manager's list of active sessions if appropriate.
     * The session will be removed from the Store after swapping
     * in, but will not be added to the active session list if it
     * is invalid or past its expiration.
     */
    protected Session swapIn(String id) throws IOException {

        if (store == null)
            return null;

        Session session = null;
        try {
            session = store.load(id);
        } catch (ClassNotFoundException e) {
            log(sm.getString("persistentManager.deserializeError", id, e));
            throw new IllegalStateException
                (sm.getString("persistentManager.deserializeError", id, e));
        }

        if (session == null)
            return (null);

        if (!session.isValid()
                || isSessionStale(session, System.currentTimeMillis())) {
            log("session swapped in is invalid or expired");
            session.expire();
            store.remove(id);
            return (null);
        }

        if(debug > 2)
            log(sm.getString("persistentManager.swapIn", id));

        session.setManager(this);
        add(session);
        ((StandardSession)session).activate();

        return (session);

    }


    /**
     * Remove the session from the Manager's list of active
     * sessions and write it out to the Store. If the session
     * is past its expiration or invalid, this method does
     * nothing.
     *
     * @param session The Session to write out.
     */
    protected void swapOut(Session session) throws IOException {

        if (store == null ||
                !session.isValid() ||
                isSessionStale(session, System.currentTimeMillis()))
            return;

        ((StandardSession)session).passivate();
        writeSession(session);
        super.remove(session);
        session.recycle();

    }


    /**
     * Write the provided session to the Store without modifying
     * the copy in memory or triggering passivation events. Does
     * nothing if the session is invalid or past its expiration.
     */
    protected void writeSession(Session session) throws IOException {

        if (store == null ||
                !session.isValid() ||
                isSessionStale(session, System.currentTimeMillis()))
            return;

        try {
            store.save(session);
        } catch (IOException e) {
            log(sm.getString
                ("persistentManager.serializeError", session.getId(), e));
            throw e;
        }

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

        if (store == null)
            log("No Store configured, persistence disabled");
        else if (store instanceof Lifecycle)
            ((Lifecycle)store).start();

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
        if (!isStarted())
            throw new LifecycleException
                (sm.getString("standardManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        setStarted(false);

        // Stop the background reaper thread
        threadStop();

        if (getStore() != null && saveOnRestart) {
            unload();
        } else {
            // Expire all active sessions
            Session sessions[] = findSessions();
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                session.expire();
            }
        }

        if (getStore() != null && getStore() instanceof Lifecycle)
            ((Lifecycle)getStore()).stop();

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
     * Indicate whether the session has been idle for longer
     * than its expiration date as of the supplied time.
     *
     * FIXME: Probably belongs in the Session class.
     */
    protected boolean isSessionStale(Session session, long timeNow) {

        int maxInactiveInterval = session.getMaxInactiveInterval();
        if (maxInactiveInterval >= 0) {
            int timeIdle = // Truncate, do not round up
                (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
            if (timeIdle >= maxInactiveInterval)
                return true;
        }

        return false;

    }


    /**
     * Invalidate all sessions that have expired.
     */
    protected void processExpires() {

        if (!started)
            return;

        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();

        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];
            if (!session.isValid())
                continue;
            if (isSessionStale(session, timeNow))
                session.expire();
        }

    }


    /**
     * Swap idle sessions out to Store if they are idle too long.
     */
    protected void processMaxIdleSwaps() {

        if (!isStarted() || maxIdleSwap < 0)
            return;

        Session sessions[] = findSessions();
        long timeNow = System.currentTimeMillis();

        // Swap out all sessions idle longer than maxIdleSwap
        // FIXME: What's preventing us from mangling a session during
        // a request?
        if (maxIdleSwap >= 0) {
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                int timeIdle = // Truncate, do not round up
                    (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle > maxIdleSwap && timeIdle > minIdleSwap) {
                    if (debug > 1)
                        log(sm.getString
                            ("persistentManager.swapMaxIdle",
                             session.getId(), new Integer(timeIdle)));
                    try {
                        swapOut(session);
                    } catch (IOException e) {
                        ;   // This is logged in writeSession()
                    }
                }
            }
        }

    }


    /**
     * Swap idle sessions out to Store if too many are active
     */
    protected void processMaxActiveSwaps() {

        if (!isStarted() || getMaxActiveSessions() < 0)
            return;

        Session sessions[] = findSessions();

        // FIXME: Smarter algorithm (LRU)
        if (getMaxActiveSessions() >= sessions.length)
            return;

        if(debug > 0)
            log(sm.getString
                ("persistentManager.tooManyActive",
                 new Integer(sessions.length)));

        int toswap = sessions.length - getMaxActiveSessions();
        long timeNow = System.currentTimeMillis();

        for (int i = 0; i < sessions.length && toswap > 0; i++) {
            int timeIdle = // Truncate, do not round up
                (int) ((timeNow - sessions[i].getLastAccessedTime()) / 1000L);
            if (timeIdle > minIdleSwap) {
                if(debug > 1)
                    log(sm.getString
                        ("persistentManager.swapTooManyActive",
                         sessions[i].getId(), new Integer(timeIdle)));
                try {
                    swapOut(sessions[i]);
                } catch (IOException e) {
                    ;   // This is logged in writeSession()
                }
                toswap--;
            }
        }

    }


    /**
     * Back up idle sessions.
     */
    protected void processMaxIdleBackups() {

        if (!isStarted() || maxIdleBackup < 0)
            return;

        Session sessions[] = findSessions();
        long timeNow = System.currentTimeMillis();

        // Back up all sessions idle longer than maxIdleBackup
        if (maxIdleBackup >= 0) {
            for (int i = 0; i < sessions.length; i++) {
                StandardSession session = (StandardSession) sessions[i];
                if (!session.isValid())
                    continue;
                int timeIdle = // Truncate, do not round up
                    (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
                if (timeIdle > maxIdleBackup) {
                    if (debug > 1)
                        log(sm.getString
                            ("persistentManager.backupMaxIdle",
                            session.getId(), new Integer(timeIdle)));

                    try {
                        writeSession(session);
                    } catch (IOException e) {
                        ;   // This is logged in writeSession()
                    }
                }
            }
        }

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
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {

        if (thread != null)
            return;

        threadDone = false;
        threadName = "StandardManager[" + container.getName() + "]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

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


    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            processExpires();
            processPersistenceChecks();
        }

    }


}
