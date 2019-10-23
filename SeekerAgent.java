import jade.core.Agent;

public class SeekerAgent extends Agent {

    Position pos;
    boolean isGrabbing;
    Orientation currOrientation;
    ArrayList<Position> cellsSeen;

    enum Orientation {
        UP, DOWN, LEFT, RIGHT
    }

    public SeekerAgent(int x, int y) {
        pos = new Position(x, y);
        isGrabbing = false;
        currOrientation = Orientation.UP;

        System.out.println("!!!!Seeker Created!!!!\n");
    }

    public void setup() {
        System.out.println("I am a Seeker!");
    }

    public ArrayList<Position> getCellsSeen() {
        return cellsSeen;
    }

    public void setCellsSeen(ArrayList<Position> cellsSeen) {
        this.cellsSeen = cellsSeen;
    }

     public void calcCellsSeen(char[][] world){
        
    }
}