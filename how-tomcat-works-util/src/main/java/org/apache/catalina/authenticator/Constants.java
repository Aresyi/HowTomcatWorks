/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/authenticator/Constants.java,v 1.7 2001/08/01 03:04:04 craigmcc Exp $
 * $Revision: 1.7 $
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


package org.apache.catalina.authenticator;


public class Constants {

    public static final String Package = "org.apache.catalina.authenticator";

    // Authentication methods for login configuration
    public static final String BASIC_METHOD = "BASIC";
    public static final String CERT_METHOD = "CLIENT-CERT";
    public static final String DIGEST_METHOD = "DIGEST";
    public static final String FORM_METHOD = "FORM";

    // User data constraints for transport guarantee
    public static final String NONE_TRANSPORT = "NONE";
    public static final String INTEGRAL_TRANSPORT = "INTEGRAL";
    public static final String CONFIDENTIAL_TRANSPORT = "CONFIDENTIAL";

    // Form based authentication constants
    public static final String FORM_ACTION = "/j_security_check";
    public static final String FORM_PASSWORD = "j_password";
    public static final String FORM_USERNAME = "j_username";

    // Cookie name for single sign on support
    public static final String SINGLE_SIGN_ON_COOKIE = "JSESSIONIDSSO";


    // --------------------------------------------------------- Request Notes


    /**
     * <p>If a user has been authenticated by the web layer, by means of a
     * login method other than CLIENT_CERT, the username and password
     * used to authenticate the user will be attached to the request as
     * Notes for use by other server components.  A server component can
     * also call several existing methods on Request to determine whether
     * or not any user has been authenticated:</p>
     * <ul>
     * <li><strong>((HttpServletRequest) getRequest()).getAuthType()</strong>
     *     will return BASIC, CLIENT-CERT, DIGEST, FORM, or <code>null</code>
     *     if there is no authenticated user.</li>
     * <li><strong>((HttpServletRequest) getRequest()).getUserPrincipal()</strong>
     *     will return the authenticated <code>Principal</code> returned by the
     *     <code>Realm</code> that authenticated this user.</li>
     * </ul>
     * <p>If CLIENT_CERT authentication was performed, the certificate chain
     * will be available as a request attribute, as defined in the
     * servlet specification.</p>
     */


    /**
     * The notes key for the password used to authenticate this user.
     */
    public static final String REQ_PASSWORD_NOTE =
      "org.apache.catalina.request.PASSWORD";


    /**
     * The notes key for the username used to authenticate this user.
     */
    public static final String REQ_USERNAME_NOTE =
      "org.apache.catalina.request.USERNAME";


    /**
     * The notes key to track the single-sign-on identity with which this
     * request is associated.
     */
    public static final String REQ_SSOID_NOTE =
      "org.apache.catalina.request.SSOID";


    // ---------------------------------------------------------- Session Notes


    /**
     * If the <code>cache</code> property of our authenticator is set, and
     * the current request is part of a session, authentication information
     * will be cached to avoid the need for repeated calls to
     * <code>Realm.authenticate()</code>, under the following keys:
     */


    /**
     * The notes key for the password used to authenticate this user.
     */
    public static final String SESS_PASSWORD_NOTE =
      "org.apache.catalina.session.PASSWORD";


    /**
     * The notes key for the username used to authenticate this user.
     */
    public static final String SESS_USERNAME_NOTE =
      "org.apache.catalina.session.USERNAME";


    /**
     * The following note keys are used during form login processing to
     * cache required information prior to the completion of authentication.
     */


    /**
     * The previously authenticated principal (if caching is disabled).
     */
    public static final String FORM_PRINCIPAL_NOTE =
        "org.apache.catalina.authenticator.PRINCIPAL";


    /**
     * The original request information, to which the user will be
     * redirected if authentication succeeds.
     */
    public static final String FORM_REQUEST_NOTE =
        "org.apache.catalina.authenticator.REQUEST";


}
