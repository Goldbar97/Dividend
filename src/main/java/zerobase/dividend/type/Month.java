package zerobase.dividend.type;

import lombok.Getter;

import java.util.HashMap;

@Getter
public enum Month {
    JAN("Jan", 1),
    FEB("Feb", 2),
    MAR("Mar", 3),
    APR("Apr", 4),
    MAY("May", 5),
    JUN("Jun", 6),
    JUL("Jul", 7),
    AUG("Aug", 8),
    SEP("Sep", 9),
    OCT("Oct", 10),
    NOV("Nov", 11),
    DEC("Dec", 12);
    
    private static final HashMap<String, Integer> map = new HashMap<>();
    private final String month;
    private final int num;

    static {
        for (Month m : Month.values()) {
            map.put(m.getMonth(), m.getNum());
        }
    }
    
    Month(String month, int num) {
        this.month = month;
        this.num = num;
    }
    
    public static int strToNumber(String s) {
        Integer result = map.get(s);
        return (result != null) ? result : -1;
    }
}
