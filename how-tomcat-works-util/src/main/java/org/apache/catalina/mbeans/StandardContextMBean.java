/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/StandardContextMBean.java,v 1.6 2002/06/06 22:36:47 amyroh Exp $
 * $Revision: 1.6 $
 * $Date: 2002/06/06 22:36:47 $
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

package org.apache.catalina.mbeans;

import java.net.URLDecoder;
import java.util.ArrayList;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;

/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardContext</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.6 $ $Date: 2002/06/06 22:36:47 $
 */

public class StandardContextMBean extends BaseModelMBean {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public StandardContextMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }
    

    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * The configuration information registry for our managed beans.
     */
    protected Registry registry = MBeanUtils.createRegistry();

    /**
     * The <code>ManagedBean</code> information describing this MBean.
     */
    protected ManagedBean managed =
        registry.findManagedBean("StandardContext");

    
    // ------------------------------------------------------------- Attributes

    
    /**
     * Return the naming resources associated with this web application.
     */
    private NamingResources getNamingResources() {
        
        return ((StandardContext)this.resource).getNamingResources();
    
    }
    
    
    /**
     * Return the MBean Names of the set of defined environment entries for  
     * this web application
     */
    public String[] getEnvironments() {
        ContextEnvironment[] envs = getNamingResources().findEnvironments();
        ArrayList results = new ArrayList();
        for (int i = 0; i < envs.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(managed.getDomain(), envs[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException
                    ("Cannot create object name for environment " + envs[i]);
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }
    
    
    /**
     * Return the MBean Names of all the defined resource references for this
     * application.
     */
    public String[] getResources() {
        
        ContextResource[] resources = getNamingResources().findResources();
        ArrayList results = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(managed.getDomain(), resources[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException
                    ("Cannot create object name for resource " + resources[i]);
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }

      
    /**
     * Return the MBean Names of all the defined resource links for this 
     * application
     */
    public String[] getResourceLinks() {
        
        ContextResourceLink[] links = getNamingResources().findResourceLinks();
        ArrayList results = new ArrayList();
        for (int i = 0; i < links.length; i++) {
            try {
                ObjectName oname =
                    MBeanUtils.createObjectName(managed.getDomain(), links[i]);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException
                    ("Cannot create object name for resource " + links[i]);
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }

    // ------------------------------------------------------------- Operations


    /**
     * Add an environment entry for this web application.
     *
     * @param envName New environment entry name
     */
    public String addEnvironment(String envName, String type) 
        throws MalformedObjectNameException {

        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextEnvironment env = nresources.findEnvironment(envName);
        if (env != null) {
            throw new IllegalArgumentException
                ("Invalid environment name - already exists '" + envName + "'");
        }
        env = new ContextEnvironment();
        env.setName(envName);
        env.setType(type);
        nresources.addEnvironment(env);
        
        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("ContextEnvironment");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), env);
        return (oname.toString());
        
    }

    
    /**
     * Add a resource reference for this web application.
     *
     * @param resourceName New resource reference name
     */
    public String addResource(String resourceName, String type) 
        throws MalformedObjectNameException {
        
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextResource resource = nresources.findResource(resourceName);
        if (resource != null) {
            throw new IllegalArgumentException
                ("Invalid resource name - already exists'" + resourceName + "'");
        }
        resource = new ContextResource();
        resource.setName(resourceName);
        resource.setType(type);
        nresources.addResource(resource);
        
        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("ContextResource");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), resource);
        return (oname.toString());
    }

    
    /**
     * Add a resource link for this web application.
     *
     * @param resourceLinkName New resource link name
     */
    public String addResourceLink(String resourceLinkName, String global, 
                String name, String type) throws MalformedObjectNameException {
        
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextResourceLink resourceLink = 
                                nresources.findResourceLink(resourceLinkName);
        if (resourceLink != null) {
            throw new IllegalArgumentException
                ("Invalid resource link name - already exists'" + 
                                                        resourceLinkName + "'");
        }
        resourceLink = new ContextResourceLink();
        resourceLink.setGlobal(global);
        resourceLink.setName(resourceLinkName);
        resourceLink.setType(type);
        nresources.addResourceLink(resourceLink);
        
        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("ContextResourceLink");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), resourceLink);
        return (oname.toString());
    }    
    
    
    /**
     * Remove any environment entry with the specified name.
     *
     * @param name Name of the environment entry to remove
     */
    public void removeEnvironment(String envName) {

        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return;
        }
        ContextEnvironment env = nresources.findEnvironment(envName);
        if (env == null) {
            throw new IllegalArgumentException
                ("Invalid environment name '" + envName + "'");
        }
        nresources.removeEnvironment(envName);

    }
    
    
    /**
     * Remove any resource reference with the specified name.
     *
     * @param resourceName Name of the resource reference to remove
     */
    public void removeResource(String resourceName) {

        resourceName = URLDecoder.decode(resourceName);
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return;
        }
        ContextResource resource = nresources.findResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException
                ("Invalid resource name '" + resourceName + "'");
        }
        nresources.removeResource(resourceName);
    }
    
    
    /**
     * Remove any resource link with the specified name.
     *
     * @param resourceName Name of the resource reference to remove
     */
    public void removeResourceLink(String resourceLinkName) {

        resourceLinkName = URLDecoder.decode(resourceLinkName);
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return;
        }
        ContextResourceLink resource = nresources.findResourceLink(resourceLinkName);
        if (resource == null) {
            throw new IllegalArgumentException
                ("Invalid resource name '" + resourceLinkName + "'");
        }
        nresources.removeResourceLink(resourceLinkName);
    }
 
    
}
