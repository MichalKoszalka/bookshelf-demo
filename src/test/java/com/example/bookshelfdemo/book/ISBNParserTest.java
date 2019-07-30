package com.example.bookshelfdemo.book;



import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ISBNParserTest {

    @Test
    void parse_DigitsWithMinuses_CorrectlyParsed() {
        // given
        var input = "913-1-31-125412-0";

        // when
        var result = ISBNParser.parse(input);

        // then
        assertThat(result).isEqualTo("9131311254120");
    }

    @Test
    void parse_DigitsWithMinusesAndX_CorrectlyParsed() {
        // given
        var input = "913-1-31-125412-X";

        // when
        var result = ISBNParser.parse(input);

        // then
        assertThat(result).isEqualTo("913131125412X");
    }

}
