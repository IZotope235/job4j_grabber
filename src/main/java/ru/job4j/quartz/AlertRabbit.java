package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS rabbit ("
                                                    + "id serial primary key, "
                                                    + "created_date timestamp);";
    private static final String INSERT_STATEMENT = "INSERT INTO rabbit (created_date) values (?) ;";

    private static Properties getConfig() {
        Properties config = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().
                getResourceAsStream("rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static Connection getConnection(Properties config) throws ClassNotFoundException {
        Connection cn = null;
        Class.forName(config.getProperty("driver-class-name"));
        try {
            cn = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
            createTable(cn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cn;
    }

    private static void createTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_STATEMENT);
        }
    }

    private static void insertIntoTable(Connection connection, Timestamp timestamp) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_STATEMENT,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, timestamp);
            ps.execute();
            try (ResultSet key = ps.getGeneratedKeys()) {
                if (key.next()) {
                    RabbitItem rabbitItem = new RabbitItem(key.getInt(1),
                            key.getTimestamp(2).toLocalDateTime());
                    System.out.println(rabbitItem);
                }
            }
        }
    }

    public static void main(String[] args) {
        Properties config = getConfig();
        try (Connection connection = getConnection(config)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(config.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            try {
                Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
                insertIntoTable(connection, new Timestamp(System.currentTimeMillis()));
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private record RabbitItem(int id, LocalDateTime created) { }
}
