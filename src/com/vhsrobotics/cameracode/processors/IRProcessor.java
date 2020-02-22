package com.vhsrobotics.cameracode.processors;

import java.util.ArrayList;

import jssc.SerialPortException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.vhsrobotics.cameracode.ImageContainer;
import com.vhsrobotics.cameracode.Imshow;
import com.vhsrobotics.cameracode.Main;
import com.vhsrobotics.cameracode.StreamHandler;
import com.vhsrobotics.cameracode.Target;

public class IRProcessor {

	private static Imshow window1 = new Imshow("ir 1");
	private static Imshow window2 = new Imshow("ir 2");
	private static Imshow window3 = new Imshow("ir 3");

	public static void processFrame (StreamHandler h) {
		if (ImageContainer.isDepthU16Ready) {
			Mat input = ImageContainer.getIrImage();
			Mat depthU16 = ImageContainer.getDepthImageU16();
			Mat depthU8 = ImageContainer.getDepthImageU8();
			Mat depthMask = Mat.zeros(input.height(), input.width(), CvType.CV_8U); //must set Mat to zero because system appears to reuse memory when making new Mat

			Mat filteredInput = Mat.zeros(input.height(), input.width(), CvType.CV_8U);
			Mat blurredInput = Mat.zeros(input.height(), input.width(), CvType.CV_8U);
			Mat targetMask = Mat.zeros(input.height(), input.width(), CvType.CV_8U);
			Mat composite = Mat.zeros(input.height(), input.width(), CvType.CV_8UC3);
			Mat output = Mat.zeros(input.height(), input.width(), CvType.CV_8UC3);
			//Mat output = new Mat(input.height(), input.width(), CvType.CV_8U);

			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			ArrayList<Target> targets = new ArrayList<Target>();
			
			//window3.showImage(depthU8);
			//Core.inRange(depth, new Scalar(900), new Scalar(2046), depthMask); // ignores objects that are closer than 6ft away.
			Core.inRange(depthU16, new Scalar(depthMetersToRaw(feetToMeters(6))), new Scalar(2045), depthMask); // ignores objects that are closer than 6ft away.

			input.copyTo(filteredInput, depthMask);
			
			//window3.showImage(filteredInput);
			
			Core.inRange(filteredInput, new Scalar(100), new Scalar(255), filteredInput); // excludes weak ir light input
			
			
			
			Imgproc.dilate(filteredInput, blurredInput, Mat.ones(7, 7, CvType.CV_8U));
			//Imgproc.erode(output, output, Mat.ones(5, 5, CvType.CV_8U));
			//Imgproc.blur(output, output, new Size(5, 5));
			//Imgproc.GaussianBlur(input, output, new Size(9, 9), 0.001);

			
			//window2.showImageHalfSize(blurredInput);

			Imgproc.findContours(blurredInput, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			
			for (int i=0; i < contours.size(); i++) {
				MatOfPoint2f contour2f = new MatOfPoint2f();

				contours.get(i).convertTo(contour2f, CvType.CV_32FC2);

				double perimeter = Imgproc.arcLength(contour2f, true);

				if (perimeter > 200) {
					Point[] pts = simplifyContour(contours.get(i), perimeter * 0.03);

					if (pts.length == 4) {
						Target t = new Target(pts, depthU16, filteredInput, h.horizontal ? 58 : 45);
						targets.add(t);
						
						Imgproc.fillConvexPoly(targetMask, new MatOfPoint(t.getPoints()), new Scalar(255));
					}
				}
			}
			
			//composite = C1ToC3(depthU8);
			//composite.copyTo(output, targetMask);
			
			int minTilt = 0;
			for (int i=0; i < targets.size(); i++) {
				/*
				Mat temp1 = Mat.zeros(filteredInput.rows(), filteredInput.cols(), CvType.CV_8U);
				Mat temp2 = Mat.zeros(filteredInput.rows(), filteredInput.cols(), CvType.CV_8U);
				filteredInput.copyTo(temp1, t.targetMask);
				depthU8.copyTo(temp2, temp1);
				
				temp2.copyTo(output);
				*/
				
				C1ToC3(targets.get(i).targetMask).copyTo(output);
				
				
				labelTarget(targets.get(i), output);
				
				if (targets.get(i).tilt < targets.get(minTilt).tilt) {
					minTilt = i;
				}
				
			}
			try {
				if (h.transmitting) {
					if (targets.size() > 0) {
						Main.serialPort.writeString("1|" + depthRawToMillimeters(targets.get(minTilt).targetDist) + "|" + targets.get(minTilt).angle + "|" + targets.get(minTilt).tilt + "\n");
						System.out.println("1|" + depthRawToMillimeters(targets.get(minTilt).targetDist) + "|" + targets.get(minTilt).angle + "|" + targets.get(minTilt).tilt);
						//System.out.println(Main.serialPort.getOutputBufferBytesCount());
					} else {
						Main.serialPort.writeString("0|0|0|0\n");
						System.out.println("0|0|0|0");
						//System.out.println(Main.serialPort.);
					}
				}
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
			
			//window2.showImage(targetMask);
			//window2.showImage(output);
			
			if (h.useGUI) {
				window1.showImage(output);
				//window1.showImageHalfSize(output);
			}
		}
	}

	public static Point[] simplifyContour (MatOfPoint contour, double epsilon) {
		MatOfInt hullPts = new MatOfInt();
		MatOfPoint2f hull2f = new MatOfPoint2f();
		MatOfPoint2f hullApprox = new MatOfPoint2f();

		Imgproc.convexHull(contour, hullPts);

		Mat temp = new Mat(hullPts.height(), 1, CvType.CV_32FC2);

		for (int i = 0; i < hullPts.height(); i++) {
			temp.put(i, 0, contour.get((int) hullPts.get(i, 0)[0], 0));
		}

		hull2f = new MatOfPoint2f(temp);

		/*
		Point[] contourArray = contour.toArray();
		int[] hullPtsArray = hullPts.toArray();
		Point[] hullArray = new Point[hullPtsArray.length];

		for (int i = 0; i < hullPtsArray.length; i++) {
			hullArray[i] = contourArray[hullPtsArray[i]];
		}

		hull2f = new MatOfPoint2f(hullArray);
		 */

		Imgproc.approxPolyDP(hull2f, hullApprox, epsilon, true);

		return hullApprox.toArray();
	}
	
	public static void labelTarget(Target t, Mat output) {
		/*
		for (int j = 0; j < pts.length; j++) {
			Imgproc.line(output, pts[j], pts[(j+1)%pts.length], new Scalar(0, 0, 255), 1);
			Imgproc.circle(output, pts[j], 5, new Scalar(0, 255, 0), 5);
		}

		System.out.println(pts.length);
		 */

		for (int j = 0; j < t.getPoints().length; j++) {
			Imgproc.line(output, t.getPoints()[j], t.getPoints()[(j+1)%t.getPoints().length], new Scalar(0, 0, 255), 1);
			//Imgproc.circle(output, pts[j], 5, new Scalar(0, 255, 0), 5);
		}


		Imgproc.circle(output, t.middle, 5, new Scalar(0, 255, 0), 5);

		//Imgproc.circle(output, t.topRight, 5, new Scalar(0, 255, 0), 5);
		//Imgproc.circle(output, t.bottomLeft, 5, new Scalar(0, 0, 255), 5);
		//System.out.println(pts[0].x);


		//Imgproc.putText(output, "(" + t.topRight.x + ", " + t.topRight.y + ")", t.topRight, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
		//Imgproc.putText(output, "(" + t.topLeft.x + ", " + t.topLeft.y + ")", t.topLeft, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
		//Imgproc.putText(output, "(" + t.bottomRight.x + ", " + t.bottomRight.y + ")", t.bottomRight, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
		//Imgproc.putText(output, "(" + t.bottomLeft.x + ", " + t.bottomLeft.y + ")", t.bottomLeft, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));

		Imgproc.putText(output, (int)t.topLength + "", t.topMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
		Imgproc.putText(output, (int)t.bottomLength + "", t.bottomMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
		Imgproc.putText(output, (int)t.leftLength + "", t.leftMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
		Imgproc.putText(output, (int)t.rightLength + "", t.rightMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);

		//Imgproc.putText(output, t.calcTilt() + "", t.middle, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 0, 255));
		//Imgproc.putText(output, (int)t.calcTilt() + "", new Point(t.middle.x + 100, t.middle.y), Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 0, 255));
		Imgproc.putText(output, depthRawToMillimeters(t.targetDist) + "", new Point(t.middle.x + 100, t.middle.y), Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 0, 255));
		//Imgproc.putText(output, t.angle + "", new Point(t.middle.x + 100, t.middle.y), Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 0, 255));
	}
	
	public static Mat C1ToC3 (Mat c1Mat) {
		Mat c3Mat = Mat.zeros(c1Mat.rows(), c1Mat.cols(), CvType.makeType(c1Mat.depth(), 3));
		ArrayList<Mat> channels = new ArrayList<Mat>();
		channels.add(c1Mat);
		channels.add(c1Mat);
		channels.add(c1Mat);
		
		Core.merge(channels, c3Mat);
		return c3Mat;
	}

	public static double depthRawToMeters (double depth) {
		return 0.1236 * Math.tan(depth / 2842.5 + 1.1863);
	}

	public static double depthRawToMillimeters (double depth) {
		return 123.6 * Math.tan(depth / 2842.5 + 1.1863);
	}

	public static double depthMetersToRaw (double depth) {
		return (Math.atan(depth / 0.1236) - 1.1863) * 2842.5;
	}

	public static double depthMillimetersToRaw (double depth) {
		return (Math.atan(depth / 0.1236) - 1.1863) * 2842.5;
	}

	public static double feetToMeters (double value) {
		return value * 0.3048;
	}
}