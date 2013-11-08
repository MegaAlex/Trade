package me.josvth.trade.transaction.inventory;

import com.conventnunnery.libraries.config.ConventYamlConfiguration;
import me.josvth.trade.transaction.inventory.slot.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutManager {

	private static final Layout DEFAULT_LAYOUT;

	static {

		final Slot[] slots = new Slot[17];

		slots[0] = new TradeSlot(0,0);
		slots[1] = new TradeSlot(1,1);
		slots[2] = new TradeSlot(2,2);

		slots[3] = new AcceptSlot(3, new ItemDescription(Material.STAINED_CLAY, 0, (byte) 5), new ItemDescription(Material.STAINED_CLAY, 0, (byte) 4));
		slots[4] = new RefuseSlot(4, new ItemDescription(Material.STAINED_CLAY, 0, (byte) 6));
		slots[5] = new StatusSlot(5, new ItemDescription(Material.STAINED_CLAY, 0, (byte) 4), new ItemDescription(Material.STAINED_CLAY, 0, (byte) 5));

		slots[6] = new MirrorSlot(6,0);
		slots[7] = new MirrorSlot(7,1);
		slots[8] = new MirrorSlot(8,2);

		slots[9] = new TradeSlot(9,0);

		slots[11] = new MoneySlot(11, new ItemDescription(Material.GOLD_INGOT, 0), 1, 5);
		slots[12] = new ExperienceSlot(12, new ItemDescription(Material.EXP_BOTTLE, 0), 1, 5);
		slots[13] = new CloseSlot(13, new ItemDescription(Material.BONE, 0));

		DEFAULT_LAYOUT = new Layout("default", 3, 3);
		DEFAULT_LAYOUT.setSlots(slots);

	}

	private final ConventYamlConfiguration configuration;

	private final Map<String, Layout> layouts = new HashMap<String, Layout>();

	public LayoutManager(ConventYamlConfiguration layoutConfiguration) {
		this.configuration = layoutConfiguration;
	}

	public int load() {

		layouts.clear();

		layouts.put("default", DEFAULT_LAYOUT);

		for (String key : configuration.getKeys(false)) {

			final int size 		= configuration.getInt(key + ".size");

			if (size == 0 || size % 9 != 0) {
				// TODO add message
				continue;
			}

			if (!configuration.isConfigurationSection(key + ".slots")) {
				// TODO add message
				continue;
			}

			final int offerSize = configuration.getInt(key + ".offer-size", 4);

			final Layout layout = new Layout(key, size / 9, offerSize);

			final Slot[] slots = new Slot[size];

			for (String slotKey : configuration.getConfigurationSection(key + ".slots").getKeys(false)) {

				int slotID = -1;

				try {
					slotID = Integer.parseInt(slotKey);
				} catch (NumberFormatException e) {
					// TODO add message
					continue;
				}

				if (slotID < 0 || slotID >= size) {
					// TODO add message
					continue;
				}

				final ConfigurationSection slotSection = configuration.getConfigurationSection(key + ".slots." + slotKey);

				final String type = slotSection.getString(slotKey + ".type");

				Slot slot = null;

				if ("accept".equalsIgnoreCase(type)) {
					slot = new AcceptSlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("accept-item")),
							ItemDescription.fromSection(slotSection.getConfigurationSection("accepted-item"))
						);
				} else if ("refuse".equalsIgnoreCase(type)) {
					slot = new RefuseSlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("refuse-item"))
					);
				} else if ("close".equalsIgnoreCase(type)) {
					slot = new CloseSlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("close-item"))
					);
				} else if ("status".equalsIgnoreCase(type)) {
					slot = new StatusSlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("considering-item")),
							ItemDescription.fromSection(slotSection.getConfigurationSection("accepted-item"))
					);
				} else if ("trade".equalsIgnoreCase(type)) {
					slot = new TradeSlot(
							slotID,
							slotSection.getInt("trade-slot", 1)
					);
				} else if ("mirror".equalsIgnoreCase(type)) {
					slot = new MirrorSlot(
							slotID,
							slotSection.getInt("mirror-slot", 1)
					);
				} else if ("money".equalsIgnoreCase(type)) {
					slot = new MoneySlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("money-item")),
							slotSection.getInt("small-modifier", 5),
							slotSection.getInt("large-modifier", 10)
					);
				} else if ("experience".equalsIgnoreCase(type)) {
					slot = new ExperienceSlot(
							slotID,
							ItemDescription.fromSection(slotSection.getConfigurationSection("experience-item")),
							slotSection.getInt("small-modifier", 1),
							slotSection.getInt("large-modifier", 5)
					);
				}

				if (slot == null) {
					// TODO add message
					continue;
				}

				slots[slotID] = slot;

			}



			layouts.put(layout.getName(), layout);

		}

		return layouts.size();

	}

	public Layout getLayout(String playerNameA, String playerNameB) {

		Layout found = DEFAULT_LAYOUT;
		boolean permissionBased = false;

		final Player playerA = Bukkit.getPlayerExact(playerNameA);
		final Player playerB = Bukkit.getPlayerExact(playerNameB);

		for (Layout layout : layouts.values()) {
			if (layout.getPriority() > found.getPriority() || (layout.getPermission() != null && permissionBased == false)) {

				if (layout.getPermission() != null) {

					final boolean AHasPermission = playerA != null && playerA.hasPermission(layout.getPermission());
					final boolean BHasPermission = playerB != null && playerB.hasPermission(layout.getPermission());

					if (AHasPermission && BHasPermission || (layout.isShared() && (AHasPermission || BHasPermission))) {
						found = layout;
						permissionBased = false;
					}

				} else {
					found = layout;
					permissionBased = false;
				}

			}
		}

		return found;

	}


}