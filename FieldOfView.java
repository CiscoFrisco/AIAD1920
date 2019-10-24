import java.lang.Math; 
import java.util.ArrayList;


public class FieldOfView {

    private Position seekerPos;
    private ArrayList<Ray> rays;
    private ArrayList<Position> cellsSeen;
    private Orientation currOrientation;
    private double angle; //line of sight angle
    private int precision; //number of rays
    private double increment;

    public FieldOfView(Orientation currOrientation, Position seekerPos) {
        this.currOrientation = currOrientation;
        this.seekerPos = seekerPos;
        angle = Math.PI/2;
        precision = 5;
        increment = angle/precision;
        rays = new ArrayList<Ray>(precision);
        cellsSeen = new ArrayList<Position>();
        setup();
    }

    public void setup() {

        double currAngle = Math.PI/4; 

        switch(currOrientation){

            case UP: 
                currAngle = 5*currAngle;
                break;
            case DOWN:
                break;
            case LEFT:
                currAngle = 3*currAngle; 
                break;
            case RIGHT:
                currAngle = 7*currAngle; 
                break;
            default:
            break;
        }

        for(int i = 0; i < precision; i++){
            if(i > 0)
                currAngle += ((currAngle + this.increment) < 2*Math.PI) ? this.increment : (currAngle - 2*Math.PI ) + increment;
            rays.add(new Ray(seekerPos,currAngle));
        }
    }

    public void calcCellsSeen(char[][] world){
        for(Ray ray: rays){
            System.out.println(ray.getAngle());
            System.out.println(ray.getSlope());
            System.out.println(ray.getStep()+"\n");
        }
    }
}