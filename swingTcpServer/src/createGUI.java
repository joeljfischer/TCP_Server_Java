import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


/**
 * @author Joel Fischer, Advanced Systems & Controls
 *
 * This class creates a GUI with slight modifications based on if its the main
 * window or a thread window
 */
public class createGUI extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L; //A serial must be created
	
	protected JTextArea textArea; //A non-user-modifiable textual display area
	protected JTextField textField; //A user-entry area (when enabled)
	protected JFrame frame; //The lowest-level of the GUI window, basically a blank canvas
	
	private int threadNum; //The thread's number, used for a few purposes. 0 for main window
	private String inputString; //A user input string in the entry area
	public boolean inputUpdated = false; //True when the user has input a string
	
	/**
	 * @param tNum The thread number
	 * 
	 * Window Constructor
	 */
	public createGUI(int tNum) {
		super(new GridBagLayout()); //Creates a new layout with the specified layout manager
		this.threadNum = tNum; //Set the input thread number to a copy local to the class
		
		textField = new JTextField(40); //Create a new text field, 40 characters wide
		textField.addActionListener(this); //Add an event listener in this class for the textField
		textField.setEditable(false); //Set the Entry Field to non-usable by default
		
		textArea = new JTextArea(10, 40); //Create a new display field, 40 chars wide, 10 rows long
		textArea.setEditable(false); //Set the display field to non-usable by default
		JScrollPane scrollPane = new JScrollPane(textArea); //Set the text area to use a scroll bar
		
		GridBagConstraints con = new GridBagConstraints(); //Generate a new Layout Manager
		con.gridwidth = GridBagConstraints.REMAINDER; //Make this component the last component width-wise
		con.fill = GridBagConstraints.HORIZONTAL; //Make the component fill the window horizontally
		add(textField, con); //Add a TextField to the layout with the previously defined parameters
		
		con.fill = GridBagConstraints.BOTH; //Make the component fill the window horizontally and vertically
		con.weightx = 1.0; //Make any X-Direction extra space distributed by the size of a column 
		con.weighty = 1.0; //Make any X-Direction extra space distributed by the size of a column 
		add(scrollPane, con); //Add a Scrolling Display area(with textArea display) to the layout
		
		if (threadNum == 0){ //If this is the main server window
			frame = new JFrame("TCP Server Main Menu"); //Create a new window with title bar as defined
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //If this window is closed, exit the program
		}
		else{ //If this is a thread window
			frame = new JFrame("TCP Connection Thread " + threadNum); //Create a new window the title bar as defined
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //If the window is closed without properly exiting the thread, do nothing
		}
		frame.add(this); //Add the layout and class to the window
		frame.pack(); //Resize the window appropriately
		frame.setVisible(true); //Display the window
	}
	
	/**
	 * Add a new line to the display in the window
	 * @param text - Incoming data to display
	 */
	public void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable(){ //Threads this statement to perform when it can along with the other threads
			public void run(){
				textArea.append(text + "\n"); //Add the incoming data to the display as a newline
			}
		});
	}
	
	/**
	 * Clear the Display in the Window
	 */
	public void clearTextArea() { 
		SwingUtilities.invokeLater(new Runnable(){ //Threads this statement to perform when it can along with the other threads
			public void run(){
				textArea.setText(""); //Set the display to an empty string
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String inText = textField.getText(); //Get the text from the Window's textField
		setInputString(inText); //Set the Window's inputString parameter to the user's input string
		inputUpdated = true; //Set the bit to let the program know new data was entered
	}

	/**
	 * Input String Setter
	 * @param inputString the inputString to set
	 */
	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

	/**
	 * Input String Getter
	 * @return the inputString
	 */
	public String getInputString() {
		return inputString;
	}
}
