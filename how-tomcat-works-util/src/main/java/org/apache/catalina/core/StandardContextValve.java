/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardContextValve.java,v 1.16 2002/03/14 20:58:24 remm Exp $
 * $Revision: 1.16 $
 * $Date: 2002/03/14 20:58:24 $
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardContext</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.16 $ $Date: 2002/03/14 20:58:24 $
 */

final class StandardContextValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardContextValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
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
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public void invoke(Request request, Response response,
                       ValveContext valveContext)
        throws IOException, ServletException {

        // Validate the request and response object types
        if (!(request.getRequest() instanceof HttpServletRequest) ||
            !(response.getResponse() instanceof HttpServletResponse)) {
            return;     // NOTE - Not much else we can do generically
        }

        // Disallow any direct access to resources under WEB-INF or META-INF
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        String contextPath = hreq.getContextPath();
        String requestURI = ((HttpRequest) request).getDecodedRequestURI();
        String relativeURI =
            requestURI.substring(contextPath.length()).toUpperCase();
        if (relativeURI.equals("/META-INF") ||
            relativeURI.equals("/WEB-INF") ||
            relativeURI.startsWith("/META-INF/") ||
            relativeURI.startsWith("/WEB-INF/")) {
            notFound(requestURI, (HttpServletResponse) response.getResponse());
            return;
        }

        Context context = (Context) getContainer();

        // Select the Wrapper to be used for this Request
        Wrapper wrapper = null;
        try {
            wrapper = (Wrapper) context.map(request, true);
        } catch (IllegalArgumentException e) {
            badRequest(requestURI, 
                       (HttpServletResponse) response.getResponse());
            return;
        }
        if (wrapper == null) {
            notFound(requestURI, (HttpServletResponse) response.getResponse());
            return;
        }

        // Ask this Wrapper to process this Request
        response.setContext(context);

        wrapper.invoke(request, response);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Report a "bad request" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param requestURI The request URI for the requested resource
     * @param response The response we are creating
     */
    private void badRequest(String requestURI, HttpServletResponse response) {

        try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, requestURI);
        } catch (IllegalStateException e) {
            ;
        } catch (IOException e) {
            ;
        }

    }

    /**
     * Report a "not found" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param requestURI The request URI for the requested resource
     * @param response The response we are creating
     */
    private void notFound(String requestURI, HttpServletResponse response) {

        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, requestURI);
        } catch (IllegalStateException e) {
            ;
        } catch (IOException e) {
            ;
        }

    }

}
