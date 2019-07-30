package com.example.bookshelfdemo.book;

import java.util.regex.Pattern;

class ISBNParser {

    private static Pattern NOT_DIGITS_OR_NOT_X = Pattern.compile("[^\\dX]");

    static String parse(String isbn) {
        return NOT_DIGITS_OR_NOT_X.matcher(isbn).replaceAll("");
    }

}
