package enigma;

import org.junit.Test;
import static org.junit.Assert.*;


import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Machine class.
 *  @author Alice Wang
 */
public class MachineTest<R1> {

    Machine m = new Machine(alphabet1, 5, 3, ALL_ROTORS);

    @Test
    public void checkinsertRotors() {
        String[] names = new String[]{"Beta", "I", "B", "IV", "IV"};
        m.insertRotors(names);
        assertEquals("Beta", m.getRotors()[0].name());
        assertEquals(0, m.getRotors()[2].setting());
        assertEquals("IV", m.getRotors()[4].name());
        String[] names2 = new String[]{ "B", "Beta", "III", "IV", "I"};
        m.insertRotors(names2);
        assertEquals("Beta", m.getRotors()[1].name());
    }

    @Test
    public void checksetRotors() {
        String[] names2 = new String[]{ "B", "Beta", "III", "IV", "I"};
        String sets = "AXLE";
        String wrongsets = "AXL";
        m.insertRotors(names2);
        m.setRotors(sets);
        assertEquals(0, m.getRotors()[1].setting());
        assertNotEquals(0, m.getRotors()[2].setting());
        assertEquals(4, m.getRotors()[4].setting());
    }

    @Test
    public void checkcovert() {
        String[] names2 = new String[]{ "B", "Beta", "III", "IV", "I"};
        String sets = "AXLE";
        m.insertRotors(names2);
        m.setRotors(sets);
        Permutation plug = new Permutation("(YF) (ZH)", alphabet1);
        m.setPlugboard(plug);
        assertEquals(25, m.convert(24));
    }

    @Test
    public void checkconvert2() {
        String input = "FROM H";
        String[] names2 = new String[]{ "B", "Beta", "III", "IV", "I"};
        String sets = "AXLE";
        m.insertRotors(names2);
        m.setRotors(sets);
        Permutation plug = new Permutation("(HQ) (EX) (IP) (TR) "
                + "(BY)", alphabet1);
        m.setPlugboard(plug);
        assertEquals("QVPQS", m.convert(input));
    }
}
