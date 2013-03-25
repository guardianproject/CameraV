package org.witness.iwitness.app.screens;

import java.util.List;
import java.util.Vector;

import org.witness.iwitness.R;
import org.witness.iwitness.models.Media;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.adapters.GalleryGridAdapter;
import org.witness.iwitness.utils.adapters.GalleryListAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

public class GalleryFragment extends Fragment implements OnItemSelectedListener, OnClickListener {
	View rootView;
	Spinner displayToggle, displaySort;
	ImageButton multiSelect;
	
	GridView mediaDisplayGrid;
	GalleryGridAdapter galleryGridAdapter;
	
	ListView mediaDisplayList;
	GalleryListAdapter galleryListAdapter;

	Activity a;
	boolean isInMultiSelectMode = false;
	
	private static final String LOG = Home.LOG;
	private List<Media> TEST_MEDIA = new Vector<Media>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_gallery, null);
		displayToggle = (Spinner) rootView.findViewById(R.id.display_toggle);
		displaySort = (Spinner) rootView.findViewById(R.id.display_sort);
		multiSelect = (ImageButton) rootView.findViewById(R.id.multi_select);
		
		mediaDisplayGrid = (GridView) rootView.findViewById(R.id.media_display_grid);
		mediaDisplayList = (ListView) rootView.findViewById(R.id.media_display_list);

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
		initLayout(savedInstanceState);	
	}
	
	private void initData() {
		Media media = new Media(a, true);
		TEST_MEDIA.add(media);
		
		galleryGridAdapter = new GalleryGridAdapter(a, TEST_MEDIA);
		galleryListAdapter = new GalleryListAdapter(a, TEST_MEDIA);
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
		mediaDisplayGrid.setAdapter(galleryGridAdapter);
		
		mediaDisplayList.removeAllViewsInLayout();
		mediaDisplayList.setAdapter(galleryListAdapter);
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
			Log.d(LOG, "selecting " + pos + " from displaySort");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void onClick(View v) {
		if(v == multiSelect) {
			
		}
		
	}
}
