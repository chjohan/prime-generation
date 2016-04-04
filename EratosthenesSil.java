///--------------------------------------------------------
//
//   File: EratosthenesSil.java for INF2440
//   implements bit-array (Boolean) for prime numbers
//   written by:  Arne Maus , Univ of Oslo,
//
//--------------------------------------------------------
import java.util.*;
import java.lang.*;
/**
* Implements the bitArray of length 'maxNum' [0..bitLen ]
*   1 - true (is prime number)
*   0 - false
*  can be used up to 2 G Bits (integer range)
*  Stores info on prime/not-prime in bits 0..6 in each byte
*  (does not touch the sign-bit - bit7)
*/
public class EratosthenesSil {
	byte [] bitArr ;           // bitArr[0] represents the 7 integers:  1,3,5,...,13, and so on
	int  maxNum;               // all primes in this bit-array is <= maxNum
	final  int [] bitMask = {1,2,4,8,16,32,64};  // kanskje trenger du denne
	final  int [] bitMask2 ={255-1,255-2,255-4,255-8,255-16,255-32,255-64}; // kanskje trenger du denne
	long startTime;
	long endTime;
	long elapsedTime;
	double millisecs;

	EratosthenesSil (int maxNum) {
      this.maxNum = maxNum;
      System.out.println("Sekvensiell kjoering.");
      System.out.println("Max primtall m:" + maxNum);
      bitArr = new byte [(maxNum/14)+1];
      setAllPrime();
      
      // Timing and generating prime numbers.
      startTime = System.nanoTime();
      generatePrimesByEratosthenes();
      endTime = System.nanoTime();
      elapsedTime = endTime - startTime;
      millisecs = (double)elapsedTime / 1000000.0;
      System.out.println("Genererte alle primtall <= " + maxNum + " paa: " + millisecs + " millisek.\n");
      
      // Timing and factorizing primenumbers.
      startTime = System.nanoTime();
      startFactorizing();
      endTime = System.nanoTime();
      elapsedTime = endTime - startTime;
      millisecs = (double)elapsedTime / 1000000.0;
      System.out.println("\n100 faktoriseringer med utskrift tok: " + millisecs + " millisek.");
      System.out.println("Dvs. " + millisecs/100 + " millisek per faktorisering.");
      
    } // end konstruktor ErathostenesSil

	 void setAllPrime() {
		  for (int i = 0; i < bitArr.length; i++) {
		   bitArr[i] = (byte)127;
		   //System.out.println(bitArr[i]);
	      }
	 }

    void crossOut(int i) {
     // set as not prime- cross out (set to 0)  bit represening 'int i'
           bitArr[i/14] &= bitMask2[(i%14)>>1];
	} //

    boolean isPrime (int i) {
        // <din kode her, husk å teste særskilt for 2 (primtall) og andre partall (ikke)>
    	if(i == 2) {
    		return true;
    	}
    	if((i&1) == 0) {
    		// 0 i siste bit dvs. partal
    		return false;
    	} else {
    		return (bitArr[i/14] & bitMask[(i%14)>>1]) != 0;
    	}
	 }

    void startFactorizing() {
    	long maxNumSquared = ((long)maxNum * (long)maxNum);
    	long start = maxNumSquared - 100;
    	ArrayList<Long> fakt;
    	for(long i = start; i < maxNumSquared; i++) {
    		fakt = factorize(i);
    		System.out.print(i + " = " + fakt.get(0));
    		printFactors(fakt);
    	}
    }
    
    void printFactors(ArrayList <Long> fakt) {
    	if(fakt.size()>1) {
    		for(int k = 1; k < fakt.size(); k++) {
    			System.out.print(" * " + fakt.get(k));
    		}
    	}
		System.out.println();
    }
    // Vi skal utføre 100 faktoriseringer for N
    ArrayList<Long> factorize (long num) {
    	ArrayList <Long> fakt = new ArrayList <Long>();
        // <Ukeoppgave i Uke 7: din kode her>
		//long currentPrime = 0;
		int currentPrime = 2;
		long squaredNum = (int)Math.sqrt(num);
		long numberToFactorize = num;
		
		//System.out.println("Factorizing " + num);
		while(numberToFactorize!=1) {
			if(numberToFactorize%currentPrime == 0) {
				fakt.add((long)currentPrime);
				numberToFactorize = numberToFactorize/currentPrime;
				//System.out.println(currentPrime + " was added to list.");
			} else if(currentPrime > squaredNum && numberToFactorize > 1) {
				fakt.add((long)numberToFactorize);	
				//System.out.println(numberToFactorize + " was added to list.");
				break;
			} else {
				currentPrime = nextPrime(currentPrime);
			}
		}	  
		  
		  return fakt;	  
    } // end factorize


    int nextPrime(int i) { // returns next prime number after number 'i' 
    	int k ; 
    	
    	if ((i&1)==0){ // if i is even, start at i+1 
    		k =i+1; 
    	} else { // next possible prime 
    		k = i+2; 
    	} 
    	
    	while (!isPrime(k)){
    		k+=2; 
    	}
    	return k; 
    } // end nextTrue
    


	void printAllPrimes(){
		// Print bitmap 
		 
		// Reads bitArr and prints prime numbers
		 int primeCounter = -1;		// Increments to odd numbers
		 int bitCounter = 0;
		 System.out.println("Primenumbers: ");
		 System.out.println(2);
		 for (byte b : bitArr ) {
			  for ( int mask = 0x01; mask != 0x100; mask <<= 1 ) {
			      boolean value = ( b & mask ) != 0;	// Reads a bit and produces a boolean value. 0 = false, 1 = true.
			      
			      if(bitCounter != 7 || bitCounter == 0) {
			    	  //System.out.println("bitcounter = " + bitCounter + ", bitCounter%7 = " + (bitCounter%7));
			    	  primeCounter = primeCounter + 2;	// We are adding 2, so primeCounter is allways an odd number.
			    	  if(primeCounter>maxNum) return;	// Returns when we are investigating uninteresting odd numbers. (When primeCounter > maxNum)
			    	  //System.out.println("primeCounter = " + primeCounter);
			    	  if(value) {
			    		  // Is a prime
				    	  System.out.println(primeCounter);
				      }
			    	  bitCounter++;
			      } else {
			    	  bitCounter = 0;	// To skip the 8th bit which contains +/- operator.
			      }
			      
			  }
			}
		 
		 /*System.out.println("Print 2:");
		for ( int i = 2; i <= maxNum; i++) if (isPrime(i)) System.out.println(" "+i);*/

	}

	void generatePrimesByEratosthenes() {
		  // krysser av alle  oddetall i 'bitArr[]' som ikke er primtall (setter de =0)
		       crossOut(1);      // 1 is not a prime

		       int maxNumSquared = (int)Math.sqrt(maxNum);

		       for(int i = 3; i < maxNumSquared; i++) {

		    	   if(isPrime(i)) {
		    		   int sum = 0;
		    		   int counter = 0;
		    		   crossOut(i*i);

		    		   while(sum<=maxNum) {
		    			   
		    			   counter = counter + 2;
		    			   sum = (i*i)+((counter)*i);
		    			   if(sum < maxNum) {
		    				   crossOut(sum);
		    			   }
		    		   }
		    	   } else {
		    		   //System.out.println(i + " is not a prime. it is an even number.");
		    	   }
		       }
		       // < din Kode her, kryss ut multipla av alle primtall <= sqrt(maxNum),
		       // og start avkryssingen av neste primtall p med p*p>

	} // end generatePrimesByEratosthenes


} // end class Bool


