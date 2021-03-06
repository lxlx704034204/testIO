package cn.ni.testSerializable;

import java.io.*;
import java.util.*;
public class Test {

    //序列化对象到文件
    public static void serialize(String fileName){
        try
        {
            //创建一个对象输出流，讲对象输出到文件
            ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(fileName));

            out.writeObject("序列化日期是：");  //序列化一个字符串到文件

            out.writeObject(new Date());    //序列化一个当前日期对象到文件

            UserInfo user=new UserInfo("renyanwei","888888",20);
            out.writeObject(user);  //序列化一个会员对象

            out.close();
        }
        catch (Exception x)
        {
            System.out.println(x.toString());
        }

    }
    //从文件反序列化到对象
    public static void deserialize(String fileName){
        try
        {
            //创建一个对象输入流，从文件读取对象
            ObjectInputStream in=new ObjectInputStream(new FileInputStream(fileName));

            //注意读对象时必须按照序列化对象顺序读，否则会出错
            //读取字符串
            String today=(String)(in.readObject());
            System.out.println(today);

            //读取日期对象
            Date date=(Date)(in.readObject());
            System.out.println(date.toString());

            //读取UserInfo对象并调用它的toString()方法
            UserInfo user=(UserInfo)(in.readObject());
            System.out.println(user.toString());

            in.close();
        }
        catch (Exception x)
        {
            System.out.println(x.toString());
        }

    }

    public static void main(String[] args) {
        serialize("D:\\test.txt");
        System.out.println("序列化完毕");

        deserialize("D:\\test.txt");
        System.out.println("反序列化完毕");
    }

}
