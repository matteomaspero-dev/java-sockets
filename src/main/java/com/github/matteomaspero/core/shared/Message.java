package com.github.matteomaspero.core.shared;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = NetworkConfig.SERIAL_VERSION_UID;
	private final Type type;
	private final Object payload;

	public Message(Type type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	public Type getType() {
		return type;
	}

	public Object getPayload() {
		return payload;
	}

	public enum Type {
		SYN, SYN_ACK, ACK,
		PICK_SIDE, SIDE_SELECTED,
		DEAL_HAND, PLAY_CARD, GAME_OVER
	}
}
