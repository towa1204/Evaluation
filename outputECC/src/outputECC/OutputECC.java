package outputECC;

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
    int versionNumber = 3;
    // 誤り訂正レベル
    ErrorCorrectionLevel ecLevelVal = ecLevel[0];
    // 埋め込む情報k'
    int kp  = 10;

    Version version = Version.getVersionForNumber(versionNumber);
    Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevelVal);
    int maxNumDataBytes = version.getTotalCodewords() - ecBlocks.getTotalECCodewords();
    int numTotalCodewords = version.getTotalCodewords();

    NewVersion newVersion = new NewVersion(versionNumber, ecLevelVal, kp);
    NewVersion.NewECBlocks newECBlocks = newVersion.getECBlocks();
    NewVersion.NewECB[] ecb = newECBlocks.getECBlocks();

    // debug 用
    System.out.println("従来の手法の誤り訂正能力");
    double normalECC = calcNormalECC(ecBlocks, numTotalCodewords);
    System.out.println("normalECC = " + normalECC);
    System.out.println();

    if (isSingleRSBlock(ecBlocks)) {
      System.out.println("既存手法(単一RSブロック)の誤り訂正能力");
      double exsistSingleECC = calcExsistSingleECC(kp, numTotalCodewords);
      System.out.println("exsistSingleECC = " + exsistSingleECC);
      System.out.println();
    }

    System.out.println("既存手法(複数RSブロック)の誤り訂正能力");
    double exsistMultiECCsub = calcExsistMultipleECC(maxNumDataBytes, kp, ecBlocks);
    double exsistMultiECC = exsistMultiECCsub != -1 ? exsistMultiECCsub : normalECC;
    System.out.println("exsistMultiECC = " + exsistMultiECC);
    System.out.println();

    System.out.println("提案手法の誤り訂正能力");
    double proposedECC = calcProposedECC(ecb, numTotalCodewords);
    System.out.println("proposedECC = " + proposedECC);
    System.out.println();

  }

  // 提案手法の誤り訂正能力を返すメソッド
  public static double calcProposedECC(NewVersion.NewECB[] ecb, int numTotalCodewords) {
    double ecc = 0;
    for (NewVersion.NewECB block : ecb) {
      // デバッグ用
      System.out.println(block.getCount() + "×(" + block.getCodewords() + "," + block.getDataCodewords() + ")" );
      // 誤り訂正可能な数
      int t = (block.getCodewords() - block.getDataCodewords()) / 2;
      // 誤り訂正可能な数の総和
      ecc += block.getCount() * t ;
    }
    // 誤り訂正可能な数の総和 / 符号長の総和
    ecc /= (double) numTotalCodewords;
    return ecc;
  }

  // 既存手法(単一RSブロック)の誤り訂正能力を返すメソッド
  // 既存手法を利用できないとき -1 を出力し通常のQRコードの求め方を利用する
  // この手法はその型番・誤り訂正レベルで適用可能なとき使用する
  public static double calcExsistSingleECC(int kp, int numTotalCodewords) {
    double ecc = 0;
    // デバッグ用
    System.out.println("(" + numTotalCodewords + "," + kp + ")");
    // 誤り訂正可能な数
    int t = (numTotalCodewords - kp) / 2;
    ecc = t / (double) numTotalCodewords;
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
          System.out.print(nkParam.get(0).get(i) + "×(");
          System.out.print(nkParam.get(1).get(i) + ",");
          System.out.println(nkParam.get(2).get(i) + ")");
          // 誤り訂正可能な数を求める
          int t = (nkParam.get(1).get(i) - nkParam.get(2).get(i)) / 2;
          // 誤り訂正可能な数の総和
          ecc += nkParam.get(0).get(i) * t;
        }
        // 誤り訂正可能な数の総和 / 符号長の総和
        ecc /= (double) (kp + p);
      } else {
        // デバッグ用
        System.out.println("(" + (kp + p) + "," + p + ")");
        // そのまま求める
        ecc = (p / 2) / (double) (kp + p);
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
      System.out.println(ecb.getCount() + "×(" + n + "," + ecb.getDataCodewords() + ")");
      // 誤り訂正可能な数の総和
      ecc += ecb.getCount() * t;
    }
    // 誤り訂正可能な数の総和 / 符号長の総和
    ecc /= (double) numTotalCodewords;
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
