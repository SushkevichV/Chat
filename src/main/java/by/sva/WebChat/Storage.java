package by.sva.WebChat;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
public class Storage {
	@Getter // не работает
	private Queue<ChatMessage> messages = new ConcurrentLinkedQueue<>(); // многопоточная очередь
	
	private ComponentEventBus eventBus = new ComponentEventBus(new Div());

	public Queue<ChatMessage> getMessages() {
		return messages;
	}

	public void addMessage(String author, String message) {
		messages.add(new ChatMessage(author, message));
		eventBus.fireEvent(new ChatEvent()); // новое событие для лисенера
	}
	
	public Registration attachListener(ComponentEventListener<ChatEvent> messageListener) {
		return eventBus.addListener(ChatEvent.class, messageListener);
	}
	
	public int size() {
		return messages.size();
	}
	
	public void addAuthor(String author) {
		messages.add(new ChatMessage(author, ""));
		eventBus.fireEvent(new ChatEvent()); // новое событие для лисенера
	}

	
	// вложенный класс
	@Getter // не работает
	@AllArgsConstructor // не работает
	public static class ChatMessage{
		private String author;
		private String message;
		
		public ChatMessage(String author, String message) {
			this.author = author;
			this.message = message;
		}

		public String getAuthor() {
			return author;
		}
		
		public void setAuthor(String author) {
			this.author = author;
		}
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
	}
	
	// еще вложенный класс
	public static class ChatEvent extends ComponentEvent<Div>{

		public ChatEvent() {
			super(new Div(), false);
		}
		
	}

}
