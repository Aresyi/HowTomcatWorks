/*
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

/*
 * This class is based on a class originally written by Jason Hunter
 * <jhunter@acm.org> as part of the book "Java Servlet Programming"
 * (O'Reilly).  See http://www.servlets.com/book for more information.
 * Used by Sun Microsystems with permission.
 */

package org.apache.catalina.util;


import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;



/**
 * Utility class that attempts to map from a Locale to the corresponding
 * character set to be used for interpreting input text (or generating
 * output text) when the Content-Type header does not include one.  You
 * can customize the behavior of this class by modifying the mapping data
 * it loads, or by subclassing it (to change the algorithm) and then using
 * your own version for a particular web application.
 *
 * @author Craig R. McClanahan
 * @revision $Date: 2001/07/22 20:25:13 $ $Version$
 */

public class CharsetMapper {


    // ---------------------------------------------------- Manifest Constants


    /**
     * Default properties resource name.
     */
    public static final String DEFAULT_RESOURCE =
      "/org/apache/catalina/util/CharsetMapperDefault.properties";


    // ---------------------------------------------------------- Constructors


    /**
     * Construct a new CharsetMapper using the default properties resource.
     */
    public CharsetMapper() {

        this(DEFAULT_RESOURCE);

    }


    /**
     * Construct a new CharsetMapper using the specified properties resource.
     *
     * @param name Name of a properties resource to be loaded
     *
     * @exception IllegalArgumentException if the specified properties
     *  resource could not be loaded for any reason.
     */
    public CharsetMapper(String name) {

        try {
            InputStream stream =
              this.getClass().getResourceAsStream(name);
            map.load(stream);
            stream.close();
        } catch (Throwable t) {
            throw new IllegalArgumentException(t.toString());
        }


    }


    // ---------------------------------------------------- Instance Variables


    /**
     * The mapping properties that have been initialized from the specified or
     * default properties resource.
     */
    private Properties map = new Properties();




    // ------------------------------------------------------- Public Methods


    /**
     * Calculate the name of a character set to be assumed, given the specified
     * Locale and the absence of a character set specified as part of the
     * content type header.
     *
     * @param locale The locale for which to calculate a character set
     */
    public String getCharset(Locale locale) {

        String charset = null;

        // First, try a full name match (language and country)
        charset = map.getProperty(locale.toString());
        if (charset != null)
            return (charset);

        // Second, try to match just the language
        charset = map.getProperty(locale.getLanguage());
        return (charset);

    }


}
