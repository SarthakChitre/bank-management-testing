import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

class BankTest {
    private final InputStream systemIn = System.in;
    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new Bank();
    }

    @AfterEach
    void tearDown() {
        // Restore standard input after each test so we don't break the console
        System.setIn(systemIn);
    }

    // Helper method to simulate a user typing on the keyboard
    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    @Test
    void testAddNewRecord() {
        // Simulates typing: Name, Acc Num, PIN, Amount
        provideInput("John Doe\n12345678\n1234\n500.0\n");
        bank.addNewRecord();

        assertAll("Verify account was added correctly",
                () -> assertEquals(1, bank.AL.size()),
                () -> assertEquals("John Doe", bank.AL.get(0).getName()),
                () -> assertEquals(1500.0, bank.AL.get(0).getAmount())
        );
    }

    @Test
    void testTransferSuccess() {
        bank.AL.add(new Account("Sender", 11111111, "1111", 500)); // Balance: 1500
        bank.AL.add(new Account("Receiver", 22222222, "2222", 0)); // Balance: 1000

        // Simulates: Sender Acc, Sender PIN, Receiver Acc, Amount
        provideInput("11111111\n1111\n22222222\n500.0\n");
        bank.transfer();

        assertEquals(1000.0, bank.AL.get(0).getAmount()); // Sender balance went down
        assertEquals(1500.0, bank.AL.get(1).getAmount()); // Receiver balance went up
    }

    @Test
    void testTransferSenderNotFound() {
        bank.AL.add(new Account("Sender", 11111111, "1111", 500));
        provideInput("99999999\nwrongpin\n"); // Invalid sender
        bank.transfer();

        assertEquals(1500.0, bank.AL.get(0).getAmount()); // Balance should remain unchanged
    }

    @Test
    void testTransferReceiverNotFound() {
        bank.AL.add(new Account("Sender", 11111111, "1111", 500));
        provideInput("11111111\n1111\n99999999\n"); // Valid sender, invalid receiver
        bank.transfer();

        assertEquals(1500.0, bank.AL.get(0).getAmount()); // Balance unchanged
    }

    @Test
    void testTransferInsufficientFunds() {
        bank.AL.add(new Account("Sender", 11111111, "1111", 500));
        bank.AL.add(new Account("Receiver", 22222222, "2222", 0));
        provideInput("11111111\n1111\n22222222\n9000.0\n"); // Trying to transfer 9000
        bank.transfer();

        assertEquals(1500.0, bank.AL.get(0).getAmount()); // Balance unchanged
    }

    @Test
    void testWithdrawSuccess() {
        bank.AL.add(new Account("User", 11111111, "1111", 500));
        provideInput("11111111\n1111\n500.0\n");
        bank.withdraw();

        assertEquals(1000.0, bank.AL.get(0).getAmount());
    }

    @Test
    void testWithdrawAccountNotFound() {
        bank.AL.add(new Account("User", 11111111, "1111", 500));
        provideInput("99999999\n0000\n");
        bank.withdraw();

        assertEquals(1500.0, bank.AL.get(0).getAmount());
    }

    @Test
    void testWithdrawInsufficientFunds() {
        bank.AL.add(new Account("User", 11111111, "1111", 500));
        provideInput("11111111\n1111\n9000.0\n");
        bank.withdraw();

        assertEquals(1500.0, bank.AL.get(0).getAmount());
    }

    @Test
    void testPrint() {
        bank.AL.add(new Account("User1", 11111111, "1111", 500));
        // Verify that printing doesn't cause a crash
        assertDoesNotThrow(() -> bank.print());
    }

    @Test
    void testSaveAndLoad() {
        bank.AL.add(new Account("User1", 11111111, "1111", 500));

        // Save to file
        assertDoesNotThrow(() -> bank.save());

        // Clear memory, then load from file
        bank.AL.clear();
        assertDoesNotThrow(() -> bank.load());

        assertEquals(1, bank.AL.size());
        assertEquals("User1", bank.AL.get(0).getName());
    }

    @Test
    void testLoadException() {
        // Delete the file to force the catch block to run
        File file = new File("BankRecord.txt");
        if (file.exists()) {
            file.delete();
        }
        assertDoesNotThrow(() -> bank.load());
    }

    @Test
    void testWithdrawWrongPin() {
        // Tests the yellow branch on line 159 by providing a matching account but a wrong PIN
        bank.AL.add(new Account("User", 11111111, "1111", 500));
        provideInput("11111111\nWrongPin\n");
        bank.withdraw();
        assertEquals(1500.0, bank.AL.get(0).getAmount());
    }

    @Test
    void testTransferWrongPin() {
        // Tests the identical yellow branch in the transfer method
        bank.AL.add(new Account("Sender", 11111111, "1111", 500));
        provideInput("11111111\nWrongPin\n");
        bank.transfer();
        assertEquals(1500.0, bank.AL.get(0).getAmount());
    }

    @Test
    void testLoadNullObject() throws Exception {
        // Tests the red break; on line 203 by manually injecting a "null" into the file
        java.io.FileOutputStream fos = new java.io.FileOutputStream("BankRecord.txt");
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fos);
        out.writeObject(null);
        out.close();

        bank.AL.clear();
        assertDoesNotThrow(() -> bank.load());
    }

    @Test
    void testSaveException() throws Exception {
        // Create the file and lock it (read-only) so Java crashes when trying to write to it
        java.io.File file = new java.io.File("BankRecord.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        file.setWritable(false); // Lock the file!

        // This will force the catch block (line 224) to execute
        assertDoesNotThrow(() -> bank.save());

        // Clean up: unlock it and delete it so we don't break other tests
        file.setWritable(true);
        file.delete();
    }
}
