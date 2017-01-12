/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/HttpRequestWrapper.java,v 1.3 2002/03/18 07:15:39 remm Exp $
 * $Revision: 1.3 $
 * $Date: 2002/03/18 07:15:39 $
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


package org.apache.catalina.connector;


import java.security.Principal;
import java.util.Locale;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpRequest;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>HttpRequest</b>
 * object.  By default, all methods are delegated to the wrapped request,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

public abstract class HttpRequestWrapper
    extends RequestWrapper
    implements HttpRequest {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     */
    public HttpRequestWrapper(HttpRequest request) {

        super(request);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {

        ((HttpRequest) request).addCookie(cookie);

    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {

        ((HttpRequest) request).addHeader(name, value);

    }


    /**
     * Add a Locale to the set of preferred Locales for this Request.  The
     * first added Locale will be the first one returned by getLocales().
     *
     * @param locale The new preferred Locale
     */
    public void addLocale(Locale locale) {

        ((HttpRequest) request).addLocale(locale);

    }


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies() {

        ((HttpRequest) request).clearCookies();

    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {

        ((HttpRequest) request).clearHeaders();

    }


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales() {

        ((HttpRequest) request).clearLocales();

    }


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param type The authentication type used
     */
    public void setAuthType(String type) {

        ((HttpRequest) request).setAuthType(type);

    }


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path) {

        ((HttpRequest) request).setContextPath(path);

    }


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method) {

        ((HttpRequest) request).setMethod(method);

    }


    /**
     * Set the query string for this Request.  This will normally be called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param query The query string
     */
    public void setQueryString(String query) {

        ((HttpRequest) request).setQueryString(query);

    }


    /**
     * Set the path information for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The path information
     */
    public void setPathInfo(String path) {

        ((HttpRequest) request).setPathInfo(path);

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {

        ((HttpRequest) request).setRequestedSessionCookie(flag);

    }


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {

        ((HttpRequest) request).setRequestedSessionId(id);

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {

        ((HttpRequest) request).setRequestedSessionURL(flag);

    }


    /**
     * Set the unparsed request URI for this Request.  This will normally be
     * called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri) {

        ((HttpRequest) request).setRequestURI(uri);

    }


    /**
     * Set the servlet path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The servlet path
     */
    public void setServletPath(String path) {

        ((HttpRequest) request).setServletPath(path);

    }


    /**
     * Set the Principal who has been authenticated for this Request.  This
     * value is also used to calculate the value to be returned by the
     * <code>getRemoteUser()</code> method.
     *
     * @param principal The user Principal
     */
    public void setUserPrincipal(Principal principal) {

        ((HttpRequest) request).setUserPrincipal(principal);

    }


}
