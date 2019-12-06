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

        if (args.length != 4) {
            System.err.println(
                    "Usage: java HideNSeek ../res/worlds/<WORLD>.txt ../csv/<CSV_FILE>.csv <NUM_MAX_ROUNDS> <LYING_PROBABILITY>");
            System.exit(1);
        }

        HideNSeekWorld world = new HideNSeekWorld(args[0]);
        Logger.init();
        CSVExport.init(args[1], new String[] { "Hiders", "Seekers", "Cells", "Obstacles", "Lying Probability",
                "Max Rounds", "Rounds Played", "Agents Lied" });

        createContainers();

        createGameMaster(world, world.getSeekers().size(), world.getHiders().size(),
                Integer.parseInt(args[2]), Double.parseDouble(args[3]));
        createHiderAgents(world.getHiders(), Double.parseDouble(args[3]));
        createSeekerAgents(world.getSeekers(), Double.parseDouble(args[3]));
    }

    public static void createGameMaster(HideNSeekWorld world, int numSeekers, int numHiders, int maxRounds,
            double lyingProbability) {

        try {
            Object[] args = new Object[5];
            args[0] = world;
            args[1] = numSeekers;
            args[2] = numHiders;
            args[3] = maxRounds;
            args[4] = lyingProbability;
            mainContainer.createNewAgent("Master", "GameMasterAgent", args).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createHiderAgents(ArrayList<Position> hiders, double lyingProbability) {

        try {
            for (int i = 0; i < hiders.size(); i++) {
                Object[] args = new Object[2];
                args[0] = hiders.get(i);
                args[1] = lyingProbability;
                hidersContainer.createNewAgent("Hider" + i, "HiderAgent", args).start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createSeekerAgents(ArrayList<Position> seekers, double lyingProbability) {

        try {
            for (int i = 0; i < seekers.size(); i++) {
                Object[] args = new Object[2];
                args[0] = seekers.get(i);
                args[1] = lyingProbability;
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