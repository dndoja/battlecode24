package duck.movement;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DirectonalMovement {
    final RobotController rc;
    final MapLocation pointOfOrientation;
    boolean bounce = false;
    Direction mainDirection;

    public DirectonalMovement(RobotController rc, MapLocation pointOfOrientation) {
        this.rc = rc;
        this.pointOfOrientation = pointOfOrientation;
    }

    public void setMainDirection(Direction direction) {
        mainDirection = direction;
    }

    public void setShouldBounce(boolean bounce) {
        this.bounce = bounce;
    }

    public void move(MapLocation currentLocation) throws GameActionException {
        rc.setIndicatorLine(currentLocation, currentLocation.add(mainDirection), 255, 0, 0);
        if (rc.canMove(mainDirection)) {
            rc.move(mainDirection);
            return;
        }

        Direction orientation = currentLocation.directionTo(pointOfOrientation);
        Direction[] directions = bounce(mainDirection, orientation);

        for (int i = 8; --i > 0;) {
            Direction first = directions[0];
            Direction second = directions[1];

            rc.setIndicatorLine(currentLocation, currentLocation.add(directions[0]), 0, 150, 100);
            rc.setIndicatorLine(currentLocation, currentLocation.add(directions[1]), 0, 150, 100);

            if (rc.canMove(first)) {
                if (bounce) {
                    mainDirection = first;
                }

                rc.move(first);
                return;
            } else if (rc.canMove(second)) {
                if (bounce) {
                    mainDirection = second;
                }

                rc.move(second);
                return;
            }

            directions = bounce ? bounce(directions[0], orientation) : hug(directions[0], orientation);
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

    private Direction[] hug(Direction direction, Direction fallbackDirection) {
        final Direction left = direction.rotateLeft();
        final Direction right = direction.rotateRight();

        final int distLeft = left.dx - fallbackDirection.dx + left.dy - fallbackDirection.dy;
        final int distRight = right.dx - fallbackDirection.dx + right.dy - fallbackDirection.dy;

        return distLeft < distRight ? new Direction[] { left, right } : new Direction[] { right, left };
    }
}
