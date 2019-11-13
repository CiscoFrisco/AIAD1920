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


public class PlayTurnBehaviour extends SimpleBehaviour {
    private enum state {TURN_START, ASK_FOV, WAIT_FOV, MOVE, ASK_APPROVAL, WAIT_APPROVAL, TURN_END}


    private MessageTemplate mt;
    private ACLMessage reply;
    private GameAgent agent;

    private state turnState = state.TURN_START;
    private boolean done = false;


    public PlayTurnBehaviour(){
        super();
    }

    public void action(){
        switch(turnState){

            case TURN_START:
                this.agent = (GameAgent) myAgent;
                this.turnState = state.ASK_FOV;
            break;

            case ASK_FOV:
                this.requestFOV();
                this.turnState = state.WAIT_FOV;
            break;

            case WAIT_FOV:
                this.receiveFOV();
                if (reply != null)
                    this.turnState = state.MOVE;
            break;

            case MOVE:
                if (myAgent instanceof HiderAgent) {
                    //TO DO: move hider
                }
                else {
                    //TO DO: move seeker
                }
                this.turnState = state.ASK_APPROVAL;
            break;

            case ASK_APPROVAL:
                //TO DO: ask master if valid move
                this.turnState = state.WAIT_APPROVAL;
            break;

            case WAIT_APPROVAL:
                //TO DO: receives approval
                this.turnState = state.TURN_END;
            break;

            case TURN_END:
                agent.addBehaviour(new WaitForTurnBehaviour(agent));
                this.done = true;
            break;

        }
    }


    
    public void requestFOV() {
        //send Position and Orientation to Master
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(agent.getMasterAID());

        request.setContent(agent.getPos().getX() + " " + agent.getPos().getY() + " " + agent.getCurrOrientation());

        request.setConversationId("req" + agent.getAID().getName());
        request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
        myAgent.send(request);
        System.out.println("Agent" + agent.getAID().getName() + " sended: " + request.getContent());
        // Prepare the template to get FOV
        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("req" + agent.getAID().getName()),
                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
    }


    public void receiveFOV() {
        //receive FOV
        reply = myAgent.receive(mt);
        if (reply != null) {

            // Reply received
            if (reply.getPerformative() == ACLMessage.INFORM) {

                String content = reply.getContent();
                System.out.println("Agent " + agent.getAID().getName() + " received:" + content);
                String[] splited = content.split(";");

                LinkedHashSet<Position> cells = new LinkedHashSet<Position>();

                for(int i = 0; i < splited.length; i++){
                    String[] coordinates = splited[i].split(",");
                    cells.add(new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
                }

                agent.setCellsSeen(cells);
            }
        } else {
            block();
        }
    }


    public boolean done(){
        return this.done;
    }

}