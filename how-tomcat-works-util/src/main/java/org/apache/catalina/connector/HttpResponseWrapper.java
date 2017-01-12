/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/HttpResponseWrapper.java,v 1.4 2002/03/18 07:15:39 remm Exp $
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


import org.apache.catalina.HttpResponse;


/**
 * Abstract convenience class that wraps a Catalina-internal <b>HttpResponse</b>
 * object.  By default, all methods are delegated to the wrapped response,
 * but subclasses can override individual methods as required to provide the
 * functionality that they require.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

public abstract class HttpResponseWrapper
    extends ResponseWrapper
    implements HttpResponse {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public HttpResponseWrapper(HttpResponse response) {

        super(response);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the value for the specified header, or <code>null</code> if this
     * header has not been set.  If more than one value was added for this
     * name, only the first is returned; use getHeaderValues() to retrieve all
     * of them.
     *
     * @param name Header name to look up
     */
    public String getHeader(String name) {

        return (((HttpResponse) response).getHeader(name));

    }


    /**
     * Return an array of all the header names set for this response, or
     * a zero-length array if no headers have been set.
     */
    public String[] getHeaderNames() {

        return (((HttpResponse) response).getHeaderNames());

    }


    /**
     * Return an array of all the header values associated with the
     * specified header name, or an zero-length array if there are no such
     * header values.
     *
     * @param name Header name to look up
     */
    public String[] getHeaderValues(String name) {

        return (((HttpResponse) response).getHeaderValues(name));

    }


    /**
     * Return the error message that was set with <code>sendError()</code>
     * for this response.
     */
    public String getMessage() {

        return (((HttpResponse) response).getMessage());

    }


    /**
     * Return the HTTP status code associated with this Response.
     */
    public int getStatus() {

        return (((HttpResponse) response).getStatus());

    }


    /**
     * Reset this response, and specify the values for the HTTP status code
     * and corresponding message.
     *
     * @exception IllegalStateException if this response has already been
     *  committed
     */
    public void reset(int status, String message) {

        ((HttpResponse) response).reset(status, message);

    }


}
