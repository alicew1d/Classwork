package gitlet;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/** The class of my Checkout commands that get instantiated whenever
 * a checkout command is called.
 * @author Alice Wang */
public class Checkout implements Serializable {

    /** Current status of my gitlet tree.*/
    private Gitlet _gitlet;

    /** Current reference of my stage object. */
    private Stage _stage;

    /** The constructor for the Checkout class that has 3 different
     * functions for each different checkout command. */
    Checkout() {
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

    /** does the checkout command when given the file name from my head commit.
     * @param filename String
     */
    void doCheckoutFile(String filename) {
        CommitObject curCommit = _gitlet.getHead(_gitlet.getcurBranch());
        TreeMap blobFiles = curCommit.getFiles();

        if (!blobFiles.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            File blobfile = new File(".gitlet/.blob/"
                    + blobFiles.get(filename));
            Blob checkoutBlob = null;
            try {
                ObjectInputStream in =
                        new ObjectInputStream(new FileInputStream(blobfile));
                checkoutBlob = (Blob) in.readObject();
                in.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            Utils.writeContents(new File(filename), checkoutBlob.getContents());
        }
    }

    /** Checkout the file from the commit ID given.
     * @param cID String
     * @param filename String */
    void doCheckoutCommitFile(String cID, String filename) {
        if (cID.length() < Utils.UID_LENGTH) {
            File cFiles = new File(Init.COMMIT_DIR);
            File[] allcommits = cFiles.listFiles();
            for (File f : allcommits) {
                if (f.getName().regionMatches(0, cID, 0, cID.length())) {
                    cID = f.getName();
                }
            }
        }
        File cFile = new File(".gitlet/.commit/" + cID);
        CommitObject cO = null;

        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(cFile));
            cO = (CommitObject) in.readObject();
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


        if (cO != null) {
            TreeMap cfileMap = cO.getFiles();
            if (!cfileMap.containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            } else {
                File blob = new File(".gitlet/.blob/"
                        + cfileMap.get(filename));
                Blob ckBlob = null;
                try {
                    ObjectInputStream in =
                            new ObjectInputStream(new FileInputStream(blob));
                    ckBlob = (Blob) in.readObject();
                    in.close();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Utils.writeContents(new File(filename), ckBlob.getContents());
            }
        } else {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
    }

    /** Function that does the command checkout [branchname].
     * @param branchName String */
    void doCheckoutBranch(String branchName) {
        if (branchName.equals(_gitlet.getcurBranch())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!_gitlet.getbranchHeadMap().containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        CommitObject cO = _gitlet.getHead(branchName);
        TreeMap<String, String> branchFiles = cO.getFiles();
        Set<String> branchFileNames = branchFiles.keySet();

        String curBranch = _gitlet.getcurBranch();
        CommitObject recentCommit = _gitlet.getHead(curBranch);
        TreeMap<String, String> recComFiles = recentCommit.getFiles();
        Set<String> recComFileNames = recComFiles.keySet();

        File[] filesList = new File(System.getProperty("user.dir")).listFiles();
        ArrayList<String> curDirFileName = new ArrayList<>();
        for (File workingfile : filesList) {
            if (workingfile.getName().equals(".gitlet")) {
                continue;
            } else {
                curDirFileName.add(workingfile.getName());
            }
        }
        for (String cF: branchFileNames) {
            if (curDirFileName.contains(cF) && !recComFileNames.contains(cF)) {
                System.out.println("There is an untracked file in the way; "
                      +  "delete it or add it first.");
                System.exit(0);
            } else if (curDirFileName.contains(cF)
                    && recComFileNames.contains(cF)) {
                Blob cfBlob = getBlobContents(branchFiles.get(cF));
                Utils.writeContents(new File(cF), cfBlob.getContents());
            } else {
                Blob cfBlob = getBlobContents(branchFiles.get(cF));
                Utils.writeContents(new File(cF), cfBlob.getContents());
            }
        }

        for (String ccF : curDirFileName) {
            if (!branchFileNames.contains(ccF) && !ccF.equals("Makefile.txt")) {
                Utils.restrictedDelete(ccF);
            }
        }

        _stage.clear();
        _stage.clearStageDir();
        Utils.writeObject(new File(Init.STAGE_OBJ_DIR), _stage);
        _gitlet.setCurBranch(branchName);
        Utils.writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);
    }

    /** get Blob given my sha1.
     * @param sha1 String
     * @return Blob */
    static Blob getBlobContents(String sha1) {
        File blobfile = new File(".gitlet/.blob/" + sha1);
        Blob checkoutBlob = null;
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(blobfile));
            checkoutBlob = (Blob) in.readObject();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return checkoutBlob;
    }

}
