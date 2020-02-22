package com.vhsrobotics.cameracode;

import java.nio.ByteBuffer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.openkinect.freenect.DepthHandler;
import org.openkinect.freenect.FrameMode;
import org.openkinect.freenect.VideoHandler;

import com.vhsrobotics.cameracode.processors.DepthProcessor;
import com.vhsrobotics.cameracode.processors.IRProcessor;
import com.vhsrobotics.cameracode.processors.RGBProcessor;


public class StreamHandler implements DepthHandler, VideoHandler {

	public boolean horizontal = true;
	
	public boolean doProcessFrames = false;
	public boolean useGUI = false;
	public boolean doRGB = false;
	public boolean doIR = false;
	public boolean doDepth = false;
	
	public volatile boolean transmitting = false;

	public void onFrameReceived(FrameMode mode, ByteBuffer frame, int time) {
		if (doProcessFrames && mode.isValid()) {
			if (mode.dataBitsPerPixel == 24 && doRGB) { // color stream
				Mat input = new Mat(mode.height, mode.width, CvType.CV_8UC3);
				
				Util.RGBToMatU8(frame, input);
				
				if (!horizontal) input = rotate(input);
				
				ImageContainer.updateRgbImage(input);
				RGBProcessor.processFrame(this);
				
			} else if (mode.dataBitsPerPixel == 8 && doIR) { // IR stream
				Mat input = new Mat(mode.height, mode.width, CvType.CV_8UC1);
				Mat input2 = new Mat(mode.height, mode.width-8, CvType.CV_8UC1);
				
				Util.IRToMatU8(frame, input);
				input2 = input.submat(4 - 1, mode.height - 4 - 1, 0, mode.width);
				
				if (!horizontal) input2 = rotate(input2);
				
				ImageContainer.updateIrImage(input2);
				IRProcessor.processFrame(this);
				
			} else if (mode.dataBitsPerPixel == 11 && doDepth) { // depth stream
				Mat inputU8 = new Mat(mode.height, mode.width, CvType.CV_8UC1);
				Mat inputU16 = new Mat(mode.height, mode.width, CvType.CV_16UC1);
				
				Util.depthRawToMatU16(frame, inputU16);
				inputU16.convertTo(inputU8, CvType.CV_8UC1, 1.0/8);
				
				if (!horizontal) 
				{
					inputU8 = rotate(inputU8);
					inputU16 = rotate(inputU16);
				}
				
				ImageContainer.updateDepthImageU8(inputU8);
				ImageContainer.updateDepthImageU16(inputU16);
				
				//DepthProcessor.processFrame(this);
			}
		}
	}
	
	public Mat rotate (Mat input) {
		Mat result = Mat.zeros(input.cols(), input.rows(), input.type());
		//Mat rotMat = Imgproc.getRotationMatrix2D(new Point(result.width()/2.0, result.height()/2.0), 90, 1); // 90 degrees counterclockwise
		
		//Imgproc.warpAffine(input, result, rotMat, result.size());
		
		/*
		for (int i = 0; i < input.rows(); i++) {
			for (int j = 0; j < input.cols(); j++) {
				result.put(input.width() - j, i, input.get(i, j));
			}
		}
		*/
		
		Core.transpose(input, result);
		Core.flip(result, result, 0);
		
		return result;
	}
}
