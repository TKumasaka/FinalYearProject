import java.util.ArrayList;

/**
 * Stores all the perceptual structures
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class WorkSpace {	
	private static final String INITIAL = "initial";
	private static final String MODIFIED = "modified";
	private static final String TARGET = "target";
	
	private String origInit;
	private String origMod;
	private String origTarg;
	
	private String[] splitInit;
	private String[] splitMod;	
	private String[] splitTarg;
	
	//[letter][descriptor:description] 
	//[letter][letter-category][object-category:letter][*string-position][*obj-length]
	private String[][] initDescrip;
	private String[][] modDescrip;
	private String[][] targDescrip;
	private int[][] iDStrength;
	private int[][] mDStrength;
	private int[][] tDStrength;
	
	//[adjacent pairing][bond type][direction]
	private String[][] initBonds;
	private String[][] targBonds;
	private int[] iBStrength;
	private int[] tBStrength;
	
	//[string][grouping]
	private String[] initGrouping;
	private String[] targGrouping;
	private int iGStrength;
	private int tGStrength;
	
	private ArrayList<String> corres;
	
	private String[][] initialRule;
	private int ruleStrength;
	
	public WorkSpace(){}
	
	//----------------------------------------\\
	//  Methods to update and store anything  \\
	//----------------------------------------\\

	public void storeOriginals(String init, String mod, String targ){
		origInit = init;
		origMod = mod;
		origTarg = targ;}
	
	public void storeSplit(String[] init, String[] mod, String[] targ){
		splitInit = init;
		splitMod = mod;
		splitTarg = targ;}

	public void storeDescrip(String[][] descrips, String name){
		if(name.equals(INITIAL)){
			initDescrip = descrips;}
		else if(name.equals(MODIFIED)){
			modDescrip = descrips;}
		else if(name.equals(TARGET)){
			targDescrip = descrips;}}
	
	public void storeBonds(String[][] bonds, String name){
		if(name.equals(INITIAL)){
			initBonds = bonds;}
		else if(name.equals(TARGET)){
			targBonds = bonds;}}

	public void storeGrouping(String[] grouping, String name){
		if(name.equals(INITIAL)){
			initGrouping = grouping;}
		else if(name.equals(TARGET)){
			targGrouping = grouping;}}
	
	public void storeCorres(ArrayList<String> corres){
		this.corres = corres;}
	
	
	public void storeInitialRule(String[][] initialRule) {
		this.initialRule = initialRule;}
	
	//-----------------------------\\
	// Update objects individually \\
	//-----------------------------\\
	public void updateADescrip(String name, int pos, int type, String descrip){
		if(name.equals(INITIAL)){
			initDescrip[pos][type] = descrip;}
		else if(name.equals(MODIFIED)){
			modDescrip[pos][type] = descrip;}
		else if(name.equals(TARGET)){
			targDescrip[pos][type] = descrip;}}
	
	
	public void updateObjDescrip(String name, int pos, String[] descrip){
		if(name.equals(INITIAL)){
			initDescrip[pos] = descrip;}
		else if(name.equals(MODIFIED)){
			modDescrip[pos] = descrip;}
		else if(name.equals(TARGET)){
			targDescrip[pos] = descrip;}}
	
	public void updateBond(String name, int pos, String[] newBond){
		if(name.equals(INITIAL)){
			initBonds[pos] = newBond;}
		else if(name.equals(TARGET)){
			targBonds[pos] = newBond;}}

	public void updateGrouping(String name, String[] grouping){
		if(name.equals(INITIAL)){
			initGrouping = grouping;}
		else if(name.equals(TARGET)){
			targGrouping = grouping;}}
	
	//------------------------------------------\\
	// All methods to return a specific object  \\
	//------------------------------------------\\		
	//gets descriptor:description specified
	
	public String getDescrip(String name, int obj, int descrip){
		String str = new String();
		if(name.equals(INITIAL)){
			str = initDescrip[obj][descrip];}
		else if(name.equals(MODIFIED)){
			str = modDescrip[obj][descrip];}
		else if(name.equals(TARGET)){
			str = targDescrip[obj][descrip];}
		return str;}
	
	public String[] getBonds(String name, int pair){
		String[] s = null;
		if(name.equals(INITIAL)){
			s = initBonds[pair];}
		else if(name.equals(TARGET)){
			s = targBonds[pair];}
		return s;}
	
	//----------------------------\\
	// Methods to get all objects \\
	//----------------------------\\
	
	public String getOrig(String name){
		String str = "";
		if(name.equals(INITIAL)){
			str = origInit;}
		else if(name.equals(MODIFIED)){
			str = origMod;}
		else if(name.equals(TARGET)){
			str = origTarg;}
		return str;}
	
	public String[] getSplitOrig(String name){
		String[] str = null;
		if(name.equals(INITIAL)){
			str = splitInit;}
		else if(name.equals(MODIFIED)){
			str = splitMod;}
		else if(name.equals(TARGET)){
			str = splitTarg;}
		return str;}
 	
	public String[][] getAllDescrip(String name){
		String[][] str = null;
		if(name.equals(INITIAL)){
			str = initDescrip;}
		else if(name.equals(MODIFIED)){
			str = modDescrip;}
		else if(name.equals(TARGET)){
			str = targDescrip;}
		return str;}
	
	public String[][] getAllBonds(String name){
		String[][] str = null;
		if(name.equals(INITIAL)){
			str = initBonds;}
		else if(name.equals(TARGET)){
			str = targBonds;}
		return str;}

	public String[] getAllGrouping(String name){
		String[] str = null;
		if(name.equals(INITIAL)){
			str = initGrouping;}
		else if(name.equals(TARGET)){
			str = targGrouping;}
		return str;}
	
	public ArrayList<String> getCorres(){
		return corres;}
	
	public String[][] getInitialRule(){
		return initialRule;}

	//---------------------------------------\\
	// Methods to check if anything is empty \\
	//---------------------------------------\\
	
	//checks if a specific description for a specific object is empty
	public boolean descripEmpty(String name, int pos, int descrip){
		boolean empty = false;
		if(name.equals(INITIAL)){
			if(initDescrip[pos][descrip].isEmpty()){
				empty = true;}}
		else if(name.equals(MODIFIED)){
			if(modDescrip[pos][descrip].isEmpty()){
				empty = true;}}
		else if(name.equals(TARGET)){
			if(targDescrip[pos][descrip].isEmpty()){
				empty = true;}}
		return empty;}
	
	//checks if a specific bond for two adj objects are empty
	public boolean bondsEmtpy(String name, int pair){
		boolean empty = false;
		if(name.equals(INITIAL)){
			if(initBonds[1][pair].isEmpty()){
				empty = true;}}
		else if(name.equals(TARGET)){
			if(targBonds[1][pair].isEmpty()){
				empty = true;}}
		return empty;}
	
	/**
	 * Check to see if all bonds of a string are empty
	 * @param name The identity of the input, Initial or Target
	 * @return
	 */
	public boolean allBondsEmpty(String name){
		boolean empty = true;
		if(name.equals(INITIAL)){
			if(initBonds != null){
				empty = false;}}
		else if(name.equals(TARGET)){
			if(targBonds != null){
				empty = false;}}
		return empty;}
	
	public boolean allGroupsEmpty(String name){
		boolean empty = true;
		if(name.equals(INITIAL)){
			if(initGrouping != null){
				empty = false;}}
		else if(name.equals(TARGET)){
			if(targGrouping != null){
				empty = false;}}
		return empty;}
	
	//-----------------------\\
	// Methods for strengths \\
	//-----------------------\\
	
	public void storeDescripStrength(String name, int[][] s){
		if(name.equals(INITIAL)){
			iDStrength = s;}
		else if(name.equals(TARGET)){
			tDStrength = s;}
		else if(name.equals(MODIFIED)){
			mDStrength = s;}}
	
	public int[][] getDescripStrength(String name){
		int[][] s = null;
		if(name.equals(INITIAL)){
			s = iDStrength;}
		else if(name.equals(TARGET)){
			s = tDStrength;}
		else if(name.equals(MODIFIED)){
			s = mDStrength;}
		return s;}
	
	public void storeBondStrength(String name, int[] strengths){
		if(name.equals(INITIAL)){
			iBStrength = strengths;}
		else if(name.equals(TARGET)){
			tBStrength = strengths;}}
	
	public int[] getBondStrengths(String name){
		int[] s = null;
		if(name.equals(INITIAL)){
			s = iBStrength;}
		else if(name.equals(TARGET)){
			s = tBStrength;}
		return s;}
	
	public void storeGroupingStrength(String name, int strength){
		if(name.equals(INITIAL)){
			iGStrength = strength;}
		else if(name.equals(TARGET)){
			tGStrength = strength;}}
	
	public int getGroupingStrength(String name){
		int s = 0;
		if(name.equals(INITIAL)){
			s = iGStrength;}
		else if(name.equals(TARGET)){
			s = tGStrength;}
		return s;}
	
	public void storeRuleStrength(int strength){
		ruleStrength = strength;}
	
	public int getRuleStrength(){
		return ruleStrength;}
}
	