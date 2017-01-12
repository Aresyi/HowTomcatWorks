/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/BootstrapService.java,v 1.17 2002/09/16 15:36:54 jfclere Exp $
 * $Revision: 1.17 $
 * $Date: 2002/09/16 15:36:54 $
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
import java.lang.reflect.Method;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;


/**
 * Special version of the Catalina bootstrap, designed to be invoked with JNI,
 * and designed to allow easier wrapping by system level components, which
 * would otherwise be confused by the asychronous startup and shutdown Catalina
 * uses. This class should be used to run Catalina as a system service under
 * Windows NT and clones.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.17 $ $Date: 2002/09/16 15:36:54 $
 */

public final class BootstrapService 
    implements Daemon {


    // ------------------------------------------------------- Static Variables


    /**
     * Service object used by main.
     */
    private static Daemon service = null;


    /**
     * Debugging detail level for processing the startup.
     */
    private static int debug = 0;


    /**
     * Catalina service.
     */
    private Object catalinaService = null;


    // -------------------------------------------------------- Service Methods


    /**
     * Load the Catalina Service.
     */
    public void init(DaemonContext context)
        throws Exception {

        String arguments[] = null;

        /* Read the arguments from the Daemon context */
        if (context!=null) {
            arguments = context.getArguments();
            if (arguments!=null) {
                for (int i = 0; i < arguments.length; i++) {
                    if (arguments[i].equals("-debug")) {
                        debug = 1;
                    }
                }
            }
        }

        log("Create Catalina server");

        // Set Catalina path
        setCatalinaHome();
        setCatalinaBase();

        // Construct the class loaders we will need
        ClassLoader commonLoader = null;
        ClassLoader catalinaLoader = null;
        ClassLoader sharedLoader = null;
        try {

            File unpacked[] = new File[1];
            File packed[] = new File[1];
            File packed2[] = new File[2];
            ClassLoaderFactory.setDebug(debug);

            unpacked[0] = new File(getCatalinaHome(),
                                   "common" + File.separator + "classes");
            packed2[0] = new File(getCatalinaHome(),
                                  "common" + File.separator + "endorsed");
            packed2[1] = new File(getCatalinaHome(),
                                  "common" + File.separator + "lib");
            commonLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed2, null);

            unpacked[0] = new File(getCatalinaHome(),
                                   "server" + File.separator + "classes");
            packed[0] = new File(getCatalinaHome(),
                                 "server" + File.separator + "lib");
            catalinaLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);
            System.err.println("Created catalinaLoader in: " + getCatalinaHome()
                 +  File.separator +
                 "server" + File.separator + "lib");

            unpacked[0] = new File(getCatalinaBase(),
                                   "shared" + File.separator + "classes");
            packed[0] = new File(getCatalinaBase(),
                                 "shared" + File.separator + "lib");
            sharedLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);

        } catch (Throwable t) {

            log("Class loader creation threw exception", t);

        }
        
        Thread.currentThread().setContextClassLoader(catalinaLoader);

        SecurityClassLoad.securityClassLoad(catalinaLoader);

        // Load our startup class and call its process() method
        if (debug >= 1)
            log("Loading startup class");
        Class startupClass =
            catalinaLoader.loadClass
            ("org.apache.catalina.startup.CatalinaService");
        Object startupInstance = startupClass.newInstance();
        
        // Set the shared extensions class loader
        if (debug >= 1)
            log("Setting startup class properties");
        String methodName = "setParentClassLoader";
        Class paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object paramValues[] = new Object[1];
        paramValues[0] = sharedLoader;
        Method method =
            startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);
        
        catalinaService = startupInstance;
        
        // Call the load() method
        methodName = "load";
        Object param[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        method = catalinaService.getClass().getMethod(methodName, paramTypes);
        if (debug >= 1)
            log("Calling startup class " + method);
        method.invoke(catalinaService, param);

    }


    /**
     * Start the Catalina Service.
     */
    public void start()
        throws Exception {

        log("Starting service");
        String methodName = "start";
        Method method = catalinaService.getClass().getMethod(methodName, null);
        method.invoke(catalinaService, null);
        log("Service started");

    }


    /**
     * Stop the Catalina Service.
     */
    public void stop()
        throws Exception {

        log("Stopping service");
        String methodName = "stop";
        Method method = catalinaService.getClass().getMethod(methodName, null);
        method.invoke(catalinaService, null);
        log("Service stopped");

    }


    /**
     * Destroy the Catalina Service.
     */
    public void destroy() {

        // FIXME

    }


    // ----------------------------------------------------------- Main Program


    /**
     * Main method, used for testing only.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        // Set the debug flag appropriately
        for (int i = 0; i < args.length; i++)  {
            if ("-debug".equals(args[i]))
                debug = 1;
        }

        if (service == null) {
            service = new BootstrapService();
            try {
                BootstrapServiceContext p0 = new BootstrapServiceContext();
                p0.setArguments(args);
                service.init(p0);
            } catch (Throwable t) {
                t.printStackTrace();
                return;
            }
        }

        try {
            String command = args[0];
            if (command.equals("start")) {
                service.start();
            } else if (command.equals("stop")) {
                service.stop();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }


    /**
     * Set the <code>catalina.base</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaBase() {

        if (System.getProperty("catalina.base") != null)
            return;
        if (System.getProperty("catalina.home") != null)
            System.setProperty("catalina.base",
                               System.getProperty("catalina.home"));
        else
            System.setProperty("catalina.base",
                               System.getProperty("user.dir"));

    }


    /**
     * Set the <code>catalina.home</code> System property to the current
     * working directory if it has not been set.
     */
    private void setCatalinaHome() {

        if (System.getProperty("catalina.home") != null)
            return;
        System.setProperty("catalina.home",
                           System.getProperty("user.dir"));

    }


    /**
     * Get the value of the catalina.home environment variable.
     */
    private static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    private static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }


    /**
     * Log a debugging detail message.
     *
     * @param message The message to be logged
     */
    private static void log(String message) {

        System.out.print("Bootstrap: ");
        System.out.println(message);

    }


    /**
     * Log a debugging detail message with an exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    private static void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }


}
