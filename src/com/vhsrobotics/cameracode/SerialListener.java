package com.vhsrobotics.cameracode;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialListener implements SerialPortEventListener {
	
	StreamHandler h;
	SerialPort s;
	
	public SerialListener (StreamHandler handler, SerialPort serial) {
		h = handler;
		s = serial;
	}

	@Override
	public void serialEvent(SerialPortEvent e) {
		if (e.isRXCHAR() && e.getEventValue() > 0) {
			String temp = "";
			try {
				temp = s.readString();
			} catch (SerialPortException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (temp.equals("a")) {
				h.transmitting = true;
			}
			if (temp.equals("b")) {
				h.transmitting = false;
			}
		}
	}

}
