public class Ray {

    private Position originPos;
    private double angle;
    private double slope;
    private double step;

    public Ray(Position originPos, double angle) {
        this.originPos = originPos;
        this.angle = angle;
        this.slope = Math.tan(angle);

        if(angle < 3*(Math.PI/2) && angle > (Math.PI/2))
            step = -0.5;
        else
            step = 0.5;
    }

    // public Position rayIntersectionAt(int x){
    //     x += originPos.x;
    //     int y = Math.floor(slope*x) + originPos.y;
    //     return new Position(x,y);
    // }

    public double getSlope(){
        return slope;
    }

    public double getStep(){
        return step;
    }

    public double getAngle(){
        return angle;
    }
}