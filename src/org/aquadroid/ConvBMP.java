package org.aquadroid;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class ConvBMP extends Activity{
	private static final String TAG = "ConvBMP";
	//--- Private constants
 //   private final static int BITMAPFILEHEADER_SIZE = 14;
    private final static int BITMAPINFOHEADER_SIZE = 40;
    //--- Private variable declaration
    //--- Bitmap file header
    //private byte bitmapFileHeader [] = new byte [14];
    private byte bfType [] =  {(byte)'B', (byte)'M'};
    private int bfSize = 0;
    private int bfReserved1 = 0;
    private int bfReserved2 = 0;
    private int bfOffBits = 918;
    //--- Bitmap info header
 //   private byte bitmapInfoHeader [] = new byte [40];
    private int biSize = BITMAPINFOHEADER_SIZE;
    private int biWidth = 0;
    private int biHeight = 0;
    private int biPlanes = 1;
    private int biBitCount = 8;
    private int biCompression = 0;
    private int biSizeImage = 0;
    private int biXPelsPerMeter = 5905;
    private int biYPelsPerMeter = 5905;
    private int biClrUsed = 216;
    private int biClrImportant = 216;
    //--- Bitmap raw data
    //private int intBitmap [];
    //private byte byteBitmap [];
    //--- File section
//    private FileOutputStream fo;
    private BufferedOutputStream bfo;
    
    private String FileBin;
    //private String FilePal;
    private String OutFile;
    //private int ImageWidth;
    //private int ImageHeight;
    private Context ctx;
    
    public ConvBMP(String outfile)
    {
    	this.OutFile = outfile;
    }
    public void setContext(Context context){
    	this.ctx = context;
    }
    public void SaveBinPalFile(String filebin, int imgwidth, int imgheight)
    {
    	this.FileBin = filebin;
    	
    	//this.ImageWidth = imagewidth;
    	//this.ImageHeight = imageheight;
    	this.biWidth = imgwidth;
    	this.biHeight = imgheight;
    	this.biSizeImage = this.biWidth*this.biHeight;
    	this.bfSize = (this.biWidth*this.biHeight+216*4+54);
    	
    	try{
    		FileOutputStream fos = openFileOutput(this.OutFile, MODE_PRIVATE);

    		//bfo = new BufferedOutputStream(new FileOutputStream(outfile));
    		bfo = new BufferedOutputStream(fos);
    		
    		writeBitmapFileHeader();
    		writeBitmapInfoHeader();
    		writePal();
    		writeBin();
    		bfo.close();
    		
    	}catch(Exception e){
    		
    	}
    }
    public void SaveBinFilePalByte(String filebin, byte[] filepal, int imgwidth, int imgheight)
    {
    	this.FileBin = filebin;
    	
    	this.biWidth = imgwidth;
    	this.biHeight = imgheight;
    	this.biSizeImage = this.biWidth*this.biHeight;
    	this.bfSize = (this.biWidth*this.biHeight+216*4+54);
    	
    	try{
    		FileOutputStream fos = this.ctx.openFileOutput(this.OutFile, this.ctx.MODE_PRIVATE);

    		bfo = new BufferedOutputStream(fos);
    		
    		writeBitmapFileHeader();
    		writeBitmapInfoHeader();
    		writePalByte(filepal);
    		writeBin();
    		bfo.close();
    		
    	}catch(Exception e){
    		
    	}
    }
    public void SaveBinPalByte(byte[] filebin, byte[] filepal, int imgwidth, int imgheight)
    {
    	this.biWidth = imgwidth;
    	this.biHeight = imgheight;
    	this.biSizeImage = this.biWidth*this.biHeight;
    	this.bfSize = (this.biWidth*this.biHeight+216*4+54);
    	
    	try{
    		FileOutputStream fos = this.ctx.openFileOutput(this.OutFile, this.ctx.MODE_PRIVATE);

    		bfo = new BufferedOutputStream(fos);
    		writeBitmapFileHeader();
    		writeBitmapInfoHeader();
    		writePalByte(filepal);
    		writeBinByte(filebin);
    		bfo.close();
    		
    	}catch(Exception e){
    		
    	}
    }
    public void SaveBinBytePalFile(byte[] filebin, int imgwidth, int imgheight)
    {
    	this.biWidth = imgwidth;
    	this.biHeight = imgheight;
    	this.biSizeImage = this.biWidth*this.biHeight;
    	this.bfSize = (this.biWidth*this.biHeight+216*4+54);
    	
    	try{
    		FileOutputStream fos = this.ctx.openFileOutput(this.OutFile, this.ctx.MODE_PRIVATE);
    		bfo = new BufferedOutputStream(fos);
    		writeBitmapFileHeader();
    		writeBitmapInfoHeader();
    		writePal();
    		writeBinByte(filebin);
    		bfo.close();
    		
    	}catch(Exception e){
    		
    	}
    }
    private void writePal()
    {
    	//leer archivo pal
    	try{
    		//File filepal = new File(this.FilePal);
    		//InputStream isp = new FileInputStream(filepal);
    		InputStream isp = this.ctx.getResources().openRawResource(R.raw.palette);
    		int length = 0;
    		
    		length = isp.available();
    		Log.e(TAG, "WritePal length = "+ length);
    		//long length = filepal.length();
    	
    		byte[] palbytes = new byte[length];
    		byte[] palette = new byte[1024];
    		
    		//Read in the bytes
    		int offset = 0;
    		int numRead = 0;
    		while (offset < palbytes.length && (numRead=isp.read(palbytes, offset, palbytes.length-offset)) >= 0) {
    			offset += numRead;
    		}
    		isp.close();
    		
 /*   		for(int p=0;p<length;p+=3)
    		{
        		//b
                bfo.write(palbytes[p]);
                //g
                bfo.write(palbytes[p+1]);
                //r
                bfo.write(palbytes[p+2]);
                //reserved
                bfo.write(0x00);
                
    		}
   */
    		for (int i = 0; i < 768/3; i++)
            {
                    palette[i*4]=palbytes[i*3+2];
                    palette[i*4+1]=palbytes[i*3+1];
                    palette[i*4+2]=palbytes[i*3];
                    palette[i*4+3]=0x0;
            }
    		bfo.write(palette,0,216*4);
    		
    	}catch(Exception e){
    		Log.e(TAG, "Exception = "+ e.toString());
    	}
    }
    private void writePalByte(byte[] palbytes)
    {
    	//leer archivo pal
    	try{
    		byte[] palette = new byte[1024];
    		
    		for (int i = 0; i < 768/3; i++)
            {
                    palette[i*4]=palbytes[i*3+2];
                    palette[i*4+1]=palbytes[i*3+1];
                    palette[i*4+2]=palbytes[i*3];
                    palette[i*4+3]=0x0;
            }
    		bfo.write(palette,0,216*4);
    		
    	}catch(Exception e){
    		
    	}
    }
    private void writeBin()
    {
    	//bin 
    	try{
    		File filebin = new File(this.FileBin);
    		InputStream isb = new FileInputStream(filebin);
    		long length = filebin.length();
	
    		byte[] binbytes = new byte[(int)length];
    		//byte[] inv_bmpdata = new byte[307200];
    		//Read in the bytes
    		int offset = 0;
    		int numRead = 0;
    		while (offset < binbytes.length && (numRead=isb.read(binbytes, offset, binbytes.length-offset)) >= 0) {
    			offset += numRead;
    		}
    		isb.close();
    		
    		int cx= this.biWidth;
            int cy= this.biHeight;
    		
/*            int cz=0;
            for (int i=(cy-1)*cx; i>=0; i-=cx) {
                for(int x=0; x<cx; x++){
                        inv_bmpdata[i+x]=binbytes[cz];
                        cz++;
                }
            }
            bfo.write(inv_bmpdata, 0, inv_bmpdata.length);
  */          
            for (int i = cy-1; i >= 0; i--)
            {
                bfo.write(binbytes,i*cx, cx);
            }
            
    	}catch(Exception e){
    		
    	}
    }

    private void writeBinByte(byte[] binbytes)
    {
    	//bin 
    	try{
    		
    		int cx= this.biWidth;
            int cy= this.biHeight;
    		
            for (int i = cy-1; i >= 0; i--)
            {
                bfo.write(binbytes,i*cx, cx);
            }
            
    	}catch(Exception e){
    		
    	}
    }
    
    private void writeBitmapFileHeader() throws Exception {
    	
        bfo.write (bfType);
        bfo.write (intToDWord (bfSize));
        bfo.write (intToWord (bfReserved1));
        bfo.write (intToWord (bfReserved2));
        bfo.write (intToDWord (bfOffBits));
    }
    private void writeBitmapInfoHeader () throws Exception {
    	
        bfo.write (intToDWord (biSize));
        bfo.write (intToDWord (biWidth));
        bfo.write (intToDWord (biHeight));
        bfo.write (intToWord (biPlanes));
        bfo.write (intToWord (biBitCount));
        bfo.write (intToDWord (biCompression));
        bfo.write (intToDWord (biSizeImage));
        bfo.write (intToDWord (biXPelsPerMeter));
        bfo.write (intToDWord (biYPelsPerMeter));
        bfo.write (intToDWord (biClrUsed));
        bfo.write (intToDWord (biClrImportant));
    }
    /*
    *
    * intToWord converts an int to a word, where the return
    * value is stored in a 2-byte array.
    *
    */
    private byte [] intToWord (int parValue) {
        byte retValue [] = new byte [2];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>	8) & 0x00FF);
        return (retValue);
    }

    /*
    *
    * intToDWord converts an int to a double word, where the return
    * value is stored in a 4-byte array.
    *
    */
    private byte [] intToDWord (int parValue) {
        byte retValue [] = new byte [4];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>	8) & 0x000000FF);
        retValue [2] = (byte) ((parValue >>	16) & 0x000000FF);
        retValue [3] = (byte) ((parValue >>	24) & 0x000000FF);
        return (retValue);
    }
}
