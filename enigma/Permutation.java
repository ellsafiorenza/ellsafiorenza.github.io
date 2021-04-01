package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author ellsafiorenza
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _strcycles = cycles;
        addCycle(_strcycles);
        _cycles = makeCycle(_strcycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        if (cycle.equals("")) {
            return;
        }
        for (int i = 0; i < size(); i++) {
            if (!hasChar(cycle, _alphabet.toChar(i))) {
                cycle += ("(" + _alphabet.toChar(i) + ")");
            }
        }
        _strcycles = cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        if (!_alphabet.contains(_alphabet.toChar(p))) {
            throw new EnigmaException("No such alphabet exist!");
        }
        char nextChar = _alphabet.toChar(p);
        for (int row = 0; row < _cycles.length; row++) {
            for (int col = 0; col < _cycles[row].length; col++) {
                if (_cycles[row][col] == _alphabet.toChar(p)) {
                    int r = (col + 1) % _cycles[row].length;
                    nextChar = _cycles[row][r];
                }
            }
        }
        return wrap(_alphabet.toInt(nextChar));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        if (!_alphabet.contains(_alphabet.toChar(c))) {
            throw new EnigmaException("No such alphabet exist!");
        }
        char prevChar = _alphabet.toChar(c);
        for (int row = 0; row < _cycles.length; row++) {
            for (int col = 0; col < _cycles[row].length; col++) {
                if (_cycles[row][col] == _alphabet.toChar(c)) {
                    int r = (col - 1) % _cycles[row].length;
                    if (r < 0) {
                        r += _cycles[row].length;
                    }
                    prevChar = _cycles[row][r];
                }
            }
        }
        return wrap(_alphabet.toInt(prevChar));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw new EnigmaException("No such alphabet exist!");
        }
        char nextChar = p;
        for (int row = 0; row < _cycles.length; row++) {
            for (int col = 0; col < _cycles[row].length; col++) {
                if (p == _cycles[row][col]) {
                    int r = (col + 1) % _cycles[row].length;
                    nextChar = _cycles[row][r];
                }
            }
        }
        return _alphabet.toChar(wrap(_alphabet.toInt(nextChar)));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("No such alphabet exist!");
        }
        char prevChar = c;
        for (int row = 0; row < _cycles.length; row++) {
            for (int col = 0; col < _cycles[row].length; col++) {
                if (c == _cycles[row][col]) {
                    int r = (col - 1) % _cycles[row].length;
                    if (r < 0) {
                        r += _cycles[row].length;
                    }
                    prevChar = _cycles[row][r];
                }
            }
        }
        return _alphabet.toChar(wrap(_alphabet.toInt(prevChar)));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int row = 0; row < _cycles.length; row++) {
            if (_cycles[row].length == 1) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** String cycle of this permutation. */
    private String _strcycles;

    /** List of cycles of this permutation. */
    private char[][] _cycles;

    /** Return the string cycle used to initialized this Permutation. */
    public String strCycles() {
        return _strcycles;
    }

    /** Return the list of cycles used to initialized this Permutation. */
    public char[][] cycles() {
        return _cycles;
    }

    /** Check if the cycle string has repetition or duplicate.
     * If the string has repetition return true. Otherwise, false.
     * @param str string that we check for repetition.
     * @return boolean value of true and false. */
    private boolean hasRepetition(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                for (int j = 0; j < str.length(); j++) {
                    if (i != j && str.charAt(i) == str.charAt(j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Check whether the cycle has a valid cycle
     * i.e. must be in this format (ABC) or (AB)(CD).
     * @param str the str that we want to check.
     * @return boolean value of true or false. */
    private boolean validCycle(String str) {
        int countOpen = 0;
        int countClose = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                countOpen += 1;
            }
            if (str.charAt(i) == ')') {
                countClose += 1;
            }
            if (countClose < countOpen - 1 || countClose > countOpen) {
                return false;
            }
        }
        return countOpen == countClose;
    }

    /** Check whether the cycle is in _alphabet.
     * If yes return true. Otherwise, false.
     * @param str the cycle String that we want to check.
     * @return boolean value of true and false. */
    private boolean cycleInAlphabet(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '(' && str.charAt(i) != ')') {
                if (!_alphabet.contains(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Check whether cycle has () or not. If yes, return true.
     * Otherwise, return false.
     * @param str string that we want to check.
     * @return boolean value of true or false. */
    private boolean hasEmptyCycle(String str) {
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.charAt(i) == '(' && str.charAt(i + 1) == ')') {
                return true;
            }
        }
        return false;
    }

    /** Check whether the space in cycle is valid.
     * i.e (AB) (CD) not (A  B).
     * @param str string cycle
     * @return true iff space is valid. Otherwise, false. */
    private boolean validSpace(String str) {
        int count = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.charAt(i) == ')' && str.charAt(i + 1) == ' ') {
                count++;
            }
        }
        return count > 0;
    }

    /** Remove the valid space so that the str will pass alphabet test.
     * @param str string cycle.
     * @return string cycle with space removed. */
    private String removeSpace(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                result += str.charAt(i);
            }
        }
        return result;
    }


    /** Help to create a char list from a given cycle string.
     * @param str the string that we want to convert into list of characters.
     * @return the list of characters. */
    private char[][] makeCycle(String str) {
        if (hasEmptyCycle(str)) {
            throw new EnigmaException("The cycle has empty cycle!");
        }
        if (!validCycle(str)) {
            throw new EnigmaException("Number of parenthesis does not match!");
        }
        if (hasRepetition(str)) {
            throw new EnigmaException("Cycle has duplicate!");
        }
        if (validSpace(str)) {
            str = str.replaceAll("\\s+", "");
        }
        if (!cycleInAlphabet(str)) {
            throw new EnigmaException("Cycle is not in Alphabet!");
        }

        int rows = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                rows++;
            }
        }
        char[][] cycle = new char[rows][];
        String[] strLst = str.split("");
        int startPos = 0, len = 0, row = 0;
        for (int i = 0; i < strLst.length; i++) {
            if (strLst[i].equals("(")) {
                startPos = i + 1;
            } else if (strLst[i].equals(")")) {
                cycle[row] = new char[len];
                for (int col = 0, p = startPos; col < len; col++, p++) {
                    cycle[row][col] = strLst[p].charAt(0);
                }
                row++;
                len = 0;
            } else {
                len++;
            }
        }
        _cycles = cycle;
        return cycle;
    }

    /** Check if string has the character c.
     * If yes, return true. Otherwise, return false.
     * @param str string that we want to check.
     * @param c character that we want to find in str.
     * @return boolean value of true or false. */
    private boolean hasChar(String str, char c) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }
}
