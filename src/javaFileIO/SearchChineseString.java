package javaFileIO;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.StringUtil;

/**
 * @author Created by chay on 2018/1/31.
 *         查找所有文件中的中文字符串，并根据目录名-文件名-行数/拼音首字母生成变量名
 */
public class SearchChineseString {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchChineseString.class);

    //要查找的文件类型集合
    private final static List<String> FILE_TYPES = Arrays.asList(
            ".java"
    );

    //源文件保存路径，输出的替换文件在该目录的/getChinese文件夹下，输出的properties文件在该目录下
//    private final static String SOURCE_PATH = "D:/test";
    private final static String SOURCE_PATH = "D:/test/Goor-mrobot-noah";

    //输出的资源文件名
    private final static String OUTPUT_PROPERTY_FILE_NAME = "messages_zh_CN.properties";
    private final static String OUTPUT_PROPERTY_PATH_NAME = "getChinese_abcdefg";

    //包裹中文的修饰符，java是双引号
    private final static String CHINESE_CONTAINER_REGEX = "\"([^\"])*\"";

    //中文正则
    private final static String CHINESE_CHARACTER_REGEX = ".*[\\u4e00-\\u9fa5]+.*";

    //输出properties文件命名正则:这里取所有非字母和数字的字符，都替换成_
    private final static String PROPERTIES_FILE_KEY_REGEX = "[^A-Za-z0-9]+";

    //java引用的properties类的名称
    private final static String PROPERTIES_CONSTANT_CLASS_NAME = "PropertiesUtil";

    //判断是log行的正则表达式，根据要校验的文件添加即可
    private final static String[] LOG_LINE_REGEX_LIST = {
            ".*system.out.print\\({1}.*",
            ".*system.out.println\\({1}.*",
            ".*system.out.printf\\({1}.*",
            ".*(log|logger){1}([\\.]{1}(info|error|debug){1}[\\(]{1}){1}.*"
    };

    //统计总共的中文数量
    private int totalChinese = 0;

    /**
     * 查找所有文件中的中文字符串，并根据目录名-文件名-行数/拼音首字母生成变量名替换输出新文件。
     * 如果文件不是我们要搜索的文件，则直接复制输出。
     * 同时会输出一个简体中文资源文件。
     *
     * @param file
     * @return
     */
    public boolean replaceChinese(File file, String sourcePath) {
        try {

            String filePath = file.getPath();
            if (filePath.contains(OUTPUT_PROPERTY_PATH_NAME)) {
                LOGGER.info("该路径是我们的输出路径，跳过");
                infoMessage("该路径是我们的输出路径，跳过");
                return true;
            }
            if (file.exists()) {
                String tempFilePath = filePath.replaceAll("\\\\", "/");//把路径中的反斜杠替换成斜杠
                String tempSourcePath = SOURCE_PATH.replaceAll("\\\\", "/") + "/";
                String tempTargetPath = (SOURCE_PATH + File.separator + OUTPUT_PROPERTY_PATH_NAME + File.separator).replaceAll("\\\\", "/");
                String targetPath = tempFilePath.replaceAll(tempSourcePath, tempTargetPath);
                //如果是文件，则读取其中的中文字符串
                if (file.isFile()) {
                    //获得文件扩展名
                    String lastName = file.getName();
                    LOGGER.info("文件名:{}", lastName);
                    infoMessage("文件名:{}", lastName);
                    int findPosition = lastName.lastIndexOf(".");
                    String fileKindString = null;
                    if (findPosition > -1) {
                        fileKindString = lastName.substring(findPosition).trim().toLowerCase();
                        LOGGER.info("findPosition:{},找到.,扩展名为:{}。", findPosition, fileKindString);
                        infoMessage("findPosition:{},找到.,扩展名为:{}。", findPosition, fileKindString);

                    }

                    String outputFilePath = targetPath.replaceAll("\\\\", "/");
                    ;
                    //如果未找到扩展名或者扩展名不是我们要替换的文件，则直接复制文件
                    if (StringUtil.isBlank(fileKindString) || !FILE_TYPES.contains(fileKindString)) {
                        LOGGER.info("未找到扩展名,可能不是需要替换的文件,直接复制到对应目录");
                        infoMessage("未找到扩展名,可能不是需要替换的文件,直接复制到对应目录");
                        // Destination directory
                        File dir = new File(outputFilePath);
                        File parentPath = new File(dir.getParent());
                        parentPath.mkdirs();
                        //移动文件 Move file to new directory
                        /*boolean success = file.renameTo(dir);
                        return success;*/
                        //复制文件
                        copyFile(file.getPath(), outputFilePath);
                        return true;
                    }

                    //从源文件，读取中文字符串，写入输出文件。同时写入资源文件。
                    return readFileChinese(tempFilePath, outputFilePath);
                }
                //如果是目录则递归遍历
                else if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        replaceChinese(files[i], sourcePath);
                    }
                }
                return true;
            } else {
                LOGGER.error("文件不存在！" + '\n');
                errorMessage("文件不存在！" + '\n');
                return false;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从源文件，读取中文字符串，写入输出文件。同时写入资源文件。
     *
     * @param sourceFilePath "D:/from.txt"
     * @param outPutFilePath "D:/to.txt"
     * @return
     */
    private boolean readFileChinese(String sourceFilePath, String outPutFilePath) {
        //State input stream quote
        FileInputStream fis = null;
        InputStreamReader iSR = null;
        BufferedReader bufferedReader = null;

        try {
            //Create target of input stream "D:/from.txt"
            fis = new FileInputStream(sourceFilePath);

            /**
             *-----------------------------------------
             * 1：字节输入流转换为字符输入流：
             InputStreamReader是字节流向字符流的桥梁，它使用指定的charset读取字节并将其解码为字符，它使用的字符集可以由名称指定或显示给定。
             根据InputStream的实例创建InputStreamReader的方法有4种：
             InputStreamReader（InputStream in）//根据默认字符集创建
             InputStreamReader（InputStream in,Charset cs）//使用给定字符集创建
             InputStreamReader（InputStream in,CharsetDecoder dec）//使用给定字符集解码器创建
             InputStreamReader（InputStream in,String charsetName）//使用指定字符集创建
             2：字节输出流转换为字符输出流
             OutputStreamWriter是字符流通向字节流的桥梁，它使用指定的charset将要写入流中的字符编码成字节，它使用的字符集可以由名称指定或显示给定，否则将接受默认的字符集：
             根据根据InputStream的实例创建OutputStreamWriter的方法有4种：
             OutputStreamWriter（outputstream out）//根据默认的字符集创建
             OutputStreamWriter（outputstream out,charset cs)//使用给定的字符集创建
             OutputStreamWriter（outputstream out,charsetDecoder dec)//使用组定字符集创建
             OutputStreamWriter（outputstream out,String charsetName)//使用指定字符集创建
             */
            iSR = new InputStreamReader(fis, "UTF-8");
            //InputStreamReader 转换成带缓存的bufferedReader
            bufferedReader = new BufferedReader(iSR);
            //可以把读出来的内容赋值给字符
            String replaceString = new String();
            String orinString;
            Map<String, String> allChineseProperties = new HashMap<String, String>();
            while ((orinString = bufferedReader.readLine()) != null) {
                //如果是日志行，直接复制
                if (isLogLine(orinString)) {
                    LOGGER.info("#########该行是日志行,原文: {} ", orinString);
                    infoMessage("#########该行是日志行,原文:{}", orinString);
                    replaceString += (orinString + "\r\n");
                    continue;
                }

                ArrayList<String> chineseArray = new ArrayList<String>();
                //根据正则截取中文字符串
                chineseArray = getChineseStringArray(orinString, chineseArray);
                if (chineseArray == null || chineseArray.size() <= 0) {
                    LOGGER.info("#########该行没找到中文字符串,原文: {} ", orinString);
                    infoMessage("#########该行没找到中文字符串,原文:{}", orinString);
                    //没找到字符串，本行直接复制。并在末尾加上换行符
                    replaceString += (orinString + "\r\n");
                    continue;
                }

                //TODO 如果在本行找到了文本，则需要对找到的字符串进行变量替换，并输出变量到另一个properties文件
                for (String temp : chineseArray) {
                    //把所有目录中的特殊字符替换成_作为key
                    String key = sourceFilePath.replaceAll(PROPERTIES_FILE_KEY_REGEX, "_");
                    //拼接大写汉字首字母
                    key += "_" + StringUtil.getSearchName(temp);
                    //判断该key值是不是已经存在
                    String oldString = allChineseProperties.get(key);
                    if (oldString == null) {
                        LOGGER.info("key:{},value:{},未保存过，添加到输出", key, temp);
                        infoMessage("key:{},value:{},未保存过，添加到输出", key, temp);
                        //不存在，则直接输出到最终map
                        allChineseProperties.put(key, temp);
                    } else {
                        //如果该key值已经存在，校验value是不是完全一样，如果不一样，则需要在key后面加时间戳做区分，作为一个新的
                        if (!oldString.equals(temp)) {
                            key += System.currentTimeMillis();
                            allChineseProperties.put(key, temp);
                        }
                        //两个值完全一样，则只执行文本的替换操作
                    }
                    //统一执行原文的替换文本的操作
                    replaceString += (orinString.replaceAll(temp, PROPERTIES_CONSTANT_CLASS_NAME + "." + key) + "\r\n");
                    LOGGER.info("替换前文本:{},替换后文本:{}", temp, key);
                    infoMessage("替换前文本:{},替换后文本:{}", temp, key);
                }
                LOGGER.info("该行找到的一系列中文字符串为：{},开始替换", chineseArray);
                infoMessage("该行找到的一系列中文字符串为：{},开始替换", chineseArray);
            }


            //Create target of output stream
            File dir = new File(outPutFilePath);
            File parentPath = new File(dir.getParent());
            //创建目录
            parentPath.mkdirs();
            //创建文件
            dir.createNewFile();
            infoMessage("outputFilePath:{}", outPutFilePath);
            writeFile(outPutFilePath, false, replaceString, "UTF-8");

            //再先读再写properties文件，文件路径还有重复
            Iterator<Map.Entry<String, String>> entries = allChineseProperties.entrySet().iterator();
            String propertiesString = "";
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                String value = entry.getValue();
                propertiesString += (entry.getKey() + "=" + replaceFirstLastDoubleQuotationMarks(value) + "\r\n");
                totalChinese++;
                LOGGER.info("总共的中文条数:{}", totalChinese);
                infoMessage("总共的中文条数:{}", totalChinese);
            }
            if (StringUtil.isNullOrEmpty(propertiesString)) {
                LOGGER.info("该文件不包含中文，文件路径:{}", sourceFilePath);
                infoMessage("该文件不包含中文，文件路径:{}", sourceFilePath);
                return true;
            }

            //TODO
            File propDir = new File((SOURCE_PATH + File.separator + OUTPUT_PROPERTY_FILE_NAME).replaceAll("\\\\", "/"));
            if (!propDir.exists()) {
                File propParentPath = new File(propDir.getParent());
                //创建目录
                propParentPath.mkdirs();
                //创建文件
                propDir.createNewFile();
            }
            //true表示在原来的文件后追加输出
            writeFile(propDir.getPath(), true, propertiesString, "UTF-8");
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (iSR != null) {
                    iSR.close();
                }
                if (fis != null) {
                    fis.close();
                }
                return true;
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 替换第一个和最后一个双引号
     *
     * @param value
     * @return
     */
    private String replaceFirstLastDoubleQuotationMarks(String value) {
        //去掉第一个 "
        if (value.indexOf("\"") == 0) {
            value = value.substring(1, value.length());
        }
        //去掉最后一个 "
        if (value.lastIndexOf("\"") == (value.length() - 1)) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * 从一行字符串中查找所有中文字符串
     * 输出格式： key = value \r\n（换行符）
     *
     * @param s
     * @return
     */
    private ArrayList<String> getChineseStringArray(String s, ArrayList<String> history) {
        String regex = null;
        StringByRegexResult sReturn = null;
        //分隔符是不是有多个
        boolean isMultiRegex = false;
        //过滤第一个//之后的字符
        regex = "//";
        sReturn = getStringByRegexBefore(s, regex);

        //过滤第一个/*之后的字符
        regex = "/\\*";
        sReturn = getStringByRegexBefore(sReturn.getStringResult(), regex);
        isMultiRegex = isMultiRegex || (sReturn.getSplitLength() > 1 ? true : false);

        //过滤第一个*/之前的字符
        regex = "\\*/";
        sReturn = getStringByRegexAfter(sReturn.getStringResult(), regex);
        isMultiRegex = isMultiRegex || (sReturn.getSplitLength() > 1 ? true : false);

        //完成一轮/*与*/之间的字符搜索，如果检测含有多个/*或*/则递归搜索
        if (isMultiRegex) {
            return getChineseStringArray(sReturn.getStringResult(), history);
        }
        //只有一层注释字符，则匹配中间的中文，提取到列表
        else {
            //TODO 匹配中文
            //1.将正在表达式封装成对象Patten 类来实现
            Pattern pattern = Pattern.compile(CHINESE_CONTAINER_REGEX);
            //2.将字符串和正则表达式相关联
            Matcher matcher = pattern.matcher(sReturn.getStringResult());

            //查找符合规则的子串
            while (matcher.find()) {
                //获取 字符串
                String temp = matcher.group();
                //获取的字符串的首位置和末位置
                LOGGER.info("字符串的首位置{}和末位置{}", matcher.start(), matcher.end());
                infoMessage("字符串的首位置{}和末位置{}", matcher.start(), matcher.end());
                //如果包含中文
                if (matchLookingAt(CHINESE_CHARACTER_REGEX, temp, null)) {
                    LOGGER.info("该引号分隔后字符串‘包含’中文:{}", temp);
                    infoMessage("该引号分隔后字符串‘包含’中文:{}", temp);
                    history.add(temp);
                } else {
                    LOGGER.info("该引号分隔后字符串‘不包’含中文:{}", temp);
                    infoMessage("该引号分隔后字符串‘不包’含中文:{}", temp);
                }
            }
            return history;
        }
    }

    private static String BEFORE = "before";
    private static String AFTER = "after";

    /**
     * 根据regex过滤字符串，取前面的还是后面的。
     * 如果是取后面的，只取第一个分隔符拆分的后面的。
     *
     * @param string
     * @param regex
     * @param direction
     * @return
     */
    private StringByRegexResult getStringByRegex(String string, String regex, String direction) {
        StringByRegexResult sReturn = new StringByRegexResult();
        if (StringUtil.isNullOrEmpty(string)) {
            LOGGER.info("该行为空行,不判断");
            infoMessage("该行为空行,不判断");
            sReturn.setStringResult(string);
            sReturn.setSplitLength(0);
            return sReturn;
        }
        String[] temp = string.split(regex);
        int length = (temp == null ? 0 : temp.length);
        //取regex前面的字符串
        if (direction.equals(BEFORE)) {
            LOGGER.info("过滤 {} 之后的注释语，保留前面的", regex);
            infoMessage("过滤 {} 之后的注释语，保留前面的", regex);
            if (!isNullOrEmptyStringArray(temp)) {
                if (length <= 1) {
                    LOGGER.info("本行不包含 {} 注释", regex);
                    infoMessage("本行不包含 {} 注释", regex);
                } else {
                    LOGGER.info("本行包含 {} 注释", regex);
                    infoMessage("本行包含 {} 注释", regex);
                }
                //取第一个，过滤掉所有注释后面的
                sReturn.setStringResult(temp[0]);
            }
        } else if (direction.equals(AFTER)) {
            LOGGER.info("过滤 {} 之前的注释语，保留后面的", regex);
            infoMessage("过滤 {} 之前的注释语，保留后面的", regex);
            if (!isNullOrEmptyStringArray(temp)) {
                if (length <= 1) {
                    LOGGER.info("本行不包含 {} 注释", regex);
                    infoMessage("本行不包含 {} 注释", regex);
                } else {
                    LOGGER.info("本行包含 {} 注释", regex);
                    infoMessage("本行包含 {} 注释", regex);
                }
                //过滤掉第一个，保留第一个注释后面的
                sReturn.setStringResult(string.replace(temp[0] + regex, ""));
            }
        } else {
            LOGGER.info("保留字符串 {} 方向未识别！错误！", direction);
            infoMessage("保留字符串 {} 方向未识别！错误！", direction);
        }
        sReturn.setSplitLength(length);
        return sReturn;
    }

    /**
     * 取regex之前的string
     *
     * @param string
     * @param regex
     * @return
     */
    private StringByRegexResult getStringByRegexBefore(String string, String regex) {
        return getStringByRegex(string, regex, BEFORE);
    }

    /**
     * 取regex之后的string
     *
     * @param string
     * @param regex
     * @return
     */
    private StringByRegexResult getStringByRegexAfter(String string, String regex) {
        return getStringByRegex(string, regex, AFTER);
    }

    /**
     * 判断数组是否为空
     *
     * @param strings
     * @return
     */
    private boolean isNullOrEmptyStringArray(String[] strings) {
        if (strings == null || strings.length <= 0) {
            LOGGER.info("数组为空或长度为0");
            infoMessage("数组为空或长度为0");
            return true;
        }
        LOGGER.info("数组长度为" + strings.length);
        infoMessage("数组长度为" + strings.length);
        return false;
    }

    /**
     * 校验该行是不是log语句
     *
     * @param lineString
     * @return
     */
    private static boolean isLogLine(String lineString) {
        boolean result = false;
        for (String regex : LOG_LINE_REGEX_LIST) {
            result = result || matchLookingAt(regex, lineString, Pattern.CASE_INSENSITIVE);
            //如果匹配到是注释行，则停止遍历，返回
            if (result) {
                return result;
            }
        }
        return result;
    }

    /**
     * @param regex 正则表达式字符串
     * @param str   要匹配的字符串
     * @param flags 匹配设置：
     *              Pattern.UNIX_LINES
     *              启用 Unix 行模式。
     *              在此模式中，.、^ 和 $ 的行为中仅识别 '/n'行结束符。
     *              通过嵌入式标志表达式 (?d) 也可以启用 Unix 行模式。
     *              CASE_INSENSITIVE
     *              启用不区分大小写的匹配。
     *              默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。可以通过指定 #UNICODE_CASE标志连同此标志来启用 Unicode 感知的、不区分大小写的匹配。
     *              通过嵌入式标志表达式  (?i)也可以启用不区分大小写的匹配。
     *              指定此标志可能对性能产生一些影响。
     *              COMMENTS
     *              模式中允许空白和注释。
     *              此模式将忽略空白和在结束行之前以 #开头的嵌入式注释。
     *              通过嵌入式标志表达式  (?x) 也可以启用注释模式。
     *              MULTILINE
     *              启用多行模式。
     *              在多行模式中，表达式 ^ 和 $仅分别在行结束符前后匹配，或者在输入序列的结尾处匹配。默认情况下，这些表达式仅在整个输入序列的开头和结尾处匹配。
     *              通过嵌入式标志表达式 (?m) 也可以启用多行模式。
     *              LITERAL
     *              启用模式的字面值解析。
     *              指定此标志后，指定模式的输入字符串就会作为字面值字符序列来对待。输入序列中的元字符或转义序列不具有任何特殊意义。
     *              标志 CASE_INSENSITIVE 和 UNICODE_CASE 在与此标志一起使用时将对匹配产生影响。其他标志都变得多余了。
     *              不存在可以启用字面值解析的嵌入式标志字符。
     *              DOTALL
     *              启用 dotall 模式。
     *              在 dotall 模式中，表达式 .可以匹配任何字符，包括行结束符。默认情况下，此表达式不匹配行结束符。
     *              通过嵌入式标志表达式 (?s) 也可以启用 dotall 模式（s 是 "single-line" 模式的助记符，在 Perl 中也使用它）。
     *              UNICODE_CASE
     *              启用 Unicode 感知的大小写折叠。
     *              指定此标志后，由 #CASE_INSENSITIVE标志启用时，不区分大小写的匹配将以符合 Unicode Standard 的方式完成。默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。
     *              通过嵌入式标志表达式 (?u)也可以启用 Unicode 感知的大小写折叠。
     *              指定此标志可能对性能产生影响。
     *              CANON_EQ
     *              启用规范等价。
     *              指定此标志后，当且仅当其完整规范分解匹配时，两个字符才可视为匹配。例如，当指定此标志时，表达式 "a/u030A" 将与字符串 "/u00E5"匹配。默认情况下，匹配不考虑采用规范等价。
     *              不存在可以启用规范等价的嵌入式标志字符。
     *              指定此标志可能对性能产生影响。
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     * matches:整个匹配，只有整个字符序列完全匹配成功，才返回True，否则返回False。但如果前部分匹配成功，将移动下次匹配的位置。
     * lookingAt:部分匹配，总是从第一个字符进行匹配,匹配成功了不再继续匹配，匹配失败了,也不继续匹配。
     * find:部分匹配，从当前位置开始匹配，找到一个匹配的子串，将移动下次匹配的位置。
     */
    private static boolean matchMatches(String regex, String str, Integer flags) {
        Matcher matcher = getMatcher(regex, str, flags);
        return matcher.matches();
    }

    /**
     * @param regex 正则表达式字符串
     * @param str   要匹配的字符串
     * @param flags 匹配设置：
     *              Pattern.UNIX_LINES
     *              启用 Unix 行模式。
     *              在此模式中，.、^ 和 $ 的行为中仅识别 '/n'行结束符。
     *              通过嵌入式标志表达式 (?d) 也可以启用 Unix 行模式。
     *              CASE_INSENSITIVE
     *              启用不区分大小写的匹配。
     *              默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。可以通过指定 #UNICODE_CASE标志连同此标志来启用 Unicode 感知的、不区分大小写的匹配。
     *              通过嵌入式标志表达式  (?i)也可以启用不区分大小写的匹配。
     *              指定此标志可能对性能产生一些影响。
     *              COMMENTS
     *              模式中允许空白和注释。
     *              此模式将忽略空白和在结束行之前以 #开头的嵌入式注释。
     *              通过嵌入式标志表达式  (?x) 也可以启用注释模式。
     *              MULTILINE
     *              启用多行模式。
     *              在多行模式中，表达式 ^ 和 $仅分别在行结束符前后匹配，或者在输入序列的结尾处匹配。默认情况下，这些表达式仅在整个输入序列的开头和结尾处匹配。
     *              通过嵌入式标志表达式 (?m) 也可以启用多行模式。
     *              LITERAL
     *              启用模式的字面值解析。
     *              指定此标志后，指定模式的输入字符串就会作为字面值字符序列来对待。输入序列中的元字符或转义序列不具有任何特殊意义。
     *              标志 CASE_INSENSITIVE 和 UNICODE_CASE 在与此标志一起使用时将对匹配产生影响。其他标志都变得多余了。
     *              不存在可以启用字面值解析的嵌入式标志字符。
     *              DOTALL
     *              启用 dotall 模式。
     *              在 dotall 模式中，表达式 .可以匹配任何字符，包括行结束符。默认情况下，此表达式不匹配行结束符。
     *              通过嵌入式标志表达式 (?s) 也可以启用 dotall 模式（s 是 "single-line" 模式的助记符，在 Perl 中也使用它）。
     *              UNICODE_CASE
     *              启用 Unicode 感知的大小写折叠。
     *              指定此标志后，由 #CASE_INSENSITIVE标志启用时，不区分大小写的匹配将以符合 Unicode Standard 的方式完成。默认情况下，不区分大小写的匹配假定仅匹配 US-ASCII 字符集中的字符。
     *              通过嵌入式标志表达式 (?u)也可以启用 Unicode 感知的大小写折叠。
     *              指定此标志可能对性能产生影响。
     *              CANON_EQ
     *              启用规范等价。
     *              指定此标志后，当且仅当其完整规范分解匹配时，两个字符才可视为匹配。例如，当指定此标志时，表达式 "a/u030A" 将与字符串 "/u00E5"匹配。默认情况下，匹配不考虑采用规范等价。
     *              不存在可以启用规范等价的嵌入式标志字符。
     *              指定此标志可能对性能产生影响。
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     * matches:整个匹配，只有整个字符序列完全匹配成功，才返回True，否则返回False。但如果前部分匹配成功，将移动下次匹配的位置。
     * lookingAt:部分匹配，总是从第一个字符进行匹配,匹配成功了不再继续匹配，匹配失败了,也不继续匹配。
     * find:部分匹配，从当前位置开始匹配，找到一个匹配的子串，将移动下次匹配的位置。
     */
    private static boolean matchLookingAt(String regex, String str, Integer flags) {
        Matcher matcher = getMatcher(regex, str, flags);
        return matcher.lookingAt();
    }

    /**
     * 获取Matcher对象
     *
     * @param regex
     * @param str
     * @param flags
     * @return
     */
    private static Matcher getMatcher(String regex, String str, Integer flags) {
        Pattern pattern;
        if (flags == null) {
            pattern = Pattern.compile(regex);
        } else {
            pattern = Pattern.compile(regex, flags);
        }
        return pattern.matcher(str);
    }

    /**
     * 读文件，并返回字符串数组
     *
     * @param sourceFilePath
     * @return
     */
    private List<String> readFile(String sourceFilePath) {
        //State input stream quote
        FileInputStream fis = null;
        InputStreamReader iSR = null;
        BufferedReader bufferedReader = null;
        List<String> result = new ArrayList<String>();
        try {
            //Create target of input stream "D:/from.txt"
            fis = new FileInputStream(sourceFilePath);

            /**
             *-----------------------------------------
             * 1：字节输入流转换为字符输入流：
             InputStreamReader是字节流向字符流的桥梁，它使用指定的charset读取字节并将其解码为字符，它使用的字符集可以由名称指定或显示给定。
             根据InputStream的实例创建InputStreamReader的方法有4种：
             InputStreamReader（InputStream in）//根据默认字符集创建
             InputStreamReader（InputStream in,Charset cs）//使用给定字符集创建
             InputStreamReader（InputStream in,CharsetDecoder dec）//使用给定字符集解码器创建
             InputStreamReader（InputStream in,String charsetName）//使用指定字符集创建
             2：字节输出流转换为字符输出流
             OutputStreamWriter是字符流通向字节流的桥梁，它使用指定的charset将要写入流中的字符编码成字节，它使用的字符集可以由名称指定或显示给定，否则将接受默认的字符集：
             根据根据InputStream的实例创建OutputStreamWriter的方法有4种：
             OutputStreamWriter（outputstream out）//根据默认的字符集创建
             OutputStreamWriter（outputstream out,charset cs)//使用给定的字符集创建
             OutputStreamWriter（outputstream out,charsetDecoder dec)//使用组定字符集创建
             OutputStreamWriter（outputstream out,String charsetName)//使用指定字符集创建
             */
            iSR = new InputStreamReader(fis, "UTF-8");
            //InputStreamReader 转换成带缓存的bufferedReader
            bufferedReader = new BufferedReader(iSR);
            String orinString;
            while ((orinString = bufferedReader.readLine()) != null) {
                result.add(orinString);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (iSR != null) {
                    iSR.close();
                }
                if (fis != null) {
                    fis.close();
                }
                return result;
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 向文件写入字符串
     *
     * @param filePath
     * @param append      是否在原来的文件后面追加文本
     * @param writeString
     * @return
     */
    private boolean writeFile(String filePath, boolean append, String writeString, String charsetName) {
        //state output stream quote
        FileOutputStream fos = null;
        OutputStreamWriter oSR = null;
        BufferedWriter bufferedWriter = null;
        try {
            //追加文本
            if (append) {
                fos = new FileOutputStream(filePath, true);
            } else {
                fos = new FileOutputStream(filePath);
            }

            oSR = new OutputStreamWriter(fos, charsetName);
            bufferedWriter = new BufferedWriter(oSR);
            bufferedWriter.write(writeString);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                //关闭文件
                if (oSR != null) {
                    oSR.close();
                }
                if (fos != null) {
                    fos.close();
                }
                return true;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                e.printStackTrace();
                return false;
            }
        }
    }

    private final static String INFO_MESSAGE = "info message:";
    private final static String ERROR_MESSAGE = "error message:";
    private final static String DEBUG_MESSAGE = "debug message:";

    /**
     * 打印日志方法，替换{}为后面的object
     *
     * @param messgeOrin
     * @param objects
     */
    private void myPrintMessage(String level, String messgeOrin, Object... objects) {
        String message = "";
        messgeOrin += "#####";
        String[] split = messgeOrin.split("\\{\\}");
        //如果没传入替换的object，直接打印原字符串
        if (objects == null || objects.length == 0 || split.length == 1) {
            System.out.println(level + messgeOrin);
            return;
        }
        for (int i = 0; i < objects.length; i++) {
            message += (split[i] + objects[i].toString());
        }
        message += split[split.length - 1];
        System.out.println(level + message);
    }

    private void infoMessage(String messgeOrin, Object... objects) {
        myPrintMessage(INFO_MESSAGE, messgeOrin, objects);
    }

    private void errorMessage(String messgeOrin, Object... objects) {
        myPrintMessage(ERROR_MESSAGE, messgeOrin, objects);
    }

    private void debugMessage(String messgeOrin, Object... objects) {
        myPrintMessage(DEBUG_MESSAGE, messgeOrin, objects);
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            LOGGER.error("复制单个文件操作出错,{}", e);
            errorMessage("复制单个文件操作出错,{}", e);
            e.printStackTrace();
        }
    }

    //CSV文件分隔符
    private static final String NEW_LINE_SEPARATOR = "\n";

    /**
     * headers 为CSV文件的文件头， fields 为对象的属性名，
     * 除创建时间外，headers与fields的字段需一一对应
     * headers 始终比fields多一个元素，最后的创建时间
     *
     * @param dataList
     * @param headers
     * @return 返回导出文件路径
     */
    private String writePropToCSV(List<String> dataList, String[] headers, String filePath) {
        if (dataList == null || dataList.size() == 0 || headers.length == 0) {
            return "";
        }
        File lgFile = new File(filePath);

        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR).withFirstRecordAsHeader();
        // 这是写入CSV的代码
        OutputStreamWriter out = null;
        CSVPrinter printer = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(lgFile), "GB2312");
            printer = new CSVPrinter(out, format);
            //写入列头数据
            printer.printRecord(headers);
            for (String bean : dataList) {
                List<String> logInfoRecord = new ArrayList<String>();
                String[] temp = bean.split("=");
                logInfoRecord.add(temp[0]);
                logInfoRecord.add(temp[1]);
                printer.printRecord(logInfoRecord);
            }
        } catch (Exception e) {
            LOGGER.error("导出出错", e);
            errorMessage("导出出错", e);
            return "";
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != printer) {
                    printer.close();
                }
            } catch (IOException e) {
                LOGGER.error("导出出错", e);
                errorMessage("导出出错", e);
            }
            return lgFile.getAbsolutePath();
        }
    }

    public int getTotalChinese() {
        return totalChinese;
    }

    public void setTotalChinese(int totalChinese) {
        this.totalChinese = totalChinese;
    }

    /**
     * main 函数
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            SearchChineseString searchChineseString = new SearchChineseString();
            //生成查找中文的prop文件
            File file = new File(SOURCE_PATH);
            searchChineseString.replaceChinese(file, SOURCE_PATH);
            searchChineseString.infoMessage("总共的中文条数:{}", searchChineseString.getTotalChinese());

            //把prop文件导出到csv
            String propFilePath = SOURCE_PATH + File.separator + OUTPUT_PROPERTY_FILE_NAME;
            searchChineseString.writePropToCSV(
                    searchChineseString.readFile(propFilePath.replaceAll("\\\\", "/")),
                    new String[]{"键名", "值"},
                    (propFilePath + ".csv").replaceAll("\\\\", "/")
            );

            //测试引号
            /*Pattern pattern = Pattern.compile("\"([^\"])*\"");
            Matcher matcher = pattern.matcher("\"adsfsdf阿三的发生地方\"+\"dsfdf\"");
            while (matcher.find()) {
                System.out.println(matcher.group());
            }*/

            //测试match
            String regex = null;
            regex = "\"([^\"])*\"";
            System.out.println(matchLookingAt(regex, "\"adsfsdf阿三的发生地方\"+\"dsfdf\"", null));
            regex = "[\\u4e00-\\u9fa5]+";
            System.out.println(matchLookingAt(regex, "\"adsfsdf阿三的发生地方\"+\"dsfdf\"", null));
            System.out.println("哈哈=====================================================");
            regex = "[\\u4e00-\\u9fa5]+";
            System.out.println(matchLookingAt(regex, "哈哈", null));
            System.out.println(matchMatches(regex, "哈哈", null));
            System.out.println("哈哈a=====================================================");
            System.out.println(matchLookingAt(regex, "哈哈a", null));
            System.out.println(matchMatches(regex, "哈哈a", null));
            System.out.println("a哈哈a=====================================================");
            System.out.println(matchLookingAt(regex, "a哈哈a", null));
            System.out.println(matchMatches(regex, "a哈哈a", null));
            System.out.println("a哈哈a=====================================================");
            regex = ".*[\\u4e00-\\u9fa5]+.*";
            System.out.println(matchLookingAt(regex, "a哈哈a", null));
            System.out.println(matchLookingAt(regex, "a哈bfsdfdsf@#$@#$#@哈a", null));
            System.out.println(matchLookingAt(regex, "aaab123312!@@!@#$$@!$a", null));
            regex = "[\\u4e00-\\u9fa5]+";
            System.out.println(matchMatches(regex, "a哈哈a", null));
            System.out.println(matchMatches(regex, "\"adsfsdf阿三的发生地方\"+\"dsfdf\"", null));
            System.out.println("log=====================================================");
//            regex = ".*log.info\\({1}.*";
            regex = ".*(log|logger){1}(\\.info\\(){1}.*";
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdlog.info-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdlog.info-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsd-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsd-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdlog.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdlog.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "log.info(-hkjhkjh(ewlog.info(qirouewoirulog.info(we", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "log.info(-hkjhkjh(ewlog.info(qirouewoirulog.info(we", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLOG.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLOG.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLOGGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLOGGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.-info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.-info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println("log Error=====================================================");
//            regex = ".*log.info\\({1}.*";
            regex = ".*(log|logger){1}([\\.]{1}(info|error|debug){1}[\\(]{1}){1}.*";
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdlog.info-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdlog.info-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsd-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsd-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdlog.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdlog.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "log.info(-hkjhkjh(ewlog.info(qirouewoirulog.info(we", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "log.info(-hkjhkjh(ewlog.info(qirouewoirulog.info(we", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLOG.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLOG.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLOGGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLOGGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.info(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.-error(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.-error(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.error(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.error(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchLookingAt(regex, "asdfsdfgdsgsdLogGER.debug(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println(matchMatches(regex, "asdfsdfgdsgsdLogGER.debug(-hkjhkjh(ewqirouewoiruwe", Pattern.CASE_INSENSITIVE));
            System.out.println("//=====================================================");
            regex = "\\/\\/";
            System.out.println("哈哈//asdfs".split(regex).length);
            regex = "//";
            System.out.println("哈哈//asdfs".split(regex).length);
            System.out.println("\\*=====================================================");
            regex = "\\/\\*";
            System.out.println("adsfsdf/*哈哈//asdfs".split(regex).length);
            regex = "/*";
            System.out.println("adsfsdf/*哈哈//asdfs".split(regex).length);
            regex = "/\\*";
            System.out.println("adsfsdf/*哈哈//asdfs".split(regex).length);
            System.out.println("\\*/=====================================================");
            regex = "\\*\\/";
            System.out.println("dsfsdfdsfsd*/哈哈//asdfs".split(regex).length);
            regex = "\\*/";
            System.out.println("dsfsdfdsfsd*/哈哈//asdfs".split(regex).length);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}