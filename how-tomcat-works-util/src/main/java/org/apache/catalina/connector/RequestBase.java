package org.apache.catalina.connector;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;


/**
 * Convenience base implementation of the <b>Request</b> interface, which can
 * be used for the Request implementation required by most Connectors.  Only
 * the connector-specific methods need to be implemented.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.21 $ $Date: 2002/08/26 12:15:58 $
 * @deprecated
 */

public abstract class RequestBase
    implements ServletRequest, Request {


    // ----------------------------------------------------- Instance Variables


    /**
     * The attributes associated with this Request, keyed by attribute name.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The authorization credentials sent with this Request.
     */
    protected String authorization = null;


    /**
     * The character encoding for this Request.
     */
    protected String characterEncoding = null;


    /**
     * The Connector through which this Request was received.
     */
    protected Connector connector = null;


    /**
     * The content length associated with this request.
     */
    protected int contentLength = -1;


    /**
     * The content type associated with this request.
     */
    protected String contentType = null;


    /**
     * The Context within which this Request is being processed.
     */
    protected Context context = null;


    /**
     * The default Locale if none are specified.
     */
    protected static Locale defaultLocale = Locale.getDefault();


    /**
     * The facade associated with this request.
     */
    protected RequestFacade facade = new RequestFacade(this);


    /**
     * Descriptive information about this Request implementation.
     */
    protected static final String info =
        "org.apache.catalina.connector.RequestBase/1.0";


    /**
     * The input stream associated with this Request.
     */
    protected InputStream input = null;


    /**
     * The preferred Locales assocaited with this Request.
     */
    protected ArrayList locales = new ArrayList();


    /**
     * Internal notes associated with this request by Catalina components
     * and event listeners.
     */
    private transient HashMap notes = new HashMap();


    /**
     * The protocol name and version associated with this Request.
     */
    protected String protocol = null;


    /**
     * The reader that has been returned by <code>getReader</code>, if any.
     */
    protected BufferedReader reader = null;


    /**
     * The remote address associated with this request.
     */
    protected String remoteAddr = null;


    /**
     * The fully qualified name of the remote host.
     */
    protected String remoteHost = null;


    /**
     * The response with which this request is associated.
     */
    protected Response response = null;


    /**
     * The scheme associated with this Request.
     */
    protected String scheme = null;


    /**
     * Was this request received on a secure connection?
     */
    protected boolean secure = false;


    /**
     * The server name associated with this Request.
     */
    protected String serverName = null;


    /**
     * The server port associated with this Request.
     */
    protected int serverPort = -1;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The socket through which this Request was received.
     */
    protected Socket socket = null;


    /**
     * The ServletInputStream that has been returned by
     * <code>getInputStream()</code>, if any.
     */
    protected ServletInputStream stream = null;


    /**
     * The Wrapper within which this Request is being processed.
     */
    protected Wrapper wrapper = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the authorization credentials sent with this request.
     */
    public String getAuthorization() {

        return (this.authorization);

    }


    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization The new authorization credentials
     */
    public void setAuthorization(String authorization) {

        this.authorization = authorization;

    }


    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {

        return (this.connector);

    }


    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {

        this.connector = connector;

    }


    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {

        return (this.context);

    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {
        this.context = context;
    }


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.  This method must be implemented by a subclass.
     */
    public ServletRequest getRequest() {

        return (facade);

    }


    /**
     * Return the Response with which this Request is associated.
     */
    public Response getResponse() {

        return (this.response);

    }


    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(Response response) {

        this.response = response;

    }


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    public Socket getSocket() {

        return (this.socket);

    }


    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    public void setSocket(Socket socket) {

        this.socket = socket;

    }


    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream() {

        return (this.input);

    }


    /**
     * Set the input stream associated with this Request.
     *
     * @param input The new input stream
     */
    public void setStream(InputStream input) {

        this.input = input;

    }


    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {

        return (this.wrapper);

    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     *
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {

        this.wrapper = wrapper;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a Locale to the set of preferred Locales for this Request.  The
     * first added Locale will be the first one returned by getLocales().
     *
     * @param locale The new preferred Locale
     */
    public void addLocale(Locale locale) {

        synchronized (locales) {
            locales.add(locale);
        }

    }


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.  The default implementation creates an
     * instance of RequestStream associated with this request, but this can
     * be overridden if necessary.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException {

        return (new RequestStream(this));

    }


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException {

        // If a Reader has been acquired, close it
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                ;
            }
        }

        // If a ServletInputStream has been acquired, close it
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                ;
            }
        }

        // The underlying input stream (perhaps from a socket)
        // is not our responsibility

    }


    /**
     * Return the object bound with the specified name to the internal notes
     * for this request, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    public Object getNote(String name) {

        synchronized (notes) {
            return (notes.get(name));
        }

    }


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this request.
     */
    public Iterator getNoteNames() {

        synchronized (notes) {
            return (notes.keySet().iterator());
        }

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        attributes.clear();
        authorization = null;
        characterEncoding = null;
        // connector is NOT reset when recycling
        contentLength = -1;
        contentType = null;
        context = null;
        input = null;
        locales.clear();
        notes.clear();
        protocol = null;
        reader = null;
        remoteAddr = null;
        remoteHost = null;
        response = null;
        scheme = null;
        secure = false;
        serverName = null;
        serverPort = -1;
        socket = null;
        stream = null;
        wrapper = null;

    }


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this request.
     *
     * @param name Name of the note to be removed
     */
    public void removeNote(String name) {

        synchronized (notes) {
            notes.remove(name);
        }

    }


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {

        this.contentLength = length;

    }


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {

        this.contentType = type;
        if (type.indexOf(';') >= 0)
            characterEncoding = RequestUtil.parseCharacterEncoding(type);

    }


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this request, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    public void setNote(String name, Object value) {

        synchronized (notes) {
            notes.put(name, value);
        }

    }


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    /**
     * Set the IP address of the remote client associated with this Request.
     *
     * @param remoteAddr The remote IP address
     */
    public void setRemoteAddr(String remoteAddr) {

        this.remoteAddr = remoteAddr;

    }


    /**
     * Set the fully qualified name of the remote client associated with this
     * Request.
     *
     * @param remoteHost The remote host name
     */
    public void setRemoteHost(String remoteHost) {

        this.remoteHost = remoteHost;

    }


    /**
     * Set the name of the scheme associated with this request.  Typical values
     * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
     *
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {

        this.scheme = scheme;

    }


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    public void setSecure(boolean secure) {

        this.secure = secure;

    }


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    public void setServerName(String name) {

        this.serverName = name;

    }


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    public void setServerPort(int port) {

        this.serverPort = port;

    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     *
     * @param name Name of the request attribute to return
     */
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Return the names of all request attributes for this Request, or an
     * empty <code>Enumeration</code> if there are none.
     */
    public Enumeration getAttributeNames() {

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Return the character encoding for this Request.
     */
    public String getCharacterEncoding() {

      return (this.characterEncoding);

    }


    /**
     * Return the content length for this Request.
     */
    public int getContentLength() {

        return (this.contentLength);

    }


    /**
     * Return the content type for this Request.
     */
    public String getContentType() {

        return (contentType);

    }


    /**
     * Return the servlet input stream for this Request.  The default
     * implementation returns a servlet input stream created by
     * <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getReader()</code> has
     *  already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream getInputStream() throws IOException {

        if (reader != null)
            throw new IllegalStateException
                (sm.getString("requestBase.getInputStream.ise"));

        if (stream == null)
            stream = createInputStream();
        return (stream);

    }


    /**
     * Return the preferred Locale that the client will accept content in,
     * based on the value for the first <code>Accept-Language</code> header
     * that was encountered.  If the request did not specify a preferred
     * language, the server's default Locale is returned.
     */
    public Locale getLocale() {

        synchronized (locales) {
            if (locales.size() > 0)
                return ((Locale) locales.get(0));
            else
                return (defaultLocale);
        }

    }


    /**
     * Return the set of preferred Locales that the client will accept
     * content in, based on the values for any <code>Accept-Language</code>
     * headers that were encountered.  If the request did not specify a
     * preferred language, the server's default Locale is returned.
     */
    public Enumeration getLocales() {

        synchronized (locales) {
            if (locales.size() > 0)
                return (new Enumerator(locales));
        }
        ArrayList results = new ArrayList();
        results.add(defaultLocale);
        return (new Enumerator(results));

    }


    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public abstract String getParameter(String name);


    /**
     * Returns a <code>Map</code> of the parameters of this request.
     * Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string
     * or posted form data.
     *
     * @return A <code>Map</code> containing parameter names as keys
     *  and parameter values as map values.
     */
    public abstract Map getParameterMap();


    /**
     * Return the names of all defined request parameters for this request.
     */
    public abstract Enumeration getParameterNames();


    /**
     * Return the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired request parameter
     */
    public abstract String[] getParameterValues(String name);


    /**
     * Return the protocol and version used to make this Request.
     */
    public String getProtocol() {

        return (this.protocol);

    }


    /**
     * Read the Reader wrapping the input stream for this Request.  The
     * default implementation wraps a <code>BufferedReader</code> around the
     * servlet input stream returned by <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getInputStream()</code>
     *  has already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public BufferedReader getReader() throws IOException {

        if (stream != null)
            throw new IllegalStateException
                (sm.getString("requestBase.getReader.ise"));

        if (reader == null) {
            String encoding = getCharacterEncoding();
            if (encoding == null)
                encoding = "ISO-8859-1";
            InputStreamReader isr =
                new InputStreamReader(createInputStream(), encoding);
            reader = new BufferedReader(isr);
        }
        return (reader);

    }


    /**
     * Return the real path of the specified virtual path.
     *
     * @param path Path to be translated
     *
     * @deprecated As of version 2.1 of the Java Servlet API, use
     *  <code>ServletContext.getRealPath()</code>.
     */
    public String getRealPath(String path) {

        if (context == null)
            return (null);
        ServletContext servletContext = context.getServletContext();
        if (servletContext == null)
            return (null);
        else {
            try {
                return (servletContext.getRealPath(path));
            } catch (IllegalArgumentException e) {
                return (null);
            }
        }

    }


    /**
     * Return the remote IP address making this Request.
     */
    public String getRemoteAddr() {

        return (this.remoteAddr);

    }


    /**
     * Return the remote host name making this Request.
     */
    public String getRemoteHost() {

        return (this.remoteHost);

    }


    /**
     * Return a RequestDispatcher that wraps the resource at the specified
     * path, which may be interpreted as relative to the current request path.
     *
     * @param path Path of the resource to be wrapped
     */
    public abstract RequestDispatcher getRequestDispatcher(String path);


    /**
     * Return the scheme used to make this Request.
     */
    public String getScheme() {

        return (this.scheme);

    }


    /**
     * Return the server name responding to this Request.
     */
    public String getServerName() {

        return (this.serverName);

    }


    /**
     * Return the server port responding to this Request.
     */
    public int getServerPort() {

        return (this.serverPort);

    }


    /**
     * Was this request received on a secure connection?
     */
    public boolean isSecure() {

        return (this.secure);

    }


    /**
     * Remove the specified request attribute if it exists.
     *
     * @param name Name of the request attribute to remove
     */
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
        }

    }


    /**
     * Set the specified request attribute to the specified value.
     *
     * @param name Name of the request attribute to set
     * @param value The associated value
     */
    public void setAttribute(String name, Object value) {

        // Name cannot be null
        if (name == null)
            throw new IllegalArgumentException
                (sm.getString("requestBase.setAttribute.namenull"));

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        synchronized (attributes) {
            attributes.put(name, value);
        }

    }


    /**
     * Overrides the name of the character encoding used in the body of
     * this request.  This method must be called prior to reading request
     * parameters or reading input using <code>getReader()</code>.
     *
     * @param enc The character encoding to be used
     *
     * @exception UnsupportedEncodingException if the specified encoding
     *  is not supported
     *
     * @since Servlet 2.3
     */
    public void setCharacterEncoding(String enc)
        throws UnsupportedEncodingException {

        // Ensure that the specified encoding is valid
        byte buffer[] = new byte[1];
        buffer[0] = (byte) 'a';
        String dummy = new String(buffer, enc);

        // Save the validated encoding
        this.characterEncoding = enc;

    }


}
