package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerDateTimeParserTest {
    @Test
    public void whenParse() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String date = "2023-11-10T17:15:37+03:00";
        String expected = "2023-11-10T17:15:37";
        assertThat(parser.parse(date)).isEqualTo(expected);
    }

    @Test
    public void whenParseNullThenNull() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String date = null;
        assertThat(parser.parse(date)).isNull();
    }
}