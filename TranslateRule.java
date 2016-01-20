import java.util.ArrayList;
/**
 * Translates and applies the current rule, using
 * the conceptual slippages from the Corresp class 
 * to produce a solution.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class TranslateRule {
	private WorkSpace ws;
	private Slipnet sn;
	private Counter count;
	private String modTarg;
	
	private static final String SUCC = "successor";
	private static final String PRED = "predecessor";
	private static final String LEFT = "right";
	private static final String RIGHT = "left";
	private static final String LEFTM = "leftmost";
	private static final String RIGHTM = "rightmost";
	private static final String MID = "middle";
	
	//Replace ____ by____
		//Replace[] by[]
		//eg. Replace letter-category of rightmost letter by successor offset -2
		//    Replace A by D
		//    Replace letter-category of rightmost letter by D
	private String[][] initialRule;
	private String[] replace;
	private String[] replaceWith;
	private String[] newReplace;
	private String[] newReplaceWith;
	private String[][] targDes;
	private ArrayList<String> mappings;
	private boolean group;
	
	//the integer versions of 'a' and 'b' respectively
	private static final int LETA = 97;
	private static final int LETB = 122;
	
	public TranslateRule(WorkSpace ws, Slipnet sn, Counter count){
		this.ws = ws;
		this.sn = sn;
		this.count = count;}	
	
	/**
	 * Runs this stage of the program, translating and
	 * applying the rule to form a solution
	 * @return The solution
	 */
	public String translate(){
		count.count();
		modTarg = "";
		group = false;
		targDes = ws.getAllDescrip("target");
		initialRule = ws.getInitialRule();
		replace = initialRule[0];
		replaceWith = initialRule[1];
		newReplace = replace;
		newReplaceWith = replaceWith;
		mappings = ws.getCorres();
		if(replace.length > 1 && replace[2].contains("group")){
			group = true;}

		//if the rule is to just flip, flip
		if(replace[0].equals("whole")&& replaceWith[0].equals("flipped")){
			System.out.println("Translated rule: Replace whole by flipped version");
			flip();}
		//if the rule is a direct letter change
		else if(replace.length == 1 && replaceWith.length == 1){
			printNewRule();
			modTarg = direct(replace[0],replaceWith[0]);}
		//if the rule is a position to letter change
		else if(replace.length > 1 && replaceWith.length == 1){
			changeReplace();
			printNewRule();
			applyRule();}
		//if replacement is a letter with successor
		else if(replace.length == 1 && replaceWith.length > 1){
			changeReplaceWith();
			printNewRule();
			applyRule();}
		//if the replacement are position and succ or pred
		else{
			changeReplace();
			changeReplaceWith();
			printNewRule();
			applyRule();}
		return modTarg;}
	
	/**
	 * Checks to see if the program contains certain mappings
	 * and translates the rule accordingly
	 */
	private void changeReplace(){
		if(mappings.contains("letter-category->length")){
			newReplace[0] = "length";}
		if(mappings.contains("letter->group")){
			newReplace[2] = "group";}
		if(mappings.contains("leftmost->rightmost")){
			newReplace[1] = RIGHTM;}
		else if(mappings.contains("rightmost->leftmost")){	
			newReplace[1] = LEFTM;}}
	
	/**
	 * Checks for opposite concepts for how the string should
	 * be changed and changes the rule accordingly
	 */
	private void changeReplaceWith(){
		if(mappings.contains("successor->predecessor")){
			newReplaceWith[0] = PRED;}
		if(mappings.contains("predecessor->successor")){
			newReplaceWith[0] = SUCC;}}
	
	/**
	 * Applies rule
	 */
	private void applyRule(){
		String result = "";
		if(newReplace.length > 1){
			if(newReplace[1].equals(LEFTM)){
				result = changeObj(getLet(LEFTM));
				modifyTarg(result,0);}
			else if(newReplace[1].equals(RIGHTM)){
				result = changeObj(getLet(RIGHTM));
				modifyTarg(result,targDes.length-1);}
			else if(newReplace[1].equals(MID)){
				result = changeObj(getLet(MID));
				midChange(result);}}
		else{
			String newLet = changeObj(replace[0].toLowerCase());
			modTarg = direct(replace[0],newLet);}}
	
	/**
	 * Create the modified target if the middle object was changed
	 * @param change The object that was changed
	 */
	private void midChange(String change){
		if(targDes.length > 3 && group){
			modTarg = targDes[0][0] + change + targDes[targDes.length-1][0];}
		else{
			int i = targDes.length/2;
			modTarg = "";
			for(int j = 0; j < targDes.length; j++){
				if(j==i){
					modTarg = modTarg + change;}
				else{
					modTarg = modTarg + targDes[j][0];}}}}
	
	/**
	 * Create the modified target string
	 * @param obj The changed object
	 * @param objPos The position of the changed object
	 */
	private void modifyTarg(String obj, int objPos){
		for(int i = 0; i < targDes.length; i++){
			if(i==objPos){
				modTarg = modTarg + obj;}
			else{
				modTarg = modTarg + targDes[i][0];}}}

	/**
	 * Get object at position specified
	 * @param pos The position of the object
	 * @return The object
	 */
	private String getLet(String pos){
		String let = "";
		if(pos.equals(LEFTM)){
			let = targDes[0][0];}
		else if(pos.equals(RIGHTM)){
			let = targDes[targDes.length-1][0];}
		else if(pos.equals(MID)){
			if(targDes.length > 3 && group){
				for(int i = 1; i < targDes.length-2; i++){
					let = let + targDes[i][0];}}
			else{
				int p = targDes.length/2;
				let = targDes[p][0];}}
		return let;}

	/**
	 * Changes the object in accordance with the second half of the rule
	 * @param replace The object being replaced
	 * @return The modified object
	 */
	private String changeObj(String replace){
		String s = "";
		int offset = 0;
		String direc = "";
		
		if(mappings.contains("left->left") || mappings.contains("right->left")){
			direc = LEFT;}
		else if(mappings.contains("right->right") || mappings.contains("left->right")){
			direc = RIGHT;}
		
		if(newReplaceWith.length == 1){
			s = directChange(replace,newReplaceWith[0]);}
		else if(newReplaceWith[0].equals(SUCC) && direc.equals(RIGHT)
				|| newReplaceWith[0].equals(PRED) && direc.equals(LEFT)){
			offset = (Integer.parseInt(newReplaceWith[1]));
			offset = -(Math.abs(offset));
			s = changeLetter(replace,offset);}
		else if(newReplaceWith[0].equals(SUCC) && direc.equals(LEFT)
				|| newReplaceWith[0].equals(PRED) && direc.equals(RIGHT)){
			offset = (Integer.parseInt(newReplaceWith[1]));
			offset = Math.abs(offset);
			s = changeLetter(replace,offset);}
		return s;}
	
	/**
	 * Flips the whole of the raw Target string to produce
	 * the modified target
	 */
	public void flip(){
		String newS = "";
		for(int i = targDes.length-1; i >= 0; i--){
			newS = newS + targDes[i][0];}
		modTarg = newS;}
	
	/**
	 * Finds and changes the target string according
	 * to a direct letter change in the rule
	 * @param let The letter to be changed
	 * @param repLet The letter to be changed to 
	 */
	public String direct(String let, String repLet){
		String newS = "";
		repLet = repLet.toLowerCase();
		for(int i = 0; i < targDes.length; i++){
			String toChange = targDes[i][0];
			String[] s = targDes[i][1].split(":");
			String letObj = s[1].toLowerCase();
			if(letObj.equals(let.toLowerCase())){
				newS = newS + (directChange(toChange,repLet));}
			else{
				newS = newS + toChange;}}
		return newS;}
	
	/**
	 * Changes the letter(s) 
	 * @param orig The letter(s) to be changed
	 * @param change The letter the original are to be changed to
	 * @return The changed string
	 */
	public String directChange(String orig,String change){
		String newS = "";
		change = change.toLowerCase();
		for(int i = 0; i < orig.length(); i++){
			newS = newS + change;}
		if(replace[0].equals("length")){
			newS = newS + change;}
		return newS;}
	
	/**
	 * Change letter(s) according to offset. 
	 * @param obj The object being changed
	 * @param offset The offset the object is being changed
	 * @return The changed object
	 */
	public String changeLetter(String obj, int offset){
		String newLet = "";
		String newObj = "";
		for(int i = 0; i < obj.length(); i++){
			int l = (int)obj.charAt(i);
			l = l - offset;
			if(l < LETA){
				l = LETA;}
			else if(l > LETB){
				l = LETB;}
			newLet = Character.toString((char)l);
			newObj = newObj + newLet;}
		
		if(replace[0].equals("length")){
			newObj = newObj + newLet;}
		return newObj;}
	
	/**
	 * Prints the translated rule
	 */
	private void printNewRule(){
		System.out.println("---------------------------------------");
		System.out.println("|           Translated Rule            |");
		System.out.println("---------------------------------------");
		System.out.print("Replace ");
		if(replace.length > 1){
			System.out.print(newReplace[0] + " of " + newReplace[1] + " " + newReplace[2] + " by ");}
		else{
			System.out.print(newReplace[0] + " by ");}
		
		if(replaceWith.length > 1){
			System.out.print(newReplaceWith[0] + " offset " + newReplaceWith[1]);}
		else{
			System.out.print(newReplaceWith[0]);}
		System.out.println();
		System.out.println("---------------------------------------");}
	
	/**
	 * Get the modified string
	 * @return The modified string
	 */
	public String getModTarg(){
		return modTarg;}
	
	/**
	 * Calculates strength of the rule
	 */
	public int calcStrength(){
		count.count();
		initialRule = ws.getInitialRule();
		int strength = 0;
		String[] replace = initialRule[0];
		String[] replaceWith = initialRule[1];
		int total = replace.length + replaceWith.length;
		if(replace[0].equals("whole") && replaceWith[0].equals("flipped")){
			strength = 80;}
		else{
			for(int i = 0; i < replace.length; i++){
				strength = strength + sn.getDepth(replace[i]);}
			for(int i = 0; i < replaceWith.length-1; i++){
				strength = strength + sn.getDepth(replaceWith[i]);}
			double d = (double)strength;
			double dt = (double) total;
			d = d/dt;
			strength = (int)Math.round(d);}
		return strength;}
	
}
