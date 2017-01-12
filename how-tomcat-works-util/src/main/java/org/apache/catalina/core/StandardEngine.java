/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardEngine.java,v 1.15 2002/05/02 22:14:45 craigmcc Exp $
 * $Revision: 1.15 $
 * $Date: 2002/05/02 22:14:45 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.util.ServerInfo;

/**
 * Standard implementation of the <b>Engine</b> interface.  Each
 * child container must be a Host implementation to process the specific
 * fully qualified host name of that virtual host.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.15 $ $Date: 2002/05/02 22:14:45 $
 */

public class StandardEngine
    extends ContainerBase
    implements Engine {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardEngine component with the default basic Valve.
     */
    public StandardEngine() {

        super();
        pipeline.setBasic(new StandardEngineValve());

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Host name to use when no server host, or an unknown host,
     * is specified in the request.
     */
    private String defaultHost = null;


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardEngine/1.0";


    /**
     * The Java class name of the default Mapper class for this Container.
     */
    private String mapperClass =
        "org.apache.catalina.core.StandardEngineMapper";


    /**
     * The <code>Service</code> that owns this Engine, if any.
     */
    private Service service = null;


    /**
     * DefaultContext config
     */
    private DefaultContext defaultContext;


    /**
     * The JVM Route ID for this Tomcat instance. All Route ID's must be unique
     * across the cluster.
     */
    private String jvmRouteId;


    // ------------------------------------------------------------- Properties


    /**
     * Return the default host.
     */
    public String getDefaultHost() {

        return (defaultHost);

    }


    /**
     * Set the default host.
     *
     * @param host The new default host
     */
    public void setDefaultHost(String host) {

        String oldDefaultHost = this.defaultHost;
        if (host == null) {
            this.defaultHost = null;
        } else {
            this.defaultHost = host.toLowerCase();
        }
        support.firePropertyChange("defaultHost", oldDefaultHost,
                                   this.defaultHost);

    }


    /**
     * Set the cluster-wide unique identifier for this Engine.
     * This value is only useful in a load-balancing scenario.
     * <p>
     * This property should not be changed once it is set.
     */
    public void setJvmRoute(String routeId) {
        this.log("setJvmRoute=" + routeId);
        jvmRouteId = routeId;
    }


    /**
     * Retrieve the cluster-wide unique identifier for this Engine.
     * This value is only useful in a load-balancing scenario.
     */
    public String getJvmRoute() {
        return jvmRouteId;
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
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {

        this.service = service;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Import the DefaultContext config into a web application context.
     *
     * @param context web application context to import default context
     */
    public void importDefaultContext(Context context) {

        if ( this.defaultContext != null )
            this.defaultContext.importDefaultContext(context);

    }


    /**
     * Add a child Container, only if the proposed child is an implementation
     * of Host.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

        if (!(child instanceof Host))
            throw new IllegalArgumentException
                (sm.getString("standardEngine.notHost"));
        super.addChild(child);

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
     * Disallow any attempt to set a parent for this Container, since an
     * Engine is supposed to be at the top of the Container hierarchy.
     *
     * @param container Proposed parent Container
     */
    public void setParent(Container container) {

        throw new IllegalArgumentException
            (sm.getString("standardEngine.notParent"));

    }


    /**
     * Start this Engine component.
     *
     * @exception LifecycleException if a startup error occurs
     */
    public void start() throws LifecycleException {

        // Log our server identification information
        System.out.println(ServerInfo.getServerInfo());

        // Standard container startup
        super.start();

    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("StandardEngine[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Add a default Mapper implementation if none have been configured
     * explicitly.
     *
     * @param mapperClass The default mapper class name to add
     */
    protected void addDefaultMapper(String mapperClass) {

        super.addDefaultMapper(this.mapperClass);

    }


}
