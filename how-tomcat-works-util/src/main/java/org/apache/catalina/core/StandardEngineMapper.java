/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/core/StandardEngineMapper.java,v 1.5 2001/07/22 20:25:08 pier Exp $
 * $Revision: 1.5 $
 * $Date: 2001/07/22 20:25:08 $
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


package org.apache.catalina.core;


import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.Mapper;
import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of <code>Mapper</code> for an <code>Engine</code>,
 * designed to process HTTP requests.  This mapper selects an appropriate
 * <code>Host</code> based on the server name included in the request.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  This Mapper only works with a
 * <code>StandardEngine</code>, because it relies on internal APIs.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/07/22 20:25:08 $
 */

public class StandardEngineMapper
    implements Mapper {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Mapper is associated.
     */
    private StandardEngine engine = null;


    /**
     * The protocol with which this Mapper is associated.
     */
    private String protocol = null;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Mapper is associated.
     */
    public Container getContainer() {

        return (engine);

    }


    /**
     * Set the Container with which this Mapper is associated.
     *
     * @param container The newly associated Container
     *
     * @exception IllegalArgumentException if this Container is not
     *  acceptable to this Mapper
     */
    public void setContainer(Container container) {

        if (!(container instanceof StandardEngine))
            throw new IllegalArgumentException
                (sm.getString("httpEngineMapper.container"));
        engine = (StandardEngine) container;

    }


    /**
     * Return the protocol for which this Mapper is responsible.
     */
    public String getProtocol() {

        return (this.protocol);

    }


    /**
     * Set the protocol for which this Mapper is responsible.
     *
     * @param protocol The newly associated protocol
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the child Container that should be used to process this Request,
     * based upon its characteristics.  If no such child Container can be
     * identified, return <code>null</code> instead.
     *
     * @param request Request being processed
     * @param update Update the Request to reflect the mapping selection?
     */
    public Container map(Request request, boolean update) {

        int debug = engine.getDebug();

        // Extract the requested server name
        String server = request.getRequest().getServerName();
        if (server == null) {
            server = engine.getDefaultHost();
            if (update)
                request.setServerName(server);
        }
        if (server == null)
            return (null);
        server = server.toLowerCase();
        if (debug >= 1)
            engine.log("Mapping server name '" + server + "'");

        // Find the matching child Host directly
        if (debug >= 2)
            engine.log(" Trying a direct match");
        Host host = (Host) engine.findChild(server);

        // Find a matching Host by alias.  FIXME - Optimize this!
        if (host == null) {
            if (debug >= 2)
                engine.log(" Trying an alias match");
            Container children[] = engine.findChildren();
            for (int i = 0; i < children.length; i++) {
                String aliases[] = ((Host) children[i]).findAliases();
                for (int j = 0; j < aliases.length; j++) {
                    if (server.equals(aliases[j])) {
                        host = (Host) children[i];
                        break;
                    }
                }
                if (host != null)
                    break;
            }
        }

        // Trying the "default" host if any
        if (host == null) {
            if (debug >= 2)
                engine.log(" Trying the default host");
            host = (Host) engine.findChild(engine.getDefaultHost());
        }

        // Update the Request if requested, and return the selected Host
        ;       // No update to the Request is required
        return (host);

    }


}
