/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/RequestWrapper.java,v 1.4 2002/03/18 07:15:39 remm Exp $
 * $Revision: 1.4 $
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


import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>Request</b>
 * object.  By default, all methods are delegated to the wrapped request,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

public abstract class RequestWrapper implements Request {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     */
    public RequestWrapper(Request request) {

        super();
        this.request = request;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The wrapped request.
     */
    protected Request request = null;


    /**
     * Return the wrapped request.
     */
    public Request getWrappedRequest() {

        return (this.request);

    }


    // ------------------------------------------------------------- Properties


    /**
     * Return the authorization credentials sent with this request.
     */
    public String getAuthorization() {

        return (request.getAuthorization());

    }


    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization The new authorization credentials
     */
    public void setAuthorization(String authorization) {

        request.setAuthorization(authorization);

    }


    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {

        return (request.getConnector());

    }


    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {

        request.setConnector(connector);

    }


    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {

        return (request.getContext());

    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {

        request.setContext(context);

    }


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (request.getInfo());

    }


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.
     */
    public ServletRequest getRequest() {

        return (request.getRequest());

    }


    /**
     * Return the Response with which this Request is associated.
     */
    public Response getResponse() {

        return (request.getResponse());

    }


    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(Response response) {

        request.setResponse(response);

    }


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    public Socket getSocket() {

        return (request.getSocket());

    }


    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    public void setSocket(Socket socket) {

        request.setSocket(socket);

    }


    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream() {

        return (request.getStream());

    }


    /**
     * Set the input stream associated with this Request.
     *
     * @param stream The new input stream
     */
    public void setStream(InputStream stream) {

        request.setStream(stream);

    }


    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {

        return (request.getWrapper());

    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     *
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {

        request.setWrapper(wrapper);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException {

        return (request.createInputStream());

    }


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException {

        request.finishRequest();

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        request.recycle();

    }


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {

        request.setContentLength(length);

    }


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {

        request.setContentType(type);

    }


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    public void setProtocol(String protocol) {

        request.setProtocol(protocol);

    }


    /**
     * Set the remote IP address associated with this Request.  NOTE:  This
     * value will be used to resolve the value for <code>getRemoteHost()</code>
     * if that method is called.
     *
     * @param remote The remote IP address
     */
    public void setRemoteAddr(String remote) {

        request.setRemoteAddr(remote);

    }


    /**
     * Set the name of the scheme associated with this request.  Typical values
     * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
     *
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {

        request.setScheme(scheme);

    }


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    public void setSecure(boolean secure) {

        request.setSecure(secure);

    }


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    public void setServerName(String name) {

        request.setServerName(name);

    }


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    public void setServerPort(int port) {

        request.setServerPort(port);

    }


}
