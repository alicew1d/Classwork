package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

/** Object that stores the head commits and branch names. This
 * information is then serialized into a file called .gitlet/metadata.
 * @author Alice Wang
 */
public class Gitlet implements Serializable {

    /** serializable UID so that I stop getting errors.*/
    private static final long serialVersionUID = 28L;

    /** mapping of my branch names to the commitobject heads.*/
    private TreeMap<String, CommitObject> _branchHeadMap;

    /** the name of my current branch.*/
    private String _curBranch;

    /** Constructor constructing a new "null" treemap.*/
    Gitlet() {
        _branchHeadMap = new TreeMap<>();
        _curBranch = "master";
    }

    /**Sets my current branch to the branchName.
     * @param branchName String */
    void setCurBranch(String branchName) {
        if (!_branchHeadMap.containsKey(branchName)) {
            System.out.println("branch name does not exist");
            return;
        }
        _curBranch = branchName;
    }

    /** returns current branch name. */
    String getcurBranch() {
        return _curBranch;
    }

    /** returns mapping of my branchHeads. */
    TreeMap getbranchHeadMap() {
        return _branchHeadMap;
    }

    /** set the head of the given STRING BNAME and COMMIT OBEJCT CHEAD.*/
    void setBranchHead(String bName, CommitObject chead) {
        _branchHeadMap.replace(bName, chead);
    }

    /** returns the head of my branch name.
     *
     * @param branchName String
     * @return Commit Object
     */
    CommitObject getHead(String branchName) {
        return _branchHeadMap.get(branchName);
    }

    /** Add a new branch head to my map.
     *
     * @param commit Commit Object
     * @param branchName String
     */
    void addHead(CommitObject commit, String branchName) {
        if (_branchHeadMap.containsKey(branchName)) {
            _branchHeadMap.replace(branchName, commit);
        } else {
            _branchHeadMap.put(branchName, commit);
        }
    }

    /** Removes a branch pointer in my map.
    @param branchName String
     */
    void removeBranch(String branchName) {
        _branchHeadMap.remove(branchName);
    }

    /** Check if .gitlet exists in my working directory!*/
    static void checkgit() {
        File checkgit = new File(Init.GITLET_DIR);
        if (!checkgit.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
