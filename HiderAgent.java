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

    public void setup() {
        super.setup();
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
                // Request received4
                System.out.println(myAgent.getAID().getName() + " received: " + request.getContent() + " from " + request.getSender().getName());

                String content = request.getContent();
                content_splited = content.split(";");
                header = content_splited[0];
                switch (header) {
                case "PLAY":
                    addBehaviour(new FOVRequestBehaviour());
                    break;
                case "FOV":
                    System.out.println("FOV_LISTENER: " + myAgent.getAID().getName());
                    addBehaviour(new FOVReceiveBehaviour(content_splited));
                    break;
                case "AM":
                    addBehaviour(new AvailableMovesReceiveBehaviour(content_splited, ((HiderAgent) myAgent).getHiders()));
                    break;
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

}