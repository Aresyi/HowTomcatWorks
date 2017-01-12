/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Pipeline.java,v 1.4 2001/07/22 20:13:30 pier Exp $
 * $Revision: 1.4 $
 * $Date: 2001/07/22 20:13:30 $
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


package org.apache.catalina;


import java.io.IOException;
import javax.servlet.ServletException;


/**
 * <p>Interface describing a collection of Valves that should be executed
 * in sequence when the <code>invoke()</code> method is invoked.  It is
 * required that a Valve somewhere in the pipeline (usually the last one)
 * must process the request and create the corresponding response, rather
 * than trying to pass the request on.</p>
 *
 * <p>There is generally a single Pipeline instance associated with each
 * Container.  The container's normal request processing functionality is
 * generally encapsulated in a container-specific Valve, which should always
 * be executed at the end of a pipeline.  To facilitate this, the
 * <code>setBasic()</code> method is provided to set the Valve instance that
 * will always be executed last.  Other Valves will be executed in the order
 * that they were added, before the basic Valve is executed.</p>
 *
 * @author Craig R. McClanahan
 * @author Peter Donald
 * @version $Revision: 1.4 $ $Date: 2001/07/22 20:13:30 $
 */

public interface Pipeline {


    // ------------------------------------------------------------- Properties


    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    public Valve getBasic();


    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prioer to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    public void setBasic(Valve valve);


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements
     * <code>Contained</code>, with the owning Container as an argument.
     * The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.</p>
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specifie Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public void addValve(Valve valve);


    /**
     * Return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     */
    public Valve[] getValves();


    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException;


    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.  If the Valve is
     * found and removed, the Valve's <code>setContainer(null)</code> method
     * will be called if it implements <code>Contained</code>.
     *
     * @param valve Valve to be removed
     */
    public void removeValve(Valve valve);


}
