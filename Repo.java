package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Formatter;
import static gitlet.Utils.*;

/** The Repo Class executes all the text commands and implements logic for
 * Gitlet.
 * @author Jiroum Masoudi */


public class Repo implements Serializable {

    /** Creates a new repository object, which stores the absolute PATH. */
    public Repo() {
        _active = "master";

        join(".", ".gitlet/Blobs").mkdir();
        join(".", ".gitlet/Branches").mkdir();
        join(".", ".gitlet/Commits").mkdir();
        for (int i = 0; i < 256; i++) {
            join(COMMITSDIR, twoHexDec(i)).mkdir();
        }
        initObjects();
    }
    /** Adds a copy of the FILE as it currently exists to the staging area.
     *  Staging a file that already exists in the staging area overwrites it.
     *  If the current version is identical to the version in the commit
     *  remove it from staging for commit.The file will be unstaged for removal
     *  if it was staged at the time of command. */

    public void add(String file) {
        Staging stage = readObject(new File(STAGEOBJ), Staging.class);
        Commit commit = lastCommit();
        File f = new File(file);
        if (!f.exists()) {
            throw error("File does not exist.");
        }
        Blob blob = new Blob(file, readContentsAsString(f));
        if (!stage.addContains(file) && !commit.trackingHash(blob.getID())) {
            stage.stageAddition(blob);
        } else if (stage.addContains(file)
                && !commit.trackingHash(blob.getID())) {
            join(BLOBSDIR, stage.additionGetID(file)).delete();
            stage.stageAddition(blob);
        } else if (stage.addContains(file)
                && commit.trackingHash(blob.getID())) {
            stage.unStageA(file);
        } else {
            stage.unStageR(blob.getFilename());
        }
        writeObject(join(STAGEOBJ), stage);

        writeObject(join(commitFile(commit.getID())), commit);
    }
    /** Generates the initial commit, staging area, and master branch
     *  objects. */
    public void initObjects() {
        Commit initial = new InitialCommit();
        Branch master = new Branch(_active, initial);
        Staging stage = new Staging();
        master.setHead(initial.getID());
        writeObject(commitFile(initial.getID()), initial);
        writeObject(join(BRANCHESDIR, master.getName()), master);
        writeObject(new File(STAGEOBJ), stage);
    }

    /** Returns the active branch Object. */
    public Branch getActive() {
        return readObject(join(BRANCHESDIR, _active), Branch.class);
    }

    /** Returns the most recent commit from the active branch. */
    public Commit lastCommit() {
        Branch active = getActive();
        return readObject(commitFile(active.getHead()), Commit.class);
    }

    /** Creates a new commit object with MESSAGE that tracks all the files
     *  currently in the staging area, while removing tracking from any files
     *  that were removed. */
    public void commit(String message) {
        Staging stage = readObject(new File(STAGEOBJ), Staging.class);
        if (stage.additionDump().isEmpty() && stage.removalDump().isEmpty()) {
            throw error("No changes added to the commit.");
        } else {
            Commit newCommit = new Commit(message, lastCommit());
            Branch activeBranch = readObject(join(BRANCHESDIR, _active),
                    Branch.class);
            newCommit.updateTracking(stage);
            activeBranch.setHead(newCommit.getID());
            writeObject(commitFile(newCommit.getID()), newCommit);
            writeObject(join(BRANCHESDIR, _active), activeBranch);
            writeObject(new File(STAGEOBJ), new Staging());
        }
    }

    /** Removes the FILE from the collection of files that the next commit will
     *  track, and deletes it from the working directory. */
    public void remove(String file) {
        Staging stage = readObject(new File(STAGEOBJ), Staging.class);
        Commit head = lastCommit();
        if (!stage.addContains(file) && !head.tracking(file)) {
            throw error("No reason to remove the file.");
        } else if (stage.addContains(file)) {
            stage.unStageA(file);
        }
        if (head.tracking(file)) {
            stage.stageRemoval(file);
            restrictedDelete(file);
        }
        writeObject(new File(STAGEOBJ), stage);
    }

    /** Prints out the log of all commits on the currently active branch this
     *  repository. */
    public void log() {
        Commit commit = lastCommit();
        Formatter output = new Formatter();
        while (commit != null) {
            output.format(commit.toString());
            commit = commit.getParent();
            if (commit != null) {
                output.format("\n\n");
            }
        }
        System.out.println(output);
    }

    /** Prints out the log of commits on all branches in this repository.*/
    public void globalLog() {
        Formatter output = new Formatter();
        for (int i = 0; i < 256; i++) {
            String dir = twoHexDec(i);
            for (String commitName : plainFilenamesIn(join(COMMITSDIR, dir))) {
                Commit commit = readObject(join(COMMITSDIR, dir, commitName),
                        Commit.class);
                output.format(commit.toString() + "\n\n");
            }

        }
        System.out.println(output);
    }
    /** Prints out all commits that have this MESSAGE. */
    public void find(String message) {
        Formatter output = new Formatter();
        for (int i = 0; i < 256; i++) {
            String dir = twoHexDec(i);
            for (String commitId : plainFilenamesIn(join(COMMITSDIR, dir))) {
                Commit commit = readObject(join(COMMITSDIR, dir, commitId),
                        Commit.class);
                if (commit.getMessage().equals(message)) {
                    output.format("%s\n", commit.getID());
                }
            }
        }
        if (output.toString().isEmpty()) {
            throw error("Found no commit with that message.");
        } else {
            System.out.println(output);
        }
    }
    /** Displays what branches currently exist, and marks the current branch
     *  with a *. Also displays what files have been staged for addition
     *  or removal.*/
    public void status() {
        Staging stg = readObject(new File(STAGEOBJ), Staging.class);
        Commit last = lastCommit();
        Formatter output = new Formatter();
        output.format("=== Branches ===\n");
        for (String branch : plainFilenamesIn(BRANCHESDIR)) {
            if (branch.equals(_active)) {
                output.format("*");
            }
            output.format("%s \n", branch);
        }
        output.format("\n=== Staged Files ===\n");
        for (String filename : stg.unmodified()) {
            output.format("%s \n", filename);
        }
        output.format("\n=== Removed Files ===\n");
        for (String filename : stg.removalDump()) {
            output.format("%s \n", filename);
        }
        output.format("\n=== Modifications Not Staged For Commit ===\n");
        output.format("\n=== Untracked Files ===\n");
        System.out.println(output);
    }

    /** Takes the version of the FILE as it exists in the head commit,
     *  the front of the current branch, and puts it in the working directory,
     *  overwriting the version of the file that's already there if there is
     *  one. The new version of the file is not staged. */
    public void checkoutFile(String file) {
        checkoutFile(lastCommit().getID(), file);
    }

    /** Takes the version of the file named FILENAME as it exists in the
     *  commit corresponding to COMMITID and puts it in the working directory,
     *  overwriting the version of the file that's already there if there is
     *  one. The new version of the file is not staged. */
    public void checkoutFile(String commitId, String filename) {
        if (!commitFile(commitId).exists()) {
            throw error("No commit with that id exists.");
        }
        Commit commit = readObject(commitFile(commitId), Commit.class);
        Staging stg = readObject(new File(STAGEOBJ), Staging.class);
        if (!commit.tracking(filename)) {
            throw error("File does not exist in that commit.");
        } else if (stg.addContains(filename)) {
            stg.unStageA(filename);
        }
        commit.checkout(commit.hashOf(filename));
        writeObject(new File(STAGEOBJ), stg);
    }

    /** Takes all files in the commit at the head of the branch named BRANCHNAME
     *  and puts them in the working directory, overwriting the versions of the
     *  files that are already there if they exist. Also, at the end of this
     *  command, the given branch will now be considered the current branch.
     *  Any files that are tracked in the current branch but are not present
     *  in the checked-out branch are deleted. The staging area is cleared,
     *  unless the checked-out branch is the current branch. */
    public void checkoutBranch(String branchName) {
        if (!join(BRANCHESDIR, branchName).exists()) {
            throw error("No such branch exists.");
        } else if (branchName.equals(_active)) {
            throw error("No need to checkout the current branch.");
        }
        Branch branch = readObject(join(BRANCHESDIR, branchName), Branch.class);
        Commit target = readObject(commitFile(branch.getHead()), Commit.class);
        if (target.inTheWay(lastCommit())) {
            throw error("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        } else {
            restore(target);
            writeObject(new File(STAGEOBJ), new Staging());
            _active = branchName;
        }
    }

    /** Creates a new branch with the given NAME and points it at the current
     *  head node. A branch is nothing more than a name for a reference
     *  (a SHA-1 identifier) to a commit node. We don't immediately switch
     *  to the newly created branch. */

    public void branch(String name) {
        if (join(BRANCHESDIR, name).exists()) {
            throw error("A branch with that name already exists.");
        }
        Branch newBranch = new Branch(name, lastCommit());
        writeObject(join(BRANCHESDIR, name), newBranch);
    }

    /**  Deletes the branch with name BRANCHNAME. This only means to delete the
     *  pointer associated with the branch; it does not mean to delete all
     *  commits that were created under the branch, or anything like that. */
    public void rmBranch(String branchName) {
        if (!join(BRANCHESDIR, branchName).exists()) {
            throw error("A branch with that name does not exist.");
        } else if (_active.equals(branchName)) {
            throw error("Cannot remove the current branch.");
        } else {
            join(BRANCHESDIR, branchName).delete();
        }
    }

    /** Checks out all the files tracked by the commit with corresponding
     *  COMMITID. Removes tracked files that are not present in that commit.
     *  Also moves the current branch's head to that commit node. The id
     *  may be abbreviated as for checkout. The staging area is cleared.
     *  The command is essentially checkout of an arbitrary commit that also
     *  changes the current branch head. */
    public void reset(String commitId) {
        if (!commitFile(commitId).exists()) {
            throw error("No commit with that id exists.");
        }
        Commit target = readObject(commitFile(commitId), Commit.class);
        if (target.inTheWay(lastCommit())) {
            throw error("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        }
        restore(target);
        writeObject(new File(STAGEOBJ), new Staging());
        Branch branch = readObject(join(BRANCHESDIR, _active), Branch.class);
        String identifier = commitFile(commitId).getParentFile().getName()
                + commitFile(commitId).getName();
        branch.setHead(identifier);
        writeObject(join(BRANCHESDIR, _active), branch);
    }

    /** Given a COMMIT, restore clears working directory and
     * places files pointing to blobs from that commit in CWD. */
    public void restore(Commit commit) {
        lastCommit().clearTracked();
        commit.checkoutAll();
    }

    /** Merges files from the OTHER branch into the current branch. */
    public void merge(String other) {
        if (!join(BRANCHESDIR, other).exists()) {
            throw error("A branch with that name does not exist.");
        } else if (other.equals(_active)) {
            throw error("Cannot merge a branch with itself.");
        }
        Staging stg = readObject(new File(STAGEOBJ), Staging.class);
        Branch activeBranch = readObject(join(BRANCHESDIR, _active),
                Branch.class);
        Commit current = lastCommit();
        Branch targetBranch = readObject(join(BRANCHESDIR, other),
                Branch.class);
        Commit target = getCommit(targetBranch.getHead());
        if (stg.anyStaged()) {
            throw error("You have uncommitted changes.");
        }
        Commit split = getCommit(activeBranch.commonAncestor(targetBranch));
        Merge mergeTing = new Merge(activeBranch, targetBranch);
        if (split.getID().equals(target.getID())) {
            System.out.println("Given branch is an ancestor of the current"
                    + " branch.");
            mergeTing.updateTracking(current);
        } else if (split.getID().equals(current.getID())) {
            mergeTing.updateTracking(target);
            restore(target);
            System.out.println("Current branch fast-forwarded.");
        } else {
            mergeTing = mergeHelper(split, current, target, mergeTing);
        }
        activeBranch.setHead(mergeTing.getID()); mergeTing.checkoutAll();
        writeObject(commitFile(mergeTing.getID()), mergeTing);
        writeObject(join(BRANCHESDIR, activeBranch.getName()), activeBranch);
    }

    /** This method is a helper for the merge method. Given the SPLIT, CURRENT,
     * and TARGET commits, along with the MERGETING, it finds the files that
     * should be kept or deleted, and returns the finalized MERGETING. */
    public Merge mergeHelper(Commit split, Commit current,
                                   Commit target, Merge mergeTing) {
        ArrayList<File> toDelete = new ArrayList<>(); boolean conflict = false;
        for (String hash : current.hashes()) {
            Blob disBlob = readObject(join(BLOBSDIR, hash), Blob.class);
            String file = disBlob.getFilename();
            if (target.modified(disBlob) && split.tracking(file)) {
                mergeTing.add(target.hashOf(file));
            } else if (!split.tracking(file)
                    && target.modified(disBlob)) {
                Blob datBlob = new Blob(file, current.hashOf(file),
                        target.hashOf(file));
                mergeTing.add(datBlob); conflict = true;
                writeObject(join(BLOBSDIR, datBlob.getID()), datBlob);
            } else if (!split.tracking(file)) {
                mergeTing.add(hash);
            } else if (!target.tracking(file) && split.tracking(file)) {
                toDelete.add(new File(file));
            }
        }
        for (String hash : target.hashes()) {
            Blob targBlob = readObject(join(BLOBSDIR, hash), Blob.class);
            String file = targBlob.getFilename();
            if (current.modified(targBlob) && split.tracking(file)) {
                mergeTing.add(current.hashOf(file));
            } else if (!current.tracking(file) && !split.tracking(file)) {
                mergeTing.add(targBlob);
            }
        }
        for (String hash : split.hashes()) {
            Blob blob = readObject(join(BLOBSDIR, hash), Blob.class);
            String file = blob.getFilename();
            if (current.modified(blob) && target.modified(blob)) {
                if (current.hashOf(file).equals(target.hashOf(file))) {
                    mergeTing.add(current.hashOf(file));
                } else if (current.tracking(file) && target.tracking(file)) {
                    Blob newBlob = new Blob(file, current.hashOf(file),
                            target.hashOf(file));
                    mergeTing.add(newBlob); conflict = true;
                    writeObject(join(BLOBSDIR, newBlob.getID()), newBlob);
                }
            } else if (current.modified(blob) && !target.tracking(file)) {
                Blob conflictBlob = new Blob(file,
                        getBlob(current.hashOf(file)), new Blob(file, ""));
                mergeTing.add(conflictBlob); conflict = true;
                writeObject(join(BLOBSDIR, conflictBlob.getID()), conflictBlob);
            } else if (target.modified(blob) && !current.tracking(file)) {
                Blob conflictBlob = new Blob(file, new Blob(file, ""),
                        getBlob(target.hashOf(file)));
                mergeTing.add(conflictBlob); conflict = true;
                writeObject(join(BLOBSDIR, conflictBlob.getID()), conflictBlob);
            }
        }
        for (File file : toDelete) {
            restrictedDelete(file);
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        return mergeTing;
    }
    /** The currently active branch for this repository. */
    private String _active;
}
