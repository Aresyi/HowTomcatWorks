/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/LifecycleListenerRule.java,v 1.1 2001/10/17 00:44:02 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2001/10/17 00:44:02 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
 */


package org.apache.catalina.startup;


import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * <p>Rule that creates a new <code>LifecycleListener</code> instance,
 * and associates it with the top object on the stack (which must
 * implement <code>LifecycleListener</code>).</p>
 */

public class LifecycleListenerRule extends Rule {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this Rule.
     *
     * @param digester Digester we are associated with
     * @param listenerClass Default name of the LifecycleListener
     *  implementation class to be created
     * @param attributeName Name of the attribute that optionally
     *  includes an override name of the LifecycleListener class
     */
    public LifecycleListenerRule(Digester digester, String listenerClass,
                                 String attributeName) {

        super(digester);
        this.listenerClass = listenerClass;
        this.attributeName = attributeName;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The attribute name of an attribute that can override the
     * implementation class name.
     */
    private String attributeName;


    /**
     * The name of the <code>LifecycleListener</code> implementation class.
     */
    private String listenerClass;


    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(Attributes attributes) throws Exception {

        // Instantiate a new LifecyleListener implementation object
        String className = listenerClass;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null)
                className = value;
        }
        Class clazz = Class.forName(className);
        LifecycleListener listener =
            (LifecycleListener) clazz.newInstance();

        // Add this LifecycleListener to our associated component
        Lifecycle lifecycle = (Lifecycle) digester.peek();
        lifecycle.addLifecycleListener(listener);

    }


}
