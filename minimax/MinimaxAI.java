package geniusectai.minimax;

import geniusectai.GeniusectAI;
import geniusectai.generic.GenericAI;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;


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
	
	public static Action minimax(int depth, Battle b)
	{
		battle = b;
		depth *=2;
		MinimaxNode decide = new MinimaxNode(battle, depth);
		scoreTree(decide,depth);
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
	
	public static Action minimax(int depth, Battle b, Action nextTurn) 
	{
		if(nextTurn instanceof Change)
		{
			MinimaxNode decide = new MinimaxNode(battle, depth);
			createChangeNodes(b.getTeam(0, true).getPokemonTeam(), "", decide, depth);
			minimaxAlgorithm(decide, depth);
			Action decision;
			if(decide.getDecision() == null)
			{
				System.err.println("The minimax result was null! Switching to generic logic.");
				decision = GenericAI.bestMove(battle, nextTurn);
			}
			else
				decision = decide.getDecision();
			GeniusectAI.markDecision(decision);
			return decision;
		}
		else return minimax(depth, b);
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
	private static void scoreTree(MinimaxNode node, int depth)
	{
		Pokemon ourActive = new Pokemon(node.getOurPokemon());
		Pokemon enemyActive = new Pokemon(node.getEnemyPokemon());
		Pokemon[] ourTeam = node.getOurTeam();
		if(depth <= 0 || enemyActive == null)
		{
			System.out.println("Collecting children.");
			MinimaxNode[] finalChildren = node.getChildren();
			if(finalChildren == null)
				score(node);
			else
			{
				for(int i = 0; i < finalChildren.length; i++)
					scoreTree(finalChildren[i],depth - 1);
				//return node;
			}
			return;
		}
		System.out.println("Node generation: "+node.getCount()+"; iterations to go: "+depth);
		System.out.println(	"Player "+node.getPlayer() + " is using "+ourActive.getName() +
							" ("+ourActive.getHealth()+" hp).");
		Action doNext = GeniusectAI.newTurn(ourActive.getTeam());
		if(doNext == null)
			return;
		if(doNext instanceof Attack)
		{
			Battle nodeBattle = node.getBattle();
			Move[] ourMoves = ourActive.getMoveset();
			createAttackNodes(ourMoves, ourActive, enemyActive, nodeBattle, node);
		}
		createChangeNodes(ourTeam, ourActive.getName(), node);
		MinimaxNode[] children = node.getChildren();
		//if(children == null)
			//return score(node);
		for(int i = 0; i < children.length; i++)
		{
			if(children[i].getDecision() == null || ourActive.nameIs(children[i].getDecisionName()))
				continue;
			System.out.println(ourActive.getName()+" is going recursive with "+children[i].getDecisionName()+". Count "+i+" of "+children.length);
			scoreTree(children[i], depth - 1);
		}
		//return node;
	}
	
	private static void createAttackNodes(Move[] moveset, Pokemon ourActive, Pokemon enemyActive, Battle nodeBattle, MinimaxNode node)
	{
		for(int i = 0; i < 4; i++)
		{
			if(moveset[i] == null || moveset[i].disabled || moveset[i].pp <= 0)
				continue;
			MinimaxNode child = new MinimaxNode(node);
			Attack attack = new Attack();
			attack.setMove(moveset[i],new Pokemon(ourActive),new Pokemon(enemyActive),nodeBattle);
			child.setDecision(attack);
			//System.out.println("Using "+ourMoves[i].name);
		}
	}
	
	private static void createChangeNodes(Pokemon[] team, String activeName, MinimaxNode node)
	{
		for(int i = 0; i < 6; i++)
		{
			if(team[i] == null || team[i].nameIs(activeName) || !team[i].isAlive())
				continue;
			MinimaxNode child = new MinimaxNode(node);
			Change change = new Change();
			change.changeTo(team[i]);
			child.setDecision(change);
			//System.out.println("Swtiching to "+ourTeam[i].getName());
		}
	}
	
	private static void createChangeNodes(Pokemon[] team, String activeName, MinimaxNode node, int depth)
	{
		for(int i = 0; i < 6; i++)
		{
			if(team[i] == null || team[i].nameIs(activeName) || !team[i].isAlive())
				continue;
			MinimaxNode child = new MinimaxNode(node);
			Change change = new Change();
			change.changeTo(team[i]);
			child.setDecision(change);
			scoreTree(node, node.getDepth() - 1);
		}
	}
	/*private static MinimaxNode scoreTree(MinimaxNode node, int depth)
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
	}*/
	
	/**
	 * Finds the best move we can use or Pokemon we can switch to using and returns the Action.
	 * @param node - The node to score.
	 * @return MinimaxNode - The scored node parameter.
	 * @see geniusect.ai.MinimaxAI#minimax(int)
	 * @see geniusect.ai.MinimaxAI#scoreTree(MinimaxNode, int)
	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	protected static MinimaxNode score(MinimaxNode node)
	{
		node.endIterations();
		int damageDoneToEnemy = node.getDamageDoneEnemy();
		int damageDoneToUs = node.getDamageDoneUs();
		int score = damageDoneToEnemy - damageDoneToUs - node.getCount();
		Action decision = node.getDecision();
		if(decision == null)
		{
			System.err.println(node.getOurName()+"'s action at count "+node.getCount()+" is null!");
			return null;
		}
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
		if(node.getIsRoot() || node.getParent() == null)
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
			if(miniResult == null)
				continue;
			int miniScore = -miniResult.score;
			if(miniScore > alpha)
			{
				System.out.println(children[i].getDecision().name+" has a score of "+miniScore+", to be achieved in "+node.getCount()+" turns.");
				decision = miniResult;
				node.setResult(children[i]);
				alpha = miniScore;
			}
		}
		return decision;
	}
}
