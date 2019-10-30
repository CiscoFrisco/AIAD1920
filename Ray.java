public class Ray {

    private Position originPos;
    private double angle;
    private char entity;

    public Ray(Position originPos, double angle) {
        this.originPos = originPos;
        this.angle = angle;
    }

    public Position getCellSeen(char[][] world) {
        double radius = 0.5; 
        double x = originPos.x + 0.5;
        double y = originPos.y + 0.5;
        boolean intersects = false;

        int seenX = originPos.x;
        int seenY = originPos.y;
        int oldSeenX, oldSeenY;

        do {
            oldSeenX = seenX;
            oldSeenY = seenY;

            x += radius*Math.cos(angle);
            y -= radius*Math.sin(angle);
            seenX = (int) x;
            seenY = (int) y;

            if (seenX < 0 || seenX >= world[0].length || seenY < 0 || seenY >= world.length) {
                seenX = oldSeenX;
                seenY = oldSeenY;
                intersects = true;
            }
            else if ( world[seenY][seenX] != '+' && ( (seenX != originPos.x) || (seenY != originPos.y)) )
                intersects = true;
           
            radius += 0.1;
        } while(!intersects);

        Position seenCell = new Position(seenX, seenY);        
        return seenCell;
    }


    public double getAngle() {
        return angle;
    }
}