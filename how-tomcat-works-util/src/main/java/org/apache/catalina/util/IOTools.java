/*
 * IOTools.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/IOTools.java,v 1.1 2002/05/11 05:06:25 billbarker Exp $
 * $Revision: 1.1 $
 * $Date: 2002/05/11 05:06:25 $
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
package org.apache.catalina.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * Contains commonly needed I/O-related methods 
 *
 * @author Dan Sandberg
 */
public class IOTools {
    protected final static int DEFAULT_BUFFER_SIZE=4*1024; //4k

    //Ensure non-instantiability
    private IOTools() {
    }

     /**
     * Read input from reader and write it to writer until there is no more
     * input from reader.
     *
     * @param reader the reader to read from.
     * @param writer the writer to write to.
     * @param buf the char array to use as a bufferx
     */
    public static void flow( Reader reader, Writer writer, char[] buf ) 
        throws IOException {
        int numRead;
        while ( (numRead = reader.read(buf) ) >= 0) {
            writer.write(buf, 0, numRead);
        }
    }

    /**
     * @see flow( Reader, Writer, char[] )
     */
    public static void flow( Reader reader, Writer writer ) 
        throws IOException {
        char[] buf = new char[DEFAULT_BUFFER_SIZE];
        flow( reader, writer, buf );
    }

    /**
     * Read input from input stream and write it to output stream 
     * until there is no more input from input stream.
     *
     * @param input stream the input stream to read from.
     * @param output stream the output stream to write to.
     * @param buf the byte array to use as a buffer
     */
    public static void flow( InputStream is, OutputStream os, byte[] buf ) 
        throws IOException {
        int numRead;
        while ( (numRead = is.read(buf) ) >= 0) {
            os.write(buf, 0, numRead);
        }
    }  

    /**
     * @see flow( Reader, Writer, byte[] )
     */ 
    public static void flow( InputStream is, OutputStream os ) 
        throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        flow( is, os, buf );
    }
}
