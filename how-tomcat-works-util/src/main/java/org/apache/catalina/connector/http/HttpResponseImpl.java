package org.apache.catalina.connector.http;


import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.HttpResponseBase;


/**
 * Implementation of <b>HttpResponse</b> specific to the HTTP connector.
 *
 * @author Craig R. McClanahan
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.13 $ $Date: 2002/03/18 07:15:40 $
 * @deprecated
 */

final class HttpResponseImpl
    extends HttpResponseBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * Descriptive information about this Response implementation.
     */
    protected static final String info =
        "org.apache.catalina.connector.http.HttpResponseImpl/1.0";


    /**
     * True if chunking is allowed.
     */
    protected boolean allowChunking;


    /**
     * Associated HTTP response stream.
     */
    protected HttpResponseStream responseStream;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Response implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Set the chunking flag.
     */
    void setAllowChunking(boolean allowChunking) {
        this.allowChunking = allowChunking;
    }


    /**
     * True if chunking is allowed.
     */
    public boolean isChunkingAllowed() {
        return allowChunking;
    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Return the HTTP protocol version implemented by this response
     * object.
     *
     * @return The &quot;HTTP/1.1&quot; string.
     */
    protected String getProtocol() {
        return("HTTP/1.1");
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        super.recycle();
        responseStream = null;
        allowChunking = false;

    }


    /**
     * Send an error response with the specified status and message.
     *
     * @param status HTTP status code to send
     * @param message Corresponding message to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status, String message) throws IOException {

        addHeader("Connection", "close");
        super.sendError(status, message);

    }


    /**
     * Clear any content written to the buffer.  In addition, all cookies
     * and headers are cleared, and the status is reset.
     *
     * @exception IllegalStateException if this response has already
     *  been committed
     */
    public void reset() {

        // Saving important HTTP/1.1 specific headers
        String connectionValue =
            (String) getHeader("Connection");
        String transferEncodingValue =
            (String) getHeader("Transfer-Encoding");
        super.reset();
        if (connectionValue != null)
            addHeader("Connection", connectionValue);
        if (transferEncodingValue != null)
            addHeader("Transfer-Encoding", transferEncodingValue);

    }


    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream createOutputStream() throws IOException {

        responseStream = new HttpResponseStream(this);
        return (responseStream);

    }


    /**
     * Tests is the connection will be closed after the processing of the
     * request.
     */
    public boolean isCloseConnection() {
        String connectionValue = (String) getHeader("Connection");
        return (connectionValue != null
                && connectionValue.equals("close"));
    }


    /**
     * Removes the specified header.
     *
     * @param name Name of the header to remove
     * @param value Value to remove
     */
    public void removeHeader(String name, String value) {

        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if ((values != null) && (!values.isEmpty())) {
                values.remove(value);
                if (values.isEmpty())
                    headers.remove(name);
            }
        }

    }


    /**
     * Has stream been created ?
     */
    public boolean isStreamInitialized() {
        return (responseStream != null);
    }


    /**
     * Perform whatever actions are required to flush and close the output
     * stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishResponse() throws IOException {

        if (getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
            if ((!isStreamInitialized()) && (getContentLength() == -1)
                && (getStatus() >= 200)
                && (getStatus() != SC_NOT_MODIFIED)
                && (getStatus() != SC_NO_CONTENT))
                setContentLength(0);
        } else {
            setHeader("Connection", "close");
        }
        super.finishResponse();

    }


    // -------------------------------------------- HttpServletResponse Methods


    /**
     * Set the HTTP status to be returned with this response.
     *
     * @param status The new HTTP status
     */
    public void setStatus(int status) {

        super.setStatus(status);

        if (responseStream != null)
            responseStream.checkChunking(this);

    }


    /**
     * Set the content length (in bytes) for this Response.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {

        if (isCommitted())
            return;

        if (included)
            return;     // Ignore any call from an included servlet

        super.setContentLength(length);

        if (responseStream != null)
            responseStream.checkChunking(this);

    }


}
