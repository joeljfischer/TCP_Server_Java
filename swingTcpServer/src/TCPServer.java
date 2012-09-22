import java.io.IOException;
import java.net.*;
/**
 * @author Joel Fischer, Advanced Systems & Controls
 * 
 * This class asks the user for a port number on which to start a server.
 * It then listens for connections and spins off individual threads for each incoming
 * connection. It listens constantly for incoming connections.
 */
public class TCPServer {
	
	private static ServerSocket servSocket = null; //Server Socket
	private static createGUI mainWindow; //GUI instance for the TCPServer

	/**
	 * @param args command line arguments, not used
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {		
		int portNum = 0; //Port Number to use
		int threadNumber = 1; //Server Thread Created Number
		mainWindow = new createGUI(0); //Create a new GUI, Main Server Program Window
		
		mainWindow.textField.setEditable(true); //Make the textField editable
		
		/*
		 * Control which ports can be selected, we only want ones a PLC can recognize
		 * Wait for the user to input a valid Port Number
		 */
		while(portNum == 0){
			mainWindow.grabFocus(); //Put the focus on this window
			mainWindow.updateTextArea("Enter an integer port number 2000 to 65535: ");
			while(mainWindow.inputUpdated == false){ //Wait for the entry
				portNum = 0; //Keep the portNum 0 as long as there is no entry
			}
			try{ //Recover from an error if a non-integer string is entered
				portNum = Integer.parseInt(mainWindow.getInputString()); //Once there's an entry, take the input string and convert it to an int
			} catch(NumberFormatException nfe) { //Recover from this error
				System.err.println("Attempted to Parse Integer from: '" + mainWindow.getInputString() + "' Failed");
				System.err.println(nfe);
				mainWindow.updateTextArea("Exception Thrown, Integer not Entered");
				mainWindow.inputUpdated = false; //Reset the input updated bit to stop the error from repeating
				mainWindow.textField.setText(""); //Erase whatever the user previously entered
			}
			if(portNum < 2000 || portNum > 65535){ //Error handle an out of range entry
				System.err.println("Input integer " + mainWindow.getInputString() + " out of range");
				mainWindow.updateTextArea("Integer out of range, Enter an Integer between 2000 and 65535");
				portNum = 0;
				mainWindow.inputUpdated = false;
				mainWindow.textField.setText("");
			}
		}
		
		mainWindow.textField.setText(""); //Reset the text field
		mainWindow.textField.setEditable(false); //Make the text field unable to take input
		
		/*
		 * Try to create a server socket on the input port of the local host
		 */
		try{
			servSocket = new ServerSocket(portNum); //Creates a server on the socket
		}
		catch(IOException e){
			mainWindow.updateTextArea("Error, See Command Line for Details"); //Errors can only go on the command-line without a large workaround
			System.err.println("Could not create server on port:" + portNum);
			System.err.println(e); //Display the error
		}
		
		/*
		 * Display the created host port number as confirmation
		 * if there was no exception.
		 */
		mainWindow.clearTextArea(); //Clear the display
		mainWindow.updateTextArea("Server Started on LocalHost Port Number: " 
				+ servSocket.getLocalPort()); //Display the Server's port number
		mainWindow.updateTextArea("Listening for Client Connection...");
		
		/*
		 * Wait for a client to connect, create a new instance of the tcpThread class
		 * which is derived from the java Thread class to allow multiple connections
		 * to a single port, each connection goes to its own thread.
		 * 
		 * Note that the accept() call blocks this method from continuing until a new
		 * connection accepts.
		 * 
		 * This is the main loop, so it always continues to loop unless the program is closed
		 */
		while(true){
			try{
				Socket connectionSocket = servSocket.accept(); //Accept a connection, create a socket for that connection
				new TCPThread(connectionSocket, threadNumber).start(); //Start a Thread for the new socket/connection
			} catch(IOException e){ //If there is an error creating the connection, it is caught here
				mainWindow.updateTextArea("Error, See Command Line for Details");
				System.err.println("Error Accepting Connection");
				System.err.println(e); //Display the error on the command line
				servSocket.close(); //Close the Server Socket
			}
			threadNumber++; //Advance the thread number
		}
	}
}
