package duck.pathing;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class MomentumPather {
    final RobotController rc;
    Direction mainDirection;
    final MapLocation pointOfOrientation;

    public MomentumPather(RobotController rc, MapLocation pointOfOrientation) {
        this.rc = rc;
        this.pointOfOrientation = pointOfOrientation;
    }

    public void setMainDirection(Direction direction) {
        mainDirection = direction;
    }

    public void move() throws GameActionException {
        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(mainDirection), 255, 0, 0);
        if (rc.canMove(mainDirection)) {
            rc.move(mainDirection);
            return;
        }

        Direction orientation = rc.getLocation().directionTo(pointOfOrientation);
        Direction[] directions = bounce(mainDirection, orientation);

        while (true){
            Direction first = directions[0];
            Direction second = directions[1];

            rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(directions[0]), 0, 150, 100);
            rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(directions[1]), 0, 150, 100);

            if (rc.canMove(first)) {
                mainDirection = first;
                rc.move(first);
                return;
            } else if (rc.canMove(second)) {
                mainDirection = second;
                rc.move(second);
                return;
            }

            directions = bounce(directions[0], orientation);
        }
    }

    private Direction[] bounce(Direction direction, Direction fallbackDirection) {
        Direction left = Direction.CENTER;
        Direction right = Direction.CENTER;

        if (direction == Direction.NORTH) {
            left = Direction.WEST;
            right = Direction.EAST;
        } else if (direction == Direction.SOUTH) {
            left = Direction.EAST;
            right = Direction.WEST;
        } else if (direction == Direction.EAST) {
            left = Direction.NORTH;
            right = Direction.SOUTH;
        } else if (direction == Direction.WEST) {
            left = Direction.SOUTH;
            right = Direction.NORTH;
        } else if (direction == Direction.NORTHEAST) {
            left = Direction.NORTHWEST;
            right = Direction.SOUTHEAST;
        } else if (direction == Direction.NORTHWEST) {
            left = Direction.SOUTHWEST;
            right = Direction.NORTHEAST;
        } else if (direction == Direction.SOUTHEAST) {
            left = Direction.NORTHEAST;
            right = Direction.SOUTHWEST;
        } else if (direction == Direction.SOUTHWEST) {
            left = Direction.SOUTHEAST;
            right = Direction.NORTHWEST;
        }

        final int distLeft = left.dx - fallbackDirection.dx + left.dy - fallbackDirection.dy;
        final int distRight = right.dx - fallbackDirection.dx + right.dy - fallbackDirection.dy;

        return distLeft < distRight ? new Direction[] { left, right } : new Direction[] { right, left };
    }
}
