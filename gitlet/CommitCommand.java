package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;


import static gitlet.Utils.*;

/** A class for the commit command. Will take any staged files
 * and then save them as commits. Will change the pointer to
 * my head commit. Will delete staged files.
 * @author Alice Wang
 */
public class CommitCommand implements Serializable {

    /** my commit message string. */
    private String _msg;

    /** my current gitlet object. */
    private Gitlet _gitlet;

    /** my current stage object. */
    private Stage _stage;

    /** read in my gitlet object and stage through Object input stream.
     * @param msg String of my commit message.
     */

    CommitCommand(String msg) {
        _msg = msg;

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

    /** Does the actions of the commit command. */
    void doCommit() throws IOException {
        if (_stage.isNull()) {
            System.out.println("No changes added to the commit");
            return;
        }

        Date timestamp =  new Date();
        CommitObject parent = _gitlet.getHead(_gitlet.getcurBranch());
        TreeMap<String, String> parentFiles = parent.getFiles();
        TreeMap<String, String> fileMap = new TreeMap<>();
        fileMap.putAll(parentFiles);
        for (String file : _stage.newFiles()) {
            Blob b = makeBlob(file);
            fileMap.put(file, b.getName());
            File bFile = new File(".gitlet/.staging/" + file);
            stagedDelete(bFile);
        }

        for (String f : _stage.modifiedFiles()) {
            Blob b = makeBlob(f);
            fileMap.put(f, b.getName());
            File bFile = new File(".gitlet/.staging/" + f);
            stagedDelete(bFile);
        }

        for (String f : _stage.removedFiles()) {
            fileMap.remove(f);
        }

        CommitObject toCommit =
                new CommitObject(_msg, timestamp, fileMap, parent);

        _stage.clear();
        writeObject(new File(Init.STAGE_OBJ_DIR), _stage);


        _gitlet.addHead(toCommit, _gitlet.getcurBranch());
        writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);

        File commitFile = new File(".gitlet/.commit/" + toCommit.getName());
        writeObject(commitFile, toCommit);
    }

    /** creates a blob and serializes it into the .gitlet/.blobs folder.
     * @param filename  String
     * @return Blob */
    static Blob makeBlob(String filename) throws IOException {
        Blob blob = new Blob(filename);
        File blobDir = new File(".gitlet/.blob/" + blob.getName());
        writeObject(blobDir, blob);
        return blob;
    }

    /** deletes a file in my staging directory.
     * @param file  File
     * @return Boolean */
    static boolean stagedDelete(File file) {
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

}
