/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/cluster/MulticastSender.java,v 1.4 2001/07/22 20:25:06 pier Exp $
 * $Revision: 1.4 $
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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


/**
 * This class is responsible for sending outgoing multicast
 * packets to a Cluster.
 *
 * @author Bip Thelin
 * @version $Revision: 1.4 $, $Date: 2001/07/22 20:25:06 $
 */

public class MulticastSender
    extends ClusterSessionBase implements ClusterSender {

    // ----------------------------------------------------- Instance Variables

    /**
     * The unique message ID
     */
    private static String senderId = null;

    /**
     * The name of our component, used for logging.
     */
    private String senderName = "MulticastSender";

    /**
     * The MulticastSocket to use
     */
    private MulticastSocket multicastSocket = null;

    /**
     * The multicastAdress this socket is bound to
     */
    private InetAddress multicastAddress = null;

    /**
     * The multicastPort this socket is bound to
     */
    private int multicastPort;


    // --------------------------------------------------------- Public Methods


    /**
     * Create a new MulticastSender, only receivers with our
     * senderId will receive our data.
     *
     * @param senderId The senderId
     * @param multicastSocket the socket to use
     * @param multicastAddress the address to use
     * @param multicastPort the port to use
     */
    MulticastSender(String senderId, MulticastSocket multicastSocket,
                    InetAddress multicastAddress, int multicastPort) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.multicastSocket = multicastSocket;
        this.senderId = senderId;
    }

    /**
     * Return a <code>String</code> containing the name of this
     * implementation, used for logging
     *
     * @return The name of the implementation
     */
    public String getName() {
        return(this.senderName);
    }

    /**
     * Send an object using a multicastSocket
     *
     * @param o The object to be sent.
     */
    public void send(Object o) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));

            oos.writeObject(o);
            oos.flush();

            byte[] obs = bos.toByteArray();

            send(obs);
        } catch (IOException e) {
            log(sm.getString("multicastSender.sendException", e.toString()));
        }
    }

    /**
     * Send multicast data
     *
     * @param b data to be sent
     */
    public void send(byte[] b) {
        ReplicationWrapper out = new ReplicationWrapper(b, senderId);
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));

            oos.writeObject(out);
            oos.flush();

            byte[] obs = bos.toByteArray();
            int size = obs.length;
            DatagramPacket p = new DatagramPacket(obs, size,
                                                  multicastAddress, multicastPort);
            send(p);
        } catch (IOException e) {
            log(sm.getString("multicastSender.sendException", e.toString()));
        }
    }

    /**
     * Send multicast data
     *
     * @param p data to be sent
     */
    private synchronized void send(DatagramPacket p) {
        try {
            multicastSocket.send(p);
        } catch (IOException e) {
            log(sm.getString("multicastSender.sendException", e.toString()));
        }
    }
}
