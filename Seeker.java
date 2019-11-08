import java.util.LinkedHashSet;

public class Seeker {

    Position pos;
    boolean isGrabbing;
    double currOrientation;
    private LinkedHashSet<Position> cellsSeen;

    public Seeker(int x, int y) {
        pos = new Position(x, y);
        isGrabbing = false;
        currOrientation = 0;
        cellsSeen = new LinkedHashSet<Position>();
    }

    public void setCellsCeen(LinkedHashSet<Position> cells){
        this.cellsSeen = cells;
    }
}