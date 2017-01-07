package org.how.tomcat.works.ex02;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

public class RequestFacade implements ServletRequest {

  private ServletRequest request = null;

  public RequestFacade(Request request) {
    this.request = request;
  }

  /* implementation of the ServletRequest*/
  public Object getAttribute(String attribute) {
    return request.getAttribute(attribute);
  }

  public Enumeration getAttributeNames() {
    return request.getAttributeNames();
  }

  public String getRealPath(String path) {
    return request.getRealPath(path);
  }

  public RequestDispatcher getRequestDispatcher(String path) {
    return request.getRequestDispatcher(path);
  }

  public boolean isSecure() {
    return request.isSecure();
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

  public ServletInputStream getInputStream() throws IOException {
    return request.getInputStream();
  }

  public Locale getLocale() {
    return request.getLocale();
  }

  public Enumeration getLocales() {
    return request.getLocales();
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

  public String[] getParameterValues(String parameter) {
    return request.getParameterValues(parameter);
  }

  public String getProtocol() {
    return request.getProtocol();
  }

  public BufferedReader getReader() throws IOException {
    return request.getReader();
  }

  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  public String getRemoteHost() {
    return request.getRemoteHost();
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

  public void removeAttribute(String attribute) {
    request.removeAttribute(attribute);
  }

  public void setAttribute(String key, Object value) {
    request.setAttribute(key, value);
  }

  public void setCharacterEncoding(String encoding)
    throws UnsupportedEncodingException {
    request.setCharacterEncoding(encoding);
  }

}