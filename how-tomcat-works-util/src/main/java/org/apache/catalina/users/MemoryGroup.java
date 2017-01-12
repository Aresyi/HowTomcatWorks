/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/MemoryGroup.java,v 1.5 2002/02/10 08:06:20 craigmcc Exp $
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
import org.apache.catalina.Role;
import org.apache.catalina.UserDatabase;


/**
 * <p>Concrete implementation of {@link Group} for the
 * {@link MemoryUserDatabase} implementation of {@link UserDatabase}.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2002/02/10 08:06:20 $
 * @since 4.1
 */

public class MemoryGroup extends AbstractGroup {


    // ----------------------------------------------------------- Constructors


    /**
     * Package-private constructor used by the factory method in
     * {@link MemoryUserDatabase}.
     *
     * @param database The {@link MemoryUserDatabase} that owns this group
     * @param groupname Group name of this group
     * @param description Description of this group
     */
    MemoryGroup(MemoryUserDatabase database,
                String groupname, String description) {

        super();
        this.database = database;
        setGroupname(groupname);
        setDescription(description);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link MemoryUserDatabase} that owns this group.
     */
    protected MemoryUserDatabase database = null;


    /**
     * The set of {@link Role}s associated with this group.
     */
    protected ArrayList roles = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Role}s assigned specifically to this group.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.iterator());
        }

    }


    /**
     * Return the {@link UserDatabase} within which this Group is defined.
     */
    public UserDatabase getUserDatabase() {

        return (this.database);

    }


    /**
     * Return the set of {@link User}s that are members of this group.
     */
    public Iterator getUsers() {

        ArrayList results = new ArrayList();
        Iterator users = database.getUsers();
        while (users.hasNext()) {
            MemoryUser user = (MemoryUser) users.next();
            if (user.isInGroup(this)) {
                results.add(user);
            }
        }
        return (results.iterator());

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Role} to those assigned specifically to this group.
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
     * Is this group specifically assigned the specified {@link Role}?
     *
     * @param role The role to check
     */
    public boolean isInRole(Role role) {

        synchronized (roles) {
            return (roles.contains(role));
        }

    }


    /**
     * Remove a {@link Role} from those assigned to this group.
     *
     * @param role The old role
     */
    public void removeRole(Role role) {

        synchronized (roles) {
            roles.remove(role);
        }

    }


    /**
     * Remove all {@link Role}s from those assigned to this group.
     */
    public void removeRoles() {

        synchronized (roles) {
            roles.clear();
        }

    }


    /**
     * <p>Return a String representation of this group in XML format.</p>
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<group groupname=\"");
        sb.append(groupname);
        sb.append("\"");
        if (description != null) {
            sb.append(" description=\"");
            sb.append(description);
            sb.append("\"");
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
                    sb.append((String) ((Role) values.next()).getRolename());
                }
                sb.append("\"");
            }
        }
        sb.append("/>");
        return (sb.toString());

    }


}
