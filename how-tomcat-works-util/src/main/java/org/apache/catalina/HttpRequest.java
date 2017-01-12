/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/HttpRequest.java,v 1.5 2002/03/14 20:57:20 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/03/14 20:57:20 $
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


import java.security.Principal;
import java.util.Locale;
import javax.servlet.http.Cookie;


/**
 * An <b>HttpRequest</b> is the Catalina internal facade for an
 * <code>HttpServletRequest</code> that is to be processed, in order to
 * produce the corresponding <code>HttpResponse</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/03/14 20:57:20 $
 */

public interface HttpRequest extends Request {


    // --------------------------------------------------------- Public Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie);


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value);


    /**
     * Add a Locale to the set of preferred Locales for this Request.  The
     * first added Locale will be the first one returned by getLocales().
     *
     * @param locale The new preferred Locale
     */
    public void addLocale(Locale locale);


    /**
     * Add a parameter name and corresponding set of values to this Request.
     * (This is used when restoring the original request on a form based
     * login).
     *
     * @param name Name of this request parameter
     * @param values Corresponding values for this request parameter
     */
    public void addParameter(String name, String values[]);


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies();


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders();


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales();


    /**
     * Clear the collection of parameters associated with this Request.
     */
    public void clearParameters();


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param type The authentication type used
     */
    public void setAuthType(String type);


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path);


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method);


    /**
     * Set the query string for this Request.  This will normally be called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param query The query string
     */
    public void setQueryString(String query);


    /**
     * Set the path information for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The path information
     */
    public void setPathInfo(String path);


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag);


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id);


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag);


    /**
     * Set the unparsed request URI for this Request.  This will normally be
     * called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri);


    /**
     * Set the decoded request URI.
     * 
     * @param uri The decoded request URI
     */
    public void setDecodedRequestURI(String uri);


    /**
     * Get the decoded request URI.
     * 
     * @return the URL decoded request URI
     */
    public String getDecodedRequestURI();


    /**
     * Set the servlet path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The servlet path
     */
    public void setServletPath(String path);


    /**
     * Set the Principal who has been authenticated for this Request.  This
     * value is also used to calculate the value to be returned by the
     * <code>getRemoteUser()</code> method.
     *
     * @param principal The user Principal
     */
    public void setUserPrincipal(Principal principal);


}
