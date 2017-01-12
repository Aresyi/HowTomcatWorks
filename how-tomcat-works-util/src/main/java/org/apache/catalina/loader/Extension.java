/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/loader/Extension.java,v 1.4 2001/07/22 20:25:10 pier Exp $
 * $Revision: 1.4 $
 * $Date: 2001/07/22 20:25:10 $
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


package org.apache.catalina.loader;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * Utility class that represents either an available "Optional Package"
 * (formerly known as "Standard Extension") as described in the manifest
 * of a JAR file, or the requirement for such an optional package.  It is
 * used to support the requirements of the Servlet Specification, version
 * 2.3, related to providing shared extensions to all webapps.
 * <p>
 * In addition, static utility methods are available to scan a manifest
 * and return an array of either available or required optional modules
 * documented in that manifest.
 * <p>
 * For more information about optional packages, see the document
 * <em>Optional Package Versioning</em> in the documentation bundle for your
 * Java2 Standard Edition package, in file
 * <code>guide/extensions/versioning.html</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2001/07/22 20:25:10 $
 */

public final class Extension {


    // ------------------------------------------------------------- Properties


    /**
     * The name of the optional package being made available, or required.
     */
    private String extensionName = null;

    public String getExtensionName() {
        return (this.extensionName);
    }

    public void setExtensionName(String extensionName) {
        this.extensionName = extensionName;
    }


    /**
     * The URL from which the most recent version of this optional package
     * can be obtained if it is not already installed.
     */
    private String implementationURL = null;

    public String getImplementationURL() {
        return (this.implementationURL);
    }

    public void setImplementationURL(String implementationURL) {
        this.implementationURL = implementationURL;
    }


    /**
     * The name of the company or organization that produced this
     * implementation of this optional package.
     */
    private String implementationVendor = null;

    public String getImplementationVendor() {
        return (this.implementationVendor);
    }

    public void setImplementationVendor(String implementationVendor) {
        this.implementationVendor = implementationVendor;
    }


    /**
     * The unique identifier of the company that produced the optional
     * package contained in this JAR file.
     */
    private String implementationVendorId = null;

    public String getImplementationVendorId() {
        return (this.implementationVendorId);
    }

    public void setImplementationVendorId(String implementationVendorId) {
        this.implementationVendorId = implementationVendorId;
    }


    /**
     * The version number (dotted decimal notation) for this implementation
     * of the optional package.
     */
    private String implementationVersion = null;

    public String getImplementationVersion() {
        return (this.implementationVersion);
    }

    public void setImplementationVersion(String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }


    /**
     * The name of the company or organization that originated the
     * specification to which this optional package conforms.
     */
    private String specificationVendor = null;

    public String getSpecificationVendor() {
        return (this.specificationVendor);
    }

    public void setSpecificationVendor(String specificationVendor) {
        this.specificationVendor = specificationVendor;
    }


    /**
     * The version number (dotted decimal notation) of the specification
     * to which this optional package conforms.
     */
    private String specificationVersion = null;

    public String getSpecificationVersion() {
        return (this.specificationVersion);
    }

    public void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return <code>true</code> if the specified <code>Extension</code>
     * (which represents an optional package required by this application)
     * is satisfied by this <code>Extension</code> (which represents an
     * optional package that is already installed.  Otherwise, return
     * <code>false</code>.
     *
     * @param required Description of the required optional package
     */
    public boolean isCompatibleWith(Extension required) {

        // Extension Name must match
        if (extensionName == null)
            return (false);
        if (!extensionName.equals(required.getExtensionName()))
            return (false);

        // Available specification version must be >= required
        if (!isNewer(specificationVersion, required.getSpecificationVersion()))
            return (false);

        // Implementation Vendor ID must match
        if (implementationVendorId == null)
            return (false);
        if (!implementationVendorId.equals(required.getImplementationVendorId()))
            return (false);

        // Implementation version must be >= required
        if (!isNewer(implementationVersion, required.getImplementationVersion()))
            return (false);

        // This available optional package satisfies the requirements
        return (true);

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("Extension[");
        sb.append(extensionName);
        if (implementationURL != null) {
            sb.append(", implementationURL=");
            sb.append(implementationURL);
        }
        if (implementationVendor != null) {
            sb.append(", implementationVendor=");
            sb.append(implementationVendor);
        }
        if (implementationVendorId != null) {
            sb.append(", implementationVendorId=");
            sb.append(implementationVendorId);
        }
        if (implementationVersion != null) {
            sb.append(", implementationVersion=");
            sb.append(implementationVersion);
        }
        if (specificationVendor != null) {
            sb.append(", specificationVendor=");
            sb.append(specificationVendor);
        }
        if (specificationVersion != null) {
            sb.append(", specificationVersion=");
            sb.append(specificationVersion);
        }
        sb.append("]");
        return (sb.toString());

    }


    // --------------------------------------------------------- Static Methods


    /**
     * Return the set of <code>Extension</code> objects representing optional
     * packages that are available in the JAR file associated with the
     * specified <code>Manifest</code>.  If there are no such optional
     * packages, a zero-length list is returned.
     *
     * @param manifest Manifest to be parsed
     */
    public static List getAvailable(Manifest manifest) {

        ArrayList results = new ArrayList();
        if (manifest == null)
            return (results);
        Extension extension = null;

        Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
            extension = getAvailable(attributes);
            if (extension != null)
                results.add(extension);
        }

        Map entries = manifest.getEntries();
        Iterator keys = entries.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            attributes = (Attributes) entries.get(key);
            extension = getAvailable(attributes);
            if (extension != null)
                results.add(extension);
        }

        return (results);

    }


    /**
     * Return the set of <code>Extension</code> objects representing optional
     * packages that are required by the application contained in the JAR
     * file associated with the specified <code>Manifest</code>.  If there
     * are no such optional packages, a zero-length list is returned.
     *
     * @param manifest Manifest to be parsed
     */
    public static List getRequired(Manifest manifest) {

        ArrayList results = new ArrayList();

        Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
            Iterator required = getRequired(attributes).iterator();
            while (required.hasNext())
                results.add(required.next());
        }

        Map entries = manifest.getEntries();
        Iterator keys = entries.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            attributes = (Attributes) entries.get(key);
            Iterator required = getRequired(attributes).iterator();
            while (required.hasNext())
                results.add(required.next());
        }

        return (results);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * If the specified manifest attributes entry represents an available
     * optional package, construct and return an <code>Extension</code>
     * instance representing this package; otherwise return <code>null</code>.
     *
     * @param attributes Manifest attributes to be parsed
     */
    private static Extension getAvailable(Attributes attributes) {

        String name = attributes.getValue("Extension-Name");
        if (name == null)
            return (null);
        Extension extension = new Extension();
        extension.setExtensionName(name);

        extension.setImplementationVendor
            (attributes.getValue("Implementation-Vendor"));
        extension.setImplementationVendorId
            (attributes.getValue("Implementation-Vendor-Id"));
        extension.setImplementationVersion
            (attributes.getValue("Implementation-Version"));
        extension.setSpecificationVendor
            (attributes.getValue("Specification-Vendor"));
        extension.setSpecificationVersion
            (attributes.getValue("Specification-Version"));

        return (extension);

    }


    /**
     * Return the set of required optional packages defined in the specified
     * attributes entry, if any.  If no such optional packages are found,
     * a zero-length list is returned.
     *
     * @param attributes Attributes to be parsed
     */
    private static List getRequired(Attributes attributes) {

        ArrayList results = new ArrayList();
        String names = attributes.getValue("Extension-List");
        if (names == null)
            return (results);
        names += " ";

        while (true) {

            int space = names.indexOf(' ');
            if (space < 0)
                break;
            String name = names.substring(0, space).trim();
            names = names.substring(space + 1);

            String value =
                attributes.getValue(name + "-Extension-Name");
            if (value == null)
                continue;
            Extension extension = new Extension();
            extension.setExtensionName(value);

            extension.setImplementationURL
                (attributes.getValue(name + "-Implementation-URL"));
            extension.setImplementationVendorId
                (attributes.getValue(name + "-Implementation-Vendor-Id"));
            extension.setImplementationVersion
                (attributes.getValue(name + "-Implementation-Version"));
            extension.setSpecificationVersion
                (attributes.getValue(name + "-Specification-Version"));

            results.add(extension);

        }

        return (results);

    }


    /**
     * Return <code>true</code> if the first version number is greater than
     * or equal to the second; otherwise return <code>false</code>.
     *
     * @param first First version number (dotted decimal)
     * @param second Second version number (dotted decimal)
     *
     * @exception NumberFormatException on a malformed version number
     */
    private boolean isNewer(String first, String second)
        throws NumberFormatException {

        if ((first == null) || (second == null))
            return (false);
        if (first.equals(second))
            return (true);

        StringTokenizer fTok = new StringTokenizer(first, ".", true);
        StringTokenizer sTok = new StringTokenizer(second, ".", true);
        int fVersion = 0;
        int sVersion = 0;
        while (fTok.hasMoreTokens() || sTok.hasMoreTokens()) {
            if (fTok.hasMoreTokens())
                fVersion = Integer.parseInt(fTok.nextToken());
            else
                fVersion = 0;
            if (sTok.hasMoreTokens())
                sVersion = Integer.parseInt(sTok.nextToken());
            else
                sVersion = 0;
            if (fVersion < sVersion)
                return (false);
            else if (fVersion > sVersion)
                return (true);
            if (fTok.hasMoreTokens())   // Swallow the periods
                fTok.nextToken();
            if (sTok.hasMoreTokens())
                sTok.nextToken();
        }

        return (true);  // Exact match

    }


}
