package org.aquadroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class ConfigSoftphone {
	private static final String TAG = "CONFIG";
	private String root = "sipRegistration";
	private String User = "155";
	private String Password = "pwd155";
	private String Domain = "192.168.100.15";
	private String sipVideo = "1";
	private String filename = "softphone.xml";
	private String path = "/sdcard/AquaDROID/settings/";
	
	public ConfigSoftphone(String path)
	{	
		this.path = path;
		try{
			//FileInputStream fis = this.context.openFileInput(this.filename);
			FileInputStream fis = new FileInputStream(new File(this.path, filename));
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		ParseXML px = new ParseXML(path, this.filename);
		this.User = px.getItem(root, "sipUserName");
		this.Password = px.getItem(root, "sipPassword");
		this.Domain = px.getItem(root, "sipDomain");
		this.sipVideo = px.getItem(root, "sipVideo");
	}
	
	public String getUser(){
		return this.User;
	}
	
	public String getPassword(){
		return this.Password;
	}
	
	public String getDomain(){
		return this.Domain;
	}
	
	public Boolean getSipVideo(){
		if(sipVideo.compareTo("1")==0){
			return true;
		}else{
			return false;
		}
	}
	
	private void CreateDefaultConfig()
	{
		try{
			//FileOutputStream fos = this.context.openFileOutput(this.filename, context.MODE_PRIVATE);
			File fileDir = new File(path);
			fileDir.mkdirs();
			FileOutputStream fos = new FileOutputStream(new File(fileDir, filename));
			StringBuffer sb = new StringBuffer();

			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			sb.append("<softphone>\n");
			sb.append("<sipRegistration>\n");
			sb.append("<sipDomain>192.168.100.15</sipDomain>\n");
			sb.append("<sipUserName>155</sipUserName>\n");
			sb.append("<sipPassword>pwd155</sipPassword>\n");
			sb.append("<sipVideo>1</sipVideo>\n");
			sb.append("</sipRegistration>\n");
			sb.append("</softphone>");
			
			fos.write(sb.toString().getBytes());
			fos.close();
			
		}catch(IOException e){
			Log.i(TAG, "Cannot open file: " + e.toString());
		}
	}

}
