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
public class AudioCodecSettings {
	private static final String TAG = "CODECS";
	public AudioCodec[] codecs;
	private String filename = "audiocodecs.xml";
	private String path = "/sdcard/AquaDROID/settings/";
	
	public AudioCodecSettings(String PATH)
	{	
		this.path = PATH;
		try{
			FileInputStream fis = new FileInputStream(new File(path, filename));
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		
		codecs = new AudioCodec[6];
		ParseAudioCodecs px = new ParseAudioCodecs(path, filename);
		px.getAudioCodecs();
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
				sb.append("<name>speex</name>\n");
				sb.append("<clock>8000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"1\" enabled=\"0\">\n");
				sb.append("<name>speex</name>\n");
				sb.append("<clock>16000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"2\" enabled=\"0\">\n");
				sb.append("<name>speex</name>\n");
				sb.append("<clock>32000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"3\" enabled=\"0\">\n");
				sb.append("<name>GSM</name>\n");
				sb.append("<clock>8000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"4\" enabled=\"0\">\n");
				sb.append("<name>PCMU</name>\n");
				sb.append("<clock>8000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"5\" enabled=\"0\">\n");
				sb.append("<name>PCMA</name>\n");
				sb.append("<clock>8000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"6\" enabled=\"0\">\n");
				sb.append("<name>L16</name>\n");
				sb.append("<clock>44100</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"7\" enabled=\"0\">\n");
				sb.append("<name>L16</name>\n");
				sb.append("<clock>44100</clock>\n");
				sb.append("<channel>stereo</channel>\n");
				sb.append("</codec>\n");
				sb.append("<codec id=\"8\" enabled=\"0\">\n");
				sb.append("<name>G722</name>\n");
				sb.append("<clock>8000</clock>\n");
				sb.append("<channel>mono</channel>\n");
				sb.append("</codec>\n");
			sb.append("</codecs>\n");
			
			fos.write(sb.toString().getBytes());
			fos.close();
			
		}catch(IOException e){
			Log.i(TAG, "Cannot open file: " + e.toString());
		}
	}

	private class ParseAudioCodecs {
		private String filename;
		private String path;
		
		public ParseAudioCodecs(String path, String filename)
		{
			this.path = path;
			this.filename = filename;
			
		}
		public void getAudioCodecs()
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
					String channel = "0";
					
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
					
					try{
					NodeList nameList3 = fstElmnt.getElementsByTagName("channel");
					Element nameElement3 = (Element) nameList3.item(0);
					nameList3 = nameElement3.getChildNodes();
					channel = ((Node) nameList3.item(0)).getNodeValue();
					}catch(Exception e){
						
					}
					
					codecs[Integer.parseInt(fstElmnt.getAttribute("id"))] = new AudioCodec(name, rate, channel, enabled);
				}
				
			}catch (Exception e) {
			}
		}
	}
	
	public class AudioCodec {
		private String Name;
		private String Rate;
		private String Channel;
		private String Enabled;
		
		AudioCodec(String name, String rate, String channel, String enabled){
			Name = name;
			Rate = rate;
			Channel = channel;
			Enabled = enabled;
		}
		
		public String getName() {
			return Name;
		}
		public void setName(String name) {
			Name = name;
		}
		public int getRate() {
			return Integer.parseInt(Rate);
		}
		public void setRate(String rate) {
			Rate = rate;
		}
		public int getChannel() {
			if(Channel.compareTo("mono")==0){
				return 1;
			}else{
				return 2;
			}
		}
		public void setChannel(String channel) {
			Channel = channel;
		}
		public Boolean getEnabled() {
			if(Enabled.compareTo("1")==0){
				return true;
			}else{
				return false;
			}
		}
		public void setEnabled(String enabled) {
			Enabled = enabled;
		}
	}
}