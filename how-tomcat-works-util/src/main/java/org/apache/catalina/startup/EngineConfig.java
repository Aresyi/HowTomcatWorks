/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/EngineConfig.java,v 1.3 2001/07/22 20:25:13 pier Exp $
 * $Revision: 1.3 $
 * $Date: 2001/07/22 20:25:13 $
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

import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.util.StringManager;


/**
 * Startup event listener for a <b>Engine</b> that configures the properties
 * of that Engine, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/07/22 20:25:13 $
 */

public final class EngineConfig
    implements LifecycleListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The Engine we are associated with.
     */
    private Engine engine = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


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


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Engine.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the engine we are associated with
        try {
            engine = (Engine) event.getLifecycle();
            if (engine instanceof StandardEngine) {
                int engineDebug = ((StandardEngine) engine).getDebug();
                if (engineDebug > this.debug)
                    this.debug = engineDebug;
            }
        } catch (ClassCastException e) {
            log(sm.getString("engineConfig.cce", event.getLifecycle()), e);
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
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = null;
        if (engine != null)
            logger = engine.getLogger();
        if (logger != null)
            logger.log("EngineConfig: " + message);
        else
            System.out.println("EngineConfig: " + message);

    }


    /**
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = null;
        if (engine != null)
            logger = engine.getLogger();
        if (logger != null)
            logger.log("EngineConfig: " + message, throwable);
        else {
            System.out.println("EngineConfig: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Process a "start" event for this Engine.
     */
    private void start() {

        if (debug > 0)
            log(sm.getString("engineConfig.start"));

    }


    /**
     * Process a "stop" event for this Engine.
     */
    private void stop() {

        if (debug > 0)
            log(sm.getString("engineConfig.stop"));

    }


}
