package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author ellsafiorenza
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = _permutation.wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = _permutation.wrap(alphabet().toInt(cposn));
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        return _permutation.wrap(_permutation.permute(
                _permutation.wrap(p + _setting)) - _setting);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        return _permutation.wrap(_permutation.invert(
                _permutation.wrap(e + _setting)) - _setting);
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The setting for the rotor. */
    private int _setting;

    /** Extra Credit part. */
    /** My ring setting.*/
    private int _ring;

    /** If I have ring setting. Initially set to false. */
    private boolean _ringStellung = false;

    /** If I have ring setting, set my ring setting accordingly. */
    public void isRingStellung() {
        _ringStellung = true;
        setRingStellung();
    }

    /** Returns true if I have ringstellung. */
    public boolean ringStellung() {
        return _ringStellung;
    }

    /** Set my ring to the given char R. */
    public void setRing(char r) {
        _ring = alphabet().toInt(r);
    }

    /** Return my ring. */
    public int ring() {
        return _ring;
    }

    /** Set my Ring setting. */
    private void setRingStellung() {
        set(_permutation.wrap(_setting - _ring));
    }
}
