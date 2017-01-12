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
 * Original code Copyright 2000 Quadcap Software:
 * This software may be freely redistributed in source or binary form
 * for any purpose.
 *
 */

package org.apache.catalina.util;

import java.util.Date;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * Fast date formatter that caches recently formatted date information
 * and uses it to avoid too-frequent calls to the underlying
 * formatter.  Note: breaks fieldPosition param of format(Date,
 * StringBuffer, FieldPosition).  If you care about the field
 * position, call the underlying DateFormat directly.
 *
 * @author Stan Bailes
 * @author Alex Chaffee
 **/
public class FastDateFormat extends DateFormat {
    DateFormat    df;
    long          lastSec = -1;
    StringBuffer  sb      = new StringBuffer();
    FieldPosition fp      = new FieldPosition(DateFormat.MILLISECOND_FIELD);

    public FastDateFormat(DateFormat df) {
        this.df = df;
    }

    public Date parse(String text, ParsePosition pos) {
        return df.parse(text, pos);
    }

    /**
     * Note: breaks functionality of fieldPosition param. Also:
     * there's a bug in SimpleDateFormat with "S" and "SS", use "SSS"
     * instead if you want a msec field.
     **/
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                               FieldPosition fieldPosition) {
        long dt = date.getTime();
        long ds = dt / 1000;
        if (ds != lastSec) {
            sb.setLength(0);
            df.format(date, sb, fp);
            lastSec = ds;
        } else {
            // munge current msec into existing string
            int ms = (int)(dt % 1000);
            int pos = fp.getEndIndex();
            int begin = fp.getBeginIndex();
            if (pos > 0) {
                if (pos > begin)
                    sb.setCharAt(--pos, Character.forDigit(ms % 10, 10));
                ms /= 10;
                if (pos > begin)
                    sb.setCharAt(--pos, Character.forDigit(ms % 10, 10));
                ms /= 10;
                if (pos > begin)
                    sb.setCharAt(--pos, Character.forDigit(ms % 10, 10));
            }
        }
        toAppendTo.append(sb.toString());
        return toAppendTo;
    }

    public static void main(String[] args) {
        String format = "yyyy-MM-dd HH:mm:ss.SSS";
        if (args.length > 0)
            format = args[0];
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        FastDateFormat fdf = new FastDateFormat(sdf);
        Date d = new Date();

        d.setTime(1); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(20); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(500); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(543); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(999); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(1050); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(2543); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(12345); System.out.println(fdf.format(d) + "\t" + sdf.format(d));
        d.setTime(12340); System.out.println(fdf.format(d) + "\t" + sdf.format(d));

        final int reps = 100000;
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < reps; i++) {
                d.setTime(System.currentTimeMillis());
                fdf.format(d);
            }
            long elap = System.currentTimeMillis() - start;
            System.out.println("fast: " + elap + " elapsed");
            System.out.println(fdf.format(d));
        }
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < reps; i++) {
                d.setTime(System.currentTimeMillis());
                sdf.format(d);
            }
            long elap = System.currentTimeMillis() - start;
            System.out.println("slow: " + elap + " elapsed");
            System.out.println(sdf.format(d));
        }
    }
}
