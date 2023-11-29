package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private final Connection cnn;
    private static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS post "
                                                        + "(id serial primary key, "
                                                        + "title text, "
                                                        + "link text UNIQUE,"
                                                        + "description text, "
                                                        + "created timestamp);";
    private static final String INSERT_STATEMENT = "INSERT INTO post (title, link, description, created) "
                                                    + "VALUES (?, ?, ?, ?)"
                                                    + "ON CONFLICT (link)"
                                                    + "DO NOTHING";
    private static final String SELECT_STATEMENT = "SELECT * FROM post";
    private static final String FIND_BY_ID_STATEMENT = "SELECT * FROM post WHERE id = ?";

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            this.cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password"));
            Statement createStatement = this.cnn.createStatement();
            createStatement.execute(CREATE_STATEMENT);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = this.cnn.prepareStatement(INSERT_STATEMENT)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement(SELECT_STATEMENT)) {
            postList = getPostsList(ps.executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement(FIND_BY_ID_STATEMENT)) {
            ps.setInt(1, id);
            postList = getPostsList(ps.executeQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postList.isEmpty() ? null : postList.get(0);
    }

    @Override
    public void close() {

    }

    private static List<Post> getPostsList(ResultSet resultSet) throws SQLException {
        List<Post> postList = new ArrayList<>();
        while (resultSet.next()) {
            postList.add(new Post(
                    resultSet.getInt(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getTimestamp(5).toLocalDateTime()));
        } return postList;
    }
}
