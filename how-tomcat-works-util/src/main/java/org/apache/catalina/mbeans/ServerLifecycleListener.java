/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/ServerLifecycleListener.java,v 1.35 2002/09/20 21:20:44 amyroh Exp $
 * $Revision: 1.35 $
 * $Date: 2002/09/20 21:20:44 $
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

package org.apache.catalina.mbeans;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import javax.management.MBeanException;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;



/**
 * Implementation of <code>LifecycleListener</code> that
 * instantiates the set of MBeans associated with the components of a
 * running instance of Catalina.
 *
 * @author Craig R. McClanahan
 * @author Amy Roh
 * @version $Revision: 1.35 $ $Date: 2002/09/20 21:20:44 $
 */

public class ServerLifecycleListener
    implements ContainerListener, LifecycleListener, PropertyChangeListener {


    // ------------------------------------------------------------- Properties


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }


    /**
     * Semicolon separated list of paths containing MBean desciptor resources.
     */
    protected String descriptors = null;

    public String getDescriptors() {
        return (this.descriptors);
    }

    public void setDescriptors(String descriptors) {
        this.descriptors = descriptors;
    }


    // ---------------------------------------------- ContainerListener Methods


    /**
     * Handle a <code>ContainerEvent</code> from one of the Containers we are
     * interested in.
     *
     * @param event The event that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        try {
            String type = event.getType();
            if (Container.ADD_CHILD_EVENT.equals(type)) {
                processContainerAddChild(event.getContainer(),
                                         (Container) event.getData());
            } else if (Container.ADD_VALVE_EVENT.equals(type)) {
                processContainerAddValve(event.getContainer(),
                                         (Valve) event.getData());
            } else if (Container.REMOVE_CHILD_EVENT.equals(type)) {
                processContainerRemoveChild(event.getContainer(),
                                            (Container) event.getData());
            } else if (Container.REMOVE_VALVE_EVENT.equals(type)) {
                processContainerRemoveValve(event.getContainer(),
                                            (Valve) event.getData());
            }
        } catch (Exception e) {
            log("Exception processing event " + event, e);
        }

    }


    // ---------------------------------------------- LifecycleListener Methods


    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {
        Lifecycle lifecycle = event.getLifecycle();
        if (Lifecycle.START_EVENT.equals(event.getType())) {

            if (lifecycle instanceof Server) {

                // Loading additional MBean descriptors
                loadMBeanDescriptors();
                createMBeans();

            }

            /*
            // Ignore events from StandardContext objects to avoid
            // reregistering the context
            if (lifecycle instanceof StandardContext)
                return;
            createMBeans();
            */

        } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {

            if (lifecycle instanceof Server) {
                destroyMBeans();
            }

        } else if (Context.RELOAD_EVENT.equals(event.getType())) {

            // Give context a new handle to the MBean server if the
            // context has been reloaded since reloading causes the
            // context to lose its previous handle to the server
            if (lifecycle instanceof StandardContext) {
                // If the context is privileged, give a reference to it
                // in a servlet context attribute
                StandardContext context = (StandardContext)lifecycle;
                if (context.getPrivileged()) {
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_REGISTRY_ATTR,
                         MBeanUtils.createRegistry());
                    context.getServletContext().setAttribute
                        (Globals.MBEAN_SERVER_ATTR,
                         MBeanUtils.createServer());
                }
            }

        }

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Handle a <code>PropertyChangeEvent</code> from one of the Containers
     * we are interested in.
     *
     * @param event The event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() instanceof Container) {
            try {
                processContainerPropertyChange((Container) event.getSource(),
                                               event.getPropertyName(),
                                               event.getOldValue(),
                                               event.getNewValue());
            } catch (Exception e) {
                log("Exception handling Container property change", e);
            }
        } else if (event.getSource() instanceof DefaultContext) {
            try {
                processDefaultContextPropertyChange
                    ((DefaultContext) event.getSource(),
                     event.getPropertyName(),
                     event.getOldValue(),
                     event.getNewValue());
            } catch (Exception e) {
                log("Exception handling DefaultContext property change", e);
            }            
        } else if (event.getSource() instanceof NamingResources) {
            try {
                processNamingResourcesPropertyChange
                    ((NamingResources) event.getSource(),
                     event.getPropertyName(),
                     event.getOldValue(),
                     event.getNewValue());
            } catch (Exception e) {
                log("Exception handling NamingResources property change", e);
            }
        } else if (event.getSource() instanceof Server) {
            try {
                processServerPropertyChange((Server) event.getSource(),
                                            event.getPropertyName(),
                                            event.getOldValue(),
                                            event.getNewValue());
            } catch (Exception e) {
                log("Exception handing Server property change", e);
            }
        } else if (event.getSource() instanceof Service) {
            try {
                processServicePropertyChange((Service) event.getSource(),
                                             event.getPropertyName(),
                                             event.getOldValue(),
                                             event.getNewValue());
            } catch (Exception e) {
                log("Exception handing Service property change", e);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Load additional MBean descriptor resources.
     */
    protected void loadMBeanDescriptors() {

        if (descriptors != null) {
            StringTokenizer tokenizer = new StringTokenizer(descriptors, ";");
            while (tokenizer.hasMoreTokens()) {
                String resource = tokenizer.nextToken();
                MBeanUtils.loadMBeanDescriptors(resource);
            }
        }

    }


    /**
     * Create the MBeans that correspond to every existing node of our tree.
     */
    protected void createMBeans() {

        try {

            MBeanFactory factory = new MBeanFactory();
            createMBeans(factory);
            createMBeans(ServerFactory.getServer());

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("createMBeans: MBeanException", e);

        } catch (Throwable t) {

            log("createMBeans: Throwable", t);

        }

    }


    /**
     * Create the MBeans for the specified Connector and its nested components.
     *
     * @param connector Connector for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Connector connector) throws Exception {

        // Create the MBean for the Connnector itself
        if (debug >= 5)
            log("Creating MBean for Connector " + connector);
        MBeanUtils.createMBean(connector);

    }


    /**
     * Create the MBeans for the specified Context and its nested components.
     *
     * @param context Context for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Context context) throws Exception {
        // Create the MBean for the Context itself
        if (debug >= 4)
            log("Creating MBean for Context " + context);
        MBeanUtils.createMBean(context);
        context.addContainerListener(this);
        if (context instanceof StandardContext) {
            ((StandardContext) context).addPropertyChangeListener(this);
            ((StandardContext) context).addLifecycleListener(this);
        }

        // If the context is privileged, give a reference to it
        // in a servlet context attribute
        if (context.getPrivileged()) {
            context.getServletContext().setAttribute
                (Globals.MBEAN_REGISTRY_ATTR,
                 MBeanUtils.createRegistry());
            context.getServletContext().setAttribute
                (Globals.MBEAN_SERVER_ATTR, 
                 MBeanUtils.createServer());
        }

        // Create the MBeans for the associated nested components
        Loader cLoader = context.getLoader();
        if (cLoader != null) {
            if (debug >= 4)
                log("Creating MBean for Loader " + cLoader);
            MBeanUtils.createMBean(cLoader);
        }
        Logger hLogger = context.getParent().getLogger();
        Logger cLogger = context.getLogger();
        if ((cLogger != null) && (cLogger != hLogger)) {
            if (debug >= 4)
                log("Creating MBean for Logger " + cLogger);
            MBeanUtils.createMBean(cLogger);
        }
        Manager cManager = context.getManager();
        if (cManager != null) {
            if (debug >= 4)
                log("Creating MBean for Manager " + cManager);
            MBeanUtils.createMBean(cManager);
        }
        Realm hRealm = context.getParent().getRealm();
        Realm cRealm = context.getRealm();
        if ((cRealm != null) && (cRealm != hRealm)) {
            if (debug >= 4)
                log("Creating MBean for Realm " + cRealm);
            MBeanUtils.createMBean(cRealm);
        }

        // Create the MBeans for the associated Valves
        if (context instanceof StandardContext) {
            Valve cValves[] = ((StandardContext)context).getValves();
            for (int l = 0; l < cValves.length; l++) {
                if (debug >= 4)
                    log("Creating MBean for Valve " + cValves[l]);
                MBeanUtils.createMBean(cValves[l]);
            }
            
        }        
        
        // Create the MBeans for the NamingResources (if any)
        NamingResources resources = context.getNamingResources();
        createMBeans(resources);

    }


    /**
     * Create the MBeans for the specified ContextEnvironment entry.
     *
     * @param environment ContextEnvironment for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextEnvironment environment)
        throws Exception {

        // Create the MBean for the ContextEnvironment itself
        if (debug >= 3) {
            log("Creating MBean for ContextEnvironment " + environment);
        }
        MBeanUtils.createMBean(environment);

    }


    /**
     * Create the MBeans for the specified ContextResource entry.
     *
     * @param resource ContextResource for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextResource resource)
        throws Exception {

        // Create the MBean for the ContextResource itself
        if (debug >= 3) {
            log("Creating MBean for ContextResource " + resource);
        }
        MBeanUtils.createMBean(resource);

    }

    
    /**
     * Create the MBeans for the specified ContextResourceLink entry.
     *
     * @param resourceLink ContextResourceLink for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(ContextResourceLink resourceLink)
        throws Exception {

        // Create the MBean for the ContextResourceLink itself
        if (debug >= 3) {
            log("Creating MBean for ContextResourceLink " + resourceLink);
        }
        MBeanUtils.createMBean(resourceLink);

    }


    /**
     * Create the MBeans for the specified DefaultContext and its nested components.
     *
     * @param dcontext DefaultContext for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(DefaultContext dcontext) throws Exception {

        // Create the MBean for the DefaultContext itself
        if (debug >= 4)
            log("Creating MBean for DefaultContext " + dcontext);
        MBeanUtils.createMBean(dcontext);
        dcontext.addPropertyChangeListener(this);
        
        // Create the MBeans for the associated nested components
        Loader dLoader = dcontext.getLoader();
        if (dLoader != null) {
            if (debug >= 4)
                log("Creating MBean for Loader " + dLoader);
            MBeanUtils.createMBean(dLoader);
        }
     
        Manager dManager = dcontext.getManager();
        if (dManager != null) {
            if (debug >= 4)
                log("Creating MBean for Manager " + dManager);
            MBeanUtils.createMBean(dManager);
        }
        
        // Create the MBeans for the NamingResources (if any)
        NamingResources resources = dcontext.getNamingResources();
        createMBeans(resources);

    }

    
    /**
     * Create the MBeans for the specified Engine and its nested components.
     *
     * @param engine Engine for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Engine engine) throws Exception {

        // Create the MBean for the Engine itself
        if (debug >= 2) {
            log("Creating MBean for Engine " + engine);
        }
        MBeanUtils.createMBean(engine);
        engine.addContainerListener(this);
        if (engine instanceof StandardEngine) {
            ((StandardEngine) engine).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        Logger eLogger = engine.getLogger();
        if (eLogger != null) {
            if (debug >= 2)
                log("Creating MBean for Logger " + eLogger);
            MBeanUtils.createMBean(eLogger);
        }
        Realm eRealm = engine.getRealm();
        if (eRealm != null) {
            if (debug >= 2)
                log("Creating MBean for Realm " + eRealm);
            MBeanUtils.createMBean(eRealm);
        }

        // Create the MBeans for the associated Valves
        if (engine instanceof StandardEngine) {
            Valve eValves[] = ((StandardEngine)engine).getValves();
            for (int j = 0; j < eValves.length; j++) {
                if (debug >= 2)
                    log("Creating MBean for Valve " + eValves[j]);
                MBeanUtils.createMBean(eValves[j]);
            }
        }

        // Create the MBeans for each child Host
        Container hosts[] = engine.findChildren();
        for (int j = 0; j < hosts.length; j++) {
            createMBeans((Host) hosts[j]);
        }

        // Create the MBeans for DefaultContext
        DefaultContext dcontext = engine.getDefaultContext();
        if (dcontext != null) {
            dcontext.setParent(engine);
            createMBeans(dcontext);
        }

    }


    /**
     * Create the MBeans for the specified Host and its nested components.
     *
     * @param host Host for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Host host) throws Exception {

        // Create the MBean for the Host itself
        if (debug >= 3) {
            log("Creating MBean for Host " + host);
        }
        MBeanUtils.createMBean(host);
        host.addContainerListener(this);
        if (host instanceof StandardHost) {
            ((StandardHost) host).addPropertyChangeListener(this);
        }

        // Create the MBeans for the associated nested components
        Logger eLogger = host.getParent().getLogger();
        Logger hLogger = host.getLogger();
        if ((hLogger != null) && (hLogger != eLogger)) {
            if (debug >= 3)
                log("Creating MBean for Logger " + hLogger);
            MBeanUtils.createMBean(hLogger);
        }
        Realm eRealm = host.getParent().getRealm();
        Realm hRealm = host.getRealm();
        if ((hRealm != null) && (hRealm != eRealm)) {
            if (debug >= 3)
                log("Creating MBean for Realm " + hRealm);
            MBeanUtils.createMBean(hRealm);
        }

        // Create the MBeans for the associated Valves
        if (host instanceof StandardHost) {
            Valve hValves[] = ((StandardHost)host).getValves();
            for (int k = 0; k < hValves.length; k++) {
                if (debug >= 3)
                    log("Creating MBean for Valve " + hValves[k]);
                MBeanUtils.createMBean(hValves[k]);
            }
        }

        // Create the MBeans for each child Context
        Container contexts[] = host.findChildren();
        for (int k = 0; k < contexts.length; k++) {
            createMBeans((Context) contexts[k]);
        }

        // Create the MBeans for DefaultContext
        DefaultContext dcontext = host.getDefaultContext();
        if (dcontext != null) {
            dcontext.setParent(host);
            createMBeans(dcontext);
        }
    
    }


    /**
     * Create the MBeans for MBeanFactory.
     *
     * @param factory MBeanFactory for which to create MBean
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(MBeanFactory factory) throws Exception {

        // Create the MBean for the MBeanFactory
        if (debug >= 2)
            log("Creating MBean for MBeanFactory " + factory);
        MBeanUtils.createMBean(factory);

    }


    /**
     * Create the MBeans for the specified NamingResources and its
     * nested components.
     *
     * @param resources NamingResources for which to create MBeans
     */
    protected void createMBeans(NamingResources resources) throws Exception {

        // Create the MBean for the NamingResources itself
        if (debug >= 2) {
            log("Creating MBean for NamingResources " + resources);
        }
        MBeanUtils.createMBean(resources);
        resources.addPropertyChangeListener(this);

        // Create the MBeans for each child environment entry
        ContextEnvironment environments[] = resources.findEnvironments();
        for (int i = 0; i < environments.length; i++) {
            createMBeans(environments[i]);
        }

        // Create the MBeans for each child resource entry
        ContextResource cresources[] = resources.findResources();
        for (int i = 0; i < cresources.length; i++) {
            createMBeans(cresources[i]);
        }
        
        // Create the MBeans for each child resource link entry
        ContextResourceLink cresourcelinks[] = resources.findResourceLinks();
        for (int i = 0; i < cresourcelinks.length; i++) {
            createMBeans(cresourcelinks[i]);
        }

    }


    /**
     * Create the MBeans for the specified Server and its nested components.
     *
     * @param server Server for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Server server) throws Exception {

        // Create the MBean for the Server itself
        if (debug >= 2)
            log("Creating MBean for Server " + server);
        MBeanUtils.createMBean(server);
        if (server instanceof StandardServer) {
            ((StandardServer) server).addPropertyChangeListener(this);
        }

        // Create the MBeans for the global NamingResources (if any)
        NamingResources resources = server.getGlobalNamingResources();
        if (resources != null) {
            createMBeans(resources);
        }

        // Create the MBeans for each child Service
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            // FIXME - Warp object hierarchy not currently supported
            if (services[i].getContainer().getClass().getName().equals
                ("org.apache.catalina.connector.warp.WarpEngine")) {
                if (debug >= 1) {
                    log("Skipping MBean for Service " + services[i]);
                }
                continue;
            }
            createMBeans(services[i]);
        }

    }


    /**
     * Create the MBeans for the specified Service and its nested components.
     *
     * @param service Service for which to create MBeans
     *
     * @exception Exception if an exception is thrown during MBean creation
     */
    protected void createMBeans(Service service) throws Exception {

        // Create the MBean for the Service itself
        if (debug >= 2)
            log("Creating MBean for Service " + service);
        MBeanUtils.createMBean(service);
        if (service instanceof StandardService) {
            ((StandardService) service).addPropertyChangeListener(this);
        }

        // Create the MBeans for the corresponding Connectors
        Connector connectors[] = service.findConnectors();
        for (int j = 0; j < connectors.length; j++) {
            createMBeans(connectors[j]);
        }

        // Create the MBean for the associated Engine and friends
        Engine engine = (Engine) service.getContainer();
        if (engine != null) {
            createMBeans(engine);
        }

    }


    /**
     * Destroy the MBeans that correspond to every existing node of our tree.
     */
    protected void destroyMBeans() {

        try {

            destroyMBeans(ServerFactory.getServer());

        } catch (MBeanException t) {

            Exception e = t.getTargetException();
            if (e == null) {
                e = t;
            }
            log("destroyMBeans: MBeanException", e);

        } catch (Throwable t) {

            log("destroyMBeans: Throwable", t);

        }

    }


    /**
     * Deregister the MBeans for the specified Connector and its nested
     * components.
     *
     * @param connector Connector for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Connector connector, Service service)
        throws Exception {

        // deregister the MBean for the Connector itself
        if (debug >= 5)
            log("Destroying MBean for Connector " + connector);
        MBeanUtils.destroyMBean(connector, service);

    }


    /**
     * Deregister the MBeans for the specified Context and its nested
     * components.
     *
     * @param context Context for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Context context) throws Exception {

        // Deregister ourselves as a ContainerListener
        context.removeContainerListener(this);

        // destroy the MBeans for the associated Valves
        if (context instanceof StandardContext) {
            Valve cValves[] = ((StandardContext)context).getValves();
            for (int l = 0; l < cValves.length; l++) {
                if (debug >= 4)
                    log("Destroying MBean for Valve " + cValves[l]);
                MBeanUtils.destroyMBean(cValves[l], context);
            }
            
        }

        // Destroy the MBeans for the associated nested components
        Realm hRealm = context.getParent().getRealm();
        Realm cRealm = context.getRealm();
        if ((cRealm != null) && (cRealm != hRealm)) {
            if (debug >= 4)
                log("Destroying MBean for Realm " + cRealm);
            MBeanUtils.destroyMBean(cRealm);
        }
        Manager cManager = context.getManager();
        if (cManager != null) {
            if (debug >= 4)
                log("Destroying MBean for Manager " + cManager);
            MBeanUtils.destroyMBean(cManager);
        }
        Logger hLogger = context.getParent().getLogger();
        Logger cLogger = context.getLogger();
        if ((cLogger != null) && (cLogger != hLogger)) {
            if (debug >= 4)
                log("Destroying MBean for Logger " + cLogger);
            MBeanUtils.destroyMBean(cLogger);
        }
        Loader cLoader = context.getLoader();
        if (cLoader != null) {
            if (debug >= 4)
                log("Destroying MBean for Loader " + cLoader);
            MBeanUtils.destroyMBean(cLoader);
        }

        // Destroy the MBeans for the NamingResources (if any)
        NamingResources resources = context.getNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }
        
        // deregister the MBean for the Context itself
        if (debug >= 4)
            log("Destroying MBean for Context " + context);
        MBeanUtils.destroyMBean(context);
        if (context instanceof StandardContext) {
            ((StandardContext) context).
                removePropertyChangeListener(this);
        }

    }


    /**
     * Deregister the MBeans for the specified ContextEnvironment entry.
     *
     * @param environment ContextEnvironment for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextEnvironment environment)
        throws Exception {

        // Destroy the MBean for the ContextEnvironment itself
        if (debug >= 3) {
            log("Destroying MBean for ContextEnvironment " + environment);
        }
        MBeanUtils.destroyMBean(environment);

    }


    /**
     * Deregister the MBeans for the specified ContextResource entry.
     *
     * @param resource ContextResource for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextResource resource)
        throws Exception {

        // Destroy the MBean for the ContextResource itself
        if (debug >= 3) {
            log("Destroying MBean for ContextResource " + resource);
        }
        MBeanUtils.destroyMBean(resource);

    }


    /**
     * Deregister the MBeans for the specified ContextResourceLink entry.
     *
     * @param resourceLink ContextResourceLink for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(ContextResourceLink resourceLink)
        throws Exception {

        // Destroy the MBean for the ContextResourceLink itself
        if (debug >= 3) {
            log("Destroying MBean for ContextResourceLink " + resourceLink);
        }
        MBeanUtils.destroyMBean(resourceLink);

    }
    
    
    /**
     * Deregister the MBeans for the specified DefaultContext and its nested
     * components.
     *
     * @param dcontext DefaultContext for which to deregister MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(DefaultContext dcontext) throws Exception {

        Manager dManager = dcontext.getManager();
        if (dManager != null) {
            if (debug >= 4)
                log("Destroying MBean for Manager " + dManager);
            MBeanUtils.destroyMBean(dManager);
        }
        
        Loader dLoader = dcontext.getLoader();
        if (dLoader != null) {
            if (debug >= 4)
                log("Destroying MBean for Loader " + dLoader);
            MBeanUtils.destroyMBean(dLoader);
        }

        // Destroy the MBeans for the NamingResources (if any)
        NamingResources resources = dcontext.getNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }
        
        // deregister the MBean for the DefaultContext itself
        if (debug >= 4)
            log("Destroying MBean for Context " + dcontext);
        MBeanUtils.destroyMBean(dcontext);
        dcontext.removePropertyChangeListener(this);

    }    
    
    
    /**
     * Deregister the MBeans for the specified Engine and its nested
     * components.
     *
     * @param engine Engine for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Engine engine) throws Exception {

        // Deregister ourselves as a ContainerListener
        engine.removeContainerListener(this);

        // Deregister the MBeans for each child Host
        Container hosts[] = engine.findChildren();
        for (int k = 0; k < hosts.length; k++) {
            destroyMBeans((Host) hosts[k]);
        }

        // Deregister the MBeans for the associated Valves
        if (engine instanceof StandardEngine) {
            Valve eValves[] = ((StandardEngine)engine).getValves();
            for (int k = 0; k < eValves.length; k++) {
                if (debug >= 3)
                    log("Destroying MBean for Valve " + eValves[k]);
                MBeanUtils.destroyMBean(eValves[k], engine);
            }
        }

        // Deregister the MBeans for the associated nested components
        Realm eRealm = engine.getRealm();
        if (eRealm != null) {
            if (debug >= 3)
                log("Destroying MBean for Realm " + eRealm);
            MBeanUtils.destroyMBean(eRealm);
        }
        Logger eLogger = engine.getLogger();
        if (eLogger != null) {
            if (debug >= 3)
                log("Destroying MBean for Logger " + eLogger);
            MBeanUtils.destroyMBean(eLogger);
        }

        // Deregister the MBean for the Engine itself
        if (debug >= 2) {
            log("Destroying MBean for Engine " + engine);
        }
        MBeanUtils.destroyMBean(engine);

    }


    /**
     * Deregister the MBeans for the specified Host and its nested components.
     *
     * @param host Host for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Host host) throws Exception {

        // Deregister ourselves as a ContainerListener
        host.removeContainerListener(this);

        // Deregister the MBeans for each child Context
        Container contexts[] = host.findChildren();
        for (int k = 0; k < contexts.length; k++) {
            destroyMBeans((Context) contexts[k]);
        }

        // Deregister the MBeans for the associated Valves
        if (host instanceof StandardHost) {
            Valve hValves[] = ((StandardHost)host).getValves();
            for (int k = 0; k < hValves.length; k++) {
                if (debug >= 3)
                    log("Destroying MBean for Valve " + hValves[k]);
                MBeanUtils.destroyMBean(hValves[k], host);
            }
        }

        // Deregister the MBeans for the associated nested components
        Realm eRealm = host.getParent().getRealm();
        Realm hRealm = host.getRealm();
        if ((hRealm != null) && (hRealm != eRealm)) {
            if (debug >= 3)
                log("Destroying MBean for Realm " + hRealm);
            MBeanUtils.destroyMBean(hRealm);
        }
        Logger eLogger = host.getParent().getLogger();
        Logger hLogger = host.getLogger();
        if ((hLogger != null) && (hLogger != eLogger)) {
            if (debug >= 3)
                log("Destroying MBean for Logger " + hLogger);
            MBeanUtils.destroyMBean(hLogger);
        }

        // Deregister the MBean for the Host itself
        if (debug >= 3) {
            log("Destroying MBean for Host " + host);
        }
        MBeanUtils.destroyMBean(host);

    }


    /**
     * Deregister the MBeans for the specified NamingResources and its
     * nested components.
     *
     * @param resources NamingResources for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(NamingResources resources) throws Exception {

        // Destroy the MBeans for each child resource entry
        ContextResource cresources[] = resources.findResources();
        for (int i = 0; i < cresources.length; i++) {
            destroyMBeans(cresources[i]);
        }
        
        // Destroy the MBeans for each child resource link entry
        ContextResourceLink cresourcelinks[] = resources.findResourceLinks();
        for (int i = 0; i < cresourcelinks.length; i++) {
            destroyMBeans(cresourcelinks[i]);
        }
        
        // Destroy the MBeans for each child environment entry
        ContextEnvironment environments[] = resources.findEnvironments();
        for (int i = 0; i < environments.length; i++) {
            destroyMBeans(environments[i]);
        }

        // Destroy the MBean for the NamingResources itself
        if (debug >= 2) {
            log("Destroying MBean for NamingResources " + resources);
        }
        MBeanUtils.destroyMBean(resources);
        resources.removePropertyChangeListener(this);

    }


    /**
     * Deregister the MBeans for the specified Server and its related
     * components.
     *
     * @param server Server for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Server server) throws Exception {
        
        // Destroy the MBeans for the global NamingResources (if any)
        NamingResources resources = server.getGlobalNamingResources();
        if (resources != null) {
            destroyMBeans(resources);
        }
        
        // Destroy the MBeans for each child Service
        Service services[] = server.findServices();
        for (int i = 0; i < services.length; i++) {
            // FIXME - Warp object hierarchy not currently supported
            if (services[i].getContainer().getClass().getName().equals
                ("org.apache.catalina.connector.warp.WarpEngine")) {
                if (debug >= 1) {
                    log("Skipping MBean for Service " + services[i]);
                }
                continue;
            }
            destroyMBeans(services[i]);
        }

        // Destroy the MBean for the Server itself
        if (debug >= 2) {
            log("Destroying MBean for Server " + server);
        }
        MBeanUtils.destroyMBean(server);
        if (server instanceof StandardServer) {
            ((StandardServer) server).removePropertyChangeListener(this);
        }

    }


    /**
     * Deregister the MBeans for the specified Service and its nested
     * components.
     *
     * @param service Service for which to destroy MBeans
     *
     * @exception Exception if an exception is thrown during MBean destruction
     */
    protected void destroyMBeans(Service service) throws Exception {

        // Deregister the MBeans for the associated Engine
        Engine engine = (Engine) service.getContainer();
        if (engine != null) {
            destroyMBeans(engine);
        }

        // Deregister the MBeans for the corresponding Connectors
        Connector connectors[] = service.findConnectors();
        for (int j = 0; j < connectors.length; j++) {
            destroyMBeans(connectors[j], service);
        }

        // Deregister the MBean for the Service itself
        if (debug >= 2) {
            log("Destroying MBean for Service " + service);
        }
        MBeanUtils.destroyMBean(service);
        if (service instanceof StandardService) {
            ((StandardService) service).removePropertyChangeListener(this);
        }

    }


    /**
     * Log a message.
     *
     * @param message The message to be logged
     */
    protected void log(String message) {

        System.out.print("ServerLifecycleListener: ");
        System.out.println(message);

    }


    /**
     * Log a message and associated exception.
     *
     * @param message The message to be logged
     * @param throwable The exception to be logged
     */
    protected void log(String message, Throwable throwable) {

        log(message);
        throwable.printStackTrace(System.out);

    }


    /**
     * Process the addition of a new child Container to a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerAddChild(Container parent,
                                            Container child) {

        if (debug >= 1)
            log("Process addChild[parent=" + parent + ",child=" + child + "]");

        try {
            if (child instanceof Context) {
                createMBeans((Context) child);
            } else if (child instanceof Engine) {
                createMBeans((Engine) child);
            } else if (child instanceof Host) {
                createMBeans((Host) child);
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerAddChild: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerAddChild: Throwable", t);
        }

    }


    /**
     * Process the addition of a new Valve to a Container.
     *
     * @param container The affected Container
     * @param valve The new Valve
     */
    protected void processContainerAddValve(Container container,
                                            Valve valve)
        throws Exception {

        if (debug >= 1) {
            log("Process addValve[container=" + container + ",valve=" +
                valve + "]");
        }

        if (debug >= 4) {
            log("Creating MBean for Valve " + valve);
        }
        MBeanUtils.createMBean(valve);

    }


    /**
     * Process a property change event on a Container.
     *
     * @param container The container on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processContainerPropertyChange(Container container,
                                                  String propertyName,
                                                  Object oldValue,
                                                  Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[container=" + container +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("defaultContext".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for DefaultContext " + oldValue);
                }
                destroyMBeans((DefaultContext) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for DefaultContext " + newValue);
                }
                createMBeans((DefaultContext) newValue);
            }
        } else if ("loader".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Loader " + oldValue);
                }
                MBeanUtils.destroyMBean((Loader) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Loader " + newValue);
                }
                MBeanUtils.createMBean((Loader) newValue);
            }
        } else if ("logger".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Logger " + oldValue);
                }
                MBeanUtils.destroyMBean((Logger) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Logger " + newValue);
                }
                MBeanUtils.createMBean((Logger) newValue);
            }
        } else if ("manager".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Manager " + oldValue);
                }
                MBeanUtils.destroyMBean((Manager) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Manager " + newValue);
                }
                MBeanUtils.createMBean((Manager) newValue);
            }
        } else if ("realm".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Realm " + oldValue);
                }
                MBeanUtils.destroyMBean((Realm) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Realm " + newValue);
                }
                MBeanUtils.createMBean((Realm) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }


    /**
     * Process a property change event on a DefaultContext.
     *
     * @param defaultContext The DefaultContext on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processDefaultContextPropertyChange(DefaultContext defaultContext,
                                                  String propertyName,
                                                  Object oldValue,
                                                  Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[defaultContext=" + defaultContext +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("loader".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Loader " + oldValue);
                }
                MBeanUtils.destroyMBean((Loader) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Loader " + newValue);
                }
                MBeanUtils.createMBean((Loader) newValue);
            }
        } else if ("logger".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Logger " + oldValue);
                }
                MBeanUtils.destroyMBean((Logger) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Logger " + newValue);
                }
                MBeanUtils.createMBean((Logger) newValue);
            }
        } else if ("manager".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Manager " + oldValue);
                }
                MBeanUtils.destroyMBean((Manager) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Manager " + newValue);
                }
                MBeanUtils.createMBean((Manager) newValue);
            }
        } else if ("realm".equals(propertyName)) {
            if (oldValue != null) {
                if (debug >= 5) {
                    log("Removing MBean for Realm " + oldValue);
                }
                MBeanUtils.destroyMBean((Realm) oldValue);
            }
            if (newValue != null) {
                if (debug >= 5) {
                    log("Creating MBean for Realm " + newValue);
                }
                MBeanUtils.createMBean((Realm) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }
    
    
    /**
     * Process the removal of a child Container from a parent Container.
     *
     * @param parent Parent container
     * @param child Child container
     */
    protected void processContainerRemoveChild(Container parent,
                                               Container child) {

        if (debug >= 1)
            log("Process removeChild[parent=" + parent + ",child=" +
                child + "]");

        try {
            if (child instanceof Context) {
                Context context = (Context) child;
                if (context.getPrivileged()) {
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_REGISTRY_ATTR);
                    context.getServletContext().removeAttribute
                        (Globals.MBEAN_SERVER_ATTR);
                }
                if (debug >= 4)
                    log("  Removing MBean for Context " + context);
                destroyMBeans(context);
                if (context instanceof StandardContext) {
                    ((StandardContext) context).
                        removePropertyChangeListener(this);
                }
            } else if (child instanceof Host) {
                Host host = (Host) child;
                destroyMBeans(host);
                if (host instanceof StandardHost) {
                    ((StandardHost) host).
                        removePropertyChangeListener(this);
                }
            }
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerRemoveChild: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerRemoveChild: Throwable", t);
        }

    }


    /**
     * Process the removal of a Valve from a Container.
     *
     * @param container The affected Container
     * @param valve The old Valve
     */
    protected void processContainerRemoveValve(Container container,
                                               Valve valve) {

        if (debug >= 1)
            log("Process removeValve[container=" + container + ",valve=" +
                valve + "]");

        try {
            MBeanUtils.destroyMBean(valve, container);
        } catch (MBeanException t) {
            Exception e = t.getTargetException();
            if (e == null)
                e = t;
            log("processContainerRemoveValve: MBeanException", e);
        } catch (Throwable t) {
            log("processContainerRemoveValve: Throwable", t);
        }

    }


    /**
     * Process a property change event on a NamingResources.
     *
     * @param resources The global naming resources on which this
     *  event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processNamingResourcesPropertyChange
        (NamingResources resources, String propertyName,
         Object oldValue, Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[namingResources=" + resources +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }

        // FIXME - Add other resource types when supported by admin tool
        if ("environment".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextEnvironment) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextEnvironment) newValue);
            }
        } else if ("resource".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextResource) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextResource) newValue);
            }
        } else if ("resourceLink".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((ContextResourceLink) oldValue);
            }
            if (newValue != null) {
                createMBeans((ContextResourceLink) newValue);
            }
        }

    }


    /**
     * Process a property change event on a Server.
     *
     * @param server The server on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processServerPropertyChange(Server server,
                                               String propertyName,
                                               Object oldValue,
                                               Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[server=" + server +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("globalNamingResources".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((NamingResources) oldValue);
            }
            if (newValue != null) {
                createMBeans((NamingResources) newValue);
            }
        } else if ("service".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Service) oldValue);
            }
            if (newValue != null) {
                createMBeans((Service) newValue);
            }
        }

    }


    /**
     * Process a property change event on a Service.
     *
     * @param service The service on which this event occurred
     * @param propertyName The name of the property that changed
     * @param oldValue The previous value (may be <code>null</code>)
     * @param newValue The new value (may be <code>null</code>)
     *
     * @exception Exception if an exception is thrown
     */
    protected void processServicePropertyChange(Service service,
                                                String propertyName,
                                                Object oldValue,
                                                Object newValue)
        throws Exception {

        if (debug >= 6) {
            log("propertyChange[service=" + service +
                ",propertyName=" + propertyName +
                ",oldValue=" + oldValue +
                ",newValue=" + newValue + "]");
        }
        if ("connector".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Connector) oldValue, service);
            }
            if (newValue != null) {
                createMBeans((Connector) newValue);
            }
        } else if ("container".equals(propertyName)) {
            if (oldValue != null) {
                destroyMBeans((Engine) oldValue);
            }
            if (newValue != null) {
                createMBeans((Engine) newValue);
            }
        }

    }


}
