/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/UserConfig.java,v 1.3 2001/09/05 00:31:50 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2001/09/05 00:31:50 $
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


package org.apache.catalina.startup;


import java.io.File;
import java.util.Enumeration;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.util.StringManager;


/**
 * Startup event listener for a <b>Host</b> that configures Contexts (web
 * applications) for all defined "users" who have a web application in a
 * directory with the specified name in their home directories.  The context
 * path of each deployed application will be set to <code>~xxxxx</code>, where
 * xxxxx is the username of the owning user for that web application
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/09/05 00:31:50 $
 */

public final class UserConfig
    implements LifecycleListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Java class name of the Context configuration class we should use.
     */
    private String configClass = "org.apache.catalina.startup.ContextConfig";


    /**
     * The Java class name of the Context implementation we should use.
     */
    private String contextClass = "org.apache.catalina.core.StandardContext";


    /**
     * The debugging detail level for this component.
     */
    private int debug = 999;


    /**
     * The directory name to be searched for within each user home directory.
     */
    private String directoryName = "public_html";


    /**
     * The base directory containing user home directories.
     */
    private String homeBase = null;


    /**
     * The Host we are associated with.
     */
    private Host host = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The Java class name of the user database class we should use.
     */
    private String userClass =
        "org.apache.catalina.startup.PasswdUserDatabase";


    // ------------------------------------------------------------- Properties


    /**
     * Return the Context configuration class name.
     */
    public String getConfigClass() {

        return (this.configClass);

    }


    /**
     * Set the Context configuration class name.
     *
     * @param configClass The new Context configuration class name.
     */
    public void setConfigClass(String configClass) {

        this.configClass = configClass;

    }


    /**
     * Return the Context implementation class name.
     */
    public String getContextClass() {

        return (this.contextClass);

    }


    /**
     * Set the Context implementation class name.
     *
     * @param contextClass The new Context implementation class name.
     */
    public void setContextClass(String contextClass) {

        this.contextClass = contextClass;

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the directory name for user web applications.
     */
    public String getDirectoryName() {

        return (this.directoryName);

    }


    /**
     * Set the directory name for user web applications.
     *
     * @param directoryName The new directory name
     */
    public void setDirectoryName(String directoryName) {

        this.directoryName = directoryName;

    }


    /**
     * Return the base directory containing user home directories.
     */
    public String getHomeBase() {

        return (this.homeBase);

    }


    /**
     * Set the base directory containing user home directories.
     *
     * @param homeBase The new base directory
     */
    public void setHomeBase(String homeBase) {

        this.homeBase = homeBase;

    }


    /**
     * Return the user database class name for this component.
     */
    public String getUserClass() {

        return (this.userClass);

    }


    /**
     * Set the user database class name for this component.
     */
    public void setUserClass(String userClass) {

        this.userClass = userClass;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Host.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the host we are associated with
        try {
            host = (Host) event.getLifecycle();
        } catch (ClassCastException e) {
            log(sm.getString("hostConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Deploy a web application for any user who has a web application present
     * in a directory with a specified name within their home directory.
     */
    private void deploy() {

        if (debug >= 1)
            log(sm.getString("userConfig.deploying"));

        // Load the user database object for this host
        UserDatabase database = null;
        try {
            Class clazz = Class.forName(userClass);
            database = (UserDatabase) clazz.newInstance();
            database.setUserConfig(this);
        } catch (Exception e) {
            log(sm.getString("userConfig.database"), e);
            return;
        }

        // Deploy the web application (if any) for each defined user
        Enumeration users = database.getUsers();
        while (users.hasMoreElements()) {
            String user = (String) users.nextElement();
            String home = database.getHome(user);
            deploy(user, home);
        }

    }


    /**
     * Deploy a web application for the specified user if they have such an
     * application in the defined directory within their home directory.
     *
     * @param user Username owning the application to be deployed
     * @param home Home directory of this user
     */
    private void deploy(String user, String home) {

        // Does this user have a web application to be deployed?
        String contextPath = "/~" + user;
        if (host.findChild(contextPath) != null)
            return;
        File app = new File(home, directoryName);
        if (!app.exists() || !app.isDirectory())
            return;
        /*
        File dd = new File(app, "/WEB-INF/web.xml");
        if (!dd.exists() || !dd.isFile() || !dd.canRead())
            return;
        */
        log(sm.getString("userConfig.deploy", user));

        // Deploy the web application for this user
        try {
            Class clazz = Class.forName(contextClass);
            Context context =
              (Context) clazz.newInstance();
            context.setPath(contextPath);
            context.setDocBase(app.toString());
            if (context instanceof Lifecycle) {
                clazz = Class.forName(configClass);
                LifecycleListener listener =
                  (LifecycleListener) clazz.newInstance();
                ((Lifecycle) context).addLifecycleListener(listener);
            }
            host.addChild(context);
        } catch (Exception e) {
            log(sm.getString("userConfig.error", user), e);
        }

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = null;
        if (host != null)
            logger = host.getLogger();
        if (logger != null)
            logger.log("UserConfig[" + host.getName() + "]: " + message);
        else
            System.out.println("UserConfig[" + host.getName() + "]: "
                               + message);

    }


    /**
     * Log a message on the Logger associated with our Host (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = null;
        if (host != null)
            logger = host.getLogger();
        if (logger != null)
            logger.log("UserConfig[" + host.getName() + "] "
                       + message, throwable);
        else {
            System.out.println("UserConfig[" + host.getName() + "]: "
                               + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Process a "start" event for this Host.
     */
    private void start() {

        if (debug > 0)
            log(sm.getString("userConfig.start"));

        deploy();

    }


    /**
     * Process a "stop" event for this Host.
     */
    private void stop() {

        if (debug > 0)
            log(sm.getString("userConfig.stop"));

    }


}
