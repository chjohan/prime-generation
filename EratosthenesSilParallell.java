///--------------------------------------------------------
//
//   File: EratosthenesSilParallell.java for INF2440

//   implements bit-array (Boolean) for prime numbers
//   written by:  Arne Maus , Univ of Oslo, modified by Christian Johansen
//
//
//
//--------------------------------------------------------
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
/**
* Implements the bitArray of length 'maxNum' [0..bitLen ]
*   1 - true (is prime number)
*   0 - false
*  can be used up to 2 G Bits (integer range)
*  Stores info on prime/not-prime in bits 0..6 in each byte
*  (does not touch the sign-bit - bit7)
*/

// TODO: Tidtagning - finn hvor jeg kan skrive ut tidtagning av faktorisering. Evt. opprete en barrier.
public class EratosthenesSilParallell{
	byte [] bitArr ;           // bitArr[0] represents the 7 integers:  1,3,5,...,13, and so on
	int  maxNum;               // all primes in this bit-array is <= maxNum
	final  int [] bitMask = {1,2,4,8,16,32,64};  // kanskje trenger du denne
	final  int [] bitMask2 ={255-1,255-2,255-4,255-8,255-16,255-32,255-64}; // kanskje trenger du denne
	int cores = (Runtime.getRuntime().availableProcessors() -1);
	int numberOfBits;
	long currentNumberToBeFactorized;
	CyclicBarrier cbMain;
	CyclicBarrier cbFact;
	CyclicBarrier cbEndFact;
	CountDownLatch latch = new CountDownLatch(1);
	int[][] primes;
	int[][] factPrimes;
	Thread[] t = new Thread[cores];
	Thread[] t2 = new Thread[cores];
	int numberOfPrimes = 0;		// Number of primes up to sqrt(maxNum)
	int totalNumberOfPrimes = 0;	// Number of primes up to maxnum
	ArrayList<Long> factors;
	
	// Time meassuring
	long startTime;
	long endTime;
	long elapsedTime;
	double millisecs;

	EratosthenesSilParallell (int maxNum) {
		System.out.println("Parallell kjoering.");
      this.maxNum = maxNum;
      System.out.println("Max primtall m:" + maxNum);
      bitArr = new byte [(maxNum/14)+1];
      numberOfBits = bitArr.length * 8;
      factors = new ArrayList<Long>();
      setAllPrime();
      startTime = System.nanoTime();
      generatePrimesToSquaredRootOfN();
      sendPrimes();
      
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
	
  // Generates prime numbers < squaredRootOfN
  void generatePrimesToSquaredRootOfN() {
  	crossOut(1);
  	
  	int maxNumSquared = (int)Math.sqrt(maxNum);
  	int maxNumBitsSquared = (int)Math.sqrt(numberOfBits);
  	
	       for(int i = 3; i < maxNumBitsSquared; i++) {

	    	   if(isPrime(i)) {
	    		   numberOfPrimes++;
	    		   int sum = 0;
	    		   int counter = 0;
	    		   if(i*i < maxNumSquared) {
	    			   crossOut(i*i);
	    		   }

	    		   while(sum<=maxNumSquared) {
	    			   
	    			   counter = counter + 2;
	    			   sum = (i*i)+((counter)*i);
	    			   if(sum < maxNumSquared) {
	    				   crossOut(sum);
	    			   }
	    		   }

	    	   }
	       }

  }
  
  void findTotalNumOfPrimes() {
	  for ( int i = 2; i < maxNum; i++) {
		  if (isPrime(i)) {
			  totalNumberOfPrimes++;
		  }
	  }
  }
  
	void checkFactors() {
		// Multiply all factors and check if they are equal to currentNumberToBeFactorized.
		// If not, add M/facProd as last factor
		long facProd = 1;
		
		if(factors.size()==0) {
			// Current number is a prime and can not be factorized.
			return;
		} else if(factors.size()==1) {
			if(factors.get(0) != currentNumberToBeFactorized) {
				factors.add((currentNumberToBeFactorized/factors.get(0)));
			} else {
				// Current number is a prime. Return.
				return;
			}
		} else {	// We have 2 or more factors to test. Multiply factors and check product.
			for (int i = 0; i < factors.size(); i++) {
			    facProd = facProd * factors.get(i);
			}
			if(facProd == currentNumberToBeFactorized) {
				// Great! We found all the factors.
				return;
			} else {
				// We are missing a factor.
				factors.add((currentNumberToBeFactorized/facProd));
			}
		}
		
	}
  
  void startFact() {
	  findTotalNumOfPrimes();
	  int primesPerThread = totalNumberOfPrimes/cores;
	  int primeRest = totalNumberOfPrimes%cores;
	  int numberOfPrimesToSendCounter;
	  int numberOfPrimesToSend;
	  int counter = 2;
	  int arrayCounter;
	  factPrimes = new int[cores][(totalNumberOfPrimes/cores)+10];
	  cbFact = new CyclicBarrier(cores, new Runnable(){
		  @Override
		  public void run() {
			  // This task will be executed once all threads reaches barrier
			  
			  checkFactors();
			  if(factors.size()!=0) {
				  System.out.print(currentNumberToBeFactorized + " = " + factors.get(0));
			  } else {
				  System.out.print(currentNumberToBeFactorized + " = " + currentNumberToBeFactorized);
			  }
			  
			  if(factors.size()>1) {
		    		for(int k = 1; k < factors.size(); k++) {
		    			System.out.print(" * " + factors.get(k));
		    		}
			  }
			  System.out.println();
			  
			  
			  factors = new ArrayList<Long>();
		  }
	  });
	  
	  // When this barrier is released, measure end time and print time result.
	  cbEndFact = new CyclicBarrier(cores, new Runnable(){
		  @Override
		  public void run() {
			  // This task will be executed once all threads reaches barrier
			  
			  // Complete time measurement for factorization.
			  endTime = System.nanoTime();
		      elapsedTime = endTime - startTime;
		      millisecs = (double)elapsedTime / 1000000.0;
		      System.out.println("\n100 faktoriseringer med utskrift tok: " + millisecs + " millisek.");
		      System.out.println("Dvs: " + millisecs/100 + " millisek per faktorisering.");
		  }
	  });
	  
	  for(int i = 0; i < cores; i ++) {

		  arrayCounter = 0;
		  if(primeRest!=0) {
			  numberOfPrimesToSendCounter = primesPerThread+1;
			  numberOfPrimesToSend = primesPerThread+1;
			  primeRest--;
		  } else {
			  numberOfPrimesToSendCounter = primesPerThread;
			  numberOfPrimesToSend = primesPerThread;
		  }
		  for(int k = counter; k < maxNum; k++) {
			 
			  if(numberOfPrimesToSendCounter!=0) {
				  if(isPrime(k)) {
					  factPrimes[i][arrayCounter] = k;
					  numberOfPrimesToSendCounter--;
					  arrayCounter++;
				  }
			  } else {
				  break;
			  }
			  counter = k+1;
		  }
		  (t2[i] = new Thread(new ParaFact(i, factPrimes, numberOfPrimesToSend, cbFact, cbEndFact))).start();	// Starting thread.
		  
  }
	  
  }
  
  void sendPrimes() {
	  int primesPerThread = numberOfPrimes/cores;
	  int primeRest = numberOfPrimes%cores;
	  int numberOfPrimesToSendCounter;
	  int numberOfPrimesToSend;
	  int counter = 3;
	  int arrayCounter;
	  primes = new int[cores][primesPerThread*primesPerThread];
	  cbMain = new CyclicBarrier(cores, new Runnable(){
		  @Override
		  public void run() {
			  // This task will be executed once all threads reaches barrier
			  //printAllPrimes();
			  endTime = System.nanoTime();
		      elapsedTime = endTime - startTime;
		      millisecs = (double)elapsedTime / 1000000.0;
		      System.out.println("\nGenererte alle primtall <= " + maxNum + " paa: " + millisecs + " millisek.\n");
		      startTime = System.nanoTime();
			  startFact();
		  }
	  });
	  
	  
		  for(int i = 0; i < cores; i ++) {

			  arrayCounter = 0;
			  if(primeRest!=0) {
				  numberOfPrimesToSendCounter = primesPerThread+1;
				  numberOfPrimesToSend = primesPerThread+1;
				  primeRest--;
			  } else {
				  numberOfPrimesToSendCounter = primesPerThread;
				  numberOfPrimesToSend = primesPerThread;
			  }
			  for(int k = counter; k < Math.sqrt(numberOfBits); k++) {
				 
				  if(numberOfPrimesToSendCounter!=0) {
					  if(isPrime(k)) {
						  primes[i][arrayCounter] = k;
						  //System.out.println("PrimeNumber " + k + " was added to primes[" + i + "][" + arrayCounter + "]");
						  numberOfPrimesToSendCounter--;
						  arrayCounter++;
					  }
				  } else {
					  break;
				  }
				  counter = k+1;
			  }
			  //System.out.println("i = " + i + " numberOfPrimesToSend: " + numberOfPrimesToSend);
			  (t[i] = new Thread(new Para(i, primes, numberOfPrimesToSend, bitArr, cbMain))).start();	// Starting thread.
			  
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
    
   



	void printAllPrimes(){
		// Print bitmap 
		 

		for (byte b : bitArr)
	    {
	        // Add 0x100 then skip char(0) to left-pad bits with zeros
	        System.out.println(Integer.toBinaryString(0x100 + b).substring(1));
	    }
		 

	}


	
	class Para implements Runnable {
		int threadNumber;
		int[][] primes;
		int nOPrimes;
		byte[] threadArr;
		CyclicBarrier cb;
		
		Para(int threadNumber, int[][] primes, int nOPrimes, byte[] arr, CyclicBarrier cb) {
			this.threadNumber = threadNumber;
			this.primes = primes;
			this.nOPrimes = nOPrimes;
			threadArr = new byte [(maxNum/14)+1];
			threadArr = arr.clone();
			this.cb = cb;
		}
		
		public void run() {		
			generatePrimesByEratosthenes();
			try {
		           cb.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
		}
		
		// Merges N -core thread byte-arrays with the global bitArr array.
		synchronized void mergeByteArrays() {
			for(int i = 0; i < bitArr.length; i++) {
				bitArr[i] &= threadArr[i]; 
			}
		}
		
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
		
	    void crossOut(int i) {
	        // set as not prime- cross out (set to 0)  bit represening 'int i'
	              threadArr[i/14] &= bitMask2[(i%14)>>1];
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
	       		return (threadArr[i/14] & bitMask[(i%14)>>1]) != 0;
	       	}
	   	 }
		
	    // Generates prime numbers < squaredRootOfN
	    /*void generatePrimesToSquaredRootOfN() {
	    	crossOut(1);
	    	
	    	int maxNumSquared = (int)Math.sqrt(maxNum);
	    	int maxNumBitsSquared = (int)Math.sqrt(numberOfBits);
	    	System.out.println("Number of bits: " + numberOfBits);
	    	System.out.println("Number of bits squared: " + maxNumBitsSquared);
	    	//System.out.println("MaxNumSquared: " + maxNumSquared);
	    	
		       for(int i = 3; i < maxNumBitsSquared; i++) {

		    	   if(isPrime(i)) {
		    		   int sum = 0;
		    		   int counter = 0;
		    		   if(i*i < maxNumSquared) {
		    			   crossOut(i*i);
		    		   }

		    		   while(sum<=maxNumSquared) {
		    			   
		    			   counter = counter + 2;
		    			   sum = (i*i)+((counter)*i);
		    			   if(sum < maxNumSquared) {
		    				   crossOut(sum);
		    			   }
		    		   }

		    	   }
		       }
		       
	    }*/
	       
		void generatePrimesByEratosthenes() {
			  // krysser av alle  oddetall i 'bitArr[]' som ikke er primtall (setter de =0)
			       crossOut(1);      // 1 is not a prime


			       for(int i = 0; i < primes[threadNumber].length; i++) {

			    	   if(isPrime(primes[threadNumber][i])) {
			    		   int sum = 0;
			    		   int counter = 0;
			    		   int number = primes[threadNumber][i];
			    		   crossOut(number*number);
			    		   //System.out.println((number * number) + " is not a prime, and was crossed out.");

			    		   while(sum<=maxNum) {
			    			   
			    			   counter = counter + 2;
			    			   sum = (number*number)+((counter)*number);
			    			   if(sum < maxNum) {
			    				   crossOut(sum);
			    				   //System.out.println((sum) + " is not a prime, and was crossed out.");
			    			   }
			    		   }
			    	   } else {
			    		   //System.out.println(i + " is not a prime. it is an even number.");
			    	   }
			       }

			       mergeByteArrays();	// Merges the current threads' threadArr with bitArr.
		} // end generatePrimesByEratosthenes
	}
	
	class ParaFact implements Runnable {
		int threadNumber;
		int[][] primes;		// Which primes current thread can use for factorization.
		int nOPrimes;
		CyclicBarrier cb;
		CyclicBarrier cbEndFact;
		long m = ((long)maxNum * (long)maxNum);
		long startFactorizingAt = m - 100;
		
		
		ParaFact(int threadNo, int[][] primes, int nOPrimes, CyclicBarrier cb, CyclicBarrier cbEndFact) {
			this.threadNumber = threadNo;
			this.primes = primes;
			this.nOPrimes = nOPrimes;
			this.cb = cb;
			this.cbEndFact = cbEndFact;
		}
		
		public void run() {
			for(long i = startFactorizingAt; i < m; i++) {			
				currentNumberToBeFactorized = i;
				factorize(i);

				try {
			           cb.await();
			         } catch (InterruptedException ex) {
			           return;
			         } catch (BrokenBarrierException ex) {
			           return;
		         }
			}
			
			// All factors have been calculated and printed.
			try {
					cbEndFact.await();
		         } catch (InterruptedException ex) {
		           return;
		         } catch (BrokenBarrierException ex) {
		           return;
	         }
			
		}
		
		synchronized void addToArrayList(long i) {
			factors.add(i);
		}
		
		
	    void factorize (long num) {
	    	int arrayCounter = 0;
			int currentPrime = primes[threadNumber][arrayCounter];
			long numberToFactorize = num;
			
			for(int i = 0; i < primes[threadNumber].length; i++) {
				if(numberToFactorize%currentPrime == 0) {
					addToArrayList((long)currentPrime);
					numberToFactorize = numberToFactorize/currentPrime;
					i = 0;
				} else if(i < primes[threadNumber].length && primes[threadNumber][i] != 0) {
					currentPrime = primes[threadNumber][i];
				}
			}
	    }
	    
	}


} // end class Bool


