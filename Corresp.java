import java.util.ArrayList;
import java.util.Random;

/**
 * Creates correspondences for objects between Initial and
 * Target strings. Based on concept mappings between descriptors
 * of two objects. 
 * @author Tomoka Kumasaka
 * @version 1.0
 */
public class Corresp {
		private WorkSpace ws;
		private Slipnet sn;
		private Temperature temp;
		private Counter count;
		private Random rand = new Random();
		private boolean firstLast;
		
		private static final String SUCCESSOR = "successor";
		private static final String PREDECESSOR = "predecessor";
		private static final String SUCCGROUP = "successor-group";
		private static final String PREDGROUP = "predecessor-group";
		private static final String SAMEGROUP = "sameness-group";
		private static final String RM = "rightmost";
		private static final String LM = "leftmost";
		private static final String LEFT = "left";
		private static final String RIGHT = "right";
		private static final String INITIAL = "initial";
		private static final String TARGET = "target";
		 
		//The objects to be used in the description
		//[letter(s)][descriptor:description]
				//[letter(s)][letter-category:uppercase letter][object-category:letter][string-position: ""][*length: ""]
		private String[][] initDes;
		private String[][] targDes;
		private String[][] modDes;

		//Group descriptions for whole string
		//[grouped: string][group category description: eg successor group]([direction:**]||[letter-category:**])[length: whole]
		private String[] initGroup;
		private String[] targGroup;
		
		//Mappings to change groupings
		private ArrayList<String> maps;
		
		//Replace ____ by____
		//Replace[] by[]
		//eg. Replace 'letter-category' of 'rightmost' 'letter' by 'successor' offset '2'
		//    Replace 'A' by 'D'
		//    Replace 'letter-category' of 'rightmost' by 'D'
		private String[][] initialRule;
		
		public Corresp(WorkSpace ws, Slipnet sn, Temperature temp, Counter count){
			this.sn = sn;
			this.ws = ws;
			this.temp = temp;
			this.count = count;}
		
		public void start(){
			maps = new ArrayList<String>();
			initDes = ws.getAllDescrip(INITIAL);
			targDes = ws.getAllDescrip(TARGET);
			modDes = ws.getAllDescrip("modified");
			initialRule = ws.getInitialRule();
			
			checkRule();
			fstlst();
			assessLengths();
			
			if(!ws.allGroupsEmpty(INITIAL) && !ws.allGroupsEmpty(TARGET) && initialRule[1].length > 1){
				initGroup = ws.getAllGrouping(INITIAL);
				targGroup = ws.getAllGrouping(TARGET);
				compareGroupings();}			
			
			//if what we are replacing is not a letter and it is not being replaced by a letter
			if(initialRule[0].length > 1 && initialRule[1].length > 1){
					//maps the replacement direction
				compareReplacement();
				//get the position that is chosen to be the letter to be replaced
				String pos = initialRule[0][1];
				//if it isn't replaceable, make it a direct replacement
				if(!replaceable(pos)){
					initialRule[1] = new String[1];
			 		initialRule[1][0] = directLet(pos);}}
			//if we are replacing a direct letter with a succ or pred
			else if(initialRule[0].length == 1 && initialRule[1].length > 1){
				//map replacement direction
				compareReplacement();
				//if the letter is not replaceable via the replacement direction
				if(!letReplaceable(initialRule[0][0])){
					//direct replacement
					initialRule[1] = new String[1];
					initialRule[1][0] = initialRule[0][0];
					maps.add(initialRule[0][0] + "->" + initialRule[0][0]);}}
			//else if we are replacing the original with a letter if what is replaced is a direct letter
			else if(initialRule[0].length == 1 && initialRule[1].length == 0){
				maps.add(initialRule[0][0] + "->" + initialRule[0][0]);}
			//else if we are replacing a letter at a certain position with a direct replacement
			else if(initialRule[0].length > 1 && initialRule[1].length == 0){
					comparePos();}
			compareDirec();
			checkSize();
			ws.storeCorres(maps);
			printInitialRule();
			printAll();}
		
		/**
		 * If the changed letter exists in the target string, then 
		 * probabilistically decides whether to directly change that. 
		 */
		private void checkRule(){
			String[] replace = initialRule[0];
			boolean exists = false;
			String let = "";
			//first check if there is an existing letter in target to match with the letter being changed in the initial
			if(replace[1].equals(LM)){
				let = initDes[0][1].split(":")[1];
				exists = checkExists(let);}
			else if(replace[1].equals(RM)){
				let = initDes[initDes.length-1][1].split(":")[1];
				exists = checkExists(let);}
			else{
				for(int i = 1; i < targDes.length-1; i++){
					let = initDes[0][1].split(":")[1];
					if(checkExists(let)){
						exists = true;
						break;}}}
			if(exists){
				toreplace(exists,let);}}
        /**
         * Checks to see if the Initial and Target inputs have any
         * matching letters.
         * @param letter The letter from the Initial string to be compared to
         * @return If there is a match in the Target string
         */
		private boolean checkExists(String letter){
			boolean exists = false;
			for(int i = 0; i < targDes.length; i++){
				String[] s = targDes[i][1].split(":");
				if(letter.equals(s[1])){
					exists = true;}}
			return exists;}
		
		/**
		 * If the Target string has a matching letter to the letter
		 * to be changed in the Initial string probabilistically decides a direct letter change
		 * @param exists If the letter to be changed also exists in Target string
		 * @param let The letter to be changed
		 */
		private void toreplace(boolean exists, String let){
			if(exists){
				if(rand.nextInt(100) < (sn.getDepth(let))){
					initialRule[0] = new String[1];
					initialRule[0][0] = let;}}
			ws.storeInitialRule(initialRule);}
	
		/**
		 * Compares the grouping descriptions of the Initial and Target strings
		 */
		private void compareGroupings(){
			count.count();
			maps.add("whole->whole");
			maps.add("group->group");
			
			//0[grouped: string] 1[group category description: eg successor group] 2([direction:**]||[letter-category:**]) 3[length: whole]
			String[] iDes = initGroup[1].split(":");
			String[] tDes = targGroup[1].split(":");
			String[] iDC = initGroup[2].split(":");
			String[] tDC = targGroup[2].split(":");
			
			if(targGroup[1].contains(SAMEGROUP)){
				maps.add(iDes[1] + "->" + tDes[1]);}
			else if(!oppGrouping(iDes[1],tDes[1],iDC[1],tDC[1])){
				sn.activate("opposite");
				sn.shrinkLink("opposite");
				if(!firstLast && !sn.fullyActive("opposite")){
					maps.add(iDes[1] + "->" + tDes[1]);
					maps.add(iDC[1] + "->" + tDC[1]);}
				else{
					maps.add(iDes[1] + "->" + changeDescrip(tDes[1]));
					maps.add(iDC[1] + "->" + changeDirec(tDC[1]));
					comparePos();}}
			else{
				if(!firstLast && !sn.fullyActive("opposite")){
					maps.add(iDes[1] + "->" + tDes[1]);
					maps.add(iDC[1] + "->" + tDC[1]);}
				else{
					maps.add(iDes[1] + "->" + changeDescrip(tDes[1]));
					maps.add(iDC[1] + "->" + changeDirec(tDC[1]));}}}
		
		/**
		 * Changes the resulting mapping if first and last have been perceived
		 */
		private String changeDescrip(String descrip){
			String c = PREDGROUP;
			if(descrip.equals(PREDGROUP)){
				c = SUCCGROUP;}
			return c;}
		
		/**
		 * Changes the direction from left to right and vice versa 
		 * @param direc The current direction
		 * @return The new direction
		 */
		private String changeDirec(String direc){
			String c = LEFT;
			if(direc.equals(LEFT)){
				c = RIGHT;}
			return c;}
		
		/**
		 * Checks if the two groupings and directions result in an opposite.
		 * @param ides Group category description of Initial string
		 * @param tdes Group category description of Target string
		 * @param idir Direction of Initial grouping
		 * @param tdir Direction of Target grouping
		 * @return If they are the same or not
		 */
		private boolean oppGrouping(String ides, String tdes, String idir, String tdir){
			boolean same = false;
			System.out.println("grouping descrips: ");
			System.out.println(ides);
			System.out.println(tdes);
			System.out.println(idir);
			System.out.println(tdir);
			if(!ides.equals(tdes) && !idir.equals(tdir)){
				same = true;}
			else if(ides.equals(tdes) && idir.equals(tdir)){
				same = true;}
			return same;}
		
		/**
		 * Sees how the direction correspondences affect the mappings.
		 * If there is an A or a Z being replaced and the replacement
		 * will be a successor or predecessor of it then it is checked
		 */
		private String comparePos(){
			String[] toChange = initialRule[0];
			boolean doOpp = false;
			String picked = "";
			int probSlip = rand.nextInt(100);
			int slip = sn.getLinkLabelLength("rightmost", "leftmost");
			boolean slippable = false;
			if(probSlip < temp.adjustProb(slip)){
				slippable = true;}
			if(sn.fullyActive("opposite") || firstLast){
				doOpp = true;}
			if(toChange[1].equals(RM)){
				if(doOpp && replaceable(LM) && slippable){
					maps.add("rightmost->leftmost");
					picked = LM;}
				else if(replaceable(RM)){
					maps.add("rightmost->rightmost");
					picked = RM;}}
			else if(toChange[1].equals(LM)){
				if(doOpp && replaceable(RM) && slippable){
					maps.add("leftmost->rightmost");
					picked = RM;}
				else if(replaceable(LM)){
					maps.add("leftmost->leftmost");
					picked = LM;}}
			else if(toChange[1].equals("middle")){
				maps.add("middle->middle");
				picked = "middle";}
			return picked;}
		
		/**
		 * Add how the object will be replaced into the mappings
		 */
		private void compareReplacement(){
			String[] rep = initialRule[1];
			if(rep[0].equals(SUCCESSOR)){
				if(firstLast){
					maps.add(SUCCESSOR + "->" + PREDECESSOR);}
				else{
					maps.add(SUCCESSOR + "->" + SUCCESSOR);}}
			else if(rep[0].equals(PREDECESSOR)){
				if(firstLast){
					maps.add(PREDECESSOR + "->" + SUCCESSOR);}
				else{
					maps.add(PREDECESSOR + "->" + PREDECESSOR);}}}
		
		/**
		 * Compare the direction it will be changed
		 */
		private void compareDirec(){
			count.count();
			String[] replaceWith = initialRule[1];
			if(maps.contains("left->left") || maps.contains("right->right") 
					|| maps.contains("left->right") || maps.contains("right->left")){
				//do nothing
			}
			else if(replaceWith.length > 1){
				//replaceWith[successor][offset 1]
				String change = replaceWith[0];
				int offset = Integer.parseInt(replaceWith[1]);
				if(sn.fullyActive("opposite")){
					offset = offset*(-1);}
				if(change.equals(SUCCESSOR) && offset < 0 
						|| change.equals(PREDECESSOR) && offset > 0){
					if(firstLast){
						maps.add("right->left");}
					else{
						maps.add("right->right");}}
				else if(change.equals(SUCCESSOR) && offset > 0
						|| change.equals(SUCCESSOR) && offset < 0){
					if(firstLast){
						maps.add("left->right");}
					else{
						maps.add("left->left");}}
				offset = Math.abs(offset);
				replaceWith[1] = "" + offset;
				initialRule[1] = replaceWith;}}
		
		/**
		 * Check what is being replaced and in what way
		 * @param position The position of the obj being replaced
		 * @return True if the object is replaceable and false otherwise
		 */
		private boolean replaceable(String position){
			boolean yes = true;
			String let = "";
			if(position.equals(LM)){
				let = targDes[0][1].split(":")[1];}
			else if(position.equals(RM)){
				let = targDes[targDes.length-1][1].split(":")[1];}
			else if(position.equals("middle")){
				if(targDes.length == 3){
					let = targDes[1][1].split(":")[1];}}
			yes = letReplaceable(let);		
			return yes;}
		
		/**
		 * Sees if the letter to be changed is changeable via the 
		 * slippages. Eg. if the letter is a 'Z' it cannot be changed
		 * to its successor.
		 * @param let The letter to be replaced
		 * @return If the letter is replaceable via its successor or predecessor
		 */
		private boolean letReplaceable(String let){
			boolean yes = true;
			if(maps.contains(SUCCESSOR + "->" + SUCCESSOR) ||
					maps.contains(PREDECESSOR + "->" + SUCCESSOR)){
				if(let.equals("Z")){
					maps.remove(SUCCESSOR + "->" + SUCCESSOR);
					maps.remove(PREDECESSOR + "->" + SUCCESSOR);
					yes = false;}}
			else if(maps.contains(PREDECESSOR + "->" + PREDECESSOR) ||
					maps.contains(SUCCESSOR + "->" + PREDECESSOR)){
				if(let.equals("A")){
					maps.remove(PREDECESSOR + "->" + PREDECESSOR);
					maps.remove(SUCCESSOR + "->" + PREDECESSOR);
					yes = false;}}			
			return yes;}
		
		/**
		 * Get the letter the Target object would directly be changed to
		 * @param name The position of the letter
		 * @return The letter to be directly changed to
		 */
		private String directLet(String name){
			count.count();
			String let = "";
			if(name.equals(LM)){
				let = modDes[0][1].split(":")[1];}
			else if(name.equals(RM)){
				let = modDes[modDes.length-1][1].split(":")[1];}
			else if(name.equals("middle")){
				for(int i = 0; i < initDes.length; i++){
					for(int j = 0; j < modDes.length; j++){
						if(!initDes[i][1].split(":")[0].equals(modDes[j][1].split(":")[1])){
							let = modDes[j][1];
							return let;}}}}
			return let;}
		
		/**
		 * For handling 'a' and 'z' when they appear in leftmost or rightmost
		 * in both raw strings. Only when 'first' and 'last' have been 
		 * attached. 
		 */
		private void fstlst(){
			firstLast = false;
			int prob = rand.nextInt(100);
			int targSize = targDes.length-1;
			int initSize = initDes.length-1;
			System.out.println(initDes[initSize][initDes[initSize].length-1]);
			System.out.println(targDes[0][targDes[0].length-1]);
			if(initDes[0][initDes[0].length-1].contains("first") && (targDes[targSize][targDes[targSize].length-1]).contains("last")
					|| initDes[0][initDes[0].length-2].contains("first") && (targDes[targSize][targDes[targSize].length-2]).contains("last")){
				count.count();
				if(prob < temp.adjustProb(sn.getLinkLabelLength("first", "last"))){
					sn.activate("opposite");
					sn.shrinkLink("opposite");
					firstLast = true;
					comparePos();}}
			else if(initDes[initSize][initDes[initSize].length-1].contains("first") && targDes[0][targDes[0].length-1].contains("last")
					|| initDes[initSize][initDes[initSize].length-2].contains("first") && targDes[0][targDes.length-2].contains("last")){
				count.count();
				if(prob < temp.adjustProb(sn.getLinkLabelLength("last", "first"))){
					sn.activate("opposite");
					sn.shrinkLink("opposite");
					firstLast = true;
					comparePos();}}}
		
		/**
		 * See if length will play a part in it
		 * eg. 1 2 3 -> 4
		 * if length plays a part we use group
		 */
		private void assessLengths(){
			boolean addLength = false;
			int num = 0;
			int prevNum = 0;
				num = targDes[0][0].length();
			for(int i = 1; i < targDes.length; i++){
				prevNum = num;
				String t = targDes[i][targDes[i].length-1];
				//check if the length description exists
				if(t.contains("length")){
					num = targDes[i][0].length();}
				if(num == prevNum + 1){
					addLength = true;}
				else{
					addLength = false;
					break;}}
			
			if(addLength){
				if(rand.nextInt(100) < sn.getLinkLength("letter-category", "length")){
					maps.add(initDes.length + "->" + num);
					maps.add("letter-category->length");}
				else{
					maps.add("letter-category->letter-category");}}
			else{
				maps.add("letter-category->letter-category");}}
		
		/**
		 * Check the length of the object being changed in the target string
		 * probabilistically allowing a slippage from letter to group
		 */
		private void checkSize(){
			String[] replace = initialRule[0];
			int length = 0;
			if(replace.length == 1){
				for(int i = 0; i < targDes.length; i++){
					if(targDes[i][1].split(":")[1].equals(replace[0])){
						length = targDes[i][0].length();
						break;}}}
			else if(replace[1].equals(LM)){
				length = targDes[0][0].length();}
			else if(replace[1].equals(RM)){
				length = targDes[targDes.length-1][0].length();}
			else if(replace[1].equals("middle")){
				if(targDes.length > 3){
					length = 2;}
				else{
					length = targDes[1][0].length();}}
			
			//if the groups are perceived close enough, allows slippage
			if(length > 1){
				if(rand.nextInt(100) < sn.getLinkLength("letter", "group")){
					maps.add("letter->group");
					if(replace.length > 1){
						replace[2] = "group";}}}
			initialRule[0] = replace;}
		
		/**
		 * Displays the rule
		 */
		private void printInitialRule(){
			String[] replace = initialRule[0];
			String[] replaceWith = initialRule[1];
			System.out.println("---------------------------------------");
			System.out.println("|             Initial Rule            |");
			System.out.println("---------------------------------------");
			System.out.print("Replace ");
			if(replace.length > 1){
				System.out.print(replace[0] + " of " + replace[1] + " " + replace[2] + " by ");}
			else{
				System.out.print(replace[0] + " by ");}
			
			if(replaceWith.length > 1){
				System.out.print(replaceWith[0] + " offset " + replaceWith[1]);}
			else{
				System.out.print(replaceWith[0]);}
			System.out.println();
			System.out.println("----------------------------------------");}
		
		/**
		 * Displays the correspondences/concept mappings between
		 * the Initial and Target strings
		 */
		private void printAll(){
			System.out.println("---------------------------------------");
			System.out.println("|    Correspondences and mappings     |");
			System.out.println("---------------------------------------");
			for(String concept : maps){
				System.out.println(concept);}
			System.out.println("----------------------------------------");}
}
