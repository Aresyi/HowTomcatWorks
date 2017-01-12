/*
 * SSIMediator.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIMediator.java,v 1.1 2002/05/24 04:38:58 billbarker Exp $
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import org.apache.catalina.util.DateTool;
import org.apache.catalina.util.URLEncoder;
import org.apache.catalina.util.Strftime;

/**
 * Allows the different SSICommand implementations to share data/talk to each other
 *
 * @author Bip Thelin
 * @author Amy Roh
 * @author Dan Sandberg
 * @version $Revision: 1.1 $, $Date: 2002/05/24 04:38:58 $
 */
public class SSIMediator {
    protected final static String DEFAULT_CONFIG_ERR_MSG = "[an error occurred while processing this directive]";
    protected final static String DEFAULT_CONFIG_TIME_FMT = "%A, %d-%b-%Y %T %Z";
    protected final static String DEFAULT_CONFIG_SIZE_FMT = "abbrev";
    protected static URLEncoder urlEncoder;
    protected String configErrMsg = DEFAULT_CONFIG_ERR_MSG;
    protected String configTimeFmt = DEFAULT_CONFIG_TIME_FMT;
    protected String configSizeFmt = DEFAULT_CONFIG_SIZE_FMT;
    protected String className = getClass().getName();
    protected SSIExternalResolver ssiExternalResolver;
    protected Date lastModifiedDate;
    protected int debug;
    protected Strftime strftime;

    static {
	//We try to encode only the same characters that apache does
	urlEncoder = new URLEncoder();
	urlEncoder.addSafeCharacter(',');
	urlEncoder.addSafeCharacter(':');
	urlEncoder.addSafeCharacter('-');
        urlEncoder.addSafeCharacter('_');
        urlEncoder.addSafeCharacter('.');
        urlEncoder.addSafeCharacter('*');
        urlEncoder.addSafeCharacter('/');
        urlEncoder.addSafeCharacter('!');
        urlEncoder.addSafeCharacter('~');
        urlEncoder.addSafeCharacter('\'');
        urlEncoder.addSafeCharacter('(');
        urlEncoder.addSafeCharacter(')');
    }

    public SSIMediator( SSIExternalResolver ssiExternalResolver, 
			Date lastModifiedDate,
			int debug ) {
	this.ssiExternalResolver = ssiExternalResolver;	
	this.lastModifiedDate = lastModifiedDate;
	this.debug = debug;

	setConfigTimeFmt( DEFAULT_CONFIG_TIME_FMT, true );
    }
    
    public void setConfigErrMsg( String configErrMsg ) {
	this.configErrMsg = configErrMsg;
    }

    public void setConfigTimeFmt( String configTimeFmt ) {
	setConfigTimeFmt( configTimeFmt, false );
    }

    public void setConfigTimeFmt( String configTimeFmt, boolean fromConstructor ) {
	this.configTimeFmt = configTimeFmt;

	//What's the story here with DateTool.LOCALE_US?? Why??
	this.strftime = new Strftime( configTimeFmt, DateTool.LOCALE_US );

	//Variables like DATE_LOCAL, DATE_GMT, and LAST_MODIFIED need to be updated when
	//the timefmt changes.  This is what Apache SSI does.
	setDateVariables( fromConstructor );
    }

    public void setConfigSizeFmt( String configSizeFmt ) {
	this.configSizeFmt = configSizeFmt;
    }

    public String getConfigErrMsg() {
	return configErrMsg;
    }

    public String getConfigTimeFmt() {
	return configTimeFmt;
    }

    public String getConfigSizeFmt() {
	return configSizeFmt;
    }

    public Collection getVariableNames() {
	Set variableNames = new HashSet();
	//These built-in variables are supplied by the mediator ( if not over-written by the user ) and always exist
	variableNames.add( "DATE_GMT" );
	variableNames.add( "DATE_LOCAL" );
	variableNames.add( "LAST_MODIFIED" );
	ssiExternalResolver.addVariableNames( variableNames );

	//Remove any variables that are reserved by this class
	Iterator iter = variableNames.iterator();
	while ( iter.hasNext() ) {
	    String name = (String) iter.next();
	    if ( isNameReserved( name ) ) {
		iter.remove();
	    }
	}
	return variableNames;	   
    }

    public long getFileSize( String path, boolean virtual ) throws IOException {
	return ssiExternalResolver.getFileSize( path, virtual );
    }

    public long getFileLastModified( String path, boolean virtual ) throws IOException {
	return ssiExternalResolver.getFileLastModified( path, virtual );
    }
    
    public String getFileText( String path, boolean virtual ) throws IOException {
	return ssiExternalResolver.getFileText( path, virtual );
    }

    protected boolean isNameReserved( String name ) {
	return name.startsWith( className + "." );
    }

    public String getVariableValue( String variableName ) {
	return getVariableValue( variableName, "none" );
    }

    public void setVariableValue( String variableName, String variableValue ) {
	if ( !isNameReserved( variableName ) ) {
	    ssiExternalResolver.setVariableValue( variableName, variableValue );
	}
    }

    public String getVariableValue( String variableName, String encoding ) {
	String lowerCaseVariableName = variableName.toLowerCase();
	String variableValue = null;

	if ( !isNameReserved( lowerCaseVariableName ) ) {
	    //Try getting it externally first, if it fails, try getting the 'built-in' value
	    variableValue = ssiExternalResolver.getVariableValue( variableName );
	    if ( variableValue == null ) {
		variableName = variableName.toUpperCase();
		variableValue = (String) ssiExternalResolver.getVariableValue( className + "." + variableName );
	    }
	    if ( variableValue != null ) {
		variableValue = encode( variableValue, encoding );
	    }
	}
	return variableValue;
    }

    protected String formatDate( Date date, TimeZone timeZone ) {
	String retVal;

	if ( timeZone != null ) {
	    //we temporarily change strftime.  Since SSIMediator is inherently single-threaded, this
	    //isn't a problem
	    TimeZone oldTimeZone = strftime.getTimeZone();
	    strftime.setTimeZone( timeZone );
	    retVal = strftime.format(date);    
	    strftime.setTimeZone( oldTimeZone );
	} else {
	    retVal = strftime.format(date);    
	}
	return retVal;
    }

    protected String encode( String value, String encoding ) {
	String retVal = null;

	if ( encoding.equalsIgnoreCase( "url" ) ) {
	    retVal = urlEncoder.encode( value );
	} else if ( encoding.equalsIgnoreCase( "none" ) ) {
	    retVal = value;
	} else if ( encoding.equalsIgnoreCase( "entity" ) ) {
	    //Not sure how this is really different than none
	    retVal = value;
	} else {
	    //This shouldn't be possible
	    throw new IllegalArgumentException("Unknown encoding: " + encoding);
	}
 	return retVal;
    }

    public void log( String message ) {
	ssiExternalResolver.log( message, null );
    }

    public void log( String message, Throwable throwable ) {
	ssiExternalResolver.log( message, throwable );
    }

    protected void setDateVariables( boolean fromConstructor ) {
	boolean alreadySet = ssiExternalResolver.getVariableValue( className + ".alreadyset" ) != null;
	//skip this if we are being called from the constructor, and this has already been set
	if ( !( fromConstructor && alreadySet ) ) {
	    ssiExternalResolver.setVariableValue( className + ".alreadyset", "true" );

	    Date date = new Date();
	    TimeZone timeZone = TimeZone.getTimeZone("GMT");
	    String retVal =  formatDate( date, timeZone );
	    
	    //If we are setting on of the date variables, we want to remove them from the user
	    //defined list of variables, because this is what Apache does
	    setVariableValue ( "DATE_GMT", null ); 
	    ssiExternalResolver.setVariableValue ( className + ".DATE_GMT", retVal ); 
	    
	    retVal = formatDate( date, null );
	    setVariableValue ( "DATE_LOCAL", null );
	    ssiExternalResolver.setVariableValue ( className + ".DATE_LOCAL", retVal );
	    
	    retVal = formatDate( lastModifiedDate, null );
	    setVariableValue ( "LAST_MODIFIED", null );
	    ssiExternalResolver.setVariableValue ( className + ".LAST_MODIFIED", retVal );
	}
    }
}
