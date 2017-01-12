/*
 * SSIServletExternalResolver.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/ssi/SSIServletExternalResolver.java,v 1.2 2002/05/26 00:00:55 remm Exp $
 * $Revision: 1.2 $
 * $Date: 2002/05/26 00:00:55 $
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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An implementation of SSIExternalResolver that is used with servlets.
 *
 * @author Dan Sandberg
 * @version $Revision: 1.2 $, $Date: 2002/05/26 00:00:55 $
 */
public class SSIServletExternalResolver implements SSIExternalResolver {
    protected final String VARIABLE_NAMES[] = {
	"AUTH_TYPE","CONTENT_LENGTH","CONTENT_TYPE","DOCUMENT_NAME",
	"DOCUMENT_URI","GATEWAY_INTERFACE","PATH_INFO", "PATH_TRANSLATED",
	"QUERY_STRING","QUERY_STRING_UNESCAPED","REMOTE_ADDR","REMOTE_HOST",
	"REMOTE_USER","REQUEST_METHOD","SCRIPT_NAME","SERVER_NAME","SERVER_PORT",
	"SERVER_PROTOCOL","SERVER_SOFTWARE" };
    
    protected HttpServlet servlet;
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected boolean isVirtualWebappRelative;
    protected int debug;

    public SSIServletExternalResolver( HttpServlet servlet, HttpServletRequest req, HttpServletResponse res, 
				       boolean isVirtualWebappRelative,
				       int debug ) {
	this.servlet = servlet;
	this.req = req;
	this.res = res;
	this.isVirtualWebappRelative = isVirtualWebappRelative;
	this.debug = debug;
    }

    public void log( String message, Throwable throwable ) {
	//We can't assume that Servlet.log( message, null )
	//is the same as Servlet.log( message ), since API
	//doesn't seem to say so.
	if ( throwable != null ) {
	    servlet.log( message, throwable );
	} else {
	    servlet.log( message );
	}
    }

    public void addVariableNames( Collection variableNames ) {
	for ( int i=0; i < VARIABLE_NAMES.length; i++ ) {
	    String variableName = VARIABLE_NAMES[ i ];
	    String variableValue = getVariableValue( variableName );
	    if ( variableValue != null ) {
		variableNames.add( variableName );
	    }
	}
	Enumeration e = req.getAttributeNames();
	while ( e.hasMoreElements() ) {
	    String name = (String) e.nextElement();
	    if ( !isNameReserved( name ) ) {
		variableNames.add( name );
	    }
	}
    }

    protected Object getReqAttributeIgnoreCase( String targetName ) {
	Object object = null;
	    
	if ( !isNameReserved( targetName ) ) {
	    object = req.getAttribute( targetName );
	    if ( object == null ) {
		Enumeration e = req.getAttributeNames();
		while ( e.hasMoreElements() ) {
		    String name = (String) e.nextElement();
		    if ( targetName.equalsIgnoreCase( name ) &&
			 !isNameReserved( name ) ) {

			object = req.getAttribute( name );
			if ( object != null ) {
			    break;
			}
		    }
		}
	    }
	}
	return object;
    }

    protected boolean isNameReserved( String name ) {
	return 
	    name.startsWith("java.") ||
	    name.startsWith("javax.") ||
	    name.startsWith("sun.");
    }

    public void setVariableValue( String name, String value ) {
	if ( !isNameReserved( name ) ) {
	    req.setAttribute( name, value );
	}
    }

    public String getVariableValue( String name ) {
	String retVal = null;

	Object object = getReqAttributeIgnoreCase( name );
	if ( object != null ) {
	    retVal = object.toString();
	} else {
	    retVal = getCGIVariable( name );
	}
	return retVal;
    }

    protected String getCGIVariable( String name ) {
	String retVal = null;

	if ( name.equalsIgnoreCase( "AUTH_TYPE" ) ) {
	    retVal = req.getAuthType();
	} else if ( name.equalsIgnoreCase( "CONTENT_LENGTH" ) ) {
	    int contentLength = req.getContentLength();
	    if ( contentLength >= 0 ) {
		retVal = Integer.toString( contentLength );
	    }
	} else if ( name.equalsIgnoreCase( "CONTENT_TYPE" ) ) {
	    retVal = req.getContentType();
	} else if ( name.equalsIgnoreCase( "DOCUMENT_NAME" ) ) {
	    String requestURI = req.getRequestURI();
	    retVal = requestURI.substring( requestURI.lastIndexOf('/') + 1 );
	} else if ( name.equalsIgnoreCase( "DOCUMENT_URI" ) ) {
	    retVal = req.getRequestURI();
	} else if ( name.equalsIgnoreCase( "GATEWAY_INTERFACE" ) ) {
	    retVal = "CGI/1.1";
	} else if ( name.equalsIgnoreCase( "PATH_INFO" ) ) {
	    retVal = req.getPathInfo();
	} else if ( name.equalsIgnoreCase( "PATH_TRANSLATED" ) ) {
	    retVal = req.getPathTranslated();
	} else if ( name.equalsIgnoreCase( "QUERY_STRING" ) ) {
	    //apache displays this as an empty string rather than (none)
	    retVal = nullToEmptyString( req.getQueryString() );
	} else if ( name.equalsIgnoreCase( "QUERY_STRING_UNESCAPED" ) ) {
	    String queryString = req.getQueryString();
	    if ( queryString != null ) {
		retVal = URLDecoder.decode( queryString );
	    }
	} else if ( name.equalsIgnoreCase( "REMOTE_ADDR" ) ) {
	    retVal = req.getRemoteAddr();
	} else if ( name.equalsIgnoreCase( "REMOTE_HOST" ) ) {
	    retVal = req.getRemoteHost();	   
	} else if ( name.equalsIgnoreCase( "REMOTE_USER" ) ) {
	    retVal = req.getRemoteUser();
	} else if ( name.equalsIgnoreCase( "REQUEST_METHOD" ) ) {
	    retVal = req.getMethod();
	} else if ( name.equalsIgnoreCase( "SCRIPT_NAME" ) ) {
	    retVal = req.getServletPath();
	} else if ( name.equalsIgnoreCase( "SERVER_NAME" ) ) {
	    retVal = req.getServerName();
	} else if ( name.equalsIgnoreCase( "SERVER_PORT" ) ) {
	    retVal = Integer.toString( req.getServerPort() );
	} else if ( name.equalsIgnoreCase( "SERVER_PROTOCOL" ) ) {
	    retVal = req.getProtocol();
	} else if ( name.equalsIgnoreCase( "SERVER_SOFTWARE" ) ) {
	    ServletContext servletContext = servlet.getServletContext();	   
	    retVal = servletContext.getServerInfo();
	}
	return retVal;
    }

    public Date getCurrentDate() {
	return new Date();
    }       

    protected String nullToEmptyString( String string ) {
	String retVal = string;

	if ( retVal == null ) {
	    retVal="";
	}
	return retVal;
    }


    protected String getPathWithoutFileName( String servletPath ) {
	String retVal = null;

	int lastSlash = servletPath.lastIndexOf('/');
	if ( lastSlash >= 0 ) {
	    //cut off file namee
	    retVal = servletPath.substring( 0, lastSlash + 1 );
	}
	return retVal;
    }

    protected String getPathWithoutContext( String servletPath ) {
	String retVal = null;

	int secondSlash = servletPath.indexOf('/', 1 );
	if ( secondSlash >= 0 ) {
	    //cut off context
	    retVal = servletPath.substring( secondSlash );
	}
	return retVal;
    }

    protected String getAbsolutePath( String path ) throws IOException {
	String pathWithoutContext = SSIServletRequestUtil.getRelativePath( req );
	String prefix = getPathWithoutFileName( pathWithoutContext );
	if ( prefix == null ) {
	    throw new IOException("Couldn't remove filename from path: " + pathWithoutContext );
	}
	String fullPath = prefix + path;
	String retVal = SSIServletRequestUtil.normalize( fullPath );

	if ( retVal == null ) {
	    throw new IOException("Normalization yielded null on path: " + fullPath );
	}
	return retVal;
    }

    protected ServletContextAndPath getServletContextAndPathFromNonVirtualPath( String nonVirtualPath ) throws IOException {
	if ( nonVirtualPath.startsWith("/") || nonVirtualPath.startsWith("\\") ) {
	    throw new IOException("A non-virtual path can't be absolute: " + nonVirtualPath );
	} 

	if ( nonVirtualPath.indexOf("../") >= 0 ) {
	    throw new IOException("A non-virtual path can't contain '../' : " + nonVirtualPath );
	}

	String path = getAbsolutePath( nonVirtualPath );

	ServletContext servletContext = servlet.getServletContext();
	ServletContextAndPath csAndP = new ServletContextAndPath( servletContext, path );
	return csAndP;
    }


    protected ServletContextAndPath getServletContextAndPathFromVirtualPath( String virtualPath ) throws IOException {
	ServletContext servletContext = servlet.getServletContext();
	String path = null;

	if ( !virtualPath.startsWith("/") &&  !virtualPath.startsWith("\\") ) {
	    path = getAbsolutePath( virtualPath );
	} else {
	    String normalized = SSIServletRequestUtil.normalize( virtualPath );
	    if ( isVirtualWebappRelative ) {
		path = normalized;
	    } else {
		servletContext = servletContext.getContext( normalized );
		if ( servletContext == null ) {
		    throw new IOException("Couldn't get context for path: " + normalized );
		}

		//If it's the root context, then there is no context element to remove, ie:
		// '/file1.shtml' vs '/appName1/file1.shtml'
		if ( !isRootContext( servletContext ) ) {
		    path = getPathWithoutContext( normalized );
		    if ( path == null ) {
			throw new IOException("Couldn't remove context from path: " + normalized );
		    }		    
		} else {
		    path = normalized;
		}
	    }
	}
	return new ServletContextAndPath( servletContext, path );
    }

    //Assumes servletContext is not-null
    //Assumes that identity comparison will be true for the same context
    //Assuming the above, getContext("/") will be non-null as long as the root context is accessible.
    //If it isn't, then servletContext can't be the root context anyway, hence they will not match.
    protected boolean isRootContext( ServletContext servletContext ) {
	return servletContext == servletContext.getContext( "/" );
    }

    protected ServletContextAndPath getServletContextAndPath( String originalPath, boolean virtual ) throws IOException {
	ServletContextAndPath csAndP = null;

	if ( debug > 0 ) {
	    log("SSIServletExternalResolver.getServletContextAndPath( " + originalPath + ", " + virtual + ")" , null);
	}
	if ( virtual ) {
	    csAndP = getServletContextAndPathFromVirtualPath( originalPath );
	} else {
	    csAndP = getServletContextAndPathFromNonVirtualPath( originalPath );
	}
	return csAndP;
    }

    protected URLConnection getURLConnection( String originalPath, boolean virtual ) throws IOException {
	ServletContextAndPath csAndP = getServletContextAndPath( originalPath, virtual );
	ServletContext context = csAndP.getServletContext();
	String path = csAndP.getPath();
	   
	URL url = context.getResource( path );
	if ( url == null ) {
	    throw new IOException("Context did not contain resource: " + path );
	}
	URLConnection urlConnection = url.openConnection();				    
	return urlConnection;
    }

    public long getFileLastModified( String path, boolean virtual ) throws IOException {
	long lastModified = 0;

	URLConnection urlConnection = getURLConnection( path, virtual );
	lastModified = urlConnection.getLastModified();
	return lastModified;		
    }

    public long getFileSize( String path, boolean virtual ) throws IOException {
	long fileSize = -1;

	URLConnection urlConnection = getURLConnection( path, virtual );
	fileSize = urlConnection.getContentLength();
	return fileSize;
    }

    //We are making lots of unnecessary copies of the included data here.  If someone ever complains that this
    //is slow, we should connect the included stream to the print writer that SSICommand uses.
    public String getFileText( String originalPath, boolean virtual ) throws IOException {
	try {
	    ServletContextAndPath csAndP = getServletContextAndPath( originalPath, virtual );
	    ServletContext context = csAndP.getServletContext();
	    String path = csAndP.getPath();

	    RequestDispatcher rd =
		context.getRequestDispatcher( path );
	    if ( rd == null ) {
		throw new IOException("Couldn't get request dispatcher for path: " + path );
	    }
	    ByteArrayServletOutputStream basos = new ByteArrayServletOutputStream();
	    ResponseIncludeWrapper responseIncludeWrapper =
		new ResponseIncludeWrapper(res, basos );
	    rd.include(req, responseIncludeWrapper );

	    //We can't assume the included servlet flushed its output
	    responseIncludeWrapper.flushOutputStreamOrWriter();
	    byte[] bytes = basos.toByteArray();

	    //Assume that the default encoding is what was used to encode the bytes. Questionable.
	    String retVal = new String( bytes );

	    //make an assumption that an empty response is a failure.  This is a problem if a truly empty file 
	    //were included, but not sure how else to tell.
	    if ( retVal.equals("") ) {
		throw new IOException("Couldn't find file: " + path );
	    }
	    return retVal;
	} catch (ServletException e) {
	    throw new IOException("Couldn't include file: " + originalPath + " because of ServletException: " + e.getMessage() );
	}
    }   

    protected class ServletContextAndPath {
	protected ServletContext servletContext;
	protected String path;

	public ServletContextAndPath( ServletContext servletContext, String path ) {
	    this.servletContext = servletContext;
	    this.path = path;
	}

	public ServletContext getServletContext() {
	    return servletContext;
	}

	public String getPath() {
	    return path;
	}
    }
}
