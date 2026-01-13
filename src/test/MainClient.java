package test;

import com.github.matteomaspero.core.client.Client;

public class MainClient {
	public static void main(String[] args) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) { }

		Client client = new Client();
		client.connect();
	}
}
