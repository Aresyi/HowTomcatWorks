/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/cluster/ClusterSessionBase.java,v 1.3 2001/07/22 20:25:06 pier Exp $
 * $Revision: 1.3 $
 * $Date: 2001/07/22 20:25:06 $
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

package org.apache.catalina.cluster;

import org.apache.catalina.Logger;
import org.apache.catalina.util.StringManager;

/**
 * This is an abstract implementation of <code>ClusterSender</code>
 * and <code>ClusterReceiver</code> which provide basic functionallity
 * shared by the two components.
 *
 * @author Bip Thelin
 * @version $Revision: 1.3 $, $Date: 2001/07/22 20:25:06 $
 */

public abstract class ClusterSessionBase {

    // ----------------------------------------------------- Instance Variables

    /**
     * The senderId associated with this component
     */
    private String senderId = null;

    /**
     * The debug level for this component
     */
    private int debug = 0;

    /**
     * The Logger associated with this component.
     */
    private Logger logger = null;

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.Package);

    // --------------------------------------------------------- Public Methods

    /**
     * The senderId is a identifier used to identify different
     * packagesin a Cluster. Each package received or send through
     * the concrete implementation of this interface will have
     * the senderId set at runtime. Usually the senderId is the
     * name of the component that is using this component.
     *
     * @param senderId The senderId to use
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * get the senderId used to identify messages being
     * send or received in a Cluster.
     *
     * @return The senderId for this component
     */
    public String getSenderId() {
        return(this.senderId);
    }

    /**
     * Set the debug detail level for this component.
     *
     * @param debug The debug level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    /**
     * Get the debug level for this component
     *
     * @return The debug level
     */
    public int getDebug() {
        return(this.debug);
    }

    /**
     * Set the Logger for this component.
     *
     * @param debug The Logger to use with this component.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Get the Logger for this component
     *
     * @return The Logger associated with this component.
     */
    public Logger getLogger() {
        return(this.logger);
    }

    public abstract String getName();

    /**
     * The log method to use in the implementation
     *
     * @param message The message to be logged.
     */
    public void log(String message) {
        Logger logger = getLogger();

        if(logger != null)
            logger.log("[Cluster/"+getName()+"]: "+message);
        else
            System.out.println("[Cluster/"+getName()+"]: "+message);
    }
}
