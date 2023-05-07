package lab6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

public class GameState
{

	private final double archerHP;
	private final Coordinate archerPosition;
	private final Integer archerUnitID;
	private final int archerAgentID;

	private final double footmanHP;
	private final Coordinate footmanPosition;
	private final Integer footmanUnitID;
	private final int footmanAgentID;

	private final Set<Coordinate> obstaclePositions;
	private final State.StateView sepiaState;

	private final int player;
	private double utilityValue = 1.0;

	public final static int FOOTMANPLAYER = 0, ARCHERPLAYER = 1;

	public GameState(State.StateView state,
			         int player)
	{
		this.sepiaState = state;

		Integer[] playerIDs = state.getPlayerNumbers();
		Integer archerPlayer = -1, footmanPlayer = -1;
		if(playerIDs.length != 2)
		{
			System.err.println("ERROR: need exactly 2 agents to play the game but got [" + playerIDs.length + "] agents");
			// return;
		}

		Map<String, Integer> unitName2PlayerIDs = new HashMap<String, Integer>();
		for(Integer playerID : playerIDs)
		{
			for(Unit.UnitView unitView : state.getUnits(playerID))
			{
				unitName2PlayerIDs.put(unitView.getTemplateView().getName().toLowerCase(), playerID);
			}
		}

		// expect "archer" and "footman" to be the only keys in the map...otherwise we have unknown units
		if(!unitName2PlayerIDs.containsKey("archer") || !unitName2PlayerIDs.containsKey("footman") || unitName2PlayerIDs.size() != 2)
		{
			System.err.println("ERROR: game should only have archer units & footman units");
			// return;
		}
		this.archerAgentID = unitName2PlayerIDs.get("archer");
		this.footmanAgentID = unitName2PlayerIDs.get("footman");

		// deal with the units of each player...archer first
		List<Integer> archerUnitIDs = state.getUnitIds(this.getArcherAgentID());
		if(archerUnitIDs.size() != 1)
		{
			System.err.println("ERROR: need the archer agent to control exactly 1 archer unit in this game");
			// return;
		}
		this.archerUnitID = archerUnitIDs.get(0);
		Unit.UnitView archerUnitView = state.getUnit(this.getArcherUnitID());
		this.archerPosition = new Coordinate(archerUnitView.getXPosition(), archerUnitView.getYPosition());
		this.archerHP = (double)archerUnitView.getHP();

		// now deal with the footmen
		List<Integer> footmanUnitIDs = state.getUnitIds(this.getFootmanAgentID());
		if(footmanUnitIDs.size() != 1)
		{
			System.err.println("ERROR: need the footman agent to control exactly 1 footman unit in this game");
			// return;
		}
		this.footmanUnitID = footmanUnitIDs.get(0);
		Unit.UnitView footmanUnitView = state.getUnit(this.getFootmanUnitID());
		this.footmanPosition = new Coordinate(footmanUnitView.getXPosition(), footmanUnitView.getYPosition());
		this.footmanHP = (double)footmanUnitView.getHP();

		// get all of the obstacles
		Set<Coordinate> obstaclePositions = new HashSet<Coordinate>();
		for(ResourceView obstacleView : state.getAllResourceNodes())
		{
			obstaclePositions.add(new Coordinate(obstacleView.getXPosition(), obstacleView.getYPosition()));
		}
		this.obstaclePositions = obstaclePositions;

		this.player = player;

		this.calculateUtilityValue();
	}

	public GameState(State.StateView state,
			         double archerHP,
			         Coordinate archerPosition,
			         Integer archerUnitID,
			         int archerAgentID,
			         double footmanHP,
			         Coordinate footmanPosition,
			         Integer footmanUnitID,
			         int footmanAgentID,
			         Set<Coordinate> obstaclePositions,
			         int player)
	{
		this.sepiaState = state;
		this.archerHP = archerHP;
		this.archerPosition = archerPosition;
		this.archerUnitID = archerUnitID;
		this.archerAgentID = archerAgentID;

		this.footmanHP = footmanHP;
		this.footmanPosition = footmanPosition;
		this.footmanUnitID = footmanUnitID;
		this.footmanAgentID = footmanAgentID;

		this.obstaclePositions = obstaclePositions;
		this.player = player;

		this.calculateUtilityValue();
	}

	// GETTERS
	public double getArcherHP() { return this.archerHP; }
	public Coordinate getArcherPosition() { return this.archerPosition; }
	public Integer getArcherUnitID() { return this.archerUnitID; }
	public int getArcherAgentID() { return this.archerAgentID; }
	public double getFootmanHP() { return this.footmanHP; }
	public Coordinate getFootmanPosition() { return this.footmanPosition; }
	public Integer getFootmanUnitID() { return this.footmanUnitID; }
	public int getFootmanAgentID() { return this.footmanAgentID; }
	public Set<Coordinate> getObstaclePositions() { return this.obstaclePositions; }
	public State.StateView getSepiaState() { return this.sepiaState; }
	public int getPlayer() { return this.player; }
	public double getUtilityValue() { return this.utilityValue; }

	// SETTERS...only one since the other values should not be modified
	public void setUtilityValue(double utilityValue) { this.utilityValue = utilityValue; }

	private int getNextPlayer()
	{
		if(this.getPlayer() == GameState.FOOTMANPLAYER)
		{
			return GameState.ARCHERPLAYER;
		} else
		{
			return GameState.FOOTMANPLAYER;
		}
	}

	public void calculateUtilityValue()
	{
		if(this.isTerminalState())
		{
			// assign utility
			if(this.getArcherHP() <= 0)
			{
				// perspective of footman....utility is great, footman won!
				this.setUtilityValue(Double.MAX_VALUE);
			} else if(this.getFootmanHP() <= 0)
			{
				// perspective of footman....utility is awful, footman lost!
				this.setUtilityValue(0.0);
			}
		}
	}

	public boolean isTerminalState()
	{
		return this.getArcherHP() <= 0.0 || this.getFootmanHP() <= 0.0;
	}

	public List<TreeNode> getChildren()
	{
		List<TreeNode> children = new ArrayList<TreeNode>(5);

		double unitAttackDamage, unitRange;
		Integer friendlyUnitID = null, enemyUnitID = null;
		Coordinate friendlyUnitPosition = null, enemyUnitPosition = null;
		if(this.getPlayer() == GameState.FOOTMANPLAYER)
		{
			friendlyUnitID = this.getFootmanUnitID();
			enemyUnitID = this.getArcherUnitID();

			friendlyUnitPosition = this.getFootmanPosition();
			enemyUnitPosition = this.getArcherPosition();

			unitRange = Math.sqrt(2);
		} else // archer
		{
			friendlyUnitID = this.getArcherUnitID();
			enemyUnitID = this.getFootmanUnitID();

			friendlyUnitPosition = this.getArcherPosition();
			enemyUnitPosition = this.getFootmanPosition();

			unitRange = this.getSepiaState().getUnit(friendlyUnitID).getTemplateView().getRange();
		}
		unitAttackDamage = this.getSepiaState().getUnit(friendlyUnitID).getTemplateView().getBasicAttack();


		// try to move the unit first...only move in cardinal directions in this game
		Direction[] allDirections = Direction.values();
		for(int directionIdx = 0; directionIdx < allDirections.length; directionIdx += 2) // skip diagonal directions
		{
			Coordinate newUnitPosition = new Coordinate(friendlyUnitPosition.getX() + allDirections[directionIdx].xComponent(),
					                                    friendlyUnitPosition.getY() + allDirections[directionIdx].yComponent());
			if(this.isLegalPosition(newUnitPosition, obstaclePositions, enemyUnitPosition))
			{
				// make a child for moving there
				GameState childGameState = null;
				if(this.getPlayer() == GameState.FOOTMANPLAYER)
				{
					childGameState = new GameState(this.getSepiaState(),

						      this.getArcherHP(),
						      this.getArcherPosition(),
						      this.getArcherUnitID(),
						      this.getArcherAgentID(),

						      this.getFootmanHP(),
						      newUnitPosition,
						      this.getFootmanUnitID(),
						      this.getFootmanAgentID(),

						      this.getObstaclePositions(),
						      this.getNextPlayer());
				} else // archer
				{
					childGameState = new GameState(this.getSepiaState(),

						      this.getArcherHP(),
						      newUnitPosition,
						      this.getArcherUnitID(),
						      this.getArcherAgentID(),

						      this.getFootmanHP(),
						      this.getFootmanPosition(),
						      this.getFootmanUnitID(),
						      this.getFootmanAgentID(),

						      this.getObstaclePositions(),
						      this.getNextPlayer());
				}

				children.add(new TreeNode(
						Collections.singletonMap(friendlyUnitID,
												 Action.createPrimitiveMove(friendlyUnitID, allDirections[directionIdx])),
						childGameState
						));
			}
		}

		// now try to attack the enemy unit...our unit cannot move
		if(this.canAttack(friendlyUnitPosition, unitRange, enemyUnitPosition))
		{
			GameState childGameState = null;
			if(this.getPlayer() == GameState.FOOTMANPLAYER)
			{
				childGameState = new GameState(this.getSepiaState(),

					      this.getArcherHP() - unitAttackDamage,
					      this.getArcherPosition(),
					      this.getArcherUnitID(),
					      this.getArcherAgentID(),

					      this.getFootmanHP(),
					      this.getFootmanPosition(),
					      this.getFootmanUnitID(),
					      this.getFootmanAgentID(),

					      this.getObstaclePositions(),
					      this.getNextPlayer());
			} else // archer
			{
				childGameState = new GameState(this.getSepiaState(),

					      this.getArcherHP(),
					      this.getArcherPosition(),
					      this.getArcherUnitID(),
					      this.getArcherAgentID(),

					      this.getFootmanHP() - unitAttackDamage,
					      this.getFootmanPosition(),
					      this.getFootmanUnitID(),
					      this.getFootmanAgentID(),

					      this.getObstaclePositions(),
					      this.getNextPlayer());
			}

			children.add(new TreeNode(
					Collections.singletonMap(friendlyUnitID,
											 Action.createPrimitiveAttack(friendlyUnitID, enemyUnitID)),
					childGameState
					));
		}

		return children;
	}

	public boolean canAttack(Coordinate attackingUnitPosition, double attackingUnitRange, Coordinate targetedUnitPosition)
	{
		double distance = attackingUnitPosition.euclideanDistanceTo(targetedUnitPosition);
		return distance <= attackingUnitRange;
	}

	public boolean isLegalPosition(Coordinate position, Set<Coordinate> obstaclePositions, Coordinate enemyPosition)
	{
		return position.getX() >= 0 && position.getX() < this.getSepiaState().getXExtent() &&
			   position.getY() >= 0 && position.getY() < this.getSepiaState().getYExtent() &&
			   !obstaclePositions.contains(position) && !enemyPosition.equals(position);
	}
}
