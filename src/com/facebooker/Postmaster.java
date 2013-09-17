package com.facebooker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Session;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.os.AsyncTask;
import android.text.StaticLayout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Postmaster extends AsyncTask<Object, Void, Void>{
	private String response;
	private LinearLayout param;
	private Boolean isUpdate;
	private Boolean getOlder;
	private String  URL;
	private SelectionFragment fragGranade;
	private ArrayList<PostView> posts = new ArrayList<PostView>();
	private ArrayList<JSONObject> rows = new ArrayList<JSONObject>();
	
	@Override
	protected Void doInBackground(Object... params) {
		// sigh -_-
		param    	= (LinearLayout) 	  params[0];
		isUpdate 	= (Boolean)      	  params[1];
		getOlder 	= (Boolean) 	  	  params[2];
		URL		 	= (String)		  	  params[3];
		fragGranade = (SelectionFragment) params[4];
		
		Session session = Session.getActiveSession();
        String access_token = session.getAccessToken();
        //Log.w("TOKEN TOKEN TOKEN", access_token);
        HttpClient client = new DefaultHttpClient();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
		
		if (!isUpdate && !getOlder) {
			try {
		        String uri = "https://graph.facebook.com/me?fields=home&access_token=" + access_token;
		        HttpGet get = new HttpGet(uri);
		        response = client.execute(get, responseHandler);
	
		        Log.w("WABBA WABBA WABBA WABBA", response);
		    } catch (Exception ex) {
		    	Log.w("ERROR ERROR ERROR ERROR", "SHIT!!!!!!!!");
		        ex.printStackTrace();
		    }
		    parseJSON();
		} // End if not update and not get older
		else if (isUpdate) {
			if(URL != null) {
				HttpGet get = new HttpGet(URL);
				try {
					response = client.execute(get, responseHandler);
					Log.w("WABBA WABBA WABBA WABBA", response);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			parseJSON();
		} else if (getOlder) {
			if(URL != null) {
				HttpGet get = new HttpGet(URL);
				try {
					response = client.execute(get, responseHandler);
					Log.w("WABBA WABBA WABBA WABBA", response);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			parseJSON();
		}
		
		
		return null;
	}

	
	private void parseJSON() {
		if (response != null) {
			try {
				JSONArray array;
				JSONObject jresponse = new JSONObject(response);
				if (!isUpdate && !getOlder) {
					JSONObject json_obj = jresponse.getJSONObject("home");	
					
					if (json_obj.has("paging")) {
						JSONObject page = json_obj.getJSONObject("paging");
						fragGranade.newpage = page.getString("previous");
						fragGranade.oldpage = page.getString("next");
					}
					
					array = json_obj.getJSONArray("data");
				} else {
					if (jresponse.has("paging")) {
						JSONObject page = jresponse.getJSONObject("paging");
						if (isUpdate)
							fragGranade.newpage = page.getString("previous");
						else if (getOlder)
							fragGranade.oldpage = page.getString("next");
					}
					array = jresponse.getJSONArray("data");
				}
				
				for (int i = 0; i < array.length(); i++) {
				    JSONObject row = array.getJSONObject(i); 
				    rows.add(row);
					PostView txt = new PostView(param.getContext());
					

					try {
						if (row.has("message")) {
							txt.setMessage(" " + row.getString("message").toString());
							if (row.has("story")) {
								txt.addMessage("\n" + row.getString("story").toString());
							} 
						} else if (row.has("story")) {
							txt.setMessage(" " + row.getString("story").toString());
						} else if (row.has("type") &&
								   row.getString("type").toString().equals("photo")) {
							txt.setMessage(" " + row.getString("link").toString());
						} else {
							// TODO: Set this to continue
							txt.setMessage("Could Not parse message\n");
							//txt.setMessage("BLUBLUBLUBLUIBASDFASDFABLUBLUBLBULBULBUI" + row.toString());	
						}
						if (row.has("picture")) {
							//txt.setPicture(getBitmapFromURL(row.getString("picture").toString()));
						}
						if (row.has("from")) {
							JSONObject from = new JSONObject(row.getString("from"));
							txt.setFrom(from.getString("name"));
							if (row.has("to")) {
								from = new JSONArray(new JSONObject(row.getString("to")).getString("data")).getJSONObject(0);
								txt.addFrom("->" + from.getString("name"));
							}
							if (row.has("comments") && row.has("likes")) {
								from = new JSONObject(row.getString("likes"));
								txt.addFrom("\n" + from.getString("count") + " likes");
								from = new JSONObject(row.getString("comments"));
								txt.addFrom("\n" + from.getString("count") + " comments");
							}
						}
						if (row.has("created_time")) {
							String dtStart = row.getString("created_time");  
							SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+0000'");  
							try {  
							    Date date = format.parse(dtStart);  
							    txt.setDate(date);  
							} catch (Exception e) {  
							    // TODO Auto-generated catch block  
							    e.printStackTrace();  
							}
						}
						
					} catch (JSONException e) {
						// SOMETHING INCREDIBLY WRONG HAS HAPPED!!!
						continue;	// ehh whatever...
					}
					posts.add(txt);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	@Override
	protected void onPostExecute(Void n) {
		if(posts != null)
			for (int i = 0; i < posts.size(); i++) {
				Log.w("ASDFASDFASDF ASDFASDFASDF ASD FAS", "HEY NUMBER OF VIEWS IS " + param.getChildCount());
				if (!isUpdate && !getOlder) // add at the begining
					param.addView(posts.get(i), param.getWidth(), 200);
				else if (getOlder)	{
					param.addView(posts.get(i), param.getChildCount(), new LayoutParams(param.getWidth(), 200));
					Log.w("VIEW TYPE", "GET OLDER GET OLDER OLDER OLDER OLDER OLDER");
				} else if (isUpdate) {
					param.addView(posts.get(i), 1, new LayoutParams(param.getWidth(), 200));
					Log.w("VIEW TYPE", "GET NEWER GET NEWER NEWER NEWER NEWER NEWER");
				}
					
			}
		new PictureGetter().execute(posts, rows);
		if (isUpdate || getOlder)
			for (int i = 1; i < param.getChildCount(); i++)
				((PostView) param.getChildAt(i)).updateDate();
    }
}
