package javaFileIO;//CharSteamimport java.io.*;//字符流public class TestChar {    public static void main(String args[]) {        FileReader fr = null;        FileWriter fw = null;        ByteArrayInputStream bAIS = null;        ByteArrayOutputStream bAOS = null;        FileOutputStream fos = null;//用于测试字符流转字节流后写文件        try {            fr = new FileReader("D:/fromchar.txt");            fw = new FileWriter("D:/tochar.txt");            char[] buffer = new char[100];            //int temp = fr.read(buffer,0,buffer.length);            //for(int i = 0;i < buffer.length; i++){            //	System.out.println(buffer[i]);            //}            while (true) {                int temp = fr.read(buffer, 0, buffer.length);                if (temp == -1) {                    break;                }                //字符流--------------------//				fw.write(buffer, 0, temp);                //字符流转字节流的方法,仅供演示,没有实际意义------------------                byte[] bufferByte = buffer.toString().getBytes();                int length = bufferByte.length;                bAIS = new ByteArrayInputStream(bufferByte, 0, length);                int data = bAIS.read();                while (data != -1) {                    System.out.println(data + " ");                    data = bAIS.read();                }                bAOS = new ByteArrayOutputStream();                bAOS.write(bufferByte, 0, length);                fos = new FileOutputStream("D:/tochar.txt");                fos.write(bAOS.toByteArray());            }        } catch (Exception e) {            System.out.println(e);        } finally {            try {                if (fr != null)                    fr.close();                if (fw != null)                    fw.close();                if (bAIS != null)                    bAIS.close();// ByteArrayInputSystem 的close()方法实际上不执行任何操作                if (bAOS != null)                    bAOS.close();                if (fos != null)                    fos.close();            } catch (Exception e) {                System.out.println(e);            }        }    }}