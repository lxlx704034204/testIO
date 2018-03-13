package objectToMap;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chay on 2017/6/7.
 * 站类型
 */
public enum StationType {

	CENTER(1, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_ZXZD"),
	NORMAL(2, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_YBZD"),
	CHARGE(3, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_CDZD"),
	OPERATION(4, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_SSSZD"),
	ASEPTIC_APPARATUS_ROOM(5, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_WJQXSZD"),
	ELEVATOR(6, "goor_domain_src_main_java_cn_mrobot_bean_area_station_StationType_java_DTZD");

	private String value;

	private int caption;

	public String getValue() {
		return value;
	}

	public int getCaption() {
		return caption;
	}

	public static String getValue(int caption) {
		String value = "";
		for(StationType noticeType : StationType.values()){
			if(caption==noticeType.getCaption()){
				value = noticeType.getValue();
			}
		}
		return value;
	}

	public static StationType getType(int caption){
		for (StationType c : StationType.values()) {
			if (c.getCaption() == caption) {
				return c;
			}
		}
		return null;
	}

	public static List list() {
		List<Map> resultList = new ArrayList<Map>();
		for (StationType c : StationType.values()) {
			resultList.add(toDTO(c)) ;
		}
		return resultList;
	}

	public static String getTypeJson(int caption){
		for (StationType c : StationType.values()) {
			if (c.getCaption() == caption) {
				return JSON.toJSONString(toDTO(c));
			}
		}
		return null;
	}

	private static Map toDTO(StationType c) {
		Map result = new HashMap<String,Object>();
		result.put("name",c);
		result.put("value",c.getValue());
		result.put("caption",c.getCaption());
		return result;
	}

	private StationType(int caption, String value) {
		this.caption = caption;
		this.value = value;
	}

}
