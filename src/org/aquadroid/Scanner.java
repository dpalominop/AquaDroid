/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.aquadroid;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;


//import android.content.SharedPreferences;
import org.aquadroid.SerialPort;
//import android.serialport.SerialPortFinder;

//public class Application extends android.app.Application {
public class Scanner {

	//public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;
	String path = "/dev/s3c2410_serial0";
	//String path = "/dev/ttyS0";
	int baudrate = 9600;
	
	public Scanner(String path, int baudrate){
		this.path = path;
		this.baudrate = baudrate;
	}

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
	//		SharedPreferences sp = getSharedPreferences("android.serialport.sample_preferences", MODE_PRIVATE);
	//		String path = sp.getString("DEVICE", "");
	//		int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));

			/* Check parameters */
	//		if ( (path.length() == 0) || (baudrate == -1)) {
	//			throw new InvalidParameterException();
	//		}
			
			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
}
