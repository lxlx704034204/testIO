package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 * Created by Martin on 2016/4/18.
 */
public class StringUtil extends StringUtils{
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

    public static long getChecksum(String test) {
        try {
            byte buffer[] = test.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            byte readBuffer[] = new byte[buffer.length];
            cis.read(readBuffer);
            return cis.getChecksum().getValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Protect against HTTP Response Splitting
     *
     * @return
     */
    public static String cleanseUrlString(String input) {
        return removeSpecialCharacters(decodeUrl(input));
    }

    public static String decodeUrl(String encodedUrl) {
        try {
            return encodedUrl == null ? null : URLDecoder.decode(encodedUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this should not happen
            LOGGER.error(e.getMessage(), e);
            return encodedUrl;
        }
    }

    public static String removeSpecialCharacters(String input) {
        if (input != null) {
            input = input.replaceAll("[ \\r\\n]", "");
        }
        return input;
    }

    /**
     * given a string with the format "fields[someFieldName].value" (very common in error validation), returns
     * only "someFieldName
     *
     * @param expression
     * @return
     */
    public static String extractFieldNameFromExpression(String expression) {
        return expression.substring(expression.indexOf('[') + 1, expression.lastIndexOf(']'));
    }

    /**
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if ((cs == null) || ((strLen = cs.length()) == 0)){
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * String转Map
     *
     * @param mapString
     * @return
     */
    public static Map transStringToMap(String mapString) {
        JSONObject jasonObject = JSON.parseObject(mapString);
        Map map = (Map) jasonObject;
        return map;
    }

    private static final int PAD_LIMIT = 8192;
    public static final String SPACE = " ";

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    public static String repeat(final char ch, final int repeat) {
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    public static String leftPad(final String str, final int size) {
        return leftPad(str, size, ' ');
    }

    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    public static boolean isNumeric(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        } else {
            int sz = cs.length();

            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * 判断单个字符是否是字母
     * @param cs
     * @return
     */
    public static boolean isCharacter(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        } else {
            Pattern p = Pattern.compile("[a-zA-z]");
            Matcher m = p.matcher(cs);
            if (m.find()) {
                return true;
            }
            return false;
        }
    }

    public static final String EMPTY = "";
    public static final char WHITESPACE = ' ';

    /**
     * @param text
     * @return
     */
    public static boolean isNullOrEmpty(String text) {
        return (null == text || text.length() == 0);
    }

    public static boolean isNullOrEmpty(String text, boolean trimWhitespaces) {
        if (null != text && trimWhitespaces) {
            return isNullOrEmpty(text.trim());
        }
        return isNullOrEmpty(text);
    }

    public static String trimEnd(String text, String postfix) {
        if (text.endsWith(postfix)) {
            return text.substring(0, text.lastIndexOf(postfix));
        }
        return text;
    }

    public static String strip(String str, String stripChars) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && WHITESPACE == str.charAt(start)) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && WHITESPACE == str.charAt(end - 1)) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /**
     * @param text
     * @return
     */
    public static String toLowerCase(String text) {
        if (null != text) {
            return text.toLowerCase();
        }
        return text;
    }

    public static String convertToCamel(String colName) {
        if (null == colName || 0 == colName.length()) {
            return colName;
        }
        if (colName.contains("_")) {
            return toCamelStyle(colName);
        } else {
            return colName.substring(0, 1).toLowerCase() + colName.substring(1);
        }
    }

    static String convertToPascal(String tableName) {
        if (null == tableName || 0 == tableName.length()) {
            return tableName;
        }
        if (tableName.contains("_")) {
            return toPascalStyle(tableName);
        } else {
            return tableName;
        }
    }

    private static String toPascalStyle(String name) {
        StringBuffer sb = new StringBuffer();
        String[] str = name.split("_");
        for (String temp : str) {
            sb.append(Character.toUpperCase(temp.charAt(0)));
            sb.append(temp.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String toCamelStyle(String name) {
        StringBuffer sb = new StringBuffer();
        String[] str = name.split("_");
        boolean firstTime = true;
        for (String temp : str) {
            if (firstTime) {
                sb.append(temp.toLowerCase());
                firstTime = false;
            } else {
                sb.append(Character.toUpperCase(temp.charAt(0)));
                sb.append(temp.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    public static String removeCRLF(String in) {
        char[] inArray = in.toCharArray();
        StringBuffer out = new StringBuffer(inArray.length);
        for (int i = 0; i < inArray.length; i++) {
            char c = inArray[i];
            if (c == '\n' || c == '\r')
                ;
            else out.append(c);
        }
        return out.toString();
    }

    public static int getCount(String string, char countChar) {
        if (string == null || string.length() == 0) return 0;
        int counter = 0;
        char[] array = string.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == countChar) counter++;
        }
        return counter;
    } // getCount

    public static boolean is8Bit(String headerString) {
        return false;
    }

    public static String toString(Object value) {
        if (null != value) {
            return value.toString();
        }
        return EMPTY;
    }

    public static Number toNumber(String value) {
        return new BigDecimal(value);
    }

    public static boolean equals(String lhs, String rhs) {
        if (null == lhs) {
            return (null == rhs);
        }
        return lhs.equals(rhs);
    }

    public static String nullToString(Object v) {
        return v == null ? "" : v.toString();
    }

    /**
     * 把字符串里的数字都提取出来
     */
    public static String collectNumber(String str) {
        if (str == null) {
            return str;
        }
        char[] chars = str.toCharArray();
        int length = chars.length;
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 按指定长度进行字符串截断
     */
    public static String cut(String str, int len) {
        if (str == null) {
            return "";
        }
        if (str.length() > len) {
            return str.substring(0, len) + "...";
        }
        return str;
    }

    /**
     * 判断字符串是否包含中文
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 将十六进制字符串转为8位二进制字符串
     */
    public static String parseToBit(String str) {
        String regex = "[a-f0-9A-F]*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        StringBuffer stringBuffer = new StringBuffer();
        if (m.matches()) {
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (Character.isDigit(c)) {
                    String binStr = Integer.toBinaryString(Integer.parseInt(c + "")); //十进制转二进制
                    stringBuffer.append(append0(binStr));
                } else if ((c >= 65 && c <= 70) || (c >= 97 && c <= 102)) {
                    String hexStr = Integer.valueOf(c + "", 16).toString();
                    String binStr = Integer.toBinaryString(Integer.parseInt(hexStr));
                    stringBuffer.append(append0(binStr));
                }
            }
        } else {
            stringBuffer.append("输入参数错误");
        }
        return stringBuffer.toString();
    }

    private static String append0(String binStr) {
        int length = binStr.length();
        if (length < 4) {
            binStr = "0" + binStr;
            return append0(binStr);
        } else {
            return binStr;
        }
    }

    public static void main(String[] args) {
        System.out.println(parseToBit("1b"));
    }

    public static String intToBit(int value, int length) {
        String binStr = Integer.toBinaryString(value);
        int binStrLength = binStr.length();
        if (binStrLength < length) {
            //小于length位，拼接0
            int diff = length - binStrLength;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = diff; i <= diff; i--) {
                if (i > 0)
                    stringBuilder.append("0");
                else break;
            }
            binStr = stringBuilder.toString() + binStr;
        }
        return binStr;
    }

    public static boolean isJSON(String message) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据名称获取searchName()
     * 去除名称中的符号，保留数字，字母，汉字首字母大写，最终返回结果为数字+大写字母
     * @param name
     * @return
     */
    public static String getSearchName(String name) {
        int length = name.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            String chStr = String.valueOf(ch);
            if (StringUtil.isNumeric(chStr)) {
                stringBuilder.append(chStr);
            } else if (StringUtil.isCharacter(chStr)) {
                stringBuilder.append(chStr.toUpperCase());
            } else if (StringUtil.isContainChinese(chStr)) {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(name.charAt(i));
                if (null != pinyins) {
                    //中文多音字默认取第一个拼音
                    String pinyin = pinyins[0];
                    char pinyinCh = pinyin.charAt(0);
                    stringBuilder.append(String.valueOf(pinyinCh).toUpperCase());
                }
            } else {
                continue;
            }
        }
        return stringBuilder.toString();
    }
}
