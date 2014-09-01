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
public class ContactSettings {
	private static final String TAG = "CONFIG";
	public Contact[] contacts;
	private String filename = "contacts.xml";
	private String path = "/sdcard/AquaDROID/settings/";
	
	public ContactSettings(String path)
	{	
		this.path = path;
		try{
			FileInputStream fis = new FileInputStream(new File(this.path, filename));
			fis.close();
		}catch(FileNotFoundException e){
			Log.e(TAG, "Create Config = "+ e);
			CreateDefaultConfig();
			
		}catch(IOException e){
			
		}
		
		contacts = new Contact[10];
		ParseContacts px = new ParseContacts(path, filename);
		px.getContacts();
	}
	
	private void CreateDefaultConfig()
	{
		try{
			File fileDir = new File(path);
			fileDir.mkdirs();
			FileOutputStream fos = new FileOutputStream(new File(fileDir, filename));
			StringBuffer sb = new StringBuffer();
			
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			sb.append("<contacts>\n");
				sb.append("<contact id=\"1\" enabled=\"1\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>JARDIN</surname>\n");
				sb.append("<phone>132</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"2\" enabled=\"1\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>AUTO BRICO</surname>\n");
				sb.append("<phone>155</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"3\" enabled=\"1\">\n");
				sb.append("<firstname>HABILLEMENT</firstname>\n");
				sb.append("<surname></surname>\n");
				sb.append("<phone>158</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"4\" enabled=\"0\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>JOUET</surname>\n");
				sb.append("<phone>3418</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"5\" enabled=\"0\">\n");
				sb.append("<firstname>EPICERIE</firstname>\n");
				sb.append("<surname>SUCREE</surname>\n");
				sb.append("<phone>3421</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"6\" enabled=\"0\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>BOUCHERIE</surname>\n");
				sb.append("<phone>3481</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"7\" enabled=\"0\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>BAGAGES</surname>\n");
				sb.append("<phone>3407</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"8\" enabled=\"0\">\n");
				sb.append("<firstname>RAYON</firstname>\n");
				sb.append("<surname>LIQUIDES</surname>\n");
				sb.append("<phone>3415</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"9\" enabled=\"0\">\n");
				sb.append("<firstname>EPICERIE</firstname>\n");
				sb.append("<surname>SALEE</surname>\n");
				sb.append("<phone>3421</phone>\n");
				sb.append("</contact>\n");
				
				sb.append("<contact id=\"10\" enabled=\"0\">\n");
				sb.append("<firstname>CHARCUTERIE</firstname>\n");
				sb.append("<surname></surname>\n");
				sb.append("<phone>3478</phone>\n");
				sb.append("</contact>\n");
			sb.append("</contacts>");
			
			fos.write(sb.toString().getBytes());
			fos.close();
			
		}catch(IOException e){
			Log.i(TAG, "Cannot open file: " + e.toString());
		}
	}

	private class ParseContacts {
		private String filename;
		private String path;
		
		public ParseContacts(String path, String filename)
		{
			this.path = path;
			this.filename = filename;
			
		}
		public void getContacts()
		{
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				File file= new File(path, filename);
				Document doc = db.parse(file);
				doc.getDocumentElement().normalize();
				
				NodeList nodeList = doc.getElementsByTagName("contact");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					Element fstElmnt = (Element) node;
					
					String username = "";
					String displayname = "";
					String enabled = "0";
					
					enabled = fstElmnt.getAttribute("enabled");
					
					try{
						NodeList nameList1 = fstElmnt.getElementsByTagName("firstname");
						Element nameElement1 = (Element) nameList1.item(0);
						nameList1 = nameElement1.getChildNodes();
						displayname = ((Node) nameList1.item(0)).getNodeValue();
					}catch(Exception e){
					}
					
					try{
						NodeList nameList2 = fstElmnt.getElementsByTagName("surname");
						Element nameElement2 = (Element) nameList2.item(0);
						nameList2 = nameElement2.getChildNodes();
						displayname = displayname + " " + (String)((Node) nameList2.item(0)).getNodeValue();
					}catch(Exception e){
						
					}
					
					try{
					NodeList nameList3 = fstElmnt.getElementsByTagName("phone");
					Element nameElement3 = (Element) nameList3.item(0);
					nameList3 = nameElement3.getChildNodes();
					username = username + ((Node) nameList3.item(0)).getNodeValue();
					}catch(Exception e){
						
					}
					
					contacts[Integer.parseInt(fstElmnt.getAttribute("id"))-1] = new Contact(username, displayname, enabled);
				}
				
			}catch (Exception e) {
				contacts[0] = new Contact("", "Error", "1");
				contacts[1] = new Contact("", "Error", "1");
				contacts[2] = new Contact("", "", "0");
				contacts[3] = new Contact("", "", "0");
				contacts[4] = new Contact("", "", "0");
				contacts[5] = new Contact("", "", "0");
				contacts[6] = new Contact("", "", "0");
				contacts[7] = new Contact("", "", "0");
				contacts[8] = new Contact("", "", "0");
				contacts[9] = new Contact("", "", "0");
			}
		}
	}
	
	public class Contact {
		private String Username;
		private String DisplayName;
		private String Enabled;
		
		Contact(String username, String displayname, String enabled){
			Username = username;
			DisplayName = displayname;
			Enabled = enabled;
		}
		
		public String getUsername() {
			return Username;
		}
		public void setUsername(String username) {
			Username = username;
		}
		public String getDisplayName() {
			return DisplayName;
		}
		public void setDisplayName(String displayName) {
			DisplayName = displayName;
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
