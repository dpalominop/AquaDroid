package org.aquadroid;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParseXML {
	private String filename;
	private String path;
	
	public ParseXML(String path, String filename)
	{
		this.path = path;
		this.filename = filename;
		
	}
	public String getItem(String root, String field)
	{
		String data = "";
		
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//inputSource = new InputSource(new FileInputStream(new File(this.fileName)));
			//FileInputStream fs = ctx.openFileInput(this.filename);
			//Document doc = db.parse(new InputSource(new FileInputStream(new File(this.filename))));
			File file= new File(path, filename);
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList nodeList = doc.getElementsByTagName(root);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Element fstElmnt = (Element) node;
				NodeList nameList = fstElmnt.getElementsByTagName(field);
				Element nameElement = (Element) nameList.item(0);
				nameList = nameElement.getChildNodes();
				data = ((Node) nameList.item(0)).getNodeValue();
			}
			
		}catch (Exception e) {
			//System.out.println("XML Pasing Excpetion = " + e);
			data = "";
		}
		return data;
	}
	
}
