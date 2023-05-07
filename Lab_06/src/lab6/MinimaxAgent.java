package lab6;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;
import lab5.Heuristic;

public class MinimaxAgent extends Agent
{

	private Integer enemyArcherUnitID;
	private Integer footmanUnitID;
	private Set<Coordinate> obstaclePositions;

	private final int maxDepth;

	public MinimaxAgent(int playerNum, String[] args)
	{
		super(playerNum);
		if(args.length < 2)
		{
			System.err.println("ERROR: need to give AlphaBetAgent a max search depth in the xml file");
			System.exit(1);
		}
		this.maxDepth = Integer.parseInt(args[1]);
		System.out.println("Constructed AlphaBetaAgent with depth limit [" + this.maxDepth + "]");
	}
	
	public final Integer getEnemyArcherUnitID() { return this.enemyArcherUnitID; }
	public final Integer getFootmanUnitID() { return this.footmanUnitID; }
	public final Set<Coordinate> getObstaclePositions() { return this.obstaclePositions; }
	public final int getMaxDepth() { return this.maxDepth; }

	@Override
	public Map<Integer, Action> initialStep(StateView state, HistoryView history) {
		// locate enemy and friendly units
		List<Integer> friendlyUnits = state.getUnitIds(this.getPlayerNumber());
		if(friendlyUnits.size() != 1)
		{
			System.err.println("[ERROR] AlphaBetAgent.initialStep: AlphaBetAgent should only have one unit under it's control");
			System.exit(1);
		}

		this.footmanUnitID = friendlyUnits.get(0);

		// check that this unit is an Archer
		if(!state.getUnit(this.getFootmanUnitID()).getTemplateView().getName().toLowerCase().equals("footman"))
		{
			System.err.println("[ERROR] AlphaBetAgent.initialStep: AlphaBetagent should control a Footman unit");
			System.exit(1);
		}

		// get the other player
		Integer[] playerIDs = state.getPlayerNumbers();
		if(playerIDs.length != 2)
		{
			System.err.println("ERROR: Should only be two players in the game");
			System.exit(1);
		}
		Integer enemyPlayerID = null;
		if(playerIDs[0] != this.getPlayerNumber())
		{
			enemyPlayerID = playerIDs[0];
		} else
		{
			enemyPlayerID = playerIDs[1];
		}

		// get the footman controlled by the other player
		List<Integer> enemyUnitIDs = state.getUnitIds(enemyPlayerID);
		if(enemyUnitIDs.size() != 1)
		{
			if(friendlyUnits.size() != 1)
			{
				System.err.println("[ERROR] AlphaBetAgent.initialStep: enemy player should only have one unit under it's control");
				System.exit(1);
			}
		}
		this.enemyArcherUnitID = enemyUnitIDs.get(0);

		// check that the enemy unit is a footman
		if(!state.getUnit(this.getEnemyArcherUnitID()).getTemplateView().getName().toLowerCase().equals("archer"))
		{
			System.err.println("[ERROR] AlphaBetAgent.initialStep: enemy agent should control an Archer unit");
			System.exit(1);
		}
		

		// get the obstacle positions
		this.obstaclePositions = new HashSet<Coordinate>();
		for(ResourceView obstacleView : state.getAllResourceNodes())
		{
			this.obstaclePositions.add(new Coordinate(obstacleView.getXPosition(), obstacleView.getYPosition()));
		}
		return this.middleStep(state, history);
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		//

	}

	@Override
	public Map<Integer, Action> middleStep(StateView state, HistoryView history) {
		// chase the archer agent for now
		Map<Integer, Action> actions = null;

		//actions = this.chaseArcher(state, history);
		actions = this.getMinimaxAction(state, history);

		return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// 

	}

	@Override
	public void terminalStep(StateView state, HistoryView history) {
		// 

	}

	//TODO
	//Input: Takes parentNode and depth (int) parameter as input
	//Output: child node representing the action that should be taken
	// Helpful functions:
	// 1. Heuristic.orderChildrenWithHeuristics()
	// 2. TreeNode.getGameState().getUtilityValue()
	// 3. TreeNode.getGameState().setUtilityValue()
	//Additional Tips:
	// 1. Function is meant to be recursive, depth parameter controls recursion
	// 2. Recursion will terminate upon terminal state, or depth = 0
	public TreeNode minimaxSearch(TreeNode parentNode, int depth)
	{
		if(parentNode.getGameState().isTerminalState() || depth <= 0)
		{
			return parentNode;
		}

		List<TreeNode> children = Heuristic.orderChildrenWithHeuristics(parentNode.getChildren());

		TreeNode bestChild = null;
		double bestUtilityValue;
		if(parentNode.getGameState().getPlayer() == GameState.FOOTMANPLAYER)
		{
			bestUtilityValue = Double.NEGATIVE_INFINITY;
			for(TreeNode child : children)
			{
				child.getGameState().setUtilityValue(this.minimaxSearch(child, depth-1).getGameState().getUtilityValue());
				if(child.getGameState().getUtilityValue() > bestUtilityValue)
				{
					bestUtilityValue = child.getGameState().getUtilityValue();
					bestChild = child;
				}
			}
		} else // archer
		{
			bestUtilityValue = Double.POSITIVE_INFINITY;
			for(TreeNode child : children)
			{
				child.getGameState().setUtilityValue(this.minimaxSearch(child, depth-1).getGameState().getUtilityValue());
				if(child.getGameState().getUtilityValue() < bestUtilityValue)
				{
					bestUtilityValue = child.getGameState().getUtilityValue();
					bestChild = child;
				}
			}
		}
		return bestChild;
	}

	public Map<Integer, Action> getMinimaxAction(StateView state, HistoryView history)
	{
		return this.minimaxSearch(new TreeNode(new GameState(state, GameState.FOOTMANPLAYER)), this.getMaxDepth()).getActions();
	}

	/**
	 * This is a method I used when initially writing the agent. I wanted to see if it would retreat when it should, etc.
	 * This metho is not used anymore, but I left it in in case you thought it was useful.
	 * @param state
	 * @param history
	 * @return
	 */
	public Map<Integer, Action> chaseArcher(StateView state, HistoryView history)
	{
		Map<Integer, Action> actions = null;

		Unit.UnitView footmanUnitView = state.getUnit(this.getFootmanUnitID());
		Unit.UnitView archerUnitView = state.getUnit(this.getEnemyArcherUnitID());

		Coordinate footmanUnitPosition = new Coordinate(footmanUnitView.getXPosition(), footmanUnitView.getYPosition());
		Coordinate archerUnitPosition = new Coordinate(archerUnitView.getXPosition(), archerUnitView.getYPosition());

		Direction[] allDirections = Direction.values();
		Direction bestDirection = null;
		int bestDistance = Integer.MAX_VALUE;

		int newDistance;
		Coordinate newFootmanUnitPosition = null;
		for(int directionIdx = 0; directionIdx < allDirections.length; directionIdx += 2)
		{
			newFootmanUnitPosition = new Coordinate(footmanUnitPosition.getX() + allDirections[directionIdx].xComponent(),
												    footmanUnitPosition.getY() + allDirections[directionIdx].yComponent());
			if(this.isLegalPosition(newFootmanUnitPosition, this.getObstaclePositions(), archerUnitPosition, state))
			{
				newDistance = newFootmanUnitPosition.chebyshevDistanceTo(archerUnitPosition);
				if(newDistance < bestDistance)
				{
					bestDistance = newDistance;
					bestDirection = allDirections[directionIdx];
				}
			}
		}
		if(bestDirection != null)
		{
			actions = Collections.singletonMap(this.getFootmanUnitID(),
					Action.createPrimitiveMove(this.getFootmanUnitID(), bestDirection));
		}

		return actions;
	}

	public boolean isLegalPosition(Coordinate position, Set<Coordinate> obstaclePositions, Coordinate enemyPosition, StateView state)
	{
		return position.getX() >= 0 && position.getX() < state.getXExtent() &&
			   position.getY() >= 0 && position.getY() < state.getYExtent() &&
			   !obstaclePositions.contains(position) && !enemyPosition.equals(position);
	}

}
