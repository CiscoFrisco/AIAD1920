
// import java.lang.String;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.NumberFormatException;
import java.io.IOException;


public class HideNSeekWorld {

    char[][] world;
    ArrayList<Hider> hiders;
    ArrayList<Seeker> seekers;
    ArrayList<Block> blocks;

    public HideNSeekWorld(String world_name) {

        hiders = new ArrayList<Hider>();
        seekers = new ArrayList<Seeker>();
        blocks = new ArrayList<Block>();

        try{
			BufferedReader reader = new BufferedReader(new FileReader(world_name));
            
            int height = Integer.parseInt(reader.readLine());
			int width = Integer.parseInt(reader.readLine());

			world = new char[height][width];

			for(int i=0;i<height; i++)
			{
				String line = reader.readLine();
				world[i] = line.toCharArray();
			}

            reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
        }
        
        printWorld();
        setupWorld();
    }

    public char[][] getWorld() {
        return world;
    }

    public void setupWorld() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                switch (world[i][j]) {
                case 'B':
                   blocks.add(new Block(j,i));
                    break;
                case 'H':
                    hiders.add(new Hider(j,i));
                    break;
                case 'S':
                    seekers.add(new Seeker(j,i));
                    break;
                }
            }
        }
    }

    public void printWorld() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                System.out.print(" | " + world[i][j]);
            }
            System.out.print(" |\n");
        }
    }

    public ArrayList<Hider> getHiders(){
        return hiders;
    }

    public ArrayList<Seeker> getSeekers(){
        return seekers;
    }
}