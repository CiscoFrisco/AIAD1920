import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GUI implements KeyListener {

    private JFrame gameFrame;
    private GameMasterAgent myAgent;
	private GameView gameView;
	private JLabel lblGameStatus;
	private JLabel lblRounds;

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
        gameView.updateGraphics(myAgent.getWorld().getWorld());
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
		gameFrame.setBounds(100, 100, 1000, 500);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.getContentPane().setLayout(null);
        gameView = new GameView(myAgent.getWorld().getWorld());
        gameView.setBounds(18, 61, 900, 350);
        gameFrame.getContentPane().add(gameView);
        gameView.repaint();

        lblGameStatus = new JLabel("Game Status");
		lblGameStatus.setBounds(8, 406, 339, 31);
        gameFrame.getContentPane().add(lblGameStatus);
        
        lblRounds = new JLabel("Rounds");
		lblRounds.setBounds(200, 406, 339, 31);
		gameFrame.getContentPane().add(lblRounds);
		
    }
    
    public void updateStatus(String text){
        lblGameStatus.setText(text);
    }

    public void updateRounds(int currentRound, int totalRounds){
        lblRounds.setText("Round " + currentRound + "/" + totalRounds);
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