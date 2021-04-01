package enigma;

import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author ellsafiorenza
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors.toArray();
        _rotors = new Rotor[_numRotors];
        _plugboard = new Permutation("", _alphabet);
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (duplicateRotor(rotors)) {
            throw new EnigmaException("Duplicate rotor exists!");
        }

        int sameRotors = 0;
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.length; j++) {
                if (rotors[i].equals(((Rotor) _allRotors[j]).name())) {
                    _rotors[i] = (Rotor) _allRotors[j];
                    sameRotors++;
                }
            }
        }
        if (sameRotors != numRotors()) {
            throw new EnigmaException("Incorrect number of rotors!");
        }
        if (!correctPawls()) {
            throw new EnigmaException("Wrong NumPawls!");
        }
        if (!(isFirstReflector() && isFixed() && isMoving())) {
            throw new EnigmaException("Invalid rotor initialization!");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (!validSetting(setting) || setting.length() != _numRotors - 1) {
            throw new EnigmaException("Invalid Setting!");
        }
        for (int i = 1; i < _rotors.length; i++) {
            _rotors[i].set(setting.charAt(i - 1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        convertRotor();
        int plug = _plugboard.permute(c);
        for (int i = _numRotors - 1; i >= 0; i--) {
            plug = _rotors[i].convertForward(plug);
        }
        for (int i = 1; i < _numRotors; i++) {
            plug = _rotors[i].convertBackward(plug);
        }
        plug = _plugboard.invert(plug);
        return plug;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            result += _alphabet.toChar(
                    convert(_alphabet.toInt(msg.charAt(i))));
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** My total number of rotors. */
    private int _numRotors;

    /** My total number of pawls. */
    private int _pawls;

    /** My collection of all rotors. */
    private Object[] _allRotors;

    /** My list of all rotors. */
    private Rotor[] _rotors;

    /** Return the list of all rotors. */
    public Rotor[] rotors() {
        return _rotors;
    }

    /** My permutation of plugboard. */
    private Permutation _plugboard;

    /** Return the permutation of plugboard. */
    public Permutation plugboard() {
        return _plugboard;
    }

    /** Check if there is a repetition in allRotors.
     * @param allRotors the list of rotors to check.
     * @return true iff there is a repetition. Otherwise, false. */
    public boolean duplicateRotor(String[] allRotors) {
        for (int i = 0; i < allRotors.length; i++) {
            for (int j = 0; j < allRotors.length; j++) {
                if (i != j && allRotors[i].equals(allRotors[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Advance the fast rotor. If the rotor is atNotch advance
     * the rotor left to it also the rotor that is at Notch. */
    public void convertRotor() {
        boolean[] val = new boolean[_numRotors];
        for (int i = 0; i < _numRotors; i++) {
            if (i == _numRotors - 1) {
                val[i] = true;
            } else {
                val[i] = false;
            }
        }
        int first = -1;
        for (int i = 1; i < _numRotors; i++) {
            if (_rotors[i].rotates() && _rotors[i - 1].rotates()
                    && _rotors[i].atNotch()) {
                if (first == -1) {
                    first = i;
                }
                val[i] = true;
            }
        }
        for (int i = 0; i < _numRotors; i++) {
            if (val[i]) {
                _rotors[i].advance();
            }
        }
        if (first != -1 && _rotors[first - 1].rotates()) {
            _rotors[first - 1].advance();
        }
    }

    /** Return true iff the first rotor is reflecting. */
    public boolean isFirstReflector() {
        return _rotors[0].reflecting();
    }

    /** Return true if the other is fixed rotor. */
    public boolean isFixed() {
        for (int i = 1; i < _rotors.length; i++) {
            if (_rotors[i].reflecting()) {
                return false;
            }
        }
        return true;
    }

    /** Return true if moving rotor is after fixed rotor
     * and reflector. */
    public boolean isMoving() {
        boolean movingRotor = false;
        for (int i = 1; i < _rotors.length; i++) {
            if (_rotors[i].rotates() && !movingRotor) {
                movingRotor = true;
            } else if (movingRotor && !_rotors[i].rotates()) {
                return false;
            }
        }
        return true;
    }

    /** Check if the number of moving rotors match the no of pawls.
     * Return true if it matches, false otherwise. */
    public boolean correctPawls() {
        int total = 0;
        for (int i = 0; i < _rotors.length; i++) {
            if (_rotors[i].rotates()) {
                total++;
            }
        }
        return total == _pawls;
    }

    /** Check setting in Alphabet.
     * @param setting the setting we want to check.
     * @return true if setting is in alphabet. */
    public boolean validSetting(String setting) {
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.contains(setting.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Set the ring setting based on string setting accordingly.
     * @param settings the ring setting based on the index. */
    public void setRotorRing(String settings) {
        if (!validSetting(settings) || settings.length() != _numRotors - 1) {
            throw new EnigmaException("Invalid Ringstellung!");
        }
        for (int i = 1; i < _rotors.length; i++) {
            _rotors[i].setRing(settings.charAt(i - 1));
            _rotors[i].isRingStellung();
        }
    }
}
