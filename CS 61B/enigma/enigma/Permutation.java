package enigma;

import static enigma.EnigmaException.*;
import java.util.ArrayList;


/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Alice Wang
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _oldcycle = cycles;
        _cycles = new ArrayList<String>();
        addAllCycles();
        checkCycleRep();
    }

    /** add all of the cycles in Permutation to _cycles. */
    private void addAllCycles() {
        String[] c = split(_oldcycle);
        for (int i = 0; i < c.length; i++) {
            if (c[i].contains("(") || c[i].contains(")")
                    || c[i].contains("*")) {
                throw new EnigmaException("invalid cycle string");
            }
            _cycles.add(c[i]);
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        _cycles.add(cycle);
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int charIn = wrap(p);
        char cIn = _alphabet.toChar(charIn);
        char permOut = cIn;
        for (int i = 0; i < _cycles.size(); i++) {
            String cur = _cycles.get(i);
            for (int j = 0; j < cur.length(); j++) {
                if (cIn == cur.charAt(j)) {
                    if (j == cur.length() - 1) {
                        permOut = cur.charAt(0);
                    } else {
                        permOut = cur.charAt(j + 1);
                    }
                }
            }
        }
        int permIn = _alphabet.toInt(permOut);
        return permIn;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int inverCharInd = wrap(c);
        char invercIn = _alphabet.toChar(inverCharInd);
        char inverPermOut = invercIn;
        for (int i = 0; i < _cycles.size(); i++) {
            String cur = _cycles.get(i);
            for (int j = 0; j < cur.length(); j++) {
                if (invercIn == cur.charAt(j)) {
                    if (j == 0) {
                        inverPermOut = cur.charAt(cur.length() - 1);
                    } else {
                        inverPermOut = cur.charAt(j - 1);
                    }
                }
            }
        }
        int inverPOIn = _alphabet.toInt(inverPermOut);
        return inverPOIn;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int ci = _alphabet.toInt(p);
        int pi = permute(ci);
        char pOut = _alphabet.toChar(pi);
        return pOut;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int ci = _alphabet.toInt(c);
        int pi = invert(ci);
        char pOut = _alphabet.toChar(pi);
        return pOut;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int check = 0;
        for (int i = 0; i < _cycles.size(); i++) {
            String curr = _cycles.get(i);
            if (curr.length() == 1) {
                return false;
            }
            for (int j = 0; j < curr.length(); j++) {
                if (_alphabet.contains(curr.charAt(j))) {
                    check++;
                }
            }
        }
        return (check == _alphabet.size());
    }

    /**
     * Returns the splitting of cycles.
     *
     * @param cycle String of original cycles.
     * @return a string array of our cycles.
     */
    public String[] split(String cycle) {
        String[] splitcycles;
        if (cycle.contains("((") || cycle.contains("))")
                || cycle.contains("*")) {
            throw new EnigmaException("wrong cycle input");
        }
        if (cycle.contains(")(")) {
            cycle = cycle.replace(")(", " ");
        }
        if (cycle.contains(" ")) {
            cycle = cycle.replace(")", "").replace("(", "");
            splitcycles = cycle.split(" ");
        } else {
            splitcycles = new String[1];
            cycle = cycle.replace("(", "");
            cycle = cycle.replace(")", "");
            splitcycles[0] = cycle;
        }
        return splitcycles;
    }

    /** check if there are repeating letters in cycle. */
    public void checkCycleRep() {
        String check = "";
        for (int i = 0; i < _cycles.size(); i++) {
            for (int j = 0; j < _cycles.get(i).length(); j++) {
                check += _cycles.get(i).charAt(j);
            }
        }
        _alphabet.checkRepeat(check);
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles of this permutation. */
    private ArrayList<String> _cycles;

    /** original cycle string of this permutation. */
    private String _oldcycle;

}
