package com.github.matteomaspero.game.shared;

public class Card {
	private int rank;
	private Suit suit;

	public Card(int rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public int getRank() {
		return rank;
	}

	public Suit getSuit() {
		return suit;
	}

	@Override
	public String toString() {
		switch (rank) {
			case 1:
				return "A of " + suit;
			case 11:
				return "J of " + suit;
			case 12:
				return "Q of " + suit;
			case 13:
				return "K of " + suit;
			default:
				return rank + " of " + suit;
		}
	}
	
	enum Suit {
		CLUBS,
		HEARTS,
		SPADES,
		DIAMONDS
	}
}
