package com.vhsrobotics.cameracode.processors;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.vhsrobotics.cameracode.ImageContainer;
import com.vhsrobotics.cameracode.Imshow;
import com.vhsrobotics.cameracode.StreamHandler;

public class DepthProcessor {
	private static Imshow window1 = new Imshow("depth 1");
	private static Imshow window2 = new Imshow("depth 2");
	private static Imshow window3 = new Imshow("depth 3");
	
	private static int countB = 1;

	public static void processFrame (StreamHandler h) {
		Mat input = ImageContainer.getDepthImageU16();
		Mat output = ImageContainer.getDepthImageU8();
		
		double[] val = input.get((int)(input.height() * 0.75), (int)(input.width() * 0.5));
		//double distance = 0.1236 * Math.tan(val[0] / 2842.5 + 1.1863); // in meters
		double distance = 123.6 * Math.tan(val[0] / 2842.5 + 1.1863); // in millimeters
		
		
		Imgproc.circle(output, new Point((int)(input.height() * 0.75), (int)(input.width() * 0.5)), 5, new Scalar(0, 0, 255), 5);
		Imgproc.putText(output, val[0] + "", new Point((int)(input.height() * 0.75), (int)(input.width() * 0.5)), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255), 3);
		Imgproc.putText(output, distance + "", new Point((int)(input.height() * 0.75), (int)(input.width() * 0.7)), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255), 3);
		
		if (countB  == 0) {
			Imgcodecs.imwrite("./testDepth.png", input);
			System.out.println("depth saved");
		}
		countB++;
		
		if (h.useGUI) {
			window2.showImage(output);
		}
	}
}
