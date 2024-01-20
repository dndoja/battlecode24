package robot.pathing;

import battlecode.common.MapLocation;
import robot.Constants;
import robot.Loggy;
import robot.utils.Utils;

public class Cartographer {
    private static final int WINDOW_RADIUS = 3;
    private static final int WINDOW_SIZE = WINDOW_RADIUS * 2 + 1;

    private final int width;
    private final int height;
    private final int[][] baseMap;
    private final boolean[][] impassabilityMap;
    private String impassaString = "";

    public Cartographer(int width, int height) {
        this.width = width;
        this.height = height;
        this.impassabilityMap = new boolean[height][width];
        this.baseMap = new int[height][width];
        for (int y = height - 1; --y >= 0;) {
            for (int x = width - 1; --x >= 0;) {
                baseMap[y][x] = Integer.MAX_VALUE;
            }
        }
    }

    public void addImpassableLocation(MapLocation location) {
        impassabilityMap[location.y][location.x] = true;
        impassaString += " " + Utils.encodeLocation(location);
    }

    public int[][] getPathingMap(MapLocation center, MapLocation goal) {
        final int[][] pathingMap = baseMap.clone();

        if (goal.x < 0 || goal.x >= width || goal.y < 0 || goal.y >= height) {
            return pathingMap;
        }

        if (impassabilityMap[goal.y][goal.x]) {
            return pathingMap;
        }

        pathingMap[goal.y][goal.x] = 0;

        boolean wasChanged = true;

        int startX = center.x - WINDOW_RADIUS;
        if (startX < 0) {
            startX = 0;
        }

        int endX = center.x + WINDOW_RADIUS;
        if (endX >= width) {
            endX = width - 1;
        }

        int startY = center.y - WINDOW_RADIUS;
        if (startY < 0) {
            startY = 0;
        }

        int endY = center.y + WINDOW_RADIUS;
        if (endY >= height) {
            endY = height - 1;
        }

        int xRadius = goal.x > center.x ? WINDOW_SIZE - (endX - goal.x) : endX - goal.x;
        int yRadius = goal.y > center.y ? WINDOW_SIZE - (endY - goal.y) : endY - goal.y;

        final int maxRadius = xRadius > yRadius ? xRadius : yRadius;

        Loggy.log("Cartographer: " + startX + " " + endX + " " + startY + " " + endY);
        Loggy.log("Cartographer: " + center + " -> " + goal + " maxRadius: " + maxRadius);

        int passes = 0;
        while (wasChanged) {
            for (int radius = 1; radius <= maxRadius; radius++) {
                int leftX = goal.x - radius;
                boolean leftXOutOfBounds = false;
                if (leftX < startX) {
                    leftXOutOfBounds = true;
                    leftX = startX;
                }

                int rightX = goal.x + radius;
                boolean rightXOutOfBounds = false;
                if (rightX > endX) {
                    rightX = endX;
                    rightXOutOfBounds = true;
                }

                int bottomY = goal.y - radius;
                boolean bottomYOutOfBounds = false;
                if (bottomY < startY) {
                    bottomY = startY;
                    bottomYOutOfBounds = true;
                }

                int topY = goal.y + radius;
                boolean topYOutOfBounds = false;
                if (topY > endY) {
                    topY = endY;
                    topYOutOfBounds = true;
                }

                if (!topYOutOfBounds && !bottomYOutOfBounds) {
                    for (int x = rightX + 1; --x >= leftX;) {
                        if (!impassabilityMap[bottomY][x]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, x, bottomY);
                            if (pathingMap[bottomY][x] - lowestNeighbourValue > 1) {
                                pathingMap[bottomY][x] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }

                        if (!impassabilityMap[topY][x]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, x, topY);
                            if (pathingMap[topY][x] - lowestNeighbourValue > 1) {
                                pathingMap[topY][x] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                } else if (!topYOutOfBounds) {
                    for (int x = rightX + 1; --x >= leftX;) {
                        if (!impassabilityMap[topY][x]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, x, topY);
                            if (pathingMap[topY][x] - lowestNeighbourValue > 1) {
                                pathingMap[topY][x] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                } else if (!bottomYOutOfBounds) {
                    for (int x = rightX + 1; --x >= leftX;) {
                        if (!impassabilityMap[bottomY][x]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, x, bottomY);
                            if (pathingMap[bottomY][x] - lowestNeighbourValue > 1) {
                                pathingMap[bottomY][x] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                }

                if (!leftXOutOfBounds && !rightXOutOfBounds) {
                    for (int y = topY + 1; --y >= bottomY;) {
                        if (!impassabilityMap[y][leftX]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, leftX, y);
                            if (pathingMap[y][leftX] - lowestNeighbourValue > 1) {
                                pathingMap[y][leftX] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }

                        if (!impassabilityMap[y][rightX]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, rightX, y);
                            if (pathingMap[y][rightX] - lowestNeighbourValue > 1) {
                                pathingMap[y][rightX] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                } else if (!leftXOutOfBounds) {
                    for (int y = topY + 1; --y >= bottomY;) {
                        if (!impassabilityMap[y][leftX]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, leftX, y);
                            if (pathingMap[y][leftX] - lowestNeighbourValue > 1) {
                                pathingMap[y][leftX] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                } else if (!rightXOutOfBounds) {
                    for (int y = topY + 1; --y >= bottomY;) {
                        if (!impassabilityMap[y][rightX]) {
                            final int lowestNeighbourValue = getLowestNeighbourValue(pathingMap, rightX, y);
                            if (pathingMap[y][rightX] - lowestNeighbourValue > 1) {
                                pathingMap[y][rightX] = lowestNeighbourValue + 1;
                                wasChanged = true;
                            }
                        }
                    }
                }
            }
            break;
        }

        return pathingMap;
    }

    private int getLowestNeighbourValue(int[][] pathingMap, int x, int y) {
        int lowestValue = Integer.MAX_VALUE;

        int xMinusOne = x - 1;
        int xPlusOne = x + 1;
        int yMinusOne = y - 1;
        int yPlusOne = y + 1;

        int val = 0;

        if (x > 0) {
            if (x < width - 1) {
                if (y > 0) {
                    if (y < height - 1) {
                        if (!impassabilityMap[yMinusOne][xMinusOne]) {
                            val = pathingMap[yMinusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][x]) {
                            val = pathingMap[yMinusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][xPlusOne]) {
                            val = pathingMap[yMinusOne][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xMinusOne]) {
                            val = pathingMap[y][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xPlusOne]) {
                            val = pathingMap[y][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xMinusOne]) {
                            val = pathingMap[yPlusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][x]) {
                            val = pathingMap[yPlusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xPlusOne]) {
                            val = pathingMap[yPlusOne][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }
                    } else {
                        if (!impassabilityMap[yMinusOne][xMinusOne]) {
                            val = pathingMap[yMinusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][x]) {
                            val = pathingMap[yMinusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][xPlusOne]) {
                            val = pathingMap[yMinusOne][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xMinusOne]) {
                            val = pathingMap[y][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xPlusOne]) {
                            val = pathingMap[y][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xMinusOne]) {
                            val = pathingMap[yPlusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][x]) {
                            val = pathingMap[yPlusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xPlusOne]) {
                            val = pathingMap[yPlusOne][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }
                    }
                } else {
                    if (!impassabilityMap[y][xMinusOne]) {
                        val = pathingMap[y][xMinusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[y][xPlusOne]) {
                        val = pathingMap[y][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][xMinusOne]) {
                        val = pathingMap[yPlusOne][xMinusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][x]) {
                        val = pathingMap[yPlusOne][x];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][xPlusOne]) {
                        val = pathingMap[yPlusOne][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }
                }
            } else {
                // x == width - 1 (can't do x+1)
                if (y > 0) {
                    if (y < height - 1) {
                        if (!impassabilityMap[yMinusOne][xMinusOne]) {
                            val = pathingMap[yMinusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][x]) {
                            val = pathingMap[yMinusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xMinusOne]) {
                            val = pathingMap[y][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xMinusOne]) {
                            val = pathingMap[yPlusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][x]) {
                            val = pathingMap[yPlusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yPlusOne][xPlusOne]) {
                            val = pathingMap[yPlusOne][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xMinusOne]) {
                            val = pathingMap[y][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xPlusOne]) {
                            val = pathingMap[y][xPlusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }
                    } else {
                        if (!impassabilityMap[yMinusOne][xMinusOne]) {
                            val = pathingMap[yMinusOne][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[yMinusOne][x]) {
                            val = pathingMap[yMinusOne][x];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }

                        if (!impassabilityMap[y][xMinusOne]) {
                            val = pathingMap[y][xMinusOne];
                            if (val < lowestValue) {
                                lowestValue = val;
                            }
                        }
                    }
                } else {
                    if (!impassabilityMap[y][xMinusOne]) {
                        val = pathingMap[y][xMinusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][xMinusOne]) {
                        val = pathingMap[yPlusOne][xMinusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][x]) {
                        val = pathingMap[yPlusOne][x];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }
                }
            }
        } else {
            // x == 0 (can't do x-1)
            if (y > 0) {
                if (y < height - 1) {
                    if (!impassabilityMap[yMinusOne][x]) {
                        val = pathingMap[yMinusOne][x];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yMinusOne][xPlusOne]) {
                        val = pathingMap[yMinusOne][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[y][xPlusOne]) {
                        val = pathingMap[y][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][xPlusOne]) {
                        val = pathingMap[yPlusOne][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yPlusOne][x]) {
                        val = pathingMap[yPlusOne][x];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }
                } else {
                    if (!impassabilityMap[yMinusOne][x]) {
                        val = pathingMap[yMinusOne][x];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[yMinusOne][xPlusOne]) {
                        val = pathingMap[yMinusOne][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }

                    if (!impassabilityMap[y][xPlusOne]) {
                        val = pathingMap[y][xPlusOne];
                        if (val < lowestValue) {
                            lowestValue = val;
                        }
                    }
                }
            } else {
                if (!impassabilityMap[y][xPlusOne]) {
                    val = pathingMap[y][xPlusOne];
                    if (val < lowestValue) {
                        lowestValue = val;
                    }
                }

                if (!impassabilityMap[yPlusOne][xPlusOne]) {
                    val = pathingMap[yPlusOne][xPlusOne];
                    if (val < lowestValue) {
                        lowestValue = val;
                    }
                }

                if (!impassabilityMap[yPlusOne][x]) {
                    val = pathingMap[yPlusOne][x];
                    if (val < lowestValue) {
                        lowestValue = val;
                    }
                }
            }
        }

        return lowestValue;
    }

    public boolean isLocationPassable(int x, int y) {
        return !impassaString.contains(String.valueOf(Utils.encodeLocation(x, y)));
    }

    public static void printPathingMap(int[][] map, MapLocation center) {
        if (!Constants.PRINT) {
            return;
        }

        int startX = center.x - WINDOW_RADIUS;
        if (startX < 0) {
            startX = 0;
        }

        int endX = center.x + WINDOW_RADIUS;
        if (endX >= map[0].length) {
            endX = map[0].length - 1;
        }

        int startY = center.y - WINDOW_RADIUS;
        if (startY < 0) {
            startY = 0;
        }

        int endY = center.y + WINDOW_RADIUS;
        if (endY >= map.length) {
            endY = map.length - 1;
        }

        for (int y = startY; y <= endY; y++) {
            String line = "";
            for (int x = startX; x <= endX; x++) {
                if (map[y][x] == Integer.MAX_VALUE) {
                    line += "X ";
                } else {
                    line += map[y][x] + " ";
                }
            }
            System.out.println(line);
        }
        System.out.println();
    }
}
