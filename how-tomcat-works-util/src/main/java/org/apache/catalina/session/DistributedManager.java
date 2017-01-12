/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/DistributedManager.java,v 1.5 2002/01/03 08:52:57 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/01/03 08:52:57 $
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

package org.apache.catalina.session;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.cluster.ClusterSender;
import org.apache.catalina.cluster.ClusterReceiver;
import org.apache.catalina.cluster.ReplicationWrapper;
import org.apache.catalina.util.CustomObjectInputStream;

/**
 * This manager is responsible for in memory replication of
 * Sessions across a defined Cluster. It could also utilize a
 * Store to make Sessions persistence.
 *
 * @author Bip Thelin
 * @version $Revision: 1.5 $, $Date: 2002/01/03 08:52:57 $
 */

public final class DistributedManager extends PersistentManagerBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "DistributedManager/1.0";

    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static String name = "DistributedManager";

    /**
     * Our ClusterSender, used when replicating sessions
     */
    private ClusterSender clusterSender = null;

    /**
     * Our ClusterReceiver
     */
    private ClusterReceiver clusterReceiver = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (this.info);
    }

    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {
        return (this.name);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create a Session and replicate it in our Cluster
     *
     * @return The newly created Session
     */
    public Session createSession() {
        Session session = super.createSession();
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));

            ((StandardSession)session).writeObjectData(oos);
            oos.close();

            byte[] obs = bos.toByteArray();
            clusterSender.send(obs);

            if(debug > 0)
                log("Replicating Session: "+session.getId());
        } catch (IOException e) {
            log("An error occurred when replicating Session: "+session.getId());
        }

        return (session);
    }

    /**
     * Start this manager
     *
     * @exception LifecycleException if an error occurs
     */
    public void start() throws LifecycleException {
        Container container = getContainer();
        Cluster cluster = null;

        if(container != null)
            cluster = container.getCluster();

        if(cluster != null) {
            this.clusterSender = cluster.getClusterSender(getName());
            this.clusterReceiver = cluster.getClusterReceiver(getName());
        }

        super.start();
    }

    /**
     * Called from our background thread to process new received Sessions
     *
     */
    public void processClusterReceiver() {
        Object[] objs = clusterReceiver.getObjects();
        StandardSession _session = null;
        ByteArrayInputStream bis = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        byte[] buf = new byte[5000];
        ReplicationWrapper repObj = null;

        for(int i=0; i < objs.length;i++) {
            try {
                bis = new ByteArrayInputStream(buf);
                repObj = (ReplicationWrapper)objs[i];
                buf = repObj.getDataStream();
                bis = new ByteArrayInputStream(buf, 0, buf.length);

                if (container != null)
                    loader = container.getLoader();

                if (loader != null)
                    classLoader = loader.getClassLoader();

                if (classLoader != null)
                    ois = new CustomObjectInputStream(bis,
                                                      classLoader);
                else
                    ois = new ObjectInputStream(bis);

                _session = (StandardSession) super.createSession();
                _session.readObjectData(ois);
                _session.setManager(this);

                if (debug > 0)
                    log("Loading replicated session: "+_session.getId());
            } catch (IOException e) {
                log("Error occurred when trying to read replicated session: "+
                    e.toString());
            } catch (ClassNotFoundException e) {
                log("Error occurred when trying to read replicated session: "+
                    e.toString());
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                        bis = null;
                    } catch (IOException e) {
                        ;
                    }
                }
            }
        }
    }

    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            processClusterReceiver();
            processExpires();
            processPersistenceChecks();
        }
    }
}
