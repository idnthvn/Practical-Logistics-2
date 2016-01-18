package sonar.logistics.api;

import cpw.mods.fml.common.Loader;

public class LogisticsAPI {

	public static final String VERSION = "1.0";

	private static RegistryWrapper registry = new RegistryWrapper();	

	public static void init() {
		if (Loader.isModLoaded("PracticalLogistics")) {
			try {
				registry = (RegistryWrapper) Class.forName("sonar.logistics.LogisticsRegistry").newInstance();
			} catch (Exception exception) {
				System.err.println("Practical Logistics API : FAILED TO INITILISE REGISTRY" + exception.getMessage());
			}
		}
	}

	public static RegistryWrapper getRegistry() {
		return registry;
	}
}
