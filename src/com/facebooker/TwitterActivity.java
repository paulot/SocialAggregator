package com.facebooker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.facebooker.R;


import twitter4j.*;
import twitter4j.auth.*;
import twitter4j.conf.*;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class TwitterActivity extends Activity {

	private static final String TWITTER_CONSUMER_KEY = "IC1Pk42t1MHqYztOchTZ0w";
	private static final String TWITTER_CONSUMER_SECRET = "pu5TKH558iu5taIAG5EYxNs2NPiXYxiJCDpBt8MIY";

	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	Button loginButton;
	//Button updateStatusButton;
	Button logoutButton;
	Button refreshButton;
	//EditText updateStatusText;
	//TextView updateLabel;
	//TextView userLabel;
	ScrollView scrollView;
	LinearLayout linearLayout;
	ArrayList<TextView> tweets;
	ArrayList<Bitmap> images;
	ArrayList<ImageView> avatars;
	ResponseList<Status> statuses;
	boolean startDrawing = false;

	ProgressDialog pDialog;

	private static Twitter twitter;
	private static RequestToken requestToken;
	
	private static SharedPreferences mSharedPreferences;
	
	AlertDialogManager alert = new AlertDialogManager();

	@Override
	protected void onPause () {
		super.onPause();
		logoutFromTwitter();
	}
	
	private MenuItem facebook;
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.size() == 0) {
			facebook = menu.add("Facebook");
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.equals(facebook)) {
            Intent intent = new Intent();
            intent.setClass(this, FacebookActivity.class);
            startActivity(intent);
	    	//this.startActivity(new In)
	    	return true;
	    }
		return false;
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_main);
		
		/** only applicaple to newer Android versions */
		if (Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		loginButton = (Button) findViewById(R.id.loginButton);
		logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setVisibility(View.GONE);
		refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setVisibility(View.GONE);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		
		

		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loginToTwitter();
			}
		});
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refresh();
				
			}
		});

		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				logoutFromTwitter();
			}
		});

		if (!isTwitterLoggedInAlready()) {
			Uri url = getIntent().getData();
			if (url != null && url.toString().startsWith(TWITTER_CALLBACK_URL)) {
				String verifier = url.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					Editor e = mSharedPreferences.edit();

					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,accessToken.getTokenSecret());
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit();

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					loginButton.setVisibility(View.GONE);
					
					statuses = twitter.getHomeTimeline();
					tweets = new ArrayList<TextView>();
					avatars = new ArrayList<ImageView>();
					
					for(int i = 0; i < statuses.size(); i++){
						Status status = statuses.get(i);
						String picurl = status.getUser().getProfileImageURL();
						URL imgurl = new URL(picurl);
						
						avatars.add(i, new ImageView(this));
						avatars.get(i).setImageBitmap(new getPic().execute(imgurl).get());
						linearLayout.addView(avatars.get(i));
						
						tweets.add(i, new TextView(this));
						tweets.get(i).setText(Html.fromHtml("<b>" + status.getUser().getName() + "</b><br/><i>@" + status.getUser().getScreenName() + "</i><br /><p>" + status.getText() + "</p><br/>"));
						linearLayout.addView(tweets.get(i));
					}
					logoutButton.setVisibility(View.VISIBLE);
					refreshButton.setVisibility(View.VISIBLE);
					
					

					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getName();
					
					//userLabel.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
				} catch (Exception e) {
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}

	}

	private void loginToTwitter() {
		
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();
			
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			if(!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
				try {
					requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
					this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
				}
				catch (TwitterException e) {
				e.printStackTrace();
				}
			}
			else{
				new Thread(new Runnable() {
					public void run() {
						try {	
							requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
							TwitterActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
						}
						catch (TwitterException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} else {
			Toast.makeText(getApplicationContext(),"Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}
	
	private void refresh(){
		try{
		for(int i = 0; i < statuses.size(); i++){
			tweets.get(i).setVisibility(View.GONE);
			avatars.get(i).setVisibility(View.GONE);
		}
		
		statuses = twitter.getHomeTimeline();
		tweets = new ArrayList<TextView>();
		avatars = new ArrayList<ImageView>();
		
		for(int i = 0; i < statuses.size(); i++){
			Status status = statuses.get(i);
			String picurl = status.getUser().getProfileImageURL();
			URL imgurl = new URL(picurl);
			
			avatars.add(i, new ImageView(this));
			avatars.get(i).setImageBitmap(new getPic().execute(imgurl).get());
			linearLayout.addView(avatars.get(i));
			
			tweets.add(i, new TextView(this));
			tweets.get(i).setText(Html.fromHtml("<b>" + status.getUser().getName() + "</b><br/><i>@" + status.getUser().getScreenName() + "</i><br /><p>" + status.getText() + "</p><br/>"));
			linearLayout.addView(tweets.get(i));
		}
		
		for(int i = 0; i < statuses.size(); i++){
			tweets.get(i).setVisibility(View.VISIBLE);
			avatars.get(i).setVisibility(View.VISIBLE);
		}
		}
		catch(Exception e){
			
		}
	}

	private void logoutFromTwitter() {
		Editor e = mSharedPreferences.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();

		if (logoutButton != null)
			logoutButton.setVisibility(View.GONE);
		if (refreshButton != null)
			refreshButton.setVisibility(View.GONE);
		if (loginButton != null)
			loginButton.setVisibility(View.VISIBLE);
		
		if (statuses != null && tweets != null && avatars != null) {
			for(int i = 0; i < statuses.size(); i++){
				tweets.get(i).setVisibility(View.GONE);
				avatars.get(i).setVisibility(View.GONE);
			}
		}
		
	}
	
	private boolean isTwitterLoggedInAlready() {
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	protected void onResume() {
		super.onResume();
	}
	
	private class getPic extends AsyncTask<URL, Void, Bitmap> {
		protected Bitmap doInBackground(URL... picURL) {
			Bitmap b = null;
			try {
				b = BitmapFactory.decodeStream(picURL[0].openConnection().getInputStream());
			} catch (Exception e) {
				Log.d("Debug Temp: ", "Issue with network thread.");
			}
			return b;
		}
	}

}
