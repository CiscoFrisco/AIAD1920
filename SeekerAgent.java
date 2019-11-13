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

    public void setup() {
        super.setup();
        registerSeeker();
        getSeekersAID();
        addBehaviour(new WarmupEndBehaviour());
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
                    AID[] temp_seekers = new AID[result_seekers.length];

                    for (int i = 0; i < result_seekers.length; ++i) {
                        if (!result_seekers[i].getName().equals(myAgent.getAID().getName())) {
                            temp_seekers[i] = result_seekers[i].getName();
                        }
                    }

                    seekers = new ArrayList<AID>(Arrays.asList(temp_seekers));

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

    public class WarmupEndBehaviour extends SimpleBehaviour {

        private ACLMessage signal;

        public void action() {
            // Receive signal from master
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            signal = myAgent.receive(mt);

            if (signal != null) {
                // Signal received
                System.out.println("Seeker " + getAID().getName() + " received: " + signal.getContent());
                ACLMessage reply = signal.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Goucha! " + getAID().getName());
                myAgent.send(reply);
                System.out.println("Seeker " + getAID().getName() + " sended: " + reply.getContent());

                addBehaviour(new WaitForTurnBehaviour((GameAgent) myAgent));
            }
        }

        public boolean done() {
            return signal != null;
        }
    }

    public class ListenSeekersBehaviour extends CyclicBehaviour {

        private int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private ACLMessage request;

        public void action() {

            switch (step) {
            case 0: // send "im listening"
                mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                request = myAgent.receive(mt);
                if (request != null) {

                    // Request received
                    String content = request.getContent();
                }
                break;
            case 1:// "receive im listening"
                step = 2;
                break;
            case 2:// send FOV
                break;
            }
        }
    }

}
