package javaFileIO;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public enum Message {
    USER_USERNAME_EXISTENT,
    WHATEVER_YOU_WANT("whatever.You.WANT"),
    NOT_DEFINED;

    private final static ThreadLocal<ResourceBundle> lang = new ThreadLocal<ResourceBundle>();
    private final static String BASENAME = "lang";
    public final String key;

    Message() {
        key = name().toLowerCase().replace('_', '.');
    }

    Message(String key) {
        this.key = key;
    }

    public String value() {
        return text(key);
    }

    public String value(Object[] o) {
        return text(key, o);
    }

    @Override
    public String toString() {
        return value();
    }

    private static String text(String key) {
        return text(key, "");
    }

    private static String text(String key, Object[] o) {
        return new MessageFormat(text(key)).format(o);
    }

    private static String text(String key, String defaultValue) {
        return key == null || lang.get() == null || !lang.get().containsKey(key) ? defaultValue : lang.get().getString(key);
    }

    /*private static String text(String key, String defaultValue, Object[] o) {
        return new MessageFormat(text(key, defaultValue)).format(o);
    }*/

    public static void setLocale(String s) {
        try {
            String[] arr = s.split("_");
            Locale locale = null;
            switch (arr.length) {
                case 1:
                    locale = new Locale(arr[0]);
                    break;
                case 2:
                    locale = new Locale(arr[0], arr[1]);
                    break;
                case 3:
                    locale = new Locale(arr[0], arr[1], arr[2]);
                    break;
            }
            setLocale(locale);
        } catch (Exception e) {
            e.printStackTrace();
            setLocale(Locale.CHINA);
        }
    }

    public static void setLocale(Locale locale) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BASENAME, locale);
        lang.set(resourceBundle);
    }

    public static void main(String[] args) {
        testLocale(Locale.CHINA);
        testLocale(Locale.CANADA);
        testLocale(Locale.US);
    }

    private static void testLocale(Locale locale) {
        setLocale(locale);
        System.err.println("国际化资源完整性检查:" + BASENAME + "_" + locale + ".properties");
        for (Message message : Message.values()) {
            if (message.value().equals("")) {
                System.err.println(message.key);
            } /*else {
                System.out.println(message.key + "=" + message.value());
            }*/
        }
    }
}