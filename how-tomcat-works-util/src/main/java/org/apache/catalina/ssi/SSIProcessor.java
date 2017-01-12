/*
 * SSIProcessor.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIProcessor.java,v 1.1 2002/05/24 04:38:58 billbarker Exp $
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.catalina.util.IOTools;

/**
 * The entry point to SSI processing.  This class does the actual parsing, delegating to the SSIMediator, SSICommand, and
 * SSIExternalResolver as necessary[
 * 
 * @author Dan Sandberg
 * @version $Revision: 1.1 $, $Date: 2002/05/24 04:38:58 $
 *
 */
public class SSIProcessor {
    /** The start pattern */
    protected final static String COMMAND_START = "<!--#";

    /** The end pattern */
    protected final static String COMMAND_END = "-->";
    protected final static int BUFFER_SIZE = 4096;

    protected SSIExternalResolver ssiExternalResolver;
    protected HashMap commands = new HashMap();
    protected int debug;
    
    public SSIProcessor( SSIExternalResolver ssiExternalResolver, int debug ) {
	this.ssiExternalResolver = ssiExternalResolver;
	this.debug = debug;
	addBuiltinCommands();
    }

    protected void addBuiltinCommands() {
	addCommand( "config", new SSIConfig() );
	addCommand( "echo", new SSIEcho() );
	addCommand( "exec", new SSIExec() );
	addCommand( "include", new SSIInclude() );
	addCommand( "flastmod", new SSIFlastmod() );
	addCommand( "fsize", new SSIFsize() );
	addCommand( "printenv", new SSIPrintenv() );
	addCommand( "set", new SSISet() );
    }

    public void addCommand( String name, SSICommand command ) {
	commands.put( name, command );
    }

    /**
     * Process a file with server-side commands, reading from reader and writing the processed
     * version to writer.
     *
     * NOTE: We really should be doing this in a streaming way rather than converting it to an array first.
     *
     * @param reader the reader to read the file containing SSIs from
     * @param writer the writer to write the file with the SSIs processed.
     * @throws IOException when things go horribly awry. Should be unlikely since
     *                     the SSICommand usually catches 'normal' IOExceptions.
     */
    public void process( Reader reader, Date lastModifiedDate, PrintWriter writer ) throws IOException {
	SSIMediator ssiMediator = new SSIMediator( ssiExternalResolver, 
						   lastModifiedDate,
						   debug );

	StringWriter stringWriter = new StringWriter();
	IOTools.flow( reader, stringWriter );
	String fileContents = stringWriter.toString();
	stringWriter = null;

        int index = 0;
	boolean inside = false;
        StringBuffer command = new StringBuffer();
	try {
	    while (index < fileContents.length()) {
		char c = fileContents.charAt( index );
		if ( !inside ) {
		    if ( c == COMMAND_START.charAt( 0 ) && charCmp( fileContents, index, COMMAND_START ) ) {
			inside = true;
			index += COMMAND_START.length();
			command.setLength( 0 ); //clear the command string
		    } else {
			writer.write( c );
			index++;
		    }
		} else {
		    if ( c == COMMAND_END.charAt( 0 ) && charCmp( fileContents, index, COMMAND_END ) ) {
			inside = false;
			index += COMMAND_END.length();
			String strCmd = parseCmd(command);
			if ( debug > 0 ) {
			    ssiExternalResolver.log( "SSIProcessor.process -- processing command: " + strCmd, null );
			}
			String[] paramNames = parseParamNames(command, strCmd.length());
			String[] paramValues = parseParamValues(command, strCmd.length());
			
			//We need to fetch this value each time, since it may change during the loop
			String configErrMsg = ssiMediator.getConfigErrMsg();		    
			SSICommand ssiCommand = (SSICommand) commands.get(strCmd.toLowerCase());
			if ( ssiCommand != null ) {
			    if ( paramNames.length==paramValues.length ) {			    
				ssiCommand.process( ssiMediator, paramNames, paramValues, writer );
			    } else {
				ssiExternalResolver.log( "Parameter names count does not match parameter values count on command: " + strCmd, null );
				writer.write( configErrMsg );
			    }
			} else {
			    ssiExternalResolver.log( "Unknown command: " + strCmd, null);
			    writer.write( configErrMsg );
			}
		    } else {
			command.append( c );
			index++;		   		    		    
		    }
		}
	    }
	} catch ( SSIStopProcessingException e ) {
	    //If we are here, then we have already stopped processing, so all is good
	}	
    }

    /**
     * Parse a StringBuffer and take out the param type token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String[]'
     */
    protected String[] parseParamNames(StringBuffer cmd, int start) {
        int bIdx = start;
        int i = 0;
        int quotes = 0;
        boolean inside = false;
        StringBuffer retBuf = new StringBuffer();

        while(bIdx < cmd.length()) {
            if(!inside) {
                while(bIdx < cmd.length()&&isSpace(cmd.charAt(bIdx)))
                    bIdx++;

                if(bIdx>=cmd.length())
                    break;

                inside=!inside;
            } else {
                while(bIdx < cmd.length()&&cmd.charAt(bIdx)!='=') {
                    retBuf.append(cmd.charAt(bIdx));
                    bIdx++;
                }

                retBuf.append('"');
                inside=!inside;
                quotes=0;

                while(bIdx < cmd.length()&&quotes!=2) {
                    if(cmd.charAt(bIdx)=='"')
                            quotes++;

                    bIdx++;
                }
            }
        }

        StringTokenizer str = new StringTokenizer(retBuf.toString(), "\"");
        String[] retString = new String[str.countTokens()];

        while(str.hasMoreTokens()) {
            retString[i++] = str.nextToken().trim();
        }

        return retString;
    }

    /**
     * Parse a StringBuffer and take out the param token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String[]'
     */
    protected String[] parseParamValues(StringBuffer cmd, int start) {
        int bIdx = start;
        int i = 0;
        int quotes = 0;
        boolean inside = false;
        StringBuffer retBuf = new StringBuffer();

        while(bIdx < cmd.length()) {
            if(!inside) {
                while(bIdx < cmd.length()&&
                      cmd.charAt(bIdx)!='"')
                    bIdx++;

                if(bIdx>=cmd.length())
                    break;

                inside=!inside;
            } else {
                while(bIdx < cmd.length() && cmd.charAt(bIdx)!='"') {
                    retBuf.append(cmd.charAt(bIdx));
                    bIdx++;
                }

                retBuf.append('"');
                inside=!inside;
            }

            bIdx++;
        }

        StringTokenizer str = new StringTokenizer(retBuf.toString(), "\"");
        String[] retString = new String[str.countTokens()];

        while(str.hasMoreTokens()) {
            retString[i++] = str.nextToken();
        }

        return retString;
    }

    /**
     * Parse a StringBuffer and take out the command token.
     * Called from <code>requestHandler</code>
     * @param cmd a value of type 'StringBuffer'
     * @return a value of type 'String', or null if there is none
     */
    private String parseCmd(StringBuffer cmd) {
	int firstLetter = -1;
	int lastLetter = -1;
	for ( int i=0; i < cmd.length(); i++ ) {
	    char c = cmd.charAt( i );
	    if ( Character.isLetter( c ) ) {
		if ( firstLetter == -1 ) {
		    firstLetter = i;
		}
		lastLetter = i;
	    } else if ( isSpace( c ) ) {
		if ( lastLetter > -1 ) {
		    break;
		}
	    } else {
		break;
	    }
	}

	String command = null;
	if ( firstLetter != -1 ) {
	    command = cmd.substring( firstLetter, lastLetter + 1 );
	}
        return command;
    }

    protected boolean charCmp(String buf, int index, String command) {
	return buf.regionMatches( index, command, 0, command.length() );
    }

    protected boolean isSpace(char c) {
        return c==' '||c=='\n'||c=='\t'||c=='\r';
    }
}
