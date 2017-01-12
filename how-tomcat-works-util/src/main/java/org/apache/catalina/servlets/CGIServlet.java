/*
 * CGIServlet.java $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/servlets/CGIServlet.java,v 1.9 2002/09/05 21:46:54 amyroh Exp $
 * $Revision: 1.9 $, $Date: 2002/09/05 21:46:54 $
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
 *
 */


package org.apache.catalina.servlets;

import java.lang.Process;
import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import org.apache.catalina.Globals;


/**
 *  CGI-invoking servlet for web applications, used to execute scripts which
 *  comply to the Common Gateway Interface (CGI) specification and are named
 *  in the path-info used to invoke this servlet.
 *
 * <p>
 * <i>Note: This code compiles and even works for simple CGI cases.
 *          Exhaustive testing has not been done.  Please consider it beta
 *          quality.  Feedback is appreciated to the author (see below).</i>
 * </p>
 * <p>
 *
 * <b>Example</b>:<br>
 * If an instance of this servlet was mapped (using
 *       <code>&lt;web-app&gt;/WEB-INF/web.xml</code>) to:
 * </p>
 * <p>
 * <code>
 * &lt;web-app&gt;/cgi-bin/*
 * </code>
 * </p>
 * <p>
 * then the following request:
 * </p>
 * <p>
 * <code>
 * http://localhost:8080/&lt;web-app&gt;/cgi-bin/dir1/script/pathinfo1
 * </code>
 * </p>
 * <p>
 * would result in the execution of the script
 * </p>
 * <p>
 * <code>
 * &lt;web-app-root&gt;/WEB-INF/cgi/dir1/script
 * </code>
 * </p>
 * <p>
 * with the script's <code>PATH_INFO</code> set to <code>/pathinfo1</code>.
 * </p>
 * <p>
 * Recommendation:  House all your CGI scripts under
 * <code>&lt;webapp&gt;/WEB-INF/cgi</code>.  This will ensure that you do not
 * accidentally expose your cgi scripts' code to the outside world and that
 * your cgis will be cleanly ensconced underneath the WEB-INF (i.e.,
 * non-content) area.
 * </p>
 * <p>
 * The default CGI location is mentioned above.  You have the flexibility to
 * put CGIs wherever you want, however:
 * </p>
 * <p>
 *   The CGI search path will start at
 *   webAppRootDir + File.separator + cgiPathPrefix
 *   (or webAppRootDir alone if cgiPathPrefix is
 *   null).
 * </p>
 * <p>
 *   cgiPathPrefix is defined by setting
 *   this servlet's cgiPathPrefix init parameter
 * </p>
 *
 * <p>
 *
 * <B>CGI Specification</B>:<br> derived from
 * <a href="http://cgi-spec.golux.com">http://cgi-spec.golux.com</a>.
 * A work-in-progress & expired Internet Draft.  Note no actual RFC describing
 * the CGI specification exists.  Where the behavior of this servlet differs
 * from the specification cited above, it is either documented here, a bug,
 * or an instance where the specification cited differs from Best
 * Community Practice (BCP).
 * Such instances should be well-documented here.  Please email the
 * <a href="mailto:tomcat-dev@jakarta.apache.org">Jakarta Tomcat group [tomcat-dev@jakarta.apache.org]</a>
 * with amendments.
 *
 * </p>
 * <p>
 *
 * <b>Canonical metavariables</b>:<br>
 * The CGI specification defines the following canonical metavariables:
 * <br>
 * [excerpt from CGI specification]
 * <PRE>
 *  AUTH_TYPE
 *  CONTENT_LENGTH
 *  CONTENT_TYPE
 *  GATEWAY_INTERFACE
 *  PATH_INFO
 *  PATH_TRANSLATED
 *  QUERY_STRING
 *  REMOTE_ADDR
 *  REMOTE_HOST
 *  REMOTE_IDENT
 *  REMOTE_USER
 *  REQUEST_METHOD
 *  SCRIPT_NAME
 *  SERVER_NAME
 *  SERVER_PORT
 *  SERVER_PROTOCOL
 *  SERVER_SOFTWARE
 * </PRE>
 * <p>
 * Metavariables with names beginning with the protocol name (<EM>e.g.</EM>,
 * "HTTP_ACCEPT") are also canonical in their description of request header
 * fields.  The number and meaning of these fields may change independently
 * of this specification.  (See also section 6.1.5 [of the CGI specification].)
 * </p>
 * [end excerpt]
 *
 * </p>
 * <h2> Implementation notes</h2>
 * <p>
 *
 * <b>standard input handling</b>: If your script accepts standard input,
 * then the client must start sending input within a certain timeout period,
 * otherwise the servlet will assume no input is coming and carry on running
 * the script.  The script's the standard input will be closed and handling of
 * any further input from the client is undefined.  Most likely it will be
 * ignored.  If this behavior becomes undesirable, then this servlet needs
 * to be enhanced to handle threading of the spawned process' stdin, stdout,
 * and stderr (which should not be too hard).
 * <br>
 * If you find your cgi scripts are timing out receiving input, you can set
 * the init parameter <code></code> of your webapps' cgi-handling servlet
 * to be
 * </p>
 * <p>
 *
 * <b>Metavariable Values</b>: According to the CGI specificion,
 * implementations may choose to represent both null or missing values in an
 * implementation-specific manner, but must define that manner.  This
 * implementation chooses to always define all required metavariables, but
 * set the value to "" for all metavariables whose value is either null or
 * undefined.  PATH_TRANSLATED is the sole exception to this rule, as per the
 * CGI Specification.
 *
 * </p>
 * <p>
 *
 * <b>NPH --  Non-parsed-header implementation</b>:  This implementation does
 * not support the CGI NPH concept, whereby server ensures that the data
 * supplied to the script are preceisely as supplied by the client and
 * unaltered by the server.
 * </p>
 * <p>
 * The function of a servlet container (including Tomcat) is specifically
 * designed to parse and possible alter CGI-specific variables, and as
 * such makes NPH functionality difficult to support.
 * </p>
 * <p>
 * The CGI specification states that compliant servers MAY support NPH output.
 * It does not state servers MUST support NPH output to be unconditionally
 * compliant.  Thus, this implementation maintains unconditional compliance
 * with the specification though NPH support is not present.
 * </p>
 * <p>
 *
 * The CGI specification is located at
 * <a href="http://cgi-spec.golux.com">http://cgi-spec.golux.com</a>.
 *
 * </p>
 * <p>
 * <h3>TODO:</h3>
 * <ul>
 * <li> Support for setting headers (for example, Location headers don't work)
 * <li> Support for collapsing multiple header lines (per RFC 2616)
 * <li> Ensure handling of POST method does not interfere with 2.3 Filters
 * <li> Refactor some debug code out of core
 * <li> Ensure header handling preserves encoding
 * <li> Possibly rewrite CGIRunner.run()?
 * <li> Possibly refactor CGIRunner and CGIEnvironment as non-inner classes?
 * <li> Document handling of cgi stdin when there is no stdin
 * <li> Revisit IOException handling in CGIRunner.run()
 * <li> Better documentation
 * <li> Confirm use of ServletInputStream.available() in CGIRunner.run() is
 *      not needed
 * <li> Make checking for "." and ".." in servlet & cgi PATH_INFO less
 *      draconian
 * <li> [add more to this TODO list]
 * </ul>
 * </p>
 *
 * @author Martin T Dengler [root@martindengler.com]
 * @author Amy Roh
 * @version $Revision: 1.9 $, $Date: 2002/09/05 21:46:54 $
 * @since Tomcat 4.0
 *
 */


public class CGIServlet extends HttpServlet {


    /* some vars below copied from Craig R. McClanahan's InvokerServlet */

    /** the string manager for this package. */
    /* YAGNI
    private static StringManager sm =
        StringManager.getManager(Constants.Package);
    */

    /** the Context container associated with our web application. */
    private ServletContext context = null;

    /** the debugging detail level for this servlet. */
    private int debug = 0;

    /** the time in ms to wait for the client to send us CGI input data */
    private int iClientInputTimeout = 100;

    /**
     *  The CGI search path will start at
     *    webAppRootDir + File.separator + cgiPathPrefix
     *    (or webAppRootDir alone if cgiPathPrefix is
     *    null)
     */
    private String cgiPathPrefix = null;


    /**
     * Sets instance variables.
     * <P>
     * Modified from Craig R. McClanahan's InvokerServlet
     * </P>
     *
     * @param config                    a <code>ServletConfig</code> object
     *                                  containing the servlet's
     *                                  configuration and initialization
     *                                  parameters
     *
     * @exception ServletException      if an exception has occurred that
     *                                  interferes with the servlet's normal
     *                                  operation
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        // Verify that we were not accessed using the invoker servlet
        String servletName = getServletConfig().getServletName();
        if (servletName == null)
            servletName = "";
        if (servletName.startsWith("org.apache.catalina.INVOKER."))
            throw new UnavailableException
                ("Cannot invoke CGIServlet through the invoker");

        // Set our properties from the initialization parameters
        String value = null;
        try {
            value = getServletConfig().getInitParameter("debug");
            debug = Integer.parseInt(value);
            cgiPathPrefix =
                getServletConfig().getInitParameter("cgiPathPrefix");
            value =
                getServletConfig().getInitParameter("iClientInputTimeout");
            iClientInputTimeout = Integer.parseInt(value);
        } catch (Throwable t) {
            //NOOP
        }
        log("init: loglevel set to " + debug);

        // Identify the internal container resources we need
        //Wrapper wrapper = (Wrapper) getServletConfig();
        //context = (Context) wrapper.getParent();

    context = config.getServletContext();
        if (debug >= 1) {
            //log("init: Associated with Context '" + context.getPath() + "'");
        }

    }



    /**
     * Prints out important Servlet API and container information
     *
     * <p>
     * Copied from SnoopAllServlet by Craig R. McClanahan
     * </p>
     *
     * @param  out    ServletOutputStream as target of the information
     * @param  req    HttpServletRequest object used as source of information
     * @param  res    HttpServletResponse object currently not used but could
     *                provide future information
     *
     * @exception  IOException  if a write operation exception occurs
     *
     */
    protected void printServletEnvironment(ServletOutputStream out,
        HttpServletRequest req, HttpServletResponse res) throws IOException {

        // Document the properties from ServletRequest
        out.println("<h1>ServletRequest Properties</h1>");
        out.println("<ul>");
        Enumeration attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = (String) attrs.nextElement();
            out.println("<li><b>attribute</b> " + attr + " = " +
                           req.getAttribute(attr));
        }
        out.println("<li><b>characterEncoding</b> = " +
                       req.getCharacterEncoding());
        out.println("<li><b>contentLength</b> = " +
                       req.getContentLength());
        out.println("<li><b>contentType</b> = " +
                       req.getContentType());
        Enumeration locales = req.getLocales();
        while (locales.hasMoreElements()) {
            Locale locale = (Locale) locales.nextElement();
            out.println("<li><b>locale</b> = " + locale);
        }
        Enumeration params = req.getParameterNames();
        while (params.hasMoreElements()) {
            String param = (String) params.nextElement();
            String values[] = req.getParameterValues(param);
            for (int i = 0; i < values.length; i++)
                out.println("<li><b>parameter</b> " + param + " = " +
                               values[i]);
        }
        out.println("<li><b>protocol</b> = " + req.getProtocol());
        out.println("<li><b>remoteAddr</b> = " + req.getRemoteAddr());
        out.println("<li><b>remoteHost</b> = " + req.getRemoteHost());
        out.println("<li><b>scheme</b> = " + req.getScheme());
        out.println("<li><b>secure</b> = " + req.isSecure());
        out.println("<li><b>serverName</b> = " + req.getServerName());
        out.println("<li><b>serverPort</b> = " + req.getServerPort());
        out.println("</ul>");
        out.println("<hr>");

        // Document the properties from HttpServletRequest
        out.println("<h1>HttpServletRequest Properties</h1>");
        out.println("<ul>");
        out.println("<li><b>authType</b> = " + req.getAuthType());
        out.println("<li><b>contextPath</b> = " +
                       req.getContextPath());
        Cookie cookies[] = req.getCookies();
    if (cookies!=null) {
        for (int i = 0; i < cookies.length; i++)
                out.println("<li><b>cookie</b> " + cookies[i].getName() +" = " +cookies[i].getValue());
    }
        Enumeration headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            out.println("<li><b>header</b> " + header + " = " +
                           req.getHeader(header));
        }
        out.println("<li><b>method</b> = " + req.getMethod());
        out.println("<li><a name=\"pathInfo\"><b>pathInfo</b></a> = "
                    + req.getPathInfo());
        out.println("<li><b>pathTranslated</b> = " +
                       req.getPathTranslated());
        out.println("<li><b>queryString</b> = " +
                       req.getQueryString());
        out.println("<li><b>remoteUser</b> = " +
                       req.getRemoteUser());
        out.println("<li><b>requestedSessionId</b> = " +
                       req.getRequestedSessionId());
        out.println("<li><b>requestedSessionIdFromCookie</b> = " +
                       req.isRequestedSessionIdFromCookie());
        out.println("<li><b>requestedSessionIdFromURL</b> = " +
                       req.isRequestedSessionIdFromURL());
        out.println("<li><b>requestedSessionIdValid</b> = " +
                       req.isRequestedSessionIdValid());
        out.println("<li><b>requestURI</b> = " +
                       req.getRequestURI());
        out.println("<li><b>servletPath</b> = " +
                       req.getServletPath());
        out.println("<li><b>userPrincipal</b> = " +
                       req.getUserPrincipal());
        out.println("</ul>");
        out.println("<hr>");

        // Document the servlet request attributes
        out.println("<h1>ServletRequest Attributes</h1>");
        out.println("<ul>");
        attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = (String) attrs.nextElement();
            out.println("<li><b>" + attr + "</b> = " +
                           req.getAttribute(attr));
        }
        out.println("</ul>");
        out.println("<hr>");

        // Process the current session (if there is one)
        HttpSession session = req.getSession(false);
        if (session != null) {

            // Document the session properties
            out.println("<h1>HttpSession Properties</h1>");
            out.println("<ul>");
            out.println("<li><b>id</b> = " +
                           session.getId());
            out.println("<li><b>creationTime</b> = " +
                           new Date(session.getCreationTime()));
            out.println("<li><b>lastAccessedTime</b> = " +
                           new Date(session.getLastAccessedTime()));
            out.println("<li><b>maxInactiveInterval</b> = " +
                           session.getMaxInactiveInterval());
            out.println("</ul>");
            out.println("<hr>");

            // Document the session attributes
            out.println("<h1>HttpSession Attributes</h1>");
            out.println("<ul>");
            attrs = session.getAttributeNames();
            while (attrs.hasMoreElements()) {
                String attr = (String) attrs.nextElement();
                out.println("<li><b>" + attr + "</b> = " +
                               session.getAttribute(attr));
            }
            out.println("</ul>");
            out.println("<hr>");

        }

        // Document the servlet configuration properties
        out.println("<h1>ServletConfig Properties</h1>");
        out.println("<ul>");
        out.println("<li><b>servletName</b> = " +
                       getServletConfig().getServletName());
        out.println("</ul>");
        out.println("<hr>");

        // Document the servlet configuration initialization parameters
        out.println("<h1>ServletConfig Initialization Parameters</h1>");
        out.println("<ul>");
        params = getServletConfig().getInitParameterNames();
        while (params.hasMoreElements()) {
            String param = (String) params.nextElement();
            String value = getServletConfig().getInitParameter(param);
            out.println("<li><b>" + param + "</b> = " + value);
        }
        out.println("</ul>");
        out.println("<hr>");

        // Document the servlet context properties
        out.println("<h1>ServletContext Properties</h1>");
        out.println("<ul>");
        out.println("<li><b>majorVersion</b> = " +
                       getServletContext().getMajorVersion());
        out.println("<li><b>minorVersion</b> = " +
                       getServletContext().getMinorVersion());
        out.println("<li><b>realPath('/')</b> = " +
                       getServletContext().getRealPath("/"));
        out.println("<li><b>serverInfo</b> = " +
                       getServletContext().getServerInfo());
        out.println("</ul>");
        out.println("<hr>");

        // Document the servlet context initialization parameters
        out.println("<h1>ServletContext Initialization Parameters</h1>");
        out.println("<ul>");
        params = getServletContext().getInitParameterNames();
        while (params.hasMoreElements()) {
            String param = (String) params.nextElement();
            String value = getServletContext().getInitParameter(param);
            out.println("<li><b>" + param + "</b> = " + value);
        }
        out.println("</ul>");
        out.println("<hr>");

        // Document the servlet context attributes
        out.println("<h1>ServletContext Attributes</h1>");
        out.println("<ul>");
        attrs = getServletContext().getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = (String) attrs.nextElement();
            out.println("<li><b>" + attr + "</b> = " +
                           getServletContext().getAttribute(attr));
        }
        out.println("</ul>");
        out.println("<hr>");



    }



    /**
     * Provides CGI Gateway service -- delegates to <code>doGet</code>
     *
     * @param  req   HttpServletRequest passed in by servlet container
     * @param  res   HttpServletResponse passed in by servlet container
     *
     * @exception  ServletException  if a servlet-specific exception occurs
     * @exception  IOException  if a read/write exception occurs
     *
     * @see javax.servlet.http.HttpServlet
     *
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException {
        doGet(req, res);
    }



    /**
     * Provides CGI Gateway service
     *
     * @param  req   HttpServletRequest passed in by servlet container
     * @param  res   HttpServletResponse passed in by servlet container
     *
     * @exception  ServletException  if a servlet-specific exception occurs
     * @exception  IOException  if a read/write exception occurs
     *
     * @see javax.servlet.http.HttpServlet
     *
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        // Verify that we were not accessed using the invoker servlet
        if (req.getAttribute(Globals.INVOKED_ATTR) != null)
            throw new UnavailableException
                ("Cannot invoke CGIServlet through the invoker");

        CGIEnvironment cgiEnv = new CGIEnvironment(req, getServletContext());

        if (cgiEnv.isValid()) {
            CGIRunner cgi = new CGIRunner(cgiEnv.getCommand(),
                                          cgiEnv.getEnvironment(),
                                          cgiEnv.getWorkingDirectory(),
                                          cgiEnv.getParameters());
            //if POST, we need to cgi.setInput
            //REMIND: how does this interact with Servlet API 2.3's Filters?!
            if ("POST".equals(req.getMethod())) {
                cgi.setInput(req.getInputStream());
            }
            cgi.setResponse(res);
            cgi.run();
        }


        //REMIND: change to debug method or something
        if ((req.getParameter("X_TOMCAT_CGI_DEBUG") != null)
            || (!cgiEnv.isValid())) {
            try {
                ServletOutputStream out = res.getOutputStream();
                out.println("<HTML><HEAD><TITLE>$Name:  $</TITLE></HEAD>");
                out.println("<BODY>$Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/servlets/CGIServlet.java,v 1.9 2002/09/05 21:46:54 amyroh Exp $<p>");

                if (cgiEnv.isValid()) {
                    out.println(cgiEnv.toString());
                } else {
                    res.setStatus(404);
                    out.println("<H3>");
                    out.println("CGI script not found or not specified.");
                    out.println("</H3>");
                    out.println("<H4>");
                    out.println("Check the <b>HttpServletRequest ");
                    out.println("<a href=\"#pathInfo\">pathInfo</a></b> ");
                    out.println("property to see if it is what you meant ");
                    out.println("it to be.  You must specify an existant ");
                    out.println("and executable file as part of the ");
                    out.println("path-info.");
                    out.println("</H4>");
                    out.println("<H4>");
                    out.println("For a good discussion of how CGI scripts ");
                    out.println("work and what their environment variables ");
                    out.println("mean, please visit the <a ");
                    out.println("href=\"http://cgi-spec.golux.com\">CGI ");
                    out.println("Specification page</a>.");
                    out.println("</H4>");

                }

                printServletEnvironment(out, req, res);

                out.println("</BODY></HTML>");

            } catch (IOException ignored) {
            }

        } //debugging


    } //doGet



    /** For future testing use only; does nothing right now */
    public static void main(String[] args) {
        System.out.println("$Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/servlets/CGIServlet.java,v 1.9 2002/09/05 21:46:54 amyroh Exp $");
    }






    /**
     * Encapsulates the CGI environment and rules to derive
     * that environment from the servlet container and request information.
     *
     * <p>
     * </p>
     *
     * @author   Martin Dengler [root@martindengler.com]
     * @version  $Revision: 1.9 $, $Date: 2002/09/05 21:46:54 $
     * @since    Tomcat 4.0
     *
     */
    protected class CGIEnvironment {


        /** context of the enclosing servlet */
        private ServletContext context = null;

        /** context path of enclosing servlet */
        private String contextPath = null;

        /** servlet URI of the enclosing servlet */
        private String servletPath = null;

        /** pathInfo for the current request */
        private String pathInfo = null;

        /** real file system directory of the enclosing servlet's web app */
        private String webAppRootDir = null;

        /** derived cgi environment */
        private Hashtable env = null;

        /** cgi command to be invoked */
        private String command = null;

        /** cgi command's desired working directory */
        private File workingDirectory = null;

        /** cgi command's query parameters */
        private Hashtable queryParameters = null;

        /** whether or not this object is valid or not */
        private boolean valid = false;


        /**
         * Creates a CGIEnvironment and derives the necessary environment,
         * query parameters, working directory, cgi command, etc.
         *
         * @param  req       HttpServletRequest for information provided by
         *                   the Servlet API
         * @param  context   ServletContext for information provided by the
         *                   Servlet API
         *
         */
        protected CGIEnvironment(HttpServletRequest req,
                                 ServletContext context) {
            setupFromContext(context);
            setupFromRequest(req);

            queryParameters = new Hashtable();
            Enumeration paramNames = req.getParameterNames();
            while (paramNames != null && paramNames.hasMoreElements()) {
                String param = paramNames.nextElement().toString();
                if (param != null) {
                    queryParameters.put(
                        param, URLEncoder.encode(req.getParameter(param)));
                }
            }

            this.valid = setCGIEnvironment(req);

            if (this.valid) {
                workingDirectory = new File(command.substring(0,
                      command.lastIndexOf(File.separator)));
            }

        }



        /**
         * Uses the ServletContext to set some CGI variables
         *
         * @param  context   ServletContext for information provided by the
         *                   Servlet API
         */
        protected void setupFromContext(ServletContext context) {
            this.context = context;
            this.webAppRootDir = context.getRealPath("/");
        }



        /**
         * Uses the HttpServletRequest to set most CGI variables
         *
         * @param  req   HttpServletRequest for information provided by
         *               the Servlet API
         */
        protected void setupFromRequest(HttpServletRequest req) {
            this.contextPath = req.getContextPath();
            this.pathInfo = req.getPathInfo();
            this.servletPath = req.getServletPath();
        }



        /**
         * Resolves core information about the cgi script.
         *
         * <p>
         * Example URI:
         * <PRE> /servlet/cgigateway/dir1/realCGIscript/pathinfo1 </PRE>
         * <ul>
         * <LI><b>path</b> = $CATALINA_HOME/mywebapp/dir1/realCGIscript
         * <LI><b>scriptName</b> = /servlet/cgigateway/dir1/realCGIscript
         * <LI><b>cgiName</b> = /dir1/realCGIscript
         * <LI><b>name</b> = realCGIscript
         * </ul>
         * </p>
         * <p>
         * CGI search algorithm: search the real path below
         *    &lt;my-webapp-root&gt; and find the first non-directory in
         *    the getPathTranslated("/"), reading/searching from left-to-right.
         *</p>
         *<p>
         *   The CGI search path will start at
         *   webAppRootDir + File.separator + cgiPathPrefix
         *   (or webAppRootDir alone if cgiPathPrefix is
         *   null).
         *</p>
         *<p>
         *   cgiPathPrefix is defined by setting
         *   this servlet's cgiPathPrefix init parameter
         *
         *</p>
         *
         * @param pathInfo       String from HttpServletRequest.getPathInfo()
         * @param webAppRootDir  String from context.getRealPath("/")
         * @param contextPath    String as from
         *                       HttpServletRequest.getContextPath()
         * @param servletPath    String as from
         *                       HttpServletRequest.getServletPath()
         * @param cgiPathPrefix  subdirectory of webAppRootDir below which
         *                       the web app's CGIs may be stored; can be null.
         *                       The CGI search path will start at
         *                       webAppRootDir + File.separator + cgiPathPrefix
         *                       (or webAppRootDir alone if cgiPathPrefix is
         *                       null).  cgiPathPrefix is defined by setting
         *                       the servlet's cgiPathPrefix init parameter.
         *
         *
         * @return
         * <ul>
         * <li>
         * <code>path</code> -    full file-system path to valid cgi script,
         *                        or null if no cgi was found
         * <li>
         * <code>scriptName</code> -
         *                        CGI variable SCRIPT_NAME; the full URL path
         *                        to valid cgi script or null if no cgi was
         *                        found
         * <li>
         * <code>cgiName</code> - servlet pathInfo fragment corresponding to
         *                        the cgi script itself, or null if not found
         * <li>
         * <code>name</code> -    simple name (no directories) of the
         *                        cgi script, or null if no cgi was found
         * </ul>
         *
         * @author Martin Dengler [root@martindengler.com]
         * @since Tomcat 4.0
         */
        protected String[] findCGI(String pathInfo, String webAppRootDir,
                                   String contextPath, String servletPath,
                                   String cgiPathPrefix) {
            String path = null;
            String name = null;
            String scriptname = null;
            String cginame = null;

            if ((webAppRootDir != null)
                && (webAppRootDir.lastIndexOf(File.separator) ==
                    (webAppRootDir.length() - 1))) {
                    //strip the trailing "/" from the webAppRootDir
                    webAppRootDir =
                    webAppRootDir.substring(0, (webAppRootDir.length() - 1));
            }

            if (cgiPathPrefix != null) {
                webAppRootDir = webAppRootDir + File.separator
                    + cgiPathPrefix;
            }

            if (debug >= 2) {
                log("findCGI: path=" + pathInfo + ", " + webAppRootDir);
            }

            File currentLocation = new File(webAppRootDir);
            StringTokenizer dirWalker =
            new StringTokenizer(pathInfo, File.separator);
            if (debug >= 3) {
                log("findCGI: currentLoc=" + currentLocation);
            }
            while (!currentLocation.isFile() && dirWalker.hasMoreElements()) {
                if (debug >= 3) {
                    log("findCGI: currentLoc=" + currentLocation);
                }
                currentLocation = new File(currentLocation,
                                           (String) dirWalker.nextElement());
            }
            if (!currentLocation.isFile()) {
                return new String[] { null, null, null, null };
            } else {
                if (debug >= 2) {
                    log("findCGI: FOUND cgi at " + currentLocation);
                }
                path = currentLocation.getAbsolutePath();
                name = currentLocation.getName();
                cginame =
                currentLocation.getParent().substring(webAppRootDir.length())
                + File.separator
                + name;

                if (".".equals(contextPath)) {
                    scriptname = servletPath + cginame;
                } else {
                    scriptname = contextPath + servletPath + cginame;
                }
            }

            if (debug >= 1) {
                log("findCGI calc: name=" + name + ", path=" + path
                    + ", scriptname=" + scriptname + ", cginame=" + cginame);
            }
            return new String[] { path, scriptname, cginame, name };

        }



        /**
         * Constructs the CGI environment to be supplied to the invoked CGI
         * script; relies heavliy on Servlet API methods and findCGI
         *
         * @param    HttpServletRequest request associated with the CGI
         *           invokation
         *
         * @return   true if environment was set OK, false if there
         *           was a problem and no environment was set
         */
        protected boolean setCGIEnvironment(HttpServletRequest req) {

            /*
             * This method is slightly ugly; c'est la vie.
             * "You cannot stop [ugliness], you can only hope to contain [it]"
             * (apologies to Marv Albert regarding MJ)
             */

            Hashtable envp = new Hashtable();

            String sPathInfoOrig = null;
            String sPathTranslatedOrig = null;
            String sPathInfoCGI = null;
            String sPathTranslatedCGI = null;
            String sCGIFullPath = null;
            String sCGIScriptName = null;
            String sCGIFullName = null;
            String sCGIName = null;
            String[] sCGINames;


            sPathInfoOrig = this.pathInfo;
            sPathInfoOrig = sPathInfoOrig == null ? "" : sPathInfoOrig;

            sPathTranslatedOrig = req.getPathTranslated();
            sPathTranslatedOrig =
                sPathTranslatedOrig == null ? "" : sPathTranslatedOrig;

            sCGINames = findCGI(sPathInfoOrig,
                                webAppRootDir,
                                contextPath,
                                servletPath,
                                cgiPathPrefix);

            sCGIFullPath = sCGINames[0];
            sCGIScriptName = sCGINames[1];
            sCGIFullName = sCGINames[2];
            sCGIName = sCGINames[3];

            if (sCGIFullPath == null
                || sCGIScriptName == null
                || sCGIFullName == null
                || sCGIName == null) {
                return false;
            }

            envp.put("SERVER_SOFTWARE", "TOMCAT");

            envp.put("SERVER_NAME", nullsToBlanks(req.getServerName()));

            envp.put("GATEWAY_INTERFACE", "CGI/1.1");

            envp.put("SERVER_PROTOCOL", nullsToBlanks(req.getProtocol()));

            int port = req.getServerPort();
            Integer iPort = (port == 0 ? new Integer(-1) : new Integer(port));
            envp.put("SERVER_PORT", iPort.toString());

            envp.put("REQUEST_METHOD", nullsToBlanks(req.getMethod()));



            /*-
             * PATH_INFO should be determined by using sCGIFullName:
             * 1) Let sCGIFullName not end in a "/" (see method findCGI)
             * 2) Let sCGIFullName equal the pathInfo fragment which
             *    corresponds to the actual cgi script.
             * 3) Thus, PATH_INFO = request.getPathInfo().substring(
             *                      sCGIFullName.length())
             *
             * (see method findCGI, where the real work is done)
             *
             */
            if (pathInfo == null
                || (pathInfo.substring(sCGIFullName.length()).length() <= 0)) {
                sPathInfoCGI = "";
            } else {
                sPathInfoCGI = pathInfo.substring(sCGIFullName.length());
            }
            envp.put("PATH_INFO", sPathInfoCGI);


            /*-
             * PATH_TRANSLATED must be determined after PATH_INFO (and the
             * implied real cgi-script) has been taken into account.
             *
             * The following example demonstrates:
             *
             * servlet info   = /servlet/cgigw/dir1/dir2/cgi1/trans1/trans2
             * cgifullpath    = /servlet/cgigw/dir1/dir2/cgi1
             * path_info      = /trans1/trans2
             * webAppRootDir  = servletContext.getRealPath("/")
             *
             * path_translated = servletContext.getRealPath("/trans1/trans2")
             *
             * That is, PATH_TRANSLATED = webAppRootDir + sPathInfoCGI
             * (unless sPathInfoCGI is null or blank, then the CGI
             * specification dictates that the PATH_TRANSLATED metavariable
             * SHOULD NOT be defined.
             *
             */
            if (sPathInfoCGI != null && !("".equals(sPathInfoCGI))) {
                sPathTranslatedCGI = context.getRealPath(sPathInfoCGI);
            } else {
                sPathTranslatedCGI = null;
            }
            if (sPathTranslatedCGI == null || "".equals(sPathTranslatedCGI)) {
                //NOOP
            } else {
                envp.put("PATH_TRANSLATED", nullsToBlanks(sPathTranslatedCGI));
            }


            envp.put("SCRIPT_NAME", nullsToBlanks(sCGIScriptName));

            envp.put("QUERY_STRING", nullsToBlanks(req.getQueryString()));

            envp.put("REMOTE_HOST", nullsToBlanks(req.getRemoteHost()));

            envp.put("REMOTE_ADDR", nullsToBlanks(req.getRemoteAddr()));

            envp.put("AUTH_TYPE", nullsToBlanks(req.getAuthType()));

            envp.put("REMOTE_USER", nullsToBlanks(req.getRemoteUser()));

            envp.put("REMOTE_IDENT", ""); //not necessary for full compliance

            envp.put("CONTENT_TYPE", nullsToBlanks(req.getContentType()));


            /* Note CGI spec says CONTENT_LENGTH must be NULL ("") or undefined
             * if there is no content, so we cannot put 0 or -1 in as per the
             * Servlet API spec.
             */
            int contentLength = req.getContentLength();
            String sContentLength = (contentLength <= 0 ? "" :
                                     (new Integer(contentLength)).toString());
            envp.put("CONTENT_LENGTH", sContentLength);


            Enumeration headers = req.getHeaderNames();
            String header = null;
            while (headers.hasMoreElements()) {
                header = null;
                header = ((String) headers.nextElement()).toUpperCase();
                //REMIND: rewrite multiple headers as if received as single
                //REMIND: change character set
                //REMIND: I forgot what the previous REMIND means
                if ("AUTHORIZATION".equalsIgnoreCase(header) ||
                    "PROXY_AUTHORIZATION".equalsIgnoreCase(header)) {
                    //NOOP per CGI specification section 11.2
                } else if("HOST".equalsIgnoreCase(header)) {
                    String host = req.getHeader(header);
        int idx =  host.indexOf(":");
        if(idx < 0) idx = host.length();
                    envp.put("HTTP_" + header.replace('-', '_'),
                             host.substring(0, idx));
                } else {
                    envp.put("HTTP_" + header.replace('-', '_'),
                             req.getHeader(header));
                }
            }

            command = sCGIFullPath;
            envp.put("X_TOMCAT_SCRIPT_PATH", command);  //for kicks

            this.env = envp;

            return true;

        }



        /**
         * Print important CGI environment information in a easy-to-read HTML
         * table
         *
         * @return  HTML string containing CGI environment info
         *
         */
        public String toString() {

            StringBuffer sb = new StringBuffer();

            sb.append("<TABLE border=2>");

            sb.append("<tr><th colspan=2 bgcolor=grey>");
            sb.append("CGIEnvironment Info</th></tr>");

            sb.append("<tr><td>Debug Level</td><td>");
            sb.append(debug);
            sb.append("</td></tr>");

            sb.append("<tr><td>Validity:</td><td>");
            sb.append(isValid());
            sb.append("</td></tr>");

            if (isValid()) {
                Enumeration envk = env.keys();
                while (envk.hasMoreElements()) {
                    String s = (String) envk.nextElement();
                    sb.append("<tr><td>");
                    sb.append(s);
                    sb.append("</td><td>");
                    sb.append(blanksToString((String) env.get(s),
                                             "[will be set to blank]"));
                    sb.append("</td></tr>");
                }
            }

            sb.append("<tr><td colspan=2><HR></td></tr>");

            sb.append("<tr><td>Derived Command</td><td>");
            sb.append(nullsToBlanks(command));
            sb.append("</td></tr>");

            sb.append("<tr><td>Working Directory</td><td>");
            if (workingDirectory != null) {
                sb.append(workingDirectory.toString());
            }
            sb.append("</td></tr>");

            sb.append("<tr><td colspan=2>Query Params</td></tr>");
            Enumeration paramk = queryParameters.keys();
            while (paramk.hasMoreElements()) {
                String s = paramk.nextElement().toString();
                sb.append("<tr><td>");
                sb.append(s);
                sb.append("</td><td>");
                sb.append(queryParameters.get(s).toString());
                sb.append("</td></tr>");
            }

            sb.append("</TABLE><p>end.");

            return sb.toString();
        }



        /**
         * Gets derived command string
         *
         * @return  command string
         *
         */
        protected String getCommand() {
            return command;
        }



        /**
         * Gets derived CGI working directory
         *
         * @return  working directory
         *
         */
        protected File getWorkingDirectory() {
            return workingDirectory;
        }



        /**
         * Gets derived CGI environment
         *
         * @return   CGI environment
         *
         */
        protected Hashtable getEnvironment() {
            return env;
        }



        /**
         * Gets derived CGI query parameters
         *
         * @return   CGI query parameters
         *
         */
        protected Hashtable getParameters() {
            return queryParameters;
        }



        /**
         * Gets validity status
         *
         * @return   true if this environment is valid, false
         *           otherwise
         *
         */
        protected boolean isValid() {
            return valid;
        }



        /**
         * Converts null strings to blank strings ("")
         *
         * @param    string to be converted if necessary
         * @return   a non-null string, either the original or the empty string
         *           ("") if the original was <code>null</code>
         */
        protected String nullsToBlanks(String s) {
            return nullsToString(s, "");
        }



        /**
         * Converts null strings to another string
         *
         * @param    string to be converted if necessary
         * @param    string to return instead of a null string
         * @return   a non-null string, either the original or the substitute
         *           string if the original was <code>null</code>
         */
        protected String nullsToString(String couldBeNull,
                                       String subForNulls) {
            return (couldBeNull == null ? subForNulls : couldBeNull);
        }



        /**
         * Converts blank strings to another string
         *
         * @param    string to be converted if necessary
         * @param    string to return instead of a blank string
         * @return   a non-null string, either the original or the substitute
         *           string if the original was <code>null</code> or empty ("")
         */
        protected String blanksToString(String couldBeBlank,
                                      String subForBlanks) {
            return (("".equals(couldBeBlank) || couldBeBlank == null)
                    ? subForBlanks
                    : couldBeBlank);
        }



    } //class CGIEnvironment






    /**
     * Encapsulates the knowledge of how to run a CGI script, given the
     * script's desired environment and (optionally) input/output streams
     *
     * <p>
     *
     * Exposes a <code>run</code> method used to actually invoke the
     * CGI.
     *
     * </p>
     * <p>
     *
     * The CGI environment and settings are derived from the information
     * passed to the constuctor.
     *
     * </p>
     * <p>
     *
     * The input and output streams can be set by the <code>setInput</code>
     * and <code>setResponse</code> methods, respectively.
     * </p>
     *
     * @author    Martin Dengler [root@martindengler.com]
     * @version   $Revision: 1.9 $, $Date: 2002/09/05 21:46:54 $
     */

    protected class CGIRunner {

        /** script/command to be executed */
        private String command = null;

        /** environment used when invoking the cgi script */
        private Hashtable env = null;

        /** working directory used when invoking the cgi script */
        private File wd = null;

        /** query parameters to be passed to the invoked script */
        private Hashtable params = null;

        /** stdin to be passed to cgi script */
        private InputStream stdin = null;

        /** response object used to set headers & get output stream */
        private HttpServletResponse response = null;

        /** boolean tracking whether this object has enough info to run() */
        private boolean readyToRun = false;




        /**
         *  Creates a CGIRunner and initializes its environment, working
         *  directory, and query parameters.
         *  <BR>
         *  Input/output streams (optional) are set using the
         *  <code>setInput</code> and <code>setResponse</code> methods,
         *  respectively.
         *
         * @param  command  string full path to command to be executed
         * @param  env      Hashtable with the desired script environment
         * @param  wd       File with the script's desired working directory
         * @param  params   Hashtable with the script's query parameters
         *
         * @param  res       HttpServletResponse object for setting headers
         *                   based on CGI script output
         *
         */
        protected CGIRunner(String command, Hashtable env, File wd,
                            Hashtable params) {
            this.command = command;
            this.env = env;
            this.wd = wd;
            this.params = params;
            updateReadyStatus();
        }



        /**
         * Checks & sets ready status
         */
        protected void updateReadyStatus() {
            if (command != null
                && env != null
                && wd != null
                && params != null
                && response != null) {
                readyToRun = true;
            } else {
                readyToRun = false;
            }
        }



        /**
         * Gets ready status
         *
         * @return   false if not ready (<code>run</code> will throw
         *           an exception), true if ready
         */
        protected boolean isReady() {
            return readyToRun;
        }



        /**
         * Sets HttpServletResponse object used to set headers and send
         * output to
         *
         * @param  response   HttpServletResponse to be used
         *
         */
        protected void setResponse(HttpServletResponse response) {
            this.response = response;
            updateReadyStatus();
        }



        /**
         * Sets standard input to be passed on to the invoked cgi script
         *
         * @param  stdin   InputStream to be used
         *
         */
        protected void setInput(InputStream stdin) {
            this.stdin = stdin;
            updateReadyStatus();
        }



        /**
         * Converts a Hashtable to a String array by converting each
         * key/value pair in the Hashtable to a String in the form
         * "key=value" (hashkey + "=" + hash.get(hashkey).toString())
         *
         * @param  h   Hashtable to convert
         *
         * @return     converted string array
         *
         * @exception  NullPointerException   if a hash key has a null value
         *
         */
        protected String[] hashToStringArray(Hashtable h)
            throws NullPointerException {
            Vector v = new Vector();
            Enumeration e = h.keys();
            while (e.hasMoreElements()) {
                String k = e.nextElement().toString();
                v.add(k + "=" + h.get(k));
            }
            String[] strArr = new String[v.size()];
            v.copyInto(strArr);
            return strArr;
        }



        /**
         * Executes a CGI script with the desired environment, current working
         * directory, and input/output streams
         *
         * <p>
         * This implements the following CGI specification recommedations:
         * <UL>
         * <LI> Servers SHOULD provide the "<code>query</code>" component of
         *      the script-URI as command-line arguments to scripts if it
         *      does not contain any unencoded "=" characters and the
         *      command-line arguments can be generated in an unambiguous
         *      manner.
         * <LI> Servers SHOULD set the AUTH_TYPE metavariable to the value
         *      of the "<code>auth-scheme</code>" token of the
         *      "<code>Authorization</code>" if it was supplied as part of the
         *      request header.  See <code>getCGIEnvironment</code> method.
         * <LI> Where applicable, servers SHOULD set the current working
         *      directory to the directory in which the script is located
         *      before invoking it.
         * <LI> Server implementations SHOULD define their behavior for the
         *      following cases:
         *     <ul>
         *     <LI> <u>Allowed characters in pathInfo</u>:  This implementation
         *             does not allow ASCII NUL nor any character which cannot
         *             be URL-encoded according to internet standards;
         *     <LI> <u>Allowed characters in path segments</u>: This
         *             implementation does not allow non-terminal NULL
         *             segments in the the path -- IOExceptions may be thrown;
         *     <LI> <u>"<code>.</code>" and "<code>..</code>" path
         *             segments</u>:
         *             This implementation does not allow "<code>.</code>" and
         *             "<code>..</code>" in the the path, and such characters
         *             will result in an IOException being thrown;
         *     <LI> <u>Implementation limitations</u>: This implementation
         *             does not impose any limitations except as documented
         *             above.  This implementation may be limited by the
         *             servlet container used to house this implementation.
         *             In particular, all the primary CGI variable values
         *             are derived either directly or indirectly from the
         *             container's implementation of the Servlet API methods.
         *     </ul>
         * </UL>
         * </p>
         *
         * @exception IOException if problems during reading/writing occur
         *
         * @see    java.lang.Runtime#exec(String command, String[] envp,
         *                                File dir)
         */
        protected void run() throws IOException {

            /*
             * REMIND:  this method feels too big; should it be re-written?
             */

            if (!isReady()) {
                throw new IOException(this.getClass().getName()
                                      + ": not ready to run.");
            }

            if (debug >= 1 ) {
                log("runCGI(envp=[" + env + "], command=" + command + ")");
            }

            if ((command.indexOf(File.separator + "." + File.separator) >= 0)
                || (command.indexOf(File.separator + "..") >= 0)
                || (command.indexOf(".." + File.separator) >= 0)) {
                throw new IOException(this.getClass().getName()
                                      + "Illegal Character in CGI command "
                                      + "path ('.' or '..') detected.  Not "
                                      + "running CGI [" + command + "].");
            }

            /* original content/structure of this section taken from
             * http://developer.java.sun.com/developer/
             *                               bugParade/bugs/4216884.html
             * with major modifications by Martin Dengler
             */
            Runtime rt = null;
            BufferedReader commandsStdOut = null;
            BufferedReader commandsStdErr = null;
            BufferedOutputStream commandsStdIn = null;
            Process proc = null;
            int bufRead = -1;

            //create query arguments
            Enumeration paramNames = params.keys();
            StringBuffer cmdAndArgs = new StringBuffer(command);
            if (paramNames != null && paramNames.hasMoreElements()) {
                cmdAndArgs.append(" ");
                while (paramNames.hasMoreElements()) {
                    String k = (String) paramNames.nextElement();
                    String v = params.get(k).toString();
                    if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                        cmdAndArgs.append(k);
                        cmdAndArgs.append("=");
                        v = java.net.URLEncoder.encode(v);
                        cmdAndArgs.append(v);
                        cmdAndArgs.append(" ");
                    }
                }
            }

            /*String postIn = getPostInput(params);
            int contentLength = (postIn.length()
                    + System.getProperty("line.separator").length());
            if ("POST".equals(env.get("REQUEST_METHOD"))) {
                env.put("CONTENT_LENGTH", new Integer(contentLength));
            }*/

        if (command.endsWith(".pl") || command.endsWith(".cgi")) {
            StringBuffer perlCommand = new StringBuffer("perl ");
            perlCommand.append(cmdAndArgs.toString());
            cmdAndArgs = perlCommand;
        }

            rt = Runtime.getRuntime();
            proc = rt.exec(cmdAndArgs.toString(), hashToStringArray(env), wd);

            /*
             * provide input to cgi
             * First  -- parameters
             * Second -- any remaining input
             */
            /*commandsStdIn = new BufferedOutputStream(proc.getOutputStream());
            if (debug >= 2 ) {
                log("runCGI stdin=[" + stdin + "], qs="
                    + env.get("QUERY_STRING"));
            }
            if ("POST".equals(env.get("REQUEST_METHOD"))) {
                if (debug >= 2) {
                    log("runCGI: writing ---------------\n");
                    log(postIn);
                    log("runCGI: new content_length=" + contentLength
                        + "---------------\n");
                }
                commandsStdIn.write(postIn.getBytes());
            }
            if (stdin != null) {
                //REMIND: document this
                /* assume if nothing is available after a time, that nothing is
                 * coming...
                 */
                /*if (stdin.available() <= 0) {
                    if (debug >= 2 ) {
                        log("runCGI stdin is NOT available ["
                            + stdin.available() + "]");
                    }
                    try {
                        Thread.currentThread().sleep(iClientInputTimeout);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (stdin.available() > 0) {
                    if (debug >= 2 ) {
                        log("runCGI stdin IS available ["
                            + stdin.available() + "]");
                    }
                    byte[] bBuf = new byte[1024];
                    bufRead = -1;
                    try {
                        while ((bufRead = stdin.read(bBuf)) != -1) {
                            if (debug >= 2 ) {
                                log("runCGI: read [" + bufRead
                                    + "] bytes from stdin");
                            }
                            commandsStdIn.write(bBuf, 0, bufRead);
                        }
                        if (debug >= 2 ) {
                            log("runCGI: DONE READING from stdin");
                        }
                    } catch (IOException ioe) {
                        //REMIND: replace with logging
                        //REMIND: should I throw this exception?
                        log("runCGI: couldn't write all bytes.");
                        ioe.printStackTrace();
                    }
                }
            }
            commandsStdIn.flush();
            commandsStdIn.close();*/
      String sContentLength = (String) env.get("CONTENT_LENGTH");
      if(!"".equals(sContentLength)) {
          commandsStdIn = new BufferedOutputStream(proc.getOutputStream());
          byte[] content = new byte[Integer.parseInt(sContentLength)];

          int lenRead = stdin.read(content);

          if ("POST".equals(env.get("REQUEST_METHOD"))) {
              String paramStr = getPostInput(params);
              if (paramStr != null) {
                  byte[] paramBytes = paramStr.getBytes();
                  commandsStdIn.write(paramBytes);

                  int contentLength = paramBytes.length;
                  if (lenRead > 0) {
                      String lineSep = System.getProperty("line.separator");

                      commandsStdIn.write(lineSep.getBytes());

                      contentLength = lineSep.length() + lenRead;
                  }

                  env.put("CONTENT_LENGTH", new Integer(contentLength));
              }
          }

          if (lenRead > 0) {
              commandsStdIn.write(content, 0, lenRead);
          }


          commandsStdIn.flush();
          commandsStdIn.close();
      }

            /* we want to wait for the process to exit,  Process.waitFor()
             * is useless in our situation; see
             * http://developer.java.sun.com/developer/
             *                               bugParade/bugs/4223650.html
             */

            boolean isRunning = true;
            commandsStdOut = new BufferedReader
                (new InputStreamReader(proc.getInputStream()));
            commandsStdErr = new BufferedReader
                (new InputStreamReader(proc.getErrorStream()));
            BufferedWriter servletContainerStdout = null;

            try {
                if (response.getOutputStream() != null) {
                    servletContainerStdout =
                        new BufferedWriter(new OutputStreamWriter
                            (response.getOutputStream()));
                }
            } catch (IOException ignored) {
                //NOOP: no output will be written
            }
            final BufferedReader stdErrRdr = commandsStdErr ;

            new Thread() {
                public void run () {
                    sendToLog(stdErrRdr) ;
                } ;
            }.start() ;


            while (isRunning) {

                try {

                    //set headers
                    String line = null;
                    while (((line = commandsStdOut.readLine()) != null)
                           && !("".equals(line))) {
                        if (debug >= 2) {
                            log("runCGI: addHeader(\"" + line + "\")");
                        }
                        if (line.startsWith("HTTP")) {
                            //TODO: should set status codes (NPH support)
                            /*
                             * response.setStatus(getStatusCode(line));
                             */
                        } else if (line.indexOf(":") >= 0) {
                            response.addHeader
                                (line.substring(0, line.indexOf(":")).trim(),
                                line.substring(line.indexOf(":") + 1).trim());
                        } else {
                            log("runCGI: bad header line \"" + line + "\"");
                        }
                    }

                    //write output
                    char[] cBuf = new char[1024];
                    while ((bufRead = commandsStdOut.read(cBuf)) != -1) {
                        if (servletContainerStdout != null) {
                            if (debug >= 4) {
                                log("runCGI: write(\"" + new String(cBuf, 0, bufRead) + "\")");
                            }
                            servletContainerStdout.write(cBuf, 0, bufRead);
                        }
                    }

                    if (servletContainerStdout != null) {
                        servletContainerStdout.flush();
                    }

                    proc.exitValue(); // Throws exception if alive

                    isRunning = false;

                } catch (IllegalThreadStateException e) {
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }
            } //replacement for Process.waitFor()
            commandsStdOut.close()  ;
        }

        private void sendToLog(BufferedReader rdr) {
            String line = null;
            int lineCount = 0 ;
            try {
                while ((line = rdr.readLine()) != null) {
                    log("runCGI (stderr):" +  line) ;
                }
                lineCount++ ;
            } catch (IOException e) {
                log("sendToLog error", e) ;
            } finally {
                try {
                    rdr.close() ;
                } catch (IOException ce) {
                    log("sendToLog error", ce) ;
                } ;
            } ;
            if ( lineCount > 0 && debug > 2) {
                log("runCGI: " + lineCount + " lines received on stderr") ;
            } ;
        }


        /**
         * Gets a string for input to a POST cgi script
         *
         * @param  params   Hashtable of query parameters to be passed to
         *                  the CGI script
         * @return          for use as input to the CGI script
         */

        protected String getPostInput(Hashtable params) {
            String lineSeparator = System.getProperty("line.separator");
            Enumeration paramNames = params.keys();
            StringBuffer postInput = new StringBuffer("");
            StringBuffer qs = new StringBuffer("");
            if (paramNames != null && paramNames.hasMoreElements()) {
                while (paramNames.hasMoreElements()) {
                    String k = (String) paramNames.nextElement();
                    String v = params.get(k).toString();
                    if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                        postInput.append(k);
                        qs.append(k);
                        postInput.append("=");
                        qs.append("=");
                        postInput.append(v);
                        qs.append(v);
                        postInput.append(lineSeparator);
                        qs.append("&");
                    }
                }
            }
            qs.append(lineSeparator);
            return qs.append(postInput).toString();
        }



    } //class CGIRunner




} //class CGIServlet
