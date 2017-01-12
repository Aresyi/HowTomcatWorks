/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/ParameterMap.java,v 1.2 2001/07/22 20:25:13 pier Exp $
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


package org.apache.catalina.util;


import java.util.HashMap;
import java.util.Map;


/**
 * Extended implementation of <strong>HashMap</strong> that includes a
 * <code>locked</code> property.  This class can be used to safely expose
 * Catalina internal parameter map objects to user classes without having
 * to clone them in order to avoid modifications.  When first created, a
 * <code>ParmaeterMap</code> instance is not locked.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2001/07/22 20:25:13 $
 */

public final class ParameterMap extends HashMap {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new, empty map with the default initial capacity and
     * load factor.
     */
    public ParameterMap() {

        super();

    }


    /**
     * Construct a new, empty map with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity The initial capacity of this map
     */
    public ParameterMap(int initialCapacity) {

        super(initialCapacity);

    }


    /**
     * Construct a new, empty map with the specified initial capacity and
     * load factor.
     *
     * @param initialCapacity The initial capacity of this map
     * @param loadFactor The load factor of this map
     */
    public ParameterMap(int initialCapacity, float loadFactor) {

        super(initialCapacity, loadFactor);

    }


    /**
     * Construct a new map with the same mappings as the given map.
     *
     * @param map Map whose contents are dupliated in the new map
     */
    public ParameterMap(Map map) {

        super(map);

    }


    // ------------------------------------------------------------- Properties


    /**
     * The current lock state of this parameter map.
     */
    private boolean locked = false;


    /**
     * Return the locked state of this parameter map.
     */
    public boolean isLocked() {

        return (this.locked);

    }


    /**
     * Set the locked state of this parameter map.
     *
     * @param locked The new locked state
     */
    public void setLocked(boolean locked) {

        this.locked = locked;

    }


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager("org.apache.catalina.util");


    // --------------------------------------------------------- Public Methods



    /**
     * Remove all mappings from this map.
     *
     * @exception IllegalStateException if this map is currently locked
     */
    public void clear() {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        super.clear();

    }


    /**
     * Associate the specified value with the specified key in this map.  If
     * the map previously contained a mapping for this key, the old value is
     * replaced.
     *
     * @param key Key with which the specified value is to be associated
     * @param value Value to be associated with the specified key
     *
     * @return The previous value associated with the specified key, or
     *  <code>null</code> if there was no mapping for key
     *
     * @exception IllegalStateException if this map is currently locked
     */
    public Object put(Object key, Object value) {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        return (super.put(key, value));

    }


    /**
     * Copy all of the mappings from the specified map to this one.  These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified Map.
     *
     * @param map Mappings to be stored into this map
     *
     * @exception IllegalStateException if this map is currently locked
     */
    public void putAll(Map map) {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        super.putAll(map);

    }


    /**
     * Remove the mapping for this key from the map if present.
     *
     * @param key Key whose mapping is to be removed from the map
     *
     * @return The previous value associated with the specified key, or
     *  <code>null</code> if there was no mapping for that key
     *
     * @exception IllegalStateException if this map is currently locked
     */
    public Object remove(Object key) {

        if (locked)
            throw new IllegalStateException
                (sm.getString("parameterMap.locked"));
        return (super.remove(key));

    }


}
