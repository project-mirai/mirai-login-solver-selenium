/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


console.log("Backend working");

/**
 * @type {HttpHeader[]} headers
 * @type {string} header
 * @return {void}
 */
function setHeader(headers, header, value) {
    for (let i = 0; i < headers.length; i++) {
        let h = headers[i];
        if (h.name.toLowerCase() == header.toLowerCase()) {
            h.value = value
            return
        }
    }
    headers.push({
        name: header,
        value: value
    })
}

chrome.webRequest.onBeforeSendHeaders.addListener((details) => {
        let headers = details.requestHeaders;
        setHeader(headers, "X-Requested-With", "com.tencent.tim");
        setHeader(headers, "User-Agent", "Mozilla/5.0 (Linux; Android 7.1.1; MIUI ONEPLUS/A5000_23_17; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045426 Mobile Safari/537.36 V1_AND_SQ_8.3.9_0_TIM_D QQ/3.1.1.2900 NetType/WIFI WebP/0.3.0 Pixel/720 StatusBarHeight/36 SimpleUISwitch/0 QQTheme/1015712");
        return {requestHeaders: headers};
    },
    {urls: ["*://ssl.captcha.qq.com/**"]},
    ["requestHeaders", "blocking"]
);
