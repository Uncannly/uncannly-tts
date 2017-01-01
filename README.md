# Uncannly-tts

The Web Speech API built into browsers doesn't do IPA, so I had to use Ivona.

So I had to build myself a tiny little Spark backend.

```
mvn clean package
cf push
```

To run locally:

```
java -cp target/tts-0.0.1-jar-with-dependencies.jar PhonemesToSpeech
```

Then you can GET `http://localhost:4567/pts?word={someIPA}`.

For it to work, you'll need to sign up for [Ivona](https://www.ivona.com/) and put your credentials into `src/main/resources/IvonaCredentials.properties` in this form:

```
accessKey = {your access key}
secretKey = {your secret key}
```