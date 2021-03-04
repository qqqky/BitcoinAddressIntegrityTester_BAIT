# BitcoinAddressIntegrityTester_BAIT
Bitcoin vanity address search, Bitcoin address derivation from random private keys, Bitcoin private key search

## This is BAIT - Bitcoin Address Integrity Tester
 
This program was created to get a practical feel on how robust is the address derivation algorithm used in Bitcoin, 
guarded by nothing else, just the math behind it and pure vastness of possible number of addresses.

Secondary reason is to use it for simple local vanity address search for new users who are interested in Bitcoin.
 

The main bitcoin core library for Java - bitcoinj-core already contains all the needed tools to make such
program. However, it uses several (4) external dependencies itself, which forces them to be included in the pre-compiled
version of this program.
 
BAIT operates on a list of first 1447113 P2PKH type addresses (unencoded public key hashes). It generates a random
private address, derives a pair of public addresses (compressed and uncompressed) and hashes them with SHA-256 and 
RIPEMD-160. The resulting value of this process is known as "public key hash". 
 
Program also supports searching for vanity addresses.
 
If you wish to include your own collection of public key hashes or their encoded versions, please edit the corresponding
files in the 'resources' directory. Note that all P2PKH addresses start with "1".
 
Program supports several modes:
 
• Vanity Address Search.
    Currently hardcoded to search for public addresses starting with (case insensitive!): 1cuTer.../1CutER.../1cuter/...etc.
    while this can be changed in the CLI options, current version only supports an exact case sensitive String,
    such as providing input "fuNnY" will only match for addresses starting with '1fuNny..." and nothing else.
    
• BAIT mode - operates on provided list of 1447113 (decoded) addresses. It will generate random private addresses,
     which will then be converted to public key hashes and checked against the map of known addresses.
     
• Combined mode - Vanity + BAIT combined. This mode, however, Will operate on the list of undecoded ('normal') addresses, 
     as we need to know those for vanity search anyways.
				
• TEST mode - this is a short test for BAIT mode. Choose it to see if the program would be able to write to the specified result
     files in results/lucky.txt
  				
Output:
  
 If search yields any results, they will be saved in results/vanity.txt or results/lucky.txt in the following format:
  
 Private Key *space* Compressed Public Key *space* Uncompressed Public Key
  	 
