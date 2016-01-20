import java.util.ArrayList;
import java.util.Random;

/**
 * Bonds adjacent letters. 
 * Only the Initial and Target strings have bonds built
 * in the workspace.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Bonds {
	private Slipnet sn;
	private WorkSpace ws;
	private Temperature temp;
	private Counter count;
	private Random rand = new Random();
	private ArrayList<Integer> toChange;
	//[adjacent pairing][bond type][direction][offset]
	private String bonds[][];
	private static final String SAMENESS = "sameness";
	private static final String SUCCESSOR = "successor";
	private static final String PREDECESSOR = "predecessor";
	private static final String LEFT = "left";
	private static final String RIGHT = "right";
	private int numPredLeft;
	private int numPredRight;
	private int numSucLeft;
	private int numSucRight;
	private boolean sameExists;
	
	//[adjacent pairing][bond type][direction][offset]
	public Bonds(Slipnet sn, WorkSpace ws, Temperature temp, Counter count){
		this.count = count;
		this.sn = sn;
		this.ws = ws;
		this.temp = temp;}
	
	/**
	 * To check the bonds of all strings
	 * @param name The name of the string, Initial, Modified or Target
	 * @return If there is a sameness bond in the string
	 */
	public boolean checkBonds(String name){
		sameExists = false;
		String[][] descrips = ws.getAllDescrip(name);
		String[][] toStore = evalAll(descrips);
  		ws.storeBonds(toStore, name);
		printOut(name);
  		count.count();
  		return sameExists;}
	
	/**
	 * Evaluates whole original string.
	 * @param str The raw string being evaluated, Initial or Target
	 * @param name Refers to the identity of the raw input, Initial or Target
	 * @return The array of bonds for that string.
	 */
	private String[][] evalAll(String[][] descrips){
		bonds = new String[descrips.length-1][3];
		String[] s = new String[2];
 		for(int i = 0; i < descrips.length-1; i++){
			//gets the bond descriptions for the pair
			s = checkAdj(descrips[i][0], descrips[i+1][0], i);
			bonds[i] = s;}
		return bonds;}
	
	/**
	 * Decides bond between two chosen objects.
	 * @param a The chosen object
	 * @param b The object adjacent to the chosen object
	 * @param pos The position of the chosen letter object in the array of bonds
	 */
	private String[] checkAdj(String a, String b, int pos){
		count.count();
		String newA = a;
		String newB = b; 
		
		String compareA = newA.substring(newA.length()-1);
		String compareB = newB.substring(newB.length()-1);
		
		String[] s = new String[4];
		s[0] = a + "-" + b;
		
		int offset = newA.compareTo(newB);
		
		//for sameness bonds
		if(compareA.equals(compareB)){
			sameExists = true;
			s = new String[3];
			s[0] = a + "-" + b;
			s[1] = SAMENESS;
			s[2] = "";
			sn.activate(SAMENESS);}
		
		//if alphabetically going backwards so z-a
		else if(offset <= 3 && offset >= 1 ){
			String choice = pick();
			if(choice.equals(PREDECESSOR)){
				s[1] = PREDECESSOR;
				s[2] = RIGHT;
				s[3] = offset + "" ;
				sn.activate(PREDECESSOR);
				sn.activate(RIGHT);}
			else if(choice.equals(SUCCESSOR)){
				s[1] = SUCCESSOR;
				s[2] = LEFT;
				s[3] = offset+ "";
				sn.activate(SUCCESSOR);
				sn.activate(LEFT);}}
		
		//if alphabetically adjacent to the right, eg. a-b, b-c, q-r
		else if(offset <= -1 && offset >= -3){
			String choice = pick();
			if(choice.equals(PREDECESSOR)){
				s[1] = PREDECESSOR;
				s[2] = LEFT;
				s[3] = offset+ "";
				sn.activate(PREDECESSOR);
				sn.activate(LEFT);}
			else if(choice.equals(SUCCESSOR)){
				s[1] = SUCCESSOR;
				s[2] = RIGHT;
				s[3] = offset+ "";
				sn.activate(SUCCESSOR);
				sn.activate(RIGHT);}}
		return s;}
	
	/**
	 * Probabilistically decides if the bonds is 'successor' or 'predecessor'.
	 * If a predecessor bond already exists, probability of return
	 * being a predecessor increases, this is the same with the
	 * successor bond.
	 * Probability will change depending on if one of those bonds already exists.
	 * @return The winning bond; successor or predecessor.
	 */
	private String pick(){
		Random prob = new Random();
		String choice = null;
		//if predecessor activation greater
		if(sn.getNodeActivation(PREDECESSOR) > sn.getNodeActivation(SUCCESSOR)){
			if(prob.nextInt(100) < newProb(PREDECESSOR,80)){
				choice = PREDECESSOR;}
			else{
				choice = SUCCESSOR;}}
		
		//if successors activation level greater
		else if(sn.getNodeActivation(SUCCESSOR) > sn.getNodeActivation(PREDECESSOR)){
			if(prob.nextInt(100) < newProb(SUCCESSOR, 80)){
				choice = PREDECESSOR;}
			else{
				choice = SUCCESSOR;}}
		
		//if both equally active 50/50
		else if(sn.getNodeActivation(SUCCESSOR) == sn.getNodeActivation(PREDECESSOR)){
			if(prob.nextInt(100) < newProb(PREDECESSOR, 50)){
				choice = PREDECESSOR;}
			else{
				choice = SUCCESSOR;}}
		
		//if neither active 50/50
		else{
			if(prob.nextInt(100) < newProb(PREDECESSOR, 50)){
				choice = PREDECESSOR;}
			else{
				choice = SUCCESSOR;}}
		return choice;}
	
	/**
	 * Calculates the new probability.
	 * If other bond descriptions have the same description as
	 * the input and the same direction, the probability of the 
	 * description in question being used will increase.
	 * @param bond The bond description being checked
	 * @param oldProb The probability to be added to
	 * @return The new probability of that description being used
	 */
	private int newProb(String bond, int oldProb){
		count.count();
		int p = oldProb;
		//probability of the type being the same
		int probType = existing(bond, 1, oldProb, bonds);
		p = p + probType;
		p = p/2;
		//adjusts the probability according to temperature
		//unfortunately it just takes it down to 0
		p = temp.adjustProb(p);
		return p;}
	
	/**
	 * Returns the probability based on already existing bonds. If the same bond as the existing bond
	 * already exists, then the probability of that bond being used increases.
	 * @param bond The bond type we are trying to find.
	 * @param pos Will be 1 or 2, depends on if evaluating type or direction.
	 * @param oldProb The untouched probability.
	 * @param allBonds All the bonds of the string
	 * @return The probability of that bond or direction existing being applied.
	 */
	private int existing(String bond, int pos, int oldProb, String[][] allBonds){
		int prob = 0;
		//the number of bonds and direction in total
		int num = allBonds.length;
		//for all the bonds
		for(int i = 0; i < num; i++){
			if(allBonds[i][pos] != null){
				if(allBonds[i][pos].equals(bond)){
					prob = prob + 100;}
				else{
					prob = prob + oldProb;}}
			else{
				prob = prob + oldProb;}}
		if(prob == 0 && num == 0){
			prob = 0;}
		else{
		prob = prob/num;}
	   return prob;}	
	
	/**
	 * Check a rebonding is possible
	 * @param name Refers to the raw input, Initial or Target
	 * @return
	 */
	public boolean rebondable(String name){
		boolean b = false;
		if(ws.getAllBonds(name) != null){
			b = true;}
		return b;}
	
	/**
	 * Attempt to re-bond the unhappier bonded objects.
	 * @param The name of the raw input string, Initial or Target
	 */
	public void tryRebond(String name){
		String[][] current = ws.getAllBonds(name);
		numPredLeft = 0;
		numPredRight = 0;
		numSucLeft = 0;
		numSucRight = 0;
		toChange = new ArrayList<Integer>();
		//calculate the most common bond
		for(int i = 0; i < current.length; i++){
			if(current[i][2] != null){
				numSame(current[i][1], current[i][2]);}}
		
		String[] changeTo = mostCommon();
		//find all the bonds that don't equal the most common one
		for(int i = 0; i < current.length; i++){
			if(current[i][2] != null && !current[i][2].equals(SAMENESS)){
				change(current[i][1],current[i][2],changeTo,i);}}
		
		//attempt to re-bond some of the bonds
		for(int pos : toChange){
			rebond(name,pos);}
		printOut(name);}
	
	/**	
	 * Checks the number of 
	 * @param t The bond type
	 * @param d The type direction if applicable
	 */
	private void numSame(String type, String direc){
		if(type.equals(PREDECESSOR) && direc.equals(LEFT)){
			numPredLeft++;}
		else if(type.equals(PREDECESSOR) && direc.equals(RIGHT)){
			numPredRight++;}
		else if(type.equals(SUCCESSOR) && direc.equals(LEFT)){
			numSucLeft++;}
		else if(type.equals(SUCCESSOR) && direc.equals(RIGHT)){
			numSucRight++;}}
	
	/**
	 * Return the most common type direction combination.
	 */
	private String[] mostCommon(){
		//used to pick if 2 are equal
		boolean pick = false;
		if(rand.nextInt(100) < 50){
			pick = true;}
		
		int current = numPredLeft;
		String[] most = new String[2];
		most[0] = PREDECESSOR;	
		most[1] = LEFT;
		if(numPredRight > current || (numPredRight == current && pick)){
			most[0] = PREDECESSOR;
			most[1] = RIGHT;
			current = numPredRight;}
		if(numSucLeft > current || (numSucLeft == current && pick)){
			most[0] = SUCCESSOR;
			most[1] = LEFT;
			current = numSucLeft;}
		if(numSucRight > current || (numSucRight == current && pick)){
			most[0] = SUCCESSOR;
			most[1] = RIGHT;
			current = numSucRight;}
		return most;}
	
	/**
	 * Changes the bond of two adjacent objects
	 * @param type The type of bond
	 * @param direc The direction of the bond
	 * @param bonding The current bonds
	 * @param position The position of the objects being re-bonded
	 */
	private void change(String type, String direc, String[] bonding, int position){
		if(!bonding[0].equals(type) && !bonding[1].equals(direc)){
			//add position of bond to be changed
			toChange.add(position);}}

	/**
	 * Create new bond between two adjacent letters.
	 * @param name Name of the original string; initial, modified or target
	 * @param pos Position of the letter being re-bonded
	 */
	private void rebond(String name, int pos){
		count.count();
		String[] oldBond = ws.getBonds(name, pos);
		String[] str = oldBond[0].split("-"); 
		String[] newBond = checkAdj(str[0],str[1],pos);
		//if they aren't the same check strength
		if(!oldBond.equals(newBond)){
			String s = strongerBond(newBond, oldBond, ws.getAllBonds(name));
			//if the new bond is stronger, update
			if(s.equals("new")){
				count.count();
				ws.updateBond(name, pos, newBond);}}}
	
	/**
	 * Calculates strength for a bond.
	 * Calculated from type of bond, the activation level of that bond
	 * and the local support for that bond and direction if applicable. 
	 * @param bond All bonds descriptions
	 * @param allBonds All bonds for the whole string
	 * @param pos Position of the bond in the array of bonds
	 * @return The strength as a percentage.
	 */
	private int calculateStrength(String[] bond, String[][] allBonds){
		int strength = 0;
		String type = bond[1];
		int typesDepth = 0;
		int tyActiv = 0;
		int catSupport = 0;
		//conceptual depth
		if(type != null){
			typesDepth = sn.getDepth(type);
			//activation level
			tyActiv = sn.getNodeActivation(type);
			//probability given from supporting category
			catSupport = existing(type, 1, 0, allBonds);}
		if(type == null){
			strength = 0;}
		else if(type.equals(SAMENESS)){
			 strength = (typesDepth + tyActiv + catSupport)/3;}
		else{
			String direc = bond[2];
			//assess direction
			int dirActiv = sn.getNodeActivation(direc);
			int dirSupport = existing(bond[2], 2, 0, allBonds);
			strength = (typesDepth + tyActiv + catSupport + dirActiv + dirSupport)/5;}
		return strength;}
	
	/**
	 * Compare strengths.
	 * If the strengths are both equal to one another, there is
	 * a 50 percent chance
	 * @param newBond The descriptions of the new bond.
	 * @param oldBond The descriptions of the old bond.
	 * @return The stronger bond.
	 */
	private String strongerBond(String[] newBond, String[] oldBond, String[][] allBonds){
		String chosen = "old";
		int oldB = temp.adjustStrength(calculateStrength(oldBond,allBonds)); 
		int newB = temp.adjustStrength(calculateStrength(newBond,allBonds));
		//also, this does not weigh much if the temperature is high
		if(oldB < newB){
			chosen = "new";}
		else if(oldB == newB){
			if(rand.nextInt(100) < 50){
				chosen = "new";}}
		return chosen;}
	
	/**
	 * Called when a sameness bond grouped. The strength of the bond
	 * will determine whether it is updated or not. 
	 * @param name Name of input string; Initial, Modified or Target
	 * @param pos Position of the modified object
	 * @param newSize New number of bonds
	 */
	public void updateAllBonds(String name, int newSize){
		String[][] descrips = ws.getAllDescrip(name);
		String[] str = new String[descrips.length];
		for(int i = 0; i < str.length; i++){
			str[i] = descrips[i][0];}
		count.count();
		ws.storeBonds(evalAll(descrips), name);
		printOut(name);
		storeOverallStrength(name);}
	
	/**
	 * The strength calculated for the whole structure
	 * @param name Name of the input string: Initial, Modified or Target
	 */
	private void storeOverallStrength(String name){
		String[][] bonds = ws.getAllBonds(name);
		int[] strengths;
		if(bonds == null){
			strengths = new int[1];
			strengths[0] = 0;}
		else{
			strengths = new int[bonds.length];
			for(int i = 0; i < strengths.length; i++){
				strengths[i] = calculateStrength(bonds[i], bonds);}}
		ws.storeBondStrength(name,strengths);}
	
	/**
	 * Updates the strengths of the bonds for both the 
	 * Initial and Target inputs.
	 */
	public void updateAllStrengths(){
		storeOverallStrength("initial");
		storeOverallStrength("target");}
	
	/**
	 * Displays the bonds.
	 * @param name The name of the raw input, Initial or Target
	 */
	private void printOut(String name){
		String[][] b = ws.getAllBonds(name);
		System.out.println("----------------------------------------------");
		System.out.println("|        Bonds for the " + name + " string         |");
		System.out.println("----------------------------------------------");
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b[i].length; j++){
				System.out.print(b[i][j] + "   ");}
			System.out.println();}
		System.out.println("----------------------------------------------");}

}

	
