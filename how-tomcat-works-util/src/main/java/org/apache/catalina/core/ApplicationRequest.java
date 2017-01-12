package org.apache.catalina.core;


import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import org.apache.catalina.Globals;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.ServletRequest</code>
 * that transforms an application request object (which might be the original
 * one passed to a servlet, or might be based on the 2.3
 * <code>javax.servlet.ServletRequestWrapper</code> class)
 * back into an internal <code>org.apache.catalina.Request</code>.
 * <p>
 * <strong>WARNING</strong>:  Due to Java's lack of support for multiple
 * inheritance, all of the logic in <code>ApplicationRequest</code> is
 * duplicated in <code>ApplicationHttpRequest</code>.  Make sure that you
 * keep these two classes in synchronization when making changes!
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.7 $ $Date: 2001/07/22 20:25:08 $
 */

class ApplicationRequest extends ServletRequestWrapper {


    // ------------------------------------------------------- Static Variables


    /**
     * The set of attribute names that are special for request dispatchers.
     */
    protected static final String specials[] =
    { Globals.REQUEST_URI_ATTR, Globals.CONTEXT_PATH_ATTR,
      Globals.SERVLET_PATH_ATTR, Globals.PATH_INFO_ATTR,
      Globals.QUERY_STRING_ATTR };


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped request around the specified servlet request.
     *
     * @param request The servlet request being wrapped
     */
    public ApplicationRequest(ServletRequest request) {

        super(request);
        setRequest(request);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The request attributes for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Override the <code>getAttribute()</code> method of the wrapped request.
     *
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    public Enumeration getAttributeNames() {

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Override the <code>removeAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
            if (!isSpecial(name))
                getRequest().removeAttribute(name);
        }

    }


    /**
     * Override the <code>setAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {

        synchronized (attributes) {
            attributes.put(name, value);
            if (!isSpecial(name))
                getRequest().setAttribute(name, value);
        }

    }


    // ------------------------------------------ ServletRequestWrapper Methods


    /**
     * Set the request that we are wrapping.
     *
     * @param request The new wrapped request
     */
    public void setRequest(ServletRequest request) {

        super.setRequest(request);

        // Initialize the attributes for this request
        synchronized (attributes) {
            attributes.clear();
            Enumeration names = request.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                Object value = request.getAttribute(name);
                attributes.put(name, value);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Is this attribute name one of the special ones that is added only for
     * included servlets?
     *
     * @param name Attribute name to be tested
     */
    protected boolean isSpecial(String name) {

        for (int i = 0; i < specials.length; i++) {
            if (specials[i].equals(name))
                return (true);
        }
        return (false);

    }


}
