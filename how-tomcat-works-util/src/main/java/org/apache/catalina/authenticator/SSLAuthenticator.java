/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/authenticator/SSLAuthenticator.java,v 1.13 2002/09/23 00:25:10 billbarker Exp $
 * $Revision: 1.13 $
 * $Date: 2002/09/23 00:25:10 $
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
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.LoginConfig;



/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes SSL certificates to identify client users.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.13 $ $Date: 2002/09/23 00:25:10 $
 */

public class SSLAuthenticator
    extends AuthenticatorBase {


    // ------------------------------------------------------------- Properties


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.authenticator.SSLAuthenticator/1.0";


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user by checking for the existence of a certificate
     * chain (which should have been made visible by an instance of
     * <code>CertificatesValve</code), and optionally asking a trust
     * manager to validate that we trust this user.
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

        // Retrieve the certificate chain for this client
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();
        if (debug >= 1)
            log(" Looking up certificates");
        X509Certificate certs[] = (X509Certificate[])
            request.getRequest().getAttribute(Globals.CERTIFICATES_ATTR);
        if ((certs == null) || (certs.length < 1)) {
            certs = (X509Certificate[])
                request.getRequest().getAttribute(Globals.SSL_CERTIFICATE_ATTR);
        }
        if ((certs == null) || (certs.length < 1)) {
            if (debug >= 1)
                log("  No certificates included with this request");
            hres.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           sm.getString("authenticator.certificates"));
            return (false);
        }

        // Authenticate the specified certificate chain
        principal = context.getRealm().authenticate(certs);
        if (principal == null) {
            if (debug >= 1)
                log("  Realm.authenticate() returned false");
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                           sm.getString("authenticator.unauthorized"));
            return (false);
        }

        // Cache the principal (if requested) and record this authentication
        register(request, response, principal, Constants.CERT_METHOD,
                 null, null);
        return (true);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Initialize the database we will be using for client verification
     * and certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        super.start();

    }


    /**
     * Finalize the database we used for client verification and
     * certificate validation (if any).
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void stop() throws LifecycleException {

        super.stop();

    }


}
