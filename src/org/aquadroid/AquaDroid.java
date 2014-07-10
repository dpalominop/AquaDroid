package org.aquadroid;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileDescriptor;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
//import android.os.PowerManager;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
//import android.widget.Button;
//import android.widget.EditText;
import android.util.Log;

public class AquaDroid extends Activity {
	private static final String TAG = "AquaDroid";
	private WebView webView;
	private Timer Timer;
	private TimerTask TimerTask;

	private String urls[];
	private String[] listslide;
	private String BackImgPath = "";
	private String ForeImgPath = "";
	public static String SERVERIP = "192.168.100.140";
	public static final int SERVERPORT = 8080;
	private Handler handler = new Handler();
	private ServerSocket serverSocket;
	private int total=0;
	int p=0;
	private String line = null;
	protected Scanner mScanner;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String mReception = "";
    private Context ctx;
    private Config cfg;
    private boolean codestatus = true;
//    private boolean isPaused = false;

    
	/** Called when the activity is first created. */
//    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);  
        lock.disableKeyguard();
        
        ctx = getApplicationContext();
        // Create reference to UI elements
        webView  = (WebView) findViewById(R.id.webview_compontent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginsEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.clearCache(true);
        //webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
        // workaround so that the default browser doesn't take over
        webView.setWebViewClient(new MyWebViewClient());
        
        cfg = new Config(ctx);
        getMessageServer();
        //openURL("http://www.google.com.pe");
        Log.e(TAG,"slide path ="+cfg.getWorkDirectory()+cfg.getSlideDirectory());
        List<String> slide = listFileByExt(cfg.getWorkDirectory()+cfg.getSlideDirectory());
        listslide = (String[]) slide.toArray(new String[0]);
        total = listslide.length;
        
        setListUrl();
        
        SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());

		SERVERIP = getLocalIpAddress();
		
		Thread fst = new Thread(new ServerThread());
		fst.start();
		
		mScanner = new Scanner(cfg.getPortCOM(),cfg.getBaudRate());
        try {
                mSerialPort = mScanner.getSerialPort();
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

                // Create a receiving thread 
                mReadThread = new ReadThread();
                mReadThread.start();
        } catch (Exception e) {
        }
        
 
    }
    private void setListUrl(){
    	urls = new String[5];
    	urls[0] = "http://www.google.com.pe";
    	urls[1] = "http://www.tcc.com.pe";
    	urls[2] = "http://www.gempsy.com";
    	urls[3] = "http://www.distriluz.com.pe";
    	urls[4] = "http://www.alfavia.com";
    }
    /** Opens the URL in a browser */
    private void openURL(String url) {
    	webView.loadUrl(url);
    	//webView.requestFocus();
    }
    private void openFileImage(String fileName){
    	String html = new String();
    	html = ("<!doctype html><html><head><meta name=\"viewport\" content=\"target-densitydpi=device-dpi\"/></head><center><img src=\""+fileName+"\"></html>" );
    	/* Finally, display the content using WebView */
    	webView.loadDataWithBaseURL("file://"+cfg.getWorkDirectory()+cfg.getSlideDirectory()+"/",html,"text/html","utf-8","");
    }
    private class MyWebViewClient extends WebViewClient {
//        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
		this.runOnUiThread(Timer_Tick);
	}

	private Runnable Timer_Tick = new Runnable() {
		public void run() {

			//This method runs in the same thread as the UI.    	       

			//Do something to the UI thread here
			//openURL(urls[p]);
			if(total > 0){
				openFileImage(listslide[p]);
				if(p >= total-1){
					p = 0;
				}else{
					p+=1;
				}	
			}
		}
	};
	public List<String> listFileByExt(String dir) {

		List<String> list = new ArrayList<String>();
		
		File directory = new File(dir);

		if (!directory.isDirectory()) {
			//System.out.println("Folder not found");
			return list;
		}

		// create an instance of FilenameFilter
		// and override its accept-method
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return Pattern.matches(".*\\.(jpg|jpeg|gif|png|bmp)", name);

				//return name.endsWith(ext);
			}
		};
		String[] fileNames = directory.list(filter);
		for (String fileName : fileNames) {
			//System.out.println(fileName);
			//list.add(directory+"/"+fileName);
			list.add(fileName);
		}
		return list;
	}

	public class ServerThread implements Runnable {
		public void run() {
			try {
				if (SERVERIP != null) {
			    	handler.post(new Runnable() {
			    		@Override
			    		public void run() {
			    			//serverStatus.setText("Listening on IP: " + SERVERIP);
			    			
			    		}
			    	});
			        serverSocket = new ServerSocket(SERVERPORT);
			        while (true) {
			        	// listen for incoming clients
			            Socket client = serverSocket.accept();
			            handler.post(new Runnable() {
			            	@Override
			            	public void run() {
			            		//serverStatus.setText("Connected.");
			            	}
			            });
			 
			            try {
			            	BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			                //String line = null;
			               // while ((line = in.readLine()) != null) {
			            	line = in.readLine();
			                	//Log.d("ServerActivity", line);
			                    handler.post(new Runnable() {
			                    	@Override
			                        public void run() {
			                    		// do whatever you want to the front end
			                    		// this is where you can be creative
			                    		
			                    		//String html = new String();
						    	    	//html = ("<!doctype html><html><head><meta name=\"viewport\" content=\"target-densitydpi=device-dpi\"/></head>"+line+"</html>" );
						    	    	/* Finally, display the content using WebView */
						    	    	//webView.loadDataWithBaseURL("file://"+directory+"/",html,"text/html","utf-8","");
						    	    	try{
						    	    		SlideShow(false,0,0);
						    	    		
						    	    		TcpClient aq = new TcpClient(cfg.getServerIp(),cfg.getServerPort(),10000);
						    	    		aq.sendData(line);
						    	    		//aq.writeFile(ctx,"receive");
						    	    		Display dis = new Display(aq.writeByte());
						    	    		dis.setContext(ctx);
						    	    		dis.setBackImgPath(BackImgPath);
						    	    		if(dis.CreateDisplayPage("view.html")){
						    	    			webView.clearCache(true);
						    	    			webView.loadUrl("file:///"+getFileStreamPath("view.html").toString());
						    	    	   						    	    			
						    	    		}
						    	    		SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());
						    	    		
						    	    	}catch(Exception e){
						    	    		SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());
						    	    	}
			                        }
			                    });
			                //}
			                //break;
			             } catch (Exception e) {
			            	 handler.post(new Runnable() {
			            		 @Override
			                     public void run() {
			            			 //serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
			                     }
			                 });
			                 e.printStackTrace();
			             }
			        }
			    } else {
			    	handler.post(new Runnable() {
			    		@Override
			            public void run() {
			    			//serverStatus.setText("Couldn't detect internet connection.");
			            }
			        });
			    }
			} catch (Exception e) {
				handler.post(new Runnable() {
			    @Override
			    	public void run() {
			        	//serverStatus.setText("Error");
			        }
			    });
			    e.printStackTrace();
			}
		}
		
			 
			    
	}
	private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            //Log.e("ServerActivity", ex.toString());
        }
        return null;
    }
	@Override
    protected void onStop() {
        super.onStop();
        try {
             // make sure you close the socket upon exiting
             serverSocket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
	private class ReadThread extends Thread {

        @Override
        public void run() {
                super.run();
                while(!isInterrupted()) {
                        int size;
                        try {
                                byte[] buffer = new byte[64];
                                if (mInputStream == null) return;
                                size = mInputStream.read(buffer);
                                if (size > 0) {
                                        onDataReceived(buffer, size);
                                }
                        } catch (IOException e) {
                                e.printStackTrace();
                                return;
                        }
                }
        }
	}
	
    protected void onDataReceived(final byte[] buffer, final int size) {
            runOnUiThread(new Runnable() {
                    public void run() {
                            //mReception.append(new String(buffer, 0, size));
                            String barcode = new String(buffer,0,size);
                            int i = barcode.indexOf('\r');
                            if(i>0){
                            		mReception += barcode.substring(0, i);
                            		if(codestatus){
                            			codestatus=false;
                            		
                            			try{
                            			
	                            			if(cfg.getHeadCode() == 1){
	                            				mReception = mReception.substring(1);
	                            			}
	                            			SlideShow(false,0,0);
							    	    		
							    	    	TcpClient aq = new TcpClient(cfg.getServerIp(),cfg.getServerPort(),10000);
							    	    	aq.sendData(mReception);
							    	    	//aq.writeFile(ctx,"receive");
							    	    	byte[] rdata = aq.writeByte();
							    	    	if(rdata.length > 0){
							    	    		Display dis = new Display(rdata);
							    	    		dis.setContext(ctx);
							    	    		dis.setBackImgPath(BackImgPath);
							    	    		if(dis.CreateDisplayPage("view.html")){
							    	    			webView.clearCache(true);
							    	    			webView.loadUrl("file:///"+getFileStreamPath("view.html").toString());
							    	    	   						    	    			
							    	    		}
							    	    	}
							    	    	SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());
							    	    	    	    		
						    	    		mReception = "";
						    	    		codestatus=true;
						    	    		
						    	    	}catch(Exception e){
						    	    		Log.e(TAG,"Exception = "+e.toString());
						    	    		SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());
						    	    		codestatus=true;
						    	    	}
                            		}
                            }else{
                            	mReception = barcode;
                            }
                            
                    }
            });
    }
    private void SlideShow(boolean status, int timecode, int timeslide){
    	if(status){
    		Timer = new Timer();
    		TimerTask = new TimerTask() {
    		//SlideTimer.schedule(new TimerTask() {
//    			@Override
    			public void run() {
    				TimerMethod();
    			}

    		};
    		Timer.schedule(TimerTask,timecode*1000, timeslide*1000);
    		
    	}else{
    		TimerTask.cancel();
	        TimerTask = null;
	        Timer.cancel();
    	}
    }
    private void getMessageServer()
    {
    	String msgdata = "001";
    	String rcv = "";
    	try{
    		Log.e(TAG, "msg init tcp");
    		TcpClient gms = new TcpClient(cfg.getServerIp(),cfg.getServerPort(),10000);
    		gms.sendData(msgdata);
    		rcv = gms.receiveData();
    		gms.close();
    	}catch(Exception e){
    		
    	}
    	Log.e(TAG, "msg server="+rcv);
    	Display dis = new Display(rcv);
    	dis.setContext(ctx);
    	dis.createWelcomePage("welcome.html");
    	this.BackImgPath = dis.getBackImgPath();
    	this.ForeImgPath = dis.getForeImgPath();
    	webView.clearCache(true);
		webView.loadUrl("file:///"+getFileStreamPath("welcome.html").toString());   		
    	
    	
    }
    @Override
    protected void onDestroy() {
            if (mReadThread != null)
                    mReadThread.interrupt();
            mScanner.closeSerialPort();
            mSerialPort = null;
            super.onDestroy();
    }
 
}