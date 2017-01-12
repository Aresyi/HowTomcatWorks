/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/mbeans/MemoryUserDatabaseMBean.java,v 1.4 2002/02/10 03:20:17 craigmcc Exp $
 * $Revision: 1.4 $
 * $Date: 2002/02/10 03:20:17 $
 *
 * ====================================================================
 *
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

package org.apache.catalina.mbeans;


import java.util.ArrayList;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.commons.modeler.BaseModelMBean;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.users.MemoryUserDatabase</code> component.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2002/02/10 03:20:17 $
 */

public class MemoryUserDatabaseMBean extends BaseModelMBean {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public MemoryUserDatabaseMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The configuration information registry for our managed beans.
     */
    protected Registry registry = MBeanUtils.createRegistry();


    /**
     * The <code>MBeanServer</code> in which we are registered.
     */
    protected MBeanServer mserver = MBeanUtils.createServer();


    /**
     * The <code>ManagedBean</code> information describing this MBean.
     */
    protected ManagedBean managed =
        registry.findManagedBean("MemoryUserDatabase");


    /**
     * The <code>ManagedBean</code> information describing Group MBeans.
     */
    protected ManagedBean managedGroup =
        registry.findManagedBean("Group");


    /**
     * The <code>ManagedBean</code> information describing Group MBeans.
     */
    protected ManagedBean managedRole =
        registry.findManagedBean("Role");


    /**
     * The <code>ManagedBean</code> information describing User MBeans.
     */
    protected ManagedBean managedUser =
        registry.findManagedBean("User");


    // ------------------------------------------------------------- Attributes


    /**
     * Return the MBean Names of all groups defined in this database.
     */
    public String[] getGroups() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator groups = database.getGroups();
        while (groups.hasNext()) {
            Group group = (Group) groups.next();
            results.add(findGroup(group.getGroupname()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all roles defined in this database.
     */
    public String[] getRoles() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator roles = database.getRoles();
        while (roles.hasNext()) {
            Role role = (Role) roles.next();
            results.add(findRole(role.getRolename()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all users defined in this database.
     */
    public String[] getUsers() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator users = database.getUsers();
        while (users.hasNext()) {
            User user = (User) users.next();
            results.add(findUser(user.getUsername()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    // ------------------------------------------------------------- Operations


    /**
     * Create a new Group and return the corresponding MBean Name.
     *
     * @param groupname Group name of the new group
     * @param description Description of the new group
     */
    public String createGroup(String groupname, String description) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.createGroup(groupname, description);
        /*
        if (roles != null) {
            for (int i = 0; i < roles.length; i++) {
                Role role = database.findRole(roles[i]);
                if (role == null) {
                    createRole(roles[i], null);
                    role = database.findRole(roles[i]);
                }
                group.addRole(role);
            }
        }
        */
        try {
            MBeanUtils.createMBean(group);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception creating group " +
                                               group + " MBean: " + e);
        }
        return (findGroup(groupname));

    }


    /**
     * Create a new Role and return the corresponding MBean Name.
     *
     * @param rolename Group name of the new group
     * @param description Description of the new group
     */
    public String createRole(String rolename, String description) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.createRole(rolename, description);
        try {
            MBeanUtils.createMBean(role);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception creating role " +
                                               role + " MBean: " + e);
        }
        return (findRole(rolename));

    }


    /**
     * Create a new User and return the corresponding MBean Name.
     *
     * @param username User name of the new user
     * @param password Password for the new user
     * @param fullName Full name for the new user
     */
    public String createUser(String username, String password,
                             String fullName) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.createUser(username, password, fullName);
        /*
        if (roles != null) {
            for (int i = 0; i < roles.length; i++) {
                Role role = database.findRole(roles[i]);
                if (role == null) {
                    createRole(roles[i], null);
                    role = database.findRole(roles[i]);
                }
                user.addRole(role);
            }
        }
        */
        try {
            MBeanUtils.createMBean(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception creating user " +
                                               user + " MBean: " + e);
        }
        return (findUser(username));

    }


    /**
     * Return the MBean Name for the specified group name (if any);
     * otherwise return <code>null</code>.
     *
     * @param groupname Group name to look up
     */
    public String findGroup(String groupname) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.findGroup(groupname);
        if (group == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedGroup.getDomain(), group);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException
                ("Cannot create object name for group " + group);
        }

    }


    /**
     * Return the MBean Name for the specified role name (if any);
     * otherwise return <code>null</code>.
     *
     * @param rolename Role name to look up
     */
    public String findRole(String rolename) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.findRole(rolename);
        if (role == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedRole.getDomain(), role);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException
                ("Cannot create object name for role " + role);
        }

    }


    /**
     * Return the MBean Name for the specified user name (if any);
     * otherwise return <code>null</code>.
     *
     * @param username User name to look up
     */
    public String findUser(String username) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.findUser(username);
        if (user == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedUser.getDomain(), user);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException
                ("Cannot create object name for user " + user);
        }

    }


    /**
     * Remove an existing group and destroy the corresponding MBean.
     *
     * @param groupname Group name to remove
     */
    public void removeGroup(String groupname) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.findGroup(groupname);
        if (group == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(group);
            database.removeGroup(group);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception destroying group " +
                                               group + " MBean: " + e);
        }

    }


    /**
     * Remove an existing role and destroy the corresponding MBean.
     *
     * @param rolename Role name to remove
     */
    public void removeRole(String rolename) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.findRole(rolename);
        if (role == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(role);
            database.removeRole(role);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception destroying role " +
                                               role + " MBean: " + e);
        }

    }


    /**
     * Remove an existing user and destroy the corresponding MBean.
     *
     * @param username User name to remove
     */
    public void removeUser(String username) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.findUser(username);
        if (user == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(user);
            database.removeUser(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception destroying user " +
                                               user + " MBean: " + e);
        }

    }


}
