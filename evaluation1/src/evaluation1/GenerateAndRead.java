package evaluation1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class GenerateAndRead {

  public static void main(String[] args) {
    // 複数QRコードを生成するプログラム
    /* 手法によってjarファイルを変更する必要がある */

    int[] errorNum = {10, 50, 75, 100, 125, 150, 175, 200, 300, 400, 500, 1000};
//    int[] errorNum = {550, 600, 650, 700};
//    int[] errorNum = {350, 425, 450, 475, 550, 600, 650};
//    int[] errorNum = {100};

    // 入力：バージョン・誤り訂正レベルの指定
    int selectNumber = 3;
    String content = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
    int[] version = {8, 8, 15, 15};
    // QRコードの画像の幅，高さ，ピクセル数
//    int width = 125 + 10 * version[selectNumber];
    int width = 100;
    if (version[selectNumber] == 8) {
      width = 100;
    } else {
      width = 150;
    }
    int height = width;
    ErrorCorrectionLevel[] ecLevel = {ErrorCorrectionLevel.L, ErrorCorrectionLevel.H,
                                      ErrorCorrectionLevel.L, ErrorCorrectionLevel.H};

    // 入力：QRコードの画像数
    int qrcodeImageNum = 1000;
    // 入力：格納するディレクトリ名とそのファイル名の頭
    String directoryName = "evaluation1\\exsist\\15L\\15L";
//    String directoryName = "evaluation1\\debug\\8L\\8L";

    String output = "C:\\Research2020\\image\\" + directoryName;
    String outputPath = "";

    // 失敗した数を格納する配列
    int[] failedArray = new int[errorNum.length];
    int[] formatFailedArray = new int[errorNum.length];

    for (int k = 0; k < errorNum.length; k++) {
      Hashtable hints = new Hashtable();
      //QRコードのバージョンを決定
      hints.put(EncodeHintType.QR_VERSION, version[selectNumber]);
      //QRコードの誤り訂正レベルを決定
      hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel[selectNumber]);

      hints.put(EncodeHintType.ERROR_PROB, errorNum[k]);

      for (int i = 0; i < qrcodeImageNum; i++) {
        try {
          QRCodeWriter qrWriter = new QRCodeWriter();

          //QRCodeWriter#encode()には以下の情報を渡す
          // (1)エンコード対象の文字列、バーコードに埋め込みたい情報
          // (2)出力するバーコードの書式
          // (3)イメージの幅
          // (4)イメージの高さ
          BitMatrix bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

          BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

          outputPath = output + "-" +String.valueOf(i) + ".png";
          //エンコードで得られたイメージを画像ファイルに出力する
          ImageIO.write(image, "png", new File(outputPath));
//          System.out.println("出力に成功しました。");

        } catch (WriterException e) {
            System.err.println("[" + content + "] をエンコードするときに例外が発生.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[" + outputPath + "] を出力するときに例外が発生.");
            e.printStackTrace();
        }
      }

      // 復号失敗した数
      int failed = 0;
      int formatFailed = 0;

      for (int i = 0; i < qrcodeImageNum; i++) {
        String inputPath = output + "-" + String.valueOf(i) + ".png";
        //System.out.println(inputPath);
        try {
          //画像データを読み込む
          BufferedImage image = ImageIO.read(new File(inputPath));
          LuminanceSource source = new BufferedImageLuminanceSource(image);
          Binarizer binarizer = new HybridBinarizer(source);
          BinaryBitmap bitmap = new BinaryBitmap(binarizer);

          QRCodeReader reader = new QRCodeReader();

          Result result = reader.decode(bitmap);

//          System.out.println("result = " + result.getText());

        }  catch (NotFoundException e) {
          System.err.println("[" + inputPath + "] イメージの中にバーコードが見つからないためデコードで例外が発生.");
          e.printStackTrace();
          failed++;
        } catch (ChecksumException e) {
          System.err.println("[" + inputPath + "] バーコードが見つかったがチェックサム検査で例外が発生.");
          e.printStackTrace();
          failed++;
        } catch (FormatException e) {
          System.err.println("[" + inputPath + "] は書式不正のためデコードで例外が発生.");
          e.printStackTrace();
          failed++;
          formatFailed++;
        } catch (IOException e) {
          System.err.println("[" + inputPath + "] を読み込むときに例外が発生.");
          e.printStackTrace();
          failed++;
        }
      }
      failedArray[k] = failed;
      formatFailedArray[k] = formatFailed;
    }

    for (int i = 0; i < failedArray.length; i++) {
      System.out.println(1000 - failedArray[i]);
    }

    for (int i = 0; i < failedArray.length; i++) {
      System.out.println("formatFailed = " + formatFailedArray[i]);
    }
  }
}
