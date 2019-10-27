public class Ray {

    private Position originPos;
    private double angle;
    private double slope;
    private double step;

    public Ray(Position originPos, double angle) {
        this.originPos = originPos;
        this.angle = angle;
        this.slope = Math.tan(angle);

        if (slope > 1.1 || slope < -1.1) {
            if (angle < 3 * (Math.PI / 2) && angle > (Math.PI / 2))
                step = -0.35;
            else
                step = 0.35;

        } else {
            if (angle < 3 * (Math.PI / 2) && angle > (Math.PI / 2))
                step = -0.6;
            else
                step = 0.6;
        }

    }

    public Position getCellSeen(char[][] world) {

        int curr_X_pos, curr_Y_pos, old_curr_X_pos, old_curr_Y_pos;
        double d_curr_X_pos, d_curr_Y_pos;

        curr_X_pos = originPos.x;
        curr_Y_pos = originPos.y;

        double currStep = step;

        // System.out.println(slope);
        // System.out.println(step);

        do {
            old_curr_X_pos = curr_X_pos;
            old_curr_Y_pos = curr_Y_pos;

            d_curr_X_pos = curr_X_pos + 0.5;
            d_curr_Y_pos = curr_Y_pos + 0.5;

            curr_X_pos = (int) Math.floor(originPos.x + currStep + 0.5);
            curr_Y_pos = (int) Math.floor(originPos.y + slope * currStep + 0.5);

            // System.out.println(originPos.x + currStep + 0.5);
            // System.out.println(originPos.y + slope * currStep + 0.5);
            // System.out.println(curr_X_pos + "|" + curr_Y_pos + " -> " +
            // world[curr_Y_pos][curr_X_pos]);
            // System.out.println("HEY: " + d_curr_X_pos + "|" + d_curr_Y_pos);

            currStep += step;

        } while ((curr_X_pos >= 0 && curr_X_pos < world.length)
                && (curr_Y_pos >= 0 && curr_Y_pos < world[curr_X_pos].length) && world[curr_Y_pos][curr_X_pos] == '+');

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