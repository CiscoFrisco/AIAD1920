
// import java.lang.String;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.NumberFormatException;
import java.io.IOException;

public class HideNSeekWorld {

    char[][] world;
    ArrayList<Position> hiders;
    ArrayList<Position> seekers;

    public HideNSeekWorld(String world_name) {

        hiders = new ArrayList<Position>();
        seekers = new ArrayList<Position>();

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
        
        setupWorld();
    }

    public char[][] getWorld() {
        return world;
    }

    public void setupWorld() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                switch (world[i][j]) {
                case 'H':
                    hiders.add(new Position(j,i));
                    break;
                case 'S':
                    seekers.add(new Position(j,i));
                    break;
                }
            }
        }
    }

    public ArrayList<Position> getHiders(){
        return hiders;
    }

    public ArrayList<Position> getSeekers(){
        return seekers;
    }
}