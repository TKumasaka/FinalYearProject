import java.util.Random;

/**
 * Finds the difference between the Initial and Modified inputs.
 * Then uses it to set up a rule.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class CreateRule {
	private Slipnet sn;
	private WorkSpace ws;
	private Temperature temp;
	private Random rand;
	private Counter count;
	
	private static final String INITIAL = "initial";
	private static final String MODIFIED = "modified";
	private boolean found; 
	boolean groups = false;
	private int offset;
	private String[][] init;
	private String[][] mod;
	private String[] initLet;
	private String[] modLet;
	//store descriptions of changed letters
	private String[] original;
	private String[] changed;
	private String changePos;
	
	//Replace ____ by____
	//Replace[] by[]
	//eg. Replace letter-category of rightmost letter by successor offset -2
	//    Replace A by D
	//    Replace letter-category of rightmost letter by D
	private String[][] initialRule;
	private String[] replace;
	private String[] replaceWith;
	
	public CreateRule(WorkSpace ws, Slipnet sn, Temperature temp, Counter count){
		this.sn = sn;
		this.ws = ws;
		this.temp = temp;
		this.count = count;
		rand = new Random();}
	
	public void start(){
		original = null;
		changed = null;
		init = ws.getAllDescrip(INITIAL);
		mod = ws.getAllDescrip(MODIFIED);
		initLet = ws.getSplitOrig(INITIAL);
		modLet = ws.getSplitOrig(MODIFIED);
		if(!ws.allGroupsEmpty(INITIAL) && !ws.allGroupsEmpty("target")){
			groups = true;}
		findDiff();
		storeRule();}
	
	/**
	 * Find the different letter between the Initial and Target input. 
	 * First check if the change is just a flipped version of the original,
	 * if not, then there is an attempt to find the changed letter.
	 */
	private void findDiff(){
		count.count();
		found = false;
		if(checkOpp()){
			found = true;
			replace = new String[1];
			replaceWith = new String[1];
			replace[0] = "whole";
			replaceWith[0] = "flipped";
			System.out.println("Initial rule: Replace whole by flipped version");}
		else{
			compareFMLM();
			if(!found){
				compareRest();}}}
	
	/**
	 * Compare leftmost and rightmost letters first.
	 * The chosen comparison is probabilistically set to
	 * 50/50.
	 */
	private void compareFMLM(){
		if(rand.nextInt(100) < 50){
			if(compareRight()){
				compareLeft();}}
		else{
			if(compareLeft()){
				compareRight();}}}
	
	/**
	 * Compares the middle letters between the Initial
	 * and Modified strings to find the difference.
	 */
	private void compareRest(){
		for(int i = 1; i < initLet.length-1; i++){
			if(!compare(initLet[i], modLet[i])){
				found = true;
				original = init[i];
				changed = mod[i];
				changePos = "middle";
				checkOffset(initLet[i],modLet[i]);
				break;}}}
	
	/**
	 * Compare two letters for equality
	 * @param obj1 The first letter
	 * @param obj2 The second letter to compare with the first letter
	 * @return False if they are not the same, and true otherwise
	 */
	private boolean compare(String obj1, String obj2){
		boolean same = false;
		checkOffset(obj1,obj2);
		if(obj1.equals(obj2)){
			same = true;}
		return same;}
	
	/**
	 * Compare the leftmost objects
	 * @return If they are both the same
	 */
	private boolean compareLeft(){
		boolean same = true;
		if(!initLet[0].equals(modLet[0])){
			original = init[0];
			changed = mod[0];
			checkOffset(initLet[0],modLet[0]);
			found = true;
			changePos = "leftmost";
			same = false;}
		return same;}
	
	/**
	 * Compare the rightmost objects
	 * @return True if they are both the same
	 */
	private boolean compareRight(){
		boolean same = true;
		if(!initLet[initLet.length-1].equals(modLet[modLet.length-1])){
			original = init[init.length-1];
			changed = mod[mod.length-1];
			checkOffset(initLet[initLet.length-1],modLet[modLet.length-1]);
			found = true;
			changePos = "rightmost";
			same = false;}
		return same;}
	
	/**
	 * Check if the only change is a flip.
	 * Probabilistically decide if this is a good enough rule to use.
	 */
	private boolean checkOpp(){
		boolean same = true;
		int j = modLet.length-1;
		for(int i = 0; i < initLet.length; i++){
			if(!initLet[i].equals(modLet[j])){
				same = false;
				break;}
			j--;}
		if(rand.nextInt(100) < temp.adjustProb(sn.getDepth("opposite"))){
			same = false;}
		return same;}
	
	/**
	 * Checks the difference in offset between the two adjacent objects
	 * @param obj1 The first object
	 * @param obj2 The object adjacent to the first object
	 */
	private void checkOffset(String obj1, String obj2){
	    offset = obj1.compareTo(obj2);}
	
	/**
	 * Stores the rule
	 */
	private void storeRule(){
		if(replaceWith == null){
			choose();}
		else if(!replaceWith[0].equals("flipped")){
			choose();}
		initialRule = new String[2][];
		initialRule[0] = replace;
		initialRule[1] = replaceWith;
		ws.storeInitialRule(initialRule);}
	
	/**
	 * Choose what will be replaced and calls a method
	 * to choose in what way it will be replaced.
	 */
	//[letter(s)][descriptor:description]
	//[letter(s)][letter-category:uppercase letter][object-category:letter][string-position: ""][*length: ""]
	private void choose(){
		replace = new String[3];
		replace[0] = "letter-category";
		replace[2] = "letter";
		if(original[2] == null){
			replace[1] = "middle";}
		else{
			replace[1] = changePos;}
		chooseReplacement();}
	
	/**
	 * Choose the replacement part of the rule
	 */
	private void chooseReplacement(){
		String[] letter = changed[1].split(":");
		int directionDepth = sn.getDepth("successor");
		int letDepth = temp.adjustProb(sn.getDepth(letter[1]));
		int totalProb = directionDepth + letDepth;
		int prob = rand.nextInt(totalProb);
		String direc = getDirection();
		if(!direction(direc,letter[1])){
			replaceWith = new String[1];
			replaceWith[0] = letter[1];}		
		else if(prob < letDepth){
			replaceWith = new String[1];
			replaceWith[0] = letter[1];}
		else{
			replaceWith = new String[2];
			replaceWith[0] = direc;
			replaceWith[1] = offset + "";}}
	
	/**
	 * Decides if the replacement will be a successor or predecessor.
	 * The offset is also determined.
	 */
	private boolean direction(String direction,String letter){
		boolean applicable = false;
		if(offset <= 3 && offset >= -3 ){
			applicable = true;}
		if(letter.equals("Z") && offset < 0){
			applicable = false;}
		else if(letter.equals("A") && direction.equals("predecessor")){
			applicable = false;}
		return applicable;}
	
	/**
	 * Gets the direction of change from the offset difference.
	 * Either successor or predecessor
	 * @return The direction
	 */
	private String getDirection(){
		String direction = "predecessor";
		if(offset < 0){
			direction = "successor";}
		return direction;}
}
