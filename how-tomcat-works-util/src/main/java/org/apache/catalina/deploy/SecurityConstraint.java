/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/deploy/SecurityConstraint.java,v 1.5 2001/07/22 20:25:10 pier Exp $
 * $Revision: 1.5 $
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


/**
 * Representation of a security constraint element for a web application,
 * as represented in a <code>&lt;security-constraint&gt;</code> element in the
 * deployment descriptor.
 * <p>
 * <b>WARNING</b>:  It is assumed that instances of this class will be created
 * and modified only within the context of a single thread, before the instance
 * is made visible to the remainder of the application.  After that, only read
 * access is expected.  Therefore, none of the read and write access within
 * this class is synchronized.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/07/22 20:25:10 $
 */

public final class SecurityConstraint {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new security constraint instance with default values.
     */
    public SecurityConstraint() {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Was the "all roles" wildcard included in the authorization constraints
     * for this security constraint?
     */
    private boolean allRoles = false;


    /**
     * Was an authorization constraint included in this security constraint?
     * This is necessary to distinguish the case where an auth-constraint with
     * no roles (signifying no direct access at all) was requested, versus
     * a lack of auth-constraint which implies no access control checking.
     */
    private boolean authConstraint = false;


    /**
     * The set of roles permitted to access resources protected by this
     * security constraint.
     */
    private String authRoles[] = new String[0];


    /**
     * The set of web resource collections protected by this security
     * constraint.
     */
    private SecurityCollection collections[] = new SecurityCollection[0];


    /**
     * The display name of this security constraint.
     */
    private String displayName = null;


    /**
     * The user data constraint for this security constraint.  Must be NONE,
     * INTEGRAL, or CONFIDENTIAL.
     */
    private String userConstraint = "NONE";


    // ------------------------------------------------------------- Properties


    /**
     * Was the "all roles" wildcard included in this authentication
     * constraint?
     */
    public boolean getAllRoles() {

        return (this.allRoles);

    }


    /**
     * Return the authorization constraint present flag for this security
     * constraint.
     */
    public boolean getAuthConstraint() {

        return (this.authConstraint);

    }


    /**
     * Set the authorization constraint present flag for this security
     * constraint.
     */
    public void setAuthConstraint(boolean authConstraint) {

        this.authConstraint = authConstraint;

    }


    /**
     * Return the display name of this security constraint.
     */
    public String getDisplayName() {

        return (this.displayName);

    }


    /**
     * Set the display name of this security constraint.
     */
    public void setDisplayName(String displayName) {

        this.displayName = displayName;

    }


    /**
     * Return the user data constraint for this security constraint.
     */
    public String getUserConstraint() {

        return (userConstraint);

    }


    /**
     * Set the user data constraint for this security constraint.
     *
     * @param userConstraint The new user data constraint
     */
    public void setUserConstraint(String userConstraint) {

        if (userConstraint != null)
            this.userConstraint = userConstraint;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add an authorization role, which is a role name that will be
     * permitted access to the resources protected by this security constraint.
     *
     * @param authRole Role name to be added
     */
    public void addAuthRole(String authRole) {

        if (authRole == null)
            return;
        if ("*".equals(authRole)) {
            allRoles = true;
            return;
        }
        String results[] = new String[authRoles.length + 1];
        for (int i = 0; i < authRoles.length; i++)
            results[i] = authRoles[i];
        results[authRoles.length] = authRole;
        authRoles = results;
        authConstraint = true;

    }


    /**
     * Add a new web resource collection to those protected by this
     * security constraint.
     *
     * @param collection The new web resource collection
     */
    public void addCollection(SecurityCollection collection) {

        if (collection == null)
            return;
        SecurityCollection results[] =
            new SecurityCollection[collections.length + 1];
        for (int i = 0; i < collections.length; i++)
            results[i] = collections[i];
        results[collections.length] = collection;
        collections = results;

    }


    /**
     * Return <code>true</code> if the specified role is permitted access to
     * the resources protected by this security constraint.
     *
     * @param role Role name to be checked
     */
    public boolean findAuthRole(String role) {

        if (role == null)
            return (false);
        for (int i = 0; i < authRoles.length; i++) {
            if (role.equals(authRoles[i]))
                return (true);
        }
        return (false);

    }


    /**
     * Return the set of roles that are permitted access to the resources
     * protected by this security constraint.  If none have been defined,
     * a zero-length array is returned (which implies that all authenticated
     * users are permitted access).
     */
    public String[] findAuthRoles() {

        return (authRoles);

    }


    /**
     * Return the web resource collection for the specified name, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Web resource collection name to return
     */
    public SecurityCollection findCollection(String name) {

        if (name == null)
            return (null);
        for (int i = 0; i < collections.length; i++) {
            if (name.equals(collections[i].getName()))
                return (collections[i]);
        }
        return (null);

    }


    /**
     * Return all of the web resource collections protected by this
     * security constraint.  If there are none, a zero-length array is
     * returned.
     */
    public SecurityCollection[] findCollections() {

        return (collections);

    }


    /**
     * Return <code>true</code> if the specified context-relative URI (and
     * associated HTTP method) are protected by this security constraint.
     *
     * @param uri Context-relative URI to check
     * @param method Request method being used
     */
    public boolean included(String uri, String method) {

        // We cannot match without a valid request method
        if (method == null)
            return (false);

        // Check all of the collections included in this constraint
        for (int i = 0; i < collections.length; i++) {
            if (!collections[i].findMethod(method))
                continue;
            String patterns[] = collections[i].findPatterns();
            for (int j = 0; j < patterns.length; j++) {
                if (matchPattern(uri, patterns[j]))
                    return (true);
            }
        }

        // No collection included in this constraint matches this request
        return (false);

    }


    /**
     * Remove the specified role from the set of roles permitted to access
     * the resources protected by this security constraint.
     *
     * @param authRole Role name to be removed
     */
    public void removeAuthRole(String authRole) {

        if (authRole == null)
            return;
        int n = -1;
        for (int i = 0; i < authRoles.length; i++) {
            if (authRoles[i].equals(authRole)) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            int j = 0;
            String results[] = new String[authRoles.length - 1];
            for (int i = 0; i < authRoles.length; i++) {
                if (i != n)
                    results[j++] = authRoles[i];
            }
            authRoles = results;
        }

    }


    /**
     * Remove the specified web resource collection from those protected by
     * this security constraint.
     *
     * @param collection Web resource collection to be removed
     */
    public void removeCollection(SecurityCollection collection) {

        if (collection == null)
            return;
        int n = -1;
        for (int i = 0; i < collections.length; i++) {
            if (collections[i].equals(collection)) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            int j = 0;
            SecurityCollection results[] =
                new SecurityCollection[collections.length - 1];
            for (int i = 0; i < collections.length; i++) {
                if (i != n)
                    results[j++] = collections[i];
            }
            collections = results;
        }

    }


    /**
     * Return a String representation of this security constraint.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SecurityConstraint[");
        for (int i = 0; i < collections.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(collections[i].getName());
        }
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Does the specified request path match the specified URL pattern?
     * This method follows the same rules (in the same order) as those used
     * for mapping requests to servlets.
     *
     * @param path Context-relative request path to be checked
     *  (must start with '/')
     * @param pattern URL pattern to be compared against
     */
    private boolean matchPattern(String path, String pattern) {

        // Normalize the argument strings
        if ((path == null) || (path.length() == 0))
            path = "/";
        if ((pattern == null) || (pattern.length() == 0))
            pattern = "/";

        // Check for exact match
        if (path.equals(pattern))
            return (true);

        // Check for path prefix matching
        if (pattern.startsWith("/") && pattern.endsWith("/*")) {
            pattern = pattern.substring(0, pattern.length() - 2);
            if (pattern.length() == 0)
                return (true);  // "/*" is the same as "/"
            if (path.endsWith("/"))
                path = path.substring(0, path.length() - 1);
            while (true) {
                if (pattern.equals(path))
                    return (true);
                int slash = path.lastIndexOf('/');
                if (slash <= 0)
                    break;
                path = path.substring(0, slash);
            }
            return (false);
        }

        // Check for suffix matching
        if (pattern.startsWith("*.")) {
            int slash = path.lastIndexOf('/');
            int period = path.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) &&
                path.endsWith(pattern.substring(1))) {
                return (true);
            }
            return (false);
        }

        // Check for universal mapping
        if (pattern.equals("/"))
            return (true);

        return (false);

    }


}
