package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author ALice Wang
 * Collaborators:  Andrew Liu
 * Kevin Chai
 * Henry Cheung
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length <= 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (args.length > 0) {
            if (args[0].equals("init")) {
                Init init = new Init();
                init.doInit();
            } else if (args[0].equals("add")) {
                parseAdd(args);
            } else if (args[0].equals("commit")) {
                parseCommit(args);
            } else if (args[0].equals("log")) {
                parseLog(args);
            } else if (args[0].equals("checkout")) {
                parseCheckout(args);
            } else if (args[0].equals("global-log")) {
                parseGlobalLog(args);
            } else if (args[0].equals("branch")) {
                parseBranch(args);
            } else if (args[0].equals("rm-branch")) {
                parseRMbranch(args);
            } else if (args[0].equals("find")) {
                parseFind(args);
            } else if (args[0].equals("status")) {
                parseStatus(args);
            } else if (args[0].equals("rm")) {
                parseRM(args);
            } else if (args[0].equals("reset")) {
                parseReset(args);
            } else if (args[0].equals("merge")) {
                parseMerge(args);
            } else {
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
        }
    }

    /** Parses through the add command with ARGS as STRING[].*/
    static void parseAdd(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Incorrect Operands");
        } else if (args.length == 2) {
            Add add = new Add(args[1]);
            add.doAdd();
        } else {
            System.out.println("Incorrect Operands");
        }
    }
    /** Parses through the commit command with ARGS as STRING[].*/
    static void parseCommit(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Please enter a commit message");
            System.exit(0);
            if (args[1].isEmpty() || args[1].equals("")) {
                System.out.println("Please enter a commit message");
                System.exit(0);
            }
        } else if (args.length == 2) {
            if (args[1].isEmpty() || args[1].equals("")) {
                System.out.println("Please enter a commit message");
                System.exit(0);
            }
            CommitCommand commit = new CommitCommand(args[1]);
            commit.doCommit();
        } else {
            System.out.println("Incorrect Operands");
            System.exit(0);
        }
    }
    /** Parses through the Log command with ARGS as STRING[].*/
    static void parseLog(String[] args) throws IOException {
        if (args.length == 1) {
            Log log = new Log();
            log.doLog();
        } else {
            System.out.println("Incorrect Operands");
        }
    }
    /** parses through the checkout command with ARGS as STRING[].*/
    static void parseCheckout(String[] args) {
        if (args.length < 2) {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        } else if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect Operands.");
                System.exit(0);
            }
            Checkout checkout = new Checkout();
            checkout.doCheckoutFile(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect Operands.");
                System.exit(0);
            }
            Checkout checkout = new Checkout();
            checkout.doCheckoutCommitFile(args[1], args[3]);
        } else if (args.length == 2) {
            Checkout checkout = new Checkout();
            checkout.doCheckoutBranch(args[1]);
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the global log command with ARGS as STRING[].*/
    static void parseGlobalLog(String[] args) {
        if (args.length > 1) {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
        GlobalLog gl = new GlobalLog();
        gl.doGlobalLog();
    }
    /** parses through the branch command with ARGS as STRING[].*/
    static void parseBranch(String[] args) {
        if (args.length == 2) {
            BranchCommand branch = new BranchCommand();
            branch.doBranch(args[1]);
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the remove branch command with ARGS as STRING[].*/
    static void parseRMbranch(String[] args) {
        if (args.length == 2) {
            BranchCommand rmbranch = new BranchCommand();
            rmbranch.doRMBranch(args[1]);
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the find command with ARGS as STRING[].*/
    static void parseFind(String[] args) {
        if (args.length == 2) {
            Find find = new Find(args[1]);
            find.doFind();
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** parses through the status command with ARGS as STRING[].*/
    static void parseStatus(String[] args) {
        if (args.length == 1) {
            Status status = new Status();
            status.doStatus();
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the remove command with ARGS as STRING[]. */
    static void parseRM(String[] args) {
        if (args.length == 2) {
            RM remove = new RM(args[1]);
            remove.doRM();
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the reset command.with ARGS as STRING[].*/
    static void parseReset(String[] args) {
        if (args.length == 2) {
            Reset reset = new Reset(args[1]);
            reset.doReset();
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
    /** Parses through the merge command with ARGS as STRING[].*/
    static void parseMerge(String[] args) throws IOException {
        if (args.length == 2) {
            Merge merge = new Merge(args[1]);
            merge.doMerge();
        } else {
            System.out.println("Incorrect Operands.");
            System.exit(0);
        }
    }
}
