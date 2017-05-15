package assignment;

import java.sql.*;
import java.util.*;

public class CreateDatabase {
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/";
	
	static final String USER = "root";
	static final String PASS = "abi10539";
	
	private String database;
	private Connection DatabaseConnect = null;
	private Statement stmt = null;
	private PreparedStatement pst = null;
	
	
	//Constructor that creates a database every time a new object of this class is created. Uses username and password to connect
	public CreateDatabase(String dataName){	
		
		database = dataName;
		
		try{
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			DatabaseConnect = DriverManager.getConnection(DB_URL, USER, PASS);
			
			//Creates a database and a statement that will be used henceforth. Database name is given as attribute
			stmt = DatabaseConnect.createStatement();
			String sql = "CREATE DATABASE IF NOT EXISTS " +database; // Create database if not exist so that every time the code is executer, there will be no error
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
			
			//Connects to the database already created
			DatabaseConnect = DriverManager.getConnection(DB_URL + database, USER, PASS);
			stmt = DatabaseConnect.createStatement();
				
		}catch(SQLException se){
			//Handle errors for JDBC
			System.out.println("Error while creating database. Please check your credentials, SQL Server");
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}
	}
	
	//This method handles the creation of table in database. Each time the name of the table is given followed by a string that represents the columns
	//of the table divided by commas in order to be applicable for SQL.
	public void createDataTable (String tablename, String columnName){
		
		try{
			//If the table already exists in database, it doesn't do anything.
		    String sql = "CREATE TABLE IF NOT EXISTS " +tablename+ "" +columnName;
			stmt.executeUpdate(sql);
			
			System.out.println("Table "+tablename+" created successfully in given database.");
			
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
			
		}catch (Exception de){
			de.printStackTrace();
		}
	}
	
	//Method that inserts data into the table. Basically, it takes the stored data from the arraylists that are previously modified in order to match
	//an SQL string and inserts the data to the table. Each line of the arrylist represents a string of all the data that needs to be inserted
	public void insertInTable(String tablename, ArrayList<String> sqlColumns){
		
		try{
			for(int i = 0;i<sqlColumns.size();i++){
				
				String sqlInsert = "INSERT INTO " + tablename + " VALUES ("+sqlColumns.get(i)+")"; 
	        	stmt.executeUpdate(sqlInsert);
			}
			
		}
		catch(SQLIntegrityConstraintViolationException integ){
			
			System.out.println("Inserted value already exists in database");
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println("Error occured in the system");
			sqle.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Method that is used to update one or more values in a data table. Only constrain in this case is that the updateValue and where string must contain
	//the "=" symbol as well as the updated values must exist in the table. In the where string, usually a primary is utilized.
	public void updateInTable(String tablename, String updateValue, String where, String columnName){
		
		try{
			if(updateValue.indexOf("=")!= -1 && where.indexOf("=")!= -1){
				String sqlUpdate = "UPDATE " +tablename+" ("+columnName+") SET " +updateValue+ " WHERE " +where;
	        	pst = DatabaseConnect.prepareStatement(sqlUpdate);
	            pst.executeUpdate();
			}
			else
				System.out.println("Not valid statement for SQL");
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println("Error occured in the system");
			sqle.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Method that is used to delete from a specified table. It has two option: Either drop all the table or specific elements of the table on condition
	//that can be set as zero.
	public void deleteFromTable(String tablename, String delValue, String where) throws SQLException{
		
		String sqlDelete = null;
		try{
			switch(delValue){
			case "":
				sqlDelete = "DROP TABLE " +tablename; //Deletes all table rows
	        	pst = DatabaseConnect.prepareStatement(sqlDelete);
	            pst.executeUpdate();
	            break;
	        default:
	        	if(where.indexOf("=")!= -1){
		        	sqlDelete = "DELETE" +delValue+ "FROM " +tablename+ "WHERE" +where;	//Deletes selected rows based on id given in where
		        	pst = DatabaseConnect.prepareStatement(sqlDelete);
		            pst.executeUpdate();
	        	}
	        	else
	        		System.out.println("Invalid argument given in method");
	            break;
			}
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println(" SQL Error occured in the system");
			sqle.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Method that is used to select columns from a specified data table in SQL. Basically, the tablename and the columns are given and appear on the console.
	//The order by is used in this case in order for the elements to appear in order of the ID primary. Otherwise, SQL will print the results in hash order
	//irrespectively of the order of entry in the table. The results of select appear in the console.
	public void selectFromTable(String tablename,String[] columns){
		
		try{
			for(int i = 0;i<columns.length;i++){
				String sqlSelect = "SELECT " +columns[i]+ " FROM " +tablename+ " ORDER BY " +columns[0];
				//ResultSet is used in order to print the elements of the data table
				ResultSet rset = stmt.executeQuery(sqlSelect);
				int j = 1;
				while(rset.next()){
					String getValue = rset.getString("" +columns[i]);
					System.out.print("Object" +j);
					System.out.println(" " +columns[i]+ ": " +getValue);
					j++;
				}
				rset.close();
			}
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println(" SQL Error occured in the system");
			sqle.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public void CloseDatabase(){
		
		try{
			DatabaseConnect.close();
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println(" SQL Error occured in the system while closing connection");
			sqle.printStackTrace();
		}
	}
	
	public void DropDatabase(String databaseName){
		
		try{
			String sql = "DROP DATABASE " +databaseName;
			stmt.executeUpdate(sql);
		}
		catch(SQLException sqle){
			//JDBC errors handler
			System.out.println(" SQL Error occured in the system while deleting database");
			sqle.printStackTrace();
		}
	}
}
