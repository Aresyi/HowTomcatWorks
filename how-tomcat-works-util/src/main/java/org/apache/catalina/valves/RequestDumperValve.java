/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/valves/RequestDumperValve.java,v 1.5 2002/02/19 22:11:26 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/02/19 22:11:26 $
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
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of a Valve that logs interesting contents from the
 * specified Request (before processing) and the corresponding Response
 * (after processing).  It is especially useful in debugging problems
 * related to headers and cookies.</p>
 *
 * <p>This Valve may be attached to any Container, depending on the granularity
 * of the logging you wish to perform.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/02/19 22:11:26 $
 */

public class RequestDumperValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.RequestDumperValve/1.0";


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
     * Log the interesting request parameters, invoke the next Valve in the
     * sequence, and log the interesting response parameters.
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

        // Skip logging for non-HTTP requests and responses
        if (!(request instanceof HttpRequest) ||
            !(response instanceof HttpResponse)) {
            context.invokeNext(request, response);
            return;
        }
        HttpRequest hrequest = (HttpRequest) request;
        HttpResponse hresponse = (HttpResponse) response;
        HttpServletRequest hreq =
            (HttpServletRequest) hrequest.getRequest();
        HttpServletResponse hres =
            (HttpServletResponse) hresponse.getResponse();

        // Log pre-service information
        log("REQUEST URI       =" + hreq.getRequestURI());
        log("          authType=" + hreq.getAuthType());
        log(" characterEncoding=" + hreq.getCharacterEncoding());
        log("     contentLength=" + hreq.getContentLength());
        log("       contentType=" + hreq.getContentType());
        log("       contextPath=" + hreq.getContextPath());
        Cookie cookies[] = hreq.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                log("            cookie=" + cookies[i].getName() + "=" +
                    cookies[i].getValue());
        }
        Enumeration hnames = hreq.getHeaderNames();
        while (hnames.hasMoreElements()) {
            String hname = (String) hnames.nextElement();
            Enumeration hvalues = hreq.getHeaders(hname);
            while (hvalues.hasMoreElements()) {
                String hvalue = (String) hvalues.nextElement();
                log("            header=" + hname + "=" + hvalue);
            }
        }
        log("            locale=" + hreq.getLocale());
        log("            method=" + hreq.getMethod());
        Enumeration pnames = hreq.getParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = (String) pnames.nextElement();
            String pvalues[] = hreq.getParameterValues(pname);
            StringBuffer result = new StringBuffer(pname);
            result.append('=');
            for (int i = 0; i < pvalues.length; i++) {
                if (i > 0)
                    result.append(", ");
                result.append(pvalues[i]);
            }
            log("         parameter=" + result.toString());
        }
        log("          pathInfo=" + hreq.getPathInfo());
        log("          protocol=" + hreq.getProtocol());
        log("       queryString=" + hreq.getQueryString());
        log("        remoteAddr=" + hreq.getRemoteAddr());
        log("        remoteHost=" + hreq.getRemoteHost());
        log("        remoteUser=" + hreq.getRemoteUser());
        log("requestedSessionId=" + hreq.getRequestedSessionId());
        log("            scheme=" + hreq.getScheme());
        log("        serverName=" + hreq.getServerName());
        log("        serverPort=" + hreq.getServerPort());
        log("       servletPath=" + hreq.getServletPath());
        log("          isSecure=" + hreq.isSecure());
        log("---------------------------------------------------------------");

        // Perform the request
        context.invokeNext(request, response);

        // Log post-service information
        log("---------------------------------------------------------------");
        log("          authType=" + hreq.getAuthType());
        log("     contentLength=" + hresponse.getContentLength());
        log("       contentType=" + hresponse.getContentType());
        Cookie rcookies[] = hresponse.getCookies();
        for (int i = 0; i < rcookies.length; i++) {
            log("            cookie=" + rcookies[i].getName() + "=" +
                rcookies[i].getValue() + "; domain=" +
                rcookies[i].getDomain() + "; path=" + rcookies[i].getPath());
        }
        String rhnames[] = hresponse.getHeaderNames();
        for (int i = 0; i < rhnames.length; i++) {
            String rhvalues[] = hresponse.getHeaderValues(rhnames[i]);
            for (int j = 0; j < rhvalues.length; j++)
                log("            header=" + rhnames[i] + "=" + rhvalues[j]);
        }
        log("           message=" + hresponse.getMessage());
        log("        remoteUser=" + hreq.getRemoteUser());
        log("            status=" + hresponse.getStatus());
        log("===============================================================");

    }


    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("RequestDumperValve[");
        if (container != null)
            sb.append(container.getName());
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


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
