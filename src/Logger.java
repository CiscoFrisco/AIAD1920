import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Logger {

    private static Path out_master_agents;
    private static Path out_hiders;
    private static Path out_seekers;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

    public static void init() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String now = sdf.format(timestamp);

        String logs_folder = "../logs/";
        String master_agents_file = logs_folder + "master_agents_" + now + ".txt";
        String hiders_file = logs_folder + "hiders_" + now + ".txt";
        String seekers_file = logs_folder + "seekers_" + now + ".txt";

        File directory = new File("../logs/");
        if (!directory.exists()) {
            directory.mkdir();
        }

        out_master_agents = Paths.get(master_agents_file);
        out_hiders = Paths.get(hiders_file);
        out_seekers = Paths.get(seekers_file);
    }

    public static void writeLog(String content, String file) {

        Path path = null;

        switch (file) {
        case "hiders":
            path = out_hiders;
            break;
        case "seekers":
            path = out_seekers;
            break;
        case "master":
            path = out_master_agents;
            break;
        default:
            break;
        }

        try {
            Files.write(path, Arrays.asList(content), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}