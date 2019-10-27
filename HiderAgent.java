import jade.core.Agent;

public class HiderAgent extends Agent {

    Position pos;
    boolean isGrabbing;

    public HiderAgent(int x, int y){
        pos = new Position(x, y);
        isGrabbing = false;
    }

    public void setup() {
        System.out.println("I am a Hider!");
    }
}