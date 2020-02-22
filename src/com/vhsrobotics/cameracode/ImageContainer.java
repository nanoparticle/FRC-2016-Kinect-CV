package com.vhsrobotics.cameracode;
import org.opencv.core.Mat;


public class ImageContainer {

	private static volatile Mat rgbImage;
	private static volatile Mat irImage;
	private static volatile Mat depthImageU8;
	private static volatile Mat depthImageU16;
	
	public static boolean isRGBReady = false;
	public static boolean isIRReady = false;
	public static boolean isDepthU8Ready = false;
	public static boolean isDepthU16Ready = false;

	public static Mat getRgbImage() {
		return rgbImage;
	}

	public static Mat getIrImage() {
		return irImage;
	}

	public static Mat getDepthImageU8() {
		return depthImageU8;
	}
	
	public static Mat getDepthImageU16() {
		return depthImageU16;
	}

	public static void updateRgbImage(Mat rgbImage) {
		ImageContainer.rgbImage = rgbImage;
		isRGBReady = true;
	}

	public static void updateIrImage(Mat irImage) {
		ImageContainer.irImage = irImage;
		isIRReady = true;
	}

	public static void updateDepthImageU8(Mat depthImage) {
		ImageContainer.depthImageU8 = depthImage;
		isDepthU8Ready = true;
	}
	
	public static void updateDepthImageU16(Mat depthImage) {
		ImageContainer.depthImageU16 = depthImage;
		isDepthU16Ready = true;
	}
}

