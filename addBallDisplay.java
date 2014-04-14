

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;

<<<<<<< HEAD

public class addBallDisplay extends Display{
	
	private final int ballX;
	private final int ballY;
	private final initialDisplay initialDisplay;
	private final int pendingBallArraySizeBeforeAddingOurBall;//Describes the amount of balls pending before our ball - used when drawing the "TEMPORARY BALL" when this window is open

	public addBallDisplay(int w, int h, JFrame f, Program program, int ballX, int ballY, initialDisplay initialDisplay, int pendingBallArraySize) {
		super(w, h, f, program);
		this.ballX = ballX;
		this.ballY = ballY;
		this.initialDisplay = initialDisplay;
		this.pendingBallArraySizeBeforeAddingOurBall = pendingBallArraySize;
=======
public class addBallDisplay extends Display {
	private int ballX;
	private int ballY;
	private double massI;
	private double chargeI;
	private double dxI;
	private double dyI;
	private final Ball pendingBall;
	private final initialDisplay d;

	private JTextField xCoord, yCoord, mass, charge, dx, dy;
	private JLabel xCoordL, yCoordL, massL, chargeL, dxL, dyL;

	public addBallDisplay(int w, int h, JFrame f, Program program, int ballX, int ballY, initialDisplay d, Ball pendingBall) {
		super(w, h, f, program);
		this.ballX = ballX;
		this.ballY = ballY;
		this.pendingBall = pendingBall;

		//temp:
		this.massI = 0.0004;
		this.chargeI = 0.001;
		this.dxI = 0;
		this.dyI = 0;

		this.d = d;
>>>>>>> FETCH_HEAD
		init();
	}

	@Override
	void init() {
<<<<<<< HEAD
		// TODO Auto-generated method stub
		
		String[] startStrs = {"Add New Ball"};		
		Button ballAdd = new Button( new addBallCommand(hostFrame,this.initialDisplay, 0.00010, ballX, ballY, 0, 0, Math.max((Math.random()*100/1000000), 200/1000000), pendingBallArraySizeBeforeAddingOurBall), startStrs, width/2-100, height/2-25, 200, 50);
		add(ballAdd);
		ballAdd.setVisible(true);
		
		
		
=======
		String[] startStrs = {"Add Ball"};
		Button ballAdd = new Button( new addBallCommand(hostFrame,d, this), startStrs,width/2-50, height*1/9, 100, 50);
		add(ballAdd);
		ballAdd.setVisible(true);
		System.out.println(ballAdd.getX() + " " + ballAdd.getY());

		/*
		int x = hostFrame.getWidth()/3; int y = hostFrame.getHeight()/9;
		xCoord = new JTextField("XCoord");
		xCoord.setBounds(x, y, 100, 25);
		this.add(xCoord);
		xCoord.setVisible(true);
		System.out.println(xCoord.getX());
		System.out.println(xCoord.getY());
		*/
		repaint();

>>>>>>> FETCH_HEAD
	}

	@Override
	protected void paintComponent(Graphics g) {
<<<<<<< HEAD
		// TODO Auto-generated method stub
		
=======
		//g.drawString("HIIIII", 50, 50);
		g.setColor(Color.black);
		g.drawRect(this.getX(), this.getY(), this.width, this.height);
		g.fillRect(0, 0, 5, 5);
	}

	public int getX(){
		return ballX;
	}

	public int getY(){
		return ballY;
	}

	public double getMass(){
		return massI;
	}

	public double getCharge(){
		return chargeI;
	}

	public double getDX(){
		return dxI;
	}

	public double getDY(){
		return dyI;
	}

	public Ball getPendingBall(){
		return pendingBall;
>>>>>>> FETCH_HEAD
	}
	
	
	


	


}
