package com.vhsrobotics.cameracode;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Target {
	public Point topRight;
	public Point bottomRight;
	public Point bottomLeft;
	public Point topLeft;
	
	public double topLength;
	public double rightLength;
	public double bottomLength;
	public double leftLength;
	
	public Point topMid;
	public Point rightMid;
	public Point bottomMid;
	public Point leftMid;
	
	public Point middle;
	
	public Mat targetMask;
	public double targetDist;
	public double angle;
	public double tilt;
	
	public Target (Point[] pts, Mat depth, Mat ir, double fov) {
		int minx = 1;
		for (int i = 1; i < pts.length; i++) {
			if (Math.abs(pts[0].x - pts[i].x) < Math.abs(pts[0].x - pts[minx].x)) {
				minx = i;
			}
		}
		
		if (minx == 1) {
			topRight = pts[0];
			bottomRight = pts[1];
			bottomLeft = pts[2];
			topLeft = pts[3];
		} else {
			topRight = pts[3];
			bottomRight = pts[0];
			bottomLeft = pts[1];
			topLeft = pts[2];
		}
		calcLengths();
		calcMids();
		middle = new Point((topMid.x + bottomMid.x) / 2, (leftMid.y + rightMid.y) / 2);
		
		calcDist(depth, ir);
		calcAngle(fov, ir.width());
		calcTilt();
	}
	
	public void calcLengths () {
		topLength = dist(topRight, topLeft);
		rightLength = dist(topRight, bottomRight);
		bottomLength = dist(bottomRight, bottomLeft);
		leftLength = dist(topLeft, bottomLeft);
	}
	
	public void calcMids () {
		topMid = mid(topLeft, topRight);
		leftMid = mid(topLeft, bottomLeft);
		rightMid = mid(topRight, bottomRight);
		bottomMid = mid(bottomRight, bottomLeft);
	}
	
	public void calcTilt () {
		tilt = ((leftLength / rightLength) - 1) * 100;
	}
	
	private double dist (Point a, Point b) {
		return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
	}
	
	private Point mid (Point a, Point b) {
		return new Point((a.x + b.x) / 2, (a.y + b.y) / 2);
	}
	
	public Point[] getPoints () {
		return new Point[]{topRight, bottomRight, bottomLeft, topLeft};
	}
	
	public void calcDist (Mat depth, Mat ir) {
		Mat temp = Mat.zeros(ir.rows(), ir.cols(), CvType.CV_8U);
		targetMask = Mat.zeros(ir.rows(), ir.cols(), CvType.CV_8U);
		Imgproc.fillConvexPoly(temp, new MatOfPoint(getPoints()), new Scalar(255));
		ir.copyTo(targetMask, temp);
		targetDist = Core.mean(depth, targetMask).val[0];
	}
	
	public void calcAngle (double fov, double width) {
		angle = fov * (middle.x - width / 2) / width;
	}
}
