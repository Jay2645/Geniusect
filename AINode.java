/*
 * The node used in the MiniMax algorithm.
 * @author TeamForretress
 */

package geniusect;

public class AINode
{
	public AINode parent; //The parent of this node.
	public AINode[] children; //The children of this node.
	public AINode result; //The winning child of this node after Minimax calculation.
	
	public Pokemon[] ourTeam; //The player who will make this decision.
	public Pokemon[] enemyTeam; //The enemy who we're going to attack or switch on.
	public Pokemon ourActive; //Which Pokemon we currently have out.
	public Pokemon enemyActive; //Which Pokemon the enemy currently has out.
	public Action decision; //What ourActive node plans to do to enemyActive.
	public int count = 0; //How deep this node is.
	public int damageDoneToUs = 0; //How much damage enemyTeam has done to ourTeam (in total).
	public int damageDoneToEnemy = 0; //How much damage we have done to enemyTeam (in total).
	public int player = 1; //Whose turn is it? (1 is the person performing the Minimax calc; -1 is the enemy).
	
	public boolean createChildren = true;
	public boolean isRoot = false;
	
	public AINode(Pokemon[] us, Pokemon[] them, Pokemon usUsing, Pokemon themUsing) 
	{
		ourTeam = us;
		enemyTeam = them;
		ourActive = usUsing;
		enemyActive = themUsing;
		isRoot = true;
	}
	
	public AINode(AINode p)
	{
		if(createChildren)
		{
			parent = p;
			parent.addChild(this);
		}
	}
	
	public void addChild(AINode child)
	{
		child.enemyTeam = new Pokemon[6];
		child.ourTeam = new Pokemon[6];
		child.ourActive = new Pokemon(enemyActive);
		child.damageDoneToUs = damageDoneToEnemy;
		child.damageDoneToEnemy = damageDoneToUs;
		child.count = count + 1;
		if(decision != null)
		{
			decision.sendToShowdown(child);
			Pokequations.printParentRecursive(this);
		}
		else
			child.enemyActive = new Pokemon(ourActive);
		if(child.ourActive.getHealth() <= 0)
		{
			System.err.println(child.ourActive.name+" has died after "+(count / 2)+" turns!");
			Pokemon change = child.ourActive.onDie();
			if(change == null)
			{
				child.createChildren = false;
				child.damageDoneToUs = Integer.MAX_VALUE;
				child.count = 0;
				child.decision = new Attack(Pokequations.bestMove(child.ourActive, child.enemyActive),child.ourActive,child.enemyActive);
				GeniusectAI.score(child);
				Pokequations.printParentRecursive(this);
			}
			else
			{
				child.ourActive = new Pokemon(change);
			}
		}
		else
			System.err.println(child.ourActive.name+" has "+child.ourActive.getHealth()+" hp");
		expandArray(child);
	}
	
	private void expandArray(AINode child)
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
			children = new AINode[1];
			children[0] = child;
			return;
		}
		AINode[] newChild = new AINode[children.length + 1];
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
	
	public void setDecision(Action d)
	{
		decision = d;
	}
}
