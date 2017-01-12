/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/users/MemoryRole.java,v 1.2 2002/02/10 03:20:17 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2002/02/10 03:20:17 $
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

import org.apache.catalina.UserDatabase;


/**
 * <p>Concrete implementation of {@link Role} for the
 * {@link MemoryUserDatabase} implementation of {@link UserDatabase}.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2002/02/10 03:20:17 $
 * @since 4.1
 */

public class MemoryRole extends AbstractRole {


    // ----------------------------------------------------------- Constructors


    /**
     * Package-private constructor used by the factory method in
     * {@link MemoryUserDatabase}.
     *
     * @param database The {@link MemoryUserDatabase} that owns this role
     * @param rolename Role name of this role
     * @param description Description of this role
     */
    MemoryRole(MemoryUserDatabase database,
               String rolename, String description) {

        super();
        this.database = database;
        setRolename(rolename);
        setDescription(description);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link MemoryUserDatabase} that owns this role.
     */
    protected MemoryUserDatabase database = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the {@link UserDatabase} within which this role is defined.
     */
    public UserDatabase getUserDatabase() {

        return (this.database);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Return a String representation of this role in XML format.</p>
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<role rolename=\"");
        sb.append(rolename);
        sb.append("\"");
        if (description != null) {
            sb.append(" description=\"");
            sb.append(description);
            sb.append("\"");
        }
        sb.append("/>");
        return (sb.toString());

    }


}
