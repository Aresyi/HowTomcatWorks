/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/Tool.java,v 1.5 2002/04/01 19:51:31 patrickl Exp $
 * $Revision: 1.5 $
 * $Date: 2002/04/01 19:51:31 $
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
import java.util.ArrayList;


/**
 * <p>General purpose wrapper for command line tools that should execute in an
 * environment with the common class loader environment set up by Catalina.
 * This should be executed from a command line script that conforms to
 * the following requirements:</p>
 * <ul>
 * <li>Passes the <code>catalina.home</code> system property configured with
 *     the pathname of the Tomcat installation directory.</li>
 * <li>Sets the system classpath to include <code>bootstrap.jar</code> and
 *     <code>$JAVA_HOME/lib/tools.jar</code>.</li>
 * </ul>
 *
 * <p>The command line to execute the tool looks like:</p>
 * <pre>
 *   java -classpath $CLASSPATH org.apache.catalina.startup.Tool \
 *     ${options} ${classname} ${arguments}
 * </pre>
 *
 * <p>with the following replacement contents:
 * <ul>
 * <li><strong>${options}</strong> - Command line options for this Tool wrapper.
 *     The following options are supported:
 *     <ul>
 *     <li><em>-ant</em> : Set the <code>ant.home</code> system property
 *         to corresponding to the value of <code>catalina.home</code>
 *         (useful when your command line tool runs Ant).</li>
 *     <li><em>-common</em> : Add <code>common/classes</code> and
 *         <code>common/lib</codE) to the class loader repositories.</li>
 *     <li><em>-debug</em> : Enable debugging messages from this wrapper.</li>
 *     <li><em>-server</em> : Add <code>server/classes</code> and
 *         <code>server/lib</code> to the class loader repositories.</li>
 *     <li><em>-shared</em> : Add <code>shared/classes</code> and
 *         <code>shared/lib</code> to the class loader repositories.</li>
 *     </ul>
 * <li><strong>${classname}</strong> - Fully qualified Java class name of the
 *     application's main class.</li>
 * <li><strong>${arguments}</strong> - Command line arguments to be passed to
 *     the application's <code>main()</code> method.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/04/01 19:51:31 $
 */

public final class Tool {


    // ------------------------------------------------------- Static Variables


    /**
     * Set <code>ant.home</code> system property?
     */
    private static boolean ant = false;


    /**
     * The pathname of our installation base directory.
     */
    private static String catalinaHome = System.getProperty("catalina.home");


    /**
     * Include common classes in the repositories?
     */
    private static boolean common = false;


    /**
     * Enable debugging detail messages?
     */
    private static boolean debug = false;


    /**
     * Include server classes in the repositories?
     */
    private static boolean server = false;


    /**
     * Include shared classes in the repositories?
     */
    private static boolean shared = false;


    // ----------------------------------------------------------- Main Program


    /**
     * The main program for the bootstrap.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        // Verify that "catalina.home" was passed.
        if (catalinaHome == null) {
            log("Must set 'catalina.home' system property");
            System.exit(1);
        }

        // Process command line options
        int index = 0;
        while (true) {
            if (index == args.length) {
                usage();
                System.exit(1);
            }
            if ("-ant".equals(args[index]))
                ant = true;
            else if ("-common".equals(args[index]))
                common = true;
            else if ("-debug".equals(args[index]))
                debug = true;
            else if ("-server".equals(args[index]))
                server = true;
            else if ("-shared".equals(args[index]))
                shared = true;
            else
                break;
            index++;
        }
        if (index > args.length) {
            usage();
            System.exit(1);
        }

        // Set "ant.home" if requested
        if (ant)
            System.setProperty("ant.home", catalinaHome);

        // Construct the class loader we will be using
        ClassLoader classLoader = null;
        try {
            if (debug) {
                log("Constructing class loader");
                ClassLoaderFactory.setDebug(1);
            }
            ArrayList packed = new ArrayList();
            ArrayList unpacked = new ArrayList();
            unpacked.add(new File(catalinaHome, "classes"));
            packed.add(new File(catalinaHome, "lib"));
            if (common) {
                unpacked.add(new File(catalinaHome,
                                      "common" + File.separator + "classes"));
                packed.add(new File(catalinaHome,
                                    "common" + File.separator + "lib"));
            }
            if (server) {
                unpacked.add(new File(catalinaHome,
                                      "server" + File.separator + "classes"));
                packed.add(new File(catalinaHome,
                                    "server" + File.separator + "lib"));
            }
            if (shared) {
                unpacked.add(new File(catalinaHome,
                                      "shared" + File.separator + "classes"));
                packed.add(new File(catalinaHome,
                                    "shared" + File.separator + "lib"));
            }
            classLoader =
                ClassLoaderFactory.createClassLoader
                ((File[]) unpacked.toArray(new File[0]),
                 (File[]) packed.toArray(new File[0]),
                 null);
        } catch (Throwable t) {
            log("Class loader creation threw exception", t);
            System.exit(1);
        }
        Thread.currentThread().setContextClassLoader(classLoader);

        // Load our application class
        Class clazz = null;
        String className = args[index++];
        try {
            if (debug)
                log("Loading application class " + className);
            clazz = classLoader.loadClass(className);
        } catch (Throwable t) {
            log("Exception creating instance of " + className, t);
            System.exit(1);
        }

        // Locate the static main() method of the application class
        Method method = null;
        String params[] = new String[args.length - index];
        System.arraycopy(args, index, params, 0, params.length);
        try {
            if (debug)
                log("Identifying main() method");
            String methodName = "main";
            Class paramTypes[] = new Class[1];
            paramTypes[0] = params.getClass();
            method = clazz.getMethod(methodName, paramTypes);
        } catch (Throwable t) {
            log("Exception locating main() method", t);
            System.exit(1);
        }

        // Invoke the main method of the application class
        try {
            if (debug)
                log("Calling main() method");
            Object paramValues[] = new Object[1];
            paramValues[0] = params;
            method.invoke(null, paramValues);
        } catch (Throwable t) {
            log("Exception calling main() method", t);
            System.exit(1);
        }

    }


    /**
     * Log a debugging detail message.
     *
     * @param message The message to be logged
     */
    private static void log(String message) {

        System.out.print("Tool: ");
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


    /**
     * Display usage information about this tool.
     */
    private static void usage() {

        log("Usage:  java org.apache.catalina.startup.Tool [<options>] <class> [<arguments>]");

    }


}
