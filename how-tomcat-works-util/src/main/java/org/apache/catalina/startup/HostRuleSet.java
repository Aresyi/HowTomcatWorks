/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/HostRuleSet.java,v 1.2 2001/11/11 19:58:35 remm Exp $
 * $Revision: 1.2 $
 * $Date: 2001/11/11 19:58:35 $
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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Host definition element.  This <code>RuleSet</code> does NOT include
 * any rules for nested Context or DefaultContext elements, which should
 * be added via instances of <code>ContextRuleSet</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2001/11/11 19:58:35 $
 */

public class HostRuleSet extends RuleSetBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public HostRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public HostRuleSet(String prefix) {

        super();
        this.namespaceURI = null;
        this.prefix = prefix;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate(prefix + "Host",
                                 "org.apache.catalina.core.StandardHost",
                                 "className");
        digester.addSetProperties(prefix + "Host");
        digester.addRule(prefix + "Host",
                         new CopyParentClassLoaderRule(digester));
        digester.addRule(prefix + "Host",
                         new LifecycleListenerRule
                         (digester,
                          "org.apache.catalina.startup.HostConfig",
                          "hostConfigClass"));
        digester.addSetNext(prefix + "Host",
                            "addChild",
                            "org.apache.catalina.Container");

        digester.addCallMethod(prefix + "Host/Alias",
                               "addAlias", 0);

        digester.addObjectCreate(prefix + "Host/Cluster",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Cluster");
        digester.addSetNext(prefix + "Host/Cluster",
                            "addCluster",
                            "org.apache.catalina.Cluster");

        digester.addObjectCreate(prefix + "Host/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Listener");
        digester.addSetNext(prefix + "Host/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addObjectCreate(prefix + "Host/Logger",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Logger");
        digester.addSetNext(prefix + "Host/Logger",
                            "setLogger",
                            "org.apache.catalina.Logger");

        digester.addObjectCreate(prefix + "Host/Realm",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Realm");
        digester.addSetNext(prefix + "Host/Realm",
                            "setRealm",
                            "org.apache.catalina.Realm");

        digester.addObjectCreate(prefix + "Host/Valve",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Host/Valve");
        digester.addSetNext(prefix + "Host/Valve",
                            "addValve",
                            "org.apache.catalina.Valve");

    }


}
