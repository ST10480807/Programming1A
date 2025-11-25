/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.maven;

import javax.swing.*;
import java.awt.GridLayout;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class POE2 {

    // Multi-User Fix
    public static Map<String, String> registeredUsers = new HashMap<>();
    private static String loggedInUsername;

    // ARRAYS
    static List<Message> sentMessages = new ArrayList<>();
    static List<Message> disregardedMessages = new ArrayList<>();
    static List<Message> storedMessages = new ArrayList<>();
    static List<String> messageHashes = new ArrayList<>();
    static List<String> messageIDs = new ArrayList<>();

    // VALIDATION
    public static boolean validateUsername(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    public static boolean validatePassword(String password) {
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&(),.?\":{}|<>].*");
        boolean longEnough = password.length() >= 8;
        return hasUpper && hasDigit && hasSpecial && longEnough;
    }

    public static boolean validateCellphone(String phone) {
        return phone.matches("^\\+\\d{1,3}\\d{9}$");
    }

    // ACCOUNT LOGIN
    public static boolean registerUser(String username, String password, String phone) {
        if (registeredUsers.containsKey(username)) return false;
        if (validateUsername(username) && validatePassword(password) && validateCellphone(phone)) {
            registeredUsers.put(username, password);
            return true;
        }
        return false;
    }

    public static boolean loginUser(String username, String password) {
        if (registeredUsers.containsKey(username) && registeredUsers.get(username).equals(password)) {
            loggedInUsername = username;
            return true;
        }
        return false;
    }

    // MESSAGE CLASS
    static final class Message {
        private static final Logger LOGGER = Logger.getLogger(Message.class.getName());

        String id;
        int number;
        String recipient;
        String text;
        String hash;

        public Message(int number, String recipient, String text) {
            this.id = generateSimpleID();
            this.number = number;
            this.recipient = recipient;
            this.text = text;
            this.hash = createMessageHash();

            if (!checkRecipientCell()) {
                LOGGER.warning("Invalid recipient format for message: " + id);
            }
        }

        public Message(String id, int number, String recipient, String text, String hash) {
            this.id = id;
            this.number = number;
            this.recipient = recipient;
            this.text = text;
            this.hash = hash;
        }

        private String generateSimpleID() {
            String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            Random rnd = new Random();
            while (sb.length() < 12) {
                int index = (int) (rnd.nextFloat() * CHARS.length());
                sb.append(CHARS.charAt(index));
            }
            return sb.toString();
        }

        public boolean checkMessageID() {
            return this.id != null && this.id.length() == 12;
        }

        public boolean checkRecipientCell() {
            return validateCellphone(recipient) && recipient.length() <= 13;
        }

        public final String createMessageHash() {
            String[] words = text.split(" ");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
            return id.substring(0, 8) + ":" + number + ":" + firstWord.toUpperCase() + lastWord.toUpperCase();
        }

        @Override
        public String toString() {
            return "Message ID: " + id +
                    "\nMessage Hash: " + hash +
                    "\nRecipient: " + recipient +
                    "\nMessage: " + text;
        }
    }

    // MESSAGE STORAGE
    static final class MessageStorage {
        private static final String FILE_PATH = "stored_messages.json";

        public static void storeMessage(Message message) {
            JSONArray messagesArray = loadMessagesArray();

            JSONObject messageObject = new JSONObject();
            messageObject.put("ID", message.id);
            messageObject.put("Number", message.number);
            messageObject.put("Recipient", message.recipient);
            messageObject.put("Text", message.text);
            messageObject.put("Hash", message.hash);
            messagesArray.add(messageObject);

            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                writer.write(messagesArray.toJSONString());
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static JSONArray loadMessagesArray() {
            File file = new File(FILE_PATH);
            if (!file.exists()) return new JSONArray();

            try (FileReader reader = new FileReader(file)) {
                Object obj = JSONValue.parse(reader);
                if (obj instanceof JSONArray array) return array;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new JSONArray();
        }

        public static void loadStoredMessages() {
            JSONArray array = loadMessagesArray();
            storedMessages.clear();
            for (Object o : array) {
                JSONObject m = (JSONObject) o;
                Message msg = new Message(
                        (String) m.get("ID"),
                        ((Long) m.get("Number")).intValue(),
                        (String) m.get("Recipient"),
                        (String) m.get("Text"),
                        (String) m.get("Hash")
                );
                storedMessages.add(msg);
            }
        }
    }

    // MENU FUNCTIONS
    public static void displaySendersAndRecipients() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages sent yet!");
            return;
        }
        StringBuilder report = new StringBuilder("All Sent Messages:\n\n");
        for (Message msg : sentMessages) {
            report.append("Sender: ").append(loggedInUsername)
                    .append("\nRecipient: ").append(msg.recipient)
                    .append("\nMessage: ").append(msg.text)
                    .append("\n\n");
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    public static void displayLongestMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages available.");
            return;
        }
        Message longest = Collections.max(sentMessages, Comparator.comparingInt(m -> m.text.length()));
        JOptionPane.showMessageDialog(null, "Longest Message:\n\n" + longest.toString());
    }

    public static void searchByMessageID() {
        String id = JOptionPane.showInputDialog("Enter Message ID to search:");
        if (id == null || id.isEmpty()) return;

        for (Message msg : sentMessages) {
            if (msg.id.equals(id)) {
                JOptionPane.showMessageDialog(null, "Message Found:\n" + msg.toString());
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message ID not found.");
    }

    public static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog("Enter recipient number (+countrycode):");
        if (recipient == null || recipient.isEmpty()) return;

        StringBuilder result = new StringBuilder("Messages sent to " + recipient + ":\n\n");
        boolean found = false;

        for (Message msg : sentMessages) {
            if (msg.recipient.equals(recipient)) {
                found = true;
                result.append(msg.toString()).append("\n\n");
            }
        }

        JOptionPane.showMessageDialog(null, found ? result.toString() : "No messages found.");
    }

    public static void deleteByHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash to delete:");
        if (hash == null || hash.isEmpty()) return;

        Message toDelete = null;
        for (Message msg : sentMessages) {
            if (msg.hash.equals(hash)) {
                toDelete = msg;
                break;
            }
        }

        if (toDelete != null) {
            sentMessages.remove(toDelete);
            JOptionPane.showMessageDialog(null, "Message deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Message hash not found.");
        }
    }

    public static void displayFullReport() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages to report.");
            return;
        }
        StringBuilder report = new StringBuilder("=== SENT MESSAGES REPORT ===\n\n");
        for (Message msg : sentMessages) {
            report.append(msg.toString()).append("\n-----------------------------\n");
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    public static void displayDisregardedMessages() {
        if (disregardedMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No disregarded messages found.");
            return;
        }

        StringBuilder report = new StringBuilder("=== DISREGARDED MESSAGES ===\n\n");
        for (Message msg : disregardedMessages) {
            report.append("Recipient: ").append(msg.recipient)
                    .append("\nMessage: ").append(msg.text)
                    .append("\n-----------------------------\n");
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    // GUI QUICKCHAT MENU 
    public static void quickChatMenu() {
        if (loggedInUsername == null) {
            JOptionPane.showMessageDialog(null, "You must be logged in to access QuickChat.");
            return;
        }

        MessageStorage.loadStoredMessages();
        boolean running = true;
        int messageCount = 0;

        while (running) {
            String menu = """
                    1. Send Message(s)
                    2. Show All Sent Messages
                    3. Show Longest Message
                    4. Search by Message ID
                    5. Search by Recipient
                    6. Delete Message (by Hash)
                    7. Display Full Report
                    8. Show Disregarded Messages
                    9. Show Stored Messages
                    10. Quit
                    """;

            String choice = JOptionPane.showInputDialog(menu);
            if (choice == null) return;

            switch (choice.trim()) {
                case "1" -> sendMessagesGUI(++messageCount);
                case "2" -> displaySendersAndRecipients();
                case "3" -> displayLongestMessage();
                case "4" -> searchByMessageID();
                case "5" -> searchByRecipient();
                case "6" -> deleteByHash();
                case "7" -> displayFullReport();
                case "8" -> displayDisregardedMessages();
                case "9" -> {
                    MessageStorage.loadStoredMessages();
                    if (storedMessages.isEmpty()) JOptionPane.showMessageDialog(null, "No stored messages.");
                    else for (Message msg : storedMessages) JOptionPane.showMessageDialog(null, msg.toString());
                }
                case "10" -> {
                    JOptionPane.showMessageDialog(null, "Goodbye!");
                    loggedInUsername = null;
                    running = false;
                }
                default -> JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    private static void sendMessagesGUI(int startCount) {
        String countInput = JOptionPane.showInputDialog("How many messages would you like to send?");
        if (countInput == null) return;
        try {
            int count = Integer.parseInt(countInput);
            for (int i = 0; i < count; i++) {
                int messageNumber = startCount + i;
                String recipient = JOptionPane.showInputDialog("Enter recipient (+countrycode):");
                if (recipient == null) continue;
                if (!validateCellphone(recipient)) {
                    JOptionPane.showMessageDialog(null, "Invalid phone number format!");
                    continue;
                }

                String text = JOptionPane.showInputDialog("Enter your message (max 250 chars):");
                if (text == null || text.trim().isEmpty() || text.length() > 250) {
                    JOptionPane.showMessageDialog(null, "Invalid message input. Disregarded.");
                    disregardedMessages.add(new Message(messageNumber, recipient, "DISREGARDED"));
                    continue;
                }

                Message msg = new Message(messageNumber, recipient, text);

                String decision = JOptionPane.showInputDialog("""
                        What would you like to do with this message?
                        1. Send Message
                        2. Disregard Message
                        3. Store Message Only
                        """);
                if (decision == null) continue;

                switch (decision.trim()) {
                    case "1" -> {
                        sentMessages.add(msg);
                        messageHashes.add(msg.hash);
                        messageIDs.add(msg.id);
                        MessageStorage.storeMessage(msg);
                        JOptionPane.showMessageDialog(null, "Message sent!\n" + msg.toString());
                    }
                    case "2" -> {
                        disregardedMessages.add(msg);
                        JOptionPane.showMessageDialog(null, "Message disregarded.");
                    }
                    case "3" -> {
                        MessageStorage.storeMessage(msg);
                        JOptionPane.showMessageDialog(null, "Message stored only.");
                    }
                    default -> JOptionPane.showMessageDialog(null, "Invalid choice. Message not processed.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number.");
        }
    }

    //CONSOLE QUICKCHAT FOR TESTING 
    public static void quickChat(Scanner scanner) {
        if (loggedInUsername == null) return;

        MessageStorage.loadStoredMessages();
        boolean running = true;
        int messageCount = 0;

        while (running && scanner.hasNextLine()) {
            String menuChoice = scanner.nextLine().trim();
            switch (menuChoice) {
                case "1" -> { // send message
                    messageCount++;
                    if (!scanner.hasNextLine()) break;
                    String recipient = scanner.nextLine();
                    if (!validateCellphone(recipient)) continue;

                    if (!scanner.hasNextLine()) break;
                    String text = scanner.nextLine();
                    Message msg = new Message(messageCount, recipient, text);

                    if (!scanner.hasNextLine()) break;
                    String decision = scanner.nextLine();

                    switch (decision) {
                        case "1" -> { sentMessages.add(msg); messageHashes.add(msg.hash); messageIDs.add(msg.id); MessageStorage.storeMessage(msg); System.out.println("Message sent!"); }
                        case "2" -> { disregardedMessages.add(msg); System.out.println("Message discarded."); }
                        case "3" -> { MessageStorage.storeMessage(msg); System.out.println("Message stored"); }
                    }
                }
                case "9", "10" -> running = false;
            }
        }
    }

    // REGISTRATION / LOGIN GUI
    public static void createUI() {
        JFrame frame = new JFrame("Account Registration & Login");
        frame.setSize(450, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtCellPhone = new JTextField();

        JButton btnRegister = new JButton("Register");
        JButton btnLogin = new JButton("Login");

        frame.add(new JLabel("Username:"));
        frame.add(txtUsername);
        frame.add(new JLabel("Password:"));
        frame.add(txtPassword);
        frame.add(new JLabel("Cellphone (+27...):"));
        frame.add(txtCellPhone);
        frame.add(btnRegister);
        frame.add(btnLogin);

        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            String cellPhone = txtCellPhone.getText();

            if (registerUser(username, password, cellPhone)) {
                JOptionPane.showMessageDialog(frame, "Registration successful!");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Registration failed! Check username, password, and phone format.\n(Or username is already taken).");
            }
        });

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            if (loginUser(user, pass)) {
                JOptionPane.showMessageDialog(frame, "Welcome " + loggedInUsername + "!");
                frame.dispose();
                quickChatMenu(); // GUI menu
            } else {
                JOptionPane.showMessageDialog(frame, "Login failed! Check your credentials.");
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(POE2::createUI);
    }
}
