package objectToMap.service.impl;



import objectToMap.service.LocaleMessageSourceService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * Created by chay on 2018/2/24.
 */
@Transactional
@Service
public class LocaleMessageSourceServiceImpl implements LocaleMessageSourceService {

    @Resource
    private MessageSource messageSource;

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    @Override
    public String getMessage(String code){
        return this.getMessage(code,new Object[]{});
    }

    @Override
    public String[] getMessage(String[] codes) {
        if(codes == null) {
            return null;
        }
        int length = codes.length;
        String[] result = new String[length];
        for(int i = 0; i < length ; i++) {
            result[i] = this.getMessage(codes[i]);
        }
        return result;
    }

    @Override
    public String getMessage(String code,String defaultMessage){
        return this.getMessage(code, null,defaultMessage);
    }

    @Override
    public String getMessage(String code,String defaultMessage,Locale locale){
        return this.getMessage(code, null,defaultMessage,locale);
    }

    @Override
    public String getMessage(String code,Locale locale){
        return this.getMessage(code,null,"",locale);
    }

    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    @Override
    public String getMessage(String code,Object[] args){
        return this.getMessage(code, args,"");
    }

    @Override
    public String getMessage(String code,Object[] args,Locale locale){
        return this.getMessage(code, args,"",locale);
    }

    /**
     *
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    @Override
    public String getMessage(String code,Object[] args,String defaultMessage){
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return this.getMessage(code, args, defaultMessage, locale);
    }

    /**
     * 指定语言.
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return
     */
    @Override
    public String getMessage(String code,Object[]args,String defaultMessage,Locale locale){
        return messageSource.getMessage(code, args, defaultMessage,locale);
    }
}
