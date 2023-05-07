// ----- SUBMISSION DETAILS ------
// ----- Student 1: Shahaf Dan (U88749996)
// ----- Student 2: Christian Pagounis (U51909628)
// ----- CS440, Intro to AI, CAS, BU
// ----- PA2, Due 04042023

package hw2.agents.heuristics;

import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.agents.heuristics.DefaultHeuristics.DefensiveHeuristics;
import hw2.agents.heuristics.DefaultHeuristics.OffensiveHeuristics;
import hw2.chess.game.Board;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.utils.Coordinate;

public class CustomHeuristics {

	/**
	//	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	//	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	//	 * in DefaultHeuristics.java (which is in the same directory as this file)
	//	 */
	
	public static Player getMaxPlayer(DFSTreeNode node)
	{
		return node.getMaxPlayer();
	}
	
	public static Player getMinPlayer(DFSTreeNode node)
	{
		return CustomHeuristics.getMaxPlayer(node).equals(node.getGame().getCurrentPlayer()) ? node.getGame().getOtherPlayer() : node.getGame().getCurrentPlayer();
	}


	public static class OffensiveHeuristics extends Object
	{

		public static int getNumberOfPiecesMaxPlayerIsThreatening(DFSTreeNode node)
		{

			int numPiecesMaxPlayerIsThreatening = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMaxPlayer(node)))
			{
				numPiecesMaxPlayerIsThreatening += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesMaxPlayerIsThreatening;
		}

	}

	public static class DefensiveHeuristics extends Object
	{

		public static int getNumberOfMaxPlayersAlivePieces(DFSTreeNode node)
		{
			int numMaxPlayersPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numMaxPlayersPiecesAlive += node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMaxPlayer(node), pieceType);
			}
			return numMaxPlayersPiecesAlive;
		}

		public static int getNumberOfMinPlayersAlivePieces(DFSTreeNode node)
		{
			int numMaxPlayersPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numMaxPlayersPiecesAlive += node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMinPlayer(node), pieceType);
			}
			return numMaxPlayersPiecesAlive;
		}

		public static int getClampedPieceValueTotalSurroundingMaxPlayersKing(DFSTreeNode node)
		{
			// what is the state of the pieces next to the king? add up the values of the neighboring pieces
			// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
			int maxPlayerKingSurroundingPiecesValueTotal = 0;

			Piece kingPiece = node.getGame().getBoard().getPieces(CustomHeuristics.getMaxPlayer(node), PieceType.KING).iterator().next();
			Coordinate kingPosition = node.getGame().getCurrentPosition(kingPiece);
			for(Direction direction : Direction.values())
			{
				Coordinate neightborPosition = kingPosition.getNeighbor(direction);
				if(node.getGame().getBoard().isInbounds(neightborPosition) && node.getGame().getBoard().isPositionOccupied(neightborPosition))
				{
					Piece piece = node.getGame().getBoard().getPieceAtPosition(neightborPosition);
					int pieceValue = Piece.getPointValue(piece.getType());
					if(piece != null && kingPiece.isEnemyPiece(piece))
					{
						maxPlayerKingSurroundingPiecesValueTotal -= pieceValue;
					} else if(piece != null && !kingPiece.isEnemyPiece(piece))
					{
						maxPlayerKingSurroundingPiecesValueTotal += pieceValue;
					}
				}
			}
			// kingSurroundingPiecesValueTotal cannot be < 0 b/c the utility of losing a game is 0, so all of our utility values should be at least 0
			maxPlayerKingSurroundingPiecesValueTotal = Math.max(maxPlayerKingSurroundingPiecesValueTotal, 0);
			return maxPlayerKingSurroundingPiecesValueTotal;
		}

		public static int getNumberOfPiecesThreateningMaxPlayer(DFSTreeNode node)
		{
			// how many pieces are threatening us?
			int numPiecesThreateningMaxPlayer = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMinPlayer(node)))
			{
				numPiecesThreateningMaxPlayer += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesThreateningMaxPlayer;
		}
		
	}

	public static double getOffensiveMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// remember the action has already taken affect at this point, so capture moves have already resolved
		// and the targeted piece will not exist inside the game anymore.
		// however this value was recorded in the amount of points that the player has earned in this node
		double damageDealtInThisNode = node.getGame().getBoard().getPointsEarned(CustomHeuristics.getMaxPlayer(node));

		switch(node.getMove().getType())
		{
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)node.getMove();
			damageDealtInThisNode += Piece.getPointValue(promoteMove.getPromotedPieceType());
			break;
		default:
			break;
		}
		// offense can typically include the number of pieces that our pieces are currently threatening
		int numPiecesWeAreThreatening = OffensiveHeuristics.getNumberOfPiecesMaxPlayerIsThreatening(node);

		return damageDealtInThisNode + numPiecesWeAreThreatening;
	}

	public static double getDefensiveMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// how many pieces exist on our team?
		int numPiecesAlive = DefensiveHeuristics.getNumberOfMaxPlayersAlivePieces(node);

		// what is the state of the pieces next to the king? add up the values of the neighboring pieces
		// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
		int kingSurroundingPiecesValueTotal = DefensiveHeuristics.getClampedPieceValueTotalSurroundingMaxPlayersKing(node);

		// how many pieces are threatening us?
		int numPiecesThreateningUs = DefensiveHeuristics.getNumberOfPiecesThreateningMaxPlayer(node);

		return numPiecesAlive + kingSurroundingPiecesValueTotal + numPiecesThreateningUs;
	}

	public static double getNonlinearPieceCombinationMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// both bishops are worth more together than a single bishop alone
		// same with knights...we want to encourage keeping pairs of elements
		double multiPieceValueTotal = 0.0;

		double exponent = 1.5; // f(numberOfKnights) = (numberOfKnights)^exponent

		// go over all the piece types that have more than one copy in the game (including pawn promotion)
		for(PieceType pieceType : new PieceType[] {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN})
		{
			multiPieceValueTotal += Math.pow(node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMaxPlayer(node), pieceType), exponent);
		}

		return multiPieceValueTotal;
	}
	
	public static int getPawnChainCount(Player player, Board board) {
		int count = 0;

        for (Piece piece : board.getPieces(player)) {
            if (piece.getType() == PieceType.PAWN) {
                Coordinate pawnPosition = board.getPiecePosition(piece);

                // Check for pawns diagonally adjacent in the forward direction
                int xOffset = player.getPlayerType() == PlayerType.BLACK ? 1 : -1;

                Coordinate leftDiagonalPawn = new Coordinate(pawnPosition.getXPosition() - 1, pawnPosition.getYPosition() + xOffset);
                Coordinate rightDiagonalPawn = new Coordinate(pawnPosition.getXPosition() + 1, pawnPosition.getYPosition() + xOffset);

                if (board.getPieceAtPosition(leftDiagonalPawn) != null && board.getPieceAtPosition(leftDiagonalPawn).getType() == PieceType.PAWN
                        && board.getPieceAtPosition(leftDiagonalPawn).getPlayer().equals(player)) {
                    count++;
                }

                if (board.getPieceAtPosition(rightDiagonalPawn) != null && board.getPieceAtPosition(rightDiagonalPawn).getType() == PieceType.PAWN
                        && board.getPieceAtPosition(rightDiagonalPawn).getPlayer().equals(player)) {
                    count++;
                }
            }
        }

        return count;
	}
	
	public static int getControlledCenterSquares(Player player, Board board, DFSTreeNode node) {
	    int count = 0;
	    Coordinate[] centerSquares = new Coordinate[]{
	        new Coordinate(3, 3),
	        new Coordinate(3, 4),
	        new Coordinate(4, 3),
	        new Coordinate(4, 4)
	    };

	    for (Piece piece : board.getPieces(player)) {
	        for (Coordinate centerSquare : centerSquares) {
	            // Check if the piece controls the center square
	            List<Move> pieceMoves = piece.getAllMoves(node.getGame()); // Assuming you've passed the Game instance to this function, you can replace node.getGame() with the actual Game instance if needed.
	            for (Move move : pieceMoves) {
	                if (move instanceof MovementMove) {
	                    MovementMove movementMove = (MovementMove) move;
	                    if (centerSquare.equals(movementMove.getTargetPosition())) {
	                        count++;
	                        break;
	                    }
	                } else if (move instanceof CaptureMove) {
	                    CaptureMove captureMove = (CaptureMove) move;
	                    Piece targetPiece = board.getPiece(captureMove.getTargetPlayer(), captureMove.getTargetPieceID());
	                    if (centerSquare.equals(board.getPiecePosition(targetPiece))) {
	                        count++;
	                        break;
	                    }
	                }
	            }
	        }
	    }
	    return count;
	}


	
	
    public static double getHeuristicValue(DFSTreeNode node) {
        // Instead of using DefaultHeuristics, use custom heuristics
//    	 return DefaultHeuristics.getMaxPlayerHeuristicValue(node);

        return getMaxPlayerHeuristicValue(node);
    }

    public static double pawnStructureHeuristic(DFSTreeNode node) {
    	/**
    	 * This heuristic rewards the agent for forming pawn chains, 
    	 * which are connected pawns that support each other. 
    	 * It helps the agent to create a strong pawn structure.
    	 */
    	
        double value = 0.0;
        value += getPawnChainCount(CustomHeuristics.getMaxPlayer(node), node.getGame().getBoard());
        value -= getPawnChainCount(CustomHeuristics.getMinPlayer(node), node.getGame().getBoard());
        return value;
    }

    public static double centerControlHeuristic(DFSTreeNode node) {
    	/**
    	 * This heuristic evaluates the control of the central squares of the board,
    	 *  which are crucial for a successful chess game. 
    	 *  It encourages the agent to control the center with its pieces.
    	 */
        double value = 0.0;
        value += getControlledCenterSquares(CustomHeuristics.getMaxPlayer(node), node.getGame().getBoard(), node);
        value -= getControlledCenterSquares(CustomHeuristics.getMinPlayer(node), node.getGame().getBoard(), node);
        return value;
    }

    public static double getMaxPlayerHeuristicValue(DFSTreeNode node) {
    	double offenseHeuristicValue = CustomHeuristics.getOffensiveMaxPlayerHeuristicValue(node);
		double defenseHeuristicValue = CustomHeuristics.getDefensiveMaxPlayerHeuristicValue(node);
		double nonlinearHeuristicValue = CustomHeuristics.getNonlinearPieceCombinationMaxPlayerHeuristicValue(node);
        double pawnStructureHeuristicValue = CustomHeuristics.pawnStructureHeuristic(node);
        double centerControlHeuristicValue = CustomHeuristics.centerControlHeuristic(node);

//        return + pawnStructureHeuristicValue + centerControlHeuristicValue;// + offenseHeuristicValue + defenseHeuristicValue + nonlinearHeuristicValue;
        return offenseHeuristicValue + defenseHeuristicValue + nonlinearHeuristicValue + pawnStructureHeuristicValue + centerControlHeuristicValue;
    }
}
