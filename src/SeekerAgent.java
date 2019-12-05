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
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SeekerAgent extends GameAgent {

    private ArrayList<AID> seekers;
    private int num_replies;
    private ArrayList<Position> knownPositions;

    public void setup() {
        super.setup();
        this.num_replies = 0;
        this.knownPositions = new ArrayList<Position>(10);

        registerSeeker();
        getSeekersAID();
        addBehaviour(new ListenRequestsBehaviour());
    }

    public void registerSeeker() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("seeker");
        sd.setName("JADE-hide-n-seek");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void getSeekersAID() {

        addBehaviour(new WakerBehaviour(this, 1000) {
            protected void onWake() {

                // Make template for seekers
                DFAgentDescription template_seekers = new DFAgentDescription();
                ServiceDescription sd_seekers = new ServiceDescription();
                sd_seekers.setType("seeker");
                template_seekers.addServices(sd_seekers);

                try {
                    DFAgentDescription[] result_seekers = DFService.search(myAgent, template_seekers);
                    seekers = new ArrayList<AID>();

                    for (int i = 0; i < result_seekers.length; ++i) {
                        String curr_seeker = result_seekers[i].getName().getName();
                        if (!curr_seeker.equals(myAgent.getAID().getName())) {
                            seekers.add(result_seekers[i].getName());
                        }
                    }

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

    public class ListenRequestsBehaviour extends CyclicBehaviour {

        private MessageTemplate mt; // The template to receive replies
        private ACLMessage request;
        private String[] content_splited;
        private String header;

        public void action() {

            mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            request = myAgent.receive(mt);
            if (request != null) {

                String senderName = request.getSender().getName();
                String file = senderName.contains("Master") ? "master" : "seekers";

                Logger.writeLog(
                        myAgent.getAID().getName() + " received: " + request.getContent() + " from " + senderName,
                        file);

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];
                switch (header) {
                case "WARM_END":
                    ((GameAgent) myAgent).setWarming(false);
                    break;
                case "PLAY":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new FOVRequestBehaviour());
                    break;
                case "FOV":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new FOVReceiveBehaviour(content_splited, ((SeekerAgent) myAgent).getSeekers()));
                    break;
                case "AM":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new SeekerMovesReceiveBehaviour(content_splited));
                    break;
                case "OPPONENTS":
                    addBehaviour(new KnownOpponentsReceiveBehaviour(content_splited));
                    ((SeekerAgent) myAgent).setNum_replies(((SeekerAgent) myAgent).getNum_replies() + 1);
                    if (((SeekerAgent) myAgent).getNum_replies() == ((SeekerAgent) myAgent).getSeekers().size()) {
                        addBehaviour(new SendReadyBehaviour());
                        ((SeekerAgent) myAgent).setNum_replies(0);
                    }
                    break;
                case "GO":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new AvailableMovesRequestBehaviour());
                    break;
                case "FINISHED_REQ":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new FinishedHandleBehaviour());
                    break;
                case "FOV_F":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new CheckWinBehaviour(content_splited));
                    break;
                case "END":
                    if (!((GameAgent) myAgent).isWarming())
                        addBehaviour(new EndAgentBehaviour());
                    break;
                default:
                    break;
                }
            } else {
                block();
            }
        }
    }

    public class CheckWinBehaviour extends OneShotBehaviour {
        private String[] content;

        public CheckWinBehaviour(String[] content) {
            super();
            this.content = content;
        }

        public void action() {
            LinkedHashSet<Position> cells = new LinkedHashSet<Position>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                cells.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            ((GameAgent) myAgent).setCellsSeen(cells);

            double distance = ((GameAgent) myAgent).getClosestOpponentDistance();
            addBehaviour(new SendCatchedHiderBehaviour(distance == 1)); // ganhei
        }
    }

    public class SendCatchedHiderBehaviour extends OneShotBehaviour {

        private boolean catched;

        public SendCatchedHiderBehaviour(boolean catched) {
            this.catched = catched;
        }

        public void action() {
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(((GameAgent) myAgent).getMasterAID());

            String content = "FINISHED;" + catched + ";";
            request.setContent(content);
            request.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());

            ((GameAgent) myAgent).send(request);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + request.getContent(), "master");
        }
    }

    public class FinishedHandleBehaviour extends OneShotBehaviour {

        public void action() {
            // send Position and Orientation to Master
            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(((GameAgent) myAgent).getMasterAID());
            request.setContent("FOV_REQ_F;" + ((GameAgent) myAgent).getPos().getX() + ";"
                    + ((GameAgent) myAgent).getPos().getY() + ";" + ((GameAgent) myAgent).getCurrOrientation());

            request.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
            ((GameAgent) myAgent).send(request);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + request.getContent(), "master");
        }
    }

    public class SeekerMovesReceiveBehaviour extends OneShotBehaviour {

        private String[] content;

        public SeekerMovesReceiveBehaviour(String[] content) {
            super();
            this.content = content;
        }

        public void action() {

            ArrayList<Position> moves = new ArrayList<>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                moves.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            moves.add(((GameAgent) myAgent).getPos());
            ((GameAgent) myAgent).setMovesAvailable(moves);

            Position hider = ((GameAgent) myAgent).getClosestOpponent();

            if (hider != null) {
                Position move = ((GameAgent) myAgent).getClosestMove(hider);
                Position pos = ((GameAgent) myAgent).getPos();
                double orientation;
                if (!move.equals(hider)) {
                    orientation = ((GameAgent) myAgent).getOrientationTo(move, hider);
                } else {
                    orientation = ((GameAgent) myAgent).getOrientationTo(pos, hider);
                }
                addBehaviour(new SendBestMoveBehaviour(move, orientation));
            } else {
                addBehaviour(new SendRandomMoveBehaviour());
            }
        }
    }

    public ArrayList<AID> getSeekers() {
        return seekers;
    }

    public void setSeekers(ArrayList<AID> seekers) {
        this.seekers = seekers;
    }

    public int getNum_replies() {
        return num_replies;
    }

    public ArrayList<Position> getKnownPositions() {
        return knownPositions;
    }

    public void setNum_replies(int num_replies) {
        this.num_replies = num_replies;
    }

    public void addPosition(Position pos) {
        if (!knownPositions.contains(pos)) {
            if (knownPositions.size() == 10) {
                knownPositions.remove(0);
            }
            knownPositions.add(pos);
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
            ((SeekerAgent) myAgent).addPosition(newPos);
            ((GameAgent) myAgent).setCurrOrientation(orientation);
            String content = "MOVE;" + oldPos.getX() + "," + oldPos.getY() + ";" + newPos.getX() + "," + newPos.getY()
                    + ";" + ((GameAgent) myAgent).getGuiOrientation() + ";";

            move.setContent(content);
            move.setConversationId("req" + ((GameAgent) myAgent).getAID().getName());
            ((GameAgent) myAgent).send(move);
            Logger.writeLog(((GameAgent) myAgent).getAID().getName() + " sent: " + move.getContent(), "master");
        }
    }

    public class SendRandomMoveBehaviour extends OneShotBehaviour {

        public void action() {

            ArrayList<Position> knownPositions = ((SeekerAgent) myAgent).getKnownPositions();
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
            ((SeekerAgent) myAgent).addPosition(newPos);

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
}
