package com.vhsrobotics.cameracode.processors;


import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.vhsrobotics.cameracode.ImageContainer;
import com.vhsrobotics.cameracode.Imshow;
import com.vhsrobotics.cameracode.StreamHandler;
import com.vhsrobotics.cameracode.Target;

public class RGBProcessor {
	
	private static Imshow window1 = new Imshow("rgb 1");
	private static Imshow window2 = new Imshow("rgb 2");
	private static Imshow window3 = new Imshow("rgb 3");
	
	private static int countA = 1;
	
	public static void processFrame (StreamHandler h) {
		Mat input = ImageContainer.getRgbImage();
		Mat hsv = new Mat(input.height(), input.width(), CvType.CV_8UC3);
		//Mat output = new Mat(input.height(), input.width(), CvType.CV_8UC3);
		Mat output = Mat.zeros(input.height(), input.width(), CvType.CV_8UC3);
		Mat mask = new Mat(input.height(), input.width(), CvType.CV_8U);
		
		ArrayList<RotatedRect> targets = new ArrayList<RotatedRect>();
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		if (countA == 0) {
			Imgcodecs.imwrite("./testRGB.png", input);
			System.out.println("rgb saved");
		}
		countA++;
		

		Imgproc.cvtColor(input, hsv, Imgproc.COLOR_RGB2HSV); // Convert input to hsv color space
		
		Core.inRange(hsv, new Scalar(40, 75, 100), new Scalar(90, 255, 255), mask); // Threshold Operation, finds bright green retro-reflective tape
		
		input.copyTo(output, mask); // Generates composite 
		
		
		Imgproc.findContours(mask.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		/*
		for (int i=0; i < contours.size(); i++) {
			MatOfPoint2f contour2f = new MatOfPoint2f();
			MatOfInt hullPts;
			
			Point[] contourArray;
			int[] hullPtsArray;
			Point[] hullArray;
			
			MatOfPoint2f hull2f;
			MatOfPoint2f hullApprox;
			
			contours.get(i).convertTo(contour2f, CvType.CV_32FC2);
			
			double perimeter = Imgproc.arcLength(contour2f, true);
			
			if (perimeter > 150) {
				hullPts = new MatOfInt();
				hull2f = new MatOfPoint2f();
				hullApprox = new MatOfPoint2f();
				
				Imgproc.convexHull(contours.get(i), hullPts);
				
				contourArray = contour2f.toArray();
				hullPtsArray = hullPts.toArray();
				hullArray = new Point[hullPtsArray.length];
				
				for (int j = 0; j < hullPtsArray.length; j++) {
					hullArray[j] = contourArray[hullPtsArray[j]];
				}
				
				hull2f = new MatOfPoint2f(hullArray);
				
				//System.out.println(hull2f.depth());
				//System.out.println(hull2f.checkVector(2));
				
				Imgproc.approxPolyDP(hull2f, hullApprox, perimeter * 0.02, true);
				
				System.out.println(hullApprox.height());
				
				Imgproc.drawContours(output, contours, i, new Scalar(255, 0, 0), 2);
				//System.out.println(Imgproc.arcLength(contour2f, true));
			}
			//System.out.println(Imgproc.arcLength(contours2f, true));
			//System.out.println(contours.get(i).size().toString());
		}
		
		*/
		
		
		for (int i=0; i < contours.size(); i++) {
			MatOfPoint2f contour2f = new MatOfPoint2f();
			
			contours.get(i).convertTo(contour2f, CvType.CV_32FC2);
			
			double perimeter = Imgproc.arcLength(contour2f, true);
			
			if (perimeter > 150) {
				Point[] pts = simplifyContour(contours.get(i), perimeter * 0.03);
				
				
				
				/*
				for (int j = 0; j < pts.length; j++) {
					Imgproc.line(output, pts[j], pts[(j+1)%pts.length], new Scalar(0, 0, 255), 1);
					Imgproc.circle(output, pts[j], 5, new Scalar(0, 255, 0), 5);
				}
				
				System.out.println(pts.length);
				*/
				if (pts.length == 4) {
					for (int j = 0; j < pts.length; j++) {
						Imgproc.line(output, pts[j], pts[(j+1)%pts.length], new Scalar(0, 0, 255), 1);
						//Imgproc.circle(output, pts[j], 5, new Scalar(0, 255, 0), 5);
					}
					
					//Target t = new Target(pts);
					
					//Imgproc.circle(output, t.middle, 5, new Scalar(0, 255, 0), 5);
					
					//Imgproc.circle(output, t.topRight, 5, new Scalar(0, 255, 0), 5);
					//Imgproc.circle(output, t.bottomLeft, 5, new Scalar(0, 0, 255), 5);
					//System.out.println(pts[0].x);
					
					
					//Imgproc.putText(output, "(" + t.topRight.x + ", " + t.topRight.y + ")", t.topRight, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
					//Imgproc.putText(output, "(" + t.topLeft.x + ", " + t.topLeft.y + ")", t.topLeft, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
					//Imgproc.putText(output, "(" + t.bottomRight.x + ", " + t.bottomRight.y + ")", t.bottomRight, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
					//Imgproc.putText(output, "(" + t.bottomLeft.x + ", " + t.bottomLeft.y + ")", t.bottomLeft, Core.FONT_HERSHEY_PLAIN, 0.6, new Scalar(0, 0, 255));
					
					//Imgproc.putText(output, (int)t.topLength + "", t.topMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
					//Imgproc.putText(output, (int)t.bottomLength + "", t.bottomMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
					//Imgproc.putText(output, (int)t.leftLength + "", t.leftMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
					//Imgproc.putText(output, (int)t.rightLength + "", t.rightMid, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
					
					//Imgproc.putText(output, t.calcTilt() + "", t.middle, Core.FONT_HERSHEY_PLAIN, 3, new Scalar(0, 0, 255));
				}
			}
		}
		
		
		
		
		/*
		for (int i=0; i < contours.size(); i++) {
			MatOfPoint2f contours2f = new MatOfPoint2f();
			Point[] rectPts;
			MatOfInt hull;
			Point[] contourPts;
			int[] hullArray;
			Point[] hullPts;
			
			contours.get(i).convertTo(contours2f, CvType.CV_32FC2); // convert contours to type required by minAreaRect operation
			RotatedRect rect = Imgproc.minAreaRect(contours2f);
			if (rect.size.area() > 500) {
				//minAreaRect  processing
				rectPts = new Point[4];
				rect.points(rectPts);
				
				for (int j = 0; j < rectPts.length; j++) {
					Imgproc.line(output, rectPts[j], rectPts[(j+1)%rectPts.length], new Scalar(255, 0, 0), 2);
				}
				
				//convex hull processing
				hull = new MatOfInt();
				contourPts = contours.get(i).toArray();
				
				Imgproc.convexHull(contours.get(i), hull);
				
				hullArray = hull.toArray();
				hullPts = new Point[hullArray.length];
				
				for (int j = 0; j < hullPts.length; j++) {
					hullPts[j] = contourPts[hullArray[j]];
				}
				for (int j = 0; j < hullPts.length; j++) {
					Imgproc.line(output, hullPts[j], hullPts[(j+1)%hullPts.length], new Scalar(0, 0, 255), 1);
					Imgproc.circle(output, hullPts[0], 5, new Scalar(0, 255, 0), 5);
					Imgproc.circle(output, hullPts[1], 5, new Scalar(0, 0, 255), 5);
				}
			}
		}
		*/
		
		/*
		for (int i=0; i < contours.size(); i++) {
			Point[] points = new Point[4];
			MatOfPoint2f temp = new MatOfPoint2f();
			contours.get(i).convertTo(temp, CvType.CV_32FC2);
			RotatedRect rect = Imgproc.minAreaRect(temp);
			rect.points(points);
			if (rect.size.area() > 500) {
				Imgproc.line(output, points[0], points[1], new Scalar(0, 0, 255), 1);
				Imgproc.line(output, points[1], points[2], new Scalar(0, 0, 255), 1);
				Imgproc.line(output, points[2], points[3], new Scalar(0, 0, 255), 1);
				Imgproc.line(output, points[3], points[0], new Scalar(0, 0, 255), 1);
				Imgproc.circle(output, rect.center, 1, new Scalar(255, 0, 0), 3);
				targets.add(rect);
			}
		}
		*/
		
		/*
		for (int i=0; i < contours.size(); i++) {
			MatOfInt hull = new MatOfInt();
			Point[] cp = contours.get(i).toArray();
			
			Imgproc.convexHull(contours.get(i), hull);
			
			int[] hp = hull.toArray();
			Point[] points = new Point[hp.length];
			
			for (int j = 0; j < points.length; j++) {
				points[j] = cp[hp[j]];
			}
			for (int j = 0; j < points.length; j++) {
				Imgproc.line(output, points[j], points[(j+1)%points.length], new Scalar(0, 0, 255), 1);
			}
		}
		*/
		
		
		if (h.useGUI) {
			//window1.showImage(input);
			window2.showImage(output);
			//window3.showImage(mask);
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
}
