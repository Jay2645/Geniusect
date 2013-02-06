

package geniusectai.minimax;

import geniusectai.generic.GenericAI;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Damage;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

import java.util.ArrayList;

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
	private ArrayList<MinimaxNode> children = new ArrayList<MinimaxNode>(); 
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
		battle.setTurnCount(0);
		ourTeam = battle.getTeam(0, true).getPokemon();
		enemyTeam = battle.getTeam(1, true).getPokemon();
		int ourID = battle.getTeam(0, true).getActive().getID();
		int enemyID = battle.getTeam(1, true).getActive().getID();
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
		if(parent.createChildren)
		{
			setValues(parent);
			if(ourActive != null)
				parent.children.add(this);
		}
	}
	
	/**
	 * A child of a Minimax node. Automatically calls addChild() on the parent node.
	 * @param parent - The parent of the new Node.
	 * @see geniusect.ai.MinimaxAI
 	 * @see geniusect.ai.MinimaxAI#minimaxAlgorithm(MinimaxNode, int)
 	 * @see geniusect.ai.MinimaxNode#addChild(MinimaxNode)
 	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	protected MinimaxNode(MinimaxNode parent, Pokemon newActive)
	{
		count = parent.count + 1;
		if(parent.createChildren)
		{
			ourTeam = parent.enemyTeam.clone();
			ourActive = ourTeam[newActive.getID()];
			setValues(parent);
			if(ourActive != null)
				parent.children.add(this);
		}
	}
	
	/**
	 * Sets our values to be the opposite of our parent's.
	 * @param parent (MinimaxNode): Our parent.
	 */
	private void setValues(MinimaxNode parent)
	{
		this.parent = parent;
		enemyTeam = parent.ourTeam.clone();
		if(ourActive == null)
		{
			ourTeam = parent.enemyTeam.clone();
			ourActive = new Pokemon(ourTeam[parent.enemyActive.getID()]);
		}
		enemyActive = new Pokemon(enemyTeam[parent.ourActive.getID()]);
		/*if(ourActive == null)
		{
			endIterations();
			return;
		}*/
		ourActive.setEnemy(enemyActive);
		enemyActive.setEnemy(ourActive);
		damageDoneToUs = damageDoneToEnemy;
		damageDoneToEnemy = damageDoneToUs;
		battle = new Battle(parent.battle);
		ourActive.getTeam().setBattle(battle);
		enemyActive.getTeam().setBattle(battle);
		ourActive.getAbility().setBattle(battle);
		enemyActive.getAbility().setBattle(battle);
		if(ourActive.getHealth() <= 0)
		{
			ourActive = null;
			for(int i = 0; i < ourTeam.length; i++)
			{
				if(ourTeam[i] == null || !ourTeam[i].isAlive())
					continue;
				if(ourActive == null)
				{
					ourActive = ourTeam[i];
					continue;
				}
				new MinimaxNode(parent,ourTeam[i]);
			}
			if(ourActive == null)
			{
				parent.children.remove(this);
				parent = null;
				return;
			}
		}
		System.err.println(ourActive.getName()+" has "+ourActive.getHealth()+" hp");
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
			Attack attack = (Attack) action;
			usingMove = attack.move;
			if(usingMove == null)
			{
				System.err.println(enemyActive.getName()+" did not make a move!");
				return;
			}
			Damage damageDone = new Damage(usingMove, enemyActive, ourActive);
			int damageAmount = damageDone.applyDamage();
			System.out.println(enemyActive.getName()+" used "+usingMove.name+"!");
			if(ourActive.isAlive())
			{
				System.err.println("Damage done: "+damageAmount+"%");
				damageDoneToUs += damageAmount;
			}
			else
			{
				ourActive = null;
				for(int i = 0; i < ourTeam.length; i++)
				{
					if(ourTeam[i] == null || !ourTeam[i].isAlive())
						continue;
					if(ourActive == null)
					{
						ourActive = ourTeam[i];
						damageDoneToUs += damageAmount;
						continue;
					}
					MinimaxNode node = new MinimaxNode(parent,ourTeam[i]);
					node.damageDoneToUs += damageAmount;
				}
				if(ourActive == null)
				{
					parent.children.remove(this);
					parent = null;
				}
			}
		}
		else if(action instanceof Change)
		{
			switchingTo = ((Change) action).switchTo;
			if(switchingTo == null)
			{
				System.err.println(enemyActive.getName()+" did not switch!");
				return;
			}
			enemyActive = switchingTo;
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
		MinimaxAI.score(this);
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
		MinimaxNode[] childArray = new MinimaxNode[0];
		if(children == null || children.isEmpty())
			return childArray;
		childArray = children.toArray(childArray);
		return childArray;
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
		if(decision == null)
		{
			System.err.println(ourActive.getName()+"'s decision was null!");
			return null;
		}
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

	/**
	 * Returns our Pokemon team as an array.
	 * @return (Pokemon[]): Our Pokemon team.
	 */
	public Pokemon[] getOurTeam() 
	{
		return ourTeam;
	}
}
