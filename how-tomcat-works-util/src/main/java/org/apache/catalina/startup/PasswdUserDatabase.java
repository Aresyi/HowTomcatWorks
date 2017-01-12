/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/startup/PasswdUserDatabase.java,v 1.2 2001/07/22 20:25:13 pier Exp $
 * $Revision: 1.2 $
 * $Date: 2001/07/22 20:25:13 $
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


package org.apache.catalina.startup;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;


/**
 * Concrete implementation of the <strong>UserDatabase</code> interface
 * that processes the <code>/etc/passwd</code> file on a Unix system.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2001/07/22 20:25:13 $
 */

public final class PasswdUserDatabase
    implements UserDatabase {


    // --------------------------------------------------------- Constructors


    /**
     * Initialize a new instance of this user database component.
     */
    public PasswdUserDatabase() {

        super();

    }


    // --------------------------------------------------- Instance Variables


    /**
     * The pathname of the Unix password file.
     */
    private static final String PASSWORD_FILE = "/etc/passwd";


    /**
     * The set of home directories for all defined users, keyed by username.
     */
    private Hashtable homes = new Hashtable();


    /**
     * The UserConfig listener with which we are associated.
     */
    private UserConfig userConfig = null;


    // ----------------------------------------------------------- Properties


    /**
     * Return the UserConfig listener with which we are associated.
     */
    public UserConfig getUserConfig() {

        return (this.userConfig);

    }


    /**
     * Set the UserConfig listener with which we are associated.
     *
     * @param userConfig The new UserConfig listener
     */
    public void setUserConfig(UserConfig userConfig) {

        this.userConfig = userConfig;
        init();

    }


    // ------------------------------------------------------- Public Methods


    /**
     * Return an absolute pathname to the home directory for the specified user.
     *
     * @param user User for which a home directory should be retrieved
     */
    public String getHome(String user) {

        return ((String) homes.get(user));

    }


    /**
     * Return an enumeration of the usernames defined on this server.
     */
    public Enumeration getUsers() {

        return (homes.keys());

    }


    // ------------------------------------------------------ Private Methods


    /**
     * Initialize our set of users and home directories.
     */
    private void init() {

        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new FileReader(PASSWORD_FILE));

            while (true) {

                // Accumulate the next line
                StringBuffer buffer = new StringBuffer();
                while (true) {
                    int ch = reader.read();
                    if ((ch < 0) || (ch == '\n'))
                        break;
                    buffer.append((char) ch);
                }
                String line = buffer.toString();
                if (line.length() < 1)
                    break;

                // Parse the line into constituent elements
                int n = 0;
                String tokens[] = new String[7];
                for (int i = 0; i < tokens.length; i++)
                    tokens[i] = null;
                while (n < tokens.length) {
                    String token = null;
                    int colon = line.indexOf(':');
                    if (colon >= 0) {
                        token = line.substring(0, colon);
                        line = line.substring(colon + 1);
                    } else {
                        token = line;
                        line = "";
                    }
                    tokens[n++] = token;
                }

                // Add this user and corresponding directory
                if ((tokens[0] != null) && (tokens[5] != null))
                    homes.put(tokens[0], tokens[5]);

            }

            reader.close();
            reader = null;

        } catch (Exception e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException f) {
                    ;
                }
                reader = null;
            }
        }

    }


}
