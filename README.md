# Uncannly-tts

The Web Speech API built into browsers doesn't do IPA, so I had to use Ivona.

So I had to build myself a tiny little Spark backend.

```
mvn clean package
cf push
```

To run locally:

```
export PORT=4567
java -cp target/tts-0.0.1-jar-with-dependencies.jar PhonemesToSpeech
```