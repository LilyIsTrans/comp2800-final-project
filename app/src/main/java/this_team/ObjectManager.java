package this_team;

import java.awt.Font;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.*;
import org.jogamp.vecmath.*;





public abstract class ObjectManager {
	
	protected TransformGroup objTG = new TransformGroup(); // use 'objTG' to position an object
	
	
	public TransformGroup position_Object() {	           // retrieve 'objTG' to which 'obj_shape' is attached
		return objTG;   
	}
	
	protected Appearance app;                              // allow each object to define its own appearance
	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // A3: attach the next transformGroup to 'objTG'
	}

		
}
//class for all box objects
class RectangleBox extends ObjectManager {//make more classes like this
	public RectangleBox() {
		Transform3D translator = new Transform3D();
		translator.setTranslation(new Vector3d(0.0f, 0f, 0f));
		objTG = new TransformGroup(translator);            

		objTG.addChild(create_Object(1f, 0.05f, 1f));                   // attach the object to 'objTG'
	}
	public RectangleBox(Vector3d v)
	{
		Transform3D translator = new Transform3D();
		translator.setTranslation(v);
		objTG = new TransformGroup(translator); 
	
		Appearance a = new Appearance();
		System.err.println(System.getProperty("user.dir"));
		a = MaterialManager.set_Appearance("middle.jpg");   // set the appearance for top of tile
		objTG.addChild(create_Object(a));                   // attach the object to 'objTG'
	}
	public RectangleBox(Vector3d v, float l, float h, float b) {
		Transform3D translator = new Transform3D();
		translator.setTranslation(v);
		objTG = new TransformGroup(translator); 
		//System.out.println("hello");
		Appearance a = new Appearance();
		System.err.println(System.getProperty("user.dir"));
		a = MaterialManager.set_Appearance("tile3.jpg");   // set the appearance for top of tile
		objTG.addChild(create_Object(l, h, b, a));                   // attach the object to 'objTG'
	}
	
	protected Node create_Object(float l, float h, float b, Appearance a) {
		System.err.println(System.getProperty("user.dir"));
		app = MaterialManager.set_Appearance("concrete.jpg" );
		
		Appearance appTop = a;
		
		Box base =  new Box(l, h, b, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, app);
		
		base.getShape(Box.TOP).setAppearance(appTop);
		
		return base;
	}
	
	protected Node create_Object(float l, float h, float b) {
		app = MaterialManager.set_Appearance(MaterialManager.White);   // set the appearance for the base
		Appearance appTop = new Appearance();
		System.err.println(System.getProperty("user.dir"));
		appTop= MaterialManager.set_Appearance("ludo.jpg" );   // set the appearance for the tower
		Box base =  new Box(l, h, b, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, app);
		
		base.getShape(Box.TOP).setAppearance(appTop);
		
		return base;
	}
	protected Node create_Object(Appearance a) {
		TransformGroup bottom = new TransformGroup();
		
		
		System.err.println(System.getProperty("user.dir"));
		app = MaterialManager.set_Appearance("concrete.jpg");
		
		Appearance appTop = a;
		
		Box base =  new Box(0.2f, 0.02f, 0.2f, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, app);
		
		base.getShape(Box.TOP).setAppearance(appTop);
		bottom.addChild(base);
		Appearance stick = new Appearance();
		stick = MaterialManager.set_Appearance("black.png");
		//first stick 
		Box stick1=new Box(0.005f, 0.05f, 0.28f, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, stick);
		Transform3D s1 = new Transform3D();
		s1.rotY(Math.PI / 4);
		TransformGroup cross1 = new TransformGroup(s1);
		cross1.addChild(stick1);
		bottom.addChild(cross1);
		//second stick
		Box stick2=new Box(0.005f, 0.05f, 0.28f, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, stick);
		Transform3D s2 = new Transform3D();
		s2.rotY(-Math.PI / 4);
		TransformGroup cross2 = new TransformGroup(s2);
		cross2.addChild(stick2);
		bottom.addChild(cross2);
		
		return bottom;
	}
}
//class for color tile objects 
class ColorTile extends ObjectManager {//make more classes like this

	public ColorTile(Vector3d v, float l, float h, String t) {
		Transform3D translator = new Transform3D();
		translator.setTranslation(v);
		objTG = new TransformGroup(translator); 
		
		objTG.addChild(create_Object(l, h, t));                   // attach the object to 'objTG'
	}
	
	public ColorTile(Vector3d v, String t, int tilt, float h)
	{
		Transform3D translator = new Transform3D();
		translator.setTranslation(v);
		
		if(tilt==1)//tilting the cylinder sideways
		{
			Transform3D axis = new Transform3D();
			axis.rotX(-Math.PI / 2);
			translator.mul(axis);//combining rotation and tilt
		}
		if(tilt==2)//tilting the cylinder sideways
		{
			Transform3D axis = new Transform3D();
			axis.rotX(-Math.PI / 2);
			axis.rotZ(Math.PI / 2);
			translator.mul(axis);//combining rotation and tilt
		}
        objTG = new TransformGroup(translator); 
		
		objTG.addChild(create_Object(0.05f, h, t));
	
	}
	
	protected Node create_Object(float r, float h, String t) {
		System.err.println(System.getProperty("user.dir"));
		app = MaterialManager.set_Appearance(t);
		
	
		Cylinder tile =  new Cylinder(r, h, Primitive.GENERATE_TEXTURE_COORDS|Cylinder.GENERATE_NORMALS, 30, 30, app);
		
		return tile;
	}
	
}

/* a derived class to create a string label and place it to the bottom of the self-made cone */
class ColorString extends ObjectManager {
	private String str;
	private Color3f clr;
	private double scl;
	private Point3f pos;                                           // make the label adjustable with parameters
	public ColorString(String str_ltrs, Color3f str_clr, double s, Point3f p) {
		str = str_ltrs;	
		clr = str_clr;
		scl = s;
		pos = p;

		Transform3D scaler = new Transform3D();
		scaler.setScale(scl);                              // scaling 4x4 matrix 
		Transform3D rotator = new Transform3D();           // 4x4 matrix for rotation
		rotator.rotY(Math.PI);
		Transform3D trfm = new Transform3D();              // 4x4 matrix for composition
		trfm.mul(rotator);                                 // apply rotation second
		trfm.mul(scaler);                                  // apply scaling first
		objTG = new TransformGroup(trfm);                  // set the combined transformation
		objTG.addChild(create_Object());                   // attach the object to 'objTG'		
	}
	
	protected Node create_Object() {
		Font my2DFont = new Font("Arial", Font.PLAIN, 1);  // font's name, style, size
		FontExtrusion myExtrude = new FontExtrusion();
		Font3D font3D = new Font3D(my2DFont, myExtrude);	
		Text3D text3D = new Text3D(font3D, str, pos);      // create 'text3D' for 'str' at position of 'pos'
		
		Appearance app = MaterialManager.set_Appearance(clr);    // use appearance to specify the string color
		return new Shape3D(text3D, app);                   // return a string label with the appearance
	}
	
}

