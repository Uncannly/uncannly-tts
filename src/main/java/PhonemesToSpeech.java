import java.io.BufferedOutputStream;
import java.io.InputStream;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Voice;
import static spark.Spark.*;

public class PhonemesToSpeech {
  public static void main(String[] args) {
    port(Integer.parseInt(System.getenv("PORT")));

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
    	IvonaSpeechCloudClient speechCloud = new IvonaSpeechCloudClient(
        new ClasspathPropertiesFileCredentialsProvider("resources/IvonaCredentials.properties"));
      speechCloud.setEndpoint("https://tts.eu-west-1.ivonacloud.com");
      String outputFileName = "./speech.mp3";
      CreateSpeechRequest createSpeechRequest = new CreateSpeechRequest();
      Input input = new Input();
      Voice voice = new Voice();
      voice.setName("Salli");
      input.setType("application/ssml+xml");
      input.setData(String.format("<speak><phoneme alphabet=\"ipa\" ph=\"%s\">_</phoneme></speak>\"", req.queryParams("word")));
      createSpeechRequest.setInput(input);
      createSpeechRequest.setVoice(voice);
      InputStream in = null;
      BufferedOutputStream outputStream = null;
      res.raw().setContentType("application/octet-stream");
      try {
        CreateSpeechResult createSpeechResult = speechCloud.createSpeech(createSpeechRequest);
        in = createSpeechResult.getBody();
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