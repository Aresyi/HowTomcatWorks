package ex03.pyrmont.connector.http;

import ex03.pyrmont.connector.ResponseStream;
import ex03.pyrmont.connector.ResponseWriter;
import ex03.pyrmont.connector.http.Constants;

import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.util.CookieTools;

public class HttpResponse implements HttpServletResponse {

  // the default buffer size
  private static final int BUFFER_SIZE = 1024;
  HttpRequest request;
  OutputStream output;
  PrintWriter writer;
  protected byte[] buffer = new byte[BUFFER_SIZE];
  protected int bufferCount = 0;
  /**
   * Has this response been committed yet?
   */
  protected boolean committed = false;
  /**
   * The actual number of bytes written to this Response.
   */
  protected int contentCount = 0;
  /**
   * The content length associated with this Response.
   */
  protected int contentLength = -1;
  /**
   * The content type associated with this Response.
   */
  protected String contentType = null;
  /**
   * The character encoding associated with this Response.
   */
  protected String encoding = null;

  /**
   * The set of Cookies associated with this Response.
   */
  protected ArrayList cookies = new ArrayList();
  /**
   * The HTTP headers explicitly added via addHeader(), but not including
   * those to be added with setContentLength(), setContentType(), and so on.
   * This collection is keyed by the header name, and the elements are
   * ArrayLists containing the associated values that have been set.
   */
  protected HashMap headers = new HashMap();
  /**
   * The date format we will use for creating date headers.
   */
  protected final SimpleDateFormat format =
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.US);
  /**
   * The error message set by <code>sendError()</code>.
   */
  protected String message = getStatusMessage(HttpServletResponse.SC_OK);
  /**
   * The HTTP status code associated with this Response.
   */
  protected int status = HttpServletResponse.SC_OK;



  public HttpResponse(OutputStream output) {
    this.output = output;
  }

  /**
   * call this method to send headers and response to the output
   */
  public void finishResponse() {
    // sendHeaders();
    // Flush and close the appropriate output mechanism
    if (writer != null) {
      writer.flush();
      writer.close();
    }
  }

  public int getContentLength() {
    return contentLength;
  }

  public String getContentType() {
    return contentType;
  }


  protected String getProtocol() {
    return request.getProtocol();
  }

  /**
   * Returns a default status message for the specified HTTP status code.
   *
   * @param status The status code for which a message is desired
   */
  protected String getStatusMessage(int status) {
    switch (status) {
      case SC_OK:
        return ("OK");
      case SC_ACCEPTED:
        return ("Accepted");
      case SC_BAD_GATEWAY:
        return ("Bad Gateway");
      case SC_BAD_REQUEST:
        return ("Bad Request");
      case SC_CONFLICT:
        return ("Conflict");
      case SC_CONTINUE:
        return ("Continue");
      case SC_CREATED:
        return ("Created");
      case SC_EXPECTATION_FAILED:
        return ("Expectation Failed");
      case SC_FORBIDDEN:
        return ("Forbidden");
      case SC_GATEWAY_TIMEOUT:
        return ("Gateway Timeout");
      case SC_GONE:
        return ("Gone");
      case SC_HTTP_VERSION_NOT_SUPPORTED:
        return ("HTTP Version Not Supported");
      case SC_INTERNAL_SERVER_ERROR:
        return ("Internal Server Error");
      case SC_LENGTH_REQUIRED:
        return ("Length Required");
      case SC_METHOD_NOT_ALLOWED:
        return ("Method Not Allowed");
      case SC_MOVED_PERMANENTLY:
        return ("Moved Permanently");
      case SC_MOVED_TEMPORARILY:
        return ("Moved Temporarily");
      case SC_MULTIPLE_CHOICES:
        return ("Multiple Choices");
      case SC_NO_CONTENT:
        return ("No Content");
      case SC_NON_AUTHORITATIVE_INFORMATION:
        return ("Non-Authoritative Information");
      case SC_NOT_ACCEPTABLE:
        return ("Not Acceptable");
      case SC_NOT_FOUND:
        return ("Not Found");
      case SC_NOT_IMPLEMENTED:
        return ("Not Implemented");
      case SC_NOT_MODIFIED:
        return ("Not Modified");
      case SC_PARTIAL_CONTENT:
        return ("Partial Content");
      case SC_PAYMENT_REQUIRED:
        return ("Payment Required");
      case SC_PRECONDITION_FAILED:
        return ("Precondition Failed");
      case SC_PROXY_AUTHENTICATION_REQUIRED:
        return ("Proxy Authentication Required");
      case SC_REQUEST_ENTITY_TOO_LARGE:
        return ("Request Entity Too Large");
      case SC_REQUEST_TIMEOUT:
        return ("Request Timeout");
      case SC_REQUEST_URI_TOO_LONG:
        return ("Request URI Too Long");
      case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
        return ("Requested Range Not Satisfiable");
      case SC_RESET_CONTENT:
        return ("Reset Content");
      case SC_SEE_OTHER:
        return ("See Other");
      case SC_SERVICE_UNAVAILABLE:
        return ("Service Unavailable");
      case SC_SWITCHING_PROTOCOLS:
        return ("Switching Protocols");
      case SC_UNAUTHORIZED:
        return ("Unauthorized");
      case SC_UNSUPPORTED_MEDIA_TYPE:
        return ("Unsupported Media Type");
      case SC_USE_PROXY:
        return ("Use Proxy");
      case 207:       // WebDAV
        return ("Multi-Status");
      case 422:       // WebDAV
        return ("Unprocessable Entity");
      case 423:       // WebDAV
        return ("Locked");
      case 507:       // WebDAV
        return ("Insufficient Storage");
      default:
        return ("HTTP Response Status " + status);
    }
  }

  public OutputStream getStream() {
    return this.output;
  }
  /**
   * Send the HTTP response headers, if this has not already occurred.
   */
  protected void sendHeaders() throws IOException {
    if (isCommitted())
      return;
    // Prepare a suitable output writer
    OutputStreamWriter osr = null;
    try {
      osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
    }
    catch (UnsupportedEncodingException e) {
      osr = new OutputStreamWriter(getStream());
    }
    final PrintWriter outputWriter = new PrintWriter(osr);
    // Send the "Status:" header
    outputWriter.print(this.getProtocol());
    outputWriter.print(" ");
    outputWriter.print(status);
    if (message != null) {
      outputWriter.print(" ");
      outputWriter.print(message);
    }
    outputWriter.print("\r\n");
    // Send the content-length and content-type headers (if any)
    if (getContentType() != null) {
      outputWriter.print("Content-Type: " + getContentType() + "\r\n");
    }
    if (getContentLength() >= 0) {
      outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
    }
    // Send all specified headers (if any)
    synchronized (headers) {
      Iterator names = headers.keySet().iterator();
      while (names.hasNext()) {
        String name = (String) names.next();
        ArrayList values = (ArrayList) headers.get(name);
        Iterator items = values.iterator();
        while (items.hasNext()) {
          String value = (String) items.next();
          outputWriter.print(name);
          outputWriter.print(": ");
          outputWriter.print(value);
          outputWriter.print("\r\n");
        }
      }
    }
    // Add the session ID cookie if necessary
/*    HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
    HttpSession session = hreq.getSession(false);
    if ((session != null) && session.isNew() && (getContext() != null)
      && getContext().getCookies()) {
      Cookie cookie = new Cookie("JSESSIONID", session.getId());
      cookie.setMaxAge(-1);
      String contextPath = null;
      if (context != null)
        contextPath = context.getPath();
      if ((contextPath != null) && (contextPath.length() > 0))
        cookie.setPath(contextPath);
      else

      cookie.setPath("/");
      if (hreq.isSecure())
        cookie.setSecure(true);
      addCookie(cookie);
    }
*/
    // Send all specified cookies (if any)
    synchronized (cookies) {
      Iterator items = cookies.iterator();
      while (items.hasNext()) {
        Cookie cookie = (Cookie) items.next();
        outputWriter.print(CookieTools.getCookieHeaderName(cookie));
        outputWriter.print(": ");
        outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
        outputWriter.print("\r\n");
      }
    }

    // Send a terminating blank line to mark the end of the headers
    outputWriter.print("\r\n");
    outputWriter.flush();

    committed = true;
  }

  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  /* This method is used to serve a static page */
  public void sendStaticResource() throws IOException {
    byte[] bytes = new byte[BUFFER_SIZE];
    FileInputStream fis = null;
    try {
      /* request.getUri has been replaced by request.getRequestURI */
      File file = new File(Constants.WEB_ROOT, request.getRequestURI());
      fis = new FileInputStream(file);
      /*
         HTTP Response = Status-Line
           *(( general-header | response-header | entity-header ) CRLF)
           CRLF
           [ message-body ]
         Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
      */
      int ch = fis.read(bytes, 0, BUFFER_SIZE);
      while (ch!=-1) {
        output.write(bytes, 0, ch);
        ch = fis.read(bytes, 0, BUFFER_SIZE);
      }
    }
    catch (FileNotFoundException e) {
      String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
        "Content-Type: text/html\r\n" +
        "Content-Length: 23\r\n" +
        "\r\n" +
        "<h1>File Not Found</h1>";
      output.write(errorMessage.getBytes());
    }
    finally {
      if (fis!=null)
        fis.close();
    }
  }

  public void write(int b) throws IOException {
    if (bufferCount >= buffer.length)
      flushBuffer();
    buffer[bufferCount++] = (byte) b;
    contentCount++;
  }

  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte b[], int off, int len) throws IOException {
    // If the whole thing fits in the buffer, just put it there
    if (len == 0)
      return;
    if (len <= (buffer.length - bufferCount)) {
      System.arraycopy(b, off, buffer, bufferCount, len);
      bufferCount += len;
      contentCount += len;
      return;
    }

    // Flush the buffer and start writing full-buffer-size chunks
    flushBuffer();
    int iterations = len / buffer.length;
    int leftoverStart = iterations * buffer.length;
    int leftoverLen = len - leftoverStart;
    for (int i = 0; i < iterations; i++)
      write(b, off + (i * buffer.length), buffer.length);

    // Write the remainder (guaranteed to fit in the buffer)
    if (leftoverLen > 0)
      write(b, off + leftoverStart, leftoverLen);
  }

  /** implementation of HttpServletResponse  */

  public void addCookie(Cookie cookie) {
    if (isCommitted())
      return;
  //  if (included)
    //        return;     // Ignore any call from an included servlet
    synchronized (cookies) {
      cookies.add(cookie);
    }
  }

  public void addDateHeader(String name, long value) {
    if (isCommitted())
      return;
//    if (included)
  //          return;     // Ignore any call from an included servlet
    addHeader(name, format.format(new Date(value)));
  }

  public void addHeader(String name, String value) {
    if (isCommitted())
      return;
//        if (included)
  //          return;     // Ignore any call from an included servlet
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values == null) {
        values = new ArrayList();
        headers.put(name, values);
      }

      values.add(value);
    }
  }

  public void addIntHeader(String name, int value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
    addHeader(name, "" + value);
  }

  public boolean containsHeader(String name) {
    synchronized (headers) {
      return (headers.get(name)!=null);
    }
  }

  public String encodeRedirectURL(String url) {
    return null;
  }

  public String encodeRedirectUrl(String url) {
    return encodeRedirectURL(url);
  }

  public String encodeUrl(String url) {
    return encodeURL(url);
  }

  public String encodeURL(String url) {
    return null;
  }

  public void flushBuffer() throws IOException {
    //committed = true;
    if (bufferCount > 0) {
      try {
        output.write(buffer, 0, bufferCount);
      }
      finally {
        bufferCount = 0;
      }
    }
  }

  public int getBufferSize() {
    return 0;
  }

  public String getCharacterEncoding() {
    if (encoding == null)
      return ("ISO-8859-1");
    else
      return (encoding);
  }

  public Locale getLocale() {
    return null;
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return null;
  }

  public PrintWriter getWriter() throws IOException {
    ResponseStream newStream = new ResponseStream(this);
    newStream.setCommit(false);
    OutputStreamWriter osr =
      new OutputStreamWriter(newStream, getCharacterEncoding());
    writer = new ResponseWriter(osr);
    return writer;
  }

  /**
   * Has the output of this response already been committed?
   */
  public boolean isCommitted() {
    return (committed);
  }

  public void reset() {
  }

  public void resetBuffer() {
  }

  public void sendError(int sc) throws IOException {
  }

  public void sendError(int sc, String message) throws IOException {
  }

  public void sendRedirect(String location) throws IOException {
  }

  public void setBufferSize(int size) {
  }

  public void setContentLength(int length) {
    if (isCommitted())
      return;
//    if (included)
  //     return;     // Ignore any call from an included servlet
    this.contentLength = length;
  }

  public void setContentType(String type) {
  }

  public void setDateHeader(String name, long value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
    setHeader(name, format.format(new Date(value)));
  }

  public void setHeader(String name, String value) {
    if (isCommitted())
      return;
//    if (included)
  //    return;     // Ignore any call from an included servlet
    ArrayList values = new ArrayList();
    values.add(value);
    synchronized (headers) {
      headers.put(name, values);
    }
    String match = name.toLowerCase();
    if (match.equals("content-length")) {
      int contentLength = -1;
      try {
        contentLength = Integer.parseInt(value);
      }
      catch (NumberFormatException e) {
        ;
      }
      if (contentLength >= 0)
        setContentLength(contentLength);
    }
    else if (match.equals("content-type")) {
      setContentType(value);
    }
  }

  public void setIntHeader(String name, int value) {
    if (isCommitted())
      return;
    //if (included)
      //return;     // Ignore any call from an included servlet
    setHeader(name, "" + value);
  }

  public void setLocale(Locale locale) {
    if (isCommitted())
      return;
    //if (included)
      //return;     // Ignore any call from an included servlet

   // super.setLocale(locale);
    String language = locale.getLanguage();
    if ((language != null) && (language.length() > 0)) {
      String country = locale.getCountry();
      StringBuffer value = new StringBuffer(language);
      if ((country != null) && (country.length() > 0)) {
        value.append('-');
        value.append(country);
      }
      setHeader("Content-Language", value.toString());
    }
  }

  public void setStatus(int sc) {
  }

  public void setStatus(int sc, String message) {
  }
}
