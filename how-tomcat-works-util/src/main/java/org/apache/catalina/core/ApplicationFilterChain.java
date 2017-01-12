/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/ApplicationFilterChain.java,v 1.11 2001/10/11 23:30:58 craigmcc Exp $
 * $Revision: 1.11 $
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


package org.apache.catalina.core;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.security.PrivilegedActionException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.util.InstanceSupport;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of <code>javax.servlet.FilterChain</code> used to manage
 * the execution of a set of filters for a particular request.  When the
 * set of defined filters has all been executed, the next call to
 * <code>doFilter()</code> will execute the servlet's <code>service()</code>
 * method itself.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.11 $ $Date: 2001/10/11 23:30:58 $
 */

final class ApplicationFilterChain implements FilterChain {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new chain instance with no defined filters.
     */
    public ApplicationFilterChain() {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of filters that will be executed on this chain.
     */
    private ArrayList filters = new ArrayList();


    /**
     * The iterator that is used to maintain the current position in the filter chain.
     * This iterator is called the first time that <code>doFilter()</code>
     * is called.
     */
    private Iterator iterator = null;


    /**
     * The servlet instance to be executed by this chain.
     */
    private Servlet servlet = null;


    /**
     * The string manager for our package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The InstanceSupport instance associated with our Wrapper (used to
     * send "before filter" and "after filter" events.
     */
    private InstanceSupport support = null;


    // ---------------------------------------------------- FilterChain Methods


    /**
     * Invoke the next filter in this chain, passing the specified request
     * and response.  If there are no more filters in this chain, invoke
     * the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {

        if( System.getSecurityManager() != null ) {
            final ServletRequest req = request;
            final ServletResponse res = response;
            try {
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction()
                    {
                        public Object run() throws ServletException, IOException {
                            internalDoFilter(req,res);
                            return null;
                        }
                    }
                );
            } catch( PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException)
                    throw (ServletException) e;
                else if (e instanceof IOException)
                    throw (IOException) e;
                else if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                else
                    throw new ServletException(e.getMessage(), e);
            }
        } else {
            internalDoFilter(request,response);
        }
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {

        // Construct an iterator the first time this method is called
        if (this.iterator == null)
            this.iterator = filters.iterator();

        // Call the next filter if there is one
        if (this.iterator.hasNext()) {
            ApplicationFilterConfig filterConfig =
              (ApplicationFilterConfig) iterator.next();
            Filter filter = null;
            try {
                filter = filterConfig.getFilter();
                support.fireInstanceEvent(InstanceEvent.BEFORE_FILTER_EVENT,
                                          filter, request, response);
                filter.doFilter(request, response, this);
                support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                          filter, request, response);
            } catch (IOException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (ServletException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (RuntimeException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (Throwable e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw new ServletException
                  (sm.getString("filterChain.filter"), e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance
        try {
            support.fireInstanceEvent(InstanceEvent.BEFORE_SERVICE_EVENT,
                                      servlet, request, response);
            if ((request instanceof HttpServletRequest) &&
                (response instanceof HttpServletResponse)) {
                servlet.service((HttpServletRequest) request,
                                (HttpServletResponse) response);
            } else {
                servlet.service(request, response);
            }
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response);
        } catch (IOException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (ServletException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (RuntimeException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (Throwable e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw new ServletException
              (sm.getString("filterChain.servlet"), e);
        }

    }


    // -------------------------------------------------------- Package Methods



    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {

        this.filters.add(filterConfig);

    }


    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {

        this.filters.clear();
        this.iterator = iterator;
        this.servlet = null;

    }


    /**
     * Set the servlet that will be executed at the end of this chain.
     *
     * @param wrapper The Wrapper for the servlet to be executed
     */
    void setServlet(Servlet servlet) {

        this.servlet = servlet;

    }


    /**
     * Set the InstanceSupport object used for event notifications
     * for this filter chain.
     *
     * @param support The InstanceSupport object for our Wrapper
     */
    void setSupport(InstanceSupport support) {

        this.support = support;

    }


}
