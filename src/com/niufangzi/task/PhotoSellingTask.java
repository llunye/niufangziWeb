package com.niufangzi.task;

import com.niufangzi.util.UtilHelper;

public class PhotoSellingTask {

	
	private static void doTest()
	{
		System.out.println("begin download image");
		String imgURL = "https://image1.ljcdn.com/x-se/hdic-frame/5d56dc71-eddb-4403-acae-32084240bf35.png.533x400.jpg"; 
		String imgName = "G:/download/huxing.jpg";
		UtilHelper.downloadWebImage(imgURL, imgName);
		System.out.println("end");
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
