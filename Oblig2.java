import java.util.*;

public class Oblig2 {
        /**
        * Handles arguments and starts the show.
        * 
        * @param    arg[0]  [number] upper limit of primenumbers
        * @param    arg[1]  [char] 'p' for parallel version, 's' for sequential version.
        */
	public static void main(String[] args) {
		int maxNum = 0;
		String seqOrPara;
                
		if(args.length==2) {
			maxNum = Integer.parseInt(args[0]);
			seqOrPara = args[1];
		
			if(maxNum>14 && seqOrPara.equals("s")) {
				EratosthenesSil e = new EratosthenesSil(maxNum);	
			} else if(maxNum>14 && seqOrPara.equals("p")) { 
				EratosthenesSilParallell v = new EratosthenesSilParallell(maxNum);
			} else if(maxNum<14) {
				System.out.println("First argument must be greater than 14.");
			} else if(!seqOrPara.equals("s") && !seqOrPara.equals("p")) {
				System.out.println("Second argument must eiter be 's' or 'p'. 'S' for sequential execution or 'p' for parallell execution.");
			}
		} else {
			System.out.println("You must run this program with two argumets:");
			System.out.println("Ex. java Oblig2 100 s");
			System.out.println("Where the integer is number of primes, and the second argument is either 's' or 'p'.");
			System.out.println("s = sequential execution & p = parallell execution.");
		}
	}
}
