/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/cluster/MulticastReceiver.java,v 1.5 2002/01/03 08:52:56 remm Exp $
 * $Revision: 1.5 $
 * $Date: 2002/01/03 08:52:56 $
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;


/**
 * This class is responsible for checking for incoming multicast
 * data and determine if the data belongs to us and if so push
 * it onto an internal stack and let it be picked up when needed.
 *
 * @author Bip Thelin
 * @version $Revision: 1.5 $, $Date: 2002/01/03 08:52:56 $
 */

public final class MulticastReceiver
    extends ClusterSessionBase implements ClusterReceiver {

    // ----------------------------------------------------- Instance Variables

    /**
     * The unique message ID
     */
    private static String senderId = null;

    /**
     * The MulticastSocket to use
     */
    private MulticastSocket multicastSocket = null;

    /**
     * Our Thread name
     */
    private String threadName = "MulticastReceiver";

    /**
     * The name of our component, used for logging.
     */
    private String receiverName = "MulticastReceiver";

    /**
     * The stack that keeps incoming requests
     */
    private static Vector stack = new Vector();

    /**
     * Has this component been started?
     */
    private boolean started = false;

    /**
     * The background thread.
     */
    private Thread thread = null;

    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;

    /**
     * The interval for the background thread to sleep
     */
    private int checkInterval = 5;

    // --------------------------------------------------------- Public Methods

    /**
     * Create a new MulticastReceiver.
     *
     * @param senderId The unique senderId
     * @param multicastSocket The MulticastSocket to use
     */
    MulticastReceiver(String senderId, MulticastSocket multicastSocket,
                    InetAddress multicastAddress, int multicastPort) {
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
        return(this.receiverName);
    }

    /**
     * Set the time in seconds for this component to
     * Sleep before it checks for new received data in the Cluster
     *
     * @param checkInterval The time to sleep
     */
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * Get the time in seconds this Cluster sleeps
     *
     * @return The time in seconds this Cluster sleeps
     */
    public int getCheckInterval() {
        return(this.checkInterval);
    }

    /**
     * Receive the objects currently in our stack and clear
     * if afterwards.
     *
     * @return An array with objects
     */
    public Object[] getObjects() {
        synchronized (stack) {
            Object[] objs = stack.toArray();
            stack.removeAllElements();
            return (objs);
        }
    }

    /**
     * Start our component
     */
    public void start() {
        started = true;

        // Start the background reaper thread
        threadStart();
    }

    /**
     * Stop our component
     */
    public void stop() {
        started = false;

        // Stop the background reaper thread
        threadStop();
    }


    // -------------------------------------------------------- Private Methods

    /**
     * Check our multicast socket for new data and determine if the
     * data matches us(senderId) and if so push it onto the stack,
     */
    private void receive() {
        try {
            byte[] buf = new byte[5000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            ByteArrayInputStream ips = null;
            ObjectInputStream ois = null;

            multicastSocket.receive(recv);
            ips = new ByteArrayInputStream(buf, 0, buf.length);
            ois = new ObjectInputStream(ips);
            ReplicationWrapper obj = (ReplicationWrapper)ois.readObject();

            if(obj.getSenderId().equals(this.senderId))
                stack.add(obj);
        } catch (IOException e) {
            log("An error occurred when trying to replicate: "+
                e.toString());
        } catch (ClassNotFoundException e) {
            log("An error occurred when trying to replicate: "+
                e.toString());
        }
    }

    // ------------------------------------------------------ Background Thread

    /**
     * The background thread.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            receive();
            threadSleep();
        }
    }

    /**
     * Sleep for the duration specified by the <code>checkInterval</code>
     * property.
     */
    private void threadSleep() {
        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
            ;
        }
    }

    /**
     * Start the background thread.
     */
    private void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        threadName = threadName+"["+senderId+"]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Stop the background thread.
     */
    private void threadStop() {
        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;
    }
}
