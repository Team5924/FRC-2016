package org.usfirst.frc.team5924.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this prm oject, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	CameraServer camera;
	
    RobotDrive robotDrive;
    RobotDrive shooter;
    
    Joystick driveStick;
    Joystick buttons;

    CANTalon shooterLifter;
    
    CANTalon intake;
    
    Servo outtakeServo;
    
    double shooterPosition;
    boolean   shooterAdjust;
    
    double intakePosition;
    boolean intakeAdjust;
    
    double shooterLifterMin;
    double shooterLifterMax;
    
    double intakeArmMin;
    double intakeArmMax;
    
    double intakeAutoPosition;
    double shooterAutoPosition;

    /*
	     * DS Button Assignments
    */
    int lowGoalPrepareButton       = 1;    
    int highGoalPrepareButton      = 2;    
    int servoOuttakeButton         = 3;
    int intakePrepareButton        = 4;
    int shooterOuttakeButton       = 5;
    int shooterIntakeButton        = 6;
    int portcullisPrepareButton    = 7;
    int chevalDeFrisePrepareButton = 8;
    int drivePrepareButton         = 9;   
    int resetButton                = 10;   
    int lowbarFwdButton            = 11;
    
    double outtakeDuration = 25; // duration that the outtake ball pusher servo is extended before automatically retracted
    int servoOut;
    
    Ultrasonic mainUltrasonic;
    DigitalOutput mainUltrasonicPing;
    DigitalInput mainUltrasonicEcho;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	camera = CameraServer.getInstance();
    	camera.setQuality(50);
    	camera.startAutomaticCapture("cam0");
    	
        robotDrive = new RobotDrive(0, 2, 1, 3);
        shooter = new RobotDrive(4, 5);
        
        driveStick = new Joystick(0);
        buttons = new Joystick(1);
        
        shooterLifter = new CANTalon(2);
        shooterLifter.changeControlMode(CANTalon.TalonControlMode.Position);
        shooterLifter.setFeedbackDevice(CANTalon.FeedbackDevice.AnalogPot);
        shooterLifter.setPID(5.2,0.003,0); 
        //shooterLifter.setPID(3.0,0.0008,0); 
        
        intake = new CANTalon(3);
        intake.changeControlMode(CANTalon.TalonControlMode.Position);
        intake.setFeedbackDevice(CANTalon.FeedbackDevice.AnalogPot);
        intake.setPID(4.9,0.0001,0);
 
        
        outtakeServo = new Servo(9);
        
        shooterAdjust = false;
        shooterLifterMin = 70.0;
        shooterLifterMax = 355.0; // prac bot was 375
        shooterAutoPosition = 0.0;

        intakeAdjust = false;
        intakeArmMin = 7.0;
        intakeArmMax = 907.0;
        intakeAutoPosition = 0.0;

        mainUltrasonicPing = new DigitalOutput(5);
        mainUltrasonicEcho = new DigitalInput(6);
        mainUltrasonic = new Ultrasonic(mainUltrasonicPing, mainUltrasonicEcho);
        mainUltrasonic.setAutomaticMode(true);
        System.out.println("main ultrasonic: " + mainUltrasonic.getRangeInches());
        
        // *********************
        // SmartDashboard
        // *********************
        SmartDashboard.putNumber("Main Ultrasonic", mainUltrasonic.getRangeInches());
        SmartDashboard.putNumber("Auto Duration", 0);
    }
   
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	System.out.println("auto start"); 
    	bdAuto();
    	
    	System.out.println("auto init end");
    }
 
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	
    }
   
    /**
     * This function is called once each time the robot enters tele-operated mode
     */
    public void teleopInit(){
    	outtakeServo.set(0); // retract outtake servo
    	servoOut = 0;
    	
    	shooterAutoPosition = 0.0;
    	intakeAutoPosition = 0.0;
    }
 
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	
    	robotDrive.arcadeDrive(-driveStick.getRawAxis(1), -driveStick.getRawAxis(4));
        
        
        // *********************************
        //   Driver Station Buttons
        // *********************************
    	
    	// shooter and intake control
        if (buttons.getRawButton(drivePrepareButton)){
        	intakeAutoPosition = intakeArmMin;
        	shooterAutoPosition = 197.0;
        }else if (buttons.getRawButton(portcullisPrepareButton)){
        	intakeAutoPosition = 906.0;
        	shooterAutoPosition = 390.0;
        }else if (buttons.getRawButton(chevalDeFrisePrepareButton)){
        	intakeAutoPosition = 906.0;
        	shooterAutoPosition = 390.0;
        }else if (buttons.getRawButton(lowbarFwdButton)){
        	intakeAutoPosition = intakeArmMin;
        	shooterAutoPosition = 366;
        }else if (buttons.getRawButton(resetButton)){
        	intakeAutoPosition = 0.0;
        	shooterAutoPosition = 0.0;
        }else if(buttons.getRawButton(intakePrepareButton)){
        	 intakeAutoPosition = intakeArmMin;
        	 shooterAutoPosition = 390;
        	 outtakeServo.set(0);
        }else if(buttons.getRawButton(lowGoalPrepareButton)){
        	intakeAutoPosition = intakeArmMin;
        	shooterAutoPosition = 252;
        	shooter.tankDrive(-0.7, -0.7);
        	outtakeServo.set(0);
        }else if(buttons.getRawButton(highGoalPrepareButton)){
        	intakeAutoPosition = intakeArmMin;
        	shooterAutoPosition = 70;
        }
        
        // outtake servo control
    	if(buttons.getRawButton(servoOuttakeButton)){
            outtakeServo.set(1);
            servoOut = 1;
        }
        
    	// shooter motor control
        if(buttons.getRawButton(shooterIntakeButton)){
            shooter.tankDrive(-0.7, -0.7);
            outtakeServo.set(0);
        }else if(buttons.getRawButton(shooterOuttakeButton)){
            shooter.tankDrive(1.0, 1.0);
        }
        
        
        // *********************************
        //   Shooter - Dart Actuator Control
        // *********************************
        shooterPosition = shooterLifter.getPosition();
        double shooterPosControl = buttons.getY();
        double desiredPosition;
        
        if (shooterAutoPosition > 0.0){
        	if (Math.abs(shooterPosition - shooterAutoPosition) < 1.0){
        		shooterAutoPosition = 0.0;
        	}else {
        		shooterLifter.set(shooterAutoPosition);
        	}
        }else{
	        // move the shooter
	        if (shooterPosControl < -0.2){
	            shooterAdjust = true;
	            
	            // Make sure the shooter does not go above the maximum allowed position 
	            if (shooterPosition < shooterLifterMax){
	                if(shooterPosControl < -0.6){
	                	desiredPosition = shooterPosition + 80;
	                    
	                    if (desiredPosition > shooterLifterMax){
	                    	desiredPosition = shooterLifterMax;
	                    }
	                	
	                	shooterLifter.set(desiredPosition);
	                }else{
	                    shooterLifter.set(shooterPosition + 15);
	                }
	                
	                System.out.println("shooter going up");
	            }else {
	                shooterLifter.set(shooterLifterMax);
	                System.out.println("at Maximum");
	            }
	            
	        }else if (shooterPosControl > .2){
	            shooterAdjust = true;
	            
	            // Make sure the shooter does not go below the minimum allowed position 
	            if (shooterPosition > shooterLifterMin){
	                if(shooterPosControl > .6){
	                    desiredPosition = shooterPosition - 80;
	                    
	                    if (desiredPosition < shooterLifterMin){
	                    	desiredPosition = shooterLifterMin;
	                    }
	                	
	                	shooterLifter.set(shooterLifterMin);
	                }else {
	                    shooterLifter.set(shooterPosition - 15);
	                }
	                
	                System.out.println("shooter going down");
	            }else{
	                shooterLifter.set(shooterLifterMin);
	                System.out.println("at Minimum");
	            }
	        }else{
	            if (shooterAdjust == true){
	                shooterAdjust = false;
	                shooterLifter.set(shooterPosition);
	            }
	        }
        }

        // *********************************
        //   Intake Arm Control
        // *********************************

        intakePosition = intake.getPosition();
        double intakeControl = buttons.getX();
        
        double intakeDeadband = 1.0;
        
        if (intakeAutoPosition > 0.0){
        	if (Math.abs(intakePosition - intakeAutoPosition) < intakeDeadband){
        		intakeAutoPosition = 0.0;
        	}else{
        		intake.set(intakeAutoPosition);
        	}
        }else{
	        // move the intake 
	        if (intakeControl < -0.2){
	            intakeAdjust = true;
	            
	            // Make sure the shooter does not go above the maximum allowed position 
	            if (intakePosition < intakeArmMax){
	                if(intakeControl < -0.6){
	                    intake.set(intakePosition + 65);
	                }else{
	                    intake.set(intakePosition + 15);
	                }
	                
	                System.out.println("intake going down " + intakePosition );
	            }else{
	                intake.set(intakeArmMax);
	                System.out.println("Intake at Maximum");
	            }
	            
	        }else if (intakeControl > .2){
	            intakeAdjust = true;
	            
	            // Make sure the shooter does not go below the minimum allowed position 
	            if (intakePosition > intakeArmMin){
	                if(intakeControl > .6){
	                    intake.set(intakePosition - 65);
	                }else {
	                    intake.set(intakePosition - 15);
	                }
	                
	                System.out.println("intake going up " + intakePosition);
	            }else{
	                intake.set(intakeArmMin);
	                System.out.println("Intake at Minimum");
	            }
	        }else {
	            if (intakeAdjust == true) {
	                intakeAdjust = false;
	                intake.set(intakePosition);
	            }
	        }
        }
        
        // retract servo after outtake
        if(servoOut > 0 && servoOut < outtakeDuration){
        	servoOut++;
        }else if(servoOut == outtakeDuration){
        	// retract the ball pusher servo
        	outtakeServo.set(0);
        	servoOut = 0; 
        }
        
        // *********************************
        //   Debugging
        // *********************************
        
        System.out.println("shooter : " + shooterLifter.getPosition() + "  intake :  " + intake.getPosition());
        
        SmartDashboard.putNumber("shooter position", shooterLifter.getPosition());
        SmartDashboard.putNumber("intake arm position", intake.getPosition());
   }
   
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
        
        System.out.println("shooter position: " + shooterLifter.getPosition());
        System.out.println("intake position: " + intake.getPosition());
    }
    
    public void lowbarAuto(){
    	shooterLifter.set(366);
    	
    	while(shooterLifter.getPosition() < 366){
    		shooterLifter.set(366);
    	}
    	
    	intake.set(intakeArmMin);
    	
    	int i = 667*5;
    	
    	while(i > 0){
    		robotDrive.arcadeDrive(0.65, 0);
    		i--;
    		System.out.println("auto loop");
    	}
    }
    
    public void bdAuto(){
    	intake.set(intakeArmMin);
    	
    	double i = 667*88.0;
    	
    	while(i > 0.0){
    		robotDrive.arcadeDrive(0.8, 0);
    		i-=1.0;
    	}
    	
    	SmartDashboard.putBoolean("bd auto", true);
    }
}