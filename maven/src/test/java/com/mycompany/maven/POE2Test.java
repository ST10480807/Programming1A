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
        POE2.sentMessages.clear();
        POE2.disregardedMessages.clear();
        POE2.storedMessages.clear();
        POE2.messageHashes.clear();
        POE2.messageIDs.clear();
        POE2.registerUser("dev_", "Pa$$w0rd!", "+27838884567");
        POE2.loginUser("dev_", "Pa$$w0rd!"); // login instead of accessing private field
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Before
    public void cleanStoredMessages() {
        File file = new File("stored_messages.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[]");
            writer.flush();
        } catch (IOException e) { }
    }

    private void populateTestData() {
        POE2.Message msg1 = new POE2.Message(1, "+27834557896", "Did you get the cake?");
        POE2.sentMessages.add(msg1);

        POE2.Message msg2 = new POE2.Message(2, "+27838884567", "Where are you? You are late! I have asked you to be on time.");
        POE2.storedMessages.add(msg2);

        POE2.disregardedMessages.add(new POE2.Message(3, "+27834484567", "Yohoooo, I am at your gate."));

        POE2.Message msg4 = new POE2.Message(4, "+27838884567", "It is dinner time!");
        POE2.sentMessages.add(msg4);

        POE2.Message msg5 = new POE2.Message(5, "+27838884567", "Ok, I am leaving without you.");
        POE2.storedMessages.add(msg5);

        POE2.MessageStorage.storeMessage(msg2);
        POE2.MessageStorage.storeMessage(msg5);

        POE2.sentMessages.addAll(POE2.storedMessages);

        POE2.messageHashes.clear();
        POE2.messageIDs.clear();
        for (POE2.Message msg : POE2.sentMessages) {
            POE2.messageHashes.add(msg.hash);
            POE2.messageIDs.add(msg.id);
        }
    }

    // validation and account test

    @Test public void testValidateUsername_Valid() { assertTrue(POE2.validateUsername("abc_")); }
    @Test public void testValidateUsername_TooLong_Fail() { assertFalse(POE2.validateUsername("abcdef")); }
    @Test public void testValidateUsername_NoUnderscore_Fail() { assertFalse(POE2.validateUsername("user")); }

    @Test public void testValidatePassword_Valid() { assertTrue(POE2.validatePassword("Passw0rd!")); }
    @Test public void testValidatePassword_NoSpecialChar_Fail() { assertFalse(POE2.validatePassword("Password12")); }
    @Test public void testValidatePassword_NoUpper_Fail() { assertFalse(POE2.validatePassword("password1!")); }
    @Test public void testValidatePassword_TooShort_Fail() { assertFalse(POE2.validatePassword("P@ss123")); }

    @Test public void testValidateCellphone_Valid() { assertTrue(POE2.validateCellphone("+27123456789")); }
    @Test public void testValidateCellphone_InvalidLength_Fail() { assertFalse(POE2.validateCellphone("+27123")); }

    @Test public void testRegisterUser_InvalidPhone_Fail() { assertFalse(POE2.registerUser("test_", "P@ssw0rd!", "0831234567")); }
    @Test public void testRegisterUserAndLogin_Success() {
        assertTrue(POE2.registerUser("user_", "Passw0rd!", "+27123456789"));
        assertTrue(POE2.loginUser("user_", "Passw0rd!"));
    }
    @Test public void testLoginUser_NotRegistered_Fail() { assertFalse(POE2.loginUser("ghost", "P@ssw0rd!")); }

    // message tests

    @Test public void testMessageCreationAndValidation_Valid() {
        POE2.Message msg = new POE2.Message(1, "+27123456789", "Hello world");
        assertTrue(msg.checkMessageID());
        assertTrue(msg.checkRecipientCell());
    }

    @Test public void testMessageHash_SingleWord() {
        POE2.Message msg = new POE2.Message(1, "+27...", "HELLO");
        assertTrue(msg.hash.endsWith(":1:HELLOHELLO"));
    }

    @Test public void testMessageHash_EmptyText_Safe() {
        POE2.Message msg = new POE2.Message(1, "+27...", "  ");
        assertTrue(msg.hash.endsWith(":1:"));
    }

    @Test public void testMessageLength_Valid() {
        String message = "This is a short message.";
        assertTrue(message.length() <= 250);
    }

    @Test public void testMessageTooLong_Fail() {
        String message = new String(new char[260]).replace('\0', 'A');
        assertFalse(message.length() <= 250);
    }

    @Test public void testMessageRecipient_TooLong_Fail() {
        POE2.Message msg = new POE2.Message(1, "+2712345678901", "Test");
        assertFalse(msg.checkRecipientCell());
    }

    // QuickChat Simulation Tests

    @Test public void testDisregardedMessages_Population() {
        String invalidText = new String(new char[260]).replace('\0', 'A');
        POE2.Message msg = new POE2.Message(6, "+27123456789", invalidText);
        POE2.disregardedMessages.add(msg);
        assertEquals(1, POE2.disregardedMessages.size());
    }

    @Test public void testSentMessagesArrayCorrectlyPopulated() {
        populateTestData();
        assertEquals(4, POE2.sentMessages.size());
    }

    @Test public void testDisplayLongestMessage() {
        populateTestData();
        POE2.Message longest = Collections.max(POE2.sentMessages, Comparator.comparingInt(m -> m.text.length()));
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest.text);
    }

    @Test public void testSearchByMessageID_Found() {
        populateTestData();
        POE2.Message msg4 = POE2.sentMessages.stream()
                .filter(msg -> msg.text.equals("It is dinner time!"))
                .findFirst().orElse(null);
        POE2.Message foundMsg = POE2.sentMessages.stream()
                .filter(msg -> msg.id.equals(msg4.id))
                .findFirst().orElse(null);
        assertNotNull(foundMsg);
    }

    @Test public void testSearchByMessageID_NotFound_Fail() {
        populateTestData();
        POE2.Message foundMsg = POE2.sentMessages.stream()
                .filter(msg -> msg.id.equals("NONEXISTENT"))
                .findFirst().orElse(null);
        assertNull(foundMsg);
    }

    @Test public void testSearchByRecipient() {
        populateTestData();
        long count = POE2.sentMessages.stream()
                .filter(msg -> msg.recipient.equals("+27838884567"))
                .count();
        assertEquals(3, count);
    }

    @Test public void testDeleteByHash_Success() {
        populateTestData();
        POE2.Message msg2 = POE2.storedMessages.get(0);
        String hashToDelete = msg2.hash;
        int initialSize = POE2.sentMessages.size();
        POE2.Message toDelete = POE2.sentMessages.stream()
                .filter(msg -> msg.hash.equals(hashToDelete))
                .findFirst().orElse(null);
        POE2.sentMessages.remove(toDelete);
        assertFalse(POE2.sentMessages.stream().anyMatch(msg -> msg.hash.equals(hashToDelete)));
        assertEquals(initialSize - 1, POE2.sentMessages.size());
    }

    @Test public void testDisplayReportContents() {
        populateTestData();
        for (POE2.Message msg : POE2.sentMessages) {
            assertNotNull(msg.id);
            assertNotNull(msg.hash);
            assertNotNull(msg.recipient);
        }
    }
}
