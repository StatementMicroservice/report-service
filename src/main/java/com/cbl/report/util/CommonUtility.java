package com.cbl.report.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtility {
    public static final String DEFAULT_TIMEZONE = "Asia/Dhaka";

    public static String extractJsonString(String rawResponse) throws Exception {
        final Pattern pattern = Pattern.compile("\\{.*\\}");
        final Matcher matcher = pattern.matcher(rawResponse);

        if (matcher.find()) {
            return matcher.group();
        } else {
            throw new Exception("Unable to extract JSON from response");
        }
    }

    public static String cleanUpJsonString(String jsonResponse) {
        return jsonResponse.replace("\\\"", "\"")
                           .replace("\\\\", "\\");
    }
}
