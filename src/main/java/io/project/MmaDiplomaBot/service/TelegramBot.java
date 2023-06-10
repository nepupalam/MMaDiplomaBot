package io.project.MmaDiplomaBot.service;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import io.project.MmaDiplomaBot.config.BotConfig;
import io.project.MmaDiplomaBot.model.User;
import io.project.MmaDiplomaBot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
        listofCommand.add(new BotCommand("/news", "Список последий новостей"));
        listofCommand.add(new BotCommand("/schedule", "меню для выбора расписания"));
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
                case   "/schedule":

                    getSchedule(chatId, update.getMessage().getChat().getFirstName());


                    break;

                case   "/news":
                        String massage =  "Последние новости ММА" + EmojiParser.parseToUnicode(":fire:") + "\n"   +EmojiParser.parseToUnicode("     Студентка ММА стала победителем конкурса «Поем в Московском городском»"+ ":microphone:" + "\n" + "подробнее: https://mmamos.ru/studentka-mma-stala-pobeditelem-konkursa-poem-v-moskovskom-gorodskom/") ;

                    sendMessage(chatId,massage );


                    break;
                default: sendMessage(chatId, "команда пока не поддерживается/или не существует");
            }

        }

        else if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();



               if(callbackData.equals("И-9-19_Button")) {
                   String text = "Понедельник:" + "\n" + "     История 12:40-14:10" + "\n" + "     Основы Алгоритмизации 14:20-15:50" + "\n" + "      Основы проектирования баз данных 16:00-17:30" + "\n" + "\n" +
                           "Вторник" + "\n" + "     Компьютерные сети 9:00-10:10" + "\n" + "     Элементы вышсшей математики 10:40-12:10" + "\n" + "\n" + "     Среда: Библеотечный день" + "\n" + "\n" + "Четверг:" + "\n" + "     Обеспечение безопстности веб 12:40-14:10" +
                           "\n" + "\n" + "Пятница:" + "\n" + "     Опирационные системы 09:00-10:30";
                   EditMessageText message = new EditMessageText();
                   message.setChatId(String.valueOf(chatId));
                   message.setText(text);
                   message.setMessageId((int) messageId);

                   try {
                       execute(message);
                   }
                   catch (TelegramApiException e) {
                       log.error("Error occurred: " + e.getMessage());
                   }
               }

                 else if (callbackData.equals("Б-9-20/Б-11-21_Button")) {

                   String text = "Понедельник: Библеотечный день" +  "\n" + "\n" +
                           "Вторник" + "\n" + "     Агент банка/кассир торгового зала 10:40-12:10"  + "\n" + "\n" + " Среда: " + "\n" + "     Организации БУ в банках 10:40-12:40" + "\n" + "     Банковские операции 12:40-14:10" + "\n" + "\n" + "Четверг:" + "\n" + "     Элементы высшей математики 10:40-12:10" + "\n" + "     Рынок ЦБ Потапова 12:40-14:10" +
                           "\n" + "\n" + "Пятница:" + "\n" + "     Анализ финансово-хозяйственной деятельности  09:00-10:30";;
                   EditMessageText message = new EditMessageText();
                   message.setChatId(String.valueOf(chatId));
                   message.setText(text);
                   message.setMessageId((int) messageId);


                   try {
                       execute(message);
                   }
                   catch (TelegramApiException e) {
                       log.error("Error occurred: " + e.getMessage());
                   }
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
    private void getSchedule(long chatId ,String name) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберете вашу группу:");
        log.info("the schedule menu has been sent: " + name );


        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var firstGroupButton = new InlineKeyboardButton();
        firstGroupButton.setText("И-9-19");
        firstGroupButton.setCallbackData("И-9-19_Button");

        var secondGroupButton = new InlineKeyboardButton();
        secondGroupButton.setText("Б-9-20/Б-11-21");
        secondGroupButton.setCallbackData("Б-9-20/Б-11-21_Button");

        rowInline.add(firstGroupButton);
        rowInline.add(secondGroupButton);

        rowsInline.add(rowInline);

        markupInLine.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInLine);

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
