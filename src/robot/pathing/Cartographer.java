package robot.pathing;

import robot.Constants;
import robot.RobotPlayer;

public class Cartographer {
    private static final int WINDOW_RADIUS = 3;

    private final int width;
    private final int height;
    private final int[][] baseMap;
    private boolean[][] impassableMap;
    private int[][] baseExploredMap;
    private int[][] baseEnemyMap;
    private int[][] baseFlagEnemyMap;
    private int[][] baseFlagFriendlyMap;
    private int[][] baseSpawnEnemyMap;
    private int[][] baseSpawnFriendlyMap;
    private int[][] baseCrumbsMap;

    public Cartographer(int width, int height) {
        this.width = width;
        this.height = height;
        this.baseMap = Constants.BASE_INT_MAP.clone();
        this.baseExploredMap = Constants.BASE_INT_MAP.clone();
        this.baseEnemyMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.baseFlagEnemyMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.baseFlagFriendlyMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.baseSpawnEnemyMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.baseSpawnFriendlyMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.baseCrumbsMap = Constants.BASE_MAX_VAL_INT_MAP.clone();
        this.impassableMap = Constants.BASE_BOOL_MAP.clone();
    }

    public int[][] djikstraMapOfType(PathingMapType mapType, int centerX, int centerY) {
        int[][] goalsMap = null;
        switch (mapType) {
            case EXPLORATION:
                goalsMap = baseExploredMap;
                break;
            case COMBAT:
                goalsMap = baseEnemyMap;
                break;
            case FLAG_ATTACK:
                goalsMap = baseFlagEnemyMap;
                break;
            case FLAG_DEFENSE:
                goalsMap = baseFlagFriendlyMap;
                break;
            case GATHERING:
                goalsMap = baseCrumbsMap;
                break;
            case SPAWN_ATTACK:
                goalsMap = baseSpawnEnemyMap;
                break;
            case SPAWN_RETURN:
                goalsMap = baseSpawnFriendlyMap;
                break;
        }

        final int[][] djikstraMap = goalsMap.clone();
        
        boolean wasChanged = true;
        int maxPasses = 2;
        int passes = 0;
        while (wasChanged && passes < maxPasses) {
            wasChanged = false;
            for (int dy = -WINDOW_RADIUS; dy <= WINDOW_RADIUS; dy++) {
                for (int dx = -WINDOW_RADIUS; dx <= WINDOW_RADIUS; dx++) {
                    final int x = centerX + dx;
                    final int y = centerY + dy;
                    if (pointIsPassable(x, y)) {
                        final int lowestNeighbourValue = getLowestNeighbourValue(djikstraMap, x, y);
                        if (djikstraMap[y][x] - lowestNeighbourValue > 1) {
                            djikstraMap[y][x] = lowestNeighbourValue + 1;
                            wasChanged = true;
                        }
                    }

                    final int y1 = height - y - 1;
                    final int x1 = width - x - 1;

                    if (pointIsPassable(x1, y1)) {
                        final int lowestNeighbourValue = getLowestNeighbourValue(djikstraMap, x1, y1);
                        if (djikstraMap[y1][x1] - lowestNeighbourValue > 1) {
                            djikstraMap[y1][x1] = lowestNeighbourValue + 1;
                            wasChanged = true;
                        }
                    }
                }
            }
            passes++;
        }

        printDjikstraMap(djikstraMap);

        return djikstraMap;
    }

    private int getLowestNeighbourValue(int[][] djikstraMap, int x, int y) {
        int lowestValue = Integer.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                final int nx = x + dx;
                final int ny = y + dy;

                if ((dx == 0 && dy == 0) || !pointIsPassable(nx, ny))
                    continue;

                final int value = djikstraMap[ny][nx];
                if (value < lowestValue) {
                    lowestValue = value;
                }
            }
        }

        return lowestValue;
    }

    public void addLocationFeature(Location location) {
        baseMap[location.y][location.x] = location.value;
        final LocationFeature feature = LocationFeature.fromInt(location.value);
        if (feature == LocationFeature.WALL){
            impassableMap[location.y][location.x] = true;
        }else{
            final int[][] featureMap = featureMap(LocationFeature.fromInt(location.value));
            if (featureMap != null) {
                featureMap(LocationFeature.fromInt(location.value))[location.y][location.x] = 0;
            }
        }

        baseExploredMap[location.y][location.x] = Integer.MAX_VALUE;
    }

    public void removeLocationFeature(Location location) {
        baseMap[location.y][location.x] = LocationFeature.EMPTY.ordinal();
        featureMap(LocationFeature.fromInt(location.value))[location.y][location.x] = Integer.MAX_VALUE;
    }

    private int[][] featureMap(LocationFeature feature) {
        switch (feature) {
            case ENEMY:
                return baseEnemyMap;
            case FLAG_ENEMY:
                return baseFlagEnemyMap;
            case FLAG_FRIENDLY:
                return baseFlagFriendlyMap;
            case SPAWN_ENEMY:
                return baseSpawnEnemyMap;
            case SPAWN_FRIENDLY:
                return baseSpawnFriendlyMap;
            case CRUMBS:
                return baseCrumbsMap;
            default:
                return null;
        }
    }

    private boolean pointFitsInMap(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private boolean pointIsPassable(int x, int y) {
        return pointFitsInMap(x, y) && !impassableMap[y][x];
    }

    public static void printDjikstraMap(int[][] map) {
        if (!Constants.PRINT) {
            return;
        }

        for (int y = 0; y < map.length; y++) {
            String line = "";
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x] == Integer.MAX_VALUE) {
                    line += "X ";
                } else {
                    line += map[y][x] + " ";
                }
            }
            System.out.println(line);
        }
        System.out.println();
    }
}
