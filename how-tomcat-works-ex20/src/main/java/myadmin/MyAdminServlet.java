package myadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.modeler.Registry;


public class MyAdminServlet extends HttpServlet {
	private Registry registry;
	private MBeanServer mBeanServer;

	public void init() throws ServletException {
		registry = (Registry) getServletContext().getAttribute(
				"org.apache.catalina.Registry");
		if (registry == null) {
			System.out.println("Registry not available");
			return;
		}
		mBeanServer = (MBeanServer) getServletContext().getAttribute(
				"org.apache.catalina.MBeanServer");
		if (mBeanServer == null) {
			System.out.println("MBeanServer not available");
			return;
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (registry == null || mBeanServer == null) {
			out.println("Registry or MBeanServer not found");
			return;
		}
		out.println("<html><head></head><body>");
		String action = request.getParameter("action");
		if ("listAllManagedBeans".equals(action)) {
			listAllManagedBeans(out);
		} else if ("listAllContexts".equals(action)) {
			listAllContexts(out);
		} else if ("removeContext".equals(action)) {
			String contextObjectName = request
					.getParameter("contextObjectName");
			removeContext(contextObjectName, out);
		} else {
			out.println("Invalid command");
		}
		out.println("</body></html>");
	}

	private void listAllManagedBeans(PrintWriter out) {
		String[] managedBeanNames = registry.findManagedBeans();
		for (int i = 0; i < managedBeanNames.length; i++) {
			out.print(managedBeanNames[i] + "<br/>");
		}
	}

	private void listAllContexts(PrintWriter out) {
		try {
			ObjectName objName = new ObjectName("Catalina:type=Context,*");
			Set set = mBeanServer.queryNames(objName, null);
			Iterator it = set.iterator();
			while (it.hasNext()) {
				ObjectName obj = (ObjectName) it.next();
				out.print(obj
						+ " <a href=?action=removeContext&contextObjectName="
						+ URLEncoder.encode(obj.toString(), "UTF-8")
						+ ">remove</a><br/>");
			}
		} catch (Exception e) {
			out.print(e.toString());
		}
	}

	private void removeContext(String contextObjectName, PrintWriter out) {
		try {
			ObjectName mBeanFactoryObjectName = new ObjectName(
					"Catalina:type=MBeanFactory");
			if (mBeanFactoryObjectName != null) {
				String operation = "removeContext";
				String[] params = new String[1];
				params[0] = contextObjectName;
				String signature[] = { "java.lang.String" };
				try {
					mBeanServer.invoke(mBeanFactoryObjectName, operation,
							params, signature);
					out.println("context removed");
				} catch (Exception e) {
					out.print(e.toString());
				}
			}
		} catch (Exception e) {
		}
	}
}
