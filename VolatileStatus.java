package geniusect;

public enum VolatileStatus {
	Confused, Curse, Embargo, Encore, Flinch, HealBlock, 
	Identified, Nightmare, Trapped, Perish, Leeched, Taunt, Levitated, 
	Torment, Infatuated, AquaRing, Ingrained, Bracing, DrawingAttention,
	DefenseCurl, Charging, Protected, None;
	
	private Pokemon victim;
	
	public void inflict(Pokemon v)
	{
		victim = v;
	}
}
