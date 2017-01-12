/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardServer.java,v 1.32 2002/09/11 14:19:33 amyroh Exp $
 * $Revision: 1.32 $
 * $Date: 2002/09/11 14:19:33 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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


import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.naming.directory.DirContext;

import org.apache.commons.beanutils.PropertyUtils;

import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Store;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;



/**
 * Standard implementation of the <b>Server</b> interface, available for use
 * (but not required) when deploying and starting Catalina.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.32 $ $Date: 2002/09/11 14:19:33 $
 */

public final class StandardServer
    implements Lifecycle, Server {


    // -------------------------------------------------------------- Constants


    /**
     * The set of class/property combinations that should <strong>NOT</strong>
     * be persisted because they are automatically calculated.
     */
    private static String exceptions[][] = {
        { "org.apache.catalina.core.StandardContext", "available" },
        { "org.apache.catalina.core.StandardContext", "configured" },
        { "org.apache.catalina.core.StandardContext", "distributable" },
        { "org.apache.catalina.core.StandardContext", "name" },
        { "org.apache.catalina.core.StandardContext", "override" },
        { "org.apache.catalina.core.StandardContext", "publicId" },
        { "org.apache.catalina.core.StandardContext", "replaceWelcomeFiles" },
        { "org.apache.catalina.core.StandardContext", "sessionTimeout" },
        { "org.apache.catalina.core.StandardContext", "workDir" },
        { "org.apache.catalina.session.StandardManager", "distributable" },
        { "org.apache.catalina.session.StandardManager", "entropy" },
    };


    /**
     * The set of classes that represent persistable properties.
     */
    private static Class persistables[] = {
        String.class,
        Integer.class, Integer.TYPE,
        Boolean.class, Boolean.TYPE,
        Byte.class, Byte.TYPE,
        Character.class, Character.TYPE,
        Double.class, Double.TYPE,
        Float.class, Float.TYPE,
        Long.class, Long.TYPE,
        Short.class, Short.TYPE,
    };


    /**
     * The set of class names that should be skipped when persisting state,
     * because the corresponding listeners, valves, etc. are configured
     * automatically at startup time.
     */
    private static String skippables[] = {
        "org.apache.catalina.authenticator.BasicAuthenticator",
        "org.apache.catalina.authenticator.DigestAuthenticator",
        "org.apache.catalina.authenticator.FormAuthenticator",
        "org.apache.catalina.authenticator.NonLoginAuthenticator",
        "org.apache.catalina.authenticator.SSLAuthenticator",
        "org.apache.catalina.core.NamingContextListener",
        "org.apache.catalina.core.StandardContextValve",
        "org.apache.catalina.core.StandardEngineValve",
        "org.apache.catalina.core.StandardHostValve",
        "org.apache.catalina.startup.ContextConfig",
        "org.apache.catalina.startup.EngineConfig",
        "org.apache.catalina.startup.HostConfig",
        "org.apache.catalina.valves.CertificatesValve",
        "org.apache.catalina.valves.ErrorDispatcherValve",
        "org.apache.catalina.valves.ErrorReportValve",
    };


    /**
     * ServerLifecycleListener classname.
     */
    private static String SERVER_LISTENER_CLASS_NAME =
        "org.apache.catalina.mbeans.ServerLifecycleListener";


    // ------------------------------------------------------------ Constructor


    /**
     * Construct a default instance of this class.
     */
    public StandardServer() {

        super();
        ServerFactory.setServer(this);

        globalNamingResources = new NamingResources();
        globalNamingResources.setContainer(this);

        if (isUseNaming()) {
            if (namingContextListener == null) {
                namingContextListener = new NamingContextListener();
                namingContextListener.setDebug(getDebug());
                addLifecycleListener(namingContextListener);
            }
        }

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Debugging detail level.
     */
    private int debug = 0;


    /**
     * Global naming resources context.
     */
    private javax.naming.Context globalNamingContext = null;


    /**
     * Global naming resources.
     */
    private NamingResources globalNamingResources = null;


    /**
     * Descriptive information about this Server implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardServer/1.0";


    /**
     * The lifecycle event support for this component.
     */
    private LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The naming context listener for this web application.
     */
    private NamingContextListener namingContextListener = null;


    /**
     * The port number on which we wait for shutdown commands.
     */
    private int port = 8005;


    /**
     * A random number generator that is <strong>only</strong> used if
     * the shutdown command string is longer than 1024 characters.
     */
    private Random random = null;


    /**
     * The set of Services associated with this Server.
     */
    private Service services[] = new Service[0];


    /**
     * The shutdown command string we are looking for.
     */
    private String shutdown = "SHUTDOWN";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


    /**
     * Has this component been initialized?
     */
    private boolean initialized = false;


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


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


    /**
     * Return the global naming resources context.
     */
    public javax.naming.Context getGlobalNamingContext() {

        return (this.globalNamingContext);

    }


    /**
     * Set the global naming resources context.
     *
     * @param globalNamingContext The new global naming resource context
     */
    public void setGlobalNamingContext
        (javax.naming.Context globalNamingContext) {

        this.globalNamingContext = globalNamingContext;

    }


    /**
     * Return the global naming resources.
     */
    public NamingResources getGlobalNamingResources() {

        return (this.globalNamingResources);

    }


    /**
     * Set the global naming resources.
     *
     * @param namingResources The new global naming resources
     */
    public void setGlobalNamingResources
        (NamingResources globalNamingResources) {

        NamingResources oldGlobalNamingResources =
            this.globalNamingResources;
        this.globalNamingResources = globalNamingResources;
        this.globalNamingResources.setContainer(this);
        support.firePropertyChange("globalNamingResources",
                                   oldGlobalNamingResources,
                                   this.globalNamingResources);

    }


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the port number we listen to for shutdown commands.
     */
    public int getPort() {

        return (this.port);

    }


    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     */
    public void setPort(int port) {

        this.port = port;

    }


    /**
     * Return the shutdown command string we are waiting for.
     */
    public String getShutdown() {

        return (this.shutdown);

    }


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    public void setShutdown(String shutdown) {

        this.shutdown = shutdown;

    }


    // --------------------------------------------------------- Server Methods


    /**
     * Add a new Service to the set of defined Services.
     *
     * @param service The Service to be added
     */
    public void addService(Service service) {

        service.setServer(this);

        synchronized (services) {
            Service results[] = new Service[services.length + 1];
            System.arraycopy(services, 0, results, 0, services.length);
            results[services.length] = service;
            services = results;

            if (initialized) {
                try {
                    service.initialize();
                } catch (LifecycleException e) {
                    e.printStackTrace(System.err);
                }
            }

            if (started && (service instanceof Lifecycle)) {
                try {
                    ((Lifecycle) service).start();
                } catch (LifecycleException e) {
                    ;
                }
            }

            // Report this property change to interested listeners
            support.firePropertyChange("service", null, service);
        }

    }


    /**
     * Wait until a proper shutdown command is received, then return.
     */
    public void await() {

        // Set up a server socket to wait on
        ServerSocket serverSocket = null;
        try {
            serverSocket =
                new ServerSocket(port, 1,
                                 InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            System.err.println("StandardServer.await: create[" + port
                               + "]: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        // Loop waiting for a connection and a valid command
        while (true) {

            // Wait for the next connection
            Socket socket = null;
            InputStream stream = null;
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(10 * 1000);  // Ten seconds
                stream = socket.getInputStream();
            } catch (AccessControlException ace) {
                System.err.println("StandardServer.accept security exception: "
                                   + ace.getMessage());
                continue;
            } catch (IOException e) {
                System.err.println("StandardServer.await: accept: " + e);
                e.printStackTrace();
                System.exit(1);
            }

            // Read a set of characters from the socket
            StringBuffer command = new StringBuffer();
            int expected = 1024; // Cut off to avoid DoS attack
            while (expected < shutdown.length()) {
                if (random == null)
                    random = new Random(System.currentTimeMillis());
                expected += (random.nextInt() % 1024);
            }
            while (expected > 0) {
                int ch = -1;
                try {
                    ch = stream.read();
                } catch (IOException e) {
                    System.err.println("StandardServer.await: read: " + e);
                    e.printStackTrace();
                    ch = -1;
                }
                if (ch < 32)  // Control character or EOF terminates loop
                    break;
                command.append((char) ch);
                expected--;
            }

            // Close the socket now that we are done with it
            try {
                socket.close();
            } catch (IOException e) {
                ;
            }

            // Match against our command string
            boolean match = command.toString().equals(shutdown);
            if (match) {
                break;
            } else
                System.err.println("StandardServer.await: Invalid command '" +
                                   command.toString() + "' received");

        }

        // Close the server socket and return
        try {
            serverSocket.close();
        } catch (IOException e) {
            ;
        }

    }


    /**
     * Return the specified Service (if it exists); otherwise return
     * <code>null</code>.
     *
     * @param name Name of the Service to be returned
     */
    public Service findService(String name) {

        if (name == null) {
            return (null);
        }
        synchronized (services) {
            for (int i = 0; i < services.length; i++) {
                if (name.equals(services[i].getName())) {
                    return (services[i]);
                }
            }
        }
        return (null);

    }


    /**
     * Return the set of Services defined within this Server.
     */
    public Service[] findServices() {

        return (services);

    }


    /**
     * Remove the specified Service from the set associated from this
     * Server.
     *
     * @param service The Service to be removed
     */
    public void removeService(Service service) {

        synchronized (services) {
            int j = -1;
            for (int i = 0; i < services.length; i++) {
                if (service == services[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            if (services[j] instanceof Lifecycle) {
                try {
                    ((Lifecycle) services[j]).stop();
                } catch (LifecycleException e) {
                    ;
                }
            }
            int k = 0;
            Service results[] = new Service[services.length - 1];
            for (int i = 0; i < services.length; i++) {
                if (i != j)
                    results[k++] = services[i];
            }
            services = results;

            // Report this property change to interested listeners
            support.firePropertyChange("service", service, null);
        }

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
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("StandardServer[");
        sb.append(getPort());
        sb.append("]");
        return (sb.toString());

    }


    /**
     * Write the configuration information for this entire <code>Server</code>
     * out to the server.xml configuration file.
     *
     * @exception InstanceNotFoundException if the managed resource object
     *  cannot be found
     * @exception MBeanException if the initializer of the object throws
     *  an exception, or persistence is not supported
     * @exception RuntimeOperationsException if an exception is reported
     *  by the persistence mechanism
     */
    public synchronized void store() throws Exception {

        // Calculate file objects for the old and new configuration files.
        String configFile = "conf/server.xml"; // FIXME - configurable?
        File configOld = new File(configFile);
        if (!configOld.isAbsolute()) {
            configOld = new File(System.getProperty("catalina.base"),
                                 configFile);
        }
        File configNew = new File(configFile + ".new");
        if (!configNew.isAbsolute()) {
            configNew = new File(System.getProperty("catalina.base"),
                                 configFile + ".new");
        }
        String ts = (new Timestamp(System.currentTimeMillis())).toString();
        //        yyyy-mm-dd hh:mm:ss
        //        0123456789012345678
        StringBuffer sb = new StringBuffer(".");
        sb.append(ts.substring(0, 10));
        sb.append('.');
        sb.append(ts.substring(11, 13));
        sb.append('-');
        sb.append(ts.substring(14, 16));
        sb.append('-');
        sb.append(ts.substring(17, 19));
        File configSave = new File(configFile + sb.toString());
        if (!configSave.isAbsolute()) {
            configSave = new File(System.getProperty("catalina.base"),
                                  configFile + sb.toString());
        }

        // Open an output writer for the new configuration file
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(configNew), "UTF8"));
        } catch (IOException e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    ;
                }
            }
            throw (e);
        }

        // Store the state of this Server MBean
        // (which will recursively store everything
        try {
            storeServer(writer, 0, this);
        } catch (Exception e) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable t) {
                    ;
                }
            }
            throw (e);
        }

        // Flush and close the output file
        try {
            writer.flush();
        } catch (Exception e) {
            throw (e);
        }
        try {
            writer.close();
        } catch (Exception e) {
            throw (e);
        }

        // Shuffle old->save and new->old
        if (configOld.renameTo(configSave)) {
            if (configNew.renameTo(configOld)) {
                return;
            } else {
                configSave.renameTo(configOld);
                throw new IOException("Cannot rename " +
                                      configNew.getAbsolutePath() + " to " +
                                      configOld.getAbsolutePath());
            }
        } else {
            throw new IOException("Cannot rename " +
                                  configOld.getAbsolutePath() + " to " +
                                  configSave.getAbsolutePath());
        }

    }


    // -------------------------------------------------------- Private Methods


    /** Given a string, this method replaces all occurrences of
     *  '<', '>', '&', and '"'.
    */

    private String convertStr(String input) {

        StringBuffer filtered = new StringBuffer(input.length());
        char c;
        for(int i=0; i<input.length(); i++) {
            c = input.charAt(i);
            if (c == '<') {
                filtered.append("&lt;");
            } else if (c == '>') {
                filtered.append("&gt;");
            } else if (c == '\'') {
                filtered.append("&apos;");
            } else if (c == '"') {
                filtered.append("&quot;");
            } else if (c == '&') {
                filtered.append("&amp;");
            } else {
                filtered.append(c);
            }
        }
            return(filtered.toString());
    }


    /**
     * Is this an instance of the default <code>Loader</code> configuration,
     * with all-default properties?
     *
     * @param loader Loader to be tested
     */
    private boolean isDefaultLoader(Loader loader) {

        if (!(loader instanceof WebappLoader)) {
            return (false);
        }
        WebappLoader wloader = (WebappLoader) loader;
        if ((wloader.getCheckInterval() != 15) ||
            (wloader.getDebug() != 0) ||
            (wloader.getDelegate() != false) ||
            !wloader.getLoaderClass().equals
             ("org.apache.catalina.loader.WebappClassLoader")) {
            return (false);
        }
        return (true);

    }


    /**
     * Is this an instance of the default <code>Manager</code> configuration,
     * with all-default properties?
     *
     * @param manager Manager to be tested
     */
    private boolean isDefaultManager(Manager manager) {

        if (!(manager instanceof StandardManager)) {
            return (false);
        }
        StandardManager smanager = (StandardManager) manager;
        if ((smanager.getDebug() != 0) ||
            !smanager.getPathname().equals("SESSIONS.ser") ||
            (smanager.getCheckInterval() != 60) ||
            !smanager.getRandomClass().equals("java.security.SecureRandom") ||
            (smanager.getMaxActiveSessions() != -1) ||
            !smanager.getAlgorithm().equals("MD5")) {
            return (false);
        }
        return (true);

    }


    /**
     * Is the specified class name + property name combination an
     * exception that should not be persisted?
     *
     * @param className The class name to check
     * @param property The property name to check
     */
    private boolean isException(String className, String property) {

        for (int i = 0; i < exceptions.length; i++) {
            if (className.equals(exceptions[i][0]) &&
                property.equals(exceptions[i][1])) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Is the specified property type one for which we should generate
     * a persistence attribute?
     *
     * @param clazz Java class to be tested
     */
    private boolean isPersistable(Class clazz) {

        for (int i = 0; i < persistables.length; i++) {
            if (persistables[i] == clazz) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Is the specified class name one that should be skipped because
     * the corresponding component is configured automatically at
     * startup time?
     *
     * @param className Class name to be tested
     */
    private boolean isSkippable(String className) {

        for (int i = 0; i < skippables.length; i++) {
            if (skippables[i] == className) {
                return (true);
            }
        }
        return (false);

    }


    /**
     * Store the relevant attributes of the specified JavaBean, plus a
     * <code>className</code> attribute defining the fully qualified
     * Java class name of the bean.
     *
     * @param writer PrintWriter to which we are storing
     * @param bean Bean whose properties are to be rendered as attributes,
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeAttributes(PrintWriter writer,
                                 Object bean) throws Exception {

        storeAttributes(writer, true, bean);

    }


    /**
     * Store the relevant attributes of the specified JavaBean.
     *
     * @param writer PrintWriter to which we are storing
     * @param include Should we include a <code>className</code> attribute?
     * @param bean Bean whose properties are to be rendered as attributes,
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeAttributes(PrintWriter writer, boolean include,
                                 Object bean) throws Exception {

        // Render a className attribute if requested
        if (include) {
            writer.print(" className=\"");
            writer.print(bean.getClass().getName());
            writer.print("\"");
        }

        // Acquire the list of properties for this bean
        PropertyDescriptor descriptors[] =
            PropertyUtils.getPropertyDescriptors(bean);
        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }

        // Render the relevant properties of this bean
        String className = bean.getClass().getName();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i] instanceof IndexedPropertyDescriptor) {
                continue; // Indexed properties are not persisted
            }
            if (!isPersistable(descriptors[i].getPropertyType()) ||
                (descriptors[i].getReadMethod() == null) ||
                (descriptors[i].getWriteMethod() == null)) {
                continue; // Must be a read-write primitive or String
            }
            Object value =
                PropertyUtils.getSimpleProperty(bean,
                                                descriptors[i].getName());
            if (value == null) {
                continue; // Null values are not persisted
            }
            if (isException(className, descriptors[i].getName())) {
                continue; // Skip the specified exceptions
            }
            if (!(value instanceof String)) {
                value = value.toString();
            }
            writer.print(' ');
            writer.print(descriptors[i].getName());
            writer.print("=\"");
            String strValue = convertStr((String) value);
            writer.print(strValue);
            writer.print("\"");
        }

    }


    /**
     * Store the specified Connector properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param connector Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeConnector(PrintWriter writer, int indent,
                                Connector connector) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Connector");
        storeAttributes(writer, connector);
        writer.println(">");

        // Store nested <Factory> element
        ServerSocketFactory factory = connector.getFactory();
        if (factory != null) {
            storeFactory(writer, indent + 2, factory);
        }

        // Store nested <Listener> elements
        if (connector instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) connector).findLifecycleListeners();
            if (listeners == null) {
                listeners = new LifecycleListener[0];
            }
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Connector>");

    }


    /**
     * Store the specified Context properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param context  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeContext(PrintWriter writer, int indent,
                              Context context) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Context");
        storeAttributes(writer, context);
        writer.println(">");

        // Store nested <InstanceListener> elements
        String iListeners[] = context.findInstanceListeners();
        for (int i = 0; i < iListeners.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<InstanceListener>");
            writer.print(iListeners[i]);
            writer.println("</InstanceListener>");
        }

        // Store nested <Listener> elements
        if (context instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) context).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Loader> element
        Loader loader = context.getLoader();
        if (loader != null) {
            storeLoader(writer, indent + 2, loader);
        }

        // Store nested <Logger> element
        Logger logger = context.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (context.getParent() != null) {
                parentLogger = context.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Manager> element
        Manager manager = context.getManager();
        if (manager != null) {
            storeManager(writer, indent + 2, manager);
        }

        // Store nested <Parameter> elements
        ApplicationParameter[] appParams = context.findApplicationParameters();
        for (int i = 0; i < appParams.length; i++) {
            for (int j = 0; j < indent + 2; j++) {
                writer.print(' ');
            }
            writer.print("<Parameter");
            storeAttributes(writer, false, appParams[i]);
            writer.println("/>");
        }

        // Store nested <Realm> element
        Realm realm = context.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (context.getParent() != null) {
                parentRealm = context.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <Resources> element
        DirContext resources = context.getResources();
        if (resources != null) {
            storeResources(writer, indent + 2, resources);
        }

        // Store nested <Valve> elements
        if (context instanceof Pipeline) {
            Valve valves[] = ((Pipeline) context).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store nested <WrapperLifecycle> elements
        String wLifecycles[] = context.findWrapperLifecycles();
        for (int i = 0; i < wLifecycles.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<WrapperLifecycle>");
            writer.print(wLifecycles[i]);
            writer.println("</WrapperLifecycle>");
        }

        // Store nested <WrapperListener> elements
        String wListeners[] = context.findWrapperListeners();
        for (int i = 0; i < wListeners.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<WrapperListener>");
            writer.print(wListeners[i]);
            writer.println("</WrapperListener>");
        }

        // Store nested naming resources elements
        NamingResources nresources = context.getNamingResources();
        if (nresources != null) {
            storeNamingResources(writer, indent + 2, nresources);
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Context>");

    }


    /**
     * Store the specified DefaultContext properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param dcontext  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeDefaultContext(PrintWriter writer, int indent,
                                     DefaultContext dcontext)
        throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<DefaultContext");
        storeAttributes(writer, dcontext);
        writer.println(">");

        // Store nested <InstanceListener> elements
        String iListeners[] = dcontext.findInstanceListeners();
        for (int i = 0; i < iListeners.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<InstanceListener>");
            writer.print(iListeners[i]);
            writer.println("</InstanceListener>");
        }

        // Store nested <Listener> elements
        if (dcontext instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) dcontext).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Loader> element
        Loader loader = dcontext.getLoader();
        if (loader != null) {
            storeLoader(writer, indent + 2, loader);
        }

        // Store nested <Logger> element
        /* Nested logger not currently supported on DefaultContext
        Logger logger = dcontext.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (dcontext.getParent() != null) {
                parentLogger = dcontext.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }
        */

        // Store nested <Manager> element
        Manager manager = dcontext.getManager();
        if (manager != null) {
            storeManager(writer, indent + 2, manager);
        }

        // Store nested <Parameter> elements
        ApplicationParameter[] appParams =
            dcontext.findApplicationParameters();
        for (int i = 0; i < appParams.length; i++) {
            for (int j = 0; j < indent + 2; j++) {
                writer.print(' ');
            }
            writer.print("<Parameter");
            storeAttributes(writer, false, appParams[i]);
            writer.println("/>");
        }

        // Store nested <Realm> element
        /* Nested realm not currently supported on DefaultContext
        Realm realm = dcontext.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (dcontext.getParent() != null) {
                parentRealm = dcontext.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }
        */

        // Store nested <Resources> element
        DirContext resources = dcontext.getResources();
        if (resources != null) {
            storeResources(writer, indent + 2, resources);
        }

        // Store nested <Valve> elements
        if (dcontext instanceof Pipeline) {
            Valve valves[] = ((Pipeline) dcontext).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store nested <WrapperLifecycle> elements
        String wLifecycles[] = dcontext.findWrapperLifecycles();
        for (int i = 0; i < wLifecycles.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<WrapperLifecycle>");
            writer.print(wLifecycles[i]);
            writer.println("</WrapperLifecycle>");
        }

        // Store nested <WrapperListener> elements
        String wListeners[] = dcontext.findWrapperListeners();
        for (int i = 0; i < wListeners.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<WrapperListener>");
            writer.print(wListeners[i]);
            writer.println("</WrapperListener>");
        }

        // Store nested naming resources elements
        NamingResources nresources = dcontext.getNamingResources();
        if (nresources != null) {
            storeNamingResources(writer, indent + 2, nresources);
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</DefaultContext>");

    }


    /**
     * Store the specified Engine properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param engine  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeEngine(PrintWriter writer, int indent,
                             Engine engine) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Engine");
        storeAttributes(writer, engine);
        writer.println(">");

        // Store nested <DefaultContext> element
        if (engine instanceof StandardEngine) {
            DefaultContext dcontext =
                ((StandardEngine) engine).getDefaultContext();
            if (dcontext != null) {
                storeDefaultContext(writer, indent + 2, dcontext);
            }
        }

        // Store nested <Host> elements (or other relevant containers)
        Container children[] = engine.findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Context) {
                storeContext(writer, indent + 2, (Context) children[i]);
            } else if (children[i] instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) children[i]);
            } else if (children[i] instanceof Host) {
                storeHost(writer, indent + 2, (Host) children[i]);
            }
        }

        // Store nested <Listener> elements
        if (engine instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) engine).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Logger> element
        Logger logger = engine.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (engine.getParent() != null) {
                parentLogger = engine.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Realm> element
        Realm realm = engine.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (engine.getParent() != null) {
                parentRealm = engine.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <Valve> elements
        if (engine instanceof Pipeline) {
            Valve valves[] = ((Pipeline) engine).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Engine>");

    }


    /**
     * Store the specified ServerSocketFactory properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param factory Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeFactory(PrintWriter writer, int indent,
                              ServerSocketFactory factory) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Factory");
        storeAttributes(writer, factory);
        writer.println("/>");

    }


    /**
     * Store the specified Host properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param host  Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeHost(PrintWriter writer, int indent,
                           Host host) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Host");
        storeAttributes(writer, host);
        writer.println(">");

        // Store nested <Alias> elements
        String aliases[] = host.findAliases();
        for (int i = 0; i < aliases.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<Alias>");
            writer.print(aliases[i]);
            writer.println("</Alias>");
        }

        // Store nested <Cluster> elements
        ; // FIXME - But it's not supported by any standard Host implementation

        // Store nested <Context> elements (or other relevant containers)
        Container children[] = host.findChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Context) {
                storeContext(writer, indent + 2, (Context) children[i]);
            } else if (children[i] instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) children[i]);
            } else if (children[i] instanceof Host) {
                storeHost(writer, indent + 2, (Host) children[i]);
            }
        }

        // Store nested <DefaultContext> element
        if (host instanceof StandardHost) {
            DefaultContext dcontext =
                ((StandardHost) host).getDefaultContext();
            if (dcontext != null) {
                Container parent = host.getParent();
                if ((parent != null) &&
                    (parent instanceof StandardEngine)) {
                    DefaultContext pcontext =
                        ((StandardEngine) parent).getDefaultContext();
                    if (dcontext != pcontext) {
                        storeDefaultContext(writer, indent + 2, dcontext);
                    }
                }
            }
        }

        // Store nested <Listener> elements
        if (host instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) host).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <Logger> element
        Logger logger = host.getLogger();
        if (logger != null) {
            Logger parentLogger = null;
            if (host.getParent() != null) {
                parentLogger = host.getParent().getLogger();
            }
            if (logger != parentLogger) {
                storeLogger(writer, indent + 2, logger);
            }
        }

        // Store nested <Realm> element
        Realm realm = host.getRealm();
        if (realm != null) {
            Realm parentRealm = null;
            if (host.getParent() != null) {
                parentRealm = host.getParent().getRealm();
            }
            if (realm != parentRealm) {
                storeRealm(writer, indent + 2, realm);
            }
        }

        // Store nested <Valve> elements
        if (host instanceof Pipeline) {
            Valve valves[] = ((Pipeline) host).getValves();
            for (int i = 0; i < valves.length; i++) {
                storeValve(writer, indent + 2, valves[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Host>");

    }


    /**
     * Store the specified Listener properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param listener Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeListener(PrintWriter writer, int indent,
                               LifecycleListener listener) throws Exception {

        if (isSkippable(listener.getClass().getName())) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Listener");
        storeAttributes(writer, listener);
        writer.println("/>");

    }


    /**
     * Store the specified Loader properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param loader Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeLoader(PrintWriter writer, int indent,
                             Loader loader) throws Exception {

        if (isDefaultLoader(loader)) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Loader");
        storeAttributes(writer, loader);
        writer.println("/>");

    }


    /**
     * Store the specified Logger properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param logger Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeLogger(PrintWriter writer, int indent,
                             Logger logger) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Logger");
        storeAttributes(writer, logger);
        writer.println("/>");

    }


    /**
     * Store the specified Manager properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param manager Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeManager(PrintWriter writer, int indent,
                              Manager manager) throws Exception {

        if (isDefaultManager(manager)) {
            return;
        }

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Manager");
        storeAttributes(writer, manager);
        writer.println(">");

        // Store nested <Store> element
        if (manager instanceof PersistentManager) {
            Store store = ((PersistentManager) manager).getStore();
            if (store != null) {
                storeStore(writer, indent + 2, store);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Manager>");

    }


    /**
     * Store the specified NamingResources properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param resources Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeNamingResources(PrintWriter writer, int indent,
                                      NamingResources resources)
        throws Exception {

        // Store nested <Ejb> elements
        ContextEjb[] ejbs = resources.findEjbs();
        if (ejbs.length > 0) {
            for (int i = 0; i < ejbs.length; i++) {
                for (int j = 0; j < indent; j++) {
                    writer.print(' ');
                }
                writer.print("<Ejb");
                storeAttributes(writer, false, ejbs[i]);
                writer.println("/>");
            }
        }

        // Store nested <Environment> elements
        ContextEnvironment[] envs = resources.findEnvironments();
        if (envs.length > 0) {
            for (int i = 0; i < envs.length; i++) {
                for (int j = 0; j < indent; j++) {
                    writer.print(' ');
                }
                writer.print("<Environment");
                storeAttributes(writer, false, envs[i]);
                writer.println("/>");
            }
        }

        // Store nested <LocalEjb> elements
        ContextLocalEjb[] lejbs = resources.findLocalEjbs();
        if (lejbs.length > 0) {
            for (int i = 0; i < lejbs.length; i++) {
                for (int j = 0; j < indent; j++) {
                    writer.print(' ');
                }
                writer.print("<LocalEjb");
                storeAttributes(writer, false, lejbs[i]);
                writer.println("/>");
            }
        }

        // Store nested <Resource> elements
        ContextResource[] dresources = resources.findResources();
        for (int i = 0; i < dresources.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<Resource");
            storeAttributes(writer, false, dresources[i]);
            writer.println("/>");
        }

        // Store nested <ResourceEnvRef> elements
        String[] eresources = resources.findResourceEnvRefs();
        for (int i = 0; i < eresources.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.println("<ResourceEnvRef>");
            for (int j = 0; j < indent + 2; j++) {
                writer.print(' ');
            }
            writer.print("<name>");
            writer.print(eresources[i]);
            writer.println("</name>");
            for (int j = 0; j < indent + 2; j++) {
                writer.print(' ');
            }
            writer.print("<type>");
            writer.print(resources.findResourceEnvRef(eresources[i]));
            writer.println("</type>");
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.println("</ResourceEnvRef>");
        }

        // Store nested <ResourceParams> elements
        ResourceParams[] params = resources.findResourceParams();
        for (int i = 0; i < params.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<ResourceParams");
            storeAttributes(writer, false, params[i]);
            writer.println(">");
            Hashtable resourceParams = params[i].getParameters();
            Enumeration nameEnum = resourceParams.keys();
            while (nameEnum.hasMoreElements()) {
                String name = (String) nameEnum.nextElement();
                String value = (String) resourceParams.get(name);
                for (int j = 0; j < indent + 2; j++) {
                    writer.print(' ');
                }
                writer.println("<parameter>");
                for (int j = 0; j < indent + 4; j++) {
                    writer.print(' ');
                }
                writer.print("<name>");
                writer.print(name);
                writer.println("</name>");
                for (int j = 0; j < indent + 4; j++) {
                    writer.print(' ');
                }
                writer.print("<value>");
                writer.print(value);
                writer.println("</value>");
                for (int j = 0; j < indent + 2; j++) {
                    writer.print(' ');
                }
                writer.println("</parameter>");
            }
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.println("</ResourceParams>");
        }

        // Store nested <ResourceLink> elements
        ContextResourceLink[] resourceLinks = resources.findResourceLinks();
        for (int i = 0; i < resourceLinks.length; i++) {
            for (int j = 0; j < indent; j++) {
                writer.print(' ');
            }
            writer.print("<ResourceLink");
            storeAttributes(writer, false, resourceLinks[i]);
            writer.println("/>");
        }

    }


    /**
     * Store the specified Realm properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param realm Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeRealm(PrintWriter writer, int indent,
                            Realm realm) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Realm");
        storeAttributes(writer, realm);
        writer.println("/>");

    }


    /**
     * Store the specified Resources properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param resources Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeResources(PrintWriter writer, int indent,
                                DirContext resources) throws Exception {

        if (resources instanceof org.apache.naming.resources.FileDirContext) {
            return;
        }
        if (resources instanceof org.apache.naming.resources.ProxyDirContext) {
            return;
        }
        if (resources instanceof org.apache.naming.resources.WARDirContext) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Resources");
        storeAttributes(writer, resources);
        writer.println("/>");

    }


    /**
     * Store the specified Server properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param server Object to be stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeServer(PrintWriter writer, int indent,
                             Server server) throws Exception {

        // Store the beginning of this element
        writer.println("<?xml version='1.0' encoding='utf-8'?>");
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Server");
        storeAttributes(writer, server);
        writer.println(">");

        // Store nested <Listener> elements
        if (server instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) server).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store nested <GlobalNamingResources> element
        NamingResources globalNamingResources =
            server.getGlobalNamingResources();
        if (globalNamingResources != null) {
            for (int i = 0; i < indent + 2; i++) {
                writer.print(' ');
            }
            writer.println("<GlobalNamingResources>");
            storeNamingResources(writer, indent + 4, globalNamingResources);
            for (int i = 0; i < indent + 2; i++) {
                writer.print(' ');
            }
            writer.println("</GlobalNamingResources>");
        }

        // Store nested <Service> elements
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            storeService(writer, indent + 2, services[i]);
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Server>");

    }


    /**
     * Store the specified Service properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param server Object to be stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeService(PrintWriter writer, int indent,
                              Service service) throws Exception {

        // Store the beginning of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Service");
        storeAttributes(writer, service);
        writer.println(">");

        // Store nested <Connector> elements
        Connector connectors[] = service.findConnectors();
        for (int i = 0; i < connectors.length; i++) {
            storeConnector(writer, indent + 2, connectors[i]);
        }

        // Store nested <Engine> element (or other appropriate container)
        Container container = service.getContainer();
        if (container != null) {
            if (container instanceof Context) {
                storeContext(writer, indent + 2, (Context) container);
            } else if (container instanceof Engine) {
                storeEngine(writer, indent + 2, (Engine) container);
            } else if (container instanceof Host) {
                storeHost(writer, indent + 2, (Host) container);
            }
        }

        // Store nested <Listener> elements
        if (service instanceof Lifecycle) {
            LifecycleListener listeners[] =
                ((Lifecycle) service).findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i].getClass().getName().equals
                    (SERVER_LISTENER_CLASS_NAME)) {
                    continue;
                }
                storeListener(writer, indent + 2, listeners[i]);
            }
        }

        // Store the ending of this element
        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.println("</Service>");

    }


    /**
     * Store the specified Store properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param store Object whose properties are being stored
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeStore(PrintWriter writer, int indent,
                             Store store) throws Exception {

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Store");
        storeAttributes(writer, store);
        writer.println("/>");

    }


    /**
     * Store the specified Valve properties.
     *
     * @param writer PrintWriter to which we are storing
     * @param indent Number of spaces to indent this element
     * @param valve Object whose properties are being valved
     *
     * @exception Exception if an exception occurs while storing
     */
    private void storeValve(PrintWriter writer, int indent,
                             Valve valve) throws Exception {

        if (isSkippable(valve.getClass().getName())) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            writer.print(' ');
        }
        writer.print("<Valve");
        storeAttributes(writer, valve);
        writer.println("/>");

    }


    /**
     * Return <code>true</code> if the specified client and server addresses
     * are the same.  This method works around a bug in the IBM 1.1.8 JVM on
     * Linux, where the address bytes are returned reversed in some
     * circumstances.
     *
     * @param server The server's InetAddress
     * @param client The client's InetAddress
     */
    private boolean isSameAddress(InetAddress server, InetAddress client) {

        // Compare the byte array versions of the two addresses
        byte serverAddr[] = server.getAddress();
        byte clientAddr[] = client.getAddress();
        if (serverAddr.length != clientAddr.length)
            return (false);
        boolean match = true;
        for (int i = 0; i < serverAddr.length; i++) {
            if (serverAddr[i] != clientAddr[i]) {
                match = false;
                break;
            }
        }
        if (match)
            return (true);

        // Compare the reversed form of the two addresses
        for (int i = 0; i < serverAddr.length; i++) {
            if (serverAddr[i] != clientAddr[(serverAddr.length-1)-i])
                return (false);
        }
        return (true);

    }


    /**
     * Return true if naming should be used.
     */
    private boolean isUseNaming() {
        boolean useNaming = true;
        // Reading the "catalina.useNaming" environment variable
        String useNamingProperty = System.getProperty("catalina.useNaming");
        if ((useNamingProperty != null)
            && (useNamingProperty.equals("false"))) {
            useNaming = false;
        }
        return useNaming;
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a LifecycleEvent listener to this component.
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
     * Remove a LifecycleEvent listener from this component.
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
                (sm.getString("standardServer.start.started"));
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Start our defined Services
        synchronized (services) {
            for (int i = 0; i < services.length; i++) {
                if (services[i] instanceof Lifecycle)
                    ((Lifecycle) services[i]).start();
            }
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

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
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardServer.stop.notStarted"));

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop our defined Services
        for (int i = 0; i < services.length; i++) {
            if (services[i] instanceof Lifecycle)
                ((Lifecycle) services[i]).stop();
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

    }

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     */
    public void initialize()
    throws LifecycleException {
        if (initialized)
            throw new LifecycleException (
                sm.getString("standardServer.initialize.initialized"));
        initialized = true;

        // Initialize our defined Services
        for (int i = 0; i < services.length; i++) {
            services[i].initialize();
        }
    }

}
