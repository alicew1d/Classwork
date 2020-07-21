package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/** the class for the FIND command. go through my commit folder and find all
 * the commits I want.
 * @author Alice Wang
 */

public class Find implements Serializable {

    /** String of the message I want to find. */
    private String _msg;


    /** The constructor for the Find command. takes in the string of the message
     * that I want to find.
     * @param msg String */
    Find(String msg) {
        Gitlet.checkgit();
        _msg = msg;
    }

    /** performs the Find command action. */
    void doFind() {
        File commits = new File(Init.COMMIT_DIR);
        File[] commitobjects = commits.listFiles();
        ArrayList<String> cIDs = new ArrayList<>();
        for (File f : commitobjects) {
            CommitObject cO = Utils.readObject(f, CommitObject.class);
            if (cO != null) {
                if (cO.getMsg().equals(_msg)) {
                    cIDs.add(cO.getName());
                    System.out.println(cO.getName());
                }
            }
        }
        if (cIDs.isEmpty()) {
            System.out.println("Found no commit with that message.");
        }
    }
}
