/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/MemoryRuleSet.java,v 1.2 2002/02/08 16:19:57 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2002/02/08 16:19:57 $
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


package org.apache.catalina.realm;


import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.xml.sax.Attributes;


/**
 * <p><strong>RuleSet</strong> for recognizing the users defined in the
 * XML file processed by <code>MemoryRealm</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2002/02/08 16:19:57 $
 */

public class MemoryRuleSet extends RuleSetBase {


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
    public MemoryRuleSet() {

        this("tomcat-users/");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public MemoryRuleSet(String prefix) {

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

        digester.addRule
            (prefix + "user",
             new MemoryUserRule(digester));

    }


}


/**
 * Private class used when parsing the XML database file.
 */
final class MemoryUserRule extends Rule {


    /**
     * Construct a new instance of this <code>Rule</code>.
     *
     * @param digester The <code>Digester</code> we are associated with.
     */
    public MemoryUserRule(Digester digester) {

        super(digester);

    }


    /**
     * Process a <code>&lt;user&gt;</code> element from the XML database file.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(Attributes attributes) throws Exception {

        String username = attributes.getValue("name");
        if (username == null) {
            username = attributes.getValue("username");
        }
        String password = attributes.getValue("password");
        String roles = attributes.getValue("roles");

        MemoryRealm realm =
            (MemoryRealm) digester.peek(digester.getCount() - 1);
        realm.addUser(username, password, roles);

    }


}
