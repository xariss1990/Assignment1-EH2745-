package assignment;

import java.util.*;

//The purpose of the class CalculateYbus is to make all the necessary steps required in order to calculate the Ybus
//matrix of a given xml file. Basically, it tries to create the buses of the system by checking the connectivity nodes,
//the terminals connections and the state of the breakers. For the Ybus matrix, the AC Line Segments as well as the Power
//Transformer Ends aka the transformer windings are utilized.
//For the first step, the state of the breakers and the connectivity of the terminals is checked.
//Next, the code tries to create the buses of the system by grouping the terminals that have the same connectivity nodes
//By confirming the state of the breakers, the program further groups the terminals based on state of each breakers. As a result,
//the final form of the buses of the system is given in this case. The rest of the code is implemented in the main method where
//the result of the Ybus matrix is given.

public class CalculateYBus {
	
	//No reason, just an empty constructor
	public CalculateYBus()	{}
	
	
	//Method that checks the breaker state from a given arraylist. If the breaker is open, it is removed from the list totally
	//Returns a list of all the closed breakers.
	public ArrayList<String> checkBreakerState(ArrayList<String> state){
		
		for(int i = 0;i<state.size();i++){
			if(state.get(i).equals("false")){
				//System.out.println("Breaker with ID "+state.get(i-1)+ " is closed");
				state.remove(i);
				i--;
			}
			if(state.get(i).equals("true")){
				//System.out.println("Breaker with ID "+state.get(i-1)+ " is open. Breaker ID must be removed");
				state.remove(i);
				state.remove(i-1);
				i--;
			}
		}
		return state;
	}
	
	
	//Method that checks the connectivity of the terminals. If the terminal is not connected, it is removed from the list
	//Returns a list of all the connected terminals.
	//Takes two lists, a list of all the terminals and a list of the terminal ID and its connectivity state
	public ArrayList<String> checkConnectedTerminal(ArrayList<String> connTerminal,ArrayList<String> terminalState){
		
		String checkID = null;
		
		for(int i = 0;i<terminalState.size();i++){
			if(terminalState.get(i).equals("false")){
				checkID = terminalState.get(i-1);	//Stores the ID of the terminal that is not connected
				for(int j = 0;j<connTerminal.size();j++){
					if(connTerminal.get(j).equals(checkID)){
						connTerminal.remove(j);		//Removes from the list of all terminals the selected terminal
						connTerminal.remove(j);		//Removes also the conducting equipment and connectivity node IDs
						break;
					}
				}
			}
		}
		return connTerminal;
	}
	
	
	//Method that replaces the conducting equipment ID of the terminals that make a reference to the transformer with the
	//ID of the specific winding in order for the terminal to refer to the winding. Used for the Ybus matrxi calculation
	public ArrayList<String> replaceWindingInTerminal(ArrayList<String> Twinding,ArrayList<String> connTerminal,ArrayList<String> transformer){
		
		for(int i = 0;i<Twinding.size();i+=2){
			String winding = Twinding.get(i);		//Winding ID
			String terminalID = Twinding.get(i+1);	//Terminal ID
			for(int j = 0;j<connTerminal.size();j+=2){
				if(terminalID.equals(connTerminal.get(j))){
					String[] parts = connTerminal.get(j+1).split(",");		//Splits the string as it contains both cond. equipment and con. node
					for(int k = 0;k<parts.length;k++){
						for(int m = 0;m<transformer.size();m++){
							if(parts[k].equals(transformer.get(m))){
								parts[k] = winding;		//replaces the transformer ID with the winding ID in the terminal
								break;
							}
						}
					}
				connTerminal.set(j+1,String.join(",", parts));
					break;
				}
			}
		}
		return connTerminal;
	}
	
	
	//Method that groups terminal based on their connectivity node. This way, the program tries to create the system's
	//buse based on the connected objects. Uses an arrayList of ArrayLists where each contained ArrayList is a group of terminals
	public ArrayList<ArrayList<String>> GroupTerminalByConnNode(ArrayList<String> connTerminal,ArrayList<String> connNode){
		
		ArrayList<ArrayList<String>> GroupList = new ArrayList<ArrayList<String>>();
		
		for(int i = 0;i<connNode.size();i++){
			ArrayList<String> GroupTerminals = new ArrayList<String>();
			for(int j = 1;j<connTerminal.size();j+=2){
				if(connTerminal.get(j).contains(connNode.get(i))){
					
					//Groups terminal by checking if they have the same connectivity node
					GroupTerminals.add(connTerminal.get(j-1));
				}
			}
			
			//By the end of each iteration, it adds on the ArrayList, the arrarList of all the interconnected terminals
			GroupList.add(GroupTerminals);
		}
		return GroupList;
	}
	
	
	//Method that further groups terminals based on the state of the breakers. The groups of terminals that are connected
	//via breaker are checked in this case. It checks the terminals individually and if the breaker is closed in this case,
	//it further groups the terminals into and ArrayList. The result is the final form of buses in the system.
	//The breakerTerminal array is obtained from the following methods that checks if the breaker is closed before calling this
	//function. The connection of terminals is checked also in the following method.
	public ArrayList<ArrayList<String>> GroupTerminalsByBreaker(ArrayList<ArrayList<String>> GroupList,ArrayList<String> breakerTerminal){
			
		for(int m = 0;m<GroupList.size();m++){
			
			for(int i = 0;i<breakerTerminal.size();i++){
			String[] parts = breakerTerminal.get(i).split(",");
				for(int n = m+1;n<GroupList.size();n++){
					if(connectTerminals(GroupList.get(m),GroupList.get(n),parts)){
						(GroupList.get(m)).addAll(GroupList.get(n));
						GroupList.remove(n);
					}
				}
			}
		}
		return GroupList;
	}
	
	//Method that checks the connectivity between two groups of terminals. Based on the terminals of the breakes it checks
	//if between the groups there is connection. If it does, then it returns a true values in order to be used in the above
	//method to further group the terminals. If there is no connectivity, then it returns a false value
	public boolean connectTerminals(ArrayList<String> List1,ArrayList<String> List2, String[] parts){
		
		boolean FirstTerminalFound = false;
		String checkedID = "";
		
		for(int m = 0;m<parts.length;m++){
			for(int k = 0;k<List1.size();k++){
				if(parts[m].equals(List1.get(k))){
					FirstTerminalFound = true;
					checkedID = parts[m];
					break;
				}
			}
		}
					
		if(FirstTerminalFound){
			for(int m = 0;m<parts.length;m++){
				for(int j = 0;j<List2.size();j++){
					if(parts[m].equals(List2.get(j)) && checkedID!=parts[m]){
						return true;
					}
				}
			}
		}
		return false;
	}
		
	
	//Based on the array that contains all the closed breakers in the system, we create a list that contains the two terminals
	//of each breaker. It will be used to further group the terminal in order to take the system's topology.
	public ArrayList<String> findBreakerTerminals(ArrayList<String> breakers,ArrayList<String> connTerminal){
		
		ArrayList<String> breakerTerminals = new ArrayList<String>();
		
		//Method works only for closed breakers in the system
		
		for(int i = 0;i<breakers.size();i++){
			String usedBreaker = breakers.get(i);
			ArrayList<String> combineTerminals = new ArrayList<String>();
			for(int j = 1;j<connTerminal.size();j+=2){
				if(connTerminal.get(j).contains(usedBreaker)){
					combineTerminals.add(connTerminal.get(j-1));
				}
				
				//ArrayList must be always of size of 2
				if(combineTerminals.size() == 2){
					breakerTerminals.add(combineTerminals.get(0)+","+combineTerminals.get(1));
					break;
				}
			}
		}
		return breakerTerminals;
	}
	
	
	//Method that determines the base voltage of a specific element determined by its ID. The method checks the base voltage
	//of the specified element from an array list and then correlates the base voltage ID with the corresponding value. Returns
	//the base voltage value utilized for the Ybus matrix calculations.
	public double FindBaseVoltage(String elementID,ArrayList<String> BaseVoltage,ArrayList<String> VoltageElement){
		
		String storeID = null;
		double baseVolt = 0;
		
		//Checks the ID of the base voltage that corresponds to the element of interest
		for(int i = 0;i<VoltageElement.size();i++){
			if((VoltageElement.get(i)).equals(elementID)){
				storeID = VoltageElement.get(i+1);
				break;
			}
		}
		
		//Matches the obtained base voltage ID with its value
		for(int j = 0;j<BaseVoltage.size();j++){
			if((BaseVoltage.get(j)).equals(storeID)){
				baseVolt = Double.parseDouble(BaseVoltage.get(j+1));
				break;
			}
		}
		return baseVolt;
	}
	
	
	//Method that gets the terminals of a connected line to the system. Checks if the given element ID matches a terminal
	//and then returns an arraylist of the two terminals that correspond to the specific line
	public ArrayList<String> getTerminalfromID(String elementID,ArrayList<String> connTerminal){
		
		ArrayList<String> elementTerminal = new ArrayList<String>();
		
		for(int i =0;i<connTerminal.size();i++){
			if(connTerminal.get(i).contains(elementID)){
				
				//Adds to the list the ID of the terminal
				elementTerminal.add(connTerminal.get(i-1));
			}
		}
		
		//Checks that the list has a size of 2 in each case.
		if(elementTerminal.size() == 0){
			System.out.println("Element not associated with terminal or terminal is disconnected");
		}
		else if(elementTerminal.size() > 2){
			System.out.println("Invalid terminal number. Check Connected Terminals List");
		}
		return elementTerminal;
	}
	
	
	//Method that gets the terminals of a connected line to the system. Checks if the given element ID matches a terminal
	//and then returns an arraylist of the two terminals that correspond to the specific line
	public String getWindingTerminal(String elementID,ArrayList<String> connTerminal){
		
		String tempString = null;
		
		for(int i =0;i<connTerminal.size();i++){
			if(connTerminal.get(i).contains(elementID)){
				
				tempString = connTerminal.get(i-1);
			}
		}
		return tempString;
	}
	
	
	//Checks if a selected terminal is connected to a bus by matching its value with the ArrayList that contains the bus. Returns
	//true for connection
	public boolean ObjectConnectedtoBus(String terminal,ArrayList<String> GridBus){
		
		for(int i = 0;i<GridBus.size();i++){
			if(!terminal.equals("")){
				if((GridBus.get(i)).equals(terminal)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	//Method that calculates the line admittance and shunt admittance of a given element. It takes into account the base voltage of the line and the base
	//power and calculates the requested admittances. Returns an array of two admittances (line and shunt admittance). A line is modelled with a conventional
	//PI-model that is comprised of a line impedance (r+jx) and two shunt admittances that are same and equal to the half of the lines' shunt admittance
	//i.e (g+jb)/2 and (g+jb)/2 that are placed in the two edges of the line
	public ComplexNumbers[] calcLineAdmittance(ReadXML object,String elementID,double basePower,double baseVolt) throws NegativeArraySizeException{
		
		double r, x, g, b, baseImp;
		
		//Calculate base impedance of the line
		baseImp = baseVolt*baseVolt/basePower;
		
		//Convert line elements to the per unit system
		r = object.getLineWindingElements("cim:ACLineSegment", elementID,'r')/baseImp;
		x = object.getLineWindingElements("cim:ACLineSegment", elementID,'x')/baseImp;
		g = object.getLineWindingElements("cim:ACLineSegment", elementID,'g')*baseImp;
		b = object.getLineWindingElements("cim:ACLineSegment", elementID,'b')*baseImp;
		
		ComplexNumbers[] LineAdmittance = new ComplexNumbers[2];
		LineAdmittance[0] = (new ComplexNumbers(r,x)).reciprocal();
		LineAdmittance[1] = new ComplexNumbers(g/2.0,b/2.0);
		
		return LineAdmittance;
		
	}
	
	
	//Method that calculates the line and shunt admittance of each winding of the transformer. It takes into account the base voltage and power in each winding
	//and based on that it calculates the requested admittances. Returns an array of the three admittances(shunt in each winding and line admittance). Basically,
	//a transformer is modelled with a conventional PI-model that is comprised of the line admittance that is the sum of the line admittance of each winding
	//in the per-unit system (r1+jx1+r2+jx2) and two shunt admittances g1+jb1 and g2+jb2 that are the shunt adimttances of each winding respectively.
	public ComplexNumbers[] calcTransAdmittance(ReadXML object,String winding1,String winding2,double baseVolt1,double baseVolt2,double basePower) throws NegativeArraySizeException{

		
		double r1, x1, g1, b1, baseImp1;
		double r2, x2, g2, b2, baseImp2;
		
		//Calculate base impedance for each winding
		baseImp1 = baseVolt1*baseVolt1/basePower;
		baseImp2 = baseVolt2*baseVolt2/basePower;
		
		//Convert transformer's first winding to the per unit system
		r1 = object.getLineWindingElements("cim:PowerTransformerEnd", winding1, 'r')/baseImp1;
		x1 = object.getLineWindingElements("cim:PowerTransformerEnd", winding1, 'x')/baseImp1;
		g1 = object.getLineWindingElements("cim:PowerTransformerEnd", winding1, 'g')*baseImp1;
		b1 = object.getLineWindingElements("cim:PowerTransformerEnd", winding1, 'b')*baseImp1;
		
		//Convert transformer's second winding to the per unit system
		r2 = object.getLineWindingElements("cim:PowerTransformerEnd", winding2, 'r')/baseImp2;
		x2 = object.getLineWindingElements("cim:PowerTransformerEnd", winding2, 'x')/baseImp2;
		g2 = object.getLineWindingElements("cim:PowerTransformerEnd", winding2, 'g')*baseImp2;
		b2 = object.getLineWindingElements("cim:PowerTransformerEnd", winding2, 'b')*baseImp2;
		
		
		if(r1+r2+x1+x2 == 0){
			System.out.println("Infinite admittance detected in the system");
			System.out.println("Check winding IDs: " +winding1+ " and " +winding2);
		}
		
		//Creates an array of th three calculated admittances and returns its content
		ComplexNumbers[] TransAdmittance = new ComplexNumbers[3];
		TransAdmittance[0] = (new ComplexNumbers((r1+r2),(x1+x2))).reciprocal();
		TransAdmittance[1] = new ComplexNumbers(g1,b1);		//T1 shunt admittance
		TransAdmittance[2] = new ComplexNumbers(g2,b2);		//T2 shunt admittance
		
		return TransAdmittance;
	}
	
}
