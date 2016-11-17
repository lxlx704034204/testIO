package testSerializable;

import java.io.*;
import java.util.*;

//一定要实现Serializable接口才能被序列化
public class UserInfo implements Serializable {
    public String userName;
    public String userPass;
    //注意，userAge变量前面的transient
    public transient int userAge;

    public UserInfo(){
    }

    public UserInfo(String username,String userpass,int userage){
        this.userName=username;
        this.userPass=userpass;
        this.userAge=userage;
    }

    public String toString(){
        return "用户名: "+this.userName+";密码："+this.userPass+
                ";年龄："+this.userAge;
    }
}
