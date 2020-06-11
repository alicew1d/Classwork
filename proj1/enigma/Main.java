package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Alice Wang
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine em = readConfig();
        String setting = _input.nextLine();
        if (!setting.contains("*")) {
            throw error("wrong setting format");
        }
        setUp(em, setting);
        String nextline = "";
        while (_input.hasNextLine()) {
            nextline = _input.nextLine();
            if (nextline.contains("*")) {
                setUp(em, nextline);
                if (_input.hasNextLine()) {
                    nextline = _input.nextLine();
                }
            }
            if (!nextline.contains("*")) {
                nextline = nextline.replace(" ", "");
                String output = em.convert(nextline);
                printMessageLine(output);
            }
        }

    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = _config.nextLine();
            if (alpha.contains("(") || alpha.contains(")")
                    || alpha.contains("*") || alpha.contains(" ")) {
                throw error("wrong alphabet config");
            }
            _alphabet = new Alphabet(alpha);

            if (!_config.hasNextInt()) {
                throw new EnigmaException("int needed for numrotors in config");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("int needed for num pawls in config");
            }
            int numPawls = _config.nextInt();
            if (numRotors - numPawls < 1) {
                throw new EnigmaException("wrong number of pawls and rotors");
            }
            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                Rotor r = readRotor();
                allRotors.add(r);
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String nametemp = _name;
            if (_count < 1) {
                _name = _config.next();
                nametemp = _name;
            }
            if (_name.contains("(") || _name.contains(")") || _name.isEmpty()) {
                throw error("wrong rotor name or config input");
            }
            String notches = _config.next();
            if (notches.contains("(") || notches.contains(")")
                || notches.isEmpty()) {
                throw error("wrong notches input");
            }
            String cycles = "";
            String next = _config.next();
            while ((next.contains(")") || next.contains("("))
                    && _config.hasNext()) {
                next = next.replace(" ", "");
                if (!next.contains(")")) {
                    cycles += next;
                } else {
                    cycles += next + " ";
                }
                next = _config.next();
                _name = next;
                _count += 1;
                if (!_config.hasNext() && next.contains("(")) {
                    cycles += next + " ";
                    next = "";
                }
            }
            if (!_config.hasNext() && next.contains("(")) {
                cycles += next;
            }
            Permutation perm = new Permutation(cycles, _alphabet);
            if (notches.charAt(0) == 'M') {
                _allRotornames += nametemp + " ";
                String realnotches = notches.substring(1);
                for (int i = 0; i < realnotches.length(); i++) {
                    if (!_alphabet.contains(realnotches.charAt(i))) {
                        throw new EnigmaException("notches not in alphabet");
                    }
                }
                return new MovingRotor(nametemp, perm, realnotches);
            } else if (notches.charAt(0) == 'N') {
                _allRotornames += nametemp + " ";
                return new FixedRotor(nametemp, perm);
            } else if (notches.charAt(0) == 'R') {
                _allRotornames += nametemp + " ";
                return new Reflector(nametemp, perm);
            } else {
                throw new EnigmaException("Rotor Types Invalid");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.replace("* ", "");
        String[] s = settings.split(" ");
        if (s.length < M.numRotors()) {
            throw new EnigmaException("incorrect setting format");
        }
        String steck = "";
        String sets = "";
        String[] rotornames = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            String name = s[i];
            if (!_allRotornames.contains(name)) {
                throw new EnigmaException("Rotor must"
                + "be in _allrotors");
            }
            rotornames[i] = name;
        }
        for (int j = M.numRotors(); j < s.length; j++) {
            String next = s[j];
            if (j == M.numRotors()) {
                sets += next;
            }
            if (next.contains("(")) {
                steck += next + " ";
            }
        }
        for (int i = 0; i < rotornames.length - 1; i++) {
            for (int j = i + 1; j < rotornames.length; j++) {
                if (rotornames[i].equals(rotornames[j])) {
                    throw new EnigmaException("rotors cannot be repeated");
                }
            }
        }
        Permutation plug = new Permutation(steck, _alphabet);
        M.insertRotors(rotornames);
        if (!M.getRotors()[0].reflecting()) {
            throw new EnigmaException("first rotor must be reflector");
        }
        M.setRotors(sets);
        M.setPlugboard(plug);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String result = "";
        if (msg.isEmpty()) {
            _output.println();
        }
        for (int i = 0; i < msg.length(); i += 5) {
            if ((msg.length() - i) <= 5) {
                result = msg.substring((msg.length() - (msg.length() - i)));
                _output.println(result);
            } else {
                result = msg.substring(i, i + 5);
                _output.print(result + " ");
            }
        }

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Integer to account for calling readRotors multiple times.*/
    private int _count;

    /** String to store _name of wanted Rotor. */
    private String _name;


    /** Stores names of all the rotors. */
    private String _allRotornames = "";
}

