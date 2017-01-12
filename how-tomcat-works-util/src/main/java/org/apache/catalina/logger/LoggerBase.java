/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/logger/LoggerBase.java,v 1.5 2002/01/25 20:12:20 amyroh Exp $
 * $Revision: 1.5 $
 * $Date: 2002/01/25 20:12:20 $
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


package org.apache.catalina.logger;


import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Logger;


/**
 * Convenience base class for <b>Logger</b> implementations.  The only
 * method that must be implemented is <code>log(String msg)</code>, plus
 * any property setting and lifecycle methods required for configuration.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/01/25 20:12:20 $
 */

public abstract class LoggerBase
    implements Logger {


    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Logger has been associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    
    /**
     * The descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.logger.LoggerBase/1.0";


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The verbosity level for above which log messages may be filtered.
     */
    protected int verbosity = ERROR;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container with which this Logger has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return descriptive information about this Logger implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     */
    public int getVerbosity() {

        return (this.verbosity);

    }


    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosity The new verbosity level
     */
    public void setVerbosity(int verbosity) {

        this.verbosity = verbosity;

    }


    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosityLevel The new verbosity level, as a string
     */
    public void setVerbosityLevel(String verbosity) {

        if ("FATAL".equalsIgnoreCase(verbosity))
            this.verbosity = FATAL;
        else if ("ERROR".equalsIgnoreCase(verbosity))
            this.verbosity = ERROR;
        else if ("WARNING".equalsIgnoreCase(verbosity))
            this.verbosity = WARNING;
        else if ("INFORMATION".equalsIgnoreCase(verbosity))
            this.verbosity = INFORMATION;
        else if ("DEBUG".equalsIgnoreCase(verbosity))
            this.verbosity = DEBUG;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Writes the specified message to a servlet log file, usually an event
     * log.  The name and type of the servlet log is specific to the
     * servlet container.  This message will be logged unconditionally.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     */
    public abstract void log(String msg);


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
    public void log(Exception exception, String msg) {

        log(msg, exception);

    }


    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file.  The name
     * and type of the servlet log file is specific to the servlet container,
     * usually an event log.  This message will be logged unconditionally.
     *
     * @param msg A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     */
    public void log(String msg, Throwable throwable) {

        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(msg);
        throwable.printStackTrace(writer);
        Throwable rootCause = null;
        if (throwable instanceof LifecycleException)
            rootCause = ((LifecycleException) throwable).getThrowable();
        else if (throwable instanceof ServletException)
            rootCause = ((ServletException) throwable).getRootCause();
        if (rootCause != null) {
            writer.println("----- Root Cause -----");
            rootCause.printStackTrace(writer);
        }
        log(buf.toString());

    }


    /**
     * Writes the specified message to the servlet log file, usually an event
     * log, if the logger is set to a verbosity level equal to or higher than
     * the specified value for this message.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, int verbosity) {

        if (this.verbosity >= verbosity)
            log(message);

    }


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
    public void log(String message, Throwable throwable, int verbosity) {

        if (this.verbosity >= verbosity)
            log(message, throwable);

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


}
