package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;

/** Log command class that is able to access my commit objects
 * and print out their logs.
 * @author Alice Wang
 */
public class Log implements Serializable {

    /** my current gitlet object. */
    private Gitlet _gitlet;

    /** Constructor for my Log command class, which loads
     * in my current gitlet object.
     */
    Log() {
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

    /** prints out the logs of all of my commit Objects. */
    void doLog() {
        CommitObject cO = _gitlet.getHead(_gitlet.getcurBranch());
        while (cO != null) {
            String log = cO.getLog();
            System.out.println(log);
            cO = cO.getParent();
        }
    }

}
