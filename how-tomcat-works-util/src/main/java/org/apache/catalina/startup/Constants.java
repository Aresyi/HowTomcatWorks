/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/Constants.java,v 1.5 2001/07/22 20:25:13 pier Exp $
 * $Revision: 1.5 $
 * $Date: 2001/07/22 20:25:13 $
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


package org.apache.catalina.startup;


/**
 * String constants for the startup package.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/07/22 20:25:13 $
 */

public final class Constants {

    public static final String Package = "org.apache.catalina.startup";

    public static final String ApplicationWebXml = "/WEB-INF/web.xml";
    public static final String DefaultWebXml = "conf/web.xml";

    public static final String TldDtdPublicId_11 =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String TldDtdResourcePath_11 =
        //        "conf/tld_11.dtd";
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd";

    public static final String TldDtdPublicId_12 =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String TldDtdResourcePath_12 =
        //        "conf/tld_12.dtd";
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd";

    public static final String WebDtdPublicId_22 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WebDtdResourcePath_22 =
        //      "conf/web_22.dtd";
        "/javax/servlet/resources/web-app_2_2.dtd";

    public static final String WebDtdPublicId_23 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WebDtdResourcePath_23 =
        //      "conf/web_23.dtd";
        "/javax/servlet/resources/web-app_2_3.dtd";

}
