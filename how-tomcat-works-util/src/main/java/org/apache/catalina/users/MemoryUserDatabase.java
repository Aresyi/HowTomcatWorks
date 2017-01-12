/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/MemoryUserDatabase.java,v 1.7 2002/08/13 08:08:17 amyroh Exp $
 * $Revision: 1.7 $
 * $Date: 2002/08/13 08:08:17 $
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.util.StringManager;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;


/**
 * <p>Concrete implementation of {@link UserDatabase} that loads all
 * defined users, groups, and roles into an in-memory data structure,
 * and uses a specified XML file for its persistent storage.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.7 $ $Date: 2002/08/13 08:08:17 $
 * @since 4.1
 */

public class MemoryUserDatabase implements UserDatabase {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new instance with default values.
     */
    public MemoryUserDatabase() {

        super();

    }


    /**
     * Create a new instance with the specified values.
     *
     * @param id Unique global identifier of this user database
     */
    public MemoryUserDatabase(String id) {

        super();
        this.id = id;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The set of {@link Group}s defined in this database, keyed by
     * group name.
     */
    protected HashMap groups = new HashMap();


    /**
     * The unique global identifier of this user database.
     */
    protected String id = null;


    /**
     * The relative (to <code>catalina.base</code>) or absolute pathname to
     * the XML file in which we will save our persistent information.
     */
    protected String pathname = "conf/tomcat-users.xml";


    /**
     * The relative or absolute pathname to the file in which our old
     * information is stored while renaming is in progress.
     */
    protected String pathnameOld = pathname + ".old";


    /**
     * The relative or absolute pathname ot the file in which we write
     * our new information prior to renaming.
     */
    protected String pathnameNew = pathname + ".new";


    /**
     * The set of {@link Role}s defined in this database, keyed by
     * role name.
     */
    protected HashMap roles = new HashMap();


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The set of {@link User}s defined in this database, keyed by
     * user name.
     */
    protected HashMap users = new HashMap();


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Group}s defined in this user database.
     */
    public Iterator getGroups() {

        synchronized (groups) {
            return (groups.values().iterator());
        }

    }


    /**
     * Return the unique global identifier of this user database.
     */
    public String getId() {

        return (this.id);

    }


    /**
     * Return the relative or absolute pathname to the persistent storage file.
     */
    public String getPathname() {

        return (this.pathname);

    }


    /**
     * Set the relative or absolute pathname to the persistent storage file.
     *
     * @param pathname The new pathname
     */
    public void setPathname(String pathname) {

        this.pathname = pathname;
        this.pathnameOld = pathname + ".old";
        this.pathnameNew = pathname + ".new";

    }


    /**
     * Return the set of {@link Role}s defined in this user database.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.values().iterator());
        }

    }


    /**
     * Return the set of {@link User}s defined in this user database.
     */
    public Iterator getUsers() {

        synchronized (users) {
            return (users.values().iterator());
        }

    }



    // --------------------------------------------------------- Public Methods


    /**
     * Finalize access to this user database.
     *
     * @exception Exception if any exception is thrown during closing
     */
    public void close() throws Exception {

        save();

        synchronized (groups) {
            synchronized (users) {
                users.clear();
                groups.clear();
            }
        }

    }


    /**
     * Create and return a new {@link Group} defined in this user database.
     *
     * @param groupname The group name of the new group (must be unique)
     * @param description The description of this group
     */
    public Group createGroup(String groupname, String description) {

        MemoryGroup group = new MemoryGroup(this, groupname, description);
        synchronized (groups) {
            groups.put(group.getGroupname(), group);
        }
        return (group);

    }


    /**
     * Create and return a new {@link Role} defined in this user database.
     *
     * @param rolename The role name of the new group (must be unique)
     * @param description The description of this group
     */
    public Role createRole(String rolename, String description) {

        MemoryRole role = new MemoryRole(this, rolename, description);
        synchronized (roles) {
            roles.put(role.getRolename(), role);
        }
        return (role);

    }


    /**
     * Create and return a new {@link User} defined in this user database.
     *
     * @param username The logon username of the new user (must be unique)
     * @param password The logon password of the new user
     * @param fullName The full name of the new user
     */
    public User createUser(String username, String password,
                           String fullName) {

        MemoryUser user = new MemoryUser(this, username, password, fullName);
        synchronized (users) {
            users.put(user.getUsername(), user);
        }
        return (user);

    }


    /**
     * Return the {@link Group} with the specified group name, if any;
     * otherwise return <code>null</code>.
     *
     * @param groupname Name of the group to return
     */
    public Group findGroup(String groupname) {

        synchronized (groups) {
            return ((Group) groups.get(groupname));
        }

    }


    /**
     * Return the {@link Role} with the specified role name, if any;
     * otherwise return <code>null</code>.
     *
     * @param rolename Name of the role to return
     */
    public Role findRole(String rolename) {

        synchronized (roles) {
            return ((Role) roles.get(rolename));
        }

    }


    /**
     * Return the {@link User} with the specified user name, if any;
     * otherwise return <code>null</code>.
     *
     * @param username Name of the user to return
     */
    public User findUser(String username) {

        synchronized (users) {
            return ((User) users.get(username));
        }

    }


    /**
     * Initialize access to this user database.
     *
     * @exception Exception if any exception is thrown during opening
     */
    public void open() throws Exception {

        synchronized (groups) {
            synchronized (users) {

                // Erase any previous groups and users
                users.clear();
                groups.clear();
                roles.clear();

                // Construct a reader for the XML input file (if it exists)
                File file = new File(pathname);
                if (!file.isAbsolute()) {
                    file = new File(System.getProperty("catalina.base"),
                                    pathname);
                }
                if (!file.exists()) {
                    return;
                }
                FileInputStream fis = new FileInputStream(file);

                // Construct a digester to read the XML input file
                Digester digester = new Digester();
                digester.addFactoryCreate
                    ("tomcat-users/group",
                     new MemoryGroupCreationFactory(this));
                digester.addFactoryCreate
                    ("tomcat-users/role",
                     new MemoryRoleCreationFactory(this));
                digester.addFactoryCreate
                    ("tomcat-users/user",
                     new MemoryUserCreationFactory(this));

                // Parse the XML input file to load this database
                try {
                    digester.parse(fis);
                    fis.close();
                } catch (Exception e) {
                    try {
                        fis.close();
                    } catch (Throwable t) {
                        ;
                    }
                    throw e;
                }

            }
        }

    }


    /**
     * Remove the specified {@link Group} from this user database.
     *
     * @param group The group to be removed
     */
    public void removeGroup(Group group) {

        synchronized (groups) {
            Iterator users = getUsers();
            while (users.hasNext()) {
                User user = (User) users.next();
                user.removeGroup(group);
            }
            groups.remove(group.getGroupname());
        }

    }


    /**
     * Remove the specified {@link Role} from this user database.
     *
     * @param role The role to be removed
     */
    public void removeRole(Role role) {

        synchronized (roles) {
            Iterator groups = getGroups();
            while (groups.hasNext()) {
                Group group = (Group) groups.next();
                group.removeRole(role);
            }
            Iterator users = getUsers();
            while (users.hasNext()) {
                User user = (User) users.next();
                user.removeRole(role);
            }
            roles.remove(role.getRolename());
        }

    }


    /**
     * Remove the specified {@link User} from this user database.
     *
     * @param user The user to be removed
     */
    public void removeUser(User user) {

        synchronized (users) {
            users.remove(user.getUsername());
        }

    }


    /**
     * Save any updated information to the persistent storage location for
     * this user database.
     *
     * @exception Exception if any exception is thrown during saving
     */
    public void save() throws Exception {

        // Write out contents to a temporary file
        File fileNew = new File(pathnameNew);
        if (!fileNew.isAbsolute()) {
            fileNew =
                new File(System.getProperty("catalina.base"), pathnameNew);
        }
        PrintWriter writer = null;
        try {

            // Configure our PrintWriter
            FileOutputStream fos = new FileOutputStream(fileNew);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
            writer = new PrintWriter(osw);

            // Print the file prolog
            writer.println("<?xml version='1.0' encoding='utf-8'?>");
            writer.println("<tomcat-users>");

            // Print entries for each defined role, group, and user
            Iterator values = null;
            values = getRoles();
            while (values.hasNext()) {
                writer.print("  ");
                writer.println(values.next());
            }
            values = getGroups();
            while (values.hasNext()) {
                writer.print("  ");
                writer.println(values.next());
            }
            values = getUsers();
            while (values.hasNext()) {
                writer.print("  ");
                writer.println(values.next());
            }

            // Print the file epilog
            writer.println("</tomcat-users>");

            // Check for errors that occurred while printing
            if (writer.checkError()) {
                writer.close();
                fileNew.delete();
                throw new IOException
                    (sm.getString("memoryUserDatabase.writeException",
                                  fileNew.getAbsolutePath()));
            }
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                writer.close();
            }
            fileNew.delete();
            throw e;
        }

        // Perform the required renames to permanently save this file
        File fileOld = new File(pathnameNew);
        if (!fileOld.isAbsolute()) {
            fileOld =
                new File(System.getProperty("catalina.base"), pathnameOld);
        }
        fileOld.delete();
        File fileOrig = new File(pathname);
        if (!fileOrig.isAbsolute()) {
            fileOrig =
                new File(System.getProperty("catalina.base"), pathname);
        }
        if (fileOrig.exists()) {
            fileOld.delete();
            if (!fileOrig.renameTo(fileOld)) {
                throw new IOException
                    (sm.getString("memoryUserDatabase.renameOld",
                                  fileOld.getAbsolutePath()));
            }
        }
        if (!fileNew.renameTo(fileOrig)) {
            if (fileOld.exists()) {
                fileOld.renameTo(fileOrig);
            }
            throw new IOException
                (sm.getString("memoryUserDatabase.renameNew",
                              fileOrig.getAbsolutePath()));
        }
        fileOld.delete();

    }


    /**
     * Return a String representation of this UserDatabase.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("MemoryUserDatabase[id=");
        sb.append(this.id);
        sb.append(",pathname=");
        sb.append(pathname);
        sb.append(",groupCount=");
        sb.append(this.groups.size());
        sb.append(",roleCount=");
        sb.append(this.roles.size());
        sb.append(",userCount=");
        sb.append(this.users.size());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the <code>StringManager</code> for use in looking up messages.
     */
    StringManager getStringManager() {

        return (sm);

    }


}



/**
 * Digester object creation factory for group instances.
 */
class MemoryGroupCreationFactory implements ObjectCreationFactory {

    public MemoryGroupCreationFactory(MemoryUserDatabase database) {
        this.database = database;
    }

    public Object createObject(Attributes attributes) {
        String groupname = attributes.getValue("groupname");
        if (groupname == null) {
            groupname = attributes.getValue("name");
        }
        String description = attributes.getValue("description");
        String roles = attributes.getValue("roles");
        Group group = database.createGroup(groupname, description);
        if (roles != null) {
            while (roles.length() > 0) {
                String rolename = null;
                int comma = roles.indexOf(',');
                if (comma >= 0) {
                    rolename = roles.substring(0, comma).trim();
                    roles = roles.substring(comma + 1);
                } else {
                    rolename = roles.trim();
                    roles = "";
                }
                if (rolename.length() > 0) {
                    Role role = database.findRole(rolename);
                    if (role == null) {
                        role = database.createRole(rolename, null);
                    }
                    group.addRole(role);
                }
            }
        }
        return (group);
    }

    private MemoryUserDatabase database = null;

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

}


/**
 * Digester object creation factory for role instances.
 */
class MemoryRoleCreationFactory implements ObjectCreationFactory {

    public MemoryRoleCreationFactory(MemoryUserDatabase database) {
        this.database = database;
    }

    public Object createObject(Attributes attributes) {
        String rolename = attributes.getValue("rolename");
        if (rolename == null) {
            rolename = attributes.getValue("name");
        }
        String description = attributes.getValue("description");
        Role role = database.createRole(rolename, description);
        return (role);
    }

    private MemoryUserDatabase database = null;

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

}


/**
 * Digester object creation factory for user instances.
 */
class MemoryUserCreationFactory implements ObjectCreationFactory {

    public MemoryUserCreationFactory(MemoryUserDatabase database) {
        this.database = database;
    }

    public Object createObject(Attributes attributes) {
        String username = attributes.getValue("username");
        if (username == null) {
            username = attributes.getValue("name");
        }
        String password = attributes.getValue("password");
        String fullName = attributes.getValue("fullName");
        if (fullName == null) {
            fullName = attributes.getValue("fullname");
        }
        String groups = attributes.getValue("groups");
        String roles = attributes.getValue("roles");
        User user = database.createUser(username, password, fullName);
        if (groups != null) {
            while (groups.length() > 0) {
                String groupname = null;
                int comma = groups.indexOf(',');
                if (comma >= 0) {
                    groupname = groups.substring(0, comma).trim();
                    groups = groups.substring(comma + 1);
                } else {
                    groupname = groups.trim();
                    groups = "";
                }
                if (groupname.length() > 0) {
                    Group group = database.findGroup(groupname);
                    if (group == null) {
                        group = database.createGroup(groupname, null);
                    }
                    user.addGroup(group);
                }
            }
        }
        if (roles != null) {
            while (roles.length() > 0) {
                String rolename = null;
                int comma = roles.indexOf(',');
                if (comma >= 0) {
                    rolename = roles.substring(0, comma).trim();
                    roles = roles.substring(comma + 1);
                } else {
                    rolename = roles.trim();
                    roles = "";
                }
                if (rolename.length() > 0) {
                    Role role = database.findRole(rolename);
                    if (role == null) {
                        role = database.createRole(rolename, null);
                    }
                    user.addRole(role);
                }
            }
        }
        return (user);
    }

    private MemoryUserDatabase database = null;

    private Digester digester = null;

    public Digester getDigester() {
        return (this.digester);
    }

    public void setDigester(Digester digester) {
        this.digester = digester;
    }

}
