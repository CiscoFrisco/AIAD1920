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

public class GameAgent extends Agent {

    private AID masterAID;

    Position pos;
    boolean isGrabbing;
    double currOrientation;
    private LinkedHashSet<Position> cellsSeen;

    public void setup() {

        Object[] args = getArguments();
        pos = (Position) args[0];
        isGrabbing = false;
        currOrientation = 0;
        cellsSeen = new LinkedHashSet<Position>();

        initMasterAID();
    }

    public void initMasterAID() {

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
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public double getCurrOrientation() {
        return currOrientation;
    }

    public void setCurrOrientation(double currOrientation) {
        this.currOrientation = currOrientation;
    }

    public LinkedHashSet<Position> getCellsSeen() {
        return cellsSeen;
    }

    public void setCellsSeen(LinkedHashSet<Position> cellsSeen) {
        this.cellsSeen = cellsSeen;
    }

    public void addCellSeen(Position cell) {
        this.cellsSeen.add(cell);
    }

    public AID getMasterAID() {
        return masterAID;
    }

    public void setMasterAID(AID masterAID) {
        this.masterAID = masterAID;
    }

    public MessageTemplate requestFOV() {
        //send Position and Orientation to Master
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(this.masterAID);

        request.setContent("FOV_REQ;" + this.pos.getX() + ";" + this.pos.getY() + ";" + this.currOrientation);

        request.setConversationId("FOV_REQ " + this.getAID().getName());
        request.setReplyWith("FOV_REQ " + System.currentTimeMillis()); // Unique value
        this.send(request);
        System.out.println("Agent" + this.getAID().getName() + " sended: " + request.getContent());

        // Prepare the template to get FOV
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("FOV_REQ " + this.getAID().getName()),
                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
        return mt;
    }

    public boolean receiveFOV(MessageTemplate mt) {
        ACLMessage reply = this.receive(mt);
    
        if (reply != null) {
            String content = reply.getContent();
            String[] splited = content.split(";");
            
            // Reply received
            if (splited[0].equals("FOV")) {
                System.out.println("Agent " + this.getAID().getName() + " received:" + content);
                
                LinkedHashSet<Position> cells = new LinkedHashSet<Position>();
                for(int i = 1; i < splited.length; i++){
                    String[] coordinates = splited[i].split(",");
                    cells.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
                }

                this.setCellsSeen(cells);
                return true;
            }
            else {
                return false;
            }
        } 
        
        return false;
    }

}
