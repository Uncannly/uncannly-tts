import java.io.BufferedOutputStream;
import java.io.InputStream;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.CreateSpeechResult;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Voice;
import com.ivona.services.tts.model.Parameters;
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
    	IvonaSpeechCloudClient speechCloud = new IvonaSpeechCloudClient(
        new ClasspathPropertiesFileCredentialsProvider("resources/IvonaCredentials.properties"));
      speechCloud.setEndpoint("https://tts.eu-west-1.ivonacloud.com");

      CreateSpeechRequest createSpeechRequest = new CreateSpeechRequest();

      Voice voice = new Voice();
      voice.setName("Salli");

      Parameters parameters = new Parameters();
      parameters.setRate("slow");

      Input input = new Input();
      input.setType("application/ssml+xml");
      String word = req.queryParams("word").split("\\(")[0];
      input.setData(String.format(
        "<speak><phoneme alphabet=\"ipa\" ph=\"%s\">_</phoneme></speak>\"", word
      ));

      createSpeechRequest.setVoice(voice);
      createSpeechRequest.setInput(input);
      createSpeechRequest.setParameters(parameters);

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