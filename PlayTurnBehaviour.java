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

    private int step = 0;
    private MessageTemplate mt;
    private ACLMessage reply;
    private GameAgent agent;

    public PlayTurnBehaviour(GameAgent agent){
        super();
        this.agent = agent;
    }

    public void action(){
        switch(step){
            case 0:
                //send Position and Orientation to Master
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(agent.getMasterAID());

                GameAgent gameAgent = (GameAgent)myAgent;

                request.setContent(gameAgent.getPos().getX() + " " + gameAgent.getPos().getY() + " " + gameAgent.getCurrOrientation());

                request.setConversationId("req" + agent.getAID().getName());
                request.setReplyWith("req" + System.currentTimeMillis()); // Unique value
                myAgent.send(request);
                System.out.println("Agent" + agent.getAID().getName() + " sended: " + request.getContent());
                // Prepare the template to get FOV
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("req" + agent.getAID().getName()),
                        MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = 1;
                break;
            case 1:
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

                        ((GameAgent)myAgent).setCellsSeen(cells);
                        
                        agent.addBehaviour(new WaitForTurnBehaviour(agent));
                    }
                } else {
                    block();
                }
                break;
            // case 2:
            //     //send move to master
            //     break;
            // case 3:
            //     //wait for approval
            //     //pos = new pos
            //     break;
        }
    }

    public boolean done(){
        return reply != null; // approved
    }

}