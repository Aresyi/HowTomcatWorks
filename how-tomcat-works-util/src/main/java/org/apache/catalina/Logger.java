/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Logger.java,v 1.3 2001/07/22 20:13:30 pier Exp $
 * $Revision: 1.3 $
 * $Date: 2001/07/22 20:13:30 $
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


package org.apache.catalina;


import java.beans.PropertyChangeListener;


/**
 * A <b>Logger</b> is a generic interface for the message and exception
 * logging methods of the ServletContext interface.  Loggers can be
 * attached at any Container level, but will typically only be attached
 * to a Context, or higher level, Container.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/07/22 20:13:30 $
 */

public interface Logger {


    // ----------------------------------------------------- Manifest Constants


    /**
     * Verbosity level constants for log messages that may be filtered
     * by the underlying logger.
     */

    public static final int FATAL = Integer.MIN_VALUE;

    public static final int ERROR = 1;

    public static final int WARNING = 2;

    public static final int INFORMATION = 3;

    public static final int DEBUG = 4;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer();


    /**
     * Set the Container with which this Logger has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container);


    /**
     * Return descriptive information about this Logger implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     */
    public int getVerbosity();


    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosity The new verbosity level
     */
    public void setVerbosity(int verbosity);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Writes the specified message to a servlet log file, usually an event
     * log.  The name and type of the servlet log is specific to the
     * servlet container.  This message will be logged unconditionally.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     */
    public void log(String message);


    /**
     * Writes the specified exception, and message, to a servlet log file.
     * The implementation of this method should call
     * <code>log(msg, exception)</code> instead.  This method is deprecated
     * in the ServletContext interface, but not deprecated here to avoid
     * many useless compiler warnings.  This message will be logged
     * unconditionally.
     *
     * @param exception An <code>Exception</code> to be reported
     * @param msg The associated message string
     */
    public void log(Exception exception, String msg);


    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file.  The name
     * and type of the servlet log file is specific to the servlet container,
     * usually an event log.  This message will be logged unconditionally.
     *
     * @param message A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     */
    public void log(String message, Throwable throwable);


    /**
     * Writes the specified message to the servlet log file, usually an event
     * log, if the logger is set to a verbosity level equal to or higher than
     * the specified value for this message.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, int verbosity);


    /**
     * Writes the specified message and exception to the servlet log file,
     * usually an event log, if the logger is set to a verbosity level equal
     * to or higher than the specified value for this message.
     *
     * @param message A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, Throwable throwable, int verbosity);


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);


}
