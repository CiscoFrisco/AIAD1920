import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

class TestFilesGenerator {

    final static File worldsFolder = new File("../res/worlds");

    public static ArrayList<String> readWorlds() {
        ArrayList<String> worlds = new ArrayList<String>();
        for (final File fileEntry : worldsFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                worlds.add("../res/worlds/" + fileEntry.getName());
            }
        }

        return worlds;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java TestFilesGenerator <OUTPUT_FILE> <TESTS>");
            System.exit(1);
        }

        int numTests = Integer.parseInt(args[1]);
        String fileName = args[0];
        CSVExport.init(fileName,
                new String[] { "FileName", "Hiders", "Seekers", "Cells", "Obstacles", "Lying Probability", "Max Rounds" });
        ArrayList<String> worlds = readWorlds();

        for (int i = 0; i < numTests; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, worlds.size());
            HideNSeekWorld world = new HideNSeekWorld(worlds.get(randomIndex));
            String hiders = String.valueOf(world.getHiders().size());
            String seekers = String.valueOf(world.getSeekers().size());
            String cells = String.valueOf(world.numCells());
            String obstacles = String.valueOf(world.numObstacles());
            String lyingProbability = String.valueOf(ThreadLocalRandom.current().nextDouble(0, 0.1));
            String maxRounds = String.valueOf(ThreadLocalRandom.current().nextInt(10, 51));

            CSVExport.writeLine(new String[] { worlds.get(randomIndex), hiders, seekers, cells, obstacles, lyingProbability, maxRounds });
        }
    }
}