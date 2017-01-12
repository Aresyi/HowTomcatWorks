/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/ConnectorMBean.java,v 1.4 2002/05/02 02:03:15 amyroh Exp $
 * $Revision: 1.4 $
 * $Date: 2002/05/02 02:03:15 $
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

import java.lang.reflect.Method;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.coyote.tomcat4.CoyoteConnector</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.4 $ $Date: 2002/05/02 02:03:15 $
 */

public class ConnectorMBean extends ClassNameMBean {


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
    public ConnectorMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes



    // ------------------------------------------------------------- Operations

    
    /**
     * Return Client authentication info
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public boolean getClientAuth()
        throws Exception {
            
        Object clientAuthObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get clientAuth
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getClientAuth", null);
                clientAuthObj = meth2.invoke(factory, null);
            }
           
        }    
        if (clientAuthObj instanceof Boolean) {
            return ((Boolean)clientAuthObj).booleanValue();
        } else return false;
        
    }
    
    
    /**
     * Set Client authentication info
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setClientAuth(boolean clientAuth)
        throws Exception {
            
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set clientAuth
                Class partypes2 [] = new Class[1];
                partypes2[0] = Boolean.TYPE;
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setClientAuth", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = new Boolean(clientAuth);
                meth2.invoke(factory, arglist2);
            } 
        } 
        
    }

    
    /**
     * Return keystoreFile
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String getKeystoreFile()
        throws Exception {
            
        Object keystoreFileObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get keystoreFile
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get keystoreFile
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getKeystoreFile", null);
                keystoreFileObj = meth2.invoke(factory, null);
            } 
        }    
        
        if (keystoreFileObj == null) {
            return null;
        } else {
            return keystoreFileObj.toString();
        }
        
    }
    
    
    /**
     * Set keystoreFile
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setKeystoreFile(String keystoreFile)
        throws Exception {
        
        if (keystoreFile == null) {
            keystoreFile = "";
        }
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set keystoreFile
                Class partypes2 [] = new Class[1];
                String str = new String();
                partypes2[0] = str.getClass();
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setKeystoreFile", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = keystoreFile;
                meth2.invoke(factory, arglist2);
            }
           
        }    
    }
    
    
    /**
     * Return keystorePass
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String getKeystorePass()
        throws Exception {
            
        Object keystorePassObj = null;
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // get keystorePass
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("getKeystorePass", null);
                keystorePassObj = meth2.invoke(factory, null);
            }
           
        }    
        
        if (keystorePassObj == null) {
            return null;
        } else {
            return keystorePassObj.toString();
        } 
        
    }
    
    
    /**
     * Set keystorePass
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void setKeystorePass(String keystorePass)
        throws Exception {
            
        if (keystorePass == null) {
            keystorePass = "";
        }
        Class coyoteConnectorCls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
        if (coyoteConnectorCls.isInstance(this.resource)) {
            // get factory
            Method meth1 = coyoteConnectorCls.getMethod("getFactory", null);
            Object factory = meth1.invoke(this.resource, null);
            Class coyoteServerSocketFactoryCls = Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            if (coyoteServerSocketFactoryCls.isInstance(factory)) {
                // set keystorePass
                Class partypes2 [] = new Class[1];
                String str = new String();
                partypes2[0] = str.getClass();
                Method meth2 = coyoteServerSocketFactoryCls.getMethod("setKeystorePass", partypes2);
                Object arglist2[] = new Object[1];
                arglist2[0] = keystorePass;
                meth2.invoke(factory, arglist2);
            }
        }    
    }
    
    
}
