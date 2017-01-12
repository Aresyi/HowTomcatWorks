/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/authenticator/SavedRequest.java,v 1.4 2001/07/22 20:09:19 pier Exp $
 * $Revision: 1.4 $
 * $Date: 2001/07/22 20:09:19 $
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


package org.apache.catalina.authenticator;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ArrayList;
import javax.servlet.http.Cookie;


/**
 * Object that saves the critical information from a request so that
 * form-based authentication can reproduce it once the user has been
 * authenticated.
 * <p>
 * <b>IMPLEMENTATION NOTE</b> - It is assumed that this object is accessed
 * only from the context of a single thread, so no synchronization around
 * internal collection classes is performed.
 * <p>
 * <b>FIXME</b> - Currently, this object has no mechanism to save or
 * restore the data content of the request, although it does save
 * request parameters so that a POST transaction can be faithfully
 * duplicated.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2001/07/22 20:09:19 $
 */

public final class SavedRequest {


    /**
     * The set of Cookies associated with this Request.
     */
    private ArrayList cookies = new ArrayList();

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Iterator getCookies() {
        return (cookies.iterator());
    }


    /**
     * The set of Headers associated with this Request.  Each key is a header
     * name, while the value is a ArrayList containing one or more actual
     * values for this header.  The values are returned as an Iterator when
     * you ask for them.
     */
    private HashMap headers = new HashMap();

    public void addHeader(String name, String value) {
        ArrayList values = (ArrayList) headers.get(name);
        if (values == null) {
            values = new ArrayList();
            headers.put(name, values);
        }
        values.add(value);
    }

    public Iterator getHeaderNames() {
        return (headers.keySet().iterator());
    }

    public Iterator getHeaderValues(String name) {
        ArrayList values = (ArrayList) headers.get(name);
        if (values == null)
            return ((new ArrayList()).iterator());
        else
            return (values.iterator());
    }


    /**
     * The set of Locales associated with this Request.
     */
    private ArrayList locales = new ArrayList();

    public void addLocale(Locale locale) {
        locales.add(locale);
    }

    public Iterator getLocales() {
        return (locales.iterator());
    }


    /**
     * The request method used on this Request.
     */
    private String method = null;

    public String getMethod() {
        return (this.method);
    }

    public void setMethod(String method) {
        this.method = method;
    }



    /**
     * The set of request parameters associated with this Request.  Each
     * entry is keyed by the parameter name, pointing at a String array of
     * the corresponding values.
     */
    private HashMap parameters = new HashMap();

    public void addParameter(String name, String values[]) {
        parameters.put(name, values);
    }

    public Iterator getParameterNames() {
        return (parameters.keySet().iterator());
    }

    public String[] getParameterValues(String name) {
        return ((String[]) parameters.get(name));
    }


    /**
     * The query string associated with this Request.
     */
    private String queryString = null;

    public String getQueryString() {
        return (this.queryString);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    /**
     * The request URI associated with this Request.
     */
    private String requestURI = null;

    public String getRequestURI() {
        return (this.requestURI);
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }


}
