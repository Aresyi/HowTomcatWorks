/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/valves/CertificatesValve.java,v 1.10 2002/06/09 02:19:44 remm Exp $
 * $Revision: 1.10 $
 * $Date: 2002/06/09 02:19:44 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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


package org.apache.catalina.valves;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.security.cert.CertificateFactory;
import javax.security.cert.X509Certificate;
import javax.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.connector.RequestWrapper;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * <p>Implementation of a Valve that deals with SSL client certificates, as
 * follows:</p>
 * <ul>
 * <li>If this request was not received on an SSL socket, simply pass it
 *     on unmodified.</li>
 * <li>If this request was received on an SSL socket:
 *     <ul>
 *     <li>If this web application is using client certificate authentication,
 *         and no certificate chain is present (because the underlying socket
 *         did not default to requiring it), force an SSL handshake to acquire
 *         the client certificate chain.</li>
 *     <li>If there is a client certificate chain present, expose it as a
 *         request attribute.</li>
 *     <li>Expose the cipher suite and key size used on this SSL socket
 *         as request attributes.</li>
 * </ul>
 *
 * <p>The above tasks have been combined into a single Valve to minimize the
 * amount of code that has to check for the existence of JSSE classes.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.10 $ $Date: 2002/06/09 02:19:44 $
 */

public final class CertificatesValve
    extends ValveBase implements Lifecycle {


    // ----------------------------------------------------- Instance Variables


    /**
     * Are certificates required for authentication by this web application?
     */
    protected boolean certificates = false;


    /**
     * A mapping table to determine the number of effective bits in the key
     * when using a cipher suite containing the specified cipher name.  The
     * underlying data came from the TLS Specification (RFC 2246), Appendix C.
     */
    protected static final CipherData ciphers[] = {
        new CipherData("_WITH_NULL_", 0),
        new CipherData("_WITH_IDEA_CBC_", 128),
        new CipherData("_WITH_RC2_CBC_40_", 40),
        new CipherData("_WITH_RC4_40_", 40),
        new CipherData("_WITH_RC4_128_", 128),
        new CipherData("_WITH_DES40_CBC_", 40),
        new CipherData("_WITH_DES_CBC_", 56),
        new CipherData("_WITH_3DES_EDE_CBC_", 168)
    };


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The descriptive information related to this implementation.
     */
    protected static final String info =
        "org.apache.catalina.valves.CertificatesValve/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The StringManager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;


    // ------------------------------------------------------------- Properties


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Expose the certificates chain if one was included on this request.
     *
     * @param request The servlet request to be processed
     * @param response The servlet response to be created
     * @param context The valve context used to invoke the next valve
     *  in the current processing pipeline
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void invoke(Request request, Response response,
                       ValveContext context)
        throws IOException, ServletException {

        // Identify the underlying request if this request was wrapped
        Request actual = request;
        while (actual instanceof RequestWrapper)
            actual = ((RequestWrapper) actual).getWrappedRequest();
        //        if (debug >= 2)
        //            log("Processing request");

        // Verify the existence of a certificate chain if appropriate
        if (certificates)
            verify(request, actual);

        // Expose the certificate chain if appropriate
        expose(request, actual);

        // Invoke the next Valve in our Pipeline
        context.invokeNext(request, response);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a LifecycleEvent listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a LifecycleEvent listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called before any of the public
     * methods of this component are utilized.  It should also send a
     * LifecycleEvent of type START_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("certificatesValve.alreadyStarted"));
        started = true;
        if (debug >= 1)
            log("Starting");

        // Check what type of authentication (if any) we are doing
        certificates = false;
        if (container instanceof Context) {
            Context context = (Context) container;
            LoginConfig loginConfig = context.getLoginConfig();
            if (loginConfig != null) {
                String authMethod = loginConfig.getAuthMethod();
                if ("CLIENT-CERT".equalsIgnoreCase(authMethod))
                    certificates = true;
            }
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(Lifecycle.START_EVENT, null);

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.  It should also send a LifecycleEvent
     * of type STOP_EVENT to any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("certificatesValve.notStarted"));
        lifecycle.fireLifecycleEvent(Lifecycle.STOP_EVENT, null);
        started = false;
        if (debug >= 1)
            log("Stopping");

        certificates = false;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Expose the certificate chain for this request, if there is one.
     *
     * @param request The possibly wrapped Request being processed
     * @param actual The actual underlying Request object
     */
    protected void expose(Request request, Request actual) {

        // Ensure that this request came in on an SSLSocket
        if (actual.getSocket() == null)
            return;
        if (!(actual.getSocket() instanceof SSLSocket))
            return;
        SSLSocket socket = (SSLSocket) actual.getSocket();

        // Look up the current SSLSession
        SSLSession session = socket.getSession();
        if (session == null)
            return;
        //        if (debug >= 2)
        //            log(" expose: Has current SSLSession");

        // Expose the cipher suite and key size
        String cipherSuite = session.getCipherSuite();
        if (cipherSuite != null)
            request.getRequest().setAttribute(Globals.CIPHER_SUITE_ATTR,
                                              cipherSuite);
        Integer keySize = (Integer) session.getValue(Globals.KEY_SIZE_ATTR);
        if (keySize == null) {
            int size = 0;
            for (int i = 0; i < ciphers.length; i++) {
                if (cipherSuite.indexOf(ciphers[i].phrase) >= 0) {
                    size = ciphers[i].keySize;
                    break;
                }
            }
            keySize = new Integer(size);
            session.putValue(Globals.KEY_SIZE_ATTR, keySize);
        }
        request.getRequest().setAttribute(Globals.KEY_SIZE_ATTR,
                                          keySize);
        //        if (debug >= 2)
        //            log(" expose: Has cipher suite " + cipherSuite +
        //                " and key size " + keySize);

        // Expose ssl_session (getId)
        byte [] ssl_session = session.getId();
        if (ssl_session!=null) {
            StringBuffer buf=new StringBuffer("");
            for(int x=0; x<ssl_session.length; x++) {
                String digit=Integer.toHexString((int)ssl_session[x]);
                if (digit.length()<2) buf.append('0');
                if (digit.length()>2) digit=digit.substring(digit.length()-2);
                buf.append(digit);
            }
            request.getRequest().setAttribute(
                "javax.servlet.request.ssl_session",
                buf.toString());
        }

        // If we have cached certificates, return them
        Object cached = session.getValue(Globals.CERTIFICATES_ATTR);
        if (cached != null) {
            //            if (debug >= 2)
            //                log(" expose: Has cached certificates");
            request.getRequest().setAttribute(Globals.CERTIFICATES_ATTR,
                                              cached);
            return;
        }

        // Convert JSSE's certificate format to the ones we need
        X509Certificate jsseCerts[] = null;
        java.security.cert.X509Certificate x509Certs[] = null;
        try {
            jsseCerts = session.getPeerCertificateChain();
            if (jsseCerts == null)
                jsseCerts = new X509Certificate[0];
            x509Certs =
              new java.security.cert.X509Certificate[jsseCerts.length];
            for (int i = 0; i < x509Certs.length; i++) {
                byte buffer[] = jsseCerts[i].getEncoded();
                CertificateFactory cf =
                  CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream =
                  new ByteArrayInputStream(buffer);
                x509Certs[i] = (java.security.cert.X509Certificate)
                  cf.generateCertificate(stream);
            }
        } catch (Throwable t) {
            return;
        }

        // Expose these certificates as a request attribute
        if ((x509Certs == null) || (x509Certs.length < 1))
            return;
        session.putValue(Globals.CERTIFICATES_ATTR, x509Certs);
        log(" expose: Exposing converted certificates");
        request.getRequest().setAttribute(Globals.CERTIFICATES_ATTR,
                                          x509Certs);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log("CertificatesValve[" + container.getName() + "]: " +
                       message);
        else
            System.out.println("CertificatesValve[" + container.getName() +
                               "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        if (logger != null)
            logger.log("CertificatesValve[" + container.getName() + "]: " +
                       message, throwable);
        else {
            System.out.println("CertificatesValve[" + container.getName() +
                               "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Verify that a client certificate chain exists if our web application
     * is doing client certificate authentication.
     *
     * @param request The possibly wrapped Request being processed
     * @param actual The actual underlying Request object
     */
    protected void verify(Request request, Request actual) {

        // Ensure that this request came in on an SSLSocket
        if (actual.getSocket() == null)
            return;
        if (!(actual.getSocket() instanceof SSLSocket))
            return;
        SSLSocket socket = (SSLSocket) actual.getSocket();

        // Look up the current SSLSession
        SSLSession session = socket.getSession();
        if (session == null)
            return;
        //        if (debug >= 2)
        //            log(" verify: Has current SSLSession");

        // Verify that there is a client certificate chain present
        X509Certificate jsseCerts[] = null;
        try {
            jsseCerts = session.getPeerCertificateChain();
            if (jsseCerts == null)
                jsseCerts = new X509Certificate[0];
        } catch (SSLPeerUnverifiedException e) {
            log(" verify: SSLPeerUnverifiedException");
            jsseCerts = new X509Certificate[0];
        }
        //        if (debug >= 2)
        //            log(" verify: Certificate chain has " +
        //                jsseCerts.length + " certificates");
        if (jsseCerts.length > 0)
            return;

        // Force a new handshake to request the client certificates
        //        if (debug >= 2)
        //            log(" verify: Invalidating current session");
        session.invalidate();
        //        if (debug >= 2)
        //            log(" verify: Forcing new SSL handshake");
        socket.setNeedClientAuth(true);
        try {
            socket.startHandshake();
        } catch (IOException e) {
            log(" verify: ", e);
        }

        // Revalidate the existence of the required certificates
        session = socket.getSession();
        if (session == null)
            return;
        try {
            jsseCerts = session.getPeerCertificateChain();
            if (jsseCerts == null)
                jsseCerts = new X509Certificate[0];
        } catch (SSLPeerUnverifiedException e) {
            log(" verify: SSLPeerUnverifiedException");
            jsseCerts = new X509Certificate[0];
        }
        //        if (debug >= 2)
        //            log(" verify: Certificate chain has " +
        //                jsseCerts.length + " certificates");

    }


}


// ------------------------------------------------------------ Private Classes


/**
 * Simple data class that represents the cipher being used, along with the
 * corresponding effective key size.  The specified phrase must appear in the
 * name of the cipher suite to be recognized.
 */

final class CipherData {

    String phrase = null;

    int keySize = 0;

    public CipherData(String phrase, int keySize) {
        this.phrase = phrase;
        this.keySize = keySize;
    }

}
