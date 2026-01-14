package com.github.matteomaspero.game.shared;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
	private ArrayList<Card> cards;

	public Deck() {
		cards = new ArrayList<>(GameConfig.DECK_SIZE);

		for (Card.Suit suit : Card.Suit.values()) {
			for (int rank = 1; rank <= 13; rank++) {
				cards.add(new Card(rank, suit));
			}
		}
	}

	public void shuffle() {
		Collections.shuffle(cards);
	}

	public Card drawCard() {
		if (cards.isEmpty()) {
			return null;
		}
		return cards.remove(cards.size() - 1);
	}

	public ArrayList<Card> getCards() {
		return cards;
	}
}
