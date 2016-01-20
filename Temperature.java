/**
 * Controls the amount of randomness in the system.
 * Updating every 15 relevant method steps.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Temperature {
	private WorkSpace ws;
	private Importance imp;
	private Happiness hap;
	private int temp = 80;
	
	public Temperature(WorkSpace ws, Importance imp, Happiness hap){
		this.ws = ws;
		this.imp = imp;
		this.hap = hap;}
	
	/**
	 * Adjusts the strength according to given formula
	 * @param oldStr The strength being adjusted.
	 * @return The new strength.
	 */
	public int adjustStrength(int oldStr){
		double oldStrength = (double) oldStr;
		double multiply = (100 - temp) + 15;
		multiply = multiply/30;
		double newStr = multiply*oldStrength;
		int strength = (int) newStr;
		return strength;}
	
	/**
	 * To calculate the new temperature. The temperatures is:
	 * (0.8*[the weighted average of the unhappiness of all objects, weighted by their relative importance]) 
	 * 							+ (0.2*[100-strength(rule)])
	 * @return The updated temperature
	 */
	public int updateTemp(){
		int ruleStrength = 0;
		double nT = 0;
		if(ws.getInitialRule() != null){
			ruleStrength = adjustStrength(ws.getRuleStrength());}
		nT =(0.8*(double)avgUnhapImp()) + 0.2*(double)ruleStrength;
		temp = (int)nT;
		return temp;}
	
	/**
	 * Calculate the average unhappiness of all the structures in the program according
	 * to their importance
	 * @return The average unhappiness value
	 */
	public int avgUnhapImp(){
		int a = 0;
		a = a + allDescrips("initDescrip") + allDescrips("targDescrip");
		a = a + allBonds("initBonds") + allBonds("targBonds");
		a = a + allGroups("initGrouping") + allGroups("targGrouping");
		a = a/8;
		return a;}

	/**
	 * Calculate the importance of all description structures and 
	 * their unhappiness combined 
	 * @param name Reference to which description structure
	 * @return The average unhappiness value
	 */
	public int allDescrips(String name){ 
		int avg = 0;
		int[] impor = imp.getImportance(name);
		int counter = 0;
		for(int i = 0; i < impor.length; i++){
			int j = 0;
			j = ((100-impor[i]) + happDescrips(name,i))/2;
			avg = avg + j;
			counter++;}
		avg = avg/counter;
		return avg;}
	
	/**
	 * Calculate the happiness of all descriptions
	 * @param Return the happiness of all descriptions
	 * @return The average unhappiness and importance of the structure
	 */
	public int happDescrips(String name, int pos){
		int total = 0;
		int counter = 0;
		int[][] obj = ws.getDescripStrength(name);
		if(obj != null){
			for(int i = 0; i < obj[pos].length; i++){
				total = total + obj[pos][i];
				counter ++;}
			counter = total/counter;}
		return counter;}
	
	/**
	 * Will calculate the unhappiness combined with the importance of
	 * each object of all bonds.
	 * @param name The name of the structures
	 * @return The average unhappiness and importance of the structure
	 */
	public int allBonds(String name){
		int[] impor = imp.getBondImp(name);
		int[] happi = hap.getBondHap(name);
		int avg = 0;
		int counter = 0;
		if(impor != null && happi != null){
			for(int i = 0; i < impor.length; i++){
				int a = ((100 - impor[i]) + happi[i])/2;
				avg = avg + a;}
			avg = avg/counter;
			counter ++;}
		return avg;}
	
	/**
	 * Calculate the unhappiness combined with importance of all group
	 * structures
	 * @param name The name of which structure
	 * @return The average unhappiness and importance of the structure
	 */
	public int allGroups(String name){
		int impor = imp.getGroupingImp(name);
		int happi = 100 - hap.getGroupHap(name);
		return (impor + happi)/2;}
	
	/**
	 * Set the correspondence importance to 80 to stop it having
	 * too much negative impact on the final temperature. 
	 * Represents the average unhappiness and importance of the structure.
	 * @param name Which correspondence structure
	 * @return The average unhappiness and importance of the structure
	 */
	public int allCorres(String name){
		return 80;}
	
	/**
	 * Get the temperature
	 * @return The temperature
	 */
	public int getTemp(){
		return temp;}

	/**
	 * The formula for adjusting probabilities according
	 * to temperature. A converted version from the one given 
	 * in the book. 
	 * @param prob The probability to be filtered
	 * @return The new, filtered probability
	 */
	public int adjustProb(int prob){
		int newP = 0;
		double p = 0;
		if(prob == 100){
			 p = 1;}
		else{
			p = ((double)prob)/100;}
		double temperature = (double)temp;
		double t1 = 0;
		double t2 = 0;
		double t3;
	
		if(prob == 0){
			newP = 0;
		}
		else if(prob <= 50){
			//calculates term1
			t1 = Math.log10(p);
			t1 = Math.abs(t1);
			t1 = roundToZero(t1);
			if(t1 < 1){
				t1 = 1;}
			
			//calculates term2
			t2 = -(t1 - 1);
			t2 = Math.pow(10,t2);
			
			//calculates the adjustment
			t3 = Math.sqrt(100 - temperature);
			t3 = 10 - t3;
			t3 = t3/100;
			t3 = t3*(t2 - p);
			t3 = p + t3;
			if(t3 > 0.5){
				t3 = 0.5;}
			t3 = t3*100;
			newP = (int)t3;
		}
		else if(prob > 50){
			t1 = 1-p;
			t2 = Math.sqrt(100 - temperature);
			t2 = 10 - t2;
			t2 = t2/100;
			t2 = t2*p;
			t1 = 1 - (t1 + t2);
			if(t1 < 0.5){
				t1 = 0.5;}
			t1 = t1*100;
			newP = (int)t1;
		}
		return newP;
	}
	
	/**
	 * Java version of Common Lisp's truncate. Rounds to 0, 
	 * to be used when adjusting probability.
	 * @param d The 
	 * @return The number rounded to 0
	 */
	public double roundToZero(double d){
		double newD = 0;
		if(d > 0){
			newD = Math.floor(d);}
		else{
			newD = Math.ceil(d);}
		return newD;}
	
}
