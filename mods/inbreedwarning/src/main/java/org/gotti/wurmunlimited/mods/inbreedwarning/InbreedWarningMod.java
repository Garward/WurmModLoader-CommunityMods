package org.gotti.wurmunlimited.mods.inbreedwarning;

import com.garward.wurmmodloader.api.events.creature.CreatureBreedEvent;
import com.garward.wurmmodloader.api.events.base.SubscribeEvent;
import com.garward.wurmmodloader.modloader.interfaces.Initable;
import com.garward.wurmmodloader.modloader.interfaces.PreInitable;
import com.garward.wurmmodloader.modloader.interfaces.WurmServerMod;

import com.wurmonline.server.creatures.Creature;  // Type import for event handler (acceptable per architecture rules)

public class InbreedWarningMod implements WurmServerMod, PreInitable, Initable {

	@Override
	public void preInit() {
		// No bytecode hooks needed - framework handles it via CreatureBreedPatch
	}

	@Override
	public void init() {
		// No legacy hook registration needed
	}

	/**
	 * Check for inbreeding when creatures are bred.
	 * Fires at counter == 1.0f (when breeding action completes).
	 */
	@SubscribeEvent
	public void onCreatureBreed(CreatureBreedEvent event) {
		// Only check when breeding completes
		if (!event.isCompleting()) {
			return;
		}

		Creature performer = event.getPerformer();

		// Check if performer has exactly one follower (the breeder creature)
		if (performer.getFollowers().length != 1 || performer.getFollowers()[0] == null) {
			return;
		}

		Creature breeder = performer.getFollowers()[0];
		Creature target = event.getTarget();

		// Determine mother and father
		final Creature mother;
		final Creature father;
		if (breeder.getSex() == 1) {
			mother = breeder;
			father = target;
		} else if (target.getSex() == 1) {
			mother = target;
			father = breeder;
		} else {
			// Neither is female, cannot breed
			return;
		}

		// Check for inbreeding
		if (isInbred(father, mother)) {
			performer.getCommunicator().sendNormalServerMessage(
				"The " + mother.getName() + " and the " + father.getName() +
				" look very similar. You think they may be related."
			);
		}
	}

	/**
	 * Check if two creatures are related (inbred).
	 */
	private boolean isInbred(Creature father, Creature mother) {
		// Same father
		if (father.getFather() != -10 && father.getFather() == mother.getFather()) {
			return true;
		}
		// Same mother
		if (father.getMother() != -10 && father.getMother() == mother.getMother()) {
			return true;
		}
		// Father is mother's parent
		if (father.getWurmId() == mother.getFather()) {
			return true;
		}
		// Mother is father's parent
		if (father.getMother() == mother.getWurmId()) {
			return true;
		}
		return false;
	}
}
