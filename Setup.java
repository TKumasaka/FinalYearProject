/**
 * Sets up the program.
 * Gives each of the input strings their initial
 * descriptions.
 * Updates the states of the slipnet and workspace to
 * fit the descriptions added to the input strings.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Setup {
	private static final String INITIAL = "initial";
	private static final String MODIFIED = "modified";
	private static final String TARGET = "target";
	
	private String init;
	private String mod;
	private String targ;
	
	private String[] splitInit;
	private String[] splitMod;
	private String[] splitTarg;
	
	private String[][] initDescrip;
	private String[][] modDescrip;
	private String[][] targDescrip;
	
	private WorkSpace ws = new WorkSpace();
	private Slipnet sn = new Slipnet();

 	public Setup(String init, String mod, String targ){
		this.init = init;
		this.mod = mod;
		this.targ = targ;
		splitInit = splitInput(init);
		splitMod = splitInput(mod);
		splitTarg = splitInput(targ);
		describe();
		storeAll();
		sn.setUpActivation();
		activateCat(splitInit);
		activateCat(splitMod);
		activateCat(splitTarg);
		activatePos(initDescrip);
		activatePos(modDescrip);
		activatePos(targDescrip);}
	
 	/**
 	 * Splits the raw strings into arrays of letters
 	 * @param st The raw input string
 	 * @return The raw input string split into letters
 	 */
	private String[] splitInput(String st){
		String str[] = new String[st.length()];
		if(st.matches("[a-z]+")){
			str = splitString(st.toCharArray());}
		else{
			System.out.println("Input can only consist of lowercase letters");}
		return str;}
	
	/**
	 * Converts the input into string counterparts
	 * @param ch The input as an array of characters
	 * @return The string array version of the input
	 */
	private String[] splitString(char[] ch){
		String[] str = new String[ch.length];
		for(int i = 0; i < ch.length; i++){
			str[i] = Character.toString(ch[i]);}
		return str;}
	
	/**
	 * Calls methods to give descriptions to all of the inputs.
	 */
	private void describe(){
		//[letter][descriptor:description]
			//[letter][letter-category][object-category][string-position]||[obj-length]
	
		initDescrip = new String[init.length()][5];
		modDescrip = new String[mod.length()][5];
		targDescrip = new String[targ.length()][5];
		
		initDescrip = setDescrips(splitInit);
		modDescrip = setDescrips(splitMod);
		targDescrip = setDescrips(splitTarg);
		
		setPosition(initDescrip);
		setPosition(modDescrip);
		setPosition(targDescrip);}
	
	/**
	 * Gives descriptions to each letter object of the split raw
	 * input.
	 * @param splits The split version of the raw string
	 * @return Descriptions given to each object
	 */
	private String[][] setDescrips(String[] splits){
		String[][] str = new String[splits.length][5];
		for(int i = 0; i < splits.length; i++){
			//letter
			str[i][0] = splits[i];
			//letter-category
			str[i][1] = "letter-category:" + splits[i].toUpperCase();
			//object-category
			str[i][2] = "object-category:letter";
			//string-position
			str[i][3] = "";
			//obj-length
			//(only changes from "" if length is perceived as relevant, else ignored)
			str[i][4] = "";}
	return str;}
	
	/**
	 * Gives each object a position if applicable
	 * @param str The current set of descriptions for the Initial, Target or Modified objects
	 */
	private void setPosition(String[][] str){
		if(str.length == 3){
			str[0][3] = "string-position:leftmost";
			str[1][3] = "string-position:middle";
			str[2][3] = "string-position:rightmost";}
		else if(str.length > 3 | str.length == 2){
			str[0][3] = "string-position:leftmost";
			str[str.length-1][3] = "string-position:rightmost";}
		else if(str.length == 1){
			str[0][3] = "string-position:leftmost";}
		else{
			System.out.println("error");}	}
	
	/**
	 * Store all setup descriptions.
	 */
	private void storeAll(){
		ws.storeOriginals(init, mod, targ);
		ws.storeSplit(splitInit, splitMod, splitTarg);
		ws.storeDescrip(initDescrip, INITIAL);
		ws.storeDescrip(modDescrip, MODIFIED);
		ws.storeDescrip(targDescrip, TARGET);
		showAll(initDescrip, INITIAL);
		showAll(modDescrip, MODIFIED);
		showAll(targDescrip, TARGET);}
	
	/**
	 * To print out the the descriptions the program starts off
	 * with.
	 * @param descrips The descriptions being printed
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
	
	 /**
	  * Activates all letter category nodes
	  * @param str The letter to be activated
	  */
	private void activateCat(String[] str){
		for(int i = 0; i < str.length; i++){                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
			sn.activate(str[i].toUpperCase());}}
	
	/**
	 * Activates any relevant position nodes
	 * @param str The descriptions built in the workspace
	 */
	private void activatePos(String str[][]){
		for(int i = 0; i < str.length; i++){
			if(!(str[i][3].isEmpty())){
				String s = str[i][3];
				String[] sp = s.split(":");
				sn.activate(sp[0]);}}}
	
	/**
	 * Gets the current workspace
	 * @return The workspace
	 */
	public WorkSpace getWs(){
		return ws;}
	
	/**
	 * Gets the current slipnet
	 * @return The slipneet
	 */
	public Slipnet getSn(){
		return sn;}
}
