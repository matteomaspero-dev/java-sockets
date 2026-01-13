package com.github.matteomaspero.game.server;

import java.io.EOFException;
import java.io.IOException;

import com.github.matteomaspero.core.server.Server.ClientHandler;

public class GameService {
	private ClientHandler clientHandler;

	public GameService(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	public void update() throws IOException, EOFException, ClassNotFoundException {
		// Game logic update
	}
}
