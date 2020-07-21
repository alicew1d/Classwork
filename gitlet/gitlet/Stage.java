package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** The class for Stage. Keeps track of all of my deleted,
 * modified, and new files from my working directory. Used as a reference
 * for when I am adding things. I also draw from the stage when committing
 * these items.
 * @author Alice Wang
 */
public class Stage implements Serializable {

    private static final long serialVersionUID = 88L;

    /** List of my new Files. */
    private List<String> _newFiles;

    /** List of my modified File names.  */
    private List<String> _modifiedFiles;

    /** List of my deleted File names. */
    private List<String> _rmFiles;

    /** Stage constructor for the stage class. Creates a new array list
     * for the file names in my Stage.
     */
    Stage() {
        _newFiles = new ArrayList<>();
        _modifiedFiles = new ArrayList<>();
        _rmFiles = new ArrayList<>();
    }

    /** returns the list of my new files.
     * @return List */
    List<String> newFiles() {
        return _newFiles;
    }

    /** returns the list of my modified files.
     * @return List */
    List<String> modifiedFiles() {
        return _modifiedFiles;
    }

    /** returns the list of my removed files.
     *
     * @return list.
     */
    List<String> removedFiles() {
        return _rmFiles;
    }

    /** checks if there is anything in my stage.
     * @return Boolean */
    boolean isNull() {
        return (_newFiles.isEmpty()
                && _modifiedFiles.isEmpty() && _rmFiles.isEmpty());
    }

    /** clears all of my Stage object lists. */
    void clear() {
        _newFiles.clear();
        _modifiedFiles.clear();
        _rmFiles.clear();
    }

    /** Adds the file name to either the new files or modified
     * files lists.
     * @param filename String
     * @param filetype String
     */
    void addName(String filename, String filetype) {
        if (filetype.equals("new")) {
            if (_rmFiles.contains(filename)) {
                _rmFiles.remove(filename);
            }
            _newFiles.add(filename);
        } else if (filetype.equals("modified")) {
            if (_rmFiles.contains(filename)) {
                _rmFiles.remove(filename);
            }
            _modifiedFiles.add(filename);
        } else if (filetype.equals("remove")) {
            _rmFiles.add(filename);
        }
    }

    /** adds all of my files to the staging directory. May be unnecessary
     * right now.
     * @throws IOException
     */
    void addtoDir() throws IOException {
        File dest = new File(".gitlet/.staging");
        if (!_newFiles.isEmpty()) {
            for (String file : _newFiles) {
                File toStage = new File(file);
                if (!toStage.exists()) {
                    throw new GitletException("File does not exist");
                }
                Files.copy(toStage.toPath(), dest.toPath());
            }
        }
        if (!_modifiedFiles.isEmpty()) {
            for (String file : _modifiedFiles) {
                File toStage = new File(file);
                if (!toStage.exists()) {
                    throw new GitletException("File does not exist");
                }
                Files.copy(toStage.toPath(), dest.toPath());
            }
        }
    }

    /**clear the directory in .staging. */
    void clearStageDir() {
        File[] stageFiles = new File(Init.STAGE_DIR).listFiles();
        for (File f : stageFiles) {
            if (!f.getName().equals("stageobject")) {
                CommitCommand.stagedDelete(f);
            }
        }
    }
}
