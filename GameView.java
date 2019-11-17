import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;

public class GameView extends JPanel {
    enum Direction {
        UP, RIGHT, DOWN, LEFT
    };

    private BufferedImage[][] graphics;
    private static int width = 10;
    private static int height = 10;
    private static int imageSize = 42;
    private TreeMap<Direction, BufferedImage> seekerImages;
    private TreeMap<Direction, BufferedImage> hiderImages;
    private TreeMap<Character, BufferedImage> otherImages;

    public GameView(char[][] map) {
        loadImages();
        graphics = new BufferedImage[height][width];
        updateGraphics(map);
    }

    private void loadImages() {
        seekerImages = new TreeMap<Direction, BufferedImage>();
        hiderImages = new TreeMap<Direction, BufferedImage>();
        otherImages = new TreeMap<Character, BufferedImage>();

        try {
            seekerImages.put(Direction.UP, ImageIO.read(new File("res/images/seeker_up.png")));
            seekerImages.put(Direction.LEFT, ImageIO.read(new File("res/images/seeker_left.png")));
            seekerImages.put(Direction.RIGHT, ImageIO.read(new File("res/images/seeker_right.png")));
            seekerImages.put(Direction.DOWN, ImageIO.read(new File("res/images/seeker_down.png")));

            hiderImages.put(Direction.UP, ImageIO.read(new File("res/images/hider_up.png")));
            hiderImages.put(Direction.LEFT, ImageIO.read(new File("res/images/hider_left.png")));
            hiderImages.put(Direction.RIGHT, ImageIO.read(new File("res/images/hider_right.png")));
            hiderImages.put(Direction.DOWN, ImageIO.read(new File("res/images/hider_down.png")));

            otherImages.put('+', ImageIO.read(new File("res/images/space.png")));
            otherImages.put('W', ImageIO.read(new File("res/images/wall.png")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 0;
        int y = 0;

        for (int i = 0; i < graphics.length; i++) {
            for (int j = 0; j < graphics[i].length; j++) {
                g.drawImage(graphics[i][j], x, y, this);
                x += imageSize;
            }

            x = 0;
            y += imageSize;
        }
    }

    /*
    public void updatePos(int i, int j, char entity, Direction direction) {
        switch (entity) {
        case 'H':
            graphics[i][j] = hiderImages.get(direction);
            break;
        case 'S':
            graphics[i][j] = seekerImages.get(direction);
            break;
        default:
            break;
        }
    }
    */

    private Direction intToDirection(int orientation){
        switch(orientation){
            case 0:
            return Direction.RIGHT;
            case 90:
            return Direction.UP;
            case 180:
            return Direction.LEFT;
            case 270:
            return Direction.DOWN;
            default:
            break;
        }

        return Direction.RIGHT;
    }

    public void updatePos(int oldX, int oldY, int newX, int newY, double orientation, char agent){
        Direction dir = intToDirection((int)orientation);
        graphics[oldY][oldX] = otherImages.get('+');
        graphics[newY][newX] = agent == 'S' ? seekerImages.get(dir) : hiderImages.get(dir);
    }

    public void updateGraphics(char[][] map) {
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++) {
                switch (map[i][j]) {
                case '+':
                case 'W':
                    graphics[i][j] = otherImages.get(map[i][j]);
                    break;
                case 'S':
                    graphics[i][j] = seekerImages.get(Direction.RIGHT);
                    break;
                case 'H':
                    graphics[i][j] = hiderImages.get(Direction.RIGHT);
                    break;
                default:
                    break;
                }
            }
    }

}