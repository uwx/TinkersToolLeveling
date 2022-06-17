package slimeknights.toolleveling;

import net.minecraftforge.fml.ForgeI18n;

/**
 * Created by Maxine on 17/06/2022.
 *
 * @author Maxine
 * @since 17/06/2022
 */
public class Translation {
    /**
     * Checks if the given key can be translated
     *
     * @param key Key to check
     * @return True if it can be translated
     * @see slimeknights.tconstruct.library.utils.Util#canTranslate
     */
    public static boolean has(String key) {
        // By-reference comparison is safe here as long as Forge impl doesn't change (and 1.16 isn't exaclty maintained)
        //noinspection StringEquality
        return ForgeI18n.getPattern(key) != key;
    }

    public static String get(String key) {
        return ForgeI18n.getPattern(key);
    }
}
