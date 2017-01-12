/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/EmbeddedManagerMBean.java,v 1.4 2001/07/22 20:25:13 pier Exp $
 * $Revision: 1.4 $
 * $Date: 2001/07/22 20:25:13 $
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

import java.net.InetAddress;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;


/**
 * Embedded MBean interface.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.4 $
 */

public interface EmbeddedManagerMBean {


    // -------------------------------------------------------------- Constants


    /**
     * Status constants.
     */
    public static final String[] states =
    {"Stopped", "Stopping", "Starting", "Started"};


    public static final int STOPPED  = 0;
    public static final int STOPPING = 1;
    public static final int STARTING = 2;
    public static final int STARTED  = 3;


    /**
     * Component name.
     */
    public static final String NAME = "Catalina servlet container";


    /**
     * Object name.
     */
    public static final String OBJECT_NAME = ":service=Catalina";


    // ------------------------------------------------------ Interface Methods


     /**
     * Retruns the Catalina component name.
     */
    public String getName();


    /**
     * Returns the state.
     */
    public int getState();


    /**
     * Returns a String representation of the state.
     */
    public String getStateString();


   /**
     * Return the debugging detail level for this component.
     */
    public int getDebug();


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug);


    /**
     * Return true if naming is enabled.
     */
    public boolean isUseNaming();


    /**
     * Enables or disables naming support.
     *
     * @param useNaming The new use naming value
     */
    public void setUseNaming(boolean useNaming);


    /**
     * Return the Logger for this component.
     */
    public Logger getLogger();


    /**
     * Set the Logger for this component.
     *
     * @param logger The new logger
     */
    public void setLogger(Logger logger);


    /**
     * Return the default Realm for our Containers.
     */
    public Realm getRealm();


    /**
     * Set the default Realm for our Containers.
     *
     * @param realm The new default realm
     */
    public void setRealm(Realm realm);


    /**
     * Return the secure socket factory class name.
     */
    public String getSocketFactory();


    /**
     * Set the secure socket factory class name.
     *
     * @param socketFactory The new secure socket factory class name
     */
    public void setSocketFactory(String socketFactory);


    /**
     * Add a new Connector to the set of defined Connectors.  The newly
     * added Connector will be associated with the most recently added Engine.
     *
     * @param connector The connector to be added
     *
     * @exception IllegalStateException if no engines have been added yet
     */
    public void addConnector(Connector connector);


    /**
     * Add a new Engine to the set of defined Engines.
     *
     * @param engine The engine to be added
     */
    public void addEngine(Engine engine);


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
                                     boolean secure);


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
    public Context createContext(String path, String docBase);


    /**
     * Create, configure, and return an Engine that will process all
     * HTTP requests received from one of the associated Connectors,
     * based on the specified properties.
     */
    public Engine createEngine();


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
    public Host createHost(String name, String appBase);


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Remove the specified Connector from the set of defined Connectors.
     *
     * @param connector The Connector to be removed
     */
    public void removeConnector(Connector connector);


    /**
     * Remove the specified Context from the set of defined Contexts for its
     * associated Host.  If this is the last Context for this Host, the Host
     * will also be removed.
     *
     * @param context The Context to be removed
     */
    public void removeContext(Context context);


    /**
     * Remove the specified Engine from the set of defined Engines, along with
     * all of its related Hosts and Contexts.  All associated Connectors are
     * also removed.
     *
     * @param engine The Engine to be removed
     */
    public void removeEngine(Engine engine);


    /**
     * Remove the specified Host, along with all of its related Contexts,
     * from the set of defined Hosts for its associated Engine.  If this is
     * the last Host for this Engine, the Engine will also be removed.
     *
     * @param host The Host to be removed
     */
    public void removeHost(Host host);


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     */
    public void start();


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     */
    public void stop();


    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy();


}
