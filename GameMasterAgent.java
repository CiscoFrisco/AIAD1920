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
    char[][] world;

    private int rounds;
    private int warmup;

    public void setup() {

        Object[] args = getArguments();
        world = (char[][]) args[0];

        rounds = 5;
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

                // System.out.println(myAgent.getAID().getName() + " received: " + request.getContent());

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];

                switch (header) {
                case "FOV_REQ":
                    addBehaviour(new FOVRequestsBehaviour(request, mt, content_splited));
                    break;
                case "AM_REQ":
                    addBehaviour(new AvailableMovesReplyBehaviour(request, mt, content_splited));
                    break;
                default:
                    break;
                }
            } else {
                block();
            }
        }
    }

    public class PlayBehaviour extends TickerBehaviour {

        int counter = 0;
        private boolean seekersWon;

        public PlayBehaviour() {
            super(null, 1000);
            this.seekersWon = false;
        }

        public void onTick() {
            GameMasterAgent master = (GameMasterAgent) myAgent;

            counter++;

            System.out.println(counter);

            if (counter == master.warmup) {
                addBehaviour(new SignalWarmupEndBehaviour());
            }

            if (counter <= master.warmup) {
                addBehaviour(new SignalTurnBehaviour(true));
            } else {
                addBehaviour(new SignalTurnBehaviour(false));
            }

            if (counter > master.rounds) {
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
            // System.out.println(getAID().getName() + " sended: " + inf.getContent());
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
                // System.out.println(getAID().getName() + " sended: " + inf.getContent());
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
                                // getAID().getName() + " received:" + reply.getContent() + " from " + splited[1]);
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

        public FOVRequestsBehaviour(ACLMessage request, MessageTemplate mt, String[] content) {
            super();
            this.content = content;
            this.request = request;
            this.mt = mt;
        }

        public void action() {

            fov = new FieldOfView(new Position(Integer.parseInt(content[1]), Integer.parseInt(content[2])),
                    Double.parseDouble(content[3]));
            fov.calcCellsSeen(((GameMasterAgent) myAgent).getWorld());

            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            String reply_content = "FOV;";
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
            // System.out.println(getAID().getName() + " sended:" + reply.getContent() + " to " + request.getSender().getName());
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

            // System.out.println("GameMaster " + getAID().getName() + " sended:" + reply.getContent());
        }
    }

    public char[][] getWorld() {
        return world;
    }

    public void setWorld(char[][] world) {
        this.world = world;
    }
}