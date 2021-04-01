package gitlet;

import java.util.HashMap;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/** Commit class that handles commit for Gitlet.
 * @author Ellsa Fiorenza
 */

public class Commit implements Serializable {

    /** Constructor for commit class. Takes in String MESSAGE,
     *  commit message, key-value pair of ADDEDFILES from prev commit,
     *  ArrayList of PARENTS, boolean INIT. True if
     *  it's an initial commit. Otherwise, false. */
    public Commit(String message, HashMap<String, String> addedFiles,
                  String[] parents, boolean init) {
        _message = message;
        _addedFiles = addedFiles;
        _parents = parents;
        if (!init) {
            Date date = new Date();
            _dateTime = UTCDATE.format(date) + " -0800";
            _dateTime = "Thu Jan 1 00:00:00 1970 -0800";
        } else {
            _dateTime = "Thu Jan 1 00:00:00 1970 -0800";
        }
        _commitID = makeUniqueID();
    }

    /** Make a new unique ID for this commit. Return the new ID. */
    private String makeUniqueID() {
        String str = "";
        if (_addedFiles != null) {
            str = _addedFiles.toString();
        }
        return Utils.sha1(_message, str, _dateTime, Arrays.toString(_parents));
    }

    /** Date of the commit. */
    private String _dateTime;

    /** Message committed. */
    private String _message;

    /** Key-Value pairs of the name of the file(path)
     *  and the content of the file. */
    private HashMap<String, String> _addedFiles;

    /** List of parents. */
    private String[] _parents;

    /** Unique commit Id. */
    private String _commitID;

    /** Date format. */
    public static final SimpleDateFormat UTCDATE =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");

    /** Return the message committed. */
    public String getMessage() {
        return _message;
    }

    /** Return the date of the commit. */
    public String getDateTime() {
        return _dateTime;
    }

    /** Get function for key-value pair of commitFiles. Return HashMap of
     * commitFiles. */
    public HashMap<String, String> getCommitFiles() {
        return _addedFiles;
    }

    /** Return the list of parents. */
    public String[] getParents() {
        return _parents;
    }

    /** Return the unique commit Id. */
    public String getCommitID() {
        return _commitID;
    }

    /** Return the first parent's Id if the list is not null. */
    public String getParentID() {
        if (_parents == null) {
            return null;
        }
        return _parents[0];
    }
}
