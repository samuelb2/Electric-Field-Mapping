/**
 *
 * @author Dean Leitersdorf, William Lee, Ophir Sneh
 *
 */

import java.awt.AWTException;

import javax.swing.JComboBox;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.JSlider;

public class initialDisplay extends Display implements MouseListener, MouseMotionListener {
	public onScreenMessage messages;
	public final double k = 8.987551787368176*Math.pow(10, 9);
	public final double permitivity_of_free_space = 8.85418782 * Math.pow(10, -12);

	private boolean paintloop = true;
	public int TIME_BETWEEN_REPLOTS = 5;
	private ballTextField balltextfield;

	public ArrayList<Ball> ballarray;
	public Queue<Ball> toAdd;//Includes the balls that are waiting to be added from "click on screen"
	//ability to add. Must be public so that other classes can add to this (such as the popup).
	public ArrayList<Ball> pendingBalls;//Used to display where you are adding a ball
	//Balls are in here only while they are being created using creation window.
	public ArrayList<Point> verticesOfBeingAddedInAnimate;//Temp representation of vertecies of being added inanimate
	public ArrayList<inanimateObject> inAnimates;
	
	

	public String[] presets;
	private JComboBox<String> presetCB;
	public String presetSelected;

	int xdif = 0;
	int ydif = 0;

	double volume;
	double lastvolume;
	ArrayList<Double> originalX = new ArrayList<Double>();
	ArrayList<Double> originalY = new ArrayList<Double>();

	private Button ballStart;
	private Button reset;
	public Button elasticWallsButton;//Must be public so other classes can edit its text when elasticity changes.
	private Button Voltage;
	private Button addOrEdit;
	private Button saveToFile;
	private Button loadFromFile;
	private Button typeBallOrWall;
	public JSlider slideElastic;

	ArrayList<JLabel> chargeDisplay;
	Force[][] electricField;
	double[][] voltageValue;

	int voltageBarLength = 300;
	int voltageBarWidth = 50;
	int voltageBarX;
	int voltageBarY;
	JLabel voltageBarMax;
	//JLabel voltageBarMid;
	JLabel voltageBarMin;

	String voltageOnMouse = "";
	int pixel =7;

	int timeCounter = -TIME_BETWEEN_REPLOTS;
	boolean ballsMoving;
	boolean voltageCalcing;
	boolean drawVoltage;
	boolean drawBalls;
	//boolean elasticWalls;
	public int elasticity;
	boolean addOrEditBoolean;//Add - true, Edit - false.
	boolean ballOrWall; //Ball - true, Wall - false.
	//double elasticWalls; // 0 <= elasticity <= 1

	public initialDisplay(int w, int h, JFrame f, Program program) {
		super(w, h, f, program);
		init();
	}

	public void init() {
		//hostProgram.closeAllFrames();//Closes stuff like "Add New Ball"...
		if(hostProgram.getJFrameById("Add Ball")!=null)
			hostProgram.getJFrameById("Add Ball").dispatchEvent(new WindowEvent(hostProgram.getJFrameById("Add Ball"), WindowEvent.WINDOW_CLOSING));
		messages = new onScreenMessage(hostProgram);
		this.voltageBarX = (int)(width/1.18);
		this.voltageBarY = height/6 + height/100;
		this.electricField = new Force[width][height];
		this.voltageValue = new double[width][height];

		balltextfield = new ballTextField();

		setSize(width, height);
		paintloop = true;

		String[] startStrs = {"Start", "Pause"};
		ballStart = (new Button( new pauseBallMovement(this), startStrs, height/9 +75, width/20, 100, 50));
		add(getBallStart());
		getBallStart().setVisible(true);

		String[] resetStrs = {"Reset"};
		reset = new Button (new Reset(this), resetStrs, height/9 +225, width/20, 100, 50);
		add(reset);
		reset.setVisible(true);

		this.elasticity = 50;
		String[] elasticWallsArray = {"Elasticity: " + this.elasticity + "%"};
		elasticWallsButton = new Button(new slideElasticWalls(this, hostProgram), elasticWallsArray,height/9 +325, width/20, 100, 50);
		/*String[] elasticWallsArray = {"Update Elasticity"};
		elasticWallsButton = new Button(new slideElasticWalls(this), elasticWallsArray,height/9 +325, width/20, 100, 50);
		*/
		add(elasticWallsButton);
		elasticWallsButton.setVisible(true);
		
		String[] voltageOnOff = {"Voltage: Off", "Voltage: On"};
		Voltage = new Button (new VoltageOnOff(this), voltageOnOff,height/9 +425, width/20, 100, 50);
		add(Voltage);
		Voltage.setVisible(true);

		String[] addOrEditStrings = {"OnClick: Add", "OnClick: Edit"};
		Voltage = new Button (new addOrEditCommand(this), addOrEditStrings,height/9 +525, width/20, 100, 50);
		add(Voltage);
		Voltage.setVisible(true);

		String[] saveToFileStrings = {"Save To File"};
		saveToFile = new Button (new SaveToFile(this), saveToFileStrings,height/9 +625, width/20, 100, 50);
		add(saveToFile);
		saveToFile.setVisible(true);

		String[] loadFromFileStrings = {"Load From File"};
		loadFromFile = new Button (new LoadFromFile(this), loadFromFileStrings, height/9 +725, width/20, 100, 50);
		add(loadFromFile);
		loadFromFile.setVisible(true);

		String[] ballOrWallStrs = {"Type: Animate", "Type: Inanimate"};
		typeBallOrWall = new Button (new ballOrWallCommand(this), ballOrWallStrs, height/9 +825, width/20, 100, 50);
		add(typeBallOrWall);
		typeBallOrWall.setVisible(true);

		presets = getAllFiles();
		presetCB = new JComboBox<String>(presets);
		presetCB.setBounds(height/9 +925, width/20, 100, 50);
		add(presetCB);
		presetCB.setVisible(true);

		inAnimates = new ArrayList<inanimateObject>();
		verticesOfBeingAddedInAnimate = new ArrayList<Point>();
		toAdd = new LinkedList<Ball>();
		ballarray = new ArrayList<Ball>();
		pendingBalls = new ArrayList<Ball>();
		chargeDisplay = new ArrayList<JLabel>();
		addMouseListener(this);
		addMouseMotionListener(this);

		voltageBarMax = new JLabel("MAX");
		voltageBarMax.setBounds(voltageBarX + 55, voltageBarY-25, 50, 75);
		add(voltageBarMax);

		/*
		voltageBarMid = new JLabel("MID");
		voltageBarMid.setBounds(voltageBarX + 55, voltageBarY+voltageBarLength/2-25, 50, 75);
		add(voltageBarMid);
		voltageBarMid.setVisible(true);
		 */
		voltageBarMin = new JLabel("MIN");
		voltageBarMin.setBounds(voltageBarX + 55, voltageBarY + voltageBarLength-50, 50, 75);
		add(voltageBarMin);

		for (int i = 0; i<2; i++) {
			for (int j = 0; j<3; j++) {
				ballarray.add(new Ball(this,0.00015, width/2-135+i*30, height/6+65+j*30, 0, 0, Math.max((Math.random()*100/1000000), 200/1000000)));
				originalX.add((double) ballarray.get(ballarray.size()-1).getX());
				originalY.add((double) ballarray.get(ballarray.size()-1).getY());

				JLabel temp = new JLabel();

				String str = "";
				str+=(int)(ballarray.get(ballarray.size()-1).charge*1000000);
				str+="µ";
				temp.setText(str);
				temp.setBounds((int)ballarray.get(ballarray.size()-1).getX(), (int)ballarray.get(ballarray.size()-1).getY(), 50, 25);
				chargeDisplay.add(temp);
				add(chargeDisplay.get(chargeDisplay.size()-1));
				temp.setVisible(true);
			}
		}

		ballarray.add(new Ball(this,0.000075, width/2-135+0*30, height/6+65+5*30, 0, 0, -Math.max(Math.random()*100/1000000, 35/1000000)));

		originalX.add((double) ballarray.get(ballarray.size()-1).getX());
		originalY.add((double) ballarray.get(ballarray.size()-1).getY());

		JLabel temp = new JLabel();
		String str = "";
		str+=(int)(ballarray.get(ballarray.size()-1).charge*1000000);
		str+="µ";
		temp.setText(str);
		temp.setBounds((int)ballarray.get(ballarray.size()-1).getX(), (int)ballarray.get(ballarray.size()-1).getY(), 50, 25);
		chargeDisplay.add(temp);
		add(chargeDisplay.get(chargeDisplay.size()-1));
		temp.setVisible(true);

		//For Buttons:
		ballsMoving = false;
		voltageCalcing = false;
		drawVoltage = false;
		drawBalls = true;
		voltageBarMax.setVisible(false);
		voltageBarMin.setVisible(false);
		addOrEditBoolean = true;
		ballOrWall = true;

		repaint();
	}

	private String[] getAllFiles() {
		File directory = new File("Save Data");
		File[] files = directory.listFiles();
		if (files != null && files.length != 0) {
			String[] filenames = new String[files.length-1];
			for (int i = 0; i < filenames.length; i++) {
				if (files[i].getName() == "README.md")
					continue;
				filenames[i] = files[i].getName();
			}
			return filenames;
		}
		return new String[] {""};
	}

	public void paintComponent(Graphics g) {
		presetSelected = presets[presetCB.getSelectedIndex()];
		if(presets.length != getAllFiles().length) {//More presets where saved
			presets = getAllFiles();
			remove(presetCB);
			presetCB = new JComboBox<String>(presets);
			presetCB.setBounds(height/9 +925, width/20, 100, 50);
			add(presetCB);
			presetCB.setVisible(true);}


		while(!messages.isEmpty()){
			messages.printMessage();
			try {
				Thread.sleep(3500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Graphics2D gg = (Graphics2D)g;
		gg.setStroke(new BasicStroke(3));
		g.setColor(new Color(0,Math.min(255*elasticity/100, 255),0));
		g.drawRect(width/6, height/6, width*2/3, height*5/6 - height/10);
		
		gg.setStroke(new BasicStroke(1));
		g.setColor(Color.BLACK);
		lastvolume=width*height;
		xdif = hostFrame.getWidth()-width;
		width=hostFrame.getWidth();
		volume = width*height;
		g.setColor(Color.BLUE);

		//ballStart.repaint();

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
		if (paintloop) {
			try {
				Thread.sleep(TIME_BETWEEN_REPLOTS);
				timeCounter+=TIME_BETWEEN_REPLOTS;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(toAdd!=null&&!toAdd.isEmpty()) {
				addBallsFromQueue();
			}
			if(ballsMoving) {
				ballMovement(g);
			}
			if(voltageCalcing) {
				calcVoltage();
			}
			if(drawVoltage) {
				drawVoltageGrid(g);
				drawVoltageScale(g);
			}
			if(drawBalls) {
				for(inanimateObject j : inAnimates){
					j.draw(g);
				}
				for(int i = 0; i <ballarray.size(); i++) {
					ballarray.get(i).draw(g);
				}
				for(int i = 0; i< chargeDisplay.size(); i++) {
					updateJLabel(chargeDisplay.get(i), i);
				}
				for(int i = 0; i < pendingBalls.size(); i++) {
					if(pendingBalls.get(i) != null) {//There may be nulls in pendingBalls.
						//For issues described when adding the balls through adding new ball window,
						//when a pending ball is 'removed' from the list, it is not truly removed,
						//rather it is set to null.
						pendingBalls.get(i).setColor(new Color(255,153,0, 128)); //Fourth value is opacity, int between 0 and 255.
						pendingBalls.get(i).draw(g);}
				}
			}

			for(Point v: verticesOfBeingAddedInAnimate){//Draw temp circles when adding an inanimate.
				g.setColor(new Color(255, 111, 0));
				g.fillOval(v.x, v.y, 5, 5);
			}
			for(int v = 0; v < verticesOfBeingAddedInAnimate.size(); v++){
				g.setColor(new Color(255, 111, 0));
				g.fillOval(verticesOfBeingAddedInAnimate.get(v).x, verticesOfBeingAddedInAnimate.get(v).y, 5, 5);
				if(v<verticesOfBeingAddedInAnimate.size()-1){
					Point current = verticesOfBeingAddedInAnimate.get(v);
					Point next = verticesOfBeingAddedInAnimate.get(v+1);
					g.drawLine(current.x, current.y, next.x, next.y);
				}
			}
			repaint();
		}
		repaint();
	}

	//PreCodition to calling method: Queue toAdd is NOT empty.
	//PostCondition to calling method: toAdd is emtpy only in MOST cases. In a case where toAdd
	//contains a ball which needs to be added in a location which is occupied by an existing
	//ball on the screen, then the ball that needs to be added will not be added during this call.
	private void addBallsFromQueue() {
		int numBallsToDoInNextCall = 0;//Used to make the method stop when this number of elements
		//is in the queue.
		Ball waiting = toAdd.peek();
		boolean spaceFree = true;
		for(Ball b : ballarray) {
			if(waiting.getX()>=b.getX()-b.getRadius()&&waiting.getX()<=b.getX()+b.getRadius()
					&&waiting.getY()>=b.getY()-b.getRadius()&&waiting.getY()<=b.getY()+b.getRadius()
					)spaceFree=false;
		}
		if(!spaceFree) {
			/*
			 * This is activated if the ball in the top of toAdd cannot be added during this method call.
			 * If so, then the ball is removed and added at the end of the queue.
			 * Note: Queue is FIFO
			 *
			 * numBallsToDoInNextCall is incremented, so to avoid an infinite loop,
			 * and tell the method to stop trying to add the balls which were added to the end of
			 * toAdd (the balls which are going to be added only in the next method call)
			 */
			Ball b = toAdd.poll();
			toAdd.add(b);
			numBallsToDoInNextCall++;
		}
		while(toAdd.size()>numBallsToDoInNextCall) {
			Ball b = toAdd.poll();
			ballarray.add(b);
			originalX.add((double) ballarray.get(ballarray.size()-1).getX());
			originalY.add((double) ballarray.get(ballarray.size()-1).getY());

			//Code also adds a label to display the charge on the particle.
			JLabel temp = new JLabel();

			String str = "";
			str+=(int)(ballarray.get(ballarray.size()-1).charge*1000000);
			str+="µ";
			temp.setText(str);
			temp.setBounds((int)ballarray.get(ballarray.size()-1).getX(), (int)ballarray.get(ballarray.size()-1).getY(), 50, 25);
			chargeDisplay.add(temp);
			add(chargeDisplay.get(chargeDisplay.size()-1));
			temp.setVisible(true);
			repaint();
		}
	}

	public void togglePaintLoop() {
		paintloop = !paintloop;
	}

	/**
	 * @return the ballStartButton
	 */
	public Button getBallStart() {
		return ballStart;
	}



	public void ballMovement(Graphics g) {
		for(int k = 0; k <ballarray.size(); k++) {
			Ball temp = ballarray.get(k);
			temp.force = new Force();

			for(int j = 0; j <ballarray.size(); j++){
				if(k!=j){
					Ball temp2 = ballarray.get(j);

					temp.force.add(CalculateForce(ballarray.get(j), ballarray.get(k)));
					//System.out.println("Calced: " + j + " on: " + k);
				}
			}
			for(inanimateObject o : inAnimates){
				//We represent the inAnimates as BALL objects around their centroid. This makes no difference for force calculations.
				Ball likeABall = new Ball(this, 0, o.getCentroid().x, o.getCentroid().y, 0, 0, -o.getCharge());
				temp.force.add(CalculateForce(temp, likeABall));
			}
		}

		for (int i = 0; i<ballarray.size(); i++) {
			ballarray.get(i).update(g, width, height, TIME_BETWEEN_REPLOTS);
			if (ballarray.get(i).hitWall == true) {
				if (xdif<0) {
					ballarray.get(i).setX(ballarray.get(i).getX()+xdif);
				}
				if (ydif<0) {
					ballarray.get(i).setY(ballarray.get(i).getY()+ydif);
				}
			}
		}
		/*
		if (xdif!= 0 || ydif!=0) {
			for (int i = 0; i<ballarray.size(); i++) {
				ballarray.get(i).getSpeed()*=Math.sqrt(lastvolume/volume);
			}
		}*/
	}

	public void calcVoltage(){
		if(timeCounter%50==0){
			//calculateElectricFieldOnScreen();
			calculateVoltageOnScreen();
			//printVoltages();
		}
		/*
		if(timeCounter%1000==0){
			updateVoltageScaleText();
		}
		 */

		printSigmaKineticEnergyAndElectric();
	}

	private void printSigmaKineticEnergyAndElectric() {
		double totE = 0;
		for(Ball b : ballarray) {
			totE+=Math.pow(b.getSpeed() , 2)*b.mass*0.5;
		}

		for(int k = 0; k <ballarray.size(); k++) {
			Ball temp = ballarray.get(k);

			for(int j = 0; j <ballarray.size(); j++) {
				if(k!=j){
					Ball temp2 = ballarray.get(j);

					totE+=(CalculatePotentialEnergy(ballarray.get(j), ballarray.get(k)));
				}
			}
		}
		//System.out.println(totE);
	}

	private double CalculatePotentialEnergy(Ball ball, Ball ball2) {
		return k*ball.charge*ball2.charge/distance(ball.getX(), ball2.getX(), ball.getY(), ball2.getY());
	}

	private void updateVoltageScaleText(ArrayList<Double> list) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html> Max: ");
		sb.append("<br>");
		try{
			Double n = list.get(list.size()-1);
			int numZerosToAdd = 0;
			if(n >= 999) {
				while(n>=10){
					n/=10;
					numZerosToAdd++;
				}
			}
			else if(n<.1) {
				while(n<1) {
					n*=10;
					numZerosToAdd--;
				}
			}
			sb.append(n.toString().substring(0, 5));

			if(numZerosToAdd!=0){
				sb.append("<br>");
				sb.append("E ");
				sb.append(numZerosToAdd);
			}
		}
		catch(IndexOutOfBoundsException e) {
		}

		voltageBarMax.setText(sb.toString());

		sb = new StringBuilder();
		sb.append("<html> Min: ");
		sb.append("<br>");
		try{
			Double n = list.get(0);

			int numZerosToAdd = 0;
			if(Math.abs(n) >= 999) {
				while(Math.abs(n) >= 10) {
					n/=10;
					numZerosToAdd++;
				}
			}
			else if(Math.abs(n) < .1) {
				while(Math.abs(n) < 10) {
					n *= 10;
					numZerosToAdd--;
				}
			}
			sb.append(n.toString().substring(0, 5));

			if(numZerosToAdd != 0) {
				sb.append("<br>");
				sb.append("E ");
				sb.append(numZerosToAdd);
			}
		}
		catch(IndexOutOfBoundsException e) {

		}
		voltageBarMin.setText(sb.toString());
	}

	private void printVoltages() {
		for(int x = 0; x <width; x++) {
			for (int y = 0; y < height; y++) {
				double v = voltageValue[x][y];
				if (v != 0) {
					//	System.out.println("X: " + x + " Y: " + y + " V: " + v);
				}
			}
		}
	}

	private void drawVoltageGrid(Graphics g) {
		ArrayList<Double> list = makeList(voltageValue);
		Collections.sort(list);
		double belowZero = getNegativeAmount(list);
		double exactlyZero  = getZeroAmount(list);
		double aboveZero = getPositiveAmount(list);

		for(int x = width/6 +5; x < width*5/6 -10; x+=pixel) {
			for (int y = height/6+5; y <height*5/6 + height/10 -30; y+=pixel) {
				double value = voltageValue[x][y];
				g.setColor(Color.black);
				int colorVal = 128;
				boolean hot = false;

				if(value < 0) {
					colorVal = (int)((belowZero - list.indexOf(value))/belowZero*128);
					colorVal = Math.min(colorVal, 128);
					hot = false;

				}else if(value>0){
					colorVal = (int)((list.indexOf(value)-belowZero+2)/aboveZero*128);
					colorVal = Math.min(colorVal, 128);
					hot = true;
				}

				if(!hot){
					g.setColor(new Color(128-colorVal, 0, colorVal+127));
				}
				else if(hot){
					g.setColor(new Color(colorVal+127, 0, 128-colorVal));
				}
				g.fillRect(x, y, 7, 7);
			}
		}
		updateVoltageScaleText(list);
	}

	private int getZeroAmount(ArrayList<Double> list) {
		int counter = 0;
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i) == 0) {
				counter++;
			}
		}
		return counter;
	}

	private int getPositiveAmount(ArrayList<Double> list) {
		int counter = 0;
		for(int i = 0; i < list.size(); i++){
			if(list.get(i) > 0) {
				counter++;
			}
		}
		return counter;
	}

	private int getNegativeAmount(ArrayList<Double> list) {
		int counter = 0;
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i) < 0){
				counter++;
			}
		}
		return counter;
	}

	private ArrayList<Double> makeList(double[][] a) {
		ArrayList<Double> retval = new ArrayList<Double>();
		for(int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				if(a[i][j]!=0) {
					retval.add(a[i][j]);
				}
			}
		}
		return retval;
	}

	private void drawVoltageScale(Graphics g) {
		int x = voltageBarX;
		int y = voltageBarY;
		int width = voltageBarWidth;
		int height = 1;
		double length = voltageBarLength/2;
		//For hot:
		for(double i = length; i >=0; i--) {
			int colorVal = (int) (i/length*128);
			g.setColor(new Color(colorVal+127, 0, 128-colorVal));
			colorVal = Math.min(colorVal, 128);
			g.fillRect(x, (int) (y+length-i), width, height);
		}
		for(double i = 1; i <=length; i++) {
			int colorVal = (int) (i/length*128);
			g.setColor(new Color(128-colorVal, 0, colorVal+127));
			colorVal = Math.min(colorVal, 128);
			g.fillRect(x, (int) (y+length+i), width, height);
		}
	}

	private void calculateVoltageOnScreen() {
		for(int x = width/6 +5; x < width*5/6-10; x+=pixel) {
			for (int y = height/6+5; y <height*5/6 + height/10-30; y+=pixel) {
				voltageValue[x][y] = 0;
				for(int i = 0; i <ballarray.size(); i++){
					Ball ball = ballarray.get(i);
					voltageValue[x][y] += calculateVoltage(ball, new Point(x, y));
				}
				for(inanimateObject o : inAnimates){
					voltageValue[x][y] += calculateVoltage(o, new Point(x,y));
				}
			}
		}
	}

	private double calculateVoltage(Ball ball, Point point) {
		return ball.charge/distance(ball.getX(), ball.getY(), point.x, point.y)/(4*Math.PI*permitivity_of_free_space);
	}

	private double calculateVoltage (inanimateObject o, Point point){
		return o.getCharge()/distance(o.getCentroid().x, o.getCentroid().y, point.x, point.y)/(4*Math.PI*permitivity_of_free_space);
	}
	private double distance(double x, double y, double x2, double y2) {
		return Math.pow(Math.pow(x - x2, 2) + Math.pow(y - y2, 2), 0.5);
	}

	private void calculateElectricFieldOnScreen() {
		for(int x = width/6+5; x < width*5/6-10; x+=pixel) {
			for (int y = height/6+5; y <height*5/6 + height/10-30; y+=pixel) {
				electricField[x][y] = new Force();
				for(int i = 0; i <ballarray.size(); i++) {
					Ball ball = ballarray.get(i);

					electricField[x][y].add(calculateElectricField(ball,  new Point (x, y)));
				}
			}
		}
	}

	private Force calculateElectricField(Ball ball, Point point) {
		double magnitude = ball.charge*k;
		magnitude/=distanceSquared(ball, new Ball(this,0, point.x, point.y, 0, 0, 0));

		// Only thing that matters for distanceSquared is the x and y coords,
		//thus all the rest can be 0s.
		double theta = calculateTheta(ball, new Ball(this,0, point.x, point.y, 0, 0, 0));
		return new Force(magnitude, theta);
	}

	private void updateJLabel(JLabel jLabel, int i) {
		String str = "";
		str += (int)(ballarray.get(i).charge*1000000);
		str += "µ";
		jLabel.setText(str);
		jLabel.setBounds((int)ballarray.get(i).getX(), (int)ballarray.get(i).getY(), 50, 25);
		//add(jLabel);
		//jLabel.setVisible(true);

	}

	public Force CalculateForce(Ball ballA, Ball ballB) {
		double magnitude = Math.abs(ballA.charge) * Math.abs(ballB.charge);
		boolean attract = attract(ballA, ballB);
		magnitude *= k;
		double distSquare = distanceSquared(ballA, ballB);
		magnitude /= distSquare;

		if(Math.pow(distSquare, 0.5)<5){
			magnitude = 0;//This is to get rid of the acceleration bug.
			//It works in the sense that anyhow, the forces would cancel out when the balls cross.
		}

		/*
		if(distSquare<1&&distSquare!=0){
			System.out.println("FFFFFFFFFFFFFFFFFFFF " + distSquare);
			distSquare=1;//This is in order to avoid massive accelerations.
		}*/

		double theta = calculateTheta(ballA, ballB);
		if(!attract) {
			theta+=Math.PI;
		}
		Force retval = new Force(magnitude, theta);
		return retval;
	}

	private double calculateTheta(Ball b1, Ball b2) {
		double theta = 0;
		double xComp = b1.getX() - b2.getX();
		double yComp = b1.getY() - b2.getY();
		if(xComp > 0) {
			theta = Math.atan(yComp/xComp);
			return theta;
		}else if(xComp < 0){
			theta =  Math.atan(yComp/xComp) + Math.PI;
			return theta;
		}
		else if(xComp == 0) {
			if(yComp == 0) {
				return 00;
			} else if (yComp > 0) {
				return Math.PI/2;
			} else if(yComp < 0) {
				return 3*Math.PI/2;
			}
		}
		return theta;
	}

	private double calculateTheta2(Ball b1, Ball b2) {
		return Math.atan2(b1.getX()-b2.getX(), b1.getY()-b2.getY());
	}

	public boolean attract(Ball ballA, Ball ballB) {
		return ballA.charge * ballB.charge < 0;
	}

	public double distanceSquared(Ball b1, Ball b2) {
		return Math.pow(b1.getX()-b2.getX(), 2) + Math.pow(b1.getY()-b2.getY(), 2);
	}

	public void setPaintLoop(boolean value) {
		paintloop = value;
	}

	public boolean isAnimate(Point p) {
		// for now
		// assumes that animate objects are not inside inanimate ones
		// assumes that an object is located at that point
		for (inanimateObject a : inAnimates) {
			if (a.shape.contains(p))
				return false;
		}
		return true;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent a) {
		/*
		 *
		 * temp commented out
		 *

		int x = a.getX() +5;
		x-=x%pixel;
		int y = a.getY() +5;
		y-=y%pixel;
		//System.out.println (x + "," +y);

		//if(voltageValue[x-(x%7)+a][y-(y%7)+a]!=0){

			System.out.println(voltageValue[x+5][y] + "," + (x-2) + "," + (y));

			ArrayList<Double> list = makeList(voltageValue);
			Collections.sort(list);
			double belowZero = getNegativeAmount(list);
			double exactlyZero  = getZeroAmount(list);
			double aboveZero = getPositiveAmount(list);
			double value = voltageValue[x+5][y];
			int colorVal = 128;
			boolean hot = false;

			if(value<0){
				colorVal = (int)((belowZero - list.indexOf(value))/belowZero*128);
				colorVal = Math.min(colorVal, 128);
				hot = false;

			}else if(value>0){
				colorVal = (int)((list.indexOf(value)-belowZero+2)/aboveZero*128);
				colorVal = Math.min(colorVal, 128);
				hot = true;
			}

			if(!hot){
				System.out.println(new Color(128-colorVal, 0, colorVal+127));
				System.out.println("Us: " + list.indexOf(value) + " Zero: "+ belowZero);
			}
			else if(hot){
				System.out.println(new Color(colorVal+127, 0, 128-colorVal));
			}
		*/
	}

	@Override
	public void mouseClicked(MouseEvent a) {
		//System.out.println("X: " + a.getX() + " Y: " + a.getY());

		try {
			Robot robot = new Robot();
			System.out.println(a.getX() + " " + a.getY() + " " + robot.getPixelColor(a.getX(), a.getY()));
		} catch (AWTException e) {
			e.printStackTrace();
		}

		if (a.getX() /*+radius*/ <= width*5/6 && a.getX() /*-radius*/ >= width/6 + 3 && a.getY()/*+radius*/
				<= height*9/10 && a.getY()/*-radius*/ >= height/6 + 3) {
			System.out.println("in box");

			//Make sure we are not placing this on another ball.
			boolean spaceFree = true;
			Ball temp = null;
			for(Ball b : ballarray){
				if(a.getX()>=b.getX()-b.getRadius()&&a.getX()<=b.getX()+b.getRadius()
						&&a.getY()>=b.getY()-b.getRadius()&&a.getY()<=b.getY()+b.getRadius())
						spaceFree = false;
			}
			if(!spaceFree) {
				for(Ball b : ballarray) {
					if(a.getX()>=b.getX()-b.getRadius()&&a.getX()<=b.getX()+b.getRadius()
							&&a.getY()>=b.getY()-b.getRadius()&&a.getY()<=b.getY()+b.getRadius())
							temp = b;
				}
			}
			final Ball ballInSpace = temp;

			if (ballOrWall) {
				if (addOrEditBoolean) {
					if (spaceFree) {
						if(hostProgram.getJFrameById("Add Ball")==null){
							final boolean ballsWhereMoving;

							if(ballsMoving) {getBallStart().simulateClick();ballsWhereMoving =true;}//Always pause.
							else ballsWhereMoving = false;
									hostProgram.createJFrame(50, 25, "Add Ball", new Color(255,153,0), false, "Add Ball");

									final int pendingBallsSize = pendingBalls.size(); //This is how many balls are already in the process of being added.
									final JFrame addBallF = hostProgram.getJFrameById("Add Ball");
									addBallF.addWindowListener(new java.awt.event.WindowAdapter() {
									    @Override
									    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
									    	if(ballsWhereMoving){
									    		if(!ballsMoving)getBallStart().simulateClick();
									    	}
									    	hostProgram.framesId.remove("Add Ball");
									    	hostProgram.frames.remove(addBallF);
									    	pendingBalls.set(pendingBallsSize, null);//Note: We don't use pendingBalls.size(),
									    	//as that size may change, and we want it to remove the current ball --
									    	//We want it to remove the current pending ball, once the adding new ball
									    	//JFrame is closed.
									    	//
									    	//Note2: Also, there is no -1 after pendingBallsSize,
									    	//as the ball we want to remove will only be added later,
									    	//meaning that the current size doesn't include it yet.
									    	//
									    	//Note 3: Also, we do not remove from pendingBalls, rather we
									    	//set to null, as removing would affect the size of the array
									    	//and in turn affect the indexes of the other balls in the
									    	//array. Affecting the other indexes of the array is bad,
									    	//as it breaks the condition set in Note 1,
									    	//that pendingBallsSize should be final and never change.
									    	//Theoretically, it's a waste of storage to set to null,
									    	//yet as we are talking about manually added balls ONLY in here
									    	//the only storage space that is lost, is the storage required
									    	//for storing NULL in an arraylist * the number of balls
									    	//added manually by user (very small number compared to
									    	//data storage which is typically available.
									    }
									});

									Display addBallD = new addBallDisplay(addBallF.getWidth(), addBallF.getHeight(), addBallF, hostProgram, a.getX(), a.getY(), this, pendingBallsSize);
									addBallF.add(addBallD);

									pendingBalls.add(new Ball(this, 0.00040, a.getX(), a.getY(), 0, 0, 0));




						} else{
							hostProgram.getJFrameById("Add Ball").toFront();
						}
					}


					else { //addOrEditBoolean = true, but spaceFree = false.
						messages.addMessage("Cannot add ball here, space is already occupied by another ball.",
								onScreenMessage.CENTER);}
					
				}
				else {//addOrEditBoolean = false.
					if(!spaceFree) {
						if(hostProgram.getJFrameById("Edit Ball")==null) {
							final boolean ballsWhereMoving;

							if (ballsMoving) {getBallStart().simulateClick();ballsWhereMoving =true;}//Always pause.
							else ballsWhereMoving = false;

							hostProgram.createJFrame(50, 50, "Edit Ball", new Color(255,153,0), false, "Edit Ball");

							final JFrame editBallF = hostProgram.getJFrameById("Edit Ball");
							editBallF.addWindowListener(new java.awt.event.WindowAdapter() {
								@Override
								public void windowClosing(java.awt.event.WindowEvent windowEvent) {
									if (ballsWhereMoving){
										if(!ballsMoving)getBallStart().simulateClick();
									}
									hostProgram.framesId.remove("Edit Ball");
									hostProgram.frames.remove(editBallF);
									ballInSpace.setColor(Ball.defualtColor);
								}});

							Display editBallD = new editBallDisplay(editBallF.getWidth(), editBallF.getHeight(),
									editBallF, hostProgram, this, ballarray.indexOf(ballInSpace));
							editBallF.add(editBallD);
							ballInSpace.setColor(Color.cyan);

						} else {hostProgram.getJFrameById("Edit Ball").toFront();}
					}
				}

			} else {//ballOrWall = false;
				if (hostProgram.getJFrameById("Add Inanimate") == null) {
					final boolean ballsWhereMoving;

					if (ballsMoving) {getBallStart().simulateClick();ballsWhereMoving = true;}//Always pause.
					else ballsWhereMoving = false;

					hostProgram.createJFrame(50, 25, "Add Inanimate", new Color(255,153,0), false, "Add Inanimate");

					final JFrame editBallF = hostProgram.getJFrameById("Add Inanimate");
					editBallF.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing (java.awt.event.WindowEvent windowEvent) {
							if (ballsWhereMoving && !ballsMoving) {
								getBallStart().simulateClick();
							}
							hostProgram.framesId.remove("Add Inanimate");
							hostProgram.frames.remove(editBallF);
							verticesOfBeingAddedInAnimate = new ArrayList<Point>();
						}
					});

					Display editBallD = new addInanimateDisplay(editBallF.getWidth(), editBallF.getHeight(),
							editBallF, hostProgram, this);
					editBallF.add(editBallD);
					verticesOfBeingAddedInAnimate.add(new Point(a.getX(), a.getY()));

				} else {
					//hostProgram.getJFrameById("Edit Ball").toFront();
					//In this case we don't bring to front, because it will always be up when we click
					//to add more vertecies and we don't want user to keep jumping between windows.

					verticesOfBeingAddedInAnimate.add(new Point(a.getX(), a.getY()));
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	public Display getSelf(){
		return this;
	}

	private class ballTextField extends JFormattedTextField implements PropertyChangeListener {
		private JLabel Size;
		private JLabel Force;

		private String size = "Size: ";
		private String force = "Force: ";

		ballTextField() {
			setColumns(10);
		}

		protected Document createDefaultModel() {
			return new ballDocument();
		}

		class ballDocument extends PlainDocument {
		}

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
		}
	}
}
