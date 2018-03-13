package objectToMap;

import java.util.Date;

/**
 * Created by chay on 2017/6/7.
 * 护士站
 */
public class Station{
    /**
     * 站名
     */
    private String name;

    /**
     * 关联场景
     */
    private Long id;

    private Date date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
