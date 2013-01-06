

package geniusect.ai;

import geniusect.Action;
import geniusect.Attack;
import geniusect.Battle;
import geniusect.Change;
import geniusect.Move;
import geniusect.Pokemon;
import geniusect.Pokequations;
/**
 * The node used in the Minimax algorithm.
 * @author TeamForretress
 * @see geniusect.ai.MinimaxAI
 * @see geniusect.ai.MinimaxAI#minimaxAlgorithm(MinimaxNode, int)
 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
 */
public class MinimaxNode
{
	/**What Battle is being simulated.*/
	private Battle battle; 
	/**The parent of this node.*/
	private MinimaxNode parent;
	/**The children of this node.*/
	private MinimaxNode[] children; 
	/**
	 * The winning child of this node after Minimax calculation.
	 * @see #children
	 */
	private MinimaxNode result;
	
	/**
	 * The team of the player who is making this decision.
	 * @see geniusect.Pokemon
	 */
	private Pokemon[] ourTeam;
	/**
	 * The team of the enemy who is making this decision.
	 * @see geniusect.Pokemon
	 */
	private Pokemon[] enemyTeam;
	/**
	 * Which Pokemon we have out at this time in the simulation.
	 * @see geniusect.Pokemon
	 */
	private Pokemon ourActive;
	/**
	 * Which Pokemon the opponent has out at this time in the simulation.
	 * @see geniusect.Pokemon
	 */
	private Pokemon enemyActive;
	/**
	 * The choice this node determined our active Pokemon will take against the enemy's active Pokemon.
	 * @see geniusect.Pokemon
	 * @see geniusect.Action
	 * @see #ourActive
	 * @see #enemyActive
	 */
	private Action decision;
	
	/**The move we are going to use, if any.*/
	private Move usingMove;
	/**The Pokemon we are going to switch to, if any.*/
	private Pokemon switchingTo;
	/**How far down the tree this node is.*/
	private int count = 0;
	
	/**How far down to go.*/
	private static int depth = 0;
	/**
	 * How much damage enemyTeam has done to ourTeam (in total).
	 * @see ourTeam
	 * @see enemyTeam
	 */
	private int damageDoneToUs = 0;
	/**
	 * How much damage ourTeam has done to enemyTeam (in total).
	 * @see ourTeam
	 * @see enemyTeam
	 */
	private int damageDoneToEnemy = 0;
	/**Whose turn is it? <i>1</i> is the user performing the Minimax calculation, <i>-1</i> is the enemy.*/
	private int player = 1;
	/**Should this node create more children?*/
	private boolean createChildren = true;
	/**Is this node the root node?*/
	private boolean isRoot = false;
	
	/**
	 The parent of all nodes used in the Minimax algorithm.
	* @param b (Battle): The Battle this node is going to use.
	* @param depth (int): How many iterations to create.
	* @see geniusect.ai.MinimaxAI
 	* @see geniusect.ai.MinimaxAI#minimaxAlgorithm(MinimaxNode, int)
 	* @see	{@link http://en.wikipedia.org/wiki/Minimax}
	*/
	public MinimaxNode(Battle b, int depth) 
	{
		battle = new Battle(b);
		ourTeam = battle.getTeam(0).getPokemon().clone();
		enemyTeam = battle.getTeam(1).getPokemon().clone();
		int ourID = battle.getTeam(0).getActive().getID();
		int enemyID = battle.getTeam(1).getActive().getID();
		ourActive = ourTeam[ourID];
		enemyActive = enemyTeam[enemyID];
		ourActive.getAbility().setBattle(battle);
		enemyActive.getAbility().setBattle(battle);
		System.err.println(ourActive);
		System.err.println(enemyActive);
		isRoot = true;
	}
	
	/**
	 * A child of a Minimax node. Automatically calls addChild() on the parent node.
	 * @param parent - The parent of the new Node.
	 * @see geniusect.ai.MinimaxAI
 	 * @see geniusect.ai.MinimaxAI#minimaxAlgorithm(MinimaxNode, int)
 	 * @see geniusect.ai.MinimaxNode#addChild(MinimaxNode)
 	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	protected MinimaxNode(MinimaxNode parent)
	{
		count = parent.count + 1;
		if(count > depth)
		{
			parent.setChildValues(this);
			endIterations();
		}
		if(createChildren && parent.parent != this)
		{
			this.parent = parent;
			this.parent.addChild(this);
		}
	}
	
	/**
	 * Adds a child to a Minimax node.
	 * Simulates a turn's events and checks to make sure the child's Pokemon is still alive before continuing.
	 * @param child - The child to add.
	 */
	private void addChild(MinimaxNode child)
	{
		setChildValues(child);
		expandArray(child);
	}
	
	/**
	 * Sets the values of a child node to the values within this one.
	 * @param child (MinimaxNode): The child to give values to.
	 */
	private void setChildValues(MinimaxNode child)
	{
		System.err.println(ourActive);
		System.err.println(enemyActive);
		child.enemyTeam = ourTeam.clone();
		child.ourTeam = enemyTeam.clone();
		child.ourActive = child.ourTeam[enemyActive.getID()];
		child.enemyActive = child.enemyTeam[ourActive.getID()];
		child.damageDoneToUs = damageDoneToEnemy;
		child.damageDoneToEnemy = damageDoneToUs;
		child.battle = battle;
		child.battle.newTurn(child.ourActive.getTeam());
		child.battle.setNextTurn(decision);
		child.ourActive.getTeam().setBattle(child.battle);
		child.enemyActive.getTeam().setBattle(child.battle);
		child.ourActive.getAbility().setBattle(child.battle);
		child.enemyActive.getAbility().setBattle(child.battle);
		GeniusectAI.simulateTurn(child);
		if(decision != null)
		{
			decision.sendToShowdown(child);
			printParentRecursive(this);
		}
		else
			child.enemyActive = new Pokemon(ourActive);
		if(child.ourActive.getHealth() <= 0)
		{
			//This is all run if the active Pokemon has died.
			System.err.println(child.ourActive.getName()+" has died after "+(count / 2)+" turns!");
			Pokemon change = child.ourActive.onDie();
			if(change == null)
			{
				child.createChildren = false;
				child.damageDoneToUs = Integer.MAX_VALUE;
				child.count = 0;
				child.decision = new Attack(Pokequations.bestMove(child.ourActive, child.enemyActive),child.ourActive,child.enemyActive,child.battle);
				MinimaxAI.score(child, child.decision);
				printParentRecursive(this);
			}
			else
			{
				child.ourActive = new Pokemon(change);
				child.decision = null;
			}
		}
		else
			System.err.println(child.ourActive.getName()+" has "+child.ourActive.getHealth()+" hp");
	}
	
	/**
	 * Expands the MinimaxNode array to fit the new child.
	 * @param child - The child to put into the new array.
	 */
	private void expandArray(MinimaxNode child)
	{
		child.player = -player;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] == null)
				child.enemyTeam[i] = null;
			else
				child.enemyTeam[i] = new Pokemon(ourTeam[i]);
			if(enemyTeam[i] == null)
				child.ourTeam[i] = null;
			else
				child.ourTeam[i] = new Pokemon(enemyTeam[i]);
		}
		if(children == null)
		{
			children = new MinimaxNode[1];
			children[0] = child;
			return;
		}
		MinimaxNode[] newChild = new MinimaxNode[children.length + 1];
		for(int i = 0; i < children.length + 1; i++)
		{
			if(i == children.length)
			{
				newChild[i] = child;
			}
			else
			{
				newChild[i] = children[i];
			}
		}
		children = newChild;
	}
	
	/**
	 * Sets all children of this node to null and prevents the node from making more children.
	 */
	protected void endIterations()
	{
		createChildren = false;
		children = null;
	}
	
	/**
	 * Recursively prints a node and all its parents.
	 * @param node - The node to print the parents of.
	 */
	protected static void printParentRecursive(MinimaxNode node)
	{
		if(node.parent == null)
		{
			System.err.println(node.ourActive.getName()+" is the parent node.");
		}
		else
		{
			System.err.println(node.ourActive.getName()+": "+node.decision.name+". Depth: "+node.getCount());
			printParentRecursive(node.parent);
		}
	}
	
	
	
	/*
	 * 
	 * GETTER AND SETTER METHODS
	 * 
	 */
	
	/**
	 * Set the action this node will take.
	 * @param action - An <i>Action</i> that will be taken by this node.
	 * @see geniusect.Action
	 */
	protected void setDecision(Action action)
	{
		if(action instanceof Attack)
		{
			usingMove = ((Attack) action).move;
			if(usingMove == null)
				System.err.println(ourActive.getName()+" did not make a move!");
		}
		else if(action instanceof Change)
		{
			switchingTo = ((Change) action).switchTo;
			if(switchingTo == null)
				System.err.println(ourActive.getName()+" did not switch!");
		}
		decision = action;
	}
	
	/**
	 * Get the action this node will take.
	 * If our decision is null, switches to generic logic and prints an error to the console.
	 * @return Action - An <i>Action</i> that will be taken by this node.
	 * @see geniusect.Action
	 */
	protected Action getDecision()
	{
		if(decision == null)
		{
			System.err.println(ourActive.getName()+" did not make a move!");
			decision = GenericAI.bestMove(ourActive, enemyActive, ourTeam, battle);
		}
		return decision;
	}
	
	/**
	 * Gets the score of our decision.
	 * If our decision is null, switches to generic logic and prints an error to the console.
	 * @return int - The score.
	 * @see #getDecision()
	 */
	protected int getDecisionScore()
	{
		getDecision();
		MinimaxAI.score(this, decision);
		return decision.score;
	}

	/**@return MinimaxNode - This node's result after Minimax calculation. NULL if no result.*/
	protected MinimaxNode getResult()
	{
		return result;
	}
	
	/**
	 * Sets the <i>result</i> MinimaxNode 
	 * (the node with the best score after all scores have been tallied) for this branch.
	 * @param resultNode - The node to set the result to.
	 */
	protected void setResult(MinimaxNode resultNode)
	{
		result = resultNode;
	}
	
	/** @return boolean - TRUE if we are the root node, else FALSE.*/
	protected boolean getIsRoot()
	{
		return isRoot;
	}
	
	/**
	 * Returns all this node's children.
	 * @return MinimaxNode[] - This node's children.
	 * @see #children
	 */
	protected MinimaxNode[] getChildren()
	{
		if(children == null)
			return new MinimaxNode[0];
		return children;
	}
	/**
	 * Gets how far down this node is down the tree.
	 * @return int - How far down the tree this node is.
	 * @see #count
	 */
	protected int getCount()
	{
		return count;
	}
	
	/**
	 * Gets the parent of this node.
	 * @return MinimaxNode - This node's parent, or NULL if there is no parent.
	 * @see #parent
	 */
	protected MinimaxNode getParent()
	{
		return parent;
	}
	
	/**
	 * Gets the damage done to the enemy.
	 * @return int - the damage WE did to the enemy.
	 * @see #damageDoneToEnemy
	 */
	protected int getDamageDoneEnemy()
	{
		return damageDoneToEnemy;
	}
	
	/**
	 * Gets the damage done to us.
	 * @return int - the damage the ENEMY did to us.
	 * @see #damageDoneToUs
	 */
	protected int getDamageDoneUs()
	{
		return damageDoneToUs;
	}
	
	/**
	 * Adds <i>damage</i> to the damage done to us.
	 * @param damage (int): The damage to add to our total.
	 */
	public void addDamageDoneUs(int damage)
	{
		damageDoneToUs += damage;
	}
	
	/**
	 * Returns the enemy's active Pokemon.
	 * @return Pokemon - The enemy's active Pokemon at this point in the simulation.
	 */
	protected Pokemon getEnemyPokemon()
	{
		return enemyActive;
	}
	
	/**
	 * Get the active enemy Pokemon's name.
	 * @return String - The enemy Pokemon name.
	 */
	protected String getEnemyName()
	{
		return enemyActive.getName();
	}
	
	/**
	 * Returns our active Pokemon.
	 * @return Pokemon - Our active Pokemon at this point in the simulation.
	 */
	public Pokemon getOurPokemon()
	{
		return ourActive;
	}
	
	/**
	 * Sets our active Pokemon to a different active Pokemon.
	 * @param newActive - Pokemon: The Pokemon to make active.
	 */
	public void setOurPokemon(Pokemon newActive)
	{
		ourActive = newActive;
	}
	
	/**
	 * Returns the Pokemon at teamslot <i>i</i>.
	 * @param i - int: The index of the wanted Pokemon.
	 * @return Pokemon - The Pokemon at index <i>i</i>.
	 */
	protected Pokemon getOurTeam(int i)
	{
		return ourTeam[i];
	}
	
	/**
	 * Returns this node's player.
	 * @return int - This node's player.
	 */
	protected int getPlayer()
	{
		return player;
	}
	
	/**
	 * Returns the name of our active Pokemon at this point in the simulation.
	 * @return String - Our active Pokemon's name.
	 */
	protected String getOurName()
	{
		return ourActive.getName();
	}
	
	/**
	 * Returns the name of the action we will take.
	 * @return String - The name of our decision.
	 */
	protected String getDecisionName()
	{
		if(decision.name == null)
			System.err.println(ourActive.getName()+"'s decision was null!");
		return decision.name;
	}

	/**
	 * Returns our Battle.
	 * @return (Battle): Our current battle.
	 */
	protected Battle getBattle() 
	{
		return battle;
	}

	/**
	 * Switches an Enemy Pokemon for this node.
	 * @param switchTo (Pokemon): The Pokemon the enemy switched to.
	 */
	public void setEnemyPokemon(Pokemon switchTo) 
	{
		enemyActive = switchTo;
	}
}
