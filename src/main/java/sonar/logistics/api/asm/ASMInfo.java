package sonar.logistics.api.asm;

import sonar.logistics.api.core.tiles.displays.info.IInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**use this with {@link IInfo}, these must have an empty constructor!!*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ASMInfo {

	/**specify the MODID required for the InfoType to load, note if you want it to always load use the Practical Logistics MODID*/
	String modid();

	/**the identification string of the InfoType*/
	String id();
}
