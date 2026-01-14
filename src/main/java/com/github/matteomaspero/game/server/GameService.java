package com.github.matteomaspero.game.server;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;

import com.github.matteomaspero.core.server.Server;
import com.github.matteomaspero.core.server.Server.ClientHandler;
import com.github.matteomaspero.core.shared.Message;
import com.github.matteomaspero.game.shared.Card;
import com.github.matteomaspero.game.shared.Deck;
import com.github.matteomaspero.game.shared.Hand;
import com.github.matteomaspero.game.shared.GameConfig.Side;

public class GameService {
	private Server server;
	private List<ClientHandler> players;
	private Deck deck;
	private Card[] cardsOnTable;
	private Integer[] scores;

	public GameService(Server server, List<ClientHandler> players) {
		this.server = server;
		this.players = players;
		deck = new Deck();
		cardsOnTable = new Card[players.size()];
		scores = new Integer[players.size()];
	}

	public void start() {
		server.log("GameService started");
		deck.shuffle();
		dealCards();

		ClientHandler player0 = players.get(0);

		try {
			player0.send(Message.Type.PICK_SIDE, null);
			Side side = (Side) player0.receive(Message.Type.SIDE_SELECTED).getPayload();
			player0.setSide(side);
		} catch (IOException | ClassNotFoundException e) {
			server.error("Error while sending PICK_SIDE message", e);
		}
	}

	// Update game state based on player actions, acts as game loop for turns
	public void update() throws IOException, EOFException, ClassNotFoundException {
		for (ClientHandler player : players) {
			Message playedCardMessage = player.receive(Message.Type.PLAY_CARD);
			Card playedCard = (Card) playedCardMessage.getPayload();
			int playerIndex = players.indexOf(player);
			cardsOnTable[playerIndex] = playedCard;
		}


	}

	public void gameover() {

	}

	private void dealCards() {
		Hand hand1 = new Hand(deck);
		Hand hand2 = new Hand(deck);

		try {
			players.get(0).send(Message.Type.DEAL_HAND, hand1);
			players.get(1).send(Message.Type.DEAL_HAND, hand2);
		} catch (IOException e) {
			server.error("Error while dealing cards", e);
		}
	}
}
