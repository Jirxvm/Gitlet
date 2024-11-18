package gitlet;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class InitialCommit extends Commit {

    /** The inital commit constructor. */
    public InitialCommit() {
        super(MESSAGE, TIME);
    }

    /** Returns the hexadecimal ID string associated with this commit.
     *  Generates the ID by calling SHA-1 hash function on this commit's
     *  timestamp and message*/
    @Override
    public String getID() {
        return Utils.sha1(getMessage(), getTimestamp());
    }

    /** Returns null. */
    @Override
    public Commit getParent() {
        return null;
    }

    /** Since the initial commit tracks no files, none will ever be in the way;
     *  always should be false. */
    @Override
    public boolean inTheWay(Commit last) {
        return false;
    }

    /** This commit's parents' identifiers. */
    private static final Commit PARENT = null;

    /** The message associated with this commit. */
    private static final String MESSAGE = "initial commit";

    /** Time stamp formatter used for commits. */
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss uuuu Z");

    /** DEFAULT_YEAR used for format and to avoid magic number use. */
    public static final int DEFAULT_YEAR = 1970;

    /** The timestamp associated with this commit. */
    private static final String TIME = FORMAT.format(ZonedDateTime.of(
            DEFAULT_YEAR,
            1, 1, 0, 0, 0, 0,
            ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()));


}

