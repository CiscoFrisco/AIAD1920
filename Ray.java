public class Ray {

    private Position originPos;
    private double angle;
    private double slope;
    private double step;

    public Ray(Position originPos, double angle) {
        this.originPos = originPos;
        this.angle = angle;
        this.slope = Math.tan(angle);

        if (angle < 3 * (Math.PI / 2) && angle > (Math.PI / 2))
            step = -0.25;
        else
            step = 0.25;
    }

    public Position getCellSeen(char[][] world) {

        double currStep = step;
        int curr_X_pos, curr_Y_pos;

        if (Math.toDegrees(angle) >= 0 && Math.toDegrees(angle) <=90) {
            curr_X_pos = (int) Math.ceil(originPos.x + currStep);
            curr_Y_pos = (int) Math.ceil(originPos.y + slope * currStep);
        } else {
            curr_X_pos = (int) Math.floor(originPos.x + currStep);
            curr_Y_pos = (int) Math.floor(originPos.y + slope * currStep);
        }

        int old_curr_X_pos = curr_X_pos;
        int old_curr_Y_pos = curr_Y_pos;

        System.out.println(Math.toDegrees(angle));
        System.out.println(step);


        while ((curr_X_pos >= 0 && curr_X_pos < world.length)
                && (curr_Y_pos >= 0 && curr_Y_pos < world[curr_X_pos].length) && world[curr_Y_pos][curr_X_pos] == '+') {

            System.out.println(curr_X_pos + "|" + curr_Y_pos + "->" +  world[curr_Y_pos][curr_X_pos]);

            currStep += step;
            old_curr_X_pos = curr_X_pos;
            old_curr_Y_pos = curr_Y_pos;

            if (Math.toDegrees(angle) >= 0 && Math.toDegrees(angle) <=90) {
                curr_X_pos = (int) Math.ceil(originPos.x + currStep);
                curr_Y_pos = (int) Math.ceil(originPos.y + slope * currStep);
            } else {
                curr_X_pos = (int) Math.floor(originPos.x + currStep);
                curr_Y_pos = (int) Math.floor(originPos.y + slope * currStep);
            }
        }

        return new Position(old_curr_X_pos, old_curr_Y_pos);
    }

    public double getSlope() {
        return slope;
    }

    public double getStep() {
        return step;
    }

    public double getAngle() {
        return angle;
    }
}