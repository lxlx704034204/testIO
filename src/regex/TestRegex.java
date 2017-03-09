package regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main( String args[] ){

        // 按指定模式在字符串查找
//        String line = "This order was placed for QT3000! OK?"; //匹配分组
//        String line = "Java is the most beautiful language in the world\njava is not the most beautiful language in the world";
//        String line = "Java is the most beautiful language in the World\njava is not the most beautiful language in the world";
        String line = "zomo";
//        String line = "zoabczoo";
//        String line = "doeses";
//        String line = "foood";
//        String line = "fooood,fol";
//        String line = "foooood,fol";
//        String line = "\r\n";
//        String line = "\r\n";
//        String line = "industries";
// 按指定模式在字符串查找
//      String line = "Windows 2001";
//      String line = "zood";
//      String line = "adra";
//      String line = "A123rA";
//      String line = "A123rA";
//      String line = "fever";
//      String line = "getter";
//      String line = "getter";
//      String line = "\r";
//      String line = "++";
//      String line = "++";
//      String line = "\n";
//      String line = "dd";
//      String line = "A_1dd";
//      String line = " ";
//      String line = "___";
//        String line = "11 11"; //匹配 数组和字母或者下划线（带分组名）

//      String pattern = "Windows (?=95|98|NT|2000)";
//      String pattern = "Windows (?!95|98|NT|2000)";
//      String pattern = "(z|f)ood";
//      String pattern = "[abc]";
//      String pattern = "[^abc]";
//      String pattern = "[a-z]";
//      String pattern = "[^a-z]";
//      String pattern = "\\ber";
//      String pattern = "er\\B";
//      String pattern = "\\cM"; //匹配\r
//      String pattern = "\\d"; //匹配\r
//      String pattern = "\\D"; //匹配\r
//      String pattern = "\\f"; //匹配\r
//      String pattern = "\\n"; //匹配\r
//      String pattern = "\\s"; //匹配\r
//      String pattern = "\\S"; //匹配\r
//      String pattern = "\\w"; //匹配\r
//      String pattern = "\\W"; //匹配\r
//      String pattern = "(.)\\1\\1"; //匹配\r
//      String pattern = "\17"; //匹配 \nm ????????????????????????
//      String pattern = "\17"; //匹配 \nm

        //分组
//        String pattern = "\\b(?<Word>\\w+)\\b\\s+\\k<Word>\\b"; //匹配 数组和字母或者下划线（带分组名）
//        String pattern = "\\b(\\w+)(\\d+)\\b\\s+\\1\\2\\b";
//        String line = "helloworldnihaoworldssss";

//        String pattern = "(\\D*)(\\d+)(.*)";  //匹配分组

//        String pattern = "oa*"; //匹配 zomo

        String pattern = "zo+"; //匹配 o 1次以上

//        String pattern = "do(es)?"; //匹配 o 1次以上

//        String pattern = "o{2}"; //匹配 o 2次

//        String pattern = "o{2,3}"; //匹配 o 2次 默认贪婪

//        String pattern = "o{2,3}?"; //匹配 o 2-3次 非贪婪

//        String pattern = "."; //匹配 所有非换行和回车的字符

//        String pattern = "\\s"; //匹配 o 换行和回车

//        String pattern = "(industr(y|ies))"; //匹配 o 换行和回车

//        String pattern = "^java"; //匹配以java开头

//        String pattern = "world$"; //匹配以world结尾

//        String pattern = "(?=world)"; //匹配以world结尾

        // 创建 Pattern 对象
//        Pattern r = Pattern.compile(pattern);

        Pattern r =  Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); //匹配多行和忽略大小写

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);

         //测试分组
//        if (m.find( )) {
//
//            System.out.println("Found value: " + m.group(0) );
//            System.out.println("Found value: " + m.group(1) );
////            System.out.println("Found value: " + m.group(2) );
////            System.out.println("Found value: " + m.group(3) );
//        } else {
//            System.out.println("NO MATCH");
//        }

        while (m.find()) {
            System.out.println(m.group(0));
        }

    }
}
