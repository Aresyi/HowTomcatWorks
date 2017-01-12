/*
 * ResponseIncludeWrapper.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/ResponseIncludeWrapper.java,v 1.1 2002/05/24 04:38:58 billbarker Exp $
 * $Revision: 1.1 $
 * $Date: 2002/05/24 04:38:58 $
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

package org.apache.catalina.ssi;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A HttpServletResponseWrapper, used from <code>SSIServletExternalResolver</code>
 *
 * @author Bip Thelin
 * @version $Revision: 1.1 $, $Date: 2002/05/24 04:38:58 $
 */
public class ResponseIncludeWrapper extends HttpServletResponseWrapper {

    /**
     * Our ServletOutputStream
     */
    protected ServletOutputStream originalServletOutputStream;
    protected ServletOutputStream servletOutputStream;
    protected PrintWriter printWriter;

    /**
     * Initialize our wrapper with the current HttpServletResponse
     * and ServletOutputStream.
     *
     * @param res The HttpServletResponse to use
     * @param out The ServletOutputStream' to use
     */
    public ResponseIncludeWrapper(HttpServletResponse res,
                                  ServletOutputStream originalServletOutputStream) {
        super(res);
        this.originalServletOutputStream = originalServletOutputStream;
    }

    /**
     * Flush the servletOutputStream or printWriter ( only one will be non-null )
     *
     * This must be called after a requestDispatcher.include, since we can't assume that
     * the included servlet flushed its stream.
     */
    public void flushOutputStreamOrWriter() throws IOException {
	if ( servletOutputStream != null ) {
	    servletOutputStream.flush();
	}
	if ( printWriter != null ) {
	    printWriter.flush();
	}
    }

    /**
     * Return a printwriter, throws and exception if a
     * OutputStream already been returned.
     *
     * @return a PrintWriter object
     * @exception java.io.IOException if the outputstream already been called
     */
    public PrintWriter getWriter() throws java.io.IOException {
        if ( servletOutputStream == null ) {
	    if ( printWriter == null ) {
		printWriter = new PrintWriter( originalServletOutputStream );
	    }
            return printWriter;
	}
	throw new IllegalStateException();
    }

    /**
     * Return a OutputStream, throws and exception if a
     * printwriter already been returned.
     *
     * @return a OutputStream object
     * @exception java.io.IOException if the printwriter already been called
     */
    public ServletOutputStream getOutputStream() throws java.io.IOException {
        if ( printWriter == null ) {
	    if ( servletOutputStream == null ) {
		servletOutputStream = originalServletOutputStream;
	    }
	    return servletOutputStream;
	}
	throw new IllegalStateException();
    }
}
