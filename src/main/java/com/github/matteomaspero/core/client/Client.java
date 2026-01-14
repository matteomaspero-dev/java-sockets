package com.github.matteomaspero.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.github.matteomaspero.core.shared.Message;
import com.github.matteomaspero.core.shared.NetworkConfig;
import com.github.matteomaspero.game.client.GameController;

/*
 * Client class
 */
public class Client {
	private Socket clientSocket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private GameController gameController;
	private String clientName;

	public Client() {
		try {
			clientSocket = new Socket(NetworkConfig.DEFAULT_HOST, NetworkConfig.DEFAULT_PORT);
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
			gameController = new GameController(this);
			clientName = null;
			
		} catch (IOException e) {
			error("Failed to create client socket on port " + NetworkConfig.DEFAULT_PORT + ". Maybe the server is handling maximum connections?", e);
		}
	}

	public void connect() {
		try {
			handshake();

			gameController.start();
			while (!clientSocket.isClosed()) {
				gameController.update();
			}
			gameController.gameover();

		} catch (IOException | ClassNotFoundException e) {
			error("Failed during handshake with server", e);
		}
	}

	public void disconnect() {
		try {
			if (clientSocket != null && !clientSocket.isClosed()) {

				clientSocket.close();
				log("Client disconnected.");
			}

		} catch (IOException e) {
			error("Failed to disconnect the client", e);
		}
	}

	public void send(Message message) throws IOException {
		out.writeObject(message);
		out.flush();
	}

	public void send(Message.Type type, Object payload) throws IOException {
		send(new Message(type, payload));
	}

	public Message receive() throws IOException, ClassNotFoundException {
		return (Message) in.readObject();
	}

	public Message receive(Message.Type expectedType) throws IOException, ClassNotFoundException {
		Message message = receive();
		if (message.getType() != expectedType) {
			disconnect();
			error("Expected " + expectedType + " message, got " + message.getType(), null);
		}
		return message;
	}

	public void log(String message) {
		System.out.println("[CLIENT] " + message);
	}

	public void error(String message, Throwable e) {
		throw new RuntimeException("[CLIENT ERROR] " + message, e);
	}

	private void handshake() throws IOException, ClassNotFoundException {
		send(Message.Type.SYN, clientName);
		clientName = (String) receive(Message.Type.SYN_ACK).getPayload();
		send(Message.Type.ACK, NetworkConfig.VERSION);
	}
}
