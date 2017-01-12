/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/ResourceSet.java,v 1.2 2001/04/25 20:30:37 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2001/04/25 20:30:37 $
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


import java.util.Collection;
import java.util.HashSet;

/**
 * Extended implementation of <strong>HashSet</strong> that includes a
 * <code>locked</code> property.  This class can be used to safely expose
 * resource path sets to user classes without having to clone them in order
 * to avoid modifications.  When first created, a <code>ResourceMap</code>
 * is not locked.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2001/04/25 20:30:37 $
 */

public final class ResourceSet extends HashSet {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new, empty set with the default initial capacity and
     * load factor.
     */
    public ResourceSet() {

        super();

    }


    /**
     * Construct a new, empty set with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity The initial capacity of this set
     */
    public ResourceSet(int initialCapacity) {

        super(initialCapacity);

    }


    /**
     * Construct a new, empty set with the specified initial capacity and
     * load factor.
     *
     * @param initialCapacity The initial capacity of this set
     * @param loadFactor The load factor of this set
     */
    public ResourceSet(int initialCapacity, float loadFactor) {

        super(initialCapacity, loadFactor);

    }


    /**
     * Construct a new set with the same contents as the existing collection.
     *
     * @param coll The collection whose contents we should copy
     */
    public ResourceSet(Collection coll) {

        super(coll);

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
     * Add the specified element to this set if it is not already present.
     * Return <code>true</code> if the element was added.
     *
     * @param o The object to be added
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public boolean add(Object o) {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        return (super.add(o));

    }


    /**
     * Remove all of the elements from this set.
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public void clear() {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        super.clear();

    }


    /**
     * Remove the given element from this set if it is present.
     * Return <code>true</code> if the element was removed.
     *
     * @param o The object to be removed
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public boolean remove(Object o) {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        return (super.remove(o));

    }


}
