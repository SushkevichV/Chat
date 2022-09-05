package by.sva.WebChat;

import com.github.rjeschke.txtmark.Processor;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route("") //URL
@Push // для автоматического обновления содержания страницы
public class MainView extends VerticalLayout implements AppShellConfigurator {
	
	private final Storage storage;
	private Registration registration;
	private Grid<Storage.ChatMessage> grid = new Grid<>();
	private VerticalLayout chat;
	private VerticalLayout login;
	private String author = "";
	
	public MainView(Storage storage) {
		this.storage = storage;
		
		buildLogin();
		buildChat();
		
	}

	private void buildLogin() {
		login = new VerticalLayout() {{
			TextField textField = new TextField();
			textField.setPlaceholder("Автор");
			add(
					textField,
					new Button("Login") {{
						addClickListener(click -> {
							login.setVisible(false);
							chat.setVisible(true);
							author = textField.getValue();
							storage.addAuthor(author);
						});
						addClickShortcut(Key.ENTER);
					}}
			);
		}};
		add(login); // добавление слоя на главный слой приложения, объявленный в сигнатуре класса
	}

	private void buildChat() {
		chat = new VerticalLayout();
		add(chat); // добавление слоя на главный слой приложения, объявленный в сигнатуре класса
		chat.setVisible(false);
		
		grid.setItems(storage.getMessages());
		grid.addColumn(new ComponentRenderer<>(message -> new Html(renderRow(message)))) // нужно сгенерировать html-тэги
				.setAutoWidth(true)
				.setHeader("Сообщения");
		TextField textField = new TextField();
		Button button = new Button("Отправить");
		button.addClickListener(click -> {
			storage.addMessage(author, textField.getValue());
			textField.clear();
		});
		button.addClickShortcut(Key.ENTER); // добавить клавишу
		
		chat.add(
				new H3("Vaadin Chat"),
				grid,
				new HorizontalLayout() {{
					add(
						textField,
						button
					);
				}}
		);
	}

	// генератор html-тэгов
	private String renderRow(Storage.ChatMessage message) {
		if(message.getMessage().isEmpty()) {
			return Processor.process(String.format("**%s** присоединился к чату", message.getAuthor()));
		}
		return Processor.process(String.format("**%s**: %s", message.getAuthor(), message.getMessage()));
	}
	
	public void onNewMessage(Storage.ChatEvent event) {
		if(getUI().isPresent()) {
			UI ui = getUI().get();
			ui.getSession().lock(); // для многопоточки
			//ui.getPage().executeJs("$0._scrollToIndex($1)", grid, storage.size()); //$0 - grid, $1 - storage.size() -> в элементе grid перейти на позицию size() - не работает
			ui.beforeClientResponse(grid, ctx -> grid.scrollToEnd()); // прокрутить до конца
			ui.access(() -> grid.getDataProvider().refreshAll()); // обновить содержимое компонента
			ui.getSession().unlock(); // для многопоточки
		}
	}
	
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		registration = storage.attachListener(this::onNewMessage);
	}
	
	@Override
	protected void onDetach(DetachEvent detachEvent) {
		registration.remove();
	}

}
