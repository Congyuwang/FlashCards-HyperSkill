package flashcards;

import java.util.HashMap;
import java.util.Map;

public class Card {
    private final Map<CardProperty, String> cardInfo = new HashMap<>();

    Card() {
        for (CardProperty p : CardProperty.values()) {
            cardInfo.put(p, "");
        }
    }

    String getProperty(CardProperty p) {
        return cardInfo.get(p);
    }

    void setProperty(CardProperty p, String value) {
        // return character is illegal.
        String copy = value.replace('\n', ' ');
        cardInfo.put(p, copy);
    }

    Card copyOf() {
        Card clone = new Card();
        for (CardProperty p : CardProperty.values()) {
            clone.setProperty(p, cardInfo.get(p));
        }
        return clone;
    }
}
