package com.example.pc.sqlpratice;

import com.maomao.client.yong.Yong;
import com.maomao.client.yong.YongConfig;

import org.litepal.LitePalApplication;

/**
 * Created by pc on 2015/2/27.
 */
public class AppContext extends LitePalApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        YongConfig config = new YongConfig.Builder()
                .attachContext(this)
                .defaultAgent()
                .create();
        Yong.net(config);
    }
}
