package lab6;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

public class ArcherAgent extends Agent
{

	private Integer archerUnitID;
	private Integer enemyFootmanUnitID;
	private Set<Coordinate> obstaclePositions;

	private final int proximityTolerance;

	public ArcherAgent(int playerNum, String[] args)
	{
		super(playerNum);

		if(args.length != 2)
		{
			System.err.println("[ERROR] ArcherAgent.ArcherAgent: need to provide two arguments");
		}
		this.proximityTolerance = Integer.parseInt(args[1]);
		System.out.println("Constructed ArcherAgent");
	}

	public final Integer getArcherUnitID() { return this.archerUnitID; }
	public final Integer getEnemyFootmanUnitID() { return this.enemyFootmanUnitID; }
	public final int getProximityTolerance() { return this.proximityTolerance; }
	public final Set<Coordinate> getObstaclePositions() { return this.obstaclePositions; }

	@Override
	public Map<Integer, Action> initialStep(StateView state, HistoryView history)
	{

		// locate enemy and friendly units
		List<Integer> friendlyUnits = state.getUnitIds(this.getPlayerNumber());
		if(friendlyUnits.size() != 1)
		{
			System.err.println("[ERROR] ArcherAgent.initialStep: ArcherAgent should only have one unit under it's control");
			System.exit(1);
		}

		this.archerUnitID = friendlyUnits.get(0);

		// check that this unit is an Archer
		if(!state.getUnit(this.getArcherUnitID()).getTemplateView().getName().toLowerCase().equals("archer"))
		{
			System.err.println("[ERROR] ArcherAgent.initialStep: ArcherAgent should control an Archer unit");
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
				System.err.println("[ERROR] ArcherAgent.initialStep: enemy player should only have one unit under it's control");
				System.exit(1);
			}
		}
		this.enemyFootmanUnitID = enemyUnitIDs.get(0);

		// check that the enemy unit is a footman
		if(!state.getUnit(this.getEnemyFootmanUnitID()).getTemplateView().getName().toLowerCase().equals("footman"))
		{
			System.err.println("[ERROR] ArcherAgent.initialStep: enemy agent should control a footman unit");
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
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView state, HistoryView history) {
		Map<Integer, Action> actions = null;

		// pretty simple agent
		// if the enemy is too far away to hit...move closer (using cardinal directions)
		// if the enemy is too close...retreat using cardinal directions
		// if the enemy is in range...attack

		Unit.UnitView archerView = state.getUnit(this.getArcherUnitID());
		Unit.UnitView footmanView = state.getUnit(this.getEnemyFootmanUnitID());

		Coordinate archerPosition = new Coordinate(archerView.getXPosition(), archerView.getYPosition());
		Coordinate footmanPosition = new Coordinate(footmanView.getXPosition(), footmanView.getYPosition());

		double archerRange = archerView.getTemplateView().getRange();

		if(this.outOfRange(archerPosition, footmanPosition, archerRange))
		{
			Direction direction = this.moveCloser(archerPosition, footmanPosition, state);
			if(direction != null)
			{
				// move closer
				actions = Collections.singletonMap(this.getArcherUnitID(),
						Action.createPrimitiveMove(this.getArcherUnitID(), direction));
			}
			
		} else if(this.tooClose(archerPosition, footmanPosition))
		{
			// retreat
			Direction direction = this.retreat(archerPosition, footmanPosition, state);
			if(direction != null)
			{
				// move closer
				actions = Collections.singletonMap(this.getArcherUnitID(),
						Action.createPrimitiveMove(this.getArcherUnitID(), direction));
			}
		} else
		{
			// attack
			actions = Collections.singletonMap(this.getArcherUnitID(),
					Action.createPrimitiveAttack(this.getArcherUnitID(), this.getEnemyFootmanUnitID()));
		}

		return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView state, HistoryView history) {
		// TODO Auto-generated method stub

	}

	public Direction moveCloser(Coordinate archerPosition, Coordinate enemyPosition, StateView state)
	{
		Direction[] allDirections = Direction.values();

		Direction bestDirection = null;
		int bestDistance = Integer.MAX_VALUE;

		int newDistance;
		Coordinate newPosition = null;
		for(int directionIdx = 0; directionIdx < allDirections.length; directionIdx += 2) // only consider cardinal directions
		{
			
			newPosition = new Coordinate(archerPosition.getX() + allDirections[directionIdx].xComponent(),
					                     archerPosition.getY() + allDirections[directionIdx].yComponent());
			if(this.isLegalPosition(newPosition, this.getObstaclePositions(), enemyPosition, state))
			{

				newDistance = newPosition.chebyshevDistanceTo(enemyPosition);
				if(newDistance < bestDistance)
				{
					bestDistance = newDistance;
					bestDirection = allDirections[directionIdx];
				}
			}
		}
		return bestDirection;
	}

	public Direction retreat(Coordinate archerPosition, Coordinate enemyPosition, StateView state)
	{
		Direction[] allDirections = Direction.values();

		Direction bestDirection = null;
		int bestDistance = Integer.MIN_VALUE;

		int newDistance;
		Coordinate newPosition = null;
		for(int directionIdx = 0; directionIdx < allDirections.length; directionIdx += 2) // only consider cardinal directions
		{
			
			newPosition = new Coordinate(archerPosition.getX() + allDirections[directionIdx].xComponent(),
					                     archerPosition.getY() + allDirections[directionIdx].yComponent());
			if(this.isLegalPosition(newPosition, this.getObstaclePositions(), enemyPosition, state))
			{

				newDistance = newPosition.chebyshevDistanceTo(enemyPosition);
				if(newDistance > bestDistance)
				{
					bestDistance = newDistance;
					bestDirection = allDirections[directionIdx];
				}
			}
		}
		return bestDirection;
	}

	public boolean outOfRange(Coordinate archerPosition, Coordinate enemyPosition, double archerRange)
	{
		double distance = archerPosition.euclideanDistanceTo(enemyPosition);
		return distance > archerRange;
	}

	public boolean tooClose(Coordinate archerPosition, Coordinate enemyPosition)
	{
		int chebyshevDistance = archerPosition.chebyshevDistanceTo(enemyPosition);
		return chebyshevDistance <= this.proximityTolerance;
	}

	public boolean isLegalPosition(Coordinate position, Set<Coordinate> obstaclePositions, Coordinate enemyPosition, StateView state)
	{
		return position.getX() >= 0 && position.getX() < state.getXExtent() &&
			   position.getY() >= 0 && position.getY() < state.getYExtent() &&
			   !obstaclePositions.contains(position) && !enemyPosition.equals(position);
	}

}
