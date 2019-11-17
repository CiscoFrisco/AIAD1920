
import java.util.ArrayList;

public class Position{

    int x;
    int y;

    public Position(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Object o) {
        return this.x == ((Position)o).x && this.y == ((Position)o).y;
    }

    public int hashCode() {
        return (Integer.toString(x) + "," + Integer.toString(y)).hashCode();
    }
}