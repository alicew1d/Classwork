package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

/** The class for my remove command.
 * @author Alice Wang
 */
public class RM implements Serializable {

    /** my current gitlet.*/
    private Gitlet _gitlet;

    /** my current stage.*/
    private Stage _stage;

    /** the filename that I am removing. */
    private String _filename;

    /** COnstructor for my remove command.
     *
     * @param filename String
     */
    RM(String filename) {
        Gitlet.checkgit();
        _filename = filename;
        File gitlet = new File(".gitlet/metadata");
        _gitlet = Utils.readObject(gitlet, Gitlet.class);

        File stage = new File(".gitlet/.staging/stageobject");
        _stage = Utils.readObject(stage, Stage.class);
    }

    /** carries out the actions for my remove class.*/
    void doRM() {
        File toremove = new File(_filename);
        CommitObject cO = _gitlet.getHead(_gitlet.getcurBranch());
        TreeMap<String, String> bfiles = cO.getFiles();

        int check = 0;
        if (_stage.modifiedFiles().contains(_filename)) {
            _stage.modifiedFiles().remove(_filename);
            CommitCommand.stagedDelete(new File(Init.STAGE_DIR + "/"
                    + _filename));
            check++;
            if (bfiles.containsKey(_filename)) {
                if (new File(_filename).exists()) {
                    Blob tempblob = new Blob(_filename);
                    if (tempblob.getName().equals(bfiles.get(_filename))) {
                        toremove.delete();
                    }
                }
                _stage.addName(_filename, "remove");
            }
            check++;
        } else if (_stage.newFiles().contains(_filename)) {
            _stage.newFiles().remove(_filename);
            CommitCommand.stagedDelete(new File(Init.STAGE_DIR + "/"
                    + _filename));
            check++;
            if (bfiles.containsKey(_filename)) {
                if (new File(_filename).exists()) {
                    Blob tempblob = new Blob(_filename);
                    if (tempblob.getName().equals(bfiles.get(_filename))) {
                        toremove.delete();
                    }
                }
                _stage.addName(_filename, "remove");
            }
        } else if (bfiles.containsKey(_filename)) {
            if (new File(_filename).exists()) {
                Blob tempblob = new Blob(_filename);
                if (tempblob.getName().equals(bfiles.get(_filename))) {
                    toremove.delete();
                }
            }
            _stage.addName(_filename, "remove");
            check++;
        }
        if (check == 0) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Utils.writeObject(new File(Init.STAGE_OBJ_DIR), _stage);
    }
}
