/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/AbstractGroup.java,v 1.4 2002/02/10 08:06:20 craigmcc Exp $
 * $Revision: 1.4 $
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


import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.UserDatabase;


/**
 * <p>Convenience base class for {@link Group} implementations.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/02/10 08:06:20 $
 * @since 4.1
 */

public abstract class AbstractGroup implements Group {


    // ----------------------------------------------------- Instance Variables


    /**
     * The description of this group.
     */
    protected String description = null;


    /**
     * The group name of this group.
     */
    protected String groupname = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this group.
     */
    public String getDescription() {

        return (this.description);

    }


    /**
     * Set the description of this group.
     *
     * @param description The new description
     */
    public void setDescription(String description) {

        this.description = description;

    }


    /**
     * Return the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getGroupname() {

        return (this.groupname);

    }


    /**
     * Set the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param groupname The new group name
     */
    public void setGroupname(String groupname) {

        this.groupname = groupname;

    }


    /**
     * Return the set of {@link Role}s assigned specifically to this group.
     */
    public abstract Iterator getRoles();


    /**
     * Return the {@link UserDatabase} within which this Group is defined.
     */
    public abstract UserDatabase getUserDatabase();


    /**
     * Return the set of {@link User}s that are members of this group.
     */
    public abstract Iterator getUsers();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Role} to those assigned specifically to this group.
     *
     * @param role The new role
     */
    public abstract void addRole(Role role);


    /**
     * Is this group specifically assigned the specified {@link Role}?
     *
     * @param role The role to check
     */
    public abstract boolean isInRole(Role role);


    /**
     * Remove a {@link Role} from those assigned to this group.
     *
     * @param role The old role
     */
    public abstract void removeRole(Role role);


    /**
     * Remove all {@link Role}s from those assigned to this group.
     */
    public abstract void removeRoles();


    // ------------------------------------------------------ Principal Methods


    /**
     * Make the principal name the same as the group name.
     */
    public String getName() {

        return (getGroupname());

    }


}
