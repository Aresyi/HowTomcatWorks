/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/connector/http/HttpResponseStream.java,v 1.14 2002/03/18 07:15:40 remm Exp $
 * $Revision: 1.14 $
 * $Date: 2002/03/18 07:15:40 $
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


package org.apache.catalina.connector.http;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ResponseStream;

/**
 * Response stream for the HTTP/1.1 connector. This stream will automatically
 * chunk the answer if using HTTP/1.1 and no Content-Length has been properly
 * set.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @deprecated
 */
public final class HttpResponseStream extends ResponseStream {


    // ----------------------------------------------------------- Constructors


    private static final int MAX_CHUNK_SIZE = 4096;


    private static final String CRLF = "\r\n";


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a servlet output stream associated with the specified Request.
     *
     * @param response The associated response
     */
    public HttpResponseStream(HttpResponseImpl response) {

        super(response);
        checkChunking(response);
        checkHead(response);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * True if chunking is allowed.
     */
    private boolean useChunking;


    /**
     * True if printing a chunk.
     */
    private boolean writingChunk;


    /**
     * True if no content should be written.
     */
    private boolean writeContent;


    // -------------------------------------------- ServletOutputStream Methods


    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(int b)
        throws IOException {

        if (suspended)
            return;

        if (!writeContent)
            return;

        if (useChunking && !writingChunk) {
            writingChunk = true;
            try {
                print("1\r\n");
                super.write(b);
                println();
            } finally {
                writingChunk = false;
            }
        } else {
            super.write(b);
        }

    }


    /**
     * Write the specified byte array.
     */
    public void write(byte[] b, int off, int len)
        throws IOException {

        if (suspended)
            return;

        if (!writeContent)
            return;

        if (useChunking && !writingChunk) {
            if (len > 0) {
                writingChunk = true;
                try {
                    println(Integer.toHexString(len));
                    super.write(b, off, len);
                    println();
                } finally {
                    writingChunk = false;
                }
            }
        } else {
            super.write(b, off, len);
        }

    }


    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    public void close() throws IOException {

        if (suspended)
            throw new IOException
                (sm.getString("responseStream.suspended"));

        if (!writeContent)
            return;

        if (useChunking) {
            // Write the final chunk.
            writingChunk = true;
            try {
                print("0\r\n\r\n");
            } finally {
                writingChunk = false;
            }
        }
        super.close();

    }


    // -------------------------------------------------------- Package Methods


    void checkChunking(HttpResponseImpl response) {
        // If any data has already been written to the stream, we must not
        // change the chunking mode
        if (count != 0)
            return;
        // Check the basic cases in which we chunk
        useChunking =
            (!response.isCommitted()
             && response.getContentLength() == -1
             && response.getStatus() != HttpServletResponse.SC_NOT_MODIFIED);
        if (!response.isChunkingAllowed() && useChunking) {
            // If we should chunk, but chunking is forbidden by the connector,
            // we close the connection
            response.setHeader("Connection", "close");
        }
        // Don't chunk is the connection will be closed
        useChunking = (useChunking && !response.isCloseConnection());
        if (useChunking) {
            response.setHeader("Transfer-Encoding", "chunked");
        } else if (response.isChunkingAllowed()) {
            response.removeHeader("Transfer-Encoding", "chunked");
        }
    }


    protected void checkHead(HttpResponseImpl response) {
        HttpServletRequest servletRequest = 
            (HttpServletRequest) response.getRequest();
        if ("HEAD".equals(servletRequest.getMethod())) {
            writeContent = false;
        } else {
            writeContent = true;
        }
    }


}
