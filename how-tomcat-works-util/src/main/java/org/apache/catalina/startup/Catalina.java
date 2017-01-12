/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/Catalina.java,v 1.48 2002/05/23 17:22:37 glenn Exp $
 * $Revision: 1.48 $
 * $Date: 2002/05/23 17:22:37 $
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Security;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;


/**
 * Startup/Shutdown shell program for Catalina.  The following command line
 * options are recognized:
 * <ul>
 * <li><b>-config {pathname}</b> - Set the pathname of the configuration file
 *     to be processed.  If a relative path is specified, it will be
 *     interpreted as relative to the directory pathname specified by the
 *     "catalina.base" system property.   [conf/server.xml]
 * <li><b>-help</b> - Display usage information.
 * <li><b>-stop</b> - Stop the currently running instance of Catalina.
 * </u>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.48 $ $Date: 2002/05/23 17:22:37 $
 */

public class Catalina {


    // ----------------------------------------------------- Instance Variables


    /**
     * Pathname to the server configuration file.
     */
    protected String configFile = "conf/server.xml";


    /**
     * Set the debugging detail level on our Digester.
     */
    protected boolean debug = false;


    /**
     * The shared extensions class loader for this server.
     */
    protected ClassLoader parentClassLoader =
        ClassLoader.getSystemClassLoader();


    /**
     * The server component we are starting or stopping
     */
    protected Server server = null;


    /**
     * Are we starting a new server?
     */
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     */
    protected boolean stopping = false;


    /**
     * Are we using naming ?
     */
    protected boolean useNaming = true;


    // ----------------------------------------------------------- Main Program


    /**
     * The application main program.
     *
     * @param args Command line arguments
     */
    public static void main(String args[]) {

        (new Catalina()).process(args);
    }


    /**
     * The instance main program.
     *
     * @param args Command line arguments
     */
    public void process(String args[]) {

        setCatalinaHome();
        setCatalinaBase();
        try {
            if (arguments(args))
                execute();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Set the shared extensions class loader.
     *
     * @param parentClassLoader The shared extensions class loader.
     */
    public void setParentClassLoader(ClassLoader parentClassLoader) {

        this.parentClassLoader = parentClassLoader;

    }


    /**
     * Set the server instance we are configuring.
     *
     * @param server The new server
     */
    public void setServer(Server server) {

        this.server = server;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Process the specified command line arguments, and return
     * <code>true</code> if we should continue processing; otherwise
     * return <code>false</code>.
     *
     * @param args Command line arguments to process
     */
    protected boolean arguments(String args[]) {

        boolean isConfig = false;

        if (args.length < 1) {
            usage();
            return (false);
        }

        for (int i = 0; i < args.length; i++) {
            if (isConfig) {
                configFile = args[i];
                isConfig = false;
            } else if (args[i].equals("-config")) {
                isConfig = true;
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else if (args[i].equals("-nonaming")) {
                useNaming = false;
            } else if (args[i].equals("-help")) {
                usage();
                return (false);
            } else if (args[i].equals("start")) {
                starting = true;
            } else if (args[i].equals("stop")) {
                stopping = true;
            } else {
                usage();
                return (false);
            }
        }

        return (true);

    }


    /**
     * Return a File object representing our configuration file.
     */
    protected File configFile() {

        File file = new File(configFile);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), configFile);
        return (file);

    }


    /**
     * Create and configure the Digester we will be using for startup.
     */
    protected Digester createStartDigester() {

        // Initialize the digester
        Digester digester = new Digester();
        if (debug)
            digester.setDebug(999);
        digester.setValidating(false);

        // Configure the actions we will be using
        digester.addObjectCreate("Server",
                                 "org.apache.catalina.core.StandardServer",
                                 "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                            "setServer",
                            "org.apache.catalina.Server");

        digester.addObjectCreate("Server/GlobalNamingResources",
                                 "org.apache.catalina.deploy.NamingResources");
        digester.addSetProperties("Server/GlobalNamingResources");
        digester.addSetNext("Server/GlobalNamingResources",
                            "setGlobalNamingResources",
                            "org.apache.catalina.deploy.NamingResources");

        digester.addObjectCreate("Server/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Listener");
        digester.addSetNext("Server/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service",
                                 "org.apache.catalina.core.StandardService",
                                 "className");
        digester.addSetProperties("Server/Service");
        digester.addSetNext("Server/Service",
                            "addService",
                            "org.apache.catalina.Service");

        digester.addObjectCreate("Server/Service/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Listener");
        digester.addSetNext("Server/Service/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate("Server/Service/Connector",
                                 "org.apache.catalina.connector.http.HttpConnector",
                                 "className");
        digester.addSetProperties("Server/Service/Connector");
        digester.addSetNext("Server/Service/Connector",
                            "addConnector",
                            "org.apache.catalina.Connector");

        digester.addObjectCreate("Server/Service/Connector/Factory",
                                 "org.apache.catalina.net.DefaultServerSocketFactory",
                                 "className");
        digester.addSetProperties("Server/Service/Connector/Factory");
        digester.addSetNext("Server/Service/Connector/Factory",
                            "setFactory",
                            "org.apache.catalina.net.ServerSocketFactory");

        digester.addObjectCreate("Server/Service/Connector/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties("Server/Service/Connector/Listener");
        digester.addSetNext("Server/Service/Connector/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        // Add RuleSets for nested elements
        digester.addRuleSet(new NamingRuleSet("Server/GlobalNamingResources/"));
        digester.addRuleSet(new EngineRuleSet("Server/Service/"));
        digester.addRuleSet(new HostRuleSet("Server/Service/Engine/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Default"));
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/DefaultContext/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/Default"));
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/DefaultContext/"));
        digester.addRuleSet(new ContextRuleSet("Server/Service/Engine/Host/"));
        digester.addRuleSet(new NamingRuleSet("Server/Service/Engine/Host/Context/"));

        digester.addRule("Server/Service/Engine",
                         new SetParentClassLoaderRule(digester,
                                                      parentClassLoader));


        return (digester);

    }


    /**
     * Create and configure the Digester we will be using for shutdown.
     */
    protected Digester createStopDigester() {

        // Initialize the digester
        Digester digester = new Digester();
        if (debug)
            digester.setDebug(999);

        // Configure the rules we need for shutting down
        digester.addObjectCreate("Server",
                                 "org.apache.catalina.core.StandardServer",
                                 "className");
        digester.addSetProperties("Server");
        digester.addSetNext("Server",
                            "setServer",
                            "org.apache.catalina.Server");

        return (digester);

    }


    /**
     * Execute the processing that has been configured from the command line.
     */
    protected void execute() throws Exception {

        if (starting)
            start();
        else if (stopping)
            stop();

    }


    /**
     * Set the <code>catalina.base</code> System property to the current
     * working directory if it has not been set.
     */
    protected void setCatalinaBase() {

        if (System.getProperty("catalina.base") != null)
            return;
        System.setProperty("catalina.base",
                           System.getProperty("catalina.home"));

    }


    /**
     * Set the <code>catalina.home</code> System property to the current
     * working directory if it has not been set.
     */
    protected void setCatalinaHome() {

        if (System.getProperty("catalina.home") != null)
            return;
        System.setProperty("catalina.home",
                           System.getProperty("user.dir"));

    }


    /**
     * Start a new server instance.
     */
    protected void start() {

        // Create and execute our Digester
        Digester digester = createStartDigester();
        File file = configFile();
        try {
            InputSource is =
                new InputSource("file://" + file.getAbsolutePath());
            FileInputStream fis = new FileInputStream(file);
            is.setByteStream(fis);
            digester.push(this);
            digester.parse(is);
            fis.close();
        } catch (Exception e) {
            System.out.println("Catalina.start: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }

        // Setting additional variables
        if (!useNaming) {
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            value = System.getProperty
                (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty
                    (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                     "org.apache.naming.java.javaURLContextFactory");
            }
        }

        // If a SecurityManager is being used, set properties for
        // checkPackageAccess() and checkPackageDefinition
        if( System.getSecurityManager() != null ) {
            String access = Security.getProperty("package.access");
            if( access != null && access.length() > 0 )
                access += ",";
            else
                access = "sun.,";
            Security.setProperty("package.access",
                access + "org.apache.catalina.,org.apache.jasper.");
            String definition = Security.getProperty("package.definition");
            if( definition != null && definition.length() > 0 )
                definition += ",";
            else
                definition = "sun.,";
            Security.setProperty("package.definition",
                // FIX ME package "javax." was removed to prevent HotSpot
                // fatal internal errors
                definition + "java.,org.apache.catalina.,org.apache.jasper.");
        }

        // Replace System.out and System.err with a custom PrintStream
        SystemLogHandler log = new SystemLogHandler(System.out);
        System.setOut(log);
        System.setErr(log);

        Thread shutdownHook = new CatalinaShutdownHook();

        // Start the new server
        if (server instanceof Lifecycle) {
            try {
                server.initialize();
                ((Lifecycle) server).start();
                try {
                    // Register shutdown hook
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                } catch (Throwable t) {
                    // This will fail on JDK 1.2. Ignoring, as Tomcat can run
                    // fine without the shutdown hook.
                }
                // Wait for the server to be told to shut down
                server.await();
            } catch (LifecycleException e) {
                System.out.println("Catalina.start: " + e);
                e.printStackTrace(System.out);
                if (e.getThrowable() != null) {
                    System.out.println("----- Root Cause -----");
                    e.getThrowable().printStackTrace(System.out);
                }
            }
        }

        // Shut down the server
        if (server instanceof Lifecycle) {
            try {
                try {
                    // Remove the ShutdownHook first so that server.stop()
                    // doesn't get invoked twice
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (Throwable t) {
                    // This will fail on JDK 1.2. Ignoring, as Tomcat can run
                    // fine without the shutdown hook.
                }
                ((Lifecycle) server).stop();
            } catch (LifecycleException e) {
                System.out.println("Catalina.stop: " + e);
                e.printStackTrace(System.out);
                if (e.getThrowable() != null) {
                    System.out.println("----- Root Cause -----");
                    e.getThrowable().printStackTrace(System.out);
                }
            }
        }

    }


    /**
     * Stop an existing server instance.
     */
    protected void stop() {

        // Create and execute our Digester
        Digester digester = createStopDigester();
        File file = configFile();
        try {
            InputSource is =
                new InputSource("file://" + file.getAbsolutePath());
            FileInputStream fis = new FileInputStream(file);
            is.setByteStream(fis);
            digester.push(this);
            digester.parse(is);
            fis.close();
        } catch (Exception e) {
            System.out.println("Catalina.stop: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }

      // Stop the existing server
      try {
          Socket socket = new Socket("127.0.0.1", server.getPort());
          OutputStream stream = socket.getOutputStream();
          String shutdown = server.getShutdown();
          for (int i = 0; i < shutdown.length(); i++)
              stream.write(shutdown.charAt(i));
          stream.flush();
          stream.close();
          socket.close();
      } catch (IOException e) {
          System.out.println("Catalina.stop: " + e);
          e.printStackTrace(System.out);
          System.exit(1);
      }


    }


    /**
     * Print usage information for this application.
     */
    protected void usage() {

        System.out.println
            ("usage: java org.apache.catalina.startup.Catalina"
             + " [ -config {pathname} ] [ -debug ]"
             + " [ -nonaming ] { start | stop }");

    }


    // --------------------------------------- CatalinaShutdownHook Inner Class


    /**
     * Shutdown hook which will perform a clean shutdown of Catalina if needed.
     */
    protected class CatalinaShutdownHook extends Thread {

        public void run() {

            if (server != null) {
                try {
                    ((Lifecycle) server).stop();
                } catch (LifecycleException e) {
                    System.out.println("Catalina.stop: " + e);
                    e.printStackTrace(System.out);
                    if (e.getThrowable() != null) {
                        System.out.println("----- Root Cause -----");
                        e.getThrowable().printStackTrace(System.out);
                    }
                }
            }

        }
    }
}


// ------------------------------------------------------------ Private Classes


/**
 * Rule that sets the parent class loader for the top object on the stack,
 * which must be a <code>Container</code>.
 */

final class SetParentClassLoaderRule extends Rule {

    public SetParentClassLoaderRule(Digester digester,
                                    ClassLoader parentClassLoader) {

        super(digester);
        this.parentClassLoader = parentClassLoader;

    }

    ClassLoader parentClassLoader = null;

    public void begin(Attributes attributes) throws Exception {

        if (digester.getDebug() >= 1)
            digester.log("Setting parent class loader");

        Container top = (Container) digester.peek();
        top.setParentClassLoader(parentClassLoader);

    }


}
