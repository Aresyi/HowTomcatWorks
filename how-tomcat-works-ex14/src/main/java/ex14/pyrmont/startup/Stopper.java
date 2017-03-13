package ex14.pyrmont.startup;

import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class Stopper {

  public static void main(String[] args) {
    // the following code is taken from the Stop method of
    // the org.apache.catalina.startup.Catalina class
    int port = 8085;
    try {
      Socket socket = new Socket("127.0.0.1", port);
      OutputStream stream = socket.getOutputStream();
      String shutdown = "SHUTDOWN";
      for (int i = 0; i < shutdown.length(); i++)
        stream.write(shutdown.charAt(i));
      stream.flush();
      stream.close();
      socket.close();
      System.out.println("The server was successfully shut down.");
    }
    catch (IOException e) {
      System.out.println("Error. The server has not been started.");
    }
  }
}