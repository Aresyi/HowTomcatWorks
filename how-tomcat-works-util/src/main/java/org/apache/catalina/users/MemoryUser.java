/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/MemoryUser.java,v 1.5 2002/02/10 08:06:20 craigmcc Exp $
 * $Revision: 1.5 $
 * $Date: 2002/02/10 08:06:20 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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


package org.apache.catalina.users;


import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.UserDatabase;


/**
 * <p>Concrete implementation of {@link User} for the
 * {@link MemoryUserDatabase} implementation of {@link UserDatabase}.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/02/10 08:06:20 $
 * @since 4.1
 */

public class MemoryUser extends AbstractUser {


    // ----------------------------------------------------------- Constructors


    /**
     * Package-private constructor used by the factory method in
     * {@link MemoryUserDatabase}.
     *
     * @param database The {@link MemoryUserDatabase} that owns this user
     * @param username Logon username of the new user
     * @param password Logon password of the new user
     * @param fullName Full name of the new user
     */
    MemoryUser(MemoryUserDatabase database, String username,
               String password, String fullName) {

        super();
        this.database = database;
        setUsername(username);
        setPassword(password);
        setFullName(fullName);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link MemoryUserDatabase} that owns this user.
     */
    protected MemoryUserDatabase database = null;


    /**
     * The set of {@link Group}s that this user is a member of.
     */
    protected ArrayList groups = new ArrayList();


    /**
     * The set of {@link Role}s associated with this user.
     */
    protected ArrayList roles = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Group}s to which this user belongs.
     */
    public Iterator getGroups() {

        synchronized (groups) {
            return (groups.iterator());
        }

    }


    /**
     * Return the set of {@link Role}s assigned specifically to this user.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.iterator());
        }

    }


    /**
     * Return the {@link UserDatabase} within which this User is defined.
     */
    public UserDatabase getUserDatabase() {

        return (this.database);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Group} to those this user belongs to.
     *
     * @param group The new group
     */
    public void addGroup(Group group) {

        synchronized (groups) {
            if (!groups.contains(group)) {
                groups.add(group);
            }
        }

    }


    /**
     * Add a new {@link Role} to those assigned specifically to this user.
     *
     * @param role The new role
     */
    public void addRole(Role role) {

        synchronized (roles) {
            if (!roles.contains(role)) {
                roles.add(role);
            }
        }

    }


    /**
     * Is this user in the specified group?
     *
     * @param group The group to check
     */
    public boolean isInGroup(Group group) {

        synchronized (groups) {
            return (groups.contains(group));
        }

    }


    /**
     * Is this user specifically assigned the specified {@link Role}?  This
     * method does <strong>NOT</strong> check for roles inherited based on
     * {@link Group} membership.
     *
     * @param role The role to check
     */
    public boolean isInRole(Role role) {

        synchronized (roles) {
            return (roles.contains(role));
        }

    }


    /**
     * Remove a {@link Group} from those this user belongs to.
     *
     * @param group The old group
     */
    public void removeGroup(Group group) {

        synchronized (groups) {
            groups.remove(group);
        }

    }


    /**
     * Remove all {@link Group}s from those this user belongs to.
     */
    public void removeGroups() {

        synchronized (groups) {
            groups.clear();
        }

    }


    /**
     * Remove a {@link Role} from those assigned to this user.
     *
     * @param role The old role
     */
    public void removeRole(Role role) {

        synchronized (roles) {
            roles.remove(role);
        }

    }


    /**
     * Remove all {@link Role}s from those assigned to this user.
     */
    public void removeRoles() {

        synchronized (roles) {
            roles.clear();
        }

    }


    /**
     * <p>Return a String representation of this user in XML format.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - For backwards compatibility,
     * the reader that processes this entry will accept either
     * <code>username</code> or </code>name</code> for the username
     * property.</p>
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<user username=\"");
        sb.append(username);
        sb.append("\" password=\"");
        sb.append(password);
        sb.append("\"");
        if (fullName != null) {
            sb.append(" fullName=\"");
            sb.append(fullName);
            sb.append("\"");
        }
        synchronized (groups) {
            if (groups.size() > 0) {
                sb.append(" groups=\"");
                int n = 0;
                Iterator values = groups.iterator();
                while (values.hasNext()) {
                    if (n > 0) {
                        sb.append(',');
                    }
                    n++;
                    sb.append(((Group) values.next()).getGroupname());
                }
                sb.append("\"");
            }
        }
        synchronized (roles) {
            if (roles.size() > 0) {
                sb.append(" roles=\"");
                int n = 0;
                Iterator values = roles.iterator();
                while (values.hasNext()) {
                    if (n > 0) {
                        sb.append(',');
                    }
                    n++;
                    sb.append(((Role) values.next()).getRolename());
                }
                sb.append("\"");
            }
        }
        sb.append("/>");
        return (sb.toString());

    }


}
