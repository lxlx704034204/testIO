package javaFileIO;

/**
 * @author Created by chay on 2018/2/2.
 */
public class StringByRegexResult {
    //返回的字符串结果
    private String stringResult;
    //根据Regex拆分的字符串数组长度
    private int splitLength;

    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    public int getSplitLength() {
        return splitLength;
    }

    public void setSplitLength(int splitLength) {
        this.splitLength = splitLength;
    }
}
