package Main;

import org.telegram.telegrambots.exceptions.TelegramApiException;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class AnonymousChatBot extends TelegramLongPollingBot {
	public Messager messager;
	
	public AnonymousChatBot() {
		messager = new Messager(this);
	}
	
	@Override
	public String getBotUsername() {
		return "anon_chatbot";
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		try {
			messager.parsingUpdate(update);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getBotToken() {
		return System.getenv("bot_token");
	}
}
