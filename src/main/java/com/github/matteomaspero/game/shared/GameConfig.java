package com.github.matteomaspero.game.shared;

public final class GameConfig {
	public static final int DECK_SIZE = 52;
	public static final int HAND_SIZE = 10;

	// Prevent instantiation
	public GameConfig() { }

	public enum Side {
		ODD, EVEN
	}
}
