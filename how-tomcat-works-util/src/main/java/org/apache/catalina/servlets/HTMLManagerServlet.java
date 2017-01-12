/*
* $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/servlets/HTMLManagerServlet.java,v 1.12 2002/09/18 14:08:34 remm Exp $
* $Revision: 1.12 $
* $Date: 2002/09/18 14:08:34 $
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


package org.apache.catalina.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.util.ServerInfo;

/**
* Servlet that enables remote management of the web applications installed
* within the same virtual host as this web application is.  Normally, this
* functionality will be protected by a security constraint in the web
* application deployment descriptor.  However, this requirement can be
* relaxed during testing.
* <p>
* The difference between this <code>ManagerServlet</code> and this
* Servlet is that this Servlet prints out a HTML interface which
* makes it easier to administrate.
* <p>
* However if you use a software that parses the output of
* <code>ManagerServlet</code you won't be able to upgrade
* to this Servlet since the output are not in the
* same format ar from <code>ManagerServlet</code>
*
* @author Bip Thelin
* @author Malcolm Edgar
* @version $Revision: 1.12 $, $Date: 2002/09/18 14:08:34 $
* @see ManagerServlet
*/

public final class HTMLManagerServlet extends ManagerServlet {

    // --------------------------------------------------------- Public Methods

    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

        // Identify the request parameters that we need
        String command = request.getPathInfo();

        String path = request.getParameter("path");
        String installPath = request.getParameter("installPath");
        String installConfig = request.getParameter("installConfig");
        String installWar = request.getParameter("installWar");

        // Prepare our output writer to generate the response message
        response.setContentType("text/html");
        Locale locale = Locale.getDefault();
        response.setLocale(locale);
        PrintWriter writer = response.getWriter();

        // Process the requested command
        if (command == null) {
            response.sendRedirect(request.getRequestURI()+"/list");
        } else if (command.equals("/install")) {
            install(writer, installConfig, installPath, installWar);
        } else if (command.equals("/list")) {
            list(writer, "");
        } else if (command.equals("/reload")) {
            reload(writer, path);
        } else if (command.equals("/remove")) {
            remove(writer, path);
        } else if (command.equals("/sessions")) {
            sessions(writer, path);
        } else if (command.equals("/start")) {
            start(writer, path);
        } else if (command.equals("/stop")) {
            stop(writer, path);
        } else {
            String message =
                sm.getString("managerServlet.unknownCommand", command);
            list(writer, message);
        }

        // Finish up the response
        writer.flush();
        writer.close();
    }

    /**
     * Install an application for the specified path from the specified
     * web application archive.
     *
     * @param writer Writer to render results to
     * @param config URL of the context configuration file to be installed
     * @param path Context path of the application to be installed
     * @param war URL of the web application archive to be installed
     */
    protected void install(PrintWriter writer, String config,
                           String path, String war) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.install(printWriter, config, path, war);

        list(writer, stringWriter.toString());
    }

    /**
     * Render a HTML list of the currently active Contexts in our virtual host,
     * and memory and server status information.
     *
     * @param writer Writer to render to
     * @param message a message to display
     */
    public void list(PrintWriter writer, String message) {

        if (debug >= 1)
            log("list: Listing contexts for virtual host '" +
                deployer.getName() + "'");

        // HTML Header Section
        writer.print(HTML_HEADER_SECTION);

        // Body Header Section
        Object[] args = new Object[1];
        args[0] = sm.getString("htmlManagerServlet.title");
        writer.print(MessageFormat.format(BODY_HEADER_SECTION, args));

        // Message Section
        args = new Object[3];
        args[0] = sm.getString("htmlManagerServlet.messageLabel");
        args[1] = (message != null) ? message : "";
        writer.print(MessageFormat.format(MESSAGE_SECTION, args));

        // Apps Header Section
        args = new Object[5];
        args[0] = sm.getString("htmlManagerServlet.appsTitle");
        args[1] = sm.getString("htmlManagerServlet.appsPath");
        args[2] = sm.getString("htmlManagerServlet.appsName");
        args[3] = sm.getString("htmlManagerServlet.appsAvailable");
        args[4] = sm.getString("htmlManagerServlet.appsSessions");
        writer.print(MessageFormat.format(APPS_HEADER_SECTION, args));

        // Apps Row Section
        // Create sorted map of deployed applications context paths.
        String contextPaths[] = deployer.findDeployedApps();

        TreeMap sortedContextPathsMap = new TreeMap();

        for (int i = 0; i < contextPaths.length; i++) {
            String displayPath = contextPaths[i];
            sortedContextPathsMap.put(displayPath, contextPaths[i]);
        }

        String appsStart = sm.getString("htmlManagerServlet.appsStart");
        String appsStop = sm.getString("htmlManagerServlet.appsStop");
        String appsReload = sm.getString("htmlManagerServlet.appsReload");
        String appsRemove = sm.getString("htmlManagerServlet.appsRemove");

        Iterator iterator = sortedContextPathsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String displayPath = (String) entry.getKey();
            String contextPath = (String) entry.getKey();
            Context context = deployer.findDeployedApp(contextPath);
            if (displayPath.equals("")) {
                displayPath = "/";
            }

            if (context != null ) {
                args = new Object[6];
                args[0] = displayPath;
                args[1] = context.getDisplayName();
                if (args[1] == null) {
                    args[1] = "&nbsp;";
                }
                args[2] = new Boolean(context.getAvailable());
                args[3] =
                    new Integer(context.getManager().findSessions().length);
                writer.print
                    (MessageFormat.format(APPS_ROW_DETAILS_SECTION, args));

                args = new Object[5];
                args[0] = displayPath;
                args[1] = appsStart;
                args[2] = appsStop;
                args[3] = appsReload;
                args[4] = appsRemove;
                if (context.getPath().equals(this.context.getPath())) {
                    writer.print(MessageFormat.format(
                        MANAGER_APP_ROW_BUTTON_SECTION, args));
                } else if (context.getAvailable()) {
                    writer.print(MessageFormat.format(
                        STARTED_APPS_ROW_BUTTON_SECTION, args));
                } else {
                    writer.print(MessageFormat.format(
                        STOPPED_APPS_ROW_BUTTON_SECTION, args));
                }

            }
        }

        // Install Section
        args = new Object[5];
        args[0] = sm.getString("htmlManagerServlet.installTitle");
        args[1] = sm.getString("htmlManagerServlet.installPath");
        args[2] = sm.getString("htmlManagerServlet.installConfig");
        args[3] = sm.getString("htmlManagerServlet.installWar");
        args[4] = sm.getString("htmlManagerServlet.installButton");
        writer.print(MessageFormat.format(INSTALL_SECTION, args));

        // Server Header Section
        args = new Object[7];
        args[0] = sm.getString("htmlManagerServlet.serverTitle");
        args[1] = sm.getString("htmlManagerServlet.serverVersion");
        args[2] = sm.getString("htmlManagerServlet.serverJVMVersion");
        args[3] = sm.getString("htmlManagerServlet.serverJVMVendor");
        args[4] = sm.getString("htmlManagerServlet.serverOSName");
        args[5] = sm.getString("htmlManagerServlet.serverOSVersion");
        args[6] = sm.getString("htmlManagerServlet.serverOSArch");
        writer.print(MessageFormat.format(SERVER_HEADER_SECTION, args));

        // Server Row Section
        args = new Object[6];
        args[0] = ServerInfo.getServerInfo();
        args[1] = System.getProperty("java.runtime.version");
        args[2] = System.getProperty("java.vm.vendor");
        args[3] = System.getProperty("os.name");
        args[4] = System.getProperty("os.version");
        args[5] = System.getProperty("os.arch");
        writer.print(MessageFormat.format(SERVER_ROW_SECTION, args));

        // HTML Tail Section
        writer.print(HTML_TAIL_SECTION);
    }

    /**
     * Reload the web application at the specified context path.
     *
     * @see ManagerServlet#reload(PrintWriter, String)
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be restarted
     */
    protected void reload(PrintWriter writer, String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.reload(printWriter, path);

        list(writer, stringWriter.toString());
    }

    /**
     * Remove the web application at the specified context path.
     *
     * @see ManagerServlet#remove(PrintWriter, String)
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be removed
     */
    protected void remove(PrintWriter writer, String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.remove(printWriter, path);

        list(writer, stringWriter.toString());
    }

    /**
     * Display session information and invoke list.
     *
     * @see ManagerServlet#sessions(PrintWriter, String)
     *
     * @param writer Writer to render to
     * @param path Context path of the application to list session information for
     */
    public void sessions(PrintWriter writer, String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.sessions(printWriter, path);

        list(writer, stringWriter.toString());
    }

    /**
     * Start the web application at the specified context path.
     *
     * @see ManagerServlet#start(PrintWriter, String)
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be started
     */
    public void start(PrintWriter writer, String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.start(printWriter, path);

        list(writer, stringWriter.toString());
    }

    /**
     * Stop the web application at the specified context path.
     *
     * @see ManagerServlet#stop(PrintWriter, String)
     *
     * @param writer Writer to render to
     * @param path Context path of the application to be stopped
     */
    protected void stop(PrintWriter writer, String path) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        super.stop(printWriter, path);

        list(writer, stringWriter.toString());
    }

    // ------------------------------------------------------ Private Constants

    // These HTML sections are broken in relatively small sections, because of
    // limited number of subsitutions MessageFormat can process
    // (maximium of 10).

    private static final String HTML_HEADER_SECTION =
        "<html> \n" +
        "<head> \n" +
        "<style> \n" +
        "  table { width: 100%; } \n" +
        "  td.page-title {  \n" +
        "    text-align: center; \n" +
        "    vertical-align: top; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    font-weight: bold; \n" +
        "    background: white; \n" +
        "    color: black; \n" +
        "  } \n" +
        "  td.title { \n" +
        "    text-align: left; \n" +
        "    vertical-align: top; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    font-style:italic; \n" +
        "    font-weight: bold; \n" +
        "    background: #D2A41C; \n" +
        "  } \n" +
        "  td.header-left { \n" +
        "    text-align: left; \n" +
        "    vertical-align: top; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    font-weight: bold; \n" +
        "    background: #FFDC75; \n" +
        "  } \n" +
        "  td.header-center { \n" +
        "    text-align: center; \n" +
        "    vertical-align: top; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    font-weight: bold; \n" +
        "    background: #FFDC75; \n" +
        "  } \n" +
        "  td.row-left { \n" +
        "    text-align: left; \n" +
        "    vertical-align: middle; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    color: black; \n" +
        "    background: white; \n" +
        "  } \n" +
        "  td.row-center { \n" +
        "    text-align: center; \n" +
        "    vertical-align: middle; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    color: black; \n" +
        "    background: white; \n" +
        "  } \n" +
        "  td.row-right { \n" +
        "    text-align: right; \n" +
        "    vertical-align: middle; \n" +
        "    font-family:verdana,sans-serif; \n" +
        "    color: black; \n" +
        "    background: white; \n" +
        "  } \n" +
        "</style> \n";

    private static final String BODY_HEADER_SECTION =
        "<title>{0}</title> \n" +
        "</head> \n" +
        "\n" +
        "<body bgcolor=\"#FFFFFF\"> \n" +
        "\n" +
        "<table border=\"2\" cellspacing=\"0\" cellpadding=\"3\" " +
        "bordercolor=\"#000000\"> \n" +
        "<tr> \n" +
        " <td class=\"page-title\" bordercolor=\"#000000\" align=\"left\" " +
        "nowrap> \n" +
        "  <font size=\"+2\">{0}</font> \n" +
        " </td> \n" +
        "</tr> \n" +
        "</table> \n" +
        "<br> \n" +
        "\n";

    private static final String MESSAGE_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\"> \n" +
        " <tr> \n" +
        "  <td class=\"row-left\"><small><b>{0}</b>&nbsp;{1}</small></td>\n" +
        " </tr> \n" +
        "</table> \n" +
        "<br> \n" +
        "\n";

    private static final String APPS_HEADER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\"> \n" +
        "<tr> \n" +
        " <td colspan=\"10\" class=\"title\">{0}</td> \n" +
        "</tr> \n" +
        "<tr> \n" +
        " <td class=\"header-left\"><small>{1}</small></td> \n" +
        " <td class=\"header-left\"><small>{2}</small></td> \n" +
        " <td class=\"header-center\"><small>{3}</small></td> \n" +
        " <td class=\"header-center\"><small>{4}</small></td> \n" +
        " <td class=\"header-center\">&nbsp;</td> \n" +
        "</tr> \n";

    private static final String APPS_ROW_DETAILS_SECTION =
        "<tr> \n" +
        " <td class=\"row-left\"><small><a href=\"{0}\">{0}</a>" +
        "</small></td> \n" +
        " <td class=\"row-left\"><small>{1}</small></td> \n" +
        " <td class=\"row-center\"><small>{2}</small></td> \n" +
        " <td class=\"row-center\">" +
        "<small><a href=\"sessions?path={0}\">{3}</a></small></td> \n";

    private static final String MANAGER_APP_ROW_BUTTON_SECTION =
        " <td class=\"row-left\"> \n" +
        "  <small> \n" +
        "  &nbsp;{1}&nbsp; \n" +
        "  &nbsp;{2}&nbsp; \n" +
        "  &nbsp;{3}&nbsp; \n" +
        "  &nbsp;{4}&nbsp; \n" +
        "  </small> \n" +
        " </td> \n" +
        "</tr> \n";

    private static final String STARTED_APPS_ROW_BUTTON_SECTION =
        " <td class=\"row-left\"> \n" +
        "  <small> \n" +
        "  &nbsp;{1}&nbsp; \n" +
        "  &nbsp;<a href=\"stop?path={0}\">{2}</a>&nbsp; \n" +
        "  &nbsp;<a href=\"reload?path={0}\">{3}</a>&nbsp; \n" +
        "  &nbsp;<a href=\"remove?path={0}\">{4}</a>&nbsp; \n" +
        "  </small> \n" +
        " </td> \n" +
        "</tr> \n";

    private static final String STOPPED_APPS_ROW_BUTTON_SECTION =
        " <td class=\"row-left\"> \n" +
        "  <small> \n" +
        "  &nbsp;<a href=\"start?path={0}\">{1}</a>&nbsp; \n" +
        "  &nbsp;{2}&nbsp; \n" +
        "  &nbsp;{3}&nbsp; \n" +
        "  &nbsp;<a href=\"remove?path={0}\">{4}</a>&nbsp; \n" +
        "  </small> \n" +
        " </td> \n" +
        "</tr> \n";

    private static final String INSTALL_SECTION =
        "<tr> \n" +
        " <td colspan=\"10\" class=\"header-left\"><small>{0}</small></td>\n" +
        "</tr> \n" +
        "<tr> \n" +
        "<form method=\"get\" action=\"install\"> \n" +
        "<input type=\"hidden\" name=\"path\"> \n" +
        " <td colspan=\"10\" class=\"row-left\"> \n" +
        "  <small>{1}</small> \n" +
        "  <input type=\"text\" name=\"installPath\" size=\"10\"> \n" +
        "  &nbsp;<small>{2}</small> \n" +
        "  <input type=\"text\" name=\"installConfig\" size=\"18\"> \n" +
        "  &nbsp;<small>{3}</small> \n" +
        "  <input type=\"text\" name=\"installWar\" size=\"18\">&nbsp; \n" +
        "  <input type=\"submit\" value=\"{4}\"> \n" +
        " </td> \n" +
        "</form> \n" +
        "</tr> \n" +
        "</table> \n" +
        "<br> \n" +
        "\n";

    private static final String SERVER_HEADER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\"> \n" +
        "<tr> \n" +
        " <td colspan=\"10\" class=\"title\">{0}</td>  \n" +
        "</tr> \n" +
        "<tr> \n" +
        " <td class=\"header-center\"><small>{1}</small></td> \n" +
        " <td class=\"header-center\"><small>{2}</small></td> \n" +
        " <td class=\"header-center\"><small>{3}</small></td> \n" +
        " <td class=\"header-center\"><small>{4}</small></td> \n" +
        " <td class=\"header-center\"><small>{5}</small></td> \n" +
        " <td class=\"header-center\"><small>{6}</small></td> \n" +
        "</tr> \n";

    private static final String SERVER_ROW_SECTION =
        "<tr> \n" +
        " <td class=\"row-center\"><small>{0}</small></td> \n" +
        " <td class=\"row-center\"><small>{1}</small></td> \n" +
        " <td class=\"row-center\"><small>{2}</small></td> \n" +
        " <td class=\"row-center\"><small>{3}</small></td> \n" +
        " <td class=\"row-center\"><small>{4}</small></td> \n" +
        " <td class=\"row-center\"><small>{5}</small></td> \n" +
        "</tr> \n" +
        "</table> \n" +
        "<br> \n" +
        "\n";

    private static final String HTML_TAIL_SECTION =
        "</body> \n" +
        "</html>";
}
