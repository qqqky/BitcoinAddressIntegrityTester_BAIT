package bitcoinTester;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import bitcoinTester.AddressConverter.Mode;

/**
 * This is BAIT - Bitcoin Address Integrity Tester.
 * 
 * This program was created to get a feel on how robust is the address derivation algorithm used in Bitcoin, 
 * guarded by nothing but the math behind it and pure vastness of possible numbers.
 *
 * 
 * Turns out the main bitcoin core library for Java - bitcoinj-core already contains all the needed tools to make such
 * program. However, it uses several (4) external dependancies itself, which forces them to be included in the pre-compiled
 * version of this program.
 * 
 * BAIT operates on a list of first 1447113 P2PKH type addresses (unencoded public key hashes). It generates a random
 * private address, derives a pair of public addresses (compressed and uncompressed) and hashes them with SHA-256 and 
 * RIPEMD-160. The resulting value of this process is known as "public key hash". It then checks if such value exists in the map.
 * 
 * Program also supports searching for vanity addresses.
 * 
 * If you wish to include your own collection of public key hashes or their encoded versions, please edit the corresponding
 * files in the 'resources' directory. 
 * Note that all P2PKH addresses start with "1" and only P2PKH type addresses are supported.
 * 
 * Program supports several modes:
 * 
 * Vanity Address Search - currently hardcoded to search for public addresses starting with (case insensitive!): 1cuTer.../
 * 				1CutER.../1cuter/...etc.
 * 				while this can be changed in the CLI options, current version only supports an exact case sensitive String,
 * 				such as providing input "fuNnY" will only match for addresses starting with '1fuNny..." and nothing else.
 *				Note that 4 ASCII characters cannot exist in Bitcoin addresses: "O"(uppercase o), "0"(zero), "l" and "I".
 * BAIT mode - operates on the provided list of 1447113 (decoded) addresses. It will generate random private addresses,
 * 				which will then be converted to public key hashes and checked against the map.
 * Combined mode - Vanity + BAIT together. This mode, however, Will operate on the list of encoded ('normal') addresses, 
 * 				as we need to know those for vanity search anyways.
 * TEST mode - this is a test for BAIT mode. Choose it to see if the program would be able to write to the specified result
 * 				file in results/lucky.txt
 * 				
 * 	Output:
 * 
 * If search yields any results, they will be saved in results/vanity.txt or results/lucky.txt in the following format:
 * 
 * Private Key <space> Compressed Public Key <space> Uncompressed Public Key
 * 	 
 * 
 * 
 * @author qqqky
 * @version 0.5
 * 
 * 
 */

public class BitcoinAddressIntegrityTester {

	private static int total = 1000000;
	private String vanityChange = "";	//placeholder
	private static boolean vanityChanged = false;
	private static final String referencePattern = "1[Cc]{1}[Uu]{1}[Tt]{1}[Ee]{1}[Rr]{1}[\\w]*";
	private static boolean progress = true;
	private AddressConverter.Mode MODE = Mode.NONE;
	
	public static void main(String[] args) {
		
		
		BitcoinAddressIntegrityTester client = new BitcoinAddressIntegrityTester();
		int processors = Runtime.getRuntime().availableProcessors();
			List<Future<?>> threadList = new ArrayList<>();
			
		int numThreads = 0;
		int cycle = 0;
		String prog;
		long result; //no use
		Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to BAIT - Bitcoin Address Integrity Tester.");
		System.out.println("This program tests random addresses against a precompiled list of 1447113 hashes of known addresses (P2PKH only)");
		System.out.println("For modes other than Vanity Search, each thread gets its own personal map, so memory consumption can get high.");
		System.out.println("If any results happened to be gathered, they will be appended to resources/vanity.txt or resources/lucky.txt");
		System.out.println();
		
		while(client.MODE == Mode.NONE) {
		System.out.println("Please select mode (1-4): ");
		System.out.println("[1] Vanity Address Search (only addresses of type P2PKH (starting with '1') are supported).");
		System.out.println("[2] BAIT mode. Generate and test random addresses against a list of P2PKH address hashes.");
		System.out.println("[3] Combined mode (vanity search + alternative BAIT mode - search is performed directly on public addresses)");
		System.out.println("[4] Test mode. Perform a test to make sure the program is working as intended.");
			try{
				int answer = scan.nextInt();
				if(answer <=0 || answer > 4) {
					throw new InputMismatchException();	
				}
				else {
					client.MODE = client.determineMode(answer);
				}
			}catch(InputMismatchException e) {
				System.out.println("Unrecognized input. Defaulting to mode [1] - Vanity Address Search");
				scan.nextLine();
				client.MODE = Mode.VANITY;
			}
			
		//<--- loop for mode selection should end here if bugs found
		//VANITY CONFIG
		if(client.MODE == Mode.VANITY || client.MODE == Mode.BOTH) {
			if(client.vanityChange !=null && client.vanityChange.equals("")) {	//if program just launched, ask if default vanity pattern is ok
				System.out.println();
			System.out.println("Selected mode includes vanity search. Remember, all P2PKH public addresses start with '1'."
			+ System.getProperty("line.separator")
			+ "Currently, vanity search is set to look for addresses starting with (case insensitive) '1CuTeR.../1cuTEr.../etc. "
			+ System.getProperty("line.separator")
			+ "Would you like to change that? (Y/N): ");
			String vanityAnswer = scan.next();
			if(vanityAnswer != null && vanityAnswer.equalsIgnoreCase("Y")) {
				System.out.println("Please input 1-5 valid symbols without quotes (remember, address cannot contain: 0, O, I or l)."
			+System.getProperty("line.separator")+"Currently, program will treat them as CaSe SeNsItIvE. Eg.: "
			+System.getProperty("line.separator")
			+"for input 'cutE', the program will set to only look for addresses starting with '1cutE...':");
				try {
					client.vanityChange = scan.next();
					if(client.vanityChange == null) {
						client.vanityChange = referencePattern;	//
						client.vanityChanged = false;
					}else {
						client.vanityChanged = true; //set flag to deal with it later
					}
				}catch(InputMismatchException e) {
					System.out.println("Unrecognized input. Will keep the default value (1Cute...)");
					scan.nextLine();
				}
			}
			}else {
				System.out.println("Your previously specified address pattern will be used again for Vanity Search. If you"
					+System.getProperty("line.separator")+"wish to change it again, the program must be restarted. ");
			}	
		}//VANITY CONFIG ENDS
		
		
		System.out.println();
		System.out.println("Mode selected successfully. Please specify run and thread options now. ");
		String optionTotal;
		System.out.println("Number of addresses to be checked (per cycle) by each thread is set to "+total+". Do you want to change that? (Y/N)");
			optionTotal  = scan.next();
			if(optionTotal != null && optionTotal.equalsIgnoreCase("Y")) {
				System.out.println("Please enter a number of privates to be checked by each thread (up to 2 billion): ");
				try {
					total = scan.nextInt();
					if(total<=0) {
						total = 1000000;
						System.out.println("Input cannot be lower than zero. Default value will be kept at: "+total);
					}
				}catch(InputMismatchException e) {
					System.out.println("Unrecognized input or number too high. Will keep the default value: "+total);
					scan.nextLine();
				}
			}
			else {
				System.out.println("Default value will be kept at: "+total);
			}
		System.out.println("How many cycles you want each thread to run for?: ");
		try{
			cycle = scan.nextInt();
		}catch(InputMismatchException e) {
			System.out.println("Unrecognized input. Number of cycles will be set to 1.");
			scan.nextLine();	//clean the line after wrong input, otherwise scanner will treat it as input to next scan
			cycle = 1;
		}
		System.out.println("Available processors on your machine: "+processors);
		System.out.println("How many threads you want? (1-"+processors+"): ");
		try{
			numThreads = scan.nextInt();
			if(numThreads <=0 || numThreads > processors) {
				System.out.println("Invalid input. Number of threads will be set to 1.");
				numThreads = 1;
			}
		}catch(InputMismatchException e) {
			System.out.println("Unrecognized input. Number of threads will be set to 1.");
			scan.nextLine();
			numThreads = 1;
		}
		
		System.out.println("Starting scan.");
		
		ExecutorService pool = Executors.newFixedThreadPool(processors-(processors-numThreads));
		
		long start = System.nanoTime();
		
		if(numThreads == 1) {
			Future<?> one = pool.submit(client.makeRunnable(total, cycle, 1, progress));
			try {
				result = Stream.of(one.get()).count();
			}catch(ExecutionException | InterruptedException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		} else {
			threadList.add(pool.submit(client.makeRunnable(total, cycle, 1, progress)));
			for(int i = 1; i<numThreads; i++) {
				threadList.add(pool.submit(client.makeRunnable(total, cycle, i+1, false)));
			}
			
			result = threadList.parallelStream().map(future -> 
				{	
					try{	
						return future.get();
					}catch(ExecutionException | InterruptedException e) {
						throw new RuntimeException(e);
				}}).count();
		}
		
		if(!pool.isShutdown())
			pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);	//block until all worker threads return

		}catch(InterruptedException e) {System.err.println("Thread interrupted while waiting for results of Futures.");}
		
		
		//output statistics of the run
		
		
		BigDecimal totalItems = new BigDecimal(total*cycle*numThreads);
		BigDecimal sixty = new BigDecimal(60).setScale(2);
		BigDecimal totalSeconds = new BigDecimal((System.nanoTime()-start)/1000000000).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalMinutes = totalSeconds.divide(sixty, RoundingMode.HALF_UP);	
		BigDecimal averageSpeedPerMin = totalItems.divide(totalSeconds.divide(sixty, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
		BigDecimal averageSpeedPerHour = averageSpeedPerMin.multiply(sixty).setScale(0);
		System.out.println("Total items checked: "+totalItems);
		System.out.println("Total time passed: "+totalSeconds+" seconds ("+totalMinutes+" minutes)");
		System.out.println("Average speed: "+averageSpeedPerMin+" items/min ("+averageSpeedPerHour+" items/hour) - Threads: "+numThreads);
		
		//add exit clause
		
		System.out.println();
		System.out.println("Run finished. Quit program (Y/N)? (Y - quit, N - go back to the main menu): ");
		String quit = scan.next();
		if(quit == null || quit.equalsIgnoreCase("Y")) {
			break;
		}else {
			client.MODE = Mode.NONE;
		}
	}//WHILE LOOP
		scan.close();
		
	}
	private final Runnable makeRunnable(int numItems, int numCycles, int threadID, boolean progress){
		
		return new Runnable() {
			
			@Override public void run() {
				
				AddressConverter converter = AddressConverter.getConverter(MODE);
				AddressConverter.Mode currentMode = converter.getMode();
				
				if(currentMode == AddressConverter.Mode.BOTH || currentMode == AddressConverter.Mode.VANITY) {
					if(BitcoinAddressIntegrityTester.isVanityChanged())
						converter.setVanityPattern(vanityChange);
				}
					
				String currentDir = System.getProperty("user.dir")+File.separator;
				
				synchronized(BitcoinAddressIntegrityTester.class) {	
					if(currentMode == AddressConverter.Mode.ADDRESS_SCAN || currentMode == AddressConverter.Mode.TEST) {
						converter.loadFileToClassMap(currentDir+"resources/DecodedAddresses1447113.txt");
					}else if(currentMode == AddressConverter.Mode.BOTH) {
						converter.loadFileToClassMap(currentDir+"resources/NormalAddresses1447113.txt");
					}
				}
				
				if(currentMode != AddressConverter.Mode.VANITY) {
					int size = converter.getMapSize();
					if(size == 0) {
						System.out.println("Map could not be loaded. Check if files in /resources/ exist and are accessible.");
					}else {
						System.out.println("Map loaded (in Runnable - "+threadID+"), number of entries: "+size);
					}	
				}
				
				
				converter.generateAndCheckAgainstTheList(numItems, numCycles, progress);
					
		}};
	}
	
	private Mode determineMode (int input) {
		
		switch (input) {
		case 1:
			return AddressConverter.Mode.VANITY;
		case 2:
			return AddressConverter.Mode.ADDRESS_SCAN;
		case 3:
			return AddressConverter.Mode.BOTH;
		case 4:
			return AddressConverter.Mode.TEST;
		default:
			throw new IllegalArgumentException("Could not determine correct mode");
		}
	}
	public static boolean isVanityChanged() {
		return BitcoinAddressIntegrityTester.vanityChanged;
	}

}
