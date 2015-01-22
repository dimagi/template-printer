package richard.chard.lu.android.templateprinter;

import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Richard Lu
 */
public abstract class Utils {

    private static final String FORMAT_REGEX_WITH_DELIMITER = "((?<=%2$s)|(?=%1$s))";

    private static final Logger LOG = Logger.create(Utils.class);

    public static String getFilePathFromUri(Uri uri) {
        LOG.trace("Entry, uri={}", uri);

        String result = last(uri.getPath().split("//"));

        LOG.trace("Exit, result={}", result);
        return result;
    }

    public static String join(String[] strings) {
        return TextUtils.join("", strings);
    }

    public static String last(String[] strings) {
        return strings[strings.length - 1];
    }

    public static String remove(String input, String toRemove) {
        return TextUtils.join("", input.split(toRemove));
    }

    public static String[] splitKeepDelimiter(
            String input,
            String delimiterStart,
            String delimiterEnd) {
        LOG.trace("Entry, input={}, delimiterStart={}, delimiterEnd={}",
                input,
                delimiterStart,
                delimiterEnd
        );

        String delimiter = String.format(FORMAT_REGEX_WITH_DELIMITER, delimiterStart, delimiterEnd);

        String[] result = input.split(delimiter);
        LOG.trace("Exit, result={}", result);
        return result;
    }

}
