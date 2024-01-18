package robot;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import robot.state.RobotRole;
import robot.state.SquadOrder;

public final class Constants {
    public static final Direction[] DIRECTIONS = new Direction[]{
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static final SquadOrder[] ORDERS = SquadOrder.values();

    public static boolean PRINT = true;
    public static final int MAX_MAP_SIZE = 1;
    public static final int[][] BASE_INT_MAP = new int[MAX_MAP_SIZE][MAX_MAP_SIZE];
    public static final boolean[][] BASE_BOOL_MAP = new boolean[MAX_MAP_SIZE][MAX_MAP_SIZE];
    public static final int[][] BASE_MAX_VAL_INT_MAP = new int[MAX_MAP_SIZE][MAX_MAP_SIZE];
    public static final int SPAWN_ZONES_COUNT = 3;
    public static final RobotRole[] SQUAD_COMPOSITION = {
        RobotRole.CAPTAIN,
        RobotRole.MEDIC,
        RobotRole.MEDIC,
        RobotRole.SOLDIER,
        RobotRole.SOLDIER,
        RobotRole.SOLDIER,
        RobotRole.SOLDIER,
        RobotRole.SOLDIER,
    }; 
    public static final int SQUAD_SIZE = SQUAD_COMPOSITION.length;
    public static final int SQUADS_COUNT = GameConstants.ROBOT_CAPACITY / SQUAD_SIZE;
    public static final int SQUADS_PER_SPAWN_ZONE = SQUADS_COUNT / SPAWN_ZONES_COUNT;
    public static final int LAST_BROADCAST_CHANNEL = 63;
    public static final int PREP_GAME_PHASE = 200;
    public static final int MIN_SPAWN_DISTANCE = 6;
    public static final int SECTOR_SIZE = 7;
    public static final int MAX_SECTOR_NUMBER_1D = 60 / SECTOR_SIZE;
    public static final int SECTOR_DIAGONAL_LENGTH = 10;
    public static final RobotRole[] ROBOT_ROLES = RobotRole.values();

    static {
        for (int y = 0; y < MAX_MAP_SIZE; y++) {
            for (int x = 0; x < MAX_MAP_SIZE; x++) {
                BASE_MAX_VAL_INT_MAP[y][x] = Integer.MAX_VALUE;
            }
        }
    }
}
