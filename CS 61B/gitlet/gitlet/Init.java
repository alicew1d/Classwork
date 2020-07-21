package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;
import static gitlet.Utils.*;

/** class for the Init method. Will initialize all necessary files and
 * directories, create a new gitlet object and stage object and serialize
 * these objects to files in hte .gitlet directory. Will also add the initial
 * commit to the gitlet object as well.
 * @author Alice Wang
 */
public class Init {
    /** Path for my gitlet directory. */
    static final String GITLET_DIR = ".gitlet/";

    /** Path for my staged files directory. */
    static final String STAGE_DIR = ".gitlet/.staging";

    /** Path for my Stage Object file. */
    static final String STAGE_OBJ_DIR = ".gitlet/.staging/stageobject";

    /** Path for my Blob directory. */
    static final String BLOB_DIR = ".gitlet/.blob";

    /**Path for my gitlet metadata file.*/
    static final String GITLET_METADATA_NAME = ".gitlet/metadata";

    /** Path for my commit directory. */
    static final String COMMIT_DIR = ".gitlet/.commit";

    /** my initial gitlet.*/
    private Gitlet _gitlet;

    /** my initial stage. */
    private Stage _stage;

    /** initializes my gitlet repo if it does not exist.*/
    Init() {
    }
    /** Helper method to run init.*/
    void doInit() {
        if (new File(GITLET_DIR).exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }

        new File(GITLET_DIR).mkdir();
        new File(STAGE_DIR).mkdir();
        new File(BLOB_DIR).mkdir();
        new File(COMMIT_DIR).mkdir();
        File stageobject = new File(STAGE_OBJ_DIR);
        File metadata = new File(GITLET_METADATA_NAME);
        try {
            metadata.createNewFile();
            stageobject.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _gitlet = new Gitlet();
        Date epoch = new Date(0);
        CommitObject initP = null;
        TreeMap<String, String> initMap = new TreeMap<>();
        CommitObject initialCommit = new CommitObject("initial commit",
                epoch, initMap, initP);

        _gitlet.addHead(initialCommit, "master");
        writeObject(metadata, _gitlet);

        _stage = new Stage();
        writeObject(stageobject, _stage);

        writeObject(new File(COMMIT_DIR + "/"
                + initialCommit.getName()), initialCommit);
    }
}

