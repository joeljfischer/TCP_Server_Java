import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * @author Joel Fischer, Advanced Systems & Controls
 * 
 * This class represents a single thread running concurrently with any others created.
 * It controls and handles a connection. It will listen for incoming data, and while
 * waiting, enables sending user input strings to the client. When data is received,
 * it displays the input data and sends back an acknowledgment.
 */

public class TCPThread extends Thread {
	private Socket connectionSocket = null; //The thread socket
	private int threadNum; //The thread number
	private createGUI window; //The GUI for the thread
	private BufferedReader inRx = null; //The input stream
	private PrintWriter outTx = null; //The output stream
	private boolean exitThread = false; //When true, the thread exits
	/**
	 * @param socket
	 * @param threadNum
	 * Thread Constructor
	 */
	public TCPThread(Socket socket, int threadNum){
		super("TCP Server Thread " + threadNum); //Run the thread constructor and create this thread
		this.connectionSocket = socket; //Set the thread's socket to the input socket
		this.threadNum = threadNum; //Set the thread's number to the input number
		this.window = new createGUI(threadNum); //Create a GUI window and pass down the thread number
	}
	
	/**
	 * Override the Thread java.lang.net method run(). This actually is the main
	 * implementation of the thread, when it terminates, the thread destroys itself.
	 */
	@Override
	public void run(){
		String rxString = ""; //Stores a string that comes from a client
		String userInput = ""; //Stores a string that comes from a user
		char txChar = (char)6; //Create a char variable with ASCII 'ACK' character
		
		/*
		 * Create new streams to/from the socket to its client, and handle exceptions
		 * creating those streams
		 */
		try{
			inRx = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); //Input stream from the client
			outTx = new PrintWriter(connectionSocket.getOutputStream(), true); //Output stream to the client
		} catch(IOException e) {
			window.updateTextArea("Error, See Command Line for Details");
			System.err.println("Error establishing Send/Receive Streams: ");
			System.err.println(e); //Print the error to the command line
			this.quitThread();
		}
		
		window.textField.setEditable(true); //Set the entry field to editable
		window.grabFocus(); //Give the newly created window focus
		
		/*
		 * As long as there is a connection, get the remote client's IP and Port Number.
		 * If the IP is the Local Address (isLoopbackAddress), then display that and
		 * the port. If it is a foreign address, display that and the port.
		 */
		if(connectionSocket.getRemoteSocketAddress() != null){ //Test that the remote IP/Port is valid
			if(connectionSocket.getInetAddress().isLoopbackAddress()){ //Test if the remote IP is the Local IP
				window.updateTextArea("Thread " + threadNum + " Connection Port: " + "LocalHost:" 
						+ connectionSocket.getPort()); //Display an easier to recognize loopback IP and the remote port
			}
			else{ //If the remote IP is not on the local machine
				window.updateTextArea("Thread " + threadNum + " Connection Port: " 
						+ connectionSocket.getInetAddress() + ":" + connectionSocket.getPort()); //Display the remote IP/Port
			}
			window.updateTextArea("\nWaiting for RX Data from Client..."); //Display some information for the user
			window.updateTextArea("Enter '/quit' to disable this connection");
			window.updateTextArea("Enter TX Data to Send to Client:\n");
		}
		
		while(!exitThread){ //Main loop of the thread, loops as long as the user does not ask to end the thread
			try{
				while(!inRx.ready()){ //While waiting for incoming data, enable sending data
					if (window.inputUpdated){ //If the user has input new data
						userInput = window.getInputString(); //Retrieve that string
						outTx.println(userInput); //Send that string to the client socket
						window.updateTextArea(currentDateTime() + " TX: " + userInput); //Display the user input on the display along with a timestamp and marker for output data
						window.textField.setText(""); //Reset the user entry area
						window.inputUpdated = false; //Reset the updated bit
						
						if(userInput.contains("/quit")) { //If the user enters the quit string
							this.quitThread(); //Call the quitThread function for this thread only
						}
					}
				}
				/*
				 * If this thread has incoming data, bring that window to the front.
				 * Then display a timestamp and the incoming data.
				 * Finally, output back the ASCII ACK character.
				 */
				while(inRx.ready() && !exitThread){ //Execute only if we are not exiting and there is incoming data
					rxString = inRx.readLine(); //Read the incoming stream
					window.grabFocus(); //Give the window focus
					window.updateTextArea(currentDateTime() + " RX: " + rxString); //Display the data with timestamp and marker for incoming data
					outTx.println(txChar); //Output back to client an acknowledgment bit
					break; //end the while loop
				}
			} catch(IOException e) { //Exception Handling
				window.updateTextArea("Error, See Command Line for Details"); //Direct the user to the CMD line
				if(!exitThread){ //This will throw an exceptions even if the streams are shutdown on exit
					System.err.println("Error Send/Receive streams: "); //General overview of the error
					System.err.println(e); //Output the error to the command line
				}
			}
		}
	}
	
	/**
	 * @return String currentDateTime
	 * This function gets a current Timestamp and returns it in string form
	 */
	public static String currentDateTime(){
		Calendar now = Calendar.getInstance(); //Get a timestamp in Calendar form
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //Set the timestamp into string form
		return sdf.format(now.getTime()); //Return the timestamp string
	}
	
	/**
	 * This function closes all outstanding streams and connections to a client,
	 * closes the window, and starts the process of ending the thread. The thread
	 * ends once the run() function runs through to the end of its current cycle.
	 */
	protected void quitThread(){
		try { //Handle errors closing streams and ending connections
			inRx.close(); //Close the input stream to the remote socket
			outTx.close(); //Close the output stream to the remote socket
			connectionSocket.close(); //Close the connection to the remote socket
			window.frame.dispose(); //Close the window and allow the JVM Garbage Collector to retrieve assets
			exitThread = true; //Set the bit to make this run() cycle of the thread its final cycle
		} catch (IOException e) {
			window.updateTextArea("\n" + "Error: See Command Line");
			System.err.println("Error Attempting to End Thread");
			System.err.println(e); //Output the error to the command line
		}
	}
}