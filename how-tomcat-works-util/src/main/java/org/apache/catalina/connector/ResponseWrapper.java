/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/ResponseWrapper.java,v 1.3 2002/03/18 07:15:39 remm Exp $
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


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>Response</b>
 * object.  By default, all methods are delegated to the wrapped response,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

public abstract class ResponseWrapper implements Response {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseWrapper(Response response) {

        super();
        this.response = response;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The wrapped response.
     */
    protected Response response = null;


    /**
     * Return the wrapped response.
     */
    public Response getWrappedResponse() {

        return (this.response);

    }


    // ------------------------------------------------------------- Properties


    /**
     * Return the Connector through which this Response is returned.
     */
    public Connector getConnector() {

        return (response.getConnector());

    }


    /**
     * Set the Connector through which this Response is returned.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {

        response.setConnector(connector);

    }


    /**
     * Return the number of bytes actually written to the output stream.
     */
    public int getContentCount() {

        return (response.getContentCount());

    }


    /**
     * Return the Context with which this Response is associated.
     */
    public Context getContext() {

        return (response.getContext());

    }


    /**
     * Set the Context with which this Response is associated.  This should
     * be called as soon as the appropriate Context is identified.
     *
     * @param context The associated Context
     */
    public void setContext(Context context) {

        response.setContext(context);

    }


    /**
     * Return the "processing inside an include" flag.
     */
    public boolean getIncluded() {

        return (response.getIncluded());

    }


    /**
     * Set the "processing inside an include" flag.
     *
     * @param included <code>true</code> if we are currently inside a
     *  RequestDispatcher.include(), else <code>false</code>
     */
    public void setIncluded(boolean included) {

        response.setIncluded(included);

    }


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (response.getInfo());

    }


    /**
     * Return the Request with which this Response is associated.
     */
    public Request getRequest() {

        return (response.getRequest());

    }


    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    public void setRequest(Request request) {

        response.setRequest(request);

    }


    /**
     * Return the <code>ServletResponse</code> for which this object
     * is the facade.
     */
    public ServletResponse getResponse() {

        return (response.getResponse());

    }


    /**
     * Return the output stream associated with this Response.
     */
    public OutputStream getStream() {

        return (response.getStream());

    }


    /**
     * Set the output stream associated with this Response.
     *
     * @param stream The new output stream
     */
    public void setStream(OutputStream stream) {

        response.setStream(stream);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream createOutputStream() throws IOException {

        return (response.createOutputStream());

    }


    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishResponse() throws IOException {

        response.finishResponse();

    }


    /**
     * Return the content length that was set or calculated for this Response.
     */
    public int getContentLength() {

        return (response.getContentLength());

    }


    /**
     * Return the content type that was set or calculated for this response,
     * or <code>null</code> if no content type was set.
     */
    public String getContentType() {

        return (response.getContentType());

    }


    /**
     * Return a PrintWriter that can be used to render error messages,
     * regardless of whether a stream or writer has already been acquired.
     */
    public PrintWriter getReporter() {

        return (response.getReporter());

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        response.recycle();

    }


    /**
     * Reset the data buffer but not any status or header information.
     */
    public void resetBuffer() {

        response.resetBuffer();

    }


}
