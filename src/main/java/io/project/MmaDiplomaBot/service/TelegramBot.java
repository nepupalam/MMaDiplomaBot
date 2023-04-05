package io.project.MmaDiplomaBot.service;

import io.project.MmaDiplomaBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//класс который определяет всю логику приложения
@Component //анотация позволяющая автоматически создавать экземпляр класса
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    //констуктор данного класса
    public TelegramBot(BotConfig config) {
        this.config = config;
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

                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default: sendMessage(chatId, "пока не поддерживается");
            }

        }



    }

    //медот который будет реагировать на сообщение /start
    private void startCommandReceived(long chatId, String name) {

        String answer = "Hi," + name ;

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

        }
    }


//геттер имени бота
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
}
