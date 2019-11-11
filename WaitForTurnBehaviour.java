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

public class WaitForTurnBehaviour extends SimpleBehaviour {

    private ACLMessage signal;
    private GameAgent agent;

    public WaitForTurnBehaviour(GameAgent agent){
        super();
        this.agent = agent;
    }

    public void action() {
        // Receive signal from master
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        signal = myAgent.receive(mt);

        if (signal != null) {
            // Signal received
            if(signal.getContent().equals("Your Turn")){
                agent.addBehaviour(new PlayTurnBehaviour(agent));
            }
        }
    }

    public boolean done() {
        return signal != null && signal.getContent().equals("Your Turn");
    }
}