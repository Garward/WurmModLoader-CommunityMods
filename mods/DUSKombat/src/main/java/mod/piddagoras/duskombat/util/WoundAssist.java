package mod.piddagoras.duskombat.util;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Wound type mapping utilities (inlined from SinduskLibrary).
 */
public class WoundAssist {
    public static Logger logger = Logger.getLogger(WoundAssist.class.getName());

    public static HashMap<String, Byte> woundNameToType = new HashMap<String, Byte>();
    public static HashMap<Byte, String> woundTypeToName = new HashMap<Byte, String>();

    public static byte getWoundType(String str){
        if(woundNameToType.containsKey(str.toLowerCase())){
            return woundNameToType.get(str.toLowerCase()).byteValue();
        }
        return Byte.parseByte(str);
    }

    public static String getWoundName(int woundType){
        return getWoundName((byte) woundType);
    }

    public static String getWoundName(byte woundType){
        if(woundTypeToName.containsKey(Byte.valueOf(woundType))){
            return woundTypeToName.get(Byte.valueOf(woundType));
        }
        logger.warning(String.format("Wound type %s is unknown to the WoundAssist system.", Byte.valueOf(woundType)));
        return "unknown";
    }

    public static void initializeWoundMaps(){
        logger.info("Initializing Wound Maps.");
        woundNameToType.put("crush", Byte.valueOf((byte) 0));
        woundNameToType.put("slash", Byte.valueOf((byte) 1));
        woundNameToType.put("pierce", Byte.valueOf((byte) 2));
        woundNameToType.put("bite", Byte.valueOf((byte) 3));
        woundNameToType.put("burn", Byte.valueOf((byte) 4));
        woundNameToType.put("poison", Byte.valueOf((byte) 5));
        woundNameToType.put("infection", Byte.valueOf((byte) 6));
        woundNameToType.put("water", Byte.valueOf((byte) 7));
        woundNameToType.put("cold", Byte.valueOf((byte) 8));
        woundNameToType.put("internal", Byte.valueOf((byte) 9));
        woundNameToType.put("acid", Byte.valueOf((byte) 10));
        for(String name : woundNameToType.keySet()){
            woundTypeToName.put(woundNameToType.get(name), name);
        }
    }
}
