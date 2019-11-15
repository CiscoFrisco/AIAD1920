import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.ArrayList;
import java.util.Arrays;

public class GameMasterAgent extends Agent {

    private ArrayList<AID> hiders;
    private ArrayList<AID> seekers;
    private int agents_ready;
    char[][] world;
    private boolean waiting_move;

    private int rounds;
    private int warmup;
    private int counter;

    public void setup() {

        Object[] args = getArguments();
        world = (char[][]) args[0];
        waiting_move = true;
        agents_ready = 0;
        counter = 0;
        printWorld();
        System.out.println("\n");
        rounds = 50;
        warmup = (int) Math.floor(0.4 * rounds);

        registerMaster();
        getAgentsAID();
    }

    public void registerMaster() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("master");
        sd.setName("JADE-hide-n-seek");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void getAgentsAID() {

        addBehaviour(new WakerBehaviour(this, 1000) {
            protected void onWake() {

                // Make template for hiders
                DFAgentDescription template_hiders = new DFAgentDescription();
                ServiceDescription sd_hiders = new ServiceDescription();
                sd_hiders.setType("hider");
                template_hiders.addServices(sd_hiders);

                // Make template for seekers
                DFAgentDescription template_seekers = new DFAgentDescription();
                ServiceDescription sd_seekers = new ServiceDescription();
                sd_seekers.setType("seeker");
                template_seekers.addServices(sd_seekers);

                try {
                    DFAgentDescription[] result_hiders = DFService.search(myAgent, template_hiders);
                    DFAgentDescription[] result_seekers = DFService.search(myAgent, template_seekers);

                    AID[] temp_hiders = new AID[result_hiders.length];
                    AID[] temp_seekers = new AID[result_seekers.length];

                    for (int i = 0; i < result_hiders.length; ++i) {
                        temp_hiders[i] = result_hiders[i].getName();
                    }

                    for (int i = 0; i < result_seekers.length; ++i) {
                        temp_seekers[i] = result_seekers[i].getName();
                    }

                    seekers = new ArrayList<AID>(Arrays.asList(temp_seekers));
                    hiders = new ArrayList<AID>(Arrays.asList(temp_hiders));

                    addBehaviour(new PlayBehaviour());
                    addBehaviour(new ListenRequestsBehaviour());

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
                // Request received

                // System.out.println(myAgent.getAID().getName() + " received: " +
                // request.getContent());

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];

                switch (header) {
                case "FOV_REQ":
                    addBehaviour(new FOVRequestsBehaviour(request, mt, content_splited, false));
                    break;
                case "FOV_REQ_F":
                    addBehaviour(new FOVRequestsBehaviour(request, mt, content_splited, true));
                    break;
                case "AM_REQ":
                    addBehaviour(new AvailableMovesReplyBehaviour(request, mt, content_splited));
                    break;
                case "MOVE":
                    addBehaviour(new MoveHandleBehaviour(content_splited, request.getSender()));
                    break;
                case "FINISHED":
                    addBehaviour(new CheckFinishedBehaviour(content_splited));
                    break;
                case "READY":
                    if (((GameMasterAgent) myAgent).getCounter() > ((GameMasterAgent) myAgent).getWarmup())
                        addBehaviour(new UpdateReadyAgentsBehaviour(false));
                    else {
                        addBehaviour(new UpdateReadyAgentsBehaviour(true));
                    }
                    break;
                default:
                    break;
                }
            } else {
                block();
            }
        }
    }

    public class CheckFinishedBehaviour extends OneShotBehaviour {

        private boolean finished;

        public CheckFinishedBehaviour(String[] content) {
            finished = Boolean.parseBoolean(content[1]);
        }

        public void action() {
            if (finished) {
                ((GameMasterAgent) myAgent).printWorld();
                System.out.println("SEEKERS WON");
                addBehaviour(new SendEndGameBehaviour()); // send end of game to every agent
            } else {
                ((GameMasterAgent) myAgent).setWaiting_move(false);
            }
        }
    }

    public class SendEndGameBehaviour extends OneShotBehaviour {

        public void action() {

            ACLMessage request = new ACLMessage(ACLMessage.INFORM);

            for (AID hider : hiders) {
                request.addReceiver(hider);
            }

            for (AID seeker : seekers) {
                request.addReceiver(seeker);
            }

            String content = "END;";
            request.setContent(content);
            request.setConversationId("req" + ((GameMasterAgent) myAgent).getAID().getName());

            ((GameMasterAgent) myAgent).send(request);
            // System.out.println("GameMaster " + getAID().getName() + " sended: " +
            // inf.getContent());

            addBehaviour(new EndMasterBehaviour());
        }
    }

    public class EndMasterBehaviour extends OneShotBehaviour {
        public void action() {
            ((GameMasterAgent) myAgent).doDelete();
        }
    }

    public class UpdateReadyAgentsBehaviour extends OneShotBehaviour {

        private boolean warming;

        public UpdateReadyAgentsBehaviour(boolean warming) {
            super();
            this.warming = warming;
        }

        public void action() {
            ((GameMasterAgent) myAgent).incReady();

            int num_seekers = ((GameMasterAgent) myAgent).getSeekers().size();
            int num_hiders = ((GameMasterAgent) myAgent).getHiders().size();

            int ready = ((GameMasterAgent) myAgent).getAgents_ready();

            if (!warming) {
                if (agents_ready == num_seekers + num_hiders) {
                    addBehaviour(new MoveByTurnBehaviour(((GameMasterAgent) myAgent).getHiders(),
                            ((GameMasterAgent) myAgent).getSeekers()));
                    ((GameMasterAgent) myAgent).resetReady();
                }
            } else {
                if (agents_ready == num_hiders) {
                    addBehaviour(new MoveByTurnBehaviour(((GameMasterAgent) myAgent).getHiders()));
                    ((GameMasterAgent) myAgent).resetReady();
                }
            }
        }
    }

    public class MoveByTurnBehaviour extends SimpleBehaviour {

        private ArrayList<AID> gameAgents;
        private int agentsProcessed;
        private int state;

        private MoveByTurnBehaviour(ArrayList<AID> hiders, ArrayList<AID> seekers) {
            this.gameAgents = new ArrayList<AID>();
            this.gameAgents.addAll(hiders);
            this.gameAgents.addAll(seekers);
            this.agentsProcessed = 0;
            this.state = 0;
        }

        private MoveByTurnBehaviour(ArrayList<AID> hiders) {
            this.gameAgents = new ArrayList<AID>();
            this.gameAgents.addAll(hiders);
            this.agentsProcessed = 0;
            this.state = 0;
        }

        public void action() {
            switch (state) {
            case 0:
                ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
                inf.addReceiver(gameAgents.get(agentsProcessed));
                inf.setContent("GO;");
                inf.setConversationId("go-turn");
                myAgent.send(inf);
                // System.out.println("GameMaster " + getAID().getName() + " sended: " +
                // inf.getContent());
                this.state = 1;
                break;
            case 1:
                if (!((GameMasterAgent) myAgent).isWaiting_move()) {
                    this.agentsProcessed++;
                    this.state = 0;
                    ((GameMasterAgent) myAgent).setWaiting_move(true);
                }
                break;
            }
        }

        public boolean done() {
            return agentsProcessed == gameAgents.size();
        }
    }

    public class PlayBehaviour extends TickerBehaviour {

        private boolean seekersWon;

        public PlayBehaviour() {
            super(null, 1000);
            this.seekersWon = false;
        }

        public void onTick() {
            GameMasterAgent master = (GameMasterAgent) myAgent;

            master.setCounter(master.getCounter() + 1);
            System.out.println(master.getCounter() + " out of " + master.rounds + "\n");
            master.printWorld();

            if (master.getCounter() == master.warmup) {
                addBehaviour(new SignalWarmupEndBehaviour());
            }

            if (master.getCounter() <= master.warmup) {
                addBehaviour(new SignalTurnBehaviour(true));
            } else {
                addBehaviour(new SignalTurnBehaviour(false));
            }

            if (master.getCounter() > master.rounds) {
                System.out.println("HIDERS WON");
                addBehaviour(new SendEndGameBehaviour());
                stop();
            }
        }
    }

    public class SignalTurnBehaviour extends OneShotBehaviour {

        private boolean isWarmup;

        public SignalTurnBehaviour(boolean isWarmup) {
            this.isWarmup = isWarmup;
        }

        public void action() {

            ACLMessage inf = new ACLMessage(ACLMessage.INFORM);

            ArrayList<AID> seekers = ((GameMasterAgent) myAgent).seekers;
            ArrayList<AID> hiders = ((GameMasterAgent) myAgent).hiders;

            if (!this.isWarmup) {
                for (int i = 0; i < seekers.size(); ++i) {
                    inf.addReceiver(seekers.get(i));
                }
            }

            for (int i = 0; i < hiders.size(); ++i) {
                inf.addReceiver(hiders.get(i));
            }

            inf.setContent("PLAY;");
            inf.setConversationId("signal-turn");
            inf.setReplyWith("inf" + System.currentTimeMillis()); // Unique value
            myAgent.send(inf);
            // System.out.println("GameMaster " + getAID().getName() + " sended: " +
            // inf.getContent());

        }
    }

    public class SignalWarmupEndBehaviour extends SimpleBehaviour {

        private int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private int num_replies;
        private int num_seekers;

        public void action() {
            switch (step) {
            case 0:
                num_seekers = seekers.size();
                // Send the cfp to all sellers
                ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
                for (int i = 0; i < seekers.size(); ++i) {
                    inf.addReceiver(seekers.get(i));
                }
                inf.setContent("WARM_END");
                inf.setConversationId("signal-warmup");
                inf.setReplyWith("inf" + System.currentTimeMillis()); // Unique value
                myAgent.send(inf);
                // System.out.println("GameMaster " + getAID().getName() + " sended: " +
                // inf.getContent());
                // Prepare the template to get acknowledgements
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("signal-warmup"),
                        MessageTemplate.MatchInReplyTo(inf.getReplyWith()));
                step = 1;
                break;
            case 1:
                // Receive all acknowledgements from seeker agents
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {

                    // Reply received
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        num_replies++;
                        String content = reply.getContent();
                        String[] splited = content.split("\\s+");
                        // System.out.println(
                        // getAID().getName() + " received:" + reply.getContent() + " from " +
                        // splited[1]);
                    }
                } else {
                    block();
                }
                break;
            }
        }

        public boolean done() {
            return (step == 1 && num_seekers == num_replies);
        }
    }

    public class FOVRequestsBehaviour extends OneShotBehaviour {

        private MessageTemplate mt; // The template to receive replies
        private ACLMessage request;
        private FieldOfView fov;
        private String[] content;
        private boolean finished;

        public FOVRequestsBehaviour(ACLMessage request, MessageTemplate mt, String[] content, boolean finished) {
            super();
            this.content = content;
            this.request = request;
            this.mt = mt;
            this.finished = finished;
        }

        public void action() {

            fov = new FieldOfView(new Position(Integer.parseInt(content[1]), Integer.parseInt(content[2])),
                    Double.parseDouble(content[3]));
            fov.calcCellsSeen(((GameMasterAgent) myAgent).getWorld());

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String reply_content = "";

            if (finished)
                reply_content += "FOV_F;";
            else
                reply_content += "FOV;";

            int x = Integer.parseInt(content[1]);
            int y = Integer.parseInt(content[2]);

            for (Position cell : fov.getCellsSeen()) {
                if (world[y][x] == 'S' && world[cell.y][cell.x] == 'H')
                    reply_content += cell.x + "," + cell.y + ";";
                else if (world[y][x] == 'H' && world[cell.y][cell.x] == 'S')
                    reply_content += cell.x + "," + cell.y + ";";
            }

            reply.setContent(reply_content);
            ((GameMasterAgent) myAgent).send(reply);
            // System.out.println("GameMaster " + getAID().getName() + " sended: " +
            // inf.getContent());
        }
    }

    public class AvailableMovesReplyBehaviour extends OneShotBehaviour {

        private MessageTemplate mt; // The template to receive replies
        private ACLMessage request;
        private String[] content;
        private ArrayList<Position> available;

        public AvailableMovesReplyBehaviour(ACLMessage request, MessageTemplate mt, String[] content) {
            super();
            this.content = content;
            this.request = request;
            this.mt = mt;
            available = new ArrayList<Position>();
        }

        public void action() {

            int x = Integer.parseInt(content[1]);
            int y = Integer.parseInt(content[2]);

            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if ((i >= 0 && i < world.length) && (j >= 0 && j < world[i].length)) {
                        if (world[j][i] == '+') {
                            available.add(new Position(i, j));

                        }
                    }
                }
            }

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String reply_content = "AM;";

            for (Position move : available) {
                reply_content += move.x + "," + move.y + ";";
            }

            reply.setContent(reply_content);
            ((GameMasterAgent) myAgent).send(reply);
            // System.out.println("GameMaster " + getAID().getName() + " sended: " +
            // inf.getContent());
        }
    }

    public class MoveHandleBehaviour extends OneShotBehaviour {

        private String[] content;
        private AID seekerAID;

        public MoveHandleBehaviour(String[] content, AID seekerAID) {
            super();
            this.content = content;
            this.seekerAID = seekerAID;
        }

        public void action() {

            String[] oldPos_s = content[1].split(",");
            String[] newPos_s = content[2].split(",");

            Position oldPos = new Position(Integer.parseInt(oldPos_s[0]), Integer.parseInt(oldPos_s[1]));
            Position newPos = new Position(Integer.parseInt(newPos_s[0]), Integer.parseInt(newPos_s[1]));

            char[][] new_world = ((GameMasterAgent) myAgent).getWorld();

            char agent = new_world[oldPos.y][oldPos.x];

            new_world[oldPos.y][oldPos.x] = '+';
            new_world[newPos.y][newPos.x] = agent;

            ((GameMasterAgent) myAgent).setWorld(new_world);

            System.out.println("\n");

            if (agent == 'S') {
                addBehaviour(new FinishedRequestBehaviour(seekerAID));
            } else {
                ((GameMasterAgent) myAgent).setWaiting_move(false);
            }
        }
    }

    public class FinishedRequestBehaviour extends OneShotBehaviour {

        private AID target;

        public FinishedRequestBehaviour(AID target) {
            super();
            this.target = target;
        }

        public void action() {

            ACLMessage request = new ACLMessage(ACLMessage.INFORM);
            request.addReceiver(target);

            String content = "FINISHED_REQ;";
            request.setContent(content);
            request.setConversationId("req" + ((GameMasterAgent) myAgent).getAID().getName());

            ((GameMasterAgent) myAgent).send(request);
            // System.out.println("GameMaster " + getAID().getName() + " sended: " +
            // inf.getContent());
        }
    }

    public char[][] getWorld() {
        return world;
    }

    public void setWorld(char[][] world) {
        this.world = world;
    }

    public void incReady() {
        this.agents_ready++;
    }

    public void resetReady() {
        this.agents_ready = 0;
    }

    public void printWorld() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                System.out.print(" | " + world[i][j]);
            }
            System.out.print(" |\n");
        }
    }

    public ArrayList<AID> getHiders() {
        return hiders;
    }

    public void setHiders(ArrayList<AID> hiders) {
        this.hiders = hiders;
    }

    public ArrayList<AID> getSeekers() {
        return seekers;
    }

    public void setSeekers(ArrayList<AID> seekers) {
        this.seekers = seekers;
    }

    public int getAgents_ready() {
        return agents_ready;
    }

    public void setAgents_ready(int agents_ready) {
        this.agents_ready = agents_ready;
    }

    public boolean isWaiting_move() {
        return waiting_move;
    }

    public void setWaiting_move(boolean waiting_move) {
        this.waiting_move = waiting_move;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getWarmup() {
        return warmup;
    }

    public void setWarmup(int warmup) {
        this.warmup = warmup;
    }
}