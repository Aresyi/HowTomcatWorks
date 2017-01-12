/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/session/FileStore.java,v 1.7 2002/03/18 18:56:21 craigmcc Exp $
 * $Revision: 1.7 $
 * $Date: 2002/03/18 18:56:21 $
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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.Container;
import org.apache.catalina.util.CustomObjectInputStream;


/**
 * Concrete implementation of the <b>Store</b> interface that utilizes
 * a file per saved Session in a configured directory.  Sessions that are
 * saved are still subject to being expired based on inactivity.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.7 $ $Date: 2002/03/18 18:56:21 $
 */

public final class FileStore
    extends StoreBase implements Store {


    // ----------------------------------------------------- Constants


    /**
     * The extension to use for serialized session filenames.
     */
    private static final String FILE_EXT = ".session";


    // ----------------------------------------------------- Instance Variables


    /**
     * The pathname of the directory in which Sessions are stored.
     * This may be an absolute pathname, or a relative path that is
     * resolved against the temporary work directory for this application.
     */
    private String directory = ".";


    /**
     * A File representing the directory in which Sessions are stored.
     */
    private File directoryFile = null;


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "FileStore/1.0";

    /**
     * Name to register for this Store, used for logging.
     */
    private static final String storeName = "fileStore";

    /**
     * Name to register for the background thread.
     */
    private static final String threadName = "FileStore";


    // ------------------------------------------------------------- Properties


    /**
     * Return the directory path for this Store.
     */
    public String getDirectory() {

        return (directory);

    }


    /**
     * Set the directory path for this Store.
     *
     * @param path The new directory path
     */
    public void setDirectory(String path) {

        String oldDirectory = this.directory;
        this.directory = path;
        this.directoryFile = null;
        support.firePropertyChange("directory", oldDirectory,
                                   this.directory);

    }


    /**
     * Return descriptive information about this Store implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

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
     * Return the number of Sessions present in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public int getSize() throws IOException {

        // Acquire the list of files in our storage directory
        File file = directory();
        if (file == null) {
            return (0);
        }
        String files[] = file.list();

        // Figure out which files are sessions
        int keycount = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(FILE_EXT)) {
                keycount++;
            }
        }
        return (keycount);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Remove all of the Sessions in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public void clear()
        throws IOException {

        String[] keys = keys();
        for (int i = 0; i < keys.length; i++) {
            remove(keys[i]);
        }

    }


    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys() throws IOException {

        // Acquire the list of files in our storage directory
        File file = directory();
        if (file == null) {
            return (new String[0]);
        }
        String files[] = file.list();

        // Build and return the list of session identifiers
        ArrayList list = new ArrayList();
        int n = FILE_EXT.length();
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(FILE_EXT)) {
                list.add(files[i].substring(0, files[i].length() - n));
            }
        }
        return ((String[]) list.toArray(new String[list.size()]));

    }


    /**
     * Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param id Session identifier of the session to load
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
    public Session load(String id)
        throws ClassNotFoundException, IOException {

        // Open an input stream to the specified pathname, if any
        File file = file(id);
        if (file == null) {
            return (null);
        }
        if (debug >= 1) {
            log(sm.getString(getStoreName()+".loading",
                             id, file.getAbsolutePath()));
        }

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            BufferedInputStream bis = new BufferedInputStream(fis);
            Container container = manager.getContainer();
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null)
                ois = new CustomObjectInputStream(bis, classLoader);
            else
                ois = new ObjectInputStream(bis);
        } catch (FileNotFoundException e) {
            if (debug >= 1)
                log("No persisted data file found");
            return (null);
        } catch (IOException e) {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }

        try {
            StandardSession session =
                (StandardSession) manager.createSession();
            session.readObjectData(ois);
            session.setManager(manager);
            return (session);
        } finally {
            // Close the input stream
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
            }
        }
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

        File file = file(id);
        if (file == null) {
            return;
        }
        if (debug >= 1) {
            log(sm.getString(getStoreName()+".removing",
                             id, file.getAbsolutePath()));
        }
        file.delete();

    }


    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */
    public void save(Session session) throws IOException {

        // Open an output stream to the specified pathname, if any
        File file = file(session.getId());
        if (file == null) {
            return;
        }
        if (debug >= 1) {
            log(sm.getString(getStoreName()+".saving",
                             session.getId(), file.getAbsolutePath()));
        }
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        } catch (IOException e) {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
            }
            throw e;
        }

        try {
            ((StandardSession)session).writeObjectData(oos);
        } finally {
            oos.close();
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Return a File object representing the pathname to our
     * session persistence directory, if any.  The directory will be
     * created if it does not already exist.
     */
    private File directory() {

        if (this.directory == null) {
            return (null);
        }
        if (this.directoryFile != null) {
            // NOTE:  Race condition is harmless, so do not synchronize
            return (this.directoryFile);
        }
        File file = new File(this.directory);
        if (!file.isAbsolute()) {
            Container container = manager.getContainer();
            if (container instanceof Context) {
                ServletContext servletContext =
                    ((Context) container).getServletContext();
                File work = (File)
                    servletContext.getAttribute(Globals.WORK_DIR_ATTR);
                file = new File(work, this.directory);
            } else {
                throw new IllegalArgumentException
                    ("Parent Container is not a Context");
            }
        }
        if (!file.exists() || !file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        this.directoryFile = file;
        return (file);

    }


    /**
     * Return a File object representing the pathname to our
     * session persistence file, if any.
     *
     * @param id The ID of the Session to be retrieved. This is
     *    used in the file naming.
     */
    private File file(String id) {

        if (this.directory == null) {
            return (null);
        }
        String filename = id + FILE_EXT;
        File file = new File(directory(), filename);
        return (file);

    }


}
