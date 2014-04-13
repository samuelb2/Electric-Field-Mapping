import java.awt.Graphics;

import javax.swing.JFrame;


public class editBallDisplay extends Display{
	
	private final initialDisplay d;
	private final int ballIndex;
	private Ball ball;

	public editBallDisplay(int w, int h, JFrame f, Program program, Display callingDisplay, int ballIndex) {
		super(w, h, f, program);
		this.d = (initialDisplay)callingDisplay;
		this.ballIndex = ballIndex;
		this.ball = d.ballarray.get(ballIndex);
		init();
	}

	@Override
	void init() {
		String[] startStrs = {"Update Ball"};		
		Button ballEdit = new Button( new updateBallCommand(hostFrame, d, ball, ballIndex ), startStrs);
		ballEdit.setBounds(width/2-50, height*7/9, 100, 50);
		add(ballEdit);
		ballEdit.setVisible(true);
	
		
	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		
	}

}
