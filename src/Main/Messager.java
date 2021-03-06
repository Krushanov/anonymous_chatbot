package Main;

import java.util.ArrayList;
import java.util.List;

import Language.Language;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import Command.CloseDialogClicked;
import Command.Command;
import Command.HelpButtonClicked;
import Command.MainMenuClicked;
import Command.SearchCompanionClicked;
import Command.SelectInterestClicked;
import Command.SelectLanguageClicked;
import Command.SelectCountCompanionClicked;
import UI.TelegramButton;

public class Messager {
	private AnonymousChatBot anonymousChatBot;
	private HashTable users;
    private MyUser user;
    public Language language;
    private Interest interest;

	private List<List<List<Dialog>>> freeDialogs;
	private List<List<List<Dialog>>> busyDialogs;

	private TelegramButton button;
	
	//Commands
	private Command searchCompanionCommand;
	private Command selectInterestCommand;
	private Command selectCountCompanionCommand;
	private Command helpCommand;
	private Command closeDialogCommand;
	private Command mainMenuCommand;
	private Command selectLanguageCommand;

	public Messager(AnonymousChatBot anonymousChatBot) {
		this.anonymousChatBot = anonymousChatBot;
		initObjects();
		initCommands();
	}
	
	// Init
	
	private void initObjects() {
		freeDialogs = new ArrayList<List<List<Dialog>>>();
		busyDialogs = new ArrayList<List<List<Dialog>>>();

		for (int i = 0; i < 3; i++) {
			freeDialogs.add(new ArrayList<List<Dialog>>());
			busyDialogs.add(new ArrayList<List<Dialog>>());

			for (int j = 0; j < 7; j++) {
				freeDialogs.get(i).add(new ArrayList<Dialog>());
				busyDialogs.get(i).add(new ArrayList<Dialog>());
			}
		}

		users = new HashTable();
		button = new TelegramButton(anonymousChatBot);
		language = new Language();
	}
	
	private void initCommands() {
		this.searchCompanionCommand = new SearchCompanionClicked();
		this.selectInterestCommand = new SelectInterestClicked();
		this.selectCountCompanionCommand = new SelectCountCompanionClicked();
		this.helpCommand = new HelpButtonClicked();
		this.closeDialogCommand = new CloseDialogClicked();
		this.mainMenuCommand = new MainMenuClicked();
		this.selectLanguageCommand = new SelectLanguageClicked();
	}
	
	// end Init
	
	private void sendMessage(MyUser user, String text) {

	}
	
	private void deleteUserFromDialog(MyUser user) {
		
	}
	
	private void addDialogFree(Dialog dialog) {
		freeDialogs.get(dialog.getMaxSize() - 2).get(dialog.getInterest()).add(dialog);
	}
	
	private void addBusyDialog(Dialog dialog) {
		busyDialogs.get(dialog.getMaxSize() - 2).get(dialog.getInterest()).add(dialog);
	}
	
	private void addUserToFree(MyUser user) {
		int i = user.getCountCompanion() - 2;
		int j = user.getInterest();

		Dialog dialog =
				freeDialogs.get(i).get(j).size() > 0 ?
						freeDialogs.get(i).get(j).get(0)
						: new Dialog(user.getInterest(), user.getCountCompanion(), anonymousChatBot);

			user.setDialog(dialog);
        try {
            dialog.addUser(user);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        addDialogFree(dialog);

			System.out.println("added");

			if (!dialog.hasFree()) {
                System.out.print("ddddddd");
                addBusyDialog(dialog);
            }

			System.out.println(dialog.getCount());
	}

	private void deleteUser(MyUser user) {
	    if (!user.getDialog().hasFree())
	        addDialogFree(user.getDialog());

	    user.getDialog().deleteUser(user);

	    System.out.println("deleted");
    }

    private void initUser(int userID, long chatID) {
		if ( (user = users.searchUser(userID)) == null ) {
			user = new MyUser(userID, chatID);
			users.addUser(user);
		}
	}

	
	public void parsingUpdate(Update update) throws TelegramApiException {
		Message message = update.getMessage();
		String messageText;

		if (update.hasMessage() && update.getMessage().hasText()) {
			int userID = update.getMessage().getFrom().getId();
			long chatID = update.getMessage().getChatId();
			messageText = update.getMessage().getText();

		    initUser(userID, chatID);

            switch (update.getMessage().getText()) {

			case "/start":
				SendMessage sendMessage1 = new SendMessage(message.getChatId(), language.getString("/start"));
				testMessage(sendMessage1);
				showMainMenu(message.getChatId());
				break;

			case "/commands":
				showMainMenu(message.getChatId());
				break;

			case "/stopchat":

			    if (user.hasDialog()) {
                    deleteUser(user);
                }

				SendMessage sendMessage2 = new SendMessage(message.getChatId(), language.getString("/stopchat"));
				testMessage(sendMessage2);
				showMainMenu(message.getChatId());
				break;

			case "/about":
				SendMessage sendMessage3 = new SendMessage(message.getChatId(), language.getString("/about"));
				testMessage(sendMessage3);
				showMainMenu(message.getChatId());
				break;

			case "/rate":
				System.out.println("Функция оценки бота");
				break;

			default:
				if (user.hasDialog()) {
					user.getDialog().sendMessage(user, messageText);
					System.out.println("send");
				}

				break;
			}

		} else if (update.hasCallbackQuery()) {
			String callData = update.getCallbackQuery().getData();

			int userID = update.getCallbackQuery().getFrom().getId();
			long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

			initUser(userID, chatId);

            switch (callData) {
            
            case "searchCompanion":
				EditMessageText newMessage1 = new EditMessageText();
				newMessage1.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("searchCompanion"));
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(searchCompanionCommand, newMessage1, language));
				addUserToFree(user);
				
                break;
            
			case "help":
				EditMessageText newMessage2 = new EditMessageText();
				newMessage2.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("help"));
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(helpCommand, newMessage2, language));

                break;
				
			case "back":
				EditMessageText newMessage3 = new EditMessageText();
				newMessage3.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("selectCommand"));
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage3, language));

                break;
                
			case "cancelSearchCompanion":
				EditMessageText newMessage4 = new EditMessageText();
				newMessage4.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("selectCommand"));
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage4, language));
                
                break;

			case "selectInterest":
				EditMessageText newMessage5 = new EditMessageText();
				newMessage5.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("selectInterest"));
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(selectInterestCommand, newMessage5, language));

				break;

			// Обработка выбора категорий
			case "/setInterest 0":
			case "/setInterest 1":
			case "/setInterest 2":
			case "/setInterest 3":
            case "/setInterest 4":
            case "/setInterest 5":
            case "/setInterest 6":
                int indexInterest = Integer.parseInt( callData.substring(13) );
				interest = new Interest(language);
				String answerInterest = interest.getString(indexInterest);
				EditMessageText newMessage6 = new EditMessageText();
				newMessage6.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(answerInterest);
                user.setInterest(indexInterest);
                button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage6, language));

                break;

			case "selectCountCompanion":
				EditMessageText newMessage9 = new EditMessageText();
				newMessage9.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("selectCountCompanion"));
				button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(selectCountCompanionCommand, newMessage9, language));

				break;

			case "/setCountCompanion 2":
			case "/setCountCompanion 3":
			case "/setCountCompanion 4":
			case "/setCountCompanion 5":
				int indexCountCompanion = Integer.parseInt( callData.substring(19) );
				EditMessageText newMessage10 = new EditMessageText();
				newMessage10.setChatId(chatId).setMessageId((int)(long)(messageId))
						.setText(language.getString("countCompanion") + Integer.toString(indexCountCompanion));
				user.setCountCompanion(indexCountCompanion);
				button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage10, language));

				System.out.println(user.getCountCompanion());

				break;

			case "selectLanguage":
				EditMessageText newMessage7 = new EditMessageText();
				newMessage7.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(language.getString("selectLanguage"));
				button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(selectLanguageCommand, newMessage7, language));

				break;

			case "/setLanguage 0":
			case "/setLanguage 1":
				int indexLanguage = Integer.parseInt( callData.substring(13) );
				String answerLanguage = language.getLanguageName(indexLanguage);
				EditMessageText newMessage8 = new EditMessageText();
				newMessage8.setChatId(chatId).setMessageId((int)(long)(messageId)).setText(answerLanguage);
				user.setLanguage(indexLanguage);
				language.setLanguage(indexLanguage);
				button = new TelegramButton(anonymousChatBot);
				anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage8, language));
				break;

			default:
				break;
			}
		}
	}
	
	private void showMainMenu(Long chatId) {
		SendMessage sendMessage = new SendMessage(chatId, language.getString("selectCommand"));
		Message message1 = testMessage(sendMessage);

		EditMessageText newMessage = new EditMessageText();
		newMessage.setChatId(chatId).setMessageId(message1.getMessageId()).setText(language.getString("selectCommand"));

		try {
			anonymousChatBot.editMessageText(button.onClick(mainMenuCommand, newMessage, language));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void showInterests(Long chatId) {

	}

	private Message testMessage(SendMessage sendMessage) {
		Message message = new Message();

		try {
			message = anonymousChatBot.sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;
	}

	public MyUser getUser() {
	    return user;
    }

}
