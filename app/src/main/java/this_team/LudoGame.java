package this_team;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;


public class LudoGame extends JPanel implements ActionListener, KeyListener, MouseListener {

	private static final long serialVersionUID = 1L;
	private static TransformGroup sceneTG;
	private static JFrame frame;
	private Canvas3D canvas3D;  
	private static int obj_Num = 30;
	private static ObjectManager[] objects = new ObjectManager[obj_Num];
	
	/* a function to build the content branch */
	public static BranchGroup create_Scene()
	{
		BranchGroup sceneBG = new BranchGroup();
		sceneTG = new TransformGroup();                    // make 'sceneTG' changeable
		sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		sceneTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		sceneTG.setCapability(Node.ENABLE_PICK_REPORTING); // need for mouse picking
		
		
		sceneTG.addChild(create_board());//adding boards transform group to main transform group
		
		
		sceneBG.addChild(sceneTG);
		sceneBG.addChild(MaterialManager.add_Lights(MaterialManager.White, 1));//adding lights
		return sceneBG;
	}
	
	public LudoGame(BranchGroup sceneBG)
	{
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas3D = new Canvas3D(config);
		canvas3D.addKeyListener(this);                     // NOTE: enable key events 	
		canvas3D.addMouseListener(this);                   // NOTE: enable mouse clicking 
		SimpleUniverse su = new SimpleUniverse(canvas3D);    // create a SimpleUniverse
		MaterialManager.define_Viewer(su, new Point3d(.50d, 2d, 1.50d));

		sceneBG.compile();		                           // optimize the BranchGroup
		su.addBranchGraph(sceneBG);                        // attach the scene to SimpleUniverse

		setLayout(new BorderLayout());
		add("Center", canvas3D);
		frame.setSize(800, 800);                           // set the size of the JFrame
		frame.setVisible(true);
	}
	
	public static void main(String [] args)
	{
		frame = new JFrame("Ludo Game");
		frame.getContentPane().add(new LudoGame(create_Scene()));  // create an instance of the class
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	//Method to create the board
	private static TransformGroup create_board()
	{
        TransformGroup board = new TransformGroup();
		objects[0] = new RectangleBox();//the board
		
		//tiles for green side
		objects[1] = new RectangleBox(new Vector3d(0.13f, 0.05f, -0.264f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//first tile
		objects[0].add_Child(objects[1].position_Object());
		objects[2] = new RectangleBox(new Vector3d(0.13f, 0.05f, -0.394f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[2].position_Object());
		objects[3] = new RectangleBox(new Vector3d(0.13f, 0.05f, -0.524f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[3].position_Object());
		objects[4] = new RectangleBox(new Vector3d(0.13f, 0.05f, -0.654f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[4].position_Object());
		//safe space tile will be here
		//.....
		objects[5] = new RectangleBox(new Vector3d(0.13f, 0.05f, -0.916f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[5].position_Object());
		objects[6] = new RectangleBox(new Vector3d(-0.005f, 0.05f, -0.916f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[6].position_Object());
		objects[6] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.916f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[6].position_Object());
		objects[7] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.785f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[7].position_Object());
		//safe space tile
		objects[8] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.654f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[8].position_Object());
		objects[9] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.524f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[9].position_Object());
		objects[10] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.394f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[10].position_Object());
		objects[11] = new RectangleBox(new Vector3d(-0.135f, 0.05f, -0.264f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[11].position_Object());
		
		//tiles for red side
		objects[12] = new RectangleBox(new Vector3d(-0.265f, 0.05f, -0.13f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[12].position_Object());
		objects[13] = new RectangleBox(new Vector3d(-0.395f, 0.05f, -0.13f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[13].position_Object());
		objects[14] = new RectangleBox(new Vector3d(-0.525f, 0.05f, -0.13f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[14].position_Object());
		objects[15] = new RectangleBox(new Vector3d(-0.655f, 0.05f, -0.13f), 0.055f, 0.02f, 0.055f, "tile2.jpg");//tile
		objects[0].add_Child(objects[15].position_Object());
		
		board.addChild( objects[0].position_Object());
		
		return board;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void keyPressed(KeyEvent e) {
		Transform3D tmp = new Transform3D();
		int key_code = e.getKeyCode();
		if ((key_code == KeyEvent.VK_R)) {
			Matrix4d mat = new Matrix4d();
			sceneTG.getTransform(tmp);                     // retrieve from working BG
			tmp.get(mat);                                  // save current matrix to 'mat'
			Matrix4d m4d = new Matrix4d();                 // define an identity matrix
			m4d.rotY(0.1);
			mat.mul(m4d);                                  // define and perform rotation
			tmp.set(mat);
			sceneTG.setTransform(tmp);                     // set back to the working BG
		}
		if (key_code == KeyEvent.VK_O) {
			tmp = new Transform3D();                       // the same matrix as L.46
			sceneTG.setTransform(tmp);                     // reset to the original
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
