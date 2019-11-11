import java.lang.Math; 
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class FieldOfView {

    private Position seekerPos;
    private double seekerAngle;
    private double amplitude; //line of sight angle
    private int precision; //number of rays

    private ArrayList<Ray> rays;
    private LinkedHashSet<Position> cellsSeen;

    public FieldOfView(Position seekerPos, double seekerAngle) {
        this.seekerPos = seekerPos;
        this.seekerAngle = seekerAngle;
        this.amplitude = 7*Math.PI/9;
        this.precision = 20;
        
        rays = new ArrayList<Ray>(precision + 1);
        cellsSeen = new LinkedHashSet<Position>();
        setup();
    }

    public void setup() {

        double angle = this.seekerAngle - this.amplitude/2;
        double increment = amplitude/precision;

        for (int i = 0; i <= this.precision; i++) {
            rays.add(new Ray(this.seekerPos, angle + i*increment));
        }
    }

    public void calcCellsSeen(char[][] world){
        for(Ray ray: rays){
            Position cellSeen = ray.getCellSeen(world);
            cellsSeen.add(cellSeen);
        }
    }

    public LinkedHashSet<Position> getCellsSeen() {
        return cellsSeen;
    }

    public void setCellsSeen(LinkedHashSet<Position> cellsSeen) {
        this.cellsSeen = cellsSeen;
    }
}