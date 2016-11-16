package javaFileIO;//import IO classimport java.io.*;//字节流class Test{	public static void main(String args []){		//State input stream quote		FileInputStream fis = null;		//state output stream quote		FileOutputStream fos = null;		InputStreamReader iSR = null;		BufferedReader bufferedReader = null;		OutputStreamWriter oSR = null;		BufferedWriter bufferedWriter = null;		try{			//Create target of input stream			fis = new FileInputStream("D:/from.txt");			//Create target of output stream			fos = new FileOutputStream("D:/to.txt");			/**			 * 字节流输入输出----------------------------			 *///			//Create an byte array//			byte [] buffer = new byte [100];//			//read data into the array//			int temp = fis.read(buffer,0,buffer.length);//			fos.write(buffer,0,temp);						/*			String s = new String(buffer); //change byte to string			s = s.trim(); //delete blank space in String's beginning and end			System.out.println(s);			*/			/*//print the data,in a bad way			for(int i = 0;i < buffer.length; i++){				System.out.println(buffer[i]);			}*/			/**			 *-----------------------------------------			 * 1：字节输入流转换为字符输入流：			 InputStreamReader是字节流向字符流的桥梁，它使用指定的charset读取字节并将其解码为字符，它使用的字符集可以由名称指定或显示给定。			 根据InputStream的实例创建InputStreamReader的方法有4种：			 InputStreamReader（InputStream in）//根据默认字符集创建			 InputStreamReader（InputStream in,Charset cs）//使用给定字符集创建			 InputStreamReader（InputStream in,CharsetDecoder dec）//使用给定字符集解码器创建			 InputStreamReader（InputStream in,String charsetName）//使用指定字符集创建			 2：字节输出流转换为字符输出流			 OutputStreamWriter是字符流通向字节流的桥梁，它使用指定的charset将要写入流中的字符编码成字节，它使用的字符集可以由名称指定或显示给定，否则将接受默认的字符集：			 根据根据InputStream的实例创建OutputStreamWriter的方法有4种：			 OutputStreamWriter（outputstream out）//根据默认的字符集创建			 OutputStreamWriter（outputstream out,charset cs)//使用给定的字符集创建			 OutputStreamWriter（outputstream out,charsetDecoder dec)//使用组定字符集创建			 OutputStreamWriter（outputstream out,String charsetName)//使用指定字符集创建			 */			iSR = new InputStreamReader(fis,"UTF-8");			//InputStreamReader 转换成带缓存的bufferedReader			bufferedReader = new BufferedReader(iSR);			//可以把读出来的内容赋值给字符			String ss = new String();			String s;			while((s = bufferedReader.readLine())!=null){				ss += s;			}			oSR = new OutputStreamWriter(fos,"UTF-8");			bufferedWriter = new BufferedWriter(oSR);			bufferedWriter.write(ss);		}		catch(Exception e){			System.out.println(e.getMessage());		}		finally{			try{				bufferedReader.close();				iSR.close();				bufferedWriter.close();				oSR.close();				fis.close();				fos.close();			}			catch(Exception e){				System.out.println(e.getMessage());			}		}	}}