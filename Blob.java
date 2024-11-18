package gitlet;

import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {

    /** Creates a new blob with @ParamTag CONTENT and FILENAME. */
    public Blob(String fileName, String content) {
        _file = fileName;
        _contents = content;
        _isCommited = false;
    }

    /** Creates a blob from two other blobs; FILENAME is the name of the
     *  filename and CURRENT and TARGET are different versions of the
     *  same file in this commit. */
    public Blob(String filename, Blob current, Blob target) {
        _file = filename;
        _contents = String.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                current.getContents(), target.getContents());
        _isCommited = true;
    }

    /** Creates a blob from two other blobs; FILENAME is the name of the
     *  filename and CURRENT and TARGET are different versions of the
     *  same file referenced by the current and target commits in a merge. */
    public Blob(String filename, String current, String target) {
        this(filename, readObject(join(BLOBSDIR, current), Blob.class),
                readObject(join(BLOBSDIR, target), Blob.class));
    }

    /** Returns this blob's filename. */
    public String getFilename() {
        return _file;
    }

    /** Returns this blob's contents. */
    public String getContents() {
        return _contents;
    }

    /** Returns this blob's SHA-1 hash ID. */
    public String getID() {
        return sha1(_file + _contents);
    }

    /** Changes _commited to true. */
    public void commit() {
        _isCommited = true;
    }

    /** Returns true if this blob is referenced by a commit. */
    public boolean isCommited() {
        return _isCommited;
    }

    /** Stores this blob's filename. */
    private String _file;

    /** Stores this blob's contents. */
    private String _contents;

    /** Tells whether or not this blob has already been
     * included in a commit or is just staged. */
    private boolean _isCommited;

}

