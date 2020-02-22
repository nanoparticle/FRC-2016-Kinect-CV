package com.vhsrobotics.cameracode;
import java.nio.ByteBuffer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.openkinect.freenect.Context;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.Freenect;
import org.openkinect.freenect.LedStatus;

public class Util {
	
	private static Context ctx;
	static Device k;
	
	public static void kInit () {
		attachShutdownHook();
		ctx = Freenect.createContext();
		if (ctx.numDevices() > 0) {
			k = ctx.openDevice(0);
		} else {
			System.err.println("No kinects detected.  Exiting.");
			System.exit(0);
		}
	}
	
	public static void attachShutdownHook () {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting Down...");
				if (ctx != null) {
					if (k != null) {
						k.setLed(LedStatus.BLINK_GREEN);
						k.stopDepth();
						k.stopVideo();
						k.close();
					}
					ctx.shutdown();
					System.out.println("Shutdown Procedure Complete.");
				}
			}
		});
	}
	
	public static void RGBToMatU8 (ByteBuffer input, Mat output) {
		byte[] temp = new byte[output.height() * output.width() * 3];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = input.get(i);
		}
		output.put(0, 0, temp);
	}
	
	public static void IRToMatU8 (ByteBuffer input, Mat output) {
		byte[] temp = new byte[output.height() * output.width()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = input.get(i);
		}
		output.put(0, 0, temp);
	}
	
	public static void depthRawToMatU16 (ByteBuffer input, Mat output) {
		short[] temp = new short[output.height() * output.width()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = (short)((input.get(i * 2) & 0xFF) | ((input.get((i * 2) + 1) & 0xFF) << 8));
		}
		output.put(0, 0, temp);
	}
	
	public static void depthToMatU8 (ByteBuffer input, Mat output) {
		Mat mat = new Mat(output.height(), output.width(), CvType.CV_16UC1);
		short[] temp = new short[output.height() * output.width()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = (short)((input.get(i * 2) & 0xFF) | ((input.get((i * 2) + 1) & 0xFF) << 8));
		}
		mat.put(0, 0, temp);
		mat.convertTo(output, CvType.CV_8UC1, 1.0/8);
	}
	
	public static int writePartialCmosRegister (short reg, short value, int startBit, int stopBit) {
		short temp = k.readCmosRegister(reg);
		short mask = (short) ~((short)(Math.pow(2, stopBit - startBit + 1) - 1) << startBit);
		temp &= mask;
		temp |= value << startBit;
		return k.writeCmosRegister(reg, temp);
	}
}
