package com.facebooker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectionFragment extends Fragment {
	private static final String TAG = "SelectionFragment";
	private LinearLayout layout;
	public String newpage;
	public String oldpage;
	
	@Override
	public void onStart() {
		super.onStart();
		layout = (LinearLayout) this.getView().findViewById(R.id.layout);
		
		new Postmaster().execute(layout, false, false, "", this);
	}
	
	public void getNew() {
		if (layout != null)
			new Postmaster().execute(layout, true, false, newpage, this);
	}
	public void getOld() {
		if (layout != null)
			new Postmaster().execute(layout, false, true, oldpage, this);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, 
	          				 ViewGroup container, 
	          				 Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    	    
	    View view = inflater.inflate(R.layout.facebook_selection, container, false);
	    return view;
	}
	
}
