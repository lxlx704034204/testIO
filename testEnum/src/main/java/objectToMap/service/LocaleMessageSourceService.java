package objectToMap.service;

import java.util.Locale;

/**
 * Created by chay on 2018/2/24.
 */
public interface LocaleMessageSourceService {
    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    String getMessage(String code);

    /**
     * 批量获取国际化列表
     * @param codes
     * @return
     */
    String[] getMessage(String[] codes);

    String getMessage(String code, String defaultMessage);

    String getMessage(String code, String defaultMessage, Locale locale);

    String getMessage(String code, Locale locale);

    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    String getMessage(String code, Object[] args);

    String getMessage(String code, Object[] args, Locale locale);

    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    String getMessage(String code, Object[] args, String defaultMessage);

    /**
     * 指定语言.
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);
}
