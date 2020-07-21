package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import static gitlet.Utils.*;

/** the CommitObject class. Will contain information about my
 * commit message, timestamp, parent commit(s), and Mapping of my
 * currently tracked files.
 * @author Alice Wang
 */
public class CommitObject implements Serializable {

    private static final long serialVersionUID = 8L;

    /** String that stores my sha1 value. */
    private String _sha1;
    /** String that stores my commit message.*/
    private String _msg;
    /** String that stores my timestamp. */
    private Date _date;
    /** String that stores my parents as a CommitObject array.*/
    private CommitObject _parent;

    /** Map from file path to Blob's Sha1 (name). */
    private TreeMap<String, String> _blobfiles;

    /** Constructor for the commit object.
     *
     * @param msg String
     * @param date Date
     * @param parent CommitObject
     * @param blobfiles Treemap
     */
    CommitObject(String msg, Date date,
                 TreeMap<String, String> blobfiles, CommitObject parent) {
        _msg = msg;
        _date = date;
        _parent = parent;
        _blobfiles = blobfiles;

        if (blobfiles.values().isEmpty() && _parent != null) {
            _sha1 = sha1(_msg, _date.toString(), _parent.getName());
        } else if (blobfiles.values().isEmpty() && _parent == null) {
            _sha1 = sha1(_msg, _date.toString());
        } else {
            String myFileNames = "";
            for (String s : _blobfiles.values()) {
                myFileNames += s + " ";
            }
            _sha1 = sha1(_msg, _date.toString(), myFileNames,
                    _parent.getName());
        }
    }

    /**get my mapping of my tracked files.
     * @return TreeMap.
     */
    TreeMap<String, String> getFiles() {
        return _blobfiles;
    }

    /** get the parent of my commit object.
     * @return CommitObject.
     * */
    CommitObject getParent() {
        return _parent;
    }

    /** return the name, or the sha1 of my commit.
     * @return String. */
    String getName() {
        return _sha1;
    }

    /** gets my commit message.
     * @return String */
    String getMsg() {
        return _msg;
    }

    /** check if this commit is merged or not.
     * @return Boolean */
    Boolean merged() {
        return false;
    }

    /** get the parents of my commit object, if I actually have multiple.
     * @return CommitObject[] */
    CommitObject[] getParents() {
        return null;
    }
    /** return the log of my specific commit.*/
    String getLog() {
        SimpleDateFormat dateformat =
                new SimpleDateFormat("E MMM d HH:mm:ss y Z");
        String d = dateformat.format(_date);
        String log = "===\n" + "commit " + _sha1 + "\n"  + "Date: "
                + d + "\n" + _msg + "\n";
        return log;
    }

}
