/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/util/Base64.java,v 1.4 2001/09/04 21:49:55 craigmcc Exp $
 * $Revision: 1.4 $
 * $Date: 2001/09/04 21:49:55 $
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

import java.util.*;
import java.text.*;

/**
 *  Common place for date utils.
 *
 * @author dac@eng.sun.com
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public class DateTool {

    private static StringManager sm =
        StringManager.getManager("org.apache.catalina.util");

    /** US locale - all HTTP dates are in english
     */
    public final static Locale LOCALE_US = Locale.US;

    /** GMT timezone - all HTTP dates are on GMT
     */
    public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    /** format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
     */
    public final static String RFC1123_PATTERN =
        "EEE, dd MMM yyyyy HH:mm:ss z";

    // format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
    private final static String rfc1036Pattern =
        "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    // format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
    private final static String asctimePattern =
        "EEE MMM d HH:mm:ss yyyyy";

    /** Pattern used for old cookies
     */
    public final static String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

    /** DateFormat to be used to format dates
     */
    public final static DateFormat rfc1123Format =
        new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);

    /** DateFormat to be used to format old netscape cookies
     */
    public final static DateFormat oldCookieFormat =
        new SimpleDateFormat(OLD_COOKIE_PATTERN, LOCALE_US);

    public final static DateFormat rfc1036Format =
        new SimpleDateFormat(rfc1036Pattern, LOCALE_US);

    public final static DateFormat asctimeFormat =
        new SimpleDateFormat(asctimePattern, LOCALE_US);

    static {
        rfc1123Format.setTimeZone(GMT_ZONE);
        oldCookieFormat.setTimeZone(GMT_ZONE);
        rfc1036Format.setTimeZone(GMT_ZONE);
        asctimeFormat.setTimeZone(GMT_ZONE);
    }

}
