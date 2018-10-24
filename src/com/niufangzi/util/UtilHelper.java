package com.niufangzi.util;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import org.apache.commons.lang.StringUtils;

import com.niufangzi.model.OperationState;

import java.lang.reflect.Field;

public class UtilHelper {

	private static Logger logger = LogManager.getLogger();
	
	/*
	 * 将网络图片进行base64编码
	 * imageUrl:图片的路径，如： http://.....xx.jpg
	 */
	public static String encodeImageToBase64(String url) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		ByteArrayOutputStream outputStream = null;
		try {
			
			URL imageUrl = new URL(url);
			BufferedImage bufferedImage = ImageIO.read(imageUrl);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
			//对字节数组Base64编码
		    BASE64Encoder encoder = new BASE64Encoder();
		    return encoder.encode(outputStream.toByteArray()); //返回Base64编码过的字节数组字符串
		} catch (Exception e) {
			logger.error(url + " : " + e.toString());
			return null;
		} 
	    
	}

	/*
	 * 将本地图片进行Base64位编码
	 * imageFile : 本地文件名
	 */
	public static String encodeImageToBase64(File imageFile) { //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		ByteArrayOutputStream outputStream = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(imageFile);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    //对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(outputStream.toByteArray()); //返回Base64编码过的字节数组字符串
	}

	/*
	 * 将Base64位编码的图片进行解码，并保存到指定目录
	 * base64：base64编码的图片信息
	 */
	public static void decodeBase64ToImage(String base64, String path, String imgName) {
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			FileOutputStream write = new FileOutputStream(new File(path + imgName));
			byte[] decoderBytes = decoder.decodeBuffer(base64);
			write.write(decoderBytes);
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	  
	/*
	 * 将网络图片存到本地
	 * imageURL: 图片的URL
	 * imageFile : 本地文件名(含路径)
	 */
    public static boolean downloadWebImage(String imgURL, String imgName) {  
        boolean res = true;
        URL url = null;   
        try {
        	File file = new File(imgName);   
        	if (! file.exists()) //不存在才下载
        	{
		        url = new URL(imgURL);  
		        DataInputStream dataInputStream = new DataInputStream(url.openStream());    
		        FileOutputStream fileOutputStream = new FileOutputStream(new File(imgName));  
		        byte[] buffer = new byte[1024];  
		        int length;  
		        while ((length = dataInputStream.read(buffer)) > 0) {  
		            fileOutputStream.write(buffer, 0, length);  
		        }
		        dataInputStream.close();  
		        fileOutputStream.close();
        	}
        } catch (Exception e) {  
        	res = false;
            e.printStackTrace();  
        } 
        return res;
    } 
	
	
	/*
	 * 检查字符串是否为数字
	 * 注意：+11 及 -11 格式会返回false
	 */
	public static boolean isNumeric(String str) {
		if (StringUtils.equalsIgnoreCase(str, ""))
		{	
			return false;
		}
		return StringUtils.isNumeric(str);
	}
	
	public static int tryStrToInt(String str, OperationState state) {
		int res = 0;
		if (state != null) {
			state.result = true; //默认操作成功
		}
		try {
			res = Integer.parseInt(str.trim());
		}
		catch (Exception e) {
			if(state != null) {
				state.result = false;
				state.functionName = "tryStrToInt";
				state.errorMsg = e.getMessage();
			}
		}
		return res;
	}
	
	public static float tryStrToFloat(String str, OperationState state) {
		float res = 0;
		state.result = true; //默认操作成功
		try {
			res = Float.parseFloat(str.trim());
		}
		catch (Exception e) {
			state.result = false;
			state.functionName = "tryStrToFloat";
			state.errorMsg = e.getMessage();
		}
		return res;
	}

	public static Date tryStrToDate(String strDate, String strFormat, OperationState state) {
		Date res = new Date();
		state.result = true; //默认操作成功
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(strFormat);  
			res = sdf.parse(strDate.trim()); 
		}
		catch (Exception e) {
			state.result = false;
			state.functionName = "tryStrToDate";
			state.errorMsg = e.getMessage();
		}
		return res;		
	}
	
	public static String DateToStr(Date date, String strFormat) {
		String str = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(strFormat);	
			str = sdf.format(date); 
		}
		catch (Exception e) {
			//
		}
		return str;
	}
	
	
	//得到对象中所有的字段 名和值
    public static String ObjectToString(Object obj)    
    {
    	String strHint = ":";    //字段名称和值之间的分割字符
		String strSplit = "\n";  //字段间分割字符
		StringBuffer sb = new StringBuffer("");
        // 获取类中的所有定义字段    
		Class<?> c = obj.getClass();
        Field[ ] fields = c.getDeclaredFields( );    
        // 循环遍历字段，获取字段对应的属性值    
        for ( Field field : fields )    
        {    
            field.setAccessible( true );     //设置可见性 
            try    
            {    
                sb.append(field.getName()).append(strHint).append(field.get(obj)).append(strSplit);
            }    
            catch ( Exception e )    
            {    
            	//
            }    
        }    
        return sb.toString();    
    } 
	
	
	
	
	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
		//URL imgUrl = new URL("http://cms-bucket.nosdn.127.net/catchpic/1/1A/1A4B252037666957E214E81B0D8CB38D.jpg?imageView&thumbnail=550x0");
		
		String strImg = encodeImageToBase64("https://image1.ljcdn.com/x-se/hdic-frame/710dab32-dacd-4371-933c-0f833fbb5260.png.533x400.jpg");
		System.out.println(strImg);
		
		/*
		float a = 1608;
		float b = 1400;
		float c = (b-a)*100 / a;
		
		
		BigDecimal bd  = new  BigDecimal(c);  
		float d = bd.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();  
		
		System.out.println(d);
		*/
		

	}

}
