package LocalCheckerClient;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.*;
import java.io.File;

public class SimpleListFilter {
	
	private static final int numP2PKHaddressesToBeSaved = 1447113;
	private static String currentDir = System.getProperty("user.dir")+File.separator;

	public static void resolveOriginalList(String pathToOriginalListOfPubs, String saveResultTo) throws IOException{
		
		Path path = Paths.get(currentDir+pathToOriginalListOfPubs);
		Path result = Paths.get(currentDir+saveResultTo);
		
		int count = SimpleListFilter.lineCountReader(path.toString());
		int recount = 0;
		String s = "";
		
		
		System.out.println("Total lines counted before processing: "+count);

		recount = 0;
		try(BufferedWriter bw = Files.newBufferedWriter(result, StandardCharsets.UTF_8);
			BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
		
			while((s = br.readLine())!= null) {
				
				if(recount<numP2PKHaddressesToBeSaved && s.startsWith("1")) {
					s = s.substring(0, s.indexOf('\t'));
					if(s.length()<=35) {	//rare case that length>34 (if encrypted address happens to start with "1")
						recount++;
						bw.write(s);
						if(recount!=numP2PKHaddressesToBeSaved) {
							bw.newLine(); 
						}
						bw.flush();
					}
					
				}
			}
				
		}//try		
		
		
		count = SimpleListFilter.lineCountReader(result.toString());
		
		System.out.println("Total lines counted after processing (should be "+numP2PKHaddressesToBeSaved+"): "+count);
		
		
		
	}
	public static void decodeAndSaveListOfPubs(String pathToAddressList, String saveToPath) {
		
		Base58 base58 = new Base58();
		AddressConverter converter = new AddressConverter();
		String s = "";
		
		int count = SimpleListFilter.lineCountReader(currentDir+pathToAddressList);
		int recount = 0;
		
		try(BufferedReader br = Files.newBufferedReader(Paths.get(currentDir+pathToAddressList).toAbsolutePath(), StandardCharsets.UTF_8);
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(currentDir+saveToPath).toAbsolutePath(), StandardCharsets.UTF_8)){
			
			while((s=br.readLine())!=null) {
				recount++;
				s = converter.bytesToHexString(base58.decodeChecked(s));
				bw.write(s);
				if(recount!=count)bw.newLine();			
			}
			
			
		}catch(IOException e) {
			System.err.println("Error decoding to ");
			e.printStackTrace();
		}
		
		System.out.println("Decoded addresses successfully added to file: "+saveToPath.toString());
		

	}
	public static int lineCountReader(String pathToFile) {
		
		int count = 0;
		String line = "";
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(currentDir+pathToFile)))){
			
			while((line = reader.readLine())!=null) {
			//	if(count==0) System.out.println(line+"lalala");
			//	if(count==1) System.out.println(line+"lalala");
				count++;
				
			}
		}catch(IOException e) {
			System.out.println("Line counter error in SimpleListFilter.lineCountReader()");
		}
		
		return count;
	}
	
	public static final void writePubsFromMapToFile(Map<String, Object> map, String pathToFile) {
		
		String addr;
		int size = map.keySet().size();
		int counter = 0;
		Iterator<String> iter = map.keySet().iterator();
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(currentDir+pathToFile)))){
			
			while(iter.hasNext()) {
				counter++;
				if(counter<size) {
					writer.write(iter.next()); writer.newLine();
				}else {
					writer.write(iter.next());
				}
				
			}
			 writer.flush();
		}catch(IOException e) {
			
		}
		System.out.println(size+ " entries have been written into "+Paths.get(pathToFile));
	}

}
