package com.facebooker;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout.LayoutParams;

public class PostView extends View {
	private String   message;
	private Bitmap	 picture;
	private Bitmap	 profile;
	//private StaticLayout layout;
	
	private String   from;
	private String 	 time;
	private Date	 date;
	
	private boolean  showLikes;
	private boolean  showComm;
	private int	     likes;
	private int      comments;
	
	private boolean justdate = false;
	// Paints
	private Paint 	  msgP;
	private TextPaint txtP;
	
	public PostView(Context context) {
		super(context);
		init();
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int vpad = 30;
		int rpad = 20;
		int lpad = 10;
		int fpad = 3;
		float topBar = 0;
		float postHeight = 0;
		
		if (justdate) {
			StaticLayout timelay  = new StaticLayout(time, txtP, this.getWidth() - rpad,Layout.Alignment.ALIGN_OPPOSITE, 1.3f, 0, false);
			StaticLayout fromLay;
			if (profile != null) {
				fromLay = new StaticLayout(from, txtP, this.getWidth() - rpad - profile.getWidth(),Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
			}
			else {
				fromLay = new StaticLayout(from, txtP, this.getWidth() - rpad,Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
			}
			canvas.translate(0, fromLay.getHeight());
			timelay.draw(canvas);
			justdate = false;
			return;
		}
		
		// if (picture != null)
		// 	picture.draw(canvas);StaticLayout
		
		
		StaticLayout fromLay;
		
		if (profile != null) {
			fromLay = new StaticLayout(from, txtP, this.getWidth() - rpad - profile.getWidth(),Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
		}
		else {
			fromLay = new StaticLayout(from, txtP, this.getWidth() - rpad,Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
		}
		
		StaticLayout timelay  = new StaticLayout(time, txtP, this.getWidth() - rpad,Layout.Alignment.ALIGN_OPPOSITE, 1.3f, 0, false);
		StaticLayout layout  = new StaticLayout(message, txtP, this.getWidth() - rpad,Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
		if (picture != null) {
			this.setLayoutParams(new LayoutParams(this.getWidth(), fromLay.getHeight() + 
																   layout.getHeight() + 
																   timelay.getHeight() +
																   vpad +
																   picture.getHeight()));
		} else
			this.setLayoutParams(new LayoutParams(this.getWidth(), layout.getHeight() +
																   timelay.getHeight() +
																   vpad +
																   fromLay.getHeight()));
		if (profile != null) {
			canvas.drawBitmap(profile, lpad, 0, msgP);
		}
		
		topBar = timelay.getHeight();
		
		txtP.setFakeBoldText(true);
		txtP.setColor(Color.rgb(59, 89, 152));		// Facebook Blue
		if (profile != null) {
			canvas.translate(profile.getWidth() + lpad + fpad, 0);
			fromLay.draw(canvas);
			canvas.translate(-profile.getWidth() - fpad, 0);
		} else {
			canvas.translate(lpad, 0);
			fromLay.draw(canvas);
		}
		//lpad already on canvas
		canvas.translate(0, fromLay.getHeight());
		timelay.draw(canvas);
		
		//txtP.setTextSize(16);
		txtP.setFakeBoldText(false);
		txtP.setColor(Color.BLACK);
		canvas.translate(0, topBar);
		layout.draw(canvas);
		if (picture != null) {
			//canvas.translate(0, layout.getHeight());
			canvas.drawBitmap(picture, layout.getWidth()/2 - picture.getWidth()/2, layout.getHeight(), msgP);
		}
		
	}
	
	private void init() {
		showLikes = true;
		showComm  = true;
		msgP 	  = new Paint();
		msgP.setAntiAlias(true); 
		msgP.setColor(Color.BLACK);
		msgP.setTextSize(16);
		txtP = new TextPaint(msgP);
	}
    
    public void setPicture(Bitmap pic) {
    	picture = pic;
    }
    public void setProfile(Bitmap pic) {
    	profile = pic;
    }
	public void addMessage(String msg) {
		message += msg;
	}
	public void setMessage(String msg) {
		message = msg;
	}
	public void setDate(Date d) {
		date = d;
		time = DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
	}
	public void updateDate() {
		justdate = true;
		time = DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
		invalidate();
	}
	public void setFrom(String msg) {
		from = msg;
	}
	public void addFrom(String msg) {
		from += msg;
	}
	public void setlikes(int l) {
		likes = l;
	}
	public void setComments(int c) {
		comments = c;
	}
	public void setShowLikes(boolean showL) {
		   showLikes = showL;
		   invalidate();
		   requestLayout();
	}
	public void setShowComm(boolean showC) {
		   showComm = showC;
		   invalidate();
		   requestLayout();
	}
}
