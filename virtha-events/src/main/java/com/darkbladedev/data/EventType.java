package com.darkbladedev.data;

import org.bukkit.command.CommandSender;

/**
 * Enum representing the different types of events that can be created.
 * Each event type corresponds to a mechanic in the mechanics package.
 */
public enum EventType {
    MOB_RAIN("mob_rain", "Mobs rain from the sky periodically");

    private final String eventName;
    private final String description;

    EventType(String eventName, String description) {
        this.eventName = eventName;
        this.description = description;
    }

    /**
     * Gets the command name used to reference this event type.
     * @return The command name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the description of this event type.
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Finds an event type by its command name.
     * @param name The command name to search for
     * @return The matching EventType or null if not found
     */
    public static EventType getByName(String name) {
        for (EventType type : values()) {
            if (type.getEventName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Sends a list of all available event types to a command sender.
     * @param sender The command sender to send the list to
     */
    public static void sendEventList(CommandSender sender) {
        sender.sendMessage("Available event types:");
        for (EventType type : values()) {
            sender.sendMessage("- " + type.getEventName() + ": " + type.getDescription());
        }
    }
}