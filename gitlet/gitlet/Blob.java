package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

/** the Blob Class. An Object used to store specific information about
 * a file including its contents and its sha1. This object is serializable
 * and will eventually be saved in a .blob directory in .gitlet.
 * @author Alice Wang */
public class Blob implements Serializable {

    /** String of my sha1 which also serves as my name. */
    private String _sha1;

    /** byte[] of my the contents of the file I represent.*/
    private byte[] _filecontent;

    /** Blob constructor that will add the contents of my file to my file
     * contents and generate my sha1 value.
     * @param filePath String
     */
    Blob(String filePath) {
        _filecontent = readContents(new File(filePath));
        _sha1 = sha1(_filecontent);
    }

    /** returns my sha1 Name.*/
    String getName() {
        return _sha1;
    }

    /** returns my file contents. */
    byte[] getContents() {
        return _filecontent;
    }

}
