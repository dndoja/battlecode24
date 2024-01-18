package robot.state;

import battlecode.common.Direction;
import robot.utils.Offset;

public class Formations {
    public static Offset[] getFormationFromDirection(Direction direction) {
        final int dx = direction.dx;
        final int dy = direction.dy;

        if (dx != 0 && dy != 0) {
            return new Offset[] {
                    new Offset(0, 0),
                    new Offset(-dx, dy),
                    new Offset(dx, -dy),
                    new Offset(dx, dy),
                    new Offset(0, dy),
                    new Offset(0, dy*2),
                    new Offset(dx, 0),
                    new Offset(dx*2, 0),
            };
        } else if (dx != 0) {
            return new Offset[] {
                    new Offset(0, 0),
                    new Offset(0, 1),
                    new Offset(0, -1),
                    new Offset(dx, 0),
                    new Offset(dx, 1),
                    new Offset(dx*2, 1),
                    new Offset(dx, -1),
                    new Offset(dx*2, -1),
            };
        } else {
            return new Offset[] {
                    new Offset(0, 0),
                    new Offset(1, 0),
                    new Offset(-1, 0),
                    new Offset(0, dy),
                    new Offset(1, dy),
                    new Offset(1, dy*2),
                    new Offset(-1, dy),
                    new Offset(-1, dy*2),
            };
        }
    }
}
