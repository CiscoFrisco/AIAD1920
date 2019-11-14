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

public class GameAgent extends Agent {

    private AID masterAID;

    Position pos;
    boolean isGrabbing;
    double currOrientation;
    private LinkedHashSet<Position> cellsSeen;
    private ArrayList<Position> moves;

    public void setup() {

        Object[] args = getArguments();
        pos = (Position) args[0];
        isGrabbing = false;
        currOrientation = 0;
        cellsSeen = new LinkedHashSet<Position>();
        moves = new ArrayList<Position>();

        initMasterAID();
    }

    public void initMasterAID() {

        addBehaviour(new OneShotBehaviour() {
            public void action() {
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
            System.out.println("Agent" + ((GameAgent) myAgent).getAID().getName() + " sended: " + request.getContent());
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
            System.out.println("Agent" + ((GameAgent) myAgent).getAID().getName() + " sended: " + request.getContent());
        }
    }

    public class FOVReceiveBehaviour extends SimpleBehaviour {

        private String[] content;
        private boolean received;

        public FOVReceiveBehaviour(String[] content) {
            super();
            this.content = content;
            this.received = false;
        }

        public void action() {

            LinkedHashSet<Position> cells = new LinkedHashSet<Position>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                cells.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            ((GameAgent) myAgent).setCellsSeen(cells);
            this.received = true;
        }

        public boolean done() {
            return this.received;
        }
    }

    public class AvailableMovesReceiveBehaviour extends SimpleBehaviour {

        private String[] content;
        private boolean received;

        public AvailableMovesReceiveBehaviour(String[] content) {
            super();
            this.content = content;
            this.received = false;
        }

        public void action() {

            ArrayList<Position> moves = new ArrayList<>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                moves.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            ((GameAgent) myAgent).setMovesAvailable(moves);
            this.received = true;
        }

        public boolean done() {
            return this.received;
        }
    }

    public class RequestInformationBehaviour extends OneShotBehaviour {

        public void action() {
            addBehaviour(new FOVRequestBehaviour());
            addBehaviour(new AvailableMovesRequestBehaviour());
        }
    }

}
