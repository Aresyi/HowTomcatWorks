/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/ApplicationHttpRequest.java,v 1.12 2002/05/23 07:13:45 remm Exp $
 * $Revision: 1.12 $
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


package org.apache.catalina.core;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.Globals;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.http.HttpServletRequest</code>
 * that transforms an application request object (which might be the original
 * one passed to a servlet, or might be based on the 2.3
 * <code>javax.servlet.http.HttpServletRequestWrapper</code> class)
 * back into an internal <code>org.apache.catalina.HttpRequest</code>.
 * <p>
 * <strong>WARNING</strong>:  Due to Java's lack of support for multiple
 * inheritance, all of the logic in <code>ApplicationRequest</code> is
 * duplicated in <code>ApplicationHttpRequest</code>.  Make sure that you
 * keep these two classes in synchronization when making changes!
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2002/05/23 07:13:45 $
 */

class ApplicationHttpRequest extends HttpServletRequestWrapper {


    // ------------------------------------------------------- Static Variables


    /**
     * The set of attribute names that are special for request dispatchers.
     */
    protected static final String specials[] =
    { Globals.REQUEST_URI_ATTR, Globals.CONTEXT_PATH_ATTR,
      Globals.SERVLET_PATH_ATTR, Globals.PATH_INFO_ATTR,
      Globals.QUERY_STRING_ATTR };


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped request around the specified servlet request.
     *
     * @param request The servlet request being wrapped
     */
    public ApplicationHttpRequest(HttpServletRequest request) {

        super(request);
        setRequest(request);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The request attributes for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The context path for this request.
     */
    protected String contextPath = null;


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.core.ApplicationHttpRequest/1.0";


    /**
     * The request parameters for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected Map parameters = new HashMap();


    /**
     * The path information for this request.
     */
    protected String pathInfo = null;


    /**
     * The query string for this request.
     */
    protected String queryString = null;


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


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Override the <code>getAttribute()</code> method of the wrapped request.
     *
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    public Enumeration getAttributeNames() {

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Override the <code>removeAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
            if (!isSpecial(name))
                getRequest().removeAttribute(name);
        }

    }


    /**
     * Override the <code>setAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {

        synchronized (attributes) {
            attributes.put(name, value);
            if (!isSpecial(name))
                getRequest().setAttribute(name, value);
        }

    }


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Override the <code>getContextPath()</code> method of the wrapped
     * request.
     */
    public String getContextPath() {

        return (this.contextPath);

    }


    /**
     * Override the <code>getParameter()</code> method of the wrapped request.
     *
     * @param name Name of the requested parameter
     */
    public String getParameter(String name) {

        synchronized (parameters) {
            Object value = parameters.get(name);
            if (value == null)
                return (null);
            else if (value instanceof String[])
                return (((String[]) value)[0]);
            else if (value instanceof String)
                return ((String) value);
            else
                return (value.toString());
        }

    }


    /**
     * Override the <code>getParameterMap()</code> method of the
     * wrapped request.
     */
    public Map getParameterMap() {

        return (parameters);

    }


    /**
     * Override the <code>getParameterNames()</code> method of the
     * wrapped request.
     */
    public Enumeration getParameterNames() {

        synchronized (parameters) {
            return (new Enumerator(parameters.keySet()));
        }

    }


    /**
     * Override the <code>getParameterValues()</code> method of the
     * wrapped request.
     *
     * @param name Name of the requested parameter
     */
    public String[] getParameterValues(String name) {

        synchronized (parameters) {
            Object value = parameters.get(name);
            if (value == null)
                return ((String[]) null);
            else if (value instanceof String[])
                return ((String[]) value);
            else if (value instanceof String) {
                String values[] = new String[1];
                values[0] = (String) value;
                return (values);
            } else {
                String values[] = new String[1];
                values[0] = value.toString();
                return (values);
            }
        }

    }


    /**
     * Override the <code>getPathInfo()</code> method of the wrapped request.
     */
    public String getPathInfo() {

        return (this.pathInfo);

    }


    /**
     * Override the <code>getQueryString()</code> method of the wrapped
     * request.
     */
    public String getQueryString() {

        return (this.queryString);

    }


    /**
     * Override the <code>getRequestURI()</code> method of the wrapped
     * request.
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
     * Perform a shallow copy of the specified Map, and return the result.
     *
     * @param orig Origin Map to be copied
     */
    Map copyMap(Map orig) {

        if (orig == null)
            return (new HashMap());
        HashMap dest = new HashMap();
        synchronized (orig) {
            Iterator keys = orig.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                dest.put(key, orig.get(key));
            }
        }
        return (dest);

    }


    /**
     * Merge the parameters from the specified query string (if any), and
     * the parameters already present on this request (if any), such that
     * the parameter values from the query string show up first if there are
     * duplicate parameter names.
     *
     * @param queryString The query string containing parameters to be merged
     */
    void mergeParameters(String queryString) {

        if ((queryString == null) || (queryString.length() < 1))
            return;

        HashMap queryParameters = new HashMap();
        String encoding = getCharacterEncoding();
        if (encoding == null)
            encoding = "ISO-8859-1";
        try {
            RequestUtil.parseParameters
                (queryParameters, queryString, encoding);
        } catch (Exception e) {
            ;
        }
        synchronized (parameters) {
            Iterator keys = parameters.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = queryParameters.get(key);
                if (value == null) {
                    queryParameters.put(key, parameters.get(key));
                    continue;
                }
                queryParameters.put
                    (key, mergeValues(value, parameters.get(key)));
            }
            parameters = queryParameters;
        }

    }


    /**
     * Set the context path for this request.
     *
     * @param contextPath The new context path
     */
    void setContextPath(String contextPath) {

        this.contextPath = contextPath;

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
     * Set the query string for this request.
     *
     * @param queryString The new query string
     */
    void setQueryString(String queryString) {

        this.queryString = queryString;

    }


    /**
     * Set the request that we are wrapping.
     *
     * @param request The new wrapped request
     */
    void setRequest(HttpServletRequest request) {

        super.setRequest(request);

        // Initialize the attributes for this request
        synchronized (attributes) {
            attributes.clear();
            Enumeration names = request.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if( ! ( Globals.REQUEST_URI_ATTR.equals(name) ||
                        Globals.SERVLET_PATH_ATTR.equals(name) ) ) {
                    Object value = request.getAttribute(name);
                    attributes.put(name, value);
                }
            }
        }

        // Initialize the parameters for this request
        synchronized (parameters) {
            parameters = copyMap(request.getParameterMap());
        }

        // Initialize the path elements for this request
        contextPath = request.getContextPath();
        pathInfo = request.getPathInfo();
        queryString = request.getQueryString();
        requestURI = request.getRequestURI();
        servletPath = request.getServletPath();

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


    // ------------------------------------------------------ Protected Methods


    /**
     * Is this attribute name one of the special ones that is added only for
     * included servlets?
     *
     * @param name Attribute name to be tested
     */
    protected boolean isSpecial(String name) {

        for (int i = 0; i < specials.length; i++) {
            if (specials[i].equals(name))
                return (true);
        }
        return (false);

    }


    /**
     * Merge the two sets of parameter values into a single String array.
     *
     * @param values1 First set of values
     * @param values2 Second set of values
     */
    protected String[] mergeValues(Object values1, Object values2) {

        ArrayList results = new ArrayList();

        if (values1 == null)
            ;
        else if (values1 instanceof String)
            results.add(values1);
        else if (values1 instanceof String[]) {
            String values[] = (String[]) values1;
            for (int i = 0; i < values.length; i++)
                results.add(values[i]);
        } else
            results.add(values1.toString());

        if (values2 == null)
            ;
        else if (values2 instanceof String)
            results.add(values2);
        else if (values2 instanceof String[]) {
            String values[] = (String[]) values2;
            for (int i = 0; i < values.length; i++)
                results.add(values[i]);
        } else
            results.add(values2.toString());

        String values[] = new String[results.size()];
        return ((String[]) results.toArray(values));

    }


}
