/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/MBeanFactory.java,v 1.40 2002/09/19 22:55:48 amyroh Exp $
 * $Revision: 1.40 $
 * $Date: 2002/09/19 22:55:48 $
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.core.StandardDefaultContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.logger.FileLogger;
import org.apache.catalina.logger.SystemErrLogger;
import org.apache.catalina.logger.SystemOutLogger;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.realm.JNDIRealm;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.RemoteAddrValve;
import org.apache.catalina.valves.RemoteHostValve;
import org.apache.catalina.valves.RequestDumperValve;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardServer</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.40 $ $Date: 2002/09/19 22:55:48 $
 */

public class MBeanFactory extends BaseModelMBean {

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();

    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = MBeanUtils.createRegistry();


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
    public MBeanFactory()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes




    // ------------------------------------------------------------- Operations

    /**
     * Return the managed bean definition for the specified bean type
     *
     * @param type MBean type
     */
    public String findObjectName(String type) {

        if (type.equals("org.apache.catalina.core.StandardContext")) {
            return "StandardContext";
        } else if (type.equals("org.apache.catalina.core.StandardDefaultContext")) {
            return "DefaultContext";
        } else if (type.equals("org.apache.catalina.core.StandardEngine")) {
            return "Engine";
        } else if (type.equals("org.apache.catalina.core.StandardHost")) {
            return "Host";
        } else {
            return null;
        }

    }

    
    /**
     * Little convenience method to remove redundant code
     * when retrieving the path string
     *
     * @param t path string
     * @return empty string if t==null || t.equals("/")
     */
    private final String getPathStr(String t) {
        if (t == null || t.equals("/")) {
            return "";
        }
        return t;
    }

    
    /**
     * Create a new AccessLoggerValve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createAccessLoggerValve(String parent)
        throws Exception {

        // Create a new AccessLogValve instance
        AccessLogValve accessLogger = new AccessLogValve();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            ((StandardContext)context).addValve(accessLogger);
        } else if (type.equals("Engine")) {
            ((StandardEngine)engine).addValve(accessLogger);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            ((StandardHost)host).addValve(accessLogger);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("AccessLogValve");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), accessLogger);
        return (oname.toString());

    }

    /**
     * Create a new AjpConnector
     *
     * @param parent MBean Name of the associated parent component
     * @param address The IP address on which to bind
     * @param port TCP port number to listen on
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createAjpConnector(String parent, String address, int port)
        throws Exception {

        Object retobj = null;

        try {

            // Create a new CoyoteConnector instance for AJP
            // use reflection to avoid j-t-c compile-time circular dependencies
            Class cls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
            Constructor ct = cls.getConstructor(null);
            retobj = ct.newInstance(null);
            Class partypes1 [] = new Class[1];
            // Set address
            String str = new String();
            partypes1[0] = str.getClass();
            Method meth1 = cls.getMethod("setAddress", partypes1);
            Object arglist1[] = new Object[1];
            arglist1[0] = address;
            meth1.invoke(retobj, arglist1);
            // Set port number
            Class partypes2 [] = new Class[1];
            partypes2[0] = Integer.TYPE;
            Method meth2 = cls.getMethod("setPort", partypes2);
            Object arglist2[] = new Object[1];
            arglist2[0] = new Integer(port);
            meth2.invoke(retobj, arglist2);
            // set protocolHandlerClassName for AJP
            Class partypes3 [] = new Class[1];
            partypes3[0] = str.getClass();
            Method meth3 = cls.getMethod("setProtocolHandlerClassName", partypes3);
            Object arglist3[] = new Object[1];
            arglist3[0] = new String("org.apache.jk.server.JkCoyoteHandler");
            meth3.invoke(retobj, arglist3);

        } catch (Exception e) {
            throw new MBeanException(e);
        }

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("name"));
        service.addConnector((Connector)retobj);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("CoyoteConnector");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), (Connector)retobj);
        return (oname.toString());

    }


    /**
     * Create a new DefaultContext.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createDefaultContext(String parent)
        throws Exception {

        // Create a new StandardDefaultContext instance
        StandardDefaultContext context = new StandardDefaultContext();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        String serviceName = pname.getKeyProperty("service");
        if (serviceName == null) {
            serviceName = pname.getKeyProperty("name");
        }
        Service service = server.findService(serviceName);
        Engine engine = (Engine) service.getContainer();
        String hostName = pname.getKeyProperty("host");
        if (hostName == null) { //if DefaultContext is nested in Engine
            context.setParent(engine);
            engine.addDefaultContext(context);
        } else {                // if DefaultContext is nested in Host
            Host host = (Host) engine.findChild(hostName);
            context.setParent(host);
            host.addDefaultContext(context);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("DefaultContext");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), context);
        return (oname.toString());

    }


    /**
     * Create a new FileLogger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createFileLogger(String parent)
        throws Exception {

        // Create a new FileLogger instance
        FileLogger fileLogger = new FileLogger();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setLogger(fileLogger);
        } else if (type.equals("Engine")) {
            engine.setLogger(fileLogger);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setLogger(fileLogger);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("FileLogger");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), fileLogger);
        return (oname.toString());

    }
    
    
    /**
     * Create a new HttpConnector
     *
     * @param parent MBean Name of the associated parent component
     * @param address The IP address on which to bind
     * @param port TCP port number to listen on
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createHttpConnector(String parent, String address, int port)
        throws Exception {

        Object retobj = null;

        try {

            // Create a new CoyoteConnector instance
            // use reflection to avoid j-t-c compile-time circular dependencies
            Class cls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
            Constructor ct = cls.getConstructor(null);
            retobj = ct.newInstance(null);
            Class partypes1 [] = new Class[1];
            // Set address
            String str = new String();
            partypes1[0] = str.getClass();
            Method meth1 = cls.getMethod("setAddress", partypes1);
            Object arglist1[] = new Object[1];
            arglist1[0] = address;
            meth1.invoke(retobj, arglist1);
            // Set port number
            Class partypes2 [] = new Class[1];
            partypes2[0] = Integer.TYPE;
            Method meth2 = cls.getMethod("setPort", partypes2);
            Object arglist2[] = new Object[1];
            arglist2[0] = new Integer(port);
            meth2.invoke(retobj, arglist2);
        } catch (Exception e) {
            throw new MBeanException(e);
        }

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("name"));
        service.addConnector((Connector)retobj);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("CoyoteConnector");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), (Connector)retobj);
        return (oname.toString());

    }

    
    /**
     * Create a new HttpsConnector
     *
     * @param parent MBean Name of the associated parent component
     * @param address The IP address on which to bind
     * @param port TCP port number to listen on
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createHttpsConnector(String parent, String address, int port)
        throws Exception {

        Object retobj = null;

        try {

            // Create a new CoyoteConnector instance
            // use reflection to avoid j-t-c compile-time circular dependencies
            Class cls = Class.forName("org.apache.coyote.tomcat4.CoyoteConnector");
            Constructor ct = cls.getConstructor(null);
            retobj = ct.newInstance(null);
            Class partypes1 [] = new Class[1];
            // Set address
            String str = new String();
            partypes1[0] = str.getClass();
            Method meth1 = cls.getMethod("setAddress", partypes1);
            Object arglist1[] = new Object[1];
            arglist1[0] = address;
            meth1.invoke(retobj, arglist1);
            // Set port number
            Class partypes2 [] = new Class[1];
            partypes2[0] = Integer.TYPE;
            Method meth2 = cls.getMethod("setPort", partypes2);
            Object arglist2[] = new Object[1];
            arglist2[0] = new Integer(port);
            meth2.invoke(retobj, arglist2);
            // Set scheme
            Class partypes3 [] = new Class[1];
            partypes3[0] = str.getClass();
            Method meth3 = cls.getMethod("setScheme", partypes3);
            Object arglist3[] = new Object[1];
            arglist3[0] = new String("https");
            meth3.invoke(retobj, arglist3);
            // Set secure
            Class partypes4 [] = new Class[1];
            partypes4[0] = Boolean.TYPE;
            Method meth4 = cls.getMethod("setSecure", partypes4);
            Object arglist4[] = new Object[1];
            arglist4[0] = new Boolean(true);
            meth4.invoke(retobj, arglist4);
            // Set factory 
            Class serverSocketFactoryCls = 
                Class.forName("org.apache.catalina.net.ServerSocketFactory");
            Class coyoteServerSocketFactoryCls = 
                Class.forName("org.apache.coyote.tomcat4.CoyoteServerSocketFactory");
            Constructor factoryConst = 
                            coyoteServerSocketFactoryCls.getConstructor(null);
            Object factoryObj = factoryConst.newInstance(null);
            Class partypes5 [] = new Class[1];
            partypes5[0] = serverSocketFactoryCls;
            Method meth5 = cls.getMethod("setFactory", partypes5);
            Object arglist5[] = new Object[1];
            arglist5[0] = factoryObj;
            meth5.invoke(retobj, arglist5);
        } catch (Exception e) {
            throw new MBeanException(e);
        }

        try {
            // Add the new instance to its parent component
            ObjectName pname = new ObjectName(parent);
            Server server = ServerFactory.getServer();
            Service service = server.findService(pname.getKeyProperty("name"));
            service.addConnector((Connector)retobj);
        } catch (Exception e) {
            // FIXME
            // disply error message 
            // the user needs to use keytool to configure SSL first
            // addConnector will fail otherwise
            return null;
        }
        
        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("CoyoteConnector");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), (Connector)retobj);
        return (oname.toString());

    }


    /**
     * Create a new JDBC Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createJDBCRealm(String parent)
        throws Exception {

        // Create a new JDBCRealm instance
        JDBCRealm realm = new JDBCRealm();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setRealm(realm);
        } else if (type.equals("Engine")) {
            engine.setRealm(realm);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setRealm(realm);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("JDBCRealm");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), realm);
        return (oname.toString());

    }


    /**
     * Create a new JNDI Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createJNDIRealm(String parent)
        throws Exception {

         // Create a new JNDIRealm instance
        JNDIRealm realm = new JNDIRealm();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setRealm(realm);
        } else if (type.equals("Engine")) {
            engine.setRealm(realm);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setRealm(realm);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("JNDIRealm");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), realm);
        return (oname.toString());

    }


    /**
     * Create a new Memory Realm.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createMemoryRealm(String parent)
        throws Exception {

         // Create a new MemoryRealm instance
        MemoryRealm realm = new MemoryRealm();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setRealm(realm);
        } else if (type.equals("Engine")) {
            engine.setRealm(realm);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setRealm(realm);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("MemoryRealm");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), realm);
        return (oname.toString());

    }


    /**
     * Create a new Remote Address Filter Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRemoteAddrValve(String parent)
        throws Exception {

        // Create a new RemoteAddrValve instance
        RemoteAddrValve valve = new RemoteAddrValve();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            ((StandardContext)context).addValve(valve);
        } else if (type.equals("Engine")) {
            ((StandardEngine)engine).addValve(valve);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            ((StandardHost)host).addValve(valve);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("RemoteAddrValve");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), valve);
        return (oname.toString());

    }


     /**
     * Create a new Remote Host Filter Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRemoteHostValve(String parent)
        throws Exception {

        // Create a new RemoteHostValve instance
        RemoteHostValve valve = new RemoteHostValve();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            ((StandardContext)context).addValve(valve);
        } else if (type.equals("Engine")) {
            ((StandardEngine)engine).addValve(valve);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            ((StandardHost)host).addValve(valve);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("RemoteHostValve");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), valve);
        return (oname.toString());

    }


    /**
     * Create a new Request Dumper Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createRequestDumperValve(String parent)
        throws Exception {

        // Create a new RequestDumperValve instance
        RequestDumperValve valve = new RequestDumperValve();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            ((StandardContext)context).addValve(valve);
        } else if (type.equals("Engine")) {
            ((StandardEngine)engine).addValve(valve);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            ((StandardHost)host).addValve(valve);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("RequestDumperValve");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), valve);
        return (oname.toString());

    }


    /**
     * Create a new Single Sign On Valve.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSingleSignOn(String parent)
        throws Exception {

        // Create a new SingleSignOn instance
        SingleSignOn valve = new SingleSignOn();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            ((StandardContext)context).addValve(valve);
        } else if (type.equals("Engine")) {
            ((StandardEngine)engine).addValve(valve);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            ((StandardHost)host).addValve(valve);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("SingleSignOn");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), valve);
        return (oname.toString());

    }


   /**
     * Create a new StandardContext.
     *
     * @param parent MBean Name of the associated parent component
     * @param path The context path for this Context
     * @param docBase Document base directory (or WAR) for this Context
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardContext(String parent, String path,
                                        String docBase)
        throws Exception {
        
        // Create a new StandardContext instance
        StandardContext context = new StandardContext();    
        path = getPathStr(path);
        context.setPath(path);
        context.setDocBase(docBase);
        ContextConfig contextConfig = new ContextConfig();
        context.addLifecycleListener(contextConfig);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        Host host = (Host) engine.findChild(pname.getKeyProperty("host"));

        // Add context to the host
        host.addChild(context);
        
        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardContext");

        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), context);
        return (oname.toString());

    }


   /**
     * Create a new StandardEngine.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this Engine
     * @param defaultHost Default hostname of this Engine
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardEngine(String parent, String name,
                                       String defaultHost)
        throws Exception {

        // Create a new StandardEngine instance
        StandardEngine engine = new StandardEngine();
        engine.setName(name);
        engine.setDefaultHost(defaultHost);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("name"));
        service.setContainer(engine);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardEngine");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), engine);
        return (oname.toString());

    }


    /**
     * Create a new StandardHost.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this Host
     * @param appBase Application base directory name
     * @param autoDeploy Should we auto deploy?
     * @param deployXML Should we deploy Context XML config files property?
     * @param liveDeploy Should we live deploy?
     * @param unpackWARs Should we unpack WARs when auto deploying?
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardHost(String parent, String name,
                                     String appBase, boolean autoDeploy,
                                     boolean deployXML, boolean liveDeploy,
                                     boolean unpackWARs)
        throws Exception {

        // Create a new StandardHost instance
        StandardHost host = new StandardHost();
        host.setName(name);
        host.setAppBase(appBase);
        host.setAutoDeploy(autoDeploy);
        host.setDeployXML(deployXML);
        host.setLiveDeploy(liveDeploy);
        host.setUnpackWARs(unpackWARs);

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        engine.addChild(host);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardHost");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), host);
        return (oname.toString());

    }


    /**
     * Create a new StandardManager.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardManager(String parent)
        throws Exception {

        // Create a new StandardManager instance
        StandardManager manager = new StandardManager();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        String type = pname.getKeyProperty("type");
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if ((type != null) && (type.equals("Context"))) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setManager(manager);
        } else if ((type != null) && (type.equals("DefaultContext"))) {
            String hostName = pname.getKeyProperty("host");
            DefaultContext defaultContext = null;
            if (hostName == null) {
                defaultContext = engine.getDefaultContext();
            } else {
                Host host = (Host)engine.findChild(hostName);
                defaultContext = host.getDefaultContext();
            }
            if (defaultContext != null ){
                manager.setDefaultContext(defaultContext);
                defaultContext.setManager(manager);
            }
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardManager");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), manager);
        return (oname.toString());

    }


    /**
     * Create a new StandardService.
     *
     * @param parent MBean Name of the associated parent component
     * @param name Unique name of this StandardService
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createStandardService(String parent, String name)
        throws Exception {

        // Create a new StandardService instance
        StandardService service = new StandardService();
        service.setName(name);

        // Add the new instance to its parent component
        Server server = ServerFactory.getServer();
        server.addService(service);

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("StandardService");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), service);
        return (oname.toString());

    }



    /**
     * Create a new System Error Logger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSystemErrLogger(String parent)
        throws Exception {

        // Create a new SystemErrLogger instance
        SystemErrLogger logger = new SystemErrLogger();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {        
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setLogger(logger);
        } else if (type.equals("Engine")) {
            engine.setLogger(logger);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setLogger(logger);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("SystemErrLogger");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), logger);
        return (oname.toString());

    }


    /**
     * Create a new System Output Logger.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createSystemOutLogger(String parent)
        throws Exception {

        // Create a new SystemOutLogger instance
        SystemOutLogger logger = new SystemOutLogger();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setLogger(logger);
        } else if (type.equals("Engine")) {
            engine.setLogger(logger);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setLogger(logger);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("SystemOutLogger");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), logger);
        return (oname.toString());
    }

    
    /**
     * Create a new  UserDatabaseRealm.
     *
     * @param parent MBean Name of the associated parent component
     * @param resourceName Global JNDI resource name of the associated
     *  UserDatabase
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createUserDatabaseRealm(String parent, String resourceName)
        throws Exception {

         // Create a new UserDatabaseRealm instance
        UserDatabaseRealm realm = new UserDatabaseRealm();
        realm.setResourceName(resourceName);
        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        String type = pname.getKeyProperty("type");
        Server server = ServerFactory.getServer();
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if (type.equals("Context")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setRealm(realm);
        } else if (type.equals("Engine")) {
            engine.setRealm(realm);
        } else if (type.equals("Host")) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            host.setRealm(realm);
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("UserDatabaseRealm");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), realm);
        return (oname.toString());
        
    }

    
    /**
     * Create a new Web Application Loader.
     *
     * @param parent MBean Name of the associated parent component
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String createWebappLoader(String parent)
        throws Exception {

        // Create a new WebappLoader instance
        WebappLoader loader = new WebappLoader();

        // Add the new instance to its parent component
        ObjectName pname = new ObjectName(parent);
        Server server = ServerFactory.getServer();
        String type = pname.getKeyProperty("type");
        Service service = server.findService(pname.getKeyProperty("service"));
        Engine engine = (Engine) service.getContainer();
        if ((type != null) && (type.equals("Context"))) {
            Host host = (Host) engine.findChild(pname.getKeyProperty("host"));
            String pathStr = getPathStr(pname.getKeyProperty("path"));
            Context context = (Context) host.findChild(pathStr);
            context.setLoader(loader);
        } else if ((type != null) && (type.equals("DefaultContext"))) {
            String hostName = pname.getKeyProperty("host");
            DefaultContext defaultContext = null;
            if (hostName == null) {
                defaultContext = engine.getDefaultContext();
            } else {
                Host host = (Host)engine.findChild(hostName);
                defaultContext = host.getDefaultContext();
            }
            if (defaultContext != null ){
                loader.setDefaultContext(defaultContext);
                defaultContext.setLoader(loader);
            }
        }

        // Return the corresponding MBean name
        ManagedBean managed = registry.findManagedBean("WebappLoader");
        ObjectName oname =
            MBeanUtils.createObjectName(managed.getDomain(), loader);
        return (oname.toString());

    }


    /**
     * Remove an existing Connector.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @param serviceName Service name of the connector to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeConnector(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        Server server = ServerFactory.getServer();
        String serviceName = oname.getKeyProperty("service");
        Service service = server.findService(serviceName);
        String port = oname.getKeyProperty("port");
        String address = oname.getKeyProperty("address");
        
        Connector conns[] = (Connector[]) service.findConnectors();

        for (int i = 0; i < conns.length; i++) {
            Class cls = conns[i].getClass();
            Method getAddrMeth = cls.getMethod("getAddress", null);
            Object addrObj = getAddrMeth.invoke(conns[i], null);
            String connAddress = null;
            if (addrObj != null) {
                connAddress = addrObj.toString();
            } 
            Method getPortMeth = cls.getMethod("getPort", null);
            Object portObj = getPortMeth.invoke(conns[i], null);
            String connPort = new String();
            if (portObj != null) {
                connPort = portObj.toString();
            }
            if (((address.equals("null")) && (connAddress==null)) && port.equals(connPort)) {
                service.removeConnector(conns[i]);
                break;
            } else if (address.equals(connAddress) && port.equals(connPort)) {
                // Remove this component from its parent component
                service.removeConnector(conns[i]);
                break;
            } 
        }

    }


    /**
     * Remove an existing Context.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeContext(String name) throws Exception {
        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("service");
        String hostName = oname.getKeyProperty("host");
        String contextName = getPathStr(oname.getKeyProperty("path"));
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        Engine engine = (Engine) service.getContainer();
        Host host = (Host) engine.findChild(hostName);
        Context context = (Context) host.findChild(contextName);

        // Remove this component from its parent component
        host.removeChild(context);

    }


    /**
     * Remove an existing Host.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeHost(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("service");
        String hostName = oname.getKeyProperty("host");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        Engine engine = (Engine) service.getContainer();
        Host host = (Host) engine.findChild(hostName);

        // Remove this component from its parent component
        engine.removeChild(host);

    }


    /**
     * Remove an existing Logger.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeLogger(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("service");
        String hostName = oname.getKeyProperty("host");
        
        String path = oname.getKeyProperty("path");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        StandardEngine engine = (StandardEngine) service.getContainer();
        if (hostName == null) {             // if logger's container is Engine
            Logger logger = engine.getLogger();
            Container container = logger.getContainer();
            if (container instanceof StandardEngine) {
                String sname =
                    ((StandardEngine)container).getService().getName();
                if (sname.equals(serviceName)) {
                    engine.setLogger(null);
                }
            }
        } else if (path == null) {      // if logger's container is Host
            StandardHost host = (StandardHost) engine.findChild(hostName);
            Logger logger = host.getLogger();
            Container container = logger.getContainer();
            if (container instanceof StandardHost) {
                String hn = ((StandardHost)container).getName();
                StandardEngine se =
                    (StandardEngine) ((StandardHost)container).getParent();
                String sname = se.getService().getName();
                if (sname.equals(serviceName) && hn.equals(hostName)) {
                    host.setLogger(null);
                }
            }
        } else {                // logger's container is Context
            StandardHost host = (StandardHost) engine.findChild(hostName);
            path = getPathStr(path);
            StandardContext context = (StandardContext) host.findChild(path);
            Logger logger = context.getLogger();
            Container container = logger.getContainer();
            if (container instanceof StandardContext) {
                String pathName = ((StandardContext)container).getName();
                StandardHost sh =
                    (StandardHost)((StandardContext)container).getParent();
                String hn = sh.getName();;
                StandardEngine se = (StandardEngine)sh.getParent();
                String sname = se.getService().getName();
                if ((sname.equals(serviceName) && hn.equals(hostName)) &&
                        pathName.equals(path)) {
                    context.setLogger(null);
                }
            }
        }
    }


    /**
     * Remove an existing Loader.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeLoader(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String type = oname.getKeyProperty("type");
        String serviceName = oname.getKeyProperty("service");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        Engine engine = (Engine) service.getContainer();  
        String hostName = oname.getKeyProperty("host");
        if ((type != null) && (type.equals("Loader"))) {      
            String contextName = getPathStr(oname.getKeyProperty("path"));
            Host host = (Host) engine.findChild(hostName);
            Context context = (Context) host.findChild(contextName);
            // Remove this component from its parent component
            context.setLoader(null);
        } else if ((type != null) && (type.equals("DefaultLoader"))) {
            DefaultContext defaultContext = null;
            if (hostName == null) {    
                defaultContext = engine.getDefaultContext();
            } else {
                Host host = (Host) engine.findChild(hostName);
                defaultContext = host.getDefaultContext();
            }
            if (defaultContext != null) {
                // Remove this component from its parent component
                defaultContext.setLoader(null);
            }
        }
    
    }


    /**
     * Remove an existing Manager.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeManager(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String type = oname.getKeyProperty("type");
        String serviceName = oname.getKeyProperty("service");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        Engine engine = (Engine) service.getContainer();  
        String hostName = oname.getKeyProperty("host");
        if ((type != null) && (type.equals("Manager"))) {      
            String contextName = getPathStr(oname.getKeyProperty("path"));
            Host host = (Host) engine.findChild(hostName);
            Context context = (Context) host.findChild(contextName);
            // Remove this component from its parent component
            context.setManager(null);
        } else if ((type != null) && (type.equals("DefaultManager"))) {
            DefaultContext defaultContext = null;
            if (hostName == null) {    
                defaultContext = engine.getDefaultContext();
            } else {
                Host host = (Host) engine.findChild(hostName);
                defaultContext = host.getDefaultContext();
            }
            if (defaultContext != null) {
                // Remove this component from its parent component
                defaultContext.setManager(null);
            }
        }

    }

    
    /**
     * Remove an existing Realm.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeRealm(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("service");
        String hostName = oname.getKeyProperty("host");
        String path = oname.getKeyProperty("path");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        StandardEngine engine = (StandardEngine) service.getContainer();
        if (hostName == null) {             // if realm's container is Engine
            Realm realm = engine.getRealm();
            Container container = realm.getContainer();
            if (container instanceof StandardEngine) {
                String sname =
                    ((StandardEngine)container).getService().getName();
                if (sname.equals(serviceName)) {
                    engine.setRealm(null);
                }
            }
        } else if (path == null) {      // if realm's container is Host
            StandardHost host = (StandardHost) engine.findChild(hostName);
            Realm realm = host.getRealm();
            Container container = realm.getContainer();
            if (container instanceof StandardHost) {
                String hn = ((StandardHost)container).getName();
                StandardEngine se =
                    (StandardEngine) ((StandardHost)container).getParent();
                String sname = se.getService().getName();
                if (sname.equals(serviceName) && hn.equals(hostName)) {
                    host.setRealm(null);
                }
            }
        } else {                // realm's container is Context
            StandardHost host = (StandardHost) engine.findChild(hostName);
            path = getPathStr(path);
            StandardContext context = (StandardContext) host.findChild(path);
            Realm realm = context.getRealm();
            Container container = realm.getContainer();
            if (container instanceof StandardContext) {
                String pathName = ((StandardContext)container).getName();
                StandardHost sh =
                    (StandardHost)((StandardContext)container).getParent();
                String hn = sh.getName();;
                StandardEngine se = (StandardEngine)sh.getParent();
                String sname = se.getService().getName();
                if ((sname.equals(serviceName) && hn.equals(hostName)) &&
                        pathName.equals(path)) {
                    context.setRealm(null);
                }
            }
        }
    }

    
    /**
     * Remove an existing Service.
     *
     * @param name MBean Name of the component to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeService(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("name");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);

        // Remove this component from its parent component
        server.removeService(service);

    }


    /**
     * Remove an existing Valve.
     *
     * @param name MBean Name of the comonent to remove
     *
     * @exception Exception if a component cannot be removed
     */
    public void removeValve(String name) throws Exception {

        // Acquire a reference to the component to be removed
        ObjectName oname = new ObjectName(name);
        String serviceName = oname.getKeyProperty("service");
        String hostName = oname.getKeyProperty("host");
        String path = oname.getKeyProperty("path");
        String sequence = oname.getKeyProperty("sequence");
        Server server = ServerFactory.getServer();
        Service service = server.findService(serviceName);
        StandardEngine engine = (StandardEngine) service.getContainer();
        if (hostName == null) {             // if valve's container is Engine
            Valve [] valves = engine.getValves();
            for (int i = 0; i < valves.length; i++) {
                Container container = ((ValveBase)valves[i]).getContainer();
                if (container instanceof StandardEngine) {
                    String sname =
                        ((StandardEngine)container).getService().getName();
                    Integer sequenceInt = new Integer(valves[i].hashCode());
                    if (sname.equals(serviceName) &&
                        sequence.equals(sequenceInt.toString())){
                        engine.removeValve(valves[i]);
                        break;
                    }
                }
            }
        } else if (path == null) {      // if valve's container is Host
            StandardHost host = (StandardHost) engine.findChild(hostName);
            Valve [] valves = host.getValves();
            for (int i = 0; i < valves.length; i++) {
                Container container = ((ValveBase)valves[i]).getContainer();
                if (container instanceof StandardHost) {
                    String hn = ((StandardHost)container).getName();
                    StandardEngine se =
                        (StandardEngine) ((StandardHost)container).getParent();
                    String sname = se.getService().getName();
                    Integer sequenceInt = new Integer(valves[i].hashCode());
                    if ((sname.equals(serviceName) && hn.equals(hostName)) &&
                        sequence.equals(sequenceInt.toString())){
                        host.removeValve(valves[i]);
                        break;
                    }
                }
            }
        } else {                // valve's container is Context
            StandardHost host = (StandardHost) engine.findChild(hostName);
            path = getPathStr(path);
            StandardContext context = (StandardContext) host.findChild(path);
            Valve [] valves = context.getValves();
            for (int i = 0; i < valves.length; i++) {
                Container container = ((ValveBase)valves[i]).getContainer();
                if (container instanceof StandardContext) {
                    String pathName = ((StandardContext)container).getName();
                    StandardHost sh =
                        (StandardHost)((StandardContext)container).getParent();
                    String hn = sh.getName();;
                    StandardEngine se = (StandardEngine)sh.getParent();
                    String sname = se.getService().getName();
                    Integer sequenceInt = new Integer(valves[i].hashCode());
                    if (((sname.equals(serviceName) && hn.equals(hostName)) &&
                        pathName.equals(path)) &&
                        sequence.equals(sequenceInt.toString())){
                        context.removeValve(valves[i]);
                        break;
                    }
                }
            }
        }
    }

}
