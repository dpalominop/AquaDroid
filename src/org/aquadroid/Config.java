package org.aquadroid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

@SuppressLint("SdCardPath")
public class Config {
	private static final String TAG = "CONFIG";
	private String filename = "terminal.xml";
	private String root = "AquaDroid";
	private String settings_directory = "settings";
	//private Context context;
	private String serverip;
	private int serverport;
	private int timecode;
	private int timeslide;
	private String work_directory = "/sdcard/AquaDROID/";
	private String image_directory = "slideshow_images/";
	private String video_directory = "slideshow_videos/";
	private String button_directory = "button_images/";
	private String sounds_directory = "sounds/";
	private String portcom = "/dev/s3c2410_serial0";
	private int baudrate = 9600;
	private int headcode = 1;
	
	private int enableSlideImages = 1;
	private int enableSlideVideos = 1;
	private int enablePhone = 1;
	
	public Config(Context context)
	{
		try{
			FileInputStream fis = new FileInputStream(new File(work_directory+"/"+settings_directory, filename));
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		ParseXML px = new ParseXML(work_directory+"/"+settings_directory, filename);
		work_directory = px.getItem(root, "WorkingDirectory", "/sdcard/AquaDROID/");
		Log.i(TAG, "directory: " + work_directory);
		
		image_directory = px.getItem(root, "ImageDirectory", "slideshow_images/");
		video_directory = px.getItem(root, "VideoDirectory", "slideshow_videos/");
		button_directory = px.getItem(root, "ButtonDirectory", "button_images/");
		sounds_directory = px.getItem(root, "SoundsDirectory", "sounds/");
		
		portcom = px.getItem(root, "PortCOM", "/dev/s3c2410_serial0");
		baudrate = Integer.parseInt(px.getItem(root, "BaudRate", "9600"));
		headcode = Integer.parseInt(px.getItem(root, "HeadCode", "1"));
		serverip = px.getItem(root, "ServerIP", "192.168.100.3");
		Log.i(TAG, "serverip: " + this.serverip);
		serverport = Integer.parseInt(px.getItem(root, "ServerPort", "1234"));
		
		timecode = Integer.parseInt(px.getItem(root, "TimeCode", "15"));
		timeslide = Integer.parseInt(px.getItem(root, "TimeSlide", "10"));
		
		enableSlideImages = Integer.parseInt(px.getItem(root, "EnableSlideImages", "1"));
		enableSlideVideos = Integer.parseInt(px.getItem(root, "EnableSlideVideos", "1"));
		enablePhone = Integer.parseInt(px.getItem(root, "EnablePhone", "1"));
	}
	public String getServerIp()
	{
		return serverip;
	}
	public int getServerPort()
	{
		return serverport;
	}
	public int getTimeCode()
	{
		return timecode;
	}
	public int getTimeSlide()
	{
		return timeslide;
	}
	public String getWorkDirectory()
	{
		return work_directory;
	}
	public String getImageDirectory()
	{
		return image_directory;
	}
	public String getVideoDirectory()
	{
		return video_directory;
	}
	public String getSoundsDirectory(){
		return sounds_directory;
	}
	public String getSettingsDirectory(){
		return settings_directory;
	}
	public String getButtonDirectory(){
		return button_directory;
	}
	public String getPortCOM()
	{
		return portcom;
	}
	public int getBaudRate()
	{
		return baudrate;
	}
	public int getHeadCode()
	{
		return headcode;
	}
	public Boolean getEnableSlideImages() {
		if(enableSlideImages==1){
			return true;
		}else{
			return false;
		}
	}
	public Boolean getEnableSlideVideos() {
		if(enableSlideVideos==1){
			return true;
		}else{
			return false;
		}
	}
	public Boolean getEnablePhone() {
		if(enablePhone==1){
			return true;
		}else{
			return false;
		}
	}
	
	private void CreateDefaultConfig()
	{
		try{
			//FileOutputStream fos = this.context.openFileOutput(this.filename, context.MODE_PRIVATE);
			File fileDir = new File(work_directory+"/"+settings_directory);
			fileDir.mkdirs();
			FileOutputStream fos = new FileOutputStream(new File(fileDir, filename));
			StringBuffer sb = new StringBuffer();

			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			sb.append("<AquaDroid>\n");
			sb.append("<WorkingDirectory>/sdcard/AquaDROID/</WorkingDirectory>\n");
			sb.append("<ImageDirectory>slideshow_images/</ImageDirectory>\n");
			sb.append("<VideoDirectory>slideshow_videos/</VideoDirectory>\n");
			sb.append("<PortCOM>/dev/s3c2410_serial0</PortCOM>\n");
			sb.append("<BaudRate>9600</BaudRate>\n");
			sb.append("<HeadCode>1</HeadCode>\n");
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
