package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

public enum YogaGoal {
    FLEXIBILITY("Flexibility"),
    STRENGTH("Strength"),
    RELAXATION("Relaxation"),
    WEIGHT_LOSS("Weight Loss"),
    MINDFULNESS("Mindfulness");

    private final String displayName;

    YogaGoal(String displayName) {
        this.displayName = displayName;
    }

    public static YogaGoal fromDisplayName(String displayName) {
        for (YogaGoal goal : values()) {
            if (goal.getDisplayName().equals(displayName)) {
                return goal;
            }
        }
        return FLEXIBILITY; // Default value
    }

    public String getDisplayName() {
        return displayName;
    }
}