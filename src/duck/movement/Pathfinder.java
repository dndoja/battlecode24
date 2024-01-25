package duck.movement;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import duck.utils.stack.StackMapLocation;

public class Pathfinder {
    private static long verticalBorderMask = 0b1111111111111111111111111111111111111111111111111L;
    private static long leftShiftBorderMask = 0b1111110111111011111101111110111111011111101111111L;
    private static long rightShiftBorderMask = 0b1111111011111101111110111111011111101111110111111l;

    public static long[] getPathingMasks(MapLocation currentLocation, MapLocation targetLoc,
            StackMapLocation impassableLocations) {
        int targetX = targetLoc.x - currentLocation.x + 3;
        int targetY = targetLoc.y - currentLocation.y + 3;

        if (targetX < 0) {
            targetX = 0;
        }

        if (targetX > 6) {
            targetX = 6;
        }

        if (targetY < 0) {
            targetY = 0;
        }

        if (targetY > 6) {
            targetY = 6;
        }

        final long passabilityMask = getPassabilityMask(currentLocation, impassableLocations);

        if ((passabilityMask & (1L << (targetY * 7 + targetX))) == 0) {
            return new long[0];
        }

        return getReachabilityMasks(targetX, targetY, passabilityMask);
    }

    public static MapLocation clampLocationToLocal(MapLocation location, MapLocation center) {
        int x = location.x;
        int y = location.y;
        int localX = location.x - center.x + 3;
        int localY = location.y - center.y + 3;

        if (localX < 0) {
            x = center.x - 3;
        }

        if (localX > 6) {
            x = center.x + 3;
        }

        if (localY < 0) {
            y = center.y - 3;
        }

        if (localY > 6) {
            y = center.y + 3;
        }

        return new MapLocation(x, y);
    }

    private static long getPassabilityMask(MapLocation currentLoc, StackMapLocation impassableLocations) {
        long impassabilityMask = 0;
        while (!impassableLocations.isEmpty()) {
            MapLocation impassableLocation = impassableLocations.pop();
            long relativeX = impassableLocation.x - currentLoc.x + 3;
            long relativeY = impassableLocation.y - currentLoc.y + 3;

            impassabilityMask |= 1L << (relativeY * 7 + relativeX);
        }

        return verticalBorderMask ^ impassabilityMask;
    }

    private static long[] getReachabilityMasks(int targetX, int targetY, long passabilityMask) {
        long[] reachabilityMasks = new long[25];
        long reachableMask = 1L << (targetY * 7 + targetX);
        System.out.println("Target: " + targetX + ", " + targetY);
        System.out.println(Long.toBinaryString(reachableMask));
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

        // printMask(prevReachableMask, targetX, targetY);

        return reachabilityMasks;
    }

    public static boolean hasReachedTarget(MapLocation currentLocation, long[] pathingMasks,
            MapLocation center) {
        long currentLocationMask = 1L << ((currentLocation.y - center.y + 3) * 7 + (currentLocation.x - center.x + 3));
        return (pathingMasks[0] & currentLocationMask) == currentLocationMask;
    }

    public static Direction[] getNextDirectionsToTarget(MapLocation currentLocation, long[] pathingMasks,
            MapLocation center) {
        
        long currentLocationMask = 1L << ((currentLocation.y - center.y + 3) * 7 + (currentLocation.x - center.x + 3));
        printMask(currentLocationMask, -1, -1);
        int currentLocationReachabilityIndex = 0;
        Direction[] directions = new Direction[8];

        for (int i = 0; i < pathingMasks.length; i++) {
            if ((pathingMasks[i] & currentLocationMask) == currentLocationMask) {
                currentLocationReachabilityIndex = i;
                break;
            }
        }

        if (currentLocationReachabilityIndex == 0) {
            return directions;
        }

        int i = 0;
        long nextReachabilityMask = pathingMasks[currentLocationReachabilityIndex - 1];

        long directionMask = currentLocationMask << 1;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.EAST;
        }

        directionMask = currentLocationMask >> 1;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.WEST;
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
            directions[i++] = Direction.NORTHEAST;
        }

        directionMask = currentLocationMask << 6;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.NORTHWEST;
        }

        directionMask = currentLocationMask >> 6;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.SOUTHEAST;
        }

        directionMask = currentLocationMask >> 8;
        if (directionMask != 0 && (nextReachabilityMask & directionMask) == directionMask) {
            directions[i++] = Direction.SOUTHWEST;
        }
        return directions;
    }

    public static void printMask(long mask, int targetX, int targetY) {
        for (int y = 7; --y >= 0;) {
            String row = "";
            for (int x = 0; x < 7; x++) {
                int index = y * 7 + x;

                if (x == targetX && y == targetY) {
                    row += "X ";
                }else{
                    row += ((mask >> index) & 1) + " ";
                }
            }
            System.out.println(row);
        }
        System.out.println("");
    }
}
