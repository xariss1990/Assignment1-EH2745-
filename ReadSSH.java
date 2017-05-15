package assignment;

//The purpose of this class is to handle the SSH files given as input in the program. Basically, it has the same functions as
//ReadXMl due to inheritance as well as one more method that handles the SSH files, reads and stores the necessary data in
//an ArrayList, with the same structure as the corresponding method in the ReadXML class

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReadSSH extends ReadXML{

	//Does exactly the same thing as the constructor of ReadXML.
	public ReadSSH(String dir){
		super(dir);
	}
	
	//Same structure and methodology with ExtractNode in the ReadXML class. Based on the tag, it stores in an ArrayList the 
	//necessary data and returns the list.
	public ArrayList<String> extractInfoFromSSHFile(String tag, ArrayList<String> newList){
		
		switch(tag){
		case "cim:SynchronousMachine":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID, P and Q of Machine. ID is required for the merge of the two ArrayList in 
				//order to place the correct values to each object.
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					newList.add("Object");	//Just a marker in order to check how many objects are in the List. Used for the arrylist merge
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:RotatingMachine.p").item(0).getTextContent());
					newList.add(element.getElementsByTagName("cim:RotatingMachine.q").item(0).getTextContent());
				}
			}
			break;
		case "cim:RegulatingControl":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and value of Regulating Control
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					newList.add("Object");
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:RegulatingControl.targetValue").item(0).getTextContent());
				}
			}
			break;
		case "cim:EnergyConsumer":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID, P and Q of the Energy Consumer
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					newList.add("Object");
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:EnergyConsumer.p").item(0).getTextContent());
					newList.add(element.getElementsByTagName("cim:EnergyConsumer.q").item(0).getTextContent());
				}
			}
			break;
		case "cim:Breaker":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and state of Breaker
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					newList.add("Object");
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:Switch.open").item(0).getTextContent());
				}
			}
			break;
		case "cim:RatioTapChanger":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and step of TapChanger
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					newList.add("Object");
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:TapChanger.step").item(0).getTextContent());
				}
			}
			break;
			//Terminal is used only for the Ybus matrix in order to check if a terminal is connected to the system or not
		case "cim:Terminal":
			for (int i = 0; i<dataList.getLength(); i++){
				
				//Specify rdf:ID and connection state of Terminal
				Node mainNode = dataList.item(i);
				if(mainNode.getNodeType() == mainNode.ELEMENT_NODE){
					
					Element element = (Element) mainNode;
					
					//Add to ArrayList
					newList.add(element.getAttribute("rdf:about").replace("#",""));
					newList.add(element.getElementsByTagName("cim:ACDCTerminal.connected").item(0).getTextContent());
				}
			}
			break;
		default:
			System.out.println("Inserted TagName either does not exist in file or further code must be written");
		}
		return newList;
	}
}
