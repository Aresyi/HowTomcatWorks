/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardDefaultContext.java,v 1.8 2002/09/19 22:55:48 amyroh Exp $
 * $Revision: 1.8 $
 * $Date: 2002/09/19 22:55:48 $
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Hashtable;
import javax.naming.directory.DirContext;
import org.apache.naming.ContextAccessController;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.catalina.util.StringManager;

/**
 * Used to store the default configuration a Host will use
 * when creating a Context.  A Context configured in server.xml
 * can override these defaults by setting the Context attribute
 * <CODE>override="true"</CODE>.
 *
 * @author Glenn Nielsen
 * @version $Revision: 1.8 $ $Date: 2002/09/19 22:55:48 $
 */

public class StandardDefaultContext 
    implements DefaultContext, LifecycleListener {


    // ----------------------------------------------------------- Constructors


    /**
     * Create the DefaultContext
     */
    public StandardDefaultContext() {

        namingResources.setContainer(this);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Contexts we are currently associated with.
     */
    private Hashtable contexts = new Hashtable();


    /**
     * The set of application listener class names configured for this
     * application, in the order they were encountered in the web.xml file.
     */
    private String applicationListeners[] = new String[0];


    /**
     * The set of application parameters defined for this application.
     */
    private ApplicationParameter applicationParameters[] =
        new ApplicationParameter[0];


    /**
     * Should we attempt to use cookies for session id communication?
     */
    private boolean cookies = true;


    /**
     * Should we allow the <code>ServletContext.getContext()</code> method
     * to access the context of other web applications in this server?
     */
    private boolean crossContext = true;


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.DefaultContext/1.0";


    /**
     * The set of classnames of InstanceListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String instanceListeners[] = new String[0];


    /**
     * The Java class name of the default Mapper class for this Container.
     */
    private String mapperClass =
        "org.apache.catalina.core.StandardContextMapper";


    /**
     * The associated naming resources.
     */
    private NamingResources namingResources = new NamingResources();


    /**
     * The context initialization parameters for this web application,
     * keyed by name.
     */
    private HashMap parameters = new HashMap();


    /**
     * The reloadable flag for this web application.
     */
    private boolean reloadable = false;


    /**
     * The swallowOutput flag for this web application.
     */
    private boolean swallowOutput = false;


    /**
     * The set of classnames of LifecycleListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperLifecycles[] = new String[0];


    /**
     * The set of classnames of ContainerListeners that will be added
     * to each newly created Wrapper by <code>createWrapper()</code>.
     */
    private String wrapperListeners[] = new String[0];


    /**
     * Java class name of the Wrapper class implementation we use.
     */
    private String wrapperClass = "org.apache.catalina.core.StandardWrapper";


    /**
     * JNDI use flag.
     */
    private boolean useNaming = true;


    /**
     * The resources DirContext object with which this Container is
     * associated.
     *
     */
    DirContext dirContext = null;


    /**
     * The human-readable name of this Container.
     */
    protected String name = "defaultContext";


    /**
     * The parent Container to which this Container is a child.
     */
    protected Container parent = null;


    /**
     * The Loader implementation with which this Container is associated.
     */
    protected Loader loader = null;


    /**
     * The Manager implementation with which this Container is associated.
     */
    protected Manager manager = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    // ----------------------------------------------------- Context Properties


    /**
     * Returns true if the internal naming support is used.
     */
    public boolean isUseNaming() {

        return (useNaming);

    }


    /**
     * Enables or disables naming.
     */
    public void setUseNaming(boolean useNaming) {
        this.useNaming = useNaming;
    }


    /**
     * Return the "use cookies for session ids" flag.
     */
    public boolean getCookies() {

        return (this.cookies);

    }


    /**
     * Set the "use cookies for session ids" flag.
     *
     * @param cookies The new flag
     */
    public void setCookies(boolean cookies) {
        boolean oldCookies = this.cookies;
        this.cookies = cookies;

    }


    /**
     * Return the "allow crossing servlet contexts" flag.
     */
    public boolean getCrossContext() {

        return (this.crossContext);

    }


    /**
     * Set the "allow crossing servlet contexts" flag.
     *
     * @param crossContext The new cross contexts flag
     */
    public void setCrossContext(boolean crossContext) {
        boolean oldCrossContext = this.crossContext;
        this.crossContext = crossContext;

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
     * Return the reloadable flag for this web application.
     */
    public boolean getReloadable() {

        return (this.reloadable);

    }


    /**
     * Set the reloadable flag for this web application.
     *
     * @param reloadable The new reloadable flag
     */
    public void setReloadable(boolean reloadable) {
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;

    }


    /**
     * Return the swallowOutput flag for this web application.
     */
    public boolean getSwallowOutput() {

        return (this.swallowOutput);

    }


    /**
     * Set the swallowOutput flag for this web application.
     *
     * @param swallowOutput The new swallowOutput flag
     */
    public void setSwallowOutput(boolean swallowOutput) {
        boolean oldSwallowOutput = this.swallowOutput;
        this.swallowOutput = swallowOutput;

    }


    /**
     * Return the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     */
    public String getWrapperClass() {

        return (this.wrapperClass);

    }


    /**
     * Set the Java class name of the Wrapper implementation used
     * for servlets registered in this Context.
     *
     * @param wrapperClass The new wrapper class
     */
    public void setWrapperClass(String wrapperClass) {
        this.wrapperClass = wrapperClass;

    }


    /**
     * Set the resources DirContext object with which this Container is
     * associated.
     *
     * @param resources The newly associated DirContext
     */
    public void setResources(DirContext resources) {
        this.dirContext = resources;

    }

    /**
     * Get the resources DirContext object with which this Container is
     * associated.
     *
     * @param resources The new associated DirContext
     */
    public DirContext getResources() {

        return this.dirContext;

    }


    /**
     * Return the Loader with which this Container is associated.  If there is
     * no associated Loader return <code>null</code>.
     */
    public Loader getLoader() {

        return loader;

    }


    /**
     * Set the Loader with which this Context is associated.
     *
     * @param loader The newly associated loader
     */
    public void setLoader(Loader loader) {
        Loader oldLoader = this.loader;
        this.loader = loader;
        
        // Report this property change to interested listeners
        support.firePropertyChange("loader", oldLoader, this.loader);
    }


    /**
     * Return the Manager with which this Container is associated.  If there is
     * no associated Manager return <code>null</code>.
     */
    public Manager getManager() {

        return manager;

    }


    /**
     * Set the Manager with which this Container is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {
        Manager oldManager = this.manager;
        this.manager = manager;
        
        // Report this property change to interested listeners
        support.firePropertyChange("manager", oldManager, this.manager);
    }


    // ------------------------------------------------------ Public Properties

    /**
     * The name of this DefaultContext
     */

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent() {

        return (parent);

    }


    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    public void setParent(Container container) {
        Container oldParent = this.parent;
        this.parent = container;
        support.firePropertyChange("parent", oldParent, this.parent);

    }

    // -------------------------------------------------------- Context Methods


    /**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addApplicationListener(String listener) {

        synchronized (applicationListeners) {
            String results[] =new String[applicationListeners.length + 1];
            for (int i = 0; i < applicationListeners.length; i++)
                results[i] = applicationListeners[i];
            results[applicationListeners.length] = listener;
            applicationListeners = results;
        }

    }


    /**
     * Add a new application parameter for this application.
     *
     * @param parameter The new application parameter
     */
    public void addApplicationParameter(ApplicationParameter parameter) {

        synchronized (applicationParameters) {
            ApplicationParameter results[] =
                new ApplicationParameter[applicationParameters.length + 1];
            System.arraycopy(applicationParameters, 0, results, 0,
                             applicationParameters.length);
            results[applicationParameters.length] = parameter;
            applicationParameters = results;
        }

    }


    /**
     * Add an EJB resource reference for this web application.
     *
     * @param ejb New EJB resource reference
     */
    public void addEjb(ContextEjb ejb) {

        namingResources.addEjb(ejb);

    }


    /**
     * Add an environment entry for this web application.
     *
     * @param environment New environment entry
     */
    public void addEnvironment(ContextEnvironment environment) {

        namingResources.addEnvironment(environment);

    }


    /**
     * Add resource parameters for this web application.
     *
     * @param resourceParameters New resource parameters
     */
    public void addResourceParams(ResourceParams resourceParameters) {

        namingResources.addResourceParams(resourceParameters);

    }


    /**
     * Add the classname of an InstanceListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of an InstanceListener class
     */
    public void addInstanceListener(String listener) {

        synchronized (instanceListeners) {
            String results[] =new String[instanceListeners.length + 1];
            for (int i = 0; i < instanceListeners.length; i++)
                results[i] = instanceListeners[i];
            results[instanceListeners.length] = listener;
            instanceListeners = results;
        }

    }


    /**
     * Add a new context initialization parameter, replacing any existing
     * value for the specified name.
     *
     * @param name Name of the new parameter
     * @param value Value of the new  parameter
     *
     * @exception IllegalArgumentException if the name or value is missing,
     *  or if this context initialization parameter has already been
     *  registered
     */
    public void addParameter(String name, String value) {
        // Validate the proposed context initialization parameter
        if ((name == null) || (value == null))
            throw new IllegalArgumentException
                (sm.getString("standardContext.parameter.required"));
        if (parameters.get(name) != null)
            throw new IllegalArgumentException
                (sm.getString("standardContext.parameter.duplicate", name));

        // Add this parameter to our defined set
        synchronized (parameters) {
            parameters.put(name, value);
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
     * Add a resource reference for this web application.
     *
     * @param resource New resource reference
     */
    public void addResource(ContextResource resource) {

        namingResources.addResource(resource);

    }


    /**
     * Add a resource environment reference for this web application.
     *
     * @param name The resource environment reference name
     * @param type The resource environment reference type
     */
    public void addResourceEnvRef(String name, String type) {

        namingResources.addResourceEnvRef(name, type);

    }


    /**
     * Add a resource link for this web application.
     *
     * @param resource New resource link
     */
    public void addResourceLink(ContextResourceLink resourceLink) {

        namingResources.addResourceLink(resourceLink);

    }


    /**
     * Add the classname of a LifecycleListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a LifecycleListener class
     */
    public void addWrapperLifecycle(String listener) {

        synchronized (wrapperLifecycles) {
            String results[] =new String[wrapperLifecycles.length + 1];
            for (int i = 0; i < wrapperLifecycles.length; i++)
                results[i] = wrapperLifecycles[i];
            results[wrapperLifecycles.length] = listener;
            wrapperLifecycles = results;
        }

    }


    /**
     * Add the classname of a ContainerListener to be added to each
     * Wrapper appended to this Context.
     *
     * @param listener Java class name of a ContainerListener class
     */
    public void addWrapperListener(String listener) {

        synchronized (wrapperListeners) {
            String results[] =new String[wrapperListeners.length + 1];
            for (int i = 0; i < wrapperListeners.length; i++)
                results[i] = wrapperListeners[i];
            results[wrapperListeners.length] = listener;
            wrapperListeners = results;
        }

    }


    /**
     * Return the set of application listener class names configured
     * for this application.
     */
    public String[] findApplicationListeners() {

        return (applicationListeners);

    }


    /**
     * Return the set of application parameters for this application.
     */
    public ApplicationParameter[] findApplicationParameters() {

        return (applicationParameters);

    }


    /**
     * Return the EJB resource reference with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired EJB resource reference
     */
    public ContextEjb findEjb(String name) {

        return namingResources.findEjb(name);

    }


    /**
     * Return the defined EJB resource references for this application.
     * If there are none, a zero-length array is returned.
     */
    public ContextEjb[] findEjbs() {

        return namingResources.findEjbs();

    }


    /**
     * Return the environment entry with the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired environment entry
     */
    public ContextEnvironment findEnvironment(String name) {

        return namingResources.findEnvironment(name);

    }


    /**
     * Return the set of defined environment entries for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ContextEnvironment[] findEnvironments() {

        return namingResources.findEnvironments();

    }


    /**
     * Return the set of defined resource parameters for this web
     * application.  If none have been defined, a zero-length array
     * is returned.
     */
    public ResourceParams[] findResourceParams() {

        return namingResources.findResourceParams();

    }


    /**
     * Return the set of InstanceListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findInstanceListeners() {

        return (instanceListeners);

    }


    /**
     * Return the value for the specified context initialization
     * parameter name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the parameter to return
     */
    public String findParameter(String name) {

        synchronized (parameters) {
            return ((String) parameters.get(name));
        }

    }


    /**
     * Return the names of all defined context initialization parameters
     * for this Context.  If no parameters are defined, a zero-length
     * array is returned.
     */
    public String[] findParameters() {

        synchronized (parameters) {
            String results[] = new String[parameters.size()];
            return ((String[]) parameters.keySet().toArray(results));
        }

    }


    /**
     * Return the resource reference with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource reference
     */
    public ContextResource findResource(String name) {

        return namingResources.findResource(name);

    }


    /**
     * Return the resource environment reference type for the specified
     * name, if any; otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource environment reference
     */
    public String findResourceEnvRef(String name) {

        return namingResources.findResourceEnvRef(name);

    }


    /**
     * Return the set of resource environment reference names for this
     * web application.  If none have been specified, a zero-length
     * array is returned.
     */
    public String[] findResourceEnvRefs() {

        return namingResources.findResourceEnvRefs();

    }


    /**
     * Return the resource link with the specified name, if any;
     * otherwise return <code>null</code>.
     *
     * @param name Name of the desired resource link
     */
    public ContextResourceLink findResourceLink(String name) {

        return namingResources.findResourceLink(name);

    }


    /**
     * Return the defined resource links for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResourceLink[] findResourceLinks() {

        return namingResources.findResourceLinks();

    }


    /**
     * Return the defined resource references for this application.  If
     * none have been defined, a zero-length array is returned.
     */
    public ContextResource[] findResources() {

        return namingResources.findResources();

    }


    /**
     * Return the set of LifecycleListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperLifecycles() {

        return (wrapperLifecycles);

    }


    /**
     * Return the set of ContainerListener classes that will be added to
     * newly created Wrappers automatically.
     */
    public String[] findWrapperListeners() {

        return (wrapperListeners);

    }


    /**
     * Return the naming resources associated with this web application.
     */
    public NamingResources getNamingResources() {

        return (this.namingResources);

    }


    /**
     * Remove the specified application listener class from the set of
     * listeners for this application.
     *
     * @param listener Java class name of the listener to be removed
     */
    public void removeApplicationListener(String listener) {

        synchronized (applicationListeners) {

            // Make sure this application listener is currently present
            int n = -1;
            for (int i = 0; i < applicationListeners.length; i++) {
                if (applicationListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified application listener
            int j = 0;
            String results[] = new String[applicationListeners.length - 1];
            for (int i = 0; i < applicationListeners.length; i++) {
                if (i != n)
                    results[j++] = applicationListeners[i];
            }
            applicationListeners = results;

        }


    }


    /**
     * Remove the application parameter with the specified name from
     * the set for this application.
     *
     * @param name Name of the application parameter to remove
     */
    public void removeApplicationParameter(String name) {

        synchronized (applicationParameters) {

            // Make sure this parameter is currently present
            int n = -1;
            for (int i = 0; i < applicationParameters.length; i++) {
                if (name.equals(applicationParameters[i].getName())) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified parameter
            int j = 0;
            ApplicationParameter results[] =
                new ApplicationParameter[applicationParameters.length - 1];
            for (int i = 0; i < applicationParameters.length; i++) {
                if (i != n)
                    results[j++] = applicationParameters[i];
            }
            applicationParameters = results;

        }

    }


    /**
     * Remove any EJB resource reference with the specified name.
     *
     * @param name Name of the EJB resource reference to remove
     */
    public void removeEjb(String name) {

        namingResources.removeEjb(name);

    }


    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    public void removeEnvironment(String name) {

        namingResources.removeEnvironment(name);

    }


    /**
     * Remove a class name from the set of InstanceListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of an InstanceListener class to be removed
     */
    public void removeInstanceListener(String listener) {

        synchronized (instanceListeners) {

            // Make sure this InstanceListener is currently present
            int n = -1;
            for (int i = 0; i < instanceListeners.length; i++) {
                if (instanceListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified InstanceListener
            int j = 0;
            String results[] = new String[instanceListeners.length - 1];
            for (int i = 0; i < instanceListeners.length; i++) {
                if (i != n)
                    results[j++] = instanceListeners[i];
            }
            instanceListeners = results;

        }

    }


    /**
     * Remove the context initialization parameter with the specified
     * name, if it exists; otherwise, no action is taken.
     *
     * @param name Name of the parameter to remove
     */
    public void removeParameter(String name) {

        synchronized (parameters) {
            parameters.remove(name);
        }

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
     * Remove any resource reference with the specified name.
     *
     * @param name Name of the resource reference to remove
     */
    public void removeResource(String name) {

        namingResources.removeResource(name);

    }


    /**
     * Remove any resource environment reference with the specified name.
     *
     * @param name Name of the resource environment reference to remove
     */
    public void removeResourceEnvRef(String name) {

        namingResources.removeResourceEnvRef(name);

    }


    /**
     * Remove any resource link with the specified name.
     *
     * @param name Name of the resource link to remove
     */
    public void removeResourceLink(String name) {

        namingResources.removeResourceLink(name);

    }


    /**
     * Remove a class name from the set of LifecycleListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a LifecycleListener class to be removed
     */
    public void removeWrapperLifecycle(String listener) {


        synchronized (wrapperLifecycles) {

            // Make sure this LifecycleListener is currently present
            int n = -1;
            for (int i = 0; i < wrapperLifecycles.length; i++) {
                if (wrapperLifecycles[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified LifecycleListener
            int j = 0;
            String results[] = new String[wrapperLifecycles.length - 1];
            for (int i = 0; i < wrapperLifecycles.length; i++) {
                if (i != n)
                    results[j++] = wrapperLifecycles[i];
            }
            wrapperLifecycles = results;

        }

    }


    /**
     * Remove a class name from the set of ContainerListener classes that
     * will be added to newly created Wrappers.
     *
     * @param listener Class name of a ContainerListener class to be removed
     */
    public void removeWrapperListener(String listener) {


        synchronized (wrapperListeners) {

            // Make sure this ContainerListener is currently present
            int n = -1;
            for (int i = 0; i < wrapperListeners.length; i++) {
                if (wrapperListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified ContainerListener
            int j = 0;
            String results[] = new String[wrapperListeners.length - 1];
            for (int i = 0; i < wrapperListeners.length; i++) {
                if (i != n)
                    results[j++] = wrapperListeners[i];
            }
            wrapperListeners = results;

        }

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        StandardContext context = null;
        NamingContextListener listener = null;

        if (event.getLifecycle() instanceof StandardContext) {
            context = (StandardContext) event.getLifecycle();
            LifecycleListener[] listeners = context.findLifecycleListeners();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] instanceof NamingContextListener) {
                    listener = (NamingContextListener) listeners[i];
                    break;
                }
            }
        }

        if (listener == null) {
            return;
        }

        if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {

            // Add context
            contexts.put(context, context);

            NamingResources contextResources = context.getNamingResources();

            // Setting the context in read/write mode
            ContextAccessController.setWritable(listener.getName(), context);

            // Send notifications to the listener to add the appropriate 
            // resources
            ContextEjb [] contextEjb = findEjbs();
            for (int i = 0; i < contextEjb.length; i++) {
                ContextEjb contextEntry = contextEjb[i];
                if (contextResources.exists(contextEntry.getName())) {
                    listener.removeEjb(contextEntry.getName());
                }
                listener.addEjb(contextEntry);
            }
            ContextEnvironment [] contextEnv = findEnvironments();
            for (int i = 0; i < contextEnv.length; i++) {
                ContextEnvironment contextEntry = contextEnv[i];
                if (contextResources.exists(contextEntry.getName())) {
                    listener.removeEnvironment(contextEntry.getName());
                }
                listener.addEnvironment(contextEntry);
            }
            ContextResource [] resources = findResources();
            for (int i = 0; i < resources.length; i++) {
                ContextResource contextEntry = resources[i];
                if (contextResources.exists(contextEntry.getName())) {
                    listener.removeResource(contextEntry.getName());
                }
                listener.addResource(contextEntry);
            }
            String [] envRefs = findResourceEnvRefs();
            for (int i = 0; i < envRefs.length; i++) {
                if (contextResources.exists(envRefs[i])) {
                    listener.removeResourceEnvRef(envRefs[i]);
                }
                listener.addResourceEnvRef
                    (envRefs[i], findResourceEnvRef(envRefs[i]));
            }

            // Setting the context in read only mode
            ContextAccessController.setReadOnly(listener.getName());

            // Add listener to the NamingResources listener list
            namingResources.addPropertyChangeListener(listener);

        } else if (event.getType().equals(Lifecycle.BEFORE_STOP_EVENT)) {

            // Remove context
            contexts.remove(context);

            // Remove listener from the NamingResource listener list
            namingResources.removePropertyChangeListener(listener);

            // Remove listener from lifecycle listeners
            context.removeLifecycleListener(this);

        }

    }


    /**
     * Import the configuration from the DefaultContext into
     * current Context.
     *
     * @param context current web application context
     */
    public void importDefaultContext(Context context) {

        if (context instanceof StandardContext) {
            ((StandardContext)context).setUseNaming(isUseNaming());
            ((StandardContext)context).setSwallowOutput(getSwallowOutput());
            if (!contexts.containsKey(context)) {
                ((StandardContext) context).addLifecycleListener(this);
            }
        }

        context.setCookies(getCookies());
        context.setCrossContext(getCrossContext());
        context.setReloadable(getReloadable());

        String [] listeners = findApplicationListeners();
        for( int i = 0; i < listeners.length; i++ ) {
            context.addApplicationListener(listeners[i]);
        }
        listeners = findInstanceListeners();
        for( int i = 0; i < listeners.length; i++ ) {
            context.addInstanceListener(listeners[i]);
        }
        String [] wrapper = findWrapperListeners();
        for( int i = 0; i < wrapper.length; i++ ) {
            context.addWrapperListener(wrapper[i]);
        }
        wrapper = findWrapperLifecycles();
        for( int i = 0; i < wrapper.length; i++ ) {
            context.addWrapperLifecycle(wrapper[i]);
        }
        String [] parameters = findParameters();
        for( int i = 0; i < parameters.length; i++ ) {
            context.addParameter(parameters[i],findParameter(parameters[i]));
        }
        ApplicationParameter [] appParam = findApplicationParameters();
        for( int i = 0; i < appParam.length; i++ ) {
            context.addApplicationParameter(appParam[i]);
        }

        if (!(context instanceof StandardContext)) {
            ContextEjb [] contextEjb = findEjbs();
            for( int i = 0; i < contextEjb.length; i++ ) {
                context.addEjb(contextEjb[i]);
            }
            ContextEnvironment [] contextEnv = findEnvironments();
            for( int i = 0; i < contextEnv.length; i++ ) {
                context.addEnvironment(contextEnv[i]);
            }
            /*
            if (context instanceof StandardContext) {
                ResourceParams [] resourceParams = findResourceParams();
                for( int i = 0; i < resourceParams.length; i++ ) {
                    ((StandardContext)context).addResourceParams
                        (resourceParams[i]);
                }
            }
            */
            ContextResource [] resources = findResources();
            for( int i = 0; i < resources.length; i++ ) {
                context.addResource(resources[i]);
            }
            String [] envRefs = findResourceEnvRefs();
            for( int i = 0; i < envRefs.length; i++ ) {
                context.addResourceEnvRef
                    (envRefs[i],findResourceEnvRef(envRefs[i]));
            }
        }

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
        sb.append("DefaultContext[");
        sb.append("]");
        return (sb.toString());

    }


}
