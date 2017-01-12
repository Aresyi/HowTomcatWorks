package org.apache.catalina.core;


import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.ValveContext;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardWrapper</code> container implementation.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.34 $ $Date: 2002/03/15 19:12:49 $
 */

final class StandardWrapperValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The filter definition for our container-provided filter.
     */
    private FilterDef filterDef = null;


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardWrapperValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the servlet we are managing, respecting the rules regarding
     * servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public void invoke(Request request, Response response,
                       ValveContext valveContext)
        throws IOException, ServletException {
        // Initialize local variables we may need
        boolean unavailable = false;
        Throwable throwable = null;
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        ServletRequest sreq = request.getRequest();
        ServletResponse sres = response.getResponse();
        Servlet servlet = null;
        HttpServletRequest hreq = null;
        if (sreq instanceof HttpServletRequest)
            hreq = (HttpServletRequest) sreq;
        HttpServletResponse hres = null;
        if (sres instanceof HttpServletResponse)
            hres = (HttpServletResponse) sres;

        // Check for the application being marked unavailable
        if (!((Context) wrapper.getParent()).getAvailable()) {
            hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                           sm.getString("standardContext.isUnavailable"));
            unavailable = true;
        }

        // Check for the servlet being marked unavailable
        if (!unavailable && wrapper.isUnavailable()) {
            log(sm.getString("standardWrapper.isUnavailable",
                             wrapper.getName()));
            if (hres == null) {
                ;       // NOTE - Not much we can do generically
            } else {
                long available = wrapper.getAvailable();
                if ((available > 0L) && (available < Long.MAX_VALUE))
                    hres.setDateHeader("Retry-After", available);
                hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                               sm.getString("standardWrapper.isUnavailable",
                                            wrapper.getName()));
            }
            unavailable = true;
        }

        // Allocate a servlet instance to process this request
        try {
            if (!unavailable) {
                servlet = wrapper.allocate();
            }
        } catch (ServletException e) {
            log(sm.getString("standardWrapper.allocateException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
            servlet = null;
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.allocateException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
            servlet = null;
        }

        // Acknowlege the request
        try {
            response.sendAcknowledgement();
        } catch (IOException e) {
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            log(sm.getString("standardWrapper.acknowledgeException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.acknowledgeException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
            servlet = null;
        }

        // Create the filter chain for this request
        ApplicationFilterChain filterChain =
            createFilterChain(request, servlet);

        // Call the filter chain for this request
        // NOTE: This also calls the servlet's service() method
        try {
            String jspFile = wrapper.getJspFile();
            if (jspFile != null)
                sreq.setAttribute(Globals.JSP_FILE_ATTR, jspFile);
            else
                sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            if ((servlet != null) && (filterChain != null)) {
                filterChain.doFilter(sreq, sres);
            }
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
        } catch (IOException e) {
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        } catch (UnavailableException e) {
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            //            throwable = e;
            //            exception(request, response, e);
            wrapper.unavailable(e);
            long available = wrapper.getAvailable();
            if ((available > 0L) && (available < Long.MAX_VALUE))
                hres.setDateHeader("Retry-After", available);
            hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                           sm.getString("standardWrapper.isUnavailable",
                                        wrapper.getName()));
            // Do not save exception in 'throwable', because we
            // do not want to do exception(request, response, e) processing
        } catch (ServletException e) {
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            sreq.removeAttribute(Globals.JSP_FILE_ATTR);
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        }

        // Release the filter chain (if any) for this request
        try {
            if (filterChain != null)
                filterChain.release();
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.releaseFilters",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // Deallocate the allocated servlet instance
        try {
            if (servlet != null) {
                wrapper.deallocate(servlet);
            }
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.deallocateException",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // If this servlet has been marked permanently unavailable,
        // unload it and release this instance
        try {
            if ((servlet != null) &&
                (wrapper.getAvailable() == Long.MAX_VALUE)) {
                wrapper.unload();
            }
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.unloadException",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Construct and return a FilterChain implementation that will wrap the
     * execution of the specified servlet instance.  If we should not execute
     * a filter chain at all, return <code>null</code>.
     * <p>
     * <strong>FIXME</strong> - Pool the chain instances!
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     */
    private ApplicationFilterChain createFilterChain(Request request,
                                                     Servlet servlet) {

        // If there is no servlet to execute, return null
        if (servlet == null)
            return (null);

        // Create and initialize a filter chain object
        ApplicationFilterChain filterChain =
          new ApplicationFilterChain();
        filterChain.setServlet(servlet);
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        filterChain.setSupport(wrapper.getInstanceSupport());

        // Acquire the filter mappings for this Context
        StandardContext context = (StandardContext) wrapper.getParent();
        FilterMap filterMaps[] = context.findFilterMaps();

        // If there are no filter mappings, we are done
        if ((filterMaps == null) || (filterMaps.length == 0))
            return (filterChain);
//        if (debug >= 1)
//            log("createFilterChain:  Processing " + filterMaps.length +
//                " filter map entries");

        // Acquire the information we will need to match filter mappings
        String requestPath = null;
        if (request instanceof HttpRequest) {
            HttpServletRequest hreq =
                (HttpServletRequest) request.getRequest();
            String contextPath = hreq.getContextPath();
            if (contextPath == null)
                contextPath = "";
            String requestURI = ((HttpRequest) request).getDecodedRequestURI();
            if (requestURI.length() >= contextPath.length())
                requestPath = requestURI.substring(contextPath.length());
        }
        String servletName = wrapper.getName();
//        if (debug >= 1) {
//            log(" requestPath=" + requestPath);
//            log(" servletName=" + servletName);
//        }
        int n = 0;

        // Add the relevant path-mapped filters to this filter chain
        for (int i = 0; i < filterMaps.length; i++) {
//            if (debug >= 2)
//                log(" Checking path-mapped filter '" +
//                    filterMaps[i] + "'");
            if (!matchFiltersURL(filterMaps[i], requestPath))
                continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMaps[i].getFilterName());
            if (filterConfig == null) {
//                if (debug >= 2)
//                    log(" Missing path-mapped filter '" +
//                        filterMaps[i] + "'");
                ;       // FIXME - log configuration problem
                continue;
            }
//            if (debug >= 2)
//                log(" Adding path-mapped filter '" +
//                    filterConfig.getFilterName() + "'");
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Add filters that match on servlet name second
        for (int i = 0; i < filterMaps.length; i++) {
//            if (debug >= 2)
//                log(" Checking servlet-mapped filter '" +
//                    filterMaps[i] + "'");
            if (!matchFiltersServlet(filterMaps[i], servletName))
                continue;
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig)
                context.findFilterConfig(filterMaps[i].getFilterName());
            if (filterConfig == null) {
//                if (debug >= 2)
//                    log(" Missing servlet-mapped filter '" +
//                        filterMaps[i] + "'");
                ;       // FIXME - log configuration problem
                continue;
            }
//            if (debug >= 2)
//                log(" Adding servlet-mapped filter '" +
//                     filterMaps[i] + "'");
            filterChain.addFilter(filterConfig);
            n++;
        }

        // Return the completed filter chain
//        if (debug >= 2)
//            log(" Returning chain with " + n + " filters");
        return (filterChain);

    }


    /**
     * Handle the specified ServletException encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    private void exception(Request request, Response response,
                           Throwable exception) {

        ServletRequest sreq = request.getRequest();
        sreq.setAttribute(Globals.EXCEPTION_ATTR, exception);

        ServletResponse sresponse = response.getResponse();
        if (sresponse instanceof HttpServletResponse)
            ((HttpServletResponse) sresponse).setStatus
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardWrapperValve[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardWrapperValve[" + containerName
                               + "]: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardWrapperValve[" + container.getName() + "]: "
                       + message, throwable);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardWrapperValve[" + containerName
                               + "]: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Return <code>true</code> if the specified servlet name matches
     * the requirements of the specified filter mapping; otherwise
     * return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param servletName Servlet name being checked
     */
    private boolean matchFiltersServlet(FilterMap filterMap,
                                        String servletName) {

//      if (debug >= 3)
//          log("  Matching servlet name '" + servletName +
//              "' against mapping " + filterMap);

        if (servletName == null)
            return (false);
        else
            return (servletName.equals(filterMap.getServletName()));

    }


    /**
     * Return <code>true</code> if the context-relative request path
     * matches the requirements of the specified filter mapping;
     * otherwise, return <code>null</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    private boolean matchFiltersURL(FilterMap filterMap,
                                    String requestPath) {

//      if (debug >= 3)
//          log("  Matching request path '" + requestPath +
//              "' against mapping " + filterMap);

        if (requestPath == null)
            return (false);

        // Match on context relative request path
        String testPath = filterMap.getURLPattern();
        if (testPath == null)
            return (false);

        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
            return (true);

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
            return (true);      // Optimize a common case
        if (testPath.endsWith("/*")) {
            String comparePath = requestPath;
            while (true) {
                if (testPath.equals(comparePath + "/*"))
                    return (true);
                int slash = comparePath.lastIndexOf('/');
                if (slash < 0)
                    break;
                comparePath = comparePath.substring(0, slash);
            }
            return (false);
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash))
                return (testPath.equals("*." +
                                        requestPath.substring(period + 1)));
        }

        // Case 4 - "Default" Match
        return (false); // NOTE - Not relevant for selecting filters

    }


}
