import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;

public class Logger {

    private static String master_agents_file;
    private static String hiders_file;
    private static String seekers_file;
    private static String logs_folder;

    public static void init() {
        String now = Instant.now().toString();
        master_agents_file = "master_agents_" + now + ".txt";
        hiders_file = "hiders" + now + ".txt";
        seekers_file = "seekers" + now + ".txt";
        logs_folder = "logs/";

        File directory = new File("logs/");
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static void writeLog(String content, String file) {

        String fileName = logs_folder;

        switch (file) {
        case "hiders":
            fileName += hiders_file;
            break;
        case "seekers":
            fileName += seekers_file;
            break;
        case "master":
            fileName += master_agents_file;
            break;
        default:
            break;
        }

        try (FileWriter fw = new FileWriter(fileName, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}