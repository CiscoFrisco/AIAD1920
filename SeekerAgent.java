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

public class SeekerAgent extends GameAgent {

    public void setup() {
        super.setup();
        registerSeeker();
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

                addBehaviour(new WaitForTurnBehaviour((GameAgent)myAgent));
            }
        }

        public boolean done() {
            return signal != null;
        }
    }
}
