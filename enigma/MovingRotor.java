package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author ellsafiorenza
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        if (hasRepeat(notches)) {
            throw new EnigmaException("Notches has duplicate!");
        }
        if (notchesNotAlphabet(notches)) {
            throw new EnigmaException("Notches not in Alphabet!");
        }
        _notches = toCharList(notches);
        set(perm.alphabet().toInt(alphabet().toChar(0)));
    }

    /** The list of character from notches string. */
    private char[] _notches;

    /** Return the list of characters from notches string. */
    public char[] notches() {
        return _notches;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    /** Return true iff I have a ratchet and can move. */
    @Override
    boolean rotates() {
        return true;
    }

    /** Check if the notches string has repetition or duplicate.
     * If the string has repetition return true. Otherwise, false.
     * @param str string that we check for repetition.
     * @return boolean value of true and false. */
    private boolean hasRepeat(String str) {
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < str.length(); j++) {
                if (i != j && str.charAt(i) == str.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Check whether the notches is in _alphabet.
     * If yes return true. Otherwise, false.
     * @param str the cycle String that we want to check.
     * @return boolean value of true and false. */
    private boolean notchesNotAlphabet(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!alphabet().contains(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    boolean atNotch() {
        for (int i = 0; i < _notches.length; i++) {
            int notchI = alphabet().toInt(_notches[i]);
            if (ringStellung()) {
                notchI = permutation().wrap(
                        alphabet().toInt(_notches[i]) - ring());
            }
            if (setting() == notchI) {
                return true;
            }
        }
        return false;
    }

    /** Make the notches string into a list of characters.
     * @param notches the notches that we want to change.
     * @return the list of characters. */
    private char[] toCharList(String notches) {
        char[] result = new char[notches.length()];
        for (int i = 0; i < notches.length(); i++) {
            result[i] = notches.charAt(i);
        }
        return result;
    }
}
