import edu.rit.pj2.IntVbl;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;



/*
parallel for's each move

wait gate thing look up name

//parallel for the move replacement as each will only affect its position in the array
use a pointer swap

do a zombie variable thing to keep track of current popcount

when value of task's popcounts bubbles up to one var then compare to min and max popcount
replace if nessissary

if final step print popcount and stepnum
and min and max then quit
*/
/*
01111000 20000 20000 0 9999

Minimum popcount: 2 at step 0
Maximum popcount: 10394 at step 6386
Final popcount: 10135 at step 20000
*/

public class ElementaryCA extends Task{
	
	private String usage = "NO";
	
	//args
	private String rule;
	private int n;
	private int steps;
	private int[] indices;
	
	private int[] ruleArray;
	private int[] world;
	private int[] newWorld;
	
	private IntVbl.Sum popcount;
	private int minPopcount = Integer.MAX_VALUE;
	private int minStep;	
	private int maxPopcount = Integer.MIN_VALUE;
	private int maxStep;
	
	
	
	private void printWorld(){
		for (int i = 0; i < world.length; i++){
			System.out.print(world[i]);
		}
	}
	
	private int getRuleIndex(int i){
		int left = (i-1 >= 0) ? world[i-1] : world[world.length-1];
		int right = (i+1 < world.length) ? world[i+1] : world[0];
				
		return left * 4 + world[i] * 2 + right;
	}
	

	public void main(String[] args) {
		
		if (args.length < 3)
		{
			System.out.println(usage);
			return;
		}
		rule = args[0];
		n = Integer.parseInt(args[1]);
		steps = Integer.parseInt(args[2]);
		
		world = new int[n];
		newWorld = new int[n];
		indices = new int[args.length - 3];
		popcount = new IntVbl.Sum(0);
		for (int i = 3; i < args.length; i++){
			world[Integer.parseInt(args[i])] = 1;
		}
		popcount.item = args.length - 3;
		
		//make instant access rule array
		ruleArray = new int[rule.length()];
		for (int i = 0; i < rule.length(); i++)
		{
			ruleArray[i] = (rule.charAt(i) == '1') ? 1 : 0;
		}

		
		printWorld();
		System.out.println("   | " + popcount.item);
		
		for (int i = 0; i < steps; i++){
			
			
			if (popcount.item > maxPopcount)
			{
				maxPopcount = popcount.item;
				maxStep = i;
			}
			if (popcount.item < minPopcount)
			{
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
			
			
			//printWorld();
			//System.out.println("   | " + popcount.item);
		
		}
		printWorld();
		System.out.println("   | " + popcount.item);
		System.out.println("Minimum popcount: " + minPopcount + " at step " + minStep);
		System.out.println("Maximum popcount: " + maxPopcount + " at step " + maxStep);
		System.out.println("Final popcount: " + popcount.item + " at step " + steps);
		
		
		

	}

}
