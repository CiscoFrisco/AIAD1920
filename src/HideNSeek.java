import jade.core.*;
import jade.wrapper.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class HideNSeek {
    private static jade.core.Runtime rt;
    private static Profile profile;
    private static Profile p1;
    private static Profile p2;
    private static ContainerController mainContainer;
    private static ContainerController hidersContainer;
    private static ContainerController seekersContainer;

    public static void main(String[] args) throws StaleProxyException, InterruptedException, IOException {

        if (args.length != 1) {
            System.err.println(
                    "Usage: java HideNSeek <EXEC_FILE>.csv\n Each execution follows the next syntax: <../res/worlds/<WORLD>.txt ../csv/<CSV_FILE>.csv <NUM_MAX_ROUNDS> <LYING_PROBABILITY>>");
            System.exit(1);
        }

        createContainers();

        Logger.init();

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String exec;

        while( (exec = reader.readLine()) != null) {

            String[] exec_split = exec.split(" ");
            HideNSeekWorld world = new HideNSeekWorld(exec_split[0]);

            CSVExport.init(exec_split[1], new String[] { "Hiders", "Seekers", "Cells", "Obstacles", "Lying Probability",
                "Max Rounds", "Rounds Played", "Agents Lied" });
            
            createGameMaster(world, world.getSeekers().size(), world.getHiders().size(), Integer.parseInt(exec_split[2]),
                    Double.parseDouble(exec_split[3]));
            createHiderAgents(world.getHiders(), Double.parseDouble(exec_split[3]));
            createSeekerAgents(world.getSeekers(), Double.parseDouble(exec_split[3]));
            
            //wait for game to end
            try {
                while (mainContainer.getAgent("Master") != null) {}
            } catch (ControllerException e) {}
        }
        
        reader.close();
        System.out.println("Execution finished!!");
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