package flashcards;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static flashcards.CardProperty.*;

public class Main {

    // Define some long strings here
    static String welcome = "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):";

    public static void main(String[] args) {

        // logger, scanner, cards, parameters
        Logger logger = new Logger();
        Scanner scanner = new Scanner(System.in);
        CardCollection cards = new CardCollection(new CardProperty[] {TERM, DEFINITION});
        HashMap<Parameters, String> parameters = new HashMap<>();

        // read parameters
        if (!readParameters(args, logger, parameters)) {
            scanner.close();
            return;
        }

        // import at startup
        String importPath = parameters.get(Parameters.IMPORT);
        if (!"".equals(importPath)) {
            CardCollection temp = importRecords(logger, importPath);
            if (temp != null) {
                cards = temp;
            }
        }

        // main loop
        while (true) {

            // receive command
            Util.output(logger, "%s\n> ", welcome);
            String command = Util.getInput(logger, scanner);

            if (command.equals("exit")) {
                exit(logger, cards, parameters);
                return;
            }

            if (command.equals("add")) {
                add(logger, scanner, cards);
                continue;
            }

            if (command.equals("remove")) {
                remove(logger, scanner, cards);
                continue;
            }

            if (command.equals("ask")) {
                ask(logger, scanner, cards);
                continue;
            }

            if (command.equals("reset stats")) {
                resetStats(logger, cards);
                continue;
            }

            if (command.equals("hardest card")) {
                hardestCards(logger, cards);
                continue;
            }

            if (command.equals("import")) {
                Util.output(logger, "File name:\n> ");
                String filePath = Util.getInput(logger, scanner);
                CardCollection temp = importRecords(logger, filePath);
                if (temp != null) {
                    cards = temp;
                }
                continue;
            }

            if (command.equals("export")) {
                Util.output(logger, "File name:\n> ");
                String filePath = Util.getInput(logger, scanner);
                export(logger, filePath, cards);
                continue;
            }

            if (command.equals("log")) {
                Util.output(logger, "File name:\n> ");
                String filePath = Util.getInput(logger, scanner);
                log(logger, filePath);
                continue;
            }

            Util.output(logger, "Illegal command!\n\n");

        }
    }

    /**
     * read args to parameters
     * @param args args to read parameters from
     * @param logger record log
     * @param parameters Hashmap to pass parameters to
     * @return false if reading failed
     */
    private static boolean readParameters(String[] args, Logger logger, HashMap<Parameters, String> parameters) {
        for (Parameters p : Parameters.values()) {
            parameters.put(p, "");
        }
        if (args.length % 2 == 1) {
            Util.output(logger, "illegal arguments (odd numbers of arguments).");
            return false;
        }
        for (int i = 0; i < args.length; i+=2) {
            switch (args[i]) {
                case "-import":
                    parameters.put(Parameters.IMPORT, args[i + 1]);
                    break;
                case "-export":
                    parameters.put(Parameters.EXPORT, args[i + 1]);
                    break;
                case "-log":
                    parameters.put(Parameters.LOG, args[i + 1]);
                    break;
                default:
                    Util.output(logger, "illegal arguments (wrong flags).");
                    return false;
            }
        }
        return true;
    }

    private static void exit(Logger logger, CardCollection cards, HashMap<Parameters, String> parameters) {
        Util.output(logger, "Bye bye!\n\n");
        // save
        String exportPath = parameters.get(Parameters.EXPORT);
        if (!"".equals(exportPath)) {
            export(logger, exportPath, cards);
        }
        // log
        String logPath = parameters.get(Parameters.LOG);
        if (!"".equals(logPath)) {
            log(logger, logPath);
        }
    }

    private static void add(Logger logger, Scanner scanner, CardCollection cards) {
        Card card = new Card();

        Util.output(logger, "The card:\n> ");
        String term = Util.getInput(logger, scanner);
        if (cards.contains(TERM, term)) {
            Util.output(logger, "The card \"%s\" already exists.\n\n", term);
            return;
        }

        Util.output(logger, "The definition of card:\n> ");
        String definition = Util.getInput(logger, scanner);
        if (cards.contains(DEFINITION, definition)) {
            Util.output(logger, "The definition \"%s\" already exists.\n\n", definition);
            return;
        }

        card.setProperty(TERM, term);
        card.setProperty(DEFINITION, definition);
        cards.add(card);
        Util.output(logger, "The pair (\"%s\":\"%s\") has been added.\n\n", term, definition);
    }

    private static void remove(Logger logger, Scanner scanner, CardCollection cards) {
        Util.output(logger, "The card:\n> ");
        String term = Util.getInput(logger, scanner);
        if (cards.getSize() == 0 || !cards.contains(TERM, term)) {
            Util.output(logger, "Can't remove \"%s\": %s\n\n", term, "there is no such card.");
        } else {
            cards.remove(TERM, term);
            Util.output(logger, "The card has been removed.\n\n");
        }
    }

    private static void ask(Logger logger, Scanner scanner, CardCollection cards) {
        if (cards.getSize() == 0) {
            Util.output(logger, "There is no card to ask.\n\n");
            return;
        }

        Util.output(logger, "How many times to ask?\n> ");
        int askTimes;
        try {
            askTimes = Integer.parseInt(Util.getInput(logger, scanner));
            if (askTimes <= 0) {
                throw new IllegalArgumentException("Negative or zero times.");
            }
        } catch (Exception e) {
            Util.output(logger, "Illegal argument: please enter a positive integer.\n\n");
            return;
        }

        for (int i = 0; i < askTimes; i++) {

            Card randomCard = cards.randomCard();
            String term = randomCard.getProperty(TERM);
            String definition = randomCard.getProperty(DEFINITION);

            Util.output(logger, String.format("Print the definition of \"%s\":\n> ", term));
            String answer = Util.getInput(logger, scanner);

            if (answer.equalsIgnoreCase(definition)) {
                Util.output(logger, "Correct answer.\n");
            } else {
                int failureTimes = Util.toInt(randomCard.getProperty(FAILURE));
                randomCard.setProperty(FAILURE, Integer.toString(failureTimes + 1));
                try {
                    String otherCard = cards.getCard(DEFINITION, answer).getProperty(TERM);
                    Util.output(logger, "Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".\n", definition, otherCard);
                } catch (NoSuchElementException e) {
                    Util.output(logger, "Wrong answer. The correct one is \"%s\".\n", definition);
                }
            }
        }
        Util.output(logger, "\n");
    }

    private static void resetStats(Logger logger, CardCollection cards) {
        for (Card card : cards) {
            card.setProperty(FAILURE, "");
        }
        Util.output(logger, "Card statistics has been reset.\n\n");
    }

    private static void hardestCards(Logger logger, CardCollection cards) {
        int maxFailureTimes = 0;
        int failedCardCount = 0;
        for (Card card : cards) {
            int currentFailure = Util.toInt(card.getProperty(FAILURE));
            if (currentFailure > maxFailureTimes) {
                maxFailureTimes = currentFailure;
                failedCardCount = 0;
            }
            if (currentFailure == maxFailureTimes && maxFailureTimes > 0) {
                failedCardCount++;
            }
        }
        if (maxFailureTimes == 0) {
            Util.output(logger, "There are no cards with errors.\n\n");
        } else {
            if (failedCardCount == 1) {
                Util.output(logger, "The hardest card is ");
            } else {
                Util.output(logger, "The hardest cards are ");
            }
            boolean firstPrint = true;
            for (Card card : cards) {
                if (Util.toInt(card.getProperty(FAILURE)) == maxFailureTimes) {
                    if (firstPrint) {
                        Util.output(logger, "\"%s\"", card.getProperty(TERM));
                        firstPrint = false;
                    } else {
                        Util.output(logger, ", \"%s\"", card.getProperty(TERM));
                    }
                }
            }
            Util.output(logger, ". You have %d errors answering them.\n\n", maxFailureTimes);
        }
    }

    private static CardCollection importRecords(Logger logger, String path) {
        CardCollection cards = null;
        try {
            cards = CardCollection.importCards(path);
            Util.output(logger, "%d cards have been loaded.\n\n", cards.getSize());
        } catch (IOException e) {
            Util.output(logger, "Import failed: file not found.\n\n");
        } catch (ImportException e1) {
            Util.output(logger, "Import failed: corrupted import file.\n\n");
        }
        return cards;
    }

    private static void export(Logger logger, String path, CardCollection cards) {
        try {
            int saved = cards.exportCards(path);
            Util.output(logger, "%d cards have been saved.\n\n", saved);
        } catch (IOException e) {
            Util.output(logger, "illegal path.\n\n");
        }
    }

    private static void log(Logger logger, String filePath) {
        try {
            logger.save(filePath);
            Util.output(logger, "The log has been saved.\n\n");
        } catch (IOException e) {
            Util.output(logger, "illegal path.\n\n");
        }
    }

    private enum Parameters{
        IMPORT,
        EXPORT,
        LOG
    }

    /**
     * Utility functions for printing output, receiving input, logging, and for converting data
     */
    private static class Util {

        private static void output(Logger logger, String format, Object ... args) {
            System.out.printf(format, args);
            logger.log(String.format(format, args));
        }

        private static String getInput(Logger logger, Scanner scanner) {
            String input = scanner.nextLine();
            logger.log(String.format("%s\n", input));
            return input;
        }

        private static int toInt(String s) {
            if (s.equals("")) {
                return 0;
            }
            return Integer.parseInt(s);
        }

    }

}
