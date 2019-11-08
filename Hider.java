import java.util.LinkedHashSet;


public class Hider {

    Position pos;
    boolean isGrabbing;
    double currOrientation;
    private LinkedHashSet<Position> cellsSeen;

    public Hider(int x, int y){
        pos = new Position(x, y);
        isGrabbing = false;
        cellsSeen = new LinkedHashSet<Position>();
    }

    public void setCellsCeen(LinkedHashSet<Position> cells){
        this.cellsSeen = cells;
    }
}