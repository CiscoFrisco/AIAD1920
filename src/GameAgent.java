import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameAgent extends Agent {

    private AID masterAID;
    private boolean warming;

    Position pos;
    boolean isGrabbing;
    double currOrientation;
    private LinkedHashSet<Position> cellsSeen;
    private ArrayList<Position> moves;
    private double lyingProbability;
    private ArrayList<Position> knownPositions;

    public void setup() {

        Object[] args = getArguments();
        pos = (Position) args[0];
        lyingProbability = (double) args[1];
        isGrabbing = false;
        currOrientation = 0;
        cellsSeen = new LinkedHashSet<Position>();
        moves = new ArrayList<Position>();
        warming = true;
        knownPositions = new ArrayList<Position>(10);

        initMasterAID();
    }

    public boolean isWarming() {
        return warming;
    }

    public ArrayList<Position> getKnownPositions() {
        return knownPositions;
    }

    public void addPosition(Position pos) {
        if (!knownPositions.contains(pos)) {
            if (knownPositions.size() == 10) {
                knownPositions.remove(0);
            }
            knownPositions.add(pos);
        }
    }

    public void setWarming(boolean warming) {
        this.warming = warming;
    }

    public void addOpponents(ArrayList<Position> opponents) {
        for (Position opponent : opponents)
            this.cellsSeen.add(opponent);
    }

    public void removeDuplicateOpponents() {

        LinkedHashSet<Position> newList = new LinkedHashSet<Position>();
        // Traverse through the first list
        for (Position pos : this.cellsSeen) {

            if (!newList.contains(pos)) {
                newList.add(pos);
            }
        }

        // return the new list
        this.cellsSeen = newList;
    }

    public void initMasterAID() {

        addBehaviour(new WakerBehaviour(this, 1000) {
            public void onWake() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("master");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    masterAID = result[0].getName();
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public double getCurrOrientation() {
        return currOrientation;
    }

    public void setCurrOrientation(double currOrientation) {
        this.currOrientation = currOrientation;
    }

    public double getGuiOrientation() {
        double guiOrientation;

        if (currOrientation > Math.toRadians(45) && currOrientation < Math.toRadians(135)) {
            guiOrientation = 90;
        } else if (currOrientation >= Math.toRadians(135) && currOrientation <= Math.toRadians(225)) {
            guiOrientation = 180;
        } else if (currOrientation > Math.toRadians(225) && currOrientation < Math.toRadians(315)) {
            guiOrientation = 270;
        } else {
            guiOrientation = 0;
        }

        // System.out.println("--> curr orientation: " +
        // Math.toDegrees(this.currOrientation));
        // System.out.println("--> gui orientation: " + guiOrientation);
        return guiOrientation;
    }

    public double getNextRandomOrientation() {
        double randomOri;

        randomOri = this.getGuiOrientation();
        randomOri += 90;
        if (randomOri == 360)
            randomOri = 0;
        return Math.toRadians(randomOri);
    }

    public LinkedHashSet<Position> getCellsSeen() {
        return cellsSeen;
    }

    public void setCellsSeen(LinkedHashSet<Position> cellsSeen) {
        this.cellsSeen = cellsSeen;
    }

    public ArrayList<Position> getMovesAvailable() {
        return moves;
    }

    public void setMovesAvailable(ArrayList<Position> moves) {
        this.moves = moves;
    }

    public AID getMasterAID() {
        return masterAID;
    }

    public void setMasterAID(AID masterAID) {
        this.masterAID = masterAID;
    }

    public Position getClosestOpponent() {

        double min_distance = 999;
        Position closest = null;

        for (Position opponent : cellsSeen) {
            double distance = getDistance(opponent, pos);
            if (distance <= min_distance) {
                min_distance = distance;
                closest = opponent;
            }
        }

        return closest;
    }

    public double getClosestOpponentDistance() {

        double min_distance = 999;
        Position closest = null;

        for (Position opponent : cellsSeen) {
            double distance = getDistance(opponent, pos);
            if (distance <= min_distance) {
                min_distance = distance;
                closest = opponent;
            }
        }

        return min_distance;
    }

    public Position getClosestMove(Position opponent) {

        Position move = null;
        double min_distance = 999;

        for (Position pos : moves) {
            double distance = getDistance(opponent, pos);
            if (distance <= min_distance) {
                move = pos;
                min_distance = distance;
            }
        }

        return move;
    }

    public Position getFurthestMove(Position opponent) {

        Position move = null;
        double max_distance = 0;

        for (Position pos : moves) {
            double distance = getDistance(opponent, pos);
            if (distance >= max_distance) {
                move = pos;
                max_distance = distance;
            }
        }

        return move;
    }

    public double getOrientationTo(Position origin, Position destiny) {
        double deltaX = destiny.getX() - origin.getX();
        double deltaY = destiny.getY() - origin.getY();

        double h = this.getDistance(origin, destiny);
        double co = Math.abs(deltaY);
        double angle = Math.asin(co / h);

        double orientation;
        if (deltaX >= 0 && deltaY < 0) {
            orientation = angle;
        } else if (deltaX < 0 && deltaY <= 0) {
            orientation = Math.PI - angle;
        } else if (deltaX <= 0 && deltaY > 0) {
            orientation = Math.PI + angle;
        } else {
            orientation = 2 * Math.PI - angle;
        }

        // System.out.println("--> agent: " + this.getAID().getName());
        // System.out.println("--> origin: " + origin.getX() + " , " + origin.getY());
        // System.out.println("--> destiny: " + destiny.getX() + " , " +
        // destiny.getY());
        // System.out.println("--> oritentation: " + Math.toDegrees(orientation));

        return orientation;
    }

    public Position getMaxKnownXY() {
        int maxX = 0;
        int maxY = 0;

        for (Position pos : knownPositions) {
            if (pos.getX() > maxX)
                maxX = pos.getX();

            if (pos.getY() > maxY)
                maxY = pos.getY();
        }

        return new Position(maxX, maxY);
    }

    public double getDistance(Position p1, Position p2) {
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public class FOVRequestBehaviour extends OneShotBehaviour {

        public void action() {
            // send Position and Orientation to Master
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(((GameAgent) myAgent).getMasterAID());
            request.setContent("FOV_REQ;" + ((GameAgent) myAgent).getPos().getX() + ";"
                    + ((GameAgent) myAgent).getPos().getY() + ";" + ((GameAgent) myAgent).getCurrOrientation());

            request.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
            ((GameAgent) myAgent).send(request);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + request.getContent(), "master");
        }
    }

    public class AvailableMovesRequestBehaviour extends OneShotBehaviour {

        public void action() {
            // send Position to Master
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(((GameAgent) myAgent).getMasterAID());

            request.setContent("AM_REQ;" + ((GameAgent) myAgent).getPos().getX() + ";"
                    + ((GameAgent) myAgent).getPos().getY() + ";");

            request.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
            ((GameAgent) myAgent).send(request);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + request.getContent(), "master");
        }
    }

    public class FOVReceiveBehaviour extends OneShotBehaviour {

        private String[] content;
        private ArrayList<AID> partnersAID;

        public FOVReceiveBehaviour(String[] content, ArrayList<AID> partnersAID) {
            super();
            this.partnersAID = partnersAID;
            this.content = content;
        }

        public void action() {
            LinkedHashSet<Position> cells = new LinkedHashSet<Position>();
            ArrayList<Position> cellsArray = new ArrayList<Position>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                cells.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
                cellsArray.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            if (!((GameAgent) myAgent).isWarming()) {
                ((GameAgent) myAgent).setCellsSeen(cells);
            } else {
                ((GameAgent) myAgent).addOpponents(cellsArray);
            }

            if (this.partnersAID.size() > 0) {
                addBehaviour(new PositionRequestBehaviour(this.partnersAID));
            } else {
                addBehaviour(new SendReadyBehaviour());
            }

        }
    }

    public class PositionRequestBehaviour extends OneShotBehaviour {

        private ArrayList<AID> opponentAID;

        public PositionRequestBehaviour(ArrayList<AID> opponentAID) {
            super();
            this.opponentAID = opponentAID;
        }

        public void action() {
            // send Position and Orientation to Master
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            String name = ((GameAgent) myAgent).getAID().getName();

            for (int i = 0; i < this.opponentAID.size(); ++i) {
                request.addReceiver(this.opponentAID.get(i));
            }

            String content = "OPPONENTS;";

            double randomNum = ThreadLocalRandom.current().nextDouble(0, 100 + 1);
            System.out.println(randomNum);
            if (randomNum < lyingProbability) {
                request.addReceiver(((GameAgent) myAgent).getMasterAID());
                Position max = ((GameAgent) myAgent).getMaxKnownXY();

                int x = ThreadLocalRandom.current().nextInt(0, max.getX() + 1);
                int y = ThreadLocalRandom.current().nextInt(0, max.getY() + 1);
                content += x + "," + y + ";";
            } else {
                LinkedHashSet<Position> hiders_seen = ((GameAgent) myAgent).getCellsSeen();

                for (Position hider : hiders_seen) {
                    content += hider.getX() + "," + hider.getY() + ";";
                }
            }

            request.setContent(content);
            request.setConversationId("req" + name);
            request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
            ((GameAgent) myAgent).send(request);

            String file = name.contains("Seeker") ? "seekers" : "hiders";
            Logger.writeLog(name + " sent: " + request.getContent(), file);
        }
    }

    public class SendBestMoveBehaviour extends OneShotBehaviour {

        private Position newPos;
        private double orientation;

        public SendBestMoveBehaviour(Position move, double orientation) {
            super();
            this.newPos = move;
            this.orientation = orientation;
        }

        public void action() {
            // send Position and Orientation to Master
            ACLMessage move = new ACLMessage(ACLMessage.INFORM);
            move.addReceiver(((GameAgent) myAgent).getMasterAID());

            Position oldPos = ((GameAgent) myAgent).getPos();

            // update Agent Position and Orientation
            ((GameAgent) myAgent).setPos(newPos);
            ((GameAgent) myAgent).addPosition(newPos);

            ((GameAgent) myAgent).setCurrOrientation(orientation);
            String content = "MOVE;" + oldPos.getX() + "," + oldPos.getY() + ";" + newPos.getX() + "," + newPos.getY()
                    + ";" + ((GameAgent) myAgent).getGuiOrientation() + ";";

            move.setContent(content);
            move.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            ((GameAgent) myAgent).send(move);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + move.getContent(), "master");
        }
    }

    public class KnownOpponentsReceiveBehaviour extends OneShotBehaviour {

        private String[] content;

        public KnownOpponentsReceiveBehaviour(String[] content) {
            super();
            this.content = content;
        }

        public void action() {

            ArrayList<Position> opponents = new ArrayList<>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                opponents.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            ((GameAgent) myAgent).addOpponents(opponents);
            // ((GameAgent) myAgent).removeDuplicateOpponents();
        }
    }

    public class SendRandomMoveBehaviour extends OneShotBehaviour {

        public void action() {

            ArrayList<Position> knownPositions = ((GameAgent) myAgent).getKnownPositions();
            ArrayList<Position> availableMoves = ((GameAgent) myAgent).getMovesAvailable();
            ArrayList<Position> c = new ArrayList<>(availableMoves);
            c.removeAll(knownPositions);
            Position newPos = new Position(0, 0);

            if (c.isEmpty()) {
                knownPositions.retainAll(availableMoves);
                newPos = knownPositions.get(0);
            } else {
                // calc random move
                int random_limit = c.size();
                int selectedRandom = ThreadLocalRandom.current().nextInt(0, random_limit);
                newPos = c.get(selectedRandom);
            }

            // send Position and Orientation to Master
            ACLMessage move = new ACLMessage(ACLMessage.INFORM);
            move.addReceiver(((GameAgent) myAgent).getMasterAID());

            Position oldPos = ((GameAgent) myAgent).getPos();

            // update Agent Position and Orientation
            ((GameAgent) myAgent).setPos(newPos);
            ((GameAgent) myAgent).addPosition(newPos);

            double nextOrientation;

            if (!oldPos.equals(newPos)) {
                nextOrientation = ((GameAgent) myAgent).getOrientationTo(oldPos, newPos);
            } else {
                nextOrientation = ((GameAgent) myAgent).getNextRandomOrientation();
            }

            ((GameAgent) myAgent).setCurrOrientation(nextOrientation);
            String content = "MOVE;" + oldPos.getX() + "," + oldPos.getY() + ";" + newPos.getX() + "," + newPos.getY()
                    + ";" + ((GameAgent) myAgent).getGuiOrientation() + ";";

            move.setContent(content);
            move.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            ((GameAgent) myAgent).send(move);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + move.getContent(), "master");
        }
    }

    public class SendReadyBehaviour extends OneShotBehaviour {

        public void action() {
            // send Position and Orientation to Master
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(((GameAgent) myAgent).getMasterAID());
            request.setContent("READY;");
            request.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            ((GameAgent) myAgent).send(request);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + request.getContent(), "master");
        }
    }

    public class EndAgentBehaviour extends OneShotBehaviour {
        public void action() {
            try {
                DFService.deregister((GameAgent) myAgent);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            ((GameAgent) myAgent).doDelete();
        }
    }
}
