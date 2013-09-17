package com.facebooker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class PictureGetter extends AsyncTask<ArrayList<?>, Void, Void> {
	ArrayList<PostView>   posts;
	ArrayList<JSONObject> rows;
	
	public static Bitmap getBitmapFromURL(String src) {
	    try {
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        Bitmap myBitmap = BitmapFactory.decodeStream(input);
	        return myBitmap;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	@Override
	protected Void doInBackground(ArrayList<?>... arg) {
		posts = (ArrayList<PostView>)   arg[0];
		rows  = (ArrayList<JSONObject>) arg[1];
		
		
		if (posts.size() != rows.size()) {
			Log.w("PICTURE GETTER", "NUM OF VIEWS AND ROWS ARE NOT THE SAME!");
			return null;
		}
		
		
		for (int i = 0; i < posts.size(); i++) {
			if (rows.get(i).has("picture")) {
				try {
					posts.get(i).setPicture(getBitmapFromURL(rows.get(i).getString("picture").toString()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//posts.get(i).invalidate();
			}
			if (rows.get(i).has("from")) {
				try {
					JSONObject from = new JSONObject(rows.get(i).getString("from"));
					String id = from.getString("id");
					//String postId = rows.get(i).getString("id");
					posts.get(i).setProfile(getBitmapFromURL("http://graph.facebook.com/" + 
															  id + "/picture?type=square"));
					//posts.get(i).setProfile(getBitmapFromURL("https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc6/" +
					//								 id + "_" + postId + "_q.jpg"));
					//posts.get(i).invalidate();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	protected void onPostExecute(Void n) {
		for (int i = 0; i < posts.size(); i++) {
			posts.get(i).invalidate();
		}
	}
	
}
