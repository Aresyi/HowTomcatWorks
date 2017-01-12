/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/NamingContextListener.java,v 1.19 2002/06/25 22:29:23 remm Exp $
 * $Revision: 1.19 $
 * $Date: 2002/06/25 22:29:23 $
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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.apache.naming.NamingContext;
import org.apache.naming.ContextBindings;
import org.apache.naming.ContextAccessController;
import org.apache.naming.EjbRef;
import org.apache.naming.ResourceRef;
import org.apache.naming.ResourceEnvRef;
import org.apache.naming.ResourceLinkRef;
import org.apache.naming.TransactionRef;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Server;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.catalina.util.StringManager;


/**
 * Helper class used to initialize and populate the JNDI context associated
 * with each context and server.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.19 $ $Date: 2002/06/25 22:29:23 $
 */

public class NamingContextListener
    implements LifecycleListener, ContainerListener, PropertyChangeListener {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new naming context listener.
     */
    public NamingContextListener() {
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Name of the associated naming context.
     */
    protected String name = "/";


    /**
     * Associated container.
     */
    protected Object container = null;


    /**
     * Debugging level.
     */
    protected int debug = 0;


    /**
     * Initialized flag.
     */
    protected boolean initialized = false;


    /**
     * Associated naming resources.
     */
    protected NamingResources namingResources = null;


    /**
     * Associated JNDI context.
     */
    protected NamingContext namingContext = null;


    /**
     * Comp context.
     */
    protected javax.naming.Context compCtx = null;


    /**
     * Env context.
     */
    protected javax.naming.Context envCtx = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return the "debug" property.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the "debug" property.
     *
     * @param debug The new debug level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the "name" property.
     */
    public String getName() {

        return (this.name);

    }


    /**
     * Set the "name" property.
     *
     * @param name The new name
     */
    public void setName(String name) {

        this.name = name;

    }


    /**
     * Return the associated naming context.
     */
    public NamingContext getNamingContext() {

        return (this.namingContext);

    }


    // ---------------------------------------------- LifecycleListener Methods


    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        container = event.getLifecycle();

        if (container instanceof Context) {
            namingResources = ((Context) container).getNamingResources();
        } else if (container instanceof Server) {
            namingResources = ((Server) container).getGlobalNamingResources();
        } else {
            return;
        }

        if (event.getType() == Lifecycle.START_EVENT) {

            if (initialized)
                return;

            Hashtable contextEnv = new Hashtable();
            try {
                namingContext = new NamingContext(contextEnv, getName());
            } catch (NamingException e) {
                // Never happens
            }
            ContextAccessController.setSecurityToken(getName(), container);
            ContextBindings.bindContext(container, namingContext, container);

            // Setting the context in read/write mode
            ContextAccessController.setWritable(getName(), container);

            try {
                createNamingContext();
            } catch (NamingException e) {
                log(sm.getString("naming.namingContextCreationFailed", e));
            }

            // Binding the naming context to the class loader
            if (container instanceof Context) {
                // Setting the context in read only mode
                ContextAccessController.setReadOnly(getName());
                try {
                    ContextBindings.bindClassLoader
                        (container, container, 
                         ((Container) container).getLoader().getClassLoader());
                } catch (NamingException e) {
                    log(sm.getString("naming.bindFailed", e));
                }
            }

            if (container instanceof Server) {
                namingResources.addPropertyChangeListener(this);
                org.apache.naming.factory.ResourceLinkFactory.setGlobalContext
                    (namingContext);
                try {
                    ContextBindings.bindClassLoader
                        (container, container, 
                         this.getClass().getClassLoader());
                } catch (NamingException e) {
                    log(sm.getString("naming.bindFailed", e));
                }
                if (container instanceof StandardServer) {
                    ((StandardServer) container).setGlobalNamingContext
                        (namingContext);
                }
            }

            initialized = true;

        } else if (event.getType() == Lifecycle.STOP_EVENT) {

            if (!initialized)
                return;

            // Setting the context in read/write mode
            ContextAccessController.setWritable(getName(), container);

            if (container instanceof Context) {
                ContextBindings.unbindClassLoader
                    (container, container, 
                     ((Container) container).getLoader().getClassLoader());
            }

            if (container instanceof Server) {
                namingResources.removePropertyChangeListener(this);
                ContextBindings.unbindClassLoader
                    (container, container, 
                     this.getClass().getClassLoader());
            }

            ContextAccessController.unsetSecurityToken(getName(), container);

            namingContext = null;
            envCtx = null;
            compCtx = null;
            initialized = false;

        }

    }


    // ---------------------------------------------- ContainerListener Methods


    /**
     * Acknowledge the occurrence of the specified event.
     * Note: Will never be called when the listener is associated to a Server,
     * since it is not a Container.
     *
     * @param event ContainerEvent that has occurred
     */
    public void containerEvent(ContainerEvent event) {

        if (!initialized)
            return;

        // Setting the context in read/write mode
        ContextAccessController.setWritable(getName(), container);

        String type = event.getType();

        if (type.equals("addEjb")) {

            String ejbName = (String) event.getData();
            if (ejbName != null) {
                ContextEjb ejb = namingResources.findEjb(ejbName);
                addEjb(ejb);
            }

        } else if (type.equals("addEnvironment")) {

            String environmentName = (String) event.getData();
            if (environmentName != null) {
                ContextEnvironment env = 
                    namingResources.findEnvironment(environmentName);
                addEnvironment(env);
            }

        } else if ((type.equals("addResourceParams")) 
                   || (type.equals("removeResourceParams"))) {

            String resourceParamsName = (String) event.getData();
            if (resourceParamsName != null) {
                ContextEjb ejb = namingResources.findEjb(resourceParamsName);
                if (ejb != null) {
                    removeEjb(resourceParamsName);
                    addEjb(ejb);
                }
                ContextResource resource = 
                    namingResources.findResource(resourceParamsName);
                if (resource != null) {
                    removeResource(resourceParamsName);
                    addResource(resource);
                }
                String resourceEnvRefValue = 
                    namingResources.findResourceEnvRef(resourceParamsName);
                if (resourceEnvRefValue != null) {
                    removeResourceEnvRef(resourceParamsName);
                    addResourceEnvRef(resourceParamsName, resourceEnvRefValue);
                }
                ContextResourceLink resourceLink = 
                    namingResources.findResourceLink(resourceParamsName);
                if (resourceLink != null) {
                    removeResourceLink(resourceParamsName);
                    addResourceLink(resourceLink);
                }
            }

        } else if (type.equals("addLocalEjb")) {

            String localEjbName = (String) event.getData();
            if (localEjbName != null) {
                ContextLocalEjb localEjb = 
                    namingResources.findLocalEjb(localEjbName);
                addLocalEjb(localEjb);
            }

        } else if (type.equals("addResource")) {

            String resourceName = (String) event.getData();
            if (resourceName != null) {
                ContextResource resource = 
                    namingResources.findResource(resourceName);
                addResource(resource);
            }

        } else if (type.equals("addResourceLink")) {

            String resourceLinkName = (String) event.getData();
            if (resourceLinkName != null) {
                ContextResourceLink resourceLink = 
                    namingResources.findResourceLink(resourceLinkName);
                addResourceLink(resourceLink);
            }

        } else if (type.equals("addResourceEnvRef")) {

            String resourceEnvRefName = (String) event.getData();
            if (resourceEnvRefName != null) {
                String resourceEnvRefValue = 
                    namingResources.findResourceEnvRef(resourceEnvRefName);
                addResourceEnvRef(resourceEnvRefName, resourceEnvRefValue);
            }

        } else if (type.equals("removeEjb")) {

            String ejbName = (String) event.getData();
            if (ejbName != null) {
                removeEjb(ejbName);
            }

        } else if (type.equals("removeEnvironment")) {

            String environmentName = (String) event.getData();
            if (environmentName != null) {
                removeEnvironment(environmentName);
            }

        } else if (type.equals("removeLocalEjb")) {

            String localEjbName = (String) event.getData();
            if (localEjbName != null) {
                removeLocalEjb(localEjbName);
            }

        } else if (type.equals("removeResource")) {

            String resourceName = (String) event.getData();
            if (resourceName != null) {
                removeResource(resourceName);
            }

        } else if (type.equals("removeResourceLink")) {

            String resourceLinkName = (String) event.getData();
            if (resourceLinkName != null) {
                removeResourceLink(resourceLinkName);
            }

        } else if (type.equals("removeResourceEnvRef")) {

            String resourceEnvRefName = (String) event.getData();
            if (resourceEnvRefName != null) {
                removeResourceEnvRef(resourceEnvRefName);
            }

        }

        // Setting the context in read only mode
        ContextAccessController.setReadOnly(getName());

    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process property change events.  Currently, only listens to such events
     * on the <code>NamingResources</code> instance for the global naming
     * resources.
     *
     * @param event The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (!initialized)
            return;

        Object source = event.getSource();
        if (source == namingResources) {

            // Setting the context in read/write mode
            ContextAccessController.setWritable(getName(), container);

            processGlobalResourcesChange(event.getPropertyName(),
                                         event.getOldValue(),
                                         event.getNewValue());

            // Setting the context in read only mode
            ContextAccessController.setReadOnly(getName());

        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Process a property change on the global naming resources, by making the
     * corresponding addition or removal to the associated JNDI context.
     *
     * @param name Property name of the change to be processed
     * @param oldValue The old value (or <code>null</code> if adding)
     * @param newValue The new value (or <code>null</code> if removing)
     */
    private void processGlobalResourcesChange(String name,
                                              Object oldValue,
                                              Object newValue) {

        // NOTE - It seems that the Context for global JNDI resources
        // is left in read-write mode, so we do not have to change it here

        if (name.equals("ejb")) {
            if (oldValue != null) {
                ContextEjb ejb = (ContextEjb) oldValue;
                if (ejb.getName() != null) {
                    removeEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                ContextEjb ejb = (ContextEjb) newValue;
                if (ejb.getName() != null) {
                    addEjb(ejb);
                }
            }
        } else if (name.equals("environment")) {
            if (oldValue != null) {
                ContextEnvironment env = (ContextEnvironment) oldValue;
                if (env.getName() != null) {
                    removeEnvironment(env.getName());
                }
            }
            if (newValue != null) {
                ContextEnvironment env = (ContextEnvironment) newValue;
                if (env.getName() != null) {
                    addEnvironment(env);
                }
            }
        } else if (name.equals("localEjb")) {
            if (oldValue != null) {
                ContextLocalEjb ejb = (ContextLocalEjb) oldValue;
                if (ejb.getName() != null) {
                    removeLocalEjb(ejb.getName());
                }
            }
            if (newValue != null) {
                ContextLocalEjb ejb = (ContextLocalEjb) newValue;
                if (ejb.getName() != null) {
                    addLocalEjb(ejb);
                }
            }
        } else if (name.equals("resource")) {
            if (oldValue != null) {
                ContextResource resource = (ContextResource) oldValue;
                if (resource.getName() != null) {
                    removeResource(resource.getName());
                }
            }
            if (newValue != null) {
                ContextResource resource = (ContextResource) newValue;
                if (resource.getName() != null) {
                    addResource(resource);
                }
            }
        } else if (name.equals("resourceEnvRef")) {
            if (oldValue != null) {
                String update = (String) oldValue;
                int colon = update.indexOf(':');
                removeResourceEnvRef(update.substring(0, colon));
            }
            if (newValue != null) {
                String update = (String) newValue;
                int colon = update.indexOf(':');
                addResourceEnvRef(update.substring(0, colon),
                                  update.substring(colon + 1));
            }
        } else if (name.equals("resourceLink")) {
            if (oldValue != null) {
                ContextResourceLink rl = (ContextResourceLink) oldValue;
                if (rl.getName() != null) {
                    removeResourceLink(rl.getName());
                }
            }
            if (newValue != null) {
                ContextResourceLink rl = (ContextResourceLink) newValue;
                if (rl.getName() != null) {
                    addResourceLink(rl);
                }
            }
        } else if (name.equals("resourceParams")) {
            String resourceParamsName = null;
            ResourceParams rp = null;
            if (oldValue != null) {
                rp = (ResourceParams) oldValue;
            }
            if (newValue != null) {
                rp = (ResourceParams) newValue;
            }
            if (rp != null) {
                resourceParamsName = rp.getName();
            }
            if (resourceParamsName != null) {
                ContextEjb ejb = namingResources.findEjb(resourceParamsName);
                if (ejb != null) {
                    removeEjb(resourceParamsName);
                    addEjb(ejb);
                }
                ContextResource resource = 
                    namingResources.findResource(resourceParamsName);
                if (resource != null) {
                    removeResource(resourceParamsName);
                    addResource(resource);
                }
                String resourceEnvRefValue = 
                    namingResources.findResourceEnvRef(resourceParamsName);
                if (resourceEnvRefValue != null) {
                    removeResourceEnvRef(resourceParamsName);
                    addResourceEnvRef(resourceParamsName, resourceEnvRefValue);
                }
                ContextResourceLink resourceLink = 
                    namingResources.findResourceLink(resourceParamsName);
                if (resourceLink != null) {
                    removeResourceLink(resourceParamsName);
                    addResourceLink(resourceLink);
                }
            }
        }


    }


    /**
     * Create and initialize the JNDI naming context.
     */
    private void createNamingContext()
        throws NamingException {

        // Creating the comp subcontext
        if (container instanceof Server) {
            compCtx = namingContext;
            envCtx = namingContext;
        } else {
            compCtx = namingContext.createSubcontext("comp");
            envCtx = compCtx.createSubcontext("env");
        }

        int i;

        if (debug >= 1)
            log("Creating JNDI naming context");

        if (namingResources == null) {
            namingResources = new NamingResources();
            namingResources.setContainer(container);
        }

        // Resource links
        ContextResourceLink[] resourceLinks = 
            namingResources.findResourceLinks();
        for (i = 0; i < resourceLinks.length; i++) {
            addResourceLink(resourceLinks[i]);
        }

        // Resources
        ContextResource[] resources = namingResources.findResources();
        for (i = 0; i < resources.length; i++) {
            addResource(resources[i]);
        }

        // Resources Env
        String[] resourceEnvRefs = namingResources.findResourceEnvRefs();
        for (i = 0; i < resourceEnvRefs.length; i++) {
            String key = resourceEnvRefs[i];
            String type = namingResources.findResourceEnvRef(key);
            addResourceEnvRef(key, type);
        }

        // Environment entries
        ContextEnvironment[] contextEnvironments = 
            namingResources.findEnvironments();
        for (i = 0; i < contextEnvironments.length; i++) {
            addEnvironment(contextEnvironments[i]);
        }

        // EJB references
        ContextEjb[] ejbs = namingResources.findEjbs();
        for (i = 0; i < ejbs.length; i++) {
            addEjb(ejbs[i]);
        }

        // Binding a User Transaction reference
        if (container instanceof Context) {
            try {
                Reference ref = new TransactionRef();
                compCtx.bind("UserTransaction", ref);
                addAdditionalParameters
                    (namingResources, ref, "UserTransaction");
            } catch (NamingException e) {
                log(sm.getString("naming.bindFailed", e));
            }
        }

        // Binding the resources directory context
        if (container instanceof Context) {
            try {
                compCtx.bind("Resources", 
                             ((Container) container).getResources());
            } catch (NamingException e) {
                log(sm.getString("naming.bindFailed", e));
            }
        }

    }


    /**
     * Set the specified EJBs in the naming context.
     */
    public void addEjb(ContextEjb ejb) {

        // Create a reference to the EJB.
        Reference ref = new EjbRef
            (ejb.getType(), ejb.getHome(), ejb.getRemote(), ejb.getLink());
        // Adding the additional parameters, if any
        addAdditionalParameters(ejb.getNamingResources(), ref, ejb.getName());
        try {
            createSubcontexts(envCtx, ejb.getName());
            envCtx.bind(ejb.getName(), ref);
        } catch (NamingException e) {
            log(sm.getString("naming.bindFailed", e));
        }

    }


    /**
     * Set the specified environment entries in the naming context.
     */
    public void addEnvironment(ContextEnvironment env) {

        Object value = null;
        // Instantiating a new instance of the correct object type, and
        // initializing it.
        String type = env.getType();
        try {
            if (type.equals("java.lang.String")) {
                value = env.getValue();
            } else if (type.equals("java.lang.Byte")) {
                if (env.getValue() == null) {
                    value = new Byte((byte) 0);
                } else {
                    value = Byte.decode(env.getValue());
                }
            } else if (type.equals("java.lang.Short")) {
                if (env.getValue() == null) {
                    value = new Short((short) 0);
                } else {
                    value = Short.decode(env.getValue());
                }
            } else if (type.equals("java.lang.Integer")) {
                if (env.getValue() == null) {
                    value = new Integer(0);
                } else {
                    value = Integer.decode(env.getValue());
                }
            } else if (type.equals("java.lang.Long")) {
                if (env.getValue() == null) {
                    value = new Long(0);
                } else {
                    value = Long.decode(env.getValue());
                }
            } else if (type.equals("java.lang.Boolean")) {
                value = Boolean.valueOf(env.getValue());
            } else if (type.equals("java.lang.Double")) {
                if (env.getValue() == null) {
                    value = new Double(0);
                } else {
                    value = Double.valueOf(env.getValue());
                }
            } else if (type.equals("java.lang.Float")) {
                if (env.getValue() == null) {
                    value = new Float(0);
                } else {
                    value = Float.valueOf(env.getValue());
                }
            } else if (type.equals("java.lang.Character")) {
                if (env.getValue() == null) {
                    value = new Character((char) 0);
                } else {
                    if (env.getValue().length() == 1) {
                        value = new Character(env.getValue().charAt(0));
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            } else {
                log(sm.getString("naming.invalidEnvEntryType", env.getName()));
            }
        } catch (NumberFormatException e) {
            log(sm.getString("naming.invalidEnvEntryValue", env.getName()));
        } catch (IllegalArgumentException e) {
            log(sm.getString("naming.invalidEnvEntryValue", env.getName()));
        }

        // Binding the object to the appropriate name
        if (value != null) {
            try {
                if (debug >= 2)
                    log("  Adding environment entry " + env.getName());
                createSubcontexts(envCtx, env.getName());
                envCtx.bind(env.getName(), value);
            } catch (NamingException e) {
                log(sm.getString("naming.invalidEnvEntryValue", e));
            }
        }

    }


    /**
     * Set the specified local EJBs in the naming context.
     */
    public void addLocalEjb(ContextLocalEjb localEjb) {



    }


    /**
     * Set the specified resources in the naming context.
     */
    public void addResource(ContextResource resource) {

        // Create a reference to the resource.
        Reference ref = new ResourceRef
            (resource.getType(), resource.getDescription(),
             resource.getScope(), resource.getAuth());
        // Adding the additional parameters, if any
        addAdditionalParameters(resource.getNamingResources(), ref, 
                                resource.getName());
        try {
            if (debug >= 2) {
                log("  Adding resource ref " + resource.getName());
                log("  " + ref);
            }
            createSubcontexts(envCtx, resource.getName());
            envCtx.bind(resource.getName(), ref);
        } catch (NamingException e) {
            log(sm.getString("naming.bindFailed", e));
        }

    }


    /**
     * Set the specified resources in the naming context.
     */
    public void addResourceEnvRef(String name, String type) {

        // Create a reference to the resource env.
        Reference ref = new ResourceEnvRef(type);
        // Adding the additional parameters, if any
        addAdditionalParameters(null, ref, name);
        try {
            if (debug >= 2)
                log("  Adding resource env ref " + name);
            createSubcontexts(envCtx, name);
            envCtx.bind(name, ref);
        } catch (NamingException e) {
            log(sm.getString("naming.bindFailed", e));
        }

    }


    /**
     * Set the specified resource link in the naming context.
     */
    public void addResourceLink(ContextResourceLink resourceLink) {

        // Create a reference to the resource.
        Reference ref = new ResourceLinkRef
            (resourceLink.getType(), resourceLink.getGlobal());
        // Adding the additional parameters, if any
        addAdditionalParameters(resourceLink.getNamingResources(), ref, 
                                resourceLink.getName());
        try {
            if (debug >= 2)
                log("  Adding resource link " + resourceLink.getName());
            createSubcontexts(envCtx, resourceLink.getName());
            envCtx.bind(resourceLink.getName(), ref);
        } catch (NamingException e) {
            log(sm.getString("naming.bindFailed", e));
        }

    }


    /**
     * Set the specified EJBs in the naming context.
     */
    public void removeEjb(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Set the specified environment entries in the naming context.
     */
    public void removeEnvironment(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Set the specified local EJBs in the naming context.
     */
    public void removeLocalEjb(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Set the specified resources in the naming context.
     */
    public void removeResource(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Set the specified resources in the naming context.
     */
    public void removeResourceEnvRef(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Set the specified resources in the naming context.
     */
    public void removeResourceLink(String name) {

        try {
            envCtx.unbind(name);
        } catch (NamingException e) {
            log(sm.getString("naming.unbindFailed", e));
        }

    }


    /**
     * Create all intermediate subcontexts.
     */
    private void createSubcontexts(javax.naming.Context ctx, String name)
        throws NamingException {
        javax.naming.Context currentContext = ctx;
        StringTokenizer tokenizer = new StringTokenizer(name, "/");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ((!token.equals("")) && (tokenizer.hasMoreTokens())) {
                try {
                    currentContext = currentContext.createSubcontext(token);
                } catch (NamingException e) {
                    // Silent catch. Probably an object is already bound in
                    // the context.
                    currentContext =
                        (javax.naming.Context) currentContext.lookup(token);
                }
            }
        }
    }


    /**
     * Add additional parameters to the reference.
     */
    private void addAdditionalParameters(NamingResources resources, 
                                         Reference ref, String name) {
        if (resources == null) {
            resources = namingResources;
        }
        ResourceParams resourceParameters = resources.findResourceParams(name);
        if (debug >= 2)
            log("  Resource parameters for " + name + " = " +
                resourceParameters);
        if (resourceParameters == null)
            return;
        Hashtable params = resourceParameters.getParameters();
        Enumeration enum1 = params.keys();
        while (enum1.hasMoreElements()) {
            String paramName = (String) enum1.nextElement();
            String paramValue = (String) params.get(paramName);
            StringRefAddr refAddr = new StringRefAddr(paramName, paramValue);
            ref.add(refAddr);
        }
    }


    /**
     * Log the specified message to our current Logger (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        if (!(container instanceof Container)) {
            System.out.println(logName() + ": " + message);
            return;
        }

        Logger logger = ((Container) container).getLogger();
        if (logger != null)
            logger.log(logName() + ": " + message);
        else
            System.out.println(logName() + ": " + message);

    }


    /**
     * Log the specified message and exception to our current Logger
     * (if any).
     *
     * @param message Message to be logged
     * @param throwable Related exception
     */
    protected void log(String message, Throwable throwable) {

        if (!(container instanceof Container)) {
            System.out.println(logName() + ": " + message + ": " + throwable);
            throwable.printStackTrace(System.out);
            return;
        }

        Logger logger = ((Container) container).getLogger();
        if (logger != null)
            logger.log(logName() + ": " + message, throwable);
        else {
            System.out.println(logName() + ": " + message + ": " + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Return the abbreviated name of this container for logging messsages
     */
    protected String logName() {

        String className = this.getClass().getName();
        int period = className.lastIndexOf(".");
        if (period >= 0)
            className = className.substring(period + 1);
        return (className + "[" + getName() + "]");

    }


}
