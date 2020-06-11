package gitlet;


import java.io.File;
import java.io.Serializable;

/** The Official class for the Global Log command.
 * @author Alice Wang.
 */
public class GlobalLog implements Serializable {

    /** the constructor for the global log class.*/
    GlobalLog() {
        Gitlet.checkgit();
    }

    /** Do the action of the global log by going through the commits
     * in my commit file and returning their log.
     */
    void doGlobalLog() {
        File commits = new File(Init.COMMIT_DIR);
        File[] allcommits = commits.listFiles();

        for (File f : allcommits) {
            if (f.isFile()) {
                File commit = new File(".gitlet/.commit/" + f.getName());
                CommitObject cO = Utils.readObject(commit, CommitObject.class);
                if (cO != null) {
                    System.out.println(cO.getLog());
                }
            }
        }
    }
}
