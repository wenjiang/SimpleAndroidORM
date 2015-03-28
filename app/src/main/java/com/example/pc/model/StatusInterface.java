package com.example.pc.model;

import com.maomao.client.yong.annotations.Get;
import com.maomao.client.yong.annotations.Header;
import com.maomao.client.yong.annotations.Headers;
import com.maomao.client.yong.annotations.Path;

/**
 * Created by pc on 2015/3/13.
 */
public interface StatusInterface {
    @Get(value = "http://192.168.22.90/interface/microblog/public/<sinceId>/<isAdvance>/<count>", override = true)
    @Headers({"X-Requested-token: b9cc06e0f009f5780e8a58e84703c5a9"})
    public int allStatuses(@Path("sinceId") String sinceId,
                           @Path("isAdvance") boolean isAdvance,
                           @Path("count") int count,
                           @Header("X-Requested-networkId") String header);
}
