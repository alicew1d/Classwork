package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Alice Wang
 */
class Alphabet {

    /**
     * A new alphabet containing CHARS.  Character number #k has index
     * K (numbering from 0). No character may be duplicated.
     */
    Alphabet(String chars) {
        this._chars = chars;
        this._size = chars.length();
        checkRepeat(chars);

    }

    /**
     * stores private String of characters.
     */
    private String _chars;

    /**
     * private store of alphabet size.
     */
    private int _size;

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return this._size;
    }

    /**
     * Returns true if preprocess(CH) is in this alphabet.
     */
    boolean contains(char ch) {
        for (int i = 0; i < _size; i++) {
            if (_chars.charAt(i) == ch) {
                return true;
            }
        }
        return false;

    }


    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        if (index > _size || index < 0) {
            throw new IndexOutOfBoundsException("index size incorrect");
        }
        return _chars.charAt(index);

    }

    /**
     * Returns the index of character preprocess(CH), which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        return _chars.indexOf(ch);

    }

    /** Search for repeating characters in String.
     * @param A String alphabet
     * Source: CS61B Lecture 16 Slides*/
    void checkRepeat(String A) {
        for (int i = 0; i < A.length(); i += 1) {
            for (int j = i + 1; j < A.length(); j += 1) {
                if (A.charAt(i) == A.charAt(j)) {
                    throw new EnigmaException("Repeating Characters Not "
                            + "allowed in alphabet or cycle");
                }
            }
        }
    }
}
