/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/MappingRequest.java,v 1.1 2002/06/28 01:35:34 remm Exp $
 * $Revision: 1.1 $
 * $Date: 2002/06/28 01:35:34 $
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


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;

/**
 * 
 *
 * @author Remy Maucherat
 * @version $Revision: 1.1 $ $Date: 2002/06/28 01:35:34 $
 */

public class MappingRequest
    implements HttpRequest, HttpServletRequest {

    public MappingRequest(String contextPath, String decodedURI,
                          String queryString) {
        this.contextPath = contextPath;
        this.decodedURI = decodedURI;
        this.queryString = queryString;
    }

    protected String contextPath = null;
    protected String decodedURI = null;
    protected String queryString = null;

    protected String pathInfo = null;
    protected String servletPath = null;
    protected Wrapper wrapper = null;

    public String getContextPath() {
        return (contextPath);
    }

    public ServletRequest getRequest() {
        return (this);
    }

    public String getDecodedRequestURI() {
        return decodedURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String query) {
        queryString = query;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String path) {
        pathInfo = path;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String path) {
        servletPath = path;
    }

    public Wrapper getWrapper() {
        return (this.wrapper);
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String getAuthorization() { return null; }
    public void setAuthorization(String authorization) {}
    public Connector getConnector() { return null; }
    public void setConnector(Connector connector) {}
    public Context getContext() { return null; }
    public void setContext(Context context) {}
    public String getInfo() { return null; }
    public Response getResponse() { return null; }
    public void setResponse(Response response) {}
    public Socket getSocket() { return null; }
    public void setSocket(Socket socket) {}
    public InputStream getStream() { return null; }
    public void setStream(InputStream input) {}
    public void addLocale(Locale locale) {}
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }
    public void finishRequest() throws IOException {}
    public Object getNote(String name) { return null; }
    public Iterator getNoteNames() { return null; }
    public void removeNote(String name) {}
    public void setContentType(String type) {}
    public void setNote(String name, Object value) {}
    public void setProtocol(String protocol) {}
    public void setRemoteAddr(String remoteAddr) {}
    public void setRemoteHost(String remoteHost) {}
    public void setScheme(String scheme) {}
    public void setServerName(String name) {}
    public void setServerPort(int port) {}
    public Object getAttribute(String name) { return null; }
    public Enumeration getAttributeNames() { return null; }
    public String getCharacterEncoding() { return null; }
    public int getContentLength() { return -1; }
    public void setContentLength(int length) {}
    public String getContentType() { return null; }
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }
    public Locale getLocale() { return null; }
    public Enumeration getLocales() { return null; }
    public String getProtocol() { return null; }
    public BufferedReader getReader() throws IOException { return null; }
    public String getRealPath(String path) { return null; }
    public String getRemoteAddr() { return null; }
    public String getRemoteHost() { return null; }
    public String getScheme() { return null; }
    public String getServerName() { return null; }
    public int getServerPort() { return -1; }
    public boolean isSecure() { return false; }
    public void removeAttribute(String name) {}
    public void setAttribute(String name, Object value) {}
    public void setCharacterEncoding(String enc)
        throws UnsupportedEncodingException {}
    public void addCookie(Cookie cookie) {}
    public void addHeader(String name, String value) {}
    public void addParameter(String name, String values[]) {}
    public void clearCookies() {}
    public void clearHeaders() {}
    public void clearLocales() {}
    public void clearParameters() {}
    public void recycle() {}
    public void setAuthType(String authType) {}
    public void setContextPath(String path) {}
    public void setMethod(String method) {}
    public void setRequestedSessionCookie(boolean flag) {}
    public void setRequestedSessionId(String id) {}
    public void setRequestedSessionURL(boolean flag) {}
    public void setRequestURI(String uri) {}
    public void setSecure(boolean secure) {}
    public void setUserPrincipal(Principal principal) {}
    public String getParameter(String name) { return null; }
    public Map getParameterMap() { return null; }
    public Enumeration getParameterNames() { return null; }
    public String[] getParameterValues(String name) { return null; }
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }
    public String getAuthType() { return null; }
    public Cookie[] getCookies() { return null; }
    public long getDateHeader(String name) { return -1; }
    public String getHeader(String name) { return null; }
    public Enumeration getHeaders(String name) { return null; }
    public Enumeration getHeaderNames() { return null; }
    public int getIntHeader(String name) { return -1; }
    public String getMethod() { return null; }
    public String getPathTranslated() { return null; }
    public String getRemoteUser() { return null; }
    public String getRequestedSessionId() { return null; }
    public String getRequestURI() { return null; }
    public void setDecodedRequestURI(String uri) {}
    public StringBuffer getRequestURL() { return null; }
    public HttpSession getSession() { return null; }
    public HttpSession getSession(boolean create) { return null; }
    public boolean isRequestedSessionIdFromCookie() { return false; }
    public boolean isRequestedSessionIdFromURL() { return false; }
    public boolean isRequestedSessionIdFromUrl() { return false; }
    public boolean isRequestedSessionIdValid() { return false; }
    public boolean isUserInRole(String role) { return false; }
    public Principal getUserPrincipal() { return null; }


}

