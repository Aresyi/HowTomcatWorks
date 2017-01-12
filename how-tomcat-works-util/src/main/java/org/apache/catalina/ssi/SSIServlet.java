/*
 * SSIServlet.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIServlet.java,v 1.1 2002/05/26 00:00:55 remm Exp $
 * $Revision: 1.1 $
 * $Date: 2002/05/26 00:00:55 $
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to process SSI requests within a webpage.
 * Mapped to a path from within web.xml.
 *
 * @author Bip Thelin
 * @author Amy Roh
 * @author Dan Sandberg
 * @version $Revision: 1.1 $, $Date: 2002/05/26 00:00:55 $
 */
public class SSIServlet extends HttpServlet {
    /** Debug level for this servlet. */
    protected int debug = 0;

    /** Should the output be buffered. */
    protected boolean buffered = false;

    /** Expiration time in seconds for the doc. */
    protected Long expires = null;

    /** virtual path can be webapp-relative */
    protected boolean isVirtualWebappRelative = false;

    //----------------- Public methods.

    /**
     * Initialize this servlet.
     * @exception ServletException if an error occurs
     */
    public void init() throws ServletException {
        String value = null;
        try {
            value = getServletConfig().getInitParameter("debug");
            debug = Integer.parseInt(value);
        } catch (Throwable t) {
            ;
        }

        try {
            value = getServletConfig().getInitParameter("isVirtualWebappRelative");
            isVirtualWebappRelative = Integer.parseInt(value) > 0 ? true : false;
        } catch (Throwable t) {
            ;
        }

        try {
            value = getServletConfig().getInitParameter("expires");
            expires = Long.valueOf(value);
        } catch (NumberFormatException e) {
            expires = null;
            log("Invalid format for expires initParam; expected integer (seconds)");
        } catch (Throwable t) {
            ;
        }
        try {
            value = getServletConfig().getInitParameter("buffered");
            buffered = Integer.parseInt(value) > 0 ? true : false;
        } catch (Throwable t) {
            ;
        }
        if (debug > 0)
            log("SSIServlet.init() SSI invoker started with 'debug'="
                + debug);
    }

    /**
     * Process and forward the GET request
     * to our <code>requestHandler()</code>     *
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     * @exception IOException if an error occurs
     * @exception ServletException if an error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {

        if (debug > 0)
            log("SSIServlet.doGet()");
        requestHandler(req, res);
    }

    /**
     * Process and forward the POST request
     * to our <code>requestHandler()</code>.
     *
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     * @exception IOException if an error occurs
     * @exception ServletException if an error occurs
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {

        if (debug > 0)
            log("SSIServlet.doPost()");
        requestHandler(req, res);
    }

    /**
     * Process our request and locate right SSI command.
     * @param req a value of type 'HttpServletRequest'
     * @param res a value of type 'HttpServletResponse'
     */
    protected void requestHandler(HttpServletRequest req,
                                HttpServletResponse res)
        throws IOException, ServletException {

        ServletContext servletContext = getServletContext();
        String path = SSIServletRequestUtil.getRelativePath( req );

        if (debug > 0)
            log("SSIServlet.requestHandler()\n" +
                "Serving " + (buffered ? "buffered " : "unbuffered ") +
                "resource '" + path + "'");

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        // (the "toUpperCase()" avoids problems on Windows systems)
        if ( path == null ||
             path.toUpperCase().startsWith("/WEB-INF") ||
             path.toUpperCase().startsWith("/META-INF") ) {

            res.sendError(res.SC_NOT_FOUND, path);
	    log( "Can't serve file: " + path );
            return;
        }
	
	URL resource = servletContext.getResource(path);
        if (resource==null) {
            res.sendError(res.SC_NOT_FOUND, path);
	    log( "Can't find file: " + path );
            return;
        }

        res.setContentType("text/html;charset=UTF-8");

        if (expires != null) {
            res.setDateHeader("Expires", (
                new java.util.Date()).getTime() + expires.longValue() * 1000);
        }

	processSSI( req, res, resource );
    }

    protected void processSSI( HttpServletRequest req,
			       HttpServletResponse res,
			       URL resource ) throws IOException {
	SSIExternalResolver ssiExternalResolver = new SSIServletExternalResolver( this, req, res,
										  isVirtualWebappRelative,
										  debug );
	SSIProcessor ssiProcessor = new SSIProcessor( ssiExternalResolver, debug );

        PrintWriter printWriter = null;
	StringWriter stringWriter = null;
        if (buffered) {
	    stringWriter = new StringWriter();
            printWriter = new PrintWriter( stringWriter );
        } else {
            printWriter = res.getWriter();
	}

        URLConnection resourceInfo = resource.openConnection();
        InputStream resourceInputStream = resourceInfo.getInputStream();
	BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( resourceInputStream ) );
	Date lastModifiedDate = new Date( resourceInfo.getLastModified() );
	ssiProcessor.process( bufferedReader, lastModifiedDate, printWriter );

        if ( buffered ) {
	    printWriter.flush();
	    String text = stringWriter.toString();
            res.getWriter().write( text );
	}
    }
}
