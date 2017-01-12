package org.apache.catalina.startup;


import java.io.File;
import java.lang.reflect.Method;


/**
 * Boostrap loader for Catalina.  This application constructs a class loader
 * for use in loading the Catalina internal classes (by accumulating all of the
 * JAR files found in the "server" directory under "catalina.home"), and
 * starts the regular execution of the container.  The purpose of this
 * roundabout approach is to keep the Catalina internal classes (and any
 * other classes they depend on, such as an XML parser) out of the system
 * class path and therefore not visible to application level classes.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.36 $ $Date: 2002/04/01 19:51:31 $
 */

public final class Bootstrap {


    // ------------------------------------------------------- Static Variables


    /**
     * Debugging detail level for processing the startup.
     */
    private static int debug = 0;


    // ----------------------------------------------------------- Main Program


    /**
     * The main program for the bootstrap.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        // Set the debug flag appropriately
        for (int i = 0; i < args.length; i++)  {
            if ("-debug".equals(args[i]))
                debug = 1;
        }
        
        // Configure catalina.base from catalina.home if not yet set
        if (System.getProperty("catalina.base") == null)
            System.setProperty("catalina.base", getCatalinaHome());

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

            unpacked[0] = new File(getCatalinaBase(),
                                   "shared" + File.separator + "classes");
            packed[0] = new File(getCatalinaBase(),
                                 "shared" + File.separator + "lib");
            sharedLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);
        } catch (Throwable t) {

            log("Class loader creation threw exception", t);
            System.exit(1);

        }

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        // Load our startup class and call its process() method
        try {

            SecurityClassLoad.securityClassLoad(catalinaLoader);

            // Instantiate a startup class instance
            if (debug >= 1)
                log("Loading startup class");
            Class startupClass =
                catalinaLoader.loadClass
                ("org.apache.catalina.startup.Catalina");
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

            // Call the process() method
            if (debug >= 1)
                log("Calling startup class process() method");
            methodName = "process";
            paramTypes = new Class[1];
            paramTypes[0] = args.getClass();
            paramValues = new Object[1];
            paramValues[0] = args;
            method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
            method.invoke(startupInstance, paramValues);

        } catch (Exception e) {
            System.out.println("Exception during startup processing");
            e.printStackTrace(System.out);
            System.exit(2);
        }

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
