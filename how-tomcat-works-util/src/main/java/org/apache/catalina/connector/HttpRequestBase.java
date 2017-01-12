package org.apache.catalina.connector;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;


/**
 * Convenience base implementation of the <b>HttpRequest</b> interface, which
 * can be used for the Request implementation required by most Connectors that
 * implement the HTTP protocol.  Only the connector-specific methods need to
 * be implemented.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.39 $ $Date: 2002/04/22 00:00:50 $
 * @deprecated
 */

public class HttpRequestBase
    extends RequestBase
    implements HttpRequest, HttpServletRequest {


    protected class PrivilegedGetSession
        implements PrivilegedAction {

        private boolean create;

        PrivilegedGetSession(boolean create) {
            this.create = create;
        }

        public Object run() {
            return doGetSession(create);
        }

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The authentication type used for this request.
     */
    protected String authType = null;


    /**
     * The context path for this request.
     */
    protected String contextPath = "";


    /**
     * The set of cookies associated with this Request.
     */
    protected ArrayList cookies = new ArrayList();


    /**
     * An empty collection to use for returning empty Enumerations.  Do not
     * add any elements to this collection!
     */
    protected static ArrayList empty = new ArrayList();


    /**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     */
    protected SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };


    /**
     * The facade associated with this request.
     */
    protected HttpRequestFacade facade = new HttpRequestFacade(this);


    /**
     * The HTTP headers associated with this Request, keyed by name.  The
     * values are ArrayLists of the corresponding header values.
     */
    protected HashMap headers = new HashMap();


    /**
     * Descriptive information about this HttpRequest implementation.
     */
    protected static final String info =
        "org.apache.catalina.connector.HttpRequestBase/1.0";


    /**
     * The request method associated with this Request.
     */
    protected String method = null;


    /**
     * The parsed parameters for this request.  This is populated only if
     * parameter information is requested via one of the
     * <code>getParameter()</code> family of method calls.  The key is the
     * parameter name, while the value is a String array of values for this
     * parameter.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong> - Once the parameters for a
     * particular request are parsed and stored here, they are not modified.
     * Therefore, application level access to the parameters need not be
     * synchronized.
     */
    protected ParameterMap parameters = null;


    /**
     * Have the parameters for this request been parsed yet?
     */
    protected boolean parsed = false;


    /**
     * The path information for this request.
     */
    protected String pathInfo = null;


    /**
     * The query string for this request.
     */
    protected String queryString = null;


    /**
     * Was the requested session ID received in a cookie?
     */
    protected boolean requestedSessionCookie = false;


    /**
     * The requested session ID (if any) for this request.
     */
    protected String requestedSessionId = null;


    /**
     * Was the requested session ID received in a URL?
     */
    protected boolean requestedSessionURL = false;


    /**
     * The request URI associated with this request.
     */
    protected String requestURI = null;


    /**
     * The decoded request URI associated with this request.
     */
    protected String decodedRequestURI = null;


    /**
     * Was this request received on a secure channel?
     */
    protected boolean secure = false;


    /**
     * The servlet path for this request.
     */
    protected String servletPath = null;


    /**
     * The currently active session for this request.
     */
    protected Session session = null;


    /**
     * The Principal who has been authenticated for this Request.
     */
    protected Principal userPrincipal = null;


    // ------------------------------------------------------------- Properties


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


    // --------------------------------------------------------- Public Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {

        synchronized (cookies) {
            cookies.add(cookie);
        }

    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {

        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values == null) {
                values = new ArrayList();
                headers.put(name, values);
            }
            values.add(value);
        }

    }


    /**
     * Add a parameter name and corresponding set of values to this Request.
     * (This is used when restoring the original request on a form based
     * login).
     *
     * @param name Name of this request parameter
     * @param values Corresponding values for this request parameter
     */
    public void addParameter(String name, String values[]) {

        synchronized (parameters) {
            parameters.put(name, values);
        }

    }


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies() {

        synchronized (cookies) {
            cookies.clear();
        }

    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {

        headers.clear();

    }


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales() {

        locales.clear();

    }


    /**
     * Clear the collection of parameters associated with this Request.
     */
    public void clearParameters() {

        if (parameters != null) {
            parameters.setLocked(false);
            parameters.clear();
        } else {
            parameters = new ParameterMap();
        }

    }


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        super.recycle();
        authType = null;
        contextPath = "";
        cookies.clear();
        headers.clear();
        method = null;
        if (parameters != null) {
            parameters.setLocked(false);
            parameters.clear();
        }
        parsed = false;
        pathInfo = null;
        queryString = null;
        requestedSessionCookie = false;
        requestedSessionId = null;
        requestedSessionURL = false;
        requestURI = null;
        decodedRequestURI = null;
        secure = false;
        servletPath = null;
        session = null;
        userPrincipal = null;

    }


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param authType The authentication type used
     */
    public void setAuthType(String authType) {

        this.authType = authType;

    }


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path) {

        if (path == null)
            this.contextPath = "";
        else
            this.contextPath = path;

    }


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method) {

        this.method = method;

    }


    /**
     * Set the path information for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The path information
     */
    public void setPathInfo(String path) {

        this.pathInfo = path;

    }


    /**
     * Set the query string for this Request.  This will normally be called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param query The query string
     */
    public void setQueryString(String query) {

        this.queryString = query;

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {

        this.requestedSessionCookie = flag;

    }


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {

        this.requestedSessionId = id;

    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {

        this.requestedSessionURL = flag;

    }


    /**
     * Set the unparsed request URI for this Request.  This will normally
     * be called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri) {

        this.requestURI = uri;

    }


    /**
     * Set the flag indicating whether this Request was received on a secure
     * communications link or not.  This will normally be called by the HTTP
     * Connector, when it parses the request headers.
     *
     * @param secure The new secure flag
     */
    public void setSecure(boolean secure) {

        this.secure = secure;

    }


    /**
     * Set the servlet path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The servlet path
     */
    public void setServletPath(String path) {

        this.servletPath = path;

    }


    /**
     * Set the Principal who has been authenticated for this Request.  This
     * value is also used to calculate the value to be returned by the
     * <code>getRemoteUser()</code> method.
     *
     * @param principal The user Principal
     */
    public void setUserPrincipal(Principal principal) {

        this.userPrincipal = principal;

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Parse the parameters of this request, if it has not already occurred.
     * If parameters are present in both the query string and the request
     * content, they are merged.
     */
    protected void parseParameters() {

        if (parsed)
            return;

        ParameterMap results = parameters;
        if (results == null)
            results = new ParameterMap();
        results.setLocked(false);

        String encoding = getCharacterEncoding();
        if (encoding == null)
            encoding = "ISO-8859-1";

        // Parse any parameters specified in the query string
        String queryString = getQueryString();
        try {
            RequestUtil.parseParameters(results, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            ;
        }

        // Parse any parameters specified in the input stream
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
            && (this.stream == null)
            && "application/x-www-form-urlencoded".equals(contentType)) {

            try {
                int max = getContentLength();
                int len = 0;
                byte buf[] = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0 ) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    // FIX ME, mod_jk when sending an HTTP POST will sometimes
                    // have an actual content length received < content length.
                    // Checking for a read of -1 above prevents this code from
                    // going into an infinite loop.  But the bug must be in mod_jk.
                    // Log additional data when this occurs to help debug mod_jk
                    StringBuffer msg = new StringBuffer();
                    msg.append("HttpRequestBase.parseParameters content length mismatch\n");
                    msg.append("  URL: ");
                    msg.append(getRequestURL());
                    msg.append(" Content Length: ");
                    msg.append(max);
                    msg.append(" Read: ");
                    msg.append(len);
                    msg.append("\n  Bytes Read: ");
                    if ( len > 0 ) {
                        msg.append(new String(buf,0,len));
                    }
                    log(msg.toString());
                    throw new RuntimeException
                        (sm.getString("httpRequestBase.contentLengthMismatch"));
                }
                RequestUtil.parseParameters(results, buf, encoding);
            } catch (UnsupportedEncodingException ue) {
                ;
            } catch (IOException e) {
                throw new RuntimeException
                        (sm.getString("httpRequestBase.contentReadFail") +
                         e.getMessage());
            }
        }

        // Store the final results
        results.setLocked(true);
        parsed = true;
        parameters = results;

    }


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public String getParameter(String name) {

        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values[0]);
        else
            return (null);

    }


    /**
     * Returns a <code>Map</code> of the parameters of this request.
     * Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string
     * or posted form data.
     *
     * @return A <code>Map</code> containing parameter names as keys
     *  and parameter values as map values.
     */
    public Map getParameterMap() {

        parseParameters();
        return (this.parameters);

    }


    /**
     * Return the names of all defined request parameters for this request.
     */
    public Enumeration getParameterNames() {

        parseParameters();
        return (new Enumerator(parameters.keySet()));

    }


    /**
     * Return the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired request parameter
     */
    public String[] getParameterValues(String name) {

        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values);
        else
            return (null);

    }


    /**
     * Return a RequestDispatcher that wraps the resource at the specified
     * path, which may be interpreted as relative to the current request path.
     *
     * @param path Path of the resource to be wrapped
     */
    public RequestDispatcher getRequestDispatcher(String path) {

        if (context == null)
            return (null);

        // If the path is already context-relative, just pass it through
        if (path == null)
            return (null);
        else if (path.startsWith("/"))
            return (context.getServletContext().getRequestDispatcher(path));

        // Convert a request-relative path to a context-relative one
        String servletPath = (String) getAttribute(Globals.SERVLET_PATH_ATTR);
        if (servletPath == null)
            servletPath = getServletPath();

        int pos = servletPath.lastIndexOf('/');
        String relative = null;
        if (pos >= 0) {
            relative = RequestUtil.normalize
                (servletPath.substring(0, pos + 1) + path);
        } else {
            relative = RequestUtil.normalize(servletPath + path);
        }

        return (context.getServletContext().getRequestDispatcher(relative));

    }


    /**
     * Was this request received on a secure connection?
     */
    public boolean isSecure() {

        return (secure);

    }


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Return the authentication type used for this Request.
     */
    public String getAuthType() {

        return (authType);

    }


    /**
     * Return the portion of the request URI used to select the Context
     * of the Request.
     */
    public String getContextPath() {

        return (contextPath);

    }


    /**
     * Return the set of Cookies received with this Request.
     */
    public Cookie[] getCookies() {

        synchronized (cookies) {
            if (cookies.size() < 1)
                return (null);
            Cookie results[] = new Cookie[cookies.size()];
            return ((Cookie[]) cookies.toArray(results));
        }

    }


    /**
     * Return the value of the specified date header, if any; otherwise
     * return -1.
     *
     * @param name Name of the requested date header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to a date
     */
    public long getDateHeader(String name) {

        String value = getHeader(name);
        if (value == null)
            return (-1L);

        // Work around a bug in SimpleDateFormat in pre-JDK1.2b4
        // (Bug Parade bug #4106807)
        value += " ";

        // Attempt to convert the date header in a variety of formats
        for (int i = 0; i < formats.length; i++) {
            try {
                Date date = formats[i].parse(value);
                return (date.getTime());
            } catch (ParseException e) {
                ;
            }
        }
        throw new IllegalArgumentException(value);

    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {

        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values != null)
                return ((String) values.get(0));
            else
                return (null);
        }

    }


    /**
     * Return all of the values of the specified header, if any; otherwise,
     * return an empty enumeration.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {

        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values != null)
                return (new Enumerator(values));
            else
                return (new Enumerator(empty));
        }

    }


    /**
     * Return the names of all headers received with this request.
     */
    public Enumeration getHeaderNames() {

        synchronized (headers) {
            return (new Enumerator(headers.keySet()));
        }

    }


    /**
     * Return the value of the specified header as an integer, or -1 if there
     * is no such header for this request.
     *
     * @param name Name of the requested header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to an integer
     */
    public int getIntHeader(String name) {

        String value = getHeader(name);
        if (value == null)
            return (-1);
        else
            return (Integer.parseInt(value));

    }


    /**
     * Return the HTTP request method used in this Request.
     */
    public String getMethod() {

        return (method);

    }


    /**
     * Return the path information associated with this Request.
     */
    public String getPathInfo() {

        return (pathInfo);

    }


    /**
     * Return the extra path information for this request, translated
     * to a real path.
     */
    public String getPathTranslated() {

        if (context == null)
            return (null);

        if (pathInfo == null)
            return (null);
        else
            return (context.getServletContext().getRealPath(pathInfo));

    }


    /**
     * Return the query string associated with this request.
     */
    public String getQueryString() {

        return (queryString);

    }


    /**
     * Return the name of the remote user that has been authenticated
     * for this Request.
     */
    public String getRemoteUser() {

        if (userPrincipal != null)
            return (userPrincipal.getName());
        else
            return (null);

    }


    /**
     * Return the session identifier included in this request, if any.
     */
    public String getRequestedSessionId() {

        return (requestedSessionId);

    }


    /**
     * Return the request URI for this request.
     */
    public String getRequestURI() {

        return (requestURI);

    }


    /**
     * Set the decoded request URI.
     *
     * @param uri The decoded request URI
     */
    public void setDecodedRequestURI(String uri) {

        this.decodedRequestURI = uri;

    }


    /**
     * Return the URL decoded request URI.
     */
    public String getDecodedRequestURI() {

        if (decodedRequestURI == null)
            decodedRequestURI = RequestUtil.URLDecode(getRequestURI());

        return decodedRequestURI;

    }


    /**
     * Reconstructs the URL the client used to make the request.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     * <p>
     * Because this method returns a <code>StringBuffer</code>,
     * not a <code>String</code>, you can modify the URL easily,
     * for example, to append query parameters.
     * <p>
     * This method is useful for creating redirect messages and
     * for reporting errors.
     *
     * @return A <code>StringBuffer</code> object containing the
     *  reconstructed URL
     */
    public StringBuffer getRequestURL() {

        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0)
            port = 80; // Work around java.net.URL bug

        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if ((scheme.equals("http") && (port != 80))
            || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());

        return (url);

    }


    /**
     * Return the portion of the request URI used to select the servlet
     * that will process this request.
     */
    public String getServletPath() {

        return (servletPath);

    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public HttpSession getSession() {

        return (getSession(true));

    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public HttpSession getSession(boolean create) {
        if( System.getSecurityManager() != null ) {
            PrivilegedGetSession dp = new PrivilegedGetSession(create);
            return (HttpSession)AccessController.doPrivileged(dp);
        }
        return doGetSession(create);
    }

    private HttpSession doGetSession(boolean create) {
        // There cannot be a session if no context has been assigned yet
        if (context == null)
            return (null);

        // Return the current session if it exists and is valid
        if ((session != null) && !session.isValid())
            session = null;
        if (session != null)
            return (session.getSession());


        // Return the requested session if it exists and is valid
        Manager manager = null;
        if (context != null)
            manager = context.getManager();

        if (manager == null)
            return (null);      // Sessions are not supported

        if (requestedSessionId != null) {
            try {
                session = manager.findSession(requestedSessionId);
            } catch (IOException e) {
                session = null;
            }
            if ((session != null) && !session.isValid())
                session = null;
            if (session != null) {
                return (session.getSession());
            }
        }

        // Create a new session if requested and the response is not committed
        if (!create)
            return (null);
        if ((context != null) && (response != null) &&
            context.getCookies() &&
            response.getResponse().isCommitted()) {
            throw new IllegalStateException
              (sm.getString("httpRequestBase.createCommitted"));
        }

        session = manager.createSession();
        if (session != null)
            return (session.getSession());
        else
            return (null);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from a cookie.
     */
    public boolean isRequestedSessionIdFromCookie() {

        if (requestedSessionId != null)
            return (requestedSessionCookie);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     */
    public boolean isRequestedSessionIdFromURL() {

        if (requestedSessionId != null)
            return (requestedSessionURL);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     *  <code>isRequestedSessionIdFromURL()</code> instead.
     */
    public boolean isRequestedSessionIdFromUrl() {

        return (isRequestedSessionIdFromURL());

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request identifies a valid session.
     */
    public boolean isRequestedSessionIdValid() {

        if (requestedSessionId == null)
            return (false);
        if (context == null)
            return (false);
        Manager manager = context.getManager();
        if (manager == null)
            return (false);
        Session session = null;
        try {
            session = manager.findSession(requestedSessionId);
        } catch (IOException e) {
            session = null;
        }
        if ((session != null) && session.isValid())
            return (true);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the authenticated user principal
     * possesses the specified role name.
     *
     * @param role Role name to be validated
     */
    public boolean isUserInRole(String role) {

        // Have we got an authenticated principal at all?
        if (userPrincipal == null)
            return (false);

        // Identify the Realm we will use for checking role assignmenets
        if (context == null)
            return (false);
        Realm realm = context.getRealm();
        if (realm == null)
            return (false);

        // Check for a role alias defined in a <security-role-ref> element
        if (wrapper != null) {
            String realRole = wrapper.findSecurityReference(role);
            if ((realRole != null) &&
                realm.hasRole(userPrincipal, realRole))
                return (true);
        }

        // Check for a role defined directly as a <security-role>
        return (realm.hasRole(userPrincipal, role));

    }


    /**
     * Return the principal that has been authenticated for this Request.
     */
    public Principal getUserPrincipal() {

        return (userPrincipal);

    }


    private void log(String message) {
        Logger logger = context.getLogger();
        logger.log(message);
    }


    private void log(String message, Throwable throwable) {
        Logger logger = context.getLogger();
        logger.log(message, throwable);
    }

}
