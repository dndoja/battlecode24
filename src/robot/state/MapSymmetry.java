package robot.state;

import java.util.ArrayList;

import robot.Constants;
import robot.utils.BoundingBox;

public enum MapSymmetry {
    VERTICAL,
    HORIZONTAL,
    ROTATIONAL,
    UNKNOWN,
    ;

    public static MapSymmetry find(BoundingBox spawBoundingBox, int mapWidth, int mapHeight) {
        final ArrayList<MapSymmetry> possibleSymmetries = new ArrayList<MapSymmetry>() {
            {
                add(MapSymmetry.ROTATIONAL);
                add(MapSymmetry.HORIZONTAL);
                add(MapSymmetry.VERTICAL);
            }
        };

        final int equator = mapHeight / 2;
        final int primeMeridian = mapWidth / 2;
        final int minSpawnMirrorDistance = Constants.MIN_SPAWN_DISTANCE / 2;

        if (spawBoundingBox.south > equator && spawBoundingBox.south < equator + minSpawnMirrorDistance) {
            possibleSymmetries.remove(MapSymmetry.VERTICAL);
        }

        if (spawBoundingBox.north < equator && spawBoundingBox.north > equator - minSpawnMirrorDistance) {
            possibleSymmetries.remove(MapSymmetry.VERTICAL);
        }

        if (spawBoundingBox.west > primeMeridian && spawBoundingBox.west < primeMeridian + minSpawnMirrorDistance) {
            possibleSymmetries.remove(MapSymmetry.HORIZONTAL);
        }

        if (spawBoundingBox.east < primeMeridian && spawBoundingBox.east > primeMeridian - minSpawnMirrorDistance) {
            possibleSymmetries.remove(MapSymmetry.HORIZONTAL);
        }

        if (spawBoundingBox.east < primeMeridian && spawBoundingBox.west > primeMeridian) {
            possibleSymmetries.remove(MapSymmetry.ROTATIONAL);
        }

        if (spawBoundingBox.north < equator && spawBoundingBox.south > equator) {
            possibleSymmetries.remove(MapSymmetry.ROTATIONAL);
        }

        if (possibleSymmetries.size() == 1) {
            return possibleSymmetries.get(0);
        } else {
            return MapSymmetry.UNKNOWN;
        }
    }
}