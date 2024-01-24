package duck;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Pathfinder {
    private static long verticalBorderMask = 0b1111111111111111111111111111111111111111111111111L;
    private static long leftShiftBorderMask = 0b1111110111111011111101111110111111011111101111111L;
    private static long rightShiftBorderMask = 0b1111111011111101111110111111011111101111110111111l;


    public static long[] getReachabilityMasks(MapLocation targetLoc) {
        return getReachabilityMasks(targetLoc, verticalBorderMask);
    }

    private static long[] getReachabilityMasks(MapLocation targetLoc, long passabilityMask) {
        long[] reachabilityMasks = new long[25];
        long reachableMask = 1 << (targetLoc.y * 7 + targetLoc.x);
        reachabilityMasks[0] = reachableMask;
        long prevReachableMask = reachableMask;

        for (int i = 1;; i++) {
            // Convolve horizontally
            long convolved = reachableMask | ((reachableMask << 1) & leftShiftBorderMask)
                    | ((reachableMask >> 1) & rightShiftBorderMask);

            // Convolve vertically
            convolved = convolved | ((convolved << 7) & verticalBorderMask)
                    | ((convolved >> 7) & verticalBorderMask);

            reachableMask = convolved & passabilityMask;

            if (reachableMask == prevReachableMask) {
                break;
            }

            prevReachableMask = reachableMask;
            reachabilityMasks[i] = reachableMask;
        }

        return reachabilityMasks;
    }

    public static Direction[] getNextDirectionsToTarget(long[] reachabilityMasks, MapLocation currentLocation) {
        long currentLocationMask = 1 << (currentLocation.y * 7 + currentLocation.x);
        int currentLocationReachabilityIndex = 0;
        Direction[] directions = new Direction[8];

        for (int i = 0; i < reachabilityMasks.length; i++) {
            if ((reachabilityMasks[i] & currentLocationMask) == currentLocationMask) {
                currentLocationReachabilityIndex = i;
                break;
            }
        }

        if (currentLocationReachabilityIndex == 0) {
            return directions;
        }

        int i = 0;
        long nextReachabilityMask = reachabilityMasks[currentLocationReachabilityIndex - 1];

        long directionMask = currentLocationMask << 1;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.WEST;
        }

        directionMask = currentLocationMask >> 1;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.EAST;
        }

        directionMask = currentLocationMask << 7;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.NORTH;
        }

        directionMask = currentLocationMask >> 7;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.SOUTH;
        }

        directionMask = currentLocationMask << 8;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.SOUTHEAST;
        }

        directionMask = currentLocationMask << 6;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.NORTHWEST;
        }

        directionMask = currentLocationMask >> 6;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.SOUTHEAST;
        }

        return directions;
    }

    public static void printMask(long mask) {
        for (int y = 6; y >= 0; y--) {
            String row = "";
            for (int x = 0; x < 7; x++) {
                int index = y * 7 + x;
                row += ((mask >> index) & 1) + " ";
            }
            System.out.println(row);
        }
        System.out.println("");
    }
}
