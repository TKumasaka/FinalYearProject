import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Stores information for a specific concept node.
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Node {
	private String name;
	private int conceptualDepth;
	private int activationLevel;
	private int clampCounter;
	private boolean fully;
	
	//<name of linked node, link length>
	private HashMap<String, Integer> linkedNodes;
	//[name of linked node][amount to spread if this node fully active]
	private Object[][] spread;
	
	/**
	 * Sets up a node
	 * @param name Name of the node
	 * @param conceptualDepth Conceptual depth of the node
	 */
	public Node(String name, int conceptualDepth){
		this.name = name;
		this.conceptualDepth = conceptualDepth;
		activationLevel = 0;
		fully = false;
		clampCounter = 0;
		linkedNodes = new HashMap<String, Integer>();}
	
	/**
	 * Sets up the all the nodes it is linked to.
	 * @param linked The name of the node it is linked to
	 * @param length Length of the link
	 */
	public void addLinkedNode(String linked, int length){
		linkedNodes.put(linked, length);}

	/**
	 * Setup the spread value.
	 * Puts the valued each linked node would get
	 * if this node was fully activated
	 */
	public void setSpread(){
		Set<String> se = linkedNodes.keySet();
		Iterator<String> it = se.iterator();
		spread = new Object[se.size()][2];
		for(int i = 0; i < se.size(); i++){
			String str = it.next();
			int percent = linkedNodes.get(str);
			spread[i][0] = str;
			spread[i][1] = 100-percent;}}
	
	/**
	 * Clamp the node to a set activation level.
	 */
	public void clamp(){
		clampCounter = 50;}
	
	/**
	 * Check if clamped. 
	 * If clamped, the counter that ensures the node
	 * remains clamped is decremented. 
	 */
	public void tryDecay(){
		if(clampCounter == 0){
			decay();}
		else{
			clampCounter--;}}
	
	/**
	 * Decays the node if there is some activation.
	 */
	public void decay(){
		fully = false;
		int decay = 100 - activationLevel;
		if(activationLevel < decay){
			activationLevel = 0;}
		else{
			activationLevel = activationLevel - decay;}}
	
	/**
	 * Update node to full activation. 
	 * Spreads activation to linked nodes
	 * where the percentage given to a neighbour is 100 minus the length
	 * of the link from the original to the neighbouring node.
	 * Spreads activation to connecting nodes.
	 */
	public void fullyActivate(){
		fully = true;
		activationLevel = 100;}
	
	/**
	 * Updates node's activation level
	 * @param level Level of activation being given to it
	 */
	public void updateActivation(int level){
		if(activationLevel + level >= 100){
			fullyActivate();}
		else{
			activationLevel = activationLevel + level;}}
	
	public int getActivationLevel(){
		return activationLevel;}
	
	/**
	 * Get any neighbouring nodes in case activation is to be spread
	 * to them.
	 * @return All neighbouring nodes
	 */
	public Object[][] getLinked(){
		return spread;}
	
	/**
	 * Check if the node is fully active
	 * @return False if the node is fully active and true otherwise
	 */
	public boolean fullyActive(){
		return fully;}
	
	/**
	 * Get the conceptual depth of the node
	 * @return The conceptual depth of the node
	 */
	public int getDepth(){
		return conceptualDepth;}
	
	/**
	 * Get the concept name of the node
	 * @return The concept name of the node
	 */
	public String getName(){
		return name;}
	
	/**
	 * Checks if the node has the link asked for.
	 * @param node The name of the link being checked for.
	 * @return True if the link exists.
	 */
	public boolean hasLink(String node){
		return linkedNodes.containsKey(node);}
	
	/**
	 * Get the length of the link between this node and the input node
	 * @param node Reference to the node this node is connected to
	 * @return The link length
	 */
	public int getLinkLength(String node){
		return linkedNodes.get(node);}
	
}
