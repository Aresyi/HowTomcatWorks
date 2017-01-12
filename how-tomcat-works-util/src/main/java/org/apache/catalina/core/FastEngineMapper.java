/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/FastEngineMapper.java,v 1.4 2002/06/09 02:19:42 remm Exp $
 * $Revision: 1.4 $
 * $Date: 2002/06/09 02:19:42 $
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


package org.apache.catalina.core;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Mapper;
import org.apache.catalina.Request;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of <code>Mapper</code> for an <code>Engine</code>,
 * designed to process HTTP requests.  This mapper selects an appropriate
 * <code>Host</code> based on the server name included in the request.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  This Mapper only works with a
 * <code>StandardEngine</code>, because it relies on internal APIs.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/06/09 02:19:42 $
 */

public final class FastEngineMapper
    implements ContainerListener, Lifecycle, Mapper, PropertyChangeListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * Cache of hostname -> Host mappings.  FIXME - use FastHashMap.
     */
    private java.util.HashMap cache = new java.util.HashMap();


    /**
     * The default host used for unknown host names.
     */
    private Host defaultHost = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The Container with which this Mapper is associated.
     */
    private StandardEngine engine = null;


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The protocol with which this Mapper is associated.
     */
    private String protocol = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Mapper is associated.
     */
    public Container getContainer() {

        return (engine);

    }


    /**
     * Set the Container with which this Mapper is associated.
     *
     * @param container The newly associated Container
     *
     * @exception IllegalArgumentException if this Container is not
     *  acceptable to this Mapper
     */
    public void setContainer(Container container) {

        if (!(container instanceof StandardEngine))
            throw new IllegalArgumentException
                (sm.getString("httpEngineMapper.container"));
        engine = (StandardEngine) container;

    }


    /**
     * Return the protocol for which this Mapper is responsible.
     */
    public String getProtocol() {

        return (this.protocol);

    }


    /**
     * Set the protocol for which this Mapper is responsible.
     *
     * @param protocol The newly associated protocol
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the child Container that should be used to process this Request,
     * based upon its characteristics.  If no such child Container can be
     * identified, return <code>null</code> instead.
     *
     * @param request Request being processed
     * @param update Update the Request to reflect the mapping selection?
     */
    public Container map(Request request, boolean update) {

        debug = engine.getDebug();

        // Extract the requested server name
        String server = request.getRequest().getServerName();
        if (server == null) {
            server = engine.getDefaultHost();
            if (update)
                request.setServerName(server);
        }
        if (server == null)
            return (null);
        if (debug >= 1)
            engine.log("Mapping server name '" + server + "'");

        // Find the specified host in our cache
        if (debug >= 2)
            engine.log(" Trying a cache match");
        Host host = (Host) cache.get(server);

        // Map to the default host if any
        if ((host == null) && (defaultHost != null)) {
            if (debug >= 2)
                engine.log(" Mapping to default host");
            host = defaultHost;
            addAlias(server, host);
        }

        // Update the Request if requested, and return the selected Host
        ;       // No update to the Request is required
        return (host);

    }


    // ---------------------------------------------- ContainerListener Methods


    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event ContainerEvent that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        Container source = (Container) event.getSource();
        String type = event.getType();
        if (source == engine) {
            if (Container.ADD_CHILD_EVENT.equals(type))
                addHost((Host) event.getData());
            else if (Container.REMOVE_CHILD_EVENT.equals(type))
                removeHost((Host) event.getData());
        } else if (source instanceof Host) {
            if (Host.ADD_ALIAS_EVENT.equals(type))
                addAlias((String) event.getData(), (Host) source);
            else if (Host.REMOVE_ALIAS_EVENT.equals(type))
                removeAlias((String) event.getData());
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
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("fastEngineMapper.alreadyStarted",
                              engine.getName()));
        started = true;

        // Configure based on our associated Engine properties
        engine.addContainerListener(this);
        engine.addPropertyChangeListener(this);
        setDefaultHost(engine.getDefaultHost());

        // Cache mappings for our child hosts
        Container children[] = engine.findChildren();
        for (int i = 0; i < children.length; i++) {
            addHost((Host) children[i]);
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("fastEngineMapper.notStarted",
                              engine.getName()));

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Deconfigure based on our associated Engine properties
        engine.removePropertyChangeListener(this);
        setDefaultHost(null);
        engine.removeContainerListener(this);

        // Clear our mapping cache
        cache.clear();

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process a property change event.
     */
    public void propertyChange(PropertyChangeEvent event) {

        Object source = event.getSource();
        if (source instanceof Engine) {
            if ("defaultHost".equals(event.getPropertyName()))
                setDefaultHost((String) event.getNewValue());
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Add an alias for the specified host.
     *
     * @param alias New alias name
     * @param host Host to resolve to
     */
    private void addAlias(String alias, Host host) {

        if (debug >= 3)
            engine.log("Adding alias '" + alias + "' for host '" +
                       host.getName() + "'");
        cache.put(alias.toLowerCase(), host);

    }


    /**
     * Add a new child Host to our associated Engine.
     *
     * @param host Child host to add
     */
    private void addHost(Host host) {

        if (debug >= 3)
            engine.log("Adding host '" + host.getName() + "'");

        host.addContainerListener(this);

        // Register the host name
        addAlias(host.getName(), host);

        // Register all associated aliases
        String aliases[] = host.findAliases();
        for (int i = 0; i < aliases.length; i++)
            addAlias(aliases[i], host);

    }


    /**
     * Return the Host that matches the specified name (or alias), if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name or alias of the desired Host
     */
    private Host findHost(String name) {

        return ((Host) cache.get(name.toLowerCase()));

    }


    /**
     * Remove the specified alias from our cache.
     *
     * @param alias Alias to remove
     */
    private void removeAlias(String alias) {

        if (debug >= 3)
            engine.log("Removing alias '" + alias + "'");
        cache.remove(alias.toLowerCase());

    }


    /**
     * Remove an existing child Host from our associated Engine.
     *
     * @param host Host to be removed
     */
    private void removeHost(Host host) {

        if (debug >= 3)
            engine.log("Removing host '" + host.getName() + "'");

        host.removeContainerListener(this);

        // Identify all names mapped to this host
        ArrayList removes = new ArrayList();
        Iterator keys = cache.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (host.equals((Host) cache.get(key)))
                removes.add(key);
        }

        // Remove the associated names
        keys = removes.iterator();
        while (keys.hasNext()) {
            removeAlias((String) keys.next());
        }

    }


    /**
     * Set the default Host used for resolving unknown host names.
     *
     * @param name Name of the default host
     */
    private void setDefaultHost(String name) {

        if (debug >= 3)
            engine.log("Setting default host '" + name + "'");

        if (name == null)
            defaultHost = null;
        else
            defaultHost = (Host) engine.findChild(name);

    }


}
