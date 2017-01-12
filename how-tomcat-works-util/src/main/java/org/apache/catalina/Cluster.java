/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Cluster.java,v 1.5 2001/07/22 20:13:30 pier Exp $
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

import org.apache.catalina.cluster.ClusterMemberInfo;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ClusterSender;

/**
 * A <b>Cluster</b> works as a Cluster client/server for the local host
 * Different Cluster implementations can be used to support different
 * ways to communicate within the Cluster. A Cluster implementation is
 * responsible for setting up a way to communicate within the Cluster
 * and also supply "ClientApplications" with <code>ClusterSender</code>
 * used when sending information in the Cluster and
 * <code>ClusterInfo</code> used for receiving information in the Cluster.
 *
 * @author Bip Thelin
 * @version $Revision: 1.5 $, $Date: 2001/07/22 20:13:30 $
 */

public interface Cluster {

    // ------------------------------------------------------------- Properties

    /**
     * Return descriptive information about this Cluster implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();

    /**
     * Return the name of the cluster that this Server is currently
     * configured to operate within.
     *
     * @return The name of the cluster associated with this server
     */
    public String getClusterName();

    /**
     * Set the time in seconds that the Cluster waits before
     * checking for changes and replicated data.
     *
     * @param checkInterval The time in seconds to sleep
     */
    public void setCheckInterval(int checkInterval);

    /**
     * Get the time in seconds that this Cluster sleeps.
     *
     * @return The value in seconds
     */
    public int getCheckInterval();

    /**
     * Set the name of the cluster to join, if no cluster with
     * this name is present create one.
     *
     * @param clusterName The clustername to join
     */
    public void setClusterName(String clusterName);

    /**
     * Set the Container associated with our Cluster
     *
     * @param container The Container to use
     */
    public void setContainer(Container container);

    /**
     * Get the Container associated with our Cluster
     *
     * @return The Container associated with our Cluster
     */
    public Container getContainer();

    /**
     * The debug detail level for this Cluster
     *
     * @param debug The debug level
     */
    public void setDebug(int debug);

    /**
     * Returns the debug level for this Cluster
     *
     * @return The debug level
     */
    public int getDebug();

    // --------------------------------------------------------- Public Methods

    /**
     * Returns a collection containing <code>ClusterMemberInfo</code>
     * on the remote members of this Cluster. This method does
     * not include the local host, to retrieve
     * <code>ClusterMemberInfo</code> on the local host
     * use <code>getLocalClusterInfo()</code> instead.
     *
     * @return Collection with all members in the Cluster
     */
    public ClusterMemberInfo[] getRemoteClusterMembers();

    /**
     * Returns a <code>ClusterSender</code> which is the interface
     * to use when sending information in the Cluster. senderId is
     * used as a identifier so that information sent through this
     * instance can only be used with the respectice
     * <code>ClusterReceiver</code>
     *
     * @return The ClusterSender
     */
    public ClusterSender getClusterSender(String senderId);

    /**
     * Returns a <code>ClusterReceiver</code> which is the interface
     * to use when receiving information in the Cluster. senderId is
     * used as a indentifier, only information send through the
     * <code>ClusterSender</code> with the same senderId can be received.
     *
     * @return The ClusterReceiver
     */
    public ClusterReceiver getClusterReceiver(String senderId);

    /**
     * Return cluster information about the local host
     *
     * @return Cluster information
     */
    public ClusterMemberInfo getLocalClusterMember();
}
