import java.util.ArrayList;
import java.util.Random;

/**
 * To add descriptions to the original input strings.
 * Includes the addition of descriptions for grouping
 * bonds that are the same. 
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Description {
	private static final String SAMENESS = "sameness";
	private Counter count;
	private Random rand = new Random();
	private Slipnet sn;
	private WorkSpace ws;
	private Temperature t;
	private String[][] objectBonds;
	//holds the new descriptions ready for the workspace
	private ArrayList<String[]> redescrip = new ArrayList<String[]>();

	public Description(Slipnet sn, WorkSpace ws, Bonds b, Temperature t, Counter count){
		this.sn = sn;
		this.ws = ws;
		this.t = t;
		this.count = count;}
	
	/**
	 * Modify descriptions after bonding. 
	 * Only called when a sameness bond occurs.
	 * @param name The name of the input string; Initial, Modified or Target
	 */
	public void checkAllBonds(String name){
		redescrip = new ArrayList<String[]>();
		objectBonds = ws.getAllBonds(name);
		reDescribe();
		String[][] newDescrips = new String[redescrip.size()][1];
		for(int i = 0; i < newDescrips.length;i++){
			String[] s = redescrip.get(i);
			newDescrips[i] = s;}
		stringPosition(newDescrips);
		ws.storeDescrip(newDescrips, name);
		showAll(newDescrips,name);
		updateAllStrengths();}
	
	/**
	 * Goes through and re-describes any sameness bonds found
	 * @param bonds The current bond structure being re-described
	 */
	public void reDescribe(){
		firstBond(objectBonds[0][0], objectBonds[0][1]);
		for(int i = 1; i < objectBonds.length; i++){
			redescrip.add(sameBond(objectBonds[i][0], objectBonds[i][1]));}}
	
	/**
	 * To see if the first bond is a sameness bond.
	 * If it is a sameness bond then the two adjacent objects
	 * are probabilistically grouped. 
	 * @param bond The objects that are bonded
	 * @param descrip The bond type of those objects
	 */
	private void firstBond(String bond, String descrip){
		String[] objs = bond.split("-");
		String[] newDescrips;
		int sameProb = 80;
		if(sn.fullyActive(SAMENESS)){
			sameProb = 90;}
		else if(sn.getNodeActivation(SAMENESS) > 0){
			sameProb = 85;}
		
		if(descrip != null && descrip.equals(SAMENESS) && (rand.nextInt(100) < t.adjustProb(sameProb))){
			count.count();
			newDescrips= new String[5];
			newDescrips[0] = objs[0] + objs[1];
			newDescrips[1] = "letter-category:" + objs[1].toUpperCase();
			newDescrips[2] = "object-category:group";
			newDescrips[3] = "";
			newDescrips[4] = addLength(newDescrips[0].length());
			redescrip.add(0, newDescrips);
			sn.activate(SAMENESS);
			}
		else{
			newDescrips = new String[4];
			newDescrips[0] = objs[0];
			newDescrips[1] = "letter-category:" + objs[0].toUpperCase();
			newDescrips[2] = "object-category:letter";
			newDescrips[3] = "";
			redescrip.add(0, newDescrips);
			newDescrips = new String[4];			
			newDescrips[0] = objs[1];
			newDescrips[1] = "letter-category:" + objs[1].toUpperCase();
			newDescrips[2] = "object-category:letter";
			newDescrips[3] = "";
			redescrip.add(1, newDescrips);}}
	
	/**
	 * Probabilistically groups two adjacent objects bonded by a sameness bond
	 * @param s The bonded objects
	 * @param descrip The type of bond for those objects
	 * @return The new descriptions
	 */
	private String[] sameBond(String s, String descrip){
		String[] str = s.split("-");
		String[] descriptions = new String[4];
		if(descrip !=null && descrip.equals(SAMENESS) && (rand.nextInt(100) < t.adjustProb(80))){
			count.count();
			//get the previous description
			String[] prev = redescrip.get(redescrip.size()-1);
			String newLet = prev[0] + str[1];
			String length = addLength(newLet.length());
			if(!length.isEmpty()){
				descriptions = new String[5];
				descriptions[4] = length;}
			descriptions[0] = newLet;
			descriptions[1] = "letter-category:" + str[1].toUpperCase();
			descriptions[2] = "object-category:group";
			descriptions[3] = "";
			redescrip.remove(redescrip.size()-1);}
		else{
			descriptions[0] = str[1];
			descriptions[1] = "letter-category:" + str[1].toUpperCase();
			descriptions[2] = "object-category:letter";
			descriptions[3] = "";}
		return descriptions;}
	
	/**
	 * Probabilistically decide whether to add length description.
	 * If the group length is smaller than 6, then probability is 
	 * added, the smaller then the length the greater the additional
	 * probability. 
	 * @param length The length of the group object
	 * @return Empty or a length description
	 */
	private String addLength(int length){
		String l = "";
		int a = sn.getNodeActivation("length");
		//to keep probability of length being added staying small additional restricted to < 50
		if(a < 50)
		if(length == 2 && a < 50){	
			a = (a + 50)/2;}
		else if(length == 3 && a < 50){
			a = (a + 40)/2;}
		else if(length == 4){
			a = (a + 30)/2;}
		else if(length == 5){
			a = (a + 20)/2;}
		
		if(rand.nextInt(100) < t.adjustProb(a)){
			count.count();
			l = "length:" + length;}
		return l;}
	
	/**
	 * Sets up the string positions for an object if applicable.
	 * @param descriptions All the descriptors for the string's objects
	 */
	private void stringPosition(String[][] descriptions){
		if(descriptions.length == 3){
			descriptions[0][3] = "string-position:leftmost";
			descriptions[1][3] = "string-position:middle";
			descriptions[2][3] = "string-position:rightmost";}
		else if(descriptions.length > 3 | descriptions.length == 2){
			descriptions[0][3] = "string-position:leftmost";
			descriptions[descriptions.length-1][3] = "string-position:rightmost";}
		else if(descriptions.length == 1){
			descriptions[0][3] = "string-position:leftmost";}}
	
	/**
	 * Assess if a letter A or letter Z.
	 * @param name Type of input; Initial or Target
	 */
	public void ftltDescrip(String name){
		count.count();
		String[][] str = ws.getAllDescrip(name);
		String[] newDescrip = null;
		for(int i = 0; i < str.length; i++){
			newDescrip = str[i];
			String[] let = str[i][1].split(":");
			//if there is an 'A' or a 'Z' object
			if(let[1].equals("A")){
				newDescrip = addFirstDescrip(str[i]);}
			else if(let[1].equals("Z")){
				newDescrip = addLastDescrip(str[i]);}
			if(newDescrip[newDescrip.length-1].contains("alphabetic-position")){
				buildFtLt(name, i, newDescrip, str);}}}
	
	/**
	 * Probabilistically builds a first or last description
	 * @param name The name of the raw string, Initial or Target
	 * @param i The position of the object being updated
	 * @param descriptions The descriptions of the object with the new description attached
	 */
	private void buildFtLt(String name, int i, String[] newDescrip, String[][] descrips){
		if(rand.nextInt(100) < t.adjustStrength(calculateStrength(newDescrip[newDescrip.length-1], i, (newDescrip.length-1), descrips))){
			activateDescrip(newDescrip[newDescrip.length-1]);
			System.out.println("Updated Description: " + newDescrip[newDescrip.length-1]);
			ws.updateObjDescrip(name, i, newDescrip);
			overallStrength(name);
			count.count();}}
	
	/**
	 * Probabilistically decides to add a 'first' description.
	 * Will only be called if an 'A' object exists.
	 * @param descriptions The list of current descriptions for the 'A' object
	 * @return The old or modified set of descriptions
	 */
	private String[] addFirstDescrip(String[] descriptions){
		//if the activation level of the node is strong enough
		if(rand.nextInt(100) < t.adjustProb(sn.getNodeActivation("first"))){
			String[] newDescrip = new String[descriptions.length+1];
			for(int i = 0; i < descriptions.length; i++){
				newDescrip[i] = descriptions[i];}
		newDescrip[newDescrip.length-1] = "alphabetic-position:first";
		descriptions = newDescrip;}
		return descriptions;}
	
	/**
	 * Probabilistically decides to add a 'last' description.
	 * Will only be called if a 'Z' object exists.
	 * @param descriptions The list of descriptions for 'Z' object
	 * @return The old or modified set of descriptions
	 */
	private String[] addLastDescrip(String[] descriptions){
		if(rand.nextInt(100) < t.adjustProb(sn.getNodeActivation("last"))){
			String[] newDescrip = new String[descriptions.length+1];
			for(int i = 0; i < descriptions.length; i++){
				newDescrip[i] = descriptions[i];}
		newDescrip[newDescrip.length-1] = "alphabetic-position:last";
		descriptions = newDescrip;}
		return descriptions;}	
	
	/** 
	 * Calculates the strength of an object's description.
	 * This is based on; the conceptual depth of the descriptor,
	 * the activation level of the description type and the local 
	 * support of the description.
	 * @param descrip A description for that object
	 * @param obj The row position, object position
	 * @param pos The column position, description position
	 * @param allDescrip All descriptions for that original input string
	 * @return The strength of the input description
	 */
	public int calculateStrength(String descrip, int obj, int pos, String[][] allDescrip){
		String[] s = descrip.split(":");
		String descriptor = s[0];
		int strength = 0;
		//conceptual depth of descriptor
		int depth = sn.getDepth(descriptor);
		//current activation of description type
		int active = sn.getNodeActivation(descriptor);
		//amount of local support
		int local = existingDescrips(descrip, obj, pos, allDescrip);
		strength = (local + depth + active)/3;
		return strength;}
	
	/**
	 * Used to calculate the strength of an object. 
	 * Probability dependent on number of local descriptions for the
	 * input description. 
	 * @param type The name of description being assessed.
	 * @param obj The position of that object's descriptions.
	 * @param pos The position of the description for specific object.
	 * @param allDescrip All descriptions for comparison.
	 * @return Returns the probability for that object
	 */
	private int existingDescrips(String type, int obj, int pos, String allDescrip[][]){
		int prob = -100;
		//-100, as we aren't counting the description we are comparing to
		String[] st = type.split(":");
		for(int i = 0; i < allDescrip.length; i++){
			if(allDescrip[i].length-1 >= pos){
				String[] s = allDescrip[i][pos].split(":");
				if(s.length>1){
					if(s[0].equals(st[0])){
						prob = prob + 100;}}}}
		prob = prob/(allDescrip.length);
		return prob;}
	
	/**
	 * Activates the slipnet for a single description
	 * @param descrip The description to be activated
	 */
	private void activateDescrip(String descrip){
		String[] d = descrip.split(":");
		sn.activate(d[0]);
		sn.activate(d[1]);}
	
	/**
	 * To get the strength of a specific object's description
	 * @param name Name of the original input, Initial, Modified, Target
	 * @param obj The position of 
	 * @param type The position of a specific object's description
	 * @return The adjusted strength for that description
	 */
	public int getDescripStrength(String name, int obj, int type){
		String descrip = ws.getDescrip(name,obj,type);
		int s = calculateStrength(descrip,obj,type,ws.getAllDescrip(name));
		return t.adjustStrength(s);}
	
	/**
	 * Calculates strength of an object in the structure of descriptions
	 * one of the original inputs. 
	 * @param name The name of the original input, Initial, Modified or Target
	 */
	private void overallStrength(String name){
		String[][] descrips = ws.getAllDescrip(name);
		int[][] s = new int[descrips.length][];
		for(int i = 0; i < descrips.length; i++){
			int[] ss = new int[descrips[i].length];
			for(int j = 1; j < descrips[i].length; j++){
				if(!descrips[i][j].isEmpty()){
					ss[j-1] = calculateStrength(descrips[i][j],i,j,descrips);
					ss[j-1] = t.adjustStrength(ss[j-1]);}}
			s[i] = ss;}
		ws.storeDescripStrength(name, s);}
	
	/**
	 * Updates the strengths for all the object descriptions
	 * for all 3 input strings.
	 */
	public void updateAllStrengths(){
		overallStrength("initial");
		overallStrength("target");
		overallStrength("modified");}
	
	/**
	 * Displays the descriptions
	 * @param descrips The descriptions for each object
	 * @param name The raw input string the descriptions belong to
	 */
	private void showAll(String[][] descrips, String name){
		System.out.println("----------------------------------------------");
		System.out.println("|     Descriptions for " + name + " string          |");
		System.out.println("----------------------------------------------");
		for(int i = 0; i < descrips.length; i++){
			System.out.println(" ---");
			System.out.print("| " + descrips[i][0] + " | ");
			for(int j = 1; j < descrips[i].length; j++){
				if(descrips[i][j].isEmpty()){
					j = j++;}
				else{
					String[] a = descrips[i][j].split(":");
					System.out.print(a[1] + " | ");}}
			System.out.println();
			System.out.println(" ---");}}
}

