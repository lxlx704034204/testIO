package cn.ni.HttpIO;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by xiaoni on 2017/3/9.
 */



public class HttpPostUtil {
    URL url;
    HttpURLConnection conn;
    String boundary = "--------";
    Map<String, String> textParams = new HashMap<String, String>();
    List<MultipartFile> fileparams = new ArrayList<MultipartFile>();
    List<File> fileList = new ArrayList<File>();
    List<String> urlList = new ArrayList<String>();
    DataOutputStream ds;

    public HttpPostUtil(String url) throws Exception {
        this.url = new URL(url);
    }
    //重新设置要请求的服务器地址，即上传文件的地址。
    public void setUrl(String url) throws Exception {
        this.url = new URL(url);
    }
    //增加一个普通字符串数据到form表单数据中
    public void addTextParameter(String name, String value) {
        textParams.put(name, value);
    }
    //增加一个文件到form表单数据中
    public void addFileParameter(MultipartFile value) {
        fileparams.add(value);
    }
    //增加一个file类型的文件到form表单中
    public void addFileList(File value){
        fileList.add(value);
    }
    public void addUrlList(String str){
        urlList.add(str);
    }
    // 清空所有已添加的form表单数据
    public void clearAllParameters() {
        textParams.clear();
        fileparams.clear();
    }
    // 发送数据到服务器，返回一个字节包含服务器的返回结果的数组
    public String send() throws Exception {
        StringBuffer result = new StringBuffer();
        initConnection();
        try {
            conn.connect();
        } catch (SocketTimeoutException e) {
            // something
            throw new RuntimeException();
        }
        ds = new DataOutputStream(conn.getOutputStream());
        writeFileParams();
        writeStringParams();
        writeFileList();
        writeUrlList();
        paramsEnd();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                "utf-8"));
        String line = "";
        while ((line = in.readLine()) != null) {
            result.append(line).append("\n");
        }
        conn.disconnect();
        return result.toString();
    }
    //文件上传的connection的一些必须设置
    private void initConnection() throws Exception {
        conn = (HttpURLConnection) this.url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(10000); //连接超时为10秒
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("contentType", "utf-8");
        conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        conn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
    }
    //普通字符串数据
    private void writeStringParams() throws Exception {
        Set<String> keySet = textParams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
            String name = it.next();
            String value = textParams.get(name);
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
            ds.writeBytes("\r\n");
            ds.writeBytes(encode(value) + "\r\n");
        }
    }
    //文件数据
    private void writeFileParams() throws Exception {
        String name = "upload";
        for (MultipartFile file : fileparams) {
            MultipartFile value = file;
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + encode(value.getOriginalFilename()) + "\"\r\n");
            ds.writeBytes("Content-Type: " + getContentType(value) + "\r\n");
            ds.writeBytes("\r\n");
            ds.write(getBytes(value));
            ds.writeBytes("\r\n");
        }
    }
    //文件数据 (file)
    private void writeFileList() throws Exception {
        String name = "upload";
        for (File file : fileList) {
            File value = file;
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + encode(value.getName()) + "\"\r\n");
            ds.writeBytes("Content-Type:  image \r\n");
            ds.writeBytes("\r\n");
            ds.write(getBytes(value));
            ds.writeBytes("\r\n");
        }
    }
    //文件数据 (url)
    private void writeUrlList() throws Exception {
        String name = "upload";
        for (String strUrl : urlList) {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            DataInputStream input = new DataInputStream(conn.getInputStream());
            DataOutputStream output = new DataOutputStream(new FileOutputStream(strUrl.substring(strUrl.lastIndexOf("/") + 1).toUpperCase()));
            byte[] buffer = new byte[1024 * 8 * 1000];
            int count = 0;
            count = input.read(buffer);
//            while ((count = input.read(buffer)) > 0) {
//              output.write(buffer, 0, count);
//            }


            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + encode(strUrl) + "\"\r\n");
            ds.writeBytes("Content-Type:  image \r\n");
            ds.writeBytes("\r\n");
            ds.write(buffer);
            ds.writeBytes("\r\n");
            output.close();
            input.close();
        }
    }
    //获取文件的上传类型，图片格式为image/png,image/jpg等。非图片为application/octet-stream
    private String getContentType(MultipartFile f) throws Exception {

//      return "application/octet-stream";  // 此行不再细分是否为图片，全部作为application/octet-stream 类型
//        ImageInputStream imagein = ImageIO.createImageInputStream(f);
//        if (imagein == null) {
//            return "application/octet-stream";
//        }
//        Iterator<ImageReader> it = ImageIO.getImageReaders(imagein);
//        if (!it.hasNext()) {
//            imagein.close();
//            return "application/octet-stream";
//        }
//        imagein.close();
//        return "image/" + it.next().getFormatName().toLowerCase();//将FormatName返回的值转换成小写，默认为大写
        return "image";

    }
    //把文件转换成字节数组
    private byte[] getBytes(MultipartFile f) throws Exception {
        InputStream is = f.getInputStream();
//        FileInputStream in = (FileInputStream)f.getInputStream();  修改，因图片太小，上传报错，直接使用inputstream即可。
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = is.read(b)) != -1) {
            out.write(b, 0, n);
        }
        is.close();
//        in.close();
        return out.toByteArray();
    }
    //把文件转换成字节数组
    private byte[] getBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        return out.toByteArray();
    }
    //添加结尾数据
    private void paramsEnd() throws Exception {
        ds.writeBytes("--" + boundary + "--" + "\r\n");
        ds.writeBytes("\r\n");
    }
    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    private String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }
    public static void main(String[] args) throws Exception {
        String postData = "{\"id\":\"212\",\"title\":\"测试1\",\"content\":\"测试1\",\"author\":\"测试1\",\"createtime\":\"2016-3-4 15:25:16\"}";
        String postUrl="http://www.myee7.com/tarot_test";
        HttpPostUtil u = new HttpPostUtil(postUrl);
//        u.addFileParameter(new MultipartFile("d:/pvm.jpg"));
//        u.addFileParameter(new MultipartFile("d:/cb6c15f6-a187-3026-8911-03eec0a95cc2.png"));
//        u.addTextParameter("gson", postData);
        u.addUrlList("http://myee7.com/push_test/100/version/apkUpdate/2017/03/09/100125/apkUpdateConfig.txt");
        String result = u.send();

        System.out.println("result:"+result);
    }

}
