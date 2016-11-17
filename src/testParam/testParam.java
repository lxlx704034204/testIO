package testParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chay on 2016/11/17.
 */

/**
 * Long等包装类都是final值，不可改变，每次赋值实际上都会新建对象
 */
public class testParam {
    public static void main(String[] args) {
        new testParam().testA();
        System.out.println("Hello World!");
    }

    private void testA(){
        Map a = new HashMap<>();
        System.out.println(a.size());
        aAdd(a);
        System.out.println(a.size());

        Integer i = 2;
        System.out.println(i);
        aAdd1(i);
        System.out.println(i);

        Feel ifeel = new Feel();
        System.out.println("--------------");
        System.out.println(ifeel.like);
        System.out.println(ifeel.aa.size());
        System.out.println(ifeel.ilike);

        doAdd(ifeel);
        System.out.println(ifeel.like);
        System.out.println(ifeel.aa.size());
        System.out.println(ifeel.ilike);

        System.out.println("--------------");
        BigDecimal bigDecimal = new BigDecimal(222);
        bAdd(bigDecimal);
        System.out.println(bigDecimal);

    }

    private void aAdd(Map a){
        a.put("a", "123");
    }

    private void aAdd1(Integer i){
        new Integer(4);
    }

    private void doAdd(Feel feel){
        feel.aa.put("a","123");
        feel.like = 133444;
        feel.ilike = 233333;
    }

    private void bAdd(BigDecimal b){
        b=b.add(new BigDecimal(1));
        System.out.println("in:"+b);
    }

    public class Feel {
        public BigDecimal alike;
        public int like;
        public Map aa = new HashMap<>();
        public Integer ilike =0;
    }

}
