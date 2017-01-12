package org.apache.catalina;

import org.apache.catalina.net.ServerSocketFactory;

/**
 * A <b>Connector</b> is a component responsible receiving requests from,
 * and returning responses to, a client application.  A Connector performs
 * the following general logic:
 * <ul>
 * <li>Receive a request from the client application.
 * <li>Create (or allocate from a pool) appropriate Request and Response
 *     instances, and populate their properties based on the contents of
 *     the received request, as described below.
 *     <ul>
 *     <li>For all Requests, the <code>connector</code>,
 *         <code>protocol</code>, <code>remoteAddr</code>,
 *         <code>response</code>, <code>scheme</code>,
 *         <code>secure</code>, <code>serverName</code>,
 *         <code>serverPort</code> and <code>stream</code>
 *         properties <b>MUST</b> be set. The <code>contentLength</code>
 *         and <code>contentType</code> properties are also generally set.
 *     <li>For HttpRequests, the <code>method</code>, <code>queryString</code>,
 *         <code>requestedSessionCookie</code>,
 *         <code>requestedSessionId</code>, <code>requestedSessionURL</code>,
 *         <code>requestURI</code>, and <code>secure</code> properties
 *         <b>MUST</b> be set.  In addition, the various <code>addXxx</code>
 *         methods must be called to record the presence of cookies, headers,
 *         and locales in the original request.
 *     <li>For all Responses, the <code>connector</code>, <code>request</code>,
 *         and <code>stream</code> properties <b>MUST</b> be set.
 *     <li>No additional headers must be set by the Connector for
 *         HttpResponses.
 *     </ul>
 * <li>Identify an appropriate Container to use for processing this request.
 *     For a stand alone Catalina installation, this will probably be a
 *     (singleton) Engine implementation.  For a Connector attaching Catalina
 *     to a web server such as Apache, this step could take advantage of
 *     parsing already performed within the web server to identify the
 *     Context, and perhaps even the Wrapper, to utilize in satisfying this
 *     Request.
 * <li>Call the <code>invoke()</code> method of the selected Container,
 *     passing the initialized Request and Response instances as arguments.
 * <li>Return any response created by the Container to the client, or
 *     return an appropriate error message if an exception of any type
 *     was thrown.
 * <li>If utilizing a pool of Request and Response objects, recycle the pair
 *     of instances that was just used.
 * </ul>
 * It is expected that the implementation details of various Connectors will
 * vary widely, so the logic above should considered typical rather than
 * normative.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2001/12/20 21:25:23 $
 */

public interface Connector {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container used for processing requests received by this
     * Connector.
     */
    public Container getContainer();


    /**
     * Set the Container used for processing requests received by this
     * Connector.
     *
     * @param container The new Container to use
     */
    public void setContainer(Container container);


    /**
     * Return the "enable DNS lookups" flag.
     */
    public boolean getEnableLookups();


    /**
     * Set the "enable DNS lookups" flag.
     *
     * @param enableLookups The new "enable DNS lookups" flag value
     */
    public void setEnableLookups(boolean enableLookups);


    /**
     * Return the server socket factory used by this Container.
     */
    public ServerSocketFactory getFactory();


    /**
     * Set the server socket factory used by this Container.
     *
     * @param factory The new server socket factory
     */
    public void setFactory(ServerSocketFactory factory);


    /**
     * Return descriptive information about this Connector implementation.
     */
    public String getInfo();


    /**
     * Return the port number to which a request should be redirected if
     * it comes in on a non-SSL port and is subject to a security constraint
     * with a transport guarantee that requires SSL.
     */
    public int getRedirectPort();


    /**
     * Set the redirect port number.
     *
     * @param redirectPort The redirect port number (non-SSL to SSL)
     */
    public void setRedirectPort(int redirectPort);


    /**
     * Return the scheme that will be assigned to requests received
     * through this connector.  Default value is "http".
     */
    public String getScheme();


    /**
     * Set the scheme that will be assigned to requests received through
     * this connector.
     *
     * @param scheme The new scheme
     */
    public void setScheme(String scheme);


    /**
     * Return the secure connection flag that will be assigned to requests
     * received through this connector.  Default value is "false".
     */
    public boolean getSecure();


    /**
     * Set the secure connection flag that will be assigned to requests
     * received through this connector.
     *
     * @param secure The new secure connection flag
     */
    public void setSecure(boolean secure);


    /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService();


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service);


    // --------------------------------------------------------- Public Methods


    /**
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible Container.
     */
    public Request createRequest();


    /**
     * Create (or allocate) and return a Response object suitable for
     * receiving the contents of a Response from the responsible Container.
     */
    public Response createResponse();

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     *
     * @exception LifecycleException If this server was already initialized.
     */
    public void initialize()
    throws LifecycleException;

}
