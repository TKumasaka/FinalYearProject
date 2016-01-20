/**
 * Will calculate the importance of all objects.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Importance {
	private static final String INITIAL = "initial";
	private static final String TARGET = "target";
	
	private WorkSpace ws;
	private Slipnet sn;
	
	//[letter][descriptor:description]
	//[letter][letter-category][object-category:letter][string-position][*obj-length]
	private int[] initDescrip;
	private int[] targDescrip;
	
	//[adjacent pairing][bond type][direction]
	private int[] initBonds;
	private int[] targBonds;
	
	private int initGrouping;
	private int targGrouping;	
	
	public Importance(WorkSpace ws, Slipnet sn){
		this.ws = ws;
		this.sn = sn;}
	
	public void update(String name){
		if(name.equals("initDescrip")){
			initDescrip = calcImport(ws.getAllDescrip(INITIAL));}
		else if(name.equals("targDescrip")){
			targDescrip = calcImport(ws.getAllDescrip(TARGET));}
		else if(name.equals("initBonds")){
			if(!ws.allBondsEmpty(INITIAL)){
				initBonds = calcBondImport(ws.getAllBonds(INITIAL));}}
		else if(name.equals("targBonds")){
			if(!ws.allBondsEmpty(TARGET)){
				targBonds = calcBondImport(ws.getAllBonds(TARGET));}}
		else if(name.equals("initGrouping")){
			initGrouping = calcGroupings(ws.getAllGrouping(INITIAL));}
		else if(name.equals("targGrouping")){
			targGrouping = calcGroupings(ws.getAllGrouping(TARGET));}
		else{
			System.out.println("Importance class error");}}
	
	/**
	 * Calculates the importance of each object. 
	 * A description is relevant if it's type has full activation and
	 * the descriptor of the description is the value of the relevant. 
	 * @param descrips The descriptions for all objects
	 * @return The importance of all the object's descriptions
	 */
	public int[] calcImport(String[][] descrips){
		int[] des = new int[descrips.length];
		for(int i = 0; i < descrips.length; i++){
			int imp = 0;
			for(int j = 1; j < descrips[i].length; j++){
				String[] str = descrips[i][j].split(":");
				if(!str[0].isEmpty() && str!=null && sn.fullyActive(str[0])){
					imp = imp + sn.getNodeActivation(str[1]);}}
			des[i] = imp/(descrips[i].length-1);}
		return des;}
	
	/**
	 * Calculate the importance of a set of bonds
	 * @param bonds The bonds, whose importance are being calculated
	 * @return The importance calculated
	 */
	public int[] calcBondImport(String[][] bonds){
		int[] bon = new int[bonds.length];
		for(int i = 0; i < bonds.length; i++){
			int imp = 0;
				if(bonds[i][2] != null && !bonds[i][2].isEmpty()){
					imp = imp + sn.getNodeActivation(bonds[i][2]);}
				else if(bonds[i][1] != null && bonds[i][1].equals("sameness")){
					imp = imp + sn.getNodeActivation(bonds[i][1]);}
				bon[i] = imp/2;}
		return bon;}
	
	/**
	 * Calculates the importance of all whole string groupings.
	 * @param groupings The object grouping 
	 * @return The importance of that grouping
	 */
	public int calcGroupings(String[] groupings){
		int des = 0;
		if(groupings != null){
			for(int i = 1; i < groupings.length; i++){
				String[] str = groupings[i].split(":");
				if(sn.fullyActive(str[0])){
					des = des + sn.getNodeActivation(str[1]);}}
			des = des/(groupings.length-1);}
		return des;}
	
	/**
	 * Get the importance values of an input's descriptions
	 * @param name The name of the string, Initial or Target
	 * @return The importance values
	 */
	public int[] getImportance(String name){
		update(name);
		int[] i = null;
		if(name.equals("initDescrip")){
			i = initDescrip;}
		else if(name.equals("targDescrip")){
			i = targDescrip;}
		else{
			i = new int[1];
			i[0] = 0;}
		return i;}
	
	/**
	 * Get the importance values of an input's bonds
	 * @param name The name of the string, Initial or Target
	 * @return The importance values
	 */
	public int[] getBondImp(String name){
		update(name);
		int[] i = null;
		if(name.equals("initBonds")){
			i = initBonds;}
		else if(name.equals("targBonds")){
			i = targBonds;}
		else{
			i = new int[1];
			i[0] = 0;}
		return i;}
	
	/**
	 * Get the importance values of an input's grouping descriptions
	 * @param name The name of the string, Initial or Target
	 * @return The importance values
	 */
	public int getGroupingImp(String name){
		update(name);
		int i = 0;
		if(name.equals("initGrouping")){
			i = initGrouping;}
		else if(name.equals("targGrouping")){
			i = targGrouping;}
		return i;}
	
}
