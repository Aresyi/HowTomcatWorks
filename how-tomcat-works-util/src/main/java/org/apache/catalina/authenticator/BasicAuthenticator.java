/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/authenticator/BasicAuthenticator.java,v 1.12 2002/03/23 17:52:16 remm Exp $
 * $Revision: 1.12 $
 * $Date: 2002/03/23 17:52:16 $
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


import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.Base64;



/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of HTTP BASIC
 * Authentication, as outlined in RFC 2617:  "HTTP Authentication: Basic
 * and Digest Access Authentication."
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2002/03/23 17:52:16 $
 */

public class BasicAuthenticator
    extends AuthenticatorBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Base64 helper object for this class.
     */
    protected static final Base64 base64Helper = new Base64();


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.authenticator.BasicAuthenticator/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param login Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config)
        throws IOException {

        // Have we already authenticated someone?
        Principal principal =
            ((HttpServletRequest) request.getRequest()).getUserPrincipal();
        if (principal != null) {
            if (debug >= 1)
                log("Already authenticated '" + principal.getName() + "'");
            return (true);
        }

        // Validate any credentials already included with this request
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();
        String authorization = request.getAuthorization();
        String username = parseUsername(authorization);
        String password = parsePassword(authorization);
        principal = context.getRealm().authenticate(username, password);
        if (principal != null) {
            register(request, response, principal, Constants.BASIC_METHOD,
                     username, password);
            return (true);
        }

        // Send an "unauthorized" response and an appropriate challenge
        String realmName = config.getRealmName();
        if (realmName == null)
            realmName = hreq.getServerName() + ":" + hreq.getServerPort();
    //        if (debug >= 1)
    //            log("Challenging for realm '" + realmName + "'");
        hres.setHeader("WWW-Authenticate",
                       "Basic realm=\"" + realmName + "\"");
        hres.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //      hres.flushBuffer();
        return (false);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse the username from the specified authorization credentials.
     * If none can be found, return <code>null</code>.
     *
     * @param authorization Authorization credentials from this request
     */
    protected String parseUsername(String authorization) {

        if (authorization == null)
            return (null);
        if (!authorization.toLowerCase().startsWith("basic "))
            return (null);
        authorization = authorization.substring(6).trim();

        // Decode and parse the authorization credentials
        String unencoded =
          new String(base64Helper.decode(authorization.getBytes()));
        int colon = unencoded.indexOf(':');
        if (colon < 0)
            return (null);
        String username = unencoded.substring(0, colon).trim();
        //        String password = unencoded.substring(colon + 1).trim();
        return (username);

    }


    /**
     * Parse the password from the specified authorization credentials.
     * If none can be found, return <code>null</code>.
     *
     * @param authorization Authorization credentials from this request
     */
    protected String parsePassword(String authorization) {

        if (authorization == null)
            return (null);
        if (!authorization.startsWith("Basic "))
            return (null);
        authorization = authorization.substring(6).trim();

        // Decode and parse the authorization credentials
        String unencoded =
          new String(base64Helper.decode(authorization.getBytes()));
        int colon = unencoded.indexOf(':');
        if (colon < 0)
            return (null);
        //        String username = unencoded.substring(0, colon).trim();
        String password = unencoded.substring(colon + 1).trim();
        return (password);

    }



}
