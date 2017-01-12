/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/deploy/ErrorPage.java,v 1.4 2001/07/22 20:25:10 pier Exp $
 * $Revision: 1.4 $
 * $Date: 2001/07/22 20:25:10 $
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


package org.apache.catalina.deploy;


import org.apache.catalina.util.RequestUtil;


/**
 * Representation of an error page element for a web application,
 * as represented in a <code>&lt;error-page&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2001/07/22 20:25:10 $
 */

public final class ErrorPage {


    // ----------------------------------------------------- Instance Variables


    /**
     * The error (status) code for which this error page is active.
     */
    private int errorCode = 0;


    /**
     * The exception type for which this error page is active.
     */
    private String exceptionType = null;


    /**
     * The context-relative location to handle this error or exception.
     */
    private String location = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the error code.
     */
    public int getErrorCode() {

        return (this.errorCode);

    }


    /**
     * Set the error code.
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;

    }


    /**
     * Set the error code (hack for default XmlMapper data type).
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(String errorCode) {

        try {
            this.errorCode = Integer.parseInt(errorCode);
        } catch (Throwable t) {
            this.errorCode = 0;
        }

    }


    /**
     * Return the exception type.
     */
    public String getExceptionType() {

        return (this.exceptionType);

    }


    /**
     * Set the exception type.
     *
     * @param exceptionType The new exception type
     */
    public void setExceptionType(String exceptionType) {

        this.exceptionType = exceptionType;

    }


    /**
     * Return the location.
     */
    public String getLocation() {

        return (this.location);

    }


    /**
     * Set the location.
     *
     * @param location The new location
     */
    public void setLocation(String location) {

        //        if ((location == null) || !location.startsWith("/"))
        //            throw new IllegalArgumentException
        //                ("Error Page Location must start with a '/'");
        this.location = RequestUtil.URLDecode(location);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ErrorPage[");
        if (exceptionType == null) {
            sb.append("errorCode=");
            sb.append(errorCode);
        } else {
            sb.append("exceptionType=");
            sb.append(exceptionType);
        }
        sb.append(", location=");
        sb.append(location);
        sb.append("]");
        return (sb.toString());

    }


}
