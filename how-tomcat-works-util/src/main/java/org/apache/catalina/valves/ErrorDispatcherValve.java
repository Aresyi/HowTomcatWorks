/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/valves/ErrorDispatcherValve.java,v 1.9 2002/08/15 23:51:14 patrickl Exp $
 * $Revision: 1.9 $
 * $Date: 2002/08/15 23:51:14 $
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


package org.apache.catalina.valves;


import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of a Valve that handles the error dispatch (that is, will
 * forward to the appropriate error page if necessary).</p>
 *
 * <p>This Valve should be attached at the Host level, although it will work
 * if attached to a Context.</p>
 *
 * <p><b>WARNING</b>: This valve is necessary for Servlet API compliance.</p>
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.9 $ $Date: 2002/08/15 23:51:14 $
 */

public class ErrorDispatcherValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The descriptive information related to this implementation.
     */
    protected static final String info =
        "org.apache.catalina.valves.ErrorDispatcherValve/1.0";


    /**
     * The StringManager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the next Valve in the sequence. When the invoke returns, check 
     * the response state, and output an error report is necessary.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response,
                       ValveContext context)
        throws IOException, ServletException {

        // Perform the request
        context.invokeNext(request, response);

        response.setSuspended(false);

        ServletRequest sreq = request.getRequest();
        Throwable t = (Throwable) sreq.getAttribute(Globals.EXCEPTION_ATTR);

        if (t != null) {
            throwable(request, response, t);
        } else {
            status(request, response);
        }

    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ErrorDispatcherValve[");
        sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Handle the specified Throwable encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    protected void throwable(Request request, Response response,
                             Throwable throwable) {
        Context context = request.getContext();
        if (context == null)
            return;
        
        Throwable realError = throwable;
        
        if (realError instanceof ServletException) {
            realError = ((ServletException) realError).getRootCause();
            if (realError == null) {
                realError = throwable;
            }
        } 
            
        ErrorPage errorPage = findErrorPage(context, realError);

        if (errorPage != null) {
            response.setAppCommitted(false);
            ServletRequest sreq = request.getRequest();
            ServletResponse sresp = response.getResponse();
            sreq.setAttribute
                (Globals.STATUS_CODE_ATTR,
                 new Integer(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            sreq.setAttribute(Globals.ERROR_MESSAGE_ATTR,
                              throwable.getMessage());
            sreq.setAttribute(Globals.EXCEPTION_ATTR,
                              realError);
            Wrapper wrapper = request.getWrapper();
            if (wrapper != null)
                sreq.setAttribute(Globals.SERVLET_NAME_ATTR,
                                  wrapper.getName());
            if (sreq instanceof HttpServletRequest)
                sreq.setAttribute(Globals.EXCEPTION_PAGE_ATTR,
                                  ((HttpServletRequest) sreq).getRequestURI());
            sreq.setAttribute(Globals.EXCEPTION_TYPE_ATTR,
                              realError.getClass());
            if (custom(request, response, errorPage)) {
                try {
                    sresp.flushBuffer();
                } catch (IOException e) {
                    log("Exception Processing " + errorPage, e);
                }
            }
        }

    }


    /**
     * Handle the HTTP status code (and corresponding message) generated
     * while processing the specified Request to produce the specified
     * Response.  Any exceptions that occur during generation of the error
     * report are logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     */
    protected void status(Request request, Response response) {

        // Do nothing on non-HTTP responses
        if (!(response instanceof HttpResponse))
            return;
        HttpResponse hresponse = (HttpResponse) response;
        if (!(response.getResponse() instanceof HttpServletResponse))
            return;
        int statusCode = hresponse.getStatus();
        String message = RequestUtil.filter(hresponse.getMessage());
        if (message == null)
            message = "";

        // Handle a custom error page for this status code
        Context context = request.getContext();
        if (context == null)
            return;

        ErrorPage errorPage = context.findErrorPage(statusCode);
        if (errorPage != null) {
            response.setAppCommitted(false);
            ServletRequest sreq = request.getRequest();
            ServletResponse sresp = response.getResponse();
            sreq.setAttribute(Globals.STATUS_CODE_ATTR,
                              new Integer(statusCode));
            sreq.setAttribute(Globals.ERROR_MESSAGE_ATTR,
                              message);
            Wrapper wrapper = request.getWrapper();
            if (wrapper != null)
                sreq.setAttribute(Globals.SERVLET_NAME_ATTR,
                                  wrapper.getName());
            if (sreq instanceof HttpServletRequest)
                sreq.setAttribute(Globals.EXCEPTION_PAGE_ATTR,
                                  ((HttpServletRequest) sreq).getRequestURI());
            if (custom(request, response, errorPage)) {
                try {
                    sresp.flushBuffer();
                } catch (IOException e) {
                    log("Exception Processing " + errorPage, e);
                }
            }
        }

    }


    /**
     * Find and return the ErrorPage instance for the specified exception's
     * class, or an ErrorPage instance for the closest superclass for which
     * there is such a definition.  If no associated ErrorPage instance is
     * found, return <code>null</code>.
     *
     * @param context The Context in which to search
     * @param exception The exception for which to find an ErrorPage
     */
    protected static ErrorPage findErrorPage
        (Context context, Throwable exception) {

        if (exception == null)
            return (null);
        Class clazz = exception.getClass();
        String name = clazz.getName();
        while (!"java.lang.Object".equals(clazz)) {
            ErrorPage errorPage = context.findErrorPage(name);
            if (errorPage != null)
                return (errorPage);
            clazz = clazz.getSuperclass();
            if (clazz == null)
                break;
            name = clazz.getName();
        }
        return (null);

    }


    /**
     * Handle an HTTP status code or Java exception by forwarding control
     * to the location included in the specified errorPage object.  It is
     * assumed that the caller has already recorded any request attributes
     * that are to be forwarded to this page.  Return <code>true</code> if
     * we successfully utilized the specified error page location, or
     * <code>false</code> if the default error report should be rendered.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param errorPage The errorPage directive we are obeying
     */
    protected boolean custom(Request request, Response response,
                             ErrorPage errorPage) {

        if (debug >= 1)
            log("Processing " + errorPage);

        // Validate our current environment
        if (!(request instanceof HttpRequest)) {
            if (debug >= 1)
                log(" Not processing an HTTP request --> default handling");
            return (false);     // NOTE - Nothing we can do generically
        }
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        if (!(response instanceof HttpResponse)) {
            if (debug >= 1)
                log("Not processing an HTTP response --> default handling");
            return (false);     // NOTE - Nothing we can do generically
        }
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();

        try {

            // Reset the response if possible (else IllegalStateException)
            hres.reset();

            // Forward control to the specified location
            ServletContext servletContext =
                request.getContext().getServletContext();
            RequestDispatcher rd =
                servletContext.getRequestDispatcher(errorPage.getLocation());
            rd.forward(hreq, hres);

            // If we forward, the response is suspended again
            response.setSuspended(false);

            // Indicate that we have successfully processed this custom page
            return (true);

        } catch (Throwable t) {

            // Report our failure to process this custom page
            log("Exception Processing " + errorPage, t);
            return (false);

        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message);
        else
            System.out.println(this.toString() + ": " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message, throwable);
        else {
            System.out.println(this.toString() + ": " + message);
            throwable.printStackTrace(System.out);
        }

    }


}
