package robot.utils;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import robot.Constants;

public class Sectors {
    int width;
    int height;
    int horizontalSectorCount;
    int verticalSectorCount;
    int sectorSize;

    public Sectors(int width, int height) {
        this.width = width;
        this.height = height;
        this.sectorSize = Constants.SECTOR_SIZE;
        this.horizontalSectorCount = width / sectorSize;
        this.verticalSectorCount = height / sectorSize;
    }

    public Sectors(int width, int height, int sectorSize) {
        this.width = width;
        this.height = height;
        this.sectorSize = sectorSize;
        this.horizontalSectorCount = width / sectorSize;
        this.verticalSectorCount = height / sectorSize;
    }

    public int getSectorNumber(MapLocation location) {
        return getSectorNumber(location.x, location.y);
    }

    public int getSectorNumber(int x, int y) {
        final int maxSectorX = horizontalSectorCount * sectorSize-1;
        final int maxSectorY = verticalSectorCount * sectorSize-1;

        if (x > maxSectorX){
            x = maxSectorX;
        }

        if (y > maxSectorY){
            y = maxSectorY;
        }

        final int sectorX = x / sectorSize;
        final int sectorY = y / sectorSize;

        return sectorY * horizontalSectorCount + sectorX;
    } 

    public MapLocation getSectorCenter(int sectorNumber) {
        final int sectorY = sectorNumber / horizontalSectorCount;
        final int sectorX = sectorNumber % horizontalSectorCount;
        
        int sectorWidth = sectorSize;
        int sectorHeight = sectorSize;

        if (sectorX == horizontalSectorCount - 1) {
            sectorWidth = width - (horizontalSectorCount - 1) * sectorSize;
        }

        if (sectorY == verticalSectorCount - 1) {
            sectorHeight = height - (verticalSectorCount - 1) * sectorSize;
        }

        final int centerX = sectorX * sectorSize + sectorWidth / 2;
        final int centerY = sectorY * sectorSize + sectorHeight / 2;

        return new MapLocation(centerX, centerY);
    }

    public int getSectorAtDirection(int sectorNumber, Direction direction) {
        final MapLocation sectorCenter = getSectorCenter(sectorNumber);
        final int sectorX = sectorCenter.x;
        final int sectorY = sectorCenter.y;

        final int distance = sectorSize;

        final int newSectorX = sectorX + direction.dx * distance;
        final int newSectorY = sectorY + direction.dy * distance;

        if (newSectorX < 0 || newSectorX >= width || newSectorY < 0 || newSectorY >= height) {
            return -1;
        }

        return getSectorNumber(newSectorX, newSectorY);
    }

    public Direction getDirectionToSector(int fromSector, int toSector) {
        final MapLocation fromSectorCenter = getSectorCenter(fromSector);
        final MapLocation toSectorCenter = getSectorCenter(toSector);

        return fromSectorCenter.directionTo(toSectorCenter);
    }

    public ArrayList<Integer> getAdjacentSectors(int sectorNumber) {
        final ArrayList<Integer> adjacentSectors = new ArrayList<>();

        for (Direction direction : Constants.DIRECTIONS) {
            final int adjacentSector = getSectorAtDirection(sectorNumber, direction);
            if (adjacentSector != -1) {
                adjacentSectors.add(adjacentSector);
            }
        }

        return adjacentSectors;
    }
}
