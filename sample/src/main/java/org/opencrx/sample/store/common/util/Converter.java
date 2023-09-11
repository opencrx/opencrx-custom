/*
 * ====================================================================
 * Project:     openCRX/Store, http://www.opencrx.org/
 * Description: ProductManager
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openCRX team nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.opencrx.sample.store.common.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Converter contains methods to convert data types and prevent null from being
 * used.
 * <p/>
 * It also provides conversion routines for intrinsic to object data types. For example,
 * from int to Integer.
 *
 * @author OAZM (initial implementation)
 * @author WFRO (port to openCRX)
 */
public final class Converter
{
    private Converter(
    ) {
    }

    public static void setLocale(
        Locale locale
    ) {
        Converter.locale = locale;
    }
    
    public static final String getString(
        final float f
    ) {
        return DecimalFormat.getCurrencyInstance(Converter.locale).format( f );
    }

    public static final float getCurrency(
        final String s
    ) {
        try {
            final Number value = DecimalFormat.getCurrencyInstance(Converter.locale).parse( s );
            return value.floatValue();
        } catch( ParseException e ) {
            e.printStackTrace();
            return 0f;
        }
    }
    
    public static final String getString(
    	final int i
    ) {
        return String.valueOf(i);
    }

    public static final String getString(
    	final boolean b
    ) {
        return String.valueOf(b);
    }

    public static final String getString(
    	final Object o
    ) {
        return null == o ? "" : o.toString();
    }

    public static final String getString(
    	final Date date
    ) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format( date );
    }

    public static final boolean getBoolean(
    	final Object o
    ) {
        if (null == o) {
            return false;
        }
        final String value = o.toString();
        return "on".equals( value ) || "true".equals( value ) ||"TRUE".equals( value ) || "True".equals( value );
    }

    public static final Boolean getBoolean(
    	final boolean b
    ) {
        return Boolean.valueOf(b);
    }

    public static final double getDouble(
    	final Object o
    ) {
        if (null == o) {
            return 0;
        }
        final String tmp = o.toString();
        if (tmp.trim().equals(""))
            return 0;


        Double d;
        try {
            d = Double.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
        return d.doubleValue();
    }

    public static final Double getDouble(
    	final double dbl
    ) {
        return Double.valueOf(dbl);
    }

    public static final float getFloat(
    	final Object o
    ) {
        if (null == o) {
            return 0;
        }
        final String tmp = o.toString();
        if (tmp.trim().equals("")) {
            return 0;
        }
        Float f;
        try {
            f = Float.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
        return f.floatValue();
    }

    public static final Float getFloat(
    	final float f
    ) {
        return Float.valueOf(f);
    }

    public static final int getInteger(
    	final Object o
    ) {
        if (null == o) {
            return 0;
        }
        final String tmp = o.toString();
        if (tmp.trim().equals("")) {
            return 0;
        }
        /**
         * Use double because the object may contain decimal places
         * and then it will crash.
         */
        Double i;
        try {
            i = Double.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
        return i.intValue();
    }

    public static final Integer getInteger(
    	final int i
    ) {
        return Integer.valueOf(i);
    }

    public static Date getDate(
    	final String value
    ) {
        try {
            return DateFormat.getDateInstance().parse( value );
        } catch( ParseException e ) {
            return Calendar.getInstance( ).getTime();
        }
    }

    public static java.sql.Date getSqlDate(
    	final Date date
    ) {
        return new java.sql.Date( date.getTime() );
    }
    
    private static Locale locale;
    
}