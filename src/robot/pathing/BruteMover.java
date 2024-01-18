package robot.pathing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import battlecode.common.*;
import robot.Constants;
import robot.utils.Utils;

enum TargetType {
    CRUMB, ENEMY, POINT
}
class Target {
    final MapLocation location;
    final TargetType type;

    public Target(MapLocation location, TargetType type) {
        this.location = location;
        this.type = type;
    }

    public boolean isValid(RobotController rc) throws GameActionException {
        if (!rc.canSenseLocation(location)) return false;

        switch (type) {
            case CRUMB:
                return rc.senseMapInfo(location).getCrumbs() > 0;
            case ENEMY:
                final RobotInfo robot = rc.senseRobotAtLocation(location);
                return robot != null && robot.getTeam() != rc.getTeam();
            case POINT:
                return true;
        }

        return false;
    }
}

class Path {
    final Direction direction;
    final int distance;
    final MapLocation newLocation;
    final MapInfo mapInfo;

    public Path(Direction direction, int distance, MapLocation newLocation, MapInfo mapInfo) {
        this.direction = direction;
        this.distance = distance;
        this.newLocation = newLocation;
        this.mapInfo = mapInfo;
    }
}

/// Try target path target destination at any cost.
public class BruteMover {
    final RobotController rc;
    final Stack<Direction> reverse = new Stack<>();
    
    private MapLocation target = null;
    private int targetPriority = 0;
    private HashSet<Integer> visited = new HashSet<Integer>();

    public BruteMover(RobotController rc) {
        this.rc = rc;
    }

    public void setTarget(MapLocation target, int priority) {
        this.target = target;
        targetPriority = priority;
        visited = new HashSet<Integer>();
    }

    public boolean hasTarget() {
        return target != null;
    }

    public MapLocation getTarget() {
        return target;
    }
    
    public void move(boolean loosely) throws GameActionException {
        if (target == null || !rc.isMovementReady()) {
            return;
        }
        
        rc.setIndicatorDot(target, 255, 0, 0);

        final MapLocation from = rc.getLocation();
        final ArrayList<Path> sortedPaths = new ArrayList<>();
        final int currentDist = from.distanceSquaredTo(target);
        for (Direction direction : Constants.DIRECTIONS) {
            final MapLocation newLocation = from.add(direction);
            if (!Utils.isLocationOnMap(newLocation) || visited.contains(newLocation.hashCode())) {
                continue;
            }

            final MapInfo mapInfo = rc.senseMapInfo(newLocation);
            if ((!mapInfo.isPassable() && !mapInfo.isWater())) {
                continue;
            }

            // final int dist = from.distanceSquaredTo(target);
            int newDist = newLocation.distanceSquaredTo(target);
            if (mapInfo.isWater()){
                newDist += 1;
            }

            // if (!loosely && newDist >= currentDist) {
            //     continue;
            // }

            final Path newPath = new Path(direction, newDist, newLocation, mapInfo);
            
            boolean foundSmaller = false;
            for (int i = 0; i < sortedPaths.size(); i++) {
                final Path otherDirection = sortedPaths.get(i);
                if (newDist <= otherDirection.distance) {
                    sortedPaths.add(i, newPath);
                    foundSmaller = true;
                    break;
                }
            }

            if (!foundSmaller) {
                sortedPaths.add(newPath);
            }
        }

        for (int i = 0; i < sortedPaths.size(); i++) {
            final Path path = sortedPaths.get(i);
            if (rc.canMove(path.direction)) {
                move(from, path.direction,false);
                return;
            }else if (path.mapInfo.isWater() && rc.canFill(path.newLocation)){
                rc.fill(path.newLocation);
                return;
            }else if (i < sortedPaths.size() - 1 && rc.senseRobotAtLocation(path.newLocation) != null) {
                final Path nextPath = sortedPaths.get(i + 1);
                if (nextPath.distance < 0){
                    return;
                }
            }
        }

        if (!reverse.isEmpty() && rc.canMove(reverse.peek())) {
            move(from, reverse.pop(), true);
        }
    }

    private void move(MapLocation from, Direction direction, boolean inReverse) {
        try {
            final MapLocation newLocation = from.add(direction);
            rc.move(direction);
            if (!inReverse) {
                reverse.add(direction.opposite());
            }
            visited.add(newLocation.hashCode());

            if (newLocation.equals(target)) {
                target = null;
                targetPriority = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
