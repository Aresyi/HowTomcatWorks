package ex03.pyrmont.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

public class HttpRequestFacade implements HttpServletRequest {

  private HttpServletRequest request;

  public HttpRequestFacade(HttpRequest request) {
    this.request = request;
  }

  /* implementation of the HttpServletRequest*/
  public Object getAttribute(String name) {
    return request.getAttribute(name);
  }

  public Enumeration getAttributeNames() {
    return request.getAttributeNames();
  }

  public String getAuthType() {
    return request.getAuthType();
  }

  public String getCharacterEncoding() {
    return request.getCharacterEncoding();
  }

  public int getContentLength() {
    return request.getContentLength();
  }

  public String getContentType() {
    return request.getContentType();
  }

  public String getContextPath() {
    return request.getContextPath();
  }

  public Cookie[] getCookies() {
    return request.getCookies();
  }

  public long getDateHeader(String name) {
    return request.getDateHeader(name);
  }

  public Enumeration getHeaderNames() {
    return request.getHeaderNames();
  }

  public String getHeader(String name) {
    return request.getHeader(name);
  }

  public Enumeration getHeaders(String name) {
    return request.getHeaders(name);
  }

  public ServletInputStream getInputStream() throws IOException {
    return request.getInputStream();
  }

  public int getIntHeader(String name) {
    return request.getIntHeader(name);
  }

  public Locale getLocale() {
    return request.getLocale();
  }

  public Enumeration getLocales() {
    return request.getLocales();
  }

  public String getMethod() {
    return request.getMethod();
  }

  public String getParameter(String name) {
    return request.getParameter(name);
  }

  public Map getParameterMap() {
    return request.getParameterMap();
  }

  public Enumeration getParameterNames() {
    return request.getParameterNames();
  }

  public String[] getParameterValues(String name) {
    return request.getParameterValues(name);
  }

  public String getPathInfo() {
    return request.getPathInfo();
  }

  public String getPathTranslated() {
    return request.getPathTranslated();
  }

  public String getProtocol() {
    return request.getProtocol();
  }

  public String getQueryString() {
    return request.getQueryString();
  }

  public BufferedReader getReader() throws IOException {
    return request.getReader();
  }

  public String getRealPath(String path) {
    return request.getRealPath(path);
  }

  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  public String getRemoteHost() {
    return request.getRemoteHost();
  }

  public String getRemoteUser() {
    return request.getRemoteUser();
  }

  public RequestDispatcher getRequestDispatcher(String path) {
    return request.getRequestDispatcher(path);
  }

  public String getRequestedSessionId() {
    return request.getRequestedSessionId();
  }

  public String getRequestURI() {
    return request.getRequestURI();
  }

  public StringBuffer getRequestURL() {
    return request.getRequestURL();
  }

  public String getScheme() {
   return request.getScheme();
  }

  public String getServerName() {
    return request.getServerName();
  }

  public int getServerPort() {
    return request.getServerPort();
  }

  public HttpSession getSession() {
    return request.getSession();
  }

  public HttpSession getSession(boolean create) {
    return request.getSession(create);
  }

  public String getServletPath() {
    return request.getServletPath();
  }

  public Principal getUserPrincipal() {
    return request.getUserPrincipal();
  }

  public boolean isRequestedSessionIdFromCookie() {
    return request.isRequestedSessionIdFromCookie();
  }

  public boolean isRequestedSessionIdFromUrl() {
    return request.isRequestedSessionIdFromURL();
  }

  public boolean isRequestedSessionIdFromURL() {
    return request.isRequestedSessionIdFromURL();
  }

  public boolean isRequestedSessionIdValid() {
    return request.isRequestedSessionIdValid();
  }

  public boolean isSecure() {
    return request.isSecure();
  }

  public boolean isUserInRole(String role) {
    return request.isUserInRole(role);
  }

  public void removeAttribute(String attribute) {
    request.removeAttribute(attribute);
  }

  public void setAttribute(String key, Object value) {
    request.setAttribute(key, value);
  }

  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    request.setCharacterEncoding(encoding);
  }

}