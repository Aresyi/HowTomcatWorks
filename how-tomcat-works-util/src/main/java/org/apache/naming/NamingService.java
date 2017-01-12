/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/naming/NamingService.java,v 1.2 2001/11/22 16:48:32 remm Exp $
 * $Revision: 1.2 $
 * $Date: 2001/11/22 16:48:32 $
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

package org.apache.naming;

import javax.naming.Context;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;

/**
 * Implementation of the NamingService JMX MBean.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.2 $
 */

public final class NamingService
    extends NotificationBroadcasterSupport
    implements NamingServiceMBean, MBeanRegistration {
    
    
    // ----------------------------------------------------- Instance Variables
    
    
    /**
     * Status of the Slide domain.
     */
    private int state = STOPPED;
    
    
    /**
     * Notification sequence number.
     */
    private long sequenceNumber = 0;
    
    
    /**
     * Old URL packages value.
     */
    private String oldUrlValue = "";
    
    
    /**
     * Old initial context value.
     */
    private String oldIcValue = "";
    
    
    // ---------------------------------------------- MBeanRegistration Methods
    
    
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        return new ObjectName(OBJECT_NAME);
    }
    
    
    public void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue())
            destroy();
    }
    
    
    public void preDeregister()
        throws Exception {
    }
    
    
    public void postDeregister() {
        destroy();
    }
    
    
    // ----------------------------------------------------- SlideMBean Methods
    
    
    /**
     * Retruns the Catalina component name.
     */
    public String getName() {
        return NAME;
    }
    
    
    /**
     * Returns the state.
     */
    public int getState() {
        return state;
    }
    
    
    /**
     * Returns a String representation of the state.
     */
    public String getStateString() {
        return states[state];
    }
    
    
    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception {
        
        Notification notification = null;
        
        if (state != STOPPED)
            return;
        
        state = STARTING;
        
        // Notifying the MBEan server that we're starting
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Starting " + NAME, "State", "java.lang.Integer", 
             new Integer(STOPPED), new Integer(STARTING));
        sendNotification(notification);
        
        try {
            
            String value = "org.apache.naming";
            String oldValue = System.getProperty(Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                oldUrlValue = oldValue;
                value = oldValue + ":" + value;
            }
            System.setProperty(Context.URL_PKG_PREFIXES, value);
            
            oldValue = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
            if (oldValue != null) {
                oldIcValue = oldValue;
            } else {
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                                   Constants.Package 
                                   + ".java.javaURLContextFactory");
            }
            
        } catch (Throwable t) {
            state = STOPPED;
            notification = new AttributeChangeNotification
                (this, sequenceNumber++, System.currentTimeMillis(), 
                 "Stopped " + NAME, "State", "java.lang.Integer", 
                 new Integer(STARTING), new Integer(STOPPED));
            sendNotification(notification);
        }
        
        state = STARTED;
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Started " + NAME, "State", "java.lang.Integer", 
             new Integer(STARTING), new Integer(STARTED));
        sendNotification(notification);
        
    }
    
    
    /**
     * Stop the servlet container.
     */
    public void stop() {
        
        Notification notification = null;
        
        if (state != STARTED)
            return;
        
        state = STOPPING;
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Stopping " + NAME, "State", "java.lang.Integer", 
             new Integer(STARTED), new Integer(STOPPING));
        sendNotification(notification);
        
        try {
            
            System.setProperty(Context.URL_PKG_PREFIXES, oldUrlValue);
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, oldIcValue);
            
        } catch (Throwable t) {
            
            // FIXME
            t.printStackTrace();
            
        }
        
        state = STOPPED;
        
        notification = new AttributeChangeNotification
            (this, sequenceNumber++, System.currentTimeMillis(), 
             "Stopped " + NAME, "State", "java.lang.Integer", 
             new Integer(STOPPING), new Integer(STOPPED));
        sendNotification(notification);
        
    }
    
    
    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy() {
        
        if (getState() != STOPPED)
            stop();
        
    }
    
    
}
