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
class RectangleBox extends ObjectManager {//make more classes like this
	public RectangleBox() {
		Transform3D translator = new Transform3D();
		translator.setTranslation(new Vector3d(0.0f, -0.54f, 0f));
		objTG = new TransformGroup(translator);            

		objTG.addChild(create_Object(1f, 0.04f, 1f));                   // attach the object to 'objTG'
	}
	public RectangleBox(Vector3d v, float l, float h, float b) {
		Transform3D translator = new Transform3D();
		translator.setTranslation(v);
		objTG = new TransformGroup(translator);            
		objTG.addChild(create_Object(l, h, b));                   // attach the object to 'objTG'
	}
	
	protected Node create_Object(float l, float h, float b) {
		app = MaterialManager.set_Appearance(MaterialManager.White);   // set the appearance for the base
		Appearance appTop = new Appearance();
		appTop= MaterialManager.set_Appearance("C:\\Users\\nazif\\Desktop\\university\\comp2800\\comp2800Nazifa\\ludo.jpg" );   // set the appearance for the tower
		Box base =  new Box(l, h, b, Primitive.GENERATE_TEXTURE_COORDS|Primitive.GENERATE_NORMALS, app);
		
		base.getShape(Box.TOP).setAppearance(appTop);
		
		return base;
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

