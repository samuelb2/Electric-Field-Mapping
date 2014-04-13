import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.Scanner;

public abstract class ButtonCommands {
	Display d;
	ButtonCommands(Display d) {
		this.d = d;
	}

	abstract void execute(int caseNum) throws IOException;
}

class pauseBallMovement extends ButtonCommands {
	initialDisplay newD = (initialDisplay) d;// Done to get access to stuff in initialDisplay and not just Display


	pauseBallMovement(initialDisplay d) {
		super(d); //Useless in this place, cuz we are using an initialDisplay.
		//Only kept here if we need to use in future.
	}


	@Override
	void execute(int caseNum) {
		switch(caseNum%2){
		case 0:
			newD.ballsMoving = true;
			break;
		case 1:
			newD.ballsMoving = false;
			break;
		}
	}
}

class Reset extends ButtonCommands{
	Reset(initialDisplay d) {
		super(d); //Useless in this place, cuz we are using an initialDisplay.
		//Only kept here if we need to use in future.
	}

	@Override
	void execute(int caseNum) {
		d.removeAll();
		d.init();
	}
}


class VoltageOnOff extends ButtonCommands{
	private final initialDisplay newD = (initialDisplay) d;// Done to get access to stuff in initialDisplay and not just Display

	VoltageOnOff(initialDisplay d) {
		super(d); //Useless in this place, cuz we are using an initialDisplay.
		//Only kept here if we need to use in future.
	}

	void execute(int caseNum) {
		switch(caseNum%2){
		case 0:
			newD.drawVoltage = true;
			newD.voltageCalcing = true;
			newD.voltageBarMax.setVisible(true);
			newD.voltageBarMin.setVisible(true);
			break;
		case 1:
			newD.drawVoltage = false;
			newD.voltageCalcing = false;
			newD.voltageBarMax.setVisible(false);
			newD.voltageBarMin.setVisible(false);
			break;
		}
	}
}

class toogleElasticWalls extends ButtonCommands{
	initialDisplay newD = (initialDisplay) d;// Done to get access to stuff in initialDisplay and not just Display
	toogleElasticWalls(initialDisplay d) {
		super(d); //Useless in this place, cuz we are using an initialDisplay.
		//Only kept here if we need to use in future.
	}

	@Override
	void execute(int caseNum) {
		switch(caseNum%2){
		case 0:
			newD.elasticWalls = false;
			break;
		case 1:
			newD.elasticWalls = true;
			break;
		}
	}
}

class addBallCommand extends ButtonCommands {

	private final JFrame callingFrame;
	private final initialDisplay d;
	private final double size;
	private final double X;
	private final double Y;
	private final double xspeed;
	private final double yspeed;
	private final double charge;
	private final int pendingBallArraySizeBeforeAddingOurBall;

	addBallCommand(JFrame callingFrame, initialDisplay d, double size, double X, double Y, double xspeed, double yspeed, double charge, int pendingBallArraySize) {
		super(d); //Useless in this place, cuz we are using an initialDisplay.
		//Only kept here if we need to use in future.
		this.callingFrame = callingFrame;
		this.d = d;
		this.size = size;
		this.X = X;
		this.Y = Y;
		this.xspeed = xspeed;
		this.yspeed = yspeed;
		this.charge = charge;
		this.pendingBallArraySizeBeforeAddingOurBall = pendingBallArraySize;
	}

	@Override
	void execute(int caseNum) {
		d.toAdd.add(new Ball(d, size, X, Y, xspeed, yspeed, charge));
		callingFrame.dispatchEvent(new WindowEvent(callingFrame, WindowEvent.WINDOW_CLOSING));
	}
}

class addOrEditCommand extends ButtonCommands{

	private final initialDisplay newD = (initialDisplay) d;
	addOrEditCommand(Display d) {
		super(d);
	}

	@Override
	void execute(int caseNum) {
switch(caseNum%2){


		case 0:

			newD.addOrEditBoolean = false;
			//Going to edit.
			break;
		case 1:
			newD.addOrEditBoolean = true;
			//Going to add.
			break;
		}

	}

}

class updateBallCommand extends ButtonCommands{

	private final JFrame callingFrame;
	private final initialDisplay newD = (initialDisplay) d;
	private final Ball b;
	private final int ballIndex;



	updateBallCommand(JFrame callingFrame,Display d, Ball b, int ballIndex) {
		super(d);
		this.b = b;
		this.ballIndex = ballIndex;
		this.callingFrame = callingFrame;
	}
	@Override
	void execute(int caseNum) {
		b.setColor(Ball.defualtColor);
		newD.ballarray.set(ballIndex, b);
		callingFrame.dispatchEvent(new WindowEvent(callingFrame, WindowEvent.WINDOW_CLOSING));

	}
}

class SaveToFile extends ButtonCommands {
	private final initialDisplay newD = (initialDisplay) d;

	SaveToFile(initialDisplay d) {
		super(d);
	}

	@Override
	void execute(int caseNum) throws IOException {
		Scanner s = new Scanner(System.in);
		System.out.print("Please input file path: ");
		Path file = Paths.get(s.next());
		try (BufferedWriter out = Files.newBufferedWriter(file, Charset.forName("US-ASCII"))) {
			out.write(String.valueOf(newD.ballarray.size()) + '\n');
			for (Ball a : newD.ballarray) {
				out.write(a.toString() + '\n');
			}

			out.write("ballsMoving: " + newD.ballsMoving + '\n');
			out.write("voltageCalcing: " + newD.voltageCalcing + '\n');
			out.write("drawVoltage: " + newD.drawVoltage + '\n');
			out.write("drawBalls: " + newD.drawBalls + '\n');
			out.write("elasticWalls: " + newD.elasticWalls + '\n');
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}
}

class LoadFromFile extends ButtonCommands {
	private final initialDisplay newD = (initialDisplay) d;

	LoadFromFile(initialDisplay d) {
		super(d);
	}

	@Override
	void execute(int caseNum) throws IOException {
		Scanner s = new Scanner(System.in);
		System.out.print("Please input file path: ");
		Path file = Paths.get(s.next());
		try (Scanner in = new Scanner(file);) {
			int n = in.nextInt();
			newD.ballarray.clear();
			for (int i = 0; i < n; i++) {
				newD.ballarray.add(new Ball(newD, in.nextDouble(), in.nextDouble(), in.nextDouble(), 
							in.nextDouble(), in.nextDouble(), in.nextDouble()));
			}
			in.next();
			newD.ballsMoving = in.nextBoolean();
			in.next();
			newD.voltageCalcing = in.nextBoolean();
			in.next();
			newD.drawVoltage = in.nextBoolean();
			in.next();
			newD.drawBalls = in.nextBoolean();
			in.next();
			newD.elasticWalls = in.nextBoolean();
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}
	}
}
