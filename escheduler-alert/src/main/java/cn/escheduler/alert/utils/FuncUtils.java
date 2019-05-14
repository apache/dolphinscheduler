package cn.escheduler.alert.utils;

public class FuncUtils {

    static public String mkString(Iterable<String> list, String split) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first)
                first = false;
            else
                sb.append(split);
            sb.append(item);
        }
        return sb.toString();
    }

}
