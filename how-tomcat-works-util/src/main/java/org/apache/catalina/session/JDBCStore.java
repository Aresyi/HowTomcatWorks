/*
 * JDBCStore.java
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/JDBCStore.java,v 1.6 2002/09/20 14:05:14 glenn Exp $
 * $Revision: 1.6 $
 * $Date: 2002/09/20 14:05:14 $
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

package org.apache.catalina.session;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.util.CustomObjectInputStream;

/**
 * Implementation of the <code>Store</code> interface that stores
 * serialized session objects in a database.  Sessions that are
 * saved are still subject to being expired based on inactivity.
 *
 * @author Bip Thelin
 * @version $Revision: 1.6 $, $Date: 2002/09/20 14:05:14 $
 */

public class JDBCStore
    extends StoreBase implements Store {

    /**
     * The descriptive information about this implementation.
     */
    protected static String info = "JDBCStore/1.0";

    /**
     * Name to register for this Store, used for logging.
     */
    protected static String storeName = "JDBCStore";

    /**
     * Name to register for the background thread.
     */
    protected String threadName = "JDBCStore";

    /**
     * Connection string to use when connecting to the DB.
     */
    protected String connString = null;

    /**
     * The database connection.
     */
    private Connection conn = null;

    /**
     * Driver to use.
     */
    protected String driverName = null;

    // ------------------------------------------------------------- Table & cols

    /**
     * Table to use.
     */
    protected String sessionTable = "tomcat$sessions";

    /**
     * Id column to use.
     */
    protected String sessionIdCol = "id";

    /**
     * Data column to use.
     */
    protected String sessionDataCol = "data";

    /**
     * Is Valid column to use.
     */
    protected String sessionValidCol = "valid";

    /**
     * Max Inactive column to use.
     */
    protected String sessionMaxInactiveCol = "maxinactive";

    /**
     * Last Accessed column to use.
     */
    protected String sessionLastAccessedCol = "lastaccess";

    // ------------------------------------------------------------- SQL Variables

    /**
     * Variable to hold the <code>getSize()</code> prepared statement.
     */
    protected PreparedStatement preparedSizeSql = null;

    /**
     * Variable to hold the <code>keys()</code> prepared statement.
     */
    protected PreparedStatement preparedKeysSql = null;

    /**
     * Variable to hold the <code>save()</code> prepared statement.
     */
    protected PreparedStatement preparedSaveSql = null;

    /**
     * Variable to hold the <code>clear()</code> prepared statement.
     */
    protected PreparedStatement preparedClearSql = null;

    /**
     * Variable to hold the <code>remove()</code> prepared statement.
     */
    protected PreparedStatement preparedRemoveSql = null;

    /**
     * Variable to hold the <code>load()</code> prepared statement.
     */
    protected PreparedStatement preparedLoadSql = null;

    // ------------------------------------------------------------- Properties

    /**
     * Return the info for this Store.
     */
    public String getInfo() {
        return(info);
    }

    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(threadName);
    }

    /**
     * Return the name for this Store, used for logging.
     */
    public String getStoreName() {
        return(storeName);
    }

    /**
     * Set the driver for this Store.
     *
     * @param driverName The new driver
     */
    public void setDriverName(String driverName) {
        String oldDriverName = this.driverName;
        this.driverName = driverName;
        support.firePropertyChange("driverName",
                                   oldDriverName,
                                   this.driverName);
        this.driverName = driverName;
    }

    /**
     * Return the driver for this Store.
     */
    public String getDriverName() {
        return(this.driverName);
    }

    /**
     * Set the Connection URL for this Store.
     *
     * @param connectionURL The new Connection URL
     */
    public void setConnectionURL(String connectionURL) {
        String oldConnString = this.connString;
        this.connString = connectionURL;
        support.firePropertyChange("connString",
                                   oldConnString,
                                   this.connString);
    }

    /**
     * Return the Connection URL for this Store.
     */
    public String getConnectionURL() {
        return(this.connString);
    }

    /**
     * Set the table for this Store.
     *
     * @param sessionTable The new table
     */
    public void setSessionTable(String sessionTable) {
        String oldSessionTable = this.sessionTable;
        this.sessionTable = sessionTable;
        support.firePropertyChange("sessionTable",
                                   oldSessionTable,
                                   this.sessionTable);
    }

    /**
     * Return the table for this Store.
     */
    public String getSessionTable() {
        return(this.sessionTable);
    }

    /**
     * Set the Id column for the table.
     *
     * @param sessionIdCol the column name
     */
    public void setSessionIdCol(String sessionIdCol) {
        String oldSessionIdCol = this.sessionIdCol;
        this.sessionIdCol = sessionIdCol;
        support.firePropertyChange("sessionIdCol",
                                   oldSessionIdCol,
                                   this.sessionIdCol);
    }

    /**
     * Return the Id column for the table.
     */
    public String getSessionIdCol() {
        return(this.sessionIdCol);
    }

    /**
     * Set the Data column for the table
     *
     * @param sessionDataCol the column name
     */
    public void setSessionDataCol(String sessionDataCol) {
        String oldSessionDataCol = this.sessionDataCol;
        this.sessionDataCol = sessionDataCol;
        support.firePropertyChange("sessionDataCol",
                                   oldSessionDataCol,
                                   this.sessionDataCol);
    }

    /**
     * Return the data column for the table
     */
    public String getSessionDataCol() {
        return(this.sessionDataCol);
    }

    /**
     * Set the Is Valid column for the table
     *
     * @param sessionValidCol The column name
     */
    public void setSessionValidCol(String sessionValidCol) {
        String oldSessionValidCol = this.sessionValidCol;
        this.sessionValidCol = sessionValidCol;
        support.firePropertyChange("sessionValidCol",
                                   oldSessionValidCol,
                                   this.sessionValidCol);
    }

    /**
     * Return the Is Valid column
     */
    public String getSessionValidCol() {
        return(this.sessionValidCol);
    }

    /**
     * Set the Max Inactive column for the table
     *
     * @param sessionMaxInactiveCol The column name
     */
    public void setSessionMaxInactiveCol(String sessionMaxInactiveCol) {
        String oldSessionMaxInactiveCol = this.sessionMaxInactiveCol;
        this.sessionMaxInactiveCol = sessionMaxInactiveCol;
        support.firePropertyChange("sessionMaxInactiveCol",
                                   oldSessionMaxInactiveCol,
                                   this.sessionMaxInactiveCol);
    }

    /**
     * Return the Max Inactive column
     */
    public String getSessionMaxInactiveCol() {
        return(this.sessionMaxInactiveCol);
    }

    /**
     * Set the Last Accessed column for the table
     *
     * @param sessionLastAccessedCol The column name
     */
    public void setSessionLastAccessedCol(String sessionLastAccessedCol) {
        String oldSessionLastAccessedCol = this.sessionLastAccessedCol;
        this.sessionLastAccessedCol = sessionLastAccessedCol;
        support.firePropertyChange("sessionLastAccessedCol",
                                   oldSessionLastAccessedCol,
                                   this.sessionLastAccessedCol);
    }

    /**
     * Return the Last Accessed column
     */
    public String getSessionLastAccessedCol() {
        return(this.sessionLastAccessedCol);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys() throws IOException {
        String keysSql =
            "SELECT COUNT(s."+sessionIdCol+"), c."+sessionIdCol+
            " FROM "+sessionTable+" s, "+sessionTable+" c"+
            " GROUP BY c."+sessionIdCol;

        Connection _conn = getConnection();
        ResultSet rst = null;
        String keys[] = null;
        int i;

        if(_conn == null)
            return(new String[0]);

        try {
            if(preparedKeysSql == null)
                preparedKeysSql = _conn.prepareStatement(keysSql);

            rst = preparedKeysSql.executeQuery();
            if (rst != null && rst.next()) {
                keys = new String[rst.getInt(1)];
                keys[0] = rst.getString(2);
                i=1;

                while(rst.next())
                    keys[i++] = rst.getString(2);
            } else {
                keys = new String[0];
            }
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } finally {
            try {
                if(rst != null)
                    rst.close();
            } catch(SQLException e) {
                ;
            }

            release(_conn);
            _conn = null;
        }

        return(keys);
    }

    /**
     * Return an integer containing a count of all Sessions
     * currently saved in this Store.  If there are no Sessions,
     * <code>0</code> is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public int getSize() throws IOException {
        int size = 0;
        String sizeSql = "SELECT COUNT("+sessionIdCol+
            ") FROM ".concat(sessionTable);
        Connection _conn = getConnection();
        ResultSet rst = null;

        if(_conn == null)
            return(size);

        try {
            if(preparedSizeSql == null)
                preparedSizeSql = _conn.prepareStatement(sizeSql);

            rst = preparedSizeSql.executeQuery();
            if (rst.next())
                size = rst.getInt(1);
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } finally {
            try {
                if(rst != null)
                    rst.close();
            } catch(SQLException e) {
                ;
            }

            release(_conn);
            _conn = null;
        }

        return(size);
    }

    /**
     * Load the Session associated with the id <code>id</code>.
     * If no such session is found <code>null</code> is returned.
     *
     * @param id a value of type <code>String</code>
     * @return the stored <code>Session</code>
     * @exception ClassNotFoundException if an error occurs
     * @exception IOException if an input/output error occurred
     */
    public Session load(String id)
        throws ClassNotFoundException, IOException {
        ResultSet rst = null;
        Connection _conn = getConnection();
        StandardSession _session = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        BufferedInputStream bis = null;
        Container container = manager.getContainer();
        String loadSql = "SELECT "+sessionIdCol+
            ", "+sessionDataCol+" FROM "+sessionTable+
            " WHERE "+sessionIdCol+" = ?";

        if(_conn == null)
            return(null);

        try {
            if(preparedLoadSql == null)
                preparedLoadSql = _conn.prepareStatement(loadSql);

            preparedLoadSql.setString(1, id);
            rst = preparedLoadSql.executeQuery();
            if (rst.next()) {
                bis = new BufferedInputStream(rst.getBinaryStream(2));

                if (container != null)
                    loader = container.getLoader();

                if (loader != null)
                    classLoader = loader.getClassLoader();

                if (classLoader != null)
                    ois = new CustomObjectInputStream(bis,
                                                      classLoader);
                else
                    ois = new ObjectInputStream(bis);
            } else if (debug > 0) {
                log(getStoreName()+": No persisted data object found");
            }
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } finally {
            try {
                if(rst != null)
                    rst.close();
            } catch(SQLException e) {
                ;
            }

            release(_conn);
            _conn = null;
        }

        if(ois != null) {
            try {
                _session = (StandardSession) manager.createSession();
                _session.readObjectData(ois);
                _session.setManager(manager);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                        bis = null;
                    } catch (IOException e) {
                        ;
                    }
                }
            }

            if (debug > 0)
                log(sm.getString(getStoreName()+".loading",
                                 id, sessionTable));
        }

        return(_session);
    }

    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Session is present, this method
     * takes no action.
     *
     * @param id Session identifier of the Session to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public void remove(String id) throws IOException {
        Connection _conn = getConnection();
        String removeSql = "DELETE FROM "+sessionTable+" WHERE "+
            sessionIdCol+" = ?";

        if(_conn == null)
            return;

        try {
            if(preparedRemoveSql == null)
                preparedRemoveSql = _conn.prepareStatement(removeSql);

            preparedRemoveSql.setString(1, id);
            preparedRemoveSql.execute();
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } finally {
            release(_conn);
            _conn = null;
        }

        if (debug > 0)
            log(sm.getString(getStoreName()+".removing", id, sessionTable));
    }

    /**
     * Remove all of the Sessions in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public void clear() throws IOException {
        Connection _conn = getConnection();
        String clearSql = "DELETE FROM ".concat(sessionTable);

        if(_conn == null)
            return;

        try {
            if(preparedClearSql == null)
                preparedClearSql = _conn.prepareStatement(clearSql);

            preparedClearSql.execute();
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } finally {
            release(_conn);
            _conn = null;
        }
    }

    /**
     * Save a session to the Store.
     *
     * @param session the session to be stored
     * @exception IOException if an input/output error occurs
     */
    public void save(Session session) throws IOException {
        String saveSql = "INSERT INTO "+sessionTable+" ("+
            sessionIdCol+", "+
            sessionDataCol+", "+
            sessionValidCol+", "+
            sessionMaxInactiveCol+", "+
            sessionLastAccessedCol+") VALUES (?, ?, ?, ?, ?)";
        Connection _conn = getConnection();
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        InputStream in = null;

        if(_conn == null)
            return;

        // If sessions already exist in DB, remove and insert again.
        // TODO:
        // * Check if ID exists in database and if so use UPDATE.
        remove(session.getId());

        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));

            ((StandardSession)session).writeObjectData(oos);
            oos.close();

            byte[] obs = bos.toByteArray();
            int size = obs.length;
            bis = new ByteArrayInputStream(obs, 0, size);
            in = new BufferedInputStream(bis, size);

            if(preparedSaveSql == null)
                preparedSaveSql = _conn.prepareStatement(saveSql);

            preparedSaveSql.setString(1, session.getId());
            preparedSaveSql.setBinaryStream(2, in, size);
            preparedSaveSql.setString(3, session.isValid()?"1":"0");
            preparedSaveSql.setInt(4, session.getMaxInactiveInterval());
            preparedSaveSql.setLong(5, session.getLastAccessedTime());
            preparedSaveSql.execute();
        } catch(SQLException e) {
            log(sm.getString(getStoreName()+".SQLException", e));
        } catch (IOException e) {
            ;
        } finally {
            if(bis != null)
                bis.close();

            if(in != null)
                in.close();

            bis = null;
            bos = null;
            oos = null;
            in = null;

            release(_conn);
            _conn = null;
        }
        if (debug > 0)
            log(sm.getString(getStoreName()+".saving",
                             session.getId(), sessionTable));
    }

    // --------------------------------------------------------- Protected Methods

    /**
     * Check the connection associated with this store, if it's
     * <code>null</code> or closed try to reopen it.
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection suceeded
     */
    protected Connection getConnection(){
        try {
            if(conn == null || conn.isClosed()) {
                Class.forName(driverName);
                log(sm.getString(getStoreName()+".checkConnectionDBClosed"));
                conn = DriverManager.getConnection(connString);
                conn.setAutoCommit(true);

                if(conn == null || conn.isClosed())
                    log(sm.getString(getStoreName()+".checkConnectionDBReOpenFail"));
            }
        } catch (SQLException ex){
            log(sm.getString(getStoreName()+".checkConnectionSQLException",
                             ex.toString()));
        } catch (ClassNotFoundException ex) {
            log(sm.getString(getStoreName()+".checkConnectionClassNotFoundException",
                             ex.toString()));
        }

        return conn;
    }

    /**
     * Release the connection, not needed here since the
     * connection is not associated with a connection pool.
     *
     * @param conn The connection to be released
     */
    protected void release(Connection conn) {
        ;
    }

    /**
     * Called once when this Store is first started.
     */
    public void start() throws LifecycleException {
        super.start();

        // Open connection to the database
        this.conn = getConnection();
    }

    /**
     * Gracefully terminate everything associated with our db.
     * Called once when this Store is stoping.
     *
     */
    public void stop() throws LifecycleException {
        super.stop();

        // Close and release everything associated with our db.
        if(conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                ;
            }

            if( preparedSizeSql != null ) {
                try {
                    preparedSizeSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            if( preparedKeysSql != null ) { 
                try {
                    preparedKeysSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            if( preparedSaveSql != null ) { 
                try {
                    preparedSaveSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            if( preparedClearSql != null ) { 
                try {
                    preparedClearSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            if( preparedRemoveSql != null ) { 
                try {
                    preparedRemoveSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            if( preparedLoadSql != null ) { 
                try {
                    preparedLoadSql.close();
                } catch (SQLException e) {
                    ;
                }
            }

            try {
                conn.close();
            } catch (SQLException e) {
                ;
            }

            this.preparedSizeSql = null;
            this.preparedKeysSql = null;
            this.preparedSaveSql = null;
            this.preparedClearSql = null;
            this.preparedRemoveSql = null;
            this.preparedLoadSql = null;
            this.conn = null;
        }
    }
}
