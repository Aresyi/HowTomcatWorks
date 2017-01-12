/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/authenticator/SingleSignOn.java,v 1.11 2002/06/09 02:19:41 remm Exp $
 * $Revision: 1.11 $
 * $Date: 2002/06/09 02:19:41 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.catalina.authenticator;


import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.ValveContext;
import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * A <strong>Valve</strong> that supports a "single sign on" user experience,
 * where the security identity of a user who successfully authenticates to one
 * web application is propogated to other web applications in the same
 * security domain.  For successful use, the following requirements must
 * be met:
 * <ul>
 * <li>This Valve must be configured on the Container that represents a
 *     virtual host (typically an implementation of <code>Host</code>).</li>
 * <li>The <code>Realm</code> that contains the shared user and role
 *     information must be configured on the same Container (or a higher
 *     one), and not overridden at the web application level.</li>
 * <li>The web applications themselves must use one of the standard
 *     Authenticators found in the
 *     <code>org.apache.catalina.authenticator</code> package.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.11 $ $Date: 2002/06/09 02:19:41 $
 */

public class SingleSignOn
    extends ValveBase
    implements Lifecycle, SessionListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The cache of SingleSignOnEntry instances for authenticated Principals,
     * keyed by the cookie value that is used to select them.
     */
    protected HashMap cache = new HashMap();


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Descriptive information about this Valve implementation.
     */
    protected static String info =
        "org.apache.catalina.authenticator.SingleSignOn";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The cache of single sign on identifiers, keyed by the Session that is
     * associated with them.
     */
    protected HashMap reverse = new HashMap();


    /**
     * The string manager for this package.
     */
    protected final static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Component started flag.
     */
    protected boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

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

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("authenticator.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        if (debug >= 1)
            log("Started");

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
                (sm.getString("authenticator.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        if (debug >= 1)
            log("Stopped");

    }


    // ------------------------------------------------ SessionListener Methods


    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event SessionEvent that has occurred
     */
    public void sessionEvent(SessionEvent event) {

        // We only care about session destroyed events
        if (!Session.SESSION_DESTROYED_EVENT.equals(event.getType()))
            return;

        // Look up the single session id associated with this session (if any)
        Session session = event.getSession();
        if (debug >= 1)
            log("Process session destroyed on " + session);
        String ssoId = null;
        synchronized (reverse) {
            ssoId = (String) reverse.get(session);
        }
        if (ssoId == null)
            return;

        // Deregister this single session id, invalidating associated sessions
        deregister(ssoId);

    }


    // ---------------------------------------------------------- Valve Methods


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Perform single-sign-on support processing for this request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response,
                       ValveContext context)
        throws IOException, ServletException {

        // If this is not an HTTP request and response, just pass them on
        if (!(request instanceof HttpRequest) ||
            !(response instanceof HttpResponse)) {
            context.invokeNext(request, response);
            return;
        }
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();
        request.removeNote(Constants.REQ_SSOID_NOTE);

        // Has a valid user already been authenticated?
        if (debug >= 1)
            log("Process request for '" + hreq.getRequestURI() + "'");
        if (hreq.getUserPrincipal() != null) {
            if (debug >= 1)
                log(" Principal '" + hreq.getUserPrincipal().getName() +
                    "' has already been authenticated");
            context.invokeNext(request, response);
            return;
        }

        // Check for the single sign on cookie
        if (debug >= 1)
            log(" Checking for SSO cookie");
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (Constants.SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }
        if (cookie == null) {
            if (debug >= 1)
                log(" SSO cookie is not present");
            context.invokeNext(request, response);
            return;
        }

        // Look up the cached Principal associated with this cookie value
        if (debug >= 1)
            log(" Checking for cached principal for " + cookie.getValue());
        SingleSignOnEntry entry = lookup(cookie.getValue());
        if (entry != null) {
            if (debug >= 1)
                log(" Found cached principal '" +
                    entry.principal.getName() + "' with auth type '" +
                    entry.authType + "'");
            request.setNote(Constants.REQ_SSOID_NOTE, cookie.getValue());
            ((HttpRequest) request).setAuthType(entry.authType);
            ((HttpRequest) request).setUserPrincipal(entry.principal);
        } else {
            if (debug >= 1)
                log(" No cached principal found, erasing SSO cookie");
            cookie.setMaxAge(0);
            hres.addCookie(cookie);
        }

        // Invoke the next Valve in our pipeline
        context.invokeNext(request, response);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SingleSignOn[");
        sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Associate the specified single sign on identifier with the
     * specified Session.
     *
     * @param ssoId Single sign on identifier
     * @param session Session to be associated
     */
    void associate(String ssoId, Session session) {

        if (debug >= 1)
            log("Associate sso id " + ssoId + " with session " + session);

        SingleSignOnEntry sso = lookup(ssoId);
        if (sso != null)
            sso.addSession(this, session);
        synchronized (reverse) {
            reverse.put(session, ssoId);
        }

    }


    /**
     * Deregister the specified single sign on identifier, and invalidate
     * any associated sessions.
     *
     * @param ssoId Single sign on identifier to deregister
     */
    void deregister(String ssoId) {

        if (debug >= 1)
            log("Deregistering sso id '" + ssoId + "'");

        // Look up and remove the corresponding SingleSignOnEntry
        SingleSignOnEntry sso = null;
        synchronized (cache) {
            sso = (SingleSignOnEntry) cache.remove(ssoId);
        }
        if (sso == null)
            return;

        // Expire any associated sessions
        Session sessions[] = sso.findSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (debug >= 2)
                log(" Invalidating session " + sessions[i]);
            // Remove from reverse cache first to avoid recursion
            synchronized (reverse) {
                reverse.remove(sessions[i]);
            }
            // Invalidate this session
            sessions[i].expire();
        }

        // NOTE:  Clients may still possess the old single sign on cookie,
        // but it will be removed on the next request since it is no longer
        // in the cache

    }


    /**
     * Register the specified Principal as being associated with the specified
     * value for the single sign on identifier.
     *
     * @param ssoId Single sign on identifier to register
     * @param principal Associated user principal that is identified
     * @param authType Authentication type used to authenticate this
     *  user principal
     * @param username Username used to authenticate this user
     * @param password Password used to authenticate this user
     */
    void register(String ssoId, Principal principal, String authType,
                  String username, String password) {

        if (debug >= 1)
            log("Registering sso id '" + ssoId + "' for user '" +
                principal.getName() + "' with auth type '" + authType + "'");

        synchronized (cache) {
            cache.put(ssoId, new SingleSignOnEntry(principal, authType,
                                                   username, password));
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message);
        else
            System.out.println(this.toString() + ": " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message, throwable);
        else {
            System.out.println(this.toString() + ": " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Look up and return the cached SingleSignOn entry associated with this
     * sso id value, if there is one; otherwise return <code>null</code>.
     *
     * @param ssoId Single sign on identifier to look up
     */
    protected SingleSignOnEntry lookup(String ssoId) {

        synchronized (cache) {
            return ((SingleSignOnEntry) cache.get(ssoId));
        }

    }


}


// ------------------------------------------------------------ Private Classes


/**
 * A private class representing entries in the cache of authenticated users.
 */
class SingleSignOnEntry {

    public String authType = null;

    public String password = null;

    public Principal principal = null;

    public Session sessions[] = new Session[0];

    public String username = null;

    public SingleSignOnEntry(Principal principal, String authType,
                             String username, String password) {
        super();
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = password;
    }

    public synchronized void addSession(SingleSignOn sso, Session session) {
        for (int i = 0; i < sessions.length; i++) {
            if (session == sessions[i])
                return;
        }
        Session results[] = new Session[sessions.length + 1];
        System.arraycopy(sessions, 0, results, 0, sessions.length);
        results[sessions.length] = session;
        sessions = results;
        session.addSessionListener(sso);
    }

    public synchronized Session[] findSessions() {
        return (this.sessions);
    }

}
