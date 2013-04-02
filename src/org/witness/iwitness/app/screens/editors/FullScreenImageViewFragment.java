package org.witness.iwitness.app.screens.editors;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IImage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.FullScreenViewFragment;
import org.witness.iwitness.app.screens.forms.TagFormFragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FullScreenImageViewFragment extends FullScreenViewFragment {
	IImage media;
	Bitmap bitmap, originalBitmap, previewBitmap;
	ImageView mediaHolder_;

	// sample sized used to downsize from native photo
	int inSampleSize;

	// Image Matrix
	Matrix matrix = new Matrix();

	// Saved Matrix for not allowing a current operation (over max zoom)
	Matrix savedMatrix = new Matrix();

	//handles threaded events for the UI thread
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 1: // loaded?
				Log.d(LOG, "bitmap loaded");
				break;
			default:
				super.handleMessage(msg);
			}
		}

	};

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;

		media = new IImage();
		media.inflate(((EditorActivity) a).media.asJson());
		informaCam = InformaCam.getInstance();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		// TODO: save state and cleanup bitmaps!
		
		try {
			bitmap.recycle();
			originalBitmap.recycle();
			previewBitmap.recycle();
		} catch(NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initLayout();
	}

	private void initLayout() {
		Log.d(LOG, media.asJson().toString());
		mediaHolderParent.setLayoutParams(new LinearLayout.LayoutParams(dims[0], dims[1]));
		
		mediaHolder_ = new ImageView(a);
		mediaHolder_.setLayoutParams(new LinearLayout.LayoutParams(dims[0], dims[1]));
		mediaHolder.addView(mediaHolder_);
		
		toggleControls.setOnClickListener(this);
		toggleControls();

		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inJustDecodeBounds = true;
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;

		byte[] bytes = null;
		if(media.bitmap != null) {
			bytes = informaCam.ioService.getBytes(media.bitmap, Type.IOCIPHER);
		} else {
			info.guardianproject.iocipher.File bitmapBytes = new info.guardianproject.iocipher.File(media.rootFolder, media.dcimEntry.name);
			bytes = informaCam.ioService.getBytes(bitmapBytes.getAbsolutePath(), Type.IOCIPHER);
			media.bitmap = bitmapBytes.getAbsolutePath();
			Log.d(LOG, "we didn't have this bitmap before for some reason...");
		}
		bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

		// Ratios between the display and the image
		double widthRatio =  Math.floor(bfo.outWidth / dims[0]);
		double heightRatio = Math.floor(bfo.outHeight / dims[1]);

		// If both of the ratios are greater than 1,
		// one of the sides of the image is greater than the screen
		if (heightRatio > widthRatio) {
			// Height ratio is larger, scale according to it
			inSampleSize = (int)heightRatio;
		} else {
			// Width ratio is larger, scale according to it
			inSampleSize = (int)widthRatio;
		}

		bfo.inSampleSize = inSampleSize;
		bfo.inJustDecodeBounds = false;

		bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bfo);
		bytes = null;

		if (media.dcimEntry.exif.orientation == ExifInterface.ORIENTATION_ROTATE_90) {
			Log.d(LOG, "Rotating Bitmap 90");
			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(90);
			bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotateMatrix,false);
		} else if (media.dcimEntry.exif.orientation == ExifInterface.ORIENTATION_ROTATE_270) {
			Log.d(LOG,"Rotating Bitmap 270");
			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(270);
			bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotateMatrix,false);
		}

		originalBitmap = bitmap;
		setBitmap();
		
		tagFormFragment = Fragment.instantiate(a, TagFormFragment.class.getName());
		
		FragmentTransaction ft = ((EditorActivity) a).fm.beginTransaction();
		ft.add(R.id.fullscreen_form_holder, tagFormFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	private void setBitmap() {
		float matrixWidthRatio = (float) dims[0] / (float) bitmap.getWidth();
		float matrixHeightRatio = (float) dims[1] / (float) bitmap.getHeight();

		// Setup the imageView and matrix for scaling
		float matrixScale = matrixHeightRatio;

		if (matrixWidthRatio < matrixHeightRatio) {
			matrixScale = matrixWidthRatio;
		} 

		mediaHolder_.setImageBitmap(bitmap);

		// Set the OnTouch and OnLongClick listeners to this (ImageEditor)
		mediaHolder.setOnTouchListener(this);
		mediaHolder.setOnClickListener(this);


		//PointF midpoint = new PointF((float)imageBitmap.getWidth()/2f, (float)imageBitmap.getHeight()/2f);
		matrix.postScale(matrixScale, matrixScale);

		// This doesn't completely center the image but it get's closer
		//int fudge = 42;
		matrix.postTranslate((float)((float) dims[0] -(float) bitmap.getWidth() * (float) matrixScale)/2f,(float)((float) dims[1] - (float) bitmap.getHeight() * matrixScale)/2f);

		mediaHolder_.setImageMatrix(matrix);
	}
}
