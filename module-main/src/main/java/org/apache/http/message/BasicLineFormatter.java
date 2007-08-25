/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.message;


import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.Header;
import org.apache.http.util.CharArrayBuffer;


/**
 * Interface for formatting elements of the HEAD section of an HTTP message.
 * This is the complement to {@link LineParser}.
 * There are individual methods for formatting a request line, a
 * status line, or a header line. The formatting does <i>not</i> include the
 * trailing line break sequence CR-LF.
 * The formatted lines are returned in memory, the formatter does not depend
 * on any specific IO mechanism.
 * Instances of this interface are expected to be stateless and thread-safe.
 *
 * @author <a href="mailto:rolandw AT apache.org">Roland Weber</a>
 *
 *
 * <!-- empty lines above to avoid 'svn diff' context problems -->
 * @version $Revision$ $Date$
 *
 * @since 4.0
 */
public class BasicLineFormatter implements LineFormatter {

    /**
     * A default instance of this class, for use as default or fallback.
     * Note that {@link BasicLineFormatter} is not a singleton, there can
     * be many instances of the class itself and of derived classes.
     * The instance here provides non-customized, default behavior.
     */
    public final static BasicLineFormatter DEFAULT = new BasicLineFormatter();



    // public default constructor


    /**
     * Obtains a buffer for formatting.
     *
     * @param buffer    a buffer already available, or <code>null</code>
     *
     * @return  the cleared argument buffer if there is one, or
     *          a new empty buffer that can be used for formatting
     */
    protected CharArrayBuffer initBuffer(CharArrayBuffer buffer) {
        if (buffer != null) {
            buffer.clear();
        } else {
            buffer = new CharArrayBuffer(64);
        }
        return buffer;
    }


    /**
     * Formats a protocol version.
     *
     * @param version           the protocol version to format
     * @param formatter         the formatter to use, or
     *                          <code>null</code> for the
     *                          {@link #DEFAULT default}
     *
     * @return  the formatted protocol version
     */
    public final static
        String formatProtocolVersion(final HttpVersion version,
                                     LineFormatter formatter) {
        if (formatter == null)
            formatter = BasicLineFormatter.DEFAULT;
        return formatter.appendProtocolVersion(null, version).toString();
    }


    // non-javadoc, see interface LineFormatter
    public CharArrayBuffer appendProtocolVersion(CharArrayBuffer buffer,
                                                 HttpVersion version) {
        if (version == null) {
            throw new IllegalArgumentException
                ("Protocol version must not be null.");
        }

        // can't use initBuffer, that would clear the argument!
        CharArrayBuffer result = buffer;
        final int len = 8; // room for "HTTP/1.1"
        if (result == null) {
            result = new CharArrayBuffer(len);
        } else {
            result.ensureCapacity(len);
        }

        result.append("HTTP/"); 
        result.append(Integer.toString(version.getMajor())); 
        result.append('.'); 
        result.append(Integer.toString(version.getMinor())); 

        return result;
    }


    // non-javadoc, see interface LineFormatter
    public CharArrayBuffer formatRequestLine(CharArrayBuffer buffer,
                                             RequestLine reqline) {

        CharArrayBuffer result = initBuffer(buffer);
        BasicRequestLine.format(result, reqline); //@@@ move code here
        return result;
    }



    // non-javadoc, see interface LineFormatter
    public CharArrayBuffer formatStatusLine(CharArrayBuffer buffer,
                                            StatusLine statline) {
        CharArrayBuffer result = initBuffer(buffer);
        BasicStatusLine.format(result, statline); //@@@ move code here
        return result;
    }


    /**
     * Formats a header.
     *
     * @param header            the header to format
     * @param formatter         the formatter to use, or
     *                          <code>null</code> for the
     *                          {@link #DEFAULT default}
     *
     * @return  the formatted header
     */
    public final static String formatHeader(final Header header,
                                            LineFormatter formatter) {
        if (formatter == null)
            formatter = BasicLineFormatter.DEFAULT;
        return formatter.formatHeader(null, header).toString();
    }


    // non-javadoc, see interface LineFormatter
    public CharArrayBuffer formatHeader(CharArrayBuffer buffer,
                                        Header header) {
        if (header == null) {
            throw new IllegalArgumentException
                ("Header must not be null.");
        }
        CharArrayBuffer result = null;

        if (header instanceof BufferedHeader) {
            // If the header is backed by a buffer, re-use the buffer
            result = ((BufferedHeader)header).getBuffer();
        } else {
            result = initBuffer(buffer);
            doFormatHeader(result, header);
        }
        return result;

    } // formatHeader


    /**
     * Actually formats a header.
     * Called from {@link #formatHeader}.
     *
     * @param buffer    the empty buffer into which to format,
     *                  never <code>null</code>
     * @param header    the header to format, never <code>null</code>
     */
    protected void doFormatHeader(CharArrayBuffer buffer,
                                  Header header) {
        final String name = header.getName();
        final String value = header.getValue();

        int len = name.length() + 2;
        if (value != null) {
            len += value.length();
        }
        buffer.ensureCapacity(len);

        buffer.append(name);
        buffer.append(": ");
        if (value != null) {
            buffer.append(value);
        }
    }


} // class BasicLineFormatter
