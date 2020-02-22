package com.vhsrobotics.cameracode;


/*
 * Author: ATUL
 * This code can be used as an alternative to imshow of OpenCV for JAVA-OpenCv 
 * Make sure OpenCV Java is in your Build Path
 * Usage :
 * -------
 * Imshow ims = new Imshow("Title");
 * ims.showImage(Mat image);
 * Check Example for usage with Webcam Live Video Feed
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Imshow {

	public JFrame Window;
	private ImageIcon image;
	private JLabel label;
	private MatOfByte matOfByte;
	private boolean SizeCustom;
	private int Height, Width;

	public Imshow(String title) {
		Window = new JFrame();
		Window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.setAutoRequestFocus(false);
		image = new ImageIcon();
		label = new JLabel();
		matOfByte = new MatOfByte();
		label.setIcon(image);
		Window.getContentPane().add(label);
		Window.setResizable(false);
		Window.setTitle(title);
		SizeCustom = false;
	}

	public Imshow(String title, int height, int width) {
		SizeCustom = true;
		Height = height;
		Width = width;

		Window = new JFrame();
		Window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.setAutoRequestFocus(false);
		image = new ImageIcon();
		label = new JLabel();
		matOfByte = new MatOfByte();
		label.setIcon(image);
		Window.getContentPane().add(label);
		Window.setResizable(false);
		Window.setTitle(title);

	}

	public void showImageOld(Mat img) {
		Mat temp = null;
		if (SizeCustom) {
			temp = Mat.zeros(new Size(Width, Height), img.type());
			Imgproc.resize(img, temp, new Size(Width, Height));
		} else {
			temp = Mat.zeros(new Size(img.width(), img.height()), img.type());
			img.copyTo(temp);
		}
		Imgcodecs.imencode(".jpg", temp, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			image.setImage(bufImage);
			Window.pack();
			label.updateUI();
			Window.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showImage(Mat img) {
		Mat temp = null;
		if (SizeCustom) {
			temp = Mat.zeros(new Size(Width, Height), img.type());
			Imgproc.resize(img, temp, new Size(Width, Height));
		} else {
			temp = Mat.zeros(new Size(img.width(), img.height()), img.type());
			img.copyTo(temp);
		}
		image.setImage(toBufferedImage(temp));
		Window.pack();
		label.updateUI();
		Window.setVisible(true);
	}
	
	public void showImageHalfSize(Mat img) {
		Mat temp;
		if (SizeCustom) {
			temp = Mat.zeros(new Size(Width, Height), img.type());
			Imgproc.resize(img, temp, new Size(Width / 2, Height / 2));
		} else {
			temp = Mat.zeros(new Size(img.width() / 2, img.height() / 2), img.type());
			Imgproc.resize(img, temp, new Size(img.width() / 2, img.height() / 2));
		}
		image.setImage(toBufferedImage(temp));
		Window.pack();
		label.updateUI();
		Window.setVisible(true);
	}
	
	public Image toBufferedImage(Mat m){
	      int type = BufferedImage.TYPE_BYTE_GRAY;
	      if ( m.channels() > 1 ) {
	          type = BufferedImage.TYPE_3BYTE_BGR;
	      }
	      int bufferSize = m.channels()*m.cols()*m.rows();
	      byte [] b = new byte[bufferSize];
	      m.get(0,0,b); // get all the pixels
	      BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	      final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	      System.arraycopy(b, 0, targetPixels, 0, b.length);  
	      return image;

	  }

}