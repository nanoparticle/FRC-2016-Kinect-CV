package com.vhsrobotics.cameracode;
import java.io.IOException;
import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.opencv.core.Core;
import org.openkinect.freenect.DepthFormat;
import org.openkinect.freenect.LedStatus;
import org.openkinect.freenect.Resolution;
import org.openkinect.freenect.VideoFormat;


public class Main {

	static {
		/*
		try {
			Runtime.getRuntime().exec(new String[] {"/bin/bash","-c","echo password| sudo stop ttyS0"});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		attachShutdownHook();
	}
	
	public static SerialPort serialPort;
	
	static StreamHandler handler;
	static Scanner s = new Scanner(System.in);
	
	public static void main(String[] args) throws Exception {
		
		serialPort = new SerialPort(SerialPortList.getPortNames()[0]);
		serialPort.openPort();
		serialPort.setParams(SerialPort.BAUDRATE_115200, 
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		System.out.println("Serial port at " + serialPort.getPortName() + " is opened: " + serialPort.isOpened());
		System.out.println();
		
		
		Util.kInit();
		
		Util.k.setLed(LedStatus.BLINK_RED_YELLOW);

		Util.k.setDepthFormat(DepthFormat.D11BIT);
		//Util.k.setVideoFormat(VideoFormat.RGB, Resolution.MEDIUM);
		Util.k.setVideoFormat(VideoFormat.IR_8BIT, Resolution.MEDIUM);
		
		handler = new StreamHandler();
		Util.k.startDepth(handler);
		Util.k.startVideo(handler);
		
		System.out.print("Setting IR Brightness to 5: ");
		Util.k.setIrBrightness(5);
		System.out.println(Util.k.getIrBrightness());
		
		/*
		
		System.out.print("Enabling Manual White Balance: ");
		System.out.println(Util.k.setFlag(1 << 15, 1)); //enable Manual white balance
		
		System.out.print("Disabling Auto-Exposure: ");
		System.out.println(Util.k.setFlag(1 << 14, 0)); //disable auto exposure
		
		System.out.print("Setting that one variable that seems to affect exposure to 654 (Exposure becomes about 33ms): "); //DON'T CHANGE!!
		System.out.println(Util.k.writeCmosRegister((short) 0x0007, (short) 0x028E)); //set pseudo-exposure to 654 (medium exposure)
		
		
		//System.out.println(Util.k.writeCmosRegister((short) 0x8105, (short) 0b111)); //set aperture correction (sharpness) to max
		
		//System.out.print("Setting saturation to 150%: ");
		//System.out.println(Util.writePartialCmosRegister((short) 0x0125, (short) 0b101, 3, 5)); //set saturation to 150%
		
		Thread.sleep(100); //gain change doesn't take if set too soon after preceding settings, minimum delay is about 55ms
		
		System.out.print("Setting Global Gain to 5: ");
		System.out.println(Util.writePartialCmosRegister((short) 0x002F, (short)5, 0, 8)); // set global analog gain (basically exposure) to 10
		
		*/
		
		handler.horizontal = false;
		
		handler.doRGB = true;
		handler.doIR = true;
		handler.doDepth = true;
		
		if (args.length > 0 && args[0].equals("nogui")) {
			handler.useGUI = false;
		} else {
			handler.useGUI = true;
		}
		handler.doProcessFrames = true;
		
		serialPort.addEventListener(new SerialListener(handler, serialPort));
		/*
		while (true) {
			Thread.sleep(10000);
			Util.k.startVideo(handler);
		}
		
		*/
		while(Util.k != null) {
			//Thread.sleep(10000);
			//int temp = s.nextInt();
			//String str = s.next();
			//short temp = (short) Integer.parseInt(str, 16);
			//System.out.println(Util.k.writeCmosRegister((short) 0x000C, (short) temp));
			//System.out.println(Util.k.writeCmosRegister((short) 0007, (short) temp));
			//System.out.println(Util.k.readCmosRegister((short) 0x002F) + "  " + Util.writePartialCmosRegister((short) 0x002F, (short)temp, 0, 8));
			//System.out.println(Util.k.setIrBrightness(temp));
		}
		
		
		//Thread.sleep(300000000);
	}
	
	public static void attachShutdownHook () {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (serialPort != null) {
					try {
						serialPort.closePort();
						System.out.println("Serial port closed.");
					} catch (SerialPortException e) {
						e.printStackTrace();
					}
				}
				Util.k.setLed(LedStatus.GREEN);
			}
		});
	}

}
