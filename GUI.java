import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class GUI implements KeyListener {

    private JFrame gameFrame;
    private GameMasterAgent myAgent;
	private GameView gameView;

    /**
     * Create the application.
     */
    public GUI(GameMasterAgent agent) {
        this.myAgent = agent;
        initFrame();
        gameFrame.setVisible(true);
    }

    public void updateMap()
	{
        gameView.updateGraphics(myAgent.getWorld());
		gameView.repaint();
    }
    
    public void updatePos(int oldX, int oldY, int newX, int newY, double orientation, char agent){
        gameView.updatePos(oldX, oldY, newX, newY, orientation, agent);
        gameView.repaint();
    }

    private void initFrame() {
		gameFrame = new JFrame();
		gameFrame.setTitle("Hide and Seek");
		gameFrame.setResizable(false);
		gameFrame.setBounds(100, 100, 611, 477);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.getContentPane().setLayout(null);
        gameView = new GameView(myAgent.getWorld());
        gameView.setBounds(18, 61, 329, 350);
        gameFrame.getContentPane().add(gameView);
        gameView.repaint();
		
	}

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

}