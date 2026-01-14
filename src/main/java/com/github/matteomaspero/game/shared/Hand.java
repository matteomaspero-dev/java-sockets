package com.github.matteomaspero.game.shared;

import java.util.ArrayList;

public class Hand {
	private ArrayList<Card> cards;

	public Hand(Deck deck) {
		cards = new ArrayList<>(GameConfig.HAND_SIZE);

		for (int i = 0; i < GameConfig.HAND_SIZE; i++) {
			cards.add(deck.drawCard());
		}
	}

	public ArrayList<Card> getCards() {
		return cards;
	}
}
