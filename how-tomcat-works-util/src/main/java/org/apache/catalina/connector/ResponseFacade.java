/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/ResponseFacade.java,v 1.6 2002/05/15 04:24:17 remm Exp $
 * $Revision: 1.6 $
 * $Date: 2002/05/15 04:24:17 $
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
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Response;


/**
 * Facade class that wraps a Catalina-internal <b>Response</b>
 * object.  All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.6 $ $Date: 2002/05/15 04:24:17 $
 */

public class ResponseFacade implements ServletResponse {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseFacade(Response response) {
        this.resp = response;
        this.response = (ServletResponse) response;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The wrapped response.
     */
    protected ServletResponse response = null;


    /**
     * The wrapped response.
     */
    protected Response resp = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Clear facade.
     */
    public void clear() {
        response = null;
        resp = null;
    }


    public void finish() {

        resp.setSuspended(true);

    }


    public boolean isFinished() {

        return resp.isSuspended();

    }


    // ------------------------------------------------ ServletResponse Methods


    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }


    public ServletOutputStream getOutputStream()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        ServletOutputStream sos = response.getOutputStream();
        if (isFinished())
            resp.setSuspended(true);
        return (sos);

    }


    public PrintWriter getWriter()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        PrintWriter writer = response.getWriter();
        if (isFinished())
            resp.setSuspended(true);
        return (writer);

    }


    public void setContentLength(int len) {

        if (isCommitted())
            return;

        response.setContentLength(len);

    }


    public void setContentType(String type) {

        if (isCommitted())
            return;

        response.setContentType(type);

    }


    public void setBufferSize(int size) {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setBufferSize(size);

    }


    public int getBufferSize() {
        return response.getBufferSize();
    }


    public void flushBuffer()
        throws IOException {

        if (isFinished())
            //            throw new IllegalStateException
            //                (/*sm.getString("responseFacade.finished")*/);
            return;

        resp.setAppCommitted(true);

        response.flushBuffer();

    }


    public void resetBuffer() {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.resetBuffer();

    }


    public boolean isCommitted() {
        return (resp.isAppCommitted());
    }


    public void reset() {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.reset();

    }


    public void setLocale(Locale loc) {

        if (isCommitted())
            return;

        response.setLocale(loc);
    }


    public Locale getLocale() {
        return response.getLocale();
    }


}
