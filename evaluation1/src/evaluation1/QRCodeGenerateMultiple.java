package evaluation1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeGenerateMultiple {

  public static void main(String[] args) {
    // 複数QRコードを生成するプログラム

    // QRコードの画像の幅，高さ
    int width = 200;
    int height = 200;
    String[] content = {"prichan", "good morning", "research is too hard."};
    int[] version = {4, 5, 6};
    ErrorCorrectionLevel[] ecLevel = {ErrorCorrectionLevel.L, ErrorCorrectionLevel.M,
                                      ErrorCorrectionLevel.Q};
    String output = "C:\\Research2020\\image\\test\\test";
    String outputPath = "";

    for (int i = 0; i < content.length; i++) {
      Hashtable hints = new Hashtable();
      //QRコードのバージョンを決定
      hints.put(EncodeHintType.QR_VERSION, version[i]);
      //QRコードの誤り訂正レベルを決定
      hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel[i]);

      try {
        QRCodeWriter qrWriter = new QRCodeWriter();

        //QRCodeWriter#encode()には以下の情報を渡す
        // (1)エンコード対象の文字列、バーコードに埋め込みたい情報
        // (2)出力するバーコードの書式
        // (3)イメージの幅
        // (4)イメージの高さ
        BitMatrix bitMatrix = qrWriter.encode(content[i], BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        outputPath = output + "-" +String.valueOf(i) + ".png";
        //エンコードで得られたイメージを画像ファイルに出力する
        ImageIO.write(image, "png", new File(outputPath));
        System.out.println("出力に成功しました。");

      } catch (WriterException e) {
          System.err.println("[" + content[i] + "] をエンコードするときに例外が発生.");
          e.printStackTrace();
      } catch (IOException e) {
          System.err.println("[" + outputPath + "] を出力するときに例外が発生.");
          e.printStackTrace();
      }
    }

  }

}
