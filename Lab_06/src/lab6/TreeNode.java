package lab6;

import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;

public class TreeNode
{

	private Map<Integer, Action> actions;
	private GameState state;
	private TreeNode parent;

	public TreeNode(GameState state)
	{
		this.actions = null;
		this.state = state;
		this.parent = null;
	}

	public TreeNode(Map<Integer, Action> actions, GameState state)
	{
		this.actions = actions;
		this.state = state;
		this.parent = null;
	}

	public TreeNode(Map<Integer, Action> actions, GameState state, TreeNode parent)
	{
		this.actions = actions;
		this.state = state;
		this.parent = parent;
	}

	public Map<Integer, Action> getActions() { return this.actions; }
	public final GameState getGameState() { return this.state; }
	public final TreeNode getParentNode() { return this.parent; }

	public void setActions(Map<Integer, Action> actions) { this.actions = actions; }
	private void setParentNode(TreeNode parent) { this.parent = parent; }

	public List<TreeNode> getChildren()
	{
		List<TreeNode> children = this.getGameState().getChildren();
		for(TreeNode child : children)
		{
			child.setParentNode(this);
		}
		return children;
	}

}
