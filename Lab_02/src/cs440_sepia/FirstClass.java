package cs440_sepia;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceNode.Type;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class FirstClass extends Agent {
	List<Integer> GoldResourceIDs;
	int curGold = 0;
	List<Integer> TreeResourceIDs;
	int curTree = 0;
	
	public FirstClass(int arg0) {
		super(arg0);
	}

	@Override
	public Map<Integer, Action> initialStep(StateView stateview, HistoryView historyview) {
		//printStats(stateview);
		this.GoldResourceIDs = stateview.getResourceNodeIds(Type.GOLD_MINE);
		this.TreeResourceIDs = stateview.getResourceNodeIds(Type.TREE);
		Map<Integer, Action> actions = new HashMap<Integer, Action>();

        // this will return a list of all of your units
        // You will need to check each unit ID to determine the unit's type
        List<Integer> myUnitIds = stateview.getUnitIds(playernum);

        // These will store the Unit IDs that are peasants and townhalls respectively
        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();

        // This loop will examine each of our unit IDs and classify them as either
        // a Townhall or a Peasant
        for(Integer unitID : myUnitIds)
        {
                // UnitViews extract information about a specified unit id
                // from the current state. Using a unit view you can determine
                // the type of the unit with the given ID as well as other information
                // such as health and resources carried.
                UnitView unit = stateview.getUnit(unitID);

                // To find properties that all units of a given type share
                // access the UnitTemplateView using the `getTemplateView()`
                // method of a UnitView instance. In this case we are getting
                // the type name so that we can classify our units as Peasants and Townhalls
                String unitTypeName = unit.getTemplateView().getName();

                if(unitTypeName.equals("TownHall"))
                        townhallIds.add(unitID);
                else if(unitTypeName.equals("Peasant"))
                        peasantIds.add(unitID);
                else
                        System.err.println("Unexpected Unit type: " + unitTypeName);
        }

        // get the amount of wood and gold you have in your Town Hall

    	Action action = null;
    	for(Integer peasantID : peasantIds) {
    		action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, this.GoldResourceIDs.get(this.curGold));
    		this.curGold = this.curGold + 1;
    		actions.put(peasantID, action);
    	}

		
		return actions;
	}
	
	public void printStats(StateView stateview) {
		//TASK 3
		List<Integer> unitIDs = stateview.getUnitIds(playernum);
		for(Integer unitID : unitIDs)
		{
		  UnitView unitView = stateview.getUnit(unitID);

		  TemplateView templateView = unitView.getTemplateView();
		  System.out.println("Unit information----------------------");
		  System.out.println(templateView.getName() + " has ID: " + unitID);
		  System.out.println("cargo amount: " + unitView.getCargoAmount());
		  System.out.println("hp left: " + unitView.getHP());
		  System.out.println("position: " + unitView.getXPosition() + ", " + unitView.getYPosition());
		  System.out.println("");
		}
		System.out.println("");
		List<Integer> GoldResourceIDs = stateview.getResourceNodeIds(Type.GOLD_MINE);
		for (Integer resourceID: GoldResourceIDs) 
		{
		  ResourceView resource = stateview.getResourceNode(resourceID);
		  System.out.println("Gold information----------------------");
		  System.out.println("Position: "+ resource.getXPosition() + ", " + resource.getYPosition());
		  System.out.println("Amount left: " + resource.getAmountRemaining());
		  System.out.println("");
		}
		System.out.println("");
		List<Integer> TreeResourceIDs = stateview.getResourceNodeIds(Type.TREE);
		for (Integer resourceID: TreeResourceIDs) 
		{
		  ResourceView resource = stateview.getResourceNode(resourceID);
		  System.out.println("Tree information----------------------");
		  System.out.println("Position: "+ resource.getXPosition() + ", " + resource.getYPosition());
		  System.out.println("Amount left: " + resource.getAmountRemaining());
		  System.out.println("");
		}
	}

	@Override
	public void loadPlayerData(InputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView newstate, HistoryView statehistory) {
		// TODO Auto-generated method stub
		Map<Integer, Action> actions = new HashMap<Integer, Action>();

        // this will return a list of all of your units
        // You will need to check each unit ID to determine the unit's type
        List<Integer> myUnitIds = newstate.getUnitIds(playernum);

        // These will store the Unit IDs that are peasants and townhalls respectively
        List<Integer> peasantIds = new ArrayList<Integer>();
        List<Integer> townhallIds = new ArrayList<Integer>();

        // This loop will examine each of our unit IDs and classify them as either
        // a Townhall or a Peasant
        for(Integer unitID : myUnitIds)
        {
                // UnitViews extract information about a specified unit id
                // from the current state. Using a unit view you can determine
                // the type of the unit with the given ID as well as other information
                // such as health and resources carried.
                UnitView unit = newstate.getUnit(unitID);

                // To find properties that all units of a given type share
                // access the UnitTemplateView using the `getTemplateView()`
                // method of a UnitView instance. In this case we are getting
                // the type name so that we can classify our units as Peasants and Townhalls
                String unitTypeName = unit.getTemplateView().getName();

                if(unitTypeName.equals("TownHall"))
                        townhallIds.add(unitID);
                else if(unitTypeName.equals("Peasant"))
                        peasantIds.add(unitID);
                else
                        System.err.println("Unexpected Unit type: " + unitTypeName);
        }

        // get the amount of wood and gold you have in your Town Hall
        int currentGold = newstate.getResourceAmount(playernum, ResourceType.GOLD);
        int currentWood = newstate.getResourceAmount(playernum, ResourceType.WOOD);

        //List<Integer> goldMines = newstate.getResourceNodeIds(Type.GOLD_MINE);
        //List<Integer> trees = newstate.getResourceNodeIds(Type.TREE);
        int currentStep = newstate.getTurnNumber();
        for(ActionResult feedback : statehistory.getCommandFeedback(playernum, currentStep-1).values()) {
        	if(feedback.getFeedback() != ActionFeedback.INCOMPLETE) {
        		Action action = null;
        		Integer peasantID = feedback.getAction().getUnitId();
        		if(newstate.getUnit(peasantID).getCargoAmount() > 0)
                {
                        // If the agent is carrying cargo then command it to deposit what its carrying at the townhall.
                        // Here we are constructing a new TargetedAction. The first parameter is the unit being commanded.
                        // The second parameter is the action type, in this case a COMPOUNDDEPOSIT. The actions starting
                        // with COMPOUND are convenience actions made up of multiple move actions and another final action
                        // in this case DEPOSIT. The moves are determined using A* planning to the location of the unit
                        // specified by the 3rd argument of the constructor.
                        action = new TargetedAction(peasantID, ActionType.COMPOUNDDEPOSIT, townhallIds.get(0));
                }
                else
                {
                        // If the agent isn't carrying anything instruct it to go collect either gold or wood
                        // whichever you have less of
                        if(currentGold < currentWood)
                        {
                                action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, this.GoldResourceIDs.get(this.curGold));
                                if((this.curGold + 1) >= this.GoldResourceIDs.size()){
                                	this.curGold = 0;
                                }
                                else {
                                	this.curGold = this.curGold + 1;
                                }
                        }
                        else
                        {
                                action = new TargetedAction(peasantID, ActionType.COMPOUNDGATHER, this.TreeResourceIDs.get(this.curTree));
                                if(this.curTree + 1 >= this.TreeResourceIDs.size()){
                                	this.curTree = 0;
                                }
                                else {
                                	this.curTree = this.curTree + 1;
                                }
                        }
                }
        		actions.put(peasantID, action);
        	}
        }
	    return actions;
	}

	@Override
	public void savePlayerData(OutputStream arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView arg0, HistoryView arg1) {
		// TODO Auto-generated method stub

	}
	
	public int[] BFS(StateView newstate) {
		//NO solution given
		return null;
	}

}
