package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Date;
import java.util.Arrays;


import static gitlet.Init.BLOB_DIR;
import static gitlet.Utils.writeObject;

/** the class for my merge command.
 * @author Alice Wang
 */
public class Merge implements Serializable {


    /** My current gitlet. */
    private Gitlet _gitlet;

    /** My current stage.*/
    private Stage _stage;

    /** my most recent split point.*/
    private CommitObject _split;

    /** my current commit. */
    private CommitObject _curB;

    /** my given commit. */
    private CommitObject _givenB;

    /** My given branch name.*/
    private String _givenBN;

    /** My current branch name.*/
    private String _curBN;

    /** A string of sha1 commits of the ancestors of my given branch.*/
    private ArrayList<String> _givenBranchAncestors;

    /** the merge constructor for the merge command.
     *
     * @param givenBranch String
     */
    Merge(String givenBranch) {
        File gitlet = new File(Init.GITLET_METADATA_NAME);
        File stage = new File(Init.STAGE_OBJ_DIR);
        _gitlet = Utils.readObject(gitlet, Gitlet.class);
        _stage = Utils.readObject(stage, Stage.class);
        _givenBN = givenBranch;
        _curBN = _gitlet.getcurBranch();
        checkFailures();
        _curB = _gitlet.getHead(_curBN);
        _givenB = _gitlet.getHead(_givenBN);
        _givenBranchAncestors = new ArrayList<>();

    }

    /** check for prelimiary failure cases that dont require
     * the split point.
     */
    void checkFailures() {
        if (!_stage.isNull()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!_gitlet.getbranchHeadMap().keySet().contains(_givenBN)) {
            System.out.println(" A branch with that name does not exist.");
            System.exit(0);
        }
        if (_curBN.equals(_givenBN)) {
            System.out.println(" Cannot merge a branch with itself.");
            System.exit(0);
        }
        CommitObject currentBranch = _gitlet.getHead(_gitlet.getcurBranch());
        Set<String> trackedFiles = currentBranch.getFiles().keySet();

        CommitObject mergeBranch = _gitlet.getHead(_givenBN);
        Set<String> mergeFiles = mergeBranch.getFiles().keySet();

        File workDir = new File(System.getProperty("user.dir"));
        File[] workingfiles = workDir.listFiles();

        for (File f : workingfiles) {
            String file = f.getName();
            if (!trackedFiles.contains(file) && mergeFiles.contains(file)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
        }
    }

    /** does the fat merge command idk how y'all fit 70 lines.*/
    void doMerge() throws IOException {
        anotherCheckAncestors();
        anothersetSplit();
        if (_split != null) {
            checkSplitFailures();
            Set<String> givenBF = _givenB.getFiles().keySet();
            Set<String> curBF = _curB.getFiles().keySet();
            Set<String> splitFiles = _split.getFiles().keySet();
            TreeMap<String, String> gBmap = _givenB.getFiles();
            TreeMap<String, String> cBmap = _curB.getFiles();
            TreeMap<String, String> smap = _split.getFiles();
            for (String file : splitFiles) {
                if (givenBF.contains(file) && curBF.contains(file)) {
                    if (!gBmap.get(file).equals(smap.get(file))
                            && cBmap.get(file).equals(smap.get(file))) {
                        doCase1(file, gBmap);
                    } else if (gBmap.get(file).equals(smap.get(file))
                            && !cBmap.get(file).equals(smap.get(file))) {
                        continue;
                    } else if (!gBmap.get(file).equals(smap.get(file))
                            && !cBmap.get(file).equals(smap.get(file))) {
                        if  (gBmap.get(file).equals(cBmap.get(file))) {
                            continue;
                        } else if (!gBmap.get(file).equals(cBmap.get(file))) {
                            Blob current = Utils.readObject(new File(BLOB_DIR
                                    + "/" + cBmap.get(file)), Blob.class);
                            Blob given = Utils.readObject(new File(Init.BLOB_DIR
                                    + "/" + gBmap.get(file)), Blob.class);
                            resolveConflict(current.getContents(),
                                     given.getContents(), file);
                        }
                    }
                } else if (!givenBF.contains(file) && curBF.contains(file)) {
                    if (cBmap.get(file).equals(smap.get(file))) {
                        doCase2(file);
                    } else {
                        Blob current = Utils.readObject(new File(Init.BLOB_DIR
                                + "/" + cBmap.get(file)), Blob.class);
                        byte[] deleted = "".getBytes();
                        resolveConflict(current.getContents(), deleted, file);
                    }
                } else if (givenBF.contains(file) && !curBF.contains(file)) {
                    if (gBmap.get(file).equals(smap.get(file))) {
                        continue;
                    } else {
                        Blob given = Utils.readObject(new File(Init.BLOB_DIR
                              +  "/" + gBmap.get(file)), Blob.class);
                        byte[] delete = "".getBytes();
                        resolveConflict(delete, given.getContents(), file);
                    }
                } else {
                    continue;
                }
            }
            iterateThroughCurandGiven();
            commitMerge();
        }
    }

    /** Case 1: when it exists in both cur and given branch and
     * it is unmodified in the cur branch but modified in the given branch.
     * copy over the files and also stage it.
     * @param file String
     * @param gBmap TreeMap
     * @throws IOException
     */
    void doCase1(String file, TreeMap gBmap) throws IOException {
        File bf = new File(Init.BLOB_DIR + "/" + gBmap.get(file));
        Blob fileblob = Utils.readObject(bf, Blob.class);
        Utils.writeContents(new File(file), fileblob.getContents());
        _stage.addName(file, "modified");
        File stage = new File(Init.STAGE_DIR + "/" + file);
        stage.delete();
        Files.copy(new File(file).toPath(), stage.toPath());
    }

    /**files present at split point, unmodified in cur branch, absent in given
     * should be removed and untracked.
     * @param file String
     */
    void doCase2(String file) {
        File toremove = new File(file);
        Utils.restrictedDelete(toremove);
        File tounstage = new File(Init.STAGE_DIR + "/" + file);
        tounstage.delete();
        _stage.addName(file, "remove");
        _stage.modifiedFiles().remove(file);
        _stage.newFiles().remove(file);
    }
    /** check to see if my split commit is the same as the head
     * of my given branch or my cur branch.
     */
    void checkSplitFailures() {
        if (_split.equals(_givenB)) {
            System.out.println(" Given branch is an ancestor of "
                    + "the current branch.");
        } else if (_split.equals(_curB)) {
            String[] workdir = new File(System.getProperty("user.dir")).list();
            _gitlet.setBranchHead(_curBN, _givenB);
            List wkDirFiles = Arrays.asList(workdir);
            Set<String> givendir = _givenB.getFiles().keySet();
            for (String f : workdir) {
                if (!givendir.contains(f)) {
                    File file = new File(f);
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                } else {
                    File file = new File(f);
                    Blob b = Utils.readObject
                            (new File(BLOB_DIR + "/"
                                    + _givenB.getFiles().get(f)), Blob.class);
                    Utils.writeContents(file, b.getContents());
                }
            }
            System.out.println(" Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /** set my split by doing BFS with queue in cur branch. */
    void anothersetSplit() {
        Queue<CommitObject> queue = new LinkedList<>();
        queue.add(_curB);
        while (!queue.isEmpty()) {
            CommitObject top = queue.poll();
            if (_givenBranchAncestors.contains(top.getName())) {
                _split = top;
                break;
            } else {
                if (top.merged()) {
                    queue.add(top.getParents()[0]);
                    queue.add(top.getParents()[1]);
                } else {
                    queue.add(top.getParent());
                }
            }
        }
    }

    /** check my given branch ancestors using stack. */
    void anotherCheckAncestors() {
        Stack<CommitObject> stack = new Stack<>();
        stack.push(_givenB);
        while (!stack.isEmpty()) {
            CommitObject firstOut = stack.pop();
            if (!_givenBranchAncestors.contains(firstOut.getName())) {
                _givenBranchAncestors.add(firstOut.getName());
            }
            if (firstOut.merged()) {
                if (firstOut.getParents()[0] != null) {
                    stack.push(firstOut.getParents()[0]);
                }
                if (firstOut.getParents()[1] != null) {
                    stack.push(firstOut.getParents()[1]);
                }
            } else {
                if (firstOut.getParent() != null) {
                    stack.push(firstOut.getParent());
                }
            }
        }
    }

    /** contcatonates the BYTE[] of my different files. used an idea from
     * https://www.codespeedy.com/concatenate-multiple-byte-array-in-java/
     * to complete.
     * @param cCont Byte[]
     * @param gCont Byte[]
     * @param filename Byte[]
     * @throws IOException
     */
    void resolveConflict(byte[] cCont, byte[] gCont, String filename)
                                            throws IOException {
        String head = "<<<<<<< HEAD\n";
        String equalsigns = "=======\n";
        String end = ">>>>>>>\n";

        byte[] headbytes = head.getBytes();
        byte[] equalsignbytes = equalsigns.getBytes();
        byte[] endbytes = end.getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(headbytes);
            outputStream.write(cCont);
            outputStream.write(equalsignbytes);
            outputStream.write(gCont);
            outputStream.write(endbytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] finalcontents = outputStream.toByteArray();

        File mergedFile = new File(filename);
        Utils.writeContents(mergedFile, finalcontents);
        _stage.addName(filename, "modified");
        File stage = new File(Init.STAGE_DIR + "/" + filename);
        stage.delete();
        Files.copy(new File(filename).toPath(), stage.toPath());
        System.out.println("Encountered a merge conflict.");
    }

    /** commit for a mergeObject. called at the end of doMerge. */
    void commitMerge() throws IOException {
        String msg =  "Merged " + _givenBN + " into " + _curBN + ".";
        CommitObject[] parents = new CommitObject[]{_curB, _givenB};
        Date timestamp =  new Date();
        TreeMap<String, String> parentFiles = _curB.getFiles();
        TreeMap<String, String> fileMap = new TreeMap<>();
        fileMap.putAll(parentFiles);
        for (String file : _stage.newFiles()) {
            Blob b = CommitCommand.makeBlob(file);
            fileMap.put(file, b.getName());
            File bFile = new File(".gitlet/.staging/" + file);
            CommitCommand.stagedDelete(bFile);
        }

        for (String f : _stage.modifiedFiles()) {
            Blob b = CommitCommand.makeBlob(f);
            fileMap.put(f, b.getName());
            File bFile = new File(".gitlet/.staging/" + f);
            CommitCommand.stagedDelete(bFile);
        }
        for (String f : _stage.removedFiles()) {
            fileMap.remove(f);
        }
        CommitObject toCommit =
                new MergeCommit(msg, timestamp, fileMap, parents);

        _stage.clear();
        _stage.clearStageDir();
        writeObject(new File(Init.STAGE_OBJ_DIR), _stage);

        _gitlet.addHead(toCommit, _gitlet.getcurBranch());
        writeObject(new File(Init.GITLET_METADATA_NAME), _gitlet);

        File commitFile = new File(".gitlet/.commit/" + toCommit.getName());
        writeObject(commitFile, toCommit);
    }

    /** iterate through the current and given files finding newly
     * created ones.*/
    void iterateThroughCurandGiven() throws IOException {
        Set<String> givenBranchFiles = _givenB.getFiles().keySet();
        Set<String> curBranchFiles = _curB.getFiles().keySet();
        Set<String> splitFiles = _split.getFiles().keySet();

        TreeMap<String, String> gBmap = _givenB.getFiles();
        TreeMap<String, String> cBmap = _curB.getFiles();
        TreeMap<String, String> smap = _split.getFiles();

        for (String f : curBranchFiles) {
            if (!splitFiles.contains(f) && !givenBranchFiles.contains(f)) {
                continue;
            } else if (givenBranchFiles.contains(f)
                    && !splitFiles.contains(f)) {
                Blob current = Utils.readObject(new File(Init.BLOB_DIR
                        + "/" + cBmap.get(f)), Blob.class);
                Blob given = Utils.readObject(new File(Init.BLOB_DIR
                        + "/" + gBmap.get(f)), Blob.class);
                resolveConflict(current.getContents(), given.getContents(), f);
            }
        }
        for (String gf : givenBranchFiles) {
            if (!splitFiles.contains(gf) && !curBranchFiles.contains(gf)) {
                File checkout = new File(gf);
                File ckbf = new File(Init.BLOB_DIR + "/" + gBmap.get(gf));
                Blob checkoutBlob = Utils.readObject(ckbf, Blob.class);
                Utils.writeContents(checkout, checkoutBlob.getContents());
                _stage.addName(gf, "modified");
                _stage.removedFiles().remove(gf);
                File stageadd = new File(Init.STAGE_DIR + "/" + gf);
                Utils.writeContents(stageadd, checkoutBlob.getContents());
            }
        }
    }
}
