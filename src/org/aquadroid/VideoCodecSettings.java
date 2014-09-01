package org.aquadroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("SdCardPath")
public class VideoCodecSettings {
	private static final String TAG = "CODECS";
	public VideoCodec[] codecs;
	private String filename = "videocodecs.xml";
	private String path = "/sdcard/AquaDROID/settings/";
	
	public VideoCodecSettings(String PATH)
	{	
		path = PATH;
		try{
			FileInputStream fis = new FileInputStream(new File(path, filename));
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		
		codecs = new VideoCodec[9];
		ParseVideoCodecs px = new ParseVideoCodecs(path, filename);
		px.getVideoCodecs();
	}
	
	private void CreateDefaultConfig()
	{
		try{
			File fileDir = new File(path);
			fileDir.mkdirs();
			FileOutputStream fos = new FileOutputStream(new File(fileDir, filename));
			StringBuffer sb = new StringBuffer();
			
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			sb.append("<codecs>\n");
				sb.append("<codec id=\"0\" enabled=\"0\">\n");
				sb.append("<name>VP8</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"1\" enabled=\"0\">\n");
				sb.append("<name>H263</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"2\" enabled=\"0\">\n");
				sb.append("<name>H263-1998</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"3\" enabled=\"1\">\n");
				sb.append("<name>H264</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"4\" enabled=\"0\">\n");
				sb.append("<name>MP4V-ES</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"5\" enabled=\"0\">\n");
				sb.append("<name>theora</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"6\" enabled=\"0\">\n");
				sb.append("<name>x-snow</name>\n");
				sb.append("<clock>90000</clock>\n");
				sb.append("</codec>\n");
			sb.append("</codecs>\n");
			
			fos.write(sb.toString().getBytes());
			fos.close();
			
		}catch(IOException e){
			Log.i(TAG, "Cannot open file: " + e.toString());
		}
	}

	private class ParseVideoCodecs {
		private String filename;
		private String path;
		
		public ParseVideoCodecs(String path, String filename)
		{
			this.path = path;
			this.filename = filename;
			
		}
		public void getVideoCodecs()
		{
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				File file= new File(path, filename);
				Document doc = db.parse(file);
				doc.getDocumentElement().normalize();
				
				NodeList nodeList = doc.getElementsByTagName("codec");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					Element fstElmnt = (Element) node;
					
					String name = "ERROR CODEC";
					String rate = "0";
					
					String enabled = "0";
					
					enabled = fstElmnt.getAttribute("enabled");
					
					try{
						NodeList nameList1 = fstElmnt.getElementsByTagName("name");
						Element nameElement1 = (Element) nameList1.item(0);
						nameList1 = nameElement1.getChildNodes();
						name = ((Node) nameList1.item(0)).getNodeValue();
					}catch(Exception e){
					}
					
					try{
						NodeList nameList2 = fstElmnt.getElementsByTagName("clock");
						Element nameElement2 = (Element) nameList2.item(0);
						nameList2 = nameElement2.getChildNodes();
						rate = ((Node) nameList2.item(0)).getNodeValue();
					}catch(Exception e){
						
					}
					
					codecs[Integer.parseInt(fstElmnt.getAttribute("id"))] = new VideoCodec(name, rate, enabled);
				}
				
			}catch (Exception e) {
			}
		}
	}
	
	public class VideoCodec {
		private String Name;
		private String Rate;
		private String Enabled;
		
		VideoCodec(String name, String rate, String enabled){
			Name = name;
			Rate = rate;
			Enabled = enabled;
		}
		
		public String getName() {
			return Name;
		}
		public void setName(String name) {
			Name = name;
		}
		public String getRate() {
			return Rate;
		}
		public void setRate(String rate) {
			Rate = rate;
		}
		public String getEnabled() {
			return Enabled;
		}
		public void setEnabled(String enabled) {
			Enabled = enabled;
		}
	}
}
