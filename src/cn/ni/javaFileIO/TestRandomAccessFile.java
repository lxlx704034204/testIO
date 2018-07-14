package cn.ni.javaFileIO;



import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
/**
 * RandomAccessFile的特点在于任意访问文件的任意位置，可以说是基于字节访问的，可通过getFilePointer()获取当前指针所在位置，
 * 可通过seek()移动指针，这体现了它的任意性，也是其与其他I/O流相比，自成一派的原因
 *
 * 一句话总结：seek用于设置文件指针位置，设置后ras会从当前指针的下一位读取到或写入到
 * @author Administrator
 *
 */
public class TestRandomAccessFile {
    public static void main(String[] args) {
        RandomAccessFile ras = null;
        RandomAccessFile ras2 = null;
        try {
            File file=new File("D:/fromRandom.txt");//创建一个txt文件,内容是1234567890abcdefghijklmn
            ras=new RandomAccessFile(file, "rw");
            //默认情况下ras的指针为0，即从第1个字节读写到
            ras.seek(8);

            File file2=new File("D:/toRandom.txt");
            ras2=new RandomAccessFile(file2, "rw");
            ras2.setLength(10);
            ras2.seek(5);
            byte[] buffer=new byte[32];
            int len=0;
            while((len=ras.read(buffer))!=-1){
                ras2.write(buffer, 0, len);//从ras2的第6个字节被写入，因为前面设置ras2的指针为5
                //ras2的写入结果是:to.txt的内容为前5位是空格，第6位是9
                //待写入的位置如果有内容将会被新写入的内容替换
            }

            System.out.println("ok");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                if(ras!=null)
                    ras.close();
                if(ras2!=null)
                    ras2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

