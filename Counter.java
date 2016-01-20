/**
 * Keeps track of when to update the Temperature.
 * Every 15 codelets, the temperature is to be
 * updated.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Counter {
	private int i;
	private Temperature t;
	private Slipnet sn;
	
	public Counter(Temperature t, Slipnet sn){
		this.t = t;
		this.sn = sn;
		i = 15;}
	
	/**
	 * Counts how many codelets have run so far.
	 * Updates the temperature when 15 codelets have
	 * run and resets the counter.
	 */
	public void count(){
		if(i > 0){
			i--;}
		else{
			i = 15;
			update();}}
	
	/**
	 * Calls methods to decay the nodes in the slipnet
	 * and update the temperatures.
	 */
	public void update(){
		sn.decay();
		t.updateTemp();}

}
