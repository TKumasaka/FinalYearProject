import java.util.*;
/**
 * Stores all the nodes. Provides the concept network
 * for the program. 
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Slipnet {
	private Random rand = new Random();
	
	//holds any nodes that need activation spread to them
	private Queue<String> spreadTo;
	
	//<name of node, node itself>
	private HashMap<String, Node> nodes;
	
	//"node1->node2", link length
	//["node1->node2"]
	private ArrayList<String> predLabels;
	private ArrayList<String> succLabels;
	private ArrayList<String> oppLabels;
	private ArrayList<String> slipLinks;
	//label link lengths
	private int predLengths;
	private int succLengths;
	private int oppLengths;

	public Slipnet(){
		nodes = new HashMap<String,Node>();
		spreadTo = new LinkedList<String>();
		predLabels = new ArrayList<String>();
		succLabels = new ArrayList<String>();
		oppLabels = new ArrayList<String>();
		slipLinks = new ArrayList<String>();
		predLengths = 60;
		succLengths = 60;
		oppLengths = 80;
		letters();
		numbers();
		otherNodes();
		addLinkNodes();
		linkSuperOrdinates();
		linkLabels();
		setLinkables();}
	
	/**
	 * Sets up the nodes that are active at the beginning and clamps them.
	 */
	public void setUpActivation(){
		activate("letter-category");
		clamp("letter-category");
		activate("string-position");
		clamp("string-position");}
		
	//-------------------------------------\\
	//  The main methods for the slipnet   \\
	//-------------------------------------\\
	/**
	 * Decay any nodes that have activation.
	 */
	public void decay(){
		Set<String> allNodes = new HashSet<String>();
		allNodes = nodes.keySet();
		for(String nodeName : allNodes){
			Node n = nodes.get(nodeName);
			n.tryDecay();}
		probFully();
		restoreLinks();}
	
	/**
	 * Acknowledges full activation of node, unless that
	 * node is already fully active. 
	 * Will set node's activation level to 100 and see
	 * if any links can be shrunk. 
	 * @param nodeName The node's identity
	 */
	public void activate(String nodeName){
		Node n = nodes.get(nodeName);
		if(!n.fullyActive()){
			n.fullyActivate();
			spreadActivation(n.getLinked());
			shrinkLink(nodeName);}}
	
	/**
	 * Discontinuously activate any nodes with a level of over 50%
	 */
	public void probFully(){
		Set<String> allNodes = new HashSet<String>();
		allNodes = nodes.keySet();
		for(String nodeName : allNodes){
			int a = probActive(nodeName);
			if(rand.nextInt(100) < a){
				activate(nodeName);}}}
	
	/**
	 * The probability of a node discontinuously becoming fully
	 * active.
	 * @param name The name of the node
	 */
	public int probActive(String name){
		int prob = 0;
		Node n = nodes.get(name);
		double al = (double)n.getActivationLevel();
		if(al > 50){
			double activate = (al/100);
			activate = Math.pow(activate, 3);
			activate = activate*100;
			prob = (int)activate;}
		return prob;}
	
	/**
	 * Spreads activation to linked nodes.
	 * @param linked The list of linked nodes and what percent to add
	 */
	private void spreadActivation(Object[][] linked){
		for(int i = 0; i < linked.length; i++){
			String name = (String) linked[i][0];
			Node n = nodes.get(name);
			//percentage to add altered in node when neighbours being retrieved
			int percent = (int) linked[i][1];
			//Checks if activation spread fully activates node
			if(!(n.getActivationLevel() == 100) && n.getActivationLevel() + percent >= 100){
				n.updateActivation(percent);
				spreadTo.add(name);
				shrinkLink(name);}
			else{
				n.updateActivation(percent);}}
		checkSpread();}
	
	/**
	 * Spread activation to neighbouring nodes of 
	 */
	private void checkSpread(){
		while(!spreadTo.isEmpty()){
			String name = spreadTo.remove();
			spreadActivation(nodes.get(name).getLinked());}}
	
	/**
	 * Get activation level of a specific node.
	 * @param nodeName Identity of the node
	 * @return The activation level of the node
	 */
	public int getNodeActivation(String nodeName){
		Node n = nodes.get(nodeName);
		int i = n.getActivationLevel();
		return i;}
	
	/**
	 * Check if a node is active.
	 * Activation level must be above a threshold of 50.
	 * @param nodeName Identity of the node.
	 * @return If the nodes is active or not.
	 */
	public boolean isActive(String nodeName){
		Node n = nodes.get(nodeName);
		boolean b = false;
		if(n.getActivationLevel() >= 50){
			b = true;}
		return b;}
	
	/**
	 * Clamp certain nodes.
	 * Sets a counter to stop these nodes from decaying.
	 * @param nodeName
	 */
	public void clamp(String nodeName){
		Node n = nodes.get(nodeName);
		n.clamp();}
	
	/**
	 * Get the conceptual depth of the node.
	 * @param nodeName The name of the node.
	 * @return The depth of the node.
	 */
	public int getDepth(String nodeName){
		Node n = nodes.get(nodeName);
		return n.getDepth();}
	
	/**
	 * Checks if a specific node is fully active.
	 * @param nodeName The name of the node
	 * @return True if the node is fully active and false otherwise
	 */
	public boolean fullyActive(String nodeName){
		Node n = nodes.get(nodeName);
		return n.fullyActive();}

	/**
	 * Shrink any possible links.
	 * Shrunk label links are used to evaluate slippages, bonds etc. Not for spreading activation. 
	 * Intrinsic length*0.4 is the shrunk length only when the corresponding label nodes
	 * are fully active. 
	 * @param name Link label's identity
	 */
	public void shrinkLink(String name){
		double shrink = 0.4;
		Node n = nodes.get(name);
		if(name.equals("opposite") && n.fullyActive()){
			oppLengths = 80;
			shrink = shrink*oppLengths;
			oppLengths = (int)shrink;}
		else if(name.equals("predecessor") && n.fullyActive()){
			predLengths = 60;
			shrink = shrink*predLengths;
			predLengths = (int)shrink;}
		else if(name.equals("successor") && n.fullyActive()){
			succLengths = 60;
			shrink = shrink*succLengths;
			succLengths = (int)shrink;}}
	
	/**
	 * Checks to see if the label nodes are fully active
	 * or not.
	 */
	public void restoreLinks(){
		if(!nodes.get("opposite").fullyActive()){
			oppLengths = 80;}
		else if(!nodes.get("predecessor").fullyActive()){
			predLengths = 60;}
		else if(!nodes.get("successor").fullyActive()){
			succLengths = 60;}}

	/**
	 * Checks to see if the two nodes are linked in the slipnet.
	 * @param first The first node to be compared
	 * @param second The node to compare to the first node
	 * @return True if they are connected and false otherwise
	 */
	public boolean hasLink(String first, String second){
		Node n1 = nodes.get(first);
		Node n2 = nodes.get(second);
		boolean exists = false;
		if(n1.hasLink(second) && n2.hasLink(first)){
			exists = true;}
		return exists;}
	
	/**
	 * Gets the length of a label link between two concept nodes if
	 * applicable
	 * @param obj1 The first node 
	 * @param obj2 The second node the first node is linked to
	 * @return The length of the link
	 */
	public int getLinkLabelLength(String obj1, String obj2){
		int l = 0;
		String li = obj1 + "->" + obj2;
		if(oppLabels.contains(li)){
			l = l + oppLengths;}
		else if(predLabels.contains(li)){
			l = l + predLengths;}
		else if(succLabels.contains(li)){
			l = l + succLengths;}
		return l;}
	
	/**
	 * Get the general length of a link between two concept nodes if applicable
	 * @param obj1 The first concept node
	 * @param obj2 The second concept node
	 * @return The length of the link between the two nodes
	 */
	public int getLinkLength(String obj1, String obj2){
		int l = 0;
		Node n1 = nodes.get(obj1);
		if(hasLink(obj1, obj2)){
			l = l + n1.getLinkLength(obj2);}
		else{
			l = 0;}
		return l;}
	
	/**
	 * Get the list of slip links
	 * @return The list of slip links
	 */
	public ArrayList<String> getSlipLinks(){
		return slipLinks;}
	
	//-------------------------------------\\
	// Setup all slipnet nodes             \\
	//-------------------------------------\\	
	/**
	 * Sets up the letter nodes
	 */
	private void letters(){
		for(int i = 0; i < 26; i++){
			int num = i + 65;
			String let = String.valueOf((char)(num));
			Node n = new Node(let,10);
			nodes.put(let, n);}}
	
	/**
	 * Sets up all number nodes
	 */
	private void numbers(){
		for(int i = 1; i < 6; i++){
			Node n = new Node(""+i,30);
			nodes.put(i+"",n);}}
	
	/**
	 * Sets up all the nodes.
	 * Each node is set up with their name and their conceptual depth.
	 */
	private void otherNodes(){
		nodes.put("leftmost",(new Node("leftmost",40)));
		nodes.put("rightmost", (new Node("rightmost",40)));
		nodes.put("middle",(new Node("middle",40)));
		nodes.put("single",(new Node("single",40)));
		nodes.put("whole",(new Node("whole",40)));
		nodes.put("left",(new Node("left",40)));
		nodes.put("right",(new Node("right",40)));
		nodes.put("letter",(new Node("letter",20)));
		nodes.put("group",(new Node("group",80)));
		nodes.put("first",(new Node("first",60)));
		nodes.put("last",(new Node("last",60)));
		nodes.put("predecessor",(new Node("predecessor",50)));
		nodes.put("successor",(new Node("successor",50)));
		nodes.put("predecessor-group",(new Node("predecessor-group",50)));
		nodes.put("successor-group",(new Node("successor-group",50)));
		nodes.put("sameness", (new Node("sameness",80)));
		nodes.put("sameness-group", (new Node("sameness-group",80)));
		//super-ordinate categories
		nodes.put("letter-category", (new Node("letter-category",30)));
		nodes.put("length", (new Node("length",60)));
		nodes.put("string-position", (new Node("string-position",70)));
		nodes.put("direction", (new Node("direction",70)));
		nodes.put("object-category", (new Node("object-category",90)));
		nodes.put("alphabetic-position", (new Node("alphabetic-position",80)));
		nodes.put("bond-category", (new Node("bond-category",80)));
		nodes.put("group-category", (new Node("group-category",80)));
		nodes.put("identity", (new Node("identity",90)));
		nodes.put("opposite", (new Node("opposite",90)));}
	
	//-------------------------------------\\
	// Set up all link nodes               \\
	//-------------------------------------\\
	private void link(String nodeName, String linkedNode, int length){
		Node n = nodes.get(nodeName);
		n.addLinkedNode(linkedNode, length);}
	
	/**
	 * The smaller the link, the closer they are.
	 * For other nodes, the conceptual depth difference is the shortness of their link
	 * from nodes to their super-ordinate categories;
	 */
	private void addLinkNodes(){
		link("A","first",75);
		link("Z","last",95);
		
		link("letter-category","length",95);
		link("length","letter-category",95);

		link("letter","group",90);
		link("group","letter",90);
		
		link("predecessor","predecessor-group",60);
		link("predecessor-group","predecessor",90);
		
		link("successor","successor-group",60);
		link("successor-group","successor",90);
		
		link("sameness","sameness-group",30);
		link("sameness-group","sameness",90);
		
		link("single","whole",90);
		link("whole","single",90);
		
		link("left","leftmost",90);
		link("leftmost","left",90);
		
		link("right","rightmost",90);
		link("rightmost","right",90);
		
		link("successor-group","length",95);
		link("predecessor-group","length",95);
		link("sameness-group","length",95);
		link("sameness-group","letter-category",50);
		
		link("right","leftmost",100);
		link("leftmost","right",100);
		link("left","rightmost",100);
		link("rightmost","left",100);
		
		link("leftmost","first",100);
		link("first","leftmost",100);
		link("rightmost","first",100);
		link("first","rightmost",100);
		
		link("leftmost","last",100);
		link("last","leftmost",100);
		link("rightmost","last",100);
		link("last","rightmost",100);}
	
	//-------------------------------------\\
	//  Add links for the super-ordinate   \\
	//  categories.                        \\
	//-------------------------------------\\
	
	/**
	 * Sets up links between nodes and their lengths.
	 */
	private void linkSuperOrdinates(){
		linkLetterCategory();
		linkAlphabeticPos();
		linkStringPos();
		linkObjCat();
		linkDirection();
		linkBondCat();
		linkGroupCat();}
	
	private void linkLetterCategory(){
		for(int i = 0; i < 26; i++){
			int num = i + 65;
			String let = String.valueOf((char)(num));
			link(let,"letter-category",20);}}
	
	private void linkAlphabeticPos(){
		link("first","alphabetic-position",20);
		link("last","alphabetic-position",20);}
	
	private void linkStringPos(){
		link("leftmost","string-position",30);
		link("rightmost","string-position",30);
		link("middle","string-position",30);
		link("single","string-position",30);
		link("whole","string-position",30);}
	
	private void linkObjCat(){
		link("letter","object-category",70);
		link("group","object-category",10);}
	
	private void linkDirection(){
		link("left","direction",30);
		link("right","direction",30);}
	
	private void linkBondCat(){
		link("predecessor","bond-category",30);
		link("successor","bond-category",30);
		link("sameness","bond-category",0);}
	
	private void linkGroupCat(){
		link("predecessor-group","group-category",30);
		link("successor-group","group-category",30);
		link("sameness-group","group-category",0);}
	

	/**
	 * Sets a node's linked labels. 
	 * The percentage of activation a linked label will receive from the node
	 * is calculated and then stored.
	 */
	private void setLinkables(){
		Set<String> all = nodes.keySet();
		for(String s : all){
			Node n = nodes.get(s);
			n.setSpread();}}
	
	/**
	 * Gives link labels lengths.
	 * These lengths can be changed by the program.
	 */
	private void linkLabels(){
		//for successor letters
		for(int i = 0; i < 25; i++){
			int num = i + 65;
			String let = String.valueOf((char)(num));
			String let2 = String.valueOf((char)(num+1));
			succLabels.add(let+"->"+let2);}

		//for predecessor letters
		for(int i = 1; i < 25; i++){
			int num = i + 65;
			String let = String.valueOf((char)(num));
			String let2 = String.valueOf((char)(num-1));
			predLabels.add(let+"->"+let2);}

		//for numbers
		for(int i = 1; i < 5; i++){
			succLabels.add(i+"->"+(i+1));
			predLabels.add((i+1)+"->"+i);}
		
		//for opposite
		oppLabels.add("first->last");
		oppLabels.add("last->first");
		oppLabels.add("leftmost->rightmost");
		oppLabels.add("rightmost->leftmost");
		oppLabels.add("left->right");
		oppLabels.add("right->left");
		oppLabels.add("predecessor->successor");
		oppLabels.add("successor->predecessor");
		oppLabels.add("predecessor-group->successor-group");
		oppLabels.add("successor-group->predecessor-group");
		
		//for sliplinks
		slipLinks.add("first->last");
		slipLinks.add("last->first");
		slipLinks.add("leftmost->rightmost");
		slipLinks.add("rightmost->leftmost");
		slipLinks.add("left->right");
		slipLinks.add("right->left");
		slipLinks.add("predecessor->successor");
		slipLinks.add("successor->predecessor");
		slipLinks.add("predecessor-group->successor-group");
		slipLinks.add("successor-group->predecessor-group");
		slipLinks.add("letter-category->length");
		slipLinks.add("length->letter-category");
		slipLinks.add("letter->group");
		slipLinks.add("group->letter");
		slipLinks.add("single->whole");
		slipLinks.add("whole->single");
	}
}
