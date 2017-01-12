/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/ContextRuleSet.java,v 1.3 2001/11/08 21:03:15 remm Exp $
 * $Revision: 1.3 $
 * $Date: 2001/11/08 21:03:15 $
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

import java.lang.reflect.Constructor;
import org.apache.catalina.Container;
import org.apache.catalina.Loader;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;


/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Context or DefaultContext definition element.  To enable parsing of a
 * DefaultContext, be sure to specify a prefix that ends with "/Default".</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/11/08 21:03:15 $
 */

public class ContextRuleSet extends RuleSetBase {


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
    public ContextRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public ContextRuleSet(String prefix) {

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

        if (!isDefaultContext()) {
            digester.addObjectCreate(prefix + "Context",
                                     "org.apache.catalina.core.StandardContext",
                                     "className");
        } else {
            digester.addObjectCreate(prefix + "Context",
                                     "org.apache.catalina.core.StandardDefaultContext",
                                     "className");
        }
        digester.addSetProperties(prefix + "Context");
        if (!isDefaultContext()) {
            digester.addRule(prefix + "Context",
                             new CopyParentClassLoaderRule(digester));
            digester.addRule(prefix + "Context",
                             new LifecycleListenerRule
                                 (digester,
                                  "org.apache.catalina.startup.ContextConfig",
                                  "configClass"));
            digester.addSetNext(prefix + "Context",
                                "addChild",
                                "org.apache.catalina.Container");
        } else {
            digester.addSetNext(prefix + "Context",
                                "addDefaultContext",
                                "org.apache.catalina.DefaultContext");
        }

        digester.addCallMethod(prefix + "Context/InstanceListener",
                               "addInstanceListener", 0);

        digester.addObjectCreate(prefix + "Context/Listener",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Context/Listener");
        digester.addSetNext(prefix + "Context/Listener",
                            "addLifecycleListener",
                            "org.apache.catalina.LifecycleListener");

        digester.addRule(prefix + "Context/Loader",
                         new CreateLoaderRule
                             (digester,
                              "org.apache.catalina.loader.WebappLoader",
                              "className"));
        digester.addSetProperties(prefix + "Context/Loader");
        digester.addSetNext(prefix + "Context/Loader",
                            "setLoader",
                            "org.apache.catalina.Loader");

        digester.addObjectCreate(prefix + "Context/Logger",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Context/Logger");
        digester.addSetNext(prefix + "Context/Logger",
                            "setLogger",
                            "org.apache.catalina.Logger");

        digester.addObjectCreate(prefix + "Context/Manager",
                                 "org.apache.catalina.session.StandardManager",
                                 "className");
        digester.addSetProperties(prefix + "Context/Manager");
        digester.addSetNext(prefix + "Context/Manager",
                            "setManager",
                            "org.apache.catalina.Manager");

        digester.addObjectCreate(prefix + "Context/Manager/Store",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Context/Manager/Store");
        digester.addSetNext(prefix + "Context/Manager/Store",
                            "setStore",
                            "org.apache.catalina.Store");

        digester.addObjectCreate(prefix + "Context/Parameter",
                                 "org.apache.catalina.deploy.ApplicationParameter");
        digester.addSetProperties(prefix + "Context/Parameter");
        digester.addSetNext(prefix + "Context/Parameter",
                            "addApplicationParameter",
                            "org.apache.catalina.deploy.ApplicationParameter");

        digester.addObjectCreate(prefix + "Context/Realm",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Context/Realm");
        digester.addSetNext(prefix + "Context/Realm",
                            "setRealm",
                            "org.apache.catalina.Realm");

        digester.addObjectCreate(prefix + "Context/ResourceLink",
                                 "org.apache.catalina.deploy.ContextResourceLink");
        digester.addSetProperties(prefix + "Context/ResourceLink");
        digester.addSetNext(prefix + "Context/ResourceLink",
                            "addResourceLink",
                            "org.apache.catalina.deploy.ContextResourceLink");

        digester.addObjectCreate(prefix + "Context/Resources",
                                 "org.apache.naming.resources.FileDirContext",
                                 "className");
        digester.addSetProperties(prefix + "Context/Resources");
        digester.addSetNext(prefix + "Context/Resources",
                            "setResources",
                            "javax.naming.directory.DirContext");

        digester.addObjectCreate(prefix + "Context/Valve",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Context/Valve");
        digester.addSetNext(prefix + "Context/Valve",
                            "addValve",
                            "org.apache.catalina.Valve");

        digester.addCallMethod(prefix + "Context/WrapperLifecycle",
                               "addWrapperLifecycle", 0);

        digester.addCallMethod(prefix + "Context/WrapperListener",
                               "addWrapperListener", 0);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Are we processing a DefaultContext element?
     */
    protected boolean isDefaultContext() {

        return (prefix.endsWith("/Default"));

    }


}


// ----------------------------------------------------------- Private Classes


/**
 * Rule that creates a new <code>Loader</code> instance, with the parent
 * class loader associated with the top object on the stack (which must be
 * a <code>Container</code>), and pushes it on to the stack.
 */

final class CreateLoaderRule extends Rule {

    public CreateLoaderRule(Digester digester, String loaderClass,
                            String attributeName) {

        super(digester);
        this.loaderClass = loaderClass;
        this.attributeName = attributeName;

    }

    private String attributeName;

    private String loaderClass;

    public void begin(Attributes attributes) throws Exception {

        // Look up the required parent class loader
        Container container = (Container) digester.peek();
        ClassLoader parentClassLoader = container.getParentClassLoader();

        // Instantiate a new Loader implementation object
        String className = loaderClass;
        if (attributeName != null) {
            String value = attributes.getValue(attributeName);
            if (value != null)
                className = value;
        }
        Class clazz = Class.forName(className);
        Class types[] = { ClassLoader.class };
        Object args[] = { parentClassLoader };
        Constructor constructor = clazz.getDeclaredConstructor(types);
        Loader loader = (Loader) constructor.newInstance(args);

        // Push the new loader onto the stack
        digester.push(loader);
        if (digester.getDebug() >= 1)
            digester.log("new " + loader.getClass().getName());

    }

    public void end() throws Exception {

        Loader loader = (Loader) digester.pop();
        if (digester.getDebug() >= 1)
            digester.log("pop " + loader.getClass().getName());

    }


}
