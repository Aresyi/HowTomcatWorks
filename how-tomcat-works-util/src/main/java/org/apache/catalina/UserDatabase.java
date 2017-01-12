/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/UserDatabase.java,v 1.3 2002/02/03 00:56:57 craigmcc Exp $
 * $Revision: 1.3 $
 * $Date: 2002/02/03 00:56:57 $
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


package org.apache.catalina;


import java.util.Iterator;


/**
 * <p>Abstract representation of a database of {@link User}s and
 * {@link Group}s that can be maintained by an application,
 * along with definitions of corresponding {@link Role}s, and
 * referenced by a {@link Realm} for authentication and access control.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2002/02/03 00:56:57 $
 * @since 4.1
 */

public interface UserDatabase {


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Group}s defined in this user database.
     */
    public Iterator getGroups();


    /**
     * Return the unique global identifier of this user database.
     */
    public String getId();


    /**
     * Return the set of {@link Role}s defined in this user database.
     */
    public Iterator getRoles();


    /**
     * Return the set of {@link User}s defined in this user database.
     */
    public Iterator getUsers();


    // --------------------------------------------------------- Public Methods


    /**
     * Finalize access to this user database.
     *
     * @exception Exception if any exception is thrown during closing
     */
    public void close() throws Exception;


    /**
     * Create and return a new {@link Group} defined in this user database.
     *
     * @param groupname The group name of the new group (must be unique)
     * @param description The description of this group
     */
    public Group createGroup(String groupname, String description);


    /**
     * Create and return a new {@link Role} defined in this user database.
     *
     * @param rolename The role name of the new role (must be unique)
     * @param description The description of this role
     */
    public Role createRole(String rolename, String description);


    /**
     * Create and return a new {@link User} defined in this user database.
     *
     * @param username The logon username of the new user (must be unique)
     * @param password The logon password of the new user
     * @param fullName The full name of the new user
     */
    public User createUser(String username, String password,
                           String fullName);


    /**
     * Return the {@link Group} with the specified group name, if any;
     * otherwise return <code>null</code>.
     *
     * @param groupname Name of the group to return
     */
    public Group findGroup(String groupname);


    /**
     * Return the {@link Role} with the specified role name, if any;
     * otherwise return <code>null</code>.
     *
     * @param rolename Name of the role to return
     */
    public Role findRole(String rolename);


    /**
     * Return the {@link User} with the specified user name, if any;
     * otherwise return <code>null</code>.
     *
     * @param username Name of the user to return
     */
    public User findUser(String username);


    /**
     * Initialize access to this user database.
     *
     * @exception Exception if any exception is thrown during opening
     */
    public void open() throws Exception;


    /**
     * Remove the specified {@link Group} from this user database.
     *
     * @param group The group to be removed
     */
    public void removeGroup(Group group);


    /**
     * Remove the specified {@link Role} from this user database.
     *
     * @param role The role to be removed
     */
    public void removeRole(Role role);


    /**
     * Remove the specified {@link User} from this user database.
     *
     * @param user The user to be removed
     */
    public void removeUser(User user);


    /**
     * Save any updated information to the persistent storage location for
     * this user database.
     *
     * @exception Exception if any exception is thrown during saving
     */
    public void save() throws Exception;


}
