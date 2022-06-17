package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationService;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final String START_CMD = "/start";
    private static final String START_MESSAGE = "Hello my friend!";
    private static final String INVALID_CMD = "Invalid command or notification, pleas, try again!";

    private final TelegramBot telegramBot;
    private final NotificationService notificationService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void notifyScheduleTasks(){
        Collection<NotificationTask> tasksInProcessing = notificationService.notifyAllScheduledTask();
        for (NotificationTask task : tasksInProcessing){
            sendMessage(task.getChatId(), task.getNotificationMessage());
        }
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Message message = update.message();

            if (message != null) {
                if (message.text().startsWith(START_CMD)) {
                    logger.info(START_CMD + " command is received");
                    sendMessage(extractChatId(message), START_MESSAGE);
                } else {
                    Optional<NotificationTask> parseResult = notificationService.parse(message.text());
                    if (parseResult.isPresent()) {
                        scheduleNotification(extractChatId(message), parseResult.get());
                    } else {
                        logger.info("command is bad!");
                        sendMessage(extractChatId(message), INVALID_CMD);
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void scheduleNotification(Long chatId, NotificationTask task) {
        notificationService.schedule(task, chatId);
        sendMessage(chatId, "Notification has been successFul scheduled");
    }

    private void sendMessage(Long chatId, String messageText){
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        telegramBot.execute(sendMessage);
    }

    private Long extractChatId(Message message){
        return message.chat().id();
    }
}
