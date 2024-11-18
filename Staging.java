package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class Staging implements Serializable {

    /** Create a new staging area with the absolute PATH of the
     * working directory.*/
    public Staging() {
        _addition = new HashMap<String, String>();
        _removal = new HashSet<String>();
    }

    /** Returns true if the staging area for addition contains the file
     * with FILENAME. */
    public boolean addContains(String filename) {
        return _addition.containsKey(filename);
    }

    /** Returns true if the staging area for removal contains the file
     * with name FILENAME. */
    public boolean removeContains(String filename) {
        return _removal.contains(filename);
    }

    /** Stages the FILE for removal and unstages for addition. */
    public void stageRemoval(String file) {
        _removal.add(file);
        _addition.remove(file);
    }

    /** Stages the file with corresponding BLOB for addition and unstages
     *  for removal. */
    public void stageAddition(Blob blob) {
        _addition.put(blob.getFilename(), blob.getID());
        _removal.remove(blob.getFilename());
        writeObject(join(BLOBSDIR, blob.getID()), blob);
    }

    /** Unstages the file with NAME for addition. */
    public void unStageR(String name) {
        _removal.remove(name);
    }

    /** Unstages the file with name FILENAME for addition. */
    public void unStageA(String filename) {
        if (!readObject(join(BLOBSDIR, additionGetID(filename)),
                Blob.class).isCommited()) {
            join(BLOBSDIR, additionGetID(filename)).delete();
        }
        _addition.remove(filename);
    }

    /** Returns the hash of FILE being tracked for addition. */
    public String additionGetID(String file) {
        return _addition.get(file);
    }

    /** Returns all SHA-1 hash identifiers for the files staged for addition. */
    public Collection<String> additionDump() {
        return _addition.keySet();
    }

    /** Returns all file names for the files staged for removal. */
    public Collection<String> removalDump() {
        return _removal;
    }

    public List<String> unmodified() {
        ArrayList<String> result = new ArrayList<String>();
        for (String file : _addition.keySet()) {
            Blob blob = new Blob(file,
                    readContentsAsString(new File(file)));
            if (blob.getID().equals(_addition.get(file))) {
                result.add(file);
            }
        }
        return result;
    }
    /** Returns if any files are staged for addition or removal. */
    public boolean anyStaged() {
        return !_addition.isEmpty() || !_removal.isEmpty();
    }
    /** The files to be added to the next commit. The keys are names of files
     * and values are SHA-1 hash IDs. */
    private HashMap<String, String> _addition;
    /** The files to be removed from the next commit; filenames. */
    private HashSet<String> _removal;

}

