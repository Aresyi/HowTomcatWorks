/*
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
package org.apache.catalina.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.KeyManagementException;
import java.security.Security;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLServerSocket;

import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;


/**
 * Socket factory for SSL sockets, using the Java Server Sockets Extension
 * (JSSE) reference implementation support classes.  Besides the usual
 * configuration mechanism based on setting JavaBeans properties, this
 * component may also be configured by passing a series of attributes set
 * with calls to <code>setAttribute()</code>.  The following attribute
 * names are recognized, with default values in square brackets:
 * <ul>
 * <li><strong>algorithm</strong> - Certificate encoding algorithm
 *     to use. [SunX509]</li>
 * <li><strong>clientAuth</strong> - Require client authentication if
 *     set to <code>true</code>. [false]</li>
 * <li><strong>keystoreFile</strong> - Pathname to the Key Store file to be
 *     loaded.  This must be an absolute path, or a relative path that
 *     is resolved against the "catalina.base" system property.
 *     ["./keystore" in the user home directory]</li>
 * <li><strong>keystorePass</strong> - Password for the Key Store file to be
 *     loaded. ["changeit"]</li>
 * <li><strong>keystoreType</strong> - Type of the Key Store file to be
 *     loaded. ["JKS"]</li>
 * <li><strong>protocol</strong> - SSL protocol to use. [TLS]</li>
 * </ul>
 *
 * @author Harish Prabandham
 * @author Costin Manolache
 * @author Craig McClanahan
 */

public class SSLServerSocketFactory
    implements org.apache.catalina.net.ServerSocketFactory {


    // ----------------------------------------------------- Instance Variables


    /**
     * The name of our protocol handler package for the "https:" protocol.
     */
    private static final String PROTOCOL_HANDLER =
        "com.sun.net.ssl.internal.www.protocol";


    /**
     * The name of the system property containing a "|" delimited list of
     * protocol handler packages.
     */
    private static final String PROTOCOL_PACKAGES =
        "java.protocol.handler.pkgs";

    /**
     * The configured socket factory.
     */
    private javax.net.ssl.SSLServerSocketFactory sslProxy = null;


    /**
     * The trust manager factory used with JSSE 1.0.1.
     */
    //    TrustManagerFactory trustManagerFactory = null;


    // ------------------------------------------------------------- Properties


    /**
     * Certificate encoding algorithm to be used.
     */
    private String algorithm = "SunX509";

    public String getAlgorithm() {
        return (this.algorithm);
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


    /**
     * Should we require client authentication?
     */
    private boolean clientAuth = false;

    public boolean getClientAuth() {
        return (this.clientAuth);
    }

    public void setClientAuth(boolean clientAuth) {
        this.clientAuth = clientAuth;
    }


    /**
     * The internal represenation of the key store file that contains
     * our server certificate.
     */
    private KeyStore keyStore = null;

    public KeyStore getKeyStore()
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException,UnrecoverableKeyException,
           KeyManagementException
    {
        if (sslProxy == null)
            initialize();
        return (this.keyStore);
    }


    /**
     * Pathname to the key store file to be used.
     */
    private String keystoreFile =
        System.getProperty("user.home") + File.separator + ".keystore";

    public String getKeystoreFile() {
        return (this.keystoreFile);
    }

    public void setKeystoreFile(String keystoreFile) {
        File file = new File(keystoreFile);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"),
                            keystoreFile);
        this.keystoreFile = file.getAbsolutePath();
    }


    /**
     * Password for accessing the key store file.
     */
    private String keystorePass = "changeit";

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }


    /**
     * Storeage type of the key store file to be used.
     */
    private String keystoreType = "JKS";

    public String getKeystoreType() {
        return (this.keystoreType);
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }


    /**
     * SSL protocol variant to use.
     */
    private String protocol = "TLS";

    public String getProtocol() {
        return (this.protocol);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return a server socket that uses all network interfaces on the host,
     * and is bound to a specified port.  The socket is configured with the
     * socket options (such as accept timeout) given to this factory.
     *
     * @param port Port to listen to
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception CertificateException       general certificate error
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate
     * @exception KeyManagementException     problem in the key management
     *                                       layer
     */
    public ServerSocket createSocket(int port)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {

        if (sslProxy == null)
            initialize();
        ServerSocket socket =
            sslProxy.createServerSocket(port);
        initServerSocket(socket);
        return (socket);

    }


    /**
     * Return a server socket that uses all network interfaces on the host,
     * and is bound to a specified port, and uses the specified
     * connection backlog.  The socket is configured with the
     * socket options (such as accept timeout) given to this factory.
     *
     * @param port Port to listen to
     * @param backlog Maximum number of connections to be queued
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception CertificateException       general certificate error
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate
     * @exception KeyManagementException     problem in the key management
     *                                       layer
     */
    public ServerSocket createSocket(int port, int backlog)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {

        if (sslProxy == null)
            initialize();
        ServerSocket socket =
            sslProxy.createServerSocket(port, backlog);
        initServerSocket(socket);
        return (socket);

    }


    /**
     * Return a server socket that uses the specified interface on the host,
     * and is bound to a specified port, and uses the specified
     * connection backlog.  The socket is configured with the
     * socket options (such as accept timeout) given to this factory.
     *
     * @param port Port to listen to
     * @param backlog Maximum number of connections to be queued
     * @param ifAddress Address of the interface to be used
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception CertificateException       general certificate error
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate
     * @exception KeyManagementException     problem in the key management
     *                                       layer
     */
    public ServerSocket createSocket(int port, int backlog,
                                     InetAddress ifAddress)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {

        if (sslProxy == null)
            initialize();
        ServerSocket socket =
            sslProxy.createServerSocket(port, backlog, ifAddress);
        initServerSocket(socket);
        return (socket);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Initialize objects that will be required to create sockets.
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception CertificateException       general certificate error
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate
     * @exception KeyManagementException     problem in the key management
     *                                       layer
     */
    private synchronized void initialize()
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {

        initHandler();
        initKeyStore();
        initProxy();

    }


    /**
     * Register our URLStreamHandler for the "https:" protocol.
     */
    private void initHandler() {

        String packages = System.getProperty(PROTOCOL_PACKAGES);
        if (packages == null)
            packages = PROTOCOL_HANDLER;
        else if (packages.indexOf(PROTOCOL_HANDLER) < 0)
            packages += "|" + PROTOCOL_HANDLER;
        System.setProperty(PROTOCOL_PACKAGES, packages);

    }


    /**
     * Initialize the internal representation of the key store file.
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception CertificateException       general certificate error
     */
    private void initKeyStore()
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException
    {

        FileInputStream istream = null;

        try {
            keyStore = KeyStore.getInstance(keystoreType);
            istream = new FileInputStream(keystoreFile);
            keyStore.load(istream, keystorePass.toCharArray());
        } catch (IOException ioe) {
            throw ioe;
        } catch (KeyStoreException kse) {
            throw kse;
        } catch (NoSuchAlgorithmException nsae) {
            throw nsae;
        } catch (CertificateException ce) {
            throw ce;
        } finally {
            if ( istream != null )
                istream.close();
        }

    }


    /**
     * Initialize the SSL socket factory.
     *
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate
     * @exception KeyManagementException     problem in the key management
     *                                       layer
     */
    private void initProxy()
    throws KeyStoreException, NoSuchAlgorithmException,
           UnrecoverableKeyException, KeyManagementException
    {

        // Register the JSSE security Provider (if it is not already there)
        try {
            Security.addProvider((java.security.Provider)
                Class.forName("com.sun.net.ssl.internal.ssl.Provider").newInstance());
        } catch (Throwable t) {
            ;
        }

        // Create an SSL context used to create an SSL socket factory
        SSLContext context = SSLContext.getInstance(protocol);

        // Create the key manager factory used to extract the server key
        KeyManagerFactory keyManagerFactory =
            KeyManagerFactory.getInstance(algorithm);
        keyManagerFactory.init(keyStore, keystorePass.toCharArray());

        // Create the trust manager factory used for checking certificates
        /*
          trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
          trustManagerFactory.init(keyStore);
        */

        // Initialize the context with the key managers
        context.init(keyManagerFactory.getKeyManagers(), null,
                     new java.security.SecureRandom());

        // Create the proxy and return
        sslProxy = context.getServerSocketFactory();

    }


    /**
     * Set the requested properties for this server socket.
     *
     * @param ssocket The server socket to be configured
     */
    private void initServerSocket(ServerSocket ssocket) {

        SSLServerSocket socket = (SSLServerSocket) ssocket;

        // Enable all available cipher suites when the socket is connected
        String cipherSuites[] = socket.getSupportedCipherSuites();
        socket.setEnabledCipherSuites(cipherSuites);

        // Set client authentication if necessary
        socket.setNeedClientAuth(clientAuth);

    }


}
