import jade.core.*;
import jade.wrapper.*;

public class HideNSeek {
    private static jade.core.Runtime rt;
    private static Profile profile;
    private static Profile p1;
    private static Profile p2;
    private static ContainerController mainContainer;
    private static ContainerController hidersContainer;
    private static ContainerController seekersContainer;

    public static void main(String[] args) throws StaleProxyException {

        createContainers();

        AgentController H1 = hidersContainer.createNewAgent("Hider1", "HiderAgent", null);
        H1.start();
        AgentController H2 = hidersContainer.createNewAgent("Hider2", "HiderAgent", null);
        H2.start();
        AgentController S1 = seekersContainer.createNewAgent("Seeker1", "SeekerAgent", null);
        S1.start();
        AgentController S2 = seekersContainer.createNewAgent("Seeker2", "SeekerAgent", null);
        S2.start();
    }

    public static void createContainers(){
        rt = jade.core.Runtime.instance();
        profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        mainContainer = rt.createMainContainer(profile);

        p1 = new ProfileImpl();
        p1.setParameter(Profile.CONTAINER_NAME, "Hiders");
        hidersContainer = rt.createAgentContainer(p1);

        p2 = new ProfileImpl();
        p2.setParameter(Profile.CONTAINER_NAME, "Seekers");
        seekersContainer = rt.createAgentContainer(p2);
    }
}