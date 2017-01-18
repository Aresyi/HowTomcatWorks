package ex07.pyrmont.core;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.naming.directory.DirContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Mapper;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.LifecycleSupport;

public class SimpleWrapper implements Wrapper, Pipeline, Lifecycle {

  public SimpleWrapper() {
    pipeline.setBasic(new SimpleWrapperValve());
  }

  // the servlet instance
  private Servlet instance = null;
  private String servletClass;
  private Loader loader;
  private String name;
  protected LifecycleSupport lifecycle = new LifecycleSupport(this);
  private SimplePipeline pipeline = new SimplePipeline(this);
  protected Container parent = null;
  protected boolean started = false;

  public synchronized void addValve(Valve valve) {
    pipeline.addValve(valve);
  }

  public Servlet allocate() throws ServletException {
    // Load and initialize our instance if necessary
    if (instance==null) {
      try {
        instance = loadServlet();
      }
      catch (ServletException e) {
        throw e;
      }
      catch (Throwable e) {
        throw new ServletException("Cannot allocate a servlet instance", e);
      }
    }
    return instance;
  }

  public Servlet loadServlet() throws ServletException {
    if (instance!=null)
      return instance;

    Servlet servlet = null;
    String actualClass = servletClass;
    if (actualClass == null) {
      throw new ServletException("servlet class has not been specified");
    }

    Loader loader = getLoader();
    // Acquire an instance of the class loader to be used
    if (loader==null) {
      throw new ServletException("No loader.");
    }
    ClassLoader classLoader = loader.getClassLoader();

    // Load the specified servlet class from the appropriate class loader
    Class classClass = null;
    try {
      if (classLoader!=null) {
        classClass = classLoader.loadClass(actualClass);
      }
    }
    catch (ClassNotFoundException e) {
      throw new ServletException("Servlet class not found");
    }
    // Instantiate and initialize an instance of the servlet class itself
    try {
      servlet = (Servlet) classClass.newInstance();
    }
    catch (Throwable e) {
      throw new ServletException("Failed to instantiate servlet");
    }

    // Call the initialization method of this servlet
    try {
      servlet.init(null);
    }
    catch (Throwable f) {
      throw new ServletException("Failed initialize servlet.");
    }
    return servlet;
  }

  public String getInfo() {
    return null;
  }

  public Loader getLoader() {
    if (loader != null)
      return (loader);
    if (parent != null)
      return (parent.getLoader());
    return (null);
  }

  public void setLoader(Loader loader) {
    this.loader = loader;
  }

  public Logger getLogger() {
    return null;
  }

  public void setLogger(Logger logger) {
  }

  public Manager getManager() {
    return null;
  }

  public void setManager(Manager manager) {
  }

  public Cluster getCluster() {
    return null;
  }

  public void setCluster(Cluster cluster) {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Container getParent() {
    return parent;
  }

  public void setParent(Container container) {
    parent = container;
  }

  public ClassLoader getParentClassLoader() {
    return null;
  }

  public void setParentClassLoader(ClassLoader parent) {
  }

  public Realm getRealm() {
    return null;
  }

  public void setRealm(Realm realm) {
  }

  public DirContext getResources() {
    return null;
  }

  public void setResources(DirContext resources) {
  }

  public long getAvailable() {
    return 0;
  }

  public void setAvailable(long available) {
  }

  public String getJspFile() {
    return null;
  }

  public void setJspFile(String jspFile) {
  }

  public int getLoadOnStartup() {
   return 0;
  }

  public void setLoadOnStartup(int value) {
  }

  public String getRunAs() {
    return null;
  }

  public void setRunAs(String runAs) {
  }

  public String getServletClass() {
    return null;
  }

  public void setServletClass(String servletClass) {
    this.servletClass = servletClass;
  }

  public void addChild(Container child) {
  }

  public void addContainerListener(ContainerListener listener) {
  }

  public void addMapper(Mapper mapper) {
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public Container findChild(String name) {
    return null;
  }

  public Container[] findChildren() {
    return null;
  }

  public ContainerListener[] findContainerListeners() {
    return null;
  }

  public void addInitParameter(String name, String value) {
  }

  public void addInstanceListener(InstanceListener listener) {
  }

  public void addSecurityReference(String name, String link) {
  }

  public void deallocate(Servlet servlet) throws ServletException {
  }

  public String findInitParameter(String name) {
    return null;
  }

  public String[] findInitParameters() {
    return null;
  }

  public String findSecurityReference(String name) {
    return null;
  }

  public String[] findSecurityReferences() {
    return null;
  }

  public Mapper findMapper(String protocol) {
    return null;
  }

  public Mapper[] findMappers() {
    return null;
  }

  public void invoke(Request request, Response response)
    throws IOException, ServletException {
    pipeline.invoke(request, response);
  }

  public boolean isUnavailable() {
    return false;
  }

  public void load() throws ServletException {
  }

  public Container map(Request request, boolean update) {
    return null;
  }

  public void removeChild(Container child) {
  }

  public void removeContainerListener(ContainerListener listener) {
  }

  public void removeMapper(Mapper mapper) {
  }

  public void removeInitParameter(String name) {
  }

  public void removeInstanceListener(InstanceListener listener) {
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

  public void removeSecurityReference(String name) {
  }

  public void unavailable(UnavailableException unavailable) {
  }

  public void unload() throws ServletException {
  }

  // method implementations of Pipeline
  public Valve getBasic() {
    return pipeline.getBasic();
  }

  public void setBasic(Valve valve) {
    pipeline.setBasic(valve);
  }

  public Valve[] getValves() {
    return pipeline.getValves();
  }

  public void removeValve(Valve valve) {
    pipeline.removeValve(valve);
  }

  // implementation of the Lifecycle interface's methods
  public void addLifecycleListener(LifecycleListener listener) {
  }

  public LifecycleListener[] findLifecycleListeners() {
    return null;
  }

  public void removeLifecycleListener(LifecycleListener listener) {
  }

  public synchronized void start() throws LifecycleException {
    System.out.println("Starting Wrapper " + name);
    if (started)
      throw new LifecycleException("Wrapper already started");

    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);
    started = true;

    // Start our subordinate components, if any
    if ((loader != null) && (loader instanceof Lifecycle))
      ((Lifecycle) loader).start();

    // Start the Valves in our pipeline (including the basic), if any
    if (pipeline instanceof Lifecycle)
      ((Lifecycle) pipeline).start();

    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(START_EVENT, null);
    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);
  }

  public void stop() throws LifecycleException {
    System.out.println("Stopping wrapper " + name);
    // Shut down our servlet instance (if it has been initialized)
    try {
      instance.destroy();
    }
    catch (Throwable t) {
    }
    instance = null;
    if (!started)
      throw new LifecycleException("Wrapper " + name + " not started");
    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(STOP_EVENT, null);
    started = false;

    // Stop the Valves in our pipeline (including the basic), if any
    if (pipeline instanceof Lifecycle) {
      ((Lifecycle) pipeline).stop();
    }

    // Stop our subordinate components, if any
    if ((loader != null) && (loader instanceof Lifecycle)) {
      ((Lifecycle) loader).stop();
    }

    // Notify our interested LifecycleListeners
    lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);
  }
}