package ru.job4j.grabber;

import java.time.LocalDateTime;
import java.util.Objects;

public class Post {
    private final int id;
    private final String title;
    private final String description;
    private final LocalDateTime created;

    public Post(int id, String title, String description, LocalDateTime created) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post post)) {
            return false;
        }
        return Objects.equals(title, post.title) && Objects.equals(created, post.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, created);
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", created=" + created
                +
                '}';

    }
}
