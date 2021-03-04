package bitcoinTester;

import java.util.Arrays;

import org.bitcoinj.core.Sha256Hash;

/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Base58 {
	
	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
			.toCharArray();
	private static final int BASE_58 = ALPHABET.length;
	private static final int BASE_256 = 256;

	private static final int[] INDEXES = new int[128];
	static {
		for (int i = 0; i < INDEXES.length; i++) {
			INDEXES[i] = -1;
		}
		for (int i = 0; i < ALPHABET.length; i++) {
			INDEXES[ALPHABET[i]] = i;
		}
	}
	public final StringBuilder sb = new StringBuilder();
	

	public String encode(byte[] input) {
		if (input.length == 0) {
			// paying with the same coin
			return "";
		}

		//
		// Make a copy of the input since we are going to modify it.
		//
		input = copyOfRange(input, 0, input.length);

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input.length && input[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The actual encoding
		//
		byte[] temp = new byte[input.length * 2];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input.length) {
			byte mod = divmod58(input, startAt);
			if (input[startAt] == 0) {
				++startAt;
			}

			temp[--j] = (byte) ALPHABET[mod];
		}

		//
		// Strip extra '1' if any
		//
		while (j < temp.length && temp[j] == ALPHABET[0]) {
			++j;
		}

		//
		// Add as many leading '1' as there were leading zeros.
		//
		while (--zeroCount >= 0) {
			temp[--j] = (byte) ALPHABET[0];
		}

		byte[] output = copyOfRange(temp, j, temp.length);
		return new String(output);
	}

	public byte[] decode(String input) {
		if (input.length() == 0) {
			// paying with the same coin
			return new byte[0];
		}

		byte[] input58 = new byte[input.length()];
		//
		// Transform the String to a base58 byte sequence
		//
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = INDEXES[c];
			}
			if (digit58 < 0) {
				throw new RuntimeException("Not a Base58 input: " + input);
			}

			input58[i] = (byte) digit58;
		}

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The encoding
		//
		byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			byte mod = divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}

		//
		// Do no add extra leading zeroes, move j to first non null byte.
		//
		while (j < temp.length && temp[j] == 0) {
			++j;
		}

		return copyOfRange(temp, j - zeroCount, temp.length);
	}

	private byte divmod58(byte[] number, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			int digit256 = (int) number[i] & 0xFF;
			int temp = remainder * BASE_256 + digit256;

			number[i] = (byte) (temp / BASE_58);

			remainder = temp % BASE_58;
		}

		return (byte) remainder;
	}

	private byte divmod256(byte[] number58, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			int digit58 = (int) number58[i] & 0xFF;
			int temp = remainder * BASE_58 + digit58;

			number58[i] = (byte) (temp / BASE_256);

			remainder = temp % BASE_256;
		}

		return (byte) remainder;
	}

	private byte[] copyOfRange(byte[] source, int from, int to) {
		byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}
	/**
     * Encodes the given version and bytes as a base58 string. A checksum is appended.
     * 
     * @param version the version to encode
     * @param payload the bytes to encode, e.g. pubkey hash
     * @return the base58-encoded string
     */
    public String encodeChecked(int version, byte[] payload) {
        if (version < 0 || version > 255)
            throw new IllegalArgumentException("Version not in range.");

        // A stringified buffer is:
        // 1 byte version + data bytes + 4 bytes check code (a truncated hash)
        byte[] addressBytes = new byte[1 + payload.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(payload, 0, addressBytes, 1, payload.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, payload.length + 1);
        System.arraycopy(checksum, 0, addressBytes, payload.length + 1, 4);
        return this.encode(addressBytes);
    }
    
    /**
     * Decodes the given base58 string into the original data bytes, using the checksum in the
     * last 4 bytes of the decoded data to verify that the rest are correct. The checksum is
     * removed from the returned data.
     *
     * @param input the base58-encoded string to decode (which should include the checksum)
     * @throws AddressFormatException if the input is not base 58 or the checksum does not validate.
     */
    public byte[] decodeChecked(String input) {
        byte[] decoded  = decode(input);
        if (decoded.length < 4)
            throw new RuntimeException("Input too short: " + decoded.length);
        byte[] data = Arrays.copyOfRange(decoded, 1, decoded.length - 4);	//edit from 0 to 1 (remove version byte as well)
     
	//checksum revalidation part omitted
     /*   byte[] checksum = Arrays.copyOfRange(decoded, decoded.length - 4, decoded.length);  
        byte[] actualChecksum = Arrays.copyOfRange(Sha256Hash.hashTwice(data), 0, 4);   
        if (!Arrays.equals(checksum, actualChecksum))
            throw new RuntimeException("Invalid checksum in Base58#decodeChecked");
	*/
       // byte[] withZeroRemoved = Arrays.copyOfRange(data, 1, data.length); 
        return data;
    }

	
}
