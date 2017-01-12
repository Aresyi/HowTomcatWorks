/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/cluster/ReplicationWrapper.java,v 1.1 2001/05/04 20:48:02 bip Exp $
 * $Revision: 1.1 $
 * $Date: 2001/05/04 20:48:02 $
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * A ReplicationWrapper, used when sending and receiving multicast
 * data, wrapped is the data and the senderId which is used for
 * identification.
 *
 * @author Bip Thelin
 * @version $Revision: 1.1 $, $Date: 2001/05/04 20:48:02 $
 */
public final class ReplicationWrapper implements Serializable {

    /**
     * Our buffer to hold the stream
     */
    private byte[] _buf = null;

    /**
     * Our sender Id
     */
    private String senderId = null;

    /**
     * Construct a new ReplicationWrapper
     *
     */
    public ReplicationWrapper(byte[] b, String senderId) {
        this.senderId = senderId;
        _buf = b;
    }

    /**
     * Write our stream to the <code>OutputStream</code> provided.
     *
     * @param out the OutputStream to write this stream to
     * @exception IOException if an input/output error occurs
     */
    public final void writeTo(OutputStream out) throws IOException {
        out.write(_buf);
    }

    /**
     * return our internal data as a array of bytes
     *
     * @return a our data
     */
    public final byte[] getDataStream() {
        return(_buf);
    }

    /**
     * Set the sender id for this wrapper
     *
     * @param senderId The sender id
     */
    public final void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * get the sender id for this wrapper
     *
     * @return The sender Id associated with this wrapper
     */
    public final String getSenderId() {
        return(this.senderId);
    }
}
