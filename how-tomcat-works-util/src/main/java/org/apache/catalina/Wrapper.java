/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Wrapper.java,v 1.5 2001/07/22 20:13:30 pier Exp $
 * $Revision: 1.5 $
 * $Date: 2001/07/22 20:13:30 $
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


package org.apache.catalina;


import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;


/**
 * A <b>Wrapper</b> is a Container that represents an individual servlet
 * definition from the deployment descriptor of the web application.  It
 * provides a convenient mechanism to use Interceptors that see every single
 * request to the servlet represented by this definition.
 * <p>
 * Implementations of Wrapper are responsible for managing the servlet life
 * cycle for their underlying servlet class, including calling init() and
 * destroy() at appropriate times, as well as respecting the existence of
 * the SingleThreadModel declaration on the servlet class itself.
 * <p>
 * The parent Container attached to a Wrapper will generally be an
 * implementation of Context, representing the servlet context (and
 * therefore the web application) within which this servlet executes.
 * <p>
 * Child Containers are not allowed on Wrapper implementations, so the
 * <code>addChild()</code> method should throw an
 * <code>IllegalArgumentException</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/07/22 20:13:30 $
 */

public interface Wrapper extends Container {


    // ------------------------------------------------------------- Properties


    /**
     * Return the available date/time for this servlet, in milliseconds since
     * the epoch.  If this date/time is in the future, any request for this
     * servlet will return an SC_SERVICE_UNAVAILABLE error.  If it is zero,
     * the servlet is currently available.  A value equal to Long.MAX_VALUE
     * is considered to mean that unavailability is permanent.
     */
    public long getAvailable();


    /**
     * Set the available date/time for this servlet, in milliseconds since the
     * epoch.  If this date/time is in the future, any request for this servlet
     * will return an SC_SERVICE_UNAVAILABLE error.  A value equal to
     * Long.MAX_VALUE is considered to mean that unavailability is permanent.
     *
     * @param available The new available date/time
     */
    public void setAvailable(long available);


    /**
     * Return the context-relative URI of the JSP file for this servlet.
     */
    public String getJspFile();


    /**
     * Set the context-relative URI of the JSP file for this servlet.
     *
     * @param jspFile JSP file URI
     */
    public void setJspFile(String jspFile);


    /**
     * Return the load-on-startup order value (negative value means
     * load on first call).
     */
    public int getLoadOnStartup();


    /**
     * Set the load-on-startup order value (negative value means
     * load on first call).
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartup(int value);


    /**
     * Return the run-as identity for this servlet.
     */
    public String getRunAs();


    /**
     * Set the run-as identity for this servlet.
     *
     * @param value New run-as identity value
     */
    public void setRunAs(String runAs);


    /**
     * Return the fully qualified servlet class name for this servlet.
     */
    public String getServletClass();


    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param servletClass Servlet class name
     */
    public void setServletClass(String servletClass);


    /**
     * Is this servlet currently unavailable?
     */
    public boolean isUnavailable();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new servlet initialization parameter for this servlet.
     *
     * @param name Name of this initialization parameter to add
     * @param value Value of this initialization parameter to add
     */
    public void addInitParameter(String name, String value);


    /**
     * Add a new listener interested in InstanceEvents.
     *
     * @param listener The new listener
     */
    public void addInstanceListener(InstanceListener listener);


    /**
     * Add a new security role reference record to the set of records for
     * this servlet.
     *
     * @param name Role name used within this servlet
     * @param link Role name used within the web application
     * @param description Description of this security role reference
     */
    public void addSecurityReference(String name, String link);


    /**
     * Allocate an initialized instance of this Servlet that is ready to have
     * its <code>service()</code> method called.  If the servlet class does
     * not implement <code>SingleThreadModel</code>, the (only) initialized
     * instance may be returned immediately.  If the servlet class implements
     * <code>SingleThreadModel</code>, the Wrapper implementation must ensure
     * that this instance is not allocated again until it is deallocated by a
     * call to <code>deallocate()</code>.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if a loading error occurs
     */
    public Servlet allocate() throws ServletException;


    /**
     * Return this previously allocated servlet to the pool of available
     * instances.  If this servlet class does not implement SingleThreadModel,
     * no action is actually required.
     *
     * @param servlet The servlet to be returned
     *
     * @exception ServletException if a deallocation error occurs
     */
    public void deallocate(Servlet servlet) throws ServletException;


    /**
     * Return the value for the specified initialization parameter name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Name of the requested initialization parameter
     */
    public String findInitParameter(String name);


    /**
     * Return the names of all defined initialization parameters for this
     * servlet.
     */
    public String[] findInitParameters();


    /**
     * Return the security role link for the specified security role
     * reference name, if any; otherwise return <code>null</code>.
     *
     * @param name Security role reference used within this servlet
     */
    public String findSecurityReference(String name);


    /**
     * Return the set of security role reference names associated with
     * this servlet, if any; otherwise return a zero-length array.
     */
    public String[] findSecurityReferences();


    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance.  This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if some other loading problem occurs
     */
    public void load() throws ServletException;


    /**
     * Remove the specified initialization parameter from this servlet.
     *
     * @param name Name of the initialization parameter to remove
     */
    public void removeInitParameter(String name);


    /**
     * Remove a listener no longer interested in InstanceEvents.
     *
     * @param listener The listener to remove
     */
    public void removeInstanceListener(InstanceListener listener);


    /**
     * Remove any security role reference for the specified role name.
     *
     * @param name Security role used within this servlet to be removed
     */
    public void removeSecurityReference(String name);


    /**
     * Process an UnavailableException, marking this servlet as unavailable
     * for the specified amount of time.
     *
     * @param unavailable The exception that occurred, or <code>null</code>
     *  to mark this servlet as permanently unavailable
     */
    public void unavailable(UnavailableException unavailable);


    /**
     * Unload all initialized instances of this servlet, after calling the
     * <code>destroy()</code> method for each instance.  This can be used,
     * for example, prior to shutting down the entire servlet engine, or
     * prior to reloading all of the classes from the Loader associated with
     * our Loader's repository.
     *
     * @exception ServletException if an unload error occurs
     */
    public void unload() throws ServletException;


}
