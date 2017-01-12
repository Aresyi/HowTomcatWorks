/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Request.java,v 1.5 2001/08/01 03:04:04 craigmcc Exp $
 * $Revision: 1.5 $
 * $Date: 2001/08/01 03:04:04 $
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


import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;


/**
 * A <b>Request</b> is the Catalina-internal facade for a
 * <code>ServletRequest</code> that is to be processed, in order to
 * produce the corresponding <code>Response</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/08/01 03:04:04 $
 */

public interface Request {


    // ------------------------------------------------------------- Properties


    /**
     * Return the authorization credentials sent with this request.
     */
    public String getAuthorization();


    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization The new authorization credentials
     */
    public void setAuthorization(String authorization);


    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector();


    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector);


    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext();


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context);


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.
     */
    public ServletRequest getRequest();


    /**
     * Return the Response with which this Request is associated.
     */
    public Response getResponse();


    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(Response response);


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    public Socket getSocket();


    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    public void setSocket(Socket socket);


    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream();


    /**
     * Set the input stream associated with this Request.
     *
     * @param stream The new input stream
     */
    public void setStream(InputStream stream);


    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper();


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     *
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper);


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException;


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException;


    /**
     * Return the object bound with the specified name to the internal notes
     * for this request, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    public Object getNote(String name);


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this request.
     */
    public Iterator getNoteNames();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle();


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this request.
     *
     * @param name Name of the note to be removed
     */
    public void removeNote(String name);


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    public void setContentLength(int length);


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    public void setContentType(String type);


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this request, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    public void setNote(String name, Object value);


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    public void setProtocol(String protocol);


    /**
     * Set the remote IP address associated with this Request.  NOTE:  This
     * value will be used to resolve the value for <code>getRemoteHost()</code>
     * if that method is called.
     *
     * @param remote The remote IP address
     */
    public void setRemoteAddr(String remote);


    /**
     * Set the name of the scheme associated with this request.  Typical values
     * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
     *
     * @param scheme The scheme
     */
    public void setScheme(String scheme);


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    public void setSecure(boolean secure);


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    public void setServerName(String name);


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    public void setServerPort(int port);


}
