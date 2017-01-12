/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIServletRequestUtil.java,v 1.1 2002/05/24 04:38:58 billbarker Exp $
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

import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.util.RequestUtil;

public class SSIServletRequestUtil {
    /**
     * Return the relative path associated with this servlet.
     *
     * Taken from DefaultServlet.java.  Perhaps this should be put in
     * org.apache.catalina.util somewhere?  Seems like it would be widely used.
     *
     * @param request The servlet request we are processing
     */
    public static String getRelativePath(HttpServletRequest request) {

        // Are we being processed by a RequestDispatcher.include()?
        if (request.getAttribute("javax.servlet.include.request_uri")!=null) {
            String result = (String)
                request.getAttribute("javax.servlet.include.path_info");
            if (result == null)
                result = (String)
                    request.getAttribute("javax.servlet.include.servlet_path");
            if ((result == null) || (result.equals("")))
                result = "/";
            return (result);
        }

        // No, extract the desired path directly from the request
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return normalize(result);
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     *
     * This normalize should be the same as DefaultServlet.normalize, which is almost the same
     * ( see source code below ) as RequestUtil.normalize.  Do we need all this duplication?
     *
     * @param path Path to be normalized
     */
    public static String normalize(String path) {
        if (path == null)
            return null;

	String normalized = path;

	//Why doesn't RequestUtil do this??

        // Normalize the slashes and add leading slash if necessary
        if ( normalized.indexOf('\\') >= 0 )
            normalized = normalized.replace( '\\', '/' );

	normalized = RequestUtil.normalize( path );
	return normalized;
    }
}
