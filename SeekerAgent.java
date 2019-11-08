import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SeekerAgent extends Agent {

    private AID masterAID;

    public void setup() {
        System.out.println("Hello I am a Seeker Agent "  + getAID().getName() + "!");
        getMasterAID();
    }

    public void getMasterAID() {

        addBehaviour(new OneShotBehaviour() {
            protected void action() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("master");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    masterAID = result[0].getName();
                    System.out.println(masterAID.getName());
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }
}