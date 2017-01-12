/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/Embedded.java,v 1.16 2002/06/09 02:19:44 remm Exp $
 * $Revision: 1.16 $
 * $Date: 2002/06/09 02:19:44 $
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


package org.apache.catalina.startup;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.InetAddress;

import org.apache.tomcat.util.IntrospectionUtils;

import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.logger.FileLogger;
import org.apache.catalina.logger.SystemOutLogger;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Convenience class to embed a Catalina servlet container environment
 * inside another application.  You must call the methods of this class in the
 * following order to ensure correct operation.
 *
 * <ul>
 * <li>Instantiate a new instance of this class.</li>
 * <li>Set the relevant properties of this object itself.  In particular,
 *     you will want to establish the default Logger to be used, as well
 *     as the default Realm if you are using container-managed security.</li>
 * <li>Call <code>createEngine()</code> to create an Engine object, and then
 *     call its property setters as desired.</li>
 * <li>Call <code>createHost()</code> to create at least one virtual Host
 *     associated with the newly created Engine, and then call its property
 *     setters as desired.  After you customize this Host, add it to the
 *     corresponding Engine with <code>engine.addChild(host)</code>.</li>
 * <li>Call <code>createContext()</code> to create at least one Context
 *     associated with each newly created Host, and then call its property
 *     setters as desired.  You <strong>SHOULD</strong> create a Context with
 *     a pathname equal to a zero-length string, which will be used to process
 *     all requests not mapped to some other Context.  After you customize
 *     this Context, add it to the corresponding Host with
 *     <code>host.addChild(context)</code>.</li>
 * <li>Call <code>addEngine()</code> to attach this Engine to the set of
 *     defined Engines for this object.</li>
 * <li>Call <code>createConnector()</code> to create at least one TCP/IP
 *     connector, and then call its property setters as desired.</li>
 * <li>Call <code>addConnector()</code> to attach this Connector to the set
 *     of defined Connectors for this object.  The added Connector will use
 *     the most recently added Engine to process its received requests.</li>
 * <li>Repeat the above series of steps as often as required (although there
 *     will typically be only one Engine instance created).</li>
 * <li>Call <code>start()</code> to initiate normal operations of all the
 *     attached components.</li>
 * </ul>
 *
 * After normal operations have begun, you can add and remove Connectors,
 * Engines, Hosts, and Contexts on the fly.  However, once you have removed
 * a particular component, it must be thrown away -- you can create a new one
 * with the same characteristics if you merely want to do a restart.
 * <p>
 * To initiate a normal shutdown, call the <code>stop()</code> method of
 * this object.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>:  The <code>main()</code> method of
 * this class is a simple example that exercizes the features of dynamically
 * starting and stopping various components.  You can execute this by executing
 * the following steps (on a Unix platform):
 * <pre>
 *   cd $CATALINA_HOME
 *   ./bin/catalina.sh embedded
 * </pre>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.16 $ $Date: 2002/06/09 02:19:44 $
 */

public class Embedded implements Lifecycle {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class with default properties.
     */
    public Embedded() {

        this(null, null);

    }


    /**
     * Construct a new instance of this class with specified properties.
     *
     * @param logger Logger implementation to be inherited by all components
     *  (unless overridden further down the container hierarchy)
     * @param realm Realm implementation to be inherited by all components
     *  (unless overridden further down the container hierarchy)
     */
    public Embedded(Logger logger, Realm realm) {

        super();
        setLogger(logger);
        setRealm(realm);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of Connectors that have been deployed in this server.
     */
    protected Connector connectors[] = new Connector[0];


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;


    /**
     * The set of Engines that have been deployed in this server.  Normally
     * there will only be one.
     */
    protected Engine engines[] = new Engine[0];


    /**
     * Descriptive information about this server implementation.
     */
    protected static final String info =
        "org.apache.catalina.startup.Embedded/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The default logger to be used by this component itself.  Unless this
     * is overridden, log messages will be writted to standard output.
     */
    protected Logger logger = null;


    /**
     * The default realm to be used by all containers associated with
     * this compoennt.
     */
    protected Realm realm = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The socket factory that will be used when a <code>secure</code>
     * Connector is created.  If a standard Connector is created, the
     * internal (to the Connector class default socket factory class)
     * will be used instead.
     */
    protected String socketFactory =
        "org.apache.catalina.net.SSLSocketFactory";


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    // ------------------------------------------------------------- Properties


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

        int oldDebug = this.debug;
        this.debug = debug;
        support.firePropertyChange("debug", new Integer(oldDebug),
                                   new Integer(this.debug));

    }


    /**
     * Return true if naming is enabled.
     */
    public boolean isUseNaming() {

        return (this.useNaming);

    }


    /**
     * Enables or disables naming support.
     *
     * @param useNaming The new use naming value
     */
    public void setUseNaming(boolean useNaming) {

        boolean oldUseNaming = this.useNaming;
        this.useNaming = useNaming;
        support.firePropertyChange("useNaming", new Boolean(oldUseNaming),
                                   new Boolean(this.useNaming));

    }


    /**
     * Return the Logger for this component.
     */
    public Logger getLogger() {

        return (this.logger);

    }


    /**
     * Set the Logger for this component.
     *
     * @param logger The new logger
     */
    public void setLogger(Logger logger) {

        Logger oldLogger = this.logger;
        this.logger = logger;
        support.firePropertyChange("logger", oldLogger, this.logger);

    }


    /**
     * Return the default Realm for our Containers.
     */
    public Realm getRealm() {

        return (this.realm);

    }


    /**
     * Set the default Realm for our Containers.
     *
     * @param realm The new default realm
     */
    public void setRealm(Realm realm) {

        Realm oldRealm = this.realm;
        this.realm = realm;
        support.firePropertyChange("realm", oldRealm, this.realm);

    }


    /**
     * Return the secure socket factory class name.
     */
    public String getSocketFactory() {

        return (this.socketFactory);

    }


    /**
     * Set the secure socket factory class name.
     *
     * @param socketFactory The new secure socket factory class name
     */
    public void setSocketFactory(String socketFactory) {

        this.socketFactory = socketFactory;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors.  The newly
     * added Connector will be associated with the most recently added Engine.
     *
     * @param connector The connector to be added
     *
     * @exception IllegalStateException if no engines have been added yet
     */
    public synchronized void addConnector(Connector connector) {

        if (debug >= 1) {
            logger.log("Adding connector (" + connector.getInfo() + ")");
        }

        // Make sure we have a Container to send requests to
        if (engines.length < 1)
            throw new IllegalStateException
                (sm.getString("embedded.noEngines"));

        // Configure this Connector as needed
        connector.setContainer(engines[engines.length - 1]);

        // Add this Connector to our set of defined Connectors
        Connector results[] = new Connector[connectors.length + 1];
        for (int i = 0; i < connectors.length; i++)
            results[i] = connectors[i];
        results[connectors.length] = connector;
        connectors = results;

        // Start this Connector if necessary
        if (started) {
            try {
                connector.initialize();
                if (connector instanceof Lifecycle) {
                    ((Lifecycle) connector).start();
                }
            } catch (LifecycleException e) {
                logger.log("Connector.start", e);
            }
        }

    }


    /**
     * Add a new Engine to the set of defined Engines.
     *
     * @param engine The engine to be added
     */
    public synchronized void addEngine(Engine engine) {

        if (debug >= 1)
            logger.log("Adding engine (" + engine.getInfo() + ")");

        // Add this Engine to our set of defined Engines
        Engine results[] = new Engine[engines.length + 1];
        for (int i = 0; i < engines.length; i++)
            results[i] = engines[i];
        results[engines.length] = engine;
        engines = results;

        // Start this Engine if necessary
        if (started && (engine instanceof Lifecycle)) {
            try {
                ((Lifecycle) engine).start();
            } catch (LifecycleException e) {
                logger.log("Engine.start", e);
            }
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
     * Create, configure, and return a new TCP/IP socket connector
     * based on the specified properties.
     *
     * @param address InetAddress to listen to, or <code>null</code>
     *  to listen on all address on this server
     * @param port Port number to listen to
     * @param secure Should this port be SSL-enabled?
     */
    public Connector createConnector(InetAddress address, int port,
                                     boolean secure) {

        if (debug >= 1)
            logger.log("Creating connector for address='" +
                       ((address == null) ? "ALL" : address.getHostAddress()) +
                       "' port='" + port + "' secure='" + secure + "'");

        String protocol = "http";
        if (secure) {
            protocol = "https";
        }

        return createConnector(address, port, protocol);

    }


    public Connector createConnector(InetAddress address, int port,
                                     String protocol) {

        Connector connector = null;

        try {

            Class clazz = 
                Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
            connector = (Connector) clazz.newInstance();

            if (address != null) {
                IntrospectionUtils.setProperty(connector, "address", 
                                               "" + address);
            }
            IntrospectionUtils.setProperty(connector, "port", "" + port);
            IntrospectionUtils.setProperty(connector, "useURIValidationHack", 
                                           "" + false);

            if (protocol.equals("ajp")) {
                IntrospectionUtils.setProperty
                    (connector, "protocolHandlerClassName",
                     "org.apache.jk.server.JkCoyoteHandler");
            } else if (protocol.equals("https")) {
                connector.setScheme("https");
                connector.setSecure(true);
                try {
                    Class serverSocketFactoryClass = Class.forName
                        ("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
                    ServerSocketFactory factory = 
                        (ServerSocketFactory) 
                        serverSocketFactoryClass.newInstance();
                    connector.setFactory(factory);
                } catch (Exception e) {
                    logger.log("Couldn't load SSL server socket factory.");
                }
            }

        } catch (Exception e) {
            logger.log("Couldn't create connector.");
        } 

        return (connector);

    }


    /**
     * Create, configure, and return a Context that will process all
     * HTTP requests received from one of the associated Connectors,
     * and directed to the specified context path on the virtual host
     * to which this Context is connected.
     * <p>
     * After you have customized the properties, listeners, and Valves
     * for this Context, you must attach it to the corresponding Host
     * by calling:
     * <pre>
     *   host.addChild(context);
     * </pre>
     * which will also cause the Context to be started if the Host has
     * already been started.
     *
     * @param path Context path of this application ("" for the default
     *  application for this host, must start with a slash otherwise)
     * @param docBase Absolute pathname to the document base directory
     *  for this web application
     *
     * @exception IllegalArgumentException if an invalid parameter
     *  is specified
     */
    public Context createContext(String path, String docBase) {

        if (debug >= 1)
            logger.log("Creating context '" + path + "' with docBase '" +
                       docBase + "'");

        StandardContext context = new StandardContext();

        context.setDebug(debug);
        context.setDocBase(docBase);
        context.setPath(path);

        ContextConfig config = new ContextConfig();
        config.setDebug(debug);
        ((Lifecycle) context).addLifecycleListener(config);

        return (context);

    }


    /**
     * Create, configure, and return an Engine that will process all
     * HTTP requests received from one of the associated Connectors,
     * based on the specified properties.
     */
    public Engine createEngine() {

        if (debug >= 1)
            logger.log("Creating engine");

        StandardEngine engine = new StandardEngine();

        engine.setDebug(debug);
        // Default host will be set to the first host added
        engine.setLogger(logger);       // Inherited by all children
        engine.setRealm(realm);         // Inherited by all children

        return (engine);

    }


    /**
     * Create, configure, and return a Host that will process all
     * HTTP requests received from one of the associated Connectors,
     * and directed to the specified virtual host.
     * <p>
     * After you have customized the properties, listeners, and Valves
     * for this Host, you must attach it to the corresponding Engine
     * by calling:
     * <pre>
     *   engine.addChild(host);
     * </pre>
     * which will also cause the Host to be started if the Engine has
     * already been started.  If this is the default (or only) Host you
     * will be defining, you may also tell the Engine to pass all requests
     * not assigned to another virtual host to this one:
     * <pre>
     *   engine.setDefaultHost(host.getName());
     * </pre>
     *
     * @param name Canonical name of this virtual host
     * @param appBase Absolute pathname to the application base directory
     *  for this virtual host
     *
     * @exception IllegalArgumentException if an invalid parameter
     *  is specified
     */
    public Host createHost(String name, String appBase) {

        if (debug >= 1)
            logger.log("Creating host '" + name + "' with appBase '" +
                       appBase + "'");

        StandardHost host = new StandardHost();

        host.setAppBase(appBase);
        host.setDebug(debug);
        host.setName(name);

        return (host);

    }


    /**
     * Create and return a class loader manager that can be customized, and
     * then attached to a Context, before it is started.
     *
     * @param parent ClassLoader that will be the parent of the one
     *  created by this Loader
     */
    public Loader createLoader(ClassLoader parent) {

        if (debug >= 1)
            logger.log("Creating Loader with parent class loader '" +
                       parent + "'");

        WebappLoader loader = new WebappLoader(parent);
        return (loader);

    }


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Remove the specified Connector from the set of defined Connectors.
     *
     * @param connector The Connector to be removed
     */
    public synchronized void removeConnector(Connector connector) {

        if (debug >= 1) {
            logger.log("Removing connector (" + connector.getInfo() + ")");
        }

        // Is the specified Connector actually defined?
        int j = -1;
        for (int i = 0; i < connectors.length; i++) {
            if (connector == connectors[i]) {
                j = i;
                break;
            }
        }
        if (j < 0)
            return;

        // Stop this Connector if necessary
        if (connector instanceof Lifecycle) {
            if (debug >= 1)
                logger.log(" Stopping this Connector");
            try {
                ((Lifecycle) connector).stop();
            } catch (LifecycleException e) {
                logger.log("Connector.stop", e);
            }
        }

        // Remove this Connector from our set of defined Connectors
        if (debug >= 1)
            logger.log(" Removing this Connector");
        int k = 0;
        Connector results[] = new Connector[connectors.length - 1];
        for (int i = 0; i < connectors.length; i++) {
            if (i != j)
                results[k++] = connectors[i];
        }
        connectors = results;

    }


    /**
     * Remove the specified Context from the set of defined Contexts for its
     * associated Host.  If this is the last Context for this Host, the Host
     * will also be removed.
     *
     * @param context The Context to be removed
     */
    public synchronized void removeContext(Context context) {

        if (debug >= 1)
            logger.log("Removing context[" + context.getPath() + "]");

        // Is this Context actually among those that are defined?
        boolean found = false;
        for (int i = 0; i < engines.length; i++) {
            Container hosts[] = engines[i].findChildren();
            for (int j = 0; j < hosts.length; j++) {
                Container contexts[] = hosts[j].findChildren();
                for (int k = 0; k < contexts.length; k++) {
                    if (context == (Context) contexts[k]) {
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }
            if (found)
                break;
        }
        if (!found)
            return;

        // Remove this Context from the associated Host
        if (debug >= 1)
            logger.log(" Removing this Context");
        context.getParent().removeChild(context);

    }


    /**
     * Remove the specified Engine from the set of defined Engines, along with
     * all of its related Hosts and Contexts.  All associated Connectors are
     * also removed.
     *
     * @param engine The Engine to be removed
     */
    public synchronized void removeEngine(Engine engine) {

        if (debug >= 1)
            logger.log("Removing engine (" + engine.getInfo() + ")");

        // Is the specified Engine actually defined?
        int j = -1;
        for (int i = 0; i < engines.length; i++) {
            if (engine == engines[i]) {
                j = i;
                break;
            }
        }
        if (j < 0)
            return;

        // Remove any Connector that is using this Engine
        if (debug >= 1)
            logger.log(" Removing related Containers");
        while (true) {
            int n = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connectors[i].getContainer() == (Container) engine) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                break;
            removeConnector(connectors[n]);
        }

        // Stop this Engine if necessary
        if (engine instanceof Lifecycle) {
            if (debug >= 1)
                logger.log(" Stopping this Engine");
            try {
                ((Lifecycle) engine).stop();
            } catch (LifecycleException e) {
                logger.log("Engine.stop", e);
            }
        }

        // Remove this Engine from our set of defined Engines
        if (debug >= 1)
            logger.log(" Removing this Engine");
        int k = 0;
        Engine results[] = new Engine[engines.length - 1];
        for (int i = 0; i < engines.length; i++) {
            if (i != j)
                results[k++] = engines[i];
        }
        engines = results;

    }


    /**
     * Remove the specified Host, along with all of its related Contexts,
     * from the set of defined Hosts for its associated Engine.  If this is
     * the last Host for this Engine, the Engine will also be removed.
     *
     * @param host The Host to be removed
     */
    public synchronized void removeHost(Host host) {

        if (debug >= 1)
            logger.log("Removing host[" + host.getName() + "]");

        // Is this Host actually among those that are defined?
        boolean found = false;
        for (int i = 0; i < engines.length; i++) {
            Container hosts[] = engines[i].findChildren();
            for (int j = 0; j < hosts.length; j++) {
                if (host == (Host) hosts[j]) {
                    found = true;
                    break;

                }
            }
            if (found)
                break;
        }
        if (!found)
            return;

        // Remove this Host from the associated Engine
        if (debug >= 1)
            logger.log(" Removing this Host");
        host.getParent().removeChild(host);

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
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        if (debug >= 1)
            logger.log("Starting embedded server");

        // Validate the setup of our required system properties
        if (System.getProperty("catalina.home") == null) {
            // Backwards compatibility patch for J2EE RI 1.3
            String j2eeHome = System.getProperty("com.sun.enterprise.home");
            if (j2eeHome != null)
                System.setProperty
                    ("catalina.home",
                     System.getProperty("com.sun.enterprise.home"));
            else
                throw new LifecycleException
                    ("Must set 'catalina.home' system property");
        }
        if (System.getProperty("catalina.base") == null)
            System.setProperty("catalina.base",
                               System.getProperty("catalina.home"));

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("embedded.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Initialize some naming specific properties
        if (!useNaming) {
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = oldValue + ":" + value;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                               "org.apache.naming.java.javaURLContextFactory");
        }

        // Start our defined Engines first
        for (int i = 0; i < engines.length; i++) {
            if (engines[i] instanceof Lifecycle)
                ((Lifecycle) engines[i]).start();
        }

        // Start our defined Connectors second
        for (int i = 0; i < connectors.length; i++) {
            connectors[i].initialize();
            if (connectors[i] instanceof Lifecycle)
                ((Lifecycle) connectors[i]).start();
        }

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
            logger.log("Stopping embedded server");

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("embedded.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop our defined Connectors first
        for (int i = 0; i < connectors.length; i++) {
            if (connectors[i] instanceof Lifecycle)
                ((Lifecycle) connectors[i]).stop();
        }

        // Stop our defined Engines second
        for (int i = 0; i < engines.length; i++) {
            if (engines[i] instanceof Lifecycle)
                ((Lifecycle) engines[i]).stop();
        }

    }


    // ------------------------------------------------------ Protected Methods


    // -------------------------------------------------------- Private Methods


    // ----------------------------------------------------------- Main Program


    /**
     * This main program is a unit test to exercize the various methods of
     * the Embedded class.  It can be used as an example of the type of code
     * that would be used in a real environment.
     *
     * @param args The command line arguments
     */
    public static void main(String args[]) {

        Embedded embedded = new Embedded(new SystemOutLogger(),
                                         new MemoryRealm());
        embedded.setDebug(5);
        embedded.setLogger(new SystemOutLogger());
        String home = System.getProperty("catalina.home");
        if (home == null) {
            System.err.println("You must set the 'catalina.home' system property");
            System.exit(1);
        }
        String base = System.getProperty("catalina.base");
        if (base == null) {
            base = home;
            System.setProperty("catalina.base", base);
        }

        // Start up this embedded server (to prove we can dynamically
        // add and remove containers and connectors later)
        try {
            embedded.start();
        } catch (LifecycleException e) {
            System.err.println("start: " + e.toString());
            e.printStackTrace();
        }

        // Assemble and install a very basic container hierarchy
        // that simulates a portion of the one configured in server.xml
        // by default
        Engine engine = embedded.createEngine();
        engine.setDefaultHost("localhost");

        Host host = embedded.createHost("localhost", home + "/webapps");
        engine.addChild(host);

        Context root = embedded.createContext("", home + "/webapps/ROOT");
        host.addChild(root);

        Context examples = embedded.createContext("/examples",
                                                  home + "/webapps/examples");
        customize(examples);    // Special customization for this web-app
        host.addChild(examples);

        // As an alternative to the three lines above, there is also a very
        // simple method to deploy a new application that has default values
        // for all context properties:
        //   String contextPath = ... context path for this app ...
        //   URL docRoot = ... URL of WAR file or unpacked directory ...
        //   ((Deployer) host).deploy(contextPath, docRoot);

        // Install the assembled container hierarchy
        embedded.addEngine(engine);

        // Assemble and install a non-secure connector for port 8080
        Connector connector =
            embedded.createConnector(null, 8080, false);
        embedded.addConnector(connector);

        // Pause for a while to allow brief testing
        // (In reality this would last until the enclosing application
        // needs to be shut down)
        try {
            Thread.sleep(2 * 60 * 1000L);       // Two minutes
        } catch (InterruptedException e) {
            ;
        }

        // Remove the examples context dynamically
        embedded.removeContext(examples);

        // Remove the engine (which should trigger removing the connector)
        embedded.removeEngine(engine);

        // Shut down this embedded server (should have nothing left to do)
        try {
            embedded.stop();
        } catch (LifecycleException e) {
            System.err.println("stop: " + e.toString());
            e.printStackTrace();
        }

    }


    /**
     * Customize the specified context to have its own log file instead of
     * inheriting the default one.  This is just an example of what you can
     * do; pretty much anything (such as installing special Valves) can
     * be done prior to calling <code>start()</code>.
     *
     * @param context Context to receive a specialized logger
     */
    private static void customize(Context context) {

        // Create a customized file logger for this context
        String basename = context.getPath();
        if (basename.length() < 1)
            basename = "ROOT";
        else
            basename = basename.substring(1);

        FileLogger special = new FileLogger();
        special.setPrefix(basename + "_log.");
        special.setSuffix(".txt");
        special.setTimestamp(true);

        // Override the default logger for this context
        context.setLogger(special);

    }


}
