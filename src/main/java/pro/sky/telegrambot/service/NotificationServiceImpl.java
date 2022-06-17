package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationServiceImpl implements NotificationService{

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String FORMAT_DATA_MESSAGE = "([0-9.:\\s]{16})(\\s)([\\W+]+)";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<NotificationTask> parse(String notificationMessage) {
        Pattern pattern = Pattern.compile(FORMAT_DATA_MESSAGE);
        Matcher matcher = pattern.matcher(notificationMessage);

        NotificationTask result = null;

        try {
            if (matcher.find()){
                LocalDateTime notificationDateTime = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMAT);
                String notification = matcher.group(3);
                result = new NotificationTask(notification, notificationDateTime);
            }
        } catch (DateTimeParseException e){
            logger.error("Filed to parse DataTime: " + notificationMessage + " with format dataTime: " + DATE_TIME_FORMAT + e);
        } catch (RuntimeException e) {
            logger.error("Filed to parse DataTime message: " + notificationMessage + e);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public NotificationTask schedule(NotificationTask task, Long chatId) {
        task.setChatId(chatId);

        NotificationTask sortedTask = repository.save(task);
        logger.info("NotificationTask has been sorted successful: " + sortedTask);
        return sortedTask;
    }

    @Override
    public Collection<NotificationTask> notifyAllScheduledTask() {
        logger.info("Trigger sending of scheduled notification");
        Collection<NotificationTask> notifications = repository.getScheduledNotification();
        logger.info("Amount {} task in process..", notifications.size());
        for (NotificationTask task : notifications){
            task.setSentStatus(true);
        }
        repository.saveAll(notifications);
        return notifications;
    }
}
