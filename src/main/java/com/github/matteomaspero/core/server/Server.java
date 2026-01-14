package com.github.matteomaspero.core.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.matteomaspero.core.shared.Message;
import com.github.matteomaspero.core.shared.NetworkConfig;
import com.github.matteomaspero.game.server.GameService;
import com.github.matteomaspero.game.shared.GameConfig;
import com.github.matteomaspero.game.shared.GameConfig.Side;

/*
 * Server class
 */
public class Server {
	private ServerSocket serverSocket;
	private List<ClientHandler> connectedClients;
	private GameService gameService;

	public Server() {
		try {
			serverSocket = new ServerSocket(NetworkConfig.DEFAULT_PORT);
			connectedClients = Collections.synchronizedList(new ArrayList<>(NetworkConfig.MAX_CONNECTIONS));
			gameService = new GameService(this, connectedClients);

		} catch (IOException e) {
			error("Failed to create server socket on port " + NetworkConfig.DEFAULT_PORT, e);
		}
	}

	public void start() {
		try {
			log("Server started on port " + NetworkConfig.DEFAULT_PORT);

			while (!serverSocket.isClosed()) {
				Socket clientSocket = serverSocket.accept();
				if (connectedClients.size() >= NetworkConfig.MAX_CONNECTIONS) {
					log("Connection refused: maximum connections reached.");
					clientSocket.close();
					continue;
				}

				ClientHandler clientHandler = new ClientHandler(clientSocket);
				connectedClients.add(clientHandler);
				clientHandler.start();
			}

		} catch (IOException e) {
			error("Error while running the server", e);
		}
	}

	public void stop() {
		try {
			for (ClientHandler clientHandler : connectedClients) {
				clientHandler.terminate();
			}
			serverSocket.close();
			log("Server stopped.");

		} catch (IOException e) {
			error("Failed to stop the server", e);
		}
	}

	public List<ClientHandler> getConnectedClients() {
		return connectedClients;
	}

	public void log(String message) {
		System.out.println("[SERVER] " + message);
	}

	public void error(String message, Throwable e) {
		throw new RuntimeException("[SERVER ERROR] " + message, e);
	}

	/*
	 * ClientHandler class
	 */
	public class ClientHandler extends Thread {
		private Socket clientSocket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private String clientName;
		private Side side;

		public ClientHandler(Socket clientSocket) {
			try {
				this.clientSocket = clientSocket;
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(clientSocket.getInputStream());
				clientName = "Anonymous:" + clientSocket.getPort();

			} catch (IOException e) {
				error("Failed to create client handler", e);
			}
		}

		@Override
		public void run() {
			try {
				handshake();
				log("Player " + clientName + " connected.");

				gameService.start();
				while (!clientSocket.isClosed()) {
					gameService.update();
				}
				gameService.gameover();

			} catch (EOFException e) {
				log("Client " + clientName + " disconnected.");
			} catch (IOException | ClassNotFoundException e) {
				if (!clientSocket.isClosed()) {
					error("Error in client handler for " + clientName, e);
				}
			} finally {
				terminate();
			}
		}

		public void terminate() {
			try {
				connectedClients.remove(this);
				if (!clientSocket.isClosed()) {
					clientSocket.close();
				}
				log("Client handler terminated for " + clientName);

			} catch (IOException e) {
				error("Failed to terminate client connection", e);
			}
		}

		public void send(Message message) throws IOException {
			out.writeObject(message);
			out.flush();
		}

		public void send(Message.Type type, Object payload) throws IOException {
			send(new Message(type, payload));
		}

		public void broadcast(Message.Type type, Object payload) throws IOException {
			for (ClientHandler clientHandler : connectedClients) {
				clientHandler.send(type, payload);
			}
		}

		public Message receive() throws IOException, ClassNotFoundException {
			return (Message) in.readObject();
		}

		public Message receive(Message.Type expectedType) throws IOException, ClassNotFoundException {
			Message message = receive();
			if (message.getType() != expectedType) {
				terminate();
				error("Expected " + expectedType + " message, got " + message.getType(), null);
			}
			return message;
		}

		public String getClientName() {
			return clientName;
		}

		public Side getSide() {
			return side;
		}

		public void setSide(Side side) {
			this.side = side;
		}

		private void handshake() throws IOException, ClassNotFoundException {
			Message syn = receive(Message.Type.SYN);
			clientName = (String) syn.getPayload();
			sanitizeClientName();

			send(Message.Type.SYN_ACK, clientName);

			Message ack = receive(Message.Type.ACK);
			String clientVersion = (String) ack.getPayload();
			verifyClientVersion(clientVersion);
		}

		private void sanitizeClientName() {
			if (clientName == null) {
				clientName = "";
			}

			clientName = clientName.toUpperCase().replaceAll("[^A-Z0-9]", "");
			if (clientName.isEmpty()) {
				clientName = "ANONYMOUS" + clientSocket.getPort();
			}
		}

		private void verifyClientVersion(String clientVersion) {
			if (!NetworkConfig.VERSION.equals(clientVersion)) {
				terminate();
				error("Client " + clientName + " has incompatible version: " + clientVersion, null);
			}
		}
	}
}
