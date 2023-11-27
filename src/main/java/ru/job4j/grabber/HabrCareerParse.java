package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGE_SCAN_LIMIT = 5;
    private final DateTimeParser dateTimeParser;
    private int id = 0;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String description = null;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            description = document.select(".vacancy-description__text").text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }

    private Post getPost(String title, String link, String description, LocalDateTime created) {
        id++;
        return new Post(id, title, link, description, created);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> listPosts = new ArrayList<>();
        try {
            for (int pageNumber = 1; pageNumber <= PAGE_SCAN_LIMIT; pageNumber++) {
                String fullLink = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
                Connection connection = Jsoup.connect(fullLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    Element dateElement = row.select(".basic-date").first();
                    String vacancyName = titleElement.text();
                    String linkPost = String.format("%s%s", link, linkElement.attr("href"));
                    LocalDateTime date = dateTimeParser.parse(dateElement.attr("datetime"));
                    String description = this.retrieveDescription(linkPost);
                    listPosts.add(getPost(vacancyName, linkPost, description, date));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listPosts;
    }

    public static void main(String[] args) throws IOException {
        Parse habr = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> postList = habr.list(SOURCE_LINK);
        for (Post post : postList) {
            System.out.printf("%s%n", post);
        }
    }
}

