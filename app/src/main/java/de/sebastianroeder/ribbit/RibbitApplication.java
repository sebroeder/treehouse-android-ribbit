package de.sebastianroeder.ribbit;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseObject;

public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "Dk8PBxqXVncK0XogTB8Fhi2bdjvfQisi87u8W9Pw",
                "qAFCCTQOdTSn5PoHKBRJXuGAeYaLF05NPvXpDna8");
    }
}