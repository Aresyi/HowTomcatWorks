package ex16.pyrmont.shutdownhook;

public class ShutdownHookDemo {
	public void start() {
		System.out.println("Demo");
		ShutdownHook ShutdownHook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(ShutdownHook);
	}

	public static void main(String[] args) {
		ShutdownHookDemo demo = new ShutdownHookDemo();
		demo.start();
		try {
			System.in.read();
		}catch(Exception e) {}
	}
}

class ShutdownHook extends Thread {
	public void run() {
		System.out.println("Shutting down");
	}
}
