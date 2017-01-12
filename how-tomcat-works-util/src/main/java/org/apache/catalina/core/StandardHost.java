/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardHost.java,v 1.29 2002/06/09 02:19:42 remm Exp $
 * $Revision: 1.29 $
 * $Date: 2002/06/09 02:19:42 $
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


package org.apache.catalina.core;


import java.io.IOException;
import java.net.URL;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Deployer;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Valve;
import org.apache.catalina.valves.ErrorDispatcherValve;


/**
 * Standard implementation of the <b>Host</b> interface.  Each
 * child container must be a Context implementation to process the
 * requests directed to a particular web application.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.29 $ $Date: 2002/06/09 02:19:42 $
 */

public class StandardHost
    extends ContainerBase
    implements Deployer, Host {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardHost component with the default basic Valve.
     */
    public StandardHost() {

        super();
        pipeline.setBasic(new StandardHostValve());

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of aliases for this Host.
     */
    private String[] aliases = new String[0];


    /**
     * The application root for this Host.
     */
    private String appBase = ".";


    /**
     * The auto deploy flag for this Host.
     */
    private boolean autoDeploy = true;


    /**
     * The Java class name of the default context configuration class
     * for deployed web applications.
     */
    private String configClass =
        "org.apache.catalina.startup.ContextConfig";


    /**
     * The Java class name of the default Context implementation class for
     * deployed web applications.
     */
    private String contextClass =
        "org.apache.catalina.core.StandardContext";


    /**
     * The <code>Deployer</code> to whom we delegate application
     * deployment requests.
     */
    private Deployer deployer = new StandardHostDeployer(this);


    /**
     * deploy Context XML config files property.
     */
    private boolean deployXML = true;


    /**
     * The Java class name of the default error reporter implementation class
     * for deployed web applications.
     */
    private String errorReportValveClass =
        "org.apache.catalina.valves.ErrorReportValve";


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardHost/1.0";


    /**
     * The live deploy flag for this Host.
     */
    private boolean liveDeploy = true;


    /**
     * The Java class name of the default Mapper class for this Container.
     */
    private String mapperClass =
        "org.apache.catalina.core.StandardHostMapper";


    /**
     * Unpack WARs property.
     */
    private boolean unpackWARs = true;


    /**
     * Work Directory base for applications.
     */
    private String workDir = null;


    /**
     * DefaultContext config
     */
    private DefaultContext defaultContext;


    // ------------------------------------------------------------- Properties


    /**
     * Return the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     */
    public String getAppBase() {

        return (this.appBase);

    }


    /**
     * Set the application root for this Host.  This can be an absolute
     * pathname, a relative pathname, or a URL.
     *
     * @param appBase The new application root
     */
    public void setAppBase(String appBase) {

        String oldAppBase = this.appBase;
        this.appBase = appBase;
        support.firePropertyChange("appBase", oldAppBase, this.appBase);

    }


    /**
     * Return the value of the auto deploy flag.  If true, it indicates that
     * this host's child webapps should be discovred and automatically
     * deployed at startup time.
     */
    public boolean getAutoDeploy() {

        return (this.autoDeploy);

    }


    /**
     * Set the auto deploy flag value for this host.
     *
     * @param autoDeploy The new auto deploy flag
     */
    public void setAutoDeploy(boolean autoDeploy) {

        boolean oldAutoDeploy = this.autoDeploy;
        this.autoDeploy = autoDeploy;
        support.firePropertyChange("autoDeploy", oldAutoDeploy,
                                   this.autoDeploy);

    }


    /**
     * Return the Java class name of the context configuration class
     * for new web applications.
     */
    public String getConfigClass() {

        return (this.configClass);

    }


    /**
     * Set the Java class name of the context configuration class
     * for new web applications.
     *
     * @param configClass The new context configuration class
     */
    public void setConfigClass(String configClass) {

        String oldConfigClass = this.configClass;
        this.configClass = configClass;
        support.firePropertyChange("configClass",
                                   oldConfigClass, this.configClass);

    }


    /**
     * Set the DefaultContext
     * for new web applications.
     *
     * @param defaultContext The new DefaultContext
     */
    public void addDefaultContext(DefaultContext defaultContext) {

        DefaultContext oldDefaultContext = this.defaultContext;
        this.defaultContext = defaultContext;
        support.firePropertyChange("defaultContext",
                                   oldDefaultContext, this.defaultContext);

    }


    /**
     * Retrieve the DefaultContext for new web applications.
     */
    public DefaultContext getDefaultContext() {
        return (this.defaultContext);
    }


    /**
     * Return the Java class name of the Context implementation class
     * for new web applications.
     */
    public String getContextClass() {

        return (this.contextClass);

    }


    /**
     * Set the Java class name of the Context implementation class
     * for new web applications.
     *
     * @param contextClass The new context implementation class
     */
    public void setContextClass(String contextClass) {

        String oldContextClass = this.contextClass;
        this.contextClass = contextClass;
        support.firePropertyChange("contextClass",
                                   oldContextClass, this.contextClass);

    }


    /**
     * Deploy XML Context config files flag accessor.
     */
    public boolean isDeployXML() {

        return (deployXML);

    }


    /**
     * Deploy XML Context config files flag mutator.
     */
    public void setDeployXML(boolean deployXML) {

        this.deployXML = deployXML;

    }


    /**
     * Return the value of the live deploy flag.  If true, it indicates that
     * a background thread should be started that looks for web application
     * context files, WAR files, or unpacked directories being dropped in to
     * the <code>appBase</code> directory, and deploys new ones as they are
     * encountered.
     */
    public boolean getLiveDeploy() {

        return (this.liveDeploy);

    }


    /**
     * Set the live deploy flag value for this host.
     *
     * @param liveDeploy The new live deploy flag
     */
    public void setLiveDeploy(boolean liveDeploy) {

        boolean oldLiveDeploy = this.liveDeploy;
        this.liveDeploy = liveDeploy;
        support.firePropertyChange("liveDeploy", oldLiveDeploy,
                                   this.liveDeploy);

    }


    /**
     * Return the default Mapper class name.
     */
    public String getMapperClass() {

        return (this.mapperClass);

    }


    /**
     * Set the default Mapper class name.
     *
     * @param mapperClass The new default Mapper class name
     */
    public void setMapperClass(String mapperClass) {

        String oldMapperClass = this.mapperClass;
        this.mapperClass = mapperClass;
        support.firePropertyChange("mapperClass",
                                   oldMapperClass, this.mapperClass);

    }


    /**
     * Return the Java class name of the error report valve class
     * for new web applications.
     */
    public String getErrorReportValveClass() {

        return (this.errorReportValveClass);

    }


    /**
     * Set the Java class name of the error report valve class
     * for new web applications.
     *
     * @param errorReportValveClass The new error report valve class
     */
    public void setErrorReportValveClass(String errorReportValveClass) {

        String oldErrorReportValveClassClass = this.errorReportValveClass;
        this.errorReportValveClass = errorReportValveClass;
        support.firePropertyChange("errorReportValveClass",
                                   oldErrorReportValveClassClass,
                                   this.errorReportValveClass);

    }


    /**
     * Return the canonical, fully qualified, name of the virtual host
     * this Container represents.
     */
    public String getName() {

        return (name);

    }


    /**
     * Set the canonical, fully qualified, name of the virtual host
     * this Container represents.
     *
     * @param name Virtual host name
     *
     * @exception IllegalArgumentException if name is null
     */
    public void setName(String name) {

        if (name == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.nullName"));

        name = name.toLowerCase();      // Internally all names are lower case

        String oldName = this.name;
        this.name = name;
        support.firePropertyChange("name", oldName, this.name);

    }


    /**
     * Unpack WARs flag accessor.
     */
    public boolean isUnpackWARs() {

        return (unpackWARs);

    }


    /**
     * Unpack WARs flag mutator.
     */
    public void setUnpackWARs(boolean unpackWARs) {

        this.unpackWARs = unpackWARs;

    }


    /**
     * Host work directory base.
     */
    public String getWorkDir() {

        return (workDir);
    }


    /**
     * Host work directory base.
     */
    public void setWorkDir(String workDir) {

        this.workDir = workDir;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Import the DefaultContext config into a web application context.
     *
     * @param context web application context to import default context
     */
    public void importDefaultContext(Context context) {

        if( this.defaultContext != null )
            this.defaultContext.importDefaultContext(context);

    }

    /**
     * Add an alias name that should be mapped to this same Host.
     *
     * @param alias The alias to be added
     */
    public void addAlias(String alias) {

        alias = alias.toLowerCase();

        // Skip duplicate aliases
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equals(alias))
                return;
        }

        // Add this alias to the list
        String newAliases[] = new String[aliases.length + 1];
        for (int i = 0; i < aliases.length; i++)
            newAliases[i] = aliases[i];
        newAliases[aliases.length] = alias;

        aliases = newAliases;

        // Inform interested listeners
        fireContainerEvent(ADD_ALIAS_EVENT, alias);

    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Context.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

        if (!(child instanceof Context)) {
            throw new IllegalArgumentException
                (sm.getString("standardHost.notContext"));
        }
        super.addChild(child);

    }


    /**
     * Return the set of alias names for this Host.  If none are defined,
     * a zero length array is returned.
     */
    public String[] findAliases() {

        return (this.aliases);

    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the Context that would be used to process the specified
     * host-relative request URI, if any; otherwise return <code>null</code>.
     *
     * @param uri Request URI to be mapped
     */
    public Context map(String uri) {

        if (debug > 0)
            log("Mapping request URI '" + uri + "'");
        if (uri == null)
            return (null);

        // Match on the longest possible context path prefix
        if (debug > 1)
            log("  Trying the longest context path prefix");
        Context context = null;
        String mapuri = uri;
        while (true) {
            context = (Context) findChild(mapuri);
            if (context != null)
                break;
            int slash = mapuri.lastIndexOf('/');
            if (slash < 0)
                break;
            mapuri = mapuri.substring(0, slash);
        }

        // If no Context matches, select the default Context
        if (context == null) {
            if (debug > 1)
                log("  Trying the default context");
            context = (Context) findChild("");
        }

        // Complain if no Context has been selected
        if (context == null) {
            log(sm.getString("standardHost.mappingError", uri));
            return (null);
        }

        // Return the mapped Context (if any)
        if (debug > 0)
            log(" Mapped to context '" + context.getPath() + "'");
        return (context);

    }


    /**
     * Remove the specified alias name from the aliases for this Host.
     *
     * @param alias Alias name to be removed
     */
    public void removeAlias(String alias) {

        alias = alias.toLowerCase();

        synchronized (aliases) {

            // Make sure this alias is currently present
            int n = -1;
            for (int i = 0; i < aliases.length; i++) {
                if (aliases[i].equals(alias)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified alias
            int j = 0;
            String results[] = new String[aliases.length - 1];
            for (int i = 0; i < aliases.length; i++) {
                if (i != n)
                    results[j++] = aliases[i];
            }
            aliases = results;

        }

        // Inform interested listeners
        fireContainerEvent(REMOVE_ALIAS_EVENT, alias);

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardHost[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    /**
     * Start this host.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {
        // Set error report valve
        if ((errorReportValveClass != null)
            && (!errorReportValveClass.equals(""))) {
            try {
                Valve valve = (Valve) Class.forName(errorReportValveClass)
                    .newInstance();
                addValve(valve);
            } catch (Throwable t) {
                log(sm.getString
                    ("standardHost.invalidErrorReportValveClass",
                     errorReportValveClass));
            }
        }

        // Set dispatcher valve
        addValve(new ErrorDispatcherValve());

        super.start();

    }


    // ------------------------------------------------------- Deployer Methods


    /**
     * Install a new web application, whose web application archive is at the
     * specified URL, into this container with the specified context path.
     * A context path of "" (the empty string) should be used for the root
     * application for this container.  Otherwise, the context path must
     * start with a slash.
     * <p>
     * If this application is successfully installed, a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners,
     * with the newly created <code>Context</code> as an argument.
     *
     * @param contextPath The context path to which this application should
     *  be installed (must be unique)
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalStateException if the specified context path
     *  is already attached to an existing web application
     * @exception IOException if an input/output error was encountered
     *  during install
     */
    public void install(String contextPath, URL war) throws IOException {

        deployer.install(contextPath, war);

    }


    /**
     * <p>Install a new web application, whose context configuration file
     * (consisting of a <code>&lt;Context&gt;</code> element) and web
     * application archive are at the specified URLs.</p>
     *
     * <p>If this application is successfully installed, a ContainerEvent
     * of type <code>INSTALL_EVENT</code> will be sent to all registered
     * listeners, with the newly created <code>Context</code> as an argument.
     * </p>
     *
     * @param config A URL that points to the context configuration file to
     *  be used for configuring the new Context
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed
     *
     * @exception IllegalArgumentException if one of the specified URLs is
     *  null
     * @exception IllegalStateException if the context path specified in the
     *  context configuration file is already attached to an existing web
     *  application
     * @exception IOException if an input/output error was encountered
     *  during installation
     */
    public synchronized void install(URL config, URL war) throws IOException {

        deployer.install(config, war);

    }


    /**
     * Return the Context for the deployed application that is associated
     * with the specified context path (if any); otherwise return
     * <code>null</code>.
     *
     * @param contextPath The context path of the requested web application
     */
    public Context findDeployedApp(String contextPath) {

        return (deployer.findDeployedApp(contextPath));

    }


    /**
     * Return the context paths of all deployed web applications in this
     * Container.  If there are no deployed applications, a zero-length
     * array is returned.
     */
    public String[] findDeployedApps() {

        return (deployer.findDeployedApps());

    }


    /**
     * Remove an existing web application, attached to the specified context
     * path.  If this application is successfully removed, a
     * ContainerEvent of type <code>REMOVE_EVENT</code> will be sent to all
     * registered listeners, with the removed <code>Context</code> as
     * an argument.
     *
     * @param contextPath The context path of the application to be removed
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  removal
     */
    public void remove(String contextPath) throws IOException {

        deployer.remove(contextPath);

    }


    /**
     * Start an existing web application, attached to the specified context
     * path.  Only starts a web application if it is not running.
     *
     * @param contextPath The context path of the application to be started
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  startup
     */
    public void start(String contextPath) throws IOException {

        deployer.start(contextPath);

    }


    /**
     * Stop an existing web application, attached to the specified context
     * path.  Only stops a web application if it is running.
     *
     * @param contextPath The context path of the application to be stopped
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs while stopping
     *  the web application
     */
    public void stop(String contextPath) throws IOException {

        deployer.stop(contextPath);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Add a default Mapper implementation if none have been configured
     * explicitly.
     *
     * @param mapperClass Java class name of the default Mapper
     */
    protected void addDefaultMapper(String mapperClass) {

        super.addDefaultMapper(this.mapperClass);

    }


}
