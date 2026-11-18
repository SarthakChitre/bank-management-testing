import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testDefaultConstructor() {
        Account acc = new Account();
        assertNull(acc.getName());
        assertEquals(0, acc.getAccountNumber());
        assertNull(acc.getPIN());
        assertEquals(0.0, acc.getAmount());
    }

    @Test
    void testParameterizedConstructorAndBaseAmount() {
        // The Account class automatically adds 1000 to the amount
        Account acc = new Account("Alice", 12345678, "1234", 500.0);
        assertEquals("Alice", acc.getName());
        assertEquals(12345678, acc.getAccountNumber());
        assertEquals("1234", acc.getPIN());
        assertEquals(1500.0, acc.getAmount());
    }

    @Test
    void testSettersAndGetters() {
        Account acc = new Account();
        acc.setName("Bob");
        acc.setAccountNumber(87654321);
        acc.setPIN("9999");
        acc.setAmount(2000.0);

        assertAll("Verify all setters",
                () -> assertEquals("Bob", acc.getName()),
                () -> assertEquals(87654321, acc.getAccountNumber()),
                () -> assertEquals("9999", acc.getPIN()),
                () -> assertEquals(2000.0, acc.getAmount())
        );
    }
}