package com.github.xsavikx.rabbit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RabbitTest {
	private Rabbit rabbit;
	private String message;
	private String key;
	private String IV;
	private boolean addPadding;
	private boolean trimPadding;

	@Before
	public void setUp() throws Exception {
		rabbit = new Rabbit();
		message = "HelloWorld!";
		key = "this_is_key";
		IV = "ivivivivivivivivivivivivivi";
		addPadding = true;
		trimPadding = true;
	}

	@Test
	public void testCryptMessage() {
		final byte[] encryptedMessage = rabbit.encryptMessage(message, key, IV,
				addPadding);
		final String decryptedString = rabbit.decryptMessage(encryptedMessage,
				key, IV, trimPadding);
		assertEquals(message, decryptedString);
	}

}
