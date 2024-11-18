# Gitlet Design Document
**author**: Jiroum Masoudi

## Classes and Data Structures

###Repository
This object keeps track of the Gitlet repository; used for persistance.
####Fields
1. private String _currentBranch: Name of the currently active branch.

###Commit
This class represents a single Gitlet commit. Created each time we want to make a new commit, and stored for later use.
####Fields
1. private String _time: Represents  the time stamp when the commit was created.
2. private String _message: Stores the message associated with this commit.
3. private TreeMap<String, String> _blobs: The blobs associated with this commit; keys are blob IDs and values
are original filenames.
4. private HashSet<String> _versions: Contains the blob Ids tracked by this commit. Used to efficiently check if this commit is tracking a certain
version of a file. 
5. private String _parent: Commit ID of this commit's parent.

###Merge
A special type of commit created when using the merge command. This
type of commit is unique in that it has two parents.
####Fields
1. private String _first: The commit ID for the commit that was merged into from the merge command 
2. private String _second: The commit ID for the commit that was merged with from the merge
###Branch
####Fields
1. private String _name: The name of this branch. 
2. private String _head: ID of the commit which is the head of this branch.
###Blob
####Fields
An object abstraction of the files stored in the git repository
1. private String _fileName: The file name this blob refers to.
2. private String _contents: The contents of the file version associated to this blob. 
3. private boolean _isCommitted: Stores whether this blob is tracked by any commits.

###StagingArea
####Fields
1. private TreeMap<String, String> _addition: Tracks files to be added in the next commit; keys are the
filenames and values are the SHA-1 hash ID of the most recent blob staged for addition. 
2. private TreeSet<String> _removal: Stores the filenames of the files to be removed in the next commit.

## Algorithms
###Main
1. public void checkInit(): Checks to see if a repository already exists. If one doesn't already it returns the following output:
`Not in an initialized Gitlet directory.`

2. public void init(): Creates the initial required filestructure for gitlet in a hidden folder.
Then creates a new gitlet repository object inside the folder.
Gives the following error message if a gitlet repository already exists in this directory:
`A Gitlet version-control system already exists in the current directory.`

3. public void add(String file): Searches the staging area's `_blobs` to check if the exact same version of the file
is already there. If not, adds it to the staging area. Also checks the staging area
to see if this file would be removed, which it should change if it is.

###Repository
1. public Repository():Creates a new repository and the file structure for a gitlet repository.
Also calls the initObjects method to create all the necessary objects. 
2. public void initObjects(): Creates all the required objects for the repository to function properly
(initial commit, staging area and master branch).
3. public Branch getCurrent(): Returns the branch of the current active branch. 
4. public Commit lastCommit(): Returns the most recent commit from the active branch. 
5. public void add(String file): First loads the staging area object. Unstages the file for removal (if it was staged for it) then
performs appropriate behavior based on whether file is staged or not and modified.

*Behavior:*
1. File is already staged and same as currently staged version
   \
   _Nothing happens_
2. File is already staged but contents are modified
   \
   _Remove older staged version and stage new version of this file_
3. File is not already staged and has same contents as most recent commit
   \
   _Nothing happens_
4. File is not already staged and contents have been modified since most recent commit
   \
   _New file is staged for addition_


6. public void remove(String file)
If the file is being tracked in the staging area it is removed from the list of files
staged for addition. If the file is tracked in the most recent commit it is added to the list
of files to be staged for removal (not to be tracked by next commit). If the file is present in the working directory it is deleted, although only if
it passes one of the two previous conditions.

7. public void commit(String message): Create a new commit object with the given message, and parent commit corresponding
to the most recent in the active branch. Then set up
this commit's tracking by:
   1. Iterating over files staged for addition, adding them to my
   _blobs
   2. Iterating over files in my parent's commit, adding all files whose name don't match any filenames staged for removal and aren't already being tracked from
step 1
   3. After that the staging area is cleared, and the active branch's
_head is updated to the new commit ID.

8. public void status(): Iterate over branches, adding them to output, and marks the active branch with a "*". Next
iterates over the files that are staged for addition, adding them in the output only if
its SHA-1 hash ID matches the one saved in our staging area. This way
we know our file is unmodified from the version we staged for addition. After that
iterate over files for removal and add them to the output. For the remaining two sections
- Modifications not staged for commit: need some way to iterate over both files
  staged for addition and files in the last commit to check if they've been modififed or
  deleted, add corresponding message, and keep them alphabetized.
- Untracked: iterate over files in the current directory and find which are not tracked in last commit
  and also not staged for addition.


9. public void log(): Starts at the active branch's head commit and iteratively
print the commit's ID, data and message, as long as the commit
has a parent.

10. public void globalLog(): Prints every commit ever made in this repository. Iterates over every
commit object in the Commits directory and prints out their information.

11. public void checkout(String file): Loads the most recent commit object and finds the blob with the correct filename.
Then writes a new file in the current directory with that blob's filename and contents.
If a file with this filename was staged for addition, it now becomes unstaged.

12. public void checkout(String filename, String commit): Loads the commit object file named *commitId* and finds its blob with matching filename.
Then loads that blob and writes a new file with named *filename* with the contents of this blob.

13. public void checkoutBranch(String branch): Calls the restore method on the head commit of the given branch. Also clears the staging
area and sets the active branch to be the given branch.

14. public void restore(Commit commit): Clears all files tracked by the most recent commit, using the commit's
clearTracked method, then creates copies of all the files tracked by the given]
commit using the commit's checkoutAll method.

15. public void branch(String name): Creates a new branch object in the Branches directory with the given name,
whose head is the most recent commit.

16. public void rmBranch(String branch): Deletes the branch object corresponding to branch from the Branches directory.


17. public void reset(String commit):Checks out all the files tracked by the commit with corresponding
`commit` and removes tracked files that are not present in that commit using the
restore method. Then clears the staging area and sets the current branch's head to the
`commit`.


18. public void merge(String other): Finds the common ancestor of the last commit and the other commit using
the active branch's ancestor method. 
Also clears the staging area and updates the head of the current branch to this
new mergecommit. Employs the use of a helper method to pass CS16B style check method length requirement.

###Branch
1. public Branch(String name, Commit head): Creates a new Branch named name that points to the active branch's head node

2. public String getName(): Returns the _name field.

3. public String getHead(): Returns the _head field.

4. public void setHead(String commit): Changes the _head field to the given commit ID.

5. public String ancestor(Branch other): Performs a breadth first graph traversal of all the nodes reachable by the head
commits from both this branch and the other branch, returning the closest
commit reachable by both the heads.

###Commit
1. public Commit(String message, Commit parent): Constructor for the commit class, storing the message and parent commit by its ID as a String.

2. public String getMessage(): Returns the _message field.

3. public String getTime(): Returns the _time field.

4. public String getID(): Returns the SHA-1 hash for this commit in hexadecimal.

5. public boolean hashTrack(String hash): Checks if the given hash is contained in the `_versions` field.

6. public boolean tracking(String filename):
Checks if the given filename is tracked in the _blobs key set.

7. public boolean modded(Blob blob): Returns true if the given blob has the same filename but different contents from the one
tracked by this commit.

8. public Commit getParent(): Returns the commit object corresponding to this commit's parent ID.

9. public String getHash(String filename): Returns the hash of the filename in the `_blobs` field.

10. public void add(Blob blob): Adds the filename and blobId corresponding to the given blob to the `_blobs` and `_versions`
fields.

11. public void updateTracking(): Handles the files staged for addition to this new commit. For each file
in the staging area directory:
     1. add ID and name to _blobs 
     2. add parent's blobs to my _blobs if they're not staged for removal and if they're not already
    being tracked from step 1.
     3. clear the staging area

12. public void checkout(String hash): Writes the contents of the blob object corresponding to hash into a file with that blob's filename
in the current directory.

13. public void checkoutAll(): Iterates through the blob IDs tracked by this commit and calls the checkout method on each. 

14. public boolean inTheWay(Commit last): Iterates through files tracked by this commit and checks to see if any
would be deleted or overwritten by a merge.

15. public void clearTracked(): Deletes all files with the same filename as those tracked by this commit
from the working directory.

16. public String toString(): Includes the commit's ID, timestamp, and message

17. public Collection<String> hashes():Returns the _versions field for use in other class methods.

###Merge
1. public Merge(Branch current, Branch target): Creates a new mergecommit object, giving it the current timestamp
and setting its first and second parents.

2. public void updateTracking(Commit other): Iteratively updates this commit so it tracks the exact same blobs tracked by the other commit.

3. public void add(Blob blob): Adds this file to the list of blobs tracked by this commit, throwing an error
if there's an untracked version of the blob in the current directory that would be
deleted.

4. public void add(String blobId): Calls the add method with the blob corresponding to blobId.

5. public String getId(): Returns an ID essentially the same as that of a regular commit, but uses both parents for generating.

6. public String toString():Essentially the same behavior as a regular commit's toString method, only includes
both parents and indicates that this commit was generated from a merge.

7. public String getFirst(): Returns the `_first` field.

8. public String getSecond(): Returns the `_second` field.

###InitialCommit
1. public InitialCommit(): Generates an initial commit, calling the parent constructor with the current time and a message reading,
"initial commit."

2. public String getId(): Generates this commits ID using its message and timestamp only.

3. public Commit getParent(): Returns null as initial commits don't have any parents.

4. public boolean checkTrack(Commit recent): Returns false if no files are tracked by the initial commit.

###Blob
1. public Blob(String filename, String contents): Constructor used for a regular blob object, storing the filename and contents in a blob object

2. public Blob(String filename, Blob current, Blob target): Constrcutor used for conflicts in a merge, where the contents of this blob
includes the contents of both the current and target blobs, with the given filename.

3. public String getContents(): Returns the contents  of this blob.

4. public String getFilename():Returns this blob's filename. 

5. public String getId(): Returns this blob's SHA-1 ID.

6. public boolean isCommited(): Returns the blob's `_commited` field.

## Persistence
###File Structure

.gitlet
    ├──Objects
    │    ├──repository
    │    └──stagingArea
    │
    ├──Branches
    │    └── <branch objects>
    │
    ├──Blobs
    │    └── <blob objects>
    │
    └──Commits
         └── <commit objects>

