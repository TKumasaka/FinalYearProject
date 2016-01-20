/**
 * Describes a grouping for a whole string.
 * Will try to group the whole input string, whether it
 * is an Initial or Target string. 
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Grouping {
	private Slipnet sn;
	private WorkSpace ws;
	private Temperature temp;
	private Counter count;
	
	private static final String SUCCESSOR = "successor";
	private static final String PREDECESSOR = "predecessor";
	private static final String SUCCGROUP = "successor-group";
	private static final String PREDGROUP = "predecessor-group";
	private static final String SAMEGROUP = "sameness-group";
	
	//[adjacent pairing][bond type][direction][offset]
	private String[][] bonds;
	private String[][] descrips;
	//[grouped: string][group category description: eg successor group]([direction:**]||[letter-category:**])[length: whole]
	private String[] newGroup;
	public Grouping(Slipnet sn, WorkSpace ws, Temperature temp, Counter count){
		this.ws = ws;
		this.sn = sn;
		this.temp = temp;
		this.count = count;}
	
	/**
	 * Will group the whole string if possible.
	 * @param name The name of the input string, Initial, Target or Modified
	 * @return If a grouping can be made.
	 */
	public boolean groupable(String name){
		boolean groupable = false;
		String orig = ws.getOrig(name);
		newGroup = new String[4];
		bonds = ws.getAllBonds(name);
		descrips = ws.getAllDescrip(name);
		//for a group of letters who are all the same
		if(descrips.length == 1){
			groupable = true;
			groupSame(orig);
			ws.storeGrouping(newGroup, name);
			count.count();
			activate(newGroup);}
		else if(bonds != null && check()){
			groupable = true;
			group(orig);
			ws.storeGrouping(newGroup, name);
			count.count();
			activate(newGroup);}
		else{
			//will ask the run class to re-bond 
			groupable = false;}
		printAll(name);
		return groupable;}
	
	/**
	 * Asks if both bonds and direction are the same.
	 * If false, the bonds will ask to re-do them.
	 * @return False if the bonds have to be re-done
	 */
	public boolean check(){
		boolean groupable = false;
		if(bonds[0][1] == null){
			groupable = false;}
		else if(bonds[0][1].equals("sameness")){
			groupable = false;}
		else if(sameBond() && sameDirection()){
			groupable = true;}
		return groupable;}
	
	/**
	 * Check bonds all of same type.
	 * @return True if all the bonds are the same, else false
	 */
	public boolean sameBond(){
		boolean groupable = true;
		for(int i = 0; i < bonds.length-1; i++){
			String a = bonds[i][1];
			String b = bonds[i+1][1];
			if(a == null || b == null || !a.equals(b)){
				groupable = false;
				break;}}
		return groupable;}
	
	/**
	 * Check the directions are all the same
	 * @return True if all bonds are the same, else false
	 */
	public boolean sameDirection(){
		boolean groupable = true;
		for(int i = 0; i < bonds.length-1; i++){
			String a = bonds[i][2];
			String b = bonds[i+1][2];
			if(!a.equals(b)){
				groupable = false;
				break;}}
		return groupable;}
	
	/**
	 * Gives group descriptions to string
	 * @param str The original raw input
	 */
	public void group(String str){
		String bond = bonds[0][1];
		newGroup[0] = str;
		if(bond.equals(SUCCESSOR)){
			String direc = bonds[0][2];
			newGroup[0] = str;
			newGroup[1] = "group-category:" + SUCCGROUP;
			newGroup[2] = "direction:" + direc;
			newGroup[3] = "length:whole";}
		else if(bond.equals(PREDECESSOR)){
			String direc = bonds[0][2];
			newGroup[0] = str;
			newGroup[1] = "group-category:" + PREDGROUP;
			newGroup[2] = "direction:" + direc;
			newGroup[3] = "length:whole";}}
	
	/**
	 * Create a sameness group for the whole string
	 * @param str The original raw input 
	 */
	public void groupSame(String str){
		newGroup[0] = str;
		newGroup[1] = "group-category:" + SAMEGROUP;
		newGroup[2] = "letter-category:" + str.substring(str.length()-1).toUpperCase();
		newGroup[3] = "length:whole";}
	
	/**
	 * Calculates the strength of a group object. Depends on: 
	 * The length of the group, the longer it is, the stronger. 
	 * The activation level of the group category.
	 * @return The strength for the grouping as a percentage
	 */
	public int calcStrength(String[] grouping){
		int s = 0;
		String[] group = grouping[2].split(":");
		int activ = sn.getNodeActivation(group[1]);
		int l = grouping[0].length();
		//for inputs with less than 10 letters additional strength is lessened
		if(grouping[0].length() < 10){
			s = s + (l*10) + activ;}
		else if(grouping[0].length() >= 10){
			s = 100 + activ;}
		s = s/2;
		return s;}
	
	/**
	 * If the group is accepted, then update it and activate
	 * any relevant nodes. 
	 * @param group The groupings for that object
	 */
	public void updateGroup(String name, String[] group){
		count.count();
		ws.storeGrouping(group, name);
		activate(group);}
	
	/**
	 * Activates nodes built in workspace.
	 */
	public void activate(String[] grouping){
		for(int i = 1; i < grouping.length; i++){
			String[] str = grouping[i].split(":");
			sn.activate(str[0]);
			sn.activate(str[1]);}}
	
	/**
	 * Calculates and stores the strength of a whole grouping
	 * if applicable.
	 * @param name The name of the raw input, Initial or Target
	 */
	public void storeStrength(String name){
		int s = 0;
		if(!ws.allGroupsEmpty(name)){
			calcStrength(ws.getAllGrouping(name));
			s = temp.adjustStrength(s);}
		ws.storeGroupingStrength(name, s);}
	
	/**
	 * Calls methods to update strengths of the grouping descriptions
	 * for the Initial and Target inputs.
	 */
	public void updateAllStrengths(){
		storeStrength("initial");
		storeStrength("target");}

	/**
	 * Displays the grouping descriptions.
	 * @param groupings The groupings being printed out
	 */
	public void printAll(String name){
		if(!ws.allGroupsEmpty(name)){
			System.out.println("----------------------------------------------");
			System.out.println("| Grouping descriptions for " + name + " string   |");
			System.out.println("----------------------------------------------");
			String[] groupings = ws.getAllGrouping(name);
			for(int i = 0; i < groupings.length; i++){
				System.out.println(groupings[i]);}
			System.out.println("----------------------------------------------");}}}
