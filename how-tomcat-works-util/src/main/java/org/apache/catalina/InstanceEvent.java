/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/InstanceEvent.java,v 1.7 2001/10/11 23:30:58 craigmcc Exp $
 * $Revision: 1.7 $
 * $Date: 2001/10/11 23:30:58 $
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


import java.util.EventObject;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * General event for notifying listeners of significant events related to
 * a specific instance of a Servlet, or a specific instance of a Filter,
 * as opposed to the Wrapper component that manages it.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.7 $ $Date: 2001/10/11 23:30:58 $
 */

public final class InstanceEvent
    extends EventObject {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The event indicating that the <code>init()</code> method is about
     * to be called for this instance.
     */
    public static final String BEFORE_INIT_EVENT = "beforeInit";


    /**
     * The event indicating that the <code>init()</code> method has returned.
     */
    public static final String AFTER_INIT_EVENT = "afterInit";


    /**
     * The event indicating that the <code>service()</code> method is about
     * to be called on a servlet.  The <code>servlet</code> property contains
     * the servlet being called, and the <code>request</code> and
     * <code>response</code> properties contain the current request and
     * response being processed.
     */
    public static final String BEFORE_SERVICE_EVENT = "beforeService";


    /**
     * The event indicating that the <code>service()</code> method has
     * returned.  The <code>servlet</code> property contains the servlet
     * that was called, and the <code>request</code> and
     * <code>response</code> properties contain the current request and
     * response being processed.
     */
    public static final String AFTER_SERVICE_EVENT = "afterService";


    /**
     * The event indicating that the <code>destroy</code> method is about
     * to be called for this instance.
     */
    public static final String BEFORE_DESTROY_EVENT = "beforeDestroy";


    /**
     * The event indicating that the <code>destroy()</code> method has
     * returned.
     */
    public static final String AFTER_DESTROY_EVENT = "afterDestroy";


    /**
     * The event indicating that the <code>service()</code> method of a
     * servlet accessed via a request dispatcher is about to be called.
     * The <code>servlet</code> property contains a reference to the
     * dispatched-to servlet instance, and the <code>request</code> and
     * <code>response</code> properties contain the current request and
     * response being processed.  The <code>wrapper</code> property will
     * contain a reference to the dispatched-to Wrapper.
     */
    public static final String BEFORE_DISPATCH_EVENT = "beforeDispatch";


    /**
     * The event indicating that the <code>service()</code> method of a
     * servlet accessed via a request dispatcher has returned.  The
     * <code>servlet</code> property contains a reference to the
     * dispatched-to servlet instance, and the <code>request</code> and
     * <code>response</code> properties contain the current request and
     * response being processed.  The <code>wrapper</code> property will
     * contain a reference to the dispatched-to Wrapper.
     */
    public static final String AFTER_DISPATCH_EVENT = "afterDispatch";


    /**
     * The event indicating that the <code>doFilter()</code> method of a
     * Filter is about to be called.  The <code>filter</code> property
     * contains a reference to the relevant filter instance, and the
     * <code>request</code> and <code>response</code> properties contain
     * the current request and response being processed.
     */
    public static final String BEFORE_FILTER_EVENT = "beforeFilter";


    /**
     * The event indicating that the <code>doFilter()</code> method of a
     * Filter has returned.  The <code>filter</code> property contains
     * a reference to the relevant filter instance, and the
     * <code>request</code> and <code>response</code> properties contain
     * the current request and response being processed.
     */
    public static final String AFTER_FILTER_EVENT = "afterFilter";


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for filter lifecycle events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param filter Filter instance for which this event occurred
     * @param type Event type (required)
     */
    public InstanceEvent(Wrapper wrapper, Filter filter, String type) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = filter;
      this.servlet = null;
      this.type = type;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for filter lifecycle events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param filter Filter instance for which this event occurred
     * @param type Event type (required)
     * @param exception Exception that occurred
     */
    public InstanceEvent(Wrapper wrapper, Filter filter, String type,
                         Throwable exception) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = filter;
      this.servlet = null;
      this.type = type;
      this.exception = exception;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for filter processing events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param filter Filter instance for which this event occurred
     * @param type Event type (required)
     * @param request Servlet request we are processing
     * @param response Servlet response we are processing
     */
    public InstanceEvent(Wrapper wrapper, Filter filter, String type,
                         ServletRequest request, ServletResponse response) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = filter;
      this.servlet = null;
      this.type = type;
      this.request = request;
      this.response = response;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for filter processing events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param filter Filter instance for which this event occurred
     * @param type Event type (required)
     * @param request Servlet request we are processing
     * @param response Servlet response we are processing
     * @param exception Exception that occurred
     */
    public InstanceEvent(Wrapper wrapper, Filter filter, String type,
                         ServletRequest request, ServletResponse response,
                         Throwable exception) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = filter;
      this.servlet = null;
      this.type = type;
      this.request = request;
      this.response = response;
      this.exception = exception;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for processing servlet lifecycle events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param servlet Servlet instance for which this event occurred
     * @param type Event type (required)
     */
    public InstanceEvent(Wrapper wrapper, Servlet servlet, String type) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = null;
      this.servlet = servlet;
      this.type = type;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for processing servlet lifecycle events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param servlet Servlet instance for which this event occurred
     * @param type Event type (required)
     * @param exception Exception that occurred
     */
    public InstanceEvent(Wrapper wrapper, Servlet servlet, String type,
                         Throwable exception) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = null;
      this.servlet = servlet;
      this.type = type;
      this.exception = exception;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for processing servlet processing events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param servlet Servlet instance for which this event occurred
     * @param type Event type (required)
     * @param request Servlet request we are processing
     * @param response Servlet response we are processing
     */
    public InstanceEvent(Wrapper wrapper, Servlet servlet, String type,
                         ServletRequest request, ServletResponse response) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = null;
      this.servlet = servlet;
      this.type = type;
      this.request = request;
      this.response = response;

    }


    /**
     * Construct a new InstanceEvent with the specified parameters.  This
     * constructor is used for processing servlet processing events.
     *
     * @param wrapper Wrapper managing this servlet instance
     * @param servlet Servlet instance for which this event occurred
     * @param type Event type (required)
     * @param request Servlet request we are processing
     * @param response Servlet response we are processing
     * @param exception Exception that occurred
     */
    public InstanceEvent(Wrapper wrapper, Servlet servlet, String type,
                         ServletRequest request, ServletResponse response,
                         Throwable exception) {

      super(wrapper);
      this.wrapper = wrapper;
      this.filter = null;
      this.servlet = servlet;
      this.type = type;
      this.request = request;
      this.response = response;
      this.exception = exception;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The exception that was thrown during the processing being reported
     * by this event (AFTER_INIT_EVENT, AFTER_SERVICE_EVENT, 
     * AFTER_DESTROY_EVENT, AFTER_DISPATCH_EVENT, and AFTER_FILTER_EVENT only).
     */
    private Throwable exception = null;


    /**
     * The Filter instance for which this event occurred (BEFORE_FILTER_EVENT
     * and AFTER_FILTER_EVENT only).
     */
    private Filter filter = null;


    /**
     * The servlet request being processed (BEFORE_FILTER_EVENT,
     * AFTER_FILTER_EVENT, BEFORE_SERVICE_EVENT, and AFTER_SERVICE_EVENT).
     */
    private ServletRequest request = null;


    /**
     * The servlet response being processed (BEFORE_FILTER_EVENT,
     * AFTER_FILTER_EVENT, BEFORE_SERVICE_EVENT, and AFTER_SERVICE_EVENT).
     */
    private ServletResponse response = null;


    /**
     * The Servlet instance for which this event occurred (not present on
     * BEFORE_FILTER_EVENT or AFTER_FILTER_EVENT events).
     */
    private Servlet servlet = null;


    /**
     * The event type this instance represents.
     */
    private String type = null;


    /**
     * The Wrapper managing the servlet instance for which this event occurred.
     */
    private Wrapper wrapper = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the exception that occurred during the processing
     * that was reported by this event.
     */
    public Throwable getException() {

        return (this.exception);

    }


    /**
     * Return the filter instance for which this event occurred.
     */
    public Filter getFilter() {

        return (this.filter);

    }


    /**
     * Return the servlet request for which this event occurred.
     */
    public ServletRequest getRequest() {

        return (this.request);

    }


    /**
     * Return the servlet response for which this event occurred.
     */
    public ServletResponse getResponse() {

        return (this.response);

    }


    /**
     * Return the servlet instance for which this event occurred.
     */
    public Servlet getServlet() {

        return (this.servlet);

    }


    /**
     * Return the event type of this event.
     */
    public String getType() {

        return (this.type);

    }


    /**
     * Return the Wrapper managing the servlet instance for which this
     * event occurred.
     */
    public Wrapper getWrapper() {

        return (this.wrapper);

    }


}
