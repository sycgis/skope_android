package com.skope.skope.ui;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.skope.skope.R;
import com.skope.skope.application.ObjectOfInterest;
import com.skope.skope.application.User.OnThumbnailLoadListener;
import com.skope.skope.maps.OOIOverlay;
import com.skope.skope.util.Type;
import com.skope.skope.util.Views;

public class OOIDetailMapActivity extends OOIMapActivity {
	private Gallery mGallery;
	private ImageAdapter mImageAdapter;
	private ObjectOfInterest mSelectedOOI;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Fill out the information on the screen and update the map.
	    // TODO: Note that the methods initializing the map view are also 
	    // called by the super class, so these are called twice. 
	    // Needs refactoring.
	    update();
	    
	    // Open map drawer
	    SlidingDrawer mapDrawer = (SlidingDrawer) findViewById(R.id.mapSlidingDrawer);
    	// Check if present; no drawer in landscape mode
	    if (mapDrawer != null) {
		    mapDrawer.open();
	    }
	    
	    // Back button
	    View backButton = findViewById(R.id.detail_back_button);
	    backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	    
	    // Store selected OOI
	    mSelectedOOI = getCache().getObjectOfInterestList().getSelectedOOI();
	    
	    // Set up the gallery
		mGallery = (Gallery) findViewById(R.id.gallery);
		mImageAdapter = new ImageAdapter(this, R.id.gallery, getCache(), getCache().getObjectOfInterestList());
	    mGallery.setAdapter(mImageAdapter);
	    
	    
	    // When the user selects a thumbnail in the gallery, update the view
	    mGallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            getCache().getObjectOfInterestList().setSelectedPosition(position);
	            update();
	        }
	    });
	    
	    mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView parent, View view,
					int position, long id) {
				getCache().getObjectOfInterestList().setSelectedPosition(position);
	            update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
								
			}
		});
	    
	    mGallery.setSelection(getCache().getObjectOfInterestList().indexOf(mSelectedOOI));
	    
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	/**
	 * Retrieves the currently selected object of interest, updates the
	 * information in the view, and calls the necessary methods to update the
	 * map view.
	 */
	protected void update() {
		ObjectOfInterest selectedOOI = getCache().getObjectOfInterestList()
				.getSelectedOOI();

		Views.createUserProfile(this, selectedOOI);

		initializeMapView();
		populateItemizedOverlays();
	}

	public void post(final Type type, final Bundle bundle) {
		super.post(type, bundle);

		switch (type) {
		case FIND_OBJECTS_OF_INTEREST_FINISHED:
			mImageAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	protected void setContentView() {
		setContentView(R.layout.detail);
	}

	@Override
	protected void initializeMapView() {
		super.initializeMapView();

		mMapView.invalidate();
	}

	@Override
	protected void populateItemizedOverlays() {
		ObjectOfInterest selectedObjectOfInterest = getCache()
				.getObjectOfInterestList().getSelectedOOI();

		// Clear current overlays
		mMapOverlays = mMapView.getOverlays();

		// Clear ooi overlay
		if (mMapOverlays.size() > 1) {
			mMapOverlays.remove(1);
		}

		LayerDrawable marker = (LayerDrawable) getResources().getDrawable(
				R.drawable.marker);

		OOIOverlay ooiOverlay = new OOIOverlay(marker, this);
		ooiOverlay.addOverlay(createOverlay(selectedObjectOfInterest));
		mMapOverlays.add(1, ooiOverlay);

		MapController mapController = mMapView.getController();

		int userLatitude = (int) (getCache().getCurrentLocation().getLatitude() * 1E6);
		int userLongitude = (int) (getCache().getCurrentLocation()
				.getLongitude() * 1E6);
		int ooiLatitude = (int) (selectedObjectOfInterest.getLocation()
				.getLatitude() * 1E6);
		int ooiLongitude = (int) (selectedObjectOfInterest.getLocation()
				.getLongitude() * 1E6);

		mapController.animateTo(new GeoPoint((userLatitude + ooiLatitude) / 2,
				(userLongitude + ooiLongitude) / 2));

		mapController.zoomToSpan(Math.abs(userLatitude - ooiLatitude),
				Math.abs(userLongitude - ooiLongitude));

	}

}
