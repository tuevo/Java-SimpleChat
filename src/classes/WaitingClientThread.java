package classes;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.nio.charset.Charset;

public class WaitingClientThread extends Thread {
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(3200);
			System.out.println("Waiting for a client...");

			Vector<Socket> clients = new Vector<Socket>();
			while (true) {
				Socket client = serverSocket.accept(); // keep listening until a client will connect (sync)
				clients.add(client);

				for (Socket c : clients) {
					ForwardingThread ft = new ForwardingThread(c, clients);
					ft.start();
				}
			}

		} catch (Exception e) {
			System.err.println("SERVER: " + e.getStackTrace());
		}
	}
}
