// ----- SUBMISSION DETAILS ------
// ----- Student 1: Shahaf Dan (U88749996)
// ----- Student 2: Christian Pagounis (U51909628)
// ----- CS440, Intro to AI, CAS, BU
// ----- PA2, Due 04042023


package hw2.agents.moveorder;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.Board;
import hw2.chess.game.piece.King;
import hw2.chess.game.piece.Pawn;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.piece.Rook;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hw2.chess.search.DFSTreeNode;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;

public class CustomMoveOrderer {
	
	public static boolean isCheck(Board board, Move m, DFSTreeNode node) {
		// ---- Perform the move on a temporary board to evaluate if it results in a check
        Board tempBoard = board.copy();
        tempBoard.applyMove(m);
        
        // ---- for each of the potential moves from this current node, is the board is equal to the tempBoard, check for check
        for (DFSTreeNode child_node:  node.getChildren()) {
        	if (child_node.getGame().getBoard() == tempBoard) {
        		// --- CHECK IF CURRENT PLAYER IS IN CHECK (because we are in the next move we check for current player and not next player)
        		if (child_node.getGame().isInCheck(node.getGame().getCurrentPlayer())) {
        			return true;
        		}
        	}
        }
        return false;
    }
	

    public static boolean isPromotion(Board board, Move m) {
    	// --- IF NOT A PROMOTION MOVE, RETURN FALSE
        if (m.getType() != MoveType.PROMOTEPAWNMOVE) {
            return false;
        }
        // ---- BUT A PROMOTION MOVE CAN STILL HAPPEN TO A NON PAWN PIECE, I GUESS
        // ----- CHECK IF THE PIECE BEING PROMOTED IS A PAWN
        return (board.getPiece(m.getActorPlayer(), m.getActorPieceID())).getType() == PieceType.PAWN;
    }
    
    public static boolean isCenterControlMove(Board board, Move m, DFSTreeNode n) {
        Coordinate destination = (board.getPiece(m.getActorPlayer(), m.getActorPieceID())).getCurrentPosition(board);
        System.out.println(" >> SHAHAF DEBUGGING >> destination" + String.valueOf(m));
        return (destination.equals(new Coordinate(3, 3)) || destination.equals(new Coordinate(3, 4))
                || destination.equals(new Coordinate(4, 3)) || destination.equals(new Coordinate(4, 4)));
    }

    public static boolean isPieceDevelopmentMove(Board board, Move m, DFSTreeNode n) {
        Piece movingPiece = board.getPiece(m.getActorPlayer(), m.getActorPieceID());
        PieceType pieceType = movingPiece.getType();
        return (pieceType == PieceType.KNIGHT || pieceType == PieceType.BISHOP);
    }

    public static boolean isCastlingMove(Board board, Move m, DFSTreeNode n) {
        return m.getType() == MoveType.CASTLEMOVE;
    }

    /**
     * This method performs move ordering, considering captures, checks, threats, and promotions.
     * This should allow the search to find good moves early and prune more nodes, leading to faster performance.
     * @param nodes. The nodes to order (these are children of a DFSTreeNode) that we are about to consider in the search.
     * @return The ordered nodes.
     */
    public static List<DFSTreeNode> order(List<DFSTreeNode> nodes) {
        List<DFSTreeNode> orderedNodes = new ArrayList<>(nodes);

//         Sort nodes based on custom evaluation criteria
        orderedNodes.sort(new Comparator<DFSTreeNode>() {
            @Override
            public int compare(DFSTreeNode n1, DFSTreeNode n2) {
                Move m1 = n1.getMove();
                Move m2 = n2.getMove();
                Board board = n1.getGame().getBoard();
                
                // LOGIC: the later prioritized a function is, the earlier in the game it will be prioritized
                
                // ---- 1. PRIORITIZE CHECKMATES
                boolean isOpponentInCheckmate_1 = n1.getGame().isInCheckmate();
                boolean isOpponentInCheckmate_2 = n2.getGame().isInCheckmate();
                if (isOpponentInCheckmate_1 && !isOpponentInCheckmate_2) {return -1;}
                if (!isOpponentInCheckmate_1 && isOpponentInCheckmate_2) {return 1;}
                
                // ---- 2. PRIORITIZE CAPTURES 
                if (m1.getType() == MoveType.CAPTUREMOVE && m2.getType() != MoveType.CAPTUREMOVE) {return -1;}
                if (m1.getType() != MoveType.CAPTUREMOVE && m2.getType() == MoveType.CAPTUREMOVE) {return 1;}

                // ---- 3. PRIORITIZE CHEKCS 
                boolean isOpponentInCheck_1 = n1.getGame().isInCheck(n1.getGame().getCurrentPlayer());
                boolean isOpponentInCheck_2 = n2.getGame().isInCheck(n2.getGame().getCurrentPlayer());
                if (isOpponentInCheck_1 && !isOpponentInCheck_2) {return -1;}
                if (!isOpponentInCheck_1 && isOpponentInCheck_2) {return 1;}

                // ---- 4. PRIORITIZE PROMOIONS
                if (isPromotion(board, m1) && !isPromotion(board, m2)) {return -1;}
                if (!isPromotion(board, m1) && isPromotion(board, m2)) {return 1;}

                // ---- 5. PRIORITIZE CENTER CONTROL
                if (isCenterControlMove(board, m1, n1) && !isCenterControlMove(board, m2, n2)) {return -1;}
                if (!isCenterControlMove(board, m1, n1) && isCenterControlMove(board, m2, n2)) {return 1;}

                // ---- 6. PRIORITIZE PIECE DEVELOPMENT
                if (isPieceDevelopmentMove(board, m1, n1) && !isPieceDevelopmentMove(board, m2, n2)) {return -1;}
                if (!isPieceDevelopmentMove(board, m1, n1) && isPieceDevelopmentMove(board, m2, n2)) {return 1;}

                // ---- 7. PRIORITIZE CASTLING
                if (isCastlingMove(board, m1, n1) && !isCastlingMove(board, m2, n2)) {return -1;}
                if (!isCastlingMove(board, m1, n1) && isCastlingMove(board, m2, n2)) {return 1;}
                
                return 0;
            }
        });

        return orderedNodes;
//    	return nodes;
    }
}
