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

public class HiderAgent extends GameAgent {

    private ArrayList<AID> hiders;
    private int num_replies;

    public void setup() {
        super.setup();
        this.num_replies = 0;
        registerHider();
        getHidersAID();
        addBehaviour(new ListenRequestsBehaviour());
    }

    public void registerHider() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("hider");
        sd.setName("JADE-hide-n-seek");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void getHidersAID() {

        addBehaviour(new WakerBehaviour(this, 1000) {
            protected void onWake() {

                // Make template for seekers
                DFAgentDescription template_hiders = new DFAgentDescription();
                ServiceDescription sd_hiders = new ServiceDescription();
                sd_hiders.setType("hider");
                template_hiders.addServices(sd_hiders);

                try {
                    DFAgentDescription[] result_hiders = DFService.search(myAgent, template_hiders);
                    hiders = new ArrayList<AID>();

                    for (int i = 0; i < result_hiders.length; ++i) {
                        String curr_hider = result_hiders[i].getName().getName();
                        if (!curr_hider.equals(myAgent.getAID().getName())) {
                            hiders.add(result_hiders[i].getName());
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

                // System.out.println(myAgent.getAID().getName() + " received: " +
                // request.getContent() + " from " + request.getSender().getName());

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];
                switch (header) {
                case "PLAY":
                    addBehaviour(new FOVRequestBehaviour());
                    break;
                case "FOV":
                    addBehaviour(new FOVReceiveBehaviour(content_splited));
                    break;
                case "AM":
                    addBehaviour(
                            new AvailableMovesReceiveBehaviour(content_splited, ((HiderAgent) myAgent).getHiders()));
                    break;
                case "OPPONENTS":
                    addBehaviour(new KnownSeekersReceiveBehaviour(content_splited));
                    ((HiderAgent) myAgent).setNum_replies(((HiderAgent) myAgent).getNum_replies() + 1);
                    if (((HiderAgent) myAgent).getNum_replies() == ((HiderAgent) myAgent).getHiders().size()) {
                        ((HiderAgent) myAgent).setNum_replies(0);
                    }
                default:
                    break;
                }
            } else {
                block();
            }
        }
    }

    public ArrayList<AID> getHiders() {
        return hiders;
    }

    public void setHiders(ArrayList<AID> hiders) {
        this.hiders = hiders;
    }

    public int getNum_replies() {
        return num_replies;
    }

    public void setNum_replies(int num_replies) {
        this.num_replies = num_replies;
    }

    public class KnownSeekersReceiveBehaviour extends OneShotBehaviour {

        private String[] content;

        public KnownSeekersReceiveBehaviour(String[] content) {
            super();
            this.content = content;
        }

        public void action() {

            ArrayList<Position> opponents = new ArrayList<>();

            for (int i = 1; i < content.length; i++) {
                String[] coordinates = content[i].split(",");
                opponents.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            ((HiderAgent) myAgent).addOpponents(opponents);
            ((HiderAgent) myAgent).setNum_replies(((HiderAgent) myAgent).getNum_replies() + 1);

            if (((HiderAgent) myAgent).getNum_replies() == ((HiderAgent) myAgent).getHiders().size()) {
                ((HiderAgent) myAgent).setNum_replies(0);
                Position seeker = ((HiderAgent)myAgent).getClosestOpponent();
                if(seeker != null){
                    System.out.println("CLOSEST_SEEKER: " + seeker.getX() + "|" + seeker.getY());  
                }
            }

            // ((GameAgent) myAgent).removeDuplicateOpponents();

            for(Position opponent : ((HiderAgent)myAgent).getCellsSeen()){
                System.out.println(opponent.getX() + "|" + opponent.getY() + "->" + myAgent.getAID().getName());
            }
        }
    }

}