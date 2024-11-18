package gitlet;

import java.io.File;

import static gitlet.Utils.*;
import static gitlet.Utils.writeObject;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jiroum Masoudi
 */
public class Main {

    /** Prompts for a repo to be initialized from ARGS if the repo doesn't
     * exist already. */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw error("Please enter a command.");
            } else if (args[0].equals("init")) {
                if (args.length == 1) {
                    init();
                } else {
                    throw error("Incorrect operands.");
                }
            } else if (!_repo.exists()) {
                throw error("Not in an initialized Gitlet directory.");
            } else {
                verify(args);
            }
        } catch (GitletException error) {
            message("%s", error.getMessage());
        }
        System.exit(0);
    }
    /** Method creates a new Git Repo object and creates the file structure.
     * If a repo already exists in the specific directory then it gives an
     * error */
    public static void init() {
        if (_repo.exists()) {
            throw error("A Gitlet version-control"
                    + " system already exists in the current directory.");
        } else {
            (new File(OBJECTSDIR)).mkdirs();
            writeObject(_repo, new Repo());
        }
    }
    /** Verifies that command in ARGS exists and has the appropriate number
     * of operands. Then executes that command. */
    public static void verify(String[] args) {
        Repo repo = Utils.readObject(_repo, Repo.class);
        if (args.length == 1) {
            oneArg(args[0]);
        } else if (args.length > 1 && args[0].equals("checkout")) {
            if (args.length == 2) {
                repo.checkoutBranch(args[1]);
            } else if (args.length == 3 && args[1].equals("--")) {
                repo.checkoutFile(args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                repo.checkoutFile(args[1], args[3]);
            } else {
                throw error("Incorrect operands.");
            }
        } else if (args.length == 2) {
            twoArgs(args[0], args[1]);
        } else {
            throw error("Incorrect operands.");
        }
        saveRepo(repo);
    }

    /** Processes length one arguments given INPUT. */
    public static void oneArg(String input) {
        Repo repo = Utils.readObject(_repo, Repo.class);
        switch (input) {
        case "commit":
            throw error("Please enter a commit message.");
        case "log":
            repo.log();
            break;
        case "global-log":
            repo.globalLog();
            break;
        case "status":
            repo.status();
            break;
        default:
            throw error("No command with that name exists.");
        }
        saveRepo(repo);
    }
    /** Processes commands with two words, FIRST and SECOND.*/
    public static void twoArgs(String first, String second) {
        Repo repo = Utils.readObject(_repo, Repo.class);
        switch (first) {
        case "add":
            repo.add(second);
            break;
        case "rm":
            repo.remove(second);
            break;
        case "branch":
            repo.branch(second);
            break;
        case "reset":
            repo.reset(second);
            break;
        case "rm-branch":
            repo.rmBranch(second);
            break;
        case "merge":
            repo.merge(second);
            break;
        case "find":
            repo.find(second);
            break;
        case "checkout":
            repo.checkoutBranch(second);
            break;
        case "commit":
            if (second.isEmpty()) {
                throw error("Please enter a commit message.");
            }
            repo.commit(second);
            break;
        default:
            throw error("No command with that name exists.");
        }
    }
    /** Serializes the repository object REPO. */
    public static void saveRepo(Repo repo) {
        Utils.writeObject(_repo, repo);
    }

    /** File that represents a repo object. */
    private static File _repo = new File(".gitlet/Objects/repoObj");
}
