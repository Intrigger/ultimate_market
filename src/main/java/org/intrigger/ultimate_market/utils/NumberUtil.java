package org.intrigger.ultimate_market.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {

    public static String formatClean(double value, int maxFractionDigits) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(','); // заменяем стандартную ',' на '\''

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(true); // включаем разделение тысяч
        df.setMaximumFractionDigits(maxFractionDigits);
        df.setMinimumFractionDigits(0);
        df.setDecimalSeparatorAlwaysShown(false);

        return df.format(value);
    }

    public static boolean isValidIntegerUnderTrillion(String input) {
        if (input == null || input.isEmpty()) return false;
        if (!input.matches("\\d+")) return false; // только цифры

        try {
            long value = Long.parseLong(input);
            return value <= 999_999_999_999L;
        } catch (NumberFormatException e) {
            return false; // слишком большое число
        }
    }
}
