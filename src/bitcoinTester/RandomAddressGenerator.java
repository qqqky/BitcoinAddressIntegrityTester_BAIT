package bitcoinTester;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/*
 * 
 * Program generates a random private address without checking if it's over the max value (extremely unlikely). 
 *
 * Valid address range for Bitcoin private key is:
 * 
 * BTC max key value is: 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140 (inclusive)
 * BTC min key value is: 0x0000000000000000000000000000000000000000000000000000000000000001 (one)
 * 
 */
public class RandomAddressGenerator {
	
	private StringBuilder sb = new StringBuilder();
	private StringBuilder biased = new StringBuilder();
	private int length = 0;
	private ThreadLocalRandom genNormal;
	private SecureRandom genSecure;

	
	private RandomAddressGenerator(int length, boolean secure) {
		this.length=length;
		if(secure) genSecure = new SecureRandom();
		else genNormal = ThreadLocalRandom.current();
	}
	public static final RandomAddressGenerator getSecureGenerator(int length) {
		return new RandomAddressGenerator(length, true);
	}
	public static final RandomAddressGenerator getNormalGenerator(int length) {
		return new RandomAddressGenerator(length, false);
	}
	private final String generateValidKey(int size) {
		
		sb.delete(0, sb.length());
		while(sb.length()!=size) {
			for(int i=1; i<=size; i++) {
				sb.append(upToF());
			}
			if(sb.length()!=size) {
				sb.delete(0, sb.length());
			}
		}
	
		
		
	//System.out.println("Generated: "+sb);
		return sb.toString();
	}
	
	public final String generate(int length) {
		
		return generateValidKey(length);
	}
	private final String upToF () {
		
		if(genNormal !=null)
			return Integer.toHexString(genNormal.nextInt(16));
		else
			return Integer.toHexString(genSecure.nextInt(16));
	}
	 
	

	
}
