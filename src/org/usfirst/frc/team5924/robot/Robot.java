package org.usfirst.frc.team5924.robot;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
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
    
    Servo ballPusher;
    
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
    
    boolean crossingDefense;
    
    boolean moving;
    
    /*
	     * DS Buttons
    */
    int blockButton                = 1;
    
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
    
    int lowGoalPrepareButton       = 12;
    
    double outtakeDuration = 25; // duration (in seconds) that the outtake ball pusher servo is extended before automatically retracted
    int servoOut;
    
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
        shooterLifter.setPID(6.0,0.008,0); 
        //shooterLifter.setPID(3.0,0.0008,0); 
        
        intake = new CANTalon(3);
        intake.changeControlMode(CANTalon.TalonControlMode.Position);
        intake.setFeedbackDevice(CANTalon.FeedbackDevice.AnalogPot);
        intake.setPID(7.8,0.0003,0);
        //intake.setPID(4,0.00003,0);
        
        ballPusher = new Servo(9);
        
        shooterAdjust = false;
        shooterLifterMin = 550.0;
        shooterLifterMax = 890.0;
        shooterAutoPosition = 0.0;

        intakeAdjust = false;
        intakeArmMin = 58.0;
        intakeArmMax = 983.0;
        intakeAutoPosition = 0.0;
    }
   
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	
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
    	ballPusher.set(0);
    	servoOut = 0;
    	
    	shooterAutoPosition = 0.0;
    	intakeAutoPosition = 0.0;
    	
    	
    	
    }
 
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {

    	robotDrive.arcadeDrive(-driveStick.getRawAxis(1), -driveStick.getRawAxis(4));
        
        // outtake
    	if(buttons.getRawButton(servoOuttakeButton)){ // outtake
            ballPusher.set(1);
            servoOut = 1;
        }
        
        if(buttons.getRawButton(shooterIntakeButton)){
            shooter.tankDrive(-0.7, -0.7);
            ballPusher.set(0);
        }else if(buttons.getRawButton(shooterOuttakeButton)){
            shooter.tankDrive(1, 1);
        }
        
        
        if (buttons.getRawButton(drivePrepareButton)){
        	intakeAutoPosition = 411.0;
        	shooterAutoPosition = 710.0;
        }else if (buttons.getRawButton(portcullisPrepareButton)){
        	intakeAutoPosition = 933.0;
        	shooterAutoPosition = 900.0;
        }else if (buttons.getRawButton(chevalDeFrisePrepareButton)){
        	intakeAutoPosition = 735.0;
        	shooterAutoPosition = 865.0;
        }else if (buttons.getRawButton(lowbarFwdButton)){
        	intakeAutoPosition = intakeArmMin;
        	shooterAutoPosition = 880;
        }else if (buttons.getRawButton(resetButton)){
        	intakeAutoPosition = 0.0;
        	shooterAutoPosition = 0.0;
        }else if(buttons.getRawButton(blockButton)){
        	// lift the intake and shooter to block shots
        	intakeAutoPosition = 635;
        	shooterAutoPosition = 520;
        }else if(buttons.getRawButton(intakePrepareButton)){
        	 intakeAutoPosition = 580;
        	 shooterAutoPosition = 860;
        	 ballPusher.set(0);
        }else if(buttons.getRawButton(lowGoalPrepareButton)){
        	intakeAutoPosition = 265;
        	shooterAutoPosition = 820;
        }else if(buttons.getRawButton(highGoalPrepareButton)){
        	intakeAutoPosition = 265;
        	shooterAutoPosition = 520;
        }
        
        
        
        // *********************************
        //   Shooter Dart Actuator Control
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
        
        if (intakeAutoPosition > 0.0){
        	if (Math.abs(intakePosition - intakeAutoPosition) < 1.0){
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
	                    intake.set(intakePosition + 70);
	                }else{
	                    intake.set(intakePosition + 15);
	                }
	                
	                System.out.println("intake going down " + intakePosition );
	            }else{
	                intake.set(intakeArmMax);
	                System.out.println("Intake at Maximum");
	            }
	            
	        }
	        else if (intakeControl > .2){
	            intakeAdjust = true;
	            
	            // Make sure the shooter does not go below the minimum allowed position 
	            if (intakePosition > intakeArmMin){
	                if(intakeControl > .6){
	                    intake.set(intakePosition - 70);
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
        	ballPusher.set(0);
        	servoOut = 0; 
        }
        
        System.out.println("shooter position: " + shooterLifter.getPosition());
        System.out.println("intake position: " + intake.getPosition());
    }
   
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
        
        System.out.println("shooter position: " + shooterLifter.getPosition());
        System.out.println("intake position:" + intake.getPosition());
    }
   
}