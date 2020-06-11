package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.util.TreeMap;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;


import static gitlet.Utils.*;

/** The class that initializes the Add command.
 * @author Alice Wang */
public class Add implements Serializable {

    /** String to store the filename of the file I want to add.*/
    private String _filename;

    /** My current Gitlet Object. */
    private Gitlet _gitlet;

    /** my current stage. */
    private Stage _stage;

    /** the source File object and path. In your
     * working directory.
     */
    private File _src;

    /** the destination File object and path.
     found in the .gitlet/.staging/ directory */
    private File _dest;

    /** Constructor for the Add command.
     * @param filename String */
    Add(String filename) {
        _filename = filename;
        _src = new File(_filename);
        _dest = new File(".gitlet/.staging/" + _filename);
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

        File stage = new File(".gitlet/.staging/stageobject");
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(stage));
            _stage = (Stage) in.readObject();
            in.close();
        } catch (IOException | ClassCastException
                | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /** executes the add command. */
    void doAdd() throws IOException {
        try {
            File toadd = new File(_filename);
            if (!toadd.exists()) {
                System.out.println("File does not exist.");
                System.exit(0);
            }
            TreeMap gitmap = _gitlet.getbranchHeadMap();
            CommitObject head = (CommitObject)
                    gitmap.get(_gitlet.getcurBranch());
            TreeMap blobfiles = head.getFiles();

            addLogic(blobfiles);

            if (_stage.removedFiles().contains(_filename)) {
                Blob compare = new Blob(_filename);
                if (compare.getName().equals(blobfiles.get(_filename))) {
                    _stage.removedFiles().remove(_filename);
                    _stage.modifiedFiles().remove(_filename);
                    _stage.newFiles().remove(_filename);
                }
            }

            File stageobject = new File(Init.STAGE_OBJ_DIR);
            writeObject(stageobject, _stage);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    /** Function that does the bulk of the Add command. Parses
     * through all the possibilities that could occur when adding.
     * takes into account persistence.
     * @param blobfiles TreeMap
     * @throws IOException
     */
    void addLogic(TreeMap blobfiles) throws IOException {

        if (blobfiles.isEmpty() && _stage.isNull()) {
            _stage.addName(_filename, "new");
            Files.copy(_src.toPath(), _dest.toPath());

        } else if (blobfiles.isEmpty() && !_stage.isNull()) {
            if (_stage.newFiles().contains(_filename)) {
                _dest.delete();
                Files.copy(_src.toPath(), _dest.toPath());
            } else if (_stage.modifiedFiles().contains(_filename)) {
                _dest.delete();
                Files.copy(_src.toPath(), _dest.toPath());
            } else if (!_stage.newFiles().contains(_filename)) {
                _stage.addName(_filename, "new");
                Files.copy(_src.toPath(), _dest.toPath());
            }

        } else if (!blobfiles.isEmpty()) {
            if (blobfiles.containsKey(_filename)) {
                Blob tempblob = new Blob(_filename);
                String tempsha1 = tempblob.getName();
                if (blobfiles.get(_filename).equals(tempsha1)) {
                    _stage.newFiles().remove(_filename);
                    _stage.modifiedFiles().remove(_filename);
                    _dest.delete();
                } else if (!blobfiles.get(_filename).equals(tempsha1)) {
                    if (!_stage.modifiedFiles().contains(_filename)) {
                        _stage.addName(_filename, "modified");
                    }
                    _dest.delete();
                    Files.copy(_src.toPath(), _dest.toPath());
                }
            } else {
                _stage.addName(_filename, "new");
                Files.copy(_src.toPath(), _dest.toPath());
            }
        }
    }
}
