package robo;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.util.Utils;
import robocode.*;

public class RobotOne extends AdvancedRobot {

	Enemy target; 
	Hashtable<String, Enemy> targets;
	int direction = 1;	
	int midpointcount=0; 
	int shortMovementCounter = 0;
	int movementDirection=1;
	int gunDirection=1;
	double midpointstrength=0;
	double firePower; 
	double previousEnergy=100;
	boolean timeToMove = false; 
	List<Point2D.Double> movementLog = new ArrayList<Point2D.Double>(); 
	double Mov = -1;
	 
	public void run() {
		setBodyColor(Color.RED);
		targets = new Hashtable<String, Enemy>();
		target = new Enemy(); 
		target.distance=100000; 
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2*Math.PI);	
		while(true){ 
			antiGravMove();					
			doFirePower();
			doScanner();
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
	
		if(distance <10){
			shortMovementCounter++;
		}
	
		if(shortMovementCounter>3){
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
			firePower = 5;
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
	    xforce += Math.sin(ang+Math.PI/2) * force;
	    yforce += Math.cos(ang+Math.PI/2) * force;
	    xforce += 5000/Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
	    xforce -= 5000/Math.pow(getRange(getX(), getY(), 0, getY()), 3);
	    yforce += 5000/Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(getRange(getX(), getY(), getX(), 0), 3);
	    
		movementLog.add(new Point2D.Double(getX()-xforce,getY()-yforce));
		checkForMove(movementLog);
	    if(timeToMove==true){
	    	goTo((getX()-xforce)*1.5,(getY()-yforce)*1.5);
	    	timeToMove=false;
	    }else{
	    	goTo(getX()-xforce,getY()-yforce);
	    }
		
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
	
	double getRange(double x1,double y1, double x2,double y2){
	    double x = x2-x1;
	    double y = y2-y1;
	    double range = Math.sqrt(x*x + y*y);
	    return range;   
	}
	

	public double absbearing( double x1,double y1, double x2,double y2 ){
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
		double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*Math.PI);
		double offset =  Math.random()*0.1;
		en.name = e.getName();
		double h = normaliseBearing(e.getHeadingRadians() - en.heading);
		h = h/(getTime() - en.ctime);
		en.changehead = h;
		en.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); 
		en.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); 
		en.bearing = e.getBearingRadians();
		en.heading = e.getHeadingRadians();
		en.ctime = getTime();	
		en.speed = e.getVelocity();
		en.distance = e.getDistance();	
		en.live = true;
		if ((en.distance < target.distance)||(target.live == false)) {
			target = en;
		}
		target.speedLog.add((Double)e.getVelocity()); 
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		if(en.distance < 150 && Math.abs(en.speed) > 4){
			offset = offset/2;
		}else{
			offset = offset/4;
		}		
		setTurnGunRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians() - getGunHeadingRadians()+offset));
	}

}

	class Enemy {
		String name;
		public double bearing,heading,speed,x,y,distance,changehead;
		public long ctime; 		
		public boolean live; 	
		public double meanSpeed; 

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
	
	
