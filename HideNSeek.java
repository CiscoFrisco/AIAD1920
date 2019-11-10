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

    public static void main(String[] args) throws StaleProxyException, InterruptedException {

        HideNSeekWorld world = new HideNSeekWorld(args[0]); 

        createContainers();

        mainContainer.createNewAgent("Master", "GameMasterAgent", null).start();
        createHiderAgents(world.getHiders().size());
        createSeekerAgents(world.getSeekers().size());
    }

    public static void createHiderAgents(int numHiders){

        try{
            for(int i = 0; i < numHiders; i++){
                hidersContainer.createNewAgent("Hider" + i, "HiderAgent", null).start();
            }  
        }
        catch(StaleProxyException e){
            e.printStackTrace();
        }
       
    }

    public static void createSeekerAgents(int numSeekers){
        
        try{
            for(int i = 0; i < numSeekers; i++){
                seekersContainer.createNewAgent("Seeker" + i, "SeekerAgent", null).start();
            }  
        }
        catch(StaleProxyException e){
            e.printStackTrace();
        }

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