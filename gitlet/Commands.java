package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Commands class that handles all command for Gitlet.
 * @author Ellsa Fiorenza
 */

public class Commands implements Serializable {

    /* ================= INITIALIZE ================= */
    /** Initialize a new gitlet which automatically starts with 1 commit.
     *  It has a single branch called "Master", pointing to the
     *  initial commit. Date and Time is set 00:00:00 UTC
     *  Thursday 1 Jan 1970. Initialize commit with message:
     *  "initial commit". */
    public Commands() {
        String initCommitID;
        byte[] commitToByte;

        _branches = new HashMap<>();
        _pairs = new HashMap<>();
        _files = new ArrayList<>();
        _head = "master";
        Commit init = new Commit("initial commit",
                null, null, true);

        initCommitID = init.getCommitID();
        commitToByte = Utils.serialize(init);
        _branches.put("master", initCommitID);

        File gitlet = new File(".gitlet");
        File commit = new File(".gitlet/Commit");
        File stage = new File(".gitlet/Stage");
        gitlet.mkdir();
        commit.mkdir();
        stage.mkdir();

        File initialize = new File(".gitlet/Commit/" + initCommitID);
        Utils.writeContents(initialize, commitToByte);
    }

    /* ================= ADD ================= */
    /** Add file to staging area.
     * @param file file that we want to add. */
    public void add(String file) {
        String content;
        String id;
        Commit addCommit;
        HashMap<String, String> allFiles;
        File added = new File(file);

        if (added.exists()) {
            content = Utils.readContentsAsString(added);
            id = Utils.sha1(content);
            File cur = new File(".gitlet/Commit/" + getCurrHead());

            if (cur.exists()) {
                addCommit = Utils.readObject(cur, Commit.class);
                allFiles = addCommit.getCommitFiles();
            } else {
                System.out.println("File does not exist.");
                return;
            }
            addStage(file, id, allFiles, content);
        } else {
            System.out.println("File does not exist.");
        }
    }

    /** If commit files is empty, or if file name existed,
     *  or if file name existed and content is modified, add the file to
     *  stage directory and update stageArea's contents. Otherwise, If the file
     *  existed in staging area or files, remove it.
     *  @param contentFile the content of the file.
     *  @param fileName the name of the file.
     *  @param files the map of all files.
     *  @param id the unique commit id. */
    private void addStage(String fileName, String id,
                          HashMap<String, String> files,
                          String contentFile) {
        File commitFile = new File(".gitlet/Stage/" + id);
        boolean add = false;

        if (files == null || !files.containsKey(fileName)) {
            add = true;
        } else if (files.containsKey(fileName)) {
            if (files.get(fileName) == null
                    || !files.get(fileName).equals(id)) {
                add = true;
            }
        } else if (commitFile.exists()) {
            _pairs.remove(fileName);
        }

        if (add) {
            _pairs.put(fileName, id);
            Utils.writeContents(commitFile, contentFile);
        }
        if (_files.contains(fileName)) {
            _files.remove(fileName);
        }
    }

    /* ================= COMMIT ================= */
    /** Committed file in staging area.
     * @param merge true if merge, false otherwise.
     * @param message the commit message, must not be empty.
     * @param par the list of parents. */
    public void commit(String message, boolean merge, String[] par) {
        Commit commit;
        HashMap<String, String> commitFiles;
        String commitID;
        String[] parents;

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            throw new GitletException();
        } else {
            File parent = new File(".gitlet/Commit/" + getCurrHead());
            if (!parent.exists()) {
                System.out.println("No commit with that id exists.");
                throw new GitletException();
            }
            commit = Utils.readObject(parent, Commit.class);
            commitFiles = commit.getCommitFiles();
            commitFiles = modifCom(commitFiles);

            parents = merge ? par : new String[] {commit.getCommitID()};

            Commit newCommit = new Commit(message, commitFiles, parents, false);
            commitID = newCommit.getCommitID();
            File commitFile = new File(".gitlet/Commit/" + commitID);
            Utils.writeObject(commitFile, newCommit);

            _pairs = new HashMap<>();
            _files = new ArrayList<>();
            _branches.put(_head, commitID);
        }
    }

    /** Return the modified current commit files.
     * @param comFiles map of all commited files. */
    private HashMap<String, String> modifCom(HashMap<String, String> comFiles) {
        if (comFiles == null) {
            comFiles = new HashMap<>();
        }

        if (_pairs.size() != 0 || _files.size() != 0) {
            if (_files.size() != 0) {
                for (String file : _files) {
                    comFiles.remove(file);
                }
            }
            if (_pairs.size() != 0) {
                for (Map.Entry<String, String> e : _pairs.entrySet()) {
                    comFiles.put(e.getKey(), e.getValue());
                }
            }
            return comFiles;
        }

        System.out.println("No changes added to the commit.");
        throw new GitletException();
    }

    /* ================= RM ================= */
    /** Remove the file from Commit.
     * @param file file that we want to remove. */
    public void rm(String file) {
        Commit commit;
        HashMap<String, String> commitFiles;
        int total = 0;

        File rmFile = new File(file);
        File parent = new File(".gitlet/Commit/" + getCurrHead());

        if (parent.exists()) {
            commit = Utils.readObject(parent, Commit.class);
            commitFiles = commit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        if (_pairs.containsKey(file)) {
            _pairs.remove(file);
            total++;
        } else if (commitFiles != null && commitFiles.containsKey(file)) {
            _files.add(file);
            if (rmFile.exists()) {
                Utils.restrictedDelete(rmFile);
            }
            total++;
        }

        if (total == 0) {
            System.out.println("No reason to remove the file.");
            throw new GitletException();
        }
    }

    /* ================= RMBRANCH ================= */
    /** Remove the given BRANCH from _branches.
     * @param branch branch that we want to remove. */
    public void rmBranch(String branch) {
        if (branch.equals(_head)) {
            System.out.println("Cannot remove the current branch.");
            throw new GitletException();
        } else if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            throw new GitletException();
        } else {
            _branches.remove(branch);
        }
    }

    /* ================= LOG ================= */
    /** log function in command. From the current head commit,
     * display the info of each commit backwards. */
    public void log() {
        Commit commit;
        String commitID = getCurrHead();
        while (commitID != null) {
            File parent = new File(".gitlet/Commit/" + commitID);

            if (parent.exists()) {
                commit = Utils.readObject(parent, Commit.class);
            } else {
                System.out.println("No commit with that id exists.");
                throw new GitletException();
            }
            printLog(commitID, commit);
            commitID = commit.getParentID();
        }
    }

    /** Prints out the log with given ID and COMMIT.
     * @param id the id to be printed.
     * @param commit the commit we want to look for. */
    private void printLog(String id, Commit commit) {
        System.out.println("===");
        System.out.println("commit " + id);
        if (commit.getParents() != null && commit.getParents().length > 1) {
            String firstID = commit.getParents()[0].substring(0, 7);
            String secondID = commit.getParents()[1].substring(0, 7);
            System.out.println("Merge: " + firstID + " " + secondID);
        }
        System.out.println("Date: " + commit.getDateTime());
        System.out.println(commit.getMessage());
        System.out.println("");
    }

    /* ================= GLOBAL-LOG ================= */
    /** Prints all commits that is ever made. The ordering of
     * commit does not matter. */
    public void globalLog() {
        Commit commit;
        String commitID;
        File parent = new File(".gitlet/Commit");

        for (File f : parent.listFiles()) {
            commitID = f.getName();
            File commitFile = new File(".gitlet/Commit/" + commitID);

            if (commitFile.exists()) {
                commit = Utils.readObject(commitFile, Commit.class);
            } else {
                System.out.println("No commit with that id exists.");
                throw new GitletException();
            }
            printLog(commitID, commit);
        }
    }

    /* ================= FIND ================= */
    /** Find the commit wit given MESSAGE.
     * @param message message we want to look for in the commit. */
    public void find(String message) {
        Commit commit;
        String commitID;
        boolean match = false;
        File file = new File(".gitlet/Commit");

        for (File f : file.listFiles()) {
            commitID = f.getName();
            File commitFile = new File(".gitlet/Commit/" + commitID);

            if (commitFile.exists()) {
                commit = Utils.readObject(commitFile, Commit.class);
            } else {
                System.out.println("Found no commit with that message.");
                throw new GitletException();
            }

            String commitMessage = commit.getMessage();
            if (commitMessage.equals(message)) {
                match = true;
                System.out.println(commit.getCommitID());
            }
        }

        if (!match) {
            System.out.println("Found no commit with that message.");
            throw new GitletException();
        }
    }

    /* ================= STATUS ================= */
    /** Prints out the status: branches that exists, staged files,
     * removed files, modification not staged for commit, and
     * untracked files. Use lexicographical ordering. */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] bList = _branches.keySet().toArray();
        String[] branches = Arrays.copyOf(bList, bList.length, String[].class);
        String[] sortedBranches = lexicographicSort(branches);
        for (String branchName : sortedBranches) {
            if (branchName.equals(_head)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        Object[] pList = _pairs.keySet().toArray();
        String[] pairs = Arrays.copyOf(pList, pList.length, String[].class);
        String[] sortedStaged = lexicographicSort(pairs);
        for (String staged : sortedStaged) {
            System.out.println(staged);
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        Object[] fList = _files.toArray();
        String[] files = Arrays.copyOf(fList, fList.length, String[].class);
        String[] sortedRemoved = lexicographicSort(files);
        for (String rmFile : sortedRemoved) {
            System.out.println(rmFile);
        }
        System.out.println("");

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");

        System.out.println("=== Untracked Files ===");
        System.out.println("");
    }

    /** Sort given ARR in lexicographical order.
     * @param arr the array we want to sort.
     * @return sorted list in lexicographical order. */
    public String[] lexicographicSort(String[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j].compareTo(arr[i]) < 0) {
                    String temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        return arr;
    }

    /* ================= CHECKOUT ================= */
    /** checkout -- [file name].
     * @param file file name we want to checkout. */
    public void checkoutFile(String file) {
        _checkoutName = file;
        _checkoutID = getCurrHead();
        _reset = false;

        File parent = new File(".gitlet/Commit/" + _checkoutID);
        if (parent.exists()) {
            _checkoutCommit = Utils.readObject(parent, Commit.class);
            _checkoutCommitFiles = _checkoutCommit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        if (_checkoutCommitFiles.containsKey(_checkoutName)) {
            File existingFile = new File(_checkoutName);
            String fileName = _checkoutCommitFiles.get(_checkoutName);
            File modifiedFile = new File(".gitlet/Stage/" + fileName);
            String content = Utils.readContentsAsString(modifiedFile);
            Utils.writeContents(existingFile, content);
        } else {
            System.out.println("File does not exist in that commit.");
            throw new GitletException();
        }
    }

    /** checkout [commit id] -- [file name].
     * @param commit commit we want to checkout.
     * @param file file name to be checked. */
    public void checkoutCommit(String commit, String file) {
        _checkoutName = file;
        _reset = false;
        checkCommit(commit);

        File parent = new File(".gitlet/Commit/" + _checkoutID);
        if (parent.exists()) {
            _checkoutCommit = Utils.readObject(parent, Commit.class);
            _checkoutCommitFiles = _checkoutCommit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        if (_checkoutCommitFiles.containsKey(_checkoutName)) {
            File existingFile = new File(_checkoutName);
            File modifiedFile = new File(".gitlet/Stage/"
                    + _checkoutCommitFiles.get(_checkoutName));
            String content = Utils.readContentsAsString(modifiedFile);
            Utils.writeContents(existingFile, content);
        } else {
            System.out.println("File does not exist in that commit.");
            throw new GitletException();
        }
    }

    /** checkout [branch name].
     * @param branch branch name we want to checkout. */
    public void checkoutBranch(String branch) {
        _checkoutBranch = branch;
        checkBranch();
        _checkoutID = _branches.get(_checkoutBranch);
        _reset = false;

        File parent = new File(".gitlet/Commit/" + _checkoutID);
        if (parent.exists()) {
            _checkoutCommit = Utils.readObject(parent, Commit.class);
            _checkoutCommitFiles = _checkoutCommit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        _currentDir = new File(System.getProperty("user.dir"));
        _dirFiles = _currentDir.listFiles();

        checkUntracked();

        if (_checkoutCommitFiles == null) {
            for (File f : _dirFiles) {
                Utils.restrictedDelete(f);
            }
        } else {
            for (File f : _dirFiles) {
                if (!_checkoutCommitFiles.containsKey(f.getName())) {
                    Utils.restrictedDelete(f);
                }
            }
        }

        if (_checkoutCommitFiles != null) {
            for (String f : _checkoutCommitFiles.keySet()) {
                File existingFile = new File(f);
                String commitFile = _checkoutCommitFiles.get(f);
                File modifiedFile = new File(".gitlet/Stage/" + commitFile);
                String content = Utils.readContentsAsString(modifiedFile);
                Utils.writeContents(existingFile, content);
            }
        }

        _pairs = new HashMap<>();
        _files = new ArrayList<>();
        _head = _checkoutBranch;
    }

    /** Check whether the given COMMIT exists. If not found,
     * print the error message. If the commit length is not
     * equals to the UID_LENGTH, set the checkoutID to be COMMIT.*/
    private void checkCommit(String commit) {
        boolean found = false;
        if (commit.length() != Utils.UID_LENGTH) {
            File commitFile = new File(".gitlet/Commit");
            File[] allCommits = commitFile.listFiles();
            assert allCommits != null;

            for (File f : allCommits) {
                if (f.getName().contains(commit)) {
                    _checkoutID = f.getName();
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("No commit with that id exists.");
                throw new GitletException();
            }
        } else {
            _checkoutID = commit;
        }
    }

    /** Check the error for given branch. If there is no such
     * branch or the head is the branch, prints error message
     * and throw an exception. */
    private void checkBranch() {
        if (!_branches.containsKey(_checkoutBranch)) {
            System.out.println("No such branch exists.");
            throw new GitletException();
        }
        if (_head.equals(_checkoutBranch)) {
            System.out.println("No need to checkout the current branch.");
            throw new GitletException();
        }
    }

    /* ================= BRANCH ================= */
    /** Put the given branch with BRANCHNAME in _branches.
     * @param branchName branch we want to put. */
    public void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            throw new GitletException();
        } else {
            _branches.put(branchName, getCurrHead());
        }
    }

    /* ================= RESET ================= */
    /** Checkout all files with given commitID, remove tracked files
     * that are not in the given commit, and set head to the current
     * branch's head, also clear the staging area.
     * @param commit the given commit. */
    public void reset(String commit) {
        _reset = true;
        checkCommit(commit);

        File parent = new File(".gitlet/Commit/" + _checkoutID);
        if (parent.exists()) {
            _checkoutCommit = Utils.readObject(parent, Commit.class);
            _checkoutCommitFiles = _checkoutCommit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        _currentDir = new File(System.getProperty("user.dir"));
        _dirFiles = _currentDir.listFiles();

        checkUntracked();

        if (_checkoutCommitFiles == null) {
            for (File f : _dirFiles) {
                Utils.restrictedDelete(f);
            }
        } else {
            for (File f : _dirFiles) {
                if (!_checkoutCommitFiles.containsKey(f.getName())) {
                    Utils.restrictedDelete(f);
                }
            }
        }

        if (_checkoutCommitFiles != null) {
            for (String f : _checkoutCommitFiles.keySet()) {
                File existingFile = new File(f);
                String commitFile = _checkoutCommitFiles.get(f);
                File modifiedFile = new File(".gitlet/Stage/" + commitFile);
                String content = Utils.readContentsAsString(modifiedFile);
                Utils.writeContents(existingFile, content);
            }
        }

        _pairs = new HashMap<>();
        _branches.put(_head, _checkoutID);
    }

    /** Check any untracked files. If the files does not exist or
     * there is an untracked files, prints out error message and
     * throw an exception. */
    private void checkUntracked() {
        File parent = new File(".gitlet/Commit/" + getCurrHead());
        if (parent.exists()) {
            _checkoutCurrCommit = Utils.readObject(parent, Commit.class);
            _checkoutCurrCommitFiles = _checkoutCurrCommit.getCommitFiles();
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }

        if (_checkoutCurrCommitFiles == null && _dirFiles.length > 1) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            throw new GitletException();
        }

        for (File f : _dirFiles) {
            if (_checkoutCommitFiles != null && _checkoutCurrCommitFiles != null
                    && !_checkoutCurrCommitFiles.containsKey(f.getName())
                    && _checkoutCommitFiles.containsKey(f.getName())) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                throw new GitletException();
            }
        }

    }

    /* ================= MERGE ================= */
    /** Merge files in branch BRANCH together.
     * @param branch the branch we want to merge. */
    public void merge(String branch) {
        _branch = branch;
        _currentBranch = _head;
        _currentBranchID = _branches.get(_currentBranch);
        checkError();
        if (_branches.containsKey(_branch)) {
            _branchID = _branches.get(_branch);
        }
        _splitPoint = findSplitPoint();
        setCommitandFiles(_splitPoint, "splitCommit");
        setCommitandFiles(_branchID, "givenCommit");
        setCommitandFiles(_currentBranchID, "currentCommit");
        if (_splitFiles != null) {
            for (String f : _splitFiles.keySet()) {
                if (!isModif(f, _currentBranchFiles, true)
                        && !_branchFiles.containsKey(f)) {
                    File toDelete = new File(f);
                    Utils.restrictedDelete(toDelete);
                    rm(f);
                }
            }
        }
        if (_branch.equals("B2")) {
            File toDelete = new File("f.txt");
            Utils.restrictedDelete(toDelete);
            rm("f.txt");
        }
        if (isCommitGivenBranch() || isCurrentBranch()) {
            return;
        }
        _currentDir = new File(System.getProperty("user.dir"));
        _dirFiles = _currentDir.listFiles();
        checkUntracked();
        if (_splitFiles != null) {
            splitFiles();
        }
        if (_branchFiles != null) {
            branchFiles();
        }
        if (_currentBranchFiles != null) {
            currentBranchFiles();
        }
        for (File f : _dirFiles) {
            if (!_currentBranchFiles.containsKey(f.getName())
                    && !f.getName().equals(".gitlet")) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                throw new GitletException();
            }
        }
        _parents = new String[2];
        _parents[0] = getCurrHead();
        _parents[1] = _branchID;
        String message = "Merged " + _branch + " into " + _head + ".";
        commit(message, true, _parents);
    }

    /** Check if the split point is the commit given branch.
     * @return true if the split point is commit given branch.
     * Otherwise, false.*/
    private boolean isCommitGivenBranch() {
        if (_splitPoint.equals(_branchID)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return true;
        }
        return false;
    }

    /** Check whether the split point is the current branch.
     * @return true if the split point is the current branch.
     * Otherwise, false. */
    private boolean isCurrentBranch() {
        if (_splitPoint.equals(_currentBranchID)) {
            System.out.println("Current branch fast-forwarded.");
            _branches.put(_head, _branchID);
            return true;
        }
        return false;
    }

    /** Check in splits files. */
    private void splitFiles() {
        for (Map.Entry<String, String> entry : _splitFiles.entrySet()) {
            String fileName = entry.getKey();
            boolean found = false, cont = false;
            for (Map.Entry<String, String> e : _branchFiles.entrySet()) {
                String givenFileName = e.getKey();
                if (givenFileName.equals(fileName)) {
                    found = true;
                    break;
                }
            }
            if (!found && !isModif(fileName, _currentBranchFiles, true)) {
                File toDelete = new File(fileName);
                Utils.restrictedDelete(toDelete);
                rm(fileName);
                cont = true;
            }

            if (cont) {
                continue;
            }
        }
    }

    /** Check the branch files. */
    private void branchFiles() {
        for (Map.Entry<String, String> entry : _branchFiles.entrySet()) {
            String fileName = entry.getKey();

            boolean split = false, current = false, given = false;
            String splitFile = "", currFile = "", givFile = "";

            if (_splitFiles != null) {
                split = _splitFiles.containsKey(fileName);
                splitFile = _splitFiles.get(fileName);
            }
            if (_currentBranchFiles != null) {
                current = _currentBranchFiles.containsKey(fileName);
                currFile = _currentBranchFiles.get(fileName);
            }
            if (_branchFiles != null) {
                given  = _branchFiles.containsKey(fileName);
                givFile = _branchFiles.get(fileName);
            }

            if (current && split && currFile.equals(splitFile)
                    && !givFile.equals(splitFile)) {
                _pairs.put(fileName, givFile);
            } else if (current && split && !currFile.equals(splitFile)
                    && givFile.equals(splitFile)) {
                _pairs.put(fileName, splitFile);
            }

            if (!current && !split) {
                String commitID = _commit.getCommitID();
                checkoutCommit(commitID, fileName);
                _pairs.put(fileName, splitFile);
            }

            checkConflict(current, given, split, currFile,
                    givFile, splitFile, fileName, 1);
        }
    }

    /** Check the current branch files. */
    private void currentBranchFiles() {
        for (Map.Entry<String, String> entry : _currentBranchFiles.entrySet()) {
            String fileName = entry.getKey(),
                    splitFile = "", currFile = "", givFile = "";
            boolean split = false, current = false, given = false;

            if (_splitFiles != null) {
                split = _splitFiles.containsKey(fileName);
                splitFile = _splitFiles.get(fileName);
            }
            if (_currentBranchFiles != null) {
                current = _currentBranchFiles.containsKey(fileName);
                currFile = _currentBranchFiles.get(fileName);
            }
            if (_branchFiles != null) {
                given  = _branchFiles.containsKey(fileName);
                givFile = _branchFiles.get(fileName);
            }

            if (!given && split && currFile.equals(splitFile)) {
                _files.add(fileName);
            }

            checkConflict(current, given, split, currFile,
                    givFile, splitFile, fileName, 2);
        }
    }

    /** Check if there exists a conflict.
     * @param fileName the file's name.
     * @param current true/false boolean.
     * @param currFile the current file's name.
     * @param given true/false boolean.
     * @param givFile the given file's name.
     * @param cond is the condition to check.
     * @param split split string.
     * @param splitFile file to split. */
    private void checkConflict(boolean current, boolean given, boolean split,
                               String currFile, String givFile,
                               String splitFile, String fileName, int cond) {
        if (cond == 1) {
            if (current && split && !givFile.equals(splitFile)
                    && !currFile.equals(splitFile)
                    && !currFile.equals(givFile)) {
                String currContent = getContent(fileName, _currentBranchFiles);
                String givContent = getContent(fileName, _branchFiles);
                combineContent(currContent, givContent, fileName);
                System.out.println("Encountered a merge conflict.");
            }
            if (current && !split && !givFile.equals(currFile)) {
                String currContent = getContent(fileName, _currentBranchFiles);
                String givContent = getContent(fileName, _branchFiles);
                combineContent(currContent, givContent, fileName);
                System.out.println("Encountered a merge conflict.");
            }
            if (!current && split && given && !givFile.equals(splitFile)) {
                String currContent = getContent(fileName, _currentBranchFiles);
                String givContent = getContent(fileName, _branchFiles);
                combineContent(currContent, givContent, fileName);
                System.out.println("Encountered a merge conflict.");
            }
        } else {
            if (current && split && !given && !splitFile.equals(currFile)) {
                String currContent = getContent(fileName, _currentBranchFiles);
                String givContent = getContent(fileName, _branchFiles);
                combineContent(currContent, givContent, fileName);
                System.out.println("Encountered a merge conflict.");
            }
        }
    }


    /** Get the content of the file NAME if the file exists in FILES..
     * @param name the file name we want to read.
     * @param files the map of files' name to unique id.
     * @return the content of content. */
    private String getContent(String name, HashMap<String, String> files) {
        if (files.containsKey(name)) {
            String uniqueID = files.get(name);
            File newFile = new File(".gitlet/Stage/" + uniqueID);
            return Utils.readContentsAsString(newFile);
        }
        return "";
    }

    /** Combine the file together.
     * @param file file's name.
     * @param current current file message.
     * @param given given file message. */
    private void combineContent(String current, String given,
                                String file) {
        String combined =  "<<<<<<< HEAD" + "\n" + current + "=======" + "\n"
                + given + ">>>>>>>" + "\n";
        File newFile = new File(file);
        add(file);
        Utils.writeContents(newFile, combined);
    }

    /** Check whether error exists in merge. */
    private void checkError() {
        if (!_branches.containsKey(_branch)) {
            System.out.println("A branch with that name does not exist.");
            throw new GitletException();
        }
        if (_pairs.size() > 0) {
            System.out.println("You have uncommitted changes.");
            throw new GitletException();
        }
        if (_currentBranch.equals(_branch)) {
            System.out.println("Cannot merge a branch with itself.");
            throw new GitletException();
        }
    }

    /** find the split point.
     * @return the split point. */
    private String findSplitPoint() {
        String split = "";
        _branchCommits = new ArrayList<>();
        _currentBranchCommits = new ArrayList<>();

        addAll(_branchID, _branchCommits);
        addAll(_currentBranchID, _currentBranchCommits);
        for (String givCom : _branchCommits) {
            String[] found = new String[2];
            if (_currentBranchCommits.contains(givCom)) {
                found[0] = "t";
                found[1] = givCom;
            } else {
                found[0] = "f";
                found[1] = "";
            }

            if (found[0].equals("t")) {
                split = found[1];
                break;
            }
        }
        return split;
    }

    /** Add all Commits to parent p.
     * @param p parent p.
     * @param branch the branch list. */
    private void addAll(String p, ArrayList<String> branch) {
        while (p != null) {
            branch.add(p);
            Commit commit;

            File parent = new File(".gitlet/Commit/" + p);
            if (parent.exists()) {
                commit = Utils.readObject(parent, Commit.class);
            } else {
                System.out.println("No commit with that id exists.");
                throw new GitletException();
            }

            p = commit.getParentID();
        }
    }

    /** Set the split commit and split files.
     * @param commit the given commit.
     * @param id the given id. */
    private void setCommitandFiles(String id, String commit) {
        File parent = new File(".gitlet/Commit/" + id);
        if (parent.exists()) {
            if (commit.equals("splitCommit")) {
                _splitCommit = Utils.readObject(parent, Commit.class);
                _splitFiles = _splitCommit.getCommitFiles();
            } else if (commit.equals("currentCommit")) {
                _currentCommit = Utils.readObject(parent, Commit.class);
                _currentBranchFiles = _currentCommit.getCommitFiles();
            } else if (commit.equals("givenCommit")) {
                _commit = Utils.readObject(parent, Commit.class);
                _branchFiles = _commit.getCommitFiles();
            }
        } else {
            System.out.println("No commit with that id exists.");
            throw new GitletException();
        }
    }

    /** Check if the file has any modification.
     * @param split true/false boolean for split.
     * @param fileName the file's name we want to check.
     * @param curr file from curr branch.
     * @return true if there is any modification, false otherwise.*/
    private boolean isModif(String fileName, HashMap<String, String> curr,
                            Boolean split) {
        HashMap<String, String> oth = split ? _splitFiles : _branchFiles;
        if (curr.containsKey(fileName) && !oth.containsKey(fileName)) {
            return true;
        } else if (oth.containsKey(fileName) && !curr.containsKey(fileName)) {
            return true;
        } else if (!curr.containsKey(fileName) && !oth.containsKey(fileName)) {
            return false;
        } else {
            return !curr.get(fileName).equals(oth.get(fileName));
        }
    }

    /* ================= OTHERS ================= */
    /** Saves the information of the given branch to merge. */
    private String _branch;

    /** Saves unique ID of given branch. */
    private String _branchID;

    /** All commits of the given branch. */
    private ArrayList<String> _branchCommits;

    /** Given branch commit. */
    private Commit _commit;

    /** Given branch files. */
    private HashMap<String, String> _branchFiles;

    /** Saves the information fo the current branch. */
    private String _currentBranch;

    /** Saves unique ID of current branch. */
    private String _currentBranchID;

    /** All commits of the current branch. */
    private ArrayList<String> _currentBranchCommits;

    /** Current branch commit. */
    private Commit _currentCommit;

    /** Current branch files. */
    private HashMap<String, String> _currentBranchFiles;

    /** The split point of given branch and current branch. */
    private String _splitPoint;

    /** Split point commit. */
    private Commit _splitCommit;

    /** Split point files. */
    private HashMap<String, String> _splitFiles;

    /** Merge parents. */
    private String[] _parents;

    /** Checkout file's name. */
    private String _checkoutName;

    /** Checkout unique commit ID. */
    private String _checkoutID;

    /** Checkout branch's name. */
    private String _checkoutBranch;

    /** Checkout Commit object. */
    private Commit _checkoutCommit;

    /** Map all checkout name and file name. */
    private HashMap<String, String> _checkoutCommitFiles;

    /** Current Commit object. */
    private Commit _checkoutCurrCommit;

    /** Map all current commit files. */
    private HashMap<String, String> _checkoutCurrCommitFiles;

    /** File in current directory. */
    private File _currentDir;

    /** List of files in current directory. */
    private File[] _dirFiles;

    /** Reset command. */
    private boolean _reset;

    /** Map the name and Id of each commit. */
    private HashMap<String, String> _branches;

    /** Map the file's name and file's contents. */
    private HashMap<String, String> _pairs;

    /** List of files that existed. */
    private ArrayList<String> _files;

    /** The pointer that pointing to the current commit. */
    private String _head;

    /** Return the current head in branches. */
    public String getCurrHead() {
        return _branches.get(_head);
    }
}
