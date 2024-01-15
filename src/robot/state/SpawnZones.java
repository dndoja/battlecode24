package robot.state;

import java.util.ArrayList;

import battlecode.common.MapLocation;
import robot.utils.BoundingBox;

public class SpawnZones {
    final BoundingBox combinedBoundingBox;
    final BoundingBox[] spawnZones;
    final int[] spawnLocationToZoneIndex;

    public SpawnZones(BoundingBox combinedBoundingBox, BoundingBox[] spawnZones, int[] spawnLocationToZoneIndex) {
        this.combinedBoundingBox = combinedBoundingBox;
        this.spawnZones = spawnZones;
        this.spawnLocationToZoneIndex = spawnLocationToZoneIndex;
    }

    public BoundingBox getCombinedBoundingBox() {
        return combinedBoundingBox;
    }

    public BoundingBox[] getZoneBoundingBoxes() {
        return spawnZones;
    }

    public int[] getSpawnLocationToZoneIndex() {
        return spawnLocationToZoneIndex;
    }

    public static SpawnZones fromSpawnLocations(MapLocation[] spawnLocs){
        final int[] locationsToSpawnZones = new int[spawnLocs.length];
        final ArrayList<ArrayList<MapLocation>> spawnZones = new ArrayList<ArrayList<MapLocation>>() {{
            add(new ArrayList<>());
            add(new ArrayList<>());
            add(new ArrayList<>());
        }};

        for (int i = 0; i < spawnLocs.length; i++) {
            final MapLocation spawnLoc = spawnLocs[i];

            for (int zoneIndex = 0; zoneIndex < 3; zoneIndex++) {
                final ArrayList<MapLocation> spawnZone = spawnZones.get(zoneIndex);
                if (spawnZone.isEmpty() || spawnZone.get(0).distanceSquaredTo(spawnLoc) <= 8) {
                    spawnZone.add(spawnLoc);
                    locationsToSpawnZones[i] = zoneIndex;
                    break;
                }
            }
        }

        final BoundingBox combineBoundingBox = BoundingBox.fromMapLocations(spawnLocs);
        final BoundingBox[] spawnZonesBoundingBoxes = new BoundingBox[] {
            BoundingBox.fromMapLocations(spawnZones.get(0)),
            BoundingBox.fromMapLocations(spawnZones.get(1)),
            BoundingBox.fromMapLocations(spawnZones.get(2))
        };

        return new SpawnZones(combineBoundingBox, spawnZonesBoundingBoxes, locationsToSpawnZones);
    }
}
