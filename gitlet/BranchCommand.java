package gitlet;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import static gitlet.Utils.writeObject;

/** Class for the Branch commands. Does the action and serializes in my gitlet
 * working object.
 * @author Alice Wang
 */
public class BranchCommand implements Serializable {

    /** Current status of my gitlet.*/
    private Gitlet _gitlet;

    /** Constructor for the branch command class that gets initialized when
     * there is a branch-related command. does branch and rm-branch.
     */
    BranchCommand() {
        Gitlet.checkgit();
        File gitlet = new File(".gitlet/metadata");
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(gitlet));
            _gitlet = (Gitlet) in.readObject();
            in.close();
        } catch (IOException | ClassCastException
                | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** Function that carries out the branch [name] command. *
     * @param bName String
     */
    void doBranch(String bName) {
        if (_gitlet.getbranchHeadMap().containsKey(bName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        CommitObject curHead = _gitlet.getHead(_gitlet.getcurBranch());
        _gitlet.addHead(curHead, bName);
        writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);
    }

    /** The function that completes the rm-branch command.
     * @param bName String
     */
    void doRMBranch(String bName) {
        if (!_gitlet.getbranchHeadMap().containsKey(bName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (bName.equals(_gitlet.getcurBranch())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        _gitlet.removeBranch(bName);
        writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);
    }

}
