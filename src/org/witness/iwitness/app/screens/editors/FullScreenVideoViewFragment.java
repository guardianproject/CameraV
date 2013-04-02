package org.witness.iwitness.app.screens.editors;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.InformaCamMediaScanner;
import org.witness.informacam.utils.InformaCamMediaScanner.OnMediaScannedListener;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.FullScreenViewFragment;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.VideoView;

public class FullScreenVideoViewFragment extends FullScreenViewFragment implements OnClickListener, OnTouchListener, EditorActivityListener {
	IVideo media;
	
	MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	VideoView videoView;
	SurfaceHolder surfaceHolder;
	MediaPlayer mediaPlayer;
	
	Uri videoUri;
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;

		media = new IVideo();
		media.inflate(((EditorActivity) a).media.asJson());
		informaCam = InformaCam.getInstance();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// copy from iocipher to local :(
				java.io.File vidTemp = new java.io.File(FullScreenVideoViewFragment.this.a.getDir(Storage.ROOT, Activity.MODE_PRIVATE), media.dcimEntry.name);
				informaCam.ioService.saveBlob(informaCam.ioService.getBytes(media.video, Type.IOCIPHER), vidTemp);
				InformaCamMediaScanner icms = new InformaCamMediaScanner(FullScreenVideoViewFragment.this.a, vidTemp);
			}
		});
	}
	
	private void initVideo() {
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initLayout();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		// TODO: save state and cleanup bitmaps!
		
	}
	
	private void initLayout() {
		View mediaHolder_ = LayoutInflater.from(a).inflate(R.layout.editors_video, null);
		mediaHolder.addView(mediaHolder_);
	}

	@Override
	public void onMediaScanned(Uri uri) {
		videoUri = uri;
		
		initVideo();
		
	}
}
