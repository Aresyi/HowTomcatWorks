package org.apache.catalina.connector.http;


/**
 * HTTP default headers and header names.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.3 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

final class DefaultHeaders {


    // -------------------------------------------------------------- Constants


    static final char[] AUTHORIZATION_NAME = "authorization".toCharArray();
    static final char[] ACCEPT_LANGUAGE_NAME = "accept-language".toCharArray();
    static final char[] COOKIE_NAME = "cookie".toCharArray();
    static final char[] CONTENT_LENGTH_NAME = "content-length".toCharArray();
    static final char[] CONTENT_TYPE_NAME = "content-type".toCharArray();
    static final char[] HOST_NAME = "host".toCharArray();
    static final char[] CONNECTION_NAME = "connection".toCharArray();
    static final char[] CONNECTION_CLOSE_VALUE = "close".toCharArray();
    static final char[] EXPECT_NAME = "expect".toCharArray();
    static final char[] EXPECT_100_VALUE = "100-continue".toCharArray();
    static final char[] TRANSFER_ENCODING_NAME =
        "transfer-encoding".toCharArray();


    static final HttpHeader CONNECTION_CLOSE =
        new HttpHeader("connection", "close");
    static final HttpHeader EXPECT_CONTINUE =
        new HttpHeader("expect", "100-continue");
    static final HttpHeader TRANSFER_ENCODING_CHUNKED =
        new HttpHeader("transfer-encoding", "chunked");


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods


    // --------------------------------------------------------- Object Methods


}
