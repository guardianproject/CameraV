package org.witness.iwitness.utils;

import org.witness.informacam.models.IOrganization;
import org.witness.informacam.models.media.IMedia;

import android.net.Uri;

public class Constants {	
	public interface EditorActivityListener {
		public void onMediaScanned(Uri uri);
	}

	public interface HomeActivityListener {
		public int[] getDimensions();
		public void launchEditor(IMedia media);
		public void logoutUser();
		public void getContextualMenuFor(IOrganization organization);
		public void getContextualMenuFor(IMedia media);
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
			public static final int ORIENTATION_PORTRAIT = org.witness.informacam.utils.Constants.Codes.Media.ORIENTATION_PORTRAIT;
			public static final int ORIENTATION_LANDSCAPE = org.witness.informacam.utils.Constants.Codes.Media.ORIENTATION_LANDSCAPE;

			public static final int TYPE_IMAGE = org.witness.informacam.utils.Constants.Codes.Media.TYPE_IMAGE;
			public static final int TYPE_VIDEO = org.witness.informacam.utils.Constants.Codes.Media.TYPE_VIDEO;
			public static final int TYPE_JOURNAL = org.witness.informacam.utils.Constants.Codes.Media.TYPE_JOURNAL;
		}

		public class Extras {
			public final static String EDIT_MEDIA = "edit_media";
			public static final String SET_ORIENTATION = "set_orientation";
			public static final String WIZARD_SUPPLEMENT = org.witness.informacam.utils.Constants.Codes.Extras.WIZARD_SUPPLEMENT;
			public static final String MESSAGE_CODE = org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE;
			public static final String RETURNED_MEDIA = org.witness.informacam.utils.Constants.Codes.Extras.RETURNED_MEDIA;
			public static final String INSTALL_NEW_KEY = org.witness.informacam.utils.Constants.Codes.Extras.INSTALL_NEW_KEY;
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

			public class Mode {
				public static final int NONE = 0;
				public static final int DRAG = 1;
				public static final int ZOOM = 2;
				public static final int TAP = 3;
			}
			
			public class Color {
				public final static int DRAW_COLOR = 0x00000000;
				public final static int DETECTED_COLOR = 0x00000000;
				public final static int OBSCURED_COLOR = 0x00000000;
			}
			
			public class Forms {
				public static final String OVERVIEW_FORM = "iWitness Top Level Annotations";
				public static final String TAG_FORM = "iWitness v 1.0";
				
				public class OverviewForm {
					public static final String TAG = "iWitness Top Level Annotations";
					public static final String QUICK_NOTE_PROMPT = "iW_quick_note";
					public static final String AUDIO_NOTE_PROMPT = "iW_audio_note";
					
				}
				
				public class TagForm {
					public static final String TAG = "iWitness v 1.0";
				}
			}


			// Maximum zoom scale
			public static final float MAX_SCALE = 10f;
		}

		public class Home {
			public final static String LOG = "******************** iWitness : HomeActivity ********************";
			public static final int ROUTE_CODE = Codes.Routes.HOME;
			public static final String TAG = "iWitness.Home";

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
				
				public class SharePopup {
					public final static String TAG = "share_popup";
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

		public static final String TAG = "iWitness_main";
	}
}
