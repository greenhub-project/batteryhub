package hmatalonga.greenhub.util;

import java.util.List;

/**
 * Created by hugo on 17-04-2016.
 */
public class StringHelper {
    public static String[] trimArray(String[] arr) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = arr[i].trim();

        return arr;
    }

    public static String convertToString(Object o) {
        if (o instanceof List<?>) {
            String s = String.valueOf(o);
            // remove '[' and ']' chars from List.toString()
            return s.substring(1, s.length() - 1);
        }
        return String.valueOf(o);
    }
}
