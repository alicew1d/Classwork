package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */


    Board _testboard = new Board();

    @Test
    public void checkInit() {
        System.out.println(_testboard);
        assert (true);
    }

    @Test
    public void checkKingPosition() {
        assertEquals(Square.sq(4, 4), _testboard.kingPosition());
    }

    @Test
    public void checkget() {
        assertEquals("WHITE", _testboard.get(3, 4).name());
        assertEquals("KING", _testboard.get(4, 4).name());
        assertEquals("EMPTY", _testboard.get(0, 8).name());
    }

    @Test
    public void checkput() {
        _testboard.put(Piece.WHITE, Square.sq(0, 8));
        assertEquals("WHITE", _testboard.get(0, 8).name());
    }

    @Test
    public void checkisUnblockedMove() {
        assertEquals(true, _testboard.isUnblockedMove(Square.sq(3, 0),
                Square.sq(3, 3)));
        assertEquals(false, _testboard.isUnblockedMove(Square.sq(3, 0),
                Square.sq(3, 4)));
        assertEquals(false, _testboard.isUnblockedMove(Square.sq(3, 0),
                Square.sq(3, 5)));
        assertEquals(true, _testboard.isUnblockedMove(Square.sq(0, 3),
                Square.sq(3, 3)));
        assertEquals(true, _testboard.isUnblockedMove(Square.sq(4, 3),
                Square.sq(1, 3)));
        assertEquals(false, _testboard.isUnblockedMove(Square.sq(0, 3),
                Square.sq(8, 3)));
    }

    @Test
    public void checkisLegal() {
        assertEquals(false, _testboard.isLegal(Square.sq(4, 4),
                Square.sq(5, 5)));
        assertEquals(false, _testboard.isLegal(Square.sq(4, 4),
                Square.sq(5, 4)));
    }

    @Test
    public void checkUndo2() {
        _testboard.makeMove(Move.mv("d1-3"));
        _testboard.makeMove(Move.mv("e6-f"));
        _testboard.undo();
        _testboard.undo();

    }

    @Test
    public void checkUndoPosition() {
        _testboard.makeMove(Square.sq(4, 7), Square.sq(5, 7));
        String code1 = _testboard.encodedBoard().substring(1);
        assertEquals(true, _testboard.myboards().contains(code1));
    }

    @Test
    public void checkCapture() {
        _testboard.makeMove(Square.sq(3, 0), Square.sq(3, 2));
        _testboard.makeMove(Square.sq(4, 3), Square.sq(6, 3));
        _testboard.makeMove(Square.sq(5, 0), Square.sq(5, 2));
        _testboard.capture(Square.sq(5, 2), Square.sq(3, 2));
        assertEquals(Piece.EMPTY, _testboard.get(Square.sq(4, 2)));
    }

    @Test
    public void checkPieceLocations() {
        HashSet<Square> black = _testboard.pieceLocations(Piece.BLACK);
        assertEquals(true, black.contains(Square.sq(0, 4)));
        assertEquals(false, black.contains(Square.sq(4, 4)));
        assertEquals(false, black.contains(Square.sq(6, 1)));

    }

    @Test
    public void checkLegalMoves() {
        List<Move> test = _testboard.legalMoves(Piece.WHITE);
        boolean contains = test.contains(Move.mv("e4-d"));
        boolean nocontain = test.contains(Move.mv("e5-h"));
        assertEquals(false, nocontain);
        assertEquals(true, contains);
        List<Move> black = _testboard.legalMoves(Piece.BLACK);
        assertEquals(true, black.contains(Move.mv("d1-3")));
        assertEquals(false, black.contains(Move.mv("d5-6")));
    }

    @Test
    public void checkHasmove() {
        boolean white = _testboard.hasMove(Piece.WHITE);
        assertEquals(true, white);
        assertEquals(true, _testboard.hasMove(Piece.BLACK));
    }

    @Test
    public void checkisHostile() {
        assertEquals(false, _testboard.isHostile(Square.sq(4, 4), Piece.WHITE));
        assertEquals(true, _testboard.isHostile(Square.sq(0, 4), Piece.WHITE));
    }

    @Test
    public void checkOrthogonalSquares() {
        ArrayList<Square> kingorth = _testboard.orthogonalSquares(Board.THRONE);
        ArrayList<Square> orthreal = new ArrayList<Square>();
        orthreal.add(Square.sq("e6"));
        orthreal.add(Square.sq("f5"));
        orthreal.add(Square.sq("e4"));
        orthreal.add(Square.sq("d5"));
        assertEquals(orthreal, kingorth);

        ArrayList<Square> zeroorth =
                _testboard.orthogonalSquares(Square.sq(0, 0));
        ArrayList<Square> zeroreal = new ArrayList<Square>();
        zeroreal.add(Square.sq(0, 1));
        zeroreal.add(Square.sq(1, 0));
        assertEquals(zeroreal, zeroorth);
    }

    @Test
    public void checkCaptured2() {
        _testboard.makeMove(Move.mv("d1-3"));
        _testboard.makeMove(Move.mv("c5-3"));
        _testboard.makeMove(Move.mv("d9-6"));
        _testboard.makeMove(Move.mv("c3-5"));
        _testboard.makeMove(Move.mv("f9-6"));
        _testboard.makeMove(Move.mv("e5-6"));
        _testboard.makeMove(Move.mv("a6-c"));
    }

    @Test
    public void checkRepeats() {
        _testboard.makeMove(Move.mv("d1-4"));
        _testboard.makeMove(Move.mv("e4-f"));
        _testboard.makeMove(Move.mv("d4-1"));
        _testboard.makeMove(Move.mv("f4-e"));
    }


}




