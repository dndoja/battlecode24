package robot.utils;

import java.util.Arrays;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import robot.Constants;

public class BoundingBox {
    public final int north;
    public final int east;
    public final int south;
    public final int west;

    public BoundingBox(int north, int east, int south, int west) {
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
    }

    @Override
    public String toString() {
        return String.format("BoundingBox(north=%d, east=%d, south=%d, west=%d)", north, east, south, west);
    } 

    public static BoundingBox fromMapLocations(MapLocation[] locations) {
        return fromMapLocations(Arrays.asList(locations));
    }

    public static BoundingBox fromMapLocations(List<MapLocation> locations) {
        int north = Integer.MIN_VALUE;
        int east = Integer.MIN_VALUE;
        int south = Integer.MAX_VALUE;
        int west = Integer.MAX_VALUE;

        for (MapLocation location : locations) {
            if (location.x > east) {
                east = location.x;
            }
            if (location.x < west) {
                west = location.x;
            }
            if (location.y > north) {
                north = location.y;
            }
            if (location.y < south) {
                south = location.y;
            }
        }

        return new BoundingBox(north, east, south, west);
    }

    public Direction getDirectionTo(BoundingBox other) {
        final boolean isNorth = other.north < this.south - Constants.MIN_SPAWN_DISTANCE;
        final boolean isSouth = other.south > this.north + Constants.MIN_SPAWN_DISTANCE;
        final boolean isEast = other.east < this.west - Constants.MIN_SPAWN_DISTANCE;
        final boolean isWest = other.west > this.east + Constants.MIN_SPAWN_DISTANCE;

        if (isNorth && isEast) {
            return Direction.SOUTHWEST;
        }
        if (isNorth && isWest) {
            return Direction.SOUTHEAST;
        }
        if (isSouth && isEast) {
            return Direction.NORTHWEST;
        }
        if (isSouth && isWest) {
            return Direction.NORTHEAST;
        }
        if (isNorth) {
            return Direction.SOUTH;
        }
        if (isSouth) {
            return Direction.NORTH;
        }
        if (isEast) {
            return Direction.WEST;
        }
        if (isWest) {
            return Direction.EAST;
        }

        return Direction.CENTER;
    }
}