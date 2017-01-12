/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Deployer.java,v 1.6 2002/04/09 23:48:21 craigmcc Exp $
 * $Revision: 1.6 $
 * $Date: 2002/04/09 23:48:21 $
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


package org.apache.catalina;


import java.io.IOException;
import java.net.URL;


/**
 * A <b>Deployer</b> is a specialized Container into which web applications
 * can be deployed and undeployed.  Such a Container will create and install
 * child Context instances for each deployed application.  The unique key
 * for each web application will be the context path to which it is attached.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2002/04/09 23:48:21 $
 */

/* public interface Deployer extends Container { */
public interface Deployer  {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The ContainerEvent event type sent when a new application is
     * being installed by <code>install()</code>, before it has been
     * started.
     */
    public static final String PRE_INSTALL_EVENT = "pre-install";


    /**
     * The ContainerEvent event type sent when a new application is
     * installed by <code>install()</code>, after it has been started.
     */
    public static final String INSTALL_EVENT = "install";


    /**
     * The ContainerEvent event type sent when an existing application is
     * removed by <code>remove()</code>.
     */
    public static final String REMOVE_EVENT = "remove";


    // --------------------------------------------------------- Public Methods


    /**
     * Return the name of the Container with which this Deployer is associated.
     */
    public String getName();


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
     *  during installation
     */
    public void install(String contextPath, URL war) throws IOException;


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
    public void install(URL config, URL war) throws IOException;


    /**
     * Return the Context for the deployed application that is associated
     * with the specified context path (if any); otherwise return
     * <code>null</code>.
     *
     * @param contextPath The context path of the requested web application
     */
    public Context findDeployedApp(String contextPath);


    /**
     * Return the context paths of all deployed web applications in this
     * Container.  If there are no deployed applications, a zero-length
     * array is returned.
     */
    public String[] findDeployedApps();


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
    public void remove(String contextPath) throws IOException;


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
    public void start(String contextPath) throws IOException;


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
    public void stop(String contextPath) throws IOException;


}
