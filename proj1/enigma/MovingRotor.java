package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Alice Wang
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _permutation = perm;
    }


    @Override
    void advance() {
        int oldset = this.setting();
        this.set(_permutation.wrap(oldset + 1));
    }

    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length(); i++) {
            for (int j = i + 1; j < _notches.length(); j++) {
                if (_notches.charAt(i) == _notches.charAt(j)) {
                    throw new EnigmaException("cannot be duplicate notches");
                }
            }
        }
        for (int i = 0; i < _notches.length(); i++) {
            char c = _notches.charAt(i);
            int notchIndex = _permutation.alphabet().toInt(c);
            if (notchIndex == this.setting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean rotates() {
        return true;
    }

    /** function that returns my notches. */
    String getnotches() {
        return _notches;
    }

    /** String to store my notches. */
    private String _notches;

    /** Store my Permutation. */
    private Permutation _permutation;


}
