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
        addBehaviour(new WaitForTurnBehaviour());
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

    public ArrayList<AID> getHiders() {
        return this.hiders;
    }
    

    public void informHiders() {
        ACLMessage info = new ACLMessage(ACLMessage.INFORM);

        for (int i = 0; i < this.hiders.size(); i++) {
            info.addReceiver(this.hiders.get(i));
        }

        String infoContent = "SEEKERS;";
        for (Position cell: this.getCellsSeen()) {
            infoContent += cell.getX() + "," + cell.getY() + ";";
        }
        info.setContent(infoContent);
        System.out.println(this.getAID().getName() + " sended: " + infoContent);

        info.setConversationId("INFO " + this.getAID().getName());
        info.setReplyWith("INFO " + System.currentTimeMillis()); // Unique value
        this.send(info);
    }
    

    public boolean receiveHiderInfo() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage info = this.receive(mt);
                
        if (info != null) {
            // Request received
            String infoContent = info.getContent();
            String[] infoSplited = infoContent.split(";");
                    
            if (infoSplited[0].equals("SEEKERS")) {
                System.out.println(this.getAID().getName() + " received: " + infoContent);

                for (int i = 1; i < infoSplited.length; i++) {
                    String[] cellSplited = infoSplited[i].split(",");
                    Position cell = new Position(Integer.parseInt(cellSplited[0]), Integer.parseInt(cellSplited[1]));
                    this.addCellSeen(cell);
                }
                
                return true;
            }
            return false;
        }

        return false;
    }

}