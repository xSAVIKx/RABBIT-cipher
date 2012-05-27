package cs.cypher;

import java.util.Arrays;

/**
 * Tested against the actual RFC.
 * @author Chase (Robert Maupin)
 * @see {@link http://tools.ietf.org/rfc/rfc4503.txt}
 */
public class Rabbit {
	private static final int rotl(int value, int shift) {
		return (value << shift) | (value >>> (32 - shift));
	}

	private static final int[] A = new int[] { 0x4D34D34D, 0xD34D34D3,
			0x34D34D34, 0x4D34D34D, 0xD34D34D3, 0x34D34D34, 0x4D34D34D,
			0xD34D34D3 };
	private int[] X = new int[8];
	private int[] C = new int[8];
	private byte b;

	public void crypt(byte[] message) {

		int index = 0;
		while (index < message.length) {
			byte[] crypt = keyStream();

			for (int i = 0; i < 16 && index < message.length; ++i) {
				message[index++] ^= crypt[i];
			}
		}
		/*
		 * Yes, I know it discards the rest of the keystream, it's on my todo
		 * list.
		 */
	}

	/**
	 * returns 16 bytes
	 */
	private byte[] keyStream() {
		nextState();

		byte[] S = new byte[16];

		int s = X[0] ^ (X[5] >>> 16) ^ (X[3] << 16);

		S[15] = (byte) (s);
		S[14] = (byte) (s >> 8);
		S[13] = (byte) (s >> 16);
		S[12] = (byte) (s >> 24);

		s = X[2] ^ (X[7] >>> 16) ^ (X[5] << 16);

		S[11] = (byte) (s);
		S[10] = (byte) (s >> 8);
		S[9] = (byte) (s >> 16);
		S[8] = (byte) (s >> 24);

		s = X[4] ^ (X[1] >>> 16) ^ (X[7] << 16);

		S[7] = (byte) (s);
		S[6] = (byte) (s >> 8);
		S[5] = (byte) (s >> 16);
		S[4] = (byte) (s >> 24);

		s = X[6] ^ (X[3] >>> 16) ^ (X[1] << 16);

		S[3] = (byte) (s);
		S[2] = (byte) (s >> 8);
		S[1] = (byte) (s >> 16);
		S[0] = (byte) (s >> 24);

		return S;
	}

	public Rabbit() {
		b = 0;
	}

	/**
	 * @param K
	 *            array of 8 short values
	 */
	public void setupKey(short[] K) {
		//some unrolling
		X[0] = (K[1] << 16) | (K[0] & 0xFFFF);
		X[1] = (K[6] << 16) | (K[5] & 0xFFFF);
		X[2] = (K[3] << 16) | (K[2] & 0xFFFF);
		X[3] = (K[0] << 16) | (K[7] & 0xFFFF);
		X[4] = (K[5] << 16) | (K[4] & 0xFFFF);
		X[5] = (K[2] << 16) | (K[1] & 0xFFFF);
		X[6] = (K[7] << 16) | (K[6] & 0xFFFF);
		X[7] = (K[4] << 16) | (K[3] & 0xFFFF);

		C[0] = (K[4] << 16) | (K[5] & 0xFFFF);
		C[1] = (K[1] << 16) | (K[2] & 0xFFFF);
		C[2] = (K[6] << 16) | (K[7] & 0xFFFF);
		C[3] = (K[3] << 16) | (K[4] & 0xFFFF);
		C[4] = (K[0] << 16) | (K[1] & 0xFFFF);
		C[5] = (K[5] << 16) | (K[6] & 0xFFFF);
		C[6] = (K[2] << 16) | (K[3] & 0xFFFF);
		C[7] = (K[7] << 16) | (K[0] & 0xFFFF);

		nextState();
		nextState();
		nextState();
		nextState();

		//unroll
		C[0] ^= X[4];
		C[1] ^= X[5];
		C[2] ^= X[6];
		C[3] ^= X[7];
		C[4] ^= X[0];
		C[5] ^= X[1];
		C[6] ^= X[2];
		C[7] ^= X[3];
	}

	/**
	 * @param IV
	 *            array of 4 short values
	 */
	public void setupIV(short[] IV) {
		C[0] ^= (IV[1] << 16) | (IV[0] & 0xFFFF);
		C[1] ^= (IV[3] << 16) | (IV[1] & 0xFFFF);
		C[2] ^= (IV[3] << 16) | (IV[2] & 0xFFFF);
		C[3] ^= (IV[2] << 16) | (IV[0] & 0xFFFF);
		C[4] ^= (IV[1] << 16) | (IV[0] & 0xFFFF);
		C[5] ^= (IV[3] << 16) | (IV[1] & 0xFFFF);
		C[6] ^= (IV[3] << 16) | (IV[2] & 0xFFFF);
		C[7] ^= (IV[2] << 16) | (IV[0] & 0xFFFF);

		nextState();
		nextState();
		nextState();
		nextState();
	}

	/**
	 * Clears all internal data.
	 */
	public void reset() {
		b = 0;
		Arrays.fill(X, 0);
		Arrays.fill(C, 0);
	}

	private void nextState() {
		// counter update
		for (int j = 0; j < 8; ++j) {
			long t = (C[j] & 0xFFFFFFFFL) + (A[j] & 0xFFFFFFFFL) + b;
			// bitwise is better
			b = (byte) (t >>> 32);
			C[j] = (int) (t & 0xFFFFFFFF);
		}

		// next state function
		int G[] = new int[8];
		for (int j = 0; j < 8; ++j) {
			// yes, my entire g function, small as I can get it
			// I would like to be able to reduce this to use 32 bits only though
			long t = (X[j] + C[j]) & 0xFFFFFFFFL;
			G[j] = (int) ((t *= t) ^ (t >>> 32));
		}

		X[0] = (int) (G[0] + rotl(G[7], 16) + rotl(G[6], 16));
		X[1] = (int) (G[1] + rotl(G[0], 8) + G[7]);
		X[2] = (int) (G[2] + rotl(G[1], 16) + rotl(G[0], 16));
		X[3] = (int) (G[3] + rotl(G[2], 8) + G[1]);
		X[4] = (int) (G[4] + rotl(G[3], 16) + rotl(G[2], 16));
		X[5] = (int) (G[5] + rotl(G[4], 8) + G[3]);
		X[6] = (int) (G[6] + rotl(G[5], 16) + rotl(G[4], 16));
		X[7] = (int) (G[7] + rotl(G[6], 8) + G[5]);
	}
}