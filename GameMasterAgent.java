import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.ArrayList;
import java.util.Arrays;

public class GameMasterAgent extends Agent {

    private ArrayList<AID> hiders;
    private ArrayList<AID> seekers;

    public void setup() {
        System.out.println("Hello I am the Game Master " + getAID().getName() + "!");

        registerMaster();

        getAgentsAID(false);
        getAgentsAID(true);
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

    public void getAgentsAID(boolean isSeeker) {

        addBehaviour(new WakerBehaviour(this, 5000) {
            protected void onWake() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();

                if (isSeeker)
                    sd.setType("seeker");
                else
                    sd.setType("hider");

                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    AID[] temp = new AID[result.length];

                    for (int i = 0; i < result.length; ++i) {
                        temp[i] = result[i].getName();
                    }

                    if (isSeeker)
                        seekers = new ArrayList<AID>(Arrays.asList(temp));
                    else
                        hiders = new ArrayList<AID>(Arrays.asList(temp));

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }
}