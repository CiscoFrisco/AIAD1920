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
    private enum state {WARMUP, PLAY, HIDERS_WIN, SEEKERS_WIN, END_GAME}

    private ArrayList<AID> hiders;
    private ArrayList<AID> seekers;
    char[][] world;

    private state gameState;
    private int rounds;
    private int warmup;


    public void setup() {

        Object[] args = getArguments();
        world = (char[][]) args[0];

        gameState = state.WARMUP;
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
                    addBehaviour(new FOVRequestsBehaviour());

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

    public class PlayBehaviour extends TickerBehaviour {
        int counter = 0;

        public PlayBehaviour() {
            super(null, 1000);
        }

        public void onTick() {
            GameMasterAgent master = (GameMasterAgent) myAgent;

            counter++;

            System.out.println(counter + ": " + master.gameState);

            switch (master.gameState) {
                case WARMUP:
                    if (counter == master.warmup) {
                        addBehaviour(new SignalWarmupEndBehaviour());
                        master.gameState = state.PLAY;
                    }
                break;

                case PLAY:
                    if (counter > master.rounds) {
                        master.gameState = state.HIDERS_WIN;
                    }
                    else {
                        addBehaviour(new SignalTurnBehaviour());
                    }
                break;

                case HIDERS_WIN:
                    System.out.println("Hiders win!");
                    master.gameState = state.END_GAME;
                break;

                case SEEKERS_WIN:
                    System.out.println("Seekers win!");
                    master.gameState = state.END_GAME;
                break;

                case END_GAME:
                    System.out.println("Game ended!");
                    stop();
                break;

            }
        }
    }

    public class SignalTurnBehaviour extends OneShotBehaviour {

        public void action() {

            ACLMessage inf = new ACLMessage(ACLMessage.INFORM);

            ArrayList<AID> seekers = ((GameMasterAgent) myAgent).seekers;
            ArrayList<AID> hiders = ((GameMasterAgent) myAgent).hiders;

            if (((GameMasterAgent) myAgent).gameState == state.PLAY) {
                for (int i = 0; i < seekers.size(); ++i) {
                    inf.addReceiver(seekers.get(i));
                }
            }

            for (int i = 0; i < hiders.size(); ++i) {
                inf.addReceiver(hiders.get(i));
            }

            inf.setContent("Your Turn");
            inf.setConversationId("signal-turn");
            inf.setReplyWith("inf" + System.currentTimeMillis()); // Unique value
            myAgent.send(inf);
            System.out.println("GameMaster" + getAID().getName() + " sended: " + inf.getContent());
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
                inf.setContent("Warmup Ended");
                inf.setConversationId("signal-warmup");
                inf.setReplyWith("inf" + System.currentTimeMillis()); // Unique value
                myAgent.send(inf);
                System.out.println("GameMaster" + getAID().getName() + " sended: " + inf.getContent());
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
                        System.out.println("GameMaster " + getAID().getName() + " received:" + reply.getContent()
                                + " from " + splited[1]);
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

    public class FOVRequestsBehaviour extends CyclicBehaviour {

        private int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private ACLMessage request;
        private FieldOfView fov;
        private String[] splited = new String[100];

        public void action() {

            switch (step) {
            case 0: // listen for request
                mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                request = myAgent.receive(mt);
                if (request != null) {
                    
                    // Request received
                    String content = request.getContent();
                    splited = content.split("\\s+");
                    step = 1;
                }
                break;
            case 1:// calc FOV
                fov = new FieldOfView(new Position(Integer.parseInt(splited[0]), Integer.parseInt(splited[1])),
                        Double.parseDouble(splited[2]));
                fov.calcCellsSeen(((GameMasterAgent) myAgent).getWorld());
                step = 2;
                break;
            case 2:// send FOV
                // construct message
                ACLMessage reply = request.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                
                String content = "";
                for (Position cell : fov.getCellsSeen()) {
                    content += cell.x + "," + cell.y + ";";
                }

                reply.setContent(content);
                ((GameMasterAgent)myAgent).send(reply);
                System.out.println("GameMaster " + getAID().getName() + " sended:" + reply.getContent());
                step = 0;
                break;
            }
        }
    }

    public char[][] getWorld() {
        return world;
    }

    public void setWorld(char[][] world) {
        this.world = world;
    }
}