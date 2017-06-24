import java.io.BufferedOutputStream;
import java.io.InputStream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

import static spark.Spark.*;

public class PhonemesToSpeech {
  public static void main(String[] args) {
    port(Integer.parseInt(System.getenv("PORT") == null ? "4567" : System.getenv("PORT")));

  	options("/*", (req, res) -> {
      String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }
      String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
          res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }
      return "OK";
    });

		before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

    get("/pts", (req, res) -> {
      AmazonPollyClient polly = new AmazonPollyClient(
        new DefaultAWSCredentialsProviderChain(), 
        new ClientConfiguration()
        );
      polly.setRegion(Region.getRegion(Regions.US_EAST_1));
      
      DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
      DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
      Voice voice = describeVoicesResult.getVoices().get(0);

      String word = req.queryParams("word").split("\\(")[0];
      String formattedWord = String.format(
        "<speak><phoneme alphabet=\"ipa\" ph=\"%s\">_</phoneme></speak>", word
      );

      SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest()
        .withTextType("ssml")
        .withText(formattedWord)
        .withVoiceId(voice.getId())
        .withOutputFormat(OutputFormat.Mp3);
      SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

      InputStream in = synthRes.getAudioStream();

      BufferedOutputStream outputStream = null;
      res.raw().setContentType("application/octet-stream");
      try {
        outputStream = new BufferedOutputStream(res.raw().getOutputStream());
        byte[] buffer = new byte[2 * 1024];
        int readBytes;
        while ((readBytes = in.read(buffer)) > 0) {
          outputStream.write(buffer, 0, readBytes);
        }
      } finally {
        if (in != null) {
          in.close();
        }
        if (outputStream != null) {
          outputStream.close();
        }
      }
      return res.raw();
    });
  }
}