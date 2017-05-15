package assignment;

//ReadXML class is responsible for handling an EQ File that is given when an object of this class is created in the form
//of file directory. Its goal is to create a document parser that will handle the information in the file. The methods
//of this class are able to parse an XML file and store in an ArrayList all the necessary information that are requested
//and utilized for the database in the following steps. Basically, for each CIM object, a number of attributes can be stored
//and the form of the code allows for the easy expansion in case more data are required. Apart from that, it returns
//information that are not directly embedded in each element e.g the BaseVoltage of the Energy Consumer that does not exist as
//child of this particular object.


import org.w3c.dom.*;
import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ReadXML {
	
	//Given as protected in order to used in the ReadSSH class as well through the extension
	protected File xmlFile;
	protected Document doc;
	protected NodeList dataList;
	
	public ReadXML(String directory){
		xmlFile = new File(directory);
	}
	
	//Ìethod that creates a parser based on the directory that is given in the constructor. Directory is given from the
	//user manually through the GUI search directory. If the file is not found in the specified directory, the program closes
	public void createParser(){
		
			try{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(xmlFile);
				
				doc.getDocumentElement().normalize();
				
		//File not found exception
		}catch(FileNotFoundException fnf){
			System.out.println("File not found in the directory");
			System.out.println("Program terminates ---------------------------------->");
			System.exit(0);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	//Method that takes as input a string name that is a tag name inside the XML file. Its form is "cim:tagName" in order to
	//create a full list of the nodes that have the specific name. If the tag name doesn't exist or the root of the XML is
	//empty string, the program exits.
	public void createNodeList(String nodeName){
		
		String root = "";	//The root of the XML file. Each file has a single root
		
		try{
			root = doc.getDocumentElement().getNodeName();
			//System.out.println("Root element :" + root);
				if(root == null){
					System.out.println("XML File is empty");
					System.exit(0);
				}
				
			dataList = doc.getElementsByTagName(nodeName);
				if(dataList.getLength() == 0){
					System.out.println("No such tag name exists in file");
					System.out.println("Please check if the given tag name " +nodeName+ " is spelled correctly.");
					System.exit(0);
				}
				
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//ExtractNode method takes as input the specified string name as well as the previously created datalist in order to
	//add into an arrylist the required objects for parsing. There are two types of objects: the ones that can be found
	//from their tag name the others that are specified by the resource ID. The method uses the switch statement to choose
	//between the different tagNames and can be expanded if other tagnames are required
	public ArrayList<String> extractNodefromEQFile(String nodeName, ArrayList<String> tempArrayList){
		
		String store = null;
		
		switch(nodeName){
		case "cim:BaseVoltage":
		
				//Check all the elements of the Nodelist specified for each tag name
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify rdf:ID and value in kV for BaseVoltage for each node of the list
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						//Each child of the mainNode is considered as element in order to acquire the desired values
						Element element = (Element) mainNode;
						
						//Add the values to an arraylist that returns it back
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:BaseVoltage.nominalVoltage").item(0).getTextContent());
					}
				}
				break;
		case "cim:Substation":
				
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify the rdf:ID and name of the Substation
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					
						//Receive rdf:ID from SubGeographical Region. Call method to get the relation - Method in Line 367
						store = getRelatedElements("Substation.Region",mainNode);
						tempArrayList.add(store);
					}
				}
				break;
		case "cim:VoltageLevel":
			
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify rdf:ID and name of VoltageLevel
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
						
						//Receive rdf:ID from Substation and baseVoltage - Method in Line 367
						store = getRelatedElements("VoltageLevel.Substation",mainNode);
						tempArrayList.add(store);
						store = getRelatedElements("VoltageLevel.BaseVoltage",mainNode);
						tempArrayList.add(store);
					}
				}
				break;
		case "cim:GeneratingUnit":
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify rdf:ID, name, maxP and minP of GeneratingUnit
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
						tempArrayList.add(element.getElementsByTagName("cim:GeneratingUnit.maxOperatingP").item(0).getTextContent());
						tempArrayList.add(element.getElementsByTagName("cim:GeneratingUnit.minOperatingP").item(0).getTextContent());	
					
						//Specify rdf:ID of Equipment Container - In this case, container is the PowerTransformer - Method in Line 367
						store = getRelatedElements("Equipment.EquipmentContainer",mainNode);
						tempArrayList.add(store);
					}
				}
				break;
		case "cim:SynchronousMachine":
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify rdf:ID, name and ratedS of Synchronous Machine
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						//Object here serves as a pointer for the merge of the two arraylists from the different files
						//It is used only for the objects that have values in both files.
						tempArrayList.add("Object");
						
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
						tempArrayList.add(element.getElementsByTagName("cim:RotatingMachine.ratedS").item(0).getTextContent());
					
						
						//Specify rdf:ID of Generating Unit, Regulating Control and Equipment Container(VoltageLevel) - Method in Line 367
						store = getRelatedElements("RotatingMachine.GeneratingUnit",mainNode);
						tempArrayList.add(store);
						store = getRelatedElements("RegulatingCondEq.RegulatingControl",mainNode);
						tempArrayList.add(store);
						store = getRelatedElements("Equipment.EquipmentContainer",mainNode);
						tempArrayList.add(store);
					}
				}
				break;
		case "cim:RegulatingControl":
				for (int i = 0; i<dataList.getLength(); i++){
					
					//Specify rdf:ID and name of RegulatingControl
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						tempArrayList.add("Object");
						
						tempArrayList.add(element.getAttribute("rdf:ID"));
						tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					}
				}
				break;
		case "cim:PowerTransformer":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and name of PowerTransformer
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					
					
					//Specify rdf:ID of Equipment Container(Voltage Level in this case) - Method in Line 367
					store = getRelatedElements("Equipment.EquipmentContainer",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
		case "cim:EnergyConsumer":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and name of EnergyConsumer
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					tempArrayList.add("Object");
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					
					
					//Specify rdf:ID of Equipment Container(VoltageLevel) and Base Voltage - Method in Line 367
					store = getRelatedElements("Equipment.EquipmentContainer",mainNode);
					tempArrayList.add(store);
					store = getRelatedElements("EnergyConsumer.BaseVoltage",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
		case "cim:PowerTransformerEnd":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID, name, r and x of TransformerEnd
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					tempArrayList.add(element.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent());
					tempArrayList.add(element.getElementsByTagName("cim:PowerTransformerEnd.x").item(0).getTextContent());
					
					//Specify rdf:ID of Equipment Container(VoltageLevel), Terminal(for Ybus only) and Power Transformer - Method in Line 367
					store = getRelatedElements("TransformerEnd.Terminal",mainNode);
					tempArrayList.add(store);
					store = getRelatedElements("PowerTransformerEnd.PowerTransformer",mainNode);
					tempArrayList.add(store);
					store = getRelatedElements("TransformerEnd.BaseVoltage",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
		case "cim:Breaker":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and name of Breaker
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					tempArrayList.add("Object");
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					
					
					//Specify rdf:ID of Equipment Container(Voltage Level) - Method in Line 367
					store = getRelatedElements("Equipment.EquipmentContainer",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
		case "cim:RatioTapChanger":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and name of TapChanger
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					tempArrayList.add("Object");
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
					
					//Specify rdf:ID of Transformer End - Method in Line 367
					store = getRelatedElements("RatioTapChanger.TransformerEnd",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
			//AC Line is used only for the Ybus matrix determination
		case "cim:ACLineSegment":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID of Line
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					
					//Specify rdf:ID of Base Voltage - Used for admittance calculation in per-unit notation - Method in Line 367
					store = getRelatedElements("ConductingEquipment.BaseVoltage",mainNode);
					tempArrayList.add(store);
				}
			}
			break;
			//Used for the Ybus determination algorithm only
		case "cim:Terminal":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID of Terminal
				//Conductivity Node and Conducting Equipment are utilized for the Ybus determination
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					
					tempArrayList.add(element.getAttribute("rdf:ID"));
					tempArrayList.add(""+getRelatedElements("Terminal.ConductingEquipment",mainNode)+","+getRelatedElements("Terminal.ConnectivityNode",mainNode)+"");
				}
			}
			break;
			//Used for the Ybus determination algorithm only
		case "cim:ConnectivityNode":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and name of TapChanger
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					Element element = (Element) mainNode;
					tempArrayList.add(element.getAttribute("rdf:ID"));
				}
			}
			break;
		default:
					System.out.println("Inserted TagName either does not exist in file or further code must be written");
				}
		return tempArrayList;
	}
	
	
	//Method that can give the attribute of a child inside a selected node. Basically, it correlates a given string name
	//with the children of a node that contain attributes. The string name is applicable with the structure given inside the
	//xml file.
	public String getRelatedElements(String relatedElement, Node firNode){
		
		//Flag that is marked as false when the string name exists in a node as a child in order to avoid conflicts
		//When the flag remains true, this means that the stringName is not connected directly with the node and further
		//analysis is required
		boolean flag = true;	
		String reqId = "";
		String ID1 = "";
		String ID2 = "";
		String reqName = "";
		NodeList xmlChild = doc.getDocumentElement().getChildNodes();
		
		if (firNode != null && firNode.hasChildNodes()){
			
			NodeList secNode = firNode.getChildNodes();
			
			//Searches the children of a node to find the requested attribute
			for(int i = 0;i<secNode.getLength();i++){
				Node thirdNode = secNode.item(i);
				if(thirdNode.hasAttributes()){
					if(thirdNode.getNodeType() == thirdNode.ELEMENT_NODE){
					
					Element reqElement = (Element) thirdNode;
					reqName = reqElement.getTagName().replace("cim:","");
					if(reqName.equals(relatedElement)){
					
						ID1 = reqElement.getAttribute("rdf:resource").replace("#","");
						flag = false;	//Flag set to false to avoid conflicts
						
						//This part is more like a corroboration that the found ID exists as a Node in the file
						for(int m = 0;m<xmlChild.getLength();m++){
							Node child = xmlChild.item(m);
							if(child.hasAttributes()){
								if(child.getNodeType() == child.ELEMENT_NODE){
									Element elChild = (Element) child;
									if(elChild.getAttribute("rdf:ID").equals(ID1)){
										//System.out.println(ID1);
									}
								}
							}
						}
					}
					
					//This part of the code is when the given String name is not directly correlated with the examined node
					//something that happens if the base voltage of the consumer is requested as it can be found indirectly 
					//through the voltage level. Checks the file based on the given name that is split into parts.
					else if(!reqName.equals(relatedElement)){
							String[] parts = relatedElement.split("\\.");
							reqId = reqElement.getAttribute("rdf:resource").replace("#","");
							
							if(doc!= null && doc.getDocumentElement().hasChildNodes()){
			
								for(int n = 0;n<xmlChild.getLength();n++){
									Node rootChild = xmlChild.item(n);
									if(rootChild.hasAttributes()){
										if(rootChild.getNodeType() == rootChild.ELEMENT_NODE){
											Element rootElChild = (Element) rootChild;
											
											//Based on the resource ID of the children in the selected node, it finds the parent nodes
											if(rootElChild.getAttribute("rdf:ID").equals(reqId)){
												
												if(rootElChild!= null && rootElChild.hasChildNodes()){
													NodeList selecNode = rootElChild.getChildNodes();
													for(int l = 0;l<selecNode.getLength();l++){
														Node tempNode = selecNode.item(l);
														
														//Checks if the parent node has children.If it does, checks if the string name
														//parts exists in this node's children. If it does, it returns the ID
														//of the node that has the given name
														if(tempNode.hasAttributes()){
															if(tempNode.getNodeType() == tempNode.ELEMENT_NODE){
																
																Element tempElem = (Element) tempNode;
																if(tempElem.getTagName().contains(parts[1])){
																	ID2 = tempElem.getAttribute("rdf:resource").replace("#","");
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		//Returns either of IDs if it exists directly in the node or not
		if(flag == false){
			return ID1;
		}else
			return ID2;
	}
	
	
	//Used exclusively in the YBus matrix determination. Based on the tag name(either ACLine Segment or PowerTransformerEnd)
	// and the ID of the requested node, it returns specific line characteristics for the YBus depending on the value of attr
	//that is either r,x,g and b.
	public double getLineWindingElements(String tag,String elementID,char attr){
		
		String result = null;
		
		try{
			dataList = doc.getElementsByTagName(tag);
				if(dataList.getLength() == 0){
					System.out.println("No such tag name exists in file");
				}
				
		}catch(Exception element){
			element.printStackTrace();
		}
		
		if(attr == 'r' || attr == 'x' || attr == 'g' || attr == 'b'){
			switch(tag){
			case "cim:ACLineSegment":
				for (int i = 0; i<dataList.getLength(); i++){
					
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						if(element.getAttribute("rdf:ID").equals(elementID)){
							if(attr == 'r'){
								result = element.getElementsByTagName("cim:ACLineSegment.r").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'x'){
								result = element.getElementsByTagName("cim:ACLineSegment.x").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'g'){
								result = element.getElementsByTagName("cim:ACLineSegment.gch").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'b'){
								result = element.getElementsByTagName("cim:ACLineSegment.bch").item(0).getTextContent();
								return Double.parseDouble(result);
							}
						}
					}
				}
				break;
			case "cim:PowerTransformerEnd":
				for (int i = 0; i<dataList.getLength(); i++){
					
					Node mainNode = dataList.item(i);
					if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
						
						Element element = (Element) mainNode;
						if(element.getAttribute("rdf:ID").equals(elementID)){
							if(attr == 'r'){
								result = element.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'x'){
								result = element.getElementsByTagName("cim:PowerTransformerEnd.x").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'g'){
								result = element.getElementsByTagName("cim:PowerTransformerEnd.g").item(0).getTextContent();
								return Double.parseDouble(result);
							}
							else if(attr == 'b'){
								result = element.getElementsByTagName("cim:PowerTransformerEnd.b").item(0).getTextContent();
								return Double.parseDouble(result);
							}
						}
					}
				}
				break;
			default:
				System.out.println("Requested elements is not connected to a bus of does not exist");
			}
		}
		
		System.out.println("WARNING!!!!!!!!!!!!!!!!!!!!");
		System.out.println("Invalid attribute given. Ybus matrix cannot be calculated");
		System.out.println("Please check the inserted attribut----------------->");
		return 0;	//If the value of attr is not applicable, it returns a zero value
	}
	
	
	//Method that takes the ID of the powerTransformer and returns an arralist of its windings, normally two or three	
	public ArrayList<String> getWindingfromTransf(String transID){
		
		ArrayList<String> TransWind = new ArrayList<String>();
		
		try{
			dataList = doc.getElementsByTagName("cim:PowerTransformerEnd");
			if(dataList.getLength() == 0){
				System.out.println("No such tag name exists in file");
			}
			
			for (int i = 0; i<dataList.getLength(); i++){
				
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					if((getRelatedElements("PowerTransformerEnd.PowerTransformer",mainNode)).equals(transID)){
						
						TransWind.add(element.getAttribute("rdf:ID"));
					}
				}
			}
				
		}catch(Exception element){
			element.printStackTrace();
		}
		
		return TransWind;
	}
	
}