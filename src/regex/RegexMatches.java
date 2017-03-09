package regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexMatches {

     //start 和 end 方法
    /*private static final String REGEX = "\\b\\scat\\b";
    private static final String INPUT = "cat cat cat cattie cat";
 
    public static void main( String args[] ){
       Pattern p = Pattern.compile(REGEX);
       Matcher m = p.matcher(INPUT); // 获取 matcher 对象
       int count = 0;
 
       while(m.find()) {
         count++;
         System.out.println("Match number "+count);
         System.out.println("start(): "+m.start());
         System.out.println("end(): "+m.end());
      }
   }*/

    // matches 和 lookingAt 方法 从开头开始匹配, lookingAt不全匹配，matches需要全匹配
   /* private static final String REGEX = "foo";
//    private static final String INPUT = "fooooooooooooooooo";
    private static final String INPUT = "foofoofoo";
    private static Pattern pattern;
    private static Matcher matcher;

    public static void main( String args[] ){
        pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(INPUT);

        System.out.println("Current REGEX is: "+REGEX);
        System.out.println("Current INPUT is: "+INPUT);

        System.out.println("lookingAt(): "+matcher.lookingAt());
        System.out.println("matches(): "+matcher.matches());
        System.out.println("find(index): "+matcher.find(7));
    }*/

      //replaceFirst 和 replaceAll 方法
    // replaceFirst 和 replaceAll 方法用来替换匹配正则表达式的文本。不同的是，replaceFirst 替换首次匹配，replaceAll 替换所有匹配
   /* private static String REGEX = "dog";
    private static String INPUT = "The dog says meow. " +
            "All dogs say meow.";
    private static String REPLACE = "cat";

    public static void main(String[] args) {
        Pattern p = Pattern.compile(REGEX);
        // get a matcher object
        Matcher m = p.matcher(INPUT);
        String replaceAllStr = m.replaceAll(REPLACE);
        String replaceFirst = m.replaceFirst(REPLACE);
        System.out.println("replaceAll结果" + replaceAllStr);
        System.out.println("replaceFirst结果" + replaceFirst);
    }*/


       //appendReplacement 和 appendTail 方法 Matcher 类也提供了appendReplacement 和 appendTail 方法用于文本替换
    /*private static String REGEX = "(a*b)";
//    private static String REGEX = "\\b(\\w+)\\b\\s+\\1\\b";
    private static String INPUT = "aabfooaabfooabfoob";
    private static String REPLACE = "-";*/
    public static void main(String[] args) {
//        Pattern p = Pattern.compile(REGEX);
//        // 获取 matcher 对象
//        Matcher m = p.matcher(INPUT);
//        StringBuffer sb = new StringBuffer();
//        while(m.find()){
//            m.appendReplacement(sb,REPLACE); //根据模式用replacement替换相应内容,并将匹配的结果添加到sb当前位置之后
//        }
////        m.appendTail(sb); //将输入序列中匹配之后的末尾字串添加到sb当前位置之后.
//        System.out.println(sb.toString());

          //演示零宽断言和PatternSyntaxException错误类
        String ResultString = null;
        try {
//            Pattern regex = Pattern.compile("(?<=<(\\w+)>).*(?=</\\1>)"); //会报错的
            Pattern regex = Pattern.compile("(?<=<(\\w)>)");
//            Pattern regex = Pattern.compile("(?<=<(\\w{1,10})>).*(?=</\\1>)"); //不会报错的  匹配exp后面的位置
//            Pattern regex = Pattern.compile("\\b\\w+(?=ing\\b)"); //不会报错的  匹配exp前面的位置
//            Pattern regex = Pattern.compile("(?!ing\\b)\\w+\\b"); //不会报错的  匹配后面跟的不是exp的位置
//            Pattern regex = Pattern.compile("\\b\\w+(?<!ing\\b)\\b"); //不会报错的  匹配前面跟的不是exp的位置
//            Pattern regex = Pattern.compile("2[0-4]\\d(/?#200-249)"); //java正则表达式应该没有注释一说

//            Matcher regexMatcher = regex.matcher("<asd>jessie</asd>");
//            Matcher regexMatcher = regex.matcher("ing");
            Matcher regexMatcher = regex.matcher("211");
            if (regexMatcher.find()) {
                ResultString = regexMatcher.group();
                System.out.println(ResultString);
            }
            else{
                System.out.println("aaaaaaaaaaaaaaaaa");
            }
        } catch (PatternSyntaxException ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getDescription());
            ex.printStackTrace();
        }

    }

}
