package org.novania.eventpvp.enums;

public enum KitType {
    BUILD("Build"),
    COMBAT("Combat");
    
    private final String displayName;
    
    KitType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static KitType fromString(String type) {
        for (KitType kitType : values()) {
            if (kitType.name().equalsIgnoreCase(type) || 
                kitType.displayName.equalsIgnoreCase(type)) {
                return kitType;
            }
        }
        return null;
    }
}