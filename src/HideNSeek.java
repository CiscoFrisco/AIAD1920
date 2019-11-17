import jade.core.*;
import jade.wrapper.*;
import java.util.ArrayList;

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
        Logger.init();

        createContainers();

        createGameMaster(world.getWorld());
        createHiderAgents(world.getHiders());
        createSeekerAgents(world.getSeekers());
    }

    public static void createGameMaster(char[][] world) {

        try {
            Object[] args = new Object[1];
            args[0] = world;
            mainContainer.createNewAgent("Master", "GameMasterAgent", args).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createHiderAgents(ArrayList<Position> hiders) {

        try {
            for (int i = 0; i < hiders.size(); i++) {
                Object[] args = new Object[1];
                args[0] = hiders.get(i);
                hidersContainer.createNewAgent("Hider" + i, "HiderAgent", args).start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createSeekerAgents(ArrayList<Position> seekers) {

        try {
            for (int i = 0; i < seekers.size(); i++) {
                Object[] args = new Object[1];
                args[0] = seekers.get(i);
                seekersContainer.createNewAgent("Seeker" + i, "SeekerAgent", args).start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createContainers() {
        rt = jade.core.Runtime.instance();
        profile = new ProfileImpl();
        mainContainer = rt.createMainContainer(profile);

        p1 = new ProfileImpl();
        p1.setParameter(Profile.CONTAINER_NAME, "Hiders");
        hidersContainer = rt.createAgentContainer(p1);

        p2 = new ProfileImpl();
        p2.setParameter(Profile.CONTAINER_NAME, "Seekers");
        seekersContainer = rt.createAgentContainer(p2);
    }
}