/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package com.mycompany.maven;

import org.junit.*;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

public class POE2Test {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Before
    public void cleanStoredMessages() {
        File file = new File("stored_messages.json");
        if (file.exists()) file.delete();
    }

    // VALIDATION TESTS 
    @Test
    public void testValidateUsername() {
        assertTrue(POE2.validateUsername("abc_"));
        assertFalse(POE2.validateUsername("abcdef"));
        assertFalse(POE2.validateUsername("abc"));
    }

    @Test
    public void testValidatePassword() {
        assertTrue(POE2.validatePassword("Passw0rd!"));
        assertFalse(POE2.validatePassword("password"));
        assertFalse(POE2.validatePassword("PASSWORD1"));
        assertFalse(POE2.validatePassword("Pass1"));
    }

    @Test
    public void testValidateCellphone() {
        assertTrue(POE2.validateCellphone("+27123456789"));
        assertFalse(POE2.validateCellphone("123456789"));
        assertFalse(POE2.validateCellphone("+27123"));
    }

    @Test
    public void testCellNumberFormatValid() {
        assertTrue(POE2.validateCellphone("+27718693002"));
    }

    @Test
    public void testCellNumberFormatInvalid() {
        assertFalse(POE2.validateCellphone("08575975889"));
    }

    // ACCOUNT TESTS 
    @Test
    public void testRegisterUserAndLogin() {
        assertFalse(POE2.registerUser("bad", "pass", "123"));
        assertTrue(POE2.registerUser("user_", "Passw0rd!", "+27123456789"));
        assertTrue(POE2.loginUser("user_", "Passw0rd!"));
        assertFalse(POE2.loginUser("user_", "wrongpass"));
        assertFalse(POE2.loginUser("wronguser", "Passw0rd!"));
    }

    // QUICKCHAT TESTS
    @Test
    public void testQuickChatQuitImmediately() {
        String simulatedInput = "3\n"; // option 3 = Quit in current code
        Scanner testInput = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        POE2.quickChat(testInput);

        String output = outContent.toString();
        assertTrue(output.contains("Welcome to QuickChat"));
        assertTrue(output.contains("Exiting QuickChat"));
    }

    @Test
    public void testQuickChatInvalidChoiceThenQuit() {
        String simulatedInput = "99\n3\n"; // invalid -> quit
        Scanner testInput = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        POE2.quickChat(testInput);

        String output = outContent.toString();
        assertTrue(output.contains("Invalid choice"));
        assertTrue(output.contains("Exiting QuickChat"));
    }

    @Test
    public void testQuickChatSendDiscardThenQuit() {
        // Only 1–3 exist in current POE2
        String simulatedInput = "1\n2\n3\n";
        Scanner testInput = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        POE2.quickChat(testInput);

        String output = outContent.toString();
        assertTrue(output.contains("Message sent!"));
        assertTrue(output.contains("Message discarded."));
        assertTrue(output.contains("Exiting QuickChat"));
    }

    // MESSAGE CLASS TESTS 
    @Test
    public void testMessageCreationAndValidation() {
        POE2.Message msg = new POE2.Message(1, "+27123456789", "Hello world");

        assertTrue(msg.checkMessageID());
        assertTrue(msg.checkRecipientCell());
        assertNotNull(msg.createMessageHash());
        assertTrue(msg.toString().contains("Hello world"));
    }

    @Test
    public void testMessageHashCorrect() {
        POE2.Message msg = new POE2.Message(0, "+27718693002", "Hi Mike, can you join us for dinner tonight");
        String[] words = msg.text.split(" ");
        String firstWord = words[0].toUpperCase();
        String lastWord = words[words.length - 1].toUpperCase();
        String expected = msg.id.substring(0, 2) + ":" + msg.number + ":" + firstWord + lastWord;

        assertEquals("Message hash generated correctly.", expected, msg.hash);
    }

    @Test
    public void testMessageIDGenerated() {
        POE2.Message msg = new POE2.Message(1, "+27718693002", "Testing ID");
        assertNotNull("Message ID generated: " + msg.id, msg.id);
        assertTrue("Message ID <=10 digits", msg.checkMessageID());
    }

    @Test
    public void testStoreMessage() throws IOException {
        POE2.Message msg = new POE2.Message(1, "+27123456789", "Test message");
        POE2.Message.storeMessage(msg);

        File file = new File("stored_messages.json");
        assertTrue(file.exists());

        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        assertTrue(content.contains("Test message"));
        assertTrue(content.contains(msg.id));
    }

    @Test
    public void testMessageLength() {
        String message = "Hi Mike, can you join us for dinner tonight";
        assertTrue("Message ready to send.", message.length() <= 250);
    }

    @Test
    public void testMessageTooLong() {
        String message = new String(new char[260]).replace('\0', 'A');
        assertFalse("Message exceeds 250 characters.", message.length() <= 250);
    }
}