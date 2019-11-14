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

public class SeekerAgent extends GameAgent {

    private ArrayList<AID> seekers;
    private boolean warming;
    private int num_replies;

    public void setup() {
        super.setup();
        this.num_replies = 0;
        this.warming = true;
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
                // Request received4
                System.out.println(myAgent.getAID().getName() + " received: " + request.getContent() + " from " + request.getSender().getName());

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];
                switch (header) {
                case "WARM_END":
                    ((SeekerAgent) myAgent).setWarming(false);
                    break;
                case "PLAY":
                    if (!((SeekerAgent) myAgent).isWarming())
                        addBehaviour(new FOVRequestBehaviour());
                    break;
                case "FOV":
                    System.out.println("FOV_LISTENER: " + myAgent.getAID().getName());
                    if (!((SeekerAgent) myAgent).isWarming())
                        addBehaviour(new FOVReceiveBehaviour(content_splited));
                    break;
                case "AM":
                    if (!((SeekerAgent) myAgent).isWarming())
                        addBehaviour(new AvailableMovesReceiveBehaviour(content_splited, ((SeekerAgent) myAgent).getSeekers()));
                    break;
                case "OPPONENTS":
                    addBehaviour(new PositionReceiveBehaviour(content_splited));
                    ((SeekerAgent) myAgent).setNum_replies(((SeekerAgent) myAgent).getNum_replies() + 1);
                    if(((SeekerAgent) myAgent).getNum_replies() == ((SeekerAgent) myAgent).getSeekers().size()){
                        System.out.println("WUT: " + ((SeekerAgent) myAgent).getNum_replies());
                        ((SeekerAgent) myAgent).setNum_replies(0);
                    }
                default:
                    break;
                }
            } else {
                block();
            }
        }
    }

    public boolean isWarming() {
        return warming;
    }

    public void setWarming(boolean warming) {
        this.warming = warming;
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

    public void setNum_replies(int num_replies) {
        this.num_replies = num_replies;
    }
}
