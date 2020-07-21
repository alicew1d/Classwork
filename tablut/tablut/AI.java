package tablut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.*;

import static tablut.Square.sq;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Alice Wang
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        String stringmove = move.toString();
        System.out.println("* " + stringmove);
        return stringmove;
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else if (_myPiece == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        int best;
        if (sense == 1) {
            best = -INFTY;
        } else {
            best = INFTY;
        }
        if (sense == 1) {
            List<Move> legalmv = board.legalMoves(board.turn());
            for (Move mv : legalmv) {
                board.makeMove(mv);
                Board next = new Board(board);
                board.undo();
                int ns;
                if (depth <= 0) {
                    ns = staticScore(next);
                } else {
                    ns = findMove(next, depth - 1, false, -1, alpha, beta);
                }
                if (ns > best) {
                    best = ns;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                    alpha = max(alpha, best);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return best;
        }
        if (sense == -1) {
            List<Move> legalmv = board.legalMoves(board.turn());
            for (Move mv : legalmv) {
                board.makeMove(mv);
                Board nextBoard = new Board(board);
                board.undo();
                int nextScore;
                if (depth == 0) {
                    nextScore = staticScore(nextBoard);
                } else {
                    nextScore = findMove(nextBoard, depth - 1,
                            false, 1, alpha, beta);
                }
                if (nextScore < best) {
                    best = nextScore;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                    beta = min(beta, best);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return best;
        }
        return best;
    }

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        List<Move> w = board.legalMoves(WHITE);
        List<Move> b = board.legalMoves(BLACK);
        int max = max(w.size(), b.size());
        if (max > (6 * 10)) {
            return 2;
        } else if (max > 3 * 10 && max <= 6 * 10) {
            return 3;
        } else if (max > 10 + 5 && max <= 3 * 10) {
            return 4;
        } else if (max > 5 && max <= 5 + 10) {
            return 5;
        } else {
            return 10;
        }
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        HashSet<Square> whiteLocations = board.pieceLocations(WHITE);
        HashSet<Square> blackLocations = board.pieceLocations(BLACK);
        int score = 0;
        Square kingP = board.kingPosition();
        if (kingP == null) {
            return -WINNING_VALUE;
        }
        if (board.winner() == WHITE) {
            return WINNING_VALUE;
        } else if (board.winner() == BLACK) {
            return -WINNING_VALUE;
        }

        if (whiteLocations.isEmpty()) {
            return -WILL_WIN_VALUE + 100;
        }
        if (blackLocations.isEmpty()) {
            return WILL_WIN_VALUE - 100;
        }
        if (whiteLocations.size() < 9) {
            int diff = 9 - whiteLocations.size();
            score -= diff * 100;
        } else if (blackLocations.size() < 16) {
            int diff = 16 - blackLocations.size();
            score += diff * 100;
        } else {
            score += 0;
        }

        for (Square sq : whiteLocations) {
            if (sq.isEdge() && board.get(sq) == KING) {
                return WINNING_VALUE;
            }
            score += hostileScore(board, WHITE, sq);
        }
        for (Square sq : blackLocations) {
            score -= hostileScore(board, BLACK, sq);
            score -= distancetoKing(sq, kingP);
        }
        return score;
    }

    /** my distance to the King given square.
     * @param sq my square
     * @param king king position
     * @return  double
     */
    double distancetoKing(Square sq, Square king) {
        double distance;
        double coldiff = sq.col() - king.col();
        double rowdiff = sq.row() - king.row();
        distance = 10 * Math.sqrt(Math.pow(coldiff, 2) + Math.pow(rowdiff, 2));
        return distance;
    }

    /**
     * Score based on how hostile the square you are moving TO with Piece p is.
     * @param sq Square
     * @param board Board
     * @param p Piece
     * @return int
     */
    private int hostileScore(Board board, Piece p, Square sq) {
        int hScore = 0;
        int finalScore = 0;
        int side;
        if (p == WHITE) {
            side = 1;
        } else {
            side = -1;
        }
        ArrayList<Square> toOrth = board.orthogonalSquares(sq);
        for (int j = 0; j < toOrth.size(); j++) {
            if (board.isHostile(toOrth.get(j), p)) {
                hScore++;
                if (board.get(sq) == KING) {
                    finalScore -= 1000;
                }
            }
        }
        if (hScore == 1) {
            finalScore += side * 7 * 10;
        } else if (hScore == 2) {
            finalScore += side * 3 * 10;
        } else if (hScore == 3) {
            finalScore -= side * 10 * 10;
        }
        return finalScore;
    }
}
