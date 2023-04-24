package io.project.MmaDiplomaBot.service;

import io.project.MmaDiplomaBot.config.BotConfig;
import io.project.MmaDiplomaBot.model.User;
import io.project.MmaDiplomaBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//класс который определяет всю логику приложения
@Slf4j
@Component //анотация позволяющая автоматически создавать экземпляр класса
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    final BotConfig config;

    //констуктор данного класса
    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommand = new ArrayList<>();
        listofCommand.add(new BotCommand("/start", "старт бота"));
        listofCommand.add(new BotCommand("/mydata", "информация о пользователе"));
        listofCommand.add(new BotCommand("/deletedata", "информация о пользователе"));
        listofCommand.add(new BotCommand("/help", "справка о командах"));
        listofCommand.add(new BotCommand("/settings", "настройки"));
        try {
            this.execute(new SetMyCommands(listofCommand, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error settings bot's command list: " +  e.getMessage());
        }
    }
    //геттер токена бота
    @Override
    public String getBotToken() {

        return config.getToken();
    }
//главный метот приложения который проверяет написали ли сообщение боту и обработка сообщения
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String massageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (massageText) {
                case "/start":


                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default: sendMessage(chatId, "пока не поддерживается");
            }

        }



    }

    //метод регистрации
    private void registerUser(Message msg) {
        //проверка на пустого пользователя
        if(userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user.toString());

        }
    }

    //медот который будет реагировать на сообщение /start
    private void startCommandReceived(long chatId, String name) {

        String answer = "Hi," + name ;

        log.info("Replied to user: " + name );

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }


//геттер имени бота
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
