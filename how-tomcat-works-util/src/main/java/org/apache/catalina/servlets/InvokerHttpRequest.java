/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/servlets/InvokerHttpRequest.java,v 1.5 2002/05/23 07:13:45 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/23 07:13:45 $
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


package org.apache.catalina.servlets;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.http.HttpServletRequest</code>
 * utilized when <code>InvokerServlet</code> processes the initial request
 * for an invoked servlet.  Subsequent requests will be mapped directly
 * to the servlet, because a new servlet mapping will have been created.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/05/23 07:13:45 $
 */

class InvokerHttpRequest extends HttpServletRequestWrapper {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped request around the specified servlet request.
     *
     * @param request The servlet request being wrapped
     */
    public InvokerHttpRequest(HttpServletRequest request) {

        super(request);
        this.pathInfo = request.getPathInfo();
        this.pathTranslated = request.getPathTranslated();
        this.requestURI = request.getRequestURI();
        this.servletPath = request.getServletPath();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.servlets.InvokerHttpRequest/1.0";


    /**
     * The path information for this request.
     */
    protected String pathInfo = null;


    /**
     * The translated path information for this request.
     */
    protected String pathTranslated = null;


    /**
     * The request URI for this request.
     */
    protected String requestURI = null;


    /**
     * The servlet path for this request.
     */
    protected String servletPath = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Override the <code>getPathInfo()</code> method of the wrapped request.
     */
    public String getPathInfo() {

        return (this.pathInfo);

    }


    /**
     * Override the <code>getPathTranslated()</code> method of the
     * wrapped request.
     */
    public String getPathTranslated() {

        return (this.pathTranslated);

    }


    /**
     * Override the <code>getRequestURI()</code> method of the wrapped request.
     */
    public String getRequestURI() {

        return (this.requestURI);

    }


    /**
     * Override the <code>getServletPath()</code> method of the wrapped
     * request.
     */
    public String getServletPath() {

        return (this.servletPath);

    }


    // -------------------------------------------------------- Package Methods



    /**
     * Return descriptive information about this implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Set the path information for this request.
     *
     * @param pathInfo The new path info
     */
    void setPathInfo(String pathInfo) {

        this.pathInfo = pathInfo;

    }


    /**
     * Set the translated path info for this request.
     *
     * @param pathTranslated The new translated path info
     */
    void setPathTranslated(String pathTranslated) {

        this.pathTranslated = pathTranslated;

    }


    /**
     * Set the request URI for this request.
     *
     * @param requestURI The new request URI
     */
    void setRequestURI(String requestURI) {

        this.requestURI = requestURI;

    }


    /**
     * Set the servlet path for this request.
     *
     * @param servletPath The new servlet path
     */
    void setServletPath(String servletPath) {

        this.servletPath = servletPath;

    }


}
