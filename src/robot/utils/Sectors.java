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

    public Sectors(int width, int height) {
        this.width = width;
        this.height = height;
        this.horizontalSectorCount = width / Constants.SECTOR_SIZE;
        this.verticalSectorCount = height / Constants.SECTOR_SIZE;
    }

    public int getSectorNumber(MapLocation location) {
        return getSectorNumber(location.x, location.y);
    }

    public int getSectorNumber(int x, int y) {
        final int maxSectorX = horizontalSectorCount * Constants.SECTOR_SIZE-1;
        final int maxSectorY = verticalSectorCount * Constants.SECTOR_SIZE-1;

        if (x > maxSectorX){
            x = maxSectorX;
        }

        if (y > maxSectorY){
            y = maxSectorY;
        }

        final int sectorX = x / Constants.SECTOR_SIZE;
        final int sectorY = y / Constants.SECTOR_SIZE;

        return sectorY * horizontalSectorCount + sectorX;
    } 

    public MapLocation getSectorCenter(int sectorNumber) {
        final int sectorY = sectorNumber / horizontalSectorCount;
        final int sectorX = sectorNumber % horizontalSectorCount;
        
        int sectorWidth = Constants.SECTOR_SIZE;
        int sectorHeight = Constants.SECTOR_SIZE;

        if (sectorX == horizontalSectorCount - 1) {
            sectorWidth = width - (horizontalSectorCount - 1) * Constants.SECTOR_SIZE;
        }

        if (sectorY == verticalSectorCount - 1) {
            sectorHeight = height - (verticalSectorCount - 1) * Constants.SECTOR_SIZE;
        }

        final int centerX = sectorX * Constants.SECTOR_SIZE + sectorWidth / 2;
        final int centerY = sectorY * Constants.SECTOR_SIZE + sectorHeight / 2;

        return new MapLocation(centerX, centerY);
    }

    public int getSectorAtDirection(int sectorNumber, Direction direction) {
        final MapLocation sectorCenter = getSectorCenter(sectorNumber);
        final int sectorX = sectorCenter.x;
        final int sectorY = sectorCenter.y;

        final int distance = Constants.SECTOR_SIZE;

        final int newSectorX = sectorX + direction.dx * distance;
        final int newSectorY = sectorY + direction.dy * distance;

        if (newSectorX < 0 || newSectorX >= width || newSectorY < 0 || newSectorY >= height) {
            return -1;
        }

        return getSectorNumber(newSectorX, newSectorY);
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
