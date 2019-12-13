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

        if (args.length != 3) {
            System.err.println("Usage: java HideNSeek ../csv/<INPUT_FILE>.csv ../csv/<OUTPUT_FILE>.csv <TEST>");
            System.exit(1);
        }

        createContainers();
        Logger.init(Boolean.parseBoolean(args[2]));

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String exec;

        reader.readLine(); // read headers
        int curr_exec = 1; // current execution

        while ((exec = reader.readLine()) != null) {

            String[] exec_split = exec.split(",");
            HideNSeekWorld world = new HideNSeekWorld(exec_split[0]);

            CSVExport.init(args[1], new String[] { "Hiders", "Seekers", "Cells", "Obstacles", "Lying Probability",
                    "Max Rounds", "Rounds Played", "Game Length" });

            createGameMaster(world, world.getSeekers().size(), world.getHiders().size(),
                    Integer.parseInt(exec_split[5]), Boolean.parseBoolean(args[2]), curr_exec);
            createHiderAgents(world.getHiders(), curr_exec);
            createSeekerAgents(world.getSeekers(), curr_exec);

            try {
                while (mainContainer.getAgent("Master" + curr_exec) != null) {
                }
            } catch (ControllerException e) {
            }
            System.out.println(curr_exec + " finished");
            curr_exec++;
        }

        reader.close();
        System.out.println("Execution finished!!");
    }

    public static void createGameMaster(HideNSeekWorld world, int numSeekers, int numHiders, int maxRounds,
            boolean test, int exec) {

        try {
            Object[] args = new Object[5];
            args[0] = world;
            args[1] = numSeekers;
            args[2] = numHiders;
            args[3] = maxRounds;
            args[4] = test;
            mainContainer.createNewAgent("Master" + exec, "GameMasterAgent", args).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createHiderAgents(ArrayList<Position> hiders, int exec) {

        try {
            for (int i = 0; i < hiders.size(); i++) {
                Object[] args = new Object[1];
                args[0] = hiders.get(i);
                hidersContainer.createNewAgent("Hider" + exec + i, "HiderAgent", args).start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

    public static void createSeekerAgents(ArrayList<Position> seekers, int exec) {

        try {
            for (int i = 0; i < seekers.size(); i++) {
                Object[] args = new Object[1];
                args[0] = seekers.get(i);
                seekersContainer.createNewAgent("Seeker" + exec + i, "SeekerAgent", args).start();
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