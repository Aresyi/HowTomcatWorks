/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/http10/HttpRequestImpl.java,v 1.5 2002/03/18 07:15:40 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/03/18 07:15:40 $
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


package org.apache.catalina.connector.http10;


import java.net.InetAddress;
import org.apache.catalina.connector.HttpRequestBase;


/**
 * Implementation of <b>HttpRequest</b> specific to the HTTP connector.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/03/18 07:15:40 $
 * @deprecated
 */

final class HttpRequestImpl
    extends HttpRequestBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The InetAddress of the remote client of ths request.
     */
    protected InetAddress inet = null;


    /**
     * Descriptive information about this Request implementation.
     */
    protected static final String info =
        "org.apache.catalina.connector.http10.HttpRequestImpl/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * [Package Private] Return the InetAddress of the remote client of
     * this request.
     */
    InetAddress getInet() {

        return (inet);

    }


    /**
     * [Package Private] Set the InetAddress of the remote client of
     * this request.
     *
     * @param inet The new InetAddress
     */
    void setInet(InetAddress inet) {

        this.inet = inet;

    }


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        super.recycle();
        inet = null;

    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the Internet Protocol (IP) address of the client that sent
     * this request.
     */
    public String getRemoteAddr() {

        return (inet.getHostAddress());

    }


    /**
     * Return the fully qualified name of the client that sent this request,
     * or the IP address of the client if the name cannot be determined.
     */
    public String getRemoteHost() {

        if (connector.getEnableLookups())
            return (inet.getHostName());
        else
            return (getRemoteAddr());

    }


    // --------------------------------------------- HttpServletRequest Methods


}
