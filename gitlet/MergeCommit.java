package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Utils.sha1;

/** The class for my Merge Commit which extends the original CommitObject
 * and implements the Serializable IO.
 * @author Alice Wang
 */
public class MergeCommit extends CommitObject implements Serializable {

    /** My parents. */
    private CommitObject[] _parents;

    /** My mapping of file name to blob sha1's. */
    private TreeMap<String, String> _blobfiles;

    /** My sha1. */
    private String _sha1;

    /** My String of message. */
    private String _msg;

    /** My Date.*/
    private Date _date;

    /** Merge Commit constructor.
     *
      * @param msg String
     * @param timestamp Date
     * @param blobfiles TreeMap
     * @param parents CommitObject[]
     */
    MergeCommit(String msg, Date timestamp, TreeMap<String,
            String> blobfiles, CommitObject[] parents) {
        super(msg, timestamp, blobfiles, parents[0]);
        this._parents = parents;
        this._date = timestamp;
        this._blobfiles = blobfiles;
        this._msg = msg;
        String myFileNames = "";
        if (_blobfiles != null) {
            for (String s : _blobfiles.values()) {
                myFileNames += s + " ";
            }
        }
        _sha1 = sha1(_msg,
                _date.toString(),
                myFileNames,
                _parents[0].getName(),
                _parents[1].getName());
    }

    @Override
    CommitObject[] getParents() {
        return _parents;
    }

    @Override
    Boolean merged() {
        return true;
    }

    @Override
    String getLog() {
        String parent1 = _parents[0].getName().substring(0, 7);
        String parent2 = _parents[1].getName().substring(0, 7);
        SimpleDateFormat dateformat =
                new SimpleDateFormat("E MMM d HH:mm:ss y Z");
        String d = dateformat.format(_date);
        String log = "===" + "\n" + "commit " + _sha1 + "\n" + "Merge: "
             + parent1 + " " + parent2 + "\n" + "Date: " + d + "\n" + _msg
                + "\n";
        return log;
    }
}
