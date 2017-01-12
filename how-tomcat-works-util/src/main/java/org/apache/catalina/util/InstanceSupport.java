/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/InstanceSupport.java,v 1.5 2001/10/11 23:30:58 craigmcc Exp $
 * $Revision: 1.5 $
 * $Date: 2001/10/11 23:30:58 $
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


import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Wrapper;


/**
 * Support class to assist in firing InstanceEvent notifications to
 * registered InstanceListeners.
 *
 * @author Craig R. McClanahan
 * @version $Id: InstanceSupport.java,v 1.5 2001/10/11 23:30:58 craigmcc Exp $
 */

public final class InstanceSupport {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new InstanceSupport object associated with the specified
     * Instance component.
     *
     * @param lifecycle The Instance component that will be the source
     *  of events that we fire
     */
    public InstanceSupport(Wrapper wrapper) {

        super();
        this.wrapper = wrapper;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of registered InstanceListeners for event notifications.
     */
    private InstanceListener listeners[] = new InstanceListener[0];


    /**
     * The source component for instance events that we will fire.
     */
    private Wrapper wrapper = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Wrapper with which we are associated.
     */
    public Wrapper getWrapper() {

        return (this.wrapper);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addInstanceListener(InstanceListener listener) {

      synchronized (listeners) {
          InstanceListener results[] =
            new InstanceListener[listeners.length + 1];
          for (int i = 0; i < listeners.length; i++)
              results[i] = listeners[i];
          results[listeners.length] = listener;
          listeners = results;
      }

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param filter The relevant Filter for this event
     */
    public void fireInstanceEvent(String type, Filter filter) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, filter, type);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param filter The relevant Filter for this event
     * @param exception Exception that occurred
     */
    public void fireInstanceEvent(String type, Filter filter,
                                  Throwable exception) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, filter, type,
                                                exception);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param filter The relevant Filter for this event
     * @param request The servlet request we are processing
     * @param response The servlet response we are processing
     */
    public void fireInstanceEvent(String type, Filter filter,
                                  ServletRequest request,
                                  ServletResponse response) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, filter, type,
                                                request, response);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param filter The relevant Filter for this event
     * @param request The servlet request we are processing
     * @param response The servlet response we are processing
     * @param exception Exception that occurred
     */
    public void fireInstanceEvent(String type, Filter filter,
                                  ServletRequest request,
                                  ServletResponse response,
                                  Throwable exception) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, filter, type,
                                                request, response, exception);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param servlet The relevant Servlet for this event
     */
    public void fireInstanceEvent(String type, Servlet servlet) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, servlet, type);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param servlet The relevant Servlet for this event
     * @param exception Exception that occurred
     */
    public void fireInstanceEvent(String type, Servlet servlet,
                                  Throwable exception) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, servlet, type,
                                                exception);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param servlet The relevant Servlet for this event
     * @param request The servlet request we are processing
     * @param response The servlet response we are processing
     */
    public void fireInstanceEvent(String type, Servlet servlet,
                                  ServletRequest request,
                                  ServletResponse response) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, servlet, type,
                                                request, response);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param servlet The relevant Servlet for this event
     * @param request The servlet request we are processing
     * @param response The servlet response we are processing
     * @param exception Exception that occurred
     */
    public void fireInstanceEvent(String type, Servlet servlet,
                                  ServletRequest request,
                                  ServletResponse response,
                                  Throwable exception) {

        if (listeners.length == 0)
            return;

        InstanceEvent event = new InstanceEvent(wrapper, servlet, type,
                                                request, response, exception);
        InstanceListener interested[] = null;
        synchronized (listeners) {
            interested = (InstanceListener[]) listeners.clone();
        }
        for (int i = 0; i < interested.length; i++)
            interested[i].instanceEvent(event);

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeInstanceListener(InstanceListener listener) {

        synchronized (listeners) {
            int n = -1;
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == listener) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;
            InstanceListener results[] =
              new InstanceListener[listeners.length - 1];
            int j = 0;
            for (int i = 0; i < listeners.length; i++) {
                if (i != n)
                    results[j++] = listeners[i];
            }
            listeners = results;
        }

    }


}
