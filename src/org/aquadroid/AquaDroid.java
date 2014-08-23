package org.aquadroid;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

@SuppressWarnings("unused")
public class AquaDroid extends Activity {
	private static final String TAG = "AquaDroid";
	private WebView webView;
	private Timer MyTimer = null;
	private TimerTask MyTimerTask = null;
	private int timeTableCaller = 10;
	
	private Button MainButton;
	private ImageView welcome;
	private ImageView imageView;
	private VideoView videoView;
	private LinearLayout PanelAnexos;
	private Button Anexo1;
	private Button Anexo2;
	private Button Anexo3;
	private Button Anexo4;
	private Button Anexo5;
	private Button Anexo6;
	private Button Anexo7;
	private Button Anexo8;
	private GLSurfaceView video;
	private SurfaceView capture;
	private SoftphoneManager mManager;
	
	private DisplayMetrics dm;
	//private SurfaceView sur_View;
	//private MediaController media_Controller;

	private String urls[];
	private String[] list_images;
	private String[] list_videos;
	private String BackImgPath = "";
	private String ForeImgPath = "";
	public static String SERVERIP = "192.168.100.140";
	public static final int SERVERPORT = 8080;
	private Handler handler = new Handler();
	private ServerSocket serverSocket;
	private int total_images=0;
	private int total_videos=0;
	int p=0, v=0;
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
    STATE aqState = STATE.WELCOME;
    STATE currentAqState;
    private int TableCallerTime = 10;

    
	/** Called when the activity is first created. */
    @Override
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
        cfg = new Config(ctx);
        Log.e(TAG,"slide path ="+cfg.getWorkDirectory()+cfg.getImageDirectory());
        
        MainButton = (Button) findViewById(R.id.MainButton);
        welcome = (ImageView) findViewById(R.id.welcome);
        imageView = (ImageView) findViewById(R.id.image_view);
        videoView = (VideoView) findViewById(R.id.video_view);
        webView  = (WebView) findViewById(R.id.web_view);
        PanelAnexos = (LinearLayout) findViewById(R.id.PanelAnexos);
        Anexo1 = (Button) findViewById(R.id.anexo1);
        Anexo2 = (Button) findViewById(R.id.anexo2);
        Anexo3 = (Button) findViewById(R.id.anexo3);
        Anexo4 = (Button) findViewById(R.id.anexo4);
        Anexo5 = (Button) findViewById(R.id.anexo5);
        Anexo6 = (Button) findViewById(R.id.anexo6);
        Anexo7 = (Button) findViewById(R.id.anexo7);
        Anexo8 = (Button) findViewById(R.id.anexo8);
        video = (GLSurfaceView) findViewById(R.id.video_surface);
        capture = (SurfaceView) findViewById(R.id.video_capture_surface);
        video.setVisibility(View.INVISIBLE);
        capture.setVisibility(View.INVISIBLE);
        
        init_layout_from_cfg();
        
        getMessageServer();

        setListUrl();
        
        setAqState(STATE.WELCOME);
		setState(aqState);
		launchState(aqState);
        //SlideShow(true,cfg.getTimeCode(),cfg.getTimeSlide());

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
    
    synchronized public void setAqState(STATE newState){
    	this.aqState = newState;
    }
    
    synchronized public void setCurrentAqState(STATE currentState){
    	Log.e("AquaDroid", "Estado actual: " + aqState.toString());
    	this.currentAqState = currentState;
    }
    
    public enum STATE {
    	WELCOME, CATALOG_PICTURE,
    	CATALOG_VIDEO, BARCODE,
    	TABLE_CALLER, WAIT_RESPONSE,
    	CALL, INCALL
    }
    
    public void setState(STATE aqState){
    	switch(aqState){
    		case WELCOME:
    			welcome.setVisibility(View.VISIBLE);
    			if(cfg.getEnablePhone()){
    				MainButton.setVisibility(View.VISIBLE);
    			}else{
    				MainButton.setVisibility(View.INVISIBLE);
    			}
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case CATALOG_PICTURE:
    			welcome.setVisibility(View.INVISIBLE);
    			imageView.setVisibility(View.VISIBLE);
    			if(cfg.getEnablePhone()){
    				MainButton.setVisibility(View.VISIBLE);
    			}else{
    				MainButton.setVisibility(View.INVISIBLE);
    			}
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case CATALOG_VIDEO:
    			welcome.setVisibility(View.INVISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.VISIBLE);
    			if(cfg.getEnablePhone()){
    				MainButton.setVisibility(View.VISIBLE);
    			}else{
    				MainButton.setVisibility(View.INVISIBLE);
    			}
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case BARCODE:
    			welcome.setVisibility(View.INVISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.VISIBLE);
    			if(cfg.getEnablePhone()){
    				MainButton.setVisibility(View.VISIBLE);
    			}else{
    				MainButton.setVisibility(View.INVISIBLE);
    			}
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case TABLE_CALLER:
    			welcome.setVisibility(View.INVISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.VISIBLE);
    			MainButton.setVisibility(View.VISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case WAIT_RESPONSE:
    			welcome.setVisibility(View.VISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			MainButton.setVisibility(View.VISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		case CALL:
    			welcome.setVisibility(View.INVISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			MainButton.setVisibility(View.VISIBLE);
    			video.setVisibility(View.VISIBLE);
		        capture.setVisibility(View.VISIBLE);
    			break;
    			
    		case INCALL:
    			welcome.setVisibility(View.VISIBLE);
    			imageView.setVisibility(View.INVISIBLE);
    			videoView.setVisibility(View.GONE);
    			videoView.setVisibility(View.INVISIBLE);
    			webView.setVisibility(View.INVISIBLE);
    			PanelAnexos.setVisibility(View.INVISIBLE);
    			MainButton.setVisibility(View.VISIBLE);
    			video.setVisibility(View.INVISIBLE);
		        capture.setVisibility(View.INVISIBLE);
    			break;
    			
    		default:
    			break;
    	}
    }
    
    public void launchState(STATE aqState){
		setCurrentAqState(aqState);
    	switch(aqState){
			case WELCOME:
				stopSlideTimer();
				if(cfg.getEnableSlideImages()){
					setAqState(STATE.CATALOG_PICTURE);
					p=0;
				}else if(cfg.getEnableSlideVideos()){
					setAqState(STATE.CATALOG_VIDEO);
				}
				initSlideTimer(cfg.getTimeSlide(), cfg.getTimeSlide());
				break;
				
			case CATALOG_PICTURE:
		    	if(total_images > 0){
					loadImageFile(list_images[p]);
					if(p >= total_images-1){
						p = 0;
						if(cfg.getEnableSlideVideos()){
							setAqState(STATE.CATALOG_VIDEO);
						}else{
							setAqState(STATE.WELCOME);
						}
					}else{
						p+=1;
					}	
				}
				break;
				
			case CATALOG_VIDEO:
				stopSlideTimer();
				if(total_videos > 0 && v<=(total_videos-1)){
					playVideo(list_videos[v]);
					v++;
				}else{
					v=0;
					setAqState(STATE.WELCOME);
					setState(this.aqState);
					launchState(this.aqState);
				}
				break;
				
			case BARCODE:
				stopSlideTimer();
				setAqState(STATE.WELCOME);
				initSlideTimer(cfg.getTimeCode(), cfg.getTimeSlide());
				break;
				
			case TABLE_CALLER:
				stopSlideTimer();
				setAqState(STATE.WELCOME);
				initSlideTimer(TableCallerTime, cfg.getTimeSlide());
				break;
				
			case WAIT_RESPONSE:
				stopSlideTimer();
				break;
				
			case CALL:
				stopSlideTimer();
				break;
				
			case INCALL:
				stopSlideTimer();
				break;
				
			default:
				break;
    	}
    }
    
    public void slotMainButton(View view){
    	switch(currentAqState){
    		case WELCOME:
    			stopSlideTimer();
    			setAqState(STATE.TABLE_CALLER);
    			setState(aqState);
    			launchState(aqState);
    			break;
    		case CATALOG_PICTURE:
    			stopSlideTimer();
    			setAqState(STATE.TABLE_CALLER);
    			setState(aqState);
    			launchState(aqState);
    			break;
			case CATALOG_VIDEO:
				//stopSlideTimer();
				if(videoView.isPlaying()){
    				videoView.stopPlayback();
    				//videoView.setVisibility(View.INVISIBLE);
    			}
				v=0;
				setAqState(STATE.TABLE_CALLER);
    			setState(aqState);
    			launchState(aqState);
				break;
    		case BARCODE:
    			stopSlideTimer();
    			setAqState(STATE.TABLE_CALLER);
    			setState(aqState);
    			launchState(aqState);
    			break;
    		case TABLE_CALLER:
    			stopSlideTimer();
    			setAqState(STATE.WELCOME);
    			setState(aqState);
    			launchState(aqState);
    		case CALL:
    			mManager.hangOut();
    			stopSlideTimer();
    			setAqState(STATE.WELCOME);
    			setState(aqState);
    			launchState(aqState);
    		default:
    			//stopSlideTimer();
    			//setAqState(STATE.CATALOG_PICTURE);
    			//initSlideTimer(0, cfg.getTimeCode());
    			break;
    	}
    }
    
	public void playVideo(String video) {
		videoView.setVideoPath(cfg.getWorkDirectory()+cfg.getVideoDirectory() + "/" + video);
		videoView.start();
	}
    
    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
       } catch (IOException e) {
           Log.e(TAG, "Error getting bitmap", e);
       }
       return bm;
    } 
    
    @SuppressLint("SetJavaScriptEnabled")
	private void init_layout_from_cfg(){
    	// Create reference to UI elements

        welcome.setImageBitmap(getImageBitmap("file://" + cfg.getWorkDirectory() + "/foreground.jpg"));
        welcome.setVisibility(View.VISIBLE);
        
        if(cfg.getEnableSlideImages()){
	        List<String> slide = listFileImagesByExt(cfg.getWorkDirectory()+cfg.getImageDirectory());
	        list_images = (String[]) slide.toArray(new String[0]);
	        total_images = list_images.length;
        }
    	imageView.setVisibility(View.INVISIBLE);
        
        if(cfg.getEnableSlideVideos()){
    		
        	List<String> slide = listFileVideosByExt(cfg.getWorkDirectory()+cfg.getVideoDirectory());
	        list_videos = (String[]) slide.toArray(new String[0]);
	        total_videos = list_videos.length;

	        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
	        {           
	            public void onCompletion(MediaPlayer mp) 
	            {
	                // Do whatever u need to do here
	            	Log.d("player", "playback complete");
	        		

	        		/*if (videoView != null) {
	                    mp = null;             
	                    videoView.stopPlayback();
	                }*/

	                /*if (v > total_videos - 1) {
	                    v = 0;
	                    initSlideTimer(0, cfg.getTimeSlide());
	                }else{
	                	playVideo(list_videos[v]);
	                	v++;
	                }*/
	            	
	            	initSlideTimer(0, cfg.getTimeSlide());

	            }           
	        });
        }

    	//media_Controller = new MediaController(this);
		dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int height = dm.heightPixels;
		int width = dm.widthPixels;
		videoView.setMinimumWidth(width);
		videoView.setMinimumHeight(height);
		//videoView.setMediaController(media_Controller);
    	videoView.setVisibility(View.INVISIBLE);
        
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginsEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.clearCache(true);
        //webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
        // workaround so that the default browser doesn't take over
        webView.setWebViewClient(new MyWebViewClient());
        webView.setVisibility(View.INVISIBLE);
        
		MainButton.setVisibility(View.INVISIBLE);
        
        if(cfg.getEnablePhone()){
        	mManager = new SoftphoneManager(this, video, capture, cfg);
            MainButton.setVisibility(View.VISIBLE);
            MainButton.setOnClickListener(new OnClickListener() {
    			public void onClick(View view){
    				//lanzarFin(view);
    				slotMainButton(null);
    				//Log.e(TAG, "Lanzando evento de boton");
    			}
    		});
            
            Anexo1.setOnClickListener(new OnClickListener() {
    			public void onClick(View view){
    				stopSlideTimer();
        			setAqState(STATE.CALL);
        			setState(aqState);
        			launchState(aqState);
    				mManager.makeCall("sip:132@192.168.100.15");
    			}
    		});
    		
    		Anexo2.setOnClickListener(new OnClickListener() {
    			public void onClick(View view){
    				stopSlideTimer();
        			setAqState(STATE.CALL);
        			setState(aqState);
        			launchState(aqState);
    				mManager.makeCall("sip:158@192.168.100.15");
    			}
    		});
    		
    		Anexo3.setOnClickListener(new OnClickListener() {
    			public void onClick(View view){
    				stopSlideTimer();
        			setAqState(STATE.CALL);
        			setState(aqState);
        			launchState(aqState);
    				mManager.makeCall("sip:157@192.168.100.15");
    			}
    		});
        }else{
        	MainButton.setVisibility(View.INVISIBLE);
        }
    }
    
    private void loadImageFile(String fileName){
        imageView.setImageBitmap(getImageBitmap("file://" + cfg.getWorkDirectory()+cfg.getImageDirectory() + fileName));
    }

	private Runnable Timer_Tick = new Runnable() {
		@Override
		public void run() {
			//This method runs in the same thread as the UI.    	       
			//Do something to the UI thread here
			//Log.e(TAG, "my timer emmit: " + aqState.toString());
			
			setState(aqState);
			launchState(aqState);
		}
	};
	
    private void TimerMethod()
	{
		//This method is called directly by the timer
		//and runs in the same thread as the timer.

		//We call the method that will work with the UI
		//through the runOnUiThread method.
    	
		this.runOnUiThread(Timer_Tick);
	}
	
    private void SlideShow(boolean status, int timecode, int timeslide){
    	if(status){
    		MyTimer = new Timer();
    		MyTimerTask = new TimerTask() {
    			@Override
    			public void run() {
    				TimerMethod();
    			}

    		};
    		MyTimer.schedule(MyTimerTask,timecode*1000, timeslide*1000);
    		
    	}else{
    		MyTimerTask.cancel();
	        MyTimerTask = null;
	        MyTimer.cancel();
    	}
    }
    
    public void initSlideTimer(int delay, int interval){
    	MyTimer = new Timer();
		MyTimerTask = new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		};
		MyTimer.schedule(MyTimerTask,delay*1000, interval*1000);
    }
    
    public void stopSlideTimer(){
    	if(MyTimerTask!=null){
    		MyTimerTask.cancel();
    		//MyTimerTask=null;
    	}
    	if(MyTimer != null){
	        MyTimer.cancel();
	        MyTimer.purge();
    	}
    }
	
	public List<String> listFileImagesByExt(String dir) {

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
	
	public List<String> listFileVideosByExt(String dir) {

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
				return Pattern.matches(".*\\.(mp4)", name);

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

    private void setListUrl(){
    	urls = new String[5];
    	urls[0] = "http://www.google.com.pe";
    	urls[1] = "http://www.tcc.com.pe";
    	urls[2] = "http://www.gempsy.com";
    	urls[3] = "http://www.distriluz.com.pe";
    	urls[4] = "http://www.alfavia.com";
    }
    
    private class MyWebViewClient extends WebViewClient {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
          view.loadUrl(url);
          return true;
      }
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
	
	/*@Override
    protected void onStop() {
        try {
             // make sure you close the socket upon exiting
             serverSocket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
        super.onStop();
    }*/
	
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
    	mManager.destroy();
        if (mReadThread != null)
                mReadThread.interrupt();
        mScanner.closeSerialPort();
        mSerialPort = null;
        stopSlideTimer();
        super.onDestroy();
    }
 
}