package gitlet;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import static gitlet.Utils.*;
import static gitlet.Utils.commitFile;

public class Branch implements Serializable {

    /** Create a new branch with NAME and HEAD. */
    public Branch(String name, Commit head) {
        _name = name;
        _head = head.getID();
    }

    /** Returns the name of this branch. */
    public String getName() {
        return _name;
    }

    /** Returns this branch's head commit. */
    public String getHead() {
        return _head;
    }

    /** Sets the head of this branch to COMMITID. */
    public void setHead(String commitID) {
        _head = commitID;
    }

    /** Returns the most recent common ancestor with the OTHER branch. */
    public String commonAncestor(Branch other) {
        TreeMap<String, Integer> master = new TreeMap<String, Integer>();
        TreeMap<Integer, String> target = new TreeMap<Integer, String>();
        Queue<String> remaining = new LinkedList<String>(); int distance = 0;
        master.put(getHead(), distance); remaining.add(getHead());
        while (!remaining.isEmpty()) {
            Commit commit = readObject(join(commitFile(remaining.remove())),
                    Commit.class);
            distance = master.get(commit.getID()) + 1;
            if (commit instanceof InitialCommit) {
                continue;
            } else if (commit instanceof Merge) {
                Merge mergeCommit = (Merge) commit;
                if (!master.containsKey(mergeCommit.getFirst())
                        || master.get(mergeCommit.getFirst()) > distance) {
                    master.put(mergeCommit.getFirst(), distance);
                    remaining.add(mergeCommit.getFirst());
                }
                if (!master.containsKey(mergeCommit.getSecond())
                        || master.get(mergeCommit.getSecond()) > distance) {
                    master.put(mergeCommit.getSecond(), distance);
                    remaining.add(mergeCommit.getSecond());
                }
            } else {
                if (!master.containsKey(commit.getParent().getID())
                        || master.get(commit.getParent().getID())
                        > distance) {
                    master.put(commit.getParent().getID(), distance);
                    remaining.add(commit.getParent().getID());
                }
            }
        }
        remaining.add(other.getHead());
        while (!remaining.isEmpty()) {
            Commit commit = readObject(join(commitFile(remaining.remove())),
                    Commit.class);
            if (commit instanceof Merge) {
                Merge mergecom = (Merge) commit;
                if (master.containsKey(mergecom.getFirst())) {
                    target.put(master.get(mergecom.getFirst()),
                            mergecom.getFirst());
                }
                if (master.containsKey(mergecom.getSecond())) {
                    target.put(master.get((mergecom.getSecond())),
                            (mergecom.getSecond()));
                }
                remaining.add(mergecom.getFirst());
                remaining.add(mergecom.getSecond());
            } else {
                if (master.containsKey(commit.getID())) {
                    target.put(master.get(commit.getID()),
                            commit.getID());
                }
                if (commit.getParent() != null) {
                    remaining.add(commit.getParent().getID());
                }
            }
        }
        return target.firstEntry().getValue();
    }

    /** The name of this branch. */
    private String _name;

    /** Stores the commit id of the head commit. */
    private String _head;


}
