package robo;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class RobotOne extends AdvancedRobot {

	Enemy target; 
	Hashtable<String, Enemy> targets;
	int direction = 1;	
	double midpointstrength=0;
	int midpointcount=0; 
	double firePower; 
	List<Point2D.Double> movementLog = new ArrayList<Point2D.Double>(); 
	int shortMovementCounter = 0; 
	boolean timeToMove = false; 
	int movementDirection=1; 
	int gunDirection=1;
	double previousEnergy=100; 

	public void run() {
		setBodyColor(Color.RED);
		targets = new Hashtable<String, Enemy>();
		target = new Enemy(); 
		target.distance=100000; 
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2*Math.PI);	
		while(true){ 
			antiGravMove();					//Move the bot
			doFirePower();					//select the fire power to use
			doScanner();					//Oscillate the scanner over the bot
			doGun();
			setFire(firePower);
			execute();
		}
			
		}
	
	void checkForMove(List<Point2D.Double> moveLog){
		double distance = 1000;
		if(moveLog.size()>1){
			distance = Point2D.distance(moveLog.get(moveLog.size()-1).getX(), moveLog.get(moveLog.size()-1).getY(), moveLog.get(moveLog.size()-2).getX(), moveLog.get(moveLog.size()-2).getY());
			System.out.println("Distance: "+distance);
		}
		if(distance <5){
			shortMovementCounter++;
		}
		
		if(shortMovementCounter>0){
			shortMovementCounter = 0; 
			timeToMove = true; 
		}
	}
	
	void doFirePower() {
	
		
		if(target.distance<150){
			firePower=16;
		}else if(target.distance>500){
			firePower=3;
		}else{
			firePower = 8;
		}
	
	}
	
	void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    GravPoint p;
		Enemy en;
    	Enumeration e = targets.elements();
	    
    	//cycle through all the enemies.  If they are alive, they are repulsive.  Calculate the force on us
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				p = new GravPoint(en.x,en.y, -1000);
		        force = p.power/Math.pow(getRange(getX(),getY(),p.x,p.y),2);
		        ang = normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
		midpointcount++;
		if (midpointcount > 2) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new GravPoint(getBattleFieldWidth()/2, getBattleFieldHeight()/2, midpointstrength);
		force = p.power/Math.pow(getRange(getX(),getY(),p.x,p.y),1.5);
	    ang = normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
	    xforce += Math.sin(ang+ Math.PI/2) * force;
	    yforce += Math.cos(ang+Math.PI/2) * force;
	    xforce += 4200/Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
	    xforce -= 4200/Math.pow(getRange(getX(), getY(), 0, getY()), 3);
	    yforce += 4200/Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
	    yforce -= 4200/Math.pow(getRange(getX(), getY(), getX(), 0), 3);
	    
	    
		movementLog.add(new Point2D.Double(getX()-xforce,getY()-yforce));
		System.out.println("im here");
		checkForMove(movementLog);
	    if(timeToMove==true){
	    	System.out.println("im here222");
	    	goTo(getX()-xforce,getY()-yforce+700);
	    	timeToMove=false;
	    }
	    
	    goTo(getX()-xforce,getY()-yforce);
	}
	
	void goTo(double x, double y) {
	    double dist = 80; 
	    double angle = Math.toDegrees(absbearing(getX(),getY(),x,y));
	    double r = turnTo(angle+Math.PI/2.5);
	    setAhead(dist * r);
	}
	
	int turnTo(double angle) {
	    double ang;
	    int dir;
	    ang = normaliseBearing(getHeading() - angle);
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    }
	    else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    }
	    else {
	        dir = 1;
	    }
	    setTurnLeft(ang);
	    return dir;
	}
	
	void doScanner() {
		setTurnRadarLeftRadians(2*Math.PI);
	}
	
	void doGun() {
		long time;
	    long nextTime;
	    Point2D.Double p;
	    p = new Point2D.Double(target.x, target.y);
	    for (int i = 0; i < 10; i++){
	        nextTime = ((int)Math.round((getRange(getX(),getY(),p.x,p.y)/(20-(3*firePower)))));
	        time = getTime() + nextTime;
	        p = target.guessPosition(time);
	    }
	    /**Turn the gun to the correct angle**/
	    double gunOffset = getGunHeadingRadians() - 
	                  (Math.PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
	    setTurnGunLeftRadians(normaliseBearing(gunOffset));
	}
	
	double normaliseBearing(double ang) {
	    if (ang > Math.PI)
	        ang -= 2*Math.PI;
	    if (ang < -Math.PI)
	        ang += 2*Math.PI;
	    return ang;
	}
	
	double normaliseHeading(double ang) {
		if (ang > 2*Math.PI)
			ang -= 2*Math.PI;
		if (ang < 0)
			ang += 2*Math.PI;
		return ang;
	}
	

	
	double getRange(double x1,double y1, double x2,double y2) {
	    double x = x2-x1;
	    double y = y2-y1;
	    double range = Math.sqrt(x*x + y*y);
	    return range;   
	}
	

	public double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 ){
			return Math.asin( xo / h );
		}
		
		if( xo > 0 && yo < 0 ){
			return Math.PI - Math.asin( xo / h );
		}
		
		if( xo < 0 && yo < 0 ){
			return Math.PI + Math.asin( -xo / h );
		}
		
		if( xo < 0 && yo > 0 ){
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		
		return 0;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		Enemy en;
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
		} else {
			en = new Enemy();
			targets.put(e.getName(),en);
		}
		//the next line gets the absolute bearing to the point where the bot is
		double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*Math.PI);
		//this section sets all the information about our target
		en.name = e.getName();
		double h = normaliseBearing(e.getHeadingRadians() - en.heading);
		h = h/(getTime() - en.ctime);
		en.changehead = h;
		en.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is
		en.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
		en.bearing = e.getBearingRadians();
		en.heading = e.getHeadingRadians();
		en.ctime = getTime();				//game time at which this scan was produced
		en.speed = e.getVelocity();
		en.distance = e.getDistance();	
		en.live = true;
		if ((en.distance < target.distance)||(target.live == false)) {
			target = en;
		}
		target.speedLog.add((Double)e.getVelocity()); 
	}
	

		
}

	class Enemy {
		String name;
		public double bearing,heading,speed,x,y,distance,changehead;
		public long ctime; 		
		public boolean live; 	
		public double[] knownXs; 
		public double[] knownYs; 
		public double meanSpeed; 
		//public boolean isStuck; 
		public List<Point2D.Double> knownPositions; 
		public List<Double> speedLog = new ArrayList<Double>(); 
		
		public Point2D.Double guessPosition(long when) {
			double diff = when - ctime;
			double mean = mean(speedLog); 
			if(speedLog.get(speedLog.size()-1)==0){
				return new Point2D.Double(x, y);
			}else{
			double newY = y + Math.cos(heading) * mean * diff;
			double newX = x + Math.sin(heading) * mean * diff;
			return new Point2D.Double(newX, newY);
			}
			}
		
		/*public void stuckrobot(){
			if(knownPositions.get(knownPositions.size()-1).equals(knownPositions.get(knownPositions.size()-2))){
				this.isStuck=true; 
			}
			else{ this.isStuck=false; }
		}*/
		
		public String getName() {
			return name;
		}
		
		public double getVelocity() {
			return speed;
		}
		
		public double mean(List<Double> speedLog){ 
			double ac=0.0; 
			for(Double a:speedLog){
				ac+=(double)a; 
			}
			return ac/speedLog.size();
		}
		
	}
	

	
	class GravPoint {
	    public double x,y,power;
	    public GravPoint(double pX,double pY,double pPower) {
	        x = pX;
	        y = pY;
	        power = pPower;
	    }
	}
	
	
