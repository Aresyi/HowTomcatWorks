/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardPipeline.java,v 1.8 2002/06/09 02:19:42 remm Exp $
 * $Revision: 1.8 $
 * $Date: 2002/06/09 02:19:42 $
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


import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.ValveContext;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of a processing <b>Pipeline</b> that will invoke
 * a series of Valves that have been configured to be called in order.  This
 * implementation can be used for any type of Container.
 *
 * <b>IMPLEMENTATION WARNING</b> - This implementation assumes that no
 * calls to <code>addValve()</code> or <code>removeValve</code> are allowed
 * while a request is currently being processed.  Otherwise, the mechanism
 * by which per-thread state is maintained will need to be modified.
 *
 * @author Craig R. McClanahan
 */

public class StandardPipeline
    implements Pipeline, Contained, Lifecycle {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new StandardPipeline instance with no associated Container.
     */
    public StandardPipeline() {

        this(null);

    }


    /**
     * Construct a new StandardPipeline instance that is associated with the
     * specified Container.
     *
     * @param container The container we should be associated with
     */
    public StandardPipeline(Container container) {

        super();
        setContainer(container);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The basic Valve (if any) associated with this Pipeline.
     */
    protected Valve basic = null;


    /**
     * The Container with which this Pipeline is associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Descriptive information about this implementation.
     */
    protected String info = "org.apache.catalina.core.StandardPipeline/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;


    /**
     * The set of Valves (not including the Basic one, if any) associated with
     * this Pipeline.
     */
    protected Valve valves[] = new Valve[0];


    // --------------------------------------------------------- Public Methods


    /**
     * Return descriptive information about this implementation class.
     */
    public String getInfo() {

        return (this.info);

    }


    // ------------------------------------------------------ Contained Methods


    /**
     * Return the Container with which this Pipeline is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Pipeline is associated.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("standardPipeline.alreadyStarted"));

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        started = true;

        // Start the Valves in our pipeline (including the basic), if any
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof Lifecycle)
                ((Lifecycle) valves[i]).start();
        }
        if ((basic != null) && (basic instanceof Lifecycle))
            ((Lifecycle) basic).start();

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardPipeline.notStarted"));

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop the Valves in our pipeline (including the basic), if any
        if ((basic != null) && (basic instanceof Lifecycle))
            ((Lifecycle) basic).stop();
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof Lifecycle)
                ((Lifecycle) valves[i]).stop();
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

    }


    // ------------------------------------------------------- Pipeline Methods


    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    public Valve getBasic() {

        return (this.basic);

    }


    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prioer to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    public void setBasic(Valve valve) {

        // Change components if necessary
        Valve oldBasic = this.basic;
        if (oldBasic == valve)
            return;

        // Stop the old component if necessary
        if (oldBasic != null) {
            if (started && (oldBasic instanceof Lifecycle)) {
                try {
                    ((Lifecycle) oldBasic).stop();
                } catch (LifecycleException e) {
                    log("StandardPipeline.setBasic: stop", e);
                }
            }
            if (oldBasic instanceof Contained) {
                try {
                    ((Contained) oldBasic).setContainer(null);
                } catch (Throwable t) {
                    ;
                }
            }
        }

        // Start the new component if necessary
        if (valve == null)
            return;
        if (valve instanceof Contained) {
            ((Contained) valve).setContainer(this.container);
        }
        if (valve instanceof Lifecycle) {
            try {
                ((Lifecycle) valve).start();
            } catch (LifecycleException e) {
                log("StandardPipeline.setBasic: start", e);
                return;
            }
        }
        this.basic = valve;

    }


    /**
     * <p>Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements
     * <code>Contained</code>, with the owning Container as an argument.
     * The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.</p>
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specifie Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public void addValve(Valve valve) {

        // Validate that we can add this Valve
        if (valve instanceof Contained)
            ((Contained) valve).setContainer(this.container);

        // Start the new component if necessary
        if (started && (valve instanceof Lifecycle)) {
            try {
                ((Lifecycle) valve).start();
            } catch (LifecycleException e) {
                log("StandardPipeline.addValve: start: ", e);
            }
        }

        // Add this Valve to the set associated with this Pipeline
        synchronized (valves) {
            Valve results[] = new Valve[valves.length +1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = valve;
            valves = results;
        }

    }


    /**
     * Return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     */
    public Valve[] getValves() {

        if (basic == null)
            return (valves);
        synchronized (valves) {
            Valve results[] = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = basic;
            return (results);
        }

    }


    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        // Invoke the first Valve in this pipeline for this request
        (new StandardPipelineValveContext()).invokeNext(request, response);

    }


    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.  If the Valve is
     * found and removed, the Valve's <code>setContainer(null)</code> method
     * will be called if it implements <code>Contained</code>.
     *
     * @param valve Valve to be removed
     */
    public void removeValve(Valve valve) {

        synchronized (valves) {

            // Locate this Valve in our list
            int j = -1;
            for (int i = 0; i < valves.length; i++) {
                if (valve == valves[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;

            // Remove this valve from our list
            Valve results[] = new Valve[valves.length - 1];
            int n = 0;
            for (int i = 0; i < valves.length; i++) {
                if (i == j)
                    continue;
                results[n++] = valves[i];
            }
            valves = results;
            try {
                if (valve instanceof Contained)
                    ((Contained) valve).setContainer(null);
            } catch (Throwable t) {
                ;
            }

        }

        // Stop this valve if necessary
        if (started && (valve instanceof Lifecycle)) {
            try {
                ((Lifecycle) valve).stop();
            } catch (LifecycleException e) {
                log("StandardPipeline.removeValve: stop: ", e);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardPipeline[" + container.getName() + "]: " +
                       message);
        else
            System.out.println("StandardPipeline[" + container.getName() +
                               "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardPipeline[" + container.getName() + "]: " +
                       message, throwable);
        else {
            System.out.println("StandardPipeline[" + container.getName() +
                               "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }


    // ------------------------------- StandardPipelineValveContext Inner Class


    protected class StandardPipelineValveContext
        implements ValveContext {


        // ------------------------------------------------- Instance Variables


        protected int stage = 0;


        // --------------------------------------------------------- Properties


        /**
          * Return descriptive information about this ValveContext 
          * implementation.
          */
        public String getInfo() {
            return info;
        }


        // ----------------------------------------------------- Public Methods


        /**
         * Cause the <code>invoke()</code> method of the next Valve that is 
         * part of the Pipeline currently being processed (if any) to be 
         * executed, passing on the specified request and response objects 
         * plus this <code>ValveContext</code> instance.  Exceptions thrown by
         * a subsequently executed Valve (or a Filter or Servlet at the 
         * application level) will be passed on to our caller.
         *
         * If there are no more Valves to be executed, an appropriate
         * ServletException will be thrown by this ValveContext.
         *
         * @param request The request currently being processed
         * @param response The response currently being created
         *
         * @exception IOException if thrown by a subsequent Valve, Filter, or
         *  Servlet
         * @exception ServletException if thrown by a subsequent Valve, Filter,
         *  or Servlet
         * @exception ServletException if there are no further Valves 
         *  configured in the Pipeline currently being processed
         */
        public void invokeNext(Request request, Response response)
            throws IOException, ServletException {

            int subscript = stage;
            stage = stage + 1;

            // Invoke the requested Valve for the current request thread
            if (subscript < valves.length) {
                valves[subscript].invoke(request, response, this);
            } else if ((subscript == valves.length) && (basic != null)) {
                basic.invoke(request, response, this);
            } else {
                throw new ServletException
                    (sm.getString("standardPipeline.noValve"));
            }

        }


    }


}
