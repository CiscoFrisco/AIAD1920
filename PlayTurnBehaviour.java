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
    private enum state {START, REQ_FOV, WAIT_FOV, INFORM, LISTEN, MOVE, END}

    private MessageTemplate mt;
    //private ACLMessage reply;
    private GameAgent agent;

    private state turnState = state.START;
    private int numInfo = 0;
    private boolean done = false;


    public PlayTurnBehaviour(){
        super();
    }


    public void action(){
        switch(turnState){

            case START:
                this.agent = (GameAgent) myAgent;
                this.turnState = state.REQ_FOV;
            break;

            case REQ_FOV:
                mt = this.agent.requestFOV();
                this.turnState = state.WAIT_FOV;
            break;

            case WAIT_FOV:
                if (this.agent.receiveFOV(mt)) {
                    this.turnState = state.INFORM;
                }
                else { block(); }
            break;

            case INFORM:
                if (this.agent instanceof SeekerAgent)
                    ((SeekerAgent) this.agent).informSeekers();
                else 
                    ((HiderAgent) this.agent).informHiders();
                this.turnState = state.LISTEN;
            break;

            case LISTEN:
                if (this.agent instanceof SeekerAgent) {
                    SeekerAgent seekerAgent = (SeekerAgent) agent; 
                    if ( seekerAgent.receiveSeekerInfo() )
                        numInfo++;
                    if (numInfo >= seekerAgent.getSeekers().size())
                        this.turnState = state.MOVE;
                }
                else {
                    HiderAgent hiderAgent = (HiderAgent) agent; 
                    if ( hiderAgent.receiveHiderInfo() )
                        numInfo++;
                    if (numInfo >= hiderAgent.getHiders().size())
                        this.turnState = state.MOVE;
                }
            break;
            
            case MOVE:
                this.turnState = state.END;
            break;

            case END:
                this.agent.addBehaviour(new WaitForTurnBehaviour());
                this.done = true;
            break;

        }
    }


    public boolean done(){
        return this.done;
    }

}