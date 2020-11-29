package evaluation1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class QRCodeReadeMultiple {

  public static void main(String[] args) {

    /* 手法によってjarファイルを変更する必要がある */

    // 入力：QRコードの画像数
    int qrcodeImageNum = 1000;

    // 入力：格納するディレクトリ名とそのファイル名の頭
    String directoryName = "evaluation1\\exsist\\8L\\8L";

    String input = "C:\\Research2020\\image\\" + directoryName + "-";
    String inputPath = "";

    // 復号失敗した数
    int failed = 0;
    for (int i = 0; i < qrcodeImageNum; i++) {
      inputPath = input + String.valueOf(i) + ".png";
      //System.out.println(inputPath);
      try {
        //画像データを読み込む
        BufferedImage image = ImageIO.read(new File(inputPath));
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(binarizer);

        QRCodeReader reader = new QRCodeReader();

        Result result = reader.decode(bitmap);

        System.out.println("result = " + result.getText());

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
      } catch (IOException e) {
        System.err.println("[" + inputPath + "] を読み込むときに例外が発生.");
        e.printStackTrace();
        failed++;
      }

//      catch(FormatException e) {
//        System.err.println("読み取り失敗");
//        failed++;
//      } catch (NotFoundException | ChecksumException | IOException e) {
//        System.err.println("復号失敗");
//        failed++;
//      }
    }
    System.out.println("成功確率 = " + (qrcodeImageNum - failed) + "/" + qrcodeImageNum);
  }

}
