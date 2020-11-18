package outputECC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.NewVersion;
import com.google.zxing.qrcode.decoder.Version;

public class OutputECC {

  public static void main(String[] args) {
    // 型番・誤り訂正レベルを与えとき，k'= 1 ~ k までの誤り訂正能力(％)を出力するプログラム
    // 得られたデータをファイル出力する
    ErrorCorrectionLevel[] ecLevel =
      {ErrorCorrectionLevel.L, ErrorCorrectionLevel.M,
       ErrorCorrectionLevel.Q, ErrorCorrectionLevel.H};

    // 型番
    int versionNumber = 6;
    // 誤り訂正レベル
    ErrorCorrectionLevel ecLevelVal = ecLevel[3];

    String fileName = "C:\\Research2020\\evaluation\\evaluation_2_6H.csv";

    Version version = Version.getVersionForNumber(versionNumber);
    Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevelVal);
    int maxNumDataBytes = version.getTotalCodewords() - ecBlocks.getTotalECCodewords();
    int numTotalCodewords = version.getTotalCodewords();

    // この型番・誤り訂正レベルのとき単一RSブロックか否か
    boolean flag = isSingleRSBlock(ecBlocks);
    double[][] eccArray = new double[flag ? 4 : 3][maxNumDataBytes];

    // 小数点第2位以下は切り捨て
    for (int i = 1; i <= maxNumDataBytes; i++) {
      NewVersion newVersion = new NewVersion(versionNumber, ecLevelVal, i);
      NewVersion.NewECBlocks newECBlocks = newVersion.getECBlocks();
      NewVersion.NewECB[] ecb = newECBlocks.getECBlocks();

      // debug 用
//      System.out.println("k' = " + i);
//      System.out.println("提案手法の誤り訂正能力");
//      double proposedECC = calcProposedECC(ecb, numTotalCodewords);
//      System.out.println("proposedECC = " + Math.floor(proposedECC * 10) / 10);
//      System.out.println();
//
//      System.out.println("従来の手法の誤り訂正能力");
//      double normalECC = calcNormalECC(ecBlocks, numTotalCodewords);
//      System.out.println("normalECC = " + Math.floor(normalECC * 10) / 10);
//      System.out.println();
//
//      if(flag) {
//        System.out.println("既存手法(単一RSブロック)の誤り訂正能力");
//        double exsistSingleECC = calcExsistSingleECC(i, numTotalCodewords);
//        System.out.println("exsistSingleECC = " + Math.floor(exsistSingleECC * 10) / 10);
//        System.out.println();
//      }
//
//      System.out.println("既存手法(複数RSブロック)の誤り訂正能力");
//      double exsistMultiECCsub = calcExsistMultipleECC(maxNumDataBytes, i, ecBlocks);
//      double exsistMultiECC = exsistMultiECCsub != -1 ? exsistMultiECCsub : normalECC;
//      System.out.println("exsistMultiECC = " + Math.floor(exsistMultiECC * 10) / 10);
//      System.out.println();


      double proposedECC = calcProposedECC(ecb, numTotalCodewords);
      eccArray[0][i-1] = Math.floor(proposedECC * 10) / 10;
      double normalECC = calcNormalECC(ecBlocks, numTotalCodewords);
      eccArray[1][i-1] = Math.floor(normalECC * 10) / 10;

      double exsistMultiECCsub = calcExsistMultipleECC(maxNumDataBytes, i, ecBlocks);
      double exsistMultiECC = exsistMultiECCsub != -1 ? exsistMultiECCsub : normalECC;
      /*
       * この実験は誤り訂正能力の高さを評価するものだから、
       * 別の符号語よりも通常の符号語の方が高くなった場合は、そっちの方へ値を変えるようにする
       * */
      if (normalECC > exsistMultiECC) {
        exsistMultiECC = normalECC;
      }

      if(flag) {
        double exsistSingleECC = calcExsistSingleECC(i, numTotalCodewords);
        eccArray[2][i-1] = Math.floor(exsistSingleECC * 10) / 10;
        eccArray[3][i-1] = Math.floor(exsistMultiECC * 10) / 10;
      } else {
        eccArray[2][i-1] = Math.floor(exsistMultiECC * 10) / 10;
      }
    }

    // ファイル書き込み
    exportCsv(fileName, eccArray);
    System.out.println("ファイル出力しました");
  }

  // ファイル書き込み
  public static void exportCsv(String fileName, double[][] eccArray){

    try {
      File file = new File(fileName);
      FileWriter filewriter = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(filewriter);

      if (eccArray.length == 4) {
        // bw.write("埋め込む情報,提案手法,従来手法,既存手法(単一),既存手法(複数)");
        bw.write("k',proposed,normal,exsist(single),exsist(multi)");
      } else {
        // bw.write("埋め込む情報,提案手法,従来手法,既存手法(複数)");
        bw.write("k',proposed,normal,exsist(multi)");
      }
      bw.newLine();

      for (int i = 0; i < eccArray[0].length; i++) {
        bw.write(String.valueOf(i + 1));
        bw.write(",");
        for (int j = 0; j < eccArray.length; j++) {
          bw.write(String.valueOf(eccArray[j][i]));
          if(j != eccArray.length - 1) {
            bw.write(",");
          }
        }
        bw.newLine();
      }
      bw.close();

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  // 提案手法の誤り訂正能力を返すメソッド
  public static double calcProposedECC(NewVersion.NewECB[] ecb, int numTotalCodewords) {
    double ecc = 0;
    for (NewVersion.NewECB block : ecb) {
      // デバッグ用
      // System.out.println(block.getCount() + "×(" + block.getCodewords() + "," + block.getDataCodewords() + ")" );
      // 誤り訂正可能な数
      int t = (block.getCodewords() - block.getDataCodewords()) / 2;
      // 誤り訂正可能な数の総和
      ecc += block.getCount() * t ;
    }
    // 誤り訂正可能な数の総和 / 符号長の総和
    ecc = ecc / (double) numTotalCodewords * 100;
    return ecc;
  }

  // 既存手法(単一RSブロック)の誤り訂正能力を返すメソッド
  // 既存手法を利用できないとき -1 を出力し通常のQRコードの求め方を利用する
  // この手法はその型番・誤り訂正レベルで適用可能なとき使用する
  public static double calcExsistSingleECC(int kp, int numTotalCodewords) {
    double ecc = 0;
    // デバッグ用
    // System.out.println("(" + numTotalCodewords + "," + kp + ")");
    // 誤り訂正可能な数
    int t = (numTotalCodewords - kp) / 2;
    ecc = t / (double) numTotalCodewords * 100;
    return ecc;
  }

  // ある型番・誤り訂正レベルのとき単一RS符号か否か
  public static boolean isSingleRSBlock(Version.ECBlocks ecBlocks) {
    int numBlocks = ecBlocks.getNumBlocks();
    int firstNumBlocks = ecBlocks.getECBlocks()[0].getCount();

    boolean flag = false;
    if(numBlocks == 1 && firstNumBlocks == 1) {
      flag = true;
    }

    return flag;
  }

  // 既存手法(複数RSブロック)の誤り訂正能力を返すメソッド
  // 既存手法を利用できないとき -1 を出力し通常のQRコードの求め方を利用する
  public static double calcExsistMultipleECC(int numDataBytes, int kp, Version.ECBlocks ecBlocks) {
    int p = calcRP(numDataBytes, kp, ecBlocks)[1];

    double ecc = 0;

    if (p == 0) {
      // 既存手法を利用できないとき -1 を出力し通常のQRコードの求め方を利用する
      ecc = -1;
    } else {
      // 分割手法を適用
      if (kp + p > 255) {
        // (k'+p, k')を分割する
        ArrayList<ArrayList<Integer>> nkParam = Decoder.nkDivision(kp + p, kp);

        for (int i = 0; i < nkParam.get(0).size(); i++) {
          // デバッグ用
//          System.out.print(nkParam.get(0).get(i) + "×(");
//          System.out.print(nkParam.get(1).get(i) + ",");
//          System.out.println(nkParam.get(2).get(i) + ")");
          // 誤り訂正可能な数を求める
          int t = (nkParam.get(1).get(i) - nkParam.get(2).get(i)) / 2;
          // 誤り訂正可能な数の総和
          ecc += nkParam.get(0).get(i) * t;
        }
        // 誤り訂正可能な数の総和 / 符号長の総和
        ecc /= (double) (kp + p);
      } else {
        // デバッグ用
        // System.out.println("(" + (kp + p) + "," + p + ")");
        // そのまま求める
        ecc = (p / 2) / (double) (kp + p) * 100;
      }
    }

    return ecc;
  }

  // 従来手法の誤り訂正能力を求めるメソッド
  public static double calcNormalECC(Version.ECBlocks ecBlocks, int numTotalCodewords) {
    double ecc = 0;
    // 誤り訂正可能な数
    int t = ecBlocks.getECCodewordsPerBlock() / 2;
    for (Version.ECB ecb : ecBlocks.getECBlocks()) {
      // 各RSブロックごとの符号長
      int n = ecb.getDataCodewords() + ecBlocks.getECCodewordsPerBlock();
      // デバッグ用
      // System.out.println(ecb.getCount() + "×(" + n + "," + ecb.getDataCodewords() + ")");
      // 誤り訂正可能な数の総和
      ecc += ecb.getCount() * t;
    }
    // 誤り訂正可能な数の総和 / 符号長の総和
    ecc = ecc / (double) numTotalCodewords * 100;
    return ecc;
  }

  // 既存手法(複数RSブロック)用
  // データが埋め込まれているRSブロック数と埋め草コード語の格納領域を算出する関数
  public static int[] calcRP(int numDataBytes, int xByte, Version.ECBlocks ecBlocks) {
    Version.ECB[] ecb = ecBlocks.getECBlocks();

    //1種類または2種類のときRSブロックそれぞれについて個数とデータサイズを取得
    int c1 = ecb[0].getCount();
    int d1 = ecb[0].getDataCodewords();
    int c2 = 0;
    int d2 = 0;
    if (ecb.length == 2) {
      c2 = ecb[1].getCount();
      d2 = ecb[1].getDataCodewords();
    }

//    System.out.println("c1="+c1+"，d1="+d1);
//    System.out.println("c2="+c2+"，d2="+d2);


    //埋め込むデータが含まれているRSブロックの個数
    int r = 0;

    //処理
    if (ecb.length == 2 && (int) Math.ceil((double) xByte / d1) >= c1) {
        r += c1;
        int y = xByte - c1 * d1;
        if ((int) Math.ceil((double) y / d2) <= c2) {
            r += (int) Math.ceil((double) y / d2);
        } else {
            System.out.println("error");
        }
    } else {
        r += (int) Math.ceil((double) xByte / d1);
    }

    /* TotalCodewords を求める処理 */
    int totalCodewords = numDataBytes + ecBlocks.getTotalECCodewords();

    /* Pを求める処理 */
    int p = 0;

    if (r <= ecb[0].getCount()) {
        p = totalCodewords - ecBlocks.getTotalECCodewords() - r * ecb[0].getDataCodewords();
    } else {
        p = totalCodewords - ecBlocks.getTotalECCodewords() -
                ecb[0].getCount() * ecb[0].getDataCodewords() - (r - ecb[0].getCount()) *
                (ecb[1].getDataCodewords());
    }

    return new int[]{r,p};
  }
}
