package org.aquadroid;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.GlobalState;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCore.RemoteProvisioningState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PayloadType;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SoftphoneManager implements LinphoneCoreListener {
	
	private static SoftphoneManager mInstance;
	private static State callState = State.Idle;
	private Context mContext;
	private LinphoneCore mLinphoneCore;
	private LinphoneCall mLinphoneCall;
	private Timer mTimer;
	
	private GLSurfaceView mVideoView;
	private SurfaceView mCaptureView;
	private AndroidVideoWindowImpl androidVideoWindowImpl;
	
	private String identity = "sip:155@192.168.100.15";
	private String password = "pwd155";
	private SoftphoneSettings soft_set;
	
	private AudioCodecSettings Audio;
	private VideoCodecSettings Video;
	
	public SoftphoneManager(Context c, GLSurfaceView video, SurfaceView capture, Config cfg) {
		mContext = c;
		LinphoneCoreFactory.instance().setDebugMode(true, "Linphone Mini");
		
		try {
			String basePath = cfg.getWorkDirectory()+"/"+cfg.getSoundsDirectory();
			//copyAssetsFromPackage(basePath);
			mInstance = this;
			soft_set = new SoftphoneSettings(cfg.getWorkDirectory()+"/"+cfg.getSettingsDirectory());
			identity = "sip:"+soft_set.getUser()+"@"+soft_set.getDomain();
			password = soft_set.getPassword();
			
			mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this, mContext);
		
			LinphoneAddress from = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
			
			String username = from.getUserName();
			String domain = from.getDomain();
			
			LinphoneAuthInfo info;
			if (password != null) {
				info = LinphoneCoreFactory.instance().createAuthInfo(username, password, null, domain);
				mLinphoneCore.addAuthInfo(info);
			}

			LinphoneProxyConfig proxyCfg;
			proxyCfg = LinphoneCoreFactory.instance().createProxyConfig(identity, domain, null, true);
			proxyCfg.setExpires(2000);
			proxyCfg.setProxy(domain);
			
			mLinphoneCore.addProxyConfig(proxyCfg); // add it to linphone
			mLinphoneCore.setDefaultProxyConfig(proxyCfg);
			
			mLinphoneCore.setContext(mContext);
			mLinphoneCore.setRing(basePath + "/oldphone_mono.wav");
			mLinphoneCore.setRootCA(basePath + "/rootca.pem");
			mLinphoneCore.setPlayFile(basePath + "/toy_mono.wav");
			//mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");
			
			int availableCores = Runtime.getRuntime().availableProcessors();
			mLinphoneCore.setCpuCount(availableCores);
			mLinphoneCore.setMaxCalls(1);
			
			LinphoneCodecDisable(0,-1);
			//LinphoneCodecEnable(0, 4);
			//LinphoneCodecEnable(0, 5);
			Audio = new AudioCodecSettings(cfg.getWorkDirectory()+ "/" +cfg.getSettingsDirectory());
			for(int i=0; i< Audio.codecs.length; i++){
				LinphoneCodecEnable(Audio.codecs[i].getName(), Audio.codecs[i].getRate(), Audio.codecs[i].getChannel(), Audio.codecs[i].getEnabled());
			}
			LinphoneCodecDisable(1,-1);
			//LinphoneCodecEnable(1, 1);
			Video = new VideoCodecSettings(cfg.getWorkDirectory()+ "/" +cfg.getSettingsDirectory());
			for(int i=0; i< Video.codecs.length; i++){
				LinphoneCodecEnable(Video.codecs[i].getName(), Video.codecs[i].getRate(), Video.codecs[i].getEnabled());
			}
			
			setUserAgent();
			setFrontCamAsDefault();
			startIterate();
	        mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
	        mLinphoneCore.enableVideo(true, true);
	        mLinphoneCore.setVideoPolicy(true, true);
	        
	        mVideoView = video;
	        mCaptureView = capture;
	        androidVideoWindowImpl = new AndroidVideoWindowImpl(mVideoView, mCaptureView);
	        
	        mCaptureView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Warning useless because value is ignored and automatically set by new APIs.
	        fixZOrder(mVideoView, mCaptureView);
	        
			androidVideoWindowImpl = new AndroidVideoWindowImpl(mVideoView, mCaptureView);
			androidVideoWindowImpl.setListener(new AndroidVideoWindowImpl.VideoWindowListener() {
				public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
					mLinphoneCore.setVideoWindow(vw);
					//mVideoView = surface;
				}

				public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw) {
					if (mLinphoneCore != null) {
						mLinphoneCore.setVideoWindow(null);
					}
				}

				public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
					mCaptureView = surface;
					mLinphoneCore.setPreviewWindow(mCaptureView);
				}

				public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {
					// Remove references kept in jni code and restart camera
					//mLinphoneCore.setPreviewWindow(null);
				}
			});
			androidVideoWindowImpl.init();
			
		} catch (LinphoneCoreException e) {
		}
		
	}
	
	private void fixZOrder(SurfaceView video, SurfaceView preview) {
		video.setZOrderOnTop(false);
		preview.setZOrderOnTop(true);
		preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
	}
	
	public void LinphoneCodecList(int type, Map<String, Integer> CodecList){
		PayloadType[] pt;
		int index;
		
		if(type==0){
			pt = mLinphoneCore.getAudioCodecs();
		}else{
			pt = mLinphoneCore.getVideoCodecs();
		}
		for(index=0; index<pt.length; index++){
			String CodecName = pt[index].getMime()+"/"+pt[index].getRate();
			CodecList.put(CodecName, index);
		}
	}
	
	public void LinphoneCodecEnable(int type, int sel_index) throws LinphoneCoreException{
		PayloadType[] pt;
		int index;
		
		if(type==0){
			pt = mLinphoneCore.getAudioCodecs();
		}else{
			pt = mLinphoneCore.getVideoCodecs();
		}
		
		for(index=0; index<pt.length; index++){
			if(index==sel_index || sel_index==-1){
				mLinphoneCore.enablePayloadType(pt[index], true);
				Log.d("index: "+Integer.toString(index) +", mime_type: "+pt[index].getMime(), " ENABLED");
			}
		}
	}
	
	public void LinphoneCodecEnable(String mime, int clockrate, int channels, Boolean flag){
		PayloadType pt = mLinphoneCore.findPayloadType(mime, clockrate, channels);
		try{
			mLinphoneCore.enablePayloadType(pt, flag);
			if(flag){
				Log.d("CODEC: " + "mime_type: " + mime + " ,clock: " + clockrate + "channels: " + channels + " ENABLED");
			}else{
				Log.d("CODEC: " + "mime_type: " + mime + " ,clock: " + clockrate + "channels: " + channels + " DISABLED");
			}
		}catch(LinphoneCoreException e){
			
		}catch(Exception e){
			
		}

	}
	
	public void LinphoneCodecEnable(String mime, int clockrate, Boolean flag) throws LinphoneCoreException{
		PayloadType pt = mLinphoneCore.findPayloadType(mime, clockrate);
		
		try{
			mLinphoneCore.enablePayloadType(pt, flag);
			if(flag){
				Log.d("CODEC: " + "mime_type: " + mime + " ,clock: " + clockrate + " ENABLED");
			}else{
				Log.d("CODEC: " + "mime_type: " + mime + " ,clock: " + clockrate + " DISABLED");
			}
		}catch(LinphoneCoreException e){
			
		}catch(Exception e){
			
		}

	}
	
	public void LinphoneCodecDisable(int type, int sel_index) throws LinphoneCoreException{
		PayloadType[] pt;
		int index;
		
		if(type==0){
			pt = mLinphoneCore.getAudioCodecs();
		}else{
			pt = mLinphoneCore.getVideoCodecs();
		}
		
		for(index=0; index<pt.length; index++){
			if(index==sel_index || sel_index==-1){
				mLinphoneCore.enablePayloadType(pt[index], false);
				Log.d("index: "+Integer.toString(index) +", mime_type: "+pt[index].getMime(), " DISABLED");
			}
		}
	}
	
	public static SoftphoneManager getInstance() {
		return mInstance;
	}
	
	public void destroy() {
		try {
			mTimer.cancel();
			mLinphoneCore.destroy();
		}
		catch (RuntimeException e) {
		}
		finally {
			mLinphoneCore = null;
			mInstance = null;
		}
	}
	
	private void startIterate() {
		TimerTask lTask = new TimerTask() {
			@Override
			public void run() {
				mLinphoneCore.iterate();
			}
		};
		
		/*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
		mTimer = new Timer("LinphoneMini scheduler");
		mTimer.schedule(lTask, 0, 20);
	}
	
	private void setUserAgent() {
		try {
			String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
			if (versionName == null) {
				versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
			}
			mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
		} catch (NameNotFoundException e) {
		}
	}
	
	private void setFrontCamAsDefault() {
		int camId = 0;
		AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
		for (AndroidCamera androidCamera : cameras) {
			if (androidCamera.frontFacing)
				camId = androidCamera.id;
		}
		mLinphoneCore.setVideoDevice(camId);
	}
	
	/*private void copyAssetsFromPackage(String basePath) throws IOException {
		SoftphoneUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
		SoftphoneUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
		SoftphoneUtils.copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
		//SoftphoneUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
		//SoftphoneUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
		SoftphoneUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
		SoftphoneUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
	}*/

	@Override
	public void globalState(LinphoneCore lc, GlobalState state, String message) {
		Log.d("Global state: " + state + "(" + message + ")");
	}

	@Override
	public void callState(LinphoneCore lc, LinphoneCall call, State cstate,
			String message) {
		Log.d("Call state: " + cstate + "(" + message + ")");
		callState = cstate;
	}
	
	public State getCallState(){
		return callState;
	}

	@Override
	public void callStatsUpdated(LinphoneCore lc, LinphoneCall call,
			LinphoneCallStats stats) {
		
	}

	@Override
	public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call,
			boolean encrypted, String authenticationToken) {
		
	}

	@Override
	public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
			RegistrationState cstate, String smessage) {
		Log.d("Registration state: " + cstate + "(" + smessage + ")");
	}

	@Override
	public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,
			String url) {
		
	}

	@Override
	public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {
		
	}

	@Override
	public void textReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneAddress from, String message) {
		
	}

	@Override
	public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneChatMessage message) {
		Log.d("Message received from " + cr.getPeerAddress().asString() + " : " + message.getText() + "(" + message.getExternalBodyUrl() + ")");
	}

	@Override
	public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
		Log.d("Composing received from " + cr.getPeerAddress().asString());
	}

	@Override
	public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {
		
	}

	@Override
	public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status,
			int delay_ms, Object data) {
		
	}

	@Override
	public void notifyReceived(LinphoneCore lc, LinphoneCall call,
			LinphoneAddress from, byte[] event) {
		
	}

	@Override
	public void transferState(LinphoneCore lc, LinphoneCall call,
			State new_call_state) {
		
	}

	@Override
	public void infoReceived(LinphoneCore lc, LinphoneCall call,
			LinphoneInfoMessage info) {
		
	}

	@Override
	public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev,
			SubscriptionState state) {
		
	}

	@Override
	public void notifyReceived(LinphoneCore lc, LinphoneEvent ev,
			String eventName, LinphoneContent content) {
		Log.d("Notify received: " + eventName + " -> " + content.getDataAsString());
	}

	@Override
	public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev,
			PublishState state) {
		
	}

	@Override
	public void configuringStatus(LinphoneCore lc,
			RemoteProvisioningState state, String message) {
		Log.d("Configuration state: " + state + "(" + message + ")");
	}

	@Override
	public void show(LinphoneCore lc) {
		
	}

	@Override
	public void displayStatus(LinphoneCore lc, String message) {
		
	}

	@Override
	public void displayMessage(LinphoneCore lc, String message) {
		
	}

	@Override
	public void displayWarning(LinphoneCore lc, String message) {
		
	}

	@Override
	public void authInfoRequested(LinphoneCore lc, String realm,
			String username, String Domain) {
		// TODO Auto-generated method stub
		
	}
	
	public void makeCall(String uri){
		try {
			mLinphoneCore.invite(uri);
		} catch (LinphoneCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void hangOut(){
		if(mLinphoneCore.getCallsNb() == 1){
			mLinphoneCall = mLinphoneCore.getCurrentCall();
			
			
			if(mLinphoneCall.getState()!=LinphoneCall.State.CallEnd){
				mLinphoneCore.terminateCall(mLinphoneCall);
			}
		}
	}
	
	public void answer() throws LinphoneCoreException{
		mLinphoneCall = mLinphoneCore.getCurrentCall();
		
		if(mLinphoneCall.getState()==LinphoneCall.State.IncomingReceived){
			mLinphoneCore.acceptCall(mLinphoneCall);
		}
	}
}
