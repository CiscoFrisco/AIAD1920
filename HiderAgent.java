import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Arrays;

public class HiderAgent extends GameAgent {

    private ArrayList<AID> seekers;

    public void setup() {
        super.setup();
        registerHider();
        getHidersAID();
        addBehaviour(new WaitForTurnBehaviour(this));
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
                template_seekers.addServices(sd_hiders);

                try {
                    DFAgentDescription[] result_hiders = DFService.search(myAgent, template_hiders);
                    AID[] temp_hiders = new AID[result_hiders.length];

                    for (int i = 0; i < result_hiders.length; ++i) {
                        if (!result_hiders[i].getName().equals(myAgent.getAID().getName())) {
                            temp_hiders[i] = result_hiders[i].getName();
                        }
                    }

                    hiders = new ArrayList<AID>(Arrays.asList(temp_hiders));

                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

}