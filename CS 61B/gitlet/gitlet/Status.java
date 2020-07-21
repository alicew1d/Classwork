package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** The constructor class for my Status command.
 * @author Alice Wang
 */
public class Status implements Serializable {

    /** my current gitlet. */
    private Gitlet _gitlet;
    /** my current stage.*/
    private Stage _stage;


    /** The constructor for my status command.
     */
    Status() {
        Gitlet.checkgit();
        File gitlet = new File(".gitlet/metadata");
        _gitlet = Utils.readObject(gitlet, Gitlet.class);

        File stage = new File(".gitlet/.staging/stageobject");
        _stage = Utils.readObject(stage, Stage.class);
    }

    /** Does the status action. */
    void doStatus() {

        String status = "=== Branches ===" + "\n";
        Set bns = _gitlet.getbranchHeadMap().keySet();
        for (Object s : bns) {
            if (s.equals(_gitlet.getcurBranch())) {
                status += "*" + s + "\n";
            } else {
                status += s + "\n";
            }
        }
        status += "\n" + "=== Staged Files ===" + "\n";
        List<String> stagedfiles = Utils.plainFilenamesIn(".gitlet/.staging");
        for (String s : stagedfiles) {
            if (s.equals("stageobject")) {
                continue;
            } else {
                status += s + "\n";
            }
        }

        status += "\n" + "=== Removed Files ===" + "\n";
        List<String> removedfiles = _stage.removedFiles();
        Collections.sort(removedfiles);
        for (String s : removedfiles) {
            status += s + "\n";
        }

        status += "\n" + "=== Modifications Not Staged For Commit ==="
                + "\n" + "\n" + "=== Untracked Files ===" + "\n";
        System.out.println(status);

    }
}
