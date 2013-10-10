/**
 * The ElementaryCA main file. Runs a cellular automata simulation.
 * 
 * This file requires pj2 to run, it is a pj2 task. The simulation
 * is parallelized by running the update loop in a parallelFor. Each
 * cell in the world could potentially be run in parallel. It prints 
 * the min, max and final population count with the steps at which 
 * they first happened.
 * 
 * @author Matthew Cheman mnc3139
 */

import edu.rit.pj2.IntVbl;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;

public class ElementaryCA extends Task{
	
	private String usage = "Usage: java pj2 ElementaryCA rule N S index...";
	
	//args
	private int n;
	private int steps;
	
	private int[] ruleArray;
	private int[] world;
	private int[] newWorld;
	
	private IntVbl.Sum popcount;
	private int minPopcount = Integer.MAX_VALUE;
	private int minStep;	
	private int maxPopcount = Integer.MIN_VALUE;
	private int maxStep;
	
	
	/**
	 * This finds generates the index of the applicable rule from the current cell index (i).
	 * ruleArray is organized such that the binary value of the cell and its neighbors is the
	 * index of the applicable rule. Ex: 101 where the cells are left-cell-right is the index
	 * 5
	 * 
	 * @param i the index of the current cell
	 * @return an index into the ruleArray
	 */
	private int getRuleIndex(int i){
		int left = (i-1 >= 0) ? world[i-1] : world[world.length-1];
		int right = (i+1 < world.length) ? world[i+1] : world[0];
				
		return left * 4 + world[i] * 2 + right;
	}
	
	/**
	 * This does the actual cell simulation. It prints out the min, max and final
	 * population counts along with their steps number. Uses a parallel reduction 
	 * on the population count and a parallelFor to find the next move. 
	 * 
	 */
	private void calculate(){
		for (int i = 0; i < steps; i++){			
			if (popcount.item > maxPopcount){
				maxPopcount = popcount.item;
				maxStep = i;
			} else if (popcount.item < minPopcount){
				minPopcount = popcount.item;
				minStep = i; 
			}			
			popcount.item = 0;
			
			parallelFor(0, world.length-1).exec(new Loop(){
				IntVbl.Sum popcountThl;
				public void start(){
					popcountThl = threadLocal(popcount);
				}
				public void run(int i){
					newWorld[i] = ruleArray[getRuleIndex(i)];
					if (newWorld[i] == 1)
						popcountThl.item++;
				}
			});
			//swap current world with the newly calculated world
			int[] temp = world;
			world = newWorld;
			newWorld = temp;		
		}

		System.out.println("Minimum popcount: " + minPopcount + " at step " + minStep);
		System.out.println("Maximum popcount: " + maxPopcount + " at step " + maxStep);
		System.out.println("Final popcount: " + popcount.item + " at step " + steps);
	}
	
	/** 
	 * Reads in the rule followed by the world length and the number of iterations followed by
	 * any the indices of any alive cells. Then sets up the world, the ruleArray and sets the 
	 * first population count.
	 * 
	 * @param args The command line arguments
	 */
	public void main(String[] args) {		
		if (args.length < 3) // not the required args
		{
			System.out.println(usage);
			return;
		}
		
		//initialization
		n = Integer.parseInt(args[1]);
		steps = Integer.parseInt(args[2]);		
		world = new int[n];
		newWorld = new int[n];
		popcount = new IntVbl.Sum(args.length - 3);	//args.length - 3 is the number of indices of alive cells
		
		//assign alive cells to corresponding index in world
		for (int i = 3; i < args.length; i++){
			world[Integer.parseInt(args[i])] = 1;
		}
		//make instant access rule array
		String rule = args[0];
		ruleArray = new int[rule.length()];
		for (int i = 0; i < rule.length(); i++)
		{
			ruleArray[i] = (rule.charAt(i) == '1') ? 1 : 0;
		}
		

		calculate();
		
	}

}
