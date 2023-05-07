package lab5;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.TreeNode;

import edu.cwru.sepia.action.ActionType;
import lab6.Coordinate;
import lab6.GameState;

public class Heuristic
{

	public static List<lab6.TreeNode> orderChildrenWithHeuristics(List<lab6.TreeNode> list)
	{
		for(lab6.TreeNode child : list)
		{
			Heuristic.applyDistanceHeuristic(child.getGameState());
			Heuristic.applyHealthHeuristic(child.getGameState());
			Heuristic.applyDamageHeuristic(child);
		}

		Collections.sort(list, new Comparator<TreeNode>()
		{
			public int compare(TreeNode a, TreeNode b)
			{
				int utilCompare = Double.compare(((lab6.TreeNode) a).getGameState().getUtilityValue(), b.getGameState().getUtilityValue());
				return ((lab6.TreeNode) a).getGameState().getPlayer() == GameState.FOOTMANPLAYER ? utilCompare : -1 * utilCompare;
			}
		});
		return list;
	}

	/**
	 * The job of this method is to modify the utility value of a node based on a heuristic that judges the quality
	 * of a state due based on the distance of the footman to the archer. Higher utility values are assigned
	 * when the footman is closer to the agent (i.e. "rush" the archer)
	 * @param node
	 */
	public static void applyDistanceHeuristic(GameState node)
	{
		Coordinate footmanPosition = node.getFootmanPosition();
		Coordinate archerPosition = node.getArcherPosition();

		node.setUtilityValue(node.getUtilityValue() + 1.0 / footmanPosition.euclideanDistanceTo(archerPosition));
	}

	/**
	 * The job of this method is to modify the utility value of a node based on a heuristic that judges the quality
	 * of a state due based on the health of the units. The state is worth more (i.e. "higher" utility value)
	 * the more the archer is damaged, and is worth less when the footman is damaged
	 * @param node
	 */
	public static void applyHealthHeuristic(GameState node)
	{
		double archerHP = node.getArcherHP();
		double footmanHP = node.getFootmanHP();

		double archerBaseHP = node.getSepiaState().getUnit(node.getArcherUnitID()).getTemplateView().getBaseHealth();
		double footmanBaseHP = node.getSepiaState().getUnit(node.getFootmanUnitID()).getTemplateView().getBaseHealth();

		// scale the health by how much the should be if undamaged
		double percentageArcherHPRemaining = archerHP / archerBaseHP;
		double percentageFootmanHPRemaining = footmanHP / footmanBaseHP;

		// always want to make sure utility values are positive in this example
		node.setUtilityValue(node.getUtilityValue() + (percentageArcherHPRemaining - percentageFootmanHPRemaining + 1));
	}

	public static void applyDamageHeuristic(lab6.TreeNode child)
	{
		double damageBonus = ((lab6.TreeNode) child).getGameState().getArcherHP();
		double basicFootmanAttack = ((lab6.TreeNode) child).getGameState().getSepiaState().getUnit(child.getGameState().getFootmanUnitID()).getTemplateView().getBasicAttack();
		if(((lab6.TreeNode) child).getActions().containsKey(((lab6.TreeNode) child).getGameState().getFootmanUnitID()) &&
				((lab6.TreeNode) child).getActions().get(((lab6.TreeNode) child).getGameState().getFootmanUnitID()).getType() == ActionType.PRIMITIVEATTACK)
		{
			damageBonus += basicFootmanAttack;
		}
		((lab6.TreeNode) child).getGameState().setUtilityValue(((lab6.TreeNode) child).getGameState().getUtilityValue() + damageBonus);
	}

}
