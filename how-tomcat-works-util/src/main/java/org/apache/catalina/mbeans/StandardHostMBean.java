/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/StandardHostMBean.java,v 1.5 2002/03/08 22:01:08 amyroh Exp $
 * $Revision: 1.5 $
 * $Date: 2002/03/08 22:01:08 $
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


import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.Registry;
import org.apache.commons.modeler.ManagedBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardHost</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.5 $ $Date: 2002/03/08 22:01:08 $
 */

public class StandardHostMBean extends BaseModelMBean {

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();

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
    public StandardHostMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes



    // ------------------------------------------------------------- Operations


   /**
     * Add an alias name that should be mapped to this Host
     *
     * @param alias The alias to be added
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void addAlias(String alias)
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        host.addAlias(alias);

    }


   /**
     * Return the set of alias names for this Host
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String [] findAliases()
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        return host.findAliases();

    }


   /**
     * Return the MBean Names of the Valves assoicated with this Host
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String [] getValves()
        throws Exception {

        Registry registry = MBeanUtils.createRegistry();
        StandardHost host = (StandardHost) this.resource;
        String mname = MBeanUtils.createManagedName(host);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = null;
        if (managed != null) {
            domain = managed.getDomain();
        }
        if (domain == null)
            domain = mserver.getDefaultDomain();
        Valve [] valves = host.getValves();
        String [] mbeanNames = new String[valves.length];
        for (int i = 0; i < valves.length; i++) {
            mbeanNames[i] =
                MBeanUtils.createObjectName(domain, valves[i]).toString();
        }

        return mbeanNames;

    }


   /**
     * Return the specified alias name from the aliases for this Host
     *
     * @param alias Alias name to be removed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void removeAlias(String alias)
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        host.removeAlias(alias);

    }


}
