/*
 * SSIExec.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIExec.java,v 1.1 2002/05/24 04:38:58 billbarker Exp $
 * $Revision: 1.1 $
 * $Date: 2002/05/24 04:38:58 $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.apache.catalina.util.IOTools;

/**
 * Implements the Server-side #exec command
 * 
 * @author Bip Thelin
 * @author Amy Roh
 * @author Dan Sandberg
 * @version $Revision: 1.1 $, $Date: 2002/05/24 04:38:58 $
 *
 */
public class SSIExec implements SSICommand {   
    protected SSIInclude ssiInclude = new SSIInclude();
    protected final static int BUFFER_SIZE = 1024;

    /**
     * @see SSICommand
     */
    public void process(SSIMediator ssiMediator,
			String[] paramNames,
			String[] paramValues,
			PrintWriter writer) {

	String configErrMsg = ssiMediator.getConfigErrMsg();
	String paramName = paramNames[0];
	String paramValue = paramValues[0];

        if ( paramName.equalsIgnoreCase("cgi") ) {
	    ssiInclude.process( ssiMediator, new String[] {"virtual"}, new String[] {paramValue}, writer );
        } else if ( paramName.equalsIgnoreCase("cmd") ) {
	    boolean foundProgram = false;
	    try {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec( paramValue );
		foundProgram = true;

		BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		char[] buf = new char[BUFFER_SIZE];
		IOTools.flow( stdErrReader, writer, buf );
		IOTools.flow( stdOutReader, writer, buf );
		proc.waitFor();
	    } catch ( InterruptedException e ) {
		ssiMediator.log( "Couldn't exec file: " + paramValue, e );
		writer.write( configErrMsg );
	    } catch ( IOException e ) {
		if ( !foundProgram ) {
		    //apache doesn't output an error message if it can't find a program
		}
		ssiMediator.log( "Couldn't exec file: " + paramValue, e );
	    }
	} 
    }
}
