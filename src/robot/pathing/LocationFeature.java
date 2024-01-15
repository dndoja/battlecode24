package robot.pathing;

public enum LocationFeature {
    EMPTY, WALL, ENEMY, WATER, FLAG_ENEMY, FLAG_FRIENDLY, SPAWN_ENEMY, SPAWN_FRIENDLY, CRUMBS,;

    public static LocationFeature fromInt(int value) {
        switch (value) {
            case 0:
                return EMPTY;
            case 1:
                return WALL;
            case 2:
                return ENEMY;
            case 3:
                return WATER;
            case 4:
                return FLAG_ENEMY;
            case 5:
                return FLAG_FRIENDLY;
            case 6:
                return SPAWN_ENEMY;
            case 7:
                return SPAWN_FRIENDLY;
            case 8:
                return CRUMBS;
            default:
                throw new RuntimeException("Invalid value");
        }
    }
}