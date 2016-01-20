import java.io.IOException;
import java.util.*;

/**
 * The main running class for the program.
 * The input goes here.
 * @author Tomoka Kumasaka
 * @version 1.0
 * */
public class Run {
	private static final String INITIAL = "initial";
	private static final String MODIFIED = "modified";
	private static final String TARGET = "target";
	private WorkSpace ws;
	private Slipnet sn;
	private Temperature t;
	private static Setup setup;
	private Description describe;
	private Bonds bonds;
	private Grouping group;
	private Corresp corres;
	private CreateRule cr;
	private TranslateRule tr;
	private Importance imp;
	private Happiness hap;
	private Counter count;
	private String newStr;

	
	//input is taken in the order 
	public static void main(String[] args){
		Run run = new Run();
		Scanner scan = new Scanner(System.in);
		System.out.println("Input an initial string");
		String initial = scan.next();
		System.out.println("Input modified version of initial string");
		String modified = scan.next();
		System.out.println("Input a target string");
		String target = scan.next();
		System.out.println(initial + " -> " + modified);
		System.out.println(target + " -> ?");

		if(run.testInput(initial,modified,target)){
			run.start(initial,modified,target);
			run.runProgram();}
		else{
			System.out.println("input can only consist of letters, each input must be different");}}
	
	public boolean testInput(String initial, String modified, String target){
		boolean b = true;
		if(initial.length() < 1){
			b = false;}
		if(!initial.matches("[a-zA-Z]+") && !modified.matches("[a-zA-Z]+") && !target.matches("[a-zA-Z]+")){
			b = false;}
		if(initial.equals(modified)){
			b = false;}
		return b;
	}
	
	public void start(String initial, String modified, String target){
		setup = new Setup(initial, modified, target);
		ws = setup.getWs();
		sn = setup.getSn();
		imp = new Importance(ws,sn);
		hap = new Happiness(ws);
		t = new Temperature(ws,imp,hap);
		count = new Counter(t, sn);
		bonds = new Bonds(sn,ws,t,count);
		describe = new Description(sn,ws,bonds,t,count);
		group = new Grouping(sn,ws,t,count);
		corres = new Corresp(ws,sn,t,count);
		cr = new CreateRule(ws,sn,t,count);
		tr = new TranslateRule(ws,sn,count);}
	
	/**
	 * Runs the program in a specific order.
	 */
	public void runProgram(){
		describe.updateAllStrengths();
		describe.ftltDescrip(INITIAL);
		describe.ftltDescrip(TARGET);
		//if there is a sameness bond, then program tries to group them
		if(bonds.checkBonds(INITIAL)){
			describe.checkAllBonds(INITIAL);
			bonds.checkBonds(INITIAL);}
		
		if(bonds.checkBonds(TARGET)){
			describe.checkAllBonds(TARGET);
			bonds.checkBonds(TARGET);}
		
		if(!group.groupable(INITIAL) && bonds.rebondable(INITIAL)){
			bonds.tryRebond(INITIAL);
			group.groupable(INITIAL);}
		
		if(!group.groupable(TARGET) && bonds.rebondable(TARGET)){
			bonds.tryRebond(TARGET);
			group.groupable(TARGET);}
		
		cr.start();
		String[][] rule = ws.getInitialRule();
		if(!rule[1][0].equals("flipped")){
			corres.start();}
		newStr = tr.translate();
		System.out.println(ws.getOrig(INITIAL) + " -> " + ws.getOrig(MODIFIED));
		System.out.println(ws.getOrig(TARGET) + " -> " + newStr); 
		t.updateTemp();
		System.out.println("Temperature: " + t.getTemp());}
	
	/**
	 * Return the solution.
	 * @return The solution string
	 */
	public String getResult(){
		return newStr;}
	
	/**
	 * Gets the final temperature
	 * @return The final temperature
	 */
	public int getTemp(){
		return t.getTemp();}
}