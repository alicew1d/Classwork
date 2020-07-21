package tablut;


import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static tablut.Move.ROOK_MOVES;
import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Alice Wang
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** List of Squares that are the THRONE and orthogonal to the THRONE. */
    static final List<Square> ORTHOGONAL_KING_SQUARE =
            List.of(THRONE, NTHRONE, ETHRONE, WTHRONE, STHRONE);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initial positions of empty Squares. */
    static final Square[] INITIAL_EMPTY_SQUARES = {
            sq(0, 0), sq(0, 1), sq(0, 2), sq(0, 6),
            sq(0, 7), sq(0, 8), sq(1, 0), sq(1, 1),
            sq(1, 2), sq(1, 3), sq(1, 5), sq(1, 6),
            sq(1, 7), sq(1, 8), sq(2, 0), sq(2, 1),
            sq(2, 2), sq(2, 3), sq(2, 5), sq(2, 6),
            sq(2, 7), sq(2, 8), sq(3, 1), sq(3, 2),
            sq(3, 3), sq(3, 5), sq(3, 6), sq(3, 7),
            sq(5, 1), sq(5, 2), sq(5, 3), sq(5, 5),
            sq(5, 6), sq(5, 7), sq(6, 0), sq(6, 1),
            sq(6, 2), sq(6, 3), sq(6, 5), sq(6, 6),
            sq(6, 7), sq(6, 8), sq(7, 0), sq(7, 1),
            sq(7, 2), sq(7, 3), sq(7, 5), sq(7, 6),
            sq(7, 7), sq(7, 8), sq(8, 0), sq(8, 1),
            sq(8, 2), sq(8, 6), sq(8, 7), sq(8, 8)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        _tablutboard = new Piece[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _tablutboard[i][j] = model.get(j, i);
            }
        }
        _moveCount = model.moveCount();
        _winner = model.winner();
        _turn = model.turn();
        _repeated = false;
        _encodedboards = new HashSet<>();
        for (String s : model._encodedboards) {
            _encodedboards.add(s);
        }
        _undostack = new Stack<>();
        _revPut = new Stack<>();
        _revPutPiece = new Stack<>();
        _kingcap = false;
        _limit = model._limit;

    }

    /** Clears the board to the initial position. */
    void init() {
        clearUndo();
        _tablutboard = new Piece[SIZE][SIZE];
        _moveCount = 0;
        _turn = BLACK;
        _winner = null;
        _repeated = false;
        _encodedboards = new HashSet<>();
        _undostack = new Stack<>();
        _revPut = new Stack<>();
        _revPutPiece = new Stack<>();
        _kingcap = false;
        _limit = 100 * 100;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i == THRONE.row() && j == THRONE.col()) {
                    _tablutboard[i][j] = KING;
                }
                for (Square attacker : INITIAL_ATTACKERS) {
                    if (attacker.row() == i && attacker.col() == j) {
                        _tablutboard[i][j] = BLACK;
                    }
                }
                for (Square defender : INITIAL_DEFENDERS) {
                    if (defender.row() == i && defender.col() == j) {
                        _tablutboard[i][j] = WHITE;
                    }
                }
                for (Square empty : INITIAL_EMPTY_SQUARES) {
                    if (empty.row() == i && empty.col() == j) {
                        _tablutboard[i][j] = EMPTY;
                    }
                }
            }
        }
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n int */
    void setMoveLimit(int n) {
        _limit = n;
        if (2 * n <= moveCount()) {
            throw new Error("limit cannot be less than move count");
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Returns the other side given my side.
     * @param side Piece*/
    private Piece nextMover(Piece side) {
        assert side != EMPTY;
        if (side == BLACK) {
            return WHITE;
        } else if (side == WHITE) {
            return BLACK;
        } else {
            return EMPTY;
        }
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String myCode = this.encodedBoard();
        if (_encodedboards.contains(myCode.substring(1))) {
            _repeated = true;
        } else {
            _encodedboards.add(myCode.substring(1));
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        Square kingP = null;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (_tablutboard[i][j] == KING) {
                    kingP = sq(j, i);
                }
            }
        }
        return kingP;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        Piece piece = _tablutboard[row][col];
        return piece;
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        int row = s.row();
        int col = s.col();
        _tablutboard[row][col] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        _revPut.push(s);
        _revPutPiece.push(get(s));
        put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** check if the columns of two square are equal.
     * @param to Square
     * @param from Square
     * @return boolean  */
    private boolean checkCol(Square from, Square to) {
        return from.col() == to.col();
    }

    /** check of rows of two square are equal.
     * @param to Square
     * @param from Square
     * @return boolean */
    private boolean checkRow(Square from, Square to) {
        return from.row() == to.row();
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board. For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty.*/
    boolean isUnblockedMove(Square from, Square to) {
        Piece fromPce = get(from);
        Piece toPce = get(to);
        if (toPce != EMPTY) {
            return false;
        }
        int fromCol = from.col();
        int toCol = to.col();
        int fromRow = from.row();
        int toRow = to.row();
        int check = 0;
        if (checkCol(from, to)) {
            if (fromRow < toRow) {
                for (int i = fromRow + 1; i <= toRow; i++) {
                    if (get(fromCol, i) != EMPTY) {
                        check++;
                    }
                }
            } else if (toRow < fromRow) {
                for (int i = toRow; i < fromRow; i++) {
                    if (get(from.col(), i) != EMPTY) {
                        check++;
                    }
                }
            }
        } else if (checkRow(from, to)) {
            if (fromCol < toCol) {
                for (int i = fromCol + 1; i <= toCol; i++) {
                    if (get(i, fromRow) != EMPTY) {
                        check++;
                    }
                }
            } else if (toCol < fromCol) {
                for (int i = to.col(); i < fromCol; i++) {
                    if (get(i, fromRow) != EMPTY) {
                        check++;
                    }
                }
            }
        } else if (toCol == fromCol && toRow == fromRow) {
            throw new Error("Move does not move Piece" + fromPce);
        }
        return check == 0;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move.
     * should just be isUnblockedMove == false */
    boolean isLegal(Square from, Square to) {
        if (get(from) == EMPTY) {
            return false;
        }
        if (get(from) != _turn && (get(from) == WHITE || get(from) == BLACK)) {
            return false;
        }
        if (get(from) == KING && _turn != WHITE) {
            return false;
        }
        if (from.col() == to.col() && from.row() == to.row()) {
            return false;
        }
        if (!isUnblockedMove(from, to)) {
            return false;
        }
        if (!(to.col() == from.col() || to.row() == from.row())) {
            return false;
        }
        if (to.col() == 4 && to.row() == 4) {
            return get(from) == KING;
        }
        return true;
    }

    /** Check if WHITE wins by looking at King Position. */
    private void checkWinner() {
        Square kingPos = kingPosition();
        if (kingPos == null) {

            _winner = BLACK;
        } else if (kingPos.isEdge()) {

            _winner = WHITE;
        }
        if (_kingcap) {

            _winner = BLACK;
        }
        if (_repeated) {
            _winner = _turn.opponent();
        }
        try {
            if (!hasMove(_turn)) {
                _winner = _turn.opponent();
            }
        } catch (NullPointerException e) {
            _winner = _turn.opponent();
        }
        if (_moveCount > _limit) {
            _winner = _turn.opponent();
        }
    }

    /** checks if the move is legal without considering whose turn it is.
     * @param to Square
     * @param from Square
     * @param p Piece
     * @return boolean */
    boolean isLegalToo(Square from, Square to, Piece p) {
        if (get(from) == EMPTY) {
            return false;
        }
        if (from.col() == to.col() && from.row() == to.row()) {
            return false;
        }
        if (!isUnblockedMove(from, to)) {
            return false;
        }
        if (!(to.col() == from.col() || to.row() == from.row())) {
            return false;
        }
        if (to.col() == 4 && to.row() == 4) {
            return get(from) == KING;
        }
        if (get(from) != p && p == BLACK) {
            return false;
        }
        if (get(from) == KING && p != WHITE) {
            return false;
        }
        if (get(from) == WHITE && p != WHITE) {
            return false;
        }
        if (get(from) == EMPTY) {
            return false;
        }
        return true;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Return a list of Squares orthogonal to sq.
     * @param sq Square */
    ArrayList<Square> orthogonalSquares(Square sq) {
        ArrayList<Square> orthSquares = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Square orthsq = sq.rookMove(i, 1);
            if (orthsq != null) {
                orthSquares.add(orthsq);
            }
        }
        return orthSquares;
    }

    /** checks if the square can be captured.
     * @param mysq Square
     * @return boolean*/
    boolean isCaptured(Square mysq) {
        ArrayList<Square> orthsqs = orthogonalSquares(mysq);

        int checkHoCol = 0;
        int checkHoRow = 0;
        int checkKing = 0;

        if (get(mysq) == EMPTY) {
            return false;
        } else if (get(mysq) == KING && ORTHOGONAL_KING_SQUARE.contains(mysq)) {
            for (Square squar : orthsqs) {
                if (isHostile(squar, KING)) {
                    checkKing++;
                }
            }
            if (checkKing == 4) {
                _kingcap = true;
            }
            return checkKing == 4;
        } else {
            for (Square square : orthsqs) {
                if (square.col() == mysq.col()) {
                    if (isHostile(square, get(mysq))) {
                        checkHoCol++;
                    }
                }
                if (square.row() == mysq.row()) {
                    if (isHostile(square, get(mysq))) {
                        checkHoRow++;
                    }
                }
            }
            if (get(mysq) == KING && (checkHoRow == 2 || checkHoCol == 2)) {
                _kingcap = true;
            }
            return checkHoRow == 2 || checkHoCol == 2;
        }
    }

    /** Checks if a Square is hostile to me given my side.
     * @param side Piece
     * @param sq Square
     * @return boolean */
    boolean isHostile(Square sq, Piece side) {
        assert side != EMPTY;
        if (side == BLACK) {
            if (get(sq) == WHITE || get(sq) == KING) {
                return true;
            }
            if (sq == THRONE && get(sq) == EMPTY) {
                return true;
            }
        } else {
            if (get(sq) == BLACK) {
                return true;
            }
            if (side == WHITE || side == KING) {
                if (sq == THRONE && get(sq) == EMPTY) {
                    return true;
                }
                if (sq == THRONE && get(sq) == KING && side != KING) {
                    ArrayList<Square> kingorth = orthogonalSquares(sq);
                    int check = 0;
                    for (Square squar : kingorth) {
                        if (get(squar) == BLACK) {
                            check++;
                        }
                    }
                    return check >= 3;
                }
            }
        }
        return false;
    }

    /** Performs a Capture after a piece had just moved to Square TO. */
    void doCapture(Square to) {
        ArrayList<Square> toOrth = orthogonalSquares(to);
        int captured = 0;
        for (int i = 0; i < toOrth.size(); i++) {
            Square orth = toOrth.get(i);
            if (isCaptured(orth)) {
                if (checkCol(orth, to) && orth.row() < to.row()) {
                    if (Square.exists(orth.col(), orth.row() - 1)) {
                        capture(to, Square.sq(orth.col(), orth.row() - 1));
                    }
                } else if (checkCol(orth, to) && orth.row() > to.row()) {
                    if (Square.exists(orth.col(), orth.row() + 1)) {
                        capture(to, Square.sq(orth.col(), orth.row() + 1));
                    }
                } else if (checkRow(orth, to) && orth.col() < to.col()) {
                    if (Square.exists(orth.col() - 1, orth.row())) {
                        capture(to, Square.sq(orth.col() - 1, orth.row()));
                    }
                } else if (checkRow(orth, to) && orth.col() > to.col()) {
                    if (Square.exists(orth.col() + 1, orth.row())) {
                        capture(to, Square.sq(orth.col() + 1, orth.row()));
                    }
                }
                captured++;
                if (captured == 3) {
                    break;
                }
            }
        }
    }

    /** Move FROM-TO, assuming this is a legal move.
     * 1. shift the piece in the from square to the to square.
     * 2. do we need to increment _movecount?
     * 3. do we need to add this move to the undo stack?
     * 4. how do we make the textplayer switch?*/
    void makeMove(Square from, Square to) {
        if (_moveCount == 0) {
            _encodedboards.add(this.encodedBoard().substring(1));
        }
        assert isLegal(from, to);
        Piece pieceMoved = get(from);

        revPut(pieceMoved, to);
        put(EMPTY, from);
        _undostack.push(mv(from, to));

        doCapture(to);
        checkRepeated();
        checkWinner();
        _moveCount += 1;
        _makeMove += 1;
        _turn = _turn.opponent();


    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied.*/
    void capture(Square sq0, Square sq2) {
        Move checkMove = _undostack.peek();
        if (Math.abs(sq0.col() - sq2.col()) == 2
                && Math.abs(sq0.row() - sq2.row()) == 2) {
            throw new Error("sq0 and sq2 are not orthogonal from each other");
        } else if (Math.abs(sq0.col() - sq2.col()) == 2
                || Math.abs(sq0.row() - sq2.row()) == 2) {
            if (sq0 != checkMove.to()) {
                throw new Error("a piece did not just move to Sq0");
            }
            if (sq2 != THRONE) {
                if (get(sq0) == EMPTY || get(sq2) == EMPTY) {
                    return;
                } else if ((get(sq0) == WHITE || get(sq0) == KING)
                        && (get(sq2) == BLACK)) {
                    return;
                } else if (get(sq0) == BLACK && get(sq2) != BLACK) {
                    return;
                } else {
                    Square captured = sq0.between(sq2);
                    revPut(EMPTY, captured);
                }
            } else {
                if (get(sq2) == KING) {
                    if (get(sq0) == BLACK && sq2 != THRONE) {
                        throw new Error("cannot capture with King and Black");
                    }
                }
                Square captured = sq0.between(sq2);
                revPut(EMPTY, captured);
            }
        } else {
            throw new Error("sq0 and sq2 are not orthogonally 2 squares away");
        }
    }

    /** Undo one move. Has no effect on the initial board. */
    void undo() {
        Move undoMove = _undostack.pop();
        if (_moveCount >= 0) {
            undoPosition();
        }
        if (_winner != null) {
            _winner = null;
        }
        Square kingP = kingPosition();
        Square checkCap;
        try {
            checkCap = _revPut.peek();
        } catch (EmptyStackException e) {
            checkCap = null;
        }
        int maxcap = 0;
        while (checkCap != null && get(checkCap) == EMPTY && maxcap < 3) {
            if (kingP == null && _revPutPiece.peek() == KING) {
                put(KING, checkCap);
            } else {
                put(_turn, checkCap);
            }
            _revPut.pop();
            _revPutPiece.pop();
            try {
                checkCap = _revPut.peek();
            } catch (EmptyStackException e) {
                maxcap = 5;
            }
            maxcap++;
        }
        Piece undoPiece = get(undoMove.to());
        put(undoPiece, undoMove.from());
        put(EMPTY, undoMove.to());
        try {
            _revPut.pop();
            _revPutPiece.pop();
        } catch (EmptyStackException e) {
            maxcap += 5;
        }
        _turn = _turn.opponent();
        _moveCount -= 1;

    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (_moveCount < 1 && _makeMove <=  0) {
            return;
        } else {
            if (!_repeated) {
                _encodedboards.remove(encodedBoard().substring(1));
            }
        }
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status.*/
    void clearUndo() {
        _undostack = new Stack<>();
        _encodedboards = new HashSet<>();
        _revPut = new Stack<>();
        _revPutPiece = new Stack<>();
    }

    /** Access of all encoded boards for testing purposes.
     * @return HashSet of my board */
    HashSet<String> myboards() {
        return _encodedboards;
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment).
     *  1. make a list of all legal moves for your piece O:*/
    List<Move> legalMoves(Piece side) {
        HashSet<Square> mypieceloc = pieceLocations(side);
        ArrayList<Move> legalMoves = new ArrayList<Move>();
        for (Square sq : mypieceloc) {
            int index = sq.index();
            for (int i = 0; i < 4; i++) {
                for (Move move : ROOK_MOVES[index][i]) {
                    if (isLegalToo(move.from(), move.to(), side)) {
                        if (get(move.from()) == WHITE && side == WHITE) {
                            legalMoves.add(move);
                        } else if (get(move.from()) == BLACK && side == BLACK) {
                            legalMoves.add(move);
                        } else if (get(move.from()) == KING && side == WHITE) {
                            legalMoves.add(move);
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        return !legalMoves(side).isEmpty();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> sqSet = new HashSet<Square>();
        if (side == WHITE) {
            Square k = kingPosition();
            if (k != null) {
                sqSet.add(kingPosition());
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (_tablutboard[i][j] == WHITE && side == WHITE) {
                    sqSet.add(sq(j, i));
                } else if (_tablutboard[i][j] == BLACK && side == BLACK) {
                    sqSet.add(sq(j, i));
                }
            }
        }
        return sqSet;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** returns my revput square stack for testing purposes. */
    Stack reversePut() {
        return _revPut;
    }
    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;

    /** Cached value of winner on this board, or null if it has not
     * been Board b = new Board() computed. */
    private Piece _winner;

    /** Number of (still undone) moves since initial position. */
    private int _moveCount;

    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /** Representation of the current board. */
    private Piece[][] _tablutboard;

    /** Keeps track of the most recently done Move. */
    private Stack<Move> _undostack;

    /** Set of all board positions that have been previously encountered. */
    private HashSet<String> _encodedboards;

    /** check to see if you have already made a move.*/
    private int _makeMove;

    /** Limit of moves in game.*/
    private int _limit;

    /** stack of squares that you have reversed put. */
    private Stack<Square> _revPut;

    /** stack of pieces that you have reversed put. */
    private Stack<Piece> _revPutPiece;

    /** check if the king has bee captured. */
    private boolean _kingcap;
}
