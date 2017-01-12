/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/ApplicationHttpResponse.java,v 1.8 2002/06/28 12:19:13 remm Exp $
 * $Revision: 1.8 $
 * $Date: 2002/06/28 12:19:13 $
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
import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.http.HttpServletResponse</code>
 * that transforms an application response object (which might be the original
 * one passed to a servlet, or might be based on the 2.3
 * <code>javax.servlet.http.HttpServletResponseWrapper</code> class)
 * back into an internal <code>org.apache.catalina.HttpResponse</code>.
 * <p>
 * <strong>WARNING</strong>:  Due to Java's lack of support for multiple
 * inheritance, all of the logic in <code>ApplicationResponse</code> is
 * duplicated in <code>ApplicationHttpResponse</code>.  Make sure that you
 * keep these two classes in synchronization when making changes!
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2002/06/28 12:19:13 $
 */

class ApplicationHttpResponse extends HttpServletResponseWrapper {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped response around the specified servlet response.
     *
     * @param response The servlet response being wrapped
     */
    public ApplicationHttpResponse(HttpServletResponse response) {

        this(response, false);

    }


    /**
     * Construct a new wrapped response around the specified servlet response.
     *
     * @param response The servlet response being wrapped
     * @param included <code>true</code> if this response is being processed
     *  by a <code>RequestDispatcher.include()</code> call
     */
    public ApplicationHttpResponse(HttpServletResponse response,
                                   boolean included) {

        super(response);
        setIncluded(included);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Is this wrapped response the subject of an <code>include()</code>
     * call?
     */
    protected boolean included = false;


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.core.ApplicationHttpResponse/1.0";


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------ ServletResponse Methods


    /**
     * Disallow <code>reset()</code> calls on a included response.
     *
     * @exception IllegalStateException if the response has already
     *  been committed
     */
    public void reset() {

        // If already committed, the wrapped response will throw ISE
        if (!included || getResponse().isCommitted())
            getResponse().reset();

    }


    /**
     * Disallow <code>setContentLength()</code> calls on an included response.
     *
     * @param len The new content length
     */
    public void setContentLength(int len) {

        if (!included)
            getResponse().setContentLength(len);

    }


    /**
     * Disallow <code>setContentType()</code> calls on an included response.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {

        if (!included)
            getResponse().setContentType(type);

    }


    /**
     * Disallow <code>setLocale()</code> calls on an included response.
     *
     * @param loc The new locale
     */
    public void setLocale(Locale loc) {

        if (!included)
            getResponse().setLocale(loc);

    }


    // -------------------------------------------- HttpServletResponse Methods


    /**
     * Disallow <code>addCookie()</code> calls on an included response.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {

        if (!included)
            ((HttpServletResponse) getResponse()).addCookie(cookie);

    }


    /**
     * Disallow <code>addDateHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addDateHeader(String name, long value) {

        if (!included)
            ((HttpServletResponse) getResponse()).addDateHeader(name, value);

    }


    /**
     * Disallow <code>addHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {

        if (!included)
            ((HttpServletResponse) getResponse()).addHeader(name, value);

    }


    /**
     * Disallow <code>addIntHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addIntHeader(String name, int value) {

        if (!included)
            ((HttpServletResponse) getResponse()).addIntHeader(name, value);

    }


    /**
     * Disallow <code>sendError()</code> calls on an included response.
     *
     * @param sc The new status code
     *
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int sc) throws IOException {

        if (!included)
            ((HttpServletResponse) getResponse()).sendError(sc);

    }


    /**
     * Disallow <code>sendError()</code> calls on an included response.
     *
     * @param sc The new status code
     * @param msg The new message
     *
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int sc, String msg) throws IOException {

        if (!included)
            ((HttpServletResponse) getResponse()).sendError(sc, msg);

    }


    /**
     * Disallow <code>sendRedirect()</code> calls on an included response.
     *
     * @param location The new location
     *
     * @exception IOException if an input/output error occurs
     */
    public void sendRedirect(String location) throws IOException {

        if (!included)
            ((HttpServletResponse) getResponse()).sendRedirect(location);

    }


    /**
     * Disallow <code>setDateHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void setDateHeader(String name, long value) {

        if (!included)
            ((HttpServletResponse) getResponse()).setDateHeader(name, value);

    }


    /**
     * Disallow <code>setHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void setHeader(String name, String value) {

        if (!included)
            ((HttpServletResponse) getResponse()).setHeader(name, value);

    }


    /**
     * Disallow <code>setIntHeader()</code> calls on an included response.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void setIntHeader(String name, int value) {

        if (!included)
            ((HttpServletResponse) getResponse()).setIntHeader(name, value);

    }


    /**
     * Disallow <code>setStatus()</code> calls on an included response.
     *
     * @param sc The new status code
     */
    public void setStatus(int sc) {

        if (!included)
            ((HttpServletResponse) getResponse()).setStatus(sc);

    }


    /**
     * Disallow <code>setStatus()</code> calls on an included response.
     *
     * @param sc The new status code
     * @param msg The new message
     */
    public void setStatus(int sc, String msg) {

        if (!included)
            ((HttpServletResponse) getResponse()).setStatus(sc, msg);

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return descriptive information about this implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the included flag for this response.
     */
    boolean isIncluded() {

        return (this.included);

    }


    /**
     * Set the included flag for this response.
     *
     * @param included The new included flag
     */
    void setIncluded(boolean included) {

        this.included = included;

    }


    /**
     * Set the response that we are wrapping.
     *
     * @param response The new wrapped response
     */
    void setResponse(HttpServletResponse response) {

        super.setResponse(response);

    }


}
