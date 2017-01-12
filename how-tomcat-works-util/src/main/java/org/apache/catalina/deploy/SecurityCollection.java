/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/deploy/SecurityCollection.java,v 1.3 2001/07/22 20:25:10 pier Exp $
 * $Revision: 1.3 $
 * $Date: 2001/07/22 20:25:10 $
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


package org.apache.catalina.deploy;


import org.apache.catalina.util.RequestUtil;


/**
 * Representation of a web resource collection for a web application's security
 * constraint, as represented in a <code>&lt;web-resource-collection&gt;</code>
 * element in the deployment descriptor.
 * <p>
 * <b>WARNING</b>:  It is assumed that instances of this class will be created
 * and modified only within the context of a single thread, before the instance
 * is made visible to the remainder of the application.  After that, only read
 * access is expected.  Therefore, none of the read and write access within
 * this class is synchronized.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2001/07/22 20:25:10 $
 */

public final class SecurityCollection {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new security collection instance with default values.
     */
    public SecurityCollection() {

        this(null, null);

    }


    /**
     * Construct a new security collection instance with specified values.
     *
     * @param name Name of this security collection
     */
    public SecurityCollection(String name) {

        this(name, null);

    }


    /**
     * Construct a new security collection instance with specified values.
     *
     * @param name Name of this security collection
     * @param description Description of this security collection
     */
    public SecurityCollection(String name, String description) {

        super();
        setName(name);
        setDescription(description);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Description of this web resource collection.
     */
    private String description = null;


    /**
     * The HTTP methods covered by this web resource collection.
     */
    private String methods[] = new String[0];


    /**
     * The name of this web resource collection.
     */
    private String name = null;


    /**
     * The URL patterns protected by this security collection.
     */
    private String patterns[] = new String[0];


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this web resource collection.
     */
    public String getDescription() {

        return (this.description);

    }


    /**
     * Set the description of this web resource collection.
     *
     * @param description The new description
     */
    public void setDescription(String description) {

        this.description = description;

    }


    /**
     * Return the name of this web resource collection.
     */
    public String getName() {

        return (this.name);

    }


    /**
     * Set the name of this web resource collection
     *
     * @param name The new name
     */
    public void setName(String name) {

        this.name = name;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add an HTTP request method to be part of this web resource collection.
     */
    public void addMethod(String method) {

        if (method == null)
            return;
        String results[] = new String[methods.length + 1];
        for (int i = 0; i < methods.length; i++)
            results[i] = methods[i];
        results[methods.length] = method;
        methods = results;

    }


    /**
     * Add a URL pattern to be part of this web resource collection.
     */
    public void addPattern(String pattern) {

        if (pattern == null)
            return;
        pattern = RequestUtil.URLDecode(pattern);
        String results[] = new String[patterns.length + 1];
        for (int i = 0; i < patterns.length; i++)
            results[i] = patterns[i];
        results[patterns.length] = pattern;
        patterns = results;

    }


    /**
     * Return <code>true</code> if the specified HTTP request method is
     * part of this web resource collection.
     *
     * @param method Request method to check
     */
    public boolean findMethod(String method) {

        if (methods.length == 0)
            return (true);
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(method))
                return (true);
        }
        return (false);

    }


    /**
     * Return the set of HTTP request methods that are part of this web
     * resource collection, or a zero-length array if all request methods
     * are included.
     */
    public String[] findMethods() {

        return (methods);

    }


    /**
     * Is the specified pattern part of this web resource collection?
     *
     * @param pattern Pattern to be compared
     */
    public boolean findPattern(String pattern) {

        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].equals(pattern))
                return (true);
        }
        return (false);

    }


    /**
     * Return the set of URL patterns that are part of this web resource
     * collection.  If none have been specified, a zero-length array is
     * returned.
     */
    public String[] findPatterns() {

        return (patterns);

    }


    /**
     * Remove the specified HTTP request method from those that are part
     * of this web resource collection.
     *
     * @param method Request method to be removed
     */
    public void removeMethod(String method) {

        if (method == null)
            return;
        int n = -1;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(method)) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            int j = 0;
            String results[] = new String[methods.length - 1];
            for (int i = 0; i < methods.length; i++) {
                if (i != n)
                    results[j++] = methods[i];
            }
            methods = results;
        }

    }


    /**
     * Remove the specified URL pattern from those that are part of this
     * web resource collection.
     *
     * @param pattern Pattern to be removed
     */
    public void removePattern(String pattern) {

        if (pattern == null)
            return;
        int n = -1;
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].equals(pattern)) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            int j = 0;
            String results[] = new String[patterns.length - 1];
            for (int i = 0; i < patterns.length; i++) {
                if (i != n)
                    results[j++] = patterns[i];
            }
            patterns = results;
        }

    }


    /**
     * Return a String representation of this security collection.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SecurityCollection[");
        sb.append(name);
        if (description != null) {
            sb.append(", ");
            sb.append(description);
        }
        sb.append("]");
        return (sb.toString());

    }


}
