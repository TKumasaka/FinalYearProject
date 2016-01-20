/**
 * Happiness of an object is a function of the strengths of the structures:
 * bonds, groups and correspondences attached to it. 
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Happiness {
	private static final String INITIAL = "initial";
	private static final String TARGET = "target";

	private WorkSpace ws;
	
	private int[][] initDescrip;
	private int[][] targDescrip;
	
	//[adjacent pairing][bond type][direction]
	private int[] initBonds;
	private int[] targBonds;
	
	private int initGrouping;
	private int targGrouping;	
	
	public Happiness(WorkSpace ws){
		this.ws = ws;}
	
	/**
	 * Updates the happiness value
	 * @param name The specific input the update is for
	 */
	public void update(String name){
		if(name.equals("initDescrip")){
			initDescrip = calcDesHap(INITIAL);}
		else if(name.equals("targDescrip")){
			targDescrip = calcDesHap(TARGET);}
		else if(name.equals("initBonds")){
			if(!ws.allBondsEmpty(INITIAL)){
				initBonds = calcBondHap(INITIAL);}}
		else if(name.equals("targBonds")){
			if(!ws.allBondsEmpty(TARGET)){
				targBonds = calcBondHap(TARGET);}}
		else if(name.equals("initGrouping")){
			if(!ws.allGroupsEmpty(INITIAL)){
				initGrouping = calcHapGroup(INITIAL);}}
		else if(name.equals("targGrouping")){
			if(!ws.allGroupsEmpty(TARGET)){
				targGrouping = calcHapGroup(TARGET);}}}
	
	/**
	 * Calculates the happiness for descriptions
	 * @param name Reference to which input
	 * @return The resulting happiness values
	 */
	public int[][] calcDesHap(String name){
		int[][] strengths = ws.getDescripStrength(name);
		return strengths;}
	
	/**
	 * Calculates the happiness of bonds
	 * @param name Reference to which input
	 * @return The resulting happiness values
	 */
	public int[] calcBondHap( String name){
		int[] strengths = ws.getBondStrengths(name);
		return strengths;}
	
	/**
	 * Calculates the happiness for groups
	 * @param name Reference to which input
	 * @return The happiness value for the group
	 */
	public int calcHapGroup(String name){
		int strength = ws.getGroupingStrength(name);
		return strength;}
	
	/**
	 * Gets the happiness for descriptions
	 * @param name Reference to which set of descriptions
	 * @return The set of description happiness values
	 */
	public int[][] getDesHap(String name){
		int[][] s = null;
		if(name.equals("initDescrip")){
			s = initDescrip;}
		else if(name.equals("targDescrip")){
			s = targDescrip;}
		return s;}
	
	/**
	 * Get the happiness of bonds
	 * @param name Reference to which set of bonds
	 * @return The set of bond happiness values
	 */
	public int[] getBondHap(String name){
		int[] s = null;
		if(name.equals("initBonds")){
			s = initBonds;}
		else if(name.equals("targBonds")){
			s = targBonds;}
		else{
			s = new int[1];
			s[0] = 0;}
		return s;}
	
	/**
	 * Get the values for group structure happiness
	 * @param name Reference to which set of grouping structures
	 * @return The happiness values for those group structures
	 */
	public int getGroupHap(String name){
		int s = 0;
		if(name.equals("initGrouping")){
			s = initGrouping;}
		else if(name.equals("targGrouping")){
			s = targGrouping;}
		return s;}
}
