import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

	public class Button extends JButton implements ActionListener {

		public int timesClicked = 0;
		public int roundLength; //How many times button must be clicked to return to original
		//position.
		ButtonCommands command;
		String[] strs; //Contains the strings that will be displayed on the button
		//with every click.

		Button(ButtonCommands command, String[]strs) {
			super(strs[0]);
			addActionListener(this);
			this.command = command;
			roundLength = strs.length;
			this.strs = strs;


		}

		public void actionPerformed(ActionEvent arg0) {
			this.setText(strs[(timesClicked+1)%roundLength]);
			command.execute(timesClicked);
			timesClicked++;	

		}
		
		public void simulateClick(){
			actionPerformed(new ActionEvent(this, 0, ""));
		}
	}
