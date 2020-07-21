package enigma;



import java.util.Collection;

import static enigma.EnigmaException.*;
import java.util.ArrayList;

/** Class that represents a complete enigma machine.
 *  @author Alice Wang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _rotors = new Rotor[numRotors];

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;

    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;

    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        ArrayList<Rotor> rotorArray = new ArrayList<>(_allRotors);
        if (rotors.length != _rotors.length) {
            throw new EnigmaException("Mismatched Rotor Length");
        }
        for (int i = 0; i < _rotors.length; i++) {
            for (int k = 0; k < rotorArray.size(); k++) {
                String comp = rotorArray.get(k).name();
                if (rotors[i].equals(comp)) {
                    _rotors[i] = rotorArray.get(k);
                    break;
                }
            }
        }

    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("wrong setting length");
        }
        int k = 0;
        for (int i = 1; i < _rotors.length; i++, k++) {
            char c = setting.charAt(k);
            if (!_rotors[i].alphabet().contains(c)) {
                throw new EnigmaException("Setting chars must be in alphabet");
            }
            _rotors[i].set(setting.charAt(k));
        }

    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;

    }

    /** Advance all of the Rotors in _rotors that should be advanced. */
    void advanceAll() {
        Boolean[] advance = new Boolean[_numRotors];
        for (int i = 0; i < _numRotors; i++) {
            advance[i] = _rotors[i].atNotch();
            if (_rotors[i].atNotch() && !_rotors[i - 1].rotates())  {
                advance[i] = false;
            }
            if (_rotors[i].atNotch()) {
                advance[i - 1] = true;
            }
            if (i == _numRotors - 1) {
                advance[i] = true;
            }
        }

        for (int j = 0; j < _numRotors; j++) {
            if (advance[j]) {
                _rotors[j].advance();
            }
        }
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        if (c == -1) {
            throw new EnigmaException("msg chars must be in alphabet");
        }
        this.advanceAll();
        int ans = _plugboard.permute(c);

        for (int n = _rotors.length - 1; n >= 0; n--) {
            ans = _rotors[n].convertForward(ans);
        }

        for (int k = 1; k < _rotors.length; k++) {
            ans = _rotors[k].convertBackward(ans);
        }
        ans = _plugboard.permute(ans);
        return ans;

    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replace(" ", "");
        String ans = "";
        for (int i = 0; i < msg.length(); i++) {
            int charindex = _alphabet.toInt(msg.charAt(i));
            int converted = this.convert(charindex);
            char convC = _alphabet.toChar(converted);
            ans += convC;
        }
        return ans;
    }

    /**
     * Returns my rotors.
     */
    Rotor[] getRotors() {
        return _rotors;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors in the machine. */
    private int _numRotors;

    /** Integer number of pawls in the machine. */
    private int _pawls;

    /** Stores the private collection of all rotors available. */
    private Collection<Rotor> _allRotors;

    /** Stores the rotors of our machine. */
    private Rotor[] _rotors;

    /** Stores the private permutation plugboard.*/
    private Permutation _plugboard;

}
