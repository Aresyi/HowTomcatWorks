/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/realm/GenericPrincipal.java,v 1.4 2001/10/27 20:29:03 craigmcc Exp $
 * $Revision: 1.4 $
 * $Date: 2001/10/27 20:29:03 $
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


package org.apache.catalina.realm;


import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import org.apache.catalina.Realm;


/**
 * Generic implementation of <strong>java.security.Principal</strong> that
 * is available for use by <code>Realm</code> implementations.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2001/10/27 20:29:03 $
 */

public class GenericPrincipal implements Principal {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new Principal, associated with the specified Realm, for the
     * specified username and password.
     *
     * @param realm The Realm that owns this Principal
     * @param name The username of the user represented by this Principal
     * @param password Credentials used to authenticate this user
     */
    public GenericPrincipal(Realm realm, String name, String password) {

        this(realm, name, password, null);

    }


    /**
     * Construct a new Principal, associated with the specified Realm, for the
     * specified username and password, with the specified role names
     * (as Strings).
     *
     * @param realm The Realm that owns this principal
     * @param name The username of the user represented by this Principal
     * @param password Credentials used to authenticate this user
     * @param roles List of roles (must be Strings) possessed by this user
     */
    public GenericPrincipal(Realm realm, String name, String password,
                            List roles) {

        super();
        this.realm = realm;
        this.name = name;
        this.password = password;
        if (roles != null) {
            this.roles = new String[roles.size()];
            this.roles = (String[]) roles.toArray(this.roles);
            if (this.roles.length > 0)
                Arrays.sort(this.roles);
        }

    }


    // ------------------------------------------------------------- Properties


    /**
     * The username of the user represented by this Principal.
     */
    protected String name = null;

    public String getName() {
        return (this.name);
    }


    /**
     * The authentication credentials for the user represented by
     * this Principal.
     */
    protected String password = null;

    public String getPassword() {
        return (this.password);
    }


    /**
     * The Realm with which this Principal is associated.
     */
    protected Realm realm = null;

    public Realm getRealm() {
        return (this.realm);
    }


    /**
     * The set of roles associated with this user.
     */
    protected String roles[] = new String[0];

    public String[] getRoles() {
        return (this.roles);
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Does the user represented by this Principal possess the specified role?
     *
     * @param role Role to be tested
     */
    public boolean hasRole(String role) {

        if (role == null)
            return (false);
        return (Arrays.binarySearch(roles, role) >= 0);

    }


    /**
     * Return a String representation of this object, which exposes only
     * information that should be public.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("GenericPrincipal[");
        sb.append(this.name);
        sb.append("]");
        return (sb.toString());

    }


}
