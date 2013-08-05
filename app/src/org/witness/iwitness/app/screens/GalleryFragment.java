package org.witness.iwitness.app.screens;

import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.adapters.GalleryGridAdapter;
import org.witness.iwitness.utils.adapters.GalleryListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class GalleryFragment extends Fragment implements OnItemSelectedListener, OnClickListener, OnItemClickListener, OnItemLongClickListener, ListAdapterListener {
	View rootView;
	Spinner displayToggle, displaySort;
	ImageButton multiSelect, displaySortTrigger;
	Button displayToggleTrigger, batchShare, batchDelete;

	GridView mediaDisplayGrid;
	GalleryGridAdapter galleryGridAdapter;

	ListView mediaDisplayList;
	GalleryListAdapter galleryListAdapter;
	RelativeLayout noMedia, pendingMedia;
	LinearLayout batchEditHolder;
	
	ProgressBar pendingProgressBar;
	TextView pendingProgressReadout;

	Activity a = null;

	boolean isInMultiSelectMode;
	List<IMedia> batch = null;
	List<IMedia> listMedia = null;
	Handler h = new Handler();

	private static final String LOG = Home.LOG;	
	private InformaCam informaCam = InformaCam.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_gallery, null);
		displayToggle = (Spinner) rootView.findViewById(R.id.display_toggle);
		displaySort = (Spinner) rootView.findViewById(R.id.display_sort);

		displayToggleTrigger = (Button) rootView.findViewById(R.id.display_toggle_trigger);
		displayToggleTrigger.setOnClickListener(this);

		displaySortTrigger = (ImageButton) rootView.findViewById(R.id.display_sort_trigger);
		displaySortTrigger.setOnClickListener(this);

		multiSelect = (ImageButton) rootView.findViewById(R.id.multi_select);
		multiSelect.setOnClickListener(this);

		mediaDisplayGrid = (GridView) rootView.findViewById(R.id.media_display_grid);
		mediaDisplayList = (ListView) rootView.findViewById(R.id.media_display_list);

		noMedia = (RelativeLayout) rootView.findViewById(R.id.media_display_no_media);
		pendingMedia = (RelativeLayout) rootView.findViewById(R.id.media_processing_holder);
		pendingProgressBar = (ProgressBar) rootView.findViewById(R.id.media_pending_progress);
		pendingProgressReadout = (TextView) rootView.findViewById(R.id.media_pending_readout);

		batchEditHolder = (LinearLayout) rootView.findViewById(R.id.media_batch_edit_holder);

		batchShare = (Button) rootView.findViewById(R.id.media_batch_share);
		batchShare.setOnClickListener(this);

		batchDelete = (Button) rootView.findViewById(R.id.media_batch_delete);
		batchDelete.setOnClickListener(this);

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

		informaCam.mediaManifest.sortBy(0);
		listMedia = informaCam.mediaManifest.getMediaList();

		galleryGridAdapter = new GalleryGridAdapter(a, listMedia);
		galleryListAdapter = new GalleryListAdapter(a, listMedia);

		if (mediaDisplayGrid != null)
		{
			mediaDisplayGrid.setAdapter(galleryGridAdapter);
			mediaDisplayGrid.setOnItemLongClickListener(this);
			mediaDisplayGrid.setOnItemClickListener(this);
		}

		if (mediaDisplayList != null)
		{
			mediaDisplayList.setAdapter(galleryListAdapter);
			mediaDisplayList.setOnItemLongClickListener(this);
			mediaDisplayList.setOnItemClickListener(this);
		}

		if(listMedia != null && listMedia.size() > 0) {
			if (noMedia != null)
				noMedia.setVisibility(View.GONE);
		} else {

			if (noMedia != null)
				noMedia.setVisibility(View.VISIBLE);
		}


		updateAdapters();			
	}

	public void toggleMultiSelectMode(boolean mode) {
		isInMultiSelectMode = mode;
		toggleMultiSelectMode();
	}

	public void toggleMultiSelectMode() {
		multiSelect.setImageDrawable(a.getResources().getDrawable(isInMultiSelectMode ? R.drawable.ic_action_selected : R.drawable.ic_action_select));
		if(isInMultiSelectMode) {
			batch = new Vector<IMedia>();
			batchEditHolder.setVisibility(View.VISIBLE);
		} else {
			batch = null;
			batchEditHolder.setVisibility(View.GONE);

		}

		galleryGridAdapter.update(listMedia);
		galleryListAdapter.update(listMedia);

	}

	private void initLayout(Bundle savedInstanceState) {
		ArrayAdapter<CharSequence> toggleAdapter = ArrayAdapter.createFromResource(a, R.array.view_options, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(a, R.array.sort_options, android.R.layout.simple_spinner_item);

		toggleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		displayToggle.setAdapter(toggleAdapter);
		displayToggle.setOnItemSelectedListener(this);

		displaySort.setAdapter(sortAdapter);
		displaySort.setOnItemSelectedListener(this);

		mediaDisplayGrid.removeAllViewsInLayout();
		mediaDisplayList.removeAllViewsInLayout();

		initData();
		toggleMultiSelectMode(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		if(parent == displayToggle) {
			switch(pos) {
			case 0:
				mediaDisplayGrid.setVisibility(View.VISIBLE);
				mediaDisplayList.setVisibility(View.GONE);				
				break;
			case 1:
				mediaDisplayGrid.setVisibility(View.GONE);
				mediaDisplayList.setVisibility(View.VISIBLE);
				break;
			}
		} else if(parent == displaySort) {
			listMedia = informaCam.mediaManifest.sortBy(pos);
			updateAdapters();			
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onClick(View v) {
		if(v == multiSelect) {
			if(isInMultiSelectMode) {
				isInMultiSelectMode = false;
			} else {
				isInMultiSelectMode = true;
			}

			toggleMultiSelectMode();
		} else if(v == displaySortTrigger) {
			displaySort.performClick();
		} else if(v == displayToggleTrigger) {
			displayToggle.performClick();
		} else if(v == batchShare) {
			// TODO
		} else if(v == batchDelete) {
			((HomeActivityListener) a).waiter(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					for(IMedia m : batch) {
						m.delete();
					}
					h.post(new Runnable() {
						@Override
						public void run() {
							// TODO: delete
							toggleMultiSelectMode(false);
							((HomeActivityListener) a).waiter(false);
						}
					});
				}
			}).start();
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int viewId, long l) {
		((HomeActivityListener) a).getContextualMenuFor(((IMedia) informaCam.mediaManifest.getMediaItem((int) l)));
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int viewId, long l) {
		if(!isInMultiSelectMode) {
			((HomeActivityListener) a).launchEditor(((IMedia) informaCam.mediaManifest.getMediaItem((int) l)));
		} else {
			try {
				IMedia m = (IMedia) listMedia.get((int) l);

				if(!m.has(Models.IMedia.TempKeys.IS_SELECTED)) {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
				}

				int selectedColor = R.drawable.blue;
				if(m.getBoolean(Models.IMedia.TempKeys.IS_SELECTED)) {
					selectedColor = R.drawable.white;
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
					batch.remove(m);
				} else {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, true);
					batch.add(m);
				}

				LinearLayout ll = (LinearLayout) view.findViewById(R.id.gallery_thumb_holder);
				ll.setBackgroundDrawable(getResources().getDrawable(selectedColor));
			} catch(JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}

	}

	private void updateAdapters() {
		if(listMedia != null) {
			this.galleryGridAdapter.update(listMedia);

			if (this.mediaDisplayGrid != null)
				mediaDisplayGrid.invalidate();

			if (this.mediaDisplayList != null)
				mediaDisplayList.invalidate();


			if(listMedia != null && listMedia.size() > 0) {
				if (noMedia != null)
					noMedia.setVisibility(View.GONE);
			} else {

				if (noMedia != null)
					noMedia.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public void updateAdapter(int which) {
		if(a != null) {
			listMedia = informaCam.mediaManifest.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
			updateAdapters();
		}
	}

	@Override
	public void setPending(int numPending, int numCompleted) {		
		if(a == null) {
			return;
		}
		
		if(numPending > 0 && numPending > numCompleted) {
			if(pendingMedia.getVisibility() == View.GONE) {
				pendingMedia.setVisibility(View.VISIBLE);
			}
			
			pendingProgressBar.setMax(numPending);
			pendingProgressBar.setProgress(numCompleted);
			
			pendingProgressReadout.setText(a.getString(R.string.x_of_x_processed, numCompleted, numPending));
			
		} else {
			if(pendingMedia.getVisibility() == View.VISIBLE) {
				pendingMedia.setVisibility(View.GONE);
			}
		}

	}

}
