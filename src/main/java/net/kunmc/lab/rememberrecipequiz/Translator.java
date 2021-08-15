package net.kunmc.lab.rememberrecipequiz;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Translator
{
    private static Map<String, String> config;
    private static boolean isInitialized = false;

    public static void initialize()
    {

        try (InputStreamReader reader = new InputStreamReader(RememberRecipeQuiz.class.getResourceAsStream(
                "/ja_JP.json"), StandardCharsets.UTF_8))
        {
            config = new Gson().fromJson(reader, new TypeToken<LinkedHashMap<String, String>>(){}.getType());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            isInitialized = false;
        }

        isInitialized = true;
    }

    public static String get(String key)
    {
        if (!isInitialized)
        {
            initialize();
            return get(key);
        }

        return config.get(key);
    }

}
