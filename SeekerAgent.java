import jade.core.Agent;

enum Orientation {
    UP, DOWN, LEFT, RIGHT
}

public class SeekerAgent extends Agent {

    Position pos;
    boolean isGrabbing;
    Orientation currOrientation;
    FieldOfView fov;

    public SeekerAgent(int x, int y) {
        pos = new Position(x, y);
        isGrabbing = false;
        currOrientation = Orientation.DOWN;
        fov = new FieldOfView(currOrientation, pos); 
    }

    public void setup() {
        System.out.println("I am a Seeker!");
    }

    public void calcFieldOfView(char[][] world){
        fov.calcCellsSeen(world);
    }
}