# CustomCreatures > Properties

***If your text editor can't display the pretty .md, you should check out
the file on github, where it's more readable:
https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/Properties.md .***


## About
The properties below can be applied to any custom creature.
For an example property `exampleProperty` this is how it's applied to the custom creature
in the .creature file:

    exampleProperty = value

The value can be whatever is defined as the type. The types and their possible values are (unless otherwise specified):

    string   : any string
    float    : 1.0 | 1.5 | 1 | -5 | [...]
    int      : 1 | -5 | [-2147483648 - 2147483647]
    short    : 1 | -5 | [-32768 - 32767]
    byte     : 0 | 110 | [-128 - 127]
    intArray : 0; 1; 2; 3 | 7 | [...]

Note that whenever an input needs several values it **must be** separated by a semicolon `;`.


## Properties

### Basic properties
disabled ***boolean***: Not a "real" property. If set to true then the creature is ignored and not built.
<br>Example: disabled = true

name ***string*** : The name of the creature.
<br>Example: name = thunder rat

plural ***string*** : The plural name.
<br>Example: plural = thunder rats

modelName ***string*** : The model's name. See the mappings.txt from your graphics.jar or
[here on GitHub](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/mappings.txt).
Modded models also work.
<br>Example: modelName = model.creature.humanoid.rooster.brown

acidVulnerability ***float***

acidResistance ***float***

aggressivity ***int*** : The range at which it aggros.
<br>Example: aggressivity = 45

alignment ***float***

armourType ***int*** : For the armour type ID see ArmourTypes.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/ArmourTypes.md)
<br>Example: armourType = 2

baseCombatRating ***float***

bonusCombatRating ***int***

biteResistance ***float***

biteVulnerability ***float***

bodyType ***byte*** : For the body type ID see BodyTypes.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/BodyTypes.md)
<br>Example: bodyType = 5

childTemplateId ***int***

coldResistance ***float***

coldVulnerability ***float***

combatDamageType ***byte*** : Type of damage it does
<br>Example: combatDamageType = 4

crushResistance ***float***

crushVulnerability ***float***

daysOfPregnancy ***byte***

denName ***string***

denMaterial ***byte*** : See Materials.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/Materials.md)
<br>Example: denMaterial = 89

description ***string***

diseaseResistance ***float***

diseaseVulnerability ***float***

eggLayer ***boolean***

eggTemplateId ***int***

mateTemplateId ***int***

adultFemaleTemplateId ***int***

adultMaleTemplateId ***int***

fireResistance ***float***

fireVulnerability ***float***

glowing ***bool***

hasHands ***bool*** : whether it is a human/monster with hands like goblin/troll
<br>Example: hasHands = true

internalResistance ***float***

internalVulnerability ***float***

isHorse ***bool***

butcheredItems  ***intArray*** : Item ids of what it should drop upon butchering. 
See [this list](https://github.com/ago1024/WurmServerModLauncher/wiki/Item-and-creature-ids) or itemIds.md for the IDs or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/itemIDs.md)

keepSex ***bool***

leaderTemplateId ***int***

maxAge ***int*** : How old it can get. (Venerable is around 40)
<br>Example: maxAge = 80

maxGroupAttackSize ***int*** : How many people can attack it.
<br>Example: maxGroupAttackSize = 8

maxHuntDistance ***int*** : How far it chases the player.
<br>Example: maxHuntDistance = 12

maxPercentOfCreatures ***float***

maxPopulationOfCreatures ***int***

meatMaterial ***byte*** : The material type for the meat. For the ids see MeatMaterials.md or
[on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/MeatMaterials.md)
<br>Example: meatMaterial = 80

moveRate ***int***

naturalArmour ***float*** : Percentage of damage blocked automatically (0.0-1.0).
Uniques have 0.04 (4%) or 0.02f (2%)
<br>Example: naturalArmour = 0.04

offZ ***float*** : ~~The height offset of the creture while on water.~~ It doesn't seem to work idk 

paintMode ***int***

physicalResistance ***float***

physicalVulnerability ***float***

pierceResistance ***float***

pierceVulnerability ***float***

poisonResistance ***float***

poisonVulnerability ***float***

reputation ***int***

combatMoves ***intArray*** : Can use combat moves.
<br>Example: combatMoves = 1; 2; 5; 7; 8

sex ***byte***

slashResistance ***float***

slashVulnerability ***float***

speed ***float***

types ***intArray*** : A list of the type numbers. See CreatureTypes.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/CreatureTypes.md)
Types like aggressive towards human/carnivore/animal/monster/ect
<br>Example: types = 16; 35; 33

useNewAttacks ***bool***

vision ***short***

waterResistance ***float***

waterVulnerability ***float***

corpseName ***string*** : the corpse's model name will be "model.corpse.[WHATEVER_YOU_WROTE].", so look for
those kinds of model strings in mappings.txt
<br>Example: corpseName = forestgiant

ghost ***bool***

subTerranean ***bool***

handDamage ***float*** : Hand damage
<br>Example: handDamage = 0.5

kickDamage ***float*** : Kick damage
<br>Example: kickDamage = 0.5

biteDamage ***float*** : Bite damage
<br>Example: biteDamage = 0.5

headButtDamage ***float*** : Headbutt damage
<br>Example: headButtDamage = 0.5

breathDamage ***float*** : Breath damage
<br>Example: breathDamage = 0.5

handDamString ***string*** : How the hand damage is called in the combat log
<br>Example: handDamString = rawr xd

biteDamString ***string***
<br>Example: biteDamString = rawr xd

kickDamString ***string***
<br>Example: kickDamString = rawr xd

headbuttDamString ***string***
<br>Example: headbuttDamString = rawr xd

breathDamString ***string***
<br>Example: breathDamString = rawr xd

deity ***int*** : For ids see Deities.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/Deities.md)
I have no idea if setting this does anything. For all vanilla creatures this is set to null.
<br>Example: deity = 1 

faith ***float***

tutorial ***bool***

noSkillgain ***bool***

royalAspiration ***bool***

noCorpse ***bool***

noServerSounds ***bool***



### Complex properties

sizeModifier ***(int, int, int)*** : Modifier to the size of the creature's model. X, Y, Z
(64, 64, 64) is default.
<br>Example: sizeModifier = 256; 256; 256

onFire ***(bool, byte)*** : onFire, fireRadius

hitSounds ***(string, string)*** : Sound it makes when hit. Male and female. See sounds.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/sounds.md)
<br>Example: hitSounds = sound.combat.hit.cat; sound.combat.hit.cat

dimension ***(short, short, short)*** : The effective size of the creature
in centimeters. Height, length, and width.
Height determines how far the creature is visible from
(401 makes it visible as far as possible). 
<br>Example: dimension = 40; 500; 100

deathSounds ***(string, string)*** : Sound it makes when it dies. Male and female. See sounds.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/sounds.md)
<br>Example: deathSounds = sound.death.dragon; sound.death.dragon

color ***(int, int, int)*** : RGB color
<br>Example: color = 128; 64; 196

boundsValues ***(float, float, float, float)*** : minX, minY, maxX, maxY
<br>Example: boundsValues = 1.5; 3; 5.2; 36.6

### Complicated properties

***You can define several instances for these properties.
The first one must end with `1`, the second with `2` and so on.
(also applies to Complicated vehicle properties)*** 

**NOTE THAT ADDPRIMARYATTACK AND ADDSECONDARYATTACK BOTH SEEM TO DO NOTHING IN WU AT THE MOMENT**
addPrimaryAttack ***(string, AttackIdentifier[STRIKE, BITE, MAUL, CLAW, HEADBUTT, KICK]
float, float, float, int, int, byte, bool, int, float)*** :
name, attackIdentifier, baseDamage, criticalChance, baseSpeed, attackReach,
weightGroup, damageType, usesWeapon, rounds, waitUntilNextAttack
**Note that adding in one or more primary attack clears out the other ones if you copied a template**
<br>Example: addPrimaryAttack1 = maul; MAUL; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4
<br>Example: addPrimaryAttack2 = bite rawrxd; BITE; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4
<br>Example: addPrimaryAttack3 = strike; STRIKE; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4

addSecondaryAttack ***same as addPrimaryAttack***
**Note that adding in one or more primary attack clears out the other ones if you copied a template**
<br>Example: addSecondaryAttack1 = "maul"; MAUL; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4
<br>Example: addSecondaryAttack2 = "bite"; BITE; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4
<br>Example: addSecondaryAttack3 = "strike"; STRIKE; 7.0; 0.04; 6.0; 3; 2; 0; true; 3; 1.4

skill ***(int, float)*** : Skill id and level. See IDs SkillList.md or [on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/SkillList.md)
<br>Example: skill1 = 10013; 25.0
<br>Example: skill2 = 10025; 10.0
<br>Example: skill3 = 10036; 47.2
<br>Example: skill3 = 10042; 45.3

### Encounters

encounter ***(byte, int, int)*** : tile type, creature count, chance. See tile types in TileTypes.md [or on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/TileTypes.md)
The encounter1 here means "it can spawn on sand with a group of 2 monsters and has 3% chance per tile poll"
<br>Example: encounter1 = TILE_SAND; 2; 3
<br>Example: encounter2 = TILE_TAR; 5; 10

# Vehicles (rideable animals)

## How do you make a rideable animal (a vehicle)?

Vehicles don't work very well without custom models. At best, it's going to look silly, goofy,
and wacky. At worst... the same. It works, it just doesn't look great.

To make a creature rideable you must first give it the type 'C_TYPE_VEHICLE' (41). You must
then define how many seats there are (1 + how many passengers) with 'vehicleNumSeats'. That
should be enough to make it ridable.

### Vehicle Properties

***For guidance on what other mods did, see Vehicles.md, [or on github](https://github.com/Tyoda/CustomCreatures/blob/master/mods/CustomCreatures/documentation/Vehicles.md)***

vehicleNumSeats ***int*** : Number of seats for the vehicle. Default is zero. Set to >0 to enable vehicle behaviour on creature.
<br>Example: vehicleNumSeats = 1

vehicleSkillNeeded ***float*** : Minimum body control required to command vehicle.
<br>Example: vehicleSkillNeeded = 21.0

vehicleName ***string*** : The name of the vehicle. The creature's name by default. Don't know what this changes.
<br>Example: vehicleName = Bing Bong

vehicleMaxDepthMeters ***float*** : Maximum depth (meters) the vehicle can travel in. Default is -0.7.
<br>Example: vehicleMaxDepthMeters = -0.7

vehicleMaxHeightDiff ***float*** : Maximum height difference the vehicle can traverse. Default is 0.04.
<br>Example: vehicleMaxHeightDiff = 0.04

vehicleMaxSpeed ***float*** : Maximum speed the vehicle can travel at. Default is 25.0.
<br>Example: vehicleMaxSpeed = 25.0

vehicleCanHaveEquipment ***bool*** : Default is true.
<br>Example: vehicleCanHaveEquipment = true

vehicleCommandType ***byte*** : Command type for the vehicle. Default is 3.
<br>Example: vehicleCommandType = 3

### Complicated vehicle properties

vehicleSeatFightModifier ***(int, float, float)*** : Fight modifier defined for each seat.
arguments are: (seat number{first seat is 0, second is 1, ...}, cover modifier, manoeuvre modifier)
<br>Example: vehicleSeatFightModifier1 = 0; 0.9; 0.7
<br>Example: vehicleSeatFightModifier2 = 1; 0.8; 0.3

vehicleSeatOffset ***(int, float, float, float)*** : seat offset in meters.
arguments are: (seat number(first seat is 0, second is 1, ...), offset X, offset Y, offset Z)
<br>Example: vehicleSeatOffset1 = 0; 1.5; 1; 0
<br>Example: vehicleSeatOffset2 = 1; 0; 0; 2.3
