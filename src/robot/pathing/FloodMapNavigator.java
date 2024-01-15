package robot.pathing;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class FloodMapNavigator implements Navigator {
    class DirectionByValue {
        final Direction direction;
        final int value;

        public DirectionByValue(Direction direction, int value) {
            this.direction = direction;
            this.value = value;
        }
    }

    private final int[][] djikstraMap;

    public FloodMapNavigator(int[][] djikstraMap) {
        this.djikstraMap = djikstraMap;
    }

    @Override
    public ArrayList<Direction> getValidDirections(MapLocation location) {
        final int currentVal = djikstraMap[location.y][location.x];

        final DirectionByValue[] directionsByValue = new DirectionByValue[] {
                new DirectionByValue(Direction.NORTH, getValueAtDirection(location, Direction.NORTH)),
                new DirectionByValue(Direction.SOUTH, getValueAtDirection(location, Direction.SOUTH)),
                new DirectionByValue(Direction.EAST, getValueAtDirection(location, Direction.EAST)),
                new DirectionByValue(Direction.WEST, getValueAtDirection(location, Direction.WEST)),
                new DirectionByValue(Direction.NORTHEAST, getValueAtDirection(location, Direction.NORTHEAST)),
                new DirectionByValue(Direction.NORTHWEST, getValueAtDirection(location, Direction.NORTHWEST)),
                new DirectionByValue(Direction.SOUTHEAST, getValueAtDirection(location, Direction.SOUTHEAST)),
                new DirectionByValue(Direction.SOUTHWEST, getValueAtDirection(location, Direction.SOUTHWEST))
        };

        final ArrayList<Direction> validDirections = new ArrayList<>();
        for (DirectionByValue directionByValue : directionsByValue) {
            if (directionByValue.value < currentVal) {
                validDirections.add(directionByValue.direction);
            }
        }

        return validDirections;
    }

    private int getValueAtDirection(MapLocation location, Direction direction) {
        int x = location.x;
        int y = location.y;
        int[] offset = { 0, 0 };

        switch (direction) {
            case NORTH:
                offset = new int[] { 0, 1 };
                break;
            case SOUTH:
                offset = new int[] { 0, -1 };
                break;
            case EAST:
                offset = new int[] { 1, 0 };
                break;
            case WEST:
                offset = new int[] { -1, 0 };
                break;
            case NORTHEAST:
                offset = new int[] { 1, 1 };
                break;
            case NORTHWEST:
                offset = new int[] { -1, 1 };
                break;
            case SOUTHEAST:
                offset = new int[] { 1, -1 };
                break;
            case SOUTHWEST:
                offset = new int[] { -1, -1 };
                break;
            case CENTER:
                offset = new int[] { 0, 0 };
                break;
        }

        x += offset[0];
        y += offset[1];

        if (x < 0 || x >= djikstraMap.length || y < 0 || y >= djikstraMap.length) {
            return Integer.MAX_VALUE;
        } else {
            return djikstraMap[x][y];
        }
    }
}