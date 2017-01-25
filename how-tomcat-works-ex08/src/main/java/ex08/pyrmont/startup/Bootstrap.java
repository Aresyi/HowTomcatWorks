package ex08.pyrmont.startup;

import ex08.pyrmont.core.SimpleWrapper;
import ex08.pyrmont.core.SimpleContextConfig;
import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.http.HttpConnector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.naming.resources.ProxyDirContext;

public final class Bootstrap {
  public static void main(String[] args) {

    //invoke: http://localhost:8080/Modern or  http://localhost:8080/Primitive

    System.setProperty("catalina.base", System.getProperty("user.dir"));
    Connector connector = new HttpConnector();
    Wrapper wrapper1 = new SimpleWrapper();
    wrapper1.setName("Primitive");
    wrapper1.setServletClass("PrimitiveServlet");
    Wrapper wrapper2 = new SimpleWrapper();
    wrapper2.setName("Modern");
    wrapper2.setServletClass("ModernServlet");

    Context context = new StandardContext();
    // StandardContext's start method adds a default mapper
    context.setPath("/myApp");
    context.setDocBase("myApp");

    context.addChild(wrapper1);
    context.addChild(wrapper2);

    // context.addServletMapping(pattern, name);
    context.addServletMapping("/Primitive", "Primitive");
    context.addServletMapping("/Modern", "Modern");
    // add ContextConfig. This listener is important because it configures
    // StandardContext (sets configured to true), otherwise StandardContext
    // won't start
    LifecycleListener listener = new SimpleContextConfig();
    ((Lifecycle) context).addLifecycleListener(listener);

    // here is our loader
    Loader loader = new WebappLoader();
    // associate the loader with the Context
    context.setLoader(loader);

    connector.setContainer(context);

    try {
      connector.initialize();
      ((Lifecycle) connector).start();
      ((Lifecycle) context).start();
      // now we want to know some details about WebappLoader
      WebappClassLoader classLoader = (WebappClassLoader) loader.getClassLoader();
      System.out.println("Resources' docBase: " + ((ProxyDirContext)classLoader.getResources()).getDocBase());
      String[] repositories = classLoader.findRepositories();
      for (int i=0; i<repositories.length; i++) {
        System.out.println("  repository: " + repositories[i]);
      }

      // make the application wait until we press a key.
      System.in.read();
      ((Lifecycle) context).stop();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}