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
        
        registerSeeker();
        getMasterAID();
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

    public void getMasterAID() {

        addBehaviour(new OneShotBehaviour() {
            public void action() {
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