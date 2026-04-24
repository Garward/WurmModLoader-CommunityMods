
# CustomCreatures > Vehicles


***If your text editor can't display the pretty .md, you should check out
the file on github, where it's more readable:
https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/Vehicles.md .***

# This file is meant to help you determine the values you want to use for your vehicle based on what other mods did.

# Ago's fork of the Creature mod.

## Panda Bear
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(23.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxSpeed(20.0f);
    vehicle.setCommandType((byte) 3);

## Zebra
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.7f, 0.9f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(21.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxSpeed(30.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);




# Wyvernmods

## Bison
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.9f, 1.2f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCanHaveEquipment(true);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Servers.localServer.PVPSERVER ? 31f : 33f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.18f);
    vehicle.setMaxDepth(-10f);
    vehicle.setMaxSpeed(35.0f);
    vehicle.setCommandType((byte) 3);


## Charger
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Servers.localServer.PVPSERVER ? 25.0f : 37.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.06f);
    vehicle.setMaxDepth(-1.7f);
    vehicle.setMaxSpeed(Servers.localServer.PVPSERVER ? 32.0f : 34.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);

## Horned Pony
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(35.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.08f);
    vehicle.setMaxDepth(-1.7f);
    vehicle.setMaxSpeed(40.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(!(Servers.localServer.PVPSERVER));

## Terror
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(95f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.10f);
    vehicle.setMaxDepth(-50f);
    vehicle.setMaxSpeed(90.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);

## Worg
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, -0.2f, 0.0f, -0.1f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(30.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.07f);
    vehicle.setMaxDepth(-1.7f);
    vehicle.setMaxSpeed(Servers.localServer.PVPSERVER ? 35.0f : 40.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(false);

## Black Wyvern
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCanHaveEquipment(true);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(30.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.15f);
    vehicle.setMaxDepth(-50f);
    vehicle.setMaxSpeed(35.0f);
    vehicle.setCommandType((byte) 3);

## Blue Wyvern
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.9f, 1.2f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCanHaveEquipment(true);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Servers.localServer.PVPSERVER ? 45f : 50f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.18f);
    vehicle.setMaxDepth(-10f);
    vehicle.setMaxSpeed(38.0f);
    vehicle.setCommandType((byte) 3);

## Green Wyvern
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.9f, 1.2f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCanHaveEquipment(true);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Servers.localServer.PVPSERVER ? 31f : 33f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.18f);
    vehicle.setMaxDepth(-10f);
    vehicle.setMaxSpeed(35.0f);
    vehicle.setCommandType((byte) 3);

## Red Wyvern
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(43f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.10f);
    vehicle.setMaxDepth(-50f);
    vehicle.setMaxSpeed(35.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);

## White Wyvern
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.8f, 1.1f);
    vehicle.setSeatOffset(0, 0.2f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(40f);
    vehicle.setName(creature.getName());
    vehicle.setMaxHeightDiff(0.10f);
    vehicle.setMaxDepth(-50f);
    vehicle.setMaxSpeed(36.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);





# Jubaroo's More Creatures mod

## Ghost Hell Horse
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.7f, 0.9f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(21.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxSpeed(30.0f);
    vehicle.setCommandType((byte)3);
    vehicle.setCanHaveEquipment(true);

## Ghost Horse
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.7f, 0.9f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(21.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxSpeed(28.0f);
    vehicle.setCommandType((byte)3);
    vehicle.setCanHaveEquipment(true);

## Rainbow Unicorn
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.7f, 0.9f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(21.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxSpeed(38.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);

## Reindeer
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.7f, 0.9f);
    vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(21.0f);
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(-0.7f);
    vehicle.setMaxHeightDiff(0.04f);
    vehicle.setMaxSpeed(26.0f);
    vehicle.setCommandType((byte) 3);
    vehicle.setCanHaveEquipment(true);





# Dragon Rider Mod

## Black Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.BlackDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.BlackDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.BlackDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.BlackDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.BlackDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Blue Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, BlueDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(BlueDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(BlueDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(BlueDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(BlueDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Green Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, GreenDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(GreenDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(GreenDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(GreenDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(GreenDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Red Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.RedDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.RedDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.RedDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.RedDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.RedDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## White Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, WhiteDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(WhiteDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(WhiteDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(WhiteDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(WhiteDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Spectral Drake
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, GhostDrakeZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(GhostDrakeVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(GhostDrakeVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(GhostDrakeSetMaxHeightDiff);
    vehicle.setMaxSpeed(GhostDrakeVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Black Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.BlackBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.BlackBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.BlackBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.BlackBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.BlackBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Blue Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.BlueBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.BlueBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.BlueBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.BlueBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.BlueBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Green Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.GreenBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.GreenBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.GreenBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.GreenBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.GreenBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Red Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, RedBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(RedBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(RedBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(RedBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(RedBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## White Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.WhiteBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.WhiteBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.WhiteBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.WhiteBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.WhiteBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);

## Spectral Basilisk
    vehicle.createPassengerSeats(0);
    vehicle.setSeatFightMod(0, 0.6F, 1.3F);
    vehicle.setSeatOffset(0, 0.0F, 0.0F, Constants.GhostBasiliskZAxisOffset);//-0.01F
    vehicle.setCreature(true);
    vehicle.setSkillNeeded(Constants.GhostBasiliskVehicleSkill);//35.0F
    vehicle.setName(creature.getName());
    vehicle.setMaxDepth(Constants.GhostBasiliskVehicleDepth);//-0.7F
    vehicle.setMaxHeightDiff(Constants.GhostBasiliskSetMaxHeightDiff);
    vehicle.setMaxSpeed(Constants.GhostBasiliskVehicleSpeed);//80.0F
    vehicle.setCommandType(ProtoConstants.TELE_START_COMMAND_CREATURE);
    vehicle.setCanHaveEquipment(false);
