
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Block {

    private int x;
    private int y;
    private boolean locked;
    private boolean beingGrabed;

    public Block(int x, int y) {

        locked = false;
        beingGrabed = false;
        this.x = x;
        this.y = y;
    }
}