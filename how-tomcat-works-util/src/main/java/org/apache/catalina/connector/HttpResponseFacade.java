package org.apache.catalina.connector;


import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpResponse;


/**
 * Facade class that wraps a Catalina-internal <b>HttpResponse</b>
 * object.  All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2001/09/28 16:53:49 $
 */

public final class HttpResponseFacade
    extends ResponseFacade
    implements HttpServletResponse {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public HttpResponseFacade(HttpResponse response) {
        super(response);
    }


    // -------------------------------------------- HttpServletResponse Methods


    public void addCookie(Cookie cookie) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addCookie(cookie);

    }


    public boolean containsHeader(String name) {
        return ((HttpServletResponse) response).containsHeader(name);
    }


    public String encodeURL(String url) {
        return ((HttpServletResponse) response).encodeURL(url);
    }


    public String encodeRedirectURL(String url) {
        return ((HttpServletResponse) response).encodeRedirectURL(url);
    }


    public String encodeUrl(String url) {
        return ((HttpServletResponse) response).encodeURL(url);
    }


    public String encodeRedirectUrl(String url) {
        return ((HttpServletResponse) response).encodeRedirectURL(url);
    }


    public void sendError(int sc, String msg)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendError(sc, msg);

    }


    public void sendError(int sc)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendError(sc);

    }


    public void sendRedirect(String location)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendRedirect(location);

    }


    public void setDateHeader(String name, long date) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setDateHeader(name, date);

    }


    public void addDateHeader(String name, long date) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addDateHeader(name, date);

    }


    public void setHeader(String name, String value) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setHeader(name, value);

    }


    public void addHeader(String name, String value) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addHeader(name, value);

    }


    public void setIntHeader(String name, int value) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setIntHeader(name, value);

    }


    public void addIntHeader(String name, int value) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addIntHeader(name, value);

    }


    public void setStatus(int sc) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setStatus(sc);

    }


    public void setStatus(int sc, String sm) {

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setStatus(sc, sm);

    }


}
