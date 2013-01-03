package geniusect.ai;

import geniusect.Action;
import geniusect.Attack;
import geniusect.Battle;
import geniusect.Change;
import geniusect.Move;
import geniusect.Pokemon;
import geniusect.Team;

/**
 * All AI logic pertaining to the MinimaxAI algorithm.
 * @author TeamForretress
 * @see geniusect.ai.MinimaxAI#miniMaxAlgorithm(MinimaxNode, int)
 * @see geniusect.ai.MinimaxNode
 * @see {@link http://en.wikipedia.org/wiki/Minimax}
 */
public class MinimaxAI {
	/**
	 * Finds the best move we can use or Pokemon we can switch to using and returns the Action.
	 * @return Action - the best choice in the given circumstance using Minimax logic.
	 * @see geniusect.ai.MinimaxAI#minimax(int)
	 * @see {@link http://en.wikipedia.org/wiki/Minimax}
	 */
	private static Battle battle;
	
	protected static Action minimax(int depth, Battle b)
	{
		battle = b;
		depth *=2;
		MinimaxNode decide = scoreTree(depth);
		minimaxAlgorithm(decide, depth);
		Action decision;
		if(decide.getDecision() == null)
		{
			System.err.println("The minimax result was null! Switching to generic logic.");
			decision = GenericAI.bestMove(battle);
		}
		else
			decision = decide.getDecision();
		GeniusectAI.markDecision(decision);
		return decision;
	}
	/**
	 * Initializes and scores a minimax tree.
	 * @param depth - How many branches to iterate down (increases exponentially).
	 * @return MinimaxNode - The parent node of the entire tree.
	 * @see geniusect.ai.MinimaxAI#minimax(int)
	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	private static MinimaxNode scoreTree(int depth)
	{
		MinimaxNode node = new MinimaxNode(battle, depth);
		scoreTree(node,depth);
		return node;
	}
	
	/**
	 * Scores a minimax branch.
	 * @param node - The MinimaxNode to add branches to.
	 * @param depth - How many branches to iterate down (increases exponentially).
	 * @return MinimaxNode - The scored version of <i>node</i>.
	 * @see geniusect.ai.MinimaxAI#minimax(int)
	 * @see geniusect.ai.MinimaxAI#scoreTree(int)
	 * @see geniusect.ai.MinimaxAI#score(MinimaxNode)
	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	private static MinimaxNode scoreTree(MinimaxNode node, int depth)
	{
		Pokemon ourActive = node.getOurPokemon();
		Pokemon enemyActive = node.getEnemyPokemon();
		System.out.println("Iterations to go: "+depth);
		System.out.println(	"Player "+node.getPlayer() + " is using "+ourActive.getName() +
							" ("+ourActive.getHealth()+" hp).");
		if(depth > 0)
		{
			boolean madeMove = false;
			for(int i = 0; i < 6; i++)
			{
				//Calculate every possible switch.
				if(node.getOurTeam(i) == null || !node.getOurTeam(i).isAlive() || node.getOurTeam(i).getName() == ourActive.getName())
					continue;
				else
				{
					madeMove = true;
					node.setDecision(new Change(node.getOurTeam(i),node.getBattle()));
					System.out.println(ourActive.getName()+" added decision: "+node.getDecision().name);
					MinimaxNode child = new MinimaxNode(node);
					
					scoreTree(child,depth - 1);
				}
			}
			for(int i = 0; i < 4; i++)
			{
				if(ourActive.getMove(i) == null)
					continue;
				System.out.println(ourActive.getName()+", "+ourActive.getMove(i).name);
				madeMove = true;
				node.setDecision(new Attack(ourActive.getMove(i),ourActive,enemyActive,node.getBattle()));
				ourActive.onNewTurn(ourActive.getMove(i).name,(ourActive.getMove(i).useMove(false,ourActive,enemyActive)), false);
				System.out.println(node.getOurName()+" added decision: "+node.getDecisionName());
				MinimaxNode child = new MinimaxNode(node);
				scoreTree(child,depth - 1);
			}
			if(!madeMove)
			{
				node.setDecision(new Attack(new Move("Struggle",node.getOurPokemon()),node.getOurPokemon(),node.getEnemyPokemon(),node.getBattle()));
				System.out.println(node.getOurName()+" added decision: "+node.getDecisionName());
				MinimaxNode child = new MinimaxNode(node);
				scoreTree(child,depth - 1);
				madeMove = true;
			}
			node.getDecision();
		}
		return node;
	}
	
	/**
	 * Finds the best move we can use or Pokemon we can switch to using and returns the Action.
	 * @param node - The node to score.
	 * @return MinimaxNode - The scored node parameter.
	 * @see geniusect.ai.MinimaxAI#minimax(int)
	 * @see geniusect.ai.MinimaxAI#scoreTree(MinimaxNode, int)
	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	protected static MinimaxNode score(MinimaxNode node, Action decision)
	{
		node.endIterations();
		int damageDoneToEnemy = node.getDamageDoneEnemy();
		int damageDoneToUs = node.getDamageDoneUs();
		int score = damageDoneToEnemy - damageDoneToUs - node.getCount();
		System.out.println("Branch: "+decision.name+".");
		System.out.println("This branch did "+damageDoneToEnemy+" damage to our enemy ("+node.getEnemyName()+") in "+node.getCount()+" turns.");
		System.out.println(node.getEnemyName()+" did "+damageDoneToUs+" damage to us ("+node.getOurName()+")."); 
		System.out.println("Score: " +score);
		decision.score = score;
		node.setDecision(decision);
		return node;
	}
	
	/**
	 * Runs the Minimax Algorithm to minimize losses.
	 * @param node - The parent node of this branch.
	 * @param depth - How many branches to iterate down (branch count increases exponentially; high numbers lead to lag).
	 * @return Action - the Action to take to minimize losses.
	 */
	private static Action minimaxAlgorithm(MinimaxNode node, int depth)
	{
		if(node.getIsRoot())
		{
			MinimaxNode result = node.getResult();
			if(result == null)
			{
				return checkChildren(node, depth);
			}
			else return result.getDecision();
		}
		//Calculates all worst-case scenarios, then returns steps to minimize losses.
		if(depth <= 0 || node.getChildren() == null)
		{
			System.err.println(	node.getDecision().name+"'s children "+node.getChildren()+" == null OR 0 == "+ depth+". " +
								"Parent: "+node.getParent().getDecision());
			MinimaxNode.printParentRecursive(node);
			return node.getDecision();
		}
		return checkChildren(node, depth);
	}
	
	/**
	 * Checks all the children of this branch and returns the one with the best score.
	 * @param node - The node to check the children of.
	 * @param depth - How many branches to iterate down (branch count increases exponentially; high numbers lead to lag).
	 * @return Action - The best result of all children of this branch.
	 */
	private static Action checkChildren(MinimaxNode node, int depth)
	{
		/*int alpha = -node.player * Integer.MAX_VALUE;
		for(int i = 0; i < node.children.length; i++)
		{
			int miniResult = miniMax(node.children[i], depth - 1);
			if(node.player == 1)
			{
				if(alpha >= miniResult)
				{
					alpha = miniResult;
					node.result = node.children[i];
					System.out.println("Current result for player 1 is "+alpha);
				}
				//System.out.println("Alpha is "+alpha+", miniResult (larger than alpha) is "+miniResult+", depth is "+depth);
			}
			else
			{
				if(alpha <= miniResult)
				{
					alpha = miniResult;
					node.result = node.children[i];
					System.out.println("Current result for player -1 is "+alpha);
				}
				//System.out.println("Alpha is "+alpha+", miniResult (smaller than alpha) is "+miniResult+", depth is "+depth);
			}
		}*/
		int alpha = Integer.MIN_VALUE;
		Action decision = null;
		MinimaxNode[] children = node.getChildren();
		for(int i = 0; i < children.length; i++)
		{
			Action miniResult = minimaxAlgorithm(children[i], depth - 1);
			int miniScore = -miniResult.score;
			if(miniScore > alpha)
			{
				System.out.println(children[i].getDecision().name+" has a score of "+miniResult+", to be achieved in "+node.getCount()+" turns.");
				decision = miniResult;
				node.setResult(children[i]);
				alpha = miniScore;
			}
		}
		return decision;
	}
}
