package pro.sky.telegrambot.service;

import pro.sky.telegrambot.model.NotificationTask;

import java.util.Collection;
import java.util.Optional;

public interface NotificationService {

    Optional<NotificationTask> parse(String notificationMessage);

    NotificationTask schedule(NotificationTask task, Long chatId);

    Collection<NotificationTask> notifyAllScheduledTask();
}
