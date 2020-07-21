package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.Arrays;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */

    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checksplit() {
        String cycle = "(ABCD) (EFGH) (IJKL)";
        String cycle3 = "(ABCD)";
        Permutation np = new Permutation(cycle, new Alphabet("ABCDEFGHIJKL"));
        assertEquals(Arrays.asList("ABCD", "EFGH", "IJKL"),
                Arrays.asList(np.split(cycle)));
        assertEquals(Arrays.asList("ABCD"),
                Arrays.asList(np.split(cycle3)));
    }

    @Test
    public void checkinvert() {
        String cycle = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation p = new Permutation(cycle,
                new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals('Q', p.invert('X'));
        assertEquals(16, p.invert(23));
        assertEquals('S', p.invert('S'));
        assertEquals(18, p.invert(18));
        assertEquals('U', p.invert('A'));
        assertEquals(20, p.invert(0));
        assertEquals('I', p.invert('V'));
        assertEquals('V', p.invert('I'));
    }

    @Test
    public void checkpermute() {
        String cycle = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation p = new Permutation(cycle,
                new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals('R', p.permute('X'));
        assertEquals(17, p.permute(23));
        assertEquals('A', p.permute('U'));
        assertEquals(0, p.permute(20));
        assertEquals(18, p.permute(18));
        assertEquals('S', p.permute('S'));
        assertEquals('J', p.permute('Z'));
    }

    @Test
    public void checkderangement() {
        String cycle = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation p = new Permutation(cycle,
                new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        String cycle2 = "(ABCDEFG) (HIJKLMN) (OPQRST) (UVWXYZ)";
        Permutation p2 = new Permutation(cycle2,
                new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        String cycle3 = "(ABCDE) (FGHIJKLMN) (OP) (QRST) (UV) (XYZ)";
        Permutation p3 = new Permutation(cycle3,
                new Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertEquals(false, p.derangement());
        assertEquals(true, p2.derangement());
        assertEquals(false, p3.derangement());

    }


}
