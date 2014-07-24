package org.aquadroid;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;
//import android.app.Activity;

public class Config {
	private static final String TAG = "Config";
	private String filename = "config.xml";
	private String root = "AquaDroid";
	private Context context;
	private String serverip;
	private int serverport;
	private int timecode;
	private int timeslide;
	private String work_directory = "/sdcard/AquaDROID/";
	private String image_directory = "slideshow_images";
	private String video_directory = "slideshow_videos";
	private String portcom = "/dev/s3c2410_serial0";
	private int baudrate = 9600;
	private int headcode = 1;
	
	private int enableSlideImages = 1;
	private int enableSlideVideos = 1;
	private int enablePhone = 1;
	
	public Config(Context context)
	{
		this.context = context;
		try{
			FileInputStream fis = this.context.openFileInput(this.filename);
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		ParseXML px = new ParseXML(this.context, this.filename);
		this.work_directory = px.getItem(root, "WorkingDirectory");
		Log.i(TAG, "directory: " + this.work_directory);
		this.image_directory = px.getItem(root, "ImageDirectory");
		//this.video_directory = px.getItem(root, "VideoDirectory");
		this.portcom = px.getItem(root, "PortCOM");
		this.baudrate = Integer.parseInt(px.getItem(root, "BaudRate"));
		this.headcode = Integer.parseInt(px.getItem(root, "HeadCode"));
		this.serverip = px.getItem(root, "ServerIP");
		Log.i(TAG, "serverip: " + this.serverip);
		this.serverport = Integer.parseInt(px.getItem(root, "ServerPort"));
		this.timecode = Integer.parseInt(px.getItem(root, "TimeCode"));
		this.timeslide = Integer.parseInt(px.getItem(root, "TimeSlide"));
		
		this.enableSlideImages = 1;//Integer.parseInt(px.getItem(root, "HeadCode"));
		this.enableSlideVideos = 1;//Integer.parseInt(px.getItem(root, "HeadCode"));
		this.enablePhone = 1;//Integer.parseInt(px.getItem(root, "HeadCode"));
		
	}
	public String getServerIp()
	{
		return this.serverip;
	}
	public int getServerPort()
	{
		return this.serverport;
	}
	public int getTimeCode()
	{
		return this.timecode;
	}
	public int getTimeSlide()
	{
		return this.timeslide;
	}
	public String getWorkDirectory()
	{
		return this.work_directory;
	}
	public String getImageDirectory()
	{
		return this.image_directory;
	}
	public String getVideoDirectory()
	{
		return this.video_directory;
	}
	public String getPortCOM()
	{
		return this.portcom;
	}
	public int getBaudRate()
	{
		return this.baudrate;
	}
	public int getHeadCode()
	{
		return this.headcode;
	}
	public int getEnableSlideImages() {
		return enableSlideImages;
	}
	public int getEnableSlideVideos() {
		return enableSlideVideos;
	}
	public int getEnablePhone() {
		return enablePhone;
	}
	private void CreateDefaultConfig()
	{
		try{
			FileOutputStream fos = this.context.openFileOutput(this.filename, context.MODE_PRIVATE);
			StringBuffer sb = new StringBuffer();

			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			sb.append("<AquaDroid>\n");
			sb.append("<WorkingDirectory>/sdcard/AquaDROID/</WorkingDirectory>");
			sb.append("<ImageDirectory>slideshow_images/</ImageDirectory>");
			sb.append("<VideoDirectory>slideshow_videos/</VideoDirectory>");
			sb.append("<PortCOM>/dev/s3c2410_serial0</PortCOM>");
			sb.append("<BaudRate>9600</BaudRate>");
			sb.append("<HeadCode>1</HeadCode>");
			sb.append("<ServerIP>192.168.100.3</ServerIP>\n");
			sb.append("<ServerPort>1234</ServerPort>\n");
			sb.append("<TimeCode>10</TimeCode>\n");
			sb.append("<TimeSlide>15</TimeSlide>\n");
			sb.append("<EnableSlideImages>1</EnableSlideImages>\n");
			sb.append("<EnableSlideVideos>1</EnableSlideVideos>\n");
			sb.append("<EnablePhone>1</EnablePhone>\n");
			sb.append("</AquaDroid>");
			
			fos.write(sb.toString().getBytes());
			fos.close();
			
		}catch(IOException e){
			Log.i(TAG, "Cannot open file: " + e.toString());
		}
	}
}
