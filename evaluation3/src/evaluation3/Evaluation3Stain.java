package evaluation3;


import java.util.Random;

import evaluation3.GridChart;

public class Evaluation3Stain {
  // 染み破損の再現アルゴリズムの動作テストプログラム
  public static void main(String[] args) {
    int size = 10;

    int[][] qr = new int[size][size];
    appendStainError(qr);

    for (int i = 0; i < qr.length; i++) {
      for (int j = 0; j < qr[i].length; j++) {
        if (qr[i][j] == 1) {
          System.out.print(1 + " ");
        } else {
          System.out.print(0 + " ");
        }
      }
      System.out.println();
    }
  }

  public static void appendStainError(int[][] qr) {
    double c = 0.1;
    int rr = (int) Math.floor(Math.sqrt(c / Math.PI) * qr.length);
    Random rand = new Random();

    GridChart[] points = new GridChart[36];
    int p = 4;
    int q = 4;

    // 円を36点取る
    for (int i = 0; i < 36; i++) {
      double theta = Math.toRadians(i * 10);
      int r = rr + (rand.nextInt(5) - 2);
      int x = p + (int)Math.floor(r * Math.cos(theta));
      int y = q + (int)Math.floor(r * Math.sin(theta));
      points[i] = new GridChart(x,y);
    }

    for (GridChart position : points) {
      if (position.getX() >= 0 && position.getX() < qr.length) {
        if (position.getY() >= 0 && position.getY() < qr.length) {
          qr[position.getY()][position.getX()] = 1;
        }
      }
    }

      for (GridChart position : points) {
        int maxY = getMaxY(points, position);
        for (int i = position.getY(); i < maxY; i++) {
          qr[i][position.getX()] = 1;
        }
      }
  }

  private static int getMaxY(GridChart[] points, GridChart loc) {
    int maxY = loc.getY();
    for (int i = 0; i < points.length; i++) {
      if (points[i].getX() == loc.getX()) {
        maxY = Math.max(maxY, points[i].getY());
      }
    }
    return maxY;
  }

}
