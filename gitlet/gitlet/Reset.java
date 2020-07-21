package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Set;

import static gitlet.Utils.writeObject;

/** The Reset class.
 * @author Alice Wang
 */
public class Reset implements Serializable {

    /** The commit ID thats passed in to my Reset. */
    private String _commitID;

    /** The commit object im trying to reset to.*/
    private CommitObject _resetC;

    /** my current working gitlet. */
    private Gitlet _gitlet;

    /** my current stage. */
    private Stage _stage;

    /** The constructor for the Reset command. Deserializes
     * gitlet and my stage.
     *
     * @param commitID String
     */
    Reset(String commitID) {
        Gitlet.checkgit();
        _commitID = commitID;
        File gitlet = new File(".gitlet/metadata");
        _gitlet = Utils.readObject(gitlet, Gitlet.class);

        File stage = new File(".gitlet/.staging/stageobject");
        _stage = Utils.readObject(stage, Stage.class);
    }

    /** does the reset action of the reset command.*/
    void doReset() {
        File[] commits = new File(Init.COMMIT_DIR).listFiles();
        if (_commitID.length() < Utils.UID_LENGTH) {
            for (File f : commits) {
                if (f.getName().regionMatches(0,
                        _commitID, 0, _commitID.length())) {
                    _commitID = f.getName();
                }
            }
        }
        File resetCommit = new File(Init.COMMIT_DIR + "/" + _commitID);
        if (!resetCommit.exists()) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        _resetC = Utils.readObject(resetCommit, CommitObject.class);
        if (_resetC != null) {
            CommitObject curCom = _gitlet.getHead(_gitlet.getcurBranch());
            Set resetFiles = _resetC.getFiles().keySet();
            Set trackedFiles = curCom.getFiles().keySet();
            String[] wkDirf = new File(System.getProperty("user.dir")).list();
            for (String wk : wkDirf) {
                if (!trackedFiles.contains(wk) && resetFiles.contains(wk)) {
                    System.out.println("There is an untracked file in the way; "
                          +  "delete it or add it first.");
                    System.exit(0);
                }
            }
            for (Object tracked : trackedFiles) {
                if (!resetFiles.contains(tracked)) {
                    String tk = (String) tracked;
                    Utils.restrictedDelete(tk);
                }
            }
            for (Object resetfile : resetFiles) {
                String rsf = (String) resetfile;
                String resetSha1 = _resetC.getFiles().get(resetfile);
                File resF = new File(rsf);
                Blob resetBlob = Checkout.getBlobContents(resetSha1);
                if (resetBlob != null && resF.exists()) {
                    Utils.writeContents(resF, resetBlob.getContents());
                }
            }
        } else {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        _stage.clear();
        _stage.clearStageDir();
        writeObject(new File(Init.STAGE_OBJ_DIR), _stage);
        _gitlet.addHead(_resetC, _gitlet.getcurBranch());
        writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);
    }
}
