package objectToMap;


import objectToMap.service.LocaleMessageSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Created by chay on 2018/3/7.
 *         获取枚举类list
 */
public class EnumUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumUtil.class);

    /**
     * 将一个 Map 对象转化为一个 JavaBean
     *
     * @param clazz 要转化的类型
     * @param map   包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InstantiationException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings("rawtypes")
    public static <T> T toBean(Class<T> clazz, Map map) {
        T obj = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            obj = clazz.newInstance(); // 创建 JavaBean 对象

            // 给 JavaBean 对象的属性赋值
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                Class c = descriptor.getPropertyType();
                String type = c.getName();
                String propertyName = descriptor.getName();
                LOGGER.info("映射的字段名为:{};类型toGenericString为:{},simpleName:{},name:{}",propertyName,c.toGenericString(),c.getSimpleName(),c.getName());
                if (map.containsKey(propertyName)) {
                    // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                    Object value = map.get(propertyName);
                    /*if ("".equals(value)) {
                        value = null;
                    }
                    Object[] args = new Object[1];
                    args[0] = value;*/
                    try {
                        descriptor.getWriteMethod().invoke(obj, getArgsByType(value, type));
                    } catch (Exception e) {
                        System.out.println("字段映射失败");
                    }
                }
            }
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("实例化 JavaBean 失败");
        } catch (IntrospectionException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("分析类属性失败");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("映射错误");
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("实例化 JavaBean 失败");
        }
        return (T) obj;
    }

    private final static String TYPE_STRING = "java.lang.String";
    private final static String TYPE_LONG = "java.lang.Long";
    private final static String TYPE_INTEGER = "java.lang.Integer";
    private final static String TYPE_DOUBLE = "java.lang.Double";
    private final static String TYPE_BOOLEAN = "java.lang.Boolean";
    private final static String TYPE_DATE = "java.util.Date";
    /**
     * 根据类型把value转换成对应的类
     * @param value
     * @param type
     * @return
     */
    public static Object[] getArgsByType(Object value,String type) throws Exception{
        LOGGER.info("获取的自动类型为:{},要转换的值为:{}",type,value);
        if ("".equals(value)) {
            value = null;
        }
        Object[] args = new Object[1];
        switch (type) {
            case TYPE_LONG:
                value = Long.parseLong(value.toString());
                break;
            case TYPE_INTEGER:
                value = Integer.parseInt(value.toString());
                break;
            case TYPE_DOUBLE:
                value = Double.parseDouble(value.toString());
                break;
            case TYPE_BOOLEAN:
                value = Boolean.parseBoolean(value.toString());
                break;
            case TYPE_DATE:
                value = new Date(value.toString());
                break;
            case TYPE_STRING:
                value = value.toString();
                break;
            default:
        }
        args[0] = value;
        return args;
    }

    /**
     * 将一个 JavaBean 对象转化为一个 Map
     *
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的 Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings("rawtypes")
    public static Map toMap(Object bean) {
        Class<? extends Object> clazz = bean.getClass();
        Map<Object, Object> returnMap = new HashMap<Object, Object>();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            getPropertiesMapNoExcept(propertyDescriptors, returnMap, bean);
        } catch (IntrospectionException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("分析类属性失败");
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("实例化 JavaBean 失败");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("映射错误");
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("调用属性的 setter 方法失败");
        }
        return returnMap;
    }

    /**
     * 将一个 JavaBean 对象转化为一个 Map
     *
     * @param bean 要转化的JavaBean 枚举类对象
     * @return 转化出来的 Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings("rawtypes")
    public static Map toEnumMap(Object bean) {
        Class<? extends Object> clazz = bean.getClass();
        Map<Object, Object> returnMap = new HashMap<Object, Object>();
        BeanInfo beanInfo = null;
        try {
            String name = bean.toString();
            //插入枚举类的名字
            returnMap.put(NAME,name);
            System.out.println(name);
            beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            getPropertiesMapWithExcept(propertyDescriptors, returnMap, bean, exceptPropNames );
        } catch (IntrospectionException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("分析类属性失败");
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("实例化 JavaBean 失败");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("映射错误");
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("调用属性的 setter 方法失败");
        }
        return returnMap;
    }

    /**
     * 获取bean的属性map，不过滤
     * @param propertyDescriptors
     * @param returnMap
     * @param bean
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map getPropertiesMapNoExcept(PropertyDescriptor[] propertyDescriptors, Map returnMap, Object bean) throws IllegalAccessException,InvocationTargetException{
        return getPropertiesMap(propertyDescriptors, returnMap, bean, false, null);
    }

    /**
     * 获取bean的属性map，带过滤exceptPropNames
     * @param propertyDescriptors
     * @param returnMap
     * @param bean
     * @param exceptPropNames
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map getPropertiesMapWithExcept(PropertyDescriptor[] propertyDescriptors, Map returnMap, Object bean, List<String> exceptPropNames) throws IllegalAccessException,InvocationTargetException{
        return getPropertiesMap(propertyDescriptors, returnMap, bean, true, exceptPropNames);
    }

    /**
     * 获取bean的属性map，可选择是否需要排除掉exceptPropNames里面排除的字段
     * @param propertyDescriptors
     * @param returnMap
     * @param bean
     * @param isExceptProps 是否需要排除字段
     * @param exceptPropNames 排除字段的名字列表
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map getPropertiesMap(PropertyDescriptor[] propertyDescriptors, Map returnMap, Object bean, boolean isExceptProps, List<String> exceptPropNames)
            throws IllegalAccessException,InvocationTargetException{
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if(isExceptProps) {
                //如果非class属性，且还需排除字段
                if (!exceptPropNames.contains(propertyName) && !propertyName.equals("class")) {
                    getPropertyMap(descriptor, bean, returnMap);
                }
            }
            else {
                //非class属性，否则会映射错误
                if (!propertyName.equals("class")) {
                    getPropertyMap(descriptor, bean, returnMap);
                }
            }
        }
        return returnMap;
    }

    /**
     * 获取bean的单个属性map
     * @param descriptor
     * @param bean
     * @param returnMap
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map getPropertyMap(PropertyDescriptor descriptor, Object bean, Map returnMap) throws IllegalAccessException,InvocationTargetException{
        Method readMethod = descriptor.getReadMethod();
        String propertyName = descriptor.getName();
        Object result = null;
        result = readMethod.invoke(bean, new Object[0]);
        if (null != propertyName) {
            propertyName = propertyName.toString();
        }
        if (null != result) {
            result = result.toString();
        }
        if(null != propertyName && null != result) {
            returnMap.put(propertyName, result);
        }
        return returnMap;
    }

    /**
     * 将一个 JavaBean 对象转化为一个 Map,其中如果字段名是需要国际化的，则进行国际化转换
     *
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的 Map 对象
     * @throws IntrospectionException    如果分析类属性失败
     * @throws IllegalAccessException    如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings("rawtypes")
    public static Map toEnumMapInternationalization(Object bean, String prop, LocaleMessageSourceService localeMessageSourceService) {
        Class<? extends Object> clazz = bean.getClass();
        Map<Object, Object> returnMap = new HashMap<Object, Object>();
        BeanInfo beanInfo = null;
        try {
            String name = bean.toString();
            //插入枚举类的名字
            returnMap.put(NAME,name);
            System.out.println(name);
            beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
//                if (!propertyName.equals("class") ) {
                if (!exceptPropNames.contains(propertyName) ) {
                    Method readMethod = descriptor.getReadMethod();
                    Object result = null;
                    result = readMethod.invoke(bean, new Object[0]);
                    if (null == propertyName) {
                        LOGGER.info("字段为空{}",propertyName);
                        continue;
                    }
                    propertyName = propertyName.toString();
                    LOGGER.info("字段名为{}",propertyName);
                    if (null == result) {
                        LOGGER.info("字段{}值为空{}",propertyName,result);
                        continue;
                    }
                    LOGGER.info("字段{}值为:{}",propertyName,result);
                    if(propertyName.equals(prop)) {
                        LOGGER.info("找到需要国际化的字段:{}",prop);
                        result = localeMessageSourceService.getMessage(result.toString());
                    }
                    else {
                        result = result.toString();
                    }
                    returnMap.put(propertyName, result);
                }
            }
        } catch (IntrospectionException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("分析类属性失败");
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("实例化 JavaBean 失败");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("映射错误");
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            System.out.println("调用属性的 setter 方法失败");
        }
        return returnMap;
    }


    //枚举类取字段的列表:前面是字段名，后面是需要国际化该字段的类名的列表
    //如需添加，一定要增加需要国际化的字段名，和字段对应的类名。如果不需要国际化的枚举类，可以直接使用list方法。
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String MODULE_NAME = "moduleName";
    public static final String CH_FIELD_NAME = "CHFieldName";
    public static final String CAPTION = "caption";
    public static final Map<String, List<String>> enumKeyMap = new HashMap();

    static {
        enumKeyMap.put(VALUE, Arrays.asList(
                "IndustrialControlPointType",
                "MapPointType",
                "StationType",
                "RoleTypeEnum",
                "MissionTypeEnum",
                "MissionListTypeEnum",
                "DoorType",
                "EmployeeTypeEnum",
                "LogLevel",
                "RobotTypeEnum"
        ));
        enumKeyMap.put(MODULE_NAME,Arrays.asList(
                "ModuleEnums"
        ));
        enumKeyMap.put(CH_FIELD_NAME,Arrays.asList(
                "StateFieldEnums"
        ));
    }

    //在转换枚举类为Map的时候，不需要放到Map里的字段
    private static final List<String> exceptPropNames = Arrays.asList(
            "class",
            "declaringClass"
    );

    /**
     *
     *
     * @return
     */
    /**
     * 处理国际化枚举类
     * 规则：根据enumKeyMap类的列表检测字段名
     * @param propName 需要国际化的字段名
     * @param clazz 枚举类型
     * @param enumList 枚举类型对象列表
     * @param localeMessageSourceService 国际化服务类
     * @return
     */
    public static List<Map> enumListInternationalization(String propName,Class<? extends Enum> clazz,Object[] enumList, LocaleMessageSourceService localeMessageSourceService) {
        try {
            //获取类名，不含包名
            String classSimpleName = clazz.getSimpleName();
            /*//获取全名，包含包名
            System.out.println(clazz.getName());
            //只获取类名
            System.out.println(clazz.getSimpleName());*/

            List<String> enumNames = enumKeyMap.get(propName);
            List<Map> result = new ArrayList<Map>();
            if(null != enumNames && enumNames.contains(classSimpleName)) {
                LOGGER.info("{}类属于{}字段需要国际化的类",classSimpleName,propName);
                for (Object o : enumList) {
                    result.add(toEnumMapInternationalization(o, propName, localeMessageSourceService));
                }
            }
            else {
                LOGGER.info("{}类属于不需要国际化的类",classSimpleName);
                for (Object o : enumList) {
                    result.add(toEnumMap(o));
                }
            }

            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return null;
        }
    }

    public static void main(String[] args) {
        Map<Object, Object> map = new TreeMap<Object, Object>();
        map.put("fsf", 1);
        map.put("sss", "1");
        System.out.println(map);
        Station p = new Station();
        p.setId(111L);
        p.setName("dddddd");
        p.setDate(new Date());
        @SuppressWarnings("unchecked")
        Map<Object, Object> m = toMap(p);
        Station p2 = toBean(Station.class, m);
        System.out.println(p);
        System.out.println(p2);
        System.out.println(m);

        System.out.println(toMap(StationType.CENTER));

        System.out.println(StationType.class.getSimpleName());
        System.out.println(StationType.class.getName());
        LocaleMessageSourceService localeMessageSourceService = new LocaleMessageSourceService() {
            @Override
            public String getMessage(String code) {
                return "国际化啦";
            }

            @Override
            public String[] getMessage(String[] codes) {
                return new String[0];
            }

            @Override
            public String getMessage(String code, String defaultMessage) {
                return null;
            }

            @Override
            public String getMessage(String code, String defaultMessage, Locale locale) {
                return null;
            }

            @Override
            public String getMessage(String code, Locale locale) {
                return null;
            }

            @Override
            public String getMessage(String code, Object[] args) {
                return null;
            }

            @Override
            public String getMessage(String code, Object[] args, Locale locale) {
                return null;
            }

            @Override
            public String getMessage(String code, Object[] args, String defaultMessage) {
                return null;
            }

            @Override
            public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
                return null;
            }
        };
        System.out.println(enumListInternationalization(VALUE, StationType.class, StationType.values(), localeMessageSourceService));
        System.out.println(enumListInternationalization(null, RfidBraceletTypeEnum.class, RfidBraceletTypeEnum.values(), localeMessageSourceService));
    }
}
