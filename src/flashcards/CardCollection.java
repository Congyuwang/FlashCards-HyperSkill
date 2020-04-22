package flashcards;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class CardCollection implements Iterable<Card> {

    /**
     * the mappings from Card keys to Cards
     */
    private final HashMap<CardProperty, HashMap<String, Card>> cards;
    private final CardProperty MAIN_KEY;
    private final LinkedHashSet<CardProperty> KEYS;
    private int size;

    /**
     * The {@code CardProperty[] keys} are used as keys for finding cards
     * @param keys an array of {@code CardProperty} that serve as keys;
     *                       the first key is used as the Main Key.
     * @throws IllegalArgumentException if no keys are provided
     */
    public CardCollection(CardProperty[] keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("Empty Key!");
        }
        MAIN_KEY = keys[0];
        KEYS = new LinkedHashSet<>(Arrays.asList(keys));
        cards = new HashMap<>();
        for (CardProperty key : KEYS) {
            cards.put(key, new HashMap<>());
        }
    }

    /**
     * Add a new card to the collection
     * @param card the new card to be added
     * @throws IllegalArgumentException when the card has duplicated key in the collection
     */
    public void add(Card card) throws IllegalArgumentException {

        // make sure that the added card has unique keys
        for (CardProperty key : KEYS) {
            String thisKey = card.getProperty(key);
            if (contains(key, thisKey)) {
                throw new IllegalArgumentException("duplicated key!");
            }
        }

        for (CardProperty key : KEYS) {
            String thisKey = card.getProperty(key);
            cards.get(key).put(thisKey, card);
        }

        size++;
    }

    /**
     * removes a card
     * @param key the key used to find the card
     * @param value the value of this key
     * @throws NoSuchElementException if the card does not exist
     * @throws IllegalArgumentException if {@code key} is not in KEY.
     */
    public void remove(CardProperty key, String value) {
        if (!KEYS.contains(key)) {
            throw new IllegalArgumentException("not a key");
        }
        Card thisCard = cards.get(key).get(value);
        if (thisCard == null) {
            throw new NoSuchElementException("there is no such card.");
        }
        for (CardProperty keyName : KEYS) {
            cards.get(keyName).remove(thisCard.getProperty(keyName));
        }
        size--;
    }

    /**
     * Check if the collection contains a certain card by looking up its property
     * @param property the property to check
     * @param value the value of the property
     * @return whether a card with that property value is in the collection
     */
    public boolean contains(CardProperty property, String value) {
        CardProperty key;
        if (!KEYS.contains(property)) {
            for (Card card : cards.get(MAIN_KEY).values()) {
                if (value.equals(card.getProperty(property))) {
                    return true;
                }
            }
            return false;
        } else {
            key = property;
        }
        return cards.get(key).containsKey(value);
    }

    /**
     * returns a copy of a card
     * @param key the key used to find the card
     * @param value the value of the card
     * @return a copy of this card
     * @throws NoSuchElementException if the card does not exist
     * @throws IllegalArgumentException if {@code key} is not in KEY.
     */
    public Card getCard(CardProperty key, String value) {
        if (!KEYS.contains(key)) {
            throw new IllegalArgumentException("not a key");
        }
        Card thisCard = cards.get(key).get(value);
        if (thisCard == null) {
            throw new NoSuchElementException("there is no such card.");
        }
        return thisCard.copyOf();
    }

    /**
     * returns a random card
     * @return Card chosen randomly
     * @throws NoSuchElementException if the collection is empty
     */
    public Card randomCard() {
        if (size == 0) {
            throw new NoSuchElementException("empty collection");
        }
        Random random = new Random();
        int randomInt = random.nextInt(size);
        int increment = 0;
        for (Card card : cards.get(MAIN_KEY).values()) {
            if (increment == randomInt) {
                return card;
            }
            increment++;
        }
        return null;
    }

    /**
     * write card collection to file
     * @param path file path
     * @throws IOException if the path is invalid
     */
    public int exportCards(String path) throws IOException {
        File file = new File(path);
        FileWriter fileWriter = new FileWriter(file);
        CardProperty[] properties = CardProperty.values();

        // header: title, size, keys, card properties
        fileWriter.write("Card Collections\n");
        fileWriter.write(String.format("%d\n", size));
        for (CardProperty property : KEYS) {
            fileWriter.append(property.toString());
            fileWriter.append(' ');
        }
        fileWriter.append('\n');
        for (CardProperty property : properties) {
            fileWriter.append(property.toString());
            fileWriter.append(' ');
        }
        fileWriter.append('\n');

        // write cards: property1\n property2\n property3 ...
        for (Card card : cards.get(MAIN_KEY).values()) {
            for (CardProperty property : properties) {
                fileWriter.append(card.getProperty(property));
                fileWriter.append('\n');
            }
        }
        fileWriter.close();
        return size;
    }

    /**
     * import card collection
     * @param path path to the file
     * @return a CardCollection
     * @throws IOException if file does not exist
     * @throws ImportException if import file is illegal
     */
    public static CardCollection importCards(String path) throws IOException, ImportException {
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        Scanner scanner = new Scanner(fileReader);

        // check and read header: title, size, keys, card properties
        int copySize;
        LinkedHashSet<CardProperty> copyKeys = new LinkedHashSet<>();
        LinkedHashSet<CardProperty> cardProperties = new LinkedHashSet<>();
        try {
            // title
            String header = scanner.nextLine();
            if(!"Card Collections".equals(header)) {
                throw new ImportException("illegal import file: wrong header");
            }
            // size
            copySize = scanner.nextInt();
            scanner.nextLine();
            // keys
            String[] stringKeys = scanner.nextLine().split(" ");
            for (String stringKey : stringKeys) {
                CardProperty key = CardProperty.valueOf(stringKey);
                if (copyKeys.contains(key)) {
                    throw new ImportException("illegal import file: duplicated keys");
                }
                copyKeys.add(key);
            }
            // card properties
            String[] stringProperties = scanner.nextLine().split(" ");
            if (stringProperties.length != CardProperty.values().length) {
                throw new ImportException("illegal import file: wrong property length");
            }
            for (String stringProperty : stringProperties) {
                CardProperty property = CardProperty.valueOf(stringProperty);
                if (cardProperties.contains(property)) {
                    throw new ImportException("illegal import file: duplicate property");
                }
                cardProperties.add(property);
            }
        } catch (Exception e) {
            fileReader.close();
            scanner.close();
            throw new ImportException("illegal import file");
        }

        // initialization
        CardProperty[] keys = new CardProperty[copyKeys.size()];
        copyKeys.toArray(keys);
        CardCollection cardCollection = new CardCollection(keys);

        // read cards
        try {
            for (int i = 0; i < copySize; i++) {
                Card newCard = new Card();
                for (CardProperty p : cardProperties) {
                    String propertyValue = scanner.nextLine();
                    newCard.setProperty(p, propertyValue);
                }
                cardCollection.add(newCard);
            }
        } catch (Exception e) {
            fileReader.close();
            scanner.close();
            throw new ImportException("illegal import file");
        }

        fileReader.close();
        scanner.close();

        return cardCollection;
    }

    /**
     * get the collection size
     * @return the number of cards
     */
    public int getSize() {
        return size;
    }

    @Override
    public Iterator<Card> iterator() {
        return new CardIterator();
    }

    private class CardIterator implements Iterator<Card> {

        LinkedList<Card> keys = new LinkedList<>(cards.get(MAIN_KEY).values());

        @Override
        public boolean hasNext() {
            return keys.size() > 0;
        }

        @Override
        public Card next() {
            return keys.pop();
        }
    }
}
