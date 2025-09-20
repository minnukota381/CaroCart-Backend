package com.carocart.authentication.util;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class Slugifier {
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-zA-Z0-9]+");

    public String toSlug(String input) {
        String base = NON_ALNUM
                .matcher(Optional.ofNullable(input).orElse("").trim())
                .replaceAll("-")
                .toLowerCase()
                .replaceAll("^-+|-+$", "");
        return base.isEmpty() ? "vendor" : base;
    }
}
