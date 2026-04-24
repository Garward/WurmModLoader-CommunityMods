package mod.sin.armoury;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.shared.constants.Enchants;
import com.garward.wurmmodloader.api.events.combat.shield.ShieldCheckEvent;
import com.garward.wurmmodloader.api.events.combat.shield.ShieldDamageEvent;

public class ShieldsTweaks {

	public static boolean checkShieldSpeed(Item shield){
        if ((shield != null)) {
            return (shield.getSpellSpeedBonus() > Server.rand.nextInt(500));
        }
		return false;
	}
	
	public static void doSharedPain(Creature attacker, Creature defender, Item shield){
		if(shield.getSpellPainShare() > 0f){
			float powerReq = Server.rand.nextInt(300);
			if(!(powerReq < shield.getSpellPainShare())){
				return;
			}
			boolean playSound = false;
			if(shield.getBonusForSpellEffect(Enchants.BUFF_ROTTING_TOUCH) > 0f){
				double damage = shield.getBonusForSpellEffect(Enchants.BUFF_ROTTING_TOUCH)*31d;
                attacker.addWoundOfType(defender, Wound.TYPE_INFECTION, 0, true, 1.0f, true, damage, 0f, 0f, false, false);
				playSound = true;
			}else if(shield.getBonusForSpellEffect(Enchants.BUFF_FLAMING_AURA) > 0f){
				double damage = shield.getBonusForSpellEffect(Enchants.BUFF_FLAMING_AURA)*27d;
                attacker.addWoundOfType(defender, Wound.TYPE_BURN, 0, true, 1.0f, true, damage, 0f, 0f, false, false);
				playSound = true;
			}else if(shield.getBonusForSpellEffect(Enchants.BUFF_FROSTBRAND) > 0f){
				double damage = shield.getBonusForSpellEffect(Enchants.BUFF_FROSTBRAND)*28d;
                attacker.addWoundOfType(defender, Wound.TYPE_COLD, 0, true, 1.0f, true, damage, 0f, 0f, false, false);
				playSound = true;
			}else if(shield.getBonusForSpellEffect(Enchants.BUFF_VENOM) > 0f){
				double damage = shield.getBonusForSpellEffect(Enchants.BUFF_VENOM)*30d;
                attacker.addWoundOfType(defender, Wound.TYPE_POISON, 0, true, 1.0f, true, damage, 0f, 0f, false, false);
				playSound = true;
			}
			if(playSound){
				SoundPlayer.playSound(attacker.getTemplate().getHitSound(attacker.getSex()), attacker.getTileX(), attacker.getTileY(), attacker.isOnSurface(), 0.3f);
			}
		}
	}

	public static void handleShieldCheck(ShieldCheckEvent event) {
		if (!ArmouryModMain.enableShieldSpeedEnchants) {
			return;
		}
		if (event.getShield() != null && checkShieldSpeed(event.getShield())) {
			event.setRefundShieldUse(true);
		}
	}

	public static void handleShieldDamage(ShieldDamageEvent event) {
		if (!ArmouryModMain.enableShieldDamageEnchants) {
			return;
		}
		if (event.getShield() == null || event.getAttacker() == null || event.getDefender() == null) {
			return;
		}
		doSharedPain(event.getAttacker(), event.getDefender(), event.getShield());
	}
}
