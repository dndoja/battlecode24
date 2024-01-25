package duck.movement;

import java.nio.file.Path;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import duck.utils.stack.StackMapLocation;

public class PathedMovement {
    final RobotController rc;
    private long[] pathingMasks;
    private MapLocation targetLocation;
    private MapLocation targetLocationRelative;
    private MapLocation maskCenterLocation;

    public PathedMovement(RobotController rc) {
        this.rc = rc;
    }

    public void pathTowards(MapLocation currentLocation, MapLocation targetLocation,
            StackMapLocation impassableLocations) {
        this.targetLocation = targetLocation;
        this.maskCenterLocation = currentLocation;
        targetLocationRelative = Pathfinder.clampLocationToLocal(targetLocation, currentLocation);
        pathingMasks = Pathfinder.getPathingMasks(currentLocation, targetLocation, impassableLocations);
    }

    public boolean hasReachedLocalTarget(MapLocation currentLocation) {
        return pathingMasks.length == 0 || Pathfinder.hasReachedTarget(currentLocation, pathingMasks, maskCenterLocation);
    }

    public boolean hasReachedGlobalTarget(MapLocation currentLocation) {
        return currentLocation.equals(targetLocation);
    }

    public MapLocation getTargetLocation() {
        return targetLocation;
    }

    public boolean move(MapLocation currentLocation) throws GameActionException {
        rc.setIndicatorDot(targetLocationRelative, 100, 0, 0);
        rc.setIndicatorDot(targetLocation, 0, 255, 0);
        final Direction[] movementDirections = Pathfinder.getNextDirectionsToTarget(currentLocation, pathingMasks,
                maskCenterLocation);

        for (Direction direction : movementDirections) {
            if (direction == null) {
                return false;
            }

            if (rc.canMove(direction)) {
                rc.move(direction);
                rc.setIndicatorLine(currentLocation, currentLocation.add(direction), 0, 0, 0);
                return true;
            }
        }

        return false;
    }
}
