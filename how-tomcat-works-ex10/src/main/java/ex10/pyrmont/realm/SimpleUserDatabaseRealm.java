package ex10.pyrmont.realm;
// modification of org.apache.catalina.realm.UserDatabaseRealm

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.users.MemoryUserDatabase;

public class SimpleUserDatabaseRealm extends RealmBase {

  protected UserDatabase database = null;
  protected static final String name = "SimpleUserDatabaseRealm";

  protected String resourceName = "UserDatabase";

  public Principal authenticate(String username, String credentials) {
    // Does a user with this username exist?
    User user = database.findUser(username);
    if (user == null) {
      return (null);
    }

    // Do the credentials specified by the user match?
    // FIXME - Update all realms to support encoded passwords
    boolean validated = false;
    if (hasMessageDigest()) {
      // Hex hashes should be compared case-insensitive
      validated = (digest(credentials).equalsIgnoreCase(user.getPassword()));
    }
    else {
      validated = (digest(credentials).equals(user.getPassword()));
    }
    if (!validated) {
      return null;
    }

    ArrayList combined = new ArrayList();
    Iterator roles = user.getRoles();
    while (roles.hasNext()) {
      Role role = (Role) roles.next();
      String rolename = role.getRolename();
      if (!combined.contains(rolename)) {
        combined.add(rolename);
      }
    }
    Iterator groups = user.getGroups();
    while (groups.hasNext()) {
      Group group = (Group) groups.next();
      roles = group.getRoles();
      while (roles.hasNext()) {
        Role role = (Role) roles.next();
        String rolename = role.getRolename();
        if (!combined.contains(rolename)) {
          combined.add(rolename);
        }
      }
    }
    return (new GenericPrincipal(this, user.getUsername(),
      user.getPassword(), combined));
  }

  // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
  protected Principal getPrincipal(String username) {
    return (null);
  }

  protected String getPassword(String username) {
    return null;
  }

  protected String getName() {
    return this.name;
  }

  public void createDatabase(String path) {
    database = new MemoryUserDatabase(name);
    ((MemoryUserDatabase) database).setPathname(path);
    try {
      database.open();
    }
    catch (Exception e)  {
    }
  }
}