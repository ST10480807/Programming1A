/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.maven;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Random;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class POE2 {

    private static String registeredUsername;
    private static String registeredPassword;

    // Validation methods
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
        return validateUsername(username) && validatePassword(password) && validateCellphone(phone)
                && (registeredUsername = username) != null
                && (registeredPassword = password) != null;
    }

    public static boolean loginUser(String username, String password) {
        return username.equals(registeredUsername) && password.equals(registeredPassword);
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
            this.id = generateID();
            this.number = number;
            this.recipient = recipient;
            this.text = text;
            this.hash = createMessageHash();

            if (!checkMessageID()) {
                LOGGER.warning(() -> "Invalid Message ID for message: " + id);
            }
        }

        private String generateID() {
            Random rand = new Random();
            long num = 1000000000L + (long) (rand.nextDouble() * 9000000000L);
            return String.valueOf(num);
        }

        public boolean checkMessageID() {
            return id.length() <= 10;
        }

        public boolean checkRecipientCell() {
            return recipient.startsWith("+") && recipient.length() <= 13;
        }

        public final String createMessageHash() {
            String[] words = text.split(" ");
            String firstWord = words.length > 0 ? words[0] : "";
            String lastWord = words.length > 1 ? words[words.length - 1] : "";
            return id.substring(0, 2) + ":" + number + ":" + firstWord.toUpperCase() + lastWord.toUpperCase();
        }

        public static void storeMessage(Message message) {
            String filePath = "stored_messages.json";
            JSONArray messagesArray = new JSONArray();
            File file = new File(filePath);

            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Object obj = JSONValue.parse(reader);
                    if (obj instanceof JSONArray existingArray) {
                        messagesArray = existingArray;
                    }
                } catch (IOException e) {
                    LOGGER.log(java.util.logging.Level.SEVERE, "Error reading message file", e);
                }
            }

            JSONObject messageObject = new JSONObject();
            messageObject.put("ID", message.id);
            messageObject.put("Number", message.number);
            messageObject.put("Recipient", message.recipient);
            messageObject.put("Text", message.text);
            messageObject.put("Hash", message.hash);
            messagesArray.add(messageObject);

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(messagesArray.toJSONString());
                writer.flush();
                JOptionPane.showMessageDialog(null, "Message stored successfully!");
            } catch (IOException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error storing message", e);
            }
        }

        @Override
        public String toString() {
            return "Message ID: " + id + "\nMessage Hash: " + hash + "\nRecipient: " + recipient + "\nMessage: " + text;
        }
    }

    // QUICKCHAT 
    public static void quickChatMenu() {
        JOptionPane.showMessageDialog(null, " Welcome to QuickChat", "Welcome", JOptionPane.INFORMATION_MESSAGE);

        boolean running = true;
        int messageCount = 0;
        List<Message> messages = new ArrayList<>();

        while (running) {
            String menu = """
                    Please select an option:
                    1. Send Message(s)
                    2. Show recent messages 
                    3. Quit
                    """;

            String choice = JOptionPane.showInputDialog(null, menu, "QuickChat Menu", JOptionPane.QUESTION_MESSAGE);
            if (choice == null) return; // user closed dialog
            choice = choice.trim();

            switch (choice) {
                case "1" -> {
                    String countInput = JOptionPane.showInputDialog("How many messages would you like to send?");
                    if (countInput == null) break;
                    try {
                        int count = Integer.parseInt(countInput);
                        for (int i = 0; i < count; i++) {
                            messageCount++;
                            String recipient = JOptionPane.showInputDialog("Enter recipient (+countrycode):");
                            if (recipient == null) break;

                            if (!validateCellphone(recipient)) {
                                JOptionPane.showMessageDialog(null, "Invalid phone number format!");
                                continue;
                            }

                            String text = JOptionPane.showInputDialog("Enter your message (max 250 chars):");
                            if (text == null || text.trim().isEmpty() || text.length() > 250) {
                                JOptionPane.showMessageDialog(null, "Please enter a message of less than 250");
                                continue;
                            }

                            Message msg = new Message(messageCount, recipient, text);
                            messages.add(msg);
                            Message.storeMessage(msg);

                            JOptionPane.showMessageDialog(null, "✅ Message sent!\n\n" + msg.toString());
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                    }
                }

                case "2" -> JOptionPane.showMessageDialog(null, "Coming soon");

               

                case "3" -> {
                    JOptionPane.showMessageDialog(null, "Exiting QuickChat. Goodbye!");
                    running = false;
                }

                default -> JOptionPane.showMessageDialog(null, "Invalid choice! Please select 1–3.");
            }
        }
    }

    //  TEXT-BASED QUICKCHAT (JUNIT TESTS)
    public static void quickChat(Scanner input) {
        System.out.println("Welcome to QuickChat");

        boolean running = true;
        while (running) {
            System.out.println("\nPlease select an option:");
            System.out.println("1. Send Message");
            System.out.println("2. show recent messages");
           
            System.out.println("3. Quit");

            System.out.print("Enter your choice: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1" -> System.out.println("Message sent!");
                case "2" -> System.out.println("Message discarded.");
                
                case "3" -> {
                    System.out.println("Exiting QuickChat");
                    running = false;
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    // REGISTRATION / LOGIN GUI 
    public static void createUI() {
        JFrame frame = new JFrame("Account Registration & Login");
        frame.setSize(450, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(7, 2, 10, 10));

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
                JOptionPane.showMessageDialog(frame, "Registration failed! Check username, password, and phone format.");
            }
        });

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            if (loginUser(user, pass)) {
                JOptionPane.showMessageDialog(frame, "Welcome " + registeredUsername + "!");
                frame.dispose();

                //chat menu
                POE2.quickChatMenu();
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
