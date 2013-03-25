package org.witness.iwitness.utils;

import android.support.v4.app.FragmentManager;

public class Constants {
	
	public interface MainFragmentListener {
		public void toggleCameraChooser(boolean show);
		public boolean getCameraChooserIsShowing();
		public FragmentManager returnFragmentManager();
		public int[] getDimensions();
		public void launchEditor(String mediaId);
		public void logoutUser();
	}
	
	public interface EditorActivityListener {
		public void lockOrientation(int newOrientation);
	}
	
	public interface HomeActivityListener {
		public void toggleCameraChooser();
	}
	
	public class Codes {
		public class Routes {
			public static final int HOME = 1;
			public static final int CAMERA = 2;
			public static final int EDITOR = 3;
			public static final int WIZARD = org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT;
			public static final int LOGIN = org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGIN;
			public static final int LOGOUT = org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGOUT;
			public static final int WIPE = 4;
		}
		
		public class Media {
			public static final int ORIENTATION_PORTRAIT = 1;
			public static final int ORIENTATION_LANDSCAPE = 2;
			
			public static final int TYPE_IMAGE = 400;
			public static final int TYPE_VIDEO = 401;
			public static final int TYPE_JOURNAL = 402;
		}

		public class Extras {
			public final static String MEDIA_ID = "media_id";
			public static final String SET_ORIENTATION = "set_orientation";
			public static final String WIZARD_SUPPLEMENT = org.witness.informacam.utils.Constants.Codes.Extras.WIZARD_SUPPLEMENT;
			public static final String MESSAGE_CODE = org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE;
		}
	}
	
	public class Utils {
		public final static String LOG = "******************** iWitness : Utils ********************";
	}
	
	public class Preferences {
		public class Keys {
			public final static String ORIGINAL_IMAGE_HANDLING = "originalImageHandling";
		}
		
		public class OriginalImageHandling {
			public final static int DELETE_ORIGINAL = 0;
			public final static int SAVE_ORIGINAL = 1;
		}
	}
	
	
	public class App {
		public class Camera {
			public final static String LOG = "******************** iWitness : CameraActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.CAMERA;
		}
		
		public class Editor {
			public final static String LOG = "******************** iWitness : EditorActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.EDITOR;
		}
		
		public class Home {
			public final static String LOG = "******************** iWitness : HomeActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.HOME;
			
			public class Tabs {
				public class UserManagement {
					public final static String TAG = App.UserManagement.TAG;
				}
				
				public class Gallery {
					public final static String TAG = App.Gallery.TAG;
				}

				public class CameraChooser {
					public final static String TAG = App.CameraChooser.TAG;
				}
			}
		}
		
		public class UserManagement {
			public final static String TAG = "user_management";
		}
		
		public class Gallery {
			public final static String TAG = "gallery";
		}
		
		public class CameraChooser {
			public final static String TAG = "camera_chooser";
		}
		
		public class Router {
			public final static String LOG = "******************** iWitness : Router ********************";
		}
		
		
		public class Wizard {
			public final static String LOG = "******************** iWitness : WizardActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.WIZARD;
		}
		
		public class Login {
			public final static String LOG = "******************** iWitness : LoginActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.LOGIN;
		}

		public class Wipe {
			public final static String LOG = "******************** iWitness : WipeActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.WIPE;
		}
	}
}
