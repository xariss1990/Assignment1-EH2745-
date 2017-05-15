package assignment;

//The mainMethod class of the program is responsible for the total execution of the program. It is distinguished in 3 parts:
//XML Parsing, Database Creation and Ybus matrix calculation. In each part a small GUI is implemented in order to allow interaction
//with the user. At first, a file browser is created where the user must choose the EQ and SSH file that they want o use in
//the program. The directories are stored in an empty file every time the program is created and then they are given as input
//in the two objects that are used for the parsing of the EQ and SSH files respectively.
//The second part involves the creation of the relational database. The user must place his credentials in the implemented
//GUI in order to create a database and store the necessary data. If this doesn't happen, then the program exits without any
//further action.
//Finally, the Ybus matrix determination involves the steps that are described in the corresponding class. Basically, a few
//preparatory steps are taken and then an empty 2D array is created based on the determined number of buses. Then the line and
//winding elements are inserted in order to give the matrix its final form. Finally, a GUI is implemented that presents the
//requested array as output

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.lang.Object;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.JFileChooser;

public class MainMethod extends JFrame {

	public static void main(String[] args){
		
		Connection conn = null;
		Statement stmt = null;
		double basePower = 1000;

		
//------------------------------------------------------------------------------------------------------------------------//
//----------------------------------------------------BROSWER OF FILES----------------------------------------------------//
//------------------------------------------------------------------------------------------------------------------------//
		
		int openFile;
		PrintWriter out = null;
		try{
			//Basically we create a file that will store the files' directories we want to parse
			//The file is emptied after the directories are given to the objects
			out = new PrintWriter("C:\\Users\\Lenovo\\workspace\\Assignment1\\src\\assignment\\SaveDirectory.txt");
		}catch(FileNotFoundException excep1){
			System.out.println("SaveDirectory.txt file not exists");
		}
		

		//Creates the directory for browsing the requested files
		JFileChooser fileChoose = new JFileChooser();
		fileChoose.setDialogTitle("Choose Directory");
		fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);

		fileChoose.setMultiSelectionEnabled(true);
		openFile = fileChoose.showOpenDialog(null);
		
		int fileLength = 0;
		
		if(openFile == JFileChooser.APPROVE_OPTION){
			
			File[] myFiles = fileChoose.getSelectedFiles(); //The chose files are stored in the array
			String[] tempDir = new String[myFiles.length];
			fileLength = myFiles.length; //Store the SaveDirectory file length to use it to create the parser objects
			
			for(int i = 0;i<myFiles.length;i++){
				try{
					//In this part, we convert the path to a string that is processed by the parser
					
					tempDir[i] = myFiles[i].getPath(); 
					tempDir[i] = tempDir[i].replace("\\","\\\\");
					System.out.println(tempDir[i]);
					}catch (NullPointerException e){
						System.out.println("Directory Not Found");
					}catch (Exception otherE){
						System.out.println("Error in storing directory in file");
						otherE.printStackTrace();
					}
					out.println(tempDir[i]);
			}
			System.out.println("File is Created");
			out.close();
		}
		else{
			System.out.println("No file chosen");
			out.close();
		}
		
		
		//Once the file contains the requested directories, a file reader is used to store the directories to the objects
		FileReader fr = null;
		BufferedReader br = null;
		String line =  null;
		String[] storedDirec = new String[fileLength];
		
		try{
			fr = new FileReader("C:\\Users\\Lenovo\\workspace\\Assignment1\\src\\assignment\\SaveDirectory.txt");
			br = new BufferedReader(fr);
			for(int i = 0;i<fileLength;i++){
				if((line = br.readLine()) !=null){
					storedDirec[i] = line;
				}
			}
			br.close();
			fr.close();
		}catch(FileNotFoundException nofile){
			System.out.println("There is no such file in directory");
		}catch (IOException ioE){
			System.out.println("IO Error");
			ioE.printStackTrace();
		}catch(Exception e1){
			e1.printStackTrace();
		}
		
		//We want to handle the exception in case the cancel button is pressed initially
		//In this case, no objects are created according to the message
		try{
		//Parse the xml EQ file
		ReadXML eqObj = new ReadXML(storedDirec[0]);
		eqObj.createParser();
		
		//Parse the xml SSH file
		ReadSSH sshObj = new ReadSSH(storedDirec[1]);
		sshObj.createParser();
		

		//Emtpy the file for the next time
		PrintWriter del = null;
		try{
			del = new PrintWriter("C:\\Users\\Lenovo\\workspace\\Assignment1\\src\\assignment\\SaveDirectory.txt");
			del.print("");
			del.close();
		}catch(FileNotFoundException excep1){
			System.out.println("SaveDirectory.txt file not exists");
		}
		
		
		
//------------------------------------------------------------------------------------------------------------------------//

		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////// XML PARSING ///////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//In this part, the created objects for the EQ and SSH are used in order to parse the required information from the files. When the method
		// in the class is called, it stores the required information in the arrylists. In some cases, due to data in both files, a new method
		//is called in order to combine the data from both arraylists into one. Apart from that new arrylists are created from the already 
		//obtained data in order to be utilized for the sql database creation as well as the calculation of the admittance matrix of the system. In the
		//arrylists, are stored only the requested data. Data required in the next sections are stored in different arrylists
		
		
		//Base Voltage Elements
		ArrayList<String> baseVolt = new ArrayList<String>();
		ArrayList<String> sqlBaseV = new ArrayList<String>();	//Used exclusively for the SQL Database. The data are processed in order to be used
																//for the database insert function in the next section
		
		eqObj.createNodeList("cim:BaseVoltage");
		eqObj.extractNodefromEQFile("cim:BaseVoltage",baseVolt);
		try{
			for(int i = 0;i<baseVolt.size();i+=2){
				double value = Double.parseDouble(baseVolt.get(i+1));	//Just to insert a double value and not a string to the database
				sqlBaseV.add("'"+baseVolt.get(i)+"',"+value+"");
			}
		}catch(ArrayIndexOutOfBoundsException e1){
			e1.printStackTrace();
		}

		
		//Substation Elements
		ArrayList<String> substation = new ArrayList<String>();
		ArrayList<String> sqlSub = new ArrayList<String>();		//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:Substation");
		eqObj.extractNodefromEQFile("cim:Substation",substation);
		//System.out.println("Substation");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t Region rdf:ID");
		try{
			for(int i = 0;i<substation.size();i+=3){
				//System.out.printf("%s \t %s \t %s \n",substation.get(i), substation.get(i+1), substation.get(i+2));
				sqlSub.add("'"+substation.get(i)+"','"+substation.get(i+1)+"','"+substation.get(i+2)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e2){
			e2.printStackTrace();
		}
		
		
		//Voltage Level Elements
		ArrayList<String> voltLev = new ArrayList<String>();
		ArrayList<String> sqlVlevel = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:VoltageLevel");
		eqObj.extractNodefromEQFile("cim:VoltageLevel",voltLev);
		//System.out.println("VoltageLevel");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t Substation rdf:ID \t \t \t BaseVoltage rdf:ID");
		try{
			for(int i = 0;i<voltLev.size();i+=4){
				//System.out.printf("%s \t %s \t %s \t %s \n",voltLev.get(i), voltLev.get(i+1), voltLev.get(i+2), voltLev.get(i+3));
				double value = Double.parseDouble(voltLev.get(i+1));	//Voltage
				sqlVlevel.add("'"+voltLev.get(i)+"',"+value+",'"+voltLev.get(i+2)+"','"+voltLev.get(i+3)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e3){
			e3.printStackTrace();
		}
		
		
		//Generating Unit Elements
		ArrayList<String> genUnit = new ArrayList<String>();
		ArrayList<String> sqlGu = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:GeneratingUnit");
		eqObj.extractNodefromEQFile("cim:GeneratingUnit",genUnit);
		//System.out.println("GeneratingUnit");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t \t \t Maximum P \t Minimum P \t Equipment Container rdf:ID");
		try{
			for(int i = 0;i<genUnit.size();i+=5){
				//System.out.printf("%s \t %s \t %s \t %s \t %s \n",genUnit.get(i), genUnit.get(i+1), genUnit.get(i+2), genUnit.get(i+3), genUnit.get(i+4));
				double value1 = Double.parseDouble(genUnit.get(i+2));	//Max P
				double value2 = Double.parseDouble(genUnit.get(i+3));	//Min P
				sqlGu.add("'"+genUnit.get(i)+"','"+genUnit.get(i+1)+"',"+value1+","+value2+",'"+genUnit.get(i+4)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e4){
			e4.printStackTrace();
		}
		
		
		//Synchronous Machine Elements
		ArrayList<String> machE = new ArrayList<String>();		//ArrayList extracted from EQ File
		ArrayList<String> machS = new ArrayList<String>();		//ArrayList extracted from SSH file
		ArrayList<String> sqlSync = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:SynchronousMachine");
		eqObj.extractNodefromEQFile("cim:SynchronousMachine",machE);
		sshObj.createNodeList("cim:SynchronousMachine");
		sshObj.extractInfoFromSSHFile("cim:SynchronousMachine",machS);

		
		machE = joinArrayLists(machE,machS);	//Calls the method in order to combine the two arraylists into one
		//System.out.println("SynchronousMachine");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t Rated S \t Generating Unit rdf:ID \t\t Regulating Control rdf:ID \t\t Equipment Container rdf:ID \t\t P \t\t Q");
		try{
			for(int i = 0;i<machE.size();i+=8){
				//System.out.printf("%s \t %s \t %s \t %s \t %s \t %s \t %s \t %s\n",machE.get(i), machE.get(i+1), machE.get(i+2), machE.get(i+3), machE.get(i+4), machE.get(i+5), machE.get(i+6), machE.get(i+7));
				double value1 = Double.parseDouble(machE.get(i+2));	//S
				double value2 = Double.parseDouble(machE.get(i+6));	//P
				double value3 = Double.parseDouble(machE.get(i+7));	//Q
				sqlSync.add("'"+machE.get(i)+"','"+machE.get(i+1)+"',"+value1+","+value2+","+value3+",'"+machE.get(i+3)+"','"+machE.get(i+4)+"','"+machE.get(i+5)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e5){
			e5.printStackTrace();
		}
		
		
		//Regulating Control Elements
		ArrayList<String> regE = new ArrayList<String>();
		ArrayList<String> regS = new ArrayList<String>();
		ArrayList<String> sqlReg = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:RegulatingControl");
		eqObj.extractNodefromEQFile("cim:RegulatingControl",regE);
		sshObj.createNodeList("cim:RegulatingControl");
		sshObj.extractInfoFromSSHFile("cim:RegulatingControl",regS);
		
		
		regE = joinArrayLists(regE,regS);
		//System.out.println("RegulatingControl");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t Target Value");
		try{
			for(int i = 0;i<regE.size();i+=3){
				//System.out.printf("%s \t %s \t %s\n",regE.get(i), regE.get(i+1), regE.get(i+2));
				double value = Double.parseDouble(regE.get(i+2));	//Target value
				sqlReg.add("'"+regE.get(i)+"','"+regE.get(i+1)+"',"+value+"");
			}
		}catch(ArrayIndexOutOfBoundsException e6){
			e6.printStackTrace();
		}
		
		
		//Power Transformer Elements
		ArrayList<String> transform = new ArrayList<String>();
		ArrayList<String> sqlPowerT = new ArrayList<String>();	//Used exclusively for the SQL Database
		ArrayList<String> TransformerID = new ArrayList<String>();	//Used for the YBus admittance matrix section based on already acquired elements
		
		eqObj.createNodeList("cim:PowerTransformer");
		eqObj.extractNodefromEQFile("cim:PowerTransformer",transform);
		//System.out.println("PowerTransformer");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t Equipment Container rdf:ID");
		try{
			for(int i = 0;i<transform.size();i+=3){
				//System.out.printf("%s \t %s \t %s\n",transform.get(i), transform.get(i+1), transform.get(i+2));
				sqlPowerT.add("'"+transform.get(i)+"','"+transform.get(i+1)+"','"+transform.get(i+2)+"'");
				TransformerID.add(transform.get(i));	//Adds only the IDs of connected transformers - Used in Ybus matrix
			}
		}catch(ArrayIndexOutOfBoundsException e7){
			e7.printStackTrace();
		}
		
		
		//Energy Consumer Elements
		ArrayList<String> consumE = new ArrayList<String>();
		ArrayList<String> consumS = new ArrayList<String>();
		ArrayList<String> sqlCons = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:EnergyConsumer");
		eqObj.extractNodefromEQFile("cim:EnergyConsumer",consumE);
		sshObj.createNodeList("cim:EnergyConsumer");
		sshObj.extractInfoFromSSHFile("cim:EnergyConsumer",consumS);
		
		
		consumE = joinArrayLists(consumE,consumS);
		//System.out.println("EnergyConsumer");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t Equipment Container rdf:ID \t\t Base Voltage rdf:ID \t\t\t P \t\t Q");
		try{
			for(int i = 0;i<consumE.size();i+=6){
				//System.out.printf("%s \t %s \t %s \t %s \t %s \t %s\n",consumE.get(i), consumE.get(i+1), consumE.get(i+2), consumE.get(i+3), consumE.get(i+4), consumE.get(i+5));
				double value1 = Double.parseDouble(consumE.get(i+4));	//P
				double value2 = Double.parseDouble(consumE.get(i+5));	//Q
				sqlCons.add("'"+consumE.get(i)+"','"+consumE.get(i+1)+"',"+value1+","+value2+",'"+consumE.get(i+2)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e8){
			e8.printStackTrace();
		}
		
		
		//Power Transformer Winding Elements
		ArrayList<String> winding = new ArrayList<String>();
		ArrayList<String> sqlWind = new ArrayList<String>();	//Used exclusively for the SQL Database
		ArrayList<String> WindingTerminal = new ArrayList<String>();	//Used for the Ybus admittance matrix - Keeps the winding ID and terminalID
		ArrayList<String> WindingBaseVolt = new ArrayList<String>();	//Used for Ybus admittance matrix - Keeps the winding ID and transformerID
		
		eqObj.createNodeList("cim:PowerTransformerEnd");
		eqObj.extractNodefromEQFile("cim:PowerTransformerEnd",winding);
		//System.out.println("PowerTransformerEnd");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t r \t\t x \t\t Transformer rdf:ID \t\t\t Base Voltage rdf:ID");
		try{
			for(int i = 0;i<winding.size();i+=7){
				//System.out.printf("%s \t %s \t %s \t %s \t %s \t %s\n",winding.get(i), winding.get(i+1), winding.get(i+2), winding.get(i+3), winding.get(i+5), winding.get(i+6));
				double value1 = Double.parseDouble(winding.get(i+2));	//r
				double value2 = Double.parseDouble(winding.get(i+3));	//x
				sqlWind.add("'"+winding.get(i)+"','"+winding.get(i+1)+"',"+value1+","+value2+",'"+winding.get(i+5)+"','"+winding.get(i+6)+"'");
				WindingTerminal.add(winding.get(i));
				WindingTerminal.add(winding.get(i+4));	//Terminal ID
				WindingBaseVolt.add(winding.get(i));
				WindingBaseVolt.add(winding.get(i+6));	//Transformer ID
			}
		}catch(ArrayIndexOutOfBoundsException e9){
			e9.printStackTrace();
		}
		
		
		//Breaker Elements
		ArrayList<String> breakE = new ArrayList<String>();
		ArrayList<String> breakS = new ArrayList<String>();
		ArrayList<String> sqlBreaker = new ArrayList<String>();	//Used exclusively for the SQL Database
		ArrayList<String> breakerClosed = new ArrayList<String>();	//Used for the Ybus admittance matrix - Keeps breaker ID and state
		
		eqObj.createNodeList("cim:Breaker");
		eqObj.extractNodefromEQFile("cim:Breaker",breakE);
		sshObj.createNodeList("cim:Breaker");
		sshObj.extractInfoFromSSHFile("cim:Breaker",breakS);
		

		breakE = joinArrayLists(breakE,breakS);
		//System.out.println("Breaker");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t Equipment Container rdf:ID \t\t State");
		try{
			for(int i = 0;i<breakE.size();i+=4){
				//System.out.printf("%s \t %s \t %s \t %s\n",breakE.get(i), breakE.get(i+1), breakE.get(i+2), breakE.get(i+3));
				boolean value = Boolean.parseBoolean(breakE.get(i+3));
				sqlBreaker.add("'"+breakE.get(i)+"','"+breakE.get(i+1)+"',"+value+",'"+breakE.get(i+2)+"'");
				breakerClosed.add(breakE.get(i));
				breakerClosed.add(breakE.get(i+3));		//Breaker state
			}
		}catch(ArrayIndexOutOfBoundsException e10){
			e10.printStackTrace();
		}
		
		
		//Ratio Tap Changer Elements
		ArrayList<String> tapE = new ArrayList<String>();
		ArrayList<String> tapS = new ArrayList<String>();
		ArrayList<String> sqlTapC = new ArrayList<String>();	//Used exclusively for the SQL Database
		
		eqObj.createNodeList("cim:RatioTapChanger");
		eqObj.extractNodefromEQFile("cim:RatioTapChanger",tapE);
		sshObj.createNodeList("cim:RatioTapChanger");
		sshObj.extractInfoFromSSHFile("cim:RatioTapChanger",tapS);
		

		tapE = joinArrayLists(tapE,tapS);
		//System.out.println("RatioTapChanger");
		//System.out.println("rdf:ID \t \t \t \t \t Name \t\t Step");
		try{
			for(int i = 0;i<tapE.size();i+=4){
				//System.out.printf("%s \t %s \t %s\n",tapE.get(i), tapE.get(i+1), tapE.get(i+3));
				int value = Integer.parseInt(tapE.get(i+3));	//step
				sqlTapC.add("'"+tapE.get(i)+"','"+tapE.get(i+1)+"',"+value+",'"+tapE.get(i+2)+"'");
			}
		}catch(ArrayIndexOutOfBoundsException e11){
			e11.printStackTrace();
		}


	///////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// SQL DATABASE CREATION //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////
		
		//In the second part of this project, a relational database is created. To do so, the rules for the relational database creation were taken
		//into account from Fundamentals of Relational Database Management Systems coursebook. Based on the relationships between different entities
		//the foreign keys of the various tables were taken into consideration. Each table has a primary key that uniquely identifies a row of the table
		//and may be used as a foreign key in another table based on the aforementioned relationships. Below a database GRID is created as well as the requested
		//elements are placed in tables. The select method is used in order to present the results of each table respectively.
		
		
		
		//Creates the frame for the login credentials of the database. Basically, an object of the class LoginToDatabase
		//that handles the GUI for the login procedure
		final JFrame LoginFrame = new JFrame("Login To Database");
        final JButton LoginButton = new JButton("Click to Login");
        
        
        LoginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LoginFrame.setSize(300, 100);
        LoginFrame.getContentPane().add(LoginButton);
        LoginFrame.setVisible(true);
 
        
        //Implements an actionlistener for the LoginButton. Basically, we say to JAVA that the database creation must take place
        //only if the correct credentials were placed. If the login is successful then it creates the database
        LoginButton.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        LoginToDatabase login = new LoginToDatabase(LoginFrame);
                        login.setVisible(true);
                        // if login is successfull
                        if(login.isLogged()){
                        	
                        	LoginFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        	CreateDatabase database = new CreateDatabase("GRID");
                        	
                        	
                        	
                        	try{
                    			//Create table Base Voltage - If table already exists, table is not created
                    			String[] BaseColumn = {"ID","Nominal_Value"};
                    			database.createDataTable("BaseVoltage","(ID VARCHAR(100) NOT NULL, NOMINAL_VALUE FLOAT, PRIMARY KEY(ID))");
                    			database.insertInTable("BaseVoltage",sqlBaseV);

                    			
                    			
                    			//Create table Substation - If table already exists, table is not created
                    			String[] subsColumn = {"Substation_ID","Name","Region_ID"};
                    			database.createDataTable("Substation","(SUBSTATION_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), REGION_ID VARCHAR(100), PRIMARY KEY(SUBSTATION_ID))");
                    			database.insertInTable("Substation",sqlSub);

                    			
                    			
                    			//Create table Voltage Level - If table already exists, table is not created
                    			String[] nomVolt = {"Voltage_ID","Name","Substation_ID","BaseVoltage_ID"};
                    			database.createDataTable("VoltageLevel","(VOLTAGE_ID VARCHAR(100) NOT NULL, NAME FLOAT, SUBSTATION_ID VARCHAR(100), BASEVOLTAGE_ID VARCHAR(100), PRIMARY KEY(VOLTAGE_ID),"
                    					+ "FOREIGN KEY (SUBSTATION_ID) REFERENCES Substation(SUBSTATION_ID) ON DELETE CASCADE ON UPDATE CASCADE,"
                    					+ "FOREIGN KEY (BASEVOLTAGE_ID) REFERENCES BaseVoltage(ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("VoltageLevel",sqlVlevel);

                    			
                    			
                    			//Create table Generating Unit - If table already exists, table is not created
                    			String[] genUnitColumn = {"GenUnit_ID","Name","Max_P","Min_P","Substation_ID"};
                    			database.createDataTable("GeneratingUnit","(GENUNIT_ID VARCHAR(100) NOT NULL, NAME VARCHAR(30), MAX_P FLOAT, MIN_P FLOAT, SUBSTATION_ID VARCHAR(100), PRIMARY KEY (GENUNIT_ID),"
                    									+ "FOREIGN KEY (SUBSTATION_ID) REFERENCES Substation(SUBSTATION_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("GeneratingUnit",sqlGu);

                    			
                    			
                    			//Create table Regulating Control - If table already exists, table is not created
                    			String[] RegColumns = {"RegControl_ID","Name","Value"};
                    			database.createDataTable("RegulatingControl","(REGCONTROL_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), VALUE FLOAT, PRIMARY KEY (REGCONTROL_ID))");
                    			database.insertInTable("RegulatingControl",sqlReg);

                    			
                    			
                    			//Create table Synchronous Machine - If table already exists, table is not created
                    			String[] machColumn = {"Machine_ID","Name","S","P","Q","GeneratingUnit_ID","RegulatingControl_ID","VoltageLevel_ID"};
                    			database.createDataTable("SyncronousMachine","(MACHINE_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), S FLOAT, P FLOAT, Q FLOAT, GENERATINGUNIT_ID VARCHAR(100), REGULATINGCONTROL_ID VARCHAR(100), VOLTAGELEVEL_ID VARCHAR(100), PRIMARY KEY(MACHINE_ID),"
                    									+ "FOREIGN KEY (GENERATINGUNIT_ID) REFERENCES GeneratingUnit(GENUNIT_ID) ON DELETE CASCADE ON UPDATE CASCADE,"
                    									+ "FOREIGN KEY (REGULATINGCONTROL_ID) REFERENCES RegulatingControl(REGCONTROL_ID) ON DELETE CASCADE ON UPDATE CASCADE,"
                    									+ "FOREIGN KEY (VOLTAGELEVEL_ID) REFERENCES VoltageLevel(VOLTAGE_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("SyncronousMachine",sqlSync);

                    			
                    			
                    			//Create table Power Transformer - If table already exists, table is not created
                    			String[] transColumn = {"Transformer_ID","Name","Substation_ID"};
                    			database.createDataTable("PowerTransformer","(TRANSFORMER_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), SUBSTATION_ID VARCHAR(100), PRIMARY KEY (TRANSFORMER_ID),"
                    									+ "FOREIGN KEY (SUBSTATION_ID) REFERENCES Substation(SUBSTATION_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("PowerTransformer",sqlPowerT);

                    			
                    			
                    			//Create table Energy Consumer - If table already exists, table is not created
                    			String[] consumerColumn = {"Consumer_ID","Name","P","Q","VoltageLevel_ID"};
                    			database.createDataTable("EnergyConsumer","(CONSUMER_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), P FLOAT, Q FLOAT, VOLTAGELEVEL_ID VARCHAR(100), PRIMARY KEY(CONSUMER_ID),"
                    									+ "FOREIGN KEY (VOLTAGELEVEL_ID) REFERENCES VoltageLevel(VOLTAGE_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("EnergyConsumer",sqlCons);

                    			
                    			
                    			//Create table Breaker - If table already exists, table is not created
                    			String[] breakColumn = {"Breaker_ID","Name","Breaker_State","VoltageLevel_ID"};
                    			database.createDataTable("Breaker","(BREAKER_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), BREAKER_STATE BOOL, VOLTAGELEVEL_ID VARCHAR(100), PRIMARY KEY(BREAKER_ID),"
                    									+ "FOREIGN KEY (VOLTAGELEVEL_ID) REFERENCES VoltageLevel(VOLTAGE_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("Breaker",sqlBreaker);

                    			
                    			
                    			//Create table Transformer Winding - If table already exists, table is not created
                    			String[] windColumn = {"Winding_ID","Name","r","x","Transformer_ID","BaseVoltage_ID"};
                    			database.createDataTable("TransformerWinding","(WINDING_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), R FLOAT, X FLOAT, TRANSFORMER_ID VARCHAR(100), BASEVOLTAGE_ID VARCHAR(100), PRIMARY KEY(WINDING_ID, TRANSFORMER_ID),"
                    									+ "FOREIGN KEY (TRANSFORMER_ID) REFERENCES PowerTransformer(TRANSFORMER_ID) ON DELETE CASCADE ON UPDATE CASCADE,"
                    									+ "FOREIGN KEY (BASEVOLTAGE_ID) REFERENCES BaseVoltage (ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("TransformerWinding",sqlWind);

                    			
                    			
                    			//Create table Tap Changer - If table already exists, table is not created
                    			String[] tapColumn = {"TapChanger_ID","Name","Change_Step","Winding_ID"};
                    			database.createDataTable("TapChanger","(TAPCHANGER_ID VARCHAR(100) NOT NULL, NAME VARCHAR(20), CHANGE_STEP INTEGER, WINDING_ID VARCHAR(100), PRIMARY KEY(TAPCHANGER_ID),"
                    									+ "FOREIGN KEY (WINDING_ID) REFERENCES TransformerWinding (WINDING_ID) ON DELETE CASCADE ON UPDATE CASCADE)");
                    			database.insertInTable("TapChanger",sqlTapC);

                    			
                    			
                    			//Before proceeding with the next section, the database must close, therefore the method is called.
                    			database.CloseDatabase();	
                    			
                    			
                    		}catch (Exception sqle){
                    			sqle.printStackTrace();
                    		}
                        }
                        else if(login.isCancelled())
                        	System.exit(0);
                    }
                });

		
		
		
		
		
		
		//////////////////////////////////////////////////////////////////////////////
		///////////////////////// Y-BUS MATRIX CALCULATION  //////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		
		//The Ybus matrix calculation takes into consideration all the procedure that takes place in the CalculateYbus
		//class. Basically, in this part of the code an object of the class is created that handles all the functions created
		//in the class. For this part, the arrayLists of the connectivity node and terminal are defined as well as the Line
		//Segment list that will be utilized for the matrix calculation.
		//The Ybus matrix procedure takes into consideration the state of the breakers, the connectivity of the terminals
		//and the elements of the lines and windings of transformer. Furthermore, the shunt elements are taken into consideration
		//The line is modelled with a convention PI-model that is comprised of the line impedance and two shunt admittances, one
		//in each terminal of the line that are equal to the half of the line's total admittance
		//The transformer is modelled with the PI-model that is comprised of two line impedances, one for each winding, and two
		//shunt admittances, one for each winding.
		//The line admittances are added to the diagonal and non-diagonal elements of the Ybus matrix. The shunt admittances
		//are added only to the diagonal elements of the Ybus matrix
		
		
        
		//Create an arrayList for the connectivity Node that contains the ID 
		ArrayList<String> connNode = new ArrayList<String>();
		eqObj.createNodeList("cim:ConnectivityNode");
		eqObj.extractNodefromEQFile("cim:ConnectivityNode",connNode);
		
		ArrayList<String> terminal = new ArrayList<String>();
		ArrayList<String> checkTerminal = new ArrayList<String>();
		
		//Create an ArrayList for the terminals that contains the ID and the Connectivity Node - Conducting Equipment
		eqObj.createNodeList("cim:Terminal");
		eqObj.extractNodefromEQFile("cim:Terminal",terminal);
		
		//Creates a list from the SSH file that contains the ID of the terminal and connection
		sshObj.createNodeList("cim:Terminal");
		sshObj.extractInfoFromSSHFile("cim:Terminal",checkTerminal);
		
		ArrayList<String> LineVoltage = new ArrayList<String>();	//Contains the ID and the base voltage of the line
		ArrayList<String> LineID = new ArrayList<String>();			//Contains the IDs of the Line
		
		eqObj.createNodeList("cim:ACLineSegment");
		eqObj.extractNodefromEQFile("cim:ACLineSegment",LineVoltage);
		for(int i = 0;i<LineVoltage.size();i+=2){
			LineID.add(LineVoltage.get(i));
		}
		
		
		/////////////////////////////--------------Preparation--------------/////////////////////////////
		//Creates the object that will handle the procedure for the determination of the Ybus
		CalculateYBus myobj = new CalculateYBus();
		
		//Checks and removes breakers that are open - Returns the list of the closed breakers
		myobj.checkBreakerState(breakerClosed);	
		
		//Checks and removes terminals that are not connected - Returns a list with all the connected terminals
		myobj.checkConnectedTerminal(terminal,checkTerminal);
		
		//Replaces in each terminal the equipment and stores the ID of the winding
		//This will help for the determination of the Ybus matrix
		myobj.replaceWindingInTerminal(WindingTerminal,terminal,TransformerID);		
		
		
		
		/////////////////////////////--------------Grouping Step 1--------------/////////////////////////////
		//Group Terminals based on Connectivity Nodes - Create System Buses
		
		ArrayList<ArrayList<String>> GridBuses = new ArrayList<ArrayList<String>>();
		GridBuses = myobj.GroupTerminalByConnNode(terminal,connNode);	
		
		//Result is 14 groups of interconnected terminals - Step 1
		
		
		
		/////////////////////////////--------------Grouping Step 2--------------/////////////////////////////
		//Group Terminals based on Breaker Condition - Create Final Buses of the System
		
		//Based on breakers that are CLOSED, it stores the breaker terminals in a new ArrayList
		ArrayList<String> breakerTerminal = new ArrayList<String>();
		breakerTerminal = myobj.findBreakerTerminals(breakerClosed,terminal);		
		
		//Based on closed breakers, it regroups the interconnected terminals
		//Result is 5 buses that the system is comprised
		GridBuses = myobj.GroupTerminalsByBreaker(GridBuses,breakerTerminal);	
		
		
		
		//Initialization of the Admittance matrix based on defined number of buses
		//Places zeros in all lines and columns of matrix
		ComplexNumbers[][] AdmitMatrix = new ComplexNumbers[GridBuses.size()][GridBuses.size()];
		for(int i = 0;i<GridBuses.size();i++){
			for(int j = 0;j<GridBuses.size();j++){
				AdmitMatrix[i][j] = new ComplexNumbers();
			}
		}
		
		System.out.println(GridBuses.size());
		
		
		/////////////////////////////--------------Step 1--------------/////////////////////////////
		//Include the lines in the Ybus Matrix - Initially all elements are equal to zero
		for(int i = 0;i<LineID.size();i++){
			
			int index1 = 0;
			int index2 = 0;
			boolean findFirstTerminal = false;
			ArrayList<String> LineTerminals = new ArrayList<String>();
			String checkedID = LineID.get(i);
			
			//For the given Line ID that is selected, get the terminals that is comprised
			LineTerminals = myobj.getTerminalfromID(checkedID,terminal);
			
			if(LineTerminals == null){
				System.out.println("Line with ID "+checkedID+" is not connected to the grid");
			}
			else{
				//Check if terminals are connected to the buses acquired from the terminals
				//Store the bus number that each terminal is connected based on the Index of GridBuses
				for(int j = 0;j<LineTerminals.size();j++){
					
					if(!findFirstTerminal){
						for(int k = 0;k<GridBuses.size();k++){
							if(myobj.ObjectConnectedtoBus(LineTerminals.get(j), GridBuses.get(k))){
								
								//Store the index of the GridBus ArrayList that contains the terminal of the line(i.e the bus where the line is connected)
								index1 = GridBuses.indexOf(GridBuses.get(k));
								findFirstTerminal = true;
								break;
							}
						}
					}
					else if(findFirstTerminal){
						for(int k = 0;k<GridBuses.size();k++){
							if(myobj.ObjectConnectedtoBus(LineTerminals.get(j), GridBuses.get(k)) && GridBuses.indexOf(GridBuses.get(k))!=index1){
								
								//Store the index of the GridBus ArrayList that contains the terminal of the line(i.e the bus where the line is connected)
								//Works only if the first terminal is verified that is connected to a bus
								index2 = GridBuses.indexOf(GridBuses.get(k));
								break;
							}
						}
					}
				}
				
				//For the two acquired indexes, calculate the admittances for the Ybus matrix
				//Find the Base voltage for each line
				double baseVoltLine = myobj.FindBaseVoltage(checkedID, baseVolt, LineVoltage);
				ComplexNumbers[] calcAdmittance = myobj.calcLineAdmittance(eqObj,checkedID,basePower,baseVoltLine);
				
				//Initially, the line admittance is added in both diagonal and non-diagonal elements of the Ybus
				AdmitMatrix[index1][index2] = AdmitMatrix[index1][index2].substract(calcAdmittance[0]);
				AdmitMatrix[index2][index1] = AdmitMatrix[index2][index1].substract(calcAdmittance[0]);
				AdmitMatrix[index1][index1] = AdmitMatrix[index1][index1].add(calcAdmittance[0]);
				AdmitMatrix[index2][index2] = AdmitMatrix[index2][index2].add(calcAdmittance[0]);
				
				//Add Shunt Admittance to the diagonal elements of Ybus Matrix
				AdmitMatrix[index1][index1] = AdmitMatrix[index1][index1].add(calcAdmittance[1]);
				AdmitMatrix[index2][index2] = AdmitMatrix[index2][index2].add(calcAdmittance[1]);
			}
		}
		

		
		/////////////////////////////--------------Step 2--------------/////////////////////////////
		//Include Transformer Admittance to the Ybus Matrix
		for(int i = 0;i<TransformerID.size();i++){
			
			int index3 = 0;
			int index4 = 0;
			boolean findFirstTerminal = false;
			ArrayList<String> TransfWinding = new ArrayList<String>();
			String checkedID = TransformerID.get(i);
			
			//For the given Transformer ID get the windings that is comprised
			TransfWinding = eqObj.getWindingfromTransf(checkedID);
			
			for(int j = 0;j<TransfWinding.size();j++){
				String winding1 = TransfWinding.get(j);	//Stores the ID of the first winding to use it for the calculations
				
				//For the given winding of transformer, find the terminal - Must be non-zero String
				String windTerminal1 = myobj.getWindingTerminal(winding1,terminal);
				if(!windTerminal1.equals("")){
					for(int k = 0;k<GridBuses.size();k++){
						
						//Check if selected terminal is connected to a bus
						if(myobj.ObjectConnectedtoBus(windTerminal1, GridBuses.get(k))){
							
							//Stores the index of the GridBus matrix that contains the first winding of the transformer(i.e where the transformer is connected)
							index3 = GridBuses.indexOf(GridBuses.get(k));
							findFirstTerminal = true;
							break;
						}
					}
				}
				for(int m = j+1;m<TransfWinding.size();m++){
					
					//Checks the second winding of the transformer - Takes the terminal ID
					String winding2 = TransfWinding.get(m);
					String windTerminal2 = myobj.getWindingTerminal(winding2,terminal);
					if(!windTerminal1.equals("") && findFirstTerminal){
						for(int n = 0;n<GridBuses.size();n++){
							
							//Check if selected terminal is connected to a bus
							if(myobj.ObjectConnectedtoBus(windTerminal2, GridBuses.get(n)) && GridBuses.indexOf(GridBuses.get(n))!=index3){
								
								//Stores the index of the GridBus matrix that contains the second winding of the transformer(i.e where the transformer is connected)
								index4 = GridBuses.indexOf(GridBuses.get(n));
								
								//Calculate the base voltage of both windings
								double baseVolt1 = myobj.FindBaseVoltage(winding1, baseVolt, WindingBaseVolt);
								double baseVolt2 = myobj.FindBaseVoltage(winding2, baseVolt, WindingBaseVolt);

								ComplexNumbers[] calcAdmittance = myobj.calcTransAdmittance(eqObj,winding1,winding2,baseVolt1,baseVolt2,basePower);
								
								//Initially, the line admittance is added in both diagonal and non-diagonal elements of the Ybus
								AdmitMatrix[index3][index4] = AdmitMatrix[index3][index4].substract(calcAdmittance[0]);
								AdmitMatrix[index4][index3] = AdmitMatrix[index4][index3].substract(calcAdmittance[0]);
								AdmitMatrix[index3][index3] = AdmitMatrix[index3][index3].add(calcAdmittance[0]);
								AdmitMatrix[index4][index4] = AdmitMatrix[index4][index4].add(calcAdmittance[0]);
								
								//Add the Shunt Admittance to the diagonal of Ybus Matrix
								AdmitMatrix[index3][index3] = AdmitMatrix[index3][index3].add(calcAdmittance[1]);
								AdmitMatrix[index4][index4] = AdmitMatrix[index4][index4].add(calcAdmittance[2]);
							}
						}
					}
				}
			}
		}
		
		//Stores in a matrix the column enumeration that corresponds with the bus number in the topology. Used in table GUI
		String[] Columnname = new String[GridBuses.size()];
		for(int i = 0;i<Columnname.length;i++){
			Columnname[i] = Integer.toString(i+1);
		}
		
		//Stores in a matrix the values of the Ybus matrix. In order to be presented, they must be turned to strings first
		//Given as strings with only 4 decimals
		String[][] temp = new String[GridBuses.size()][GridBuses.size()];
		for(int i = 0;i<GridBuses.size();i++){
			for(int j = 0;j<GridBuses.size();j++){
				temp[i][j] = String.format("%.4f %.4f" + "i",AdmitMatrix[i][j].getReal(), AdmitMatrix[i][j].getImag());
			}
		}	

		
//-------------------------------------------------------------------------------------------------------------------//
//---------------------------------------------------GUI TABLE-------------------------------------------------------//
//-------------------------------------------------------------------------------------------------------------------//
		
		//Creates the frame object(instance of class GuiTable) that creates the frame where the Ybus matrix will be presented
		//Sets also some parameters such as frame size, if it must be visible or not etc
		JFrame frame1  = new JFrame();
		frame1.setSize (625,280);
		
		GuiTable gui = new GuiTable(temp,Columnname);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(600,200);
		gui.setVisible(true);
		gui.setTitle("Ybus Admittance Matrix");
		
//-------------------------------------------------------------------------------------------------------------------//
			
		
		}catch(ArrayIndexOutOfBoundsException are){
			System.out.println("Error! No created files, program terminates----------------------------->");
			System.exit(0);
		}
	}
		
		
	//This method takes as input the two matrices from EQ and SSH file that are related to the same element and combines it into one and returns it back
	//for further process. The procedure is based on the index object that was added to some arrays.
	public static ArrayList<String> joinArrayLists(ArrayList<String> EqList, ArrayList<String> sshList){

			
			ArrayList<Integer> counteq = new ArrayList<Integer>();
			ArrayList<Integer> countssh = new ArrayList<Integer>();
			
			List<String> split1 = new ArrayList<String>();
			List<String> split2 = new ArrayList<String>();
			
			ArrayList<String> combine = new ArrayList<String>();
			
			//Checks how many object exist in arrayList and addes their index to a new list
			for(int i = 0;i<EqList.size();i++){
				if(EqList.get(i) == "Object"){
					counteq.add(i);
				}
			}
			
			for(int j = 0;j<sshList.size();j++){
				if(sshList.get(j) == "Object"){
					countssh.add(j);
				}
			}
			
			//The size of the matrices must be equal. If it is 1, removes the ID and object index from the second matrix and addes it to the first arrayList
			if(counteq.size() == countssh.size()){
				if(counteq.size() == 1){
					if(EqList.get(1).equals(sshList.get(1))){
						EqList.remove(0);
						sshList.remove(0);
						sshList.remove(0);
						combine.addAll(EqList);
						combine.addAll(sshList);
					}
				}
				else{
					//Based on the IDs of both lists, it splits both lists on the specified index and addes the two parts to the new combined matrix
					//The indexes are taken from the specified matrices counteq and countssh
					counteq.add(EqList.size());
					countssh.add(sshList.size());
					for(int i = 0;i<counteq.size()-1;i++){
						for(int j = 0;j<countssh.size()-1;j++){
							if(EqList.get(counteq.get(i)+1).equals(sshList.get(countssh.get(j)+1))){

									split1 = EqList.subList(counteq.get(i),counteq.get(i+1));
									split2 = sshList.subList(countssh.get(j)+2,countssh.get(j+1));
									
									combine.addAll(split1);
									combine.addAll(split2);
							}
						}
					}
				}
			}
			else
				System.out.println("Error! Arraylists size not the same. Please check the data");
			
			//If any remaining object indexes are left, remove them
			for(int i = 0;i<combine.size();i++){
				if(combine.get(i) == "Object"){
					combine.remove(i);
					i=0;
				}
			}
			return combine;
		}
}
