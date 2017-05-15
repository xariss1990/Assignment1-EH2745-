package assignment;

//This class implements a part of the GUI that is created in the main method. Basically, this class creates a frame that
//contains a button to login in the database. Once pressed, it shows two panels in order to place the username and password
//that will be utilized to login and create the relational database.
//The class is able to handle the correct insertion of credentials and the wrong one by resetting the username and password
//and allowing the user to re-enter them. If the cancel button is pressed, then it exits the program by showing a waring message

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginToDatabase extends JDialog {

    private JTextField tfuser;
    private JPasswordField pfpass;
    private JLabel lbuser;
    private JLabel lbpass;
    private JButton login;
    private JButton cancel;
    private boolean success;
    private boolean failure;
    private JTextField field1 = new JTextField("root");
    private JPasswordField pass1 = new JPasswordField("abi10539");
    
    
    //Once created, the class sets the username and password panels
    public LoginToDatabase(Frame parent) {
        super(parent, "Login to Database", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
 
        cs.fill = GridBagConstraints.HORIZONTAL;
        
        //Created the Username panel
        lbuser = new JLabel("Username: ");
        panel.add(lbuser, cs);
        tfuser = new JTextField(20);
        panel.add(tfuser, cs);
        
        //Creates and places in a specific place the Password panel
        lbpass= new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbpass, cs);
        
        pfpass = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfpass, cs);
        
        //Create Login button in GUI
        //Implements the actionlistener that basically tells java what to do in case the login button is pressed. If the credentials
        //match the ones that were set, it proceeds. Otherwise, it allows the user to re-enter the credentials
        login = new JButton("Login");
        login.addActionListener(new ActionListener() {
        	 
            public void actionPerformed(ActionEvent e) {
                if (authenticate(tfuser.getText().trim(), new String(pfpass.getPassword()))) {
                    success = true;
                    dispose();
                } else {
                	//Output of a warning message
                    JOptionPane.showMessageDialog(LoginToDatabase.this,
                    "Invalid username or password",
                    "Login",
                    JOptionPane.ERROR_MESSAGE);
                    		
                    
                    // Reset Username and password
                    tfuser.setText("");
                    pfpass.setText("");
                    success = false;
                    failure = true;
                }
            }
        });
        
        //Create Cancel button in GUI
        //Implements the actionlistener that basically tells java what to do in case the cancel button is pressed. When
        //cancel is pressed, the program outputs a warning message and terminates
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	JOptionPane.showMessageDialog(LoginToDatabase.this,
                        "No userame or password were introduced. Error while attempting to Log in Database. "
                        + "Program Terminated",
                        "Cancel",
                        JOptionPane.ERROR_MESSAGE);
                		dispose();	//Closes the frame	
                		System.exit(0);	//Ends the progra,
                
            }
        });
        
        //This part of the code places the buttons in the java panel 
        JPanel placeButton = new JPanel();
        placeButton.add(login);
        placeButton.add(cancel);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(placeButton, BorderLayout.PAGE_END);
 
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    
    //Method that returns a boolean value in case the credentials are correct and the login successful
    public boolean isLogged() {
        return success;
    }
    
    //Method that returns a boolean value in case the cancel button is pressed
    public boolean isCancelled(){
    	return failure;
    }
    
    //Method that checks if the introduced username and password are the same with the correct ones
    //Returns a boolean value for each operation
    public boolean authenticate(String username, String password) {
        // hardcoded username and password
        if (username.equals("root") && password.equals("abi10539")) {
            return true;
        }
        return false;
    }
        
}