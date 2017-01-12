/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/ContextResourceMBean.java,v 1.8 2002/06/24 21:11:42 amyroh Exp $
 * $Revision: 1.8 $
 * $Date: 2002/06/24 21:11:42 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.deploy.ContextResource</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.8 $ $Date: 2002/06/24 21:11:42 $
 */

public class ContextResourceMBean extends BaseModelMBean {


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
    public ContextResourceMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Attributes


    /**
     * Obtain and return the value of a specific attribute of this MBean.
     *
     * @param name Name of the requested attribute
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public Object getAttribute(String name)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {
 
        // Validate the input parameters
        if (name == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute name is null"),
                 "Attribute name is null");

        ContextResource cr = null;
        try {
            cr = (ContextResource) getManagedResource();
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        } catch (InvalidTargetObjectTypeException e) {
             throw new MBeanException(e);
        }
        
        String value = null;
        if ("auth".equals(name)) {
            return (cr.getAuth());
        } else if ("description".equals(name)) {
            return (cr.getDescription());
        } else if ("name".equals(name)) {
            return (cr.getName());              
        } else if ("scope".equals(name)) {
            return (cr.getScope());  
        } else if ("type".equals(name)) {
            return (cr.getType());
        } else {
            NamingResources nr = cr.getNamingResources(); 
            if (nr == null) {
                throw new AttributeNotFoundException
                    ("Cannot find naming resource "+cr.getName());
            }
            ResourceParams rp = nr.findResourceParams(cr.getName());
            if (rp == null) {
                throw new AttributeNotFoundException
                    ("Cannot find resource param "+cr.getName());
            }
            value = (String) rp.getParameters().get(name);
            if (value == null) {
                throw new AttributeNotFoundException
                    ("Cannot find attribute "+name+rp);
            }
        }
        
        return value;
        
    }

    
    /**
     * Set the value of a specific attribute of this MBean.
     *
     * @param attribute The identification of the attribute to be set
     *  and the new value
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
     public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {

        // Validate the input parameters
        if (attribute == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute is null"),
                 "Attribute is null");
        String name = attribute.getName();
        Object value = attribute.getValue();
        if (name == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute name is null"),
                 "Attribute name is null"); 
        
        ContextResource cr = null;
        try {
            cr = (ContextResource) getManagedResource();
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        } catch (InvalidTargetObjectTypeException e) {
             throw new MBeanException(e);
        }
        
        if ("auth".equals(name)) {
            cr.setAuth((String)value);
        } else if ("description".equals(name)) {
            cr.setDescription((String)value);
        } else if ("name".equals(name)) {
            cr.setName((String)value);              
        } else if ("scope".equals(name)) {
            cr.setScope((String)value);  
        } else if ("type".equals(name)) {
            cr.setType((String)value);
        } else {
            ResourceParams rp = 
                cr.getNamingResources().findResourceParams(cr.getName());
            if (rp != null) {
                String valueStr = ""+value;
                rp.addParameter(name, valueStr);
                cr.getNamingResources().removeResourceParams(cr.getName());
            } else {
                rp = new ResourceParams();
                rp.setName(cr.getName());
                String valueStr = ""+value;
                rp.addParameter(name, valueStr);
            }
            cr.getNamingResources().addResourceParams(rp);
        }
        
        // cannot use side-efects.  It's removed and added back each time 
        // there is a modification in a resource.
        NamingResources nr = cr.getNamingResources();
        nr.removeResource(cr.getName());
        nr.addResource(cr);
    }
    
}
