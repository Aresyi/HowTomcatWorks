/*
 * SSIFsize.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIFsize.java,v 1.2 2002/06/05 19:09:17 dsandberg Exp $
 * $Revision: 1.2 $
 * $Date: 2002/06/05 19:09:17 $
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

package org.apache.catalina.ssi;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * Implements the Server-side #fsize command
 *
 * @author Bip Thelin
 * @author Dan Sandberg
 * @version $Revision: 1.2 $, $Date: 2002/06/05 19:09:17 $
 */
public final class SSIFsize implements SSICommand {
    protected final static int ONE_KILOBYTE = 1024;
    protected final static int ONE_MEGABYTE = 1024 * 1024;

    /**
     * @see SSICommand
     */
    public void process(SSIMediator ssiMediator,
			  String[] paramNames,
			  String[] paramValues,
			  PrintWriter writer) {

	String configErrMsg = ssiMediator.getConfigErrMsg();
        for(int i=0;i<paramNames.length;i++) {
	    String paramName = paramNames[i];
	    String paramValue = paramValues[i];

	    try {
		if ( paramName.equalsIgnoreCase("file") ||
		     paramName.equalsIgnoreCase("virtual") ) {
		    boolean virtual = paramName.equalsIgnoreCase("virtual");
		    long size = ssiMediator.getFileSize( paramValue,  virtual );
		    String configSizeFmt = ssiMediator.getConfigSizeFmt();
		    writer.write( formatSize(size, configSizeFmt ) );
		} else {
		    ssiMediator.log("#fsize--Invalid attribute: " + paramName );
		    writer.write( configErrMsg );
		}
	    } catch ( IOException e ) {
		ssiMediator.log("#fsize--Couldn't get size for file: " + paramValue, e );
		writer.write( configErrMsg );
	    }
	}
    }

    public String repeat( char aChar, int numChars ) {
	if ( numChars < 0 ) {
	    throw new IllegalArgumentException("Num chars can't be negative");
	}
	StringBuffer buf = new StringBuffer();
	for ( int i=0; i < numChars; i++ ) {
	    buf.append( aChar );
	}
	return buf.toString();
    }

    public String padLeft( String str, int maxChars ) {
	String result = str;
	int charsToAdd = maxChars - str.length();
	if ( charsToAdd > 0 ) {
	    result = repeat( ' ', charsToAdd ) + str;
	}
	return result;
    }




    //We try to mimick Apache here, as we do everywhere
    //All the 'magic' numbers are from the util_script.c Apache source file.
    protected String formatSize(long size, String format) {
        String retString = "";

        if ( format.equalsIgnoreCase("bytes") ) {
	    DecimalFormat decimalFormat = new DecimalFormat("#,##0");
	    retString = decimalFormat.format( size );
        } else {
	    if ( size == 0 ) {
		retString = "0k";
            } else if ( size < ONE_KILOBYTE ) {
		retString = "1k";
	    } else if ( size < ONE_MEGABYTE ) {
		retString = Long.toString( (size + 512) / ONE_KILOBYTE );
		retString += "k";
            } else if ( size  < 99 * ONE_MEGABYTE ) {
		DecimalFormat decimalFormat = new DecimalFormat("0.0M");
		retString = decimalFormat.format( size  / (double) ONE_MEGABYTE );
	    } else {
		retString = Long.toString( (size + ( 529 * ONE_KILOBYTE) ) / ONE_MEGABYTE );
		retString += "M";
	    }
	    retString = padLeft( retString, 5 );
        }

        return retString;
    }
}

