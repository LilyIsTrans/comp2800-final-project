package this_team;


import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.universe.*;
import org.jogamp.vecmath.*;


public class MaterialManager {
	
	public final static Color3f White = new Color3f(1.0f, 1.0f, 1.0f);
	public final static Color3f Red = new Color3f(1.0f, 0.0f, 0.0f);
	public final static Color3f Orange = new Color3f(1.0f, 0.6f, 0.0f);
	public final static Color3f Yellow = new Color3f(1.0f, 1.0f, 0.0f);
	public final static Color3f Green = new Color3f(0.0f, 1.0f, 0.0f);
	public final static Color3f Blue = new Color3f(0.0f, 0.0f, 1.0f);
	public final static Color3f Cyan = new Color3f(0.0f, 1.0f, 1.0f);
	public final static Color3f Magenta = new Color3f(1.0f, 0.0f, 1.0f);
	public final static Color3f Purple = new Color3f(0.5f, 0.0f, 0.5f);
	public final static Color3f Grey = new Color3f(0.35f, 0.35f, 0.35f);
	public final static Color3f Black = new Color3f(0.0f, 0.0f, 0.0f);
	public final static Color3f Lime = new Color3f(0.0f, 1.0f, 0.5f);
	
	public final static BoundingSphere hundred_BS = new BoundingSphere(new Point3d(), 100.0);
	public final static BoundingSphere twenty_BS = new BoundingSphere(new Point3d(), 20.0);


	/* a function to create and return material definition */
	public static Material set_Material(Color3f m_clr) {
		Material mtl = new Material();
		mtl.setShininess(108);                              
		mtl.setAmbientColor(White);
		mtl.setDiffuseColor(m_clr);
		mtl.setSpecularColor(Grey);
		mtl.setEmissiveColor(Black);                       // non-emissive
		mtl.setLightingEnable(true);
		return mtl;
	}
	
    /* a function to set appearance with provided color ('clr') and predefined material */
	public static Appearance set_Appearance(Color3f clr) {		
		Appearance app = new Appearance();
		app.setMaterial(set_Material(clr));                // set appearance's material
		return app;
	}
	
	/* a function to place one light or two lights at opposite locations */
	public static BranchGroup add_Lights(Color3f clr, int p_num) {
		BranchGroup lightBG = new BranchGroup();
		Point3f atn = new Point3f(0.5f, 0.0f, 0.0f);
		PointLight ptLight;
		float adjt = 1f;
		for (int i = 0; (i < p_num) && (i < 2); i++) {
			if (i > 0) 
				adjt = -1f;                                // use 'adjt' to change light position 
			ptLight = new PointLight(clr, new Point3f(3.0f * adjt, 1.0f, 3.0f  * adjt), atn);
			ptLight.setInfluencingBounds(hundred_BS);
			lightBG.addChild(ptLight);                     // attach the point light to 'lightBG'
		}
		return lightBG;
	}

	/* a function to position viewer at 'eye' location */
	public static void define_Viewer(SimpleUniverse simple_U, Point3d eye) {
	    TransformGroup viewTransform = simple_U.getViewingPlatform().getViewPlatformTransform();
		Point3d center = new Point3d(0, 0, 0);             // define the point where the eye looks at
		Vector3d up = new Vector3d(0, 1, 0);               // define camera's up direction
		Transform3D view_TM = new Transform3D();
		view_TM.lookAt(eye, center, up);                   // look at 'center' from 'eye'
		view_TM.invert();
	    viewTransform.setTransform(view_TM);               // set the TransformGroup of ViewingPlatform
	}
	
	/* a function to define the appearance with texture mapping */
	public static Appearance set_Appearance(String s) {
		Appearance app = MaterialManager.set_Appearance(MaterialManager.White);

		
	app.setTexture(MaterialManager.texture_Appearance(s));
		
		TextureAttributes textureAttrib= new TextureAttributes();
		textureAttrib.setTextureMode(TextureAttributes.REPLACE);
		app.setTextureAttributes(textureAttrib);
		
		
		
		return app;
	}

	/* a function to define the texture with a specific image */	
	private static Texture2D texture_Appearance(String f_name) {
		String file_name = f_name;    // indicate the location of the image
		TextureLoader loader = new TextureLoader(file_name, null);
		ImageComponent2D image = loader.getImage();        // get the image
		if (image == null)
			System.out.println("Cannot load file: " + file_name);

		Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);                        // define the texture with the image
		
		return texture;
	}
}


