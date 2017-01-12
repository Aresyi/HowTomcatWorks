/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/RequestStream.java,v 1.6 2002/03/18 07:15:39 remm Exp $
 * $Revision: 1.6 $
 * $Date: 2002/03/18 07:15:39 $
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


package org.apache.catalina.connector;


import java.io.InputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import org.apache.catalina.Request;
import org.apache.catalina.util.StringManager;


/**
 * Convenience implementation of <b>ServletInputStream</b> that works with
 * the standard implementations of <b>Request</b>.  If the content length has
 * been set on our associated Request, this implementation will enforce
 * not reading more than that many bytes on the underlying stream.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

public class RequestStream
    extends ServletInputStream {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet input stream associated with the specified Request.
     *
     * @param request The associated request
     */
    public RequestStream(Request request) {

        super();
        closed = false;
        count = 0;
        length = request.getRequest().getContentLength();
        stream = request.getStream();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;


    /**
     * The number of bytes which have already been returned by this stream.
     */
    protected int count = 0;


    /**
     * The content length past which we will not read, or -1 if there is
     * no defined content length.
     */
    protected int length = -1;


    /**
     * The localized strings for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The underlying input stream from which we should read data.
     */
    protected InputStream stream = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Close this input stream.  No physical level I-O is performed, but
     * any further attempt to read from this stream will throw an IOException.
     * If a content length has been set but not all of the bytes have yet been
     * consumed, the remaining bytes will be swallowed.
     */
    public void close() throws IOException {

        if (closed)
            throw new IOException(sm.getString("requestStream.close.closed"));

        if (length > 0) {
            while (count < length) {
                int b = read();
                if (b < 0)
                    break;
            }
        }

        closed = true;

    }



    /**
     * Read and return a single byte from this input stream, or -1 if end of
     * file has been encountered.
     *
     * @exception IOException if an input/output error occurs
     */
    public int read() throws IOException {

        // Has this stream been closed?
        if (closed)
            throw new IOException(sm.getString("requestStream.read.closed"));

        // Have we read the specified content length already?
        if ((length >= 0) && (count >= length))
            return (-1);        // End of file indicator

        // Read and count the next byte, then return it
        int b = stream.read();
        if (b >= 0)
            count++;
        return (b);

    }


    /**
     * Read some number of bytes from the input stream, and store them
     * into the buffer array b.  The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     *
     * @param b The buffer into which the data is read
     *
     * @exception IOException if an input/output error occurs
     */
    public int read(byte b[]) throws IOException {

        return (read(b, 0, b.length));

    }


    /**
     * Read up to <code>len</code> bytes of data from the input stream
     * into an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read,
     * possibly zero.  The number of bytes actually read is returned as
     * an integer.  This method blocks until input data is available,
     * end of file is detected, or an exception is thrown.
     *
     * @param b The buffer into which the data is read
     * @param off The start offset into array <code>b</code> at which
     *  the data is written
     * @param len The maximum number of bytes to read
     *
     * @exception IOException if an input/output error occurs
     */
    public int read(byte b[], int off, int len) throws IOException {

        int toRead = len;
        if (length > 0) {
            if (count >= length)
                return (-1);
            if ((count + len) > length)
                toRead = length - count;
        }
        int actuallyRead = super.read(b, off, toRead);
        return (actuallyRead);

    }


}
