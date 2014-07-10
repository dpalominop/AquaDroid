package org.aquadroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class Display {
	private static final String TAG = "Display";
	private Context ctx;
	private byte[] data;
	private String strdata = "";
	private String ForeImgPath = "";
	private String BackImgPath = "";
	
	public Display(byte[] data)
	{
		this.data = data;
	}
	public Display(String strdata)
	{
		this.strdata = strdata;
	}
	public void setContext(Context context){
		this.ctx = context;
	}
	public boolean CreateDisplayPage(String htmlpage)
	{
		Log.e(TAG,"byte size ="+data.length);
			
		boolean status=true;
		int posi=0;
		int posf;
		String tmp;
		int slen=data.length;
		int command;
		int imgindex=0;
		int cnt=0;
		try{
			//String str = new String(data,"ASCII");
			String str = new String(data,"ISO-8859-1");
			Log.e(TAG,"string size ="+str.length());
			//Log.e(TAG,"str ="+str);
			
			FileOutputStream fos = this.ctx.openFileOutput(htmlpage, this.ctx.MODE_PRIVATE);
			fos.write(this.HeadScriptHTML().getBytes());
			if(this.BackImgPath.length() > 0){
				String body = "<BODY BACKGROUND=\""+this.BackImgPath+"\">\n";
				fos.write(body.getBytes());
			}else{
				fos.write("<BODY>\n".getBytes());
			}
			while(status){
				//check loop
				if(cnt > 20){
					fos.close();
					return false;
				}
				if(posi >= slen){
					break;
				}
				if((posf=str.indexOf('\t', posi)) < 0){
					break;
				}
				tmp = str.substring(posi, posf);
				tmp = tmp.replaceAll("\\D+","");
				command=Integer.parseInt(tmp);
				
				switch(command){
				case 25:
					String[] msg = new String[15];
					int pos11=posf+1;
					int pos12;
					for (int i=0; i<10; i++){
						if(i==9){
							if((pos12=str.indexOf('\n', pos11)) > 0){
								msg[i]=str.substring(pos11, pos12);
							}else{
							}
						}else{
							if((pos12=str.indexOf('\t', pos11)) > 0){
								msg[i]=str.substring(pos11, pos12);
							}else{
							
							}
						}
						pos11=pos12+1;
					}
					posi=pos11;
					
					StringBuffer sb = new StringBuffer();
					int effect = Integer.parseInt(msg[8]);
					switch(effect){
						case 0:
							sb.append("<SPAN style=\"position:absolute;left:"+msg[0]+"px;top:"+msg[1]+"px;");
							break;
						case 1:
							sb.append("<blink style=\"position:absolute;left:"+msg[0]+"px;top:"+msg[1]+"px;");
							break;
						case 2:
							sb.append("<MARQUEE style=\"position:absolute;left:"+msg[0]+"px;top:"+msg[1]+"px;");
							break;
						default:
							sb.append("<SPAN style=\"position:absolute;left:"+msg[0]+"px;top:"+msg[1]+"px;");
							break;
					}
					
					// Output background color
					sb.append(BackgroundColor(msg[2]));
                                        
					// Output foreground color
					sb.append(ForegroundColor(msg[3]));
                    
                 // Output font size
                    sb.append("font-size:"+msg[4]+"pt;");
                    
                 // Output font family
                    sb.append("font-family:arial;");
                 
                 // Output font style or font weight
                    sb.append(Style(msg[6]));
                    
                 // Output text
                    if(msg[9].indexOf((char)0xa4) > 0){
                    	msg[9] = msg[9].replaceAll("\\xA4", "&#8364");
                    }
                    if(msg[9].indexOf("&#164;") > 0){
                    	msg[9] = msg[9].replaceAll("&#164", "&#8364");
                    }
                    switch(effect){
                    	case 0:
                    		sb.append("\">\n"+msg[9]+"\n</SPAN>\n");
                    		break;
                    	case 1:
                    		sb.append("\">\n"+msg[9]+"\n</blink>\n");
                    		break;
                    	case 2:
                    		sb.append("\">\n"+msg[9]+"\n</MARQUEE>\n");
                    		break;
                    	default:
                    		sb.append("\">\n"+msg[9]+"\n</SPAN>\n");
                    		break;
                    }
                    
                    fos.write(sb.toString().getBytes());
                    
					break;
				
				case 27:
					//Log.e(TAG,"command 27");
					String[] img = new String[15];
                    int pos21=posf+1;
                    int pos22;
                    for (int i=0; i<7; i++){
                            if((pos22=str.indexOf('\t', pos21)) > 0){
                            	img[i]=str.substring(pos21, pos22);
                            }else{
                            	fos.close();
                            	return false;
                            }
                            pos21=pos22+1;
                    }

                    int L=Integer.parseInt(img[2]);
                    int C=Integer.parseInt(img[3]);
                    if(L<=0 || C<=0){
                    	fos.close();
                    	return false;
                    }
                    byte[] binbyte=new byte[L*C];
                    byte[] palbyte=new byte[768];
                    
                    int pos_2bi=pos21;
                    int pos_2bf=pos21+L*C;
                    //Log.e(TAG,"pos_bf ="+pos_bf);
                    if(pos_2bf > slen){
                    		fos.close();
                            return false;
                    }

                    for(int i=0;i<L*C;i++){
                    	binbyte[i]=data[pos_2bi+i];
                    }
                    
                    //String bindata = str.substring(pos_bi,pos_bf);
                    //Log.e(TAG,"bindata ="+stringToHex(bindata));
                    
                    int pos_2pi=pos_2bf;
                    int pos_2pf=pos_2pi+768;
                    //Log.e(TAG,"pos_pf ="+pos_pf);
                    if(pos_2pf > slen){
                    		fos.close();
                            return false;
                    }
                    for(int i=0;i<768;i++){
                    	palbyte[i]=data[pos_2pi+i];
                    }
                    //String paldata = str.substring(pos_pi,pos_pf);
                    //Log.e(TAG,"paldata ="+stringToHex(paldata));
                    String outfile = String.format("%s%d%s", "img",imgindex++,".bmp");
                    
                    ConvBMP primg = new ConvBMP(outfile);
                    primg.setContext(this.ctx);
                    //primg.SaveBinPalByte(bindata.getBytes("ASCII"), paldata.getBytes("ASCII"), L, C);
                    primg.SaveBinPalByte(binbyte, palbyte, L, C);
                    
                    // Output X, Y position
                    StringBuffer bm = new StringBuffer();
                    bm.append("<SPAN style=\"position:absolute;left:"+img[0]+"px;top:"+img[1]+"px;");
                    bm.append("\">\n<img src=\""+this.ctx.getFileStreamPath(outfile).toString()+"\">\n</SPAN>\n");
                    
                    fos.write(bm.toString().getBytes());
                    
                    posi=pos_2pf;
					break;
				case 28:
					Log.e(TAG,"command 28");
					String[] imgt = new String[15];
                    int pos31=posf+1;
                    int pos32;
                    for (int i=0; i<8; i++){
                            if((pos32=str.indexOf('\t', pos31)) > 0){
                            	imgt[i]=str.substring(pos31, pos32);
                            }else{
                            	fos.close();
                            	return false;
                            }
                            pos31=pos32+1;
                    }

                    int LT=Integer.parseInt(imgt[2]);
                    int CT=Integer.parseInt(imgt[3]);
                    if(LT<=0 || CT<=0){
                    	fos.close();
                    	return false;
                    }
                    byte[] tbinbyte=new byte[LT*CT];
                                        
                    int pos_3bi=pos31;
                    int pos_3bf=pos31+LT*CT;
                    Log.e(TAG,"pos_3bf ="+pos_3bf);
                    if(pos_3bf > slen){
                    		fos.close();
                            return false;
                    }

                    for(int i=0;i<LT*CT;i++){
                    	tbinbyte[i]=data[pos_3bi+i];
                    }
                    
                    String toutfile = String.format("%s%d%s", "img",imgindex++,".bmp");
                    
                    ConvBMP prtimg = new ConvBMP(toutfile);
                    prtimg.setContext(this.ctx);
                    //primg.SaveBinPalByte(bindata.getBytes("ASCII"), paldata.getBytes("ASCII"), L, C);
                    prtimg.SaveBinBytePalFile(tbinbyte, LT, CT);
                    
                    // Output X, Y position
                    StringBuffer bmt = new StringBuffer();
                    bmt.append("<SPAN style=\"position:absolute;left:"+imgt[0]+"px;top:"+imgt[1]+"px;");
                    bmt.append("\">\n<img src=\""+this.ctx.getFileStreamPath(toutfile).toString()+"\">\n</SPAN>\n");
                    
                    fos.write(bmt.toString().getBytes());
                    
                    posi=pos_3bf;
					break;
				default:
					status=false;
					break;
				}
				cnt++;
			}
			fos.write("</BODY>\n</HTML>\n".getBytes());
			fos.close();
			return true;
		}catch(IOException e){
			return false;
		}catch(NumberFormatException e){
			return false;
		}
	}
	private String ForegroundColor(String color)
	{
		if      (color.equalsIgnoreCase("0")) return "color:transparent;";
        else if (color.equalsIgnoreCase("5")) return "color:yellow;";
        else if (color.equalsIgnoreCase("35")) return "color:red;";
        else if (color.equalsIgnoreCase("185")) return "color:green;";
        else if (color.equalsIgnoreCase("206")) return "color:blue;";
        else if (color.equalsIgnoreCase("215")) return "color:black;";
        else return "color:red;";
	}
	private String BackgroundColor(String color)
	{
		if      (color.equalsIgnoreCase("0")) return "background-color:transparent;";
        else if (color.equalsIgnoreCase("5")) return "background-color:yellow;";
        else if (color.equalsIgnoreCase("35")) return "background-color:red;";
        else if (color.equalsIgnoreCase("185")) return "background-color:green;";
        else if (color.equalsIgnoreCase("206")) return "background-color:blue;";
        else if (color.equalsIgnoreCase("215")) return "background-color:black;";
        else return "background-color:red;";
	}
	public String Style(String style)
	{
		if      (style.equalsIgnoreCase("0")) return "font-style:normal;";
        else if (style.equalsIgnoreCase("1")) return "font-style:italic;";
        else if (style.equalsIgnoreCase("2")) return "font-weight:bold;";
        else return "font-style:normal;";
	}
	public void createWelcomePage(String htmlpage)
	{
		String tmp;
		int command;
		int posi=0;
		int pos1,posf;
		int len = this.strdata.length();
		int cnt=0;
		boolean status=true;
		String[] msgw = new String[15];
		String[] msga = new String[15];
		Config cfg = new Config(this.ctx);
		
		String rcv = this.strdata;
			
			
		while(status) {
			if(cnt >= len){
				break;
			}
			if((posf=rcv.indexOf('\t', posi)) < 0){
				break;
			}
			tmp = rcv.substring(posi, posf);
			tmp = tmp.replaceAll("\\D+","");
			command=Integer.parseInt(tmp);
			switch(command) {
				case 19:
					if((pos1=rcv.indexOf('\n', posf+1)) > 0){
						
					}else{
						
					}
					posi=pos1+1;
					break;
				case 20:
					int pos11=posf+1;
					int pos12;
					for (int i=0; i<10; i++){
						if(i==9){
							if((pos12=rcv.indexOf('\n', pos11)) > 0){
								msga[i]=rcv.substring(pos11, pos12);
							}else{
							}
						}else{
							if((pos12=rcv.indexOf('\t', pos11)) > 0){
								msga[i]=rcv.substring(pos11, pos12);
							}else{
						
							}
						}
						pos11=pos12+1;
					}
					posi=pos11;
					break;
				case 21:
					int pos21=posf+1;
					int pos22;
					for (int i=0; i<10; i++){
						if(i==9){
							if((pos22=rcv.indexOf('\n', pos21)) > 0){
								msgw[i]=rcv.substring(pos21, pos22);
							}else{
							}
						}else{
							if((pos22=rcv.indexOf('\t', pos21)) > 0){
								msgw[i]=rcv.substring(pos21, pos22);
							}else{
								
							}
						}
						pos21=pos22+1;
					}
					posi=pos21;
					break;
				default:
					status=false;
					break;
			}
			cnt++;
		}
		
		File directory = new File(cfg.getWorkDirectory());
		String[] fileNames = null;
		int bflen=0;
    	if (directory.isDirectory()) {
    		FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               	Pattern pattern = Pattern.compile("foreground|background\\.(jpg|jpeg|gif|png|bmp)",Pattern.CASE_INSENSITIVE);
                return pattern.matcher(name).find();
            }
    	};
    	
    	fileNames = directory.list(filter);
    	
    	bflen=fileNames.length;
    	}
    	try{
    		FileOutputStream fos = this.ctx.openFileOutput(htmlpage, this.ctx.MODE_PRIVATE);
    		fos.write(this.HeadScriptHTML().getBytes());
    	
    		String Foreground="";
			String Background="";
			
    		if(bflen > 0){
    			
    			boolean forestat=true;
    			boolean backstat=true;
    				
    			for(int i=0;i<bflen;i++){
    				if(fileNames[i].toLowerCase().startsWith("foreground") && forestat){
                           Foreground=fileNames[i];
                           this.ForeImgPath = cfg.getWorkDirectory()+Foreground;
                           forestat=false;
    				}else if(fileNames[i].toLowerCase().startsWith("background") && backstat){
                           Background=fileNames[i];
                           this.BackImgPath = cfg.getWorkDirectory()+Background;
                           backstat=false;
    				}
    			}
    		}	
    				
    				
    		StringBuffer sbw = new StringBuffer();
    		if(Foreground.length() > 0){
    			sbw.append("<BODY BACKGROUND=\""+cfg.getWorkDirectory()+Foreground+"\">\n");
    		}else{
    			sbw.append("<BODY>\n");
    		}
    		if(len > 0){
	    		int effect = Integer.parseInt(msgw[8]);
	    		switch(effect){
	    			case 0:
	    				sbw.append("<SPAN style=\"position:absolute;left:"+msgw[0]+"px;top:"+msgw[1]+"px;");
	    				break;
	    			case 1:
	    				sbw.append("<blink style=\"position:absolute;left:"+msgw[0]+"px;top:"+msgw[1]+"px;");
	    				break;
	    			case 2:
	    				sbw.append("<MARQUEE style=\"position:absolute;left:"+msgw[0]+"px;top:"+msgw[1]+"px;");
	    				break;
	    			default:
	    				sbw.append("<SPAN style=\"position:absolute;left:"+msgw[0]+"px;top:"+msgw[1]+"px;");
	    				break;
	    		}
	    				
	    		// Output background color
	            sbw.append(BackgroundColor(msgw[2]));
	                   
	            // Output foreground color
	            sbw.append(ForegroundColor(msgw[3]));
	                    
	            // Output font size
	            sbw.append("font-size:"+msgw[4]+"pt;");
	                    
	            // Output font family
	            sbw.append("font-family:arial;");
	                 
	            // Output font style or font weight
	            sbw.append(Style(msgw[6]));
	                    
	            // Output text
	            
	            switch(effect){
	               	case 0:
	               		sbw.append("\">\n"+msgw[9]+"\n</SPAN>\n");
	               		break;
	               	case 1:
	               		sbw.append("\">\n"+msgw[9]+"\n</blink>\n");
	               		break;
	               	case 2:
	               		sbw.append("\">\n"+msgw[9]+"\n</MARQUEE>\n");
	               		break;
	               	default:
	               		sbw.append("\">\n"+msgw[9]+"\n</SPAN>\n");
	               		break;
	            }
    		}            
	        fos.write(sbw.toString().getBytes());
    		fos.write("</BODY>\n</HTML>\n".getBytes());
        	fos.close();
                    
    	}catch(IOException e){
    				
    	}
	}
	public String getForeImgPath()
	{
		return this.ForeImgPath;
	}
	public void setForeImgPath(String path)
	{
		this.ForeImgPath = path;
	}
	public String getBackImgPath()
	{
		return this.BackImgPath;
	}
	public void setBackImgPath(String path)
	{
		this.BackImgPath = path;
	}
	
	private String HeadScriptHTML() {
		
		StringBuffer hd = new StringBuffer();
		hd.append("<HTML>\n");
		hd.append("<HEAD>\n");
		hd.append("<SCRIPT type=\"text/javascript\">\n");
		hd.append("<!--\n");
		hd.append("speed=750;\n");
		hd.append("blink=document.all.tags(\"blink\");\n");
		hd.append("swi=1;\n");
		hd.append("bringBackBlinky();\n");
		hd.append("function bringBackBlinky() {\n");
		hd.append("if (swi == 1) {\n");
		hd.append("sho=\"visible\";\n");
		hd.append("swi=0;\n");
		hd.append("}\n");
		hd.append("else {\n");
		hd.append("sho=\"hidden\";\n");
		hd.append("swi=1;\n");
		hd.append("}\n");
		hd.append("for(i=0;i<blink.length;i++) {\n");
		hd.append("blink[i].style.visibility=sho;\n");
		hd.append("}\n");
		hd.append("setTimeout(\"bringBackBlinky()\", speed);\n");
		hd.append("}\n");
		hd.append("// -->\n");
		hd.append("</SCRIPT>\n");
		hd.append("</HEAD>\n");
		
		return hd.toString();
	}
	
}
