package bitcoinTester;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;

import bitcoinTester.AddressChecker.AddressPair;

/*
 * 
 * Program converts priv key to public addresses (compressed+uncompressed) using existing libraries.
 * Tested and seems to be working correctly (99.99%??)
 * 
 */
public class AddressConverter {
	
	
	public enum Mode {VANITY, ADDRESS_SCAN, BOTH, TEST, NONE};
	private Pattern regex = Pattern.compile("1[Cc]{1}[Uu]{1}[Tt]{1}[Ee]{1}[Rr]{1}[\\w]*");	//vanity search
	private final String referencePattern = "1[Cc]{1}[Uu]{1}[Tt]{1}[Ee]{1}[Rr]{1}[\\w]*";
	private static final int total = 10;
	private static final int cycle = 1;
	private final int billion = 1000000000;
	private static final boolean vanity = false;
	private boolean test = false;
	private final Base58 base58 = new Base58();
	private Mode mode;
	private String pubKeyHashUncomp = "";
	private String pubKeyHashComp = "";
	private final Map<String, Object> map = new HashMap<>();
	
	
	public static void main(String[] args) {
		
		AddressConverter converter = AddressConverter.getConverter(Mode.ADDRESS_SCAN);
		
		if(converter.mode == Mode.ADDRESS_SCAN)
			converter.loadFileToClassMap("resources/DecodedAddresses1447113.txt");
		if(converter.mode == Mode.BOTH)
			converter.loadFileToClassMap("resources/NormalAddresses1447113.txt");
		
		if(!converter.getMap().isEmpty()) System.out.println("Map loaded, number of entries: "+converter.getMap().size());
				
		long start = System.nanoTime();
		converter.generateAndCheckAgainstTheList(total, cycle, true);
		
		System.out.println("Total items checked: "+(total*cycle));
		System.out.println("Total time passed: "+((System.nanoTime()-start)/converter.billion)+" seconds.");
		

	}
	
	private AddressConverter(AddressConverter.Mode requestedMode) {
	
		if(requestedMode == null){
			mode = Mode.NONE;
		}
		else {
			switch(requestedMode) {
		
			case VANITY:
				mode = Mode.VANITY;	
			break;
			case ADDRESS_SCAN:
				mode = Mode.ADDRESS_SCAN;
				this.test = false;
			break;
			case BOTH:
				mode = Mode.BOTH;
				this.test = false;
			break;
			case TEST:
				mode = Mode.TEST;
				this.test = true;
			break;
			default:
			throw new IllegalArgumentException("Mode not supported by constructor or some values have changed");
		
			}
		}
		
	}
	public static AddressConverter getConverter(AddressConverter.Mode requestedMode) {
		
			return new AddressConverter(requestedMode);	
	}
	
	public final void generateAndCheckAgainstTheList(int numitems, int numcycles, boolean progress) {
		
		if(this.mode == Mode.NONE) {
			throw new UnsupportedOperationException("This operation is not supported in current state. ");
		}
		switch(this.mode) {
		case ADDRESS_SCAN:
			this.doBAITinHashedMode(numitems, numcycles, progress);
			break;
		case VANITY:
			this.doVanity(numitems, numcycles, progress);
			break;
		case BOTH:
			this.doCombinedMode(numitems, numcycles, progress);
			break;
		case TEST:
			this.doBAITinTestMode(numitems, numcycles, progress);
			
		}
	}
	public final void doCombinedMode(int numitems, int numcycles, boolean progress) {
		
		String temp="";
		
			int count=0;
			int cycle=0;
			
			long cyclestart;
			int fivePercent = numitems*5/100;
			AddressPair pair;
			RandomAddressGenerator gen = RandomAddressGenerator.getNormalGenerator(64);
		
		//Main work
	MAIN: while(cycle<numcycles) {
			cyclestart = System.nanoTime();
			while(count<numitems) {
			
				temp = gen.generate(64);
				pair = this.retrieveAddressPair(temp);

									
			if(map.containsKey(pair.getUncompressed()) || map.containsKey(pair.getCompressed())) {
					Toolkit.getDefaultToolkit().beep();
					System.out.println(pair);
					this.formatAndWriteToFile(temp, pair, "results/lucky.txt");
					break MAIN;
			}
			
			if(regex.matcher(pair.getCompressed()).matches() || regex.matcher(pair.getUncompressed()).matches()){
					Toolkit.getDefaultToolkit().beep();
					System.out.println("Address: "+temp);
					System.out.println(pair);
					this.formatAndWriteToFile(temp, pair, "results/vanity.txt");	
				}
			
			count++;
			if(progress) {
				if(fivePercent !=0 && count%fivePercent==0) {
					System.out.print((count*100/numitems)+ "% done (~"+count+")   \r");
					}
			}		
			}
				
				if(progress) {
					System.out.println("Cycle "+(cycle+1)+" finished     \t\t");
					System.out.println("Avg. speed in cycle: "+(numitems/((System.nanoTime()-cyclestart)/billion))+" checks/sec by a single thread");
				}	
			count=0;
			cycle++;
		
		}	
	}
	public final void doBAITinHashedMode(int numitems, int numcycles, boolean progress) {
	
	String temp="";
	
		int count=0;
		int cycle=0;
		
		long cyclestart;
		int fivePercent = numitems*5/100;
		AddressPair pair;
		RandomAddressGenerator gen = RandomAddressGenerator.getSecureGenerator(64);
		
	
	//Main work
	MAIN: while(cycle<numcycles) {
		cyclestart = System.nanoTime();
		while(count<numitems) {
		
			temp = gen.generate(64);
			pubKeyHashUncomp = this.bytesToHexString(ECKey.fromPrivate(hexToByteData(temp), false).getPubKeyHash());
			pubKeyHashComp = this.bytesToHexString(ECKey.fromPrivate(hexToByteData(temp), true).getPubKeyHash());

						
		if(map.containsKey(pubKeyHashUncomp) || map.containsKey(pubKeyHashComp)) {
				Toolkit.getDefaultToolkit().beep();
				System.out.println(pubKeyHashUncomp + " "+pubKeyHashComp);
				pair = this.retrieveAddressPair(temp);
				this.formatAndWriteToFile(temp, pair, "results/lucky.txt");
				break MAIN;
		}

			count++;
			if(progress) {
				if(fivePercent !=0 && count%fivePercent==0) {
					
					System.out.print((count*100/numitems)+ "% done (~"+count+")   \r");
				}
			}		
		}
			
			if(progress) {
				System.out.println("Cycle "+(cycle+1)+" finished     \t\t");
				System.out.println("Avg. speed in cycle: "+(numitems/((System.nanoTime()-cyclestart)/billion))+" checks/sec by a single thread");
			}	
		count=0;
		cycle++;
	}	
}		
	public final void doVanity(int numitems, int numcycles, boolean progress) {
		
		String temp="";
		int count=0;
		int cycle=0;
		
		long cyclestart=0;
		int fivePercent = numitems*5/100;
		AddressPair pair;
		RandomAddressGenerator gen = RandomAddressGenerator.getSecureGenerator(64);
	
	//Main work
	MAIN: while(cycle<numcycles) {
			cyclestart = System.nanoTime();
			while(count<numitems) {
		
				temp = gen.generate(64);
				pair = this.retrieveAddressPair(temp);
		
				if(regex.matcher(pair.getCompressed()).matches() || regex.matcher(pair.getUncompressed()).matches()){
					Toolkit.getDefaultToolkit().beep();
					this.formatAndWriteToFile(temp, pair, "results/vanity.txt");
					System.out.println("Address: "+temp);
					System.out.println(pair);
				}
		
				count++;
				if(progress) {
					if(fivePercent !=0 && count%fivePercent==0) {	
						System.out.print((count*100/numitems)+ "% done (~"+count+")   \r");
					}
				}		
			}
			
			if(progress) {
				System.out.println("Cycle "+(cycle+1)+" finished     \t\t");
				System.out.println("Avg. speed during this cycle: "+(numitems/((System.nanoTime()-cyclestart)/billion))+" vanity pairs/sec by a single thread");
			}	
			count=0;
			cycle++;
		}	
	}
	public final void doBAITinTestMode(int numitems, int numcycles, boolean progress) {
		
		String temp="";
		
			int count=0;
			int cycle=0;
			
			long cyclestart;
			int fivePercent = numitems*5/100;
			AddressPair pair;
			RandomAddressGenerator gen = RandomAddressGenerator.getSecureGenerator(64);
			//AddressChecker checker = AddressChecker.getDefaultChecker();
		
		//Main work
		MAIN: while(cycle<numcycles) {
			cyclestart = System.nanoTime();
			while(count<numitems) {
			
				temp = gen.generate(64);
				pubKeyHashUncomp = this.bytesToHexString(ECKey.fromPrivate(hexToByteData(temp), false).getPubKeyHash());
				pubKeyHashComp = this.bytesToHexString(ECKey.fromPrivate(hexToByteData(temp), true).getPubKeyHash());

				
		//throw in stubs for the test
			if(cycle == 0 && count==50) {
					System.out.println("TEST CASE INITIALIZED");
					System.out.println("Is map empty? "+map.isEmpty());
					System.out.println("Map size: "+map.size());
					pubKeyHashUncomp = "e913436287a99eee638592484e6f7cd92145d612"; //this should be in map
					pubKeyHashComp = "1NFPX8TiKnpErokpAzeH8PTTtkSjg6kRF0";
					
			}
			
						
			if(map.containsKey(pubKeyHashUncomp) || map.containsKey(pubKeyHashComp)) {
					Toolkit.getDefaultToolkit().beep();
					System.out.println(pubKeyHashUncomp + " "+pubKeyHashComp);
					pair = this.retrieveAddressPair(temp);
					this.formatAndWriteToFile(temp, pair, "results/lucky.txt");
					System.out.println("Test has ended.");
					break MAIN;
			}

				count++;
				if(progress) {
					if(fivePercent !=0 && count%fivePercent==0) {
						
						System.out.print((count*100/numitems)+ "% done (~"+count+")   \r");
					}
				}		
			}
				
				if(progress) {
					System.out.println("Cycle "+(cycle+1)+" finished     \t\t");
					System.out.println("Avg. speed in cycle: "+(numitems/((System.nanoTime()-cyclestart)/billion))+" checks/sec by a single thread");
				}	
			count=0;
			cycle++;
		}	
	}		
	public final AddressPair retrieveAddressPair(String priv) {
		
	
			//uncompressed
		String uncompressed = base58.encodeChecked(0, ECKey.fromPrivate(hexToByteData(priv), false).getPubKeyHash());
			//compressed
		String compressed = base58.encodeChecked(0, ECKey.fromPrivate(hexToByteData(priv), true).getPubKeyHash());
		
		
		return new AddressPair(uncompressed, compressed);

	// Left for future reference

	//	byte[] r1 = pub.getPubKeyHash();		//SHA256+RIPEMD bytes
	
	
	//	System.out.println("WIF: "+pub.getPrivateKeyAsWiF(MainNetParams.get()));
		
	/*	byte[] r2 = new byte[21];		
		if(r1.length==20) {
			r2[0] = 0;			//ADD "0" (not 00...)
			for (int i = 0 ; i < r1.length ; i++) {
				r2[i+1] = r1[i];
			}
		}
	//	System.out.println("Public key hash with added 0 byte: "+bytesToHexString(r2));
		byte[] s1 = new byte[32];
		byte[] s2 = new byte[32];
		try {
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			s1 = sh.digest(r2);
			s2 = sh.digest(s1);		//checksum is first 4 bytes of this
		
		}catch(NoSuchAlgorithmException e) {
			LazyClientLogger.info("Digesting went wrong");
		}
		//append checksum
		byte[] last = new byte[25];			//copy
		for(int i = 0; i<r2.length; i++) {
			last[i] = r2[i];
		}
		for(int i = 0; i<4; i++) {
			last[i+21] = s2[i];
		}
	*/	
	//	System.out.println(bytesToHexString(last));
		
		
	}
	
	String bytesToHexString(byte[] hexbytes) {		
		StringBuilder builder = new StringBuilder();
		for(byte a: hexbytes) {
			int i = a&0xFF;		//apply mask so result is always positive
			if(i<16) {			//finally fixed the bug (was i<=16...)
				builder.append("0"); builder.append(Integer.toHexString(i)); //always want 2 symbols
			}else {
				builder.append(Integer.toHexString(i));
			}
		}	
			return builder.toString();
	}
	//maybe rewrite this one with StringBuilder?
	public byte[] hexToByteData(String hex)
	{
	    byte[] convertedByteArray = new byte[hex.length()/2];
	    int count  = 0;

	    for( int i = 0; i < hex.length() -1; i += 2 )
	    {
	        String output;
	        output = hex.substring(i, (i + 2));
	        int decimal = (int)(Integer.parseInt(output, 16));
	        convertedByteArray[count] =  (byte)(decimal & 0xFF);
	        count ++;
	    }
	    return convertedByteArray;
	}
	public final synchronized void loadFileToClassMap(String pathOfFile) {
		

	if(!this.map.isEmpty())
		this.map.clear();
		String temp = "";
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(pathOfFile))){
			
			while((temp = reader.readLine())!=null) {
				this.map.put(temp, null);
			}
		}catch(IOException e) {
			System.err.println("Error loading map in AddressConverter#loadFileToMap()");
			e.printStackTrace();
		}
			
	}
	
	public synchronized void formatAndWriteToFile(String prv, AddressPair pair, String targetPath) {
		
		System.out.println("Interesting: "+prv);
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(targetPath), StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
			
			writer.write(prv); writer.write("\t"); writer.write(pair.getCompressed());
			writer.write("\t"); writer.write(pair.getUncompressed());
			writer.newLine();
			writer.flush();
			
		}catch(IOException e) {
			
			e.printStackTrace();
		}
	}
	private Map<String, Object> getMap(){
		return this.map;
	}
	public void setVanityPattern(String input) {
		
		String temp = "";
		if(input == null || input.length()>10) {
			System.out.println("No input for vanity pattern or input too long. Will use the default pattern instead.");
			temp = referencePattern;
		}else if(input.contains("0") || input.contains("O") || input.contains("I") || input.contains("l")) {
			System.out.println("Wrong input characters found. Address cannot contain: 0, O, I or l. Will use the default pattern instead.");
			temp = referencePattern;
		}else {
			StringBuilder sb = new StringBuilder("1");
			for(int i=0; i<input.length();i++) {
				temp = input.substring(i, i+1);
				sb.append("[").append(temp).append("]{1}");
			}
			sb.append("[\\w]*");
			temp = sb.toString();
			System.out.println("Vanity regex pattern set to: "+ temp);
			
		}
		regex = Pattern.compile(temp);
	}
	public int getMapSize() {
		if(this.map.isEmpty()) {
			return 0;
		}
		else return this.map.size();
	}
	public Mode getMode() {
		return mode;
	}

}
