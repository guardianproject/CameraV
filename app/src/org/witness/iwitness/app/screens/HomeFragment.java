package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.HomeActivityListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HomeFragment extends SherlockFragment implements
		ListAdapterListener, OnClickListener {
	View rootView;

	Activity a = null;

	Handler h = new Handler();

	private static final String LOG = Home.LOG;
	private final InformaCam informaCam = InformaCam.getInstance();
	private ActionMode mActionMode;

	private View mBtnPhoto;

	private View mBtnVideo;

	private View mBtnGallery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_main, null);
		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG, "GALLERY ON ACTIVITY CREATED CALLED");

		initLayout(savedInstanceState);
	}

	private void initData() {

		// informaCam.mediaManifest.sortBy(0);
		// listMedia = informaCam.mediaManifest.getMediaList();
		//
		// galleryGridAdapter = new GalleryGridAdapter(a, listMedia);
		// if (mediaDisplayGrid != null) {
		// mediaDisplayGrid.setAdapter(galleryGridAdapter);
		// mediaDisplayGrid.setOnItemLongClickListener(this);
		// mediaDisplayGrid.setOnItemClickListener(this);
		// }
		//
		// if (listMedia != null && listMedia.size() > 0) {
		// if (noMedia != null)
		// noMedia.setVisibility(View.GONE);
		// } else {
		//
		// if (noMedia != null)
		// noMedia.setVisibility(View.VISIBLE);
		// }
		//
		// updateAdapters();
	}

	private void initLayout(Bundle savedInstanceState) {
		initData();

		mBtnPhoto = rootView.findViewById(R.id.btnPhoto);
		mBtnPhoto.setOnClickListener(this);
		mBtnVideo = rootView.findViewById(R.id.btnVideo);
		mBtnVideo.setOnClickListener(this);
		mBtnGallery = rootView.findViewById(R.id.btnGallery);
		mBtnGallery.setOnClickListener(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_select: {
			mActionMode = getSherlockActivity().startActionMode(
					mActionModeSelect);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private final ActionMode.Callback mActionModeSelect = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			// MenuInflater inflater = mode.getMenuInflater();
			// inflater.inflate(R.menu.context_menu, menu);
			menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share)
					.setIcon(R.drawable.ic_gallery_share);
			menu.add(Menu.NONE, R.string.home_gallery_delete, 0,
					R.string.home_gallery_delete).setIcon(
					R.drawable.ic_gallery_trash);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.string.menu_done:
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.string.menu_share:
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	@Override
	public void updateAdapter(int which) {
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnPhoto) {
			((HomeActivityListener) a).launchCamera();
		} else if (v == mBtnVideo) {
			((HomeActivityListener) a).launchVideo();
		} else if (v == mBtnGallery) {
			((HomeActivityListener) a).launchGallery();
		}
	}
}
