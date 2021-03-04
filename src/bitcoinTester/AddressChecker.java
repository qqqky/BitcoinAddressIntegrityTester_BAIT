package bitcoinTester;

public class AddressChecker {
	
	
	static class AddressPair{
		
		private String uncompressedAddress;
		private String compressedAddress;
		
		public AddressPair(String uncompressed, String compressed) {
			this.uncompressedAddress=uncompressed.trim();
			this.compressedAddress=compressed.trim();
		}
		public final String getCompressed() {
			return compressedAddress;
			
		}
		public final String getUncompressed() {
			return uncompressedAddress;
		}
		public String toString() {
			return String.format("Uncompressed: %s, Compressed %s", uncompressedAddress, compressedAddress);
		}
		public boolean equals(AddressPair another) {
			if(this.getCompressed().equals(another.getCompressed()) && this.getUncompressed().equals(another.getUncompressed())) {
				return true;
			}
			return false;
		}
	}
	
}
